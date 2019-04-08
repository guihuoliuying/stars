package com.stars.services.callboss.cache;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.callboss.CallBossConstant;

public class RoleDamageCache implements Comparable<RoleDamageCache> {
    private long roleId;// 角色Id
    private String roleName;// 角色名字
    private int damage;// 伤害值
    private int rank;// 排名

    public RoleDamageCache(long roleId, String roleName, int damage) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.damage = damage;
        this.rank = CallBossConstant.RANK_INIT;
    }

    @Override
    public int compareTo(RoleDamageCache compare) {
        if (damage != compare.getDamage()) {
            return damage < compare.getDamage() ? 1 : -1;
        } else {
            return roleId == compare.getRoleId() ? 0 : roleId < compare.getRoleId() ? -1 : 1;
        }
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeInt(rank);
        buff.writeString(String.valueOf(roleId));
        buff.writeString(roleName);
        buff.writeInt(damage);
    }

    public String getRoleName() {
        return roleName;
    }

    public long getRoleId() {
        return roleId;
    }

    public int getDamage() {
        return damage;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
