package com.hch;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServicesConfiguration {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("books", r ->
                        r.path("/books/**")
                                .filters(f -> f.setPath("/"))
                                .uri("http://localhost:8083"))
                .route("ratings", r ->
                        r.path("/ratings/**")
                                .uri("http://localhost:8084"))
                .build();
    }
}
