package test.clientAndServer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

/**
 * Created by zd on 2015/11/2.
 */
public class Packets {

    private static byte[] pow = new byte[1048576];

    public static byte t[] = null;

    static {
        for (int i = 0; i < 1048576; i++) {
            int j = i + 1;
            pow[i] = (byte) ((j * j + j * 4) % 10);
        }
        ByteBuf b = Unpooled.buffer();
        b.writeByte((byte) -82);
        b.writeInt(256);
        b.writeShort(0x004E);
        b.writeLong(System.currentTimeMillis());
        b.writeBytes(new byte[246]);
        b.writeByte((byte) -81);
        t = encrypt(b);
    }

    private static byte[] encrypt(final ByteBuf buffer) {
        final byte array[] = new byte[buffer.readableBytes()];
        buffer.forEachByte(new ByteBufProcessor() {
            int l = 0;

            @Override
            public boolean process(byte value) throws Exception {
                if (l == array.length - 1) {
                    array[l] = value;
                    return false;
                }
                if (5 <= l && l < 1048576) {
                    array[l] = (byte) (value + pow[l - 5]);
                } else if (l >= 1048576) {
                    int index = l + 1;
                    array[l] = (byte) (value + (index * index + index * 4) % 10);
                } else {
                    array[l] = value;
                }
                l++;
                return true;
            }
        });
        return array;
    }

    public static void decrypt(ByteBuf buf) {
        int len = buf.readableBytes();
        int offset = buf.readerIndex();
        for (int i = 1; i <= len; i++) {
            if (i % 5 == 0) {
                buf.setByte(offset + i - 1, buf.getByte(offset + i - 1) - 7);
            } else if (i % 4 == 0) {
                buf.setByte(offset + i - 1, buf.getByte(offset + i - 1) - 3);
            } else {
                buf.setByte(offset + i - 1, buf.getByte(offset + i - 1) - i * 7 % 10);
            }
        }
    }


    public static byte[] arrays() {
        byte[] array = Arrays.copyOf(t, t.length);
        long value = System.currentTimeMillis();
        int index = 7;
        array[index] = (byte) ((value >>> 56) + pow[2]);
        array[index + 1] = (byte) ((value >>> 48) + pow[3]);
        array[index + 2] = (byte) ((value >>> 40) + pow[4]);
        array[index + 3] = (byte) ((value >>> 32) + pow[5]);
        array[index + 4] = (byte) ((value >>> 24) + pow[6]);
        array[index + 5] = (byte) ((value >>> 16) + pow[7]);
        array[index + 6] = (byte) ((value >>> 8) + pow[8]);
        array[index + 7] = (byte) (value + pow[9]);
        return array;
    }

}
