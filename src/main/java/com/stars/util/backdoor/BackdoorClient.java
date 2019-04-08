package com.stars.util.backdoor;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zws on 2015/10/13.
 */
public class BackdoorClient {

    public static void main(String[] args) throws Exception {
        int port = 12345;
        if (args != null && args.length > 0) {
            port = Integer.parseInt(args[0]);
        }
        // build the connection
        final ByteBuf delimiter = Unpooled.copiedBuffer("\n".getBytes("UTF-8"));
        Bootstrap bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(1))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());

                        ch.pipeline().addLast(new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, delimiter));
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<String>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String s)
                                    throws Exception {
                                System.out.println(s);
                            }
                        });
                    }
                });
        Channel ch = bootstrap.connect("127.0.0.1", port).sync().channel();
        // wait for input
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
//            System.out.print("console> ");
            try {
                String line = br.readLine();
                ch.writeAndFlush(line + "\n");
            } catch (IOException e) {
                System.out.println("error> invalid command");
            }
        }
    }

}
