package com.stars.modules.scene.event;

import com.stars.core.event.Event;

/**
 * 进入关卡事件
 * Created by liuyuheng on 2016/8/12.
 */
public class EnterSceneEvent extends Event {
	private byte sceneType;
    private String sceneId;
    private byte lastSceneType;
    private String lastSceneId;

    public EnterSceneEvent(byte sceneType,String sceneId,byte lastSceneType,String lastSceneId) {
        this.sceneType = sceneType;
        this.sceneId = sceneId;
        this.lastSceneType = lastSceneType;
        this.lastSceneId = lastSceneId;
    }

	public byte getSceneType() {
		return sceneType;
	}

	public void setSceneType(byte sceneType) {
		this.sceneType = sceneType;
	}

	public String getSceneId() {
		return sceneId;
	}

	public void setSceneId(String sceneId) {
		this.sceneId = sceneId;
	}

	public byte getLastSceneType() {
		return lastSceneType;
	}

	public void setLastSceneType(byte lastSceneType) {
		this.lastSceneType = lastSceneType;
	}

	public String getLastSceneId() {
		return lastSceneId;
	}

	public void setLastSceneId(String lastSceneId) {
		this.lastSceneId = lastSceneId;
	}

}
