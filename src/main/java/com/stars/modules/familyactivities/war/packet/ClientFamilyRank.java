package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.FamilyRankInfo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;

/**
 * Created by chenkeyu on 2017-06-29.
 */
public class ClientFamilyRank extends PlayerPacket {
    public static final byte rank = 0x00;
    public static final byte qua = 0x01;

    private List<FamilyRankInfo> infoList;
    private String descText;
    private byte havQualify;//1,有资格，0,无资格

    public byte subType;

    public ClientFamilyRank() {
    }

    public ClientFamilyRank(byte subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case rank:
                buff.writeString(descText);
                buff.writeByte((byte) (infoList != null ? infoList.size() : 0));
                if (infoList != null) {
                    for (FamilyRankInfo rankInfo : infoList) {
                        rankInfo.writeToBuff(buff);
                    }
                }
                break;
            case qua:
                buff.writeByte(havQualify);
                break;
        }

    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_RANK;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }

    public void setInfoList(List<FamilyRankInfo> infoList) {
        this.infoList = infoList;
    }

    public void setHavQualify(byte havQualify) {
        this.havQualify = havQualify;
    }
}
