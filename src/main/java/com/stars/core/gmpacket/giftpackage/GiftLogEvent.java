package com.stars.core.gmpacket.giftpackage;

import com.stars.core.event.Event;

public class GiftLogEvent extends Event{
	public static int TYPE_CANUSE = 1;//可使用
	public static int TYPE_HADUSED = 2;//已用
	public static int TYPE_NOGIFTCODE = 3;//没有礼包数据
	public static int TYPE_GIFTHADGOT = 4;//礼包已经领取,不能重复领取
	public static int TYPE_OVERTIME =5;//过期
	
	private String window;//界面
	private String serizesid;//序列号
	private String giftID;//礼包id
	private String giftType;//礼包类型
	private String toolInfo;//礼包详细
	private String guildId;//公会
	
	public GiftLogEvent(int type){
		this.giftType = type+"";
	}

	public static int getTYPE_CANUSE() {
		return TYPE_CANUSE;
	}

	public static void setTYPE_CANUSE(int tYPE_CANUSE) {
		TYPE_CANUSE = tYPE_CANUSE;
	}

	public static int getTYPE_HADUSED() {
		return TYPE_HADUSED;
	}

	public static void setTYPE_HADUSED(int tYPE_HADUSED) {
		TYPE_HADUSED = tYPE_HADUSED;
	}

	public static int getTYPE_NOGIFTCODE() {
		return TYPE_NOGIFTCODE;
	}

	public static void setTYPE_NOGIFTCODE(int tYPE_NOGIFTCODE) {
		TYPE_NOGIFTCODE = tYPE_NOGIFTCODE;
	}

	public static int getTYPE_GIFTHADGOT() {
		return TYPE_GIFTHADGOT;
	}

	public static void setTYPE_GIFTHADGOT(int tYPE_GIFTHADGOT) {
		TYPE_GIFTHADGOT = tYPE_GIFTHADGOT;
	}

	public static int getTYPE_OVERTIME() {
		return TYPE_OVERTIME;
	}

	public static void setTYPE_OVERTIME(int tYPE_OVERTIME) {
		TYPE_OVERTIME = tYPE_OVERTIME;
	}

	public String getWindow() {
		return window;
	}

	public void setWindow(String window) {
		this.window = window;
	}

	public String getSerizesid() {
		return serizesid;
	}

	public void setSerizesid(String serizesid) {
		this.serizesid = serizesid;
	}

	public String getGiftID() {
		return giftID;
	}

	public void setGiftID(String giftID) {
		this.giftID = giftID;
	}

	public String getGiftType() {
		return giftType;
	}

	public void setGiftType(String giftType) {
		this.giftType = giftType;
	}

	public String getToolInfo() {
		return toolInfo;
	}

	public void setToolInfo(String toolInfo) {
		this.toolInfo = toolInfo;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}
	
}
