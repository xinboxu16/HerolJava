package com.hjc.herol.util.cache;

import java.io.Serializable;

/**
 * 实现此接口后还需要再memcacheutil类中增加cachedClass，并使用
 * com.qx.persistent.HibernateUtil.find(Class<T>, long)
 * 代替where进行查询。
 * 需要对控制了的对象在第一次存库时调用memcacheutil.add，再调用HIbernateUtil.insert
 *
 */
public interface MemcacheSupport extends Serializable{
	long getIdentifier();
}
