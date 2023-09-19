package com.innovationhub.backend.controllers;

import com.innovationhub.backend.models.ContentPortfolio;
import com.innovationhub.backend.models.VideoAttribute;
import com.innovationhub.backend.services.VideoContentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountNotFoundException;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/content")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Tag(name = "Video Content", description = "The Video Content API. " +
        "Contains all the operations that can be performed on video content of a user.")
public class VideoContentController {

    private VideoContentService videoContentService;
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadVideo(
            @Parameter(
                description = "Files to be uploaded",
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "video") MultipartFile video, Authentication authentication) throws AccountNotFoundException, IOException {
        videoContentService.processVideo(video, authentication.getName());
        return ResponseEntity.ok("Your uploaded video is being processed");
    }

    @GetMapping
    public ResponseEntity<List<VideoAttribute>> fetchPortfolioVideo(Authentication authentication)
            throws AccountNotFoundException {
        return ResponseEntity.ok(videoContentService.getVideos(authentication.getName()));
    }

    @DeleteMapping(value = "/{videoId}")
    public ResponseEntity<?> deleteVideo(@PathVariable("videoId") Long videoId) {
        videoContentService.deleteVideo(videoId);
        return ResponseEntity.noContent().build();
    }
}
