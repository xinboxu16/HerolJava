package com.hjc.herol.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Helper<T> {
	public Logger log;
	private Class<T> tClass;
	
	@SuppressWarnings("unchecked")
	public Helper()
	{
		ParameterizedType parameterizedType = ((ParameterizedType)getClass().getGenericSuperclass());
		Type[] t = parameterizedType.getActualTypeArguments();
		tClass = (Class<T>) t[0];
		
		log = LoggerFactory.getLogger(tClass);
	}
	
	/**
	 * 读取socket配置
	 */
	protected Properties readProperties(String name) throws IOException{
		Properties properties = new Properties();
		InputStream inputStream = tClass.getResourceAsStream(name);
		Reader reader =  new InputStreamReader(inputStream, Charset.forName("UTF-8"));
		properties.load(reader);
		inputStream.close();
		return properties;
		
	}
}
