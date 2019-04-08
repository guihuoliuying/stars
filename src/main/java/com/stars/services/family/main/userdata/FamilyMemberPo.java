package com.stars.services.family.main.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyMemberPo extends DbRow implements Cloneable {

    /* 数据库数据 */
    private long familyId; // 家族id
    private long roleId; // 成员id
    private int jobId; // 职业id
    private byte postId; // 职位id
    private String roleName; // 成员名字
    private int roleLevel; // 成员等级
    private int roleFightScore; // 成员战力
    private long historicalContribution; // 历史贡献
    private String weekContribution; // 周贡献
    private int rmbDonation; // 元宝捐献
    private int joinTimestamp; // 加入时间戳
    private int offlineTimestamp; // 离线时间戳
    /* 内存数据 */
    private Map<Integer, Integer> contributionMap = new HashMap<>(); // version(date) -> contribution
    private long accumulatedWeekContribution;
    private boolean isOnline;

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familymember", "`familyid`=" + familyId + " and `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familymember` where `familyid`=" + familyId + " and `roleid`=" + roleId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FamilyMemberPo other = (FamilyMemberPo) super.clone();
        other.contributionMap = new HashMap<>(contributionMap);
        return other;
    }

    public FamilyMemberPo copy() {
        FamilyMemberPo other = new FamilyMemberPo();
        other.familyId = this.familyId;
        other.roleId = this.roleId;
        other.jobId = this.jobId;
        other.postId = this.postId;
        other.roleName = this.roleName;
        other.roleLevel = this.roleLevel;
        other.roleFightScore = this.roleFightScore;
        other.historicalContribution = this.historicalContribution;
        other.weekContribution = this.weekContribution;
        other.rmbDonation = this.rmbDonation;
        other.joinTimestamp = this.joinTimestamp;
        other.offlineTimestamp = this.offlineTimestamp;
        other.accumulatedWeekContribution = this.accumulatedWeekContribution;
        other.isOnline = this.isOnline;
        other.contributionMap = new HashMap<>(contributionMap);
        return other;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId)); // 成员ID
        buff.writeString(roleName); // 成员名字
        buff.writeByte(postId); // 职位ID
        buff.writeInt(jobId); // 职业ID
        buff.writeInt(roleLevel); // 成员等级
        buff.writeInt(roleFightScore); // 成员战力
        buff.writeInt(rmbDonation); // 元宝捐献
        buff.writeInt(0); // 每日贡献
        buff.writeString(Long.toString(accumulatedWeekContribution)); // 周贡献
        buff.writeString(Long.toString(historicalContribution)); // 总贡献
        buff.writeByte((byte) (isOnline ? 1 : 0)); // 是否在线，1-在线，0-离线
        buff.writeInt(offlineTimestamp); // 离线时间，单位：秒
    }

    public void expireAndRecalcWeekContribution(long now) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
        calendar.setTimeInMillis(now);
        Set<String> set = new HashSet<>();
        /* 生成每周周几 */
        // fixme: 还需要每日重置修改
        int days = calendar.get(Calendar.DAY_OF_WEEK);
        days = days - 1 < 1 ? 7 : days - 1;
        for (int i = 0; i < days; i++) {
            try {
                set.add(sdf.format(calendar.getTime()));
            } catch (Exception e) {
                LogUtil.error("", e);
            }
            calendar.add(Calendar.DATE, -1);
        }
        /* 移除过期数据 */
        Iterator<Map.Entry<Integer, Integer>> iterator = contributionMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            calendar.setTimeInMillis(entry.getKey() * 1000L);
            String timeStr = sdf.format(calendar.getTime());
            if (!set.contains(timeStr)) {
                iterator.remove();
            }
        }
        /* 计算周帮贡 */
        recalcWeekContribution();
    }

    public void recalcWeekContribution() {
        long tmpContribution = 0L;
        for (Integer contribution : contributionMap.values()) {
            tmpContribution += contribution;
        }
        accumulatedWeekContribution = tmpContribution;
    }

    public void addContribution(int version, int contribution) {
        if (contribution > 0) {
            Integer oldValue = contributionMap.get(version);
            contributionMap.put(version, oldValue == null ? contribution : oldValue + contribution);
            historicalContribution += contribution;
            recalcWeekContribution();
        }
    }

    /* Mem Data Getter/Setter */
    public Map<Integer, Integer> getContributionMap() {
        return contributionMap;
    }

    public void setContributionMap(String weekContribution) {
        this.contributionMap = new HashMap<>();
        try {
            Map<Integer, Integer> map = StringUtil.toMap(weekContribution, Integer.class, Integer.class, '=', ',');
            this.contributionMap = map;
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    public long getAccumulatedWeekContribution() {
        return accumulatedWeekContribution;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    /* Db Data Getter/Setter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public byte getPostId() {
        return postId;
    }

    public void setPostId(byte postId) {
        this.postId = postId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public int getRoleFightScore() {
        return roleFightScore;
    }

    public void setRoleFightScore(int roleFightScore) {
        this.roleFightScore = roleFightScore;
    }

    public long getHistoricalContribution() {
        return historicalContribution;
    }

    public void setHistoricalContribution(long historicalContribution) {
        this.historicalContribution = historicalContribution;
    }

    public String getWeekContribution() {
        weekContribution = StringUtil.makeString(contributionMap, '=', ',');
        return weekContribution;
    }

    public void setWeekContribution(String weekContribution) {
        this.weekContribution = weekContribution;
        setContributionMap(weekContribution);
        expireAndRecalcWeekContribution(System.currentTimeMillis());
    }

    public int getRmbDonation() {
        return rmbDonation;
    }

    public void setRmbDonation(int rmbDonation) {
        this.rmbDonation = rmbDonation;
    }

    public int getJoinTimestamp() {
        return joinTimestamp;
    }

    public void setJoinTimestamp(int joinTimestamp) {
        this.joinTimestamp = joinTimestamp;
    }

    public int getOfflineTimestamp() {
        return offlineTimestamp;
    }

    public void setOfflineTimestamp(int offlineTimestamp) {
        this.offlineTimestamp = offlineTimestamp;
    }
}

class FamilyContributionItem implements Comparable<FamilyContributionItem> {
    public int contribution;
    public int version;

    public FamilyContributionItem(int contribution, int version) {
        this.contribution = contribution;
        this.version = version;
    }

    @Override
    public int compareTo(FamilyContributionItem other) {
        return this.version - other.version;
    }
}
