package com.bootcamp.credits.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document(collection="creditcard")
public class CreditCard {
	@Id
	private String id;
	@NotEmpty
	private String customerId;
	@NotEmpty
	@JsonIgnore
	private Integer typeAccount;
	private String descripTypeAccount;
	@NotEmpty
	private Double creditAmount;
	@NotEmpty
	private Double existingAmount;
	private LocalDateTime creditDate;
	@NotEmpty
	private String numberCard;
	private String typeCustomer;
}
