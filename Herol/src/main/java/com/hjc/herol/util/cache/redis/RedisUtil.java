package com.hjc.herol.util.cache.redis;

import org.apache.commons.logging.Log;

import com.hjc.herol.herolrouter.core.GameInit;
import com.hjc.herol.util.Helper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisUtil extends Helper<RedisUtil> {
	private static RedisUtil instance;
	public static String password = null;
	public static final int GLOBAL_DB = 0;// 全局
	
	private JedisPool pool;
	public String host;
	public int port;
	
	public static RedisUtil getInstance() {
		if (instance == null) {
			instance = new RedisUtil();
		}
		return instance;
	}
	
	public void init() {
		String redisServer = null;
		if (GameInit.cfg != null) {
			redisServer = GameInit.cfg.get("redisServer");
			password = GameInit.cfg.get("redisPwd");
		}
		
		if (redisServer == null) {
			redisServer = "127.0.0.1:6379";
		}
		
		String tmp[] = redisServer.split(":");
		host = tmp[0];
		port = Integer.parseInt(tmp[1]);
		if (tmp.length == 2) {
			port = Integer.parseInt(tmp[1].trim());
		}
		log.info("Redis at {}:{}", host, port);
		pool = new JedisPool(host, port);
	}
	
	public Jedis getRedis() {
		return pool.getResource();
	}
	
	public String hget(int db, String key, String field){
		if (key == null) {
			return null;
		}
		Jedis redisJedis = this.pool.getResource();
		redisJedis.auth(password);
		redisJedis.select(db);
		String ret = redisJedis.hget(key, field);
		this.pool.close();
		return ret;
	}
}
