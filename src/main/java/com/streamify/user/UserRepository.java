package com.streamify.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    Boolean existsByEmail(String email);

    @Query("""
            SELECT user
            FROM User user
            WHERE LOWER(user.fullName) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(user.username) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(user.email) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<User> searchUsers(Pageable pageable, @Param("query") String query);
}
