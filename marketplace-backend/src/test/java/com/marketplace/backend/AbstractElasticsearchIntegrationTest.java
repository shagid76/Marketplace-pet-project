package com.marketplace.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Base class for tests that need both a real Elasticsearch node and MongoDB.
 * Spins up ES 8.x and MongoDB 7 containers via Testcontainers and wires their
 * URIs into the Spring context so the full application wiring (including
 * MongoDB repositories used by service beans) succeeds.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractElasticsearchIntegrationTest {

    @Container
    static final ElasticsearchContainer ELASTICSEARCH =
            new ElasticsearchContainer(
                    DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.1"))
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                    // Disable mmap to avoid the vm.max_map_count bootstrap check on CI runners.
                    .withEnv("node.store.allow_mmap", "false")
                    // ElasticsearchContainer 1.20.x defaults to an HTTPS wait strategy for ES 8.x,
                    // even when security is disabled.  Override with a plain HTTP health check so
                    // Testcontainers only marks the container ready once ES is actually serving HTTP.
                    .waitingFor(Wait.forHttp("/_cluster/health")
                            .forPort(9200)
                            .forStatusCode(200)
                            .withStartupTimeout(Duration.ofMinutes(3)));

    @Container
    static final MongoDBContainer MONGO =
            new MongoDBContainer(DockerImageName.parse("mongo:7.0"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        // Wire in real Elasticsearch and disable the test-profile exclusions so
        // that ES repositories are bootstrapped in this integration context.
        registry.add("spring.elasticsearch.uris",
                () -> "http://" + ELASTICSEARCH.getHttpHostAddress());
        registry.add("spring.autoconfigure.exclude", () -> "");

        // Wire in real MongoDB so that service beans (e.g. ProductService) whose
        // constructor-injected MongoDB repositories can be satisfied by Spring.
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }
}
