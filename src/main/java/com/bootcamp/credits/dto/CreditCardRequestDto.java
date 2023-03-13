package com.bootcamp.credits.dto;

import java.util.Date;

import lombok.Data;

@Data
public class CreditCardRequestDto {
	private String id;
	private String customerId;
	private Integer typeAccount;
	private String descripTypeAccount;
	private Double creditAmount;
	private Double existingAmount;
	private Date creditDate;
	private String numberCard;
	private String typeCustomer;
	private Double amount;
}
