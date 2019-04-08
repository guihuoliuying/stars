package com.stars.services.guest;

import com.stars.core.event.Event;

/**
 * Created by zhouyaohui on 2017/1/11.
 */
public class GuestExchangeEvent extends Event {

    private boolean result;
    private int itemId;

    public GuestExchangeEvent(boolean result, int itemId) {
        this.result = result;
        this.itemId = itemId;
    }

    public boolean isResult() {
        return result;
    }

    public int getItemId() {
        return itemId;
    }
}
