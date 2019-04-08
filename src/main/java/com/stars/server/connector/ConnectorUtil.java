package com.stars.server.connector;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by zws on 2015/8/21.
 */
public class ConnectorUtil {

    /**
     * 在ByteBuf中从index开始读取一个字符串
     * @param buf 字节缓冲区
     * @param index 下标
     * @return 字符串
     */
    public static String getString(ByteBuf buf, int index) {
        short len = buf.getShort(index);
        byte[] data = new byte[len];
        buf.getBytes(index + 2, data);
        return new String(data);
    }

    /**
     * 往Channel发送数据包（暂时不使用ChannelPipeline）
     * @param ch 通道
     * @param packet 数据包
     */
    public static void send(Channel ch, Packet packet) {
        ByteBuf buf = ch.alloc().buffer();
        try {
            packet.writeToBuffer(new NewByteBuffer(buf));
            ch.unsafe().write(buf, new DefaultChannelPromise(ch));
            ch.unsafe().flush();
        } catch (Exception e) {
            ReferenceCountUtil.release(buf);
            throw e;
        }
    }

}
