package com.bootcamp.credits.dto;

import com.bootcamp.credits.entity.CreditCard;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CreditCardResponseDto {
	private CreditCard creditcard;
	private String message;
}
