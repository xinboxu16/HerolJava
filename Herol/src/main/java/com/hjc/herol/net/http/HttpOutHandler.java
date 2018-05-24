package com.hjc.herol.net.http;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * ChannelHandlerAdapter有个重要的方法isSharable()
 * 引入了优化的线程局部变量InternalThreadLocalMap，将在以后分析，此处可简单理解为线程变量ThreadLocal，
 * 即每个线程都有一份ChannelHandler是否Sharable的缓存。这样可以减少线程间的竞争，提升性能
 * 
 * 类似的ChannelInboundHandler处理入站事件，以及用户自定义事件
 * ChannelOutboundHandlerAdapter作为ChannelOutboundHandler的事件，默认将出站事件传播到下一个出站处理器
 */

public class HttpOutHandler extends ChannelHandlerAdapter {

	public HttpOutHandlerImp handler = new HttpOutHandlerImp();
	
	/**
	 * ChannelHandlerContext
	 * 每当有ChannelHandler添加到ChannelPipeline就会创建ChannelHandlerContext
	 * 使ChannelHandler能够与ChannelPipeline以及其他的ChannelHandler交互
	 * 可以通知ChannelPipeline中下一个ChannelHandler
	 * 具有丰富的用于处理事件和执行i/o操作的api
	 * 
	 * ChannelPromise用于通知
	 * 当请求通过channel将数据写入远程节点时被调用
	 */
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		// TODO Auto-generated method stub
		super.write(ctx, msg, promise);
		handler.write(ctx, msg, promise);
	}
	
}
