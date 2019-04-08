package com.stars.core.gmpacket.giftpackage;

import com.stars.core.gmpacket.GmPacketDefine;
import com.stars.core.gmpacket.util.GmConnectUtil;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.network.server.handler.MainServerGmHandler;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.login.util.Md5Util;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketRequest;
import com.stars.services.ServiceHelper;
import com.stars.services.mail.userdata.RoleEmailPo;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/2/10.
 */
public class ExchangeGiftPackageGm extends GmPacketHandler {
    private String activateCode;// 激活码
    private long iSequenceNum;// 请求序列号
    private long roleId;// 角色ID
    private int channel;// 渠道ID
    private String account;// 账号

    public ExchangeGiftPackageGm() {
    }

    public ExchangeGiftPackageGm(String activateCode, long iSequenceNum, long roleId, int channel, String account) {
        this.activateCode = activateCode;
        this.iSequenceNum = iSequenceNum;
        this.roleId = roleId;
        this.channel = channel;
        this.account = account;
    }

    public void checkingGiftId() {
        GmPacketRequest gmPacketRequest = new GmPacketRequest();
        gmPacketRequest.setOpType(GmPacketDefine.EXCHANGE_GIFT_PACKAGE);
        gmPacketRequest.setSign(Md5Util.getMD5Str(MainServerGmHandler.publicKey + String.valueOf(GmPacketDefine.EXCHANGE_GIFT_PACKAGE)));
        gmPacketRequest.setArgsStr(this.toString());
        GmConnectUtil.util.sendGmRequest(gmPacketRequest);
    }

    @Override
    public String handle(HashMap args) {
        // 礼包id
        int giftId = Integer.parseInt((String) args.get("gift_id"));
        // 激活码状态,1:可使用,2:已使用,3:激活码不存在,4:玩家已领取过该礼包,5:已过期
        int type = Integer.parseInt((String) args.get("type"));
        long roleId = Long.parseLong((String) args.get("iSequenceNum"));// 请求序列号
        String title = (String) args.get("title");// 标题
        String content = (String) args.get("content");// 正文
        String guildIdStr = (String) args.get("guildId");
        int guildId = StringUtil.isEmpty(guildIdStr) ? 0 : Integer.parseInt(guildIdStr);// 公会id
        String activateCode = (String) args.get("activateCode");// 激活码
        // 礼包物品信息
        List<Map<String, String>> itemDict = (List<Map<String, String>>) args.get("itemData");
        Map<Integer, Integer> toolMap = new HashMap<>();
        for (Map<String, String> item : itemDict) {
            int itemId = Integer.parseInt(item.get("itemCode"));
            int itemCount = Integer.parseInt(item.get("count"));
            Integer oldValue = toolMap.get(itemId);
            if (oldValue == null) {
                toolMap.put(itemId, itemCount);
            } else {
                toolMap.put(itemId, oldValue + itemCount);
            }
        }
        String result = "";
        GiftLogEvent event = new GiftLogEvent(type);
        event.setSerizesid(roleId+"");
        event.setGiftID(giftId+"");
        event.setGuildId(guildId+"");
        event.setToolInfo("");
        switch (type) {
            case 1:// 可使用
                RoleEmailPo roleEmailPo = new RoleEmailPo();
                roleEmailPo.setReceiverId(roleId);
                roleEmailPo.setTitle(title);
                roleEmailPo.setText(content);
                roleEmailPo.setAffixMap(toolMap);
                ServiceHelper.emailService().gmSend(roleEmailPo);
                result = "operation_gamecode_switch_suc";
                ServerLogConst.console.info("兑换礼包成功roleid=" + roleId);
                event.setToolInfo(toolsInfo((HashMap<Integer,Integer>)toolMap));                             
                break;
            case 2:// 已使用
                result = "operation_gamecode_switch_used";
                break;
            case 3:// 激活码不存在
                result = "operation_gamecode_switch_notexist";
                break;
            case 4:// 玩家已领取过该礼包
                result = "operation_gamecode_switch_switched";
                break;
            case 5:// 已过期
                result = "operation_gamecode_switch_timeout";
                break;
        }
        Player player = PlayerSystem.get(roleId);
        ServerLogConst.console.info("send gift log|"+roleId);
        if(player!=null){    	
        	ServiceHelper.roleService().notice(roleId, event);
        	ServerLogConst.console.info("send gift log end|"+roleId);
        }
        if (!result.isEmpty()) {
            PacketManager.send(roleId, new ClientText(result));
        }
        return null;
    }
    
    private String toolsInfo(HashMap<Integer, Integer> map){
    	StringBuilder sb = new StringBuilder();
    	for(Integer code:map.keySet()){
    		sb.append(code).append("@").append(map.get(code)).append("&");
    	}
    	String tmp = sb.toString();
    	if(tmp.contains("&")){
    		tmp = tmp.substring(0,tmp.lastIndexOf("&"));
    	}
    	return tmp;
    }
    

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("");
        builder.append("{")
                .append("\"activateCode\":")
                .append("\"")
                .append(activateCode)
                .append("\"")
                .append(",")
                .append("\"iSequenceNum\":")
                .append(iSequenceNum)
                .append(",")
                .append("\"roleId\":")
                .append("\"")
                .append(roleId)
                .append("\"")
                .append(",")
                .append("\"account\":")
                .append("\"")
                .append(account)
                .append("\"")
                .append(",")
                .append("\"channel\":")
                .append(channel)
                .append("}");
        return builder.toString();
    }
}
