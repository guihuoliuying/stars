package com.stars.modules.weeklygift.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.weeklygift.WeeklyGiftPacketSet;
import com.stars.modules.weeklygift.prodata.WeeklyGiftVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class ClientWeeklyGift extends PlayerPacket {
    public static final byte C_PRODUCT = 0x00;
    public static final byte C_USER = 0x01;

    private byte subType;

    private String actTitle;
    private String timeText;
    private String ruleText;
    private List<WeeklyGiftVo> vos;

    private int charge;
    private Map<Integer, Integer> giftDays;
    private OperateActVo vo;

    public ClientWeeklyGift() {
    }

    public ClientWeeklyGift(byte subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case C_PRODUCT:
                buff.writeString(actTitle);
                buff.writeString(timeText);
                buff.writeString(ruleText);
                buff.writeByte((byte) (vos != null ? vos.size() : 0));
                if (vos != null) {
                    for (WeeklyGiftVo vo : vos) {
                        vo.writeToBuff(buff);
                        com.stars.util.LogUtil.info("subType:{}, weeklyVo:{}", subType, vo.toString());
                    }
                }
                break;
            case C_USER:
                buff.writeInt(charge);
                buff.writeByte((byte) (giftDays != null ? giftDays.size() : 0));
                if (giftDays != null) {
                    for (Map.Entry<Integer, Integer> entry : giftDays.entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
                LogUtil.info("subType:{},charge:{},giftDays:{}", subType, charge, giftDays);
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return WeeklyGiftPacketSet.C_WEEKLYGIFT;
    }

    public void setActTitle(String actTitle) {
        this.actTitle = actTitle;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }

    public void setRuleText(String ruleText) {
        this.ruleText = ruleText;
    }

    public void setVos(List<WeeklyGiftVo> vos) {
        this.vos = vos;
    }

    public void setVo(OperateActVo vo) {
        this.vo = vo;
    }

    public void setCharge(int charge) {
        this.charge = charge;
    }

    public void setGiftDays(Map<Integer, Integer> giftDays) {
        this.giftDays = giftDays;
    }
}
