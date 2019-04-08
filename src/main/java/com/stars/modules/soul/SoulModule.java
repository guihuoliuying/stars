package com.stars.modules.soul;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.soul.event.SoulLevelUpEvent;
import com.stars.modules.soul.event.SoulStageUpEvent;
import com.stars.modules.soul.packet.ClientSoulPacket;
import com.stars.modules.soul.prodata.SoulLevel;
import com.stars.modules.soul.prodata.SoulStage;
import com.stars.modules.soul.usrdata.RoleSoul;
import com.stars.modules.task.TaskManager;
import com.stars.modules.task.TaskModule;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class SoulModule extends AbstractModule {
    private RoleSoul roleSoul;
    private Map<Integer, SoulLevel> soulLevelsMap = new LinkedHashMap<>();

    public SoulModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleSoul = DBUtil.queryBean(DBUtil.DB_USER, RoleSoul.class, String.format("select * from rolesoul where roleid=%s;", id()));
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleSoul == null) {
            roleSoul = new RoleSoul(id());
            context().insert(roleSoul);
        }
        caculateAttrAndFight(false);
        signCalRedPoint(MConst.Soul, RedPointConst.SOUL);
    }

    @Override
    public void onSyncData() throws Throwable {
        TaskModule taskModule = module(MConst.Task);
        for (SoulLevel soulLevel : soulLevelsMap.values()) {
            taskModule.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulLevel, soulLevel.getSoulGodType() + ""),
                    soulLevel.getSoulGodLevel(), true);
        }
        taskModule.updateRoleAcceptTaskProcess(TaskManager.getTaskKey(TaskManager.Task_Type_SoulStage, "2"),
                roleSoul.getStage(), true);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.SOULGOD)) {
            return;
        }
        if (redPointIds.contains(RedPointConst.SOUL)) {
            SoulLevel nextSoulLevel = roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage());
            if (nextSoulLevel == null) {
                /**
                 * 突破
                 */
                if (roleSoul.getStage() != SoulManager.maxSoulStage) {
                    SoulStage soulStage = SoulManager.soulStageMap.get(roleSoul.getStage() + 1);
                    if (!soulStage.isLimit(moduleMap(), false)) {
                        ToolModule toolModule = module(MConst.Tool);
                        if (toolModule.contains(soulStage.getReqItemMap())) {
                            redPointMap.put(RedPointConst.SOUL, "");
                            return;
                        }
                    }

                }
            } else {
                /**
                 * 升级
                 */
                if (!nextSoulLevel.isLimit(moduleMap(), false)) {
                    ToolModule toolModule = module(MConst.Tool);
                    if (toolModule.contains(nextSoulLevel.getReqItemMap())) {
                        redPointMap.put(RedPointConst.SOUL, "");
                        return;
                    }
                }

            }
            redPointMap.put(RedPointConst.SOUL, null);
        }

    }

    /**
     * 计算战力和属性
     */

    private void caculateAttrAndFight(boolean sendClient) {
        soulLevelsMap = new LinkedHashMap<>();
        int stage = roleSoul.getStage();
        int myType = roleSoul.getType();
        Attribute totalAttribute = new Attribute();
        SoulStage soulStage = SoulManager.soulStageMap.get(stage);
        totalAttribute.addAttribute(soulStage.getAttribute());//
        for (int type = 1; type <= 7; type++) {
            SoulLevel soulLevel = null;
            if (myType > type) {
                Map<Integer, SoulLevel> soulTypeMap = SoulManager.soulTypeMap.get(type);
                int maxLevel = soulStage.getMaxLevel();
                soulLevel = soulTypeMap.get(maxLevel);
            } else if (myType == type) {
                Map<Integer, SoulLevel> soulTypeMap = SoulManager.soulTypeMap.get(type);
                soulLevel = soulTypeMap.get(roleSoul.getLevel());

            } else if (myType < type) {
                Map<Integer, Map<Integer, SoulLevel>> stageMap = SoulManager.soulStageLevelMap.get(stage - 1);
                if (stageMap == null) {
                    /**
                     * 初始数据
                     */
                    soulLevel = SoulManager.soulStageLevelMap.get(stage).get(type).get(soulStage.getMinLevel());
                } else {
                    Map<Integer, SoulLevel> soulTypeMap = stageMap.get(type);
                    SoulStage soulStageTmp = SoulManager.soulStageMap.get(stage - 1);
                    int maxLevel = soulStageTmp.getMaxLevel();
                    soulLevel = soulTypeMap.get(maxLevel);
                }

            }
            soulLevelsMap.put(type, soulLevel);
            Attribute attribute = soulLevel.getAttribute();
            totalAttribute.addAttribute(attribute);

        }
        RoleModule roleModule = module(MConst.Role);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_SOUL, totalAttribute);
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_SOUL, FormularUtils.calFightScore(totalAttribute));
        if (sendClient) {
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore();
        }

    }


    /**
     * 打开主界面
     */
    public void reqMainUI() {
        ClientSoulPacket clientSoulPacket = new ClientSoulPacket(ClientSoulPacket.SEND_MAIN_UI);
        clientSoulPacket.setRoleSoul(roleSoul);
        clientSoulPacket.setSoulLevels(soulLevelsMap.values());
        RoleModule roleModule = module(MConst.Role);
        Attribute deltaAttribute = new Attribute();
        String nextCost = "";
        SoulLevel nextSoulLevel = roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage());
        if (nextSoulLevel == null) {
            if (roleSoul.getStage() != SoulManager.maxSoulStage) {
                SoulStage soulStage = roleSoul.getSoulStage();
                SoulStage nextSoulStage = roleSoul.getSoulStage().getNextStage();
                if (nextSoulStage != null) {
                    deltaAttribute.addAttribute(nextSoulStage.getAttribute());
                    deltaAttribute.subAttribute(soulStage.getAttribute());
                    nextCost = nextSoulStage.getReqItem();
                }
            }
        } else {
            deltaAttribute.addAttribute(nextSoulLevel.getAttribute());
            SoulLevel oldSoulLevel = SoulManager.soulTypeMap.get(nextSoulLevel.getSoulGodType()).get(nextSoulLevel.getSoulGodLevel() - 1);
            if (oldSoulLevel != null) {
                deltaAttribute.subAttribute(oldSoulLevel.getAttribute());
            }
            nextCost = nextSoulLevel.getReqItem();
        }
        int totalFightScore = roleModule.getRoleRow().getFightScoreMap().get(RoleManager.FIGHTSCORE_SOUL);
        clientSoulPacket.setTotalFightScore(totalFightScore);
        Attribute attribute = roleModule.getRoleRow().getAttrMap().get(RoleManager.ROLEATTR_SOUL);
        clientSoulPacket.setTotalAttribute(attribute);
        clientSoulPacket.setDeltaAttribute(deltaAttribute);
        clientSoulPacket.setCostItem(nextCost);
        send(clientSoulPacket);
    }

    /**
     * 请求升级
     */
    public void reqUpgrade() {
        SoulLevel nextSoulLevel = roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage());
        if (nextSoulLevel == null) {
            if (roleSoul.getStage() == SoulManager.maxSoulStage) {
                warn("已达满级");
            } else {
                warn("需要突破，无法升级");
            }
            return;
        }
        if (nextSoulLevel.isLimit(moduleMap(), true)) {
            return;
        }
        Map<Integer, Integer> reqItemMap = nextSoulLevel.getReqItemMap();
        ToolModule toolModule = module(MConst.Tool);
        boolean success = toolModule.deleteAndSend(reqItemMap, EventType.SOUL.getCode());
        if (success) {
            com.stars.util.LogUtil.info("soul upgrade roleid:{} at {} from {} to {}", id(), roleSoul.getType(), roleSoul.getLevel(), nextSoulLevel.getSoulGodLevel());
            roleSoul.setType(nextSoulLevel.getSoulGodType());
            roleSoul.setLevel(nextSoulLevel.getSoulGodLevel());
            context().update(roleSoul);
            warn("升级成功");
            caculateAttrAndFight(true);
            eventDispatcher().fire(new SoulLevelUpEvent(roleSoul.getStage(), roleSoul.getType(), roleSoul.getLevel(), soulLevelsMap));
            ClientSoulPacket clientSoulPacket = new ClientSoulPacket(ClientSoulPacket.SEND_UPGRADE);
            send(clientSoulPacket);
            reqMainUI();
            signCalRedPoint(MConst.Soul, RedPointConst.SOUL);
        } else {
            warn("消耗道具不足");
        }
    }

    /**
     * 一键升级
     */
    public void reqOnekeyUpgrade() {
        SoulLevel soulLevel = roleSoul.getSoulLevel();
        Map<Integer, Integer> cost = new HashMap<>();
        ToolModule toolModule = module(MConst.Tool);
        SoulLevel oldSoulLevel = soulLevel;
        while (soulLevel.getNextSoulLevel(roleSoul.getStage()) != null) {
            Map<Integer, Integer> tmpCost = new HashMap<>(cost);
            SoulLevel nextSoulLevel = soulLevel.getNextSoulLevel(roleSoul.getStage());
            if (nextSoulLevel.isLimit(moduleMap(), true)) {
                break;
            }
            MapUtil.add(tmpCost, nextSoulLevel.getReqItemMap());
            boolean contains = toolModule.contains(tmpCost);
            if (!contains) {
                if (soulLevel == oldSoulLevel) {
                    warn("消耗道具不足");
                    return;
                }
                break;
            }
            cost = tmpCost;
            soulLevel = nextSoulLevel;
        }
        if (soulLevel == oldSoulLevel) {
            return;
        }
        if (cost.size() == 0) {
            if (soulLevel.getNextSoulLevel(roleSoul.getStage()) == null) {
                if (roleSoul.getStage() == SoulManager.maxSoulStage) {
                    warn("已达满级");
                } else {
                    warn("需要突破，无法升级");
                }
            } else {
                warn("消耗道具不足");
            }
            return;
        }
        boolean success = toolModule.deleteAndSend(cost, EventType.SOUL.getCode());
        if (success) {
            com.stars.util.LogUtil.info("soul upgrade roleid:{} at {} from {} to {}", id(), roleSoul.getType(), roleSoul.getLevel(), soulLevel.getSoulGodLevel());
            roleSoul.setType(soulLevel.getSoulGodType());
            roleSoul.setLevel(soulLevel.getSoulGodLevel());
            context().update(roleSoul);
            warn("升级成功");
            caculateAttrAndFight(true);
            eventDispatcher().fire(new SoulLevelUpEvent(roleSoul.getStage(), roleSoul.getType(), roleSoul.getLevel(), soulLevelsMap));
            reqMainUI();
            signCalRedPoint(MConst.Soul, RedPointConst.SOUL);
        } else {
            warn("扣除道具失败");
        }
    }

    /**
     * 请求突破
     */
    public void reqBreak() {
        if (roleSoul.getSoulLevel().getNextSoulLevel(roleSoul.getStage()) != null) {
            warn("处于不可突破阶段，请先升级");
            return;
        }
        int stage = roleSoul.getStage();
        if (stage == SoulManager.maxSoulStage) {
            warn("已达到满阶，不可突破");
            return;
        } else {
            SoulStage soulStage = roleSoul.getSoulStage().getNextStage();
            if (soulStage.isLimit(moduleMap(), true)) {
                return;
            }
            Map<Integer, Integer> reqItemMap = soulStage.getReqItemMap();
            ToolModule toolModule = module(MConst.Tool);
            if (toolModule.deleteAndSend(reqItemMap, EventType.SOUL.getCode())) {
                LogUtil.info("soul break roleid:{}  from {} to {}", id(), roleSoul.getStage(), soulStage.getStage());
                roleSoul.setStage(soulStage.getStage());
                roleSoul.setType(1);
                context().update(roleSoul);
                warn("突破成功");
                caculateAttrAndFight(true);
                eventDispatcher().fire(new SoulStageUpEvent(roleSoul.getStage(), roleSoul.getType(), roleSoul.getLevel(), soulLevelsMap));
                ClientSoulPacket clientSoulPacket = new ClientSoulPacket(ClientSoulPacket.SEND_BREAK_SUCCESS);
                send(clientSoulPacket);
                signCalRedPoint(MConst.Soul, RedPointConst.SOUL);
            } else {
                warn("突破消耗道具不足");
            }


        }
    }

    public Map<Integer, SoulLevel> getSoulLevelsMap() {
        return soulLevelsMap;
    }

    /**
     * 处理事件
     *
     * @param event
     */
    public void onEvent(Event event) {
        if (event instanceof AddToolEvent) {
            signCalRedPoint(MConst.Soul, RedPointConst.SOUL);
        }
    }

    public RoleSoul getRoleSoul() {
        return roleSoul;
    }
}
