package com.hjc.herol.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class SpringUtil {
	private static final String path = "classpath*:applicationContext.xml";
	private static ApplicationContext applicationContext;
	
	public static void init() {
		applicationContext = new FileSystemXmlApplicationContext(path);
		//ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	public static <T> T getBean(String name) {
		return (T)applicationContext.getBean(name);
	}
}
