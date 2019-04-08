package com.stars.services.fightingmaster.event;

import com.stars.core.event.Event;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/22.
 */
public class NoticeMainServerAddTool extends Event {
    private Map<Integer, Integer> itemMap;
    private byte status = 0;//0失败,1成功
    
    //有效加成勋章id，默认无加成为-1
    private int validMedalId = -1;

	public NoticeMainServerAddTool(Map<Integer, Integer> tool,byte status) {
        itemMap = tool;
        this.status = status;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }
    
    public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
    
	public int getValidMedalId() {
		return validMedalId;
	}

	public void setValidMedalId(int validMedalId) {
		this.validMedalId = validMedalId;
	}
}
