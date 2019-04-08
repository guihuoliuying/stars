package com.stars.core.gmpacket.util;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.codec.GmPacketDecoder;
import com.stars.network.server.handler.MainServerGmHandler;
import com.stars.server.main.gmpacket.GmPacketRequest;
import com.stars.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.nio.ByteOrder;

/**
 * Created by liuyuheng on 2017/2/10.
 */
public class GmConnectUtil {
    public static GmConnectUtil util = new GmConnectUtil();
    private ChannelFuture channelFuture;
    private String ip;
    private int port;

    public GmConnectUtil() {
        BootstrapConfig config = ServerManager.getServer().getConfig();
        this.ip = config.getProps().get(config.getServer()).getProperty("gmReqIp");
        this.port = Integer.parseInt(config.getProps().get(config.getServer()).getProperty("gmReqPort", "0"));
    }

    public void sendGmRequest(GmPacketRequest gmPacketRequest) {
        boolean isConnect = true;
        if (channelFuture == null || !channelFuture.channel().isActive()) {
            isConnect = connect();
            LogUtil.info("======exchange new connect,status={}", isConnect);
        }
        if (isConnect) {
            send(gmPacketRequest);
            LogUtil.info("======exchange send body={}", gmPacketRequest.toString());
        }
//        close();
    }

    public boolean connect() {
        EventLoopGroup ioGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ioGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new GmPacketDecoder(Short.MAX_VALUE, 0, 4));
                            ch.pipeline().addLast(new IdleStateHandler(8, 2, 0));
                            ch.pipeline().addLast(new MainServerGmHandler());
                        }
                    });
            this.channelFuture = bootstrap.connect(ip, port).sync();
        } catch (Exception e) {
            LogUtil.error("创建运营gm socket连接失败");
            LogUtil.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public void send(GmPacketRequest gmPacketRequest) {
        Channel channel = channelFuture.channel();
        ByteBuf buf = channel.alloc().buffer();
        buf = buf.order(ByteOrder.BIG_ENDIAN);
        NewByteBuffer buffer = new NewByteBuffer(buf);
        try {
            String body = gmPacketRequest.toString();
            byte[] strArray = body.getBytes(CharsetUtil.UTF_8);
            // 包头,4个字节,包体长度^13542
            buffer.writeInt(strArray.length ^ 13542);
            // 包体
            buffer.writeBytes(strArray);
            channel.writeAndFlush(buffer.getBuff());
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public void close() {
        if (this.channelFuture != null) {
            try {
                this.channelFuture.channel().close();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }

        }
    }
}
