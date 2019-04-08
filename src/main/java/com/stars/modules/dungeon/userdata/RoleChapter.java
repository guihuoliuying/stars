package com.stars.modules.dungeon.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class RoleChapter extends DbRow {
    private long roleId;
    private int chapterId;// 章节Id
    private byte isReward;// 是否已领取奖励(1未领,2已领)

    public RoleChapter() {
    }

    public RoleChapter(long roleId, int chapterId) {
        this.roleId = roleId;
        this.chapterId = chapterId;
        this.isReward = DungeonManager.CHAPTER_STAR_NOT_REWARD;
//        setInsertStatus();
    }

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(chapterId);
        buffer.writeByte(isReward);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolechapter", " roleid=" + this.getRoleId() +
                " and chapterid=" + chapterId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolechapter", " roleid=" + this.getRoleId() + " and chapterid=" + chapterId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getChapterId() {
        return chapterId;
    }

    public void setChapterId(int chapterId) {
        this.chapterId = chapterId;
    }

    public byte getIsReward() {
        return isReward;
    }

    public void setIsReward(byte isReward) {
        this.isReward = isReward;
    }
}
