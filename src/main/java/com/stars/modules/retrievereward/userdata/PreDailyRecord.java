package com.stars.modules.retrievereward.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreDailyRecord extends DbRow {
	
	private long roleId;
	
	private Map<Short, Integer>recordMap;
	
	private long lastResetTimeStamp;
	
	public PreDailyRecord(){}
	
	public PreDailyRecord(long roleId , Map<Short, Integer> recordMap , long lastResetTimeStamp){
		this.roleId = roleId;
		this.recordMap = recordMap;
		this.lastResetTimeStamp = lastResetTimeStamp;
	}

	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "predailyrecord",
        		" roleid=" + this.roleId);
	}

	@Override
	public String getDeleteSql() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setRecord(String record){
		recordMap = new HashMap<Short, Integer>();
		if (record.equals("")) {
			return;
		}
		String[] strings = record.split("[|]");
		for (String string : strings) {
			String[] ss = string.split("[=]");
			recordMap.put(Short.parseShort(ss[0]),Integer.parseInt(ss[1]));
		}
	}
	
	public String getRecord(){
		if (recordMap == null || recordMap.size() <= 0) {
			return "";
		}
		StringBuffer buffer = new StringBuffer();
		Set<Short>set = recordMap.keySet();
		for (Short dailyId : set) {
			if (buffer.length() > 0) {
				buffer.append("|");
			}
			buffer.append(dailyId).append("=");
			buffer.append(recordMap.get(dailyId));
		}
		return buffer.toString();
	}

	
	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public Map<Short, Integer> getRecordMap() {
		return recordMap;
	}

	public void setRecordMap(Map<Short, Integer> recordMap) {
		this.recordMap = recordMap;
	}
	
	public int getPreDailyRecord(short dailyId){
		if (recordMap.containsKey(dailyId)) {
			return recordMap.get(dailyId);
		}
		return 0;
	}
	
	public void setPreDailRecord(short dailyId,int count){
		this.recordMap.put(dailyId, count);
	}
	
	public long getLastResetTimeStamp(){
    	return lastResetTimeStamp;
    }
    
    public void setLastResetTimeStamp(long value){
    	this.lastResetTimeStamp = value;
    }
}
