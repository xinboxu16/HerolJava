package com.hjc.herol.core;

import com.alibaba.fastjson.JSON;
import com.hjc.herol.net.ProtoIds;
import com.hjc.herol.net.ProtoMessage;
import com.hjc.herol.net.http.HttpInHandler;
import com.hjc.herol.util.Helper;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName: Router
 * @Description: 消息路由分发
 * 
 */
public class Router extends Helper<Router> {
	private volatile static Router router = null;
	
	public static Router getInstance() {
		if (null == router) {
			synchronized (Router.class) {
				if (null == router) {
					router = new Router();
				}
			}
		}
		return router;
	}
	
	/**
	 * @Title: route
	 * @Description: 消息路由分发
	 * @param val
	 * @param ctx
	 */
	public void route(String val, ChannelHandlerContext ctx) {
		ProtoMessage data = null;
		try {
			data = JSON.parseObject(val, ProtoMessage.class);
		} catch (Exception e) {
			// TODO: handle exception
			log.error("格式错误，需json格式数据");
			HttpInHandler.writeJSON(ctx, ProtoMessage.getErrorResp("json格式错误"));
			return;
		}
		
		if (data.getTypeid() == null) {
			log.error("没有协议号");
			HttpInHandler.writeJSON(ctx, ProtoMessage.getErrorResp("协议号"));
			return;
		}
		
		switch (data.getTypeid()) {
		case ProtoIds.TEST:
			HttpInHandler.writeJSON(ctx, ProtoMessage.getSuccessResp());
			break;		
		default:
			log.error("未知协议号:{}", data.getTypeid());
			HttpInHandler.writeJSON(ctx,
					ProtoMessage.getErrorResp("未知协议号" + data.getTypeid()));
			break;
		}
	}
}
