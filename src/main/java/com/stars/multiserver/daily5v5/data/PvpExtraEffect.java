package com.stars.multiserver.daily5v5.data;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

public class PvpExtraEffect {
	
	private int fivepvpvipeffid;
	
	private String name;
	
	private String desc;
	
	private String activedesc;
	
	private int effecttype;
	
	private String param;
	
	private int[] paramArr;
	
	private byte passive;
	
	private int level;
	
	private String effecticon;
	
	private int buffId;

	public int getFivepvpvipeffid() {
		return fivepvpvipeffid;
	}

	public void setFivepvpvipeffid(int fivepvpvipeffid) {
		this.fivepvpvipeffid = fivepvpvipeffid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getActivedesc() {
		return activedesc;
	}

	public void setActivedesc(String activedesc) {
		this.activedesc = activedesc;
	}

	public int getEffecttype() {
		return effecttype;
	}

	public void setEffecttype(int effecttype) {
		this.effecttype = effecttype;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) throws Exception {
		this.param = param;
		paramArr = StringUtil.toArray(param, int[].class, '+');
	}

	public byte getPassive() {
		return passive;
	}

	public void setPassive(byte passive) {
		this.passive = passive;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getEffecticon() {
		return effecticon;
	}

	public void setEffecticon(String effecticon) {
		this.effecticon = effecticon;
	}

	public int[] getParamArr() {
		return paramArr;
	}

	public void setParamArr(int[] paramArr) {
		this.paramArr = paramArr;
	}
	
	public int getBuffId() {
		return buffId;
	}

	public void setBuffId(int buffId) {
		this.buffId = buffId;
	}

	public void write(NewByteBuffer buff){
		buff.writeInt(fivepvpvipeffid);//效果id
		buff.writeString(name);//效果名称
		buff.writeString(desc);//技能效果描述
		buff.writeString(activedesc);//激活描述
		buff.writeInt(effecttype);//效果类型
		buff.writeString(param);//不同效果对应的参数
		buff.writeByte(passive);//效果是否为被动效果，0否，1是
		buff.writeInt(level);//效果等级
		buff.writeString(effecticon);//效果图标名称
	}

}
