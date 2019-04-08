package com.stars.modules.teampvpgame.userdata;

import com.stars.modules.teampvpgame.TeamPVPGameManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 报名提交者
 * Created by liuyuheng on 2016/12/16.
 */
public class SignUpSubmiter {
    private long roleId;// roleId
    private String roleName;// 名称
    private int jobId;// 职业
    private int level;// 等级
    private int fightScore;// 战力
    private long submitTimestamp;// 提交时间

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeString(String.valueOf(roleId));
        buff.writeString(roleName);
        buff.writeInt(jobId);
        buff.writeInt(level);
        buff.writeInt(fightScore);
        buff.writeInt((int) (TeamPVPGameManager.signUpConfirmTime -
                Math.floor((System.currentTimeMillis() - submitTimestamp) / 1000.0)));// 剩余时间(秒)
    }

    public SignUpSubmiter(long roleId) {
        this.roleId = roleId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public long getSubmitTimestamp() {
        return submitTimestamp;
    }

    public void setSubmitTimestamp(long submitTimestamp) {
        this.submitTimestamp = submitTimestamp;
    }
}
