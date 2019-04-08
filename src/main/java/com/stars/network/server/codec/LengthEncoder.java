package com.stars.network.server.codec;

import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class LengthEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        try {
            // 包头
            out.writeByte((byte) -82);
            // 消息长度占位
            out.writeInt(msg.readableBytes() + 1);
            out.writeByte(2); // 压缩标志位
            // 消息内容
            out.writeBytes(msg);
            // 包尾
            out.writeByte((byte) -81);
//            // 消息长度计算
//            int msgLength = out.readableBytes();
//            int writerIndex = out.writerIndex();
//            out.writerIndex(writerIndex - msgLength + 1);
//            // 1 + 4 + 1 = 6
//            out.writeInt(msgLength - 6);
//            out.writerIndex(writerIndex);
        } catch (Exception e) {
            CoreLogger.error("error while encoding", e);
            throw e;
        }
    }
}
