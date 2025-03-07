package com.streamify.Storage;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;

@RestController
@RequestMapping("/media")
public class MediaController {
    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/users/{username}/profile/image")
    public ResponseEntity<Resource> getUserProfileImage(
            @PathVariable("username") String username
    ) throws MalformedURLException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mediaService.getUserProfileImageByUsername(username));
    }

    @GetMapping("/post/preview/{post-id}")
    public ResponseEntity<Resource> getPostPreviewImage(
            @PathVariable("post-id") String postId
    ) throws IOException, InterruptedException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mediaService.getPostPreviewImage(postId));
    }

    @GetMapping("/post/{post-media-id}")
    public  ResponseEntity<Resource> getPostContent(
            @PathVariable("post-media-id") String postMediaId
    ) throws MalformedURLException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mediaService.getPostContent(postMediaId));
    }

    @GetMapping("/story/{story-id}")
    public ResponseEntity<Resource> getStoryContent(
            @PathVariable("story-id") String storyId
    ) throws MalformedURLException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(mediaService.getStoryContent(storyId));
    }

}
