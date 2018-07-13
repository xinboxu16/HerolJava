package com.hjc.herol.net.socket;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.protobuf.MessageLite;
import com.hjc.herol.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 参考ProtobufVarint32LengthFieldPrepender 和 ProtobufEncoder
 * 
 * CustomProtobufEncoder序列化传入的protobuf类型，并且为它创建了一个4个字节的包头，格式如下
 * body长度（low） body长度（high）保留字节 类型
 */

public class CustomProtobufEncoder extends MessageToByteEncoder<Object>{
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		//反射获取数据
		MessageLite message = (MessageLite)msg;
		//Field field = msg.getClass().getDeclaredField("cmd_");
		//field.setAccessible(true);
		//int b = (int)field.get(msg);
		//getDeclaredMethod*()获取的是类自身声明的所有方法，包含public、protected和private方法。
		//getMethod*()获取的是类的所有共有方法，这就包括自身的所有public方法，和从基类继承的、从接口实现的所有public方法。
		Class<?> classz = msg.getClass();
		Method method = classz.getMethod("getCmd");
		int cmd = (int)method.invoke(msg);
		System.out.println(cmd);
		
		byte[] body = message.toByteArray();
		int headerLen = Integer.parseInt(Utils.getProperty("net", "packetHeaderLength"));
		int bodyLength = body.length;
		int total = headerLen + bodyLength;
		byte[] packet = new byte[total];
		System.arraycopy(Utils.intToBytes(bodyLength), 0, packet, 0, 4);
		System.arraycopy(Utils.intToBytes(cmd), 0, packet, 5, 4);
		System.arraycopy(body, 0, packet, headerLen, bodyLength);
		
		out.writeBytes(packet);
	}
}
