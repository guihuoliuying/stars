package com.stars.modules.newsignin.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.TimeUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenkeyu on 2017/2/6 10:52
 */
public class RoleSigninPo extends DbRow {
    private long roleid;            //玩家Id
    private Set<String> signedDates; //当月签到的所有signId
    private int signCount;          //当月签到总次数(包括补签)
    private int reSignCount;        //当月补签总次数
    private Set<Integer> accAwards; //已领取的累积奖励次数
    private Set<Integer> specAwards;//已领取的特殊奖励次数

    public RoleSigninPo(long roleid) {
        this.roleid = roleid;
        this.signedDates = new HashSet<>();
        this.accAwards = new HashSet<>();
        this.specAwards = new HashSet<>();
    }

    public void writeToBuffer(NewByteBuffer buff) {
        String dateStr = TimeUtil.getDateYYYYMMDD();
        buff.writeString(dateStr);
        buff.writeString(getSignedDates());
        buff.writeInt(signCount);
        buff.writeInt(reSignCount);
        buff.writeString(getAccAwards());
        buff.writeString(getSpecAwards());
    }

    public long getRoleid() {
        return roleid;
    }

    public void setRoleid(long roleid) {
        this.roleid = roleid;
    }

    public void addSign(String signDate) {
        signedDates.add(signDate);
    }

    public void addAccAwards(int times) {
        accAwards.add(times);
    }

    public void addSpecAwards(int times) {
        specAwards.add(times);
    }

    public void resetAccAwards() {
        accAwards.clear();
    }

    public void resetSpecAwards() {
        specAwards.clear();
    }

    public void resetSign() {
        signedDates.clear();
    }

    public void signCountInc() {
        signCount++;
    }

    public void reSignCountInc() {
        reSignCount++;
    }

    public String getSignedDates() {
        StringBuffer buffer = new StringBuffer();
        for (String signId : this.signedDates) {
            buffer.append(signId).append("|");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    public String getAccAwards() {
        StringBuffer buffer = new StringBuffer();
        for (Integer times : this.accAwards) {
            buffer.append(times).append("|");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    public String getSpecAwards() {
        StringBuffer buffer = new StringBuffer();
        for (Integer times : this.specAwards) {
            buffer.append(times).append("|");
        }
        if (buffer.length() > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer.toString();
    }

    public void setSignedDates(String signedDates) {}

    public void setAccAwards(String accAwards) {}

    public void setSpecAwards(String specAwards) {}

    public Set<Integer> getAccAwardsSet() {
        return accAwards;
    }

    public Set<Integer> getSpecAwardsSet() {
        return specAwards;
    }

    public Set<String> getSignedDatesSet() {
        return signedDates;
    }

    public void setSignedDatesStr(String signedIdsStr) {
        if (signedIdsStr == null || signedIdsStr.equals("")) return;
        String[] strings = signedIdsStr.split("\\|");
        for (String signId : strings) {
            this.signedDates.add(signId);
        }
    }

    public void setAccAwardsStr(String accAwards) {
        if (accAwards == null || accAwards.equals("")) return;
        String[] strings = accAwards.split("\\|");
        for (String times : strings) {
            this.accAwards.add(Integer.parseInt(times));
        }
    }

    public void setSpecAwardsStr(String specAwards) {
        if (specAwards == null || specAwards.equals("")) return;
        String[] strings = specAwards.split("\\|");
        for (String times : strings) {
            this.specAwards.add(Integer.parseInt(times));
        }
    }

    public int getSignCount() {
        return signCount;
    }

    public void setSignCount(int signCount) {
        this.signCount = signCount;
    }

    public int getReSignCount() {
        return reSignCount;
    }

    public void setReSignCount(int reSignCount) {
        this.reSignCount = reSignCount;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolesignin", "`roleid`=" + roleid);
    }

    /*@Override
    public String getDeleteSql() {
        return "delete from `rolesignin` where `roleid`=" + roleid;
    }*/

    @Override
    public String getDeleteSql() {
        return "";
    }
}
