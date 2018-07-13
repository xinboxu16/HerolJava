package com.hjc.herol.manager.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.quartz.CronScheduleBuilder;
import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class TaskManager {
	private static TaskManager m_instance = new TaskManager();
	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
	private Scheduler m_scheduler = null;
	
	public static TaskManager getInstance() {
		return m_instance;
	}
	
	/**
     * 第一种方法：设定指定任务task在指定时间time执行 
     * schedule(TimerTask task, long delay)
     */
	public void start() {
		try {
			System.out.println("------- 初始化 -------------------");
			//通过schedulerFactory获取一个调度器
			SchedulerFactory schedulerFactory = new StdSchedulerFactory();
			m_scheduler = schedulerFactory.getScheduler();
			
			System.out.println("------- 初始化完成 --------");
			
			System.out.println("------- 开始Scheduler ----------------");
			// 启动  
            if (!m_scheduler.isShutdown()) {  
            	m_scheduler.start();  
            }  
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
	}
	
	private void initSimpleSchedule(Class<? extends Job> jobClass, String jobName, String jobGroup, String triggerName, String triggerGroup, int delaySecond) {
		try {
			//创建jobDetail实例，绑定Job实现类
			//指明job的名称，所在组的名称，以及绑定job类
			JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();
			//Date date = delaySecond == 0 ? new Date() : DateBuilder.nextGivenSecondDate(null, delaySecond);
			//定义调度触发规则 使用simpleTrigger规则
			SimpleTrigger trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
					.withIdentity(triggerName, triggerGroup)
					.withSchedule(SimpleScheduleBuilder.simpleSchedule())
					.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(delaySecond))//.repeatSecondlyForever(1).withRepeatCount(8)
					.startNow().build();
			
			//把作业和触发器注册到任务调度中
			Date date = m_scheduler.scheduleJob(jobDetail, trigger);
			
			System.out.println(jobDetail.getKey() + "将会在:" + m_dateFormat.format(date) +"时运行"
					+ "重复" + trigger.getRepeatCount() + "次" 
					+ "每"+trigger.getRepeatInterval()/1000L+"s 重复一次");
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerSimpleSchedule(Class<? extends Job> jobClass, String jobName, String triggerName, int delaySecond) {
		String jobGroup = "DefaultJobGroup";
		String triggerGroup = "DefaultTriggerGroup";
		initSimpleSchedule(jobClass, jobName, jobGroup, triggerName, triggerGroup, delaySecond);
	}
	
	public void registerSimpleSchedule(Class<? extends Job> jobClass, String jobName, String jobGroup, String triggerName, String triggerGroup, int delaySecond) {
		initSimpleSchedule(jobClass, jobName, jobGroup, triggerName, triggerGroup, delaySecond);
	}
	
	private void initCronSchedule(Class<? extends Job> jobClass, String cronExpression, String jobName, String jobGroup, String triggerName, String triggerGroup) {
		try {
			//创建jobDetail实例，绑定Job实现类
			//指明job的名称，所在组的名称，以及绑定job类
			JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup).build();
			
			//定义调度触发规则 使用simpleTrigger规则.startAt(new Date())
			SimpleTrigger trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
					.withIdentity(triggerName, triggerGroup)
					.withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
					.startNow().build();
			
			//把作业和触发器注册到任务调度中
			Date date = m_scheduler.scheduleJob(jobDetail, trigger);
			
			System.out.println(jobDetail.getKey() + "将会在:" + m_dateFormat.format(date) +"时运行"
					+ "重复" + trigger.getRepeatCount() + "次" 
					+ "每"+trigger.getRepeatInterval()/1000L+"s 重复一次");
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void registerCornSchedule(Class<? extends Job> jobClass, String cronExpression, String jobName, String triggerName) {
		//定义调度触发规则 使用simpleTrigger规则
		String jobGroup = "DefaultJobGroup";
		String triggerGroup = "DefaultTriggerGroup";
		initCronSchedule(jobClass, cronExpression, jobName, jobGroup, triggerName, triggerGroup);
	}
	
	public void registerCornSchedule(Class<? extends Job> jobClass, String cronExpression, String jobName, String jobGroup, String triggerName, String triggerGroup) {
		initCronSchedule(jobClass, cronExpression, jobName, jobGroup, triggerName, triggerGroup);
	}
	
	//移除一个任务 
	public void removeJob(String jobName, String jobGroup, String triggerName, String triggerGroup)
	{
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
		try {
			m_scheduler.pauseTrigger(triggerKey);// 停止触发器
			m_scheduler.unscheduleJob(triggerKey);// 移除触发器 
			m_scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//移除一个任务 
	public void removeJob(String jobName, String triggerName)
	{
		String jobGroup = "DefaultJobGroup";
		String triggerGroup = "DefaultTriggerGroup";
		TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);
		try {
			m_scheduler.pauseTrigger(triggerKey);// 停止触发器
			m_scheduler.unscheduleJob(triggerKey);// 移除触发器 
			m_scheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSimpleJob(Class<? extends Job> jobClass, String jobName, String jobGroup) {
		//定义调度触发规则 使用simpleTrigger规则
		JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroup)
	    		.storeDurably()//该job没有触发器，该方法将durability设置为true
	    		.build();
	    //添加一个没有出发器的job
		try {
			m_scheduler.addJob(jobDetail, true);
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addSimpleJob(String jobName, String jobGroup) {
		System.out.println("手动触发job...");
		try {
			m_scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//改变触发器
	public void changeTrigger(String triggerName, String triggerGroup) {
		SimpleTrigger trigger = (SimpleTrigger)TriggerBuilder.newTrigger()
		    		.withIdentity(triggerName, triggerGroup)
		    		.startNow()
		    		.withSchedule(SimpleScheduleBuilder
		    				.simpleSchedule()
		    				.withIntervalInMinutes(1)
		    				.withRepeatCount(20))
		    		.build();
		//根据触发器获取指定的Job然后更改此Job的触发器
		//新的触发器不需要旧的触发器的名称相同
		//获取TriggerKey（用来标识唯一的Trigger）
		try {
			Date date = m_scheduler.rescheduleJob(new TriggerKey(triggerName,triggerGroup), trigger);
			System.out.println("job6 重新在 " + m_dateFormat.format(date)+ "时运行 ，"
		    		+ "重复: " + trigger.getRepeatCount() + " 次, "
		    		+ "每 " + trigger.getRepeatInterval() / 1000L + " s 重复一次");
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shutdown() {
		try {
			System.out.println("------- 关闭Scheduler ---------------------");
			
			m_scheduler.shutdown();
			
			System.out.println("------- 关闭完成 -----------------");
			
			SchedulerMetaData metaData = m_scheduler.getMetaData();
		    System.out.println("Executed " + metaData.getNumberOfJobsExecuted() + " jobs.");
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
