package com.stars.modules.sendvigour;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.data.DataManager;
import com.stars.modules.sendvigour.event.SendVigourActEvent;
import com.stars.modules.sendvigour.listener.SendVigourActEventListener;

import java.util.*;

/**
 * Created by gaopeidian on 2016/10/8.
 */
public class SendVigourModuleFactory extends AbstractModuleFactory<SendVigourModule> {

    public SendVigourModuleFactory() {
        super(new SendVigourPacketSet());
    }

    @Override
    public SendVigourModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new SendVigourModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(SendVigourActEvent.class, new SendVigourActEventListener(module));
    }

    @Override
    public void loadProductData() throws Exception {
        loadConfig();
    }

    private void loadConfig() throws Exception {
        Map<Integer, String> configMap = DataManager.getActivityFlowConfig(SendVigourManager.ACTIVITY_FLOW_ID);
        SendVigourActivityFlow flow = new SendVigourActivityFlow();
        flow.init(SchedulerHelper.getScheduler(), configMap);

        //拿出配置的step list,排序并存放进map中
        List<Integer> stepList = new ArrayList<Integer>();
        Set<Integer> keySet = configMap.keySet();
        for (Integer stepId : keySet) {
			stepList.add(stepId);
		}
        Collections.sort(stepList);     
        LinkedHashMap map = new LinkedHashMap();
        int size = stepList.size();
    	for (int i = 0; i < size - 1; i += 2) {
			int startStep = stepList.get(i);
			int endStep = stepList.get(i + 1);
			if (startStep % 2 == 1 && endStep % 2 == 0) {
				map.put(startStep, endStep);
			}
		}       
        SendVigourManager.STEP_MAP = map;
        
        SendVigourManager.SEND_VIGOUR = DataManager.getCommConfig("daily_vigor", 0);
    }
}
