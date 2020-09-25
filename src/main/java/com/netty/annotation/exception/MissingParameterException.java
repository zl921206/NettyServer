package com.netty.annotation.exception;

public class MissingParameterException extends Exception {

	private static final long serialVersionUID = 1L;

	public MissingParameterException(String msg) {
		super(msg);
	}
}
