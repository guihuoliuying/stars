package com.stars.services.rank.userdata;

import com.stars.db.DBUtil;
import com.stars.db.SqlUtil;
import com.stars.modules.bestcp520.BestCPManager;
import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCPRankPo extends AbstractRankPo {
    private int cpId;
    private int voteSum;

    public BestCPRankPo(int cpId, int voteSum) {
        this.cpId = cpId;
        this.voteSum = voteSum;

    }

    public BestCPRankPo() {
    }

    @Override
    public long getUniqueId() {
        return cpId;
    }

    @Override
    public void writeToBuffer(int rankId, NewByteBuffer buff) {

    }

    @Override
    public AbstractRankPo copy() {
        try {
            return (AbstractRankPo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("BestCPRankPo克隆失败", e);
        }
        return null;
    }


    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rankbestcp", "cpid=" + cpId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rankbestcp", "cpid=" + cpId);
    }

    @Override
    public int compareTo(Object o) {
        BestCPRankPo other = null;
        if (o instanceof BestCPRankPo) {
            other = (BestCPRankPo) o;
            if (!(other.getCpId().equals(this.getCpId()))) {
                int sub = other.getVoteSum() - this.getVoteSum();
                if (sub == 0) {
                    return other.getBestCP().getOrder() - getBestCP().getOrder();
                }
                return sub;
            }
        }
        return 0;
    }

    public Integer getCpId() {
        return cpId;
    }

    public void setCpId(Integer cpId) {
        this.cpId = cpId;
    }


    public int getVoteSum() {
        return voteSum;
    }

    public void setVoteSum(int voteSum) {
        this.voteSum = voteSum;
    }

    public BestCP getBestCP() {
        return BestCPManager.bestCPMap.get(cpId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BestCPRankPo that = (BestCPRankPo) o;

        if (cpId != that.cpId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (cpId ^ (cpId >>> 32));
    }
}
