package com.stars.modules.familyactivities.treasure.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.treasure.FamilyTreasurePacket;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-02-23 18:25
 */
public class ClientFamilyTreasure extends PlayerPacket {
    public static final byte normal = 0x00;
    public static final byte sunday = 0x01;

    private int level;//家族探宝阶级
    private int step;//家族探宝步数
    private long totalDamage;//一周内boss被打得伤害值
    private int rank;       //家族排名
    private long hp;     //boss被扣去的生命值
    private int count;      //每日剩余挑战次数
    private byte type;      //类型

    @Override
    public void execPacket(Player player) {

    }

    public ClientFamilyTreasure() {}

    public ClientFamilyTreasure(byte type) {
        this.type = type;
    }

    @Override
    public short getType() {
        return FamilyTreasurePacket.C_TREASURE;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(type);
        switch (type) {
            case normal:
                buff.writeInt(level);
                buff.writeByte((byte) step);
                buff.writeLong(hp);
                buff.writeInt(count);
                break;
            case sunday:
                buff.writeLong(totalDamage);
                buff.writeInt(rank);
                buff.writeInt(count);
                break;
            default:
                break;
        }
        LogUtil.info("请求类型:{}--阶级:{}--步数:{}--血量:{}--次数:{}--总伤害:{}--排名:{}", type, level, step, hp, count, totalDamage, rank);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setTotalDamage(long totalDamage) {
        this.totalDamage = totalDamage;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
