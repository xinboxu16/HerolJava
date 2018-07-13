package com.hjc.herol.manager.socket;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.hjc.herol.exception.ExceptionUtils;
import com.hjc.herol.manager.socket.factory.IPacketFactory;
import com.hjc.herol.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class SocketMessageManager {
	private ReadWriteLock lock = new ReentrantReadWriteLock();
	private static SocketMessageManager m_SocketManager = null;
	private HashMap<Integer, IPacketFactory> m_messages = new HashMap<Integer, IPacketFactory>();
	
	public static SocketMessageManager getInsatnce()
	{
		if (m_SocketManager == null)
		{
			m_SocketManager = new SocketMessageManager();
		}
		return m_SocketManager;
	}
	
	public void registerPacketFactory(int cmd, IPacketFactory packetFactory)
	{
		lock.writeLock().lock();
		if (!m_messages.containsKey(cmd)) {
			m_messages.put(cmd, packetFactory);
		}
		lock.writeLock().unlock();
	}
	
	public void onMessage(int cmd, ByteBuf buffer, ChannelHandlerContext ctx)
	{
		if (m_messages.containsKey(cmd)) {
			IPacketFactory factory = m_messages.get(cmd);
			factory.onMessage(buffer, ctx);
			return;
		}
		try {
			throw ExceptionUtils.createException(Constants.ExceptionType.StringError, "canot find cmd "+String.valueOf(cmd));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
