package com.hjc.herol.util;

import java.util.Properties;

public class Config {
	public String SERVER_CONFIG_PATH = "/server.properties"; // 服务器的配置路径
	
	/**
	 * @Description: 2015年5月7日合并getLockServerList，getCacheServerList，getRouterCacheServerList
	 * 				 直接通过serverName获取server地址
	 * @param serverName
	 * @return
	 */
	public String getServerByName(String serverName) {
		return Utils.getProperty(SERVER_CONFIG_PATH, serverName);
	}
	
	public String get(String key) {
		String retString = Utils.getProperty(SERVER_CONFIG_PATH, key);
		if (null != retString) {
			retString = retString.trim();
		}
		return retString;
	}
}
