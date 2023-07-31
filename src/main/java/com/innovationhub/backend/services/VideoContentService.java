package com.innovationhub.backend.services;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.security.auth.login.AccountNotFoundException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
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

    public void processVideo(MultipartFile video, String username) throws AccountNotFoundException {
        ContentPortfolio portfolio = getPortfolio(username);
        if (portfolio.getId() == null) {
            portfolio = contentPortfolioRepository.save(portfolio);
        }
        VideoAttribute processedVideo = sendVideoToAiEngine(video);
        processedVideo.setContentPortfolio(portfolio);
        processedVideo = videoAttributeRepository.save(processedVideo);
        portfolio.getVideos().add(processedVideo);
        contentPortfolioRepository.save(portfolio);
    }

    public VideoAttribute sendVideoToAiEngine(MultipartFile video) throws VideoProcessException {
        /*WebClient client = buildClient();
        List response = client.post()
                .uri("/")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(video)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                    throw new VideoProcessException("Unknown error while processing the video");
                })
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    throw new VideoProcessException("Unknown error while processing the video");
                })
                .bodyToMono(List.class)
                .timeout(Duration.ofSeconds(10000))
                .block();
        assert response != null;

        List<String> bestFrames = new ArrayList<>(response);*/
        return VideoAttribute.builder()
                .videoName(video.getName())
                //.bestFrames(bestFrames)
                .bestFrames(new ArrayList<>())
                .build();
    }
    public WebClient buildClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        WebClient client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:8000/api")
                .build();

        return client;
    }
}
