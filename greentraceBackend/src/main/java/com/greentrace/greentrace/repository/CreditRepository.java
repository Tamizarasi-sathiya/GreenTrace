package com.greentrace.greentrace.repository;

import com.greentrace.greentrace.model.Credit;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CreditRepository extends MongoRepository<Credit,String> {
}