package com.stars.services.rank.userdata;

import com.stars.db.DbRow;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/11/25.
 */
public abstract class AbstractRankPo extends DbRow implements Comparable, Cloneable {

    private int rankId;
    private int rank;// 排名

    public abstract long getUniqueId();

    public abstract void writeToBuffer(int rankId, NewByteBuffer buff);

    public abstract AbstractRankPo copy();

    public final int getRankId() {
        return rankId;
    }

    public final void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public final void setRank(int rank) {
        this.rank = rank;
    }

    public final int getRank() {
        return rank;
    }

}
