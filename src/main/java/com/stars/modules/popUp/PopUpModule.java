package com.stars.modules.popUp;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.popUp.packet.ClientPopUp;
import com.stars.modules.popUp.prodata.PopUpInfo;
import com.stars.modules.popUp.userdata.RolePopUp;
import com.stars.modules.push.event.PushLoginDoneEvent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class PopUpModule extends AbstractModule {

    private RolePopUp rolePopUp;

    public PopUpModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("PopUp", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `rolepopup` where `roleid` = " + id();
        rolePopUp = DBUtil.queryBean(DBUtil.DB_USER, RolePopUp.class, sql);
        if (rolePopUp == null) {
            rolePopUp = new RolePopUp(id());
            context().insert(rolePopUp);//添加插入语句
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        rolePopUp = new RolePopUp(id());
        context().insert(rolePopUp);//添加插入语句
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
    	if(!isCreation){
    		if(rolePopUp == null) return;
    		rolePopUp.weeklyReset();
    		context().update(rolePopUp);
    	}
    }
    
    @Override
    public void onTimingExecute() {
    	if(rolePopUp == null) return;
		rolePopUp.weeklyReset();
		context().update(rolePopUp);
    }

    @Override
    public void onSyncData() throws Throwable {
//        checkAndPopUp(true,null);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if(rolePopUp == null) return;
        rolePopUp.dailyReset();
        context().update(rolePopUp);
    }

    public void forbidden(int popId){
        if(rolePopUp == null) return;
        rolePopUp.addToForbidden(popId);
        context().update(rolePopUp);
    }

    public void recordPopUp(int popId){
        if(rolePopUp == null) return;
        PopUpInfo popUpInfo = PopUpManager.getPopUpInfoById(popId);
        if(popUpInfo == null || popUpInfo.getFrequency() == PopUpConstant.FREQUENCY_TYPE_EVERY_TIMES) return;
        rolePopUp.addToForbidden(popId);
        context().update(rolePopUp);
    }

    public void checkAndPopUp(boolean login,List<String> sysName){
        if(rolePopUp == null) return;
        List<PopUpInfo> popUpList = new ArrayList<>();
        long now = System.currentTimeMillis();
        for(PopUpInfo popUpInfo : PopUpManager.getPopUpInfoMaps().values()){
            if(!popUpInfo.matchTimes(now)) continue;                    //时间限制
            if(rolePopUp.isForbidden(popUpInfo.getPopUpId())) continue; //今日已禁用
            if((login && popUpInfo.isLoginCheck())                      //登陆触发限制
                    || popUpInfo.matchSysName(sysName)){                //开启系统触发
                if(popUpInfo.checkCondition(moduleMap())){              //条件检测
                    popUpList.add(popUpInfo);
                }
            }
        }

        if(StringUtil.isEmpty(popUpList)) return;
        Collections.sort(popUpList);//排序

        for(PopUpInfo info:popUpList){
            recordPopUp(info.getPopUpId());
        }
        LogUtil.info("弹脸数据|{}", popUpList);
        ClientPopUp clientPopUp = new ClientPopUp(ClientPopUp.RESP_POPUP);
        clientPopUp.setPopUpList(popUpList);
        send(clientPopUp);
    }

    public void onEvent(Event event) {
        if (event instanceof PushLoginDoneEvent) {
            checkAndPopUp(true, null);
        }
    }
}
