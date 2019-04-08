package com.stars.modules.familyactivities.war.packet.ui.auxiliary;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class PktAuxFamilyWarFamilyInfo {
    public long familyId;
    public String familyName;
    public int serverId;
    public String serverName;
    public int seq;
    public int groupId;

    public PktAuxFamilyWarFamilyInfo(long familyId, String familyName, int serverId) {
        this.familyId = familyId;
        this.familyName = familyName;
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public long getFamilyId() {
        return familyId;
    }

    public String getFamilyName() {
        return familyName;
    }

    public int getServerId() {
        return serverId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    @Override
    public String toString() {
        return "familyId:" + this.familyId + " familyName:" + familyName + " serverId:" + serverId + " serverName:" + serverName + " seq:" + seq + " groupId:" + groupId;
    }
}
