package com.stars.modules.offlinepvp.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by liuyuheng on 2016/10/11.
 */
public class RoleOfflinePvp extends DbRow {
    private long roleId;
    private int matchVoId;// 匹配voId
    private int standardLevel;// 基准等级(确定奖励的等级号)
    private int standardFightScore;// 基准战力
    private byte refreshedNum;// 已刷新对手次数
    private int challegedNum;// 已挑战次数---notice：含义改为剩余挑战次数
    private String winIndex;// 已战胜对手序号
    private String rewardedIndex;// 已领取次数奖励
    private byte autoRefreshNum;// 已自动刷新次数
    private byte buyRefreshNum;// 已购买刷新次数
    private byte buyChallengeNum;// 已购买挑战次数

    /* 内存数据 */
    private Set<Byte> winIndexSet = new HashSet<>();// 已战胜对手序号
    private Set<Byte> rewardedIndexSet = new HashSet<>();// 已领取次数奖励

    public RoleOfflinePvp() {
    }

    public RoleOfflinePvp(long roleId) {
        this.roleId = roleId;
        this.matchVoId = 0;
        this.standardLevel = 0;
        this.standardFightScore = 0;
        this.refreshedNum = 0;
        this.challegedNum = 0;
        this.winIndex = "";
        this.rewardedIndex = "";
        this.autoRefreshNum = 0;
        this.buyRefreshNum = 0;
        this.buyChallengeNum = 0;
    }

    public byte getWinEnemyNum() {
        return (byte) winIndexSet.size();
    }

    public void addWinIndex(byte addIndex) {
        winIndexSet.add(addIndex);
        winIndex = setToString(winIndexSet);
    }

    public void clearWinIndex() {
        winIndexSet.clear();
        winIndex = setToString(winIndexSet);
    }

    public void addRewardIndex(byte addIndex) {
        rewardedIndexSet.add(addIndex);
        rewardedIndex = setToString(rewardedIndexSet);
    }

    public void clearRewardIndex() {
        rewardedIndexSet.clear();
        rewardedIndex = setToString(rewardedIndexSet);
    }

    private String setToString(Set<Byte> set) {
        StringBuilder builder = new StringBuilder("");
        for (byte index : set) {
            builder.append(index).append(",");
        }
        if (builder.lastIndexOf(",") != -1)
            builder.deleteCharAt(builder.lastIndexOf(","));
        return builder.toString();
    }

    public Set<Byte> getWinIndexSet() {
        return winIndexSet;
    }

    public Set<Byte> getRewardedIndexSet() {
        return rewardedIndexSet;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleofflinepvp", " roleid=" + this.getRoleId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleofflinepvp", " roleid=" + this.getRoleId());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getMatchVoId() {
        return matchVoId;
    }

    public void setMatchVoId(int matchVoId) {
        this.matchVoId = matchVoId;
    }

    public int getStandardLevel() {
        return standardLevel;
    }

    public void setStandardLevel(int standardLevel) {
        this.standardLevel = standardLevel;
    }

    public int getStandardFightScore() {
        return standardFightScore;
    }

    public void setStandardFightScore(int standardFightScore) {
        this.standardFightScore = standardFightScore;
    }

    public byte getRefreshedNum() {
        return refreshedNum;
    }

    public void setRefreshedNum(byte refreshedNum) {
        this.refreshedNum = refreshedNum;
    }

    public int getChallegedNum() {
        return challegedNum;
    }

    public void setChallegedNum(int challegedNum) {
        this.challegedNum = challegedNum;
    }

    public String getWinIndex() {
        return winIndex;
    }

    public void setWinIndex(String winIndex) throws Exception {
        this.winIndex = winIndex;
        if (StringUtil.isEmpty(winIndex))
            return;
        List<Byte> list = StringUtil.toArrayList(winIndex, Byte.class, ',');
        winIndexSet = new HashSet<>();
        winIndexSet.addAll(list);
    }

    public byte getAutoRefreshNum() {
        return autoRefreshNum;
    }

    public void setAutoRefreshNum(byte autoRefreshNum) {
        this.autoRefreshNum = autoRefreshNum;
    }

    public String getRewardedIndex() {
        return rewardedIndex;
    }

    public void setRewardedIndex(String rewardedIndex) throws Exception {
        this.rewardedIndex = rewardedIndex;
        if (StringUtil.isEmpty(rewardedIndex))
            return;
        List<Byte> list = StringUtil.toArrayList(rewardedIndex, Byte.class, ',');
        rewardedIndexSet = new HashSet<>();
        rewardedIndexSet.addAll(list);
    }

    public byte getBuyRefreshNum() {
        return buyRefreshNum;
    }

    public void setBuyRefreshNum(byte buyRefreshNum) {
        this.buyRefreshNum = buyRefreshNum;
    }

    public byte getBuyChallengeNum() {
        return buyChallengeNum;
    }

    public void setBuyChallengeNum(byte buyChallengeNum) {
        this.buyChallengeNum = buyChallengeNum;
    }
}
