package com.stars.modules.serverLog.logBean;

public class Stat2Bean {
	private String name;//账号
	private String palform;//平台
	private String channel;//渠道
	private int levelcount;//等级分布
	private int level;//等级
	private int vipcount;//vip分布
	private int vip;//vip

	public String getPalform() {
		return palform;
	}
	public void setPalform(String palform) {
		this.palform = palform;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevelcount() {
		return levelcount;
	}
	public void setLevelcount(int levelcount) {
		this.levelcount = levelcount;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getVipcount() {
		return vipcount;
	}
	public void setVipcount(int vipcount) {
		this.vipcount = vipcount;
	}
	public int getVip() {
		return vip;
	}
	public void setVip(int vip) {
		this.vip = vip;
	}
}
