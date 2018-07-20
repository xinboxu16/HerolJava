package com.hjc.herol.herolrouter.core;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	public static final Logger logger = LoggerFactory.getLogger(CoreServlet.class);
	
	public CoreServlet() {
		super();
	}
	
	@Override
	public void service(ServletRequest req, ServletResponse res) {
		logger.info("servlet service");
	}
	
	@Override
	public void destroy() {
		logger.info("CoreServelet destroy");
	}
	
	@Override
	public void init() {
		logger.info("CoreServelet init");
		GameInit.init();// 初始化登录服务器
	}
}
