package com.streamify.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    @Query("""
            SELECT comment
            FROM Comment comment
            WHERE comment.post.id = :postId
            AND comment.status = 'ACTIVE'
            """)
    Page<Comment> findAllCommentByPostId(Pageable pageable, @Param("postId") String postId);

    @Query("""
            SELECT comment
            FROM Comment comment
            WHERE comment.parentComment.id = :commentId
            AND comment.status = 'ACTIVE'
            """)
    Page<Comment> findAllCommentReplies(Pageable pageable, String commentId);
}
