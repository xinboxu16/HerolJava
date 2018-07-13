package com.hjc.herol.net.socket;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.hjc.herol.util.Helper;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

/**
 * @ClassName: ChannelMgr
 * @Description: Channel管理类
 * 
 */
public class ChannelMgr extends Helper<ChannelMgr> {
	public Map<String, ChannelUser> channelMap;
	private AtomicLong channelIdGen;
	private static ChannelMgr instance;
	
	private ChannelMgr()
	{
		channelMap = new ConcurrentHashMap<String, ChannelUser>();
		channelIdGen = new AtomicLong(0);
	}
	
	public static ChannelMgr getInstance()
	{
		if (instance == null) {
			synchronized (ChannelMgr.class) {
				if (instance == null) {
					instance = new ChannelMgr();
				}
			}
		}
		return instance;
	}
	
	/**
	 * @Title: addChannelUser
	 * @Description: channel管理
	 * @param ctx
	 * @param userId
	 * @return ChannelUser
	 * @throws
	 */
	public ChannelUser addChannelUser(ChannelHandlerContext ctx, Long userId) {
		ChannelUser ret = null;
		synchronized (channelMap) {
			Channel channel = ctx.channel();
			//Long channelId = Long.valueOf(ChannelMgr.getInstance().genChannelId());
			String channelId = channel.id().asShortText();
			ret = new ChannelUser();
			ret.channelId = channelId;
			ret.channel = ctx.channel();
			ret.ctx = ctx;
			ret.userId = userId;
			channelMap.put(channelId, ret);
		}
		return ret;
	}
	
	/**
	 * @Title: getUseridByChannel
	 * @Description: 根据Channel找到Userid
	 * @param ctx
	 * @return Long
	 * @throws
	 */
	public ChannelUser findByChannel(Channel channel) {
		synchronized (channelMap) {
			Iterator<ChannelUser> it = channelMap.values().iterator();
			while (it.hasNext()) {
				ChannelUser u = it.next();
				if (u.channel.equals(channel)) {
					return u;
				}
			}
			return null;
		}
	}
	
	/**
	 * @Title: closeAllChannel
	 * @Description: 关闭所有Channel void
	 * @throws
	 */
	public void closeAllChannel() {
		synchronized (channelMap) {
			Iterator<ChannelUser> it = channelMap.values().iterator();
			while (it.hasNext()) {
				ChannelUser u = it.next();
				it.remove();
				u.channel.close();
			}
		}
		log.info("关闭所有channel");
	}
	
	/**
	 * @Title: removeChannel
	 * @Description: 移除channel
	 * @param ctx
	 *            void
	 * @throws
	 */
	public void removeChannel(Channel channel) {
		synchronized (channelMap) {
			Iterator<ChannelUser> it = channelMap.values().iterator();
			while (it.hasNext()) {
				ChannelUser u = it.next();
				if (u.channel.equals(channel)) {
					it.remove();
					channel.close();
					// 离线通知登录服务器
					// ServerNotify
					// .logout((int) (u.junZhuId - GameInit.serverId) / 1000);
				}
			}
		}
	}
	
	/**
	 * @Title: findByJunZhuId
	 * @Description: 根据君主id找到ChannelUser
	 * @param junZhuId
	 * @return ChannelUser
	 * @throws
	 */
	public ChannelUser findByUserId(Long userId) {
		synchronized (channelMap) {
			Iterator<ChannelUser> it = channelMap.values().iterator();
			while (it.hasNext()) {
				ChannelUser u = it.next();
				Long v = u.userId;
				if (v != null && v.longValue() == userId.longValue()) {
					return u;
				}
			}
		}
		return null;
	}
	
	/**
	 * @Title: getChannel
	 * @Description: 根据君主id获取Channel
	 * @param junZhuId
	 * @return ChannelHandlerContext
	 * @throws
	 */
	public ChannelHandlerContext getChannel(Long heroId) {
		ChannelUser cu = findByUserId(heroId);
		if (cu == null) {
			return null;
		}
		return cu.ctx;
	}
	
	/**
	 * @Title: getAllChannels
	 * @Description: 获取所有Channel
	 * @return List<ChannelUser>
	 * @throws
	 */
//	public List<ChannelUser> getAllChannels() {
//		List<ChannelUser> list = new LinkedList<ChannelUser>();
//		synchronized (channelMap) {
//			for (ChannelUser user : channelMap.values()) {
//				list.add(user);
//			}
//		}
//		return list;
//	}
	
	public Map<String, ChannelUser> getAllChannels() {
		return channelMap;
	}
	
	/**
	 * 自动生成通道id
	 * @return
	 */
	public long genChannelId() {
		return channelIdGen.getAndIncrement();
	}
}
