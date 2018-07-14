package com.hjc.herol.util.cache;

import java.util.HashSet;
import java.util.Set;

public class MemcacheUtil {
	/**
	 * 控制哪些类进行memcached缓存。 被控制的类在进行创建时，需要注意调用MemcacheUtil的add和hibernate的insert。
	 */
	public static Set<Class<? extends MemcacheSupport>> cachedClass = new HashSet<Class<? extends MemcacheSupport>>();
}
