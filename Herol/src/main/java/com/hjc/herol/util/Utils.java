package com.hjc.herol.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
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
}
