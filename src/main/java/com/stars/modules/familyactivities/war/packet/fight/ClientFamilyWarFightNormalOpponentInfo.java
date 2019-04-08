package com.stars.modules.familyactivities.war.packet.fight;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarOpponent;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/12/20.
 */
public class ClientFamilyWarFightNormalOpponentInfo extends PlayerPacket {

    private String camp1FamilyName;
    private String camp2FamilyName;
    private String camp1ServerName;
    private String camp2ServerName;
    private List<PktAuxFamilyWarOpponent> opponentList = new ArrayList<>();

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_FIGHT_NORMAL_OPPONENT_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(camp1FamilyName); // camp 1 family name
        buff.writeString(camp2FamilyName); // camp 2 family name
        buff.writeString(camp1ServerName);
        buff.writeString(camp2ServerName);
        buff.writeInt(opponentList.size()); // size of opponent's list
        for (PktAuxFamilyWarOpponent opponent : opponentList) {
            buff.writeString(Long.toString(opponent.getRoleId())); // roleId
            buff.writeString(opponent.getRoleName()); // role name
            buff.writeInt(opponent.getFightScore()); // fight score
            buff.writeInt(opponent.getModelId()); // model id
            buff.writeByte(opponent.getCamp()); // camp
        }
    }

    public void setCamp1ServerName(String camp1ServerName) {
        this.camp1ServerName = camp1ServerName;
    }

    public void setCamp2ServerName(String camp2ServerName) {
        this.camp2ServerName = camp2ServerName;
    }

    public void setCamp1FamilyName(String camp1FamilyName) {
        this.camp1FamilyName = camp1FamilyName;
    }

    public void setCamp2FamilyName(String camp2FamilyName) {
        this.camp2FamilyName = camp2FamilyName;
    }

    public void addOpponentInfo(long roleId, String roleName, int fightScore, int jobId, byte camp) {
        opponentList.add(new PktAuxFamilyWarOpponent(roleId, roleName, fightScore, jobId, camp));
    }
}
