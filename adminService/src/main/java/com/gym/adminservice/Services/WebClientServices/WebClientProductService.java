package com.gym.adminservice.Services.WebClientServices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WebClientProductService {

    private final WebClient.Builder webClient;


    private void postRequestAsyncronus(String path, String name, String brand, String category, Object body){
        webClient.build().post()
                .uri(uriBuilder-> uriBuilder
                        .path(path)
                        .queryParam("name",name)
                        .queryParam("brand",brand)
                        .queryParam("category",category)
                        .build())
                .bodyValue(body)
                .retrieve().toBodilessEntity().subscribe(
                        success-> System.out.println("Request sent to product service successfully"),
                error-> System.out.println("Failed to send request")
                );
    }

    private void delete(String id,String path){
        webClient.build().delete()
                .uri(uri->uri
                        .path(path)
                        .queryParam("id", id)
                        .build())
                .retrieve().toBodilessEntity().subscribe(
                        success-> System.out.println("Delete request sent by API"),
                error-> System.out.println("Failed to sent request")
                );
    }
}
