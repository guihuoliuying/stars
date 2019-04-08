package com.stars.modules.chat.usrdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;

import java.util.HashSet;

public class ChaterInfo extends DbRow {
	/**
	 * 玩家拒绝接收的消息频道
	 */
	private HashSet<Byte>refuseChannel;
	
	private long roleId;
	
	public ChaterInfo(){
		
	}
	
	public ChaterInfo(long roleId){
		this.roleId = roleId;
		refuseChannel = new HashSet<Byte>();
	}
	
	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "chaterinfo", "roleid="+roleId);
	}

	@Override
	public String getDeleteSql() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRefuseChannel() {
		if (refuseChannel.size() <= 0) {
			return "";
		}
		StringBuffer sb = new StringBuffer();
		for (Byte byte1 : refuseChannel) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(byte1);
		}
		return sb.toString();
	}

	public void setRefuseChannel(String refuseChannel) {
		this.refuseChannel = new HashSet<Byte>();
		if (refuseChannel == null || refuseChannel.equals("")) {
			return;
		}
		String[] strings = refuseChannel.split("&");
		for (String string : strings) {
			this.refuseChannel.add(Byte.parseByte(string));
		}
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	
	public boolean refuseChannel(byte channel){
		return refuseChannel.contains(channel);
	}
	
	public HashSet<Byte>getRefuseChannelSet(){
		return this.refuseChannel;
	}

	public boolean removeRefuseChannel(byte channel){
		return this.refuseChannel.remove(channel);
	}
	
	public boolean addRefuseChannel(byte channel){
		return this.refuseChannel.add(channel);
	}
}
