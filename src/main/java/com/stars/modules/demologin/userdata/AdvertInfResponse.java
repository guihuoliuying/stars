package com.stars.modules.demologin.userdata;

public class AdvertInfResponse {
	
	private int status;//操作状态    0 成功    其他失败
	
	private String msg;
	
	private String shortid;//短链id
	
	private String adsource;//短链标示

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getShortid() {
		return shortid;
	}

	public void setShortid(String shortid) {
		this.shortid = shortid;
	}

	public String getAdsource() {
		return adsource;
	}

	public void setAdsource(String adsource) {
		this.adsource = adsource;
	}

}
