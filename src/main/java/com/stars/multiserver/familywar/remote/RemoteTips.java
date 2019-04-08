package com.stars.multiserver.familywar.remote;

/**
 * Created by chenkeyu on 2017-06-29.
 */
public class RemoteTips {
    private long roleId;
    private boolean tips;
    private int battleType;

    public RemoteTips(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public boolean isTips() {
        return tips;
    }

    public void setTips(boolean tips) {
        this.tips = tips;
    }

    public int getBattleType() {
        return battleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }
}
