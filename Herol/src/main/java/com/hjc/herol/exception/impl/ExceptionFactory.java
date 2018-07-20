package com.hjc.herol.exception.impl;

import com.hjc.herol.exception.AbstractException;
import com.hjc.herol.exception.IExceptionFactory;
import com.hjc.herol.util.Constants.ExceptionType;

public class ExceptionFactory implements IExceptionFactory{

	public RuntimeException createException(ExceptionType eType, String message) {
		AbstractException exception = null;
		if (ExceptionType.StringError == eType) {
			exception = new StringException();
			exception.setMessage(message);
		}
		return exception.createException();
	}

}

class StringException extends AbstractException {

	public RuntimeException createException()
	{
		return new RuntimeException(message);
	}
	
}
