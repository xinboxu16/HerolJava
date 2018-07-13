package com.hjc.herol.net.socket;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.MessageLite;
import com.hjc.herol.core.GameServer;
import com.hjc.herol.manager.socket.SocketMessageManager;
import com.hjc.herol.task.ExecutorPool;
import com.hjc.herol.util.Constants;
import com.hjc.herol.util.Helper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;

public class SocketHandlerImp extends Helper<SocketHandlerImp>{
	public static volatile boolean ENCRIPT_DECRIPT = false;
	
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		ExecutorPool.channelHandleThreadpool.execute(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				if (!GameServer.shutdown) {// 服务器开启的情况下
					dataHandle(ctx, msg);
				} else {// 服务器已关闭
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errMsg", "server closed");
					SocketHandler.writeJSON(ctx, jsonObject);
				}
			}
		});
	}
	
	/**
	 * @Title: dataHandle
	 * @Description: 数据处理
	 * @param ctx
	 * @param msg
	 *            void
	 * @throws
	 */
	public void dataHandle(final ChannelHandlerContext ctx, final Object msg) {
		@SuppressWarnings("unchecked")
		Map<Integer, ByteBuf> messages = (Map<Integer, ByteBuf>)(msg);
		for (int cmd : messages.keySet()) {
			ByteBuf buf = messages.get(cmd);
			if (buf.hasArray()) {
				SocketMessageManager.getInsatnce().onMessage(cmd, buf, ctx);
			}
		}

/*		String body = (String) msg;
		body = Utils.codeFilter(body, ENCRIPT_DECRIPT);
		ProtoMessage data = null;
		try {
			data = JSON.parseObject(body, ProtoMessage.class);
		} catch (Exception e) {
			log.error("格式错误，需json格式数据");
			SocketHandler.writeJSON(ctx, ProtoMessage.getErrorResp("json格式错误," + body));
			return;
		}
		if (Constants.MSG_LOG_DEBUG) {
			if (data.getTypeid() != null && data.getTypeid() != ProtoIds.TEST) {
				log.info("read :" + body);
			}
		}*/
		
		//SocketRouter.getInstance().route(data, ctx);
	}
	
	public static void writeJSON(ChannelHandlerContext ctx, Object msg) {
		if (msg == null || msg instanceof String) {
			ctx.channel().writeAndFlush(msg);
		} else {
			//String sentMsg = JSON.toJSONString(msg);
			if (ctx.channel().isWritable()) {
				ctx.channel().writeAndFlush(msg);
				log.warn("channelId:{}", ctx.channel().id().asShortText());
			}
		}
	}
	
	public static void writeJSON(ChannelGroup group, Object msg) {
		// 群发
		if (msg == null || msg instanceof String) {
			group.writeAndFlush(msg);
		} else {
			String sentMsg = JSON.toJSONString(msg);
			group.writeAndFlush(sentMsg);
		}
	}
	
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		log.info("ip:{}断开连接", ctx.channel().remoteAddress());
		Long userid = ChannelMgr.getInstance().findByChannel(ctx.channel()).userId;
		if (userid != null) {
			//FightMgr.getInstance().exitPkSceneMap(userid);
			//FightMgr.getInstance().exitWaitingUsers(userid);
		}
		ChannelMgr.getInstance().removeChannel(ctx.channel());
		ctx.close();
	}
	
	public void channelActive(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		log.info("ip:{}建立连接", channel.remoteAddress());
		// 添加到channel管理
		ChannelMgr.getInstance().addChannelUser(ctx, 0L);	}
	
	public void channelInactive(ChannelHandlerContext ctx) {
		Channel channel = ctx.channel();
		log.info("ip:{}掉线", channel.remoteAddress());
		ChannelMgr.getInstance().removeChannel(ctx.channel());
		ctx.close();
	}
	
	public void write(ChannelHandlerContext ctx, Object msg) {
		if (Constants.MSG_LOG_DEBUG) {
			//String resp = (String) msg;
			log.info("ip:{},write:{}", ctx.channel().remoteAddress(), msg.toString());
		}
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("ip:" + ctx.channel().remoteAddress() + "抛出异常", cause);
		Long userid = ChannelMgr.getInstance().findByChannel(ctx.channel()).userId;
		if (userid != null) {
			//FightMgr.getInstance().exitPkSceneMap(userid);
			//FightMgr.getInstance().exitWaitingUsers(userid);
		}
		ctx.close();
	}
}
