package com.stars.modules.daily.userdata;

/**
 * Created by zhanghaizhen on 2017/7/7.
 */
public class RoleTagFightDelta implements Comparable<RoleTagFightDelta>{

    private long roleId;
    private byte tagId;
    private int fightDelta;
    private int totalRemainCount;
    private byte containSuperAward; //是否有超级奖励
    @Override
    public int compareTo(RoleTagFightDelta object) {
        if(totalRemainCount == 0)
            return 1;
        if((object.containSuperAward - containSuperAward) != (byte)0) //有超级奖励，排名最高
            return object.containSuperAward - containSuperAward;
        return object.fightDelta - this.fightDelta;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public byte getTagId() {
        return tagId;
    }

    public void setTagId(byte tagId) {
        this.tagId = tagId;
    }

    public int getFightDelta() {
        return fightDelta;
    }

    public void setFightDelta(int fightDelta) {
        this.fightDelta = fightDelta;
    }

    public int getTotalRemainCount() {
        return totalRemainCount;
    }

    public void setTotalRemainCount(int totalRemainCount) {
        this.totalRemainCount = totalRemainCount;
    }

    public byte getContainSuperAward() {
        return containSuperAward;
    }

    public void setContainSuperAward(byte containSuperAward) {
        this.containSuperAward = containSuperAward;
    }
}
