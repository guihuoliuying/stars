package test.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd on 2015/10/29.
 */
public class SimpleClient {

    private static String IP;
    public static int IO_THREADS;
    private static int PORT;
    private static int SEND_BUFFER;
    private static int RECV_BUFFER;
    public static int CONNECTIONS;
    public static int REQUESTS;

    private static Channel[] channelList;

    public static void main(String[] args) throws IOException, InterruptedException {
        final Random random = new Random();
        loadConfig();
        channelList = new Channel[CONNECTIONS];
        buildConnection();
        Executors.newScheduledThreadPool(1).execute(new Runnable() {
            int round = 0;

            @Override
            public void run() {
                for (; ; ) {
                    int r = 0;
                    while (r < REQUESTS) {
//                        for (final Channel channel : channelList) {
//                            channel.eventLoop().execute(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ByteBuf buf = Unpooled.directBuffer(12);
//                                    buf.writeInt(8);
//                                    buf.writeLong(System.currentTimeMillis());
//                                    DefaultChannelPromise defaultChannelPromise = new DefaultChannelPromise(channel);
//                                    defaultChannelPromise.addListener(new ChannelFutureListener() {
//                                        @Override
//                                        public void operationComplete(ChannelFuture future) throws Exception {
//                                            ResultManager.resultManager().getSendNum().incrementAndGet();
//                                        }
//                                    });
//                                    channel.writeAndFlush(buf, defaultChannelPromise);
//                                }
//                            });
//                            if (++r == REQUESTS) break;
//                        }
                        for (int i = random.nextInt(CONNECTIONS); i < CONNECTIONS; i++) {
                            final Channel ch = channelList[i];
                            ch.eventLoop().execute(new Runnable() {
                                @Override
                                public void run() {
                                    ByteBuf buf = Unpooled.directBuffer(12);
                                    buf.writeInt(8);
                                    buf.writeLong(System.currentTimeMillis());
                                    DefaultChannelPromise defaultChannelPromise = new DefaultChannelPromise(ch);
                                    defaultChannelPromise.addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) throws Exception {
                                            ResultManager.resultManager().getSendNum().incrementAndGet();
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
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("do job");
                    }
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        });
        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
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

//    private static List<Channel> channelList = new ArrayList<>();


    public static void buildConnection() throws InterruptedException {
        EventLoopGroup executors = new NioEventLoopGroup(IO_THREADS);
        int conn = CONNECTIONS;
        for (int i = 0; i < CONNECTIONS; i++) {
            Channel channel = new Bootstrap().group(executors)
                    .option(ChannelOption.SO_SNDBUF, SEND_BUFFER)
                    .option(ChannelOption.SO_RCVBUF, RECV_BUFFER)
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
                                    if (t <= 5) {
                                        t = 5;
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
//            channelList.add(channel);
//            if (--conn == 0) {
//                break;
//            }
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
        SEND_BUFFER = Integer.parseInt(properties.getProperty("send_buffer")) * 1024;
        RECV_BUFFER = Integer.parseInt(properties.getProperty("receive_buffer")) * 1024;
    }

}
