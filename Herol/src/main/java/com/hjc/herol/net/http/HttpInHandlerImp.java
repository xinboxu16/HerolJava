package com.hjc.herol.net.http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

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

import eu.medsea.mimeutil.MimeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelProgressiveFuture;
import io.netty.channel.ChannelProgressiveFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

public class HttpInHandlerImp extends Helper<HttpInHandlerImp> {
	public static String DATA = "data";
	private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
	private static final Pattern ALLOWED_FILE_NAME = Pattern.compile("[A-Za-z0-9][-_A-Za-z0-9\\.]*");
	public static volatile boolean CODE_DEBUG = false;
	
	public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
		/** work线程的内容转交线程池管理类处理，缩短work线程耗时 **/
		ExecutorPool.channelHandleThreadpool.execute(new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				// 服务器开启的情况下
				if (!GameServer.shutdown) {
					if (msg instanceof HttpRequest) {
						//FullHttpRequest 包含了 HttpRequest 和 FullHttpMessage，是一个 HTTP 请求的完全体
						//msg 转换成 FullHttpRequest 的方法很简单
						DefaultFullHttpRequest request = (DefaultFullHttpRequest) msg;
						final String uri = request.getUri();
						if (uri.equals("/favicon.ico")) {
							return;
						}
						
						// 首先对HTTP请求小弟的解码结果进行判断，如果解码失败，直接构造HTTP 400错误返回。
						if (!request.getDecoderResult().isSuccess()) {
							writeError(ctx, HttpResponseStatus.BAD_REQUEST);
				            return;
				        }
						
						final String path = sanitizeUri(uri);
						if (path == null) {
							writeError(ctx, HttpResponseStatus.FORBIDDEN);
				            return;
				        }
						
						if (request.getMethod() == HttpMethod.POST) {
							postHandle(ctx, request);
							return;
						}
						
						// 处理get请求
						if (request.getMethod() == HttpMethod.GET) {
							File file = new File(path);
							// 如果文件不存在或者是系统隐藏文件，则构造404 异常返回
							if (file.isHidden() || !file.exists()) {
								/**
								 * 检测是否是带参数的url
								 * QueryStringDecoder 的作用就是把 HTTP uri 分割成 path 和 key-value 参数对，
								 * 也可以用来解码 Content-Type = "application/x-www-form-urlencoded" 的 HTTP POST。
								 * 特别注意的是，该 decoder 仅能使用一次
								 */
								QueryStringDecoder decoder = new QueryStringDecoder(uri, CharsetUtil.UTF_8);
								Map<String,	List<String>> params = decoder.parameters();
								if (!params.isEmpty()) {
									getHandle(ctx, params, request);
								}
								else {
									writeError(ctx, HttpResponseStatus.NOT_FOUND);
								}
								return;
							}
							
							// 如果文件是目录，则发送目录的连接给客户端浏览器
							if (file.isDirectory()) {
								if (uri.endsWith("/")) {
									sendListenering(ctx, file);
								}else {
									writeRedirect(ctx, uri + '/');
					            }
								return;
							}
							
							// 用户在浏览器上第几超链接直接打开或者下载文件，合法性监测
							if (!file.isFile()) {
								writeError(ctx, HttpResponseStatus.FORBIDDEN);
								return;
							}
							else
							{
								//缓存验证
								/**
								 * If-Modified-Since是标准的HTTP请求头标签，在发送HTTP请求时，把浏览器端缓存页面的最后修改时间一起发到服务器去，服务器会把这个时间与服务器上实际文件的最后修改时间进行比较。
									如果时间一致，那么返回HTTP状态码304（不返回文件内容），客户端接到之后，就直接把本地缓存文件显示到浏览器中。
									如果时间不一致，就返回HTTP状态码200和新的文件内容，客户端接到之后，会丢弃旧文件，把新文件缓存起来，并显示到浏览器中
								 */
								String ifModifiedSince = request.headers().get(HttpHeaders.Names.IF_MODIFIED_SINCE);
								if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
									try {
										SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.HTTP_DATE_FORMAT, Locale.US);
										Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);
										long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
										long fileLastModifiedSeconds = file.lastModified() / 1000;
										if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {  
											writeError(ctx, HttpResponseStatus.NOT_MODIFIED);  
							            }
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return;
								}
								writeFile(ctx, HttpResponseStatus.OK, file);
							}
						}
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
	
	//验证url
	private String sanitizeUri(String uri)
	{
		try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			try {
				uri = URLDecoder.decode(uri, "ISO-8859-1");
			} catch (UnsupportedEncodingException e2) {
				throw new Error();
			}
		}
		
		// 将硬编码的文件路径
		if (!uri.startsWith("/")) {
			return null;
		}
		
		// 将硬编码的文件路径 File.separatorChar系统识别的分隔符
		uri = uri.replace('/', File.separatorChar);
		if (uri.contains(File.separator + '.')
	            || uri.contains('.' + File.separator) || uri.startsWith(".")
	            || uri.endsWith(".") || INSECURE_URI.matcher(uri).matches())
		{
            return null;
        }
		return System.getProperty("user.dir") + File.separator + uri;
	}
	
	private void getHandle(final ChannelHandlerContext ctx, final Map<String,List<String>> params, final DefaultFullHttpRequest request) {
		
		List<String> typeList = params.get("data");
		if (typeList != null) {
			if (Constants.MSG_LOG_DEBUG) {
				log.info("ip:{},read :{}", ctx.channel().remoteAddress(),typeList.get(0));
			}
		}
		
		//501 - Not Implemented 不支持实现请求所需要的功能，页眉值指定了未实现的配置。例如，客户发出了一个服务器不支持的PUT请求
		//writeJSON(ctx, HttpResponseStatus.NOT_IMPLEMENTED, "not implement");
		writeJSON(ctx, HttpResponseStatus.NOT_IMPLEMENTED, String.format("get 请求成功! %s", params.values().toString()) );
		
		String jsonStr = request.content().toString(CharsetUtil.UTF_8);
		JSONObject obj = JSON.parseObject(jsonStr);
		if (obj != null)
		{
			for(Map.Entry<String, Object> item : obj.entrySet()){
				System.out.println(item.getKey()+"="+item.getValue().toString());
			}
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
	
	public static void writeHtml(ChannelHandlerContext ctx, String msg)
	{
		writeHtml(ctx, HttpResponseStatus.OK, Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
	}
	
	public static void writeRedirect(ChannelHandlerContext ctx, String msg)
	{
		writeRedirect(ctx, HttpResponseStatus.FOUND, msg);
	}
	
	public static void writeFile(ChannelHandlerContext ctx, File file)
	{
		writeFile(ctx, HttpResponseStatus.OK, file);
	}
	
	private static void writeJSON(ChannelHandlerContext ctx, HttpResponseStatus status, ByteBuf content) {
		if (ctx.channel().isWritable()) {
			FullHttpResponse response = null;
			if (content != null) {
				response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
				response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json; charset=utf-8");
				response.headers().set("userid", 101);
			}else{
				response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
			}
			
			if (response.content() != null) {
				response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			}
			// not keep-alive
			ctx.write(response).addListener(ChannelFutureListener.CLOSE);
		}
	}
	
	private static void writeHtml(ChannelHandlerContext ctx, HttpResponseStatus status, ByteBuf content) {
		if (ctx.channel().isWritable()) {
	        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
	        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
	        if (response.content() != null) {
	        	response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			}
	        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
    }
	
    private static void writeRedirect(ChannelHandlerContext ctx, HttpResponseStatus status, String newUri) {
    	if (ctx.channel().isWritable()) {
    		FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
            response.headers().set(HttpHeaders.Names.LOCATION, newUri);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    	}
    }
    
    public static void writeError(ChannelHandlerContext ctx, HttpResponseStatus status, String content)
    {
    	FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
        if (response.content() != null) {
        	response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
		}
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
	
	public static void writeError(ChannelHandlerContext ctx, HttpResponseStatus status) {
		if (ctx.channel().isWritable()) {
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
			
			String content = "";
			if (status == HttpResponseStatus.BAD_REQUEST)
			{
				content = "请求错误";
			}
			else if (status == HttpResponseStatus.FORBIDDEN) {
				content = "请求禁止";
			}
			else if (status == HttpResponseStatus.NOT_FOUND) {
				content = "文件不存在";
			}
			else if (status == HttpResponseStatus.METHOD_NOT_ALLOWED) {
				content = "请求方法不允许";
			}else if (status == HttpResponseStatus.NOT_MODIFIED) {
				//设置日期头
				SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.HTTP_DATE_FORMAT, Locale.US);  
		        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.HTTP_DATE_GMT_TIMEZONE));  
		        Calendar time = new GregorianCalendar();  
		        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime())); 
			}
			
	        if (content != null) {
	        	response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=UTF-8");
	        	ByteBuf buffer = Unpooled.copiedBuffer(content, CharsetUtil.UTF_8);  
	        	response.content().writeBytes(buffer);
	        	buffer.release();
	        	response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, response.content().readableBytes());
			}
	        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
		}
    }
	
	private static void writeFile(ChannelHandlerContext ctx, HttpResponseStatus status, File file)
	{
		if (ctx.channel().isWritable()) {
			// IE下才会打开文件，其他浏览器都是直接下载
	        // 随机文件读写类以读的方式打开文件
			RandomAccessFile randomAccessFile = null;
			long fileLength = 0;
			try {
				// 以只读的方式打开文件
				randomAccessFile = new RandomAccessFile(file, "r");
				fileLength = randomAccessFile.length();
				
				String mimeType = Files.probeContentType(file.toPath());
				if(mimeType == null)
				{
					Collection<?> mimeTypes = MimeUtil.getMimeTypes(file); 
					mimeType = mimeTypes.iterator().next().toString();
				}
				System.out.print(mimeType);
				
				//不能使用DefaultFullHttpResponse传输文件
				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
				response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, fileLength);
				setDateAndCacheHeaders(response, file);
		        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, mimeType);
		        response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
		        ctx.write(response);
		        
		        // 同过netty的村可多File对象直接将文件写入到发送缓冲区，最后为sendFileFeature增加GenericFeatureListener，
		        // 如果发送完成，打印“Transfer complete”
	        	ChannelFuture fileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
	        	fileFuture.addListener(new ChannelProgressiveFutureListener() {
					
					public void operationComplete(ChannelProgressiveFuture future) {
						// TODO Auto-generated method stub
						System.out.println("传输完成");
					}
					
					public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
						// TODO Auto-generated method stub
						if (total < 0) { // total unknown
		                    System.out.println("传输进度: " + progress);
		                } else {
		                    System.out.println("传输进度: " + progress + " / " + total);
		                }
					}
				});
	        	
	        	ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
	        	lastContentFuture.addListener(ChannelFutureListener.CLOSE);
	        	//fileFuture.addListener(ChannelFutureListener.CLOSE);
		        
			} catch (FileNotFoundException e) {
				closeFile(randomAccessFile);
				writeError(ctx, HttpResponseStatus.NOT_FOUND);
			}catch(Exception e){
				closeFile(randomAccessFile);
				writeError(ctx, HttpResponseStatus.BAD_REQUEST, e.toString());
			}
		}
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("netty exception:", cause);
	}
	
	//关闭文件
	private static void closeFile(RandomAccessFile randomAccessFile)
	{
		try {
			randomAccessFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
     * 设置日期和缓存文件
     *  
     * @param response 
     *            HTTP response 
     * @param fileToCache 
     *            file to extract content type 
     */  
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {  
        SimpleDateFormat dateFormatter = new SimpleDateFormat(Constants.HTTP_DATE_FORMAT, Locale.US);  
        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.HTTP_DATE_GMT_TIMEZONE));  
  
        // Date header  
        Calendar time = new GregorianCalendar();  
        response.headers().set(HttpHeaders.Names.DATE, dateFormatter.format(time.getTime()));  
  
        // Add cache headers 添加头缓存 
        time.add(Calendar.SECOND, Constants.HTTP_CACHE_SECONDS);  
        response.headers().set(HttpHeaders.Names.EXPIRES, dateFormatter.format(time.getTime()));  
        response.headers().set(HttpHeaders.Names.CACHE_CONTROL, "private, max-age=" + Constants.HTTP_CACHE_SECONDS);  
        response.headers().set(HttpHeaders.Names.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));  
    } 
	
	/**
     * 这里是构建了一个html页面返回给浏览器
     * @param ctx
     * @param dir
     */
    private void sendListenering(ChannelHandlerContext ctx, File dir) {
        StringBuilder buf = new StringBuilder();
        String dirPath = dir.getPath();
        buf.append("<!DOCTYPE html>\r\n");
        buf.append("<html><head><title>");
        buf.append(dirPath);
        buf.append(" 目录：");
        buf.append("</title></head><body>\r\n");
        buf.append("<h3>");
        buf.append(dirPath).append(" 目录：");
        buf.append("</h3>\r\n");
        buf.append("<ul>");
        // 此处打印了一个 .. 的链接
        buf.append("<li>链接：<a href=\"../\">..</a></li>\r\n");
        // 用于展示根目录下的所有文件和文件夹，同时使用超链接标识
        for (File f : dir.listFiles()) {
            if (f.isHidden() || !f.canRead()) {
            continue;
            }
            String name = f.getName();
            if (!ALLOWED_FILE_NAME.matcher(name).matches()) {
            	continue;
            }
            buf.append("<li>链接：<a href=\"");
            buf.append(name);
            buf.append("\">");
            buf.append(name);
            buf.append("</a></li>\r\n");
        }
        buf.append("</ul></body></html>\r\n");
        writeHtml(ctx, HttpResponseStatus.OK, Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8));
    }
}
