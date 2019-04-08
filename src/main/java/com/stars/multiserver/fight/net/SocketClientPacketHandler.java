package com.stars.multiserver.fight.net;

import com.stars.bootstrap.ServerManager;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.server.fight.MultiServer;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SocketClientPacketHandler extends SimpleChannelInboundHandler<ByteBuf> {
	
	private SocketClient sc;
	
	public SocketClientPacketHandler(SocketClient sc){
		this.sc = sc;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg)
			throws Exception {
		com.stars.util.LogUtil.info("socketClient 收到战斗服返回的数据");
		ByteBuf buf = (ByteBuf) msg;
        final int connectionId = buf.readInt();
        final com.stars.network.server.packet.Packet packet = Packet.newPacket(new NewByteBuffer(buf));
        if (sc.getFlag().equals(MultiServerHelper.SOCKETCLIENT_FLAG_PVPSERVICE2FIGHTSERVER)) {
        	ServiceSystem.get("pkService").tell(packet, com.stars.core.actor.Actor.noSender);
		}else if (sc.getFlag().equals(MultiServerHelper.SOCKETCLIENT_FLAG_LOOTSERVER2FIGHTSERVER)) {
//			ServiceSystem.get("lootTreasureService").tell(packet, Actor.noSender);
//			MultiServer.getBusiness().dispatch(packet);
			((MultiServer)(ServerManager.getServer())).getBusiness().dispatch(packet);
		}else if (sc.getFlag().equals(MultiServerHelper.SOCKETCLIENT_FLAG_LOOTSERVICE2LOOTSERVER)) {
			ServiceSystem.get("lootTreasureService").tell(packet, Actor.noSender);
		}
        
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    LogUtil.info("游戏服到战斗服的连接建立成功");
	    super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		sc.setChannelFuture(null);
	    super.channelInactive(ctx);
	}

}
