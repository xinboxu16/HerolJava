package com.hjc.herol.herolrouter.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hjc.herol.herolrouter.client.RouteController;
import com.hjc.herol.util.Config;
import com.hjc.herol.util.SpringUtil;
import com.hjc.herol.util.cache.redis.RedisUtil;
import com.hjc.herol.util.hibernate.HibernateUtil;

public class GameInit {
	private static Logger log = LoggerFactory.getLogger(GameInit.class);
	// 配置文件根目录
	public static String confFileBasePath = "/";
	// 读取server.properties配置
	public static Config cfg;
	
	public static void init() {
		log.info("================开启管理服务器================");
		// 加载服务器配置文件
		log.info("加载服务器配置文件");
		cfg = new Config();
		final RedisUtil redisUtil = RedisUtil.getInstance();
		//加载spring
		SpringUtil.init();
		// 加载hibernate
		log.info("加载hibernate");
		HibernateUtil.init();
		log.info("================完成开启管理服务器================");
	}
	
	public static void destroy() {
		log.info("================注销================");
		HibernateUtil.destroy();
	}
	
	public static void main(String args[]) {
		init();
		
		//注册
		RouteController controller = new RouteController();
		controller.regist("aaa", "123456");
		
		destroy();
	}
}
