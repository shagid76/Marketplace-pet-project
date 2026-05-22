package com.marketplace.backend;

import com.marketplace.backend.repository.ProductSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


class BackendApplicationTests extends AbstractMongoIntegrationTest {
	@MockitoBean
	private ProductSearchRepository productSearchRepository;

	@MockitoBean
	private ElasticsearchOperations elasticsearchOperations;

	@Test
	void contextLoads() {
	}
}
