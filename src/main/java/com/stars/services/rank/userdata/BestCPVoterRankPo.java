package com.stars.services.rank.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.bestcp520.BestCPManager;
import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPVoterRankPo extends AbstractRankPo {
    private long roleId;
    private int cpId;
    private int voteSum;

    public BestCPVoterRankPo(long roleId, int cpId, int voteSum) {
        this.roleId = roleId;
        this.cpId = cpId;
        this.voteSum = voteSum;

    }

    public BestCPVoterRankPo() {
    }

    @Override
    public long getUniqueId() {
        return roleId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {

    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("BestCPVoterRankPo克隆失败", e);
        }
        return null;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rankbestcpvoter", "roleid=" + roleId + " and cpid=" + cpId);

    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rankbestcpvoter", "roleid=" + roleId + " and cpid=" + cpId);
    }

    @Override
    public int compareTo(Object o) {
        BestCPVoterRankPo other = null;
        if (o instanceof BestCPVoterRankPo) {
            other = (BestCPVoterRankPo) o;
            long sub;
            sub = other.getCpId() - this.getCpId();
            if (sub == 0) {
                sub = other.getVoteSum() - this.getVoteSum();
                if (sub == 0) {
                    return (int) (other.getRoleId() - this.getRoleId());
                }
                return (int) sub;
            }
            return (int) sub;
        }
        return 0;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getCpId() {
        return cpId;
    }

    public void setCpId(int cpId) {
        this.cpId = cpId;
    }


    public int getVoteSum() {
        return voteSum;
    }

    public void setVoteSum(int voteSum) {
        this.voteSum = voteSum;
    }

    public BestCP getBestCP() {
        return  BestCPManager.bestCPMap.get(cpId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BestCPVoterRankPo that = (BestCPVoterRankPo) o;

        if (cpId != that.cpId) return false;
        if (roleId != that.roleId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (roleId ^ (roleId >>> 32));
    }
}
