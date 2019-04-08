package com.stars.modules.baby.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.baby.BabyPacketSet;
import com.stars.modules.baby.prodata.BabySweepVo;
import com.stars.modules.baby.prodata.BabyVo;
import com.stars.modules.baby.usedata.RoleBaby;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.*;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class ClientBaby extends PlayerPacket {
    public static final byte defaultValue = 0x00;//
    public static final byte sweep_vo = 0x01;//扫荡的产品数据下行
    public static final byte change_name = 0x02;//改名下行
    public static final byte can_buy_count = 0x03;
    public static final byte sweep_result = 0x04;
    public static final byte baby_follow = 0x05;//宝宝是否跟随
    public static final byte feed_tips = 0x06;
    public static final byte SEND_FASHION_LIST = 0x07;//时装列表
    public static final byte SEND_USING_FASHION_ID = 0x08;//当前使用时装id
    public static final byte SEND_ACTIVE_FASHION_SUCCESS = 0x09;//激活时装成功（携带时装id）

    private byte subType;

    private BabyVo babyVo;//当前阶段的产品数据
    private RoleBaby roleBaby;//当前用户数据
    private List<BabySweepVo> babySweepVos = new ArrayList<>();
    private Map<Integer, Byte> sweepCount = new HashMap<>();
    private int reqItemId;
    private int reqItemCount;
    private String newName;
    private int canBuyTime;
    private int reqGold;
    private byte isFollow;
    private int maxBabyLevel;
    private int babyStage;
    private int babyLevel;
    private int tips;
    private int fashionId;
    private Map<Integer, Integer> itemMap = new HashMap<>();

    public ClientBaby(byte subType) {
        this.subType = subType;
    }

    public ClientBaby() {
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case defaultValue:
                buff.writeInt(reqItemId);
                buff.writeInt(reqItemCount);
                babyVo.writeToBuff0(buff);
                buff.writeInt(maxBabyLevel);
                roleBaby.writeToBuff(buff, babyStage);
                buff.writeInt(canBuyTime);
                buff.writeInt(reqGold);
                break;
            case sweep_vo:
                buff.writeByte((byte) sweepCount.size());
                for (Map.Entry<Integer, Byte> entry : sweepCount.entrySet()) {
                    buff.writeInt(entry.getKey());//扫荡id:1,2,3,4
                    buff.writeByte(entry.getValue());//剩余次数 <=0都是没次数了
                }
                break;
            case change_name:
                buff.writeString(newName);
                break;
            case can_buy_count:
                buff.writeInt(canBuyTime);
                buff.writeInt(reqGold);
                break;
            case sweep_result:
                buff.writeByte((byte) itemMap.size());
                for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case baby_follow:
                buff.writeByte(isFollow);//1:跟随，0:不跟随
                break;
            case feed_tips:
                buff.writeInt(tips);
                break;
            case SEND_FASHION_LIST: {
                Set<Integer> ownFashionIdSet = roleBaby.getOwnFashionIdSet();
                buff.writeInt(ownFashionIdSet.size());
                for (int fashionId : ownFashionIdSet) {
                    buff.writeInt(fashionId);
                }
            }
            break;
            case SEND_USING_FASHION_ID: {
                buff.writeInt(roleBaby.getUsingFashionId());
            }
            break;
            case SEND_ACTIVE_FASHION_SUCCESS: {
                buff.writeInt(fashionId);
            }break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return BabyPacketSet.C_BABY;
    }

    public void setBabyVo(BabyVo babyVo) {
        this.babyVo = babyVo;
    }

    public void setRoleBaby(RoleBaby roleBaby) {
        this.roleBaby = roleBaby;
    }

    public void setBabySweepVos(List<BabySweepVo> babySweepVos) {
        this.babySweepVos = babySweepVos;
    }

    public void setSubType(byte subType) {
        this.subType = subType;
    }

    public void setReqItemId(int reqItemId) {
        this.reqItemId = reqItemId;
    }

    public void setReqItemCount(int reqItemCount) {
        this.reqItemCount = reqItemCount;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void setCanBuyTime(int canBuyTime) {
        this.canBuyTime = canBuyTime;
    }

    public void setReqGold(int reqGold) {
        this.reqGold = reqGold;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public void setIsFollow(byte isFollow) {
        this.isFollow = isFollow;
    }

    public void setMaxBabyLevel(int maxBabyLevel) {
        this.maxBabyLevel = maxBabyLevel;
    }

    public void setBabyStage(int babyStage) {
        this.babyStage = babyStage;
    }

    public void setBabyLevel(int babyLevel) {
        this.babyLevel = babyLevel;
    }

    public void setTips(int tips) {
        this.tips = tips;
    }

    public void setSweepCount(Map<Integer, Byte> sweepCount) {
        this.sweepCount = sweepCount;
    }

    public int getFashionId() {
        return fashionId;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }
}
