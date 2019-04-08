package com.stars.modules.familyactivities.war;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.drop.DropModule;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.familyactivities.war.event.FamilyWarEnterSafeSceneEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFighterAddingSucceededEvent;
import com.stars.modules.familyactivities.war.event.FamilyWarFightingOrNotEvent;
import com.stars.modules.familyactivities.war.packet.fight.ClientFamilyWarBattleFightEliteResult;
import com.stars.modules.fightingmaster.event.FightReadyEvent;
import com.stars.modules.pk.event.BackCityEvent;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.FamilyWarConst;
import com.stars.multiserver.familywar.FamilyWarUtil;
import com.stars.multiserver.familywar.event.FamilyWarRevivePayReqEvent;
import com.stars.multiserver.familywar.event.FamilyWarSendPacketEvent;
import com.stars.multiserver.familywar.event.FamilyWarSupportEvent;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyPost;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/11/22.
 */
public class FamilyActWarModule extends AbstractModule {

    private int fightServerId;
    private boolean isInEliteFihgt = false;
    private long fightStartTimeStamp;

    public FamilyActWarModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("家族战（家族活动）", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onTimingExecute() {
        if (fightStartTimeStamp == 0L)
            return;
        if ((System.currentTimeMillis() - fightStartTimeStamp) / 1000 > 30 * 60 && isInEliteFihgt) {
            isInEliteFihgt = false;
        }
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        ServiceHelper.familyWarService().roleOnline(id());
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.familyWarService().roleOffline(id());
    }

    public boolean payForRevive() {
        ToolModule toolModule = module(MConst.Tool);
        if (toolModule.deleteAndSend(ToolManager.BANDGOLD, FamilyActWarManager.revivePay, EventType.FAMILY_WAR_REVIVE.getCode())) {
            return true;
        }
        return false;
    }

    private void updateRoleState(boolean fight) {
        isInEliteFihgt = fight;
        if (isInEliteFihgt) {
            fightStartTimeStamp = System.currentTimeMillis();
        } else {
            fightStartTimeStamp = 0L;
        }
    }

    public boolean isInEliteFihgt() {
        LogUtil.info("familywar|是否在精英战中:{}", isInEliteFihgt);
        return isInEliteFihgt;
    }

    private boolean isMaster() {
        FamilyModule family = module(MConst.Family);
        boolean isMaster = family.getAuth().getPost().getId() == FamilyPost.MASTER_ID;
        LogUtil.info("familywar|族长");
        return isMaster;
    }

    private long getFamilyId() {
        FamilyModule family = module(MConst.Family);
        return family.getAuth().getFamilyId();
    }

    @Override
    public void onReconnect() throws Throwable {
        ServiceHelper.familyWarService().roleOnline(id());
    }

    public void onEvent(Event event) {
        if (event instanceof LoginSuccessEvent) {
            if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_LOCAL) {
                ServiceHelper.familyWarLocalService().sendMainIcon2Role(id(), isMaster(), getFamilyId());
            }
            if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_QUALIFYING) {
                MainRpcHelper.familyWarQualifyingService().sendMainIcon2Role(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), id(), isMaster(), getFamilyId());
            }
            if (FamilyWarConst.battleType == FamilyWarConst.W_TYPE_REMOTE) {
                MainRpcHelper.familyWarRemoteService().sendMainIcon2Role(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), id(), isMaster(), getFamilyId());
            }
            ServiceHelper.familyWarService().roleOnline(id());
        } else if (event instanceof FamilyWarRevivePayReqEvent) {
            FamilyWarRevivePayReqEvent reviveEvent = (FamilyWarRevivePayReqEvent) event;
            boolean isPay = payForRevive();
            if (isPay) {
                ServiceHelper.familyWarLocalService().handleRevive(reviveEvent.getBattleId(), reviveEvent.getFightId(), reviveEvent.getFigterUid());
            }

        } else if (event instanceof FightReadyEvent) {

        } else if (event instanceof FamilyWarFighterAddingSucceededEvent) {
            LogUtil.info(" {} 加入战斗", id());
            FamilyWarFighterAddingSucceededEvent e = (FamilyWarFighterAddingSucceededEvent) event;
            fightServerId = e.getFightServerId();
            SceneModule scene = module(MConst.Scene);
            scene.setLastSceneType(e.getSceneType());
        } else if (event instanceof BackCityEvent) {
            // 自己不在安全区
            SceneModule scene = module(MConst.Scene);
            scene.backToCity();
        } else if (event instanceof FamilyWarSupportEvent) {
            if (getInt("F_SUPPORT_COUNT") >= FamilyActWarManager.maxSupportCount) {
                warn("已经达到最大次数");
            }
            //增加已点赞次数
            setInt("F_SUPPORT_COUNT", getInt("F_SUPPORT_COUNT") + 1);
            //发奖
            DropModule dropModule = module(MConst.Drop);
            ToolModule toolModule = module(MConst.Tool);
            Map<Integer, Integer> toolMap = dropModule.executeDrop(FamilyActWarManager.dropIdOfSupportAward, 1, true);
            toolModule.addAndSend(toolMap, EventType.FAMILY_WAR_SUPPORT.getCode());
            //增加家族被赞次数
            FamilyModule familyModule = module(MConst.Family);
            long familyId = familyModule.getAuth().getFamilyId();
            FamilyWarSupportEvent supportEvent = (FamilyWarSupportEvent) event;
            if (supportEvent.getWarType() == FamilyWarConst.W_TYPE_LOCAL) {
                ServiceHelper.familyWarLocalService().addSupport(id(), familyId);
            } else if (supportEvent.getWarType() == FamilyWarConst.W_TYPE_QUALIFYING) {
                MainRpcHelper.familyWarQualifyingService().addSupport(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), id(), familyId);
            } else if (supportEvent.getWarType() == FamilyWarConst.W_TYPE_REMOTE) {
                MainRpcHelper.familyWarRemoteService().addSupport(FamilyWarUtil.getFamilyWarServerId(), MultiServerHelper.getServerId(), id(), familyId);
            }
        } else if (event instanceof FamilyWarSendPacketEvent) {
            SceneModule scene = module(MConst.Scene);
            FamilyWarSendPacketEvent packetEvent = (FamilyWarSendPacketEvent) event;
            if (packetEvent.getSceneType() == FamilyWarConst.SCENE_TYPE_ALL) {
                send(packetEvent.getPacket());
            } else if (packetEvent.getSceneType() == FamilyWarConst.SCENE_TYPE_SAFE) {
                if (scene.getLastSceneType() == SceneManager.SCENETYPE_CITY
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMIL
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_SAFE_SCENE
                        || scene.getLastSceneType() == -1) {//临时的
                    send(packetEvent.getPacket());
                }
            } else if (packetEvent.getSceneType() == FamilyWarConst.SCENE_TYPE_FAMILY_WAR) {
                if (scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH) {
                    if (packetEvent.getPacket() instanceof ClientFamilyWarBattleFightEliteResult) {
                        RoleModule roleModule = module(MConst.Role);
                        LogUtil.info(roleModule.getRoleRow().getName() + "=====================" + "精英赛结算");
                    }
                    send(packetEvent.getPacket());
                }
            } else if (packetEvent.getSceneType() == FamilyWarConst.SCENE_TYPE_NOT_FIGHT_SCENE) {
                LogUtil.info("roleId:{},lastSceneType:{}", id(), scene.getLastSceneType());
                if (scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_ELITE_FIGHT
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_NORMAL_FIGHT
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_STAGE_FIGTH
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMILY_WAR_SAFE_SCENE
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_CITY
                        || scene.getLastSceneType() == SceneManager.SCENETYPE_FAMIL) {
                    send(packetEvent.getPacket());
                }
            }
        } else if (event instanceof FamilyWarEnterSafeSceneEvent) {
            LogUtil.info("进入家族战安全区:{}", id());
            SceneModule sceneModule = module(MConst.Scene);
            sceneModule.enterScene(SceneManager.SCENETYPE_FAMILY_WAR_SAFE_SCENE, FamilyActWarManager.stageIdOfSafe, event);
        } else if (event instanceof FamilyWarFightingOrNotEvent) {
            updateRoleState(((FamilyWarFightingOrNotEvent) event).isFightOrNot());
        }
    }
}
