package com.stars.modules.newequipment.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyuxing on 2016/12/29.
 */
public class EffectPlayRecord extends DbRow {
    private long roleId;
    private List<Integer> effectPlayList;

    public EffectPlayRecord(long roleId) {
        this.roleId = roleId;
        this.effectPlayList = new ArrayList<>();
    }

    public EffectPlayRecord() {
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public List<Integer> getEffectPlayList() {
        return effectPlayList;
    }

    public void setEffectPlayList(List<Integer> effectPlayList) {
        this.effectPlayList = effectPlayList;
    }

    public String getEffectPlay() {
        return StringUtil.makeString(effectPlayList,'+');
    }

    public void setEffectPlay(String effectPlay) throws Exception{
        if(StringUtil.isEmpty(effectPlay)){
            effectPlayList = new ArrayList<>();
        }else{
            effectPlayList = StringUtil.toArrayList(effectPlay,Integer.class,'+');
        }
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "effectplayrecord", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("effectplayrecord", "`roleid`=" + roleId);
    }
}
