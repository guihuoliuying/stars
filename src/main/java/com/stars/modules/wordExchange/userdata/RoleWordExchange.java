package com.stars.modules.wordExchange.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/15.
 */
public class RoleWordExchange extends DbRow {
    private long roleId;
    private Map<Integer,Integer> recordMap;

    public RoleWordExchange() {
    }

    public RoleWordExchange(long roleId) {
        this.roleId = roleId;
        this.recordMap = new HashMap<>();
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRecord() {
        return StringUtil.makeString(recordMap,'=',',');
    }

    public void setRecord(String recond) {
        this.recordMap = StringUtil.toMap(recond, Integer.class, Integer.class, '=', ',');
    }

    public Map<Integer, Integer> getRecordMap() {
        return recordMap;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolewordexchange", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolewordexchange", "`roleid`=" + roleId);
    }

    public int getExchangeTimesById(int id){
        if(StringUtil.isEmpty(recordMap)) return 0;
        Integer count = recordMap.get(id);
        return count == null ? 0 : count;
    }

    public void addRecord(int id,int count){
        if(count <= 0) return;
        if(recordMap.containsKey(id)){
            recordMap.put(id,recordMap.get(id) + count);
        }else{
            recordMap.put(id,count);
        }
    }
}
