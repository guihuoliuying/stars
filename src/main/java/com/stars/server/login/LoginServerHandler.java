package com.stars.server.login;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.session.GameSession;
import com.stars.server.login.packet.ServerLoginCheck;
import com.stars.server.login.packet.ServerRegister;
import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.ReferenceCountUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Created by liuyuheng on 2015/12/28.
 */
public class LoginServerHandler extends ChannelInboundHandlerAdapter {
	
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = ctx.channel().remoteAddress().toString();
        com.stars.util.LogUtil.info("客户端连接 " + remoteAddr + " 连接断开... ");
        super.channelInactive(ctx);
    }

    private static byte[] pow = new byte[1048576];

    static {
        for (int i = 0; i < 1048576; i++) {
            int j = i + 1;
            pow[i] = (byte) ((j * j + j * 4) % 10);
        }
    }

    private static byte[] gzipData(byte[] payload) {
        byte[] data = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(payload);
            gzos.close();
            data = baos.toByteArray();
            baos.close();
        } catch (IOException e) {
        	com.stars.util.LogUtil.error(e.getMessage(), e);
        } finally {
            return data;
        }
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

    /* 减少一次遍历数组 */
    private static ByteBuf decrypt3(ByteBuf inBuf) {
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
        ReferenceCountUtil.release(inBuf);
        return outBuf;
    }

    
    public static void sendToClient(GameSession session, Packet packet) {
    	packet.setSession(session);
        NewByteBuffer buff = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        buff.writeShort(packet.getType());
        packet.writeToBuffer(buff);
        writeToFrontend(session.getChannel(), buff.getBuff());
    }

    /**
     * 往客户端连接写入数据包
     *
     * @param ch
     * @param inBuf
     * @return 负载长度（因为经过压缩处理）
     */
    private static long writeToFrontend(Channel ch, ByteBuf inBuf) {
        byte[] payload = new byte[inBuf.readableBytes()];
        inBuf.readBytes(payload);
        // compress data if required
        byte compressType = 2;
        if (payload.length > 4096) {
            payload = gzipData(payload);
            compressType = 1;
        }
        // encrypt
        payload = encrypt(payload);
        // write the buffer
        ByteBuf outBuf = ch.alloc().buffer();
        try {
            outBuf.writeByte((byte) -82);
            outBuf.writeInt(payload.length + 1);
            outBuf.writeByte(compressType);
            outBuf.writeBytes(payload);
            outBuf.writeByte((byte) -81);

            // send the message
            ch.unsafe().write(outBuf, new DefaultChannelPromise(ch));
            ch.unsafe().flush();
            return payload.length + 1; // 1为压缩类型
        } catch (Exception e) {
            ReferenceCountUtil.release(outBuf);
            throw e;
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuf = (ByteBuf) msg;
        try {
            inBuf = decrypt3(inBuf);
            NewByteBuffer buff = new NewByteBuffer(inBuf);
            // get the packet type
            short packetType = buff.readShort();
            GameSession session = new GameSession();
            session.setChannel(ctx.channel());
            Packet packet = null;
            switch (packetType) {
                case com.stars.server.login.LoginConstant.SERVER_LOGINCHECK:
                	com.stars.util.LogUtil.info("---receive login check packet");
                    packet = new ServerLoginCheck();
                    execPacket(packet, session, buff);
                    break;
                case com.stars.server.login.LoginConstant.SERVER_REGISTER:
                	com.stars.util.LogUtil.info("---receive login register packet");
                    packet = new ServerRegister();
                    execPacket(packet, session, buff);
                case LoginConstant.SERVER_HEARTBEAT:
                	com.stars.util.LogUtil.info("receive heartbeat packet");
                    break;
                default:
                	LogUtil.error("unknown packet type," + packetType);
                    break;
            }
        } finally {
            ReferenceCountUtil.release(inBuf);
        }

    }

    private void execPacket(Packet packet, GameSession session, NewByteBuffer buff){
        packet.setSession(session);
        packet.readFromBuffer(buff);
        packet.execPacket();
    }
}
