package com.stars.modules.achievement.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.achievement.AchievementManager;
import com.stars.modules.achievement.prodata.AchievementStageVo;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/8/7.
 */
public class AchievementStagePo extends DbRow {
    private long roleId;
    private String stageScoreStr;
    private String commonAwardStr;
    private String perfectAwardStr;

    //内存
    private Map<Integer,Integer> stageScoreMap = new HashMap<>();
    private Map<Integer,Integer> commonAwardMap = new HashMap<>();
    private Map<Integer,Integer> perfectAwardMap = new HashMap<>();


    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getStageScoreStr() {
        if(StringUtil.isEmpty(stageScoreMap))
            return "";

        return StringUtil.makeString(stageScoreMap,'=','|');
    }

    public void setStageScoreStr(String stageScoreStr) {
        this.stageScoreStr = stageScoreStr;
        if(StringUtil.isEmpty(stageScoreStr))
            return;
        this.stageScoreMap = StringUtil.toMap(stageScoreStr, Integer.class, Integer.class, '=', '|');
    }

    public String getCommonAwardStr() {
        if(StringUtil.isEmpty(commonAwardMap))
            return "";
        return StringUtil.makeString(commonAwardMap,'=','|');
    }

    public void setCommonAwardStr(String commonAwardstr) {
        this.commonAwardStr = commonAwardstr;
        if(StringUtil.isEmpty(commonAwardstr))
            return;
        this.commonAwardMap = StringUtil.toMap(commonAwardstr,Integer.class,Integer.class,'=','|');
    }

    public String getPerfectAwardStr() {
        if(StringUtil.isEmpty(perfectAwardMap))
            return "";
        return StringUtil.makeString(perfectAwardMap,'=','|');
    }

    public void setPerfectAwardStr(String perfectAwardStr) {
        this.perfectAwardStr = perfectAwardStr;
        if(StringUtil.isEmpty(perfectAwardStr))
            return;
        this.perfectAwardMap = StringUtil.toMap(perfectAwardStr,Integer.class,Integer.class,'=','|');
    }

    public Map<Integer, Integer> getCommonAwardMap() {
        return commonAwardMap;
    }

    public void setCommonAwardMap(Map<Integer, Integer> commonAwardMap) {
        this.commonAwardMap = commonAwardMap;
    }

    public Map<Integer, Integer> getPerfectAwardMap() {
        if(perfectAwardMap == null)
            perfectAwardMap = new HashMap<>();
        return perfectAwardMap;
    }

    public void setPerfectAwardMap(Map<Integer, Integer> perfectAwardMap) {
        this.perfectAwardMap = perfectAwardMap;
    }

    public Map<Integer, Integer> getStageScoreMap() {
        if(stageScoreMap == null)
            stageScoreMap = new HashMap<>();
        return stageScoreMap;
    }

    public void setStageScoreMap(Map<Integer, Integer> stageScoreMap) {
        this.stageScoreMap = stageScoreMap;
    }



    public int getTotalStageScore(){ //获得总的成就积分
        if(StringUtil.isEmpty(stageScoreMap))
            return 0;
        int totalScore = 0;
        for(int score : stageScoreMap.values()){
            totalScore += score;
        }
        return totalScore;
    }

    public int getMaxActiveStage(){ //获得玩家最高激活的成就阶段
        if(StringUtil.isEmpty(commonAwardMap)) //如果还没领取过奖励，默认激活第一阶
            return 1;
        int maxStage = 1;
        for(int stage:commonAwardMap.keySet()){ //遍历领取过最高阶段奖励
            if(stage > maxStage)
                maxStage = stage;
        }
        int nextStage = maxStage + 1; //领取过的最高阶段的下一阶为玩家当前激活的最高阶数
        AchievementStageVo achievementStageVo = AchievementManager.getAchievementStageVoByStage(nextStage);
        return achievementStageVo == null ? maxStage : nextStage;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleachievementstage", "`roleid` = " + roleId );
    }

    @Override
    public String getDeleteSql() {
        return null;
    }
}
