package com.stars.modules.skyrank.userdata;

/**
 * 
 * 天梯积分记录
 * 
 * @author xieyuejun
 *
 */
public class SkyRankScoreRecord {
	
	//1,10,10&2,10,10
	private short type;// 积分产出业务类型
	private int linkWin;// 暂时没用，可拓展
	private int addScoreTimes;// 已经添加积分的次数

	public SkyRankScoreRecord(String contents){
		String[] args = contents.split(",");
		this.type = Short.parseShort(args[0]);
		this.addScoreTimes = Integer.parseInt(args[1]);
	}
	
	public String toString(){
		return this.type+","+this.addScoreTimes;
	}
	
	public SkyRankScoreRecord(short type) {
		this.type = type;
	}

	public void addScoreTime() {
		addScoreTimes++;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public int getLinkWin() {
		return linkWin;
	}

	public void setLinkWin(int linkWin) {
		this.linkWin = linkWin;
	}

	public int getAddScoreTimes() {
		return addScoreTimes;
	}

	public void setAddScoreTimes(int addScoreTimes) {
		this.addScoreTimes = addScoreTimes;
	}

}
