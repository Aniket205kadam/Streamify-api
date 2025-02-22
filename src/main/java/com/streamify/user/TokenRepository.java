package com.streamify.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, String> {
    @Query("""
            SELECT token
            FROM Token token
            WHERE token.token = :token
            AND token.user.username = :username
            """)
    Optional<Token> findByToken(@Param("username") String username, @Param("token") String token);
}
