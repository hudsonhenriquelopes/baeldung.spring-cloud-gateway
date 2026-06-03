package com.hch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/* @EnableDiscoveryClient is not need anymore.
 * Spring Cloud already discovers that and register on Eureka. */
@SpringBootApplication
@EnableFeignClients
public class SpringCloudGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudGatewayApplication.class, args);
    }
}