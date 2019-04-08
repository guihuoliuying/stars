package com.stars.services.friend.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2016/11/14.
 */
public class ReceiveFlowerRecordPo extends DbRow implements Comparable<ReceiveFlowerRecordPo> {
    private long roleId;
    private long friendId;
    private String friendName;
    private int count;
    private int jobId;
    private int level;
    private long occurTimestamp;

    public ReceiveFlowerRecordPo() {
    }

    public ReceiveFlowerRecordPo(long roleId, long friendId, String friendName, int count, int jobId, int level) {
        this.roleId = roleId;
        this.friendId = friendId;
        this.friendName = friendName;
        this.count = count;
        this.jobId = jobId;
        this.level = level;
        this.occurTimestamp = System.currentTimeMillis();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "receiveflowerrecord", "`roleid`=" + roleId + " and `friendid`=" + friendId + " and `occurtimestamp`=" + occurTimestamp);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `receiveflowerrecord` where `roleid`=" + roleId + " and `friendid`=" + friendId + " and `occurtimestamp`=" + occurTimestamp;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getOccurTimestamp() {
        return occurTimestamp;
    }

    public void setOccurTimestamp(long occurTimestamp) {
        this.occurTimestamp = occurTimestamp;
    }

    public void addOccurTimestamp(){
        this.occurTimestamp++;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void writeToBuff(NewByteBuffer buff) {
        buff.writeLong(friendId);
        buff.writeString(friendName);
        buff.writeInt(count);
        buff.writeInt(level);
        buff.writeInt(jobId);
        buff.writeLong(occurTimestamp);
    }

    @Override
    public int compareTo(ReceiveFlowerRecordPo po) {
        if(this.occurTimestamp == po.occurTimestamp){
            return 0;
        }else if(this.occurTimestamp < po.occurTimestamp){
            return 1;
        }else{
            return -1;
        }
    }
}
