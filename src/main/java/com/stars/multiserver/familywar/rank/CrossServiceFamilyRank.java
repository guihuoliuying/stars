package com.stars.multiserver.familywar.rank;

import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-06-28.
 */
public class CrossServiceFamilyRank implements Comparable<CrossServiceFamilyRank> {
    private String familyId;
    private String familyName;
    private int serverId;
    private String masterName;
    private String type;
    private long fightScore;

    public String getUniqueId() {
        return familyId;
    }

    public CrossServiceFamilyRank(String familyId, long fightScore) {
        this.familyId = familyId;
        this.fightScore = fightScore;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(familyId);
        buff.writeString(MultiServerHelper.getServerName(serverId));
        buff.writeString(familyName);
        buff.writeString(masterName);
        buff.writeString(Long.toString(fightScore));
        buff.writeString(type);
    }

    @Override
    public int compareTo(CrossServiceFamilyRank other) {
        if (this.fightScore > other.fightScore) {
            return -1;
        } else if (this.fightScore < other.fightScore) {
            return 1;
        } else {
            return this.serverId - ((CrossServiceFamilyRank) other).serverId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrossServiceFamilyRank that = (CrossServiceFamilyRank) o;
        return this.familyId.equals(that.familyId);
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public long getFightScore() {
        return fightScore;
    }

    public void setFightScore(long fightScore) {
        this.fightScore = fightScore;
    }
}
