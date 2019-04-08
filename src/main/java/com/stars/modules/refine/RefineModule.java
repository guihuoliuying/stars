package com.stars.modules.refine;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.push.conditionparser.CondUtils;
import com.stars.modules.refine.cache.RoleRefine;
import com.stars.modules.refine.packet.ClientRefine;
import com.stars.modules.refine.prodata.RefineVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-08-02.
 */
public class RefineModule extends AbstractModule {
    private Map<Integer, RoleRefine> roleRefineMap;

    public RefineModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        roleRefineMap = new HashMap<>();
    }

    @Override
    public void onSyncData() throws Throwable {

    }

    public void view() {
        if (!RefineManager.isOpen) {
            warn("杂物回收功能暂时关闭");
            return;
        }
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.REFINE)) {
            warn("功能暂未开放");
            return;
        }
        roleRefineMap = new HashMap<>();
        ToolModule tool = module(MConst.Tool);
        for (Map.Entry<Integer, RefineVo> entry : RefineManager.refineVoMap.entrySet()) {
            int count = (int) tool.getCountByItemId(entry.getKey());
            if (count >= 1 && CondUtils.isTrue(entry.getValue().getCondChecker(), moduleMap())) {
                RoleRefine roleRefine = new RoleRefine(id());
                roleRefine.setItemId(entry.getKey());
                roleRefine.setCount(count);
                roleRefineMap.put(entry.getKey(), roleRefine);
            }
        }
        sendToClient();
    }

    private void sendToClient() {
        ClientRefine clientRefine = new ClientRefine();
        clientRefine.setRoleRefineMap(roleRefineMap);
        send(clientRefine);
    }

    public void refine(int itemId, int count) {
        if (!RefineManager.isOpen) {
            warn("杂物回收功能暂时关闭");
            return;
        }
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.REFINE)) {
            warn("功能暂未开放");
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (!roleRefineMap.containsKey(itemId)) {
            warn("不符合回收条件:" + itemId);
            return;
        }
        if (tool.deleteAndSend(itemId, count, EventType.REFINE.getCode())) {
            RoleRefine roleRefine = roleRefineMap.get(itemId);
            roleRefine.setCount((int) tool.getCountByItemId(itemId));
            if (roleRefine.getCount() == 0) roleRefineMap.remove(itemId);
            Map<Integer, Integer> itemMap = new HashMap<>(RefineManager.refineVoMap.get(itemId).getOutputItemMap());
            MapUtil.multiply(itemMap, count);
            tool.addAndSend(itemMap, EventType.REFINE.getCode());
            ClientAward clientAward = new ClientAward();
            clientAward.setAwrd(itemMap);
            send(clientAward);
            sendToClient();
            ServerLogModule logModule = module(MConst.ServerLog);
            StringBuilder sb = new StringBuilder();
            sb.append("recycle_id@number:").append(itemId).append("@").append(count).append("#")
                    .append("essence:").append(ServerLogModule.itemMapStr(itemMap));
            logModule.dynamic_4_Log_for_Baby_or_Refine(ThemeType.DYNAMIC_REFINE.getThemeId(), "recycle", sb.toString());
        } else {
            warn("道具不足");
        }
    }

//    public void doToolChangeEvent(int itemId) {
//        if (!RefineManager.refineVoMap.containsKey(itemId)) {
//            return;
//        }
//        if (!CondUtils.isTrue(RefineManager.refineVoMap.get(itemId).getCondChecker(), moduleMap())) {
//            return;
//        }
//        ToolModule tool = module(MConst.Tool);
//        RoleRefine roleRefine = roleRefineMap.get(itemId);
//        if (roleRefine == null) {
//            roleRefine = new RoleRefine(id());
//            roleRefine.setItemId(itemId);
//            roleRefineMap.put(itemId, roleRefine);
//        }
//        roleRefine.setCount((int) tool.getCountByItemId(itemId));
//    }
//
//    public void doToolChangeEvent(Set<Integer> itemIds) {
//        for (int itemId : itemIds) {
//            doToolChangeEvent(itemId);
//        }
//    }
}
