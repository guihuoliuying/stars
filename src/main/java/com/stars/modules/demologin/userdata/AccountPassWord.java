package com.stars.modules.demologin.userdata;

public class AccountPassWord {
	private String account;
	private String password;
	private String uid;
	
	public AccountPassWord(){}
	
	public AccountPassWord(String account,String password){
		this.account = account;
		this.password = password;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
