package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

public class ClientFamilyWarMainIcon extends PlayerPacket {
    /* 活动状态 */
    private int state;//图标状态
    private long countdown; //剩余时间
    private byte qualification;

    public ClientFamilyWarMainIcon() {
    }

    public ClientFamilyWarMainIcon(int state, long countdown) {
        this.state = state;
        this.countdown = countdown;
    }

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(state);
        buff.writeInt((int) countdown);
        buff.writeByte(qualification);
        LogUtil.info("familywar|主界面Icon|roleid:{},state:{},countdown:{},qualification:{}",getRoleId(), state, countdown, qualification);
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_MAIN_ICON;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getCountdown() {
        return countdown;
    }

    public void setCountdown(long countdown) {
        this.countdown = countdown;
    }

    public byte getQualification() {
        return qualification;
    }

    public void setQualification(byte qualification) {
        this.qualification = qualification;
    }

}
