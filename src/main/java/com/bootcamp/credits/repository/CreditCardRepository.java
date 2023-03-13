package com.bootcamp.credits.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.bootcamp.credits.entity.CreditCard;

public interface CreditCardRepository extends ReactiveMongoRepository<CreditCard, String> {

}
