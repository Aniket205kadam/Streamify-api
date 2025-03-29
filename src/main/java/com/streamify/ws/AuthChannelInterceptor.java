package com.streamify.ws;

import com.streamify.exception.OperationNotPermittedException;
import com.streamify.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
    private JwtService jwtService;
    private UserDetailsService userDetailsService;

    @Autowired
    public void getJwtService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Autowired
    public void getUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    @Override
    public Message<?> preSend(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel
    ) {
        System.out.println("Try to connected");
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null)
            throw new IllegalArgumentException("Accessor must not be null!");
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SUBSCRIBE.equals(accessor.getCommand())
        ) {
            String token = getTokenFromHeader(accessor);
            String identifier = jwtService.extractIdentifier(token);
            if (StringUtils.hasText(token) && StringUtils.hasText(identifier) &&
                            jwtService.isTokenValid(token, userDetailsService.loadUserByUsername(identifier))
            ) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                accessor.setUser(authentication);
            } else {
                throw new OperationNotPermittedException("Invalid or missing JWT token");
            }
        }
        return message;
    }

    private String getTokenFromHeader(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
