package com.stars.network.server.handler;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketRequest;
import com.stars.util.LogUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.HashMap;

/**
 * Created by liuyuheng on 2016/12/9.
 */
public class MainServerGmHandler extends ChannelInboundHandlerAdapter {
	public static String publicKey = "xjw1314";
    private Gson gson;
	
	@Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        com.stars.util.LogUtil.info("main server gm handler is active");
        gson = new GsonBuilder()
                .registerTypeAdapter(
                        new TypeToken<HashMap<String, Object>>() {}.getType(),
                        new GmJsonDeserializer()).create();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        com.stars.util.LogUtil.info("main server gm handler is not active");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NewByteBuffer buffer = new NewByteBuffer((ByteBuf) msg);
        // 包头,4个字节
        int head = buffer.readInt();
        com.stars.util.LogUtil.info("head:"+head);
        // 包体
        String body = buffer.readGMString(head);
        if (!body.contains("\"args\":") && body.contains("\"result\":")) {
            body = body.replace("\"result\":[{", "\"result\":{").replace("}],\"status\"", "},\"status\"");
        }
        com.stars.server.main.gmpacket.GmPacketRequest gmRequest = gson.fromJson(body, GmPacketRequest.class);

        String result =null;
        // todo:验证签名
        com.stars.util.LogUtil.info("optype:"+gmRequest.getOpType());
        // 签名为空则不验证签名
        if (gmRequest.getSign() != null && !gmRequest.getSign().isEmpty()) {
            if (!gmRequest.getSign().equals(makeSign(String.valueOf(gmRequest.getOpType())))) {
                result = GmPacketHandler.resultToJson("验证不通过u");
                com.stars.util.LogUtil.info("sign:" + gmRequest.getSign());
                com.stars.util.LogUtil.info("makesign:" + makeSign(String.valueOf(gmRequest.getOpType())));
            }
        }
        if(result==null){
        	result = gmRequest.execute();        	
        }
        com.stars.util.LogUtil.info("result:"+result);
        if (result == null)
            return;
        try {
            ByteBuf byteBuf = ctx.channel().alloc().buffer();
            buffer = new NewByteBuffer(byteBuf);
            byte[] strArray = result.getBytes(CharsetUtil.UTF_8);
            // 包头,4个字节,包体长度^13542
            buffer.writeInt(strArray.length ^ 13542);
            buffer.writeBytes(strArray);
            ctx.channel().writeAndFlush(buffer.getBuff());
        }catch (Exception excepetion){
            com.stars.util.LogUtil.error(excepetion.getMessage(),excepetion);
            LogUtil.info("MainServerGmHandler|writing Err|Exception:"+result);
        }
    }

    public String makeSign(String body){	
    	return "";
    }
}
