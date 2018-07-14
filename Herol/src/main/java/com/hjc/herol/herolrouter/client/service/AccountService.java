package com.hjc.herol.herolrouter.client.service;

import com.hjc.herol.util.StringUtils;

public class AccountService {
	/**
	 * @Title: loginOrRegist
	 * @Description: 登录或注册
	 * @param username
	 * @param password
	 * @param channel
	 * @return int 100-登录成功，101-登录密码错误，200-注册成功，201-注册密码小于6位
	 * @throws
	 */
	public int loginOrRegist(String username, String password, int channel) {
		String name = StringUtils.escapeSql(username);
		String pwd = StringUtils.escapeSql(password);
		
		Account account = HibernateUtil.find();
	}
}
