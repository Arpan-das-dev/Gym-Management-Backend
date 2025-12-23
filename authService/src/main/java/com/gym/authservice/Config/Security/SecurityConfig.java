package com.gym.authservice.Config.Security;

import com.gym.authservice.Config.Jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Public routes
                        .pathMatchers("/fitStudio/auth/**").permitAll()
                        .pathMatchers("/ws/**").permitAll()
                        .pathMatchers("fitStudio/payment-service/all/*").permitAll()
                        .pathMatchers("/fitStudio/plan-service/*/all/**").permitAll()
                        .pathMatchers("/fitStudio/member-service/*/all/**").permitAll()
                        .pathMatchers("/fiStudio/member-service/*/all/**").permitAll()
                        .pathMatchers("/fitStudio/trainerservice/*/all/**").permitAll()
                        .pathMatchers("/fitStudio/payment-service/matrices/all/**").permitAll()
                        .pathMatchers("/fitStudio/admin/report-message/all/users")
                        .hasAnyRole("MEMBER","TRAINER","ADMIN","TRAINER_PENDING")
                        // plan service configuration
                        // Admin routes only
                        .pathMatchers("/fitStudio/plan-service/*/admin/**")
                        .hasRole("ADMIN")
                        .pathMatchers("/fitStudio/member-service/*/admin/**")
                        .hasRole("ADMIN")
                        .pathMatchers("/fitStudio/trainer-service/*/admin/**")
                        .hasAnyRole("ADMIN")
                        .pathMatchers("/fitStudio/member-service/*/member/**")
                        .hasAnyRole("ADMIN","MEMBER")
                        .pathMatchers("/fitStudio/trainer-service/*/trainer/**")
                        .hasAnyRole("ADMIN","TRAINER")
                        // admin service routes
                        .pathMatchers("/fitStudio/admin/auth-management/**").hasRole("ADMIN")
                        .pathMatchers("/fitStudio/admin/report-message/administrator/**").hasRole("ADMIN")
                        // Everything else must be authenticated
                        .anyExchange().authenticated()
                )
                // Add your JWT filter BEFORE authentication phase
                .addFilterAt(JwtAuthenticationWebFilter(jwtUtil), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public AuthenticationWebFilter JwtAuthenticationWebFilter(JwtUtil jwtUtil) {
        ReactiveAuthenticationManager authManager = Mono::just;
        AuthenticationWebFilter filter =
                new AuthenticationWebFilter(authManager);
        filter.setServerAuthenticationConverter(exchange -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.empty();
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.isValidToken(token)) {
                return Mono.empty();
            }

            String email = jwtUtil.extractEmail(token);
            String role = jwtUtil.extractAllClaims(token).get("role", String.class);

            return Mono.just(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            token,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    )
            );
        });

        filter.setSecurityContextRepository(
                NoOpServerSecurityContextRepository.getInstance()
        );

        filter.setAuthenticationSuccessHandler((ex, auth) -> {
            log.info("AUTHORITIES = {}", auth.getAuthorities());
            return ex.getChain().filter(ex.getExchange());
        });

        return filter;
    }


}
