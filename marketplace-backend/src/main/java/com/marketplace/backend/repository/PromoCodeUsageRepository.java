package com.marketplace.backend.repository;


import com.marketplace.backend.model.PromoCode.PromoCodeUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromoCodeUsageRepository extends MongoRepository<PromoCodeUsage, String> {
    int countByUserIdAndPromoCodeId(String userId, String promoCodeId);
}
