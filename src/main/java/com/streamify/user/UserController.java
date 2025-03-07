package com.streamify.user;

import com.streamify.common.PageResponse;
import com.streamify.post.PostResponse;
import com.streamify.post.PostService;
import com.streamify.story.StoryResponse;
import com.streamify.story.StoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@Tag(name = "User")
public class UserController {
    private final PostService postService;
    private final StoryService storyService;
    private final UserService userService;

    public UserController(PostService postService, StoryService storyService, UserService userService) {
        this.postService = postService;
        this.storyService = storyService;
        this.userService = userService;
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(
            @PathVariable("username")
            @NotNull
            @NotEmpty
            String username
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserByUsername(username));
    }

    @GetMapping("/suggestedUsers")
    public ResponseEntity<PageResponse<UserDto>> getSuggestedUsers(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "5", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getSuggestedUsers2(page, size, connectedUser));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<UserDto>> searchUsers(
            @RequestParam("query") String query,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "5", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.searchUsers(query, page, size, connectedUser));
    }

    @PostMapping("/search/recent")
    public ResponseEntity<?> addRecentSearch(
            @RequestParam("username") String username,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.addRecentSearch(username, connectedUser));
    }

    @GetMapping("/search/recent")
    public ResponseEntity<List<UserDto>> getRecentSearch(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getRecentSearch(connectedUser));
    }

    @PatchMapping("/follow/{following-id}")
    public ResponseEntity<String> followUser(
            @PathVariable("following-id") String followingId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.followUser(followingId, connectedUser));
    }

    @GetMapping("/isFollowing/{user-id}")
    public ResponseEntity<Boolean> isFollowingUser(
            @PathVariable("user-id") String userId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.isFollowingUser(userId, connectedUser));
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<User> getUserById(
            @PathVariable("user-id") String userId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserById(userId));
    }

    @GetMapping("/followers")
    public ResponseEntity<List<UserDto>> findMyFollowers(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserFollowers(connectedUser));
    }

    @GetMapping("/followings")
    public ResponseEntity<List<UserDto>> findMyFollowings(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findUserFollowings(connectedUser));
    }

    @GetMapping("/{user-id}/posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllPostByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllPostsByUserId(page, size, userId));
    }

    @GetMapping("/{user-id}/reels")
    public ResponseEntity<PageResponse<PostResponse>> getAllReelsByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllReelsByUserId(page, size, userId));
    }

    @GetMapping("/{user-id}/saved-posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllSavedPostsByUser(
            @PathVariable("user-id") String userId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllSavedPostsByUser(page, size, userId));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<PageResponse<PostResponse>> getAllMyPost(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllMyPost(page, size, connectedUser));
    }

    @GetMapping("/my-reels")
    public ResponseEntity<PageResponse<PostResponse>> getAllMyReel(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getAllMyReel(page, size, connectedUser));
    }

    @GetMapping("/{username}/stories")
    public ResponseEntity<List<StoryResponse>> findStoryByUser(
            @PathVariable("username") String username
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(storyService.findStoriesByUser(username));
    }

    @GetMapping("/has-story")
    public ResponseEntity<Boolean> connectedUserHasStory(
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.connectedUserHasStory(connectedUser)
                );
    }

    @GetMapping("/{username}/about-account")
    public ResponseEntity<AboutAccount> getAboutInfo(
            @PathVariable("username") String username
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findAboutInfo(username));
    }
}
