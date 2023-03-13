package com.bootcamp.credits.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.credits.clients.CustomerRestClient;
import com.bootcamp.credits.clients.TransactionsRestClient;
import com.bootcamp.credits.dto.CreditCardRequestDto;
import com.bootcamp.credits.dto.CreditCardResponseDto;
import com.bootcamp.credits.dto.Message;
import com.bootcamp.credits.dto.Transaction;
import com.bootcamp.credits.entity.CreditCard;
import com.bootcamp.credits.repository.CreditCardRepository;
import com.bootcamp.credits.service.CreditCardService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CreditCardServiceImpl implements CreditCardService{

	@Autowired
    private CreditCardRepository creditCardRepository;
	
	@Autowired
    CustomerRestClient customerRestClient;
	
	@Autowired
	TransactionsRestClient transactionRestClient;
	
	@Override
	public Flux<CreditCard> getAll() {
		return creditCardRepository.findAll();
	}

	@Override
	public Mono<CreditCard> getCreditCardById(String creditId) {
		return creditCardRepository.findById(creditId);
	}

	@Override
	public Mono<CreditCardResponseDto> createCreditCardPerson(CreditCardRequestDto creditCardRequestDto) {
		CreditCard creditCard = new CreditCard(null,creditCardRequestDto.getCustomerId(), 5, "TAR_CRED_PERSONAL", creditCardRequestDto.getCreditAmount()
				, creditCardRequestDto.getCreditAmount(), creditCardRequestDto.getCreditDate(), creditCardRequestDto.getNumberCard(), null);
		return customerRestClient.getPersonById(creditCardRequestDto.getCustomerId()).flatMap(c ->{
			creditCard.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(creditCard, "CreditCard created successfully");
		}).defaultIfEmpty(new CreditCardResponseDto(null, "Client does not exist"));
	}
	
	@Override
	public Mono<CreditCardResponseDto> createCreditCardCompany(CreditCardRequestDto creditCardRequestDto) {
		CreditCard creditCard = new CreditCard(null,creditCardRequestDto.getCustomerId(), 6, "TAR_CRED_EMPRESARIAL", creditCardRequestDto.getCreditAmount()
				, creditCardRequestDto.getCreditAmount(), creditCardRequestDto.getCreditDate(), creditCardRequestDto.getNumberCard(), null);
		return customerRestClient.getCompanyById(creditCardRequestDto.getCustomerId()).flatMap(c ->{
			creditCard.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(creditCard, "CreditCard created successfully");
		}).defaultIfEmpty(new CreditCardResponseDto(null, "Client does not exist"));
	}

	@Override
	public Mono<CreditCard> updateCreditCard(CreditCardRequestDto creditCardRequestDto) {
		return creditCardRepository.findById(creditCardRequestDto.getId())
                .flatMap(uCredit -> {
                	uCredit.setCustomerId(creditCardRequestDto.getCustomerId());
                	uCredit.setTypeAccount(creditCardRequestDto.getTypeAccount());
                	uCredit.setCreditAmount(creditCardRequestDto.getCreditAmount());
                	uCredit.setExistingAmount(creditCardRequestDto.getExistingAmount());
                	uCredit.setCreditDate(creditCardRequestDto.getCreditDate());
                	uCredit.setNumberCard(creditCardRequestDto.getNumberCard());
                    return creditCardRepository.save(uCredit);
        });
	}

	@Override
	public Mono<Message> deleteCreditCard(String creditId) {
		Message message = new Message("CreditCard does not exist");
		return creditCardRepository.findById(creditId)
                .flatMap(dCredit -> {
                	message.setMessage("CreditCard deleted successfully");
                	return creditCardRepository.deleteById(dCredit.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}
	
	@Override
	public Mono<CreditCardResponseDto> payCreditCard(CreditCardRequestDto creditRequestDto) {
		return creditCardRepository.findById(creditRequestDto.getId()).flatMap(uCredit -> {
			Double newAmount = uCredit.getExistingAmount() + creditRequestDto.getAmount();
			if(newAmount > uCredit.getCreditAmount()) {
				return Mono.just(new CreditCardResponseDto(null, "Payment exceeds the limit"));
			}else {
				uCredit.setExistingAmount(newAmount);
				return updateAccount(uCredit, creditRequestDto.getAmount(), "PAGO");
			}
		}).defaultIfEmpty(new CreditCardResponseDto(null, "CreditCard does not exist"));
	}

	@Override
	public Mono<CreditCardResponseDto> consumeCreditCard(CreditCardRequestDto creditRequestDto) {
		return creditCardRepository.findById(creditRequestDto.getId()).flatMap(uCredit -> {
			Double newAmount = uCredit.getExistingAmount() - creditRequestDto.getAmount();
			if(newAmount<0) {
				return Mono.just(new CreditCardResponseDto(null, "You don't have enough balance"));
			}else {
				uCredit.setExistingAmount(newAmount);
				return updateAccount(uCredit, creditRequestDto.getAmount(), "CONSUMO");
			}
		}).defaultIfEmpty(new CreditCardResponseDto(null, "CreditCard does not exist"));
	}
	
	@Override
	public Flux<CreditCard> getAllCreditCardXCustomerId(String customerId) {
		return creditCardRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}
	
	private Mono<CreditCardResponseDto> saveNewAccount(CreditCard creditCard, String message) {
		return creditCardRepository.save(creditCard).flatMap(x -> {
			return Mono.just(new CreditCardResponseDto(creditCard, message));
		});
	}
	
	private Mono<CreditCardResponseDto> updateAccount(CreditCard creditCard, Double amount, String typeTransaction) {
		return creditCardRepository.save(creditCard).flatMap(x -> {
			return registerTransaction(creditCard, amount, typeTransaction);
		});
	}
	
	private Mono<CreditCardResponseDto> registerTransaction(CreditCard creditCard, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(creditCard.getCustomerId());
		transaction.setProductId(creditCard.getId());
		transaction.setProductType(creditCard.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setCustomerType(creditCard.getTypeCustomer());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new CreditCardResponseDto(creditCard, "Successful transaction"));
        });
	}

}
