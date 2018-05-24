package com.hjc.herol.net.http;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

public class HttpInHandler extends ChannelHandlerAdapter {
	public HttpInHandlerImp handler = new HttpInHandlerImp();
	
	//读取数据时调用
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// TODO Auto-generated method stub
		handler.channelRead(ctx, msg);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		handler.exceptionCaught(ctx, cause);
	}
	
	public static void writeJSON(ChannelHandlerContext ctx, HttpResponseStatus status, Object msg)
	{
		HttpInHandlerImp.writeJSON(ctx, status, msg);
	}
	
	public static void writeJSON(ChannelHandlerContext ctx, Object msg) {
		HttpInHandlerImp.writeJSON(ctx, msg);
	}
}
