package com.stars.network.server.buffer;

import com.stars.util.log.CoreLogger;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author xieyuejun
 *
 * 封装的ByteBuffer ，便于使用
 *
 */
public class NewByteBuffer {
	
	private ByteBuf buff = null;

	public NewByteBuffer(ByteBuf buff) {
		this.buff = buff;
	}
	
	public ByteBuf getBuff(){
		return buff;
	}

	public int getPacketLength() {
		return buff.readableBytes();
	}

	public NewByteBuffer(ChannelHandlerContext ctx) {
		buff = ctx.alloc().buffer();
	}

	public NewByteBuffer(ChannelHandlerContext ctx, int length) {
		buff = ctx.alloc().buffer(length);
	}

	public void writeByte(byte value) {
		buff.writeByte(value);
	}

	public void writeInt(int value) {
		buff.writeInt(value);
	}

	public void writeShort(short value) {
		buff.writeShort(value);
	}

	public void writeBytes(byte[] value) {
		buff.writeBytes(value);
	}

	public void writeBytesWithSize(byte[] value) {
		buff.writeInt(value.length);
		buff.writeBytes(value);
	}

	public void writeLong(long value) {
		buff.writeLong(value);
	}

	public byte readByte() {
		return buff.readByte();
	}

	public byte[] readBytes() {
		byte[] bytes = new byte[buff.readableBytes()];
		buff.readBytes(bytes);
		return bytes;
	}

    public byte[] readBytes(int len) {
        byte[] bytes = new byte[len];
        buff.readBytes(bytes);
        return bytes;
    }

	public short readShort() {
		return buff.readShort();
	}

	public int readInt() {
		return buff.readInt();
	}

	public long readLong() {
		return buff.readLong();
	}

    public void writeBoolean(boolean value){
        buff.writeBoolean(value);
    }

    public boolean readBoolean(){
        return buff.readBoolean();
    }

	public void release() {
		buff.release();
	}

	public void writeString(String str) {
		String value = str == null ? "" : str;	// null转换为空字符串
		byte[] strArray = value.getBytes(CharsetUtil.UTF_8);
        if (strArray.length > Short.MAX_VALUE) {
            throw new IllegalArgumentException("字符串过长");
        }
		buff.writeShort(strArray.length);
		buff.writeBytes(strArray);
	}

	public String readString() {
		try {
			short length = buff.readShort();
			byte[] bytes = new byte[length];
			buff.readBytes(bytes);
			return new String(bytes, 0, length, "UTF-8");
		} catch (Exception e) {
			com.stars.util.log.CoreLogger.error(e.getMessage(), e);
		}
		return null;
	}
	
	public String readGMString(int head){
		try {
			int length = head^13542;
			byte[] bytes = new byte[length];
			buff.readBytes(bytes);
			return new String(bytes, 0, length, "UTF-8");
		} catch (Exception e) {
			com.stars.util.log.CoreLogger.error(e.getMessage(), e);
		}
		return null;
	}

	public void writeLongString(String str) {
        String value = str == null ? "" : str;	// null转换为空字符串
        byte[] strArray = value.getBytes(CharsetUtil.UTF_8);
        buff.writeInt(strArray.length);
        buff.writeBytes(strArray);
    }

    public String readLongString() {
        try {
            int length = buff.readInt();
            byte[] bytes = new byte[length];
            buff.readBytes(bytes);
            return new String(bytes, 0, length, "UTF-8");
        } catch (Exception e) {
            CoreLogger.error(e.getMessage(), e);
        }
        return null;
    }
}
