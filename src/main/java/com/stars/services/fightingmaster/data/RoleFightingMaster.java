package com.stars.services.fightingmaster.data;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.multiserver.MultiServerHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhouyaohui on 2016/11/17.
 */
public class RoleFightingMaster extends DbRow{

    private long roleId;
    private int fightScore; // 战力分数
    private int disScore;   // 表现积分
    private int fightCount;   // 战斗次数
    private int ymdH;        // 最后一次战斗日期
    private short fightTimes;   // 每天战斗次数
    private int matchRobotPersent;  // 机器人的匹配概率
    private int seqWinOrFailed; // 连赢或者连输 1xxx 表示连赢多少场 2xxx表示连输多少场
    private int rank;       // 排名
    private String name;    // 名字
    private int serverId;   // 服务id
    private int level;      // 等级
    private byte fiveAward; // 五战奖励 0 未领取 1已领取
    private int lastRankAwardYMD;   // 最后一次排行榜奖励日期，例如：20161221。
    private String rankUP;  // 已经获得过的跨榜奖励
    private String serverName;  // 服务器名字
    private Set<Integer> rankSet = new HashSet<>();
    private int validMedalId; //有效勋章id(装备id)

    public boolean alreadyRankUp(int rankId) {
        if (rankSet.contains(rankId)) {
            return true;
        } else {
            rankSet.add(rankId);
            return false;
        }
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRankUp() {
        StringBuilder builder = new StringBuilder();
        for (Integer rankId : rankSet) {
            builder.append(rankId).append("+");
        }
        if (builder.length() != 0) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    public void setRankUp(String rankUp) {
        if (rankUp.equals("")) {
            return;
        }
        String[] rankIds = rankUp.split("[+]");
        for (String s : rankIds) {
            rankSet.add(Integer.valueOf(s));
        }
    }

    public int getLastRankAwardYMD() {
        return lastRankAwardYMD;
    }

    public void setLastRankAwardYMD(int lastRankAwardYMD) {
        this.lastRankAwardYMD = lastRankAwardYMD;
    }

    public byte getFiveAward() {
        return fiveAward;
    }

    public void setFiveAward(byte fiveAward) {
        this.fiveAward = fiveAward;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
        this.serverName = MultiServerHelper.getServerName(serverId);
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

    public int getSeqWinOrFailed() {
        return seqWinOrFailed;
    }

    public void setSeqWinOrFailed(int seqWinOrFailed) {
        this.seqWinOrFailed = seqWinOrFailed;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDisScore() {
        return disScore;
    }

    public void setDisScore(int disScore) {
        if (disScore < 1) {
            // 最低不能低过1分
            this.disScore = 1;
        } else {
            this.disScore = disScore;
        }
    }

    public int getFightCount() {
        return fightCount;
    }

    public void setFightCount(int fightCount) {
        this.fightCount = fightCount;
    }

    public int getYmdH() {
        return ymdH;
    }

    public void setYmdH(int ymdH) {
        this.ymdH = ymdH;
    }

    public short getFightTimes() {
        return fightTimes;
    }

    public void setFightTimes(short fightTimes) {
        this.fightTimes = fightTimes;
    }

    public int getMatchRobotPersent() {
        return matchRobotPersent;
    }

    public void setMatchRobotPersent(int matchRobotPersent) {
        this.matchRobotPersent = matchRobotPersent;
    }

    public int getValidMedalId() {
        return validMedalId;
    }

    public void setValidMedalId(int validMedalId) {
        this.validMedalId = validMedalId;
    }
    
    public boolean isSequenceWin() {
        if (seqWinOrFailed / 1000 == 1) {
            return true;
        } else {
            return false;
        }
    }

    public int sequenceCount() {
        return seqWinOrFailed % 1000;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolefightingmaster", "roleid = " + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from rolefightingmaster where roleid = " + roleId;
    }
    
    
}
