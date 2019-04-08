package com.stars.modules.gem.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.packet.ClientGemTishenVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.productdata.ItemVo;

import java.util.Iterator;
import java.util.Map;

/**
 * 获取物品的监听器;
 * Created by panzhenfeng on 2016/7/29.
 */
public class GetToolGemListener extends AbstractEventListener<Module> {

    public GetToolGemListener(Module tm){
        super(tm);
    }

    @Override
    public void onEvent(Event event) {
        AddToolEvent ate = (AddToolEvent)event;
        Map<Integer,Integer> toolMap = ate.getToolMap();
        if (toolMap == null || toolMap.size() <= 0) {
            return;
        }
        Iterator<Integer> it = toolMap.keySet().iterator();
        GemModule gemModule = (GemModule)this.module();
        ItemVo itemVo ;
        int id;
        ClientGemTishenVo clientGemTishenVo = new ClientGemTishenVo();
        while (it.hasNext()) {
            id = it.next();
            //判断是否是宝石, 是的话，下发宝石的数据到客户端GemLevelVo;
            itemVo = ToolManager.getItemVo(id);
            if(itemVo.getType()==ToolManager.TYPE_DIAMOND){
                gemModule.updateRedPoints();
                clientGemTishenVo.addGemLevelVo(id);
            }
        }
        gemModule.send(clientGemTishenVo);
    }

}