package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.flow.FamilyWarFlowInfo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

public class ClientFamilyWarUiFlowInfo extends PlayerPacket {

    private byte familyState = FamilyWarConst.waitQulification;

    @Override
    public void execPacket(Player player) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        FamilyWarFlowInfo flowInfo = FamilyWarConst.FAMILY_WAR_FLOW_INFO_MAP.get(FamilyWarConst.battleType);
        buff.writeInt(flowInfo.getWarType());//类型，1=本服赛，2=海选，3=跨服决赛
        buff.writeByte(flowInfo.getState());//状态，1=未开始，2=取资格前，3=取资格后，4=已完成
        buff.writeByte(familyState);//是否具有跨服海选赛的资格，1：有   0：没有  -1：没取资格
        buff.writeInt(FamilyWarConst.remoteType);
        LogUtil.info("familywar|battleType:{},warType:{},state:{},familyState:{},quliState:{}", FamilyWarConst.battleType, flowInfo.getWarType(), flowInfo.getState(), familyState, FamilyWarConst.remoteType);
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_MAIN;
    }

    public void setFamilyState(byte familyState) {
        this.familyState = familyState;
    }
}
