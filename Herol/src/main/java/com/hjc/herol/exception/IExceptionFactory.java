package com.hjc.herol.exception;

import com.hjc.herol.util.Constants;

//异常类工厂模式
public interface IExceptionFactory {
	public Exception createException(Constants.ExceptionType eType, String message);
}

