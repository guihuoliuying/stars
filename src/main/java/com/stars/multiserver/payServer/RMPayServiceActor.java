package com.stars.multiserver.payServer;


import com.stars.bootstrap.ServerManager;
import com.stars.server.main.actor.ActorServer;
import com.stars.services.SConst;
import com.stars.services.ServiceSystem;
import com.stars.services.pay.PayOrderInfo;
import com.stars.util.JsonUtil;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashMap;

public class RMPayServiceActor extends ServiceActor implements RMPayServerService {
	
	HashMap<String, HttpConnection>tradeNo_Channel;
	
	public static int PAY_STATUS_SUCCED = 0;
	
	public static int PYA_STATUS_FAILED = -1;
	
	public RMPayServiceActor(){
		ActorServer.getActorSystem().addActor(SConst.RMPayService, this);
		tradeNo_Channel = new HashMap<String, HttpConnection>();
	}

	@Override
	public void init() throws Throwable {
		ServiceSystem.getOrAdd(SConst.RMPayService, this);
	}

	@Override
	public void printState() {

	}

	@Override
	public void registerMainServer(int rmServerId, int mainServer) {
		
	}
	
	@Override
	public void doPayOrderCallBack(int rmServerId, String orderNo,int status) {
		LogUtil.info("order = "+orderNo+" do back");
		HttpConnection channel = tradeNo_Channel.get(orderNo);
		if (channel != null && channel.isActive()) {
			if (status == PYA_STATUS_FAILED) {
				channel.send(String.valueOf(PYA_STATUS_FAILED));
			}else {
				channel.send(orderNo);
			}
			LogUtil.info("response paycenter order="+orderNo);
		}
		tradeNo_Channel.remove(orderNo);
	}
	
//	cpTradeNo	String	Y	 通行证及支付服务生成的订单号
//	gameId 	int	Y	游戏编号
//	userId	String	Y	用户编号
//	roleId	String	N	角色编号
//	serverId	int	N	游戏区号(客户端传过来)
//	channelId	String	Y	渠道编号
//	itemId	String	N	购买的物品编号
//	itemAmount	int	Y	发货数量，优先使用money字段发货
//	privateField	String	N	应用自定义字段varchar(128)
//	保留字段，除非有特别说明，否则一律为空字符串。
//	money	int	Y	发货金额（分，值为-1时使用itemAmount字段发货
//	currencyType	String	Y	货币类型：CNY(人民币);USD(美元)；
//	fee	float	Y	实际支付金额(单位，元);
//	和currencyType结合；人民币以元为单位，USD以美元为单位；
//	status	String	Y	交易状态，0表示成功
//	giftId	String	N	赠品编号
//	sign	String	Y	验证签名，privateKey接入时分配
//	sign=md5(cpTradeNo|gameId|userId|roleId|serverId|channelId|itemId|itemAmount|privateField|money|status|privateKey)
//	字段如果为null则用空的字符串代替

	@Override
	public void onReceived0(Object message, Actor sender) {
		if (message instanceof PayMessage) {
			
			PayMessage payMessage = (PayMessage)message;
			
			if (payMessage.getgSon() == null || payMessage.getgSon().equals("")) {
				//nginx的心跳测试
				LogUtil.info("tgw 心跳测试");
				payMessage.getChannel().send("200");
				return;
			}
			LogUtil.info("recaive order : "+payMessage.getgSon());
			PayOrderInfo payOrderInfo = JsonUtil.fromJson(payMessage.getgSon(), PayOrderInfo.class);
			String tradeNo = payOrderInfo.getCpTradeNo();
			int serverId = payOrderInfo.getServerId();
			tradeNo_Channel.put(tradeNo, payMessage.getChannel());
			RMPayServerHelper.payService().recaivePayOrder(serverId, ServerManager.getServer().getConfig().getServerId(),payOrderInfo,false);
			LogUtil.info("send payOrder to mainServer="+serverId);
		}
	}
}
