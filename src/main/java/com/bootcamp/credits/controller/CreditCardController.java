package com.bootcamp.credits.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootcamp.credits.dto.CreditCardRequestDto;
import com.bootcamp.credits.dto.CreditCardResponseDto;
import com.bootcamp.credits.dto.Message;
import com.bootcamp.credits.entity.CreditCard;
import com.bootcamp.credits.service.CreditCardService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/creditcard")
public class CreditCardController {
	
	@Autowired
    private CreditCardService creditcardService;
	
	@GetMapping
    public Flux<CreditCard> getAll(){
		return creditcardService.getAll();
    }
	
	@GetMapping("/{creditId}")
    public Mono<CreditCard> getCreditCardById(@PathVariable String creditId){
		return creditcardService.getCreditCardById(creditId);
    }
	
	@PostMapping("/person")
    public Mono<CreditCardResponseDto> createCreditCardPerson(@RequestBody CreditCardRequestDto creditCardRequestDto){
		return creditcardService.createCreditCardPerson(creditCardRequestDto);
    }
	
	@PostMapping("/company")
    public Mono<CreditCardResponseDto> createCreditCardCompany(@RequestBody CreditCardRequestDto creditCardRequestDto){
		return creditcardService.createCreditCardCompany(creditCardRequestDto);
    }
	
	@PutMapping
	public Mono<CreditCard> updateCCreditCard(@RequestBody CreditCardRequestDto creditCardRequestDto){
		return creditcardService.updateCreditCard(creditCardRequestDto);
    }
	
	@DeleteMapping("/{creditId}")
	public Mono<Message> deleteCreditCard(@PathVariable String creditId){
		return creditcardService.deleteCreditCard(creditId);
    }
	
	@PostMapping("/pay")
    public Mono<CreditCardResponseDto> payCreditCard(@RequestBody CreditCardRequestDto creditCardRequestDto){
		return creditcardService.payCreditCard(creditCardRequestDto);
    }
	
	@PostMapping("/consume")
    public Mono<CreditCardResponseDto> consumeCreditCard(@RequestBody CreditCardRequestDto creditCardRequestDto){
		return creditcardService.consumeCreditCard(creditCardRequestDto);
    }
	
	@GetMapping("/consult/{customerId}")
    public Flux<CreditCard> getAllCreditCardXCustomerId(@PathVariable String customerId){
		return creditcardService.getAllCreditCardXCustomerId(customerId);
    }
	
}
