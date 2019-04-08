package com.stars.services.family.main.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.services.family.main.FamilyMainConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyPo extends DbRow implements Cloneable {

    private long familyId; // 家族id
    private String name; // 家族名字
    private String masterName; // 族长名字
    private int level; // 家族等级
    private int money; // 家族资金
    private byte allowApplication; // 是否允许申请
    private int qualificationMinLevel; // 申请条件-最小等级
    private int qualificationMinFightScore; // 申请条件-最小战力
    private byte autoVerified; // 是否自动审核
    private int memberCount; // 成员数量
    private long totalFightScore; // 总战力
    private int creationTimestamp; // 创建时间
    private int lastActiveTimestamp; // 上次活跃时间
    private String notice; // 公告
    private byte lock;
    private int emailCount;//已发邮件的个数

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "family", "`familyid`=" + familyId).replace("insert", "insert ignore");
    }

    @Override
    public String getDeleteSql() {
        return "delete from `family` where `familyid`=" + familyId;
    }

    public void increaseMemberCount() {
        memberCount++;
    }

    public void decreaseMemberCount() {
        memberCount--;
    }

    public void lock() {
        lock = FamilyMainConst.allLock;
        LogUtil.info("family|家族:{} 上了全锁 lock :{}", familyId, lock);
    }

    public void unlock() {
        lock = FamilyMainConst.unLock;
        LogUtil.info("family|家族:{} 解除家族锁 lock :{}", familyId, lock);
    }

    public void halfLock() {
        lock = FamilyMainConst.halfLock;
        LogUtil.info("family|家族:{} 上了半锁 lock :{}", familyId, lock);
    }

    public byte getLockState() {
        return lock;
    }

    public boolean isLock() {
        return lock == FamilyMainConst.allLock || lock == FamilyMainConst.halfLock;
    }

    public boolean isAllLock() {
        return lock == FamilyMainConst.allLock;
    }

    public boolean isHalfLock() {
        return lock == FamilyMainConst.halfLock;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /* Mem Data Getter/Setter */
    public boolean isAllowedApplication() {
        return allowApplication == 1;
    }

    public boolean isAutoVerified() {
        return autoVerified == 1;
    }

    /* Db Data Getter/Setter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public byte getAllowApplication() {
        return allowApplication;
    }

    public void setAllowApplication(byte allowApplication) {
        this.allowApplication = allowApplication;
    }

    public int getQualificationMinLevel() {
        return qualificationMinLevel;
    }

    public void setQualificationMinLevel(int qualificationMinLevel) {
        this.qualificationMinLevel = qualificationMinLevel;
    }

    public int getQualificationMinFightScore() {
        return qualificationMinFightScore;
    }

    public void setQualificationMinFightScore(int qualificationMinFightScore) {
        this.qualificationMinFightScore = qualificationMinFightScore;
    }

    public byte getAutoVerified() {
        return autoVerified;
    }

    public void setAutoVerified(byte autoVerified) {
        this.autoVerified = autoVerified;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public long getTotalFightScore() {
        return totalFightScore;
    }

    public void setTotalFightScore(long totalFightScore) {
        this.totalFightScore = totalFightScore;
    }

    public int getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(int creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    public int getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(int lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    public String getNotice() {
        if(StringUtil.isNotEmpty(notice)){
            notice = notice.replace("'","");
        }
        return notice;
    }

    public void setNotice(String notice) {
        if(StringUtil.isNotEmpty(notice)){
            notice = notice.replace("'","");
        }
        this.notice = notice;
    }

    public byte getLock() {
        return lock;
    }

    public void setLock(byte lock) {
        this.lock = lock;
    }

    public int getEmailCount() {
        return emailCount;
    }

    public void setEmailCount(int emailCount) {
        this.emailCount = emailCount;
    }
}
