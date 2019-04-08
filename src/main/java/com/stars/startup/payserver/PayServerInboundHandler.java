package com.stars.startup.payserver;

import com.stars.multiserver.payServer.HttpConnection;
import com.stars.multiserver.payServer.PayMessage;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.SConst;
import com.stars.core.actor.Actor;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.CharsetUtil;

import java.io.IOException;

public class PayServerInboundHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
//			HttpRequest request = (HttpRequest)msg;
//			LogUtil.info("request url:"+request.getUri());
			return;
		}
		if (msg instanceof HttpContent) {
			HttpContent hContent = (HttpContent)msg;
			ByteBuf buf = hContent.content();
			String content = buf.toString(CharsetUtil.UTF_8);
			buf.release();
			PayMessage payMessage = new PayMessage(new HttpConnection(ctx.channel()), content);
			ActorServer.getActorSystem().getActor(SConst.RMPayService).tell(payMessage, Actor.noSender);
			
//			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
//					Unpooled.wrappedBuffer("aaaaaaaaaa".getBytes("UTF-8")));
//			response.headers().set(HttpHeaders.CONTENT_LENGTH,)
//			ctx.writeAndFlush(response);
		}
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		if (cause instanceof IOException) {
			return;
		}else {
			super.exceptionCaught(ctx, cause);
		}
	}
}
