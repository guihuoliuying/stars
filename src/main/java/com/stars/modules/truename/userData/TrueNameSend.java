package com.stars.modules.truename.userData;

public class TrueNameSend {
	
	private String appId;
	
	private String type;
	
	private String account;
	
	private String realName;
	
	private String cardType;
	
	private String cardNum;
	
	private String ext;
	
	private String version;
	
	private String ip;
	
	private String mac;
	
	private String imei;
	
	private String sign;
	
	public TrueNameSend() {
		// TODO Auto-generated constructor stub
	}

	public TrueNameSend(String appId, String account, String realName, String cardNum, String sign, String cardType) {
		super();
		this.appId = appId;
		this.type = "9";
		this.account = account;
		this.realName = realName;
		this.cardType = cardType;
		this.cardNum = cardNum;
		this.ext = "";
		this.version = "";
		this.ip = "";
		this.mac = "";
		this.imei = "";
		this.sign = sign;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getCardType() {
		return cardType;
	}

	public void setCardType(String cardType) {
		this.cardType = cardType;
	}

	public String getCardNum() {
		return cardNum;
	}

	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	

}
