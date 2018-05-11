package com.hjc.herol.util.mongo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MongoUtil {
	
	private static final Map<String, MongoUtil> instances = new ConcurrentHashMap<String, MongoUtil>();
	
	/**
	 * 实例化
	 * 
	 * @return MongoDBManager对象
	 */
	static {
		// 初始化默认的MongoDB数据库
		getInstance("db");
	}
	
	public static MongoUtil getInstance()
	{
		return getInstance("db");
	}
	
	public static MongoUtil getInstance(String dbName)
	{
		MongoUtil mongoMgr = instances.get(dbName);
		if (mongoMgr == null)
		{
			mongoMgr = buildInstance(dbName);
			if(mongoMgr == null)
			{
				return null;
			}
			instances.put(dbName, mongoMgr);
		}
		return mongoMgr;
	}
	
	private static synchronized MongoUtil buildInstance(String dbName)
	{
		return null;
	}
}
