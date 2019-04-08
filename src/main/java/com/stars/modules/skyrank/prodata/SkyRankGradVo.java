package com.stars.modules.skyrank.prodata;

import com.stars.network.server.buffer.NewByteBuffer;

/**
 * 
 * 天梯段位配置数据
 * 
 * @author xieyuejun
 *
 */
public class SkyRankGradVo implements Comparable<SkyRankGradVo> {

	// skyrankgradid 天梯段位唯一标识
	// name 字符串，段位名称
	// reqscore 整值，达到该段位的最小段位分
	// icon 字符串，段位图标
	// iconeff 字符串，段位特效
	// star 整值，段位星星数量
	// bufferscore 整值，段位缓冲分

	private int skyRankGradId;// 天梯段位唯一标识
	private String name;// 字符串，段位名称
	private int reqscore;// 整值，达到该段位的最小段位分
	private String icon;// 字符串，段位图标
	private String iconEff;// 字符串，段位特效
	private int star;// 整值，段位星星数量
	private int bufferScore;// 整值，段位缓冲分
	
	public void writeBuffer(NewByteBuffer buff) {
		buff.writeInt(skyRankGradId);
		buff.writeString(name);
		buff.writeInt(reqscore);
		buff.writeString(icon);
		buff.writeString(iconEff);
		buff.writeInt(star);
		buff.writeInt(bufferScore);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getReqscore() {
		return reqscore;
	}

	public void setReqscore(int reqscore) {
		this.reqscore = reqscore;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getBufferScore() {
		return bufferScore;
	}

	public void setBufferScore(int bufferScore) {
		this.bufferScore = bufferScore;
	}

	public int getSkyRankGradId() {
		return skyRankGradId;
	}

	public void setSkyRankGradId(int skyRankGradId) {
		this.skyRankGradId = skyRankGradId;
	}

	public String getIconEff() {
		return iconEff;
	}

	public void setIconEff(String iconEff) {
		this.iconEff = iconEff;
	}

	@Override
	public int compareTo(SkyRankGradVo o) {
		return  o.getReqscore() - this.reqscore;
	}

}
