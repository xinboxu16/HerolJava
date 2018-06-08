package com.hjc.herol.net.http;

import java.io.File;

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
	
	public static void writeHtml(ChannelHandlerContext ctx, String msg) {
		HttpInHandlerImp.writeHtml(ctx, msg);
    }
	
	public static void writeRedirect(ChannelHandlerContext ctx, String newUri) {
    	HttpInHandlerImp.writeRedirect(ctx, newUri);
    }
	
	public static void writeError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		HttpInHandlerImp.writeError(ctx, status);
    }
	
	public static void writeFile(ChannelHandlerContext ctx, File file)
	{
		HttpInHandlerImp.writeFile(ctx, file);
	}
}
