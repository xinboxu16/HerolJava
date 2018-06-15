package com.hjc.herol.net.socket;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.hjc.herol.task.ExecutorPool;
import com.hjc.herol.util.Helper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

public class SocketServer extends Helper<SocketServer>{
	
	public static Properties p;
	public static int port;
	
	private NioEventLoopGroup bossGroup = new NioEventLoopGroup();
	private NioEventLoopGroup workGroup = new NioEventLoopGroup();
	
	public SocketServer() {
		
	}
	
	public void initData() {
		try {
			p = readProperties("/net.properties");
			port = Integer.parseInt(p.getProperty("socketPort"));
		} catch (Exception e) {
			log.error("socket配置文件读取错误");
			e.printStackTrace();
		}
	}
	
	public void start() {
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		//设置TCP缓冲区 
		bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
		
		/**
		 * // 通过NoDelay禁用Nagle,使消息立即发出去，不用等待到一定的数据量才发出去
		    Nagle和DelayedAcknowledgment是如何影响性能的
			Nagle和DelayedAcknowledgment虽然都是好心，但是它们在一起的时候却会办坏事。
			如果一个 TCP 连接的一端启用了 Nagle‘s Algorithm，而另一端启用了 TCP Delayed Ack，而发送的数据包又比较小，则可能会出现这样的情况：
			发送端在等待接收端对上一个packet 的 Ack 才发送当前的 packet，而接收端则正好延迟了此 Ack 的发送，那么这个正要被发送的 packet 就会同样被延迟。
			当然 Delayed Ack 是有个超时机制的，而默认的超时正好就是 40ms。
			现代的 TCP/IP 协议栈实现，默认几乎都启用了这两个功能，你可能会想，按我上面的说法，当协议报文很小的时候，岂不每次都会触发这个延迟问题？
			事实不是那样的。仅当协议的交互是发送端连续发送两个 packet，然后立刻 read 的 时候才会出现问题。
			现在让我们假设某个应用程序发出了一个请求，希望发送小块数据。我们可以选择立即发送数据或者等待产生更多的数据然后再一次发送两种策略。
			如果我们马上发送数据，那么交互性的以及客户/服务器型的应用程序将极大地受益。
			例如，当我们正在发送一个较短的请求并且等候较大的响应时，相关过载与传输的数据总量相比就会比较低，而且，如果请求立即发出那么响应时间也会快一些。
			以上操作可以通过设置套接字的TCP_NODELAY选项来完成，这样就禁用了Nagle 算法。 
			另外一种情况则需要我们等到数据量达到最大时才通过网络一次发送全部数据，这种数据传输方式有益于大量数据的通信性能，典型的应用就是文件服务器。
			应用Nagle算法在这种情况下就会产生问题。但是，如果你正在发送大量数据，你可以设置TCP_CORK选项禁用Nagle化，其方式正好同 TCP_NODELAY相反（TCP_CORK 和 TCP_NODELAY 是互相排斥的）。
		 */
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		/**
		 * 之前项目中遇到一个问题，聊天服务器的开启，关闭和重启，例如将服务器关闭后，实际上关闭了服务器的监听套接字(close)，
		 * 如果此时用户点击开启服务器，那么用户希望的情况是服务器又立即启动了。而由于close后，执行了主动关闭，执行主动关闭的一端，
		 * 在客户执行被动关闭之后会经历TIME_WAIT状态，如上图。TIME_WAIT的时间为1-4分钟不等，当某端口处于TIME_WAIT状态时，是无法被绑定的(bind).
		 * 如果在项目中用户关闭服务器后，希望立即启动，而程序却要等1-4分钟不等的时间后才能重新启动服务器明显不是我们所希望的。
		 * SO_REUSEADDR套接字选项就可以用来解决这个问题，实现瞬间重启服务器
		 */
		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childHandler(new SocketServerInitializer());
		
		//启动端口
		ChannelFuture future;
		try {
			future = bootstrap.bind(port).sync();
			// closeFuture等待服务端监听端口关闭 会阻塞
			future.channel().closeFuture().sync();
			//System.out.println("服务器关闭bye");
			if (future.isSuccess()) {
				log.info("端口{}已绑定", port);
			}
		} catch (InterruptedException e) {
			log.info("端口{}绑定失败", port);
		}
	}
	
	private class SocketServerInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			/** 官方自带的处理protobuf的方法
			 *  在基于netty的项目中使用protobuf，需要处理不同的消息，因此需要不同的编码和解码方式，代码如下。
			 *  注意的是ProtobufDecoder仅仅负责解码，它不支持读半包。因此，在ProtobufDecoder前面一定要有能够处理读半包的解码器，
			 *  有以下三种方式可以选择。
				使用Netty提供的ProtobufVarint32LengthFieldPrepender，可以处理半包消息
				继承Netty提供的通用半包解码器LengthFieldBasedFrameDecoder
				继承ByteToMessageDecoder类，自己处理半包消息
			 
			//netty自带的 用于decode前解决半包和粘包问题（利用包头中的包含数组长度来识别半包粘包）
			pipeline.addLast(new ProtobufVarint32FrameDecoder());
			//反序列化指定的Probuf字节数组为protobuf类型。 netty4官方的编解码器必须指定单一的protobuf类型才行
            pipeline.addLast(new ProtobufDecoder(StockTickOuterClass.StockTick.getDefaultInstance()));
            //用于在序列化的字节数组前加上一个简单的包头，只包含序列化的字节长度。
            pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
            //用于对Probuf类型序列化。
            pipeline.addLast(new ProtobufEncoder());
            
        	或者下面的样子
        	//数据分包，组包，粘包
        	 * netty 常用的处理大数据分包传输问题的解决类。
        	 * maxFrameLength：解码的帧的最大长度。
        	 * lengthFieldOffset： 长度属性的起始位（偏移位），包中存放有整个大数据包长度的字节，这段字节的其实位置
        	 * lengthFieldLength：长度属性的长度，即存放整个大数据包长度的字节所占的长度
        	 * lengthAdjustmen：长度调节值，在总长被定义为包含包头长度时，修正信息长度。
        	 * initialBytesToStrip：跳过的字节数，根据需要我们跳过lengthFieldLength个字节，以便接收端直接接受到不含“长度属性”的内容。
		     pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
	     	 编码类，自动将"HELLO, WORLD" 格式的数据转换成0x000C | "HELLO, WORLD" |
		     pipeline.addLast(new LengthFieldPrepender(4));
		
		     pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
		     pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
            */
			
			//我们可以参考以上官方的编解码代码，将实现我们客户化的protobuf编解码插件，但是要支持多种不同类型protobuf数据在一个socket上传输：
			pipeline.addLast(new CustomProtobufDecoder());
			pipeline.addLast(new CustomProtobufEncoder());
			//pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
			//pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
			// 业务逻辑处理
			pipeline.addLast(new SocketHandler());
			pipeline.addLast("timeout", new IdleStateHandler(100, 0, 0,  TimeUnit.SECONDS));// //此两项为添加心跳机制,60秒查看一次在线的客户端channel是否空闲  
			pipeline.addLast(new HeartBeatServerHandler());// 心跳处理handler 
		}
		
	}
	
	public void shut() {
		workGroup.shutdownGracefully();
		workGroup.shutdownGracefully();
		// 关闭所有channel连接
		log.info("关闭所有channel连接");
		ChannelMgr.getInstance().closeAllChannel();
		log.info("端口{}已解绑", port);
	}
	
	
	public static volatile SocketServer m_instance;
	public static SocketServer getInstance() {
		if (m_instance == null) {
			synchronized (SocketServer.class) {
				if (m_instance == null) {
					m_instance = new SocketServer();
				}
			}
		}
		return m_instance;
	}
	
	public static void main(String[] args) {
		ExecutorPool.initThreadsExcutor();// 初始化线程池
		SocketServer server = new SocketServer();
		server.initData();
		server.start();
	}
}

class HeartBeatServerHandler extends ChannelHandlerAdapter {  
	
	public int aa = 0;
    private int loss_connect_time = 0;  
  
    @Override  
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)  
            throws Exception {  
        if (evt instanceof IdleStateEvent) {  
            IdleStateEvent event = (IdleStateEvent) evt;  
            if (event.state() == IdleState.READER_IDLE) {  
                loss_connect_time++;  
                System.out.println("[60 秒没有接收到客户端" + ctx.channel().id()  
                        + "的信息了]");  
                if (loss_connect_time > 2) {  
                    // 超过20秒没有心跳就关闭这个连接  
                    System.out.println("[关闭这个不活跃的channel:" + ctx.channel().id()  
                            + "]");  
                    ctx.channel().close();  
                }  
            }  
        } else {  
            super.userEventTriggered(ctx, evt);  
        }  
    }  
  
}  
