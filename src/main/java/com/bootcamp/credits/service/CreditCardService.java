package com.bootcamp.credits.service;

import com.bootcamp.credits.dto.CreditCardRequestDto;
import com.bootcamp.credits.dto.CreditCardResponseDto;
import com.bootcamp.credits.dto.Message;
import com.bootcamp.credits.entity.CreditCard;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditCardService {

	Flux<CreditCard> getAll();

	Mono<CreditCard> getCreditCardById(String creditCardId);

	Mono<CreditCardResponseDto> createCreditCardPerson(CreditCardRequestDto creditCardRequestDto);
	
	Mono<CreditCardResponseDto> createCreditCardCompany(CreditCardRequestDto creditCardRequestDto);

	Mono<CreditCard> updateCreditCard(CreditCardRequestDto creditCardRequestDto);

	Mono<Message> deleteCreditCard(String creditCardId);
	
	Mono<CreditCardResponseDto> payCreditCard(CreditCardRequestDto creditRequestDto);
	
	Mono<CreditCardResponseDto> consumeCreditCard(CreditCardRequestDto creditRequestDto);

	Flux<CreditCard> getAllCreditCardXCustomerId(String customerId);

}
