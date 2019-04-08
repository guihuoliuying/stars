package com.stars.modules.family.event;

import com.stars.core.event.Event;

public class FamilyLogEvent extends Event {
	
	public static byte RED_SEND = 1;
	
	public static byte EXCHANGE = 2;
	
	public static byte FAMILY_QUIT = 3;
	
	private byte opType;
	
	private long familyId;
	
	private long roleId;
	
	private byte type;
	
	private int itemId;
	
	private byte itemType;
	
	private int num;
	
	private int money;
	
	public FamilyLogEvent(byte opType) {
		this.opType = opType;
	}

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getMoney() {
		return money;
	}

	public void setMoney(int money) {
		this.money = money;
	}

	public byte getItemType() {
		return itemType;
	}

	public void setItemType(byte itemType) {
		this.itemType = itemType;
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	public long getFamilyId() {
		return familyId;
	}

	public void setFamilyId(long familyId) {
		this.familyId = familyId;
	}

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

}
