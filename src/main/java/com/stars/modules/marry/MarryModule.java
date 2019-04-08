package com.stars.modules.marry;

import com.stars.core.attr.Attribute;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.data.DataManager;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.fashion.FashionManager;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.marry.event.MarrySceneFinishEvent;
import com.stars.modules.marry.packet.ClientMarry;
import com.stars.modules.marry.packet.ClientMarryBattleInfo;
import com.stars.modules.marry.prodata.MarryRingLvl;
import com.stars.modules.marry.summary.MarrySummaryComponentImpl;
import com.stars.modules.marry.userdata.RoleRing;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.Scene;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.scene.imp.fight.MarryDungeonScene;
import com.stars.modules.scene.packet.ClientMarryBattleScore;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.prodata.SafeinfoVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.impl.FashionToolFunc;
import com.stars.modules.tool.func.impl.JobBoxToolFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.services.marry.event.MarryAppointSceneCheckEvent;
import com.stars.services.marry.event.MarryLogEvent;
import com.stars.services.marry.event.MarryToolEvent;
import com.stars.services.marry.event.WeddingActCheckEvent;
import com.stars.services.marry.userdata.Marry;
import com.stars.services.marry.userdata.MarryRole;
import com.stars.services.marry.userdata.MarryWedding;
import com.stars.services.summary.SummaryComponent;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryModule extends AbstractModule {

    // 表白列表，目前只用于红点
    static int MAX_RING_NUM = 1;
    private Set<Long> profressList = new HashSet<>();
    private Marry marry;
    private byte iconFlag; // 只控制婚宴图标
    private Map<Integer, RoleRing> marryRingMap = new HashMap<>();


    public MarryModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("结婚系统", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {

    }

    @Override
    public void onDataReq() throws Exception {
        String sql = "select * from `rolering` where `roleid`=" + id();
        marryRingMap = DBUtil.queryMap(DBUtil.DB_USER, "pos", RoleRing.class, sql);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        playerOnline();
        updateMarryAttr();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        ServiceHelper.marryService().onDailyReset(id());
    }

    @Override
    public void onOffline() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        ServiceHelper.marryService().playerOffline(roleModule.getLastEnterWeddingSceneId(), id());
    }

    @Override
    public void onReconnect() throws Throwable {
        playerOnline();
        updateMarryAttr();
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            long coupleRoleId = 0L;
            byte marryState = 0;
            if (marry != null) {
                coupleRoleId = marry.getOther(id());
                marryState = marry.getState();
            }
            componentMap.put(SummaryConst.C_MARRY, new MarrySummaryComponentImpl(coupleRoleId, marryState));
        }
    }

    /**
     * 更新玩家结婚常用数据
     */
    private void updateSummary() {
        context().markUpdatedSummaryComponent(SummaryConst.C_MARRY);
    }

    public void updateMarryAttr() {
        if (marryRingMap.size() == 0) {
            return;
        }
        Attribute attribute = new Attribute();
        int totalFightScore = 0;
        MarryRingLvl marryRingLvVo;
        for (Map.Entry<Integer, RoleRing> entry : marryRingMap.entrySet()) {
            marryRingLvVo = MarryManager.getMarryRingLvVo(entry.getValue().getRingId(), entry.getValue().getLevel());
            if (null == marryRingLvVo) continue;
            attribute.addAttribute(marryRingLvVo.getAttribute());
            totalFightScore = totalFightScore + marryRingLvVo.getFightPower();
        }

        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_MARRYRING, attribute);
        // 更新战力
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_MARRYRING, totalFightScore);
    }

    public void updateMarryAttrWithSend() {
        updateMarryAttr();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    /**
     * 戒指是否可以激活
     *
     * @return
     */
    public boolean isCanActiveMarryRing(int ringId) {
        // return marryRingMap.size() >= MAX_RING_NUM;
        if (marryRingMap.size() < MAX_RING_NUM) {
            return true;
        } else {
            // 目前只有一个戒指位，直接取位置1
            RoleRing roleRing = marryRingMap.get(MarryManager.RING_POS1);
            MarryRingLvl marryRingLvl = MarryManager.getMarryRingLvVo(roleRing.getRingId(), roleRing.getLevel());
            if (null == marryRingLvl) {
                return false;
            }
            MarryRingLvl marryRingLvlNew = MarryManager.getMarryRingLvVo(ringId, (short) 1);
            if (null == marryRingLvlNew) {
                return false;
            }
            if (marryRingLvl.getFightPower() >= marryRingLvlNew.getFightPower()) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 激活戒指
     *
     * @param id
     */
    public void activeMarryRing(int id) {
        if (marryRingMap == null) {
            marryRingMap = new HashMap<>();
        }
        if (marryRingMap.size() >= MAX_RING_NUM) {
            RoleRing roleRing = marryRingMap.get(MarryManager.RING_POS1);
            // 原戒指返回背包
            ToolModule toolModule = module(MConst.Tool);
            toolModule.addAndSend(roleRing.getRingId(), 1, EventType.MARRY_RING_REPLACE.getCode());
            roleRing.setRingId(id);
            roleRing.setLevel((short) 1);
            context().update(roleRing);
        } else {
            RoleRing ring = new RoleRing(id());
            ring.setRingId(id);
            ring.setLevel((short) 1);
            ring.setPos(MarryManager.RING_POS1);
            context().insert(ring);
            marryRingMap.put(MarryManager.RING_POS1, ring);
        }
        updateMarryAttrWithSend();
    }

    /**
     * 获取戒指信息
     */
    public void getRingList() {
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.MARRY_RING_INFO);
        List<RoleRing> list = new ArrayList<>();
        for (Map.Entry<Integer, RoleRing> entry : marryRingMap.entrySet()) {
            list.add(entry.getValue());
        }
        res.setRingList(list);
        send(res);
    }

    /**
     * 角色登陆完成
     */
    public void playerOnline() {
        RoleModule roleModule = module(MConst.Role);
        MarryRole mc = new MarryRole();
        Role role = roleModule.getRoleRow();
        mc.setRoleId(id());
        mc.setFight(role.getFightScore());
        mc.setName(role.getName());
        mc.setJobId(role.getJobId());
        mc.setLevel(role.getLevel());
        ServiceHelper.marryService().playerOnline(mc);
    }

    /**
     * 操作道具
     * 普通预约和豪华预约消耗需要特殊判断
     *
     * @param mte
     */
    public void handleTool(MarryToolEvent mte) {
        ToolModule toolModule = module(MConst.Tool);
        EventType toolType = MarryManager.toolEventMap.get(mte.getOperator());
        if (null == toolType)
            toolType = EventType.USETOOL;
        if (mte.getHandleType() == MarryToolEvent.ADD) {
            toolModule.addAndSend(mte.getToolMap(), toolType.getCode());
        }
        if (mte.getHandleType() == MarryToolEvent.SUB) {
            if (toolModule.deleteAndSend(mte.getToolMap(), toolType.getCode())) {
                if (mte.getOperator() == MarryManager.TOOL_OPERATOR_APPOINT_GENERAL ||
                        mte.getOperator() == MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS) {
                    ServiceHelper.marryService().toolSubCallback(id(), mte.getOperator(), 1);
                } else {
                    ServiceHelper.marryService().toolSubCallback(id(), mte.getOperator(), mte.getArg());
                }
            } else {
                if (mte.getOperator() == MarryManager.TOOL_OPERATOR_APPOINT_GENERAL ||
                        mte.getOperator() == MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS) {
                    ServiceHelper.marryService().toolSubCallback(id(), mte.getOperator(), 0);
                } else {
                    String toolName = "";
                    for (Integer toolId : mte.getToolMap().keySet()) {
                        ItemVo itemVo = ToolManager.getItemVo(toolId);
                        toolName = itemVo.getName();
                    }
                    warn("marry_activity_tips_cost", toolName);
                }
            }
        }
    }

    /**
     * 进入豪华婚礼场景
     */
    public void enterWeddingScene(String key) {
        SceneModule sm = module(MConst.Scene);
        if (sm.getScene().getSceneType() != SceneManager.SCENETYPE_CITY && sm.getScene().getSceneType() != SceneManager.SCENETYPE_FAMIL)
            return;
        int sceneId = DataManager.getCommConfig("marry_party_stageid", 201);
        RoleModule roleModule = module(MConst.Role);
        roleModule.setLastEnterWeddingSceneId(key);
        sm.enterScene(SceneManager.SCENETYPE_WEDDING, sceneId, key);
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), "进入豪华婚礼场景", true));
        }
    }

    /**
     * 回城
     */
    public void backCity() {
        RoleModule roleModule = module(MConst.Role);
        MarryWedding luxurious = ServiceHelper.marryService().getWeddingSync(roleModule.getLastEnterWeddingSceneId());
        /**
         if (luxurious != null && luxurious.getState() == MarryWedding.RUN
         && (luxurious.getOrder() == id() || luxurious.getOther() == id())) {    // 坑爹的需求，婚礼期间，双方不能退出场景
         warn(I18n.get("marry.wedding.backcity.failed"));
         return;
         }**/
        SceneModule sceneModule = module(MConst.Scene);
        // 本来安全区的场景切换是需要走传送的，但是婚礼场景策划要求不走传送，这里特殊处理一下，需要手动清除上一个场景中的角色
        String sceneStr = roleModule.getJoinSceneStr();
        SafeinfoVo vo = SceneManager.getSafeVo(DataManager.getCommConfig("marry_party_safeposition", 101));
        ServiceHelper.arroundPlayerService().removeArroundPlayer(sceneStr, id());
        roleModule.getRoleRow().setPositionStr(vo.getCharPosition());   // 下面会执行保存,这里不保存了
        roleModule.updateSafeStageId(vo.getSafeId());
        sceneModule.backToCity();
        if (SpecialAccountManager.isSpecialAccount(id())) {
            eventDispatcher().fire(new SpecialAccountEvent(id(), "从婚礼场景回城", true));
        }
    }

    /**
     * 红点
     *
     * @param redPointIds
     * @param redPointMap
     */
    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.MARRY_PROFRESS)) {
            if (profressList.size() == 0 || marry != null) {
                redPointMap.put(RedPointConst.MARRY_PROFRESS, null);
            } else {
                StringBuilder builder = new StringBuilder();
                for (long roleId : profressList) {
                    builder.append(roleId).append("+");
                }
                builder.delete(builder.length() - 1, builder.length());
                redPointMap.put(RedPointConst.MARRY_PROFRESS, builder.toString());
            }
        }
        /**
         if (redPointIds.contains(RedPointConst.MARRY_WEDDING)) {
         MarryWedding wedding = ServiceHelper.marryService().getWeddingSync(getLastEnterWeddingSceneId());
         if (wedding != null && wedding.isMyWedding(id())
         && wedding.getOrder() != id()
         && wedding.getState() == MarryWedding.RUN) {
         redPointMap.put(RedPointConst.MARRY_WEDDING, Boolean.TRUE.toString());
         } else {
         redPointMap.put(RedPointConst.MARRY_WEDDING, null);
         }
         }**/
        if (redPointIds.contains(RedPointConst.MARRY_WEDDING_ICON)) {
            /**
             * 本想借助小红点控制开关，但是前端认为不好处理，还是以模块内定义消息处理
             */
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.WEDDING_ICON);
            res.setIcon(iconFlag);
            send(res);
        }
    }

    /**
     * 表白列表
     *
     * @param profressList
     */
    public void updateProfressList(Set<Long> profressList) {
        this.profressList = profressList;
    }

    /**
     * 更新结婚数据
     *
     * @param marry
     */
    public void updateMarry(Marry marry) {
        this.marry = marry;
        // 下发包更新客户端角色名字
        ClientRole res = new ClientRole(ClientRole.UPDATE_MARRY_NAME, null);
        if (this.marry != null && this.marry.getState() == Marry.MARRIED) {
            RoleSummaryComponent role = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(marry.getOther(id()), MConst.Role);
            res.setMarryName(role.getRoleName());
        } else {
            res.setMarryName("");
        }
        updateSummary();
        send(res);
    }

    /**
     * 获取结婚数据，有延时，使用时注意情境
     *
     * @return
     */
    public Marry getMarry() {
        return marry;
    }

    /**
     * 是否是结义对象
     *
     * @param roleId
     * @return
     */
    public boolean marriageWith(long roleId) {
        if (marry == null) {
            return false;
        }
        return marry.getOther(id()) == roleId;
    }

    /**
     * 是否已经结义
     *
     * @param roleId
     * @return
     */
    public boolean isMarried(long roleId) {
        if (marriageWith(roleId) && marry.getState() == Marry.MARRIED) {
            return true;
        }
        return false;
    }

    public boolean isMarried() {
        return marry != null && marry.getState() == Marry.MARRIED;
    }

    /**
     * 豪华预约场景检查
     *
     * @param ev
     */

    public void handleMarryAppointSceneCheck(MarryAppointSceneCheckEvent ev) {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        Scene scene = sceneModule.getScene();
        boolean result = false;
        if (scene.getSceneType() != SceneManager.SCENETYPE_CITY && scene.getSceneType() != SceneManager.SCENETYPE_FAMIL) {
            result = false;
        } else {
            result = true;
        }
        ServiceHelper.marryService().appointSceneCheckBack(ev.getAppoinder(), ev.getGerder(), ev.getAppType(), result);
    }

    /**
     * ]
     * 处理服务器主动拉入婚宴场景
     */

    public void handleEnterSceneEvent() {
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        Scene scene = sceneModule.getScene();
        if (scene.getSceneType() == SceneManager.SCENETYPE_CITY) {
            enterWeddingScene(marry.getUniqueKey());
        }
    }

    /**
     * 通过红点控制婚宴活动按钮
     *
     * @param ev
     */

    public void handleWeddingActEvent(WeddingActCheckEvent ev) {
        if (ev.getIconFlag() == iconFlag) return;
        iconFlag = ev.getIconFlag();
        signCalRedPoint(MConst.Marry, RedPointConst.MARRY_WEDDING_ICON);
    }

    public void handleMarryLog(MarryLogEvent ev) {
        ServerLogModule serverLogModule = (ServerLogModule) module(MConst.ServerLog);
        serverLogModule.marryLog(ev.getOperateId(), ev.getStaticStr(), ev.getTarget());
    }


    public void showMarryBattle() {
        //玩家要进入
        TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(DataManager.getCommConfig("marry_battlestage_teamdungeonid", 103));
        if (teamDungeonVo == null) {
            com.stars.util.LogUtil.info("缺少情义副本数据，请检查teamdungeon和commondefine:marry_battlestage_teamdungeonid ");
            return;
        }
        //检测次数是否已经达到上限
        short dailyId = teamDungeonVo.getDailyid();
        Map<Integer, Integer> awardMap = StringUtil.toMap(DailyManager.getDailyVo(dailyId).getShowitem(), Integer.class, Integer.class, '=', '|');
        //下发奖励和和次数信息
        RoleModule role = module(MConst.Role);
        ServiceHelper.marryService().sendShipDungeon(id(), role.getLevel(), awardMap);
    }

    /**
     * 进入情义副本
     */
    public void enterMarryBattle(int stageId, TeamDungeonVo teamDungeonVo) {
        //检测玩家是否结婚呢
        if (marry == null || marry.getState() != Marry.MARRIED) {
            warn("marrybattle_intro_notmarry");
            return;
        }
        //进入副本
        SceneModule sceneModule = (SceneModule) module(MConst.Scene);
        sceneModule.enterScene(SceneManager.SCENETYPE_MARRY_DUNGEON,
                stageId, teamDungeonVo);
    }

    public int getRoleMarryBattleStage(TeamDungeonVo teamDungeonVo) {
        RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
        int stageId = teamDungeonVo.getStageIdByLevel(roleModule.getLevel());
        StageinfoVo stageinfoVo = SceneManager.getStageVo(stageId);
        if (stageinfoVo == null) {
            return -1;
        }
        return stageinfoVo.getStageId();
    }

    public void doMarryScoreEvent(long other, int score) {
        SceneModule scene = module(MConst.Scene);
        try {
            MarryDungeonScene fightScene = (MarryDungeonScene) scene.getScene();
            fightScene.marryBattleScoreMap.put(Long.toString(other), score);
            ClientMarryBattleScore client = new ClientMarryBattleScore();
            client.setMarryBattleScoreMap(fightScene.marryBattleScoreMap);
            send(client);
        } catch (Exception e) {
            LogUtil.info("结婚组队，场景强转失败:{} , other:{} , score:{}", id(), other, scene);
            e.printStackTrace();
        }
    }

    public void syncSelfData(long roleId, long other) {
        SceneModule scene = module(MConst.Scene);
        DeityWeaponModule deity = module(MConst.Deity);
        RoleModule role = module(MConst.Role);
        DailyModule daily = module(MConst.Daily);
        int sceneType = scene.getLastSceneType();
        ClientMarryBattleInfo battleInfo = new ClientMarryBattleInfo();
        battleInfo.setRoleId(roleId);
        battleInfo.setDeityWeapon(deity.getCurRoleDeityWeapoonId());
        battleInfo.setFightScore(role.getFightScore());
        battleInfo.setJobId((byte) role.getRoleRow().getJobId());
        battleInfo.setLevel((short) role.getLevel());
        battleInfo.setName(role.getRoleRow().getName());
        battleInfo.setType((byte) 0);
        if (sceneType == SceneManager.SCENETYPE_CITY) {
            TeamDungeonVo teamDungeonVo = TeamDungeonManager.getTeamDungeonVo(DataManager.getCommConfig("marry_battlestage_teamdungeonid", 103));
            short dailyId = teamDungeonVo.getDailyid();
            int dailyRemainCount = daily.getDailyRemain(dailyId);
            if (dailyRemainCount <= 0) {
                battleInfo.setRoleState(ClientMarryBattleInfo.SAFE_NONECOUNT);
            } else {
                battleInfo.setRoleState(ClientMarryBattleInfo.SAFE_HAVECOUNT);
            }
        } else {
            battleInfo.setRoleState(ClientMarryBattleInfo.FIGHTING);
        }
        PlayerUtil.send(other, battleInfo);
    }

    public void finishReward(MarrySceneFinishEvent event) {
        ToolModule toolModule = module(MConst.Tool);
        toolModule.addAndSend(event.getItemMap(), EventType.MARRY_BATTLE.getCode()); //添加物品
        toolModule.addAndSend(event.getExtraItemMap(), EventType.MARRY_BATTLE.getCode());
        ClientStageFinish packet = new ClientStageFinish(event.getStageType(), event.getStatus());
        packet.setItemMap(event.getItemMap());
        packet.setUseTime(event.getUseTime());
        packet.setMySocre(event.getMarryScoreMap().get(Long.toString(id())));
        packet.setMarryScore(getOtherScore(Long.toString(id()), event.getMarryScoreMap()));
        packet.setExtraItemMap(event.getExtraItemMap());
        send(packet);
        ServerLogModule logModule = module(MConst.ServerLog);
        logModule.Log_core_activity(event.getFinish(), ThemeType.ACTIVITY_33.getThemeId(), logModule.makeJuci(),
                ThemeType.ACTIVITY_33.getThemeId(), event.getStageId(), event.getUseTime());
    }

    private int getOtherScore(String roleIds, Map<String, Integer> marryBattleScoreMap) {
        for (Map.Entry<String, Integer> entry : marryBattleScoreMap.entrySet()) {
            if (!entry.getKey().equals(roleIds)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    public void onRoleRename(RoleRenameEvent event) {
        ServiceHelper.marryService().updateRoleName(id(), event.getNewName());
    }

    public void sendMarryFashionState(){
        if(isMarried(id()))
            return;
        int marryFashionItemId = FashionManager.getMarryFashionItemId();
        if(marryFashionItemId <= 0){
            return;
        }
        ItemVo itemVo = ToolManager.getItemVo(marryFashionItemId);
        if(itemVo == null){
            return;
        }
        byte showBuyIcon = (byte)1; //是否需要显示购买按钮
        FashionModule fashionModule = (FashionModule) module(MConst.Fashion);
        if(fashionModule.getIsActive(getMarryFashionId())){
            showBuyIcon = (byte)0;
        }
        ClientMarry clientMarry = new ClientMarry();
        clientMarry.setResType(ClientMarry.SHOW_BUY_MARRY_FASHION_ICON);
        clientMarry.setIsShowBuyIcon(showBuyIcon);
        send(clientMarry);
    }

    public void sendBuyMarryFashionInfo(){
        if(!isMarried()) //未结婚
            return;
        FashionModule fashionModule = (FashionModule) module(MConst.Fashion);
        if(fashionModule.getIsActive(getMarryFashionId())){ //已经拥有
            return;
        }
        int fashionItemId = FashionManager.getMarryFashionItemId();
        int itemCount = FashionManager.getMarryFashionBuyCount();
        int reqCostItemId = FashionManager.getBuyMarryFashionItemId();
        int reqCost = FashionManager.getBuyMarryfashionReqCount();
        ClientMarry clientMarry = new ClientMarry();
        clientMarry.setResType(ClientMarry.SHOW_BUY_MARRY_FASION_INFO);
        clientMarry.setFashionItemId(fashionItemId);
        clientMarry.setItemCount(itemCount);
        clientMarry.setReqItemId(reqCostItemId);
        clientMarry.setReqCost(reqCost);
        send(clientMarry);
    }

    public void buyMarryFashion(){
        if(!isMarried())
            return;
        int fashionItemId = FashionManager.getMarryFashionItemId();
        int itemCount = FashionManager.getMarryFashionBuyCount();
        int reqCostItemId = FashionManager.getBuyMarryFashionItemId();
        int reqCost = FashionManager.getBuyMarryfashionReqCount();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if(!toolModule.contains(reqCostItemId,reqCost)){
            warn("common_tips_noreqitem");
            return;
        }
        //扣除需要道具，增加购买所得
        toolModule.deleteAndSend(reqCostItemId,reqCost,EventType.BUY_MARRY_FASHION.getCode());
        toolModule.addAndSend(fashionItemId, itemCount, EventType.BUY_MARRY_FASHION.getCode());
        ClientMarry clientMarry = new ClientMarry();
        clientMarry.setResType(ClientMarry.MARRY_FASHION_INFO);
        clientMarry.setFashionId(getMarryFashionId());
        send(clientMarry);
    }

    private int getMarryFashionId(){
        int marryFashionItemId = FashionManager.getMarryFashionItemId();
        if(marryFashionItemId <= 0){
            return -1;
        }
        ItemVo itemVo = ToolManager.getItemVo(marryFashionItemId);
        if(itemVo == null){
            return -1;
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        JobBoxToolFunc func = (JobBoxToolFunc) itemVo.getToolFunc();  //找到配置的激活结婚时装道具礼包
        Map<Integer, Map<Integer, Integer>> jobMap = func.getJobToolMap();
        Map<Integer,Integer> toolMap = jobMap.get(roleModule.getRoleRow().getJobId()); //礼包理应是分职业礼包
        for(Integer itemId: toolMap.keySet()){ //理论上，这里是时装激活道具
            ItemVo itemVo1 = ToolManager.getItemVo(itemId);
            if(! (itemVo1.getToolFunc() instanceof FashionToolFunc)){
                continue;
            }
            FashionToolFunc fashionToolFunc = (FashionToolFunc)itemVo1.getToolFunc();
            int fashionId = fashionToolFunc.getFashionId();  //从激活道具可找到对应的结婚时装
            return fashionId;
        }
        return -1;
    }
}
