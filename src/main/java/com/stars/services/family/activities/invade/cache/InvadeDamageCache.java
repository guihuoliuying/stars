package com.stars.services.family.activities.invade.cache;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/10/19.
 */
public class InvadeDamageCache implements Comparable<InvadeDamageCache> {
    private long roleId;// 角色Id
    private String roleName;// 角色名字
    private int damage;// 伤害值
    private int rank;// 名次

    public InvadeDamageCache(long roleId, String roleName, int damage) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.damage = damage;
    }

    @Override
    public int compareTo(InvadeDamageCache o) {
        if (damage != o.getDamage()) {
            return damage < o.getDamage() ? 1 : -1;
        } else {
            return roleId == o.getRoleId() ? 0 : roleId < o.getRoleId() ? -1 : 1;// roleId小的在前面
        }
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(String.valueOf(roleId));
        buff.writeString(roleName);
        buff.writeInt(damage);
        buff.writeInt(rank);
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
