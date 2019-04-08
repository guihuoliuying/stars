package com.stars.services.family.main.memdata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhaowenshuo on 2016/8/25.
 */
public class RecommendationFamily implements Comparable<RecommendationFamily> {

    // 列出所有需要展示的信息
    // 列出排序需要考虑的信息

    private long familyId;
    private String name;
    private int level;
    private int memberCount; // 成员数量
    private int memberLimit; // 成员上限
    private byte allowApplication; // 是否允许申请
    private String masterName; // 族长名字
    private long totalFightScore; // 总战力
    private int qualificationMinLevel; // 申请条件最小等级
    private int qualificationMinFightScore; // 申请条件最小战力
    private String notice; // 公告

    @Override
    public int compareTo(RecommendationFamily other) {
        if (this.getAppliedWeight() != other.getAppliedWeight()) {
            return -(this.getAppliedWeight() - other.getAppliedWeight());
        }
        if (this.getLevel() != other.getLevel()) {
            return -(this.getLevel() - other.getLevel());
        }
        if (this.getTotalFightScore() != other.getTotalFightScore()) {
            return -(this.getTotalFightScore() > other.getTotalFightScore() ? 1 : -1);
        } else {
            return 0;
        }
    }

    private int getAppliedWeight() {
        return allowApplication == 1 && memberLimit > memberCount ? 1 : 0;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(familyId));
        buff.writeString(name);
        buff.writeInt(level);
        buff.writeInt(memberCount);
        buff.writeInt(memberLimit); // 如果客户端能拿到，我这边去掉
        buff.writeString(masterName); // 族长名字
        buff.writeInt(qualificationMinLevel); // 申请条件：最小等级
        buff.writeInt(qualificationMinFightScore); // 申请条件：最小战力
        buff.writeString(notice); // 公告
    }

    @Override
    public String toString() {
        return "RecommendationFamily{" +
                "familyId=" + familyId +
                ", name='" + name + '\'' +
                ", level=" + level +
                ", memberCount=" + memberCount +
                ", memberLimit=" + memberLimit +
                ", allowApplication=" + allowApplication +
                ", masterName='" + masterName + '\'' +
                ", totalFightScore=" + totalFightScore +
                ", qualificationMinLevel=" + qualificationMinLevel +
                ", qualificationMinFightScore=" + qualificationMinFightScore +
                ", notice='" + notice + '\'' +
                '}';
    }

    /* Getter/Setter */
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public int getMemberLimit() {
        return memberLimit;
    }

    public void setMemberLimit(int memberLimit) {
        this.memberLimit = memberLimit;
    }

    public byte getAllowApplication() {
        return allowApplication;
    }

    public void setAllowApplication(byte allowApplication) {
        this.allowApplication = allowApplication;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public long getTotalFightScore() {
        return totalFightScore;
    }

    public void setTotalFightScore(long totalFightScore) {
        this.totalFightScore = totalFightScore;
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

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
