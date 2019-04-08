package test.clientAndServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import test.directOrRelay.ResultManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd on 2015/10/29.
 */
public class SimpleClient1 {

    private static String IP;
    public static int IO_THREADS;
    private static int PORT;
    private static int CLIENT_SEND_BUFF;
    private static int CLIENT_RECV_BUFF;
    public static int CONNECTIONS;
    public static int REQUESTS;
    public static int BIND_PORT;
    private static int SERVER_SEND_BUFF;
    private static int SERVER_RECV_BUFF;

    private static Channel[] channelList;

    public static void main(String[] args) throws Exception {
        loadConfig();
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(IO_THREADS);
        CountDownLatch countDownLatch = new CountDownLatch(CONNECTIONS == 1 ? 1 : 10);
        bind(eventLoopGroup, countDownLatch);
        countDownLatch.await();
        Thread.sleep(1);
        System.out.println("build connection");
        channelList = new Channel[CONNECTIONS];
        buildConnection(eventLoopGroup);
        Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
            int round = 0;

            @Override
            public void run() {
                Random random = new Random();
                for (; ; ) {
                    try {
                        int r = 0;
                        while (r < REQUESTS) {
                            for (int i = random.nextInt(CONNECTIONS); i < CONNECTIONS; i++) {
                                final Channel ch = channelList[i];
                                ch.eventLoop().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        ByteBuf buf = Unpooled.directBuffer(256);
                                        buf.writeInt(252);
                                        buf.writeLong(System.currentTimeMillis());
                                        buf.writeBytes(new byte[244]);
                                        DefaultChannelPromise defaultChannelPromise = new DefaultChannelPromise(ch);
                                        defaultChannelPromise.addListener(new ChannelFutureListener() {
                                            @Override
                                            public void operationComplete(ChannelFuture future) throws Exception {
                                                if (future.isSuccess()) {
                                                    test.clientAndServer.ResultManager.resultManager().getSendNum().incrementAndGet();
                                                } else {
                                                    System.out.println("发送失败" + future.cause().getMessage());
                                                    future.cause().printStackTrace();
                                                }
                                            }
                                        });
                                        ch.writeAndFlush(buf, defaultChannelPromise);
                                    }
                                });
                                if (++r >= REQUESTS) {
                                    break;
                                }
                            }
                        }
                        if (++round % 10000 == 0) {
                            System.out.println("sleep");
                            Thread.sleep(10000);
                            System.out.println("do job");
                        }
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    ResultManager.resultManager().stat();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
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
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                    long sendTime = msg.readLong();
                                    long t = System.currentTimeMillis() - sendTime;
                                    if (t <= 1) {
                                        t = 1;
                                    } else if (t <= 2) {
                                        t = 2;
                                    } else if (t <= 3) {
                                        t = 3;
                                    } else if (t <= 4) {
                                        t = 4;
                                    } else if (t <= 5) {
                                        t = 5;
                                    } else if (t <= 10) {
                                        t = 10;
                                    } else if (t <= 15) {
                                        t = 15;
                                    } else if (t <= 20) {
                                        t = 20;
                                    } else if (t <= 30) {
                                        t = 30;
                                    } else if (t <= 40) {
                                        t = 40;
                                    } else if (t <= 60) {
                                        t = 60;
                                    } else if (t <= 80) {
                                        t = 80;
                                    } else if (t <= 100) {
                                        t = 100;
                                    } else {
                                        t = 101;
                                    }
                                    ResultManager.resultManager().add(t);
                                }
                            });
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
        REQUESTS = Integer.parseInt(properties.getProperty("requests"));
        CLIENT_SEND_BUFF = Integer.parseInt(properties.getProperty("CLIENT_SEND_BUFF")) * 1024;
        CLIENT_RECV_BUFF = Integer.parseInt(properties.getProperty("CLIENT_RECV_BUFF")) * 1024;
        SERVER_SEND_BUFF = Integer.parseInt(properties.getProperty("SERVER_SEND_BUFF")) * 1024;
        SERVER_RECV_BUFF = Integer.parseInt(properties.getProperty("SERVER_RECV_BUFF")) * 1024;
        BIND_PORT = Integer.parseInt(properties.getProperty("bind_port"));
    }

    public static void bind(NioEventLoopGroup nioEventLoopGroup, final CountDownLatch countDownLatch) throws Exception {
        new ServerBootstrap().group(nioEventLoopGroup, nioEventLoopGroup)
                .channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_RCVBUF, SERVER_RECV_BUFF)
                .childOption(ChannelOption.SO_SNDBUF, SERVER_SEND_BUFF)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast(new SimpleChannelInboundHandler<ByteBuf>() {

                                    @Override
                                    public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                        countDownLatch.countDown();
                                    }

                                    @Override
                                    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
                                        long sendTime = msg.readLong();
                                        long t = System.currentTimeMillis() - sendTime;
                                        if (t <= 1) {
                                            t = 1;
                                        } else if (t <= 2) {
                                            t = 2;
                                        } else if (t <= 3) {
                                            t = 3;
                                        } else if (t <= 4) {
                                            t = 4;
                                        } else if (t <= 5) {
                                            t = 5;
                                        } else if (t <= 10) {
                                            t = 10;
                                        } else if (t <= 15) {
                                            t = 15;
                                        } else if (t <= 20) {
                                            t = 20;
                                        } else if (t <= 30) {
                                            t = 30;
                                        } else if (t <= 40) {
                                            t = 40;
                                        } else if (t <= 60) {
                                            t = 60;
                                        } else if (t <= 80) {
                                            t = 80;
                                        } else if (t <= 100) {
                                            t = 100;
                                        } else {
                                            t = 101;
                                        }
                                        ResultManager.resultManager().add(t);
                                    }

                                });
                    }
                })
                .bind(BIND_PORT)
                .sync();
    }
}
