package com.bootcamp.credits.service.impl;

import java.time.LocalDateTime;

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
	
	/**
	 * Obtiene todas las tarjetas de crédito
	 * @return Flux<CreditCard>
	 */
	@Override
	public Flux<CreditCard> getAll() {
		return creditCardRepository.findAll();
	}

	/**
	 * Obtiene una tarjeta de crédito por su id 
	 * @param creditId
	 * @return Mono<CreditCard>
	 */
	@Override
	public Mono<CreditCard> getCreditCardById(String creditId) {
		return creditCardRepository.findById(creditId);
	}

	/**
	 * Registro de una tarjeta de crédito personal
	 * Se obtiene el cliente getPersonById()
	 * Se crea la tarjeta de crédito personal saveNewAccount()
	 * @param creditCardRequestDto
	 * @return Mono<CreditCardResponseDto>
	 */
	@Override
	public Mono<CreditCardResponseDto> createCreditCardPerson(CreditCardRequestDto creditCardRequestDto) {
		CreditCard creditCard = new CreditCard(null,creditCardRequestDto.getCustomerId(), 5, "TAR_CRED_PERSONAL", creditCardRequestDto.getCreditAmount()
				, creditCardRequestDto.getCreditAmount(), LocalDateTime.now(), creditCardRequestDto.getNumberCard(), null);
		return customerRestClient.getPersonById(creditCardRequestDto.getCustomerId()).flatMap(c ->{
			creditCard.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(creditCard, "CreditCard created successfully");
		}).defaultIfEmpty(new CreditCardResponseDto(null, "Client does not exist"));
	}
	
	/**
	 * Registro de una tarjeta de crédito empresarial
	 * Se obtiene el cliente getPersonById()
	 * Se crea la tarjeta de crédito empresarial saveNewAccount()
	 * @param creditCardRequestDto
	 * @return Mono<CreditCardResponseDto>
	 */
	@Override
	public Mono<CreditCardResponseDto> createCreditCardCompany(CreditCardRequestDto creditCardRequestDto) {
		CreditCard creditCard = new CreditCard(null,creditCardRequestDto.getCustomerId(), 6, "TAR_CRED_EMPRESARIAL", creditCardRequestDto.getCreditAmount()
				, creditCardRequestDto.getCreditAmount(), LocalDateTime.now(), creditCardRequestDto.getNumberCard(), null);
		return customerRestClient.getCompanyById(creditCardRequestDto.getCustomerId()).flatMap(c ->{
			creditCard.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(creditCard, "CreditCard created successfully");
		}).defaultIfEmpty(new CreditCardResponseDto(null, "Client does not exist"));
	}

	/**
	 * Actualización de una tarjeta de crédito
	 * Se obtiene el crédito por el id findById() y se actualiza save()
	 * @param creditCardRequestDto
	 * @return Mono<CreditCard>
	 */
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

	/**
	 * Eliminación de una tarjeta de crédito
	 * Se obtiene el crédito por el id findById() y se elimina deleteById()
	 * @param creditId
	 * @return Mono<Message>
	 */
	@Override
	public Mono<Message> deleteCreditCard(String creditId) {
		Message message = new Message("CreditCard does not exist");
		return creditCardRepository.findById(creditId)
                .flatMap(dCredit -> {
                	message.setMessage("CreditCard deleted successfully");
                	return creditCardRepository.deleteById(dCredit.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}
	
	/**
	 * Pago de una tarjeta de crédito
	 * Se obtiene el crédito por el id findById()
	 * Se valida que el pago no exceda el limite de la tarjeta de crédito
	 * Se actualiza la tarjeta de crédito y se registra la transacción updateAccount()
	 * @param creditCardRequestDto
	 * @return Mono<CreditCardResponseDto>
	 */
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

	/**
	 * Consumo de una tarjeta de crédito
	 * Se obtiene el crédito por el id findById()
	 * Se valida que el Retiro no exceda el saldo de la tarjeta de crédito
	 * Se actualiza la tarjeta de crédito y se registra la transacción updateAccount()
	 * @param creditCardRequestDto
	 * @return Mono<CreditCardResponseDto>
	 */
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
	
	/**
	 * Obtiene las tarjetas de crédito por el id del cliente
	 * @param customerId
	 * @return Flux<CreditCard>
	 */
	@Override
	public Flux<CreditCard> getAllCreditCardXCustomerId(String customerId) {
		return creditCardRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}
	
	/**
	 * Guarda una tarjeta de crédito save()
	 * @param creditCard
	 * @param message
	 * @return Mono<CreditCardResponseDto>
	 */
	private Mono<CreditCardResponseDto> saveNewAccount(CreditCard creditCard, String message) {
		return creditCardRepository.save(creditCard).flatMap(x -> {
			return Mono.just(new CreditCardResponseDto(creditCard, message));
		});
	}
	
	/**
	 * Guarda una tarjeta de crédito save() y registra una transacción registerTransaction()
	 * @param creditCard
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<CreditCardResponseDto>
	 */
	private Mono<CreditCardResponseDto> updateAccount(CreditCard creditCard, Double amount, String typeTransaction) {
		return creditCardRepository.save(creditCard).flatMap(x -> {
			return registerTransaction(creditCard, amount, typeTransaction);
		});
	}
	
	/**
	 * Registra una transacción createTransaction() 
	 * @param creditCard
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<CreditCardResponseDto>
	 */
	private Mono<CreditCardResponseDto> registerTransaction(CreditCard creditCard, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(creditCard.getCustomerId());
		transaction.setProductId(creditCard.getId());
		transaction.setProductType(creditCard.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(LocalDateTime.now());
		transaction.setCustomerType(creditCard.getTypeCustomer());
		transaction.setBalance(creditCard.getExistingAmount());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new CreditCardResponseDto(creditCard, "Successful transaction"));
        });
	}

}
