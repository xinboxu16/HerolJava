package com.hjc.herol.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hjc.herol.util.Helper;

/**
 * @ClassName: ExecutorPool
 * @Description: 线程池管理
 */
public class ExecutorPool extends Helper<ExecutorPool> {
	public static ExecutorService channelHandleThreadpool = null;
	
	public static void initThreadsExcutor() {
		channelHandleThreadpool = Executors.newCachedThreadPool();
	}
	
	public static void execute(Runnable runnable) {
		channelHandleThreadpool.execute(runnable);
	}
	
	public static void shutdown() {
		if (!channelHandleThreadpool.isShutdown()) {
			channelHandleThreadpool.shutdown();
		}
	}
}
