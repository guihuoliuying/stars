package com.stars.modules.demologin.userdata;
/**
 * 
 * 内部账号,白名单
 * @author huangdimin
 *
 */
public class InnetAccount {
	private String account=null;
	private String sid = null;
	private String time = null;
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getSid() {
		return sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
}
