package com.streamify.story;

import com.streamify.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("stories")
@Tag(name = "Story")
public class StoryController {
    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    @PostMapping
    public ResponseEntity<?> addStory(
            @RequestParam("caption")
            @Size(max = 255, message = "Caption must be at most 255 characters")
            String caption,
            Authentication connectedUser,
            @RequestPart("content")
            @NotNull(message = "Content file must not be null")
            MultipartFile content
    ) throws IOException, InterruptedException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(storyService.addStory(caption, false, content, connectedUser));
    }

    @GetMapping("/{story-id}")
    public ResponseEntity<StoryResponse> addStory(
            @PathVariable("story-id") String storyId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storyService.findStoryById(storyId));
    }

    @PatchMapping("/{story-id}/viewers/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void viewStory(
            @PathVariable("story-id") String storyId,
            @PathVariable("user-id") String viewerId
    ) {
        storyService.viewStory(storyId, viewerId);
    }

    @PostMapping("/{story-id}/replies")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendReplyToStory(
            @RequestParam("content")
            @NotNull(message = "Reply must not be null")
            @NotEmpty(message = "Reply must not be empty")
            @Size(message = "Reply must be at most 500 characters")
            String content,
            @PathVariable("story-id")
            String storyId,
            Authentication connectedUser
    ) {
        storyService.sendReplyToStory(content, storyId, connectedUser);
    }

    @GetMapping("/{story-id}/replies")
    public ResponseEntity<PageResponse<StoryReplyResponse>> findAllStoryReplies(
            @PathVariable("story-id") String storyId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storyService.findAllStoryReplies(storyId, connectedUser, page, size));
    }

    @PutMapping("/{story-id}")
    public ResponseEntity<String> updateStory(
            @PathVariable("story-id") String storyId,
            @RequestParam("caption")
            @Size(max = 255, message = "Caption must be at most 255 characters")
            String caption,
            @RequestParam("isArchived")
            boolean isArchived,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(storyService.updateStory(storyId, caption, isArchived, connectedUser));
    }

    @DeleteMapping("/{story-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStory(
            @PathVariable("story-id") String storyId,
            Authentication connectedUser
    ) throws IOException {
        storyService.deleteStoryById(storyId, connectedUser);
    }

    @GetMapping("/get/followers")
    public ResponseEntity<List<FollowingsStoryResponse>> getFollowersStories(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storyService.findAllFollowingsStories(connectedUser));
    }
}
