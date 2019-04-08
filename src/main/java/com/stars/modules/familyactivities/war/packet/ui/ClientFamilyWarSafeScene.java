package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by chenkeyu on 2017-04-11 18:50
 */
public class ClientFamilyWarSafeScene extends PlayerPacket {

    private byte type;
    private byte memberType;//成员类型2:精英成员，1:匹配成员,0无资格
    private long camp1FamilyPoints;//己方家族积分
    private long camp2FamilyPoints;//对方家族积分
    private long remainTime;//本轮剩余时间
    private String postionStr;//坐标
    private int safeId;

    public ClientFamilyWarSafeScene() {

    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(safeId);
        buff.writeString(postionStr);
        buff.writeByte(type);
        buff.writeByte(memberType);
        buff.writeLong(camp1FamilyPoints);
        buff.writeLong(camp2FamilyPoints);
        buff.writeLong(remainTime);
        LogUtil.info("familywar|进入安全区,id:{},postionStr:{},type:{},memberType:{},camp1FamilyPoints:{},camp2FamilyPoints:{},remainTime:{}",
                getRoleId(), postionStr, type, memberType, camp1FamilyPoints, camp2FamilyPoints, remainTime);
    }


    public void setType(byte type) {
        this.type = type;
    }

    public void setSafeId(int safeId) {
        this.safeId = safeId;
    }

    public void setPostionStr(String postionStr) {
        this.postionStr = postionStr;
    }

    public void setMemberType(byte memberType) {
        this.memberType = memberType;
    }

    public void setCamp1FamilyPoints(long camp1FamilyPoints) {
        this.camp1FamilyPoints = camp1FamilyPoints;
    }

    public void setCamp2FamilyPoints(long camp2FamilyPoints) {
        this.camp2FamilyPoints = camp2FamilyPoints;
    }

    public void setRemainTime(long remainTime) {
        this.remainTime = remainTime;
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_ENTER_SAFE_SCENE;
    }
}
