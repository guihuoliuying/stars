package com.stars.modules.marry.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.marry.MarryPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-07-05.
 */
public class ClientMarryBattleInfo extends PlayerPacket {
    private long roleId;
    private byte type;// 0=真实玩家;1=构造玩家数据,默认是0
    private byte jobId;
    private String name;
    private short level;
    private int fightScore;
    private int deityWeapon;

    private int roleState;//0:离线中  1:战斗中  2:安全区，且副本有次数  3:安全区，但无副本次数

    public static final int OFFLINE = 0;
    public static final int FIGHTING = 1;
    public static final int SAFE_HAVECOUNT = 2;
    public static final int SAFE_NONECOUNT = 3;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId));
        buff.writeByte(type);
        buff.writeByte(jobId);
        buff.writeString(name);
        buff.writeShort(level);
        buff.writeInt(fightScore);
        buff.writeInt(deityWeapon);
        buff.writeInt(roleState);
        LogUtil.info("结婚组队，对象数据|roleId:{},type:{},jobId:{},name:{},level:{},fightScore:{},deityWeapon:{},roleState:{}",
                roleId, type, jobId, name, level, fightScore, deityWeapon, roleState);
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MarryPacketSet.C_MARRY_BATTLE_INFO;
    }

    @Override
    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setJobId(byte jobId) {
        this.jobId = jobId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public void setDeityWeapon(int deityWeapon) {
        this.deityWeapon = deityWeapon;
    }

    public void setRoleState(int roleState) {
        this.roleState = roleState;
    }
}
