package com.stars.modules.hotUpdate;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.hotUpdate.event.HotUpdateAddItemEvent;
import com.stars.modules.hotUpdate.event.HotUpdateDeleteItemEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class HotUpdateModule extends AbstractModule {

    public HotUpdateModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("HotUpdate", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {

    }

    @Override
    public void onCreation(String name, String account) throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    @Override
    public void onFiveOClockReset(Calendar now) throws Throwable {
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
    }

    @Override
    public void onMonthlyReset() throws Throwable {
    }

    @Override
    public void onReconnect() throws Throwable {
    }

    @Override
    public void onOffline() throws Throwable {
    }

    @Override
    public void onExit() throws Throwable {
    }

    @Override
    public void onTimingExecute() {
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
    }

    public void handleAddItemEvent(HotUpdateAddItemEvent event){
        if(event == null) return;
        Map<Integer,Integer> itemMap = event.getAwardMap();
        if(StringUtil.isEmpty(itemMap)) return;

        if(event.getType() == HotUpdateConstant.DIRECTLY){
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(itemMap);
        }else{
            ServiceHelper.emailService().sendToSingle(id(),
                    event.getMailTemplateId(),id(), "系统",itemMap);
        }

        com.stars.util.LogUtil.info("HotUpdateAddItemEvent|"+event.getLogSignal()+"|"+id()+"|"+
                StringUtil.makeString(itemMap,'=',','));
    }

    public void handleDeleteItemEvent(HotUpdateDeleteItemEvent event){
        if(event == null) return;
        Map<Integer,Integer> itemMap = event.getAwardMap();
        if(StringUtil.isEmpty(itemMap)) return;

        Map<Integer,Integer> realDeleteMap = new HashMap<>();

        ToolModule toolModule = module(MConst.Tool);
        if(toolModule.contains(itemMap)) {
            toolModule.deleteAndSend(itemMap);
            realDeleteMap.putAll(itemMap);
        }else{
            long hasCount;
            for(Map.Entry<Integer,Integer> entry:itemMap.entrySet()){
                if(entry == null) continue;
                hasCount = toolModule.getCountByItemId(entry.getKey());
                if(hasCount <= 0) continue;
                if(hasCount >= entry.getValue()){
                    realDeleteMap.put(entry.getKey(),entry.getValue());
                }else{
                    realDeleteMap.put(entry.getKey(),(int)hasCount);
                }
            }
            toolModule.deleteAndSend(realDeleteMap);
        }

        com.stars.util.LogUtil.info("handleDeleteItemEvent|"+event.getLogSignal()+"|"+id()+"|"+
                StringUtil.makeString(itemMap,'=',',')+"|"+
                StringUtil.makeString(realDeleteMap,'=',','));
    }

    public void handleBaseEvent(Object[] data){
        if(data == null) return;
        try{



        }catch (Exception e){
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    public void handleCommEvent(Object[] data){
        if(data == null) return;
        try{



        }catch (Exception e){
            com.stars.util.LogUtil.error(e.getMessage(), e);
        }
    }

    public void handleStandyEvent(Object[] data){
        if(data == null) return;
        try{



        }catch (Exception e){
            LogUtil.error(e.getMessage(), e);
        }
    }
}
