package com.bootcamp.credits.service.impl;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.credits.clients.CustomerRestClient;
import com.bootcamp.credits.clients.TransactionsRestClient;
import com.bootcamp.credits.dto.CreditRequestDto;
import com.bootcamp.credits.dto.CreditResponseDto;
import com.bootcamp.credits.dto.Message;
import com.bootcamp.credits.dto.Transaction;
import com.bootcamp.credits.entity.Credit;
import com.bootcamp.credits.repository.CreditRepository;
import com.bootcamp.credits.service.CreditService;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class CreditServiceImpl implements CreditService{
	
	@Autowired
    private CreditRepository creditRepository;
	
	@Autowired
    CustomerRestClient customerRestClient;
	
	@Autowired
	TransactionsRestClient transactionRestClient;
	
	/**
	 * Obtiene todos los créditos
	 * @return Flux<Credit>
	 */
	@Override
	public Flux<Credit> getAll() {
		return creditRepository.findAll();
	}

	/**
	 * Obtiene el crédito por su id 
	 * @param creditId
	 * @return Mono<Credit>
	 */
	@Override
	public Mono<Credit> getCreditById(String creditId) {
		return creditRepository.findById(creditId);
	}

	/**
	 * Registro de un crédito personal
	 * Se obtiene el cliente getPersonById()
	 * Se valida si ya tiene un crédito getCreditByIdCustomerPerson(), si no tiene se crea el crédito personal saveNewAccount()
	 * @param creditRequestDto
	 * @return Mono<CreditResponseDto>
	 */
	@Override
	public Mono<CreditResponseDto> createCreditPerson(CreditRequestDto creditRequestDto) {
		Credit credit = new Credit(null,creditRequestDto.getCustomerId(), 3, "CRED_PERSONAL"
				, creditRequestDto.getCreditAmount() , 0.0, LocalDateTime.now(), creditRequestDto.getTypeCustomer());
		return customerRestClient.getPersonById(creditRequestDto.getCustomerId()).flatMap(c ->{
			credit.setTypeCustomer(c.getTypeCustomer());
			return getCreditByIdCustomerPerson(creditRequestDto.getCustomerId(),credit.getDescripTypeAccount(),c.getTypeCustomer()).flatMap(v -> {
				return Mono.just(new CreditResponseDto(null, "Personal client already has a personal credit: "+credit.getDescripTypeAccount()));
			}).switchIfEmpty(saveNewAccount(credit, "Credit created successfully"));
		}).defaultIfEmpty(new CreditResponseDto(null, "Client does not exist"));
	}
	
	/**
	 * Registro de un crédito empresarial
	 * Se obtiene el client getCompanyById()
	 * Se crea el crédito empresarial saveNewAccount()
	 * @param creditRequestDto
	 * @return Mono<CreditResponseDto>
	 */
	@Override
	public Mono<CreditResponseDto> createCreditCompany(CreditRequestDto creditRequestDto) {
		Credit credit = new Credit(null,creditRequestDto.getCustomerId(), 4, "CRED_EMPRESARIAL"
				, creditRequestDto.getCreditAmount() , 0.0, LocalDateTime.now(), creditRequestDto.getTypeCustomer());
		return customerRestClient.getCompanyById(creditRequestDto.getCustomerId()).flatMap(c ->{
			credit.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(credit, "Credit created successfully");
		}).defaultIfEmpty(new CreditResponseDto(null, "Client does not exist"));
		
	}

	/**
	 * Actualización de un crédito
	 * Se obtiene el crédito por el id findById() y se actualiza save()
	 * @param creditRequestDto
	 * @return Mono<Credit>
	 */
	@Override
	public Mono<Credit> updateCredit(CreditRequestDto creditRequestDto) {
		return creditRepository.findById(creditRequestDto.getId())
                .flatMap(uCredit -> {
                	uCredit.setCustomerId(creditRequestDto.getCustomerId());
                	uCredit.setTypeAccount(creditRequestDto.getTypeAccount());
                	uCredit.setCreditAmount(creditRequestDto.getCreditAmount());
                	uCredit.setExistingAmount(creditRequestDto.getExistingAmount());
                	uCredit.setCreditDate(creditRequestDto.getCreditDate());
                    return creditRepository.save(uCredit);
        });
	}

	/**
	 * Eliminación de un crédito
	 * Se obtiene el crédito por el id findById() y se elimina deleteById()
	 * @param creditId
	 * @return Mono<Message>
	 */
	@Override
	public Mono<Message> deleteCredit(String creditId) {
		Message message = new Message("Credit does not exist");
		return creditRepository.findById(creditId)
                .flatMap(dCredit -> {
                	message.setMessage("Credit deleted successfully");
                	return creditRepository.deleteById(dCredit.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}

	/**
	 * Pago de un crédito personal o empresarial
	 * Se obtiene el crédito por el id findById()
	 * Se valida que el pago no exceda el limite del crédito
	 * Se actualiza el crédito y se registra la transacción updateAccount()
	 * @param creditRequestDto
	 * @return Mono<CreditResponseDto>
	 */
	@Override
	public Mono<CreditResponseDto> payCredit(CreditRequestDto creditRequestDto) {
		return creditRepository.findById(creditRequestDto.getId()).flatMap(uCredit -> {
			Double newAmount = uCredit.getExistingAmount() + creditRequestDto.getAmount();
			if(newAmount > uCredit.getCreditAmount()) {
				return Mono.just(new CreditResponseDto(null, "Payment exceeds the limit"));
			}else {
				uCredit.setExistingAmount(newAmount);
				return updateAccount(uCredit, creditRequestDto.getAmount(), "PAGO");
			}
		}).defaultIfEmpty(new CreditResponseDto(null, "Credit does not exist"));
	}
	
	/**
	 * Obtiene los créditos por el id del cliente
	 * @param customerId
	 * @return Flux<Credit>
	 */
	@Override
	public Flux<Credit> getAllCreditXCustomerId(String customerId) {
		return creditRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}

	/**
	 * Se guarda un crédito save()
	 * @param credit
	 * @param message
	 * @return Mono<CreditResponseDto>
	 */
	public Mono<CreditResponseDto> saveNewAccount(Credit credit, String message) {
		return creditRepository.save(credit).flatMap(x -> {
			return Mono.just(new CreditResponseDto(credit, message));
		});
	}
	
	/**
	 * Se guarda un crédito save() y se registra la transacción registerTransaction()
	 * @param credit
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<CreditResponseDto>
	 */
	public Mono<CreditResponseDto> updateAccount(Credit credit, Double amount, String typeTransaction) {
		return creditRepository.save(credit).flatMap(x -> {
			return registerTransaction(credit, amount, typeTransaction);
		});
	}
	
	/**
	 * Se obtiene los créditos según el cliente, tipo de transacción y tipo de cliente
	 * @param customerId
	 * @param type
	 * @param customer
	 * @return Mono<Credit>
	 */
	public Mono<Credit> getCreditByIdCustomerPerson(String customerId, String type, String customer) {
		return creditRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId))
				.filter(c -> c.getDescripTypeAccount().equals("CRED_PERSONAL"))
				.filter(c -> c.getTypeCustomer().equals(customer))
				.next();
	}
	
	/**
	 * Se registra una transacción createTransaction()
	 * @param credit
	 * @param amount
	 * @param typeTransaction
	 * @return Mono<CreditResponseDto>
	 */
	private Mono<CreditResponseDto> registerTransaction(Credit credit, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(credit.getCustomerId());
		transaction.setProductId(credit.getId());
		transaction.setProductType(credit.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(LocalDateTime.now());
		transaction.setCustomerType(credit.getTypeCustomer());
		transaction.setBalance(credit.getExistingAmount());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new CreditResponseDto(credit, "Successful transaction"));
        });
	}

}
