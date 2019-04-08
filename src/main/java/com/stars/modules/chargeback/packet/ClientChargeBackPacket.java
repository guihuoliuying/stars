package com.stars.modules.chargeback.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.chargeback.ChargeBackPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/3/21.
 */
public class ClientChargeBackPacket extends PlayerPacket {
    public static final byte reqRule = 1;
    public static final byte reqYb = 2;
    private byte sendType = 0;

    /**
     * 规则描述
     */
    private String ruleDesc = null;
    private String chargeDescTemp = null;
    /**
     * 奖励字典<奖励itemid,奖励要求描述 >
     */
    private Map<Integer, Integer> rewardMap = new HashMap<>();
    private Integer currentYb = 0;
    /**
     * 前往充值按钮信息
     */
    private String btn_desc;
    private String openWindow;

    public ClientChargeBackPacket() {

    }

    @Override
    public short getType() {
        return ChargeBackPacketSet.clientChargeBack;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        /**
         * 下发请求类型
         */
        buff.writeByte(sendType);
        switch (sendType) {
            case reqRule: {
                buff.writeByte((byte) rewardMap.size());
                for (Map.Entry<Integer, Integer> entry : rewardMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                buff.writeString(this.ruleDesc);
                buff.writeString(this.chargeDescTemp);
                buff.writeString(this.btn_desc);
                buff.writeString(this.openWindow);
            }
            break;
            case reqYb: {
                buff.writeInt(this.currentYb);
            }
            break;
        }
    }

    public ClientChargeBackPacket(byte sendType, Map<Integer, Integer> rewardMap, String ruleDesc, String chargeDescTemp, String btn_desc, String openWindow) {
        this.sendType = sendType;
        this.rewardMap = rewardMap;
        this.ruleDesc = ruleDesc;
        this.chargeDescTemp = chargeDescTemp;
        this.btn_desc = btn_desc;
        this.openWindow = openWindow;
    }

    public ClientChargeBackPacket(byte sendType, int currentYb) {
        this.sendType = sendType;
        this.currentYb = currentYb;
    }

    public byte getSendType() {
        return sendType;
    }

    public void setSendType(byte sendType) {
        this.sendType = sendType;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public void setRuleDesc(String ruleDesc) {
        this.ruleDesc = ruleDesc;
    }

    public Map<Integer, Integer> getRewardMap() {
        return rewardMap;
    }

    public void setRewardMap(Map<Integer, Integer> rewardMap) {
        this.rewardMap = rewardMap;
    }

    public Integer getCurrentYb() {
        return currentYb;
    }

    public void setCurrentYb(Integer currentYb) {
        this.currentYb = currentYb;
    }

    public String getBtn_desc() {
        return btn_desc;
    }

    public void setBtn_desc(String btn_desc) {
        this.btn_desc = btn_desc;
    }

    public String getOpenWindow() {
        return openWindow;
    }

    public void setOpenWindow(String openWindow) {
        this.openWindow = openWindow;
    }
}
