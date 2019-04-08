package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarPointsObj;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by zhaowenshuo on 2016/12/19.
 */
public class ClientFamilyWarUiPointsRank extends PlayerPacket {

    public static final byte SUBTYPE_ELITE_FIGHT = 0;
    public static final byte SUBTYPE_NORMAL_FIGHT = 1;

    private byte subtype;
    private byte warType;
    private List<PktAuxFamilyWarPointsObj> top100List;
    private int myRank;
    private PktAuxFamilyWarPointsObj myRankObj;


    public ClientFamilyWarUiPointsRank() {
    }

    public ClientFamilyWarUiPointsRank(byte subtype, byte warType, List<PktAuxFamilyWarPointsObj> top100List) {
        this.subtype = subtype;
        this.warType = warType;
        this.top100List = top100List;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_POINTS_RANK;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype); // 0 - 精英，1 - 匹配
        buff.writeByte(warType); // 1 - 本服，2 - 海选，3 - 跨服
        buff.writeInt(top100List.size()); // 列表大小
        for (PktAuxFamilyWarPointsObj obj : top100List) {
            obj.writeToBuff(buff);
        }
        buff.writeInt(myRank);//小于等于0是没有排名
        if (myRankObj != null && myRank > 0) {
            myRankObj.writeToBuff(buff);
        }

    }

    public PktAuxFamilyWarPointsObj getMyRankObj() {
        return myRankObj;
    }

    public void setMyRankObj(PktAuxFamilyWarPointsObj myRankObj) {
        this.myRankObj = myRankObj;
    }

    public int getMyRank() {
        return myRank;
    }

    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }
}
