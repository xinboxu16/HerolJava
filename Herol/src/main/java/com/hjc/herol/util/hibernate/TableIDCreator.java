package com.hjc.herol.util.hibernate;

import java.io.IOException;

import com.hjc.herol.util.Helper;
import com.hjc.herol.util.cache.memcached.MemcachedCRUD;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;

public class TableIDCreator extends Helper<TableIDCreator>{
	public static String poolName = "TableIdDbPool";
	public static XMemcachedClientBuilder builder = MemcachedCRUD.init(poolName, "cacheServer");
	public static MemcachedClient memcachedClient;
	static {
		try {
			builder.setName(poolName);
			memcachedClient = builder.build();
			memcachedClient.setPrimitiveAsString(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static <T> long getTableID(Class<T> clazz, long startId) {
		// 表的主键ID从1开始
		Long id = null;
		try {
			String key = clazz.getName() + "#id";
			//Xmemcached还提供了一个称为计数器的封装，它封装了incr/decr方法，使用它就可以类似AtomicLong那样去操作计数：
			//getCounter的第二个参数是计数器的初始值。
			if (memcachedClient.getCounter(key) == null) {
				// 从数据库里查询该表当前主键的最大值
				id = HibernateUtil.getTableIDMax(clazz);
				if (id == null) {
					boolean ret = memcachedClient.set(key, 0, startId);
					log.info("A开始为table:{}设置主键ID:{} ret {}", key, startId, ret);
				}else {
					// 即便数据库有记录，也比较该id是否满足参数startId的要求。
					boolean ret = memcachedClient.set(key, 0, Math.max(startId, id));
					log.info("B开始为table:{}设置主键ID:{} ret {}", key, id, ret);
				}
			}
			id = memcachedClient.incr(key, 1);
			if (id == -1) {
				log.error("table:{}主键增加失败", key);
				return -1;
			}else {
				log.info("table:{}的ID加1增长为{}", key, id);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}
}
