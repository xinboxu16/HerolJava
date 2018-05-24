package com.hjc.herol.net.http;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hjc.herol.core.GameServer;
import com.hjc.herol.core.Router;
import com.hjc.herol.net.ProtoMessage;
import com.hjc.herol.net.ResultCode;
import com.hjc.herol.task.ExecutorPool;
import com.hjc.herol.util.Constants;
import com.hjc.herol.util.Helper;
import com.hjc.herol.util.Utils;
import com.hjc.herol.util.encrypt.XXTeaCoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;

public class HttpInHandlerImp extends Helper<HttpInHandlerImp> {
	public static String DATA = "data";
	public static volatile boolean CODE_DEBUG = false;
	
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		/** work线程的内容转交线程池管理类处理，缩短work线程耗时 **/
		ExecutorPool.channelHandleThreadpool.execute(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				// 服务器开启的情况下
				if (!GameServer.shutdown) {
					//FullHttpRequest 包含了 HttpRequest 和 FullHttpMessage，是一个 HTTP 请求的完全体
					//msg 转换成 FullHttpRequest 的方法很简单
					DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;
					// 处理get请求
					if (request.getMethod() == HttpMethod.GET) {
						getHandle(ctx, request);
					}
					else if (request.getMethod() == HttpMethod.POST) {
						postHandle(ctx, request);
					}
				}
				else//服务器关闭
				{
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("errorMsg", "server closed");
					writeJSON(ctx, jsonObject);
				}
			}
		});
	}
	
	private void getHandle(final ChannelHandlerContext ctx, final DefaultFullHttpRequest request) {
		/**
		 * QueryStringDecoder 的作用就是把 HTTP uri 分割成 path 和 key-value 参数对，
		 * 也可以用来解码 Content-Type = "application/x-www-form-urlencoded" 的 HTTP POST。
		 * 特别注意的是，该 decoder 仅能使用一次
		 */
		QueryStringDecoder decoder = new QueryStringDecoder(request.getUri(), CharsetUtil.UTF_8);
		Map<String,	List<String>> params = decoder.parameters();
		if (!params.isEmpty()) {
			List<String> typeList = params.get("type");
			if (Constants.MSG_LOG_DEBUG) {
				log.info("ip:{},read :{}", ctx.channel().remoteAddress(),typeList.get(0));
			}
		}
		//501 - Not Implemented 不支持实现请求所需要的功能，页眉值指定了未实现的配置。例如，客户发出了一个服务器不支持的PUT请求
		writeJSON(ctx, HttpResponseStatus.NOT_IMPLEMENTED, "not implement");
		
		String jsonStr = request.content().toString(CharsetUtil.UTF_8);
		JSONObject obj = JSON.parseObject(jsonStr);
		for(Map.Entry<String, Object> item : obj.entrySet()){
			System.out.println(item.getKey()+"="+item.getValue().toString());
		}
	}
	
	private void postHandle(final ChannelHandlerContext ctx, final DefaultFullHttpRequest request) {
		//使用 HttpPostRequestDecoder 解析时，无需先将 msg 转换成 FullHttpRequest
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), request);
		
		try {
			InterfaceHttpData data = decoder.getBodyHttpData(DATA);
			if (data != null) {
				String val = ((Attribute) data).getValue();
				val = codeFilter(val);
				log.info("ip:{},read :{}", ctx.channel().remoteAddress(), val);
				Router.getInstance().route(val, ctx);
			}
		} catch (Exception e) {
			// TODO: handle exception
			final StringBuffer ebuff = Utils.exceptionTrace(HttpInHandlerImp.class, e);
			HttpInHandler.writeJSON(ctx, ProtoMessage.getErrorResp(ResultCode.SERVER_ERR, ebuff.toString()));
		}
	}
	
	/**
	 * @Title: codeFilter
	 * @Description: 编解码过滤
	 * @param val
	 * @return
	 * @throws UnsupportedEncodingException
	 *             String
	 * @throws
	 */
	private String codeFilter(String val) throws UnsupportedEncodingException {
		val = val.contains("%") ? URLDecoder.decode(val, "UTF-8") : val;
		String valTmp = val;
		val = CODE_DEBUG ? XXTeaCoder.decryptBase64StringToString(val, XXTeaCoder.key) : val;
		if (Constants.MSG_LOG_DEBUG) {
			if (val == null) {
				val = valTmp;
			}
		}
		return val;
	}
	
	public static void writeJSON(ChannelHandlerContext ctx, HttpResponseStatus status, Object msg) {
		String sentMsg = null;
		if (msg instanceof String) {
			sentMsg = (String)msg;
		}
		else
		{
			sentMsg = JSON.toJSONString(msg);
		}
		sentMsg = CODE_DEBUG ? XXTeaCoder.encryptToBase64String(sentMsg, XXTeaCoder.key) : sentMsg;
		/**
		 * 非池类堆字节buf，实际为一个字节数组，直接在Java虚拟机堆内存中，分配字节缓存；
		 * 非池类Direct buf，实际为一个nio 字节buf，从操作系统实际物理内存中，分配字节缓存。
		 * Unpooled创建字节buf，实际委托给内部字节分配器UnpooledByteBufAllocator。
		 */
		writeJSON(ctx, status, Unpooled.copiedBuffer(sentMsg, CharsetUtil.UTF_8));
		ctx.flush();
	}
	
	public static void writeJSON(ChannelHandlerContext ctx, Object msg) {
		String sentMsg = null;
		if (msg instanceof String) {
			sentMsg = (String) msg;
		}else
		{
			sentMsg = JSON.toJSONString(msg);
		}
		sentMsg = CODE_DEBUG ? XXTeaCoder.encryptToBase64String(sentMsg, XXTeaCoder.key) : sentMsg;
		writeJSON(ctx, HttpResponseStatus.OK, Unpooled.copiedBuffer(sentMsg, CharsetUtil.UTF_8));
		ctx.flush();
	}
	
	private static void writeJSON(ChannelHandlerContext ctx, HttpResponseStatus status, ByteBuf content) {
		if (ctx.channel().isWritable()) {
			FullHttpResponse msg = null;
			if (content != null) {
				msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
				msg.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=utf-8");
				msg.headers().set("userid", 101);
			}else{
				msg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
			}
			
			if (msg.content() != null) {
				msg.headers().set(HttpHeaders.Names.CONTENT_LENGTH, msg.content().readableBytes());
			}
			content.release();
			// not keep-alive
			ctx.write(msg).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("netty exception:", cause);
	}
}
