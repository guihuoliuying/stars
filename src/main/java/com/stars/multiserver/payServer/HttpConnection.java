package com.stars.multiserver.payServer;

import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.HttpHeaders.Values;

public class HttpConnection {
	private Channel channel;
	public HttpConnection(Channel channel){
		this.channel = channel;
	}
	public boolean isActive(){
		return channel.isActive();
	}
	public void send(String string){
		try {
			byte data[] = string.getBytes("UTF-8");
			ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
			FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
					byteBuf);
			response.headers().set(HttpHeaders.Names.CONTENT_TYPE,"text/plain");
			response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, data.length);
			if (HttpHeaders.isKeepAlive(response)) {
				response.headers().set(HttpHeaders.Names.CONNECTION, Values.KEEP_ALIVE);
			}
			channel.writeAndFlush(response);
		} catch (Exception e) {
			LogUtil.error(e.getMessage(), e);
		}
	}
}
