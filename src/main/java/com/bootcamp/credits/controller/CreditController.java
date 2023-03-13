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

import com.bootcamp.credits.dto.CreditRequestDto;
import com.bootcamp.credits.dto.CreditResponseDto;
import com.bootcamp.credits.entity.Credit;
import com.bootcamp.credits.service.CreditService;
import com.bootcamp.credits.dto.Message;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/credit")
public class CreditController {
	
	@Autowired
    private CreditService creditService;
	
	@GetMapping
    public Flux<Credit> getAll(){
		return creditService.getAll();
    }
	
	@GetMapping("/{creditId}")
    public Mono<Credit> getCreditById(@PathVariable String creditId){
		return creditService.getCreditById(creditId);
    }
	
	@PostMapping("/person")
    public Mono<CreditResponseDto> createCreditPerson(@RequestBody CreditRequestDto creditRequestDto){
		return creditService.createCreditPerson(creditRequestDto);
    }
	
	@PostMapping("/company")
    public Mono<CreditResponseDto> createCreditCompany(@RequestBody CreditRequestDto creditRequestDto){
		return creditService.createCreditCompany(creditRequestDto);
    }
	
	@PutMapping
	public Mono<Credit> updateCredit(@RequestBody CreditRequestDto creditRequestDto){
		return creditService.updateCredit(creditRequestDto);
    }
	
	@DeleteMapping("/{creditId}")
	public Mono<Message> deleteCredit(@PathVariable String creditId){
		return creditService.deleteCredit(creditId);
    }
	
	@PostMapping("/pay")
    public Mono<CreditResponseDto> payCredit(@RequestBody CreditRequestDto creditRequestDto){
		return creditService.payCredit(creditRequestDto);
    }
	
	@GetMapping("/consult/{customerId}")
    public Flux<Credit> getAllCreditXCustomerId(@PathVariable String customerId){
		return creditService.getAllCreditXCustomerId(customerId);
    }
	
}
