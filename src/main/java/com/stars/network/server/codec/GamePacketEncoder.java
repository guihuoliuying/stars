package com.stars.network.server.codec;


import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GamePacketEncoder extends MessageToByteEncoder<com.stars.network.server.packet.Packet> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) throws Exception {
        try {
            // 包头
            out.writeByte((byte) -82);
            // 消息长度占位
            out.writeInt(-1);
            // 写入连接ID
            out.writeInt(packet.getSession().getConnectionId());
            // 消息内容
            out.writeShort(packet.getType());
            packet.writeToBuffer(new NewByteBuffer(out));
            // 包尾
            out.writeByte((byte) -81);
            // 消息长度计算
            int msgLength = out.readableBytes();
            int writerIndex = out.writerIndex();
            out.writerIndex(writerIndex - msgLength + 1);
            // 1 + 4 + 1 = 6
            out.writeInt(msgLength - 6);
            out.writerIndex(writerIndex);
        } catch (Exception e) {
            CoreLogger.error("error while encoding", e);
            throw e;
        }
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        CoreLogger.error("error while encoding packet", cause);
//        super.exceptionCaught(ctx, cause);
//    }
}
