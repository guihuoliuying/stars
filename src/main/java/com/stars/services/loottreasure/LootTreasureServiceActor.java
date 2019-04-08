package com.stars.services.loottreasure;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.SchedulerHelper;
import com.stars.bootstrap.ServerManager;
import com.stars.core.activityflow.ActivityFlow;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.modules.data.DataManager;
import com.stars.modules.loottreasure.LootTreasureActivityFlow;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.event.NotifyEnterLootTreasureEvent;
import com.stars.modules.loottreasure.packet.AttendLootTreasure;
import com.stars.modules.loottreasure.packet.ClientLootTreasureEnterBack;
import com.stars.modules.loottreasure.packet.ClientLootTreasureRankBack;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.modules.pk.packet.ModifyConnectorRoute;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.event.RequestExitFightEvent;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.multiserver.LootTreasure.LTDamageRank;
import com.stars.multiserver.LootTreasure.LTDamageRankVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.role.RoleNotification;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 野外夺宝服务;
 * Created by panzhenfeng on 2016/10/10.
 */
public class LootTreasureServiceActor extends ServiceActor implements LootTreasureService {

    private LootTreasureActivityFlow lootTreasureActivityFlow;
    private Map<String, LTDamageRank> ltDamageRankMap = new ConcurrentHashMap<>();
    private Map<String, List<Long>> lootSectionRoleMap = new ConcurrentHashMap<>();
    private int serverId;
    private String serverName;

    private int rmLTServerId;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.LootTreasureService, this);
        //开启野外活动Flow;
        lootTreasureActivityFlow = new LootTreasureActivityFlow();
        lootTreasureActivityFlow.init(SchedulerHelper.getScheduler(), DataManager.getActivityFlowConfig(2));
        serverId = ServerManager.getServer().getConfig().getServerId();
//        serverName = ServerManager.getServerName();
        serverName = MultiServerHelper.getServerName();
        Properties p = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.LOOTTREASURE);
        rmLTServerId = Integer.parseInt(p.getProperty("serverId"));
    }

    @Override
    public void printState() {

    }

    public void requestAttend(FighterEntity fEntity, int jobId) {
        LootSectionVo lootSectionVo = LootTreasureManager.getLootSectionVoByLevel(fEntity.getLevel());
        boolean isValid = false;
        do {
            if (lootSectionVo == null) {
                LogUtil.error("找不到LootSection表中的数据: 人物等级 = " + fEntity.getLevel());
                break;
            }
            isValid = true;
        } while (false);

        if (isValid) {
            StageinfoVo stageinfoVo = SceneManager.getStageVo(lootSectionVo.getStageid());
            fEntity.setPosition(stageinfoVo.getPosition());
            fEntity.setRotation(stageinfoVo.getRotation());
            String lootSectionId = getLootSectionIdByRoleId(Long.parseLong(fEntity.getUniqueId()));
            if (!StringUtil.isNotEmpty(lootSectionId)) {
                lootSectionId = String.valueOf(lootSectionVo.getLevelsection());
            }
            AttendLootTreasure attendLootTreasure = new AttendLootTreasure(serverId, serverName, Integer.parseInt(lootSectionId), fEntity, jobId);
            MainRpcHelper.rmltService().attendLoottreasue(rmLTServerId, attendLootTreasure);
        }
        fireSpecialAccountEvent(Long.parseLong(fEntity.getUniqueId()), Long.parseLong(fEntity.getUniqueId()), "出现夺宝活动中", true);
    }

    private void fireSpecialAccountEvent(long selfId, long roleId, String content, boolean self) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(selfId, new SpecialAccountEvent(roleId, content, self));
        }
    }

    //设置从夺宝服不间断拉取排行榜数据;
    private void setSchedulePollRankDataEnable(boolean value) {
//        if (scheduler != null) {
//            scheduler.shutdownNow();
//        }
//        if (value) {
//            scheduler = Executors.newScheduledThreadPool(1);
//            scheduler.scheduleAtFixedRate(new Runnable() {
//                @Override
//                public void run() {
//                    ServerLootTreasureRankReq serverLootTreasureRankReq = new ServerLootTreasureRankReq();
//                    sendPacketToServer(serverLootTreasureRankReq);
//                }
//            }, 0, 5, TimeUnit.SECONDS);
//        }
    }

    public List<LTDamageRankVo> getLtDamageRankVoLists(String lootSectionId) {
        if (ltDamageRankMap != null) {
            LTDamageRank ltDamageRank = ltDamageRankMap.get(lootSectionId);
            if (ltDamageRank != null) {
                return ltDamageRank.getLtDamageRankVoList();
            }
            return null;
        }
        return null;
    }

    //设置夺宝活动的状态;
    public void setLootTreasureActivityState(boolean isStarting) {
        setSchedulePollRankDataEnable(isStarting);
    }

    public boolean isActivityFlowValid() {
        return lootTreasureActivityFlow != null;
    }

    public long getStartActivityTimeStamp() {
        return lootTreasureActivityFlow.getStartTimeStamp();
    }

    public long getEndActivityTimeStamp() {
        return lootTreasureActivityFlow.getEndTimeStamp();
    }

    /**
     * @param roleId
     */
    public void checkLootTreasureActivity(long roleId) {
        if (lootTreasureActivityFlow != null) {
            lootTreasureActivityFlow.checkAcitvityTime(ActivityFlow.STEP_START_CHECK, false, roleId);
        }
    }

    @Override
    public void onReceived0(Object message, Actor sender) {
        if (message instanceof ClientLootTreasureRankBack) {
//            ClientLootTreasureRankBack clientLootTreasureRankBack = (ClientLootTreasureRankBack) message;
//            pvpDamageRankVoMap = clientLootTreasureRankBack.pvpDamageRankVoMap;
            return;
        }
        if (message instanceof ClientLootTreasureEnterBack) {
            ClientLootTreasureEnterBack clientLootTreasureEnterBack = (ClientLootTreasureEnterBack) message;
            NotifyEnterLootTreasureEvent notifyEnterLootTreasureEvent = new NotifyEnterLootTreasureEvent();
            notifyEnterLootTreasureEvent.stageId = clientLootTreasureEnterBack.getStageId();
            ServiceHelper.roleService().notice(clientLootTreasureEnterBack.getRoleId(), new RoleNotification(notifyEnterLootTreasureEvent));
            return;
        }
    }

    @Override
    public void addClientLootTreasureRankList(int serverId, String lootSectionId, LTDamageRank ltDamageRank) {
        ltDamageRankMap.put(lootSectionId, ltDamageRank);
    }

    @Override
    public void attendLTBack(int server, long roleId, byte flag) {
        if (flag == 0) {
            //通知连接服更改路由
            ModifyConnectorRoute mcr = new ModifyConnectorRoute();
            String serverId = ServerManager.getServer().getConfig().getProps().get(BootstrapConfig.LOOTTREASURE).getProperty("serverId");
            mcr.setServerId(Integer.parseInt(serverId));
            mcr.setRoleId(roleId);
            PacketManager.send(roleId, mcr);
        }
    }

    @Override
    public void existFight(int serverId, long roleId) {
        //在这里更改路由为连接服直接连到主服去;
        ModifyConnectorRoute mcr = new ModifyConnectorRoute();
        mcr.setServerId(serverId);
        mcr.setRoleId(roleId);
        PacketManager.send(roleId, mcr);
        LogUtil.info("夺宝服务收到要离开战斗的协议了: roleId = " + roleId);
        LootTreasureManager.log("主服 收到请求离开战斗， 并切换路由到主服: roleId = " + roleId + ",serverId=" + serverId);
        RequestExitFightEvent requestExitFightEvent = new RequestExitFightEvent();
        ServiceHelper.roleService().notice(roleId, new RoleNotification(requestExitFightEvent));
    }

    @Override
    public void setRoleAtLootSection(long roleId, String lootSectionId) {
        if (!lootSectionRoleMap.containsKey(lootSectionId)) {
            lootSectionRoleMap.put(lootSectionId, new ArrayList<Long>());
        }
        if (!lootSectionRoleMap.get(lootSectionId).contains(roleId)) {
            lootSectionRoleMap.get(lootSectionId).add(roleId);
        }
    }

    public String getLootSectionIdByRoleId(long roleId) {
        for (Map.Entry<String, List<Long>> kvp : lootSectionRoleMap.entrySet()) {
            for (int i = 0, len = kvp.getValue().size(); i < len; i++) {
                if (kvp.getValue().get(i).equals(roleId)) {
                    return kvp.getKey();
                }
            }
        }
        return null;
    }

    @Override
    public void sendAwardEmail(int serverId, int customType, long receiveRoleId, int templateId, long sendId, String sendName, Map<Integer, Integer> affixMap) {
        /*switch (customType) { // 将逻辑移到夺宝服，修改：zhouyaohui 2017/01/21
            case MConst.CCLootTreasure:
                //这里包含有掉落道具，要派发事件到dropModule里去处理;
                RequestSendSingleEmailEvent requestSendSingleEmailEvent = new RequestSendSingleEmailEvent(receiveRoleId, templateId, sendId, sendName, affixMap);
                requestSendSingleEmailEvent.setCurstomType(customType);
                ServiceHelper.roleService().notice(receiveRoleId, requestSendSingleEmailEvent);
                return;
        }*/
        ServiceHelper.emailService().sendToSingle(receiveRoleId, templateId, sendId, sendName, affixMap);
    }

}
