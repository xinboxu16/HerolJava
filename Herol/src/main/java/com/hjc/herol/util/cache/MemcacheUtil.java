package com.hjc.herol.util.cache;

import java.util.HashSet;
import java.util.Set;

import com.hjc.herol.util.cache.memcached.MemcachedCRUD;

public class MemcacheUtil {
	/**
	 * 控制哪些类进行memcached缓存。 被控制的类在进行创建时，需要注意调用MemcacheUtil的add和hibernate的insert。
	 */
	public static Set<Class<? extends MemcacheSupport>> cachedClass = new HashSet<Class<? extends MemcacheSupport>>();

	public static boolean add(Object t, long id)
	{
		if (!cachedClass.contains(t.getClass())) {
			return false;
		}
		String cString = t.getClass().getSimpleName();
		String key = cString + "#" + id;
		return MemcachedCRUD.getInstance().add(key, t);
	}
	
	public static void update(Object t, long id) {
		if (!cachedClass.contains(t.getClass())) {
			return;
		}
		//得到类的简写名称
		String cString = t.getClass().getSimpleName();
		String key = cString + '#' + id;
		MemcachedCRUD.getInstance().update(key, t);
	}
	
	public static <T> T get(Class<T> t, long id)
	{
		if (!cachedClass.contains(t)) {
			return null;
		}
		String c = t.getSimpleName();
		String keyString = c+"#"+id;
		Object object = MemcachedCRUD.getInstance().getObject(keyString);
		return (T)object;
	}
}
