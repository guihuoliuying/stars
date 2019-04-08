package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PkAuxQualifyFamilyWarPointsObj;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.List;

/**
 * Created by chenkeyu on 2017-05-27.
 */
public class ClientFamilyWarPointsRank extends PlayerPacket {
    private List<PkAuxQualifyFamilyWarPointsObj> pointsObjs;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte((byte) pointsObjs.size());
        LogUtil.info("familywar|跨服海选家族排名，本组家族个数:{}", pointsObjs.size());
        for (PkAuxQualifyFamilyWarPointsObj pointsObj : pointsObjs) {
            pointsObj.writeToBuff(buff);
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_POINTS_RANK;
    }

    public void setPointsObjs(List<PkAuxQualifyFamilyWarPointsObj> pointsObjs) {
        this.pointsObjs = pointsObjs;
    }
}
