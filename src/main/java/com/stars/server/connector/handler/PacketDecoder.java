package com.stars.server.connector.handler;

import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * Created by zhaowenshuo on 14-8-28.
 */
public class PacketDecoder extends LengthFieldBasedFrameDecoder {

    private boolean isDecoding = false;
    private Object data = null;

    public PacketDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    public PacketDecoder(
            int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public PacketDecoder(
            int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    public PacketDecoder(
            ByteOrder byteOrder, int maxFrameLength, int lengthFieldOffset, int lengthFieldLength,
            int lengthAdjustment, int initialBytesToStrip, boolean failFast) {
        super(byteOrder, maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object obj = super.decode(ctx, in);
//        ByteBuf buf = null;
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
//            buf.readerIndex(buf.readerIndex() + 5);
//            buf.writerIndex(buf.writerIndex() - 1);
            return buf.slice(buf.readerIndex() + 5, buf.readableBytes() - 6);
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        if (!swallowException(ctx, cause)) {
        	LogUtil.error(cause.getMessage(), cause);
        }
    }

    /**
     * 客户端通过发送RST断开连接时，会抛出异常。现在捕获该异常，避免过多日志
     * @param cause
     * @return 返回true表示该异常不需要打印；返回false表示该异常需要打印。
     */
    private boolean swallowException(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            if ("远程主机强迫关闭了一个现有的连接。".equals(cause.getMessage())) {
                return true;
            }
            if ("Connection reset by peer".equals(cause.getMessage())) {
                return true;
            }
        }
        return false;
    }

}
