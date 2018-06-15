package com.hjc.herol.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hjc.herol.util.encrypt.XXTeaCoder;

public class Utils {
	public static Logger log = LoggerFactory.getLogger(Utils.class);
	private static Map<String, Logger> loggerMap = new HashMap<String, Logger>();

	public static Logger getLoggerObj(Class<?> classValue)
	{
		String className = classValue.getName();
		if (!loggerMap.containsKey(className)) {
			Logger log = LoggerFactory.getLogger(classValue);
			loggerMap.put(className, log);
		}
		return loggerMap.get(className);
	}
	
	public static StringBuffer exceptionTrace(Class<?> cls, Exception e) {
		Logger log = LoggerFactory.getLogger(cls);
		//log.error("error msg:", e);
		e.printStackTrace();
		// Print our stack trace
		StringBuffer eBuffer = new StringBuffer(e.getMessage() + ",");
		StackTraceElement[] trace = e.getStackTrace();
		for (StackTraceElement traceElement : trace) {
			eBuffer.append("\r\n " + traceElement);
		}
		log.error("post error msg:", eBuffer.toString());
		return eBuffer;
	}
	
	/**
	 * @Title: codeFilter
	 * @Description: 编解码过滤
	 * @param val
	 * @return String
	 * @throws
	 */
	public static String codeFilter(String val, boolean ENCRIPT_DECRIPT) {
		try {
			val = val.contains("%") ? URLDecoder.decode(val, "UTF-8") : val;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String valTmp = val;
		val = ENCRIPT_DECRIPT ? XXTeaCoder.decryptBase64StringToString(val,
				XXTeaCoder.key) : val;
		if (Constants.MSG_LOG_DEBUG) {
			if (val == null) {
				val = valTmp;
			}
			log.info("server received : {}", val);
		}
		return val;
	}
}
