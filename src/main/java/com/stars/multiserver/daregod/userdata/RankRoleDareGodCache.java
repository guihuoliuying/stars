package com.stars.multiserver.daregod.userdata;

import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by chenkeyu on 2017-08-24.
 */
public class RankRoleDareGodCache implements Comparable<RankRoleDareGodCache> {
    private long roleId;
    private int serverId;
    private String roleName;
    private int rank;
    private long damage;//当天总伤害
    private int fightScore;//战力
    private int fightType;
    private int fashionId;
    private int rankAward;
    private int jobId;

    public void writeToBuffer(NewByteBuffer buffer) {
        buffer.writeLong(roleId);
        buffer.writeString(MultiServerHelper.getServerName(serverId));
        buffer.writeString(roleName);
        buffer.writeInt(rank);
        buffer.writeLong(damage);//当天总伤害
        buffer.writeInt(fightScore);
        buffer.writeInt(fightType);//当前所处战力段,每次重置都是0，第一次打开界面时计算
        buffer.writeInt(fashionId);//时装
        buffer.writeInt(rankAward);
        buffer.writeInt(jobId);
    }

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(rank);
        buffer.writeString(roleName);
        buffer.writeLong(damage);
    }

    public RankRoleDareGodCache(long roleId) {
        this.roleId = roleId;
    }

    public RankRoleDareGodCache(long roleId, int serverId, String roleName, long damage, int fightScore, int fightType, int fashionId, int jobId) {
        this.roleId = roleId;
        this.serverId = serverId;
        this.roleName = roleName;
        this.damage = damage;
        this.fightScore = fightScore;
        this.fightType = fightType;
        this.fashionId = fashionId;
        this.jobId = jobId;
    }

    public int getRank() {
        return rank;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getServerId() {
        return serverId;
    }

    public String getRoleName() {
        return roleName;
    }

    public long getDamage() {
        return damage;
    }

    public int getFightScore() {
        return fightScore;
    }

    public int getFightType() {
        return fightType;
    }

    public int getFashionId() {
        return fashionId;
    }

    public int getRankAward() {
        return rankAward;
    }

    public int getJobId() {
        return jobId;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setRankAward(int rankAward) {
        this.rankAward = rankAward;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setDamage(long damage) {
        this.damage = damage;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public void setFashionId(int fashionId) {
        this.fashionId = fashionId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = result * 31 + Long.valueOf(roleId).hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        RankRoleDareGodCache cache = (RankRoleDareGodCache) obj;
        if (cache == this) return true;
        return this.roleId == cache.getRoleId();
    }

    @Override
    public int compareTo(RankRoleDareGodCache o) {
        if (roleId == o.roleId) return 0;
        if (damage < o.damage) return 1;
        if (damage > o.damage) return -1;
        return (int) (roleId - o.roleId);
    }

    @Override
    public String toString() {
        return "RankRoleDareGodCache{" +
                "roleId=" + roleId +
                ", serverId=" + serverId +
                ", roleName='" + roleName + '\'' +
                ", rank=" + rank +
                ", damage=" + damage +
                ", fightScore=" + fightScore +
                ", fightType=" + fightType +
                ", fashionId=" + fashionId +
                ", rankAward=" + rankAward +
                ", jobId=" + jobId +
                '}';
    }
}
