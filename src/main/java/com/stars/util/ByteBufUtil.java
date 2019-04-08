package com.stars.util;

import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;

/**
 * Created by zd on 2016/1/29.
 */
public class ByteBufUtil {

    public static void writeString(ByteBuf b, String str) {
        String value = str == null ? "" : str;
        byte[] strArray = value.getBytes(CharsetUtil.UTF_8);
        b.writeShort(strArray.length);
        b.writeBytes(strArray);
    }

    public static String readString(ByteBuf b) {
        try {
            short length = b.readShort();
            byte[] bytes = new byte[length];
            b.readBytes(bytes);
            return new String(bytes, 0, length, "UTF-8");
        } catch (Exception e) {
            CoreLogger.error(e.getMessage(), e);
        }
        return null;
    }

}
