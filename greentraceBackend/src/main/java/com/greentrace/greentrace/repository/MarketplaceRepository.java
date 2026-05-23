package com.greentrace.greentrace.repository;

import com.greentrace.greentrace.model.MarketplaceListing;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MarketplaceRepository
        extends MongoRepository<MarketplaceListing,String> {
}