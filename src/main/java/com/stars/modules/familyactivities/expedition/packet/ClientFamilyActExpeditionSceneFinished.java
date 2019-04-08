package com.stars.modules.familyactivities.expedition.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.expedition.FamilyActExpeditionPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/10/12.
 */
public class ClientFamilyActExpeditionSceneFinished extends PlayerPacket {

    private int curExpeId; // 当前远征的id
    private int curExpeStep; // 当前远征的小关
    private byte status; // 0失败/2胜利
    private byte star; // 星星数
    private int useTime; // 使用时间

    public ClientFamilyActExpeditionSceneFinished() {
    }

    public ClientFamilyActExpeditionSceneFinished(int curExpeId, int curExpeStep, byte status, byte star, int useTime) {
        this.curExpeId = curExpeId;
        this.curExpeStep = curExpeStep;
        this.status = status;
        this.star = star;
        this.useTime = useTime;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActExpeditionPacketSet.C_SCENE_FINISHED;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(curExpeId); // 当前远征的id
        buff.writeInt(curExpeStep); // 当前远征的小关
        buff.writeByte(status); // 0失败/2胜利
        buff.writeByte(star); // 星星数
        buff.writeInt(useTime); // 使用时间
    }

    public void setCurExpeId(int curExpeId) {
        this.curExpeId = curExpeId;
    }

    public void setCurExpeStep(int curExpeStep) {
        this.curExpeStep = curExpeStep;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public void setStar(byte star) {
        this.star = star;
    }

    public void setUseTime(int useTime) {
        this.useTime = useTime;
    }
}
