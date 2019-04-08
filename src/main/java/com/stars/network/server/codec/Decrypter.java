package com.stars.network.server.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class Decrypter extends MessageToMessageDecoder<ByteBuf> {

    private static byte[] pow = new byte[1048576];

    static {
        for (int i = 0; i < 1048576; i++) {
            int j = i + 1;
            pow[i] = (byte) ((j * j + j * 4) % 10);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf inBuf, List<Object> list) throws Exception {
        final byte[] data = new byte[inBuf.readableBytes()];
        inBuf.forEachByte(new ByteBufProcessor() {
            int i = 0;
            @Override
            public boolean process(byte value) throws Exception {
                if (i < 1048576) {
                    data[i] = (byte) (value - pow[i]);
                } else {
                    int index = i + 1;
                    data[i] = (byte) (value - (index * index + index * 4) % 10);
                }
                i++;
                return true;
            }
        });
        ByteBuf outBuf = inBuf.alloc().buffer(data.length);
        outBuf.writeBytes(data);
        list.add(outBuf);
    }
}
