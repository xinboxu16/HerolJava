package com.hjc.herol.util;

public class Constants {
	/**日志记录配置**/
	public static boolean MSG_LOG_DEBUG = true;
	public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";  
	public static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
	public static final int HTTP_CACHE_SECONDS = 60;
	public static final int ACCOUNT_LOGIN_SUCCESS = 100;
	public static final int ACCOUNT_PASSWORD_ERROR = 101;
	public static final int ACCOUNT_REGISTER_SUCCESS = 200;
	public static final int ACCOUNT_PASSWORD_LESS = 201;
	
	public enum ExceptionType
	{
		None,
		StringError
	}
}
