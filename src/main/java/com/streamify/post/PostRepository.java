package com.streamify.post;

import com.streamify.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    @Query("""
            SELECT post
            FROM Post post
            LEFT JOIN FETCH post.likes
            WHERE post.id = :postId
            """)
    Optional<Post> findWithLikesDetailsById(@Param("postId") String postId);

    @Query("""
        SELECT post
        FROM Post post
        WHERE post.user.id = :userId
        AND post.isArchived = false
        AND post.visibility = :visibility
        """)
    Page<Post> findAllDisplayablePosts(Pageable pageable, @Param("userId") String userId, @Param("visibility") PostVisibility visibility);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            AND post.isReel = true
            AND post.isArchived = false
            AND post.visibility = :visibility
            """)
    Page<Post> findAllDisplayableReels(Pageable pageable, @Param("userId") String userId, @Param("visibility") PostVisibility visibility);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            """)
    Page<Post> findAllMyPosts(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.id IN :savedPostIds
            """)
    Page<Post> findAllMySavedPosts(Pageable pageable, @Param("savedPostIds") List<String> savedPostIds);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id = :userId
            AND post.isReel = true
            """)
    Page<Post> findAllMyReels(Pageable pageable, @Param("userId") String userId);

    @Query("""
            SELECT post
            FROM Post post
            WHERE :user MEMBER OF post.user.followers
            """)
    Page<Post> findAllFollowingsPosts(Pageable pageable, @Param("user") User user);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.isReel = true
            """)
    Page<Post> findAllReels(Pageable pageable);

    @Query("""
            SELECT post
            FROM Post post
            WHERE post.user.id != :userId
            """)
    List<Post> findSuggestedPosts(String userId);
}
