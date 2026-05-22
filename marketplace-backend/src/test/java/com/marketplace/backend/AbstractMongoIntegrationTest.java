package com.marketplace.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractMongoIntegrationTest {

	@Container
	static final MongoDBContainer MONGO = new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

	@DynamicPropertySource
	static void overrideMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
		registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
	}
}
