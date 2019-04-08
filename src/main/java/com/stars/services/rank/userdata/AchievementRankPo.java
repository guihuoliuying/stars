package com.stars.services.rank.userdata;

import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by zhanghaizhen on 2017/8/7.
 */
public class AchievementRankPo  extends RoleRankPo {

    private long roleId;
    private String name;
    private int fightScore;
    private int stage;
    private int achieveScore;


    public AchievementRankPo(){

    }

    @Override
    public long getUniqueId() {
        return roleId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {

    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleachieverank", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return String.format("delete from `roleachieverank` where `roleid`=" + roleId);
    }

    @Override
    public int compareTo(Object o) {
        AchievementRankPo po = (AchievementRankPo) o;
        if (roleId == po.getRoleId()) {
            return 0;
        }
        if(achieveScore != po.getAchieveScore()){
            return achieveScore < po.getAchieveScore() ? 1 : -1;
        }else if (stage != po.getStage()) {
            return stage < po.getStage() ? 1 : -1;
        }else if (fightScore != po.getFightScore()){
            return fightScore < po.getFightScore() ? 1 : -1;
        }else{
            return roleId < po.getRoleId() ? -1 : 1;// roleId小的在前面
        }

    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("AchievementRankPo克隆失败", e);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AchievementRankPo that = (AchievementRankPo) o;

        if (roleId != that.getRoleId()) return false;

        return true;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static AchievementRankPo build(AchievementRankPo po) {
        AchievementRankPo achievementRankPo = new AchievementRankPo();
        achievementRankPo.setRoleId(po.getRoleId());
        achievementRankPo.setName(po.getName());
        achievementRankPo.setStage(po.getStage());
        achievementRankPo.setFightScore(po.getFightScore());
        achievementRankPo.setAchieveScore(po.getAchieveScore());
        return achievementRankPo;
    }


    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getFightScore() {
        return fightScore;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    public int getAchieveScore() {
        return achieveScore;
    }

    public void setAchieveScore(int achieveScore) {
        this.achieveScore = achieveScore;
    }
}