package com.gym.trainerService.Configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class responsible for initializing and providing a shared
 * {@link WebClient.Builder} instance across the application.
 * <p>
 * The {@link WebClient} is a non-blocking, reactive HTTP client introduced in Spring WebFlux
 * as a modern alternative to {@code RestTemplate}. It supports asynchronous communication
 * and backpressure, making it suitable for high-concurrency microservice interactions.
 * </p>
 *
 * <p><b>Purpose:</b></p>
 * <ul>
 *   <li>Establishes a centralized {@link WebClient.Builder} bean to support reactive HTTP calls.</li>
 *   <li>Ensures consistent configurations such as headers, base URLs, or filters when cloned downstream.</li>
 *   <li>Encourages dependency injection for service-level REST client creation.</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * @Service
 * public class TrainerClientService {
 *     private final WebClient webClient;
 *
 *     public TrainerClientService(WebClient.Builder builder) {
 *         this.webClient = builder.baseUrl("http://trainer-service/api").build();
 *     }
 *
 *     public Mono<TrainerResponse> getTrainer(String trainerId) {
 *         return webClient.get()
 *                 .uri("/trainer/{id}", trainerId)
 *                 .retrieve()
 *                 .bodyToMono(TrainerResponse.class);
 *     }
 * }
 * }</pre>
 *
 * @author Arpan
 * @since 1.0
 */
@Configuration
public class WebClientConfig {

    /**
     * Provides a reusable {@link WebClient.Builder} bean.
     * <p>
     * This builder allows downstream services to configure base URLs,
     * add custom filters, apply interceptors, and define request/response codecs.
     * </p>
     *
     * <p><b>Best Practice:</b> Inject this builder rather than creating
     * a new {@link WebClient} directly, as modifications to the builder
     * are shared across all derived clients for consistent configuration.</p>
     *
     * @return a globally available {@link WebClient.Builder} instance for HTTP communication
     */
    @Bean
    public WebClient.Builder webClient() {
        return  WebClient.builder();
    }
}
