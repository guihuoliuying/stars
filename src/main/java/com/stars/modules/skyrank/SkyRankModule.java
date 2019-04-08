package com.stars.modules.skyrank;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.skyrank.event.SkyRankDailyAwardEvent;
import com.stars.modules.skyrank.event.SkyRankGradAwardEvent;
import com.stars.modules.skyrank.event.SkyRankLogEvent;
import com.stars.modules.skyrank.event.SkyRankScoreHandleEvent;
import com.stars.modules.skyrank.packet.ClientSkyRankGradAwardData;
import com.stars.modules.skyrank.prodata.SkyRankGradVo;
import com.stars.modules.skyrank.prodata.SkyRankSeasonGradAwardVo;
import com.stars.modules.skyrank.prodata.SkyRankUpAwardVo;
import com.stars.modules.tool.ToolModule;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 天梯系统
 *
 * @author xieyuejun
 */
public class SkyRankModule extends AbstractModule {

    // FIXME 战力更新 名字更改都要同步更新

    public SkyRankModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }
    
    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	RoleModule rm = module(MConst.Role);
    	ServiceHelper.skyRankLocalService().RoledailyReset(id(), rm.getRoleRow().getName(), rm.getRoleRow().getFightScore());
    }
    
    @Override
    public void onInit(boolean isCreation) throws Throwable {
    	signCalRedPoint(MConst.SkyRank, RedPointConst.SKYRANK_DAILY_AWARD);
//        RoleModule roleModule = module(MConst.Role);
//        Role roleRow = roleModule.getRoleRow();
//        ServiceHelper.skyRankLocalService().checkRankGradeWhileLogin(
//                id(), roleRow.getName(), roleRow.getFightScore());
    }

    public void onEvent(Event event) {
        if (event instanceof SkyRankGradAwardEvent) {
            sendGradUpAward(((SkyRankGradAwardEvent) event).getNewRGV());
        }
        if (event instanceof SkyRankScoreHandleEvent) {
            try {
                RoleModule rm = module(MConst.Role);
                ServiceHelper.skyRankLocalService().handleScoreChange(
                        new SkyRankScoreHandle(id(), ((SkyRankScoreHandleEvent) event).getFightType(),
                                ((SkyRankScoreHandleEvent) event).getIsWin(), rm.getRoleRow().getName(), rm.getRoleRow()
                                .getFightScore()));
            } catch (Throwable e) {
                com.stars.util.LogUtil.error(e.getMessage(), e);
            }
        }
        if (event instanceof SkyRankLogEvent) {
            SkyRankLogEvent skyRankLogEvent = (SkyRankLogEvent) event;
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            serverLogModule.log_skyRank(skyRankLogEvent.getInfo());
        }
        if (event instanceof RoleRenameEvent) {
            String newName = ((RoleRenameEvent) event).getNewName();
            RoleModule rm = module(MConst.Role);
            ServiceHelper.skyRankLocalService().updateRoleName(
                    id(), newName, rm.getRoleRow()
                            .getFightScore());
        }
        if (event instanceof SkyRankDailyAwardEvent){
        	signCalRedPoint(MConst.SkyRank, RedPointConst.SKYRANK_DAILY_AWARD);
        }
    }

    /**
     * 发送段位提升奖励
     */
    public void sendGradUpAward(SkyRankGradVo newRGV) {
        SkyRankUpAwardVo upAward = SkyRankManager.getManager().getSkyRankUpAwardVo(newRGV.getSkyRankGradId());
        if (upAward != null) {
            int dropId = upAward.getDropId();
            DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer, Integer> toolMap = null;
            if (dropId > 0) {
                LogUtil.info("sendGradUpAward " + id() + "|grad=" + newRGV.getSkyRankGradId());
                toolMap = dropModule.executeDrop(dropId, 1, false);
                toolModule.addAndSend(toolMap, EventType.SKYRANK_UPGRAD.getCode());
                ClientSkyRankGradAwardData clientSkyRankGradAwardData = new ClientSkyRankGradAwardData();
                clientSkyRankGradAwardData.setOpType(ClientSkyRankGradAwardData.GRAD_UP_AWARD);
                clientSkyRankGradAwardData.setAwarToolMap(toolMap);
                com.stars.network.server.packet.PacketManager.send(id(), clientSkyRankGradAwardData);
            }
        }
    }

    public void sendSeasonGradAward(SkyRankGradVo newRGV) {
        SkyRankSeasonGradAwardVo upAward = SkyRankManager.getManager().getSkyRankSeasonGradAwardVo(newRGV.getSkyRankGradId());
        if (upAward != null) {


            int dropId = upAward.getDropId();
            DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer, Integer> toolMap = null;
            if (dropId > 0) {
                toolMap = dropModule.executeDrop(dropId, 1, false);
                toolModule.addAndSend(toolMap, EventType.SKYRANK_SEASON_GRAD_AWARD.getCode());
                ClientSkyRankGradAwardData clientSkyRankGradAwardData = new ClientSkyRankGradAwardData();
                clientSkyRankGradAwardData.setOpType(ClientSkyRankGradAwardData.GRAD_UP_AWARD);
                clientSkyRankGradAwardData.setAwarToolMap(toolMap);
                PacketManager.send(id(), clientSkyRankGradAwardData);
            }
        }
    }
    
    public void getDailyAward(){
    	RoleModule rm = module(MConst.Role);
    	Role roleRow = rm.getRoleRow();
    	int dailyAwardId = ServiceHelper.skyRankLocalService().getDailyAward(id(), roleRow.getName(), roleRow.getFightScore());
    	ClientSkyRankGradAwardData packet = new ClientSkyRankGradAwardData();
    	packet.setOpType(ClientSkyRankGradAwardData.GET_DAILY_AWARD);
    	if(dailyAwardId>0){    		
    		DropModule dropModule = module(MConst.Drop);
    		ToolModule toolModule = module(MConst.Tool);
    		Map<Integer, Integer> awardMap = dropModule.executeDrop(dailyAwardId, 1, true);
    		toolModule.addAndSend(awardMap, EventType.SKYRANK_DAILY_AWARD.getCode());
    		packet.setGetResult((byte)1);
    	}
    	send(packet);
    	signCalRedPoint(MConst.SkyRank, RedPointConst.SKYRANK_DAILY_AWARD);
    }
    
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
    	if(redPointIds.contains(Integer.valueOf(RedPointConst.SKYRANK_DAILY_AWARD))){
    		checkDailyAwardRedPoint(redPointMap);
    	}
    }
    
    private void checkDailyAwardRedPoint(Map<Integer, String> redPointMap){
    	RoleModule rm = module(MConst.Role);
    	Role roleRow = rm.getRoleRow();
    	Object[] awardStateInfo = ServiceHelper.skyRankLocalService().getDailyAwardState(id(), roleRow.getName(), roleRow.getFightScore());
    	int awardId = (Integer)awardStateInfo[0];
    	byte awardState = (Byte)awardStateInfo[1];
    	if(awardId>0&&awardState==0){
    		redPointMap.put(RedPointConst.SKYRANK_DAILY_AWARD, "");
    	}else{
    		redPointMap.put(RedPointConst.SKYRANK_DAILY_AWARD, null);
    	}
    }

}
