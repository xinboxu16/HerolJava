package com.hjc.herol.manager.socket.factory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public interface IPacketFactory {
	public void onMessage(ByteBuf buffer, ChannelHandlerContext ctx);
}
