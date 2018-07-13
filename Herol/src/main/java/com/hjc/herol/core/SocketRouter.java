package com.hjc.herol.core;

import com.hjc.herol.net.ProtoIds;
import com.hjc.herol.net.ProtoMessage;
import com.hjc.herol.net.socket.SocketHandler;
import com.hjc.herol.util.Helper;

import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName: Router
 * @Description: 消息路由分发
 * 
 */
public class SocketRouter extends Helper<SocketRouter> {
	private volatile static SocketRouter router = null;
	
	public static SocketRouter getInstance() {
		if (null == router) {
			synchronized (SocketRouter.class) {
				if (null == router) {
					router = new SocketRouter();
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
	public void route(ProtoMessage data, ChannelHandlerContext ctx) {
		if (data.getTypeid() == null) {
			log.error("未知协议号:{}", data.getTypeid());
			SocketHandler.writeJSON(ctx, ProtoMessage.getErrorResp("未知协议号" + data.getTypeid()));
			return;
		}
		switch (data.getTypeid()) {
		case ProtoIds.TEST:
			//SocketHandler.writeJSON(ctx, "delay");
			break;
		case ProtoIds.CREATE_ROLE:
			//playerMgr.createRole(ctx, data);
			break;
		case ProtoIds.EXIT_SCENE:
			//fightMgr.exitScene(data.getUserid());
			break;
		case ProtoIds.FIGHT_ENTER_SCENE:
			//fightMgr.enterScene(ctx, data);
			break;
		case ProtoIds.FIGHT_SKILL:
			//fightMgr.skill(ctx, data);
			break;
		case ProtoIds.FIGHT_PICK_HERO:
			//fightMgr.pickHero(ctx, data);
			break;
		default:
			log.error("未知协议号:{}", data.getTypeid());
			SocketHandler.writeJSON(ctx,
					ProtoMessage.getErrorResp("未知协议号" + data.getTypeid()));
			break;
		}
	}
}
