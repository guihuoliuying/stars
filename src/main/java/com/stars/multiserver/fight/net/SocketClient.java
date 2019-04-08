package com.stars.multiserver.fight.net;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.codec.GamePacketDecoder;
import com.stars.network.server.packet.Packet;
import com.stars.util.LogUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class SocketClient {
	
	private ChannelFuture channelFuture;
	
	private String flag;
	
	private int connId;
	
	public SocketClient(String ip,int port,String flag,int connId){
		this.flag = flag;
		this.connId = connId;
		channelFuture = connect(ip, port);
	}

	public ChannelFuture connect(String ip, int port) {
		final SocketClient sClient = this;
		EventLoopGroup ioGroup = new NioEventLoopGroup();
	    final EventLoopGroup packetGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(ioGroup).channel(NioSocketChannel.class)
					.option(ChannelOption.SO_SNDBUF, 128 * 1024)
					.option(ChannelOption.SO_RCVBUF, 128 * 1024)
					.handler(new ChannelInitializer<NioSocketChannel>() {
						@Override
						protected void initChannel(NioSocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(new GamePacketDecoder(Integer.MAX_VALUE, 1, 4, 1, 0))
									.addLast(new IdleStateHandler(8, 2, 0))
									.addLast("inbound",new SocketClientPacketHandler(sClient));
						}
					});
			ChannelFuture channelFuture = bootstrap.connect(ip, port).sync(); // 必须同步，异步有问题
			return channelFuture;
		} catch (Exception e) {
			com.stars.util.LogUtil.error(e.getMessage(), e);
		}
		return null;
	}
	
	public boolean isConnect(){
		if (channelFuture == null) {
			return false;
		}
		return true;
	}
	
	public void send(Packet packet) {
		Channel channel = channelFuture.channel();
		ByteBuf buf = channel.alloc().buffer();
		try {
			// 包头
			buf.writeByte((byte) -82);
			// 消息长度占位
			buf.writeInt(-1);
			// 写入连接ID
			buf.writeInt(connId);
			// 消息内容
			buf.writeShort(packet.getType());
			packet.writeToBuffer(new NewByteBuffer(buf));
			// 包尾
			buf.writeByte((byte) -81);
			// 消息长度计算
			int msgLength = buf.readableBytes();
			int writerIndex = buf.writerIndex();
			buf.writerIndex(writerIndex - msgLength + 1);
			// 1 + 4 + 1 = 6
			buf.writeInt(msgLength - 6);
			buf.writerIndex(writerIndex);
			channel.writeAndFlush(buf);
		} catch (Exception e) {
			com.stars.util.LogUtil.error(e.getMessage(), e);
		}

	}
	
	public void close(){
		if (this.channelFuture != null) {
			try {
				this.channelFuture.channel().close();
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
			
		}
	}

	public String getFlag() {
		return flag;
	}

	public ChannelFuture getChannelFuture() {
		return channelFuture;
	}

	public void setChannelFuture(ChannelFuture channelFuture) {
		this.channelFuture = channelFuture;
	}
}
