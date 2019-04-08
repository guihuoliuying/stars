package com.stars.modules.drop.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.email.event.RequestSendSingleEmailEvent;
import com.stars.modules.loottreasure.packet.ClientLootTreasureInfo;
import com.stars.services.ServiceHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**监听通过消息请求发送邮件;
 * Created by panzhenfeng on 2016/10/28.
 */
public class RequestSendSingleEmailListener  extends AbstractEventListener<Module> {
    public RequestSendSingleEmailListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        if(!(this.module() instanceof  DropModule)){
            return;
        }
        DropModule dropModule = (DropModule)this.module();
        RequestSendSingleEmailEvent requestSendSingleEmailEvent = (RequestSendSingleEmailEvent) event;
        long receiveRoleId = requestSendSingleEmailEvent.getReceiveRoleId();
        int templateId = requestSendSingleEmailEvent.getTemplateId();
        long sendId = requestSendSingleEmailEvent.getSendId();
        String sendName = requestSendSingleEmailEvent.getSendName();
        Map<Integer, Integer> affixMap = requestSendSingleEmailEvent.getAffixMap();
        Map<Integer, Integer> resultMap;
        //根据自定义类型做处理;
        switch (requestSendSingleEmailEvent.getCurstomType()){
            case MConst.CCLootTreasure:
                Map<Integer, Integer> tmpMap;
                resultMap = new ConcurrentHashMap<>();
                int itemId;
                int itemCount;
                for(Map.Entry<Integer, Integer> kvp : affixMap.entrySet()){
                    tmpMap = dropModule.executeDrop(kvp.getKey(), kvp.getValue(),false);
                    for(Map.Entry<Integer, Integer> itemKvp: tmpMap.entrySet()){
                        itemId = itemKvp.getKey();
                        itemCount = itemKvp.getValue();
                        if(resultMap.containsKey(itemId)){
                            resultMap.put(itemId, resultMap.get(itemId)+itemCount);
                        }else{
                            resultMap.put(itemId, itemCount);
                        }
                    }
                }
                //要发送到客户端进行奖励预览;
                ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_END_AWARDS);
                clientLootTreasureInfo.setRewardMap(resultMap);
                dropModule.send(clientLootTreasureInfo);
                break;
            default:
                resultMap = requestSendSingleEmailEvent.getAffixMap();
                break;
        }
        //邮件发送;
        ServiceHelper.emailService().sendToSingle(receiveRoleId, templateId, sendId, sendName, resultMap);
    }
}