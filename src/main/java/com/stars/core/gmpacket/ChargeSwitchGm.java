package com.stars.core.gmpacket;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.packet.ClientChargeSwitchPacket;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;

import java.util.HashMap;
import java.util.Set;

/**
 * Optype:1054
 * 充值开关
 * Created by huwenjun on 2017/3/31.
 */
public class ChargeSwitchGm extends GmPacketHandler {
    private static final int OPEN = 0;
    private static final int CLOSE = 1;
    private static final int QUERY_INFO = 2;

    @Override
    public String handle(HashMap args) {
        String valueStr = (String) args.get("value");
        int value = 0;
        if (valueStr != null) {
            value = Integer.parseInt(valueStr);
        }
        String result;
        switch (value) {
            case OPEN: {
                result = openCharge();
            }
            break;
            case CLOSE: {
                result = closeCharge();
            }
            break;
            case QUERY_INFO: {
                result = queryInfo();
            }
            break;
            default: {
                result = "无效参数";
            }
            break;
        }

        GmPacketResponse response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(result));
        return response.toString();
    }

    private void sendAllNotifyPacket() {
        Set<String> roleIds = PlayerSystem.system().getActors().keySet();
        for (String roleId : roleIds) {
            Player player = PlayerSystem.get(Long.parseLong(roleId));
            ClientChargeSwitchPacket clientChargeSwitchPacket = new ClientChargeSwitchPacket();
            player.send(clientChargeSwitchPacket);
        }
    }

    private String queryInfo() {
        return "当前充值状态为:" + VipManager.chargeSwitchState;
    }

    private String closeCharge() {
        VipManager.chargeSwitchState = 1;
        sendAllNotifyPacket();
        return "关闭成功，当前充值状态为:" + VipManager.chargeSwitchState;
    }

    private String openCharge() {
        VipManager.chargeSwitchState = 0;
        sendAllNotifyPacket();
        return "开启成功，当前充值状态为:" + VipManager.chargeSwitchState;
    }
}
