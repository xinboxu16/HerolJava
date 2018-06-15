package com.hjc.herol.net.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

public class ChannelUser {
	public String channelId;
	public Long userId;
	public ChannelHandlerContext ctx;
	public Channel channel;
}
