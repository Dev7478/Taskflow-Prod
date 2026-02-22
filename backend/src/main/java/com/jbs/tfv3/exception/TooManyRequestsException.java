package com.jbs.tfv3.exception;

public class TooManyRequestsException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TooManyRequestsException(String msg) {
		super(msg);
	}
}
