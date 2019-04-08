package com.stars.modules.familyactivities.invade;

import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.arroundPlayer.ArroundPlayerModule;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.BaseTeamModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.familyactivities.FamilyActUtil;
import com.stars.modules.familyactivities.invade.event.FamilyInvadeEnterDungeonEvent;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvade;
import com.stars.modules.familyactivities.invade.packet.ClientFamilyInvadeNotice;
import com.stars.modules.familyactivities.invade.prodata.FamilyInvadeVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.city.FamilyScene;
import com.stars.modules.scene.imp.city.SafeCityScene;
import com.stars.modules.scene.packet.ClientMonsterDrop;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.prodata.NpcInfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.activities.invade.FamilyActInvadeFlow;
import com.stars.services.family.activities.invade.cache.AwardBoxCache;
import com.stars.util.I18n;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyInvadeModule extends AbstractModule {
    private Map<Integer, Integer> monsterDrop = new HashMap<>();// 战斗副本怪物掉落
    private Map<String, AwardBoxCache> awardBoxMap = new HashMap<>();// 宝箱
    private Map<String, Boolean> awardBoxStatus = new HashMap<>();// 宝箱状态

    public FamilyInvadeModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("外族入侵(家族活动)", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        // 活动已开启
        if (FamilyActInvadeFlow.isStarted()) {
            monsterDrop = new HashMap<>();
            awardBoxMap = new HashMap<>();
            awardBoxStatus = new HashMap<>();
            send(new ClientFamilyInvadeNotice(ClientFamilyInvadeNotice.START));
        }
    }

    @Override
    public void onOffline() throws Throwable {
        // 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return;
        // 离线做退出家族领地处理
        ServiceHelper.familyActInvadeService().removeMember(getFamilyId(), id());
    }

    @Override
    public void onTimingExecute() {
        if (awardBoxMap.isEmpty()) {
            return;
        }
        // 清除过期宝箱数据
        Iterator<AwardBoxCache> iterator = awardBoxMap.values().iterator();
        while (iterator.hasNext()) {
            AwardBoxCache boxCache = iterator.next();
            if (System.currentTimeMillis() - boxCache.getCreateTimestamp() > FamilyInvadeManager.awardBoxShow) {
                iterator.remove();
                awardBoxStatus.put(boxCache.getAwardBoxUId(), Boolean.TRUE);
            }
        }
    }

    /**
     * 入侵活动开始处理
     */
    public void invadeStartHandler() {
        SceneModule sceneModule = module("scene");
        // 活动开始,已经在家族场景中
        if (sceneModule.getScene() instanceof FamilyScene) {
            updateMember();
        }
        send(new ClientFamilyInvadeNotice(ClientFamilyInvadeNotice.START));
    }

    /**
     * 更新参加活动成员数据
     */
    public void updateMember() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return;
        // 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return;
        BaseTeamModule teamModule = module(MConst.Team);
        ServiceHelper.familyActInvadeService().addUpdateMember(familyAuth.getFamilyId(), teamModule.selfToTeamMember(BaseTeamManager.TEAM_TYPE_FAMILYINVADE));
    }

    /**
     * 移除参加活动成员数据
     */
    public void removeMemeber() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return;
        // 活动未开启
        if (!FamilyActInvadeFlow.isStarted())
            return;
        ServiceHelper.familyActInvadeService().removeMember(familyAuth.getFamilyId(), id());
    }

    public long getFamilyId() {
        FamilyAuth familyAuth = FamilyActUtil.getAuth(moduleMap());
        if (familyAuth == null)
            return 0;
        return familyAuth.getFamilyId();
    }

    /**
     * 怪物掉落缓存并发送客户端
     *
     * @param dropIds
     */
    public void addMonsterDrop(Map<String, Integer> dropIds) {
        Map<String, List<Map<Integer, Integer>>> sendDrop = new HashMap<>();
        DropModule dropModule = module(MConst.Drop);
        for (Map.Entry<String, Integer> entry : dropIds.entrySet()) {
            List<Map<Integer, Integer>> mapList = dropModule.executeDropNotCombine(entry.getValue(), 1,false);
            for (Map<Integer, Integer> map : mapList) {
                MapUtil.add(monsterDrop, map);
            }
            sendDrop.put(entry.getKey(), mapList);
        }
        // send to client
        ClientMonsterDrop packet = new ClientMonsterDrop(sendDrop);
        send(packet);
    }

    /**
     * 战斗结算
     *
     * @param result
     * @param invadeId
     */
    public void finishReward(byte result, int invadeId) {
        FamilyInvadeVo invadeVo = FamilyInvadeManager.getInvadeVo(invadeId);
        Map<Integer, Integer> rewards = new HashMap<>();
        // 怪物掉落
        MapUtil.add(rewards, monsterDrop);
        if (result == SceneManager.STAGE_VICTORY) {
            DropModule dropModule = module(MConst.Drop);
            MapUtil.add(rewards, dropModule.executeDrop(invadeVo.getAward(), 1,true));
        }
        ToolModule toolModule = module(MConst.Tool);
        Map<Integer, Integer> map = toolModule.addAndSend(rewards, EventType.FAMILYINVADE.getCode());
        monsterDrop.clear();
        ClientStageFinish packet = new ClientStageFinish(SceneManager.SCENETYPE_FAMILY_INVADE, result);
        packet.setItemMap(map);
        send(packet);
    }

    /**
     * 请求宝箱列表
     */
    public void reqAwardBox() {
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.SPAWN_AWARD_BOX);
        packet.setAwardBoxMap(awardBoxMap);
        packet.setAwardBoxStatus(awardBoxStatus);
        send(packet);
        fireSpecialAccountLogEvent("家族入侵请求宝箱列表");
    }

    /**
     * 刷宝箱
     *
     * @param map
     */
    public void spawnAwardBox(Map<String, AwardBoxCache> map) {
        this.awardBoxMap = map;
        this.awardBoxStatus = new HashMap<>();
        for (AwardBoxCache boxCache : awardBoxMap.values()) {
            awardBoxStatus.put(boxCache.getAwardBoxUId(), Boolean.FALSE);
        }
        // send to client
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.SPAWN_AWARD_BOX);
        packet.setAwardBoxMap(awardBoxMap);
        packet.setAwardBoxStatus(awardBoxStatus);
        send(packet);
    }

    /**
     * 开宝箱
     *
     * @param boxUId
     */
    public void openAwardBox(String boxUId, float curPosX, float curPosZ) {
        AwardBoxCache boxCache = awardBoxMap.get(boxUId);
        if (boxCache == null) {
            return;
        }
        if (awardBoxStatus.get(boxUId)) {
            warn(I18n.get("family.invade.awardBoxOpened"));
            return;
        }
        if (System.currentTimeMillis() - boxCache.getCreateTimestamp() > FamilyInvadeManager.awardBoxShow) {
            warn(I18n.get("family.invade.awardBoxTimeout"));
            return;
        }
        NpcInfoVo npcInfoVo = SceneManager.getNpcVo(boxCache.getNpcId());
        if (!FormularUtils.isPointInCircle(boxCache.getPosX() / 10f, boxCache.getPosZ() / 10f, npcInfoVo.getRange() / 10f,
                curPosX, curPosZ)) {
            warn(I18n.get("family.invade.awardBoxNotInTrigger"));
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(FamilyInvadeManager.boxReward, MConst.CCFamilyInvade,EventType.FAMILYDONATE.getCode());
        awardBoxStatus.put(boxUId, Boolean.TRUE);
        // update to client
        ClientFamilyInvade packet = new ClientFamilyInvade(ClientFamilyInvade.UPDATE_AWARD_BOX);
        packet.setAwardBoxUId(boxUId);
        packet.setAwardBoxStatus(awardBoxStatus);
        send(packet);
        fireSpecialAccountLogEvent("加入入侵开宝箱");
    }

    /**
     * 战斗交互包处理
     *
     * @param packet
     */
    public void receiveFightPacket(PlayerPacket packet) {
        ServiceHelper.familyActInvadeService().receiveFightPacket(getFamilyId(), id(), packet);
    }

    /**
     * 进入活动战斗处理
     * 1.策划需求,需要屏蔽被周围玩家同步
     *
     * @param event
     */
    public void enterInvadeDungeonHandler(Event event) {
        ArroundPlayerModule apm = module("arroundplayer");
        SceneModule sceneModule = module("scene");
        RoleModule roleModule = module("role");
        StringBuilder builder = new StringBuilder("");
        builder.append(SceneManager.ARROUND_SCENE_PREFIX)
                .append(roleModule.getSafeStageId())
                .append("invade")
                .append(((FamilyInvadeEnterDungeonEvent) event).getStageId());
        // 构造一个假的场景
        apm.doEnterSceneEvent(SceneManager.SCENETYPE_FAMILY_INVADE, builder.toString(), sceneModule.getLastSceneType(),
                roleModule.getJoinSceneStr());
        sceneModule.setScene(new SafeCityScene());
    }
    private void fireSpecialAccountLogEvent(String content) {
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), content, true));
        }
    }
}
