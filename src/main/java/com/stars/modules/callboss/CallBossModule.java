package com.stars.modules.callboss;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.callboss.prodata.CallBossVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.CallBossScene;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.I18n;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liuyuheng on 2016/9/2.
 */
public class CallBossModule extends AbstractModule {
    public CallBossModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("召唤boss", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
    	//红点检查
        signCalRedPoint(MConst.CallBoss, RedPointConst.ALIVE_CALL_BOSS);
    }
    
    @Override
    public void onOffline() throws Throwable {
        SceneModule sceneModule = (SceneModule)module(MConst.Scene);
        Scene scene = sceneModule.getScene();
        if (scene != null && scene instanceof CallBossScene) {
			scene.exit(moduleMap());
		}
    }
    
    /**
     * 召唤boss请求,必须先进行扣除消耗判断
     * 一些产品数据判断在这里处理
     *
     * @param bossId
     * @param rewardGroupId 召唤者选择奖励组id
     */
    public void callBoss(int bossId, byte rewardGroupId) {
        CallBossVo callBossVo = CallBossManager.getCallBossVo(bossId);
        if (callBossVo == null)
            return;
        if (!callBossVo.getSelectRewardMap().containsKey(rewardGroupId))
            return;
        // 是否在指定日期&时间
        if (!callBossVo.getWeekDayList().contains(DateUtil.getChinaWeekDay())
                || System.currentTimeMillis() < DateUtil.hourStrTimeToDateTime(CallBossManager.startTime).getTime()
                || System.currentTimeMillis() > DateUtil.hourStrTimeToDateTime(CallBossManager.endTime).getTime()) {
            warn(I18n.get("callboss.errorTime"));
            return;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        // 角色等级
        if (roleModule.getLevel() < callBossVo.getLevelLimit()) {
            warn(I18n.get("callboss.roleLvlNotEnough"));
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        // 消耗判断
        if (!toolModule.contains(callBossVo.getCostMap())) {
            warn(I18n.get("callboss.costNotEnough"));
            return;
        }
        // 召唤boss(同步),返回成功扣除消耗道具
        boolean callResult = ServiceHelper.callBossService().executeCallBoss(id(), roleModule.getRoleRow().getName(),
                bossId, rewardGroupId);
        if (callResult)
            toolModule.deleteAndSend(callBossVo.getCostMap(), EventType.CALLBOSS.getCode());
        if (SpecialAccountManager.isSpecialAccount(id())){
            eventDispatcher().fire(new SpecialAccountEvent(id(), "请求召唤boss数据", true));
        }
    }
    
    public void callBossStatusChange(){
    	//红点检查
        signCalRedPoint(MConst.CallBoss, RedPointConst.ALIVE_CALL_BOSS);
    }
    
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if(redPointIds.contains(Integer.valueOf(RedPointConst.ALIVE_CALL_BOSS))){
        	Set<Integer> aliveBossSet =  ServiceHelper.callBossService().getAliveBossIds(id());
            checkRedPoint(redPointMap, aliveBossSet, RedPointConst.ALIVE_CALL_BOSS);
        }
    }
    
    private void checkRedPoint(Map<Integer, String> redPointMap,Set<Integer> list,int redPointConst){
        StringBuilder builder = new StringBuilder("");
        if(!list.isEmpty()){
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()){
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst,builder.toString().isEmpty() ? null : builder.toString());
        }
        else {
            redPointMap.put(redPointConst, null );
        }
    }
}
