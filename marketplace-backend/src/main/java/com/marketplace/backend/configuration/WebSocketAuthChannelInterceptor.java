package com.marketplace.backend.configuration;

import com.marketplace.backend.security.CustomUserDetails;
import com.marketplace.backend.security.CustomUserDetailsService;
import com.marketplace.backend.security.jwt.JwtService;
import com.marketplace.backend.service.ChatAccessGuard;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CHAT_TOPIC = Pattern.compile("^/topic/chat/([^/]+)$");
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final ChatAccessGuard chatAccessGuard;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        return switch (command) {
            case CONNECT   -> handleConnect(accessor, message);
            case SUBSCRIBE -> handleSubscribe(accessor, message);
            case SEND      -> handleSend(accessor, message);
            default        -> message;
        };
    }

    private Message<?> handleConnect(StompHeaderAccessor accessor, Message<?> message) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            log.debug("WS CONNECT rejected: missing or malformed Authorization header");
            return null;
        }

        String token = authHeader.substring(BEARER.length());
        Optional<Claims> claims = jwtService.validateAndExtract(token);
        if (claims.isEmpty()) {
            log.debug("WS CONNECT rejected: invalid or expired token");
            return null;
        }
        if (!JwtService.TokenType.ACCESS.name().equals(claims.get().get("type", String.class))) {
            log.debug("WS CONNECT rejected: token type is not ACCESS");
            return null;
        }

        try {
            CustomUserDetails user = userDetailsService.loadUserByUsername(claims.get().getSubject());
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities()
            );
            accessor.setUser(auth);
            return message;
        } catch (Exception e) {
            log.debug("WS CONNECT rejected: {}", e.getMessage());
            return null;
        }
    }

    private Message<?> handleSubscribe(StompHeaderAccessor accessor, Message<?> message) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            log.debug("WS SUBSCRIBE rejected: no authenticated principal");
            return null;
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        Matcher m = CHAT_TOPIC.matcher(destination);
        if (m.matches()) {
            String chatId = m.group(1);
            String userId = extractUserId(principal);
            if (userId == null) {
                log.debug("WS SUBSCRIBE rejected: cannot extract userId from principal");
                return null;
            }
            if (!chatAccessGuard.isParticipant(chatId, userId)) {
                log.debug("WS SUBSCRIBE rejected: user {} is not a participant of chat {}", userId, chatId);
                return null;
            }
        }

        return message;
    }

    private Message<?> handleSend(StompHeaderAccessor accessor, Message<?> message) {
        if (accessor.getUser() == null) {
            log.debug("WS SEND rejected: no authenticated principal");
            return null;
        }
        return message;
    }

    private String extractUserId(Principal principal) {
        if (principal instanceof Authentication auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getUser().getId();
        }
        return null;
    }
}
