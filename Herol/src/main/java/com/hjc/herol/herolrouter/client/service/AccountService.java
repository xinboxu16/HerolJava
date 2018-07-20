package com.hjc.herol.herolrouter.client.service;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.hjc.herol.herolrouter.client.model.Account;
import com.hjc.herol.util.Constants;
import com.hjc.herol.util.StringUtils;
import com.hjc.herol.util.cache.MemcacheUtil;
import com.hjc.herol.util.hibernate.HibernateUtil;
import com.hjc.herol.util.hibernate.TableIDCreator;

@Service
public class AccountService {
	
	public Account getAccount(String username) {
		String nameString = StringUtils.escapeSql(username);
		Account account = null;
		try {
			account = HibernateUtil.find(Account.class, "where name='" + nameString + "'");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return account;
	}
	
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
		
		Account account = null;
		try {
			account = HibernateUtil.find(Account.class, "where name='"+name+"'");
			if (account != null) {
				// 登录
				account = HibernateUtil.find(Account.class, "where name='" + name + "' and pwd='" + pwd + "'");
				if (account == null) {
					return Constants.ACCOUNT_PASSWORD_ERROR;
				}
				account.setLastlogintime(new Date());
				HibernateUtil.save(account);
				return Constants.ACCOUNT_LOGIN_SUCCESS;
			}else {
				if (pwd.length() < 6) {
					return Constants.ACCOUNT_PASSWORD_LESS;
				}
				Account newAccount = new Account();
				newAccount.setId(TableIDCreator.getTableID(Account.class, 1));
				newAccount.setName(name);
				newAccount.setPwd(pwd);
				newAccount.setChannel(channel);
				newAccount.setCreatetime(new Date());
				newAccount.setLastlogintime(new Date());
				
				MemcacheUtil.add(newAccount, newAccount.getIdentifier());
				HibernateUtil.insert(newAccount);
				return Constants.ACCOUNT_REGISTER_SUCCESS;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * @Title: regist
	 * @Description:
	 * @param username
	 * @param password
	 * @return int 0-注册成功，1-账号已存在，2-密码至少8位
	 * @throws
	 */
	public int regist(String username, String password, int channel) {
		String name = StringUtils.escapeSql(username);
		String pwd = StringUtils.escapeSql(password);
		
		try {
			if (HibernateUtil.find(Account.class, "where name='"+name+"'") != null) {
				return 1;
			}
			if (pwd.length() < 8) {
				return 2;
			}
			Account account = new Account();
			account.setId(TableIDCreator.getTableID(Account.class, 1));
			account.setName(name);
			account.setPwd(pwd);
			account.setChannel(channel);
			account.setCreatetime(new Date());
			MemcacheUtil.add(account, account.getIdentifier());
			HibernateUtil.insert(account);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
	
	/**
	 * @Title: login
	 * @Description: 登录
	 * @param username
	 * @param password
	 * @return int 0-成功，1-账号不存在，2-密码不正确
	 * @throws
	 */
	public int login(String username, String password, int channel) {
		String name = StringUtils.escapeSql(username);
		String pwd = StringUtils.escapeSql(password);
		try {
			if (HibernateUtil.find(Account.class, "where name='" + name + "'") == null) {
				return 1;
			}
			if (HibernateUtil.find(Account.class, "where name='" + name
					+ "' and pwd='" + pwd + "'") == null) {
				return 2;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return 0;
	}
}
