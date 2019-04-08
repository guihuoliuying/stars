package com.stars.services.mooncake;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-09-21.
 */
public class RoleMoonCakeCache implements Comparable<RoleMoonCakeCache> {
    private long roleId;
    private int weeklyMaxScore;
    private String roleName;
    private int fightScore;

    private int rank;
    private Map<Integer, Integer> awardMap = new HashMap<>();

    public RoleMoonCakeCache(long roleId) {
        this.roleId = roleId;
    }

    public RoleMoonCakeCache(long roleId, int weeklyMaxScore/*, String roleName, int fightScore*/) {
        this.roleId = roleId;
        this.weeklyMaxScore = weeklyMaxScore;
//        this.roleName = roleName;
//        this.fightScore = fightScore;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId));
        buff.writeInt(rank);
        buff.writeInt(weeklyMaxScore);
        buff.writeString(roleName);
        buff.writeInt(fightScore);
        buff.writeByte((byte) awardMap.size());
        for (Map.Entry<Integer, Integer> entry : awardMap.entrySet()) {
            buff.writeInt(entry.getKey());
            buff.writeInt(entry.getValue());
        }
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getWeeklyMaxScore() {
        return weeklyMaxScore;
    }

    public void setWeeklyMaxScore(int weeklyMaxScore) {
        this.weeklyMaxScore = weeklyMaxScore;
    }


    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Map<Integer, Integer> getAwardMap() {
        return awardMap;
    }

    public void setAwardMap(Map<Integer, Integer> awardMap) {
        this.awardMap = awardMap;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + Long.valueOf(roleId).hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        RoleMoonCakeCache roleMoonCakeCache = (RoleMoonCakeCache) obj;
        if (this.roleId == roleMoonCakeCache.roleId) return true;
        return false;
    }

    @Override
    public int compareTo(RoleMoonCakeCache o) {
        if (this.roleId == o.roleId) return 0;
        return o.weeklyMaxScore - this.weeklyMaxScore;
    }

    @Override
    public String toString() {
        return "RoleMoonCakeCache{" +
                "roleId=" + roleId +
                ", weeklyMaxScore=" + weeklyMaxScore +
                ", roleName='" + roleName + '\'' +
                ", fightScore=" + fightScore +
                ", rank=" + rank +
                ", awardMap=" + awardMap +
                '}';
    }
}
