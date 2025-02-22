package com.streamify.comment;

import com.streamify.common.Mapper;
import com.streamify.common.PageResponse;
import com.streamify.exception.OperationNotPermittedException;
import com.streamify.post.Post;
import com.streamify.post.PostRepository;
import com.streamify.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final Mapper mapper;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, Mapper mapper) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.mapper = mapper;
    }

    private Post findPostById(@NonNull String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("The post is not found with ID: " + postId));
    }

    public String sendCommentOnPost(String postId, String content, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Post post = findPostById(postId);
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .status(CommentStatus.ACTIVE)
                .build();
        return commentRepository.save(comment).getId();
    }

    private Comment findCommentById(@NonNull String commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment is not found with ID: " + commentId));
    }

    public String updateComment(String commentId, Authentication connectedUser, String content) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        if (!(comment.getUser().getId().equals(user.getId()))) {
            throw new OperationNotPermittedException("You don't have the authority to delete the comment");
        }
        comment.setContent(content);
        return commentRepository.save(comment).getId();
    }

    public String likeComment(String commentId, Authentication connectedUser) {
        // todo - prevent the user to like multiple time
        Comment comment = findCommentById(commentId);
        comment.setLikes(comment.getLikes() + 1);
        return commentRepository.save(comment).getId();
    }

    public String unlikeComment(String commentId, Authentication connectedUser) {
        // todo - prevent the user to unlike multiple time
        Comment comment = findCommentById(commentId);
        comment.setLikes(comment.getLikes() - 1);
        return commentRepository.save(comment).getId();
    }

    public Boolean deleteComment(String commentId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        if (!comment.getUser().getId().equals(user.getId())) {
            throw new OperationNotPermittedException("You don't have to authority to delete the comment");
        }
        commentRepository.deleteById(comment.getId());
        return true;
    }

    public String sendReplyToComment(String postId, String commentId, String content, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Comment comment = findCommentById(commentId);
        System.out.println("Comment: " + comment.getId());
        Comment reply = Comment.builder()
                .content(content)
                .user(user)
                .parentComment(comment)
                .status(CommentStatus.ACTIVE)
                .build();
        commentRepository.save(reply);
        if (!comment.getReplies().isEmpty()) {
            List<Comment> replies = comment.getReplies();
            replies.add(reply);
        } else {
            comment.setReplies(List.of(reply));
        }
        return commentRepository.save(comment).getId();
    }

    public PageResponse<CommentResponse> getAllPostComment(String postId, int page, int size) {
        Post post = findPostById(postId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findAllCommentByPostId(pageable, post.getId());
        List<CommentResponse> commentResponses = comments.stream()
                .map(mapper::toCommentResponse)
                .toList();
        return PageResponse.<CommentResponse>builder()
                .content(commentResponses)
                .number(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
    }

    public PageResponse<CommentResponse> getAllCommentReplies(String commentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findAllCommentReplies(pageable, commentId);
        List<CommentResponse> commentResponses = comments.stream()
                .map(mapper::toCommentRelyResponse)
                .toList();
        return PageResponse.<CommentResponse>builder()
                .content(commentResponses)
                .number(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .build();
    }
}
