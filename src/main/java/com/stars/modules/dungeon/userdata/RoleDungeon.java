package com.stars.modules.dungeon.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class RoleDungeon extends DbRow {
    private long roleId;
    private int dungeonId;// 关卡Id
    private byte star;// 星星数
    private byte number;// 已挑战次数
    private byte status;// 状态;1=已开启,2=已通关

    public RoleDungeon() {
    }

    public RoleDungeon(long roleId, int dungeonId) {
        this.roleId = roleId;
        this.dungeonId = dungeonId;
        this.star = 0;
        this.number = 0;
        this.status = DungeonManager.STAGE_OPEN;
//        setInsertStatus();
    }

    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(dungeonId);
        buffer.writeByte(star);
        buffer.writeByte(number);
        buffer.writeByte(status);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roledungeon", " roleid=" + this.getRoleId() +
                " and dungeonid=" + dungeonId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roledungeon", " roleid=" + this.getRoleId() + " and dungeonid=" + dungeonId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getDungeonId() {
        return dungeonId;
    }

    public void setDungeonId(int dungeonId) {
        this.dungeonId = dungeonId;
    }

    public byte getStar() {
        return star;
    }

    public void setStar(byte star) {
        this.star = star;
    }

    public byte getNumber() {
        return number;
    }

    public void setNumber(byte number) {
        this.number = number;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }
}
