package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

/**
 * Created by chenkeyu on 2017-05-27.
 */
public class PktAuxQualifyFamilyWarOpponent {
    private int battleType;
    private long familyId;
    private String serverName;
    private String familyName;
    private long time;
    private byte winOrLose; //1:赢 0:未战 -1:输

    public PktAuxQualifyFamilyWarOpponent(int battleType, long familyId, String serverName, String familyName, long time, byte winOrLose) {
        this.battleType = battleType;
        this.familyId = familyId;
        this.serverName = serverName;
        this.familyName = familyName;
        this.time = time;
        this.winOrLose = winOrLose;
    }

    public String getServerName() {
        return serverName;
    }

    public int getBattleType() {
        return battleType;
    }

    public long getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public long getTime() {
        return time;
    }

    public byte getWinOrLose() {
        return winOrLose;
    }
}
