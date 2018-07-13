package com.hjc.herol.manager.socket.factory.Impl;

import com.hjc.herol.manager.socket.factory.IPacketFactory;
import com.hjc.herol.util.Helper;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class KeepAliveReponse extends Helper<KeepAliveReponse> implements IPacketFactory{

	@Override
	public void onMessage(ByteBuf buffer, ChannelHandlerContext ctx) {
		log.info("KeepAliveReponse {} is trigger", ctx.channel().remoteAddress());
	}

}
