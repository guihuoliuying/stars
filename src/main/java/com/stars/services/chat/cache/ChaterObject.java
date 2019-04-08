/**
 * $RCSfile: CacheObject.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2009/12/24 02:24:24 $
 *
 * New Jive  from Jdon.com.
 *
 * This software is the proprietary information of CoolServlets, Inc.
 * Use is subject to license terms.
 */

package com.stars.services.chat.cache;

import java.util.HashSet;

/**
 * @author dengzhou
 *世界频道聊天者对象，用于存储玩家基本数据信息
 */
public final class ChaterObject {

	private long roleId;
	
	private HashSet<Byte>refuseChannel;
	
    public MyLinkedListNode linkedListNode;

    public ChaterObject(long roleId) {
    	this.roleId = roleId;
    }

	public long getRoleId() {
		return roleId;
	}

	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}

	public void setRefuseChannel(HashSet<Byte>refuseChannel){
		if (this.refuseChannel == null) {
			this.refuseChannel = new HashSet<Byte>();
		}else {
			this.refuseChannel.clear();
		}
		for (Byte byte1 : refuseChannel) {
			this.refuseChannel.add(byte1);
		}
	}

	public HashSet<Byte> getRefuseChannel() {
		return refuseChannel;
	}

}