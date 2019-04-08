package com.stars.modules.book.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.book.BookModule;
import com.stars.modules.tool.event.AddToolEvent;

import java.util.Map;

/**
 * Created by zhoujin on 2017/5/15.
 */
public class GetToolBookListener extends AbstractEventListener<BookModule> {
    public GetToolBookListener(BookModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AddToolEvent ate = (AddToolEvent)event;
        Map<Integer,Integer> toolMap = ate.getToolMap();
        if (toolMap == null || toolMap.size() <= 0) {
            return;
        }
        module().signCalBookRedPoint();//计算红点
    }
}
