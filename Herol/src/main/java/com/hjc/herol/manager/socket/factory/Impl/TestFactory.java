package com.hjc.herol.manager.socket.factory.Impl;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hjc.herol.core.SocketRouter;
import com.hjc.herol.manager.socket.factory.IPacketFactory;
import com.hjc.herol.net.ProtoIds;
import com.hjc.herol.net.ProtoMessage;
import com.hjc.herol.proto.test.RichManPb;
import com.hjc.herol.proto.test.RichManPb.RichMan;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class TestFactory implements IPacketFactory{

	@Override
	public void onMessage(ByteBuf buffer, ChannelHandlerContext ctx) {
		try {
			RichManPb.RichMan richMan = RichManPb.RichMan.parseFrom(buffer.array());
			System.out.println(richMan.getId());
			ProtoMessage data = new ProtoMessage();
			data.setTypeid(ProtoIds.CREATE_ROLE);
			data.setUserid((long) richMan.getId());
			data.setData(richMan);
			SocketRouter.getInstance().route(data, ctx);
//			for (RichMan.Car car : richMan.getCarsList()) {
//				System.out.println(car.getName());
//			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
