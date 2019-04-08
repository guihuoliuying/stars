package com.stars.modules.baby;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.baby.event.BabyFashionChangeEvent;
import com.stars.modules.baby.packet.ClientBaby;
import com.stars.modules.baby.prodata.BabyFashion;
import com.stars.modules.baby.prodata.BabySweepVo;
import com.stars.modules.baby.prodata.BabyVo;
import com.stars.modules.baby.usedata.RoleBaby;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.DailyModule;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.userdata.RoleFamilyPo;
import com.stars.modules.familyTask.FamilyTaskManager;
import com.stars.modules.familyTask.FamilyTaskModule;
import com.stars.modules.familyTask.packet.ClientFamilyTask;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.marry.MarryModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.modules.searchtreasure.SearchTreasureModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.DirtyWords;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.util.*;

import static com.stars.modules.baby.BabyConst.*;

/**
 * Created by chenkeyu on 2017-07-20.
 */
public class BabyModule extends AbstractModule {
    private RoleBaby roleBaby;

    public BabyModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleBaby = DBUtil.queryBean(DBUtil.DB_USER, RoleBaby.class, "select * from rolebaby where roleid = " + id());
        if (roleBaby == null) {
            roleBaby = new RoleBaby(id());
            roleBaby.setBabyName("可爱宝宝");
            roleBaby.setLastTimesFeedAction("");
            context().insert(roleBaby);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        if (roleBaby == null) {
            roleBaby = new RoleBaby(id());
            roleBaby.setBabyName("可爱宝宝");
            roleBaby.setLastTimesFeedAction("");
            context().insert(roleBaby);
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        roleBaby.setNormalPrayTimes(0);
        roleBaby.setPayPrayTimes(0);
        roleBaby.setNormalFeedTimes(0);
        roleBaby.setPayFeedTimes(0);
        roleBaby.setExtraFeedOrPrayTimes(0);
        context().update(roleBaby);
    }

    @Override
    public void onSyncData() throws Throwable {
        ClientBaby clientBaby = new ClientBaby(ClientBaby.SEND_USING_FASHION_ID);
        clientBaby.setRoleBaby(roleBaby);
        send(clientBaby);
        clientBaby = new ClientBaby();
        clientBaby.setSubType(ClientBaby.baby_follow);
        clientBaby.setIsFollow(roleBaby.getIsFollow());
        send(clientBaby);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        RoleModule role = module(MConst.Role);
        BabyVo babyVo = BabyManager.getBabyVo(role.getBabyStage(), role.getBabyLevel());
        if (babyVo == null) {
            redPointMap.put(RedPointConst.BABY_PRAY, null);
            redPointMap.put(RedPointConst.BABY_FEED, null);
            redPointMap.put(RedPointConst.BABY_PAY_FEED, null);
            redPointMap.put(RedPointConst.BABY_PAY_PRAY, null);
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (role.getBabyStage() == PRAY) {
            if (roleBaby.getNormalPrayTimes() < BabyManager.getMaxPrayOrFeedCount(role.getBabyStage()) + roleBaby.getExtraFeedOrPrayTimes()
                    && tool.contains(babyVo.getNormalItemId(), babyVo.getNormalCount())) {
                redPointMap.put(RedPointConst.BABY_PRAY, "");
            } else {
                redPointMap.put(RedPointConst.BABY_PRAY, null);
            }
            if (tool.contains(babyVo.getPayItemId(), babyVo.getPayCount())) {
                redPointMap.put(RedPointConst.BABY_PAY_PRAY, "");
            } else {
                redPointMap.put(RedPointConst.BABY_PAY_PRAY, null);
            }
        }
        if (role.getBabyStage() != PRAY) {
            redPointMap.put(RedPointConst.BABY_PRAY, null);
            redPointMap.put(RedPointConst.BABY_PAY_PRAY, null);
            if (roleBaby.getNormalFeedTimes() < BabyManager.getMaxPrayOrFeedCount(role.getBabyStage()) + roleBaby.getExtraFeedOrPrayTimes()
                    && tool.contains(babyVo.getNormalItemId(), babyVo.getNormalCount())
                    && ((role.getBabyStage() == BABY && !BabyManager.isMaxLevel(role.getBabyLevel(), roleBaby.getCurrentProgress()))
                    || roleBaby.getCurrentProgress() < babyVo.getProgress())) {
                redPointMap.put(RedPointConst.BABY_FEED, "");
            } else {
                redPointMap.put(RedPointConst.BABY_FEED, null);
            }
            if (tool.contains(babyVo.getPayItemId(), babyVo.getPayCount())
                    && ((role.getBabyStage() == BABY && !BabyManager.isMaxLevel(role.getBabyLevel(), roleBaby.getCurrentProgress()))
                    || roleBaby.getCurrentProgress() < babyVo.getProgress())) {
                redPointMap.put(RedPointConst.BABY_PAY_FEED, "");
            } else {
                redPointMap.put(RedPointConst.BABY_PAY_FEED, null);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (BabyFashion babyFashion : BabyManager.babyFashionVoMap.values()) {
            if (!isActivedFashion(babyFashion.getId())) {
                int activateCode = babyFashion.getActivateCode();
                ToolModule toolModule = module(MConst.Tool);
                long countByItemId = toolModule.getCountByItemId(activateCode);
                if (countByItemId > 0) {
                    sb.append(babyFashion.getId()).append("+");
//                    redPointMap.put(RedPointConst.BABY_FASHION_ACTIVE, "");
//                } else {
//                    redPointMap.put(RedPointConst.BABY_FASHION_ACTIVE, null);
//                }
                } /*else {
                redPointMap.put(RedPointConst.BABY_FASHION_ACTIVE, null);
            }*/
            }
        }
        redPointMap.put(RedPointConst.BABY_FASHION_ACTIVE, sb.length() > 0 ? sb.toString() : null);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleBaby == null) return;
        checkLevelOpen();
        Attribute roleAttribute = new Attribute();
        RoleModule role = module(MConst.Role);
        List<BabyVo> babyVoList = BabyManager.getBabyVoList(role.getBabyStage(), role.getBabyLevel());
        for (BabyVo babyVo : babyVoList) {
            int count = babyVo.getProgress() / babyVo.getRate();
            for (int i = 0; i < count; i++) {
                roleAttribute.addAttribute(babyVo.getAttribute());//之前的属性
            }
            roleAttribute.addAttribute(babyVo.getExtraAttribute());
        }
        BabyVo babyVo = BabyManager.getBabyVo(role.getBabyStage(), role.getBabyLevel());
        if (babyVo.getStage() != PRAY) {
            int end = roleBaby.getCurrentProgress() > babyVo.getProgress() ? babyVo.getProgress() : roleBaby.getCurrentProgress();
            for (int i = 0; i < end / babyVo.getRate(); i++) {
                roleAttribute.addAttribute(babyVo.getAttribute());//当前等级的属性
            }
            roleAttribute.addAttribute(babyVo.getExtraAttribute());
        } else {
            roleBaby.setPrayStage(babyVo.getExtraAttribute());
        }
        /**
         * 添加宝宝时装属性
         */
        roleAttribute.addAttribute(calBabyFashionAttr());
        roleBaby.setAttribute(roleAttribute);
        roleBaby.setPower(FormularUtils.calFightScore(roleAttribute));
        context().update(roleBaby);
        updateAttrAndFighScore(false);
        signRedPoint();
    }

    /**
     * 检测等级开放
     */
    private void checkLevelOpen() {
        RoleModule roleModule = module(MConst.Role);
        int babyLevel = roleModule.getBabyLevel();
        int babyStage = roleModule.getBabyStage();
        BabyVo babyVo = BabyManager.getBabyVo(babyStage, babyLevel);
        if (roleBaby.getCurrentProgress() >= babyVo.getProgress()) {
            BabyVo nextBabyVo = BabyManager.getBabyVo(babyStage, babyLevel + 1);
            if (nextBabyVo != null) {
                Role roleRow = roleModule.getRoleRow();
                roleRow.setBabyLevel(nextBabyVo.getLevel());
                int deltaProgress = roleBaby.getCurrentProgress() - babyVo.getProgress();
                roleBaby.setCurrentProgress(deltaProgress);
            }
        }
    }

    public Attribute calBabyFashionAttr() {
        /**
         * 宝宝时装
         */
        Attribute babyFashionAttribute = new Attribute();
        for (int fashionId : roleBaby.getOwnFashionIdSet()) {
            BabyFashion babyFashion = BabyManager.babyFashionVoMap.get(fashionId);
            Attribute fashionAttr = new Attribute(babyFashion.getAttr());
            babyFashionAttribute.addAttribute(fashionAttr);
        }
        return babyFashionAttribute;
    }

    private void updateAttrAndFighScore(boolean send) {
        RoleModule role = module(MConst.Role);
        role.updatePartAttr(MConst.Baby, roleBaby.getAttribute());
        role.updatePartFightScore(MConst.Baby, roleBaby.getPower());
        if (send) {
            role.sendRoleAttr();
            role.sendUpdateFightScore();
        }
    }

    public void view() {
        RoleModule role = module(MConst.Role);
        BabyVo babyVo = BabyManager.getBabyVo(role.getBabyStage(), role.getBabyLevel());
        sendRoleData(babyVo);
        signRedPoint();
    }

    public void follow(byte isFollow) {
        roleBaby.setIsFollow(isFollow);
        context().update(roleBaby);
        ClientBaby clientBaby = new ClientBaby();
        clientBaby.setSubType(ClientBaby.baby_follow);
        clientBaby.setIsFollow(roleBaby.getIsFollow());
        send(clientBaby);
        RoleModule role = module(MConst.Role);
        ServiceHelper.arroundPlayerService().updateBabyFollow(role.getJoinSceneStr(), id(), isFollow);
    }

    public void prayOrBringUp(int type) {
        ToolModule tool = module(MConst.Tool);
        RoleModule role = module(MConst.Role);
        BabyVo babyVo = BabyManager.getBabyVo(role.getBabyStage(), role.getBabyLevel());
        int FEED_MULTIPLE = 1;
        int PRAY_FAIL_LUCKY_UP = 0;
        double PRAY_ARGS = 1.0;
        int FEED_CRIT_MULTI = 1;
        int CRIT_PER = 1;
        int TIPS = 1;
        String baby_Noitem = DataManager.getGametext("baby_noitem");
        ServerLogModule logModule = module(MConst.ServerLog);
        int itemId;
        int count;
        if (type == NORMAL_PRAY_OR_FEED) {
            if (!tool.deleteAndSend(babyVo.getNormalItemId(), babyVo.getNormalCount(), EventType.BABY_PRAY_OR_FEED.getCode())) {
                warn(String.format(baby_Noitem, getItemName(babyVo.getNormalItemId())));
                return;
            }
            if (role.getBabyStage() == PRAY && roleBaby.getNormalPrayTimes() >= BabyManager.getMaxPrayOrFeedCount(role.getBabyStage()) + roleBaby.getExtraFeedOrPrayTimes()) {
                warn("baby_pray_notime");
                return;
            }
            if (role.getBabyStage() != PRAY && roleBaby.getNormalFeedTimes() >= BabyManager.getMaxPrayOrFeedCount(role.getBabyStage()) + roleBaby.getExtraFeedOrPrayTimes()) {
                warn("baby_feed_notime");
                return;
            }
            PRAY_FAIL_LUCKY_UP = BabyManager.NORMAL_LUCKY_VALUE;
            PRAY_ARGS = BabyManager.NORMAL_RATE;
            FEED_CRIT_MULTI = BabyManager.NORMAL_CRIT;
            CRIT_PER = BabyManager.NORMAL_CRIT_PER;
            itemId = babyVo.getNormalItemId();
            count = babyVo.getNormalCount();
        } else if (type == PAY_PRAY_OR_FEED) {
            if (!tool.deleteAndSend(babyVo.getPayItemId(), babyVo.getPayCount(), EventType.BABY_PRAY_OR_FEED.getCode())) {
                warn(String.format(baby_Noitem, getItemName(babyVo.getPayItemId())));
                return;
            }
            FEED_MULTIPLE = BabyManager.PAY_MULTIPLE;
            PRAY_FAIL_LUCKY_UP = BabyManager.PAY_LUCKY_VALUE;
            PRAY_ARGS = BabyManager.PAY_RATE;
            FEED_CRIT_MULTI = BabyManager.PAY_CRIT;
            CRIT_PER = BabyManager.PAY_CRIT_PER;
            TIPS = 3;
            itemId = babyVo.getPayItemId();
            count = babyVo.getPayCount();
        } else {
            warn("求子or培养类型出错:" + type);
            com.stars.util.LogUtil.info("求子or培养类型出错:{}", type);
            return;
        }
        if (role.getBabyStage() == PRAY) {

//            int randomValue = (int) (roleBaby.getCurrentProgress() * PRAY_ARGS);
            int randomValue = (int) (PRAY_ARGS + Math.pow((double) (roleBaby.getCurrentProgress() / (double) babyVo.getProgress()), 3.0) * 1000);
            int random = new Random().nextInt(1000) + 1;
            com.stars.util.LogUtil.info("普通求子|randomValue:{},random:{},args:{},curPro:{},pro:{}", randomValue, random, PRAY_ARGS, roleBaby.getCurrentProgress(), babyVo.getProgress());
            int result;
            if (random <= randomValue) {
                // TODO: 2017-07-21 成功
                BabyVo nextBabyVo = BabyManager.getNextBabyVo(role.getBabyStage(), role.getBabyLevel());
                roleBaby.setCurrentProgress(0);
                role.getRoleRow().setBabyStage(nextBabyVo.getStage());
                role.getRoleRow().setBabyLevel(nextBabyVo.getLevel());
//                roleBaby.getAttribute().addAttribute(nextBabyVo.getAttribute());
                roleBaby.getAttribute().addAttribute(nextBabyVo.getExtraAttribute());
                roleBaby.setPower(FormularUtils.calFightScore(roleBaby.getAttribute()));
                updateAttrAndFighScore(false);
                result = 1;
            } else {
                if (type == NORMAL_PRAY_OR_FEED) {
                    roleBaby.addNormalPrayTimes();
                } else {
                    roleBaby.addPayPrayTimes();
                }
                roleBaby.addCurrentProgress(PRAY_FAIL_LUCKY_UP);
                int index = new Random().nextInt(BabyManager.PRAY_FAIL_TIPS.size());
                warn(String.format(BabyManager.PRAY_FAIL_TIPS.get(index), PRAY_FAIL_LUCKY_UP));
                result = 2;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("consume@number:").append(itemId).append("@").append(count).append("#").
                    append("lucky:").append(roleBaby.getCurrentProgress()).append("#").append("result:").append(result);
            logModule.dynamic_4_Log_for_Baby_or_Refine(ThemeType.DYNAMIC_BABY.getThemeId(), "baby_wish", sb.toString());
        } else {
            if (type == NORMAL_PRAY_OR_FEED) {
                roleBaby.addNormalFeedTimes();
            } else {
                roleBaby.addPayFeedTimes();
            }
            int random = new Random().nextInt(1000) + 1;
            int addDelta;
            int currentProgress = roleBaby.getCurrentProgress();
            if (random <= CRIT_PER) {
                // TODO: 2017-07-24 暴击？
                TIPS++;
                addDelta = babyVo.getRate() * FEED_MULTIPLE * FEED_CRIT_MULTI;
            } else {
                addDelta = babyVo.getRate() * FEED_MULTIPLE;
            }
            roleBaby.addCurrentProgress(addDelta);
            if (roleBaby.getCurrentProgress() >= babyVo.getProgress() && role.getBabyStage() == BABY) {
                // TODO: 2017-07-21 升级
                BabyVo nextBabyVo = BabyManager.getNextBabyVo(role.getBabyStage(), role.getBabyLevel());
                if (nextBabyVo != null) {
                    roleBaby.setCurrentProgress(0);
                    role.getRoleRow().setBabyStage(nextBabyVo.getStage());
                    role.getRoleRow().setBabyLevel(nextBabyVo.getLevel());
                    addDelta = (babyVo.getProgress() - currentProgress);
                } else {
                    roleBaby.setCurrentProgress(babyVo.getProgress());
                    addDelta = (babyVo.getProgress() - currentProgress);
                }
            } else if (roleBaby.getCurrentProgress() >= babyVo.getProgress() && role.getBabyStage() != BABY) {
                addDelta = (babyVo.getProgress() - currentProgress);
            }
            for (int i = 0; i < addDelta / babyVo.getRate(); i++) {
                roleBaby.getAttribute().addAttribute(babyVo.getAttribute());
            }
            roleBaby.setLastTimesFeedAction(String.format(BabyManager.getFeedTips(type, role.getBabyStage()), role.getRoleRow().getName(), addDelta));
            roleBaby.setPower(FormularUtils.calFightScore(roleBaby.getAttribute()));
            updateAttrAndFighScore(true);
            ClientBaby clientBaby = new ClientBaby();
            clientBaby.setSubType(ClientBaby.feed_tips);
            clientBaby.setTips(Integer.parseInt(role.getBabyStage() + "" + TIPS));
            send(clientBaby);
            StringBuilder sb = new StringBuilder();
            sb.append("consume@number:").append(itemId).append("@").append(count).append("#")
                    .append("style@lv:").append(role.getBabyStage()).append("@").append(role.getBabyLevel() == 0 ? 1 : role.getBabyLevel());
            logModule.dynamic_4_Log_for_Baby_or_Refine(ThemeType.DYNAMIC_BABY.getThemeId(), "baby_feed", sb.toString());
        }
        sendRoleData(babyVo);
        context().update(roleBaby);
        context().update(role.getRoleRow());
        signRedPoint();
    }

    public void signRedPoint() {
        signCalRedPoint(MConst.Baby, RedPointConst.BABY_PRAY);
        signCalRedPoint(MConst.Baby, RedPointConst.BABY_FEED);
        signCalRedPoint(MConst.Baby, RedPointConst.BABY_PAY_PRAY);
        signCalRedPoint(MConst.Baby, RedPointConst.BABY_PAY_FEED);
        signCalRedPoint(MConst.Baby, RedPointConst.BABY_FASHION_ACTIVE);
    }

    public byte isBabyFollow() {
        return roleBaby.getIsFollow();
    }

    public int getBabyFashion() {
        return roleBaby.getUsingFashionId();
    }

    private String getItemName(int itemId) {
        return DataManager.getGametext(ToolManager.getItemName(itemId));
    }

    private void sendRoleData(BabyVo babyVo) {
        RoleModule role = module(MConst.Role);
        ClientBaby baby = new ClientBaby();
        baby.setSubType(ClientBaby.defaultValue);
        baby.setRoleBaby(roleBaby);
        baby.setBabyVo(babyVo);
        baby.setBabyStage(role.getBabyStage());
        baby.setBabyLevel(role.getBabyLevel());
        baby.setMaxBabyLevel(BabyManager.getBabyMaxLevel());
        if (roleBaby.getChangeName() == CHANGE) {
            baby.setReqItemId(BabyManager.CHANGENAME_REQ_ITEMID);
            baby.setReqItemCount(BabyManager.CHANGENAME_REQ_ITEMCOUNT);
        }
        VipModule vip = module(MConst.Vip);
        VipinfoVo vipinfoVo = VipManager.getVipinfoVo(vip.getVipLevel());
        if (vipinfoVo != null) {
            baby.setCanBuyTime(vipinfoVo.getCanBuyForBaby() - roleBaby.getExtraFeedOrPrayTimes());
            baby.setReqGold(vipinfoVo.getReqGoldForBaby());
        }
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_BASE, role.getRoleRow());
        send(clientRole);
        send(baby);
    }

    public void updateStage() {
        RoleModule role = module(MConst.Role);
        if (role.getBabyStage() == BABY) {
            warn("baby_feed_maxlv");
            com.stars.util.LogUtil.info("宝宝满阶段|roleId:{}, stage:{},level:{}", id(), role.getBabyStage(), role.getBabyLevel());
            return;
        }
        BabyVo babyVo = BabyManager.getBabyVo(role.getBabyStage(), role.getBabyLevel());
        if (roleBaby.getCurrentProgress() >= babyVo.getProgress()) {
            BabyVo nextBabyVo = BabyManager.getNextBabyVo(role.getBabyStage(), role.getBabyLevel());
            roleBaby.setCurrentProgress(0);
            role.getRoleRow().setBabyStage(nextBabyVo.getStage());
            role.getRoleRow().setBabyLevel(nextBabyVo.getLevel());
            roleBaby.getAttribute().addAttribute(nextBabyVo.getExtraAttribute());
            roleBaby.setPower(FormularUtils.calFightScore(roleBaby.getAttribute()));
            updateAttrAndFighScore(true);
            sendRoleData(babyVo);
            context().update(roleBaby);
            context().update(role.getRoleRow());
        } else {
            warn("baby_feed_notproject");
            com.stars.util.LogUtil.info(" {} 玩家进度未满 stage:{},level:{},pro:{}", id(), role.getBabyStage(), role.getBabyLevel(), roleBaby.getCurrentProgress());
        }
    }

    public void getSweepCount() {
        Map<Integer, Byte> sweepCount = new HashMap<>();
        DailyModule daily = module(MConst.Daily);
        MarryModule marry = module(MConst.Marry);
        SearchTreasureModule stm = module(MConst.SearchTreasure);
        FamilyTaskModule familyTaskModule=module(MConst.FamilyTask);
        sweepCount.put(BabyConst.RIDE_SWEEP, (byte) daily.getDailyRemain(DailyManager.DAILYID_PRODUCEDUNGEON_RIDE));
        sweepCount.put(BabyConst.STONE_SWEEP, (byte) daily.getDailyRemain(DailyManager.DAILYID_PRODUCEDUNGEON_STRENGTHEN_STONE));
        sweepCount.put(BabyConst.MARRY_SWEEP, (byte) (marry.isMarried() ? ServiceHelper.marryService().getRemainTeamDungeon(id()) : -1));
        sweepCount.put(BabyConst.SEARCHTREASURE_SWEEP, (byte) stm.getRecordMapSearchTreasure().getDailyRemainInCount());
        sweepCount.put(BabyConst.FAMILY_TASK_NORMAL, (byte)familyTaskModule.getRemainNotCommitTaskCount());
        ClientBaby clientBaby = new ClientBaby();
        clientBaby.setSubType(ClientBaby.sweep_vo);
        clientBaby.setSweepCount(sweepCount);
        send(clientBaby);
    }

    public void sweep(int id, int extendId) {
        if (!BabyManager.SWEEP) {
            warn("宝宝扫荡功能暂时关闭");
            return;
        }
        RoleModule role = module(MConst.Role);
        ToolModule tool = module(MConst.Tool);
        BabySweepVo babySweepVo = BabyManager.getBabySweepVo(id, role.getLevel(), roleBaby.getSweepMark());
        if (babySweepVo == null) {
            warn("没有对应的产品数据");
            com.stars.util.LogUtil.info("没有对应的产品数据|roleId:{},level:{},mark:{}", id, role.getLevel(), roleBaby.getSweepMark());
            return;
        }
        Map<Integer, Integer> itemMap = new HashMap<>(babySweepVo.getItemMap());
        if (role.getBabyStage() < babySweepVo.getReqStage()) {
            warn("baby_rush_lackstage");
            return;
        }
        if (role.getBabyLevel() < babySweepVo.getReqLevel()) {
            warn("baby_rush_lacklv");
            return;
        }
        if (role.getBabyEnergy() < babySweepVo.getSweepConsume()) {
            warn("baby_rush_lowenergy");
            return;
        }
        ServerLogModule logModule = module(MConst.ServerLog);
        short dailyId = BabyManager.getDailyId(id);
        ForeShowModule open = module(MConst.ForeShow);
        if (dailyId != -1) {
            DailyModule daily = module(MConst.Daily);
            if (daily.getDailyRemain(dailyId) <= 0) {
                warn("baby_rush_outtalk");
                return;
            }
            if (dailyId == DailyManager.DAILYID_PRODUCEDUNGEON_RIDE && !open.isOpen(ForeShowConst.DAILY_RIDE)) {
                warn("坐骑副本尚未开放");
                return;
            }
            if (dailyId == DailyManager.DAILYID_PRODUCEDUNGEON_STRENGTHEN_STONE && !open.isOpen(ForeShowConst.DAILY_PRODUCE)) {
                warn("强化石副本尚未开放");
                return;
            }
            if (!tool.deleteAndSend(ToolManager.BABY_ENERGY, babySweepVo.getSweepConsume(), EventType.BABY_SWEEP.getCode())) {
                warn("baby_rush_lowenergy");
                return;
            }
            tool.addAndSend(itemMap, EventType.BABY_SWEEP.getCode());
            ClientBaby clientAward = new ClientBaby();
            clientAward.setSubType(ClientBaby.sweep_result);
            clientAward.setItemMap(itemMap);
            send(clientAward);
            eventDispatcher().fire(new DailyFuntionEvent(dailyId, 1));
            eventDispatcher().fire(new DailyAwardCheckEvent());
            dolog((int) dailyId);
        } else if (id == BabyConst.MARRY_SWEEP) {
            MarryModule marry = module(MConst.Marry);
            if (!marry.isMarried()) {
                warn("baby_rush_notmarry!");
                return;
            }
            int remainCount = 0;
            try {
                remainCount = ServiceHelper.marryService().getRemainTeamDungeon(id());
            } catch (Exception e) {
                com.stars.util.LogUtil.info("获取结婚组队次数的时候超时了:{}", id());
                e.printStackTrace();
            }
            if (remainCount <= 0) {
                warn("baby_rush_outtalk");
                return;
            }
            if (!tool.deleteAndSend(ToolManager.BABY_ENERGY, babySweepVo.getSweepConsume(), EventType.BABY_SWEEP.getCode())) {
                warn("baby_rush_lowenergy");
                return;
            }
            List<Long> roleIds = new ArrayList<>();
            roleIds.add(id());
            tool.addAndSend(itemMap, EventType.BABY_SWEEP.getCode());
            ClientBaby clientAward = new ClientBaby();
            clientAward.setSubType(ClientBaby.sweep_result);
            clientAward.setItemMap(itemMap);
            send(clientAward);
            ServiceHelper.marryService().addDungeon(roleIds);
        } else if (id == BabyConst.SEARCHTREASURE_SWEEP) {
            SearchTreasureModule stm = module(MConst.SearchTreasure);
            int remainCount = stm.getRecordMapSearchTreasure().getDailyRemainInCount();
            int mark_search = stm.getRecordMapSearchTreasure().getMapSearchState();
            if (remainCount <= 0 /*|| mark_search != SearchTreasureConstant.SEARCH_PROCESS_NONE*/) {
                warn("baby_rush_outtalk");
                return;
            }
            if (!open.isOpen(ForeShowConst.DAILY_SEARCH)) {
                warn("六国寻宝暂未开放");
                return;
            }
            if (!tool.deleteAndSend(ToolManager.BABY_ENERGY, babySweepVo.getSweepConsume(), EventType.BABY_SWEEP.getCode())) {
                warn("baby_rush_lowenergy");
                return;
            }
            roleBaby.setSweepMark(BabyManager.getNextMark(roleBaby.getSweepMark()));
            context().update(roleBaby);
            tool.addAndSend(itemMap, EventType.BABY_SWEEP.getCode());
            ClientBaby clientAward = new ClientBaby();
            clientAward.setSubType(ClientBaby.sweep_result);
            clientAward.setItemMap(itemMap);
            send(clientAward);
            stm.getRecordMapSearchTreasure().setMapId0AndFinish();
            stm.dispatchDailyEvent();
            eventDispatcher().fire(new DailyAwardCheckEvent());
        } else if (id == BabyConst.FAMILY_TASK_NORMAL || id == BabyConst.FAMILY_TASK_MIDDLE || id == BabyConst.FAMILY_TASK_HIGH) {
           FamilyTaskModule familyTaskModule=module(MConst.FamilyTask);
            FamilyModule familyModule = module(MConst.Family);
            RoleFamilyPo roleFamilyPo = familyModule.getRoleFamilyPo();
            Map<Integer, Byte> familyTaskMap = roleFamilyPo.getFamilyTaskMap();
            if (!familyTaskMap.containsKey(extendId)) {
                warn("无此家族任务");
                return;
            }
            if (familyTaskMap.get(extendId) != FamilyTaskManager.ALREADY_COMMIT) {
                if (!tool.deleteAndSend(ToolManager.BABY_ENERGY, babySweepVo.getSweepConsume(), EventType.BABY_SWEEP.getCode())) {
                    warn("baby_rush_lowenergy");
                    return;
                }
                familyTaskMap.put(extendId, FamilyTaskManager.ALREADY_COMMIT);
                familyTaskModule.addDailyTaskTimes(false);
                familyTaskModule.openSelfInfoUI(ClientFamilyTask.RESP_VIEW_SELF_UI);
                context().update(roleFamilyPo);
                tool.addAndSend(itemMap, EventType.BABY_SWEEP.getCode());
                ClientBaby clientAward = new ClientBaby();
                clientAward.setSubType(ClientBaby.sweep_result);
                clientAward.setItemMap(itemMap);
                send(clientAward);
            } else {
                warn("当前家族任务已完成");
            }

        } else {
            warn("扫荡类型错误");
        }
        send(new ClientRole(ClientRole.UPDATE_RESOURCE, role.getRoleRow()));
        StringBuilder sb = new StringBuilder();
        sb.append("activity_id@number:").append(id).append("@").append(babySweepVo.getSweepConsume()).append("#")
                .append("reward@number:").append(ServerLogModule.itemMapStr(itemMap));
        logModule.dynamic_4_Log_for_Baby_or_Refine(ThemeType.DYNAMIC_BABY.getThemeId(), "baby_play", sb.toString());
    }

    /**
     * 脱裤子放屁的运营日志
     *
     * @param dailyId
     */
    private void dolog(int dailyId) {
        byte produceDungeonType = 0;
        for (Map.Entry<Byte, Map<Integer, ProduceDungeonVo>> entry : DungeonManager.produceDungeonVoMap.entrySet()) {
            for (Map.Entry<Integer, ProduceDungeonVo> voEntry : entry.getValue().entrySet()) {
                ProduceDungeonVo vo = voEntry.getValue();
                if (vo.getDailyId() == dailyId) {
                    produceDungeonType = entry.getKey();
                }
            }
        }
        if (produceDungeonType != 0) {
            DungeonModule dungeonModule = (DungeonModule) moduleMap().get(MConst.Dungeon);
            ProduceDungeonVo produceDungeonVo = dungeonModule.getEnterProduceDungeonVo(produceDungeonType);
            if (produceDungeonVo == null) return;
            StageinfoVo stageinfoVo = SceneManager.getStageVo(produceDungeonVo.getStageId());
            if (stageinfoVo == null) return;
            ThemeType themeType = getLogThemeType(produceDungeonType);
            if (themeType != null) {
                ServerLogModule logModule = module(MConst.ServerLog);
                logModule.Log_core_activity(ServerLogConst.ACTIVITY_START, themeType.getThemeId(), logModule.makeJuci(),
                        themeType.getThemeId(), stageinfoVo.getStageId(), 0);
            }
        }
    }

    private ThemeType getLogThemeType(byte produceDungeonType) {
        ThemeType themeType = null;
        switch (produceDungeonType) {
            case SceneManager.PRODUCE_ROLEEXP:
                themeType = ThemeType.ACTIVITY_18;
                break;
            case SceneManager.PRODUCE_STRENGTHEN_STONE:
                themeType = ThemeType.ACTIVITY_22;
                break;
            case SceneManager.PRODUCE_RIDEFOOD:
                themeType = ThemeType.ACTIVITY_25;
                break;
        }
        return themeType;
    }

    public void sendAttrTips() {
        RoleModule role = module(MConst.Role);
        role.sendRoleAttr();
        role.sendUpdateFightScore();
    }

    public void changeBabyName(String name) {
        if (!isNameLegal(name)) return;
        if (roleBaby.getChangeName() == UN_CHANGE) {
            roleBaby.setBabyName(name);
            roleBaby.setChangeName(CHANGE);
        } else {
            ToolModule tool = module(MConst.Tool);
            if (!tool.contains(BabyManager.CHANGENAME_REQ_ITEMID, BabyManager.CHANGENAME_REQ_ITEMCOUNT)) {
                warn("baby_name_notgold");
                return;
            }
            tool.deleteAndSend(BabyManager.CHANGENAME_REQ_ITEMID, BabyManager.CHANGENAME_REQ_ITEMCOUNT, EventType.BABY_CHANGE_NAME.getCode());
            roleBaby.setBabyName(name);
        }
        ClientBaby clientBaby = new ClientBaby();
        clientBaby.setSubType(ClientBaby.change_name);
        clientBaby.setNewName(roleBaby.getBabyName());
        send(clientBaby);
        context().update(roleBaby);
        view();
    }

    private boolean isNameLegal(String name) {
        if (StringUtil.isNotEmpty(name)) {
            //判断是否符合限定的长度;
            String[] tmpStrArr = DataManager.getCommConfig("randomname_length").split("\\+");
            int minBytesCount = Integer.parseInt(tmpStrArr[0]);
            int maxBytesCount = Integer.parseInt(tmpStrArr[1]);
            int curRoleNameBytesCount = name.length();
            if (curRoleNameBytesCount > maxBytesCount) {
                com.stars.network.server.packet.PacketManager.send(id(), new ClientText(MConst.CCLogin, "randomename_toolong"));
                return false;
            }
            if (curRoleNameBytesCount < minBytesCount) {
                com.stars.network.server.packet.PacketManager.send(id(), new ClientText(MConst.CCLogin, "randomename_tooshort"));
                return false;
            }
            if (isContainDirtyWords(name)) {
                com.stars.network.server.packet.PacketManager.send(id(), new ClientText(MConst.CCLogin, "randomename_unablecharacter"));
                return false;
            }
            if (!StringUtil.isValidString(name)) {
                com.stars.network.server.packet.PacketManager.send(id(), new ClientText(MConst.CCLogin, "randomename_unablecharacter"));
                return false;
            }
            return true;
        }
        PacketManager.send(id(), new ClientText(MConst.CCLogin, "名字不能为空!"));
        return false;
    }

    private boolean isContainDirtyWords(String str_) {
        return DirtyWords.checkName(str_);
    }

    public void addExtraTimes(int times) {
        VipModule vip = module(MConst.Vip);
        VipinfoVo vipinfoVo = VipManager.getVipinfoVo(vip.getVipLevel());
        if (vipinfoVo == null) return;
        if (times > vipinfoVo.getCanBuyForBaby() - roleBaby.getExtraFeedOrPrayTimes()) {
            warn("baby_buytimes_maxtime");
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (!tool.contains(ToolManager.BANDGOLD, vipinfoVo.getReqGoldForBaby())) {
            warn("baby_baytimes_materialshort");
            return;
        }
        tool.deleteAndSend(ToolManager.BANDGOLD, vipinfoVo.getReqGoldForBaby(), EventType.BABY_BUY_TIMES.getCode());
        roleBaby.addExtraFeedOrPrayTimes(times);
        context().update(roleBaby);
        ClientBaby clientBaby = new ClientBaby();
        clientBaby.setSubType(ClientBaby.can_buy_count);
        clientBaby.setCanBuyTime(vipinfoVo.getCanBuyForBaby() - roleBaby.getExtraFeedOrPrayTimes());
        clientBaby.setReqGold(vipinfoVo.getReqGoldForBaby());
        send(clientBaby);
    }

    /**
     * 是否激活此时装
     *
     * @param fashionId
     * @return
     */
    public boolean isActivedFashion(Integer fashionId) {
        return roleBaby.isOwnFashionId(fashionId);
    }

    /**
     * 激活时装
     *
     * @param fashionId
     */
    public void activeFashion(Integer fashionId) {
        roleBaby.addFashionId(fashionId);
        BabyFashion babyFashion = BabyManager.babyFashionVoMap.get(fashionId);
        Attribute fashionAttr = new Attribute(babyFashion.getAttr());
        roleBaby.getAttribute().addAttribute(fashionAttr);
        roleBaby.setPower(FormularUtils.calFightScore(roleBaby.getAttribute()));
        context().update(roleBaby);
        com.stars.util.LogUtil.info("roleid:{} activeFashion :{} success", id(), fashionId);
        updateAttrAndFighScore(true);
    }

    /**
     * 请求激活宝宝时装
     *
     * @param fashionId
     */
    public void reqActiveFashion(Integer fashionId) {
        com.stars.util.LogUtil.info("roleid:{} reqActiveFashion:{}", id(), fashionId);
        boolean actived = isActivedFashion(fashionId);
        if (!actived) {
            BabyFashion babyFashion = BabyManager.babyFashionVoMap.get(fashionId);
            int itemId = babyFashion.getActivateCode();
            ToolModule toolModule = module(MConst.Tool);
            if (toolModule.contains(itemId, 1)) {
                boolean success = toolModule.deleteAndSend(itemId, 1, EventType.BABY_FASHION_ACTIVE.getCode());
                if (success) {
                    activeFashion(fashionId);
                    ClientBaby clientBaby = new ClientBaby(ClientBaby.SEND_ACTIVE_FASHION_SUCCESS);
                    clientBaby.setFashionId(fashionId);
                    send(clientBaby);
                    reqUseFashionById(fashionId);
                } else {
                    warn("道具扣除失败");
                }
            } else {
                warn("激活道具不足");
            }
        } else {
            warn("已经激活此宝宝时装");
        }
    }

    /**
     * 请求宝宝时装列表
     */
    public void reqBabyFashionList() {
        ClientBaby clientBaby = new ClientBaby(ClientBaby.SEND_FASHION_LIST);
        clientBaby.setRoleBaby(roleBaby);
        send(clientBaby);
    }

    /**
     * 请求使用指定时装
     *
     * @param fashionId
     */
    public void reqUseFashionById(int fashionId) {
        LogUtil.info("roleid:{} use fashion:{}", id(), fashionId);
        roleBaby.setUsingFashionId(fashionId);
        context().update(roleBaby);
        ClientBaby clientBaby = new ClientBaby(ClientBaby.SEND_USING_FASHION_ID);
        clientBaby.setRoleBaby(roleBaby);
        send(clientBaby);
        eventDispatcher().fire(new BabyFashionChangeEvent(fashionId));
    }

    public RoleBaby getRoleBaby() {
        return roleBaby;
    }
}
