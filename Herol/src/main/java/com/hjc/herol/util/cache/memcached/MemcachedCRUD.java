package com.hjc.herol.util.cache.memcached;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.hjc.herol.herolrouter.core.GameInit;
import com.hjc.herol.util.Helper;

import net.rubyeye.xmemcached.GetsResponse;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator;
import net.rubyeye.xmemcached.utils.AddrUtil;

public class MemcachedCRUD extends Helper<MemcachedCRUD>{
	
	/**
     * 批量请求，每批次请求数量
     */
    private final static int MERGE_FACTOR = 20;
    
    /**
     * 链接超时时间
     */
    private final static int CONNECT_TIMEOUT = 500;
    
    /**
     * 超时时间的配置 单位：毫秒
     */
    protected static final long REQUEST_TIME_OUT = 1000;
    
    /**
     * 步长
     */
    private final long STEP = 1L;
    
    /**
     * 默认值
     */
    private final long DEFAULTVALUE = 0;
	
	protected static MemcachedCRUD memcachedCRUD = new MemcachedCRUD();
	public static String poolName = "gameDBPool";
	public static MemcachedClientBuilder builder;
	protected static MemcachedClient memcachedClient;
	static {
		try {
			builder = init(poolName, "cacheServer");
			memcachedClient = builder.build();
			// if true, then store all primitives as their string value.
			memcachedClient.setPrimitiveAsString(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static XMemcachedClientBuilder init(String poolName, String confKey) {
		// 缓存服务器
		String cacheServers = GameInit.cfg.getServerByName(confKey);
		String server = "127.0.0.1:11211";
		if (cacheServers != null && "".equals(cacheServers)) {
			server = cacheServers;
		}
		
		// 创建一个连接池
		XMemcachedClientBuilder clientBuilder = new XMemcachedClientBuilder(AddrUtil.getAddresses(server));
		clientBuilder.setName(poolName);
		log.info("连接池{}缓存配置 {}", poolName, server);
		// 宕机报警  
		clientBuilder.setFailureMode(true);
		clientBuilder.setCommandFactory(new BinaryCommandFactory());
		clientBuilder.setSessionLocator(new KetamaMemcachedSessionLocator());
		clientBuilder.setConnectionPoolSize(1);
		//设置连接超时时间(2分钟)，默认1分钟，单位毫秒
		clientBuilder.setConnectTimeout(2*60*1000);
		//设置操作超时时间(3秒)，默认1秒，单位毫秒
		clientBuilder.setOpTimeout(3*1000);
		clientBuilder.setEnableHealSession(true);
		 // 网络关闭，失败，重试间隔时间间隔，10毫秒
		clientBuilder.setHealSessionInterval(10);
		return clientBuilder;
	}
	
	private MemcachedCRUD(){
		
	}
	
	public static MemcachedCRUD getInstance() {
		return memcachedCRUD;
	}
	
	public void destroy() {
		try {
			if (!memcachedClient.isShutdown()) {
				memcachedClient.shutdown();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
     * 根据key值从IMemcachedClient获取缓存对象
     *
     * @param key 缓存的Key
     * @param obj Object
     * @param time int
     * @return 缓存对象
     */
	public boolean add(String key, Object data)
	{
		// 是否添加成功
		boolean ret = false;
		try {
			ret = memcachedClient.add(key, 0, data);
		} catch (Exception e) {
			log.error("add memcached faield : ", key, e);
		}
		return ret;
	}
	
	/**
     * 根据key值和更新对象，在Memcached中更新缓存对象
     *
     * @param key 缓存的Key
     * @param data 缓存对象
     * @return 是否成功更新
     */
	public boolean update(String key, Object data) {
		boolean ret = false;
		try {
			GetsResponse<Object> result = memcachedClient.gets(key);
			if (null != result) {
				//第一个是存储的 key名称 
				//第二个是expire时间（单位秒） 0  表示永久存储（默认是一个月）
				// 第三个参数就是实际存储的数据，可以是任意的java可序列化类型
				ret = memcachedClient.cas(key, 0, data, result.getCas());
			}
		} catch (TimeoutException | InterruptedException | MemcachedException e) {
			log.error("更新memcache失败 {} {}", key, e);
		}
		return ret;
	}
	
	/**
     * 根据key值和更新对象，在Memcached中更新缓存对象 与updateObject不同之处在于不需要乐观锁判断，避免并发更新同一个主键对象失败
     *
     * @param key 缓存的Key
     * @param data 缓存对象
     * @param time int
     * @return 是否成功更新
     */
	public boolean update2(String key, Object data)
	{
		boolean result = false;
		try {
			result = memcachedClient.set(key, 0, data);
		} catch (Exception e) {
			log.error("update memcached faield : ", key, e);
		}
		return result;
	}
	
	/**
     * 根据key值从MemcachedClient获取缓存对象
     *
     * @param key 缓存的Key
     * @return 缓存对象
     */
	public Object getObject(String key) {
		Object object = null;
		try {
			object = memcachedClient.get(key);
		} catch (TimeoutException | InterruptedException | MemcachedException e) {
			log.error("get memcached faield : ", key, e);
		}
		return object;
	}
	
	/**
     * 缓存累加器
     *
     * @param key 缓存key
     * @param initValue 初始化的值
     * @param invalidDurance 失效时间
     * @return
     */
	public long incr(String key, long initValue, int invalidDurance){
		int expire = 0;
        if (0 != invalidDurance)
        {
            expire = (int)(System.currentTimeMillis() / 1000) + invalidDurance;
        }
        try
        {
            long result = memcachedClient.incr(key, STEP, initValue, REQUEST_TIME_OUT, expire);
            return result;
        }
        catch (Exception e)
        {
            log.error("incr memcached faield : ", key, e);
        }
        return 0;
	}
	
	/**
     * 递减计数器
     *
     * @author wulinfeng
     * @param key 缓存key
     * @param invalidDurance 失效时间
     * @return
     */
    public long decr(String key, int invalidDurance)
    {
        int expire = 0;
        if (0 != invalidDurance)
        {
            expire = (int)(System.currentTimeMillis() / 1000) + invalidDurance;
        }
        try
        {
            long result = memcachedClient.decr(key, STEP, DEFAULTVALUE, REQUEST_TIME_OUT, expire);
            return result;
        }
        catch (Exception e)
        {
        	log.error("incr memcached faield : ", key, e);
        }
        return 0;
    }
    
    /**
     * 根据key值从IMemcachedClient删除缓存对象
     *
     * @param key 缓存的Key
     * @return 是否成功删除
     */
    public boolean deleteObject(String key)
    {
        try
        {
            boolean result = memcachedClient.delete(key, REQUEST_TIME_OUT);
            return result;
        }
        catch (Exception e)
        {
        	log.error("delete memcached faield : ", key, e);
        }
        return false;
    }
    
    /**
     * 根据key值从IMemcachedClient获取缓存对象
     *
     * @param key 缓存的Key
     * @return 缓存对象
     */
    public Object getsObject(String key)
    {
        try
        {
            GetsResponse result = memcachedClient.gets(key, REQUEST_TIME_OUT);
            return result;
        }
        catch (Exception e)
        {
        	log.error("gets memcached faield : ", key, e);
        }
        return null;
    }
}
