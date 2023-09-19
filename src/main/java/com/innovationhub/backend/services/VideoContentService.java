package com.innovationhub.backend.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.innovationhub.backend.exception.ResourceNotFoundException;
import com.innovationhub.backend.exception.VideoProcessException;
import com.innovationhub.backend.models.ContentPortfolio;
import com.innovationhub.backend.models.User;
import com.innovationhub.backend.models.VideoAttribute;
import com.innovationhub.backend.repositories.ContentPortfolioRepository;
import com.innovationhub.backend.repositories.UserRepository;
import com.innovationhub.backend.repositories.VideoAttributeRepository;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.security.auth.login.AccountNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Log4j2
@AllArgsConstructor
public class VideoContentService {
    private ContentPortfolioRepository contentPortfolioRepository;
    private UserRepository userRepository;
    private VideoAttributeRepository videoAttributeRepository;
    public ContentPortfolio getPortfolio(String username) throws AccountNotFoundException {
        if (userRepository.findUserByUsername(username).isEmpty()) {
            throw new AccountNotFoundException("Username not found");
        }
        User user = userRepository.findUserByUsername(username).get();
        if (contentPortfolioRepository.findContentPortfolioByUserId(user.getId()).isEmpty()) { // no content uploaded
            return ContentPortfolio.builder()
                    .user(user)
                    .videos(new ArrayList<>())
                    .build();
        } else {
            return contentPortfolioRepository.findContentPortfolioByUserId(user.getId()).get();
        }
    }
    public List<VideoAttribute> getVideos(String username) throws AccountNotFoundException {
        ContentPortfolio contentPortfolio = getPortfolio(username);
        return contentPortfolio.getVideos();
    }

    public void deleteVideo(Long videoId) {
        if (!videoAttributeRepository.existsById(videoId)) {
            throw new ResourceNotFoundException(String.format("Video with id - %s does not exist", videoId));
        }
        videoAttributeRepository.deleteById(videoId);
    }

    public void processVideo(MultipartFile video, String username) throws AccountNotFoundException, IOException {
        ContentPortfolio portfolio = getPortfolio(username);
        if (portfolio.getId() == null) {
            portfolio = contentPortfolioRepository.save(portfolio);
        }
        Mono<String> response = sendVideoToAiEngine(video);
        ContentPortfolio finalPortfolio = portfolio;
        response.subscribe(res -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(res);
                BestFrameExtractionResult result = objectMapper.treeToValue(jsonNode, BestFrameExtractionResult.class);

                List<String> bestFrames = new ArrayList<>(result.keyframes);
                VideoAttribute processedVideo = VideoAttribute.builder()
                        .videoName(video.getOriginalFilename())
                        .bestFrames(String.join("#", bestFrames))
                        .contentPortfolio(finalPortfolio)
                        .build();
                videoAttributeRepository.save(processedVideo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<String> sendVideoToAiEngine(MultipartFile video) throws VideoProcessException, IOException {
        WebClient client = buildClient();

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("video", new ByteArrayResource(video.getBytes()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"video\"; filename=\"" + video.getOriginalFilename() + "\"");

        return client.post()
                .uri("/keyframes")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    throw new VideoProcessException("Unknown error while processing the video");
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    throw new VideoProcessException("Unknown error while processing the video");
                })
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10000));
    }
    public WebClient buildClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500000)
                .responseTimeout(Duration.ofMillis(500000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(500000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(500000, TimeUnit.MILLISECONDS)));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder().codecs(
                                clientCodecConfigurer ->
                                        clientCodecConfigurer.defaultCodecs().maxInMemorySize(100000000)).build())
                .baseUrl("http://localhost:5000/api")
                .build();

        return client;
    }
    static class BestFrameExtractionResult {
        @JsonProperty("keyframes") // Specify the JSON key
        List<String> keyframes = new ArrayList<>();
    }
}
