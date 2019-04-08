package com.stars.server.connector.handler;

import com.google.gson.Gson;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.server.connector.BackendSession;
import com.stars.server.connector.Connector;
import com.stars.server.connector.ConnectorUtil;
import com.stars.server.connector.FrontendSession;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.server.login.packet.ClientWarning;
import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zws on 2015/8/21.
 */
public class FrontendInboundHandler extends ChannelInboundHandlerAdapter {

    private Gson gson = new Gson();
    private static byte[] pow = new byte[256];

    static {
        for (int i = 0; i < 256; i++) {
            int j = i + 1;
            pow[i] = (byte) ((j * j + j * 4) % 10);
        }
    }

    private int connectionId;
    private com.stars.server.connector.FrontendSession session;
    private String account;
    private long heartTimestamp = 0;// 心跳时间戳
    private int heartErrorCount = 0;// 心跳异常次数
    private static int HEART_ERROR_LIMIT = 30;// 心跳连续异常阀值
    private static long HEART_CHECK_INTERVAL = 1500L;// 心跳间隔检测值(ms)

    // for test
    private int counter = 0;
    private long timestamp = 0L;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        connectionId = com.stars.server.connector.Connector.newConnectionId(); // 为每条连接分配一个ID

        // 检查负载
        if (!increasePayload()) {
            com.stars.util.LogUtil.info("连接服过载, 忽略新建连接, numOfConnection={}, tid={}, cid={}",
                    com.stars.server.connector.Connector.payload.get(), Thread.currentThread().getId(), connectionId);
            ctx.close();
            return;
        }

        // 初始化会话
        com.stars.server.connector.FrontendSession session = new com.stars.server.connector.FrontendSession(connectionId);
        session.frontendChannel(ctx.channel());
        session.connectTimestamp(System.currentTimeMillis()); // 连接时间
        this.session = session;
        com.stars.server.connector.Connector.putSession(connectionId, session);
        // 统计（连接）
//        if (true) {
//            session.stat(new ConnectorStat());
//        }
        // 统计（线程）：增加连接数
//        ConnectorStat stat = Connector.currentStat.get();
//        stat.incrConnection();

        // 访问控制
        if (com.stars.server.connector.Connector.config.needAccessControl()) {
            SocketAddress socketAddress = ctx.channel().remoteAddress();
            if (socketAddress instanceof InetSocketAddress
                    && com.stars.server.connector.Connector.config.inAllowsSet(((InetSocketAddress) socketAddress).getHostString())) {
                com.stars.util.LogUtil.info("访问控制接收连接，" + socketAddress);

            } else {
                ctx.close();
                com.stars.util.LogUtil.info("访问控制拒绝连接，" + socketAddress);
                return;
            }
        }

        if (com.stars.server.connector.Connector.config.isTestOn()) {
            // fixme: fix it later
//            session.backendChannelIndex(Connector.nextBackendSession().serverId());
            session.state(com.stars.server.connector.FrontendSession.BINDING);
        }
        super.channelActive(ctx);
        /* todo:temp code 客户端建立连接,同时建立连接服与游戏服的连接 */
        //Connector与Main之间的Channel是ThreadLocal，所以不能在本线程处理
//        this.session.frontendChannel().eventLoop().submit(new BindBackendTask(
//                this.session,this.account,null,1));
        bindBackend(this.session, com.stars.server.connector.Connector.MAIN_SERVER_ID, this.account);
        /* */
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        decreasePayload();

        com.stars.server.connector.FrontendSession session = com.stars.server.connector.Connector.getSession(connectionId);
//        if (session != null && session.state() == FrontendSession.BINDING) {
//            ConnectorUtil.send(getBackendChannel(session), new FrontendClosedN2mPacket(connectionId));
//        }
        if (session != null) {
            for (Integer serverId : session.getBoundServerIdSet()) {
                com.stars.server.connector.BackendSession backendSession = com.stars.server.connector.Connector.getBackendSession(serverId);
                if (backendSession != null) {
                    com.stars.server.connector.ConnectorUtil.send(backendSession.channel(), new FrontendClosedN2mPacket(connectionId));
                }
            }
        }
        com.stars.util.LogUtil.info("客户端连接关闭, account={}, sid={}, tid={}, elapse={}ms, numOfConnection={}",
                account,
                session != null ? session.backendChannelIndex() : -1,
                Thread.currentThread().getId(),
                session != null ? (System.currentTimeMillis() - session.connectTimestamp()) : 0,
                com.stars.server.connector.Connector.payload.get());

        // 统计（连接）：输出统计结果
//        if (session != null && session.stat() != null) {
//            LogUtil.info("stat[connection]: {}", session.stat().getPacketDistributeString());
//        }
        // 统计（线程）：减少连接数
//        ConnectorStat stat = Connector.currentStat.get();
//        stat.descConnection();

        com.stars.server.connector.Connector.removeSession(connectionId);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            switch (((IdleStateEvent) evt).state()) {
                case READER_IDLE:
                    com.stars.util.LogUtil.info("客户端连接心跳超时, account={}, tid={}",
                            account, Thread.currentThread().getId());
                    ctx.close(); // 丢失心跳
                    break;
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf inBuf = (ByteBuf) msg;
        try {
            /* todo: 解密的时候可以预留一部分空间，这样往后端发送数据就可以使用这段空间
             *
             */
            inBuf = decrypt3(inBuf);
            // 获取协议类型
            final short packetType = inBuf.getShort(0);
            byte subtype = inBuf.readableBytes() >= 7 ? inBuf.getByte(6) : (byte) 0xFF;
//            LogUtil.info("收包|account:{}|type:{}|subtype:{}|len:{}|tid:{}|serverId:{}",
//                    session.account(),
//                    String.format("0x%04X", packetType),
//                    String.format("0x%02X", subtype),
//                    inBuf.readableBytes(),
//                    Thread.currentThread().getId(),
//                    session.backendChannelIndex());
            Channel ch = null;
            if (!handleProtocol(session, packetType, inBuf)) { // 处理协议
                if (session != null && session.state() == com.stars.server.connector.FrontendSession.BINDING
                        && (ch = getBackendChannel(session)) != null) {
                    writeToBackend(ch, connectionId, inBuf); // 发往后端连接
                } else {
                    if (session == null) {
                        com.stars.util.LogUtil.error("转发数据包错误: 会话为空");
                    } else if (session.state() != com.stars.server.connector.FrontendSession.BINDING) {
                        com.stars.util.LogUtil.error(
                                "转发数据包错误: 没有绑定游戏服, account={}, packetType=0x{}, tid={}",
                                account, Integer.toHexString(packetType), Thread.currentThread().getId());
                    } else if (ch == null) {
                        com.stars.util.LogUtil.info("转发数据包错误: 游戏服");
                    }
                }
            }
//            System.out.println("total: " + (System.currentTimeMillis() - s));
        } finally {
            ReferenceCountUtil.release(inBuf);
        }

    }

    /**
     * 绑定前端连接和后端连接（游戏服）
     * @param session 会话
     * @param serverId 游戏服id
     * @param account 用户账号
     */
    public static void bindBackend(com.stars.server.connector.FrontendSession session, int serverId, String account) {
        com.stars.server.connector.BackendSession ch = null;

        ch = com.stars.server.connector.Connector.getBackendSession(serverId);
        if (ch == null) {
            /* 游戏服不可用，则丢弃请求（不进行切线） */
            com.stars.util.LogUtil.info("后端服务不可用, account={}, sid={}, tid={}",
                    account, serverId, Thread.currentThread().getId());
            if (serverId == com.stars.server.connector.Connector.MAIN_SERVER_ID) {
                session.frontendChannel().close();
            }
            return;
        } else {
            com.stars.util.LogUtil.info("绑定后端服务, account={}, sid={}, tid={}",
                    account, ch.serverId(), Thread.currentThread().getId());
        }
        if (ch == null) {
            com.stars.util.LogUtil.info("客户端登陆, 没有可用的游戏服连接, account={}, sid={}, tid={}",
                    account, serverId, Thread.currentThread().getId());
            session.frontendChannel().close();
            return;
        }
        if (!ch.channel().isWritable()) {
            //对应游戏服连接不可写，忽略新连接
            com.stars.util.LogUtil.info("后端服务不可写, account={}, sid={}, tid={}",
                    account, ch.serverId(), Thread.currentThread().getId());
//            session.frontendChannel().close();
            return;
        }
        session.account(account);
        session.backendChannelIndex(ch.serverId());
        session.state(com.stars.server.connector.FrontendSession.BINDING);
    }

    private Channel getBackendChannel(com.stars.server.connector.FrontendSession session) {
        BackendSession backendSession = com.stars.server.connector.Connector.getBackendSession(session.backendChannelIndex());
        if (backendSession != null) {
            return backendSession.channel();
        }
        return null;
    }

    private static ByteBuf decrypt(ByteBuf buf) {
        int readerIndex = buf.readerIndex();
        int writerIndex = buf.writerIndex();
//        System.out.println("readerIndex=" + readerIndex + ", writerIndex=" + writerIndex + ", delta=" + (writerIndex-readerIndex));
        for (int l = 0, t = readerIndex; t < writerIndex; l++, t++) {
            if (l < 256) {
                buf.setByte(t, buf.getByte(t) - pow[l]);
            } else {
                int index = l + 1;
                buf.setByte(t, buf.getByte(t) - (index * index + index * 4) % 10);
            }
        }
        return buf;
    }

    /* 因为decrypt()速率不稳定，有时会耗时50ms；所以使用另外一种方式 */
    private static ByteBuf decrypt2(ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        for (int i = 0; i < data.length; i++) {
            if (i < 256) {
                data[i] = (byte) (data[i] - pow[i]);
            } else {
                int index = i + 1;
                data[i] = (byte) (data[i] - (index * index + index * 4) % 10);
            }
        }
        ByteBuf out = buf.alloc().buffer(data.length);
        out.writeBytes(data);
        ReferenceCountUtil.release(buf);
        return out;
    }

    /* 减少一次遍历数组 */
    private static ByteBuf decrypt3(ByteBuf inBuf) {
        final byte[] data = new byte[inBuf.readableBytes()];
        inBuf.forEachByte(new ByteBufProcessor() {
            int i = 0;
            @Override
            public boolean process(byte value) throws Exception {
                if (i < 256) {
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

    public static void writeToBackend(Channel ch, int connectionId, ByteBuf inBuf) {
        ByteBuf outBuf = ch.alloc().buffer(inBuf.readableBytes() + 10);
        try {
            outBuf.writeByte(-82); // 包头
            outBuf.writeInt(inBuf.readableBytes() + 4); // 包长
            outBuf.writeInt(connectionId); // 玩家ID
            outBuf.writeBytes(inBuf); // 负载
            outBuf.writeByte(-81); // 包尾

            // write to backend directly
            ch.unsafe().write(outBuf, new DefaultChannelPromise(ch, ch.eventLoop()));
            ch.unsafe().flush();
        } catch (Exception e) {
            ReferenceCountUtil.release(outBuf);
            throw e;
        }

    }

    /**
     * 处理协议
     * @param session 会话
     * @param packetType 数据包类型
     * @param inBuf 包数据
     * @return 是否要发往后端
     */
    private boolean handleProtocol(com.stars.server.connector.FrontendSession session, short packetType, ByteBuf inBuf) {
        /* 统计数据包 */
//        ConnectorStat stat = Connector.currentStat.get();
//        stat.packetNumber(packetType, 1L);
//        stat.packetLength(packetType, inBuf.readableBytes());
//        if (session.stat() != null) { // 连接相关的数据包
//            session.stat().packetNumber(packetType, 1L);
//            session.stat().packetLength(packetType, inBuf.readableBytes());
//        }
        /* 处理协议 */
        switch (packetType) {
            case com.stars.server.connector.Connector.PROTO_CLIENT_LOGIN: // 登录协议
                String token = ConnectorUtil.getString(inBuf, 6); // packetType + packetId
                session.account(getAccountFromToken(token));
                return false;
            case com.stars.server.connector.Connector.PROTO_CLIENT_HEARTBEAT2: // 客户端心跳
                // 是否快速回一个包给客户端
                if (session.backendChannelIndex() != com.stars.server.connector.Connector.MAIN_SERVER_ID) {
                    ByteBuf buf = session.frontendChannel().alloc().buffer();
                    try {
                        buf.writeShort(0x30); // 心跳包协议
                        buf.writeInt(0); // packet id
                        buf.writeLong(0); // 时间戳
                        BackendInboundHandler.writeToFrontend(session.frontendChannel(), buf);
                    } catch (Throwable t) {
                        com.stars.util.LogUtil.error("快速回复心跳包异常", t);
                    } finally {
                        ReferenceCountUtil.release(buf);
                    }
                }
                // 心跳加速判断
                if (System.currentTimeMillis() - heartTimestamp <= HEART_CHECK_INTERVAL) {
                    heartErrorCount++;
                    com.stars.util.LogUtil.info("连接服心跳检测异常,连续异常次数={}", heartErrorCount);
                    if (heartErrorCount >= HEART_ERROR_LIMIT) {
                        com.stars.util.LogUtil.info("连接服心跳检测连续异常次数超过阀值 {},断掉前端连接", HEART_ERROR_LIMIT);
                        // 提示
                        ClientWarning packet = new ClientWarning("心跳加速异常");
                        ByteBuf buf = session.frontendChannel().alloc().buffer();
                        try {
                            buf.writeShort(packet.getType());
                            buf.writeInt(0); // packetId
                            packet.writeToBuffer(new NewByteBuffer(buf));
                            BackendInboundHandler.writeToFrontend(session.frontendChannel(), buf);
                        } catch (Throwable t) {
                            com.stars.util.LogUtil.error("心跳加速异常", t);
                            ReferenceCountUtil.release(buf);
                        }
                        this.heartErrorCount = 0;
                        // 关闭前端连接
                        session.frontendChannel().close();
                        return true;
                    }
                } else {
                    this.heartErrorCount = 0;
                }
                this.heartTimestamp = System.currentTimeMillis();
                // do nothing
                return false;
            case 0x7E00: // 测试数据包类型
                if (com.stars.server.connector.Connector.config.isTestOn()) {
                    if (!com.stars.server.connector.Connector.config.needRelayTestPacket()) {
                        ByteBuf tmpBuf = inBuf.slice();
                        tmpBuf.setShort(0, 0x7E01);
                        BackendInboundHandler.writeToFrontend(session.frontendChannel(), tmpBuf);
                        return true;
                    }
                } else {
                    return true;
                }
            case 0x7E10: // test
                if (com.stars.server.connector.Connector.config.isTestOn()) {
                    if (!com.stars.server.connector.Connector.config.needRelayTestPacket()) {
                        ByteBuf tmpBuf = inBuf.slice();
                        tmpBuf.setShort(0, 0x7E11);
                        BackendInboundHandler.writeToFrontend(session.frontendChannel(), tmpBuf);
                        return true;
                    }
                } else {
                    return true;
                }
        }
        return false;
    }

    private String getAccountFromToken(String token) {
        try {
            Map map = gson.fromJson(token, HashMap.class);
            if (map.containsKey("account")) {
                return (String) map.get("account");
            } else if (map.containsKey("uid") && map.containsKey("channel")) {
                return map.get("uid") + "#" + ((String) map.get("channel")).split("@")[0];
            } else {
                return "null";
            }
        } catch (Throwable t) {
            com.stars.util.LogUtil.error("", t);
            return "null";
        }
    }

    private boolean increasePayload() {
        return com.stars.server.connector.Connector.payload.incrementAndGet() <= com.stars.server.connector.Connector.config.thresholdOfPayload();
    }

    private void decreasePayload() {
        int tmp = Connector.payload.decrementAndGet();
        if (tmp < 0) {
            com.stars.util.LogUtil.info("连接服负载计算异常，payload={}", tmp);
        }
    }

    class BindBackendTask implements Runnable {

        private com.stars.server.connector.FrontendSession session;
        private String account;
        private ByteBuf inBuf;
        private int serverId;

        private BindBackendTask(com.stars.server.connector.FrontendSession session, String account, ByteBuf inBuf, int serverId){
            this.session = session;
            this.account = account;
            this.inBuf = inBuf;
            this.serverId = serverId;
        }

        @Override
        public void run() {
            if (session.state() == com.stars.server.connector.FrontendSession.BINDING) { // 因为处于异步状态，所以要再判断一次
                com.stars.util.LogUtil.info("客户端连接已绑定游戏服, cid={}, account={}, sid={}",
                        session.connectionId(), account, serverId);
                return;
            }
            bindBackend(session, this.serverId, this.account); // 绑定前端连接和后端连接（游戏服）
            Channel ch = null;
            if (session != null && session.state() == com.stars.server.connector.FrontendSession.BINDING
                    && (ch = getBackendChannel(session)) != null) {
                writeToBackend(ch, session.connectionId(), inBuf); // 发往后端连接
            } else {
                if (session == null) {
                    com.stars.util.LogUtil.error("转发数据包错误: 会话为空");
                } else if (session.state() != FrontendSession.BINDING) {
                    com.stars.util.LogUtil.error(
                            "转发数据包错误: 没有绑定游戏服, account={}, packetType={}, tid={}",
                            this.account, Thread.currentThread().getId());
                } else if (ch == null) {
                    LogUtil.info("转发数据包错误: 游戏服");
                }
            }
        }
    }

}
