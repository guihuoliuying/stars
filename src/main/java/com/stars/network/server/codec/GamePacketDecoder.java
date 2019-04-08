package com.stars.network.server.codec;

import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Created by zhaowenshuo on 14-8-28.
 */
public class GamePacketDecoder extends LengthFieldBasedFrameDecoder {

    private boolean isDecoding = false;
    private Object data = null;

    public GamePacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    public GamePacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public GamePacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    public GamePacketDecoder(ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object obj = super.decode(ctx, in);
        if (obj != null && obj instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) obj;
            byte head = buf.getByte(buf.readerIndex());
            byte tail = buf.getByte(buf.readerIndex() + buf.readableBytes() - 1);
            if (head != -82) {
                throw new CorruptedFrameException("invalid head: " + head);
            }
            if (tail != -81) {
                throw new CorruptedFrameException("invalid tail: " + tail);
            }
            buf.readerIndex(buf.readerIndex()+5);//跳过包头，包长
        }
        return obj;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!swallowException(cause)) {
            CoreLogger.error("", cause);
        }
        ctx.close();
//        super.exceptionCaught(ctx, cause);
    }

    private boolean swallowException(Throwable cause) {
        if (cause instanceof IOException) {
            if ("远程主机强迫关闭了一个现有的连接。".equals(cause.getMessage())) {
                return true;
            }
            if ("Connection reset by peer.".equals(cause.getMessage())) {
                return true;
            }
        }
        return false;
    }
}
