package com.stars.modules.tool.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.newequipment.NewEquipmentConstant;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.fightingmaster.event.NoticeMainServerAddTool;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/11/22.
 */
public class NoticeMainServerAddToolListener extends AbstractEventListener {
    public NoticeMainServerAddToolListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        NoticeMainServerAddTool tool = (NoticeMainServerAddTool) event;
        ToolModule toolModule = (ToolModule) module();
        Map<Integer, Integer> map = toolModule.addAndSend(tool.getItemMap(), EventType.NOTICEMAINSERVER.getCode());
        
        //勋章产出加成
        int validMedalId = tool.getValidMedalId();
        if (validMedalId != -1) {
        	Map<Integer, Integer> addReward = NewEquipmentManager.calMedalAddReward(NewEquipmentConstant.FightingMaster_AddProduce_TargetId, validMedalId, map);
			if (addReward != null && addReward.size() > 0) {
				toolModule.addAndSend(addReward, EventType.NOTICEMAINSERVER.getCode());
			}
		}
    }
}
