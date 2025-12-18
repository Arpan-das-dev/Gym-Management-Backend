package com.gym.authservice.Config.Jwt;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT authentication filter for Spring MVC–based applications.
 * <p>
 * <b>⚠ IMPORTANT NOTE:</b>
 * This filter is <b>NOT used in the current application</b>.
 * </p>
 *
 * <p>
 * The project was originally built using the traditional Spring MVC (Servlet)
 * security model. At that time, this {@link WebFilter}-based JWT authentication
 * mechanism was sufficient for extracting JWT tokens, validating them, and
 * populating the {@link org.springframework.security.core.context.SecurityContext}.
 * </p>
 *
 * <p>
 * However, after introducing an <b>API Gateway</b> and migrating the application
 * to a <b>reactive (Spring WebFlux)</b> architecture, authentication is now handled
 * using Spring Security’s <b>Reactive OAuth2 Resource Server</b> support
 * ({@code oauth2ResourceServer().jwt()}).
 * </p>
 *
 * <p>
 * As a result:
 * <ul>
 *   <li>This filter is intentionally <b>not registered</b> in the reactive security chain.</li>
 *   <li>JWT validation and role extraction are now handled by Spring Security internally.</li>
 *   <li>Authorization is enforced via {@code hasRole}/{@code hasAuthority} matchers.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>When can this class still be used?</b>
 * <ul>
 *   <li>In Spring MVC (Servlet-based) applications</li>
 *   <li>In non-reactive microservices without an API Gateway</li>
 *   <li>In legacy systems that do not use {@code oauth2ResourceServer().jwt()}</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Why is it kept?</b>
 * <ul>
 *   <li>As reference documentation for MVC-based JWT authentication</li>
 *   <li>For reuse in non-reactive services if needed</li>
 *   <li>For understanding the JWT → SecurityContext flow</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>⚠ DO NOT ENABLE THIS FILTER</b> in reactive (WebFlux) security configurations.
 * Doing so may cause authentication inconsistencies or duplicate security contexts.
 * </p>
 *
 * @author Arpan Das
 * @since 1.0
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements WebFilter {

    private final JwtUtil jwtUtil;

    /**
     * Extracts and validates JWT token from the Authorization header and
     * populates the {@link ReactiveSecurityContextHolder} with authentication data.
     *
     * <p>
     * This method performs the following steps:
     * <ol>
     *   <li>Extracts the {@code Authorization} header</li>
     *   <li>Validates the JWT token</li>
     *   <li>Extracts user email and role claims</li>
     *   <li>Creates a {@link UsernamePasswordAuthenticationToken}</li>
     *   <li>Writes authentication into the reactive security context</li>
     * </ol>
     * </p>
     *
     * <p>
     * If the token is missing, invalid, or expired, the request continues
     * without authentication.
     * </p>
     *
     * @param exchange current server exchange
     * @param chain    reactive web filter chain
     * @return {@link Mono} signaling completion
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.isValidToken(token)) {
                return chain.filter(exchange);
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractAllClaims(token).get("role", String.class);

            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
            var authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);

            // Attach authentication context reactively
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            // Invalid token - just continue without authentication
            log.warn("JWT authentication failed: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }
}
