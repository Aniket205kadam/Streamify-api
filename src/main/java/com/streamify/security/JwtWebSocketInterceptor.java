package com.streamify.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.Optional;

public class JwtWebSocketInterceptor implements HandshakeInterceptor {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtWebSocketInterceptor(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        System.out.println("Handshake request is processing...");

        if (request instanceof ServletServerHttpRequest serverHttpRequest) {
            System.out.println("Request is an instance of ServletServerHttpRequest");

            String token = extractToken(serverHttpRequest);
            System.out.println("Extracted token: " + token);

            if (token != null) {
                String identifier = jwtService.extractIdentifier(token);
                System.out.println("Extracted identifier: " + identifier);

                if (identifier != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
                        System.out.println("Loaded user details: " + userDetails.getUsername());

                        if (jwtService.isTokenValid(token, userDetails)) {
                            Claims claims = jwtService.extractAllClaims(token);
                            attributes.put("username", claims.getSubject());
                            System.out.println("Handshake successful for user: " + claims.getSubject());
                            return true;
                        } else {
                            System.out.println("Token is invalid or expired");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to load user details: " + e.getMessage());
                    }
                } else {
                    System.out.println("Identifier is null");
                }
            } else {
                System.out.println("Token is null");
            }
        } else {
            System.out.println("Request is not an instance of ServletServerHttpRequest");
        }

        return false;
    }

    private String extractToken(ServletServerHttpRequest request) {
        return Optional.ofNullable(request.getServletRequest().getHeader("Authorization"))
                .filter(authHeader -> authHeader.startsWith("Bearer "))
                .map(authHeader -> authHeader.substring(7))
                .orElse(null);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {}
}