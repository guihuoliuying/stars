package com.stars.modules.familyactivities.war.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;

/**
 * Created by chenkeyu on 2017-07-03.
 */
public class ServerFamilyRank extends PlayerPacket {
    @Override
    public void execPacket(Player player) {
        int serverOpenday = DataManager.getServerDays();
        int maxDay = FamilyActWarManager.familywar_cycletime_max;
        ClientFamilyRank clientFamilyRank = new ClientFamilyRank(ClientFamilyRank.qua);
        if (serverOpenday > maxDay) {
            clientFamilyRank.setHavQualify((byte) 1);
        } else {
            clientFamilyRank.setHavQualify((byte) 0);
        }
        send(clientFamilyRank);
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.S_FAMILY_RANK;
    }
}
