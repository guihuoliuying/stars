package com.stars.network;

import io.netty.buffer.ByteBuf;

/**
 * Created by zhaowenshuo on 2016/2/1.
 */
public class NetworkUtil {

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

    public static String readString(ByteBuf buf) {
        short len = buf.readShort();
        byte[] data = new byte[len];
        buf.readBytes(data);
        return new String(data);
    }
}
