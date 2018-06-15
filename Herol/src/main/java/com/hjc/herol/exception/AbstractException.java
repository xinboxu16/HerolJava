package com.hjc.herol.exception;

public abstract class AbstractException {
	public String message = "";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public Exception createException()
	{
		return null;
	}
}
