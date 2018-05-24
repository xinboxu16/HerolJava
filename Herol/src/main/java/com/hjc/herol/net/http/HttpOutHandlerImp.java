package com.hjc.herol.net.http;

import java.nio.charset.Charset;

import com.hjc.herol.util.Constants;
import com.hjc.herol.util.Helper;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;

public class HttpOutHandlerImp extends Helper<HttpOutHandlerImp> {
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
	{
		if (Constants.MSG_LOG_DEBUG) {
			DefaultFullHttpResponse response = (DefaultFullHttpResponse) msg;
			log.info("ip:{},write:{}", ctx.channel().remoteAddress(), response.content().toString(Charset.forName("UTF-8")));
		}
	}
}
