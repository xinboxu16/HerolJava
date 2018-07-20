package com.hjc.herol.util.hibernate;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.alibaba.fastjson.JSONObject;
import com.hjc.herol.exception.ExceptionUtils;
import com.hjc.herol.util.Constants;
import com.hjc.herol.util.Helper;
import com.hjc.herol.util.SpringUtil;
import com.hjc.herol.util.cache.MemcacheSupport;
import com.hjc.herol.util.cache.MemcacheUtil;

/**
 * http://wiki.jikexueyuan.com/list/java/
 *  配置对象
	配置对象是你在任何 Hibernate 应用程序中创造的第一个 Hibernate 对象，并且经常只在应用程序初始化期间创造。它代表了 Hibernate 所需一个配置或属性文件。配置对象提供了两种基础组件。
	
	数据库连接：由 Hibernate 支持的一个或多个配置文件处理。这些文件是 hibernate.properties 和 hibernate.cfg.xml。
	类映射设置：这个组件创造了 Java 类和数据库表格之间的联系。
	
	SessionFactory 对象
	配置对象被用于创造一个 SessionFactory 对象，使用提供的配置文件为应用程序依次配置 Hibernate，并允许实例化一个会话对象。SessionFactory 是一个线程安全对象并由应用程序所有的线程所使用。
	SessionFactory 是一个重量级对象所以通常它都是在应用程序启动时创造然后留存为以后使用。每个数据库需要一个 SessionFactory 对象使用一个单独的配置文件。所以如果你使用多种数据库那么你要创造多种 SessionFactory 对象。
	
	Session 对象
	一个会话被用于与数据库的物理连接。Session 对象是轻量级的，并被设计为每次实例化都需要与数据库的交互。持久对象通过 Session 对象保存和检索。
	Session 对象不应该长时间保持开启状态因为它们通常情况下并非线程安全，并且它们应该按照所需创造和销毁。
	
	Transaction 对象
	一个事务代表了与数据库工作的一个单元并且大部分 RDBMS 支持事务功能。在 Hibernate 中事务由底层事务管理器和事务（来自 JDBC 或者 JTA）处理。
	这是一个选择性对象，Hibernate 应用程序可能不选择使用这个接口，而是在自己应用程序代码中管理事务。
	
	Query 对象
	Query 对象使用 SQL 或者 Hibernate 查询语言（HQL）字符串在数据库中来检索数据并创造对象。一个查询的实例被用于连结查询参数，限制由查询返回的结果数量，并最终执行查询。
 	
 	Criteria 对象
	Criteria 对象被用于创造和执行面向规则查询的对象来检索对象。
 * @author lenovo
 *
 */

public class HibernateUtil extends Helper<HibernateUtil>{
	public static Map<Class<?>, String> beanKeyMap = new HashMap<Class<?>, String>();
	
	/**
	 * SessionFactory 对象
		配置对象被用于创造一个 SessionFactory 对象，使用提供的配置文件为应用程序依次配置 Hibernate，并允许实例化一个会话对象。SessionFactory 是一个线程安全对象并由应用程序所有的线程所使用。
		SessionFactory 是一个重量级对象所以通常它都是在应用程序启动时创造然后留存为以后使用。每个数据库需要一个 SessionFactory 对象使用一个单独的配置文件。所以如果你使用多种数据库那么你要创造多种 SessionFactory 对象。
	 */
	private static SessionFactory sessionFactory;
	
	public static void init() {
		sessionFactory = buildSessionFactory();
	}
	
	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	/**
	 * classpath*:applicationContext.xml会加入本项目及所有jar包根目录下的applicationContext.xml文件，跨jar包依赖时使用 
	   classpath:applicationContext.xml只加入本项目根目录下的applicationContext.xml文件，不依赖其它jar包的配置文件时推荐这样写，以避免冲突。
	 * @return
	 */
	public static SessionFactory buildSessionFactory() {
		log.info("开始构建hibernate");
		sessionFactory = SpringUtil.getBean("sessionFactory");
		log.info("结束构建hibernate");
		
		return sessionFactory;
	}
	
	public static String getKeyField(Class<?> field) {
		synchronized (beanKeyMap) {
			String key = beanKeyMap.get(field);
			if (null != key) {
				return key;
			}
			Field[] fs = field.getDeclaredFields();
			for (Field f : fs) {
				//isAnnotationPresent无参的那个方法判断是否存在注解，而这个api判断是否存在指定Class的注解
				if (f.isAnnotationPresent(javax.persistence.Id.class)) {
					key = f.getName();
					beanKeyMap.put(field, key);
					break;
				}
			}
			return key;
		}
	}
	
	public static <T> T find(Class<T> t, long id) throws Exception {
		String keyField = getKeyField(t);
		if (keyField == null) {
			throw new RuntimeException("类型" + t + "没有标注主键");
		}
		if (!MemcacheUtil.cachedClass.contains(t)) {
			return find(t, "where "+keyField+"="+id, false);
		}
		T ret = MemcacheUtil.get(t, id);
		if (ret == null) {
			log.info("MC未命中{}#{}", t.getSimpleName(), id);
			ret = find(t, "where "+keyField+"="+id, false);
			if (ret != null) {
				log.info("DB命中{}#{}", t.getSimpleName(), id);
				MemcacheUtil.add(ret, id);
			} else {
				log.info("DB未命中{}#{}", t.getSimpleName(), id);
			}
		} else {
			log.info("MC命中{}#{}", t.getSimpleName(), id);
		}
		return ret;
	}
	
	public static <T> T find(Class<T> t, String where) throws Exception {
		return find(t, where, true);
	}
	
	public static <T> T find(Class<T> t, String where, boolean checkMCControl) throws Exception {
		if (checkMCControl && MemcacheUtil.cachedClass.contains(t)) {
			// 请使用static <T> T find(Class<T> t,long id)
			//为何RuntimeException不需要捕获 http://ku-uga.iteye.com/blog/814275
			throw ExceptionUtils.createException(Constants.ExceptionType.StringError, "由MemcacheUtil控制的类不能直接查询DB:" + t);
		}
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		T ret = null;
		try {
			//FIXME 使用 session的get方法代替。
			String hqlString = "from " + t.getSimpleName() + " " + where;
			Query<?> query = session.createQuery(hqlString);
			//当确定返回的实例只有一个或者null时 用uniqueResult()方法
			ret = (T)query.uniqueResult();
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			log.error("list fail for {} {}", t, where);
			log.error("list fail", e);
			throw new Exception("查询异常");
		}
		return ret;
	}
	
	public static boolean insert(Object obj)
	{
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		try {
			session.save(obj);
			session.getTransaction().commit();
		} catch (Exception e) {
			log.error("要insert的数据{}", obj == null ? "null" : JSONObject.toJSON(obj).toString());
			log.error("保存出错", e);
			session.getTransaction().rollback();
			return false;
		}
		return true;
	}
	
	public static boolean save(Object o) {
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		boolean mcOk = false;
		try {
			if (o instanceof MemcacheSupport) {
				// 需要对控制了的对象在第一次存库时调用MC.add
				MemcacheSupport support = (MemcacheSupport) o;
				// MC中控制了哪些类存缓存。
				MemcacheUtil.update(o, support.getIdentifier());
				mcOk = true;
				session.update(o);
			} else {
				session.saveOrUpdate(o);
			}
			transaction.commit();
		} catch (Exception e) {
			log.error("要save的数据{},{}", o, o == null ? "null" : JSONObject.toJSON(o).toString());
			if (mcOk) {
				log.error("memcacheUtil保存成功后报错，可能是数据库条目丢失。");
			}
			log.error("保存出错", e);
			mcOk = false;
			transaction.rollback();
		}
		return mcOk;
	}
	
	/**
	 * @param t
	 * @param where
	 *            例子： where uid>100
	 * @return
	 */
	public static <T> List<T> list(Class<T> t, String where) {
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		List<T> list = Collections.EMPTY_LIST;
		try {
			String hql = "from "+t.getSimpleName()+" "+ where;
			Query query = session.createQuery(hql);
			list = (List<T>) query.list();
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			log.error("list fail for {} {}", t, where);
		}
		return list;
	}
	
	/**
	 * 注意这个方法会返回大于等于1的值。数据库无记录也会返回1，而不是null
	 * @param t
	 * @return
	 */
	public static <T> Long getTableIDMax(Class<T> t) {
		Long id = null;
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction = session.beginTransaction();
		String hql = "select max(id) from "+t.getSimpleName();
		try {
			Query query = session.createQuery(hql);
			Object uniqueResult = query.uniqueResult();
			if (uniqueResult == null) {
				id = 1L;
			}else {
				id = Long.parseLong(uniqueResult+"");
				id = Math.max(1L, id);
			}
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			log.error("query max id fail for {} {} {}", t, hql, e);
		}
		return id;
	}
	
	public static void destroy()
	{
		sessionFactory.getCurrentSession().close();
		sessionFactory.close();
	}
}
