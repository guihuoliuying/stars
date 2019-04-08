package com.stars.modules.friend.event;

import com.stars.core.event.Event;

import java.util.List;

public class FriendLogEvent extends Event {
	
	public static byte APPLY = 1;
	public static byte ACCEPT = 2;
	public static byte PHYSICAL = 3;
	public static byte FLOWER = 4;
	public static byte BLACKLIST = 5;
	public static byte FIGHT = 6;
	public static byte FAMILY_INVITE = 7;
	
	public FriendLogEvent(byte opType) {
		this.opType = opType;
	}
	
	private byte opType;
	
	private long friendId;
	
	private int num;
	
	private int friendShip;
	
	private byte state;
	
	private List<Long> friendList;

	public byte getOpType() {
		return opType;
	}

	public void setOpType(byte opType) {
		this.opType = opType;
	}

	public long getFriendId() {
		return friendId;
	}

	public void setFriendId(long friendId) {
		this.friendId = friendId;
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public int getFriendShip() {
		return friendShip;
	}

	public void setFriendShip(int friendShip) {
		this.friendShip = friendShip;
	}

	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}

	public List<Long> getFriendList() {
		return friendList;
	}

	public void setFriendList(List<Long> friendList) {
		this.friendList = friendList;
	}

}
