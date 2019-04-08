package com.stars.modules.skytower;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.skytower.event.FinishSkyTowerLayerEvent;
import com.stars.modules.skytower.packet.ClientSkyTowerInfo;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.modules.skytower.recordmap.RecordMapSkyTower;
import com.stars.modules.tool.ToolModule;
import com.stars.util.DateUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 镇妖塔模块;
 * Created by panzhenfeng on 2016/8/10.
 */
public class SkyTowerModule extends AbstractModule {
    private RecordMapSkyTower recordMapSkyTower = null;

    public SkyTowerModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("zhenyaota", id, self, eventDispatcher, moduleMap);
    }

    private void initRecordMap() throws SQLException {
        recordMapSkyTower = new RecordMapSkyTower(this, context());
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        updateRedPoints();
    }

    public void updateRedPoints(){
        signCalRedPoint(MConst.SkyTower, RedPointConst.SKYTOWER_DAILYREWARD_CANGET);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if(redPointIds.contains((RedPointConst.SKYTOWER_DAILYREWARD_CANGET))){
            boolean isCanGetAward = recordMapSkyTower.isCanGetDayAward();
            if(isCanGetAward){
                redPointMap.put(RedPointConst.SKYTOWER_DAILYREWARD_CANGET, String.valueOf(recordMapSkyTower.getDayRewardLayerId()));
            }else {
                redPointMap.put(RedPointConst.SKYTOWER_DAILYREWARD_CANGET, null);
            }
        }
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        String roleId = Long.toString(roleModule.getRoleRow().getRoleId());
        initRecordMap();
    }

    @Override
    public void onDataReq() throws Exception {
        initRecordMap();
    }


    @Override
    public void onSyncData() {

    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        //获取当前所处最高层，用于更新奖励;
        int curLayerId = recordMapSkyTower.getCurLayerId();
        //如果一天都没有通关,那么给玩家加个失败奖励，失败奖励的获取，是从当前所在layerId往-1开始数，取最近的一个的失败奖励;
        if(isOpenedChallengeRewardLimit() && getRecordMapSkyTower().isCanAddFailedAward()){
            SkyTowerVo skyTowerVo = recordMapSkyTower.getPrestFailAwardSkyTowerVo(curLayerId);
            if(skyTowerVo != null){
                getRecordMapSkyTower().addFailChallengeAwardLayerId(skyTowerVo.getLayerId());
            }
            if(recordMapSkyTower.isCurFailChallengeAwardValid()){
                //将今天的奖励放置到之前的奖励字段，以供领取;
                recordMapSkyTower.setCurFailChallengeToPre();
            }
        }
        recordMapSkyTower.setPreDayMaxlayerId(curLayerId);
        recordMapSkyTower.setPreDayMaxLayerIsPass(recordMapSkyTower.getCurLayerIsPass());
        //清空当天的失败奖励池;
        recordMapSkyTower.setCurFailChallengeAwardLayerIds("0");
        //设置当前的每日奖励;
        recordMapSkyTower.setDailyAwardLayerId(curLayerId);
        syncToClientSkyTowerInfo();
        updateRedPoints();
    }

    @Override
    public void onTimingExecute() {
        //检查是否可以重置重置九层高塔次数
        long thisWeekRestTime = recordMapSkyTower.getThisWeekResetLayerTime();
        long now = System.currentTimeMillis();
        long lastResetTime = recordMapSkyTower.getLastResetLayerTime();
        long lastWeekResetTime = thisWeekRestTime - DateUtil.DAY*7;
        //本周重置时间已过，同时玩家上次重置时间比本周重置早  或者  本周重置时间未到，但是玩家比上周重置时间还早
        if((thisWeekRestTime < now && thisWeekRestTime > lastResetTime)||(thisWeekRestTime > now && lastResetTime < lastWeekResetTime)){
            recordMapSkyTower.setWeeklyResetLayerCount(0);
        }
    }

    public void syncToClientSkyTowerInfo(){
        //通知服务端镇妖塔数据的变化;
        ClientSkyTowerInfo clientSkyTowerInfo = new ClientSkyTowerInfo(getRecordMapSkyTower());
        send(clientSkyTowerInfo);
    }

    public RecordMapSkyTower getRecordMapSkyTower() {
        return recordMapSkyTower;
    }

    /**
     * 判断是否首次通关奖励;
     * @param stageId
     * @return
     */
    public boolean isFirstPass(int stageId){
        if(recordMapSkyTower.getCurLayerId()==stageId && recordMapSkyTower.getCurLayerIsPass()==0){
            return true;
        }
        return false;
    }

    public void checkDailyEvent(){
        if(getRecordMapSkyTower().isTodayFirstIn()){
            getRecordMapSkyTower().setTodayStr();
            // 抛出日常活动事件
            eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_SKYTOWER, 1));
        }
    }

    public void fireEvent(Event event){
        eventDispatcher().fire(event);
    }

    /**
     * 是否允许跳转到某一层;
     * @return
     */
    public boolean isCanJumpToLayer(int layerId){
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(layerId);
        RoleModule roleModule = (RoleModule)this.module(MConst.Role);
        //判断等级是否达到;
        if (skyTowerVo.getLevellimit() > roleModule.getLevel()){
            return false;
        }
        //判断战力是否足够;
        if(skyTowerVo.getFightscore() > roleModule.getRoleRow().getFightScore()){
            return  false;
        }
        return true;
    }

    /**
     * 是否开启挑战奖励的限制;
     * @return
     */
    public boolean isOpenedChallengeRewardLimit(){
        //挑战奖励的开启等级;
        int challengeOpenLevel = Integer.parseInt(DataManager.getCommConfig("skytower_challengereward_level"));
        RoleModule roleModule = (RoleModule)this.module(MConst.Role);
        if(roleModule.getLevel()>=challengeOpenLevel){
            return  true;
        }
        return false;
    }

    /**
     * 请求领取奖励:
     * @param type SkyTowerConstant里的REQUEST_INFO_GET_AWARD_DAY...
     */
    public void requestGetAwards(byte type){
        Map<Integer, Integer> rewardMap = null;
        switch (type){
            case SkyTowerConstant.REQUEST_INFO_GET_AWARD_DAY:
                //判断是否有每日奖励可以领取;
                rewardMap = recordMapSkyTower.getDayAwardMap();
                if(rewardMap != null){
                    ToolModule toolModule = (ToolModule)this.module(MConst.Tool);
                    toolModule.addAndSend(rewardMap, MConst.CCSkyTower, EventType.SKYTOWER.getCode());
                    //清掉镇妖塔的每日奖励记录;
                    recordMapSkyTower.setDailyAwardLayerId(0);
                    syncToClientSkyTowerInfo();
                }
                break;
            case SkyTowerConstant.REQUEST_INFO_GET_AWARD_CHALLENGE:
                //判断是否有挑战奖励可以领取;
                rewardMap = recordMapSkyTower.getPreFailChallengeAwardMap();
                if(rewardMap != null){
                    ToolModule toolModule = (ToolModule)this.module(MConst.Tool);
                    toolModule.addAndSend(rewardMap, MConst.CCSkyTower,EventType.SKYTOWER.getCode());
                    //清掉镇妖塔的挑战奖励记录;
                    recordMapSkyTower.removeAllPreChallengeAwardLayerId();
                    syncToClientSkyTowerInfo();
                }
                break;
            case SkyTowerConstant.REQUEST_RESET_LAYER:
                //请求重置九层塔层数
                int remainTime = recordMapSkyTower.getRemainResetLayerCount();
                if (remainTime <= 0){
                    return;
                }
                int currentLayer = recordMapSkyTower.getCurLayerId();
                SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(currentLayer);
                if (skyTowerVo == null)
                    return;
                int newCurrentLayerSerialId = skyTowerVo.getResetlayer();
                SkyTowerVo newSkyTowerVo = SkyTowerManager.getSkyTowerByLayerSerialId(newCurrentLayerSerialId);
                if (newSkyTowerVo == null)
                    return;
                int newCurrentLayer = newSkyTowerVo.getLayerId();
                recordMapSkyTower.setCurLayerId(newCurrentLayer);
                recordMapSkyTower.addWeeklyResetLayerCount(); //重置记录+1
                syncToClientSkyTowerInfo();
                break;
        }
    }

    public void syncSkyTowerRank(int curLayerId) {
        int tmpLayerId = curLayerId - 1;
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(tmpLayerId);
        if(skyTowerVo != null){
            eventDispatcher().fire(new FinishSkyTowerLayerEvent(this.id(), skyTowerVo.getLayerSerial()));
        }else{
            eventDispatcher().fire(new FinishSkyTowerLayerEvent(this.id(), 0));
        }
    }

    public int getSkyTowerLayerSerial() {
        int tmpLayerId = 0;
        if(recordMapSkyTower.getHistoryMaxLayerId() == SkyTowerManager.getMaxLayerId() && recordMapSkyTower.getCurLayerIsPass() == (byte)1){
            tmpLayerId = SkyTowerManager.getMaxLayerId(); //已通过最高层
        }else{
            tmpLayerId = recordMapSkyTower.getHistoryMaxLayerId() - 1;
        }
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(tmpLayerId);
        if (skyTowerVo != null) {
            return skyTowerVo.getLayerSerial();
        } else {
            return 0;
        }
    }
}
