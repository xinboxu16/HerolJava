package com.hjc.herol.util.mongo;

import java.io.InputStream;
import java.util.Properties;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.omg.CORBA.portable.UnknownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;

//Morphia 是一个针对Mongo和Java 对象转换的映射的轻量级ORM类型的安全类库
public class MorphiaUtil {
	public static Logger logger = LoggerFactory.getLogger(MorphiaUtil.class);
	public static Datastore ds;
	private static final String CONF_PATH = "/spring-mongodb/mongodb.properties";
	public static String dbName = "db";
	
	public static Datastore getDatastore()
	{
		if(ds == null)
		{
			ds = buildDatastore();
		}
		return ds;
	}
	
	public static Datastore buildDatastore()
	{
		MongoClient mongo = null;
		try
		{
			String hosts = getProperty(CONF_PATH, dbName + ".host");
			String[] hostArray = hosts.split(":");
			mongo = new MongoClient(new ServerAddress(hostArray[0], Integer.parseInt(hostArray[1])), getDBOptions(dbName));
		}catch(UnknownException e)
		{
			e.printStackTrace();
		}
		Morphia morphia = new Morphia();
		//告诉Morphia去扫描一个包,映射包中的所有类
		morphia.mapPackage("com.hjc.herol");
		ds = morphia.createDatastore(mongo, "herol");
		//调用 Datastore.ensureIndexes() 使你的索引生效。
		ds.ensureIndexes();
		if(ds == null)
		{
			logger.error("mongo connect failed");
		}
		return ds;
	}
	
	/**
	 * 根据properties文件的key获取value
	 * 
	 * @param filePath
	 *            properties文件路径
	 * @param key
	 *            属性key
	 * @return 属性value
	 */
	private static String getProperty(String filePath, String key)
	{
		Properties props = new Properties();
		try
		{
			InputStream in = MongoUtil.class.getResourceAsStream(filePath);
			props.load(in);
			String value = props.getProperty(key);
			return value;
		}catch(Exception e)
		{
			logger.info("load mongo properties exception {}", e);
			System.exit(0);
			return null;
		}
	}
	
	/**
	 * @Title: getDBOptions
	 * @Description: 获取数据参数设置
	 * @return
	 * @return MongoClientOptions
	 * @throws
	 */
	private static MongoClientOptions getDBOptions(String dbName)
	{
		MongoClientOptions.Builder build = new MongoClientOptions.Builder();
		// 与目标数据库能够建立的最大connection数量为50
		build.connectionsPerHost(Integer.parseInt(getProperty(CONF_PATH, dbName + ".connectionsPerHost")));
		// 如果当前所有的connection都在使用中，则每个connection上可以有50个线程排队等待
		build.threadsAllowedToBlockForConnectionMultiplier(Integer.parseInt(getProperty(CONF_PATH, dbName + ".threadsAllowedToBlockForConnectionMultiplier")));
		build.maxWaitTime(Integer.parseInt(getProperty(CONF_PATH, dbName + ".maxWaitTime")));
		build.connectTimeout(Integer.parseInt(getProperty(CONF_PATH, dbName + ".connectTimeout")));
		MongoClientOptions myOptions = build.build();
		return myOptions;
	}
}
