package com.stars.modules.baby.usedata;

import com.stars.core.attr.Attribute;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.baby.BabyConst;
import com.stars.modules.baby.BabyManager;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class RoleBaby extends DbRow {
    private long roleId;
    private String babyName;//宝宝名字
    //    private int babyStage;//宝宝阶段1,2,3,4  这两个挂在角色身上，这里就去掉了
//    private int babyLevel;//宝宝等级
    private int currentProgress;//宝宝当前阶段当前等级的进度
    private int normalPrayTimes;//今天普通求子的次数
    private int payPrayTimes;//今天重金求子的次数
    private int normalFeedTimes;//今日吃土的次数
    private int payFeedTimes;//今日下馆子次数
    private byte changeName;//是否改过宝宝名字|0:为改过,1:改过
    private String lastTimesFeedAction;//上次喂养动作的一些记录
    private int sweepMark;//六国寻宝扫荡标记0,1,2,3
    private int extraFeedOrPrayTimes;//购买的额外次数
    private byte isFollow;//是否跟随
    //内存数据
    private Attribute attribute = new Attribute();
    private Attribute prayStage = new Attribute();
    private int power;
    private String ownFashionIds = "";
    private int usingFashionId = BabyManager.defaultFashionId;
    private Set<Integer> ownFashionIdSet = new HashSet<>();

    public RoleBaby() {
    }

    public RoleBaby(long roleId) {
        this.roleId = roleId;
        addFashionId(BabyManager.defaultFashionId);
    }

    public void writeToBuff(NewByteBuffer buff, int babyStage) {
        if (babyStage == BabyConst.PRAY) {
            prayStage.writeToBuffer(buff);
        } else {
            attribute.writeToBuffer(buff);          //属性
        }
        buff.writeInt(power);                   //战力
        buff.writeInt(BabyManager.getMaxPrayOrFeedCount(babyStage) - normalPrayTimes + extraFeedOrPrayTimes);         //普通求子次数
        buff.writeInt(currentProgress);         //当前阶段的进度
        buff.writeInt(BabyManager.getMaxPrayOrFeedCount(babyStage) - normalFeedTimes + extraFeedOrPrayTimes);         //普通培养次数
        buff.writeString(babyName);             //宝宝名字
        buff.writeString(lastTimesFeedAction);  //上次培养动作
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getBabyName() {
        return babyName;
    }

    public void setBabyName(String babyName) {
        this.babyName = babyName;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public void addCurrentProgress(int progress) {
        this.currentProgress += progress;
    }

    public int getNormalPrayTimes() {
        return normalPrayTimes;
    }

    public void setNormalPrayTimes(int normalPrayTimes) {
        this.normalPrayTimes = normalPrayTimes;
    }

    public void addNormalPrayTimes() {
        this.normalPrayTimes++;
    }

    public int getPayPrayTimes() {
        return payPrayTimes;
    }

    public void setPayPrayTimes(int payPrayTimes) {
        this.payPrayTimes = payPrayTimes;
    }

    public void addPayPrayTimes() {
        this.payPrayTimes++;
    }

    public int getNormalFeedTimes() {
        return normalFeedTimes;
    }

    public void setNormalFeedTimes(int normalFeedTimes) {
        this.normalFeedTimes = normalFeedTimes;
    }

    public void addNormalFeedTimes() {
        this.normalFeedTimes++;
    }

    public void addPayFeedTimes() {
        this.payFeedTimes++;
    }

    public int getPayFeedTimes() {
        return payFeedTimes;
    }

    public void setPayFeedTimes(int payFeedTimes) {
        this.payFeedTimes = payFeedTimes;
    }

    public byte getChangeName() {
        return changeName;
    }

    public void setChangeName(byte changeName) {
        this.changeName = changeName;
    }

    public String getLastTimesFeedAction() {
        return lastTimesFeedAction;
    }

    public void setLastTimesFeedAction(String lastTimesFeedAction) {
        this.lastTimesFeedAction = lastTimesFeedAction;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Attribute getPrayStage() {
        return prayStage;
    }

    public void setPrayStage(Attribute prayStage) {
        this.prayStage = prayStage;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getSweepMark() {
        return sweepMark;
    }

    public void setSweepMark(int sweepMark) {
        this.sweepMark = sweepMark;
    }

    public int getExtraFeedOrPrayTimes() {
        return extraFeedOrPrayTimes;
    }

    public void setExtraFeedOrPrayTimes(int extraFeedOrPrayTimes) {
        this.extraFeedOrPrayTimes = extraFeedOrPrayTimes;
    }

    public void addExtraFeedOrPrayTimes(int delta) {
        this.extraFeedOrPrayTimes += delta;
    }

    public byte getIsFollow() {
        return isFollow;
    }

    public void setIsFollow(byte isFollow) {
        this.isFollow = isFollow;
    }

    public String getOwnFashionIds() {
        return ownFashionIds;
    }

    public void setOwnFashionIds(String ownFashionIds) {
        this.ownFashionIds = ownFashionIds;
        try {
            ownFashionIdSet = StringUtil.toHashSet(ownFashionIds, Integer.class, '+');
            if (ownFashionIdSet.size() == 0) {
                addFashionId(BabyManager.defaultFashionId);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加拥有时装
     *
     * @param fashionId
     */
    public void addFashionId(int fashionId) {
        ownFashionIdSet.add(fashionId);
        this.ownFashionIds = StringUtil.makeString(ownFashionIdSet, '+');
    }

    public boolean isOwnFashionId(int fashionId) {
        return ownFashionIdSet.contains(fashionId);
    }

    public Set<Integer> getOwnFashionIdSet() {
        return ownFashionIdSet;
    }

    public int getUsingFashionId() {
        return usingFashionId;
    }

    public void setUsingFashionId(int usingFashionId) {
        if (usingFashionId == 0) {
            usingFashionId = BabyManager.defaultFashionId;
        }
        this.usingFashionId = usingFashionId;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolebaby", "`roleid`=" + this.roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from rolebaby where roleid=" + this.roleId;
    }

    @Override
    public String toString() {
        return "RoleBaby{" +
                "roleId=" + roleId +
                ", babyName='" + babyName + '\'' +
                ", currentProgress=" + currentProgress +
                ", normalPrayTimes=" + normalPrayTimes +
                ", payPrayTimes=" + payPrayTimes +
                ", normalFeedTimes=" + normalFeedTimes +
                ", payFeedTimes=" + payFeedTimes +
                ", changeName=" + changeName +
                ", lastTimesFeedAction='" + lastTimesFeedAction + '\'' +
                ", sweepMark=" + sweepMark +
                ", extraFeedOrPrayTimes=" + extraFeedOrPrayTimes +
                ", isFollow=" + isFollow +
                "} ";
    }
}
