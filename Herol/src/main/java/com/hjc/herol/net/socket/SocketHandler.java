package com.hjc.herol.net.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.group.ChannelGroup;

public class SocketHandler extends ChannelHandlerAdapter {
	
	public SocketHandlerImp handler = new SocketHandlerImp();
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		handler.channelRead(ctx, msg);
	}

	@Override
	public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
		handler.disconnect(ctx, promise);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		handler.channelActive(ctx);
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg,
			ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
		handler.write(ctx, msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		handler.exceptionCaught(ctx, cause);
	}
	
	@Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  
        Channel incoming = ctx.channel();  
        //ChatServer.channels.remove(incoming);  
  
        System.out.println("[SERVER] - " + incoming.remoteAddress() + " 离开\n");  
  
        // A closed Channel is automatically removed from ChannelGroup,  
        // so there is no need to do "channels.remove(ctx.channel());"  
    }  

	public static void writeJSON(ChannelHandlerContext ctx, Object msg) {
		SocketHandlerImp.writeJSON(ctx, msg);
	}
	
	//群发
	public static void writeJSON(ChannelGroup group, Object msg) {
		SocketHandlerImp.writeJSON(group, msg);
	}
}
