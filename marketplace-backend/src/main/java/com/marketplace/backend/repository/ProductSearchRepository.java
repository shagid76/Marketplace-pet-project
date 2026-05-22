    package com.marketplace.backend.repository;

    import com.marketplace.backend.model.Product.ProductDocument;
    import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

    import java.util.List;

    public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
        @Override
        List<ProductDocument> findAllById(Iterable<String> ids);
    }
