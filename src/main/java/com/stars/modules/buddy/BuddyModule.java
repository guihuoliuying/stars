package com.stars.modules.buddy;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.buddy.event.BuddyAchieveEvent;
import com.stars.modules.buddy.event.BuddyActiveEvent;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.buddy.event.FightBuddyChangeEvent;
import com.stars.modules.buddy.packet.*;
import com.stars.modules.buddy.pojo.BuddyGuardPo;
import com.stars.modules.buddy.prodata.*;
import com.stars.modules.buddy.summary.BuddySummaryComponentImpl;
import com.stars.modules.buddy.userdata.RoleBuddy;
import com.stars.modules.buddy.userdata.RoleBuddyLineup;
import com.stars.modules.buddy.util.BuddyUtil;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/8/5.
 */
public class BuddyModule extends AbstractModule {

    // recordmap中的key
    public final static String BUDDY_MAX_HISTORY_FIGHTSCORE = "buddy.max.history.fightscore"; //历史最高伙伴战力
    private Map<Integer, RoleBuddy> roleBuddyMap = new HashMap<>();// 角色伙伴
    private Map<Byte, RoleBuddyLineup> roleLineupMap = new HashMap<>();// 角色伙伴阵型
    private int followBuddyId;// 跟随伙伴Id
    private int fightBuddyId;// 上阵伙伴Id
    private Map<Integer, Map<Integer, BuddyGuardPo>> allBuddyGuardPoMap;
    private Set<Integer> openGroupIds;

    private Set<Integer> canActivite;
    private Set<Integer> canUpExp;
    private Set<Integer> canUpStage;
    private Set<Integer> canLineUp;
    private Set<Integer> canUpArm;


    public BuddyModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("伙伴", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `rolebuddy` where `roleid`=" + id();
        roleBuddyMap = DBUtil.queryMap(DBUtil.DB_USER, "buddyid", RoleBuddy.class, sql);
        sql = "select * from `rolebuddylineup` where `roleid`=" + id();
        roleLineupMap = DBUtil.queryMap(DBUtil.DB_USER, "lineupid", RoleBuddyLineup.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
            roleBuddy.calAttribute();
            if (roleBuddy.getIsFollow() == BuddyManager.BUDDY_FOLLOW) {
                followBuddyId = roleBuddy.getBuddyId();
            }
            if (roleBuddy.getIsFight() == BuddyManager.BUDDY_FIGHT) {
                fightBuddyId = roleBuddy.getBuddyId();
            }
        }
        /* 检测阵型激活 */
        for (byte lineupId : BuddyManager.lineupLevelMap.keySet()) {
            activeLineup(lineupId);
        }
        canActivite = new HashSet<>();
        canUpExp = new HashSet<>();
        canUpStage = new HashSet<>();
        canLineUp = new HashSet<>();
        canUpArm = new HashSet<>();
        changeActivite();
        changeExp();
        changeStage();
        changeLineUp();
        changeArmUp();
        initGuardData();
        // 更新阵型属性
        updateLineupAddRoleAttr();
        // 标记需要计算的红点
//        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_CAN_UPGRADE_STAGE);

    }


    @Override
    public void onSyncData() throws Throwable {
        // 登录下发跟随的伙伴数据
        ClientFollowBuddyData packet = new ClientFollowBuddyData(roleBuddyMap.get(followBuddyId));
        send(packet);

        if (StringUtil.isNotEmpty(roleBuddyMap)) {
            for (RoleBuddy roleBuddy : roleBuddyMap.values()) { //全部检查是否完成成就
                int buddyId = roleBuddy.getBuddyId();
                int level = roleBuddy.getLevel();
                eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.LEVELUP, level, level, getRoleBuddyLevelMap()));
                eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.STAGEUP, level, level, getRoleBuddyLevelMap()));
                eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.ARMLEVELUP, level, level, getRoleBuddyLevelMap()));
            }
        }

    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put("buddy", new BuddySummaryComponentImpl(roleBuddyMap));
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.BUDDY_ACTIVATE))) {
            checkBuddyActivate(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.BUDDY_UP_EXP))) {
            checkBuddyUpExp(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.BUDDY_UP_STAGE))) {
            checkBuddyUpStage(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.BUDDY_LINEUP))) {
            checkBuddyLineUp(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.BUDDY_ARM))) {
            checkRedPoint(redPointMap, canUpArm, RedPointConst.BUDDY_ARM);
        }
    }

    @Override
    public void onOffline() throws Throwable {
        ServerLogModule log = module(MConst.ServerLog);
        String staticStr = "";
        String lvStr = "";
        String quaStr = "";
        String armStr = "";
        String lineupStr = "";
        for (Map.Entry<Integer, RoleBuddy> m : roleBuddyMap.entrySet()) {
            BuddyinfoVo infoVo = BuddyManager.getBuddyinfoVo(m.getValue().getBuddyId());
            if (null == infoVo) continue;
            if ("".equals(lvStr)) {
                lvStr = lvStr + m.getValue().getBuddyId() + "@" + m.getValue().getLevel();
                armStr = armStr + m.getValue().getBuddyId() + "@" + m.getValue().getArmLevel();
                quaStr = quaStr + m.getValue().getBuddyId() + "@" + infoVo.getQuality();
            } else {
                lvStr = lvStr + "&" + m.getValue().getBuddyId() + "@" + m.getValue().getLevel();
                armStr = armStr + "&" + m.getValue().getBuddyId() + "@" + m.getValue().getArmLevel();
                quaStr = quaStr + "&" + m.getValue().getBuddyId() + "@" + infoVo.getQuality();
            }
        }
        for (Map.Entry<Byte, RoleBuddyLineup> m : roleLineupMap.entrySet()) {
            if (m.getValue().getBuddyId() == 0) continue;
            if ("".equals(lineupStr)) {
                lineupStr = lineupStr + m.getValue().getLineupId() + "@" + m.getValue().getBuddyId();
            } else {
                lineupStr = lineupStr + "&" + m.getValue().getLineupId() + "@" + m.getValue().getBuddyId();
            }
        }
        if (roleBuddyMap.size() > 0) {
            staticStr = "buddy_code@buddy_lv:" + lvStr + "#" + "buddy_code@quality:" + quaStr + "#" +
                    "buddy_code@armlevel:" + armStr + "#" + "buddylineup@buddy_code:" + lineupStr;
        }
        log.log_buddy(staticStr);
    }

    public void checkBuddyActivate(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, canActivite, RedPointConst.BUDDY_ACTIVATE);
    }

    public void checkBuddyUpExp(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, canUpExp, RedPointConst.BUDDY_UP_EXP);
    }

    public void checkBuddyUpStage(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, canUpStage, RedPointConst.BUDDY_UP_STAGE);
    }

    public void checkBuddyLineUp(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, canLineUp, RedPointConst.BUDDY_LINEUP);
    }

    public void changeActivite() {
        if (canActivite == null) {
            canActivite = new HashSet<>();
        }
        for (int buddyId : BuddyManager.buddyinfoVoMap.keySet()) {
            if (roleBuddyMap.get(buddyId) == null) {
                BuddyStageVo newLevel = BuddyManager.getBuddyStageVo(buddyId, 1);
                ToolModule toolModule = module(MConst.Tool);
                if (toolModule.contains(newLevel.getReqItemMap())) {
                    canActivite.add(buddyId);
                } else {
                    if (canActivite.contains(buddyId)) {
                        canActivite.remove(buddyId);
                    }
                }
            } else {
                if (canActivite.contains(buddyId)) {
                    canActivite.remove(buddyId);
                }
            }
        }
        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_ACTIVATE);
    }

    public void changeStage() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BUDDY_UP_LEVEL)) {
            return;
        }
        RoleBuddy roleBuddy;
        if (canUpStage == null) {
            canUpStage = new HashSet<>();
        }
        for (Map.Entry<Integer, RoleBuddy> entry : roleBuddyMap.entrySet()) {
            roleBuddy = entry.getValue();
            BuddyStageVo newLevel = BuddyManager.getBuddyStageVo(roleBuddy.getBuddyId(), roleBuddy.getStageLevel() + 1);
            if (newLevel == null) {
                if (canUpStage.contains(roleBuddy.getBuddyId())) {
                    canUpStage.remove(roleBuddy.getBuddyId());
                }
                continue;
            }
            ToolModule toolModule = module(MConst.Tool);
            if (roleBuddy.getLevel() >= newLevel.getReqBuddyLv() && toolModule.contains(newLevel.getReqItemMap())) {
                canUpStage.add(roleBuddy.getBuddyId());
            } else {
                if (canUpStage.contains(roleBuddy.getBuddyId())) {
                    canUpStage.remove(roleBuddy.getBuddyId());
                }
            }
        }
        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_UP_STAGE);
    }

    public void changeExp() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BUDDY_ADD_EXP)) {
            return;
        }
        if (canUpExp == null) {
            canUpExp = new HashSet<>();
        }
        int expItem = BuddyManager.expItemId;
        RoleBuddy roleBuddy;
        ToolModule toolModule = module(MConst.Tool);
        for (Map.Entry<Integer, RoleBuddy> entry : roleBuddyMap.entrySet()) {
            roleBuddy = entry.getValue();
            if (canLvUp(roleBuddy.getBuddyId()) && toolModule.contains(expItem, getExpItemNum2NextLv(entry.getKey()))) {
                canUpExp.add(roleBuddy.getBuddyId());
            } else {
                if (canUpExp.contains(roleBuddy.getBuddyId())) {
                    canUpExp.remove(roleBuddy.getBuddyId());
                }
            }
        }
        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_UP_EXP);
    }

    public void changeArmUp() {
        if (canUpArm == null) {
            canUpArm = new HashSet<>();
        }

        canUpArm.clear();

        if (roleBuddyMap != null) {
            for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
                int buddyId = roleBuddy.getBuddyId();
                BuddyArmsVo buddyArmsVo = BuddyManager.getBuddyArmVo(buddyId, roleBuddy.getArmLevel() + 1);
                // 已满级
                if (buddyArmsVo == null) {
                    continue;
                }
                // 等级不足
                if (roleBuddy.getLevel() < buddyArmsVo.getReqbuddylv()) {
                    continue;
                }
                // 阶级不足
                if (roleBuddy.getStageLevel() < buddyArmsVo.getReqStageLv()) {
                    continue;
                }
                // 装备不全
                if (!roleBuddy.equipAllPutOn()) {
                    continue;
                }

                canUpArm.add(buddyId);
            }
        }

        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_ARM);
    }

    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Integer> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }

    /**
     * 根据buddyId获得玩家伙伴数据
     *
     * @param buddyId
     * @return 未激活伙伴返回null
     */
    public RoleBuddy getRoleBuddy(int buddyId) {
        return roleBuddyMap.get(buddyId);
    }

    public Iterator<RoleBuddy> getRoleBuddyIterator() {
        return roleBuddyMap.values().iterator();
    }

    /**
     * 获得出战伙伴Id
     *
     * @return
     */
    public int getFightBuddyId() {
        return fightBuddyId;
    }

    /**
     * 获得武装等级大于等于指定的伙伴
     *
     * @param armLv
     * @return
     */
    private Map<Integer, RoleBuddy> getBuddyByArmLv(int armLv) {
        Map<Integer, RoleBuddy> map = new HashMap<>();
        for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
            if (roleBuddy.getArmLevel() >= armLv) {
                map.put(roleBuddy.getBuddyId(), roleBuddy);
            }
        }
        return map;
    }

    /**
     * 打开伙伴界面,下发所有伙伴数据
     */
    public void sendAllBuddyData() {
        //一次性同时发产品数据和用户数据会超过大小限制..暂时处理强行分包 应该增量下发而不是全部下发 by dyr
        Map<Integer, BuddyinfoVo> firstMap = new HashMap<>();
        Map<Integer, BuddyinfoVo> secondMap = new HashMap<>();
        byte count = 0;
        for (BuddyinfoVo vo : BuddyManager.buddyinfoVoMap.values()) {
            count++;
            firstMap.put(vo.getBuddyId(), vo);
            if (count >= 10) {
                break;
            }
        }
        for (BuddyinfoVo vo : BuddyManager.buddyinfoVoMap.values()) {
            if (firstMap.get(vo.getBuddyId()) == null) {
                secondMap.put(vo.getBuddyId(), vo);
            }
        }
        ClientAllBuddyData firstPacket = new ClientAllBuddyData();
        firstPacket.setBuddyinfoVoMap(firstMap);
        firstPacket.setLevelVoMap(BuddyManager.buddyLevelVoMap);
        firstPacket.setStageVoMap(BuddyManager.buddyStageVoMap);
        firstPacket.setArmVoMap(BuddyManager.buddyArmsVoMap);
        send(firstPacket);

        ClientAllBuddyData secondPacket = new ClientAllBuddyData();
        secondPacket.setBuddyinfoVoMap(secondMap);
        secondPacket.setLevelVoMap(BuddyManager.buddyLevelVoMap);
        secondPacket.setStageVoMap(BuddyManager.buddyStageVoMap);
        secondPacket.setArmVoMap(BuddyManager.buddyArmsVoMap);
        send(secondPacket);
        //星阵产品数据
        ClientAllBuddyData lineupPack = new ClientAllBuddyData();
        lineupPack.setLineupVoMap(BuddyManager.lineupLevelMap);
        send(lineupPack);
        //用户数据
        ClientAllBuddyData userDataPack = new ClientAllBuddyData();
        userDataPack.setRoleBuddyMap(roleBuddyMap);
        userDataPack.setRoleLineupMap(roleLineupMap);
        send(userDataPack);
    }

    /**
     * 下发伙伴更新
     *
     * @param changeList
     */
    private void sendBuddyUpdate(byte sendType, List<RoleBuddy> changeList) {
        ClientUpdateBuddy packet = new ClientUpdateBuddy(sendType, changeList);
        send(packet);
    }

    private void sendBuddyUpdate(byte sendType, RoleBuddy roleBuddy) {
        List<RoleBuddy> list = new LinkedList<>();
        list.add(roleBuddy);
        sendBuddyUpdate(sendType, list);
    }

    /**
     * 下发阵型更新
     *
     * @param changeList
     */
    private void sendLineupUpdate(List<RoleBuddyLineup> changeList) {
        ClientUpdateLineup packet = new ClientUpdateLineup(changeList);
        send(packet);
    }

    private void sendLineupUpdate(RoleBuddyLineup roleBuddyLineup) {
        List<RoleBuddyLineup> list = new LinkedList<>();
        list.add(roleBuddyLineup);
        sendLineupUpdate(list);
    }

    /**
     * 伙伴是否可以增加经验
     * 给使用道具增加经验调用
     *
     * @param buddyId
     * @return
     */
    public boolean canAddExp(int buddyId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        if (roleBuddy == null) return false;
        BuddyLevelVo nextLevelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel() + 1);
        if (nextLevelVo == null) return false;// 满级
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();// 角色等级
        if (roleLevel < nextLevelVo.getReqRoleLv()) {
            /**
             * 是否处于一颗经验丹即可升级
             */
            if (roleBuddy.getExp() + BuddyManager.expUnit < nextLevelVo.getReqExp()) {
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * 伙伴是否可以升级
     *
     * @param buddyId
     * @return
     */
    public boolean canLvUp(int buddyId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        if (roleBuddy == null) return false;
        BuddyLevelVo nextLevelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel() + 1);
        if (nextLevelVo == null) return false;// 满级
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();// 角色等级
        if (roleLevel < nextLevelVo.getReqRoleLv()) {
            return false;
        }
        return true;
    }

    /**
     * 伙伴加经验
     *
     * @param buddyId
     * @param addExp
     */
    public void addExp(int buddyId, int addExp) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        if (roleBuddy == null) {// 未获得
            return;
        }
        BuddyLevelVo nextLevelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel() + 1);
        if (nextLevelVo == null) {// 已满级
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        int roleLevel = roleModule.getLevel();// 角色等级
        int oldLevel = roleBuddy.getLevel();// 原等级
        if (roleLevel < nextLevelVo.getReqRoleLv()) {
            roleBuddy.setExp(Math.min((roleBuddy.getExp() + addExp), nextLevelVo.getReqExp()));
            context().update(roleBuddy);
            sendBuddyUpdate(ClientUpdateBuddy.ADDEXP, roleBuddy);
            return;
        }
        roleBuddy.setExp(roleBuddy.getExp() + addExp);
        while (roleBuddy.getExp() >= nextLevelVo.getReqExp()) {
            roleBuddy.setExp(roleBuddy.getExp() - nextLevelVo.getReqExp());
            roleBuddy.setLevel(roleBuddy.getLevel() + 1);
            nextLevelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel() + 1);
            if (nextLevelVo == null) {// 已满级
                roleBuddy.setExp(0);
                break;
            }
            if (roleLevel < nextLevelVo.getReqRoleLv()) {
                roleBuddy.setExp(Math.min(roleBuddy.getExp(), nextLevelVo.getReqExp()));
                break;
            }
        }
        context().update(roleBuddy);
        if (roleBuddy.getLevel() > oldLevel) {// 升级
            // 更新属性
            roleBuddy.calAttribute();
            // send to client
            sendBuddyUpdate(ClientUpdateBuddy.LEVELUP, roleBuddy);
            // 可能抛出事件
            eventDispatcher().fire(new BuddyUpgradeEvent(buddyId, BuddyUpgradeEvent.LEVELUP, oldLevel, roleBuddy.getLevel(), getRoleBuddyLevelMap()));
            eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.LEVELUP, oldLevel, roleBuddy.getLevel(), getRoleBuddyLevelMap()));
            if (buddyId == fightBuddyId) {
                eventDispatcher().fire(new FightBuddyChangeEvent());
            }
            if (isInLineUp(roleBuddy.getBuddyId()) || BuddyManager.buddyIdGuardMap.containsKey(roleBuddy.getBuddyId())) {//在阵营中，更新伙伴给角色增加的战力
                updateAddRoleAttrWithSend();
            }
        } else {
            // send to client
            sendBuddyUpdate(ClientUpdateBuddy.ADDEXP, roleBuddy);
        }

        //更新常用数据
        updateBuddySummary();
        changeExp();
        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_UP_EXP);
    }

    public Map<Integer, Integer> getRoleBuddyLevelMap() {
        Map<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, RoleBuddy> entry : roleBuddyMap.entrySet()) {
            map.put(entry.getValue().getBuddyId(), entry.getValue().getLevel());
        }
        return map;
    }

    /**
     * 伙伴升阶（激活）
     *
     * @param buddyId
     */
    public void upgradeStageLv(int buddyId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        BuddyStageVo nextStageVo = BuddyManager.getBuddyStageVo(buddyId,
                roleBuddy == null ? 1 : roleBuddy.getStageLevel() + 1);
        if (nextStageVo == null) {// 已满阶
            return;
        }
        // 伙伴等级不足
        if (roleBuddy != null && roleBuddy.getLevel() < nextStageVo.getReqBuddyLv()) {
            return;
        }
        // 消耗不足
        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(nextStageVo.getReqItemMap(), EventType.BUDDYUP.getCode())) {
            return;
        }
        byte sendType;
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (roleBuddy == null) {// 激活
            if (!foreShowModule.isOpen(ForeShowConst.BUDDY_ADD_EXP)) {
                return;
            }
            roleBuddy = new RoleBuddy(id(), buddyId);
            // 2016.11.23策划需求修改：激活第一个默认跟随+出战
            if (roleBuddyMap.isEmpty()) {
                roleBuddy.setIsFollow(BuddyManager.BUDDY_FOLLOW);
                roleBuddy.setIsFight(BuddyManager.BUDDY_FIGHT);
                this.followBuddyId = buddyId;
                this.fightBuddyId = buddyId;
                // 更新跟随的伙伴数据
                ClientFollowBuddyData packet = new ClientFollowBuddyData(roleBuddy);
                send(packet);
            }
            context().insert(roleBuddy);
            roleBuddyMap.put(roleBuddy.getBuddyId(), roleBuddy);
            eventDispatcher().fire(new BuddyActiveEvent(roleBuddy.getBuddyId()));
            sendType = ClientUpdateBuddy.ACTIVE_BUDDY;
            activeLineupHandler();// 激活阵型
            changeActivite();
        } else {// 升阶
            if (!foreShowModule.isOpen(ForeShowConst.BUDDY_UP_LEVEL)) {
                return;
            }
            roleBuddy.setStageLevel(roleBuddy.getStageLevel() + 1);
            context().update(roleBuddy);
            sendType = ClientUpdateBuddy.UPGRADE_STAGE_LV;

        }
        changeExp();
        changeStage();
        // 更新属性
        roleBuddy.calAttribute();
        // send to client
        sendBuddyUpdate(sendType, roleBuddy);
        //更新常用数据
        updateBuddySummary();
        eventDispatcher().fire(new BuddyUpgradeEvent(buddyId, BuddyUpgradeEvent.STAGEUP, roleBuddy.getStageLevel() - 1,
                roleBuddy.getStageLevel(), getRoleBuddyLevelMap()));
        eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.STAGEUP, roleBuddy.getStageLevel() - 1,
                roleBuddy.getStageLevel(), getRoleBuddyLevelMap()));
        if (buddyId == fightBuddyId) {
            eventDispatcher().fire(new FightBuddyChangeEvent());
        }
        changeLineUp();
        if (isInLineUp(roleBuddy.getBuddyId()) || BuddyManager.buddyIdGuardMap.containsKey(roleBuddy.getBuddyId())) {//在阵营中，更新伙伴给角色增加的战力
            updateAddRoleAttrWithSend();
        }
    }


    public void changeLineUp() {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.BUDDY_LINE)) {
            return;
        }
        List<Integer> lineUpBuddyList = LineUpBuddy();
        if (StringUtil.isEmpty(roleLineupMap)) {
            return;
        }
        if (lineUpBuddyList.size() < getActiveLineupCount()) {
            for (Map.Entry<Byte, RoleBuddyLineup> entry : roleLineupMap.entrySet()) {
                if (entry.getValue().getBuddyId() == 0) {
                    for (int buddyId : roleBuddyMap.keySet()) {
                        if (BuddyManager.buddyIdGuardMap.containsKey(buddyId)) {
                            continue;
                        }
                        if (!lineUpBuddyList.contains(buddyId)) {
                            if (canLineUp == null) {
                                canLineUp = new HashSet<>();
                            }
                            canLineUp.add(buddyId);
                        } else {
                            if (canLineUp.contains(buddyId)) {
                                canLineUp.remove(buddyId);
                            }
                        }
                    }
                } else {
                    if (canLineUp.contains(entry.getValue().getBuddyId())) {
                        canLineUp.remove(entry.getValue().getBuddyId());
                    }
                }
            }
        } else {
            canLineUp.clear();
        }
        signCalRedPoint(MConst.Buddy, RedPointConst.BUDDY_LINEUP);
    }

    /**
     * 获得已激活的阵型个数
     *
     * @return
     */
    private int getActiveLineupCount() {
        int count = 0;
        RoleModule roleModule = module(MConst.Role);
        VipModule vipModule = module(MConst.Vip);
        for (Map.Entry<Byte, Map<Integer, BuddyLineupVo>> entry : BuddyManager.lineupLevelMap.entrySet()) {
            BuddyLineupVo lineupVo = entry.getValue().get(BuddyManager.BUDDY_INIT_ARMLV);
            if (lineupVo != null) {
                if (lineupVo.getReqRoleLv() == 0 || roleModule.getLevel() >= lineupVo.getReqRoleLv()) {
                    if (lineupVo.getReqVipLv() == 0 || vipModule.getVipLevel() >= lineupVo.getReqVipLv()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private List<Integer> LineUpBuddy() {
        List<Integer> buddyList = new ArrayList<>();
        for (Map.Entry<Byte, RoleBuddyLineup> entry : roleLineupMap.entrySet()) {
            if (entry.getValue().getBuddyId() != 0) {
                buddyList.add(entry.getValue().getBuddyId());
            }
        }
        return buddyList;
    }


    /**
     * 阵型激活
     * 监听事件：1.角色升级事件;2.vip等级升级事件;
     */
    public void activeLineupHandler() {
        List<RoleBuddyLineup> changeList = new LinkedList<>();
        for (byte lineupId : BuddyManager.lineupLevelMap.keySet()) {
            if (!activeLineup(lineupId))
                continue;
            changeList.add(roleLineupMap.get(lineupId));
        }
        if (changeList.isEmpty()) return;
        // send to client
        sendLineupUpdate(changeList);
        changeLineUp();
    }

    public boolean activeLineup(byte lineupId) {
        RoleModule roleModule = module(MConst.Role);
        BuddyLineupVo lineupVo = BuddyManager.getBuddyLineupVo(lineupId, BuddyManager.BUDDY_INIT_ARMLV);
        if (lineupVo == null)
            return false;
        // 已激活
        if (roleLineupMap.containsKey(lineupId))
            return false;
        if (lineupVo.getReqRoleLv() != 0 && roleModule.getLevel() < lineupVo.getReqRoleLv())
            return false;
        VipModule vipModule = module(MConst.Vip);
        // vip等级要求
        if (lineupVo.getReqVipLv() != 0 && vipModule.getVipLevel() < lineupVo.getReqVipLv())
            return false;
        RoleBuddyLineup roleBuddyLineup = new RoleBuddyLineup(id(), lineupId);
        roleLineupMap.put(lineupId, roleBuddyLineup);
        context().insert(roleBuddyLineup);

        return true;
    }

    /**
     * 阵型配置
     *
     * @param lineupId
     * @param buddyId  0=取消配置该位置伙伴
     */
    public void configLineup(byte lineupId, int buddyId) {
        if (BuddyManager.buddyIdGuardMap.containsKey(buddyId)) {
            warn("buddyguard_tips1");
            return;
        }
        List<RoleBuddyLineup> changeList = new LinkedList<>();
        RoleBuddyLineup roleBuddyLineup = roleLineupMap.get(lineupId);
        if (roleBuddyLineup == null)
            return;
        if (buddyId != 0 && !roleBuddyMap.containsKey(buddyId))
            return;
        // 如果伙伴已经配置到阵型中,则替换
        for (RoleBuddyLineup curRoleLineup : roleLineupMap.values()) {
            if (curRoleLineup.getBuddyId() != buddyId)
                continue;
            curRoleLineup.setBuddyId(0);
            context().update(curRoleLineup);
            changeList.add(curRoleLineup);
        }
        roleBuddyLineup.setBuddyId(buddyId);
        context().update(roleBuddyLineup);

        if (canLineUp.contains((int) lineupId)) {
            canLineUp.remove((int) lineupId);
        }
        changeLineUp();
        changeList.add(roleBuddyLineup);
        // 更新阵型属性
        updateAddRoleAttrWithSend();
        // send to client
        sendLineupUpdate(changeList);
    }

    /**
     * 阵型一键配置
     */
    public void autoConfigLineup() {
        List<RoleBuddyLineup> changeList = new LinkedList<>();
        Set<Integer> configuratedIdSet = new HashSet<>();
        for (RoleBuddyLineup roleBuddyLineup : roleLineupMap.values()) {
            BuddyLineupVo buddyLineupVo = BuddyManager.getBuddyLineupVo(roleBuddyLineup.getLineupId(),
                    BuddyManager.BUDDY_INIT_ARMLV);
            if (buddyLineupVo == null)
                continue;
            int topBuddyId = getTopAttrBuddy(buddyLineupVo.getFirstAttrName(), configuratedIdSet);
            if (topBuddyId != 0) {
                configuratedIdSet.add(topBuddyId);
            }
            roleBuddyLineup.setBuddyId(topBuddyId);
            context().update(roleBuddyLineup);
            changeList.add(roleBuddyLineup);
        }
        changeLineUp();
        // send to client
        if (changeList.isEmpty()) return;
        // 更新阵型属性
        updateAddRoleAttrWithSend();
        sendLineupUpdate(changeList);
    }

    public boolean isInLineUp(int buddyId) {
        if (StringUtil.isEmpty(roleLineupMap)) return false;
        for (RoleBuddyLineup roleBuddyLineup : roleLineupMap.values()) {
            if (roleBuddyLineup == null || roleBuddyLineup.getBuddyId() != buddyId) continue;
            return true;
        }
        return false;
    }

    /**
     * 更新阵型附加角色属性
     */
    private void updateLineupAddRoleAttr() {
        Attribute totalAttr = new Attribute();
        // 遍历所有已解锁阵型,累加属性
        for (RoleBuddyLineup roleBuddyLineup : roleLineupMap.values()) {
            if (roleBuddyLineup.getBuddyId() == 0) continue;
            Attribute lineupAddAttr = new Attribute();
            RoleBuddy roleBuddy = roleBuddyMap.get(roleBuddyLineup.getBuddyId());
            BuddyLineupVo lineupVo = BuddyManager.getBuddyLineupVo(roleBuddyLineup.getLineupId(), roleBuddy.getArmLevel());
            if (lineupVo == null) continue;
            for (Map.Entry<String, Integer> entry : lineupVo.getAttrPointMap().entrySet()) {
                int calValue = BuddyUtil.calAddRoleAttr(roleBuddy.getAttribute().get(entry.getKey()),
                        lineupVo.getAttrAddPoint(entry.getKey()));
                lineupAddAttr.setSingleAttr(entry.getKey(), calValue);
            }
            totalAttr.addAttribute(lineupAddAttr);
        }
        /**
         * 守卫属性
         */
        for (Map.Entry<Integer, Map<Integer, BuddyGuardPo>> buddyGuardPoEntry : allBuddyGuardPoMap.entrySet()) {
            Integer groupId = buddyGuardPoEntry.getKey();
            if (openGroupIds.contains(groupId)) {
                for (BuddyGuardPo buddyGuardPo : buddyGuardPoEntry.getValue().values()) {
                    if (buddyGuardPo.getStatus() == 1) {
                        Attribute guardAddAttr = new Attribute();
                        BuddyGuard buddyGuard = buddyGuardPo.getBuddyGuard();
                        RoleBuddy roleBuddy = roleBuddyMap.get(buddyGuard.getBuddyid());
                        // 更新属性
                        roleBuddy.calAttribute();
                        for (Map.Entry<String, Integer> entry : buddyGuard.getAttrPointMap().entrySet()) {
                            int calValue = BuddyUtil.calAddRoleAttr(roleBuddy.getAttribute().get(entry.getKey()),
                                    buddyGuard.getAttrAddPoint(entry.getKey()));
                            guardAddAttr.setSingleAttr(entry.getKey(), calValue);
                        }
                        guardAddAttr.addAttribute(buddyGuard.getFixedAttribute());
                        totalAttr.addAttribute(guardAddAttr);
                    }
                }
            }
        }
        RoleModule roleModule = module(MConst.Role);
        roleModule.updatePartAttr(MConst.Buddy, totalAttr);
        roleModule.updatePartFightScore(MConst.Buddy, FormularUtils.calFightScore(totalAttr));
        //记录玩家伙伴历史最高战力
        int maxHistoryFightScore = context().recordMap().getInt(BUDDY_MAX_HISTORY_FIGHTSCORE, 0);
        if (maxHistoryFightScore < FormularUtils.calFightScore(totalAttr)) {
            context().recordMap().setInt(BUDDY_MAX_HISTORY_FIGHTSCORE, FormularUtils.calFightScore(totalAttr));
        }
    }

    private void updateAddRoleAttrWithSend() {
        updateLineupAddRoleAttr();
        RoleModule roleModule = module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    /**
     * 获得对应属性最高的伙伴
     *
     * @param attrName
     * @param set      去重set
     * @return
     */
    private int getTopAttrBuddy(String attrName, Set<Integer> set) {
        int topAttr = 0;
        int topBuddyId = 0;
        for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
            if (set.contains(roleBuddy.getBuddyId())) continue;
            if (BuddyManager.buddyIdGuardMap.containsKey(roleBuddy.getBuddyId())) {
                continue;
            }
            int attr = roleBuddy.getAttribute().get(attrName);
            if (attr > topAttr) {
                topAttr = attr;
                topBuddyId = roleBuddy.getBuddyId();
            }
        }
        return topBuddyId;
    }

    /**
     * 修改跟随伙伴Id
     *
     * @param newBuddyId=0表示取消当前跟随
     */
    public void changeFollowBuddy(int newBuddyId) {
        if (newBuddyId == followBuddyId)
            return;
        List<RoleBuddy> changeList = new LinkedList<>();
        if (newBuddyId != 0) {
            RoleBuddy newFollowBuddy = roleBuddyMap.get(newBuddyId);
            if (newFollowBuddy == null)
                return;
            newFollowBuddy.setIsFollow(BuddyManager.BUDDY_FOLLOW);
            context().update(newFollowBuddy);
            changeList.add(newFollowBuddy);
        }
        if (followBuddyId != 0) {
            RoleBuddy oldFollowBuddy = roleBuddyMap.get(followBuddyId);
            oldFollowBuddy.setIsFollow(BuddyManager.BUDDY_NOT_FOLLOW);
            context().update(oldFollowBuddy);
            changeList.add(oldFollowBuddy);
        }
        followBuddyId = newBuddyId;
        // send to client
        sendBuddyUpdate(ClientUpdateBuddy.CHANGE_FOLLOW_BUDDY, changeList);
        // 更新跟随的伙伴数据
        ClientFollowBuddyData packet = new ClientFollowBuddyData(roleBuddyMap.get(followBuddyId));
        send(packet);
    }

    /**
     * 修改出战伙伴Id
     *
     * @param newBuddyId=0表示取消当前出战
     */
    public void changeFightBuddy(int newBuddyId) {
        if (newBuddyId == fightBuddyId)
            return;
        List<RoleBuddy> changeList = new LinkedList<>();
        if (newBuddyId != 0) {
            RoleBuddy newFightBuddy = roleBuddyMap.get(newBuddyId);
            if (newFightBuddy == null)
                return;
            newFightBuddy.setIsFight(BuddyManager.BUDDY_FIGHT);
            context().update(newFightBuddy);
            changeList.add(newFightBuddy);
        }
        if (fightBuddyId != 0) {
            RoleBuddy oldFightBuddy = roleBuddyMap.get(fightBuddyId);
            oldFightBuddy.setIsFight(BuddyManager.BUDDY_NOT_FIGHT);
            context().update(oldFightBuddy);
            changeList.add(oldFightBuddy);
        }
        fightBuddyId = newBuddyId;
        // send to client
        sendBuddyUpdate(ClientUpdateBuddy.CHANGE_FIGHT_BUDDY, changeList);
        updateBuddySummary();
        eventDispatcher().fire(new FightBuddyChangeEvent());
    }

    public void putRoleBuddy(RoleBuddy roleBuddy) {
        roleBuddyMap.put(roleBuddy.getBuddyId(), roleBuddy);
    }

    /**
     * 穿装备
     *
     * @param buddyId
     * @param partId
     */
    public void putOnEquip(int buddyId, byte partId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        // 未获得
        if (roleBuddy == null) {
            return;
        }
        // 位置不存在
        if (!roleBuddy.getEquipMap().containsKey(partId)) {
            return;
        }
        // 已穿上
        if (roleBuddy.getEquipMap().get(partId) == BuddyManager.BUDDY_EQUIP_PUTON) {
            return;
        }
        BuddyArmsVo buddyArmsVo = BuddyManager.getBuddyArmVo(buddyId, roleBuddy.getArmLevel());
        int equipItemId = buddyArmsVo.getEquipItemId(partId);
        if (equipItemId == 0) {
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        // 消耗不足
        if (!toolModule.deleteAndSend(equipItemId, 1, EventType.PUTONEQUIP.getCode())) {
            return;
        }
        roleBuddy.putOnEquip(partId);
        roleBuddy.calAttribute();
        context().update(roleBuddy);
        // send to client
        sendBuddyUpdate(ClientUpdateBuddy.PUTON_EQUIP, roleBuddy);
        updateBuddySummary();
        if (buddyId == fightBuddyId) {
            eventDispatcher().fire(new FightBuddyChangeEvent());
        }
        if (isInLineUp(roleBuddy.getBuddyId()) || BuddyManager.buddyIdGuardMap.containsKey(roleBuddy.getBuddyId())) {//在阵营中，更新伙伴给角色增加的战力
            updateAddRoleAttrWithSend();
        }

        //红点检查
        changeArmUp();
    }

    /**
     * 武装升级
     *
     * @param buddyId
     */
    public void upgradeArmLevel(int buddyId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        // 未获得
        if (roleBuddy == null) {
            return;
        }
        BuddyArmsVo buddyArmsVo = BuddyManager.getBuddyArmVo(buddyId, roleBuddy.getArmLevel() + 1);
        // 已满级
        if (buddyArmsVo == null) {
            return;
        }
        // 等级不足
        if (roleBuddy.getLevel() < buddyArmsVo.getReqbuddylv()) {
            return;
        }
        // 阶级不足
        if (roleBuddy.getStageLevel() < buddyArmsVo.getReqStageLv()) {
            return;
        }
        // 装备不全
        if (!roleBuddy.equipAllPutOn()) {
            warn("buddy_tips_noequipup");
            return;
        }
        int preLevel = roleBuddy.getArmLevel();
        roleBuddy.setArmLevel(roleBuddy.getArmLevel() + 1);
        roleBuddy.resetEquip();
        roleBuddy.calAttribute();
        context().update(roleBuddy);
        // send to client
        sendBuddyUpdate(ClientUpdateBuddy.UPGRADE_ARM_LV, roleBuddy);
        updateBuddySummary();
        eventDispatcher().fire(new BuddyUpgradeEvent(buddyId, BuddyUpgradeEvent.ARMLEVELUP, preLevel, roleBuddy.getArmLevel(), getRoleBuddyLevelMap()));
        eventDispatcher().fire(new BuddyAchieveEvent(buddyId, BuddyAchieveEvent.ARMLEVELUP, preLevel, roleBuddy.getArmLevel(), getRoleBuddyLevelMap()));
        if (buddyId == fightBuddyId) {
            eventDispatcher().fire(new FightBuddyChangeEvent());
        }
        if (isInLineUp(roleBuddy.getBuddyId()) || BuddyManager.buddyIdGuardMap.containsKey(roleBuddy.getBuddyId())) {//在阵营中，更新伙伴给角色增加的战力
            updateAddRoleAttrWithSend();
        }

        //红点检查
        changeArmUp();
    }

    /**
     * 所有伙伴等级总和
     *
     * @return
     */
    public int allBuddyLevelSum() {
        int levelSum = 0;
        if (!roleBuddyMap.isEmpty()) {
            for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
                levelSum = levelSum + roleBuddy.getLevel();
            }
        }
        return levelSum;
    }

    /**
     * 判断伙伴是否已配置到阵型
     *
     * @param buddyId
     * @return
     */
    private boolean isConfigurated(int buddyId) {
        for (RoleBuddyLineup roleLineup : roleLineupMap.values()) {
            if (roleLineup.getBuddyId() == buddyId) return false;
        }
        return true;
    }

    /**
     * 更新伙伴常用数据
     */
    private void updateBuddySummary() {
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(id(), new BuddySummaryComponentImpl(roleBuddyMap));
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
        context().markUpdatedSummaryComponent(MConst.Buddy);
    }

    /**
     * 给指定伙伴升一级
     *
     * @param buddyId
     */
    public void upgradeLevel1(int buddyId) {
        if (!canAddExp(buddyId)) {
            return;
        }
        int itemNum = getExpItemNum2NextLv(buddyId);
        ToolModule toolModule = module(MConst.Tool);
        toolModule.useToolByItemId(BuddyManager.expItemId, itemNum, buddyId + "");//这里的第三个参数随意，为了满足道具模块的写法
    }

    /**
     * 获取指定伙伴下一级需要的经验丹
     *
     * @param buddyId
     * @return
     */
    private int getExpItemNum2NextLv(int buddyId) {
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        BuddyLevelVo nextBuddyLevelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel() + 1);
        int needExp = nextBuddyLevelVo.getReqExp() - roleBuddy.getExp();
        int mod = needExp % BuddyManager.expUnit;
        return mod == 0 ? needExp / BuddyManager.expUnit : needExp / BuddyManager.expUnit + 1;
    }

    /**
     * 给指定伙伴升级到当前能升的最大级
     *
     * @param buddyId
     */
    public void upgradeLevelHighest(int buddyId) {
        if (!canAddExp(buddyId)) {
            return;
        }
        RoleBuddy roleBuddy = roleBuddyMap.get(buddyId);
        RoleModule roleModule = module(MConst.Role);
        int needExp = 0;
        for (int level = roleBuddy.getLevel() + 1; ; level++) {
            BuddyLevelVo nextBuddyLevelVo = BuddyManager.getBuddyLevelVo(buddyId, level);
            if (nextBuddyLevelVo == null) break;
            if (nextBuddyLevelVo.getReqRoleLv() > roleModule.getLevel()) {
                if (roleBuddy.getExp() < nextBuddyLevelVo.getReqExp()) {
                    needExp += nextBuddyLevelVo.getReqExp();
                }
                break;
            }
            needExp += nextBuddyLevelVo.getReqExp();
        }
        needExp = needExp - roleBuddy.getExp();
        int mod = needExp % BuddyManager.expUnit;
        int itemNum = mod == 0 ? needExp / BuddyManager.expUnit - 1 : needExp / BuddyManager.expUnit;
        ToolModule toolModule = module(MConst.Tool);
        long ownCount = toolModule.getCountByItemId(BuddyManager.expItemId);
        if (itemNum == 0) {
            return;
        }
        toolModule.useToolByItemId(BuddyManager.expItemId, (int) Math.min(itemNum, ownCount), buddyId + "");
    }

//    public String makeFsStr() {
//        int levelFs = 0; // 等级
//        int armFs = 0; // 悟性
//        int stageFs = 0; // 阶级
//        int lineupFs = 0; // 星阵
//        for (RoleBuddy roleBuddy : roleBuddyMap.values()) {
//            int buddyId = roleBuddy.getBuddyId();
//            BuddyLevelVo levelVo = BuddyManager.getBuddyLevelVo(buddyId, roleBuddy.getLevel());
//            BuddyArmsVo armVo = BuddyManager.getBuddyArmVo(buddyId, roleBuddy.getArmLevel());
//            BuddyStageVo stageVo = BuddyManager.getBuddyStageVo(buddyId, roleBuddy.getStageLevel());
//
//            // 计算等级战力
//            if (levelVo != null) {
//                levelFs += FormularUtils.calFightScore(levelVo.getAttribute());
//            }
//            // 计算悟性战力
//            if (armVo != null) {
//                armFs += FormularUtils.calFightScore(armVo.getArmLevelAttr());
//                for (Map.Entry<Byte, Byte> entry : roleBuddy.getEquipMap().entrySet()) {
//                    if (entry.getValue() == BuddyManager.BUDDY_EQUIP_PUTON) {
//                        armFs += FormularUtils.calFightScore(armVo.getEquipAttr(entry.getKey()));
//                    }
//                }
//            }
//            // 计算阶级战力
//            if (stageVo != null) {
//                stageFs += FormularUtils.calFightScore(stageVo.getAttribute());
//                // 被动技能
//            }
//        }
//        // 计算星阵战力
//        Attribute lineupAttr = new Attribute();
//        for (RoleBuddyLineup roleBuddyLineup : roleLineupMap.values()) {
//            if (roleBuddyLineup.getBuddyId() == 0) continue;
//            Attribute attr = new Attribute();
//            RoleBuddy roleBuddy = roleBuddyMap.get(roleBuddyLineup.getBuddyId());
//            BuddyLineupVo lineupVo = BuddyManager.getBuddyLineupVo(roleBuddyLineup.getLineupId(), roleBuddy.getArmLevel());
//            if (lineupVo == null) continue;
//            for (Map.Entry<String, Integer> entry : lineupVo.getAttrPointMap().entrySet()) {
//                int calValue = BuddyUtil.calAddRoleAttr(lineupVo, roleBuddy, entry.getKey());
//                attr.setSingleAttr(entry.getKey(), calValue);
//            }
//            lineupAttr.addAttribute(attr);
//        }
//        lineupFs = FormularUtils.calFightScore(lineupAttr);
//        //
//        StringBuilder sb = new StringBuilder();
//        sb.append("buddy_level:").append(levelFs).append("#")
//                .append("buddy_arm:").append(armFs).append("#")
//                .append("buddy_stage:").append(stageFs).append("#")
//                .append("buddy_lineup:").append(lineupFs).append("#");
//        return sb.toString();
//    }

    public String makeFsStr() {
        int levelFs = 0; // 等级
        int armFs = 0; // 悟性
        int stageFs = 0; // 阶级

        Attribute levelAttr = new Attribute();
        Attribute armAttr = new Attribute();
        Attribute stageAttr = new Attribute();

        for (RoleBuddy buddyPo : roleBuddyMap.values()) {
            int buddyId = buddyPo.getBuddyId();
            BuddyLevelVo levelVo = BuddyManager.getBuddyLevelVo(buddyId, buddyPo.getLevel());
            BuddyArmsVo armVo = BuddyManager.getBuddyArmVo(buddyId, buddyPo.getArmLevel());
            BuddyStageVo stageVo = BuddyManager.getBuddyStageVo(buddyId, buddyPo.getStageLevel());

            for (RoleBuddyLineup lineupPo : roleLineupMap.values()) {
                if (lineupPo.getBuddyId() == buddyId) { // 如果伙伴在星阵中，则要计算战力
                    BuddyLineupVo lineupVo = BuddyManager.getBuddyLineupVo(lineupPo.getLineupId(), buddyPo.getArmLevel());
                    if (lineupVo != null) {
                        Attribute attr;
                        // 等级
                        attr = new Attribute();
                        for (Map.Entry<String, Integer> entry : lineupVo.getAttrPointMap().entrySet()) {
                            int calValue = BuddyUtil.calAddRoleAttr(lineupVo, levelVo.getAttribute(), entry.getKey());
                            attr.setSingleAttr(entry.getKey(), calValue);
                        }
                        levelAttr.addAttribute(attr);
                        // 悟性
                        attr = new Attribute();
                        Attribute tmpArmAttr = new Attribute();
                        tmpArmAttr.addAttribute(armVo.getArmLevelAttr());
                        for (Map.Entry<Byte, Byte> entry : buddyPo.getEquipMap().entrySet()) { // 计算装备的属性
                            if (entry.getValue() == BuddyManager.BUDDY_EQUIP_PUTON) {
                                tmpArmAttr.addAttribute(armVo.getEquipAttr(entry.getKey()));
                            }
                        }
                        for (Map.Entry<String, Integer> entry : lineupVo.getAttrPointMap().entrySet()) {
                            int calValue = BuddyUtil.calAddRoleAttr(lineupVo, tmpArmAttr, entry.getKey());
                            attr.setSingleAttr(entry.getKey(), calValue);
                        }
                        armAttr.addAttribute(attr);
                        // 阶段
                        attr = new Attribute();
                        for (Map.Entry<String, Integer> entry : lineupVo.getAttrPointMap().entrySet()) {
                            int calValue = BuddyUtil.calAddRoleAttr(lineupVo, stageVo.getAttribute(), entry.getKey());
                            attr.setSingleAttr(entry.getKey(), calValue);
                        }
                        stageAttr.addAttribute(attr);
                    }
                }
            }
        }
        levelFs = FormularUtils.calFightScore(levelAttr);
        armFs = FormularUtils.calFightScore(armAttr);
        stageFs = FormularUtils.calFightScore(stageAttr);
        //
        StringBuilder sb = new StringBuilder();
        sb.append("buddy_level:").append(levelFs).append("#")
                .append("buddy_arm:").append(armFs).append("#")
                .append("buddy_stage:").append(stageFs).append("#");
        return sb.toString();
    }

    private void initGuardData() {
        allBuddyGuardPoMap = new HashMap<>();
        openGroupIds = new HashSet<>();
        for (Map.Entry<Integer, List<BuddyGuard>> entry : BuddyManager.buddyGuardGroupMap.entrySet()) {
            List<BuddyGuard> buddyGuards = entry.getValue();
            Integer groupId = entry.getKey();
            Map<Integer, BuddyGuardPo> buddyGuardPoMap = allBuddyGuardPoMap.get(groupId);
            if (buddyGuardPoMap == null) {
                buddyGuardPoMap = new HashMap<>();
                allBuddyGuardPoMap.put(groupId, buddyGuardPoMap);
            }
            for (BuddyGuard buddyGuard : buddyGuards) {
                int status = buddyGuard.checkRoleOpen(moduleMap());
                buddyGuardPoMap.put(buddyGuard.getPosition(), new BuddyGuardPo(buddyGuard.getPosition(), buddyGuard.getGroupid(), status));
                if (status == 1 || status == 2) {
                    openGroupIds.add(buddyGuard.getGroupid());
                }
            }
        }

    }

    public void onEvent(Event event) {
        if (event instanceof RoleLevelUpEvent || event instanceof VipLevelupEvent) {
            initGuardData();
        }
        if (event instanceof BuddyActiveEvent) {
            BuddyActiveEvent buddyActiveEvent = (BuddyActiveEvent) event;
            int buddyId = buddyActiveEvent.getBuddyId();
            if (BuddyManager.buddyIdGuardMap.containsKey(buddyId)) {
                BuddyGuard buddyGuard = BuddyManager.buddyIdGuardMap.get(buddyId);
                BuddyGuardPo buddyGuardPo = allBuddyGuardPoMap.get(buddyGuard.getGroupid()).get(buddyGuard.getPosition());
                if (buddyGuardPo.getStatus() == 2) {
                    initGuardData();
                    updateAddRoleAttrWithSend();
                }
            }

        }
    }

    public void reqBuddyGuardMain(boolean includeProduct) {
        ClientGuardPacket clientGuardPacket = new ClientGuardPacket(ClientGuardPacket.SEND_BUDDY_GUARD_MAIN);
        clientGuardPacket.setIncludeProductData(includeProduct);
        clientGuardPacket.setAllBuddyGuardPoMap(allBuddyGuardPoMap);
        clientGuardPacket.setOpenGroupIds(openGroupIds);
        send(clientGuardPacket);
    }

}
