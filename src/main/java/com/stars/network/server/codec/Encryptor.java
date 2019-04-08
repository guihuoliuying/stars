package com.stars.network.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class Encryptor extends MessageToMessageEncoder<ByteBuf> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf inBuf, List<Object> out) throws Exception {
        byte[] payload = new byte[inBuf.readableBytes()];
        inBuf.readBytes(payload);
        // encrypt
        payload = encrypt(payload);
        // write the buffer
        ByteBuf outBuf = ctx.alloc().buffer(payload.length);
        outBuf.writeBytes(payload);
        out.add(outBuf);

    }

    private static byte[] encrypt(byte[] payload) {
        for (int i = 1; i <= payload.length; i++) {
            if (i % 5 == 0) {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + 7);
            } else if (i % 4 == 0) {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + 3);
            } else {
                payload[(i - 1)] = (byte) (payload[(i - 1)] + i * 7 % 10);
            }
        }
        return payload;
    }
}
