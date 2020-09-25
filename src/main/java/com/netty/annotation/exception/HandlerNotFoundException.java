package com.netty.annotation.exception;

public class HandlerNotFoundException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public HandlerNotFoundException(String msg) {
		super(msg);
	}
	
	public HandlerNotFoundException() {
	}
	
}
