package com.stars.modules.serverLog.logBean;

public class StatBean {
	private String channel;//子渠道
	private long accountCount;//账号数
	private long sumGold;//渠道的金币数
	private long sumMoney;//渠道的银两
	private long sumbandGold;//渠道的绑金数
	private String palform;//平台4安卓   5IOS
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public long getAccountCount() {
		return accountCount;
	}
	public void setAccountCount(long accountCount) {
		this.accountCount = accountCount;
	}
	public long getSumGold() {
		return sumGold;
	}
	public void setSumGold(long sumGold) {
		this.sumGold = sumGold;
	}
	public long getSumMoney() {
		return sumMoney;
	}
	public void setSumMoney(long sumMoney) {
		this.sumMoney = sumMoney;
	}
	public long getSumbandGold() {
		return sumbandGold;
	}
	public void setSumbandGold(long sumbandGold) {
		this.sumbandGold = sumbandGold;
	}
	public String getPalform() {
		return palform;
	}
	public void setPalform(String palform) {
		this.palform = palform;
	}
}
