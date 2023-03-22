package com.bootcamp.credits.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CreditRequestDto {
	private String id;
	private String customerId;
	private Integer typeAccount;
	private String descripTypeAccount;
	private Double creditAmount;
	private Double existingAmount;
	private LocalDateTime creditDate;
	private String typeCustomer;
	private Double amount;
}
