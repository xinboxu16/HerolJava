package com.hjc.herol.exception;

import com.hjc.herol.exception.impl.ExceptionFactory;
import com.hjc.herol.util.Constants;

public class ExceptionUtils {
	public static Exception createException(Constants.ExceptionType eType, String message)
	{
		IExceptionFactory iFactory = new ExceptionFactory();
		return iFactory.createException(eType, message);
	}
}
