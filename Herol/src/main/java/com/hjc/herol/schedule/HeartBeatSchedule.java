package com.hjc.herol.schedule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;

import com.hjc.herol.net.socket.ChannelMgr;
import com.hjc.herol.net.socket.ChannelUser;
import com.hjc.herol.net.socket.SocketHandler;
import com.hjc.herol.proto.test.CommonPb;

public class HeartBeatSchedule implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		Map<String, ChannelUser> map = ChannelMgr.getInstance().getAllChannels();
		for(Map.Entry<String, ChannelUser> entry : map.entrySet())
		{
			ChannelUser user = map.get(entry.getKey());
			
			CommonPb.KeepAliveRequest.Builder builder = CommonPb.KeepAliveRequest.newBuilder();
			builder.setCmd(100000);
			builder.setName("保持连接");
			
			SocketHandler.writeJSON(user.ctx, builder.build());
		}
		JobKey jobKey = context.getJobDetail().getKey();
		 
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");  
	    System.out.println("SimpleJob类 ："+ jobKey + " 在 " + dateFormat.format(new Date())+" 时运行");
	}
	
}
