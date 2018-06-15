package com.hjc.herol.net.socket;

import java.util.List;

import com.google.protobuf.MessageLite;
import com.hjc.herol.exception.ExceptionUtils;
import com.hjc.herol.util.Constants;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * 参考ProtobufVarint32FrameDecoder 和 ProtobufDecoder
 * 
 * 自定义的 body长度（low） body长度（high）保留字节 类型
 */

public class CustomProtobufDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 如果可读长度小于包头长度，退出。
		while (in.readableBytes() > 4) {
			//markReaderIndex()把当前的readerIndex赋值到markReaderIndex中。
			in.markReaderIndex();
			
			// 获取包头中的body长度 readByte读取一个字节
			byte bLow = in.readByte();
			byte bHigh = in.readByte();
			/**
			由于0xff最低的8位是1，因此number中低8位中的&之后，如果原来是1，结果还是1，原来是0，结果位还是0.
			高于8位的，0xff都是0，所以无论是0还是1，结果都是0。
			*/
			short sLow = (short)(bLow & 0xff);
			short sHigh = (short)(bHigh & 0xff);
			//这是左移8位
			sLow <<= 8;
			short length = (short)(sLow | sHigh);
			
			// 获取包头中的protobuf类型
			in.readByte();
			byte dataType = in.readByte();
			
			// 如果可读长度小于body长度，恢复读指针，退出。
			if (in.readableBytes() < length) {
				in.resetReaderIndex();
				return;
			}
			
			// 读取body
			ByteBuf bBodyBuf = in.readBytes(length);
			
			byte[] array;
			int offset;
			
			int readableLen = bBodyBuf.readableBytes();
			if (bBodyBuf.hasArray()) {
				array = bBodyBuf.array();
				offset = bBodyBuf.arrayOffset() + bBodyBuf.readerIndex();
			}else {
				array = new byte[readableLen];
				bBodyBuf.getBytes(bBodyBuf.readerIndex(), array, 0, readableLen);
				offset = 0;
			}
			
			//反序列化
			MessageLite result = decodeBody(dataType, array, offset, readableLen);
			out.add(result);
		}
		
	}
	
	public MessageLite decodeBody(byte dataType, byte[] array, int offset, int length) throws Exception {
		if (dataType == 0x00) {
            //return StockTickOuterClass.StockTick.getDefaultInstance().getParserForType().parseFrom(array, offset, length);

        } else if (dataType == 0x01) {
            //return OptionTickOuterClass.OptionTick.getDefaultInstance().getParserForType().parseFrom(array, offset, length);
        }
		throw ExceptionUtils.createException(Constants.ExceptionType.StringError, "canot find decode class");
	}
}
