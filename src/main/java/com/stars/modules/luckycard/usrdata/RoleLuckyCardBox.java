package com.stars.modules.luckycard.usrdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/27.
 */
public class RoleLuckyCardBox extends DbRow {
    private long roleId;
    private int cardId;
    private int count;
    private String rewards;//单个卡的奖励，避免活动重置产品数据更改，备份一个，专供重置发奖
    private Map<Integer, Integer> rewardMap = new HashMap<>();

    public RoleLuckyCardBox(long roleId, int cardId, int count, String rewards) {
        this.roleId = roleId;
        this.cardId = cardId;
        this.count = count;
        setRewards(rewards);
    }

    public RoleLuckyCardBox() {
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roleluckycardbox", String.format(" roleid=%d and cardid=%d ", roleId, cardId));

    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roleluckycardbox", String.format(" roleid=%d and cardid=%d ", roleId, cardId));

    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getCardId() {
        return cardId;
    }

    public void setCardId(int cardId) {
        this.cardId = cardId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void addCount(int count) {
        this.count += count;
    }

    public void writeBuff(NewByteBuffer buff) {
        buff.writeInt(cardId);
        buff.writeInt(count);
    }

    public String getRewards() {
        return rewards;
    }

    public void setRewards(String rewards) {
        this.rewards = rewards;
        rewardMap = StringUtil.toMap(rewards, Integer.class, Integer.class, '+', '|');
    }

    public Map<Integer, Integer> getRewardMap() {
        return rewardMap;
    }

    public void setRewardMap(Map<Integer, Integer> rewardMap) {
        this.rewardMap = rewardMap;
    }
}
