package com.hjc.herol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.hjc.herol.util.mongo.MorphiaUtil;
import com.hjc.herol.util.mongo.SubBean;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
	public static Logger logger = LoggerFactory.getLogger(MorphiaUtil.class);
	/**
	 * @Title: main
	 * @Description: Test Code
	 * @param args
	 * void
	 * @throws
	 */
	public static void main(String[] args) {
		mongoTest();
	}
	
	//測試mongodb连接
	private static void mongoTest()
	{
		Datastore ds = MorphiaUtil.getDatastore();
		for (long i = 1l; i <= 3l; i++) {
			TestBean bean = new TestBean();
			bean.setId(i);
			bean.setMsg("test bean1");
			bean.setScore(100.2d);
			SubBean sub1 = new SubBean();
			sub1.setId(13l);
			sub1.setStr("sub bean 1");
			SubBean sub2 = new SubBean();
			sub2.setId(12l);
			sub2.setStr("sub bean 2");
			Map<Long, SubBean> subs = new HashMap<Long, SubBean>();
			subs.put(sub1.getId(), sub1);
			subs.put(sub2.getId(), sub2);
			bean.setSubBean(sub1);
			bean.setSubBeans(subs);
			Key<TestBean> key = ds.save(bean);
			logger.info(key.getCollection());
			logger.info(key.getClass() + "");
			logger.info(key.getId() + "");
			logger.info(key.getType() + "");
		}

		// query
		List<TestBean> list = ds.createQuery(TestBean.class).asList();
		for (TestBean testBean : list) {
			logger.info(JSON.toJSONString(testBean));
			// MorphiaUtil.deleteData(MorphiaUtil.ds.createQuery(TestBean.class)
			// .field("_id").equal(testBean.getId()));
		}
		// update
		ds.update(
				ds.createQuery(TestBean.class).field("_id").equal(2),
				ds.createUpdateOperations(TestBean.class).set("subBeans.12.str", "update map val"));
		// query
		List<TestBean> list2 = ds.createQuery(TestBean.class).asList();
		for (TestBean testBean : list2) {
			logger.info(JSON.toJSONString(testBean));
		}
	}
}
