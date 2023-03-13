package com.bootcamp.credits.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.bootcamp.credits.entity.Credit;

public interface CreditRepository extends ReactiveMongoRepository<Credit, String> {

}
