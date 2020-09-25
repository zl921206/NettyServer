package com.netty.annotation.exception;

public class RequestMethodNotSupportException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public RequestMethodNotSupportException(String msg) {
		super(msg);
	}
	
	public RequestMethodNotSupportException() {
	}
	
}
