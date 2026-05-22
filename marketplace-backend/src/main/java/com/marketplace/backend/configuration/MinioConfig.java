package com.marketplace.backend.configuration;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${images.s3Endpoint}")
    private String endpoint;

    @Value("${images.accessKey}")
    private String accessKey;

    @Value("${images.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
      return MinioClient.builder()
              .endpoint(endpoint)
              .credentials(accessKey, secretKey)
              .build();
    }
}