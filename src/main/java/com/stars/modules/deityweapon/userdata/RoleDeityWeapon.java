package com.stars.modules.deityweapon.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.deityweapon.DeityWeaponConstant;
import com.stars.modules.deityweapon.DeityWeaponManager;
import com.stars.modules.deityweapon.prodata.DeityWeaponVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 用户神兵表;
 * Created by panzhenfeng on 2016/12/2.
 */
public class RoleDeityWeapon extends DbRow {
    //角色id;
    private long roleId;
    //神兵类型;
    private byte type;
    //神兵等级;
    private int level;
    //到期时间, 注意:0时标识永久;
    private long endTimestamp;
    //状态;
    private byte state;

    //是否是永久的;
    public boolean isForever(){
        return endTimestamp == 0;
    }

    //是否过期了;
    public boolean isExpired(){
        if(isForever()){
            return false;
        }else{
            return System.currentTimeMillis() > endTimestamp;
        }
    }

    public int getDeityweaponId(int jobId){
        DeityWeaponVo deityWeaponVo = DeityWeaponManager.getDeityWeaponVo(jobId, type);
        if(deityWeaponVo != null){
            return deityWeaponVo.getDeityweaponId();
        }
        return 0;
    }

    public String makeString() {
        StringBuilder sb = new StringBuilder();
        sb.append(roleId).append(";").append(type).append(";").append(level).append(";").append(endTimestamp).append(";").append(state);
        return sb.toString();
    }

    public void parseString(String str){
        if(str == null) return;
        String[] strData = str.split(";");
        this.roleId = Long.parseLong(strData[0]);
        this.type = Byte.parseByte(strData[1]);
        this.level = Integer.parseInt(strData[2]);
        this.endTimestamp = Long.parseLong(strData[3]);
        this.state = Byte.parseByte(strData[4]);
    }

    public void writeBuff(NewByteBuffer buff){
        buff.writeString(String.valueOf(roleId));
        buff.writeByte(type);
        buff.writeInt(level);
        buff.writeString(String.valueOf(endTimestamp));
        buff.writeByte(state);
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, DeityWeaponConstant.RoledeityweaponName, " roleid='" + this.getRoleId() + "' and type="+this.getType());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql(DeityWeaponConstant.RoledeityweaponName, " roleid='" + this.getRoleId() + "' and type="+this.getType());
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }
}
