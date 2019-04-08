package com.stars.modules.shop.event;

import com.stars.core.event.Event;

public class BuyGoodsEvent extends Event {
	private int goodsId;
	private int count;
	
	public BuyGoodsEvent(int goodsId , int count){
		this.goodsId = goodsId;
		this.count = count;
	}

	public int getGoodsId() {
		return goodsId;
	}
	
	public int getCount() {
		return count;
	}
}
