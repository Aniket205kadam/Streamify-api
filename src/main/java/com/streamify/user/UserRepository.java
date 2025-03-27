package com.streamify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Boolean existsByEmail(String email);

    @Query("""
            SELECT CASE WHEN COUNT(f) > 0 THEN TRUE ELSE FALSE END
            FROM User user JOIN user.following f
            WHERE user.id = :userId
            AND f.username = :username
            """)
    boolean isFollowing(@Param("userId") String userId, @Param("username") String username);

    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(user.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(user.email) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> searchUsers(Pageable pageable, @Param("query") String query);

    @Query("""
            SELECT user
            FROM User user
            LEFT JOIN FETCH user.followers
            WHERE user.id = :userId
            """)
    Optional<User> findFollowersWithDetails(@Param("userId") String userId);

    @Query("""
            SELECT user
            FROM User user
            LEFT JOIN FETCH user.following
            WHERE user.id = :userId
            """)
    Optional<User> findFollowingsWithDetails(@Param("userId") String userId);

    @Query("""
            SELECT user
            FROM User user
            JOIN FETCH user.recentSearchedUser
            WHERE user.id = :userId
            """)
    List<User> findRecentSearchesWithDetails(@Param("userId") String userId);

    @Query("""
            SELECT user
            FROM User user
            LEFT JOIN FETCH user.savedPost
            WHERE user.id = :userId
            """)
    Optional<User> findUserWithSavedPostDetailsById(@Param("userId") String userId);

    @Query("""
            SELECT user
            FROM User user
            ORDER BY SIZE(user.followingCount) DESC
            """)
    Page<User> findMostFollowingCountUsers(Pageable pageable);
}
