package com.bootcamp.credits.service.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bootcamp.credits.clients.TransactionsRestClient;
import com.bootcamp.credits.clients.CustomerRestClient;
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
	
	@Override
	public Flux<Credit> getAll() {
		return creditRepository.findAll();
	}

	@Override
	public Mono<Credit> getCreditById(String creditId) {
		return creditRepository.findById(creditId);
	}

	@Override
	public Mono<CreditResponseDto> createCreditPerson(CreditRequestDto creditRequestDto) {
		Credit credit = new Credit(null,creditRequestDto.getCustomerId(), 3, "CRED_PERSONAL"
				, creditRequestDto.getCreditAmount() , 0.0, creditRequestDto.getCreditDate(), creditRequestDto.getTypeCustomer());
		return customerRestClient.getPersonById(creditRequestDto.getCustomerId()).flatMap(c ->{
			credit.setTypeCustomer(c.getTypeCustomer());
			return getCreditByIdCustomerPerson(creditRequestDto.getCustomerId(),credit.getDescripTypeAccount(),c.getTypeCustomer()).flatMap(v -> {
				return Mono.just(new CreditResponseDto(null, "Personal client already has a personal credit: "+credit.getDescripTypeAccount()));
			}).switchIfEmpty(saveNewAccount(credit, "Credit created successfully"));
		}).defaultIfEmpty(new CreditResponseDto(null, "Client does not exist"));
	}
	
	@Override
	public Mono<CreditResponseDto> createCreditCompany(CreditRequestDto creditRequestDto) {
		Credit credit = new Credit(null,creditRequestDto.getCustomerId(), 4, "CRED_EMPRESARIAL"
				, creditRequestDto.getCreditAmount() , 0.0, creditRequestDto.getCreditDate(), creditRequestDto.getTypeCustomer());
		return customerRestClient.getCompanyById(creditRequestDto.getCustomerId()).flatMap(c ->{
			credit.setTypeCustomer(c.getTypeCustomer());
			return saveNewAccount(credit, "Credit created successfully");
		}).defaultIfEmpty(new CreditResponseDto(null, "Client does not exist"));
		
	}

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

	@Override
	public Mono<Message> deleteCredit(String creditId) {
		Message message = new Message("Credit does not exist");
		return creditRepository.findById(creditId)
                .flatMap(dCredit -> {
                	message.setMessage("Credit deleted successfully");
                	return creditRepository.deleteById(dCredit.getId()).thenReturn(message);
        }).defaultIfEmpty(message);
	}

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
	
	@Override
	public Flux<Credit> getAllCreditXCustomerId(String customerId) {
		return creditRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId));
	}

	public Mono<CreditResponseDto> saveNewAccount(Credit credit, String message) {
		return creditRepository.save(credit).flatMap(x -> {
			return Mono.just(new CreditResponseDto(credit, message));
		});
	}
	
	public Mono<CreditResponseDto> updateAccount(Credit credit, Double amount, String typeTransaction) {
		return creditRepository.save(credit).flatMap(x -> {
			return registerTransaction(credit, amount, typeTransaction);
		});
	}
	
	public Mono<Credit> getCreditByIdCustomerPerson(String customerId, String type, String customer) {
		Flux<Credit> r = creditRepository.findAll()
				.filter(c -> c.getCustomerId().equals(customerId))
				.filter(c -> c.getDescripTypeAccount().equals("CRED_PERSONAL"))
				.filter(c -> c.getTypeCustomer().equals(customer));
		Mono<Credit> m= r.next();
		return m;
	}
	
	private Mono<CreditResponseDto> registerTransaction(Credit credit, Double amount, String typeTransaction){
		Transaction transaction = new Transaction();
		transaction.setCustomerId(credit.getCustomerId());
		transaction.setProductId(credit.getId());
		transaction.setProductType(credit.getDescripTypeAccount());
		transaction.setTransactionType(typeTransaction);
		transaction.setAmount(amount);
		transaction.setTransactionDate(new Date());
		transaction.setCustomerType(credit.getTypeCustomer());
		return transactionRestClient.createTransaction(transaction).flatMap(t -> {
			return Mono.just(new CreditResponseDto(credit, "Successful transaction"));
        });
	}

}
