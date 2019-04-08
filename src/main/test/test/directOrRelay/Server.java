package test.directOrRelay;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zd on 2015/10/29.
 */
public class Server {

    private static String IP;
    public static int IO_THREADS;
    private static int PORT;
    private static int CLIENT_SEND_BUFF;
    private static int CLIENT_RECV_BUFF;
    public static int CONNECTIONS;
    public static int BIND_PORT;
    private static int SERVER_SEND_BUFF;
    private static int SERVER_RECV_BUFF;
    private static boolean RELAY;

    private static Channel[] channelList;

    public static void main(String[] args) throws Exception {
        loadConfig();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(IO_THREADS);
        bind(eventLoopGroup);
        channelList = new Channel[CONNECTIONS == 1 ? 1 : 10];
        buildConnection(eventLoopGroup);
    }

    public static void buildConnection(NioEventLoopGroup executors) throws InterruptedException {
        for (int i = 0; i < CONNECTIONS; i++) {
            Channel channel = new Bootstrap().group(executors)
                    .option(ChannelOption.SO_SNDBUF, CLIENT_SEND_BUFF)
                    .option(ChannelOption.SO_RCVBUF, CLIENT_RECV_BUFF)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                        }
                    })
                    .connect(IP, PORT)
                    .sync()
                    .channel();
            channelList[i] = channel;
        }
    }

    public static void loadConfig() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("test.properties"));
        IP = properties.getProperty("ip");
        PORT = Integer.parseInt(properties.getProperty("port"));
        IO_THREADS = Integer.parseInt(properties.getProperty("io_thread"));
        CONNECTIONS = Integer.parseInt(properties.getProperty("connection"));
        CLIENT_SEND_BUFF = Integer.parseInt(properties.getProperty("CLIENT_SEND_BUFF")) * 1024;
        CLIENT_RECV_BUFF = Integer.parseInt(properties.getProperty("CLIENT_RECV_BUFF")) * 1024;
        SERVER_SEND_BUFF = Integer.parseInt(properties.getProperty("SERVER_SEND_BUFF")) * 1024;
        SERVER_RECV_BUFF = Integer.parseInt(properties.getProperty("SERVER_RECV_BUFF")) * 1024;
        BIND_PORT = Integer.parseInt(properties.getProperty("bind_port"));
        RELAY = Boolean.parseBoolean(properties.getProperty("IS_RELAY"));
    }

    public static void bind(NioEventLoopGroup nioEventLoopGroup) throws Exception {
        new ServerBootstrap().group(nioEventLoopGroup, nioEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, SERVER_RECV_BUFF)
                .childOption(ChannelOption.SO_SNDBUF, SERVER_SEND_BUFF)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 0))
                                .addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                                    private int id;

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        id = channel();
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                        msg.retain();
                                        if (RELAY) {
                                            channelList[id].writeAndFlush(msg);
                                        } else {
                                            ctx.writeAndFlush(msg);
                                        }
                                    }

                                });
                    }
                })
                .bind(BIND_PORT)
                .sync();
    }

    private static AtomicInteger atomicInteger = new AtomicInteger();

    private static int channel() {
        return atomicInteger.incrementAndGet() % channelList.length;
    }

}
