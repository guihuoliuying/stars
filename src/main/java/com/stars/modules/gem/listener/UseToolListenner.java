package com.stars.modules.gem.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.modules.gem.GemModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.tool.productdata.ItemVo;

/**
 * Created by huwenjun on 2017/6/12.
 */
public class UseToolListenner extends AbstractEventListener<GemModule> {
    public UseToolListenner(GemModule module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        UseToolEvent useToolEvent = (UseToolEvent) event;
        //判断是否是宝石, 是的话，下发宝石的数据到客户端GemLevelVo;
        ItemVo itemVo = ToolManager.getItemVo(useToolEvent.getItemId());
        if (itemVo.getType() == ToolManager.TYPE_DIAMOND) {
            module().updateRedPoints();
        }
    }
}
