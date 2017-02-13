package com.silverbar.orders.exception;

public class OrderNotFoundException extends RuntimeException {

	private static final long serialVersionUID = -8236792653873870536L;
	
	private static final String MESSAGE = "No matching order found";
	
	public OrderNotFoundException(){
		super(MESSAGE);
	}

}
