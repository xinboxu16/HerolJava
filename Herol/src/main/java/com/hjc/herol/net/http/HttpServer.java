package com.hjc.herol.net.http;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;

import com.hjc.herol.task.ExecutorPool;
import com.hjc.herol.util.Helper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

public class HttpServer extends Helper<HttpServer> {
	
	/** volatile
		用在多线程，同步变量。 线程为了提高效率，将某成员变量(如A)拷贝了一份（如B），线程中对A的访问其实访问的是B。
		只在某些动作时才进行A和B的同步。因此存在A和B不一致的情况。volatile就是用来避免这种情况的。
		volatile告诉jvm， 它所修饰的变量不保留拷贝，直接访问主内存中的（也就是上面说的A) 
		
		在Java内存模型中，有main memory，每个线程也有自己的memory (例如寄存器)。
		为了性能，一个线程会在自己的memory中保持要访问的变量的副本。
		这样就会出现同一个变量在某个瞬间，在一个线程的memory中的值可能与另外一个线程memory中的值，或者main memory中的值不一致的情况
	*/
	public volatile static HttpServer instance;
	
	public static Properties p;
	public static String ip;
	public static int port;
	public static String pvpIp;
	public static int pvpPort;
	public static boolean isSSL;
	
	/**Reactor(反应堆)线程模型
	 * 常说Reactor线程模型，那什么是Reactor呢？可以这样理解，Reactor就是一个执行while (true) { selector.select(); ...}循环的线程，
	 * 会源源不断的产生新的事件，称作反应堆很贴切。
	 *事件又分为连接事件、IO读和IO写事件，一般把连接事件单独放一线程里处理，即主Reactor（MainReactor），
	 *IO读和IO写事件放到另外的一组线程里处理，即从Reactor（SubReactor），从Reactor线程数量一般为2*(CPUs - 1)。
	 *所以在运行时，MainReactor只处理Accept事件，连接到来，马上按照策略转发给从Reactor之一，只处理连接，故开销非常小；
	 *每个SubReactor管理多个连接，负责这些连接的读和写，属于IO密集型线程，读到完整的消息就丢给业务线程池处理业务，
	 *处理完比后，响应消息一般放到队列里，SubReactor会去处理队列，然后将消息写回
	 */
	//NioEventLoopGroup是一个线程池，线程池中的线程就是NioEventLoop
	//bossGroup是用来处理TCP连接请求的，workGroup是来处理IO事件的。
	//实际上bossGroup中有多个NioEventLoop线程，每个NioEventLoop绑定一个端口，也就是说，如果程序只需要监听1个端口的话，bossGroup里面只需要有一个NioEventLoop线程就行了。
	//一个端口只需要一个NioServerSocketChannel即可。
	private volatile NioEventLoopGroup bossGroup = null;
	private volatile NioEventLoopGroup workGroup = null;
	
	public HttpServer()
	{
//		int a = 0x7fffffff;
//		int b = 0x7fffffff;
//		int c = a/b;
		log.info("HttpServer Constructors");
	}
	
	/**
	 * https://blog.csdn.net/chenchaofuck1/article/details/51702129
	 * 线程同步的单例 
	 * 解决同步问题和性能问题
	 * 为了达到线程安全，又能提高代码执行效率，我们这里可以采用DCL的双检查锁机制来完成
	 * 同步锁外判断，为避免在实例已经创建的情况下每次获取实例都加锁取，影响性能；
	 * 锁内判断，考虑多线程情况下，两个以上线程都已经运行至同步锁处，也就是都已经判断变量为空，如锁内不再次判断，会导致实例重复创建
	 * 
	 * 双锁有弊端 安全的Singleton的构造一般只有两种方法，一是在类载入时就创建该实例，二是使用性能较差的synchronized方法。
	 * @return
	 */
	public static HttpServer getInstance()
	{
		if (instance == null) {
			synchronized (HttpServer.class) {
				if (instance == null) {
					instance = new HttpServer();
					instance.initData();
				}
			}
		}
		return instance;
	}
	
	public void initData()
	{
		try
		{
			p = readProperties();
			
			isSSL = Boolean.parseBoolean(p.getProperty("isSSL"));
			
			ip = p.getProperty("ip");
			port = Integer.parseInt(p.getProperty("port"));
			
			// pvp服务器ip端口
			pvpIp = p.getProperty("pvpIp");
			pvpPort = Integer.parseInt(p.getProperty("pvpPort"));
		}catch (IOException e) {
			// TODO: handle exception
			log.error("socket配置文件读取错误");
			e.printStackTrace();
		}
	}
	
	public void start()
	{
		//0表示创建默认个线程 cpu的核数的2倍
		bossGroup = new NioEventLoopGroup(0, Executors.newCachedThreadPool());// boss线程组
		workGroup = new NioEventLoopGroup(0, Executors.newCachedThreadPool());// work线程组
		
		try
		{
			/**
			 * BootStrap在netty的应用程序中负责引导服务器和客户端。netty包含了两种不同类型的引导： 
			 * 1. 使用服务器的ServerBootStrap，用于接受客户端的连接以及为已接受的连接创建子通道。 
			 * 2. 用于客户端的BootStrap，不接受新的连接，并且是在父通道类完成一些操作。
			 */
			ServerBootstrap bootstrap = new ServerBootstrap();
			//设置serverbootstrap要使用的Eventloopgroup，这个Eventloopgroup将用于色热VRchannel和被接收的子channel的i/o处理
			bootstrap.group(bossGroup, workGroup);
			//指定要使用的channel实现
			//用它来建立新accept的连接，用于构造serversocketchannel的工厂类
			bootstrap.channel(NioServerSocketChannel.class);		
			/** 
	        * 对于ChannelOption.SO_BACKLOG的解释： 
	        * 服务器端TCP内核维护有两个队列，我们称之为A、B队列。客户端向服务器端connect时，会发送带有SYN标志的包（第一次握手），服务器端 
	        * 接收到客户端发送的SYN时，向客户端发送SYN ACK确认（第二次握手），此时TCP内核模块把客户端连接加入到A队列中，然后服务器接收到 
	        * 客户端发送的ACK时（第三次握手），TCP内核模块把客户端连接从A队列移动到B队列，连接完成，应用程序的accept会返回。也就是说accept 
	        * 从B队列中取出完成了三次握手的连接。 
	        * A队列和B队列的长度之和就是backlog。当A、B队列的长度之和大于ChannelOption.SO_BACKLOG时，新的连接将会被TCP内核拒绝。 
	        * 所以，如果backlog过小，可能会出现accept速度跟不上，A、B队列满了，导致新的客户端无法连接。要注意的是，backlog对程序支持的 
	        * 连接数并无影响，backlog影响的只是还没有被accept取出的连接 
	        */
			bootstrap.option(ChannelOption.SO_BACKLOG, 128);//设置TCP缓冲区 
			bootstrap.option(ChannelOption.SO_SNDBUF, 32 * 1024);//设置发送数据缓冲大小
			bootstrap.option(ChannelOption.SO_RCVBUF, 32 * 1024);//设置接受数据缓冲大小
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);//保持连接
			/**
			 * 通过添加hanlder，我们可以监听Channel的各种动作以及状态的改变，包括连接，绑定，接收消息等
			 * 在基类AbstractBootstrap有handler方法，目的是添加一个handler，监听Bootstrap的动作，客户端的Bootstrap中，继承了这一点
			 * 在服务端的ServerBootstrap中增加了一个方法childHandler，它的目的是添加handler，用来监听已经连接的客户端的Channel的动作和状态。
			 * handler在初始化时就会执行，而childHandler会在客户端成功connect后才执行，这是两者的区别
			 */
			bootstrap.childHandler(new HttpServerInitializer());
			log.info("端口{}绑定", port);
			Channel ch = bootstrap.bind(port).sync().channel();
			ch.closeFuture().sync();
		}catch (Exception e) {  
            e.printStackTrace();  
        }
		
	}
	
	/**
	 * ChannelInitializer继承于ChannelInboundHandler接口
	 * ChannelInitializer是一个抽象类，不能直接使用
	 * 为accept channel的pipeline预添加的inboundhandler 
	 * channel注册到pipeline时的执行操作
	 */
	class HttpServerInitializer extends ChannelInitializer<SocketChannel>
	{
		
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			// TODO Auto-generated method stub
			ChannelPipeline pipeline = ch.pipeline();
			
			/* http request解码  表示接收客户端发送消息*/
			/**
			 * HttpRequestDecoder先通过RequestLine和Header解析成HttpRequest对象，传入到HttpObjectAggregator。
			 * 然后再通过body解析出httpContent对象，传入到HttpObjectAggregator。
			 * 当HttpObjectAggregator发现是LastHttpContent，则代表http协议解析完成，封装FullHttpRequest
			 */
			pipeline.addLast("decoder", new HttpRequestDecoder());
			//设置块的最大字节数
			pipeline.addLast("aggregator", new HttpObjectAggregator(512*1024));
			
			/* http response 编码  表示向客户端发送消息*/
			pipeline.addLast("encoder", new HttpResponseEncoder());
			//处理大数据流设置为分开一个个chunk去接受信息。
			pipeline.addLast("http-chunked", new ChunkedWriteHandler());
			/**
	         * 压缩
	         * Compresses an HttpMessage and an HttpContent in gzip or deflate encoding
	         * while respecting the "Accept-Encoding" header.
	         * If there is no matching encoding, no compression is done.
	         */
	        //pipeline.addLast("deflater", new HttpContentCompressor());
			/**
			 * Netty中有3个实现了ChannelHandler接口的类，
			 * 其中2个是接口（ChannelInboundHandler用来处理入站数据也就是接收数据、ChannelOutboundHandler用来处理出站数据也就是写数据），
			 * 一个是抽象类ChannelHandlerAdapter类
			 */
			/* http response handler */
			pipeline.addLast("outbound", new HttpOutHandler());
			/* http request handler */
			pipeline.addLast("inbound", new HttpInHandler());
		}
	}
	
	public void shut()
	{
		if (bossGroup != null && workGroup != null) {
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
		log.info("端口{}绑定", port);
	}
	
	public static void main(String args[]) {
		ExecutorPool.initThreadsExcutor();// 初始化线程池
		HttpServer.getInstance().start();
	}
}
