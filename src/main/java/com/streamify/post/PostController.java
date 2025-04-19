package com.streamify.post;

import com.streamify.comment.CommentResponse;
import com.streamify.comment.CommentService;
import com.streamify.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("posts")
@Tag(name = "Post")
public class PostController {
    private final PostService postService;
    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;

    public PostController(PostService postService, CommentService commentService, SimpMessagingTemplate messagingTemplate) {
        this.postService = postService;
        this.commentService = commentService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/contents")
    public ResponseEntity<String> storedContents(
            Authentication connectedUser,
            @RequestPart("contents") MultipartFile... contents
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.uploadPostContent(connectedUser, contents));
    }

    @PostMapping("/{post-id}/meta-data")
    public ResponseEntity<String> savePostMetaData(
            @RequestBody @Valid PostRequest request,
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.uploadPostMetaData(request, postId, connectedUser));
    }

    @PatchMapping("/{post-id}/hide/like-count")
    public ResponseEntity<String> updateHideLikeCount(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updateHideLikeCount(postId, connectedUser));
    }

    @PatchMapping("/{post-id}/hide/commenting")
    public ResponseEntity<String> updateCommenting(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updateCommenting(postId, connectedUser));
    }

    @GetMapping("/{post-id}")
    public ResponseEntity<PostResponse> getPostById(
            @PathVariable("post-id") String postId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getPostById(postId));
    }

    @PutMapping("/{post-id}")
    public ResponseEntity<PostResponse> updatePost(
            @RequestBody @Valid PostRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.updatePost(request, connectedUser));
    }

    @DeleteMapping("/{post-id}")
    public ResponseEntity<Boolean> deletePostById(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) throws IOException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.deletePostById(postId, connectedUser));
    }

    /*@MessageMapping("/{post-id}/like")
    public void likePost(
            @DestinationVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        System.out.println("Someone try to like the post");
        Integer likeCount = postService.likePost(postId, connectedUser);
        messagingTemplate.convertAndSend(
                 "/topic/posts/likes/" + postId,
                likeCount
        );
    }
*/

    @PatchMapping("/{post-id}/like")
    public ResponseEntity<Integer> likePost(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) throws IOException, InterruptedException {
         return ResponseEntity
                 .status(HttpStatus.OK)
                 .body(postService.likePost(postId, connectedUser));
    }

    @PatchMapping("{post-id}/save")
    public ResponseEntity<?> savePost(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
        postService.savePost(postId, connectedUser);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/{post-id}/isLiked")
    public ResponseEntity<Boolean> isLikedPost(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
       return ResponseEntity
               .status(HttpStatus.OK)
               .body(postService.isLikeThisPost(postId, connectedUser));
    }
    @GetMapping("/{post-id}/isSaved")
    public ResponseEntity<Boolean> isSavedPost(
            @PathVariable("post-id") String postId,
            Authentication connectedUser
    ) {
       return ResponseEntity
               .status(HttpStatus.OK)
               .body(postService.isSavedPost(postId, connectedUser));
    }

    @PostMapping("/{post-id}/comment")
    public ResponseEntity<String> sendCommentOnPost(
            @PathVariable("post-id") String postId,
            @RequestParam("content") @Size(max = 22000, message = "Comment must not exceed 2200 characters")
            String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.sendCommentOnPost(postId, content, connectedUser));
    }

    @PatchMapping("/comment/{comment-id}")
    public ResponseEntity<String> updateComment(
            @PathVariable("comment-id") String commentId,
            @RequestParam("content") @Size(max = 22000, message = "Comment must not exceed 2200 characters")
            String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.updateComment(commentId, connectedUser, content));
    }

    @PatchMapping("/comment/{comment-id}/like")
    public ResponseEntity<Integer> likeComment(
            @PathVariable("comment-id") String commentId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.likeComment(commentId, connectedUser));
    }

    @DeleteMapping("/comment/{comment-id}")
    public ResponseEntity<Boolean> deleteCommentById(
            @PathVariable("comment-id") String commentId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.deleteComment(commentId, connectedUser));
    }

    @PostMapping("/{post-id}/comment/{comment-id}/reply")
    public ResponseEntity<String> sendReplyToComment(
            @PathVariable("post-id") String postId,
            @PathVariable("comment-id") String commentId,
            @RequestParam("content") String content,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentService.sendReplyToComment(postId, commentId, content, connectedUser));
    }

    @GetMapping("/{post-id}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> getAllPostComments(
            @PathVariable("post-id") String postId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.getAllPostComment(postId, page, size));
    }

    @GetMapping("/comment/{comment-id}/isLiked")
    public ResponseEntity<Boolean> isLikedComment(
            @PathVariable("comment-id") String commentId,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.isLikedComment(commentId, connectedUser));
    }

    @GetMapping("/{comment-id}/replies")
    public ResponseEntity<PageResponse<CommentResponse>> getALLCommentReplies(
            @PathVariable("comment-id") String commentId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(commentService.getAllCommentReplies(commentId, page, size));
    }

    @GetMapping("/followings-posts")
    public ResponseEntity<PageResponse<PostResponse>> followingsPost(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.findFollowingsPosts(connectedUser, page, size));
    }

    @GetMapping("/explore")
    public ResponseEntity<PageResponse<PostResponse>> getExploreContent(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getSuggestedContent(page, size, connectedUser));
    }

    @GetMapping("/reels")
    public ResponseEntity<PageResponse<PostResponse>> getReels(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(postService.getReels(page, size, connectedUser));
    }
}
