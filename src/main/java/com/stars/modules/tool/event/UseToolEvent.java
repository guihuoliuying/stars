package com.stars.modules.tool.event;

import com.stars.core.event.Event;

/**
 * 使用某道具时抛出事件
 * <p>
 * Created by chenkeyu on 2016/12/6.
 */
public class UseToolEvent extends Event {
	private int itemId;
	private int count;

	public UseToolEvent(int itemId, int count) {
		this.itemId = itemId;
		this.count = count;
	}

	public int getItemId() {
		return itemId;
	}

	public int getCount() {
		return count;
	}
}
