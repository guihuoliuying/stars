package com.stars.modules.authentic;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.authentic.event.AuthenticEvent;
import com.stars.modules.authentic.packet.ClientAuthentic;
import com.stars.modules.authentic.packet.ClientRoleAuth;
import com.stars.modules.authentic.prodata.AuthenticVo;
import com.stars.modules.authentic.userdata.RoleAuthenticPo;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.packet.ClientAnnouncement;
import com.stars.modules.drop.DropModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.I18n;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2016/12/22.
 */
public class AuthenticModule extends AbstractModule {
    private RoleAuthenticPo roleAuthentic;
    //    private List<Integer> delayFlag = new ArrayList<>();            //延时更新标志
    private boolean moneyDelayFlag = false;                         //金币鉴宝延时更新标志
    private boolean moneyFlag;                              //金币鉴宝红点标志
    private boolean goldFlag;                               //元宝鉴宝红点标志

    public AuthenticModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleAuthentic = DBUtil.queryBean(DBUtil.DB_USER, RoleAuthenticPo.class, "select * from roleauthentic where `roleid`=" + id());
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleAuthentic = new RoleAuthenticPo(id());
        context().insert(roleAuthentic);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (roleAuthentic == null) {
            roleAuthentic = new RoleAuthenticPo(id());
            context().insert(roleAuthentic);
        }
        signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_MONEY);
        signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_GOLD);
        moneyFlag = false;
        goldFlag = false;
    }

    @Override
    public void onTimingExecute() {
        if (roleAuthentic == null) return;
        if (!moneyFlag && isHavMoneyFreeCount(getAuthenticVo(AuthenticConst.money))) {
            moneyFlag = true;
            signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_MONEY);
        }
        if (!goldFlag && isHavGoldFreeCount(getAuthenticVo(AuthenticConst.gold))) {
            goldFlag = true;
            signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_GOLD);
        }
        if (roleAuthentic.getMoneyCount() >= getAuthenticVo(AuthenticConst.money).getLimitcount()) {
            moneyFlag = false;
            signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_MONEY);
        }
        if (roleAuthentic.getGoldCount() >= getAuthenticVo(AuthenticConst.gold).getLimitcount()) {
            goldFlag = false;
            signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_GOLD);
        }
        if (moneyDelayFlag) {
            countDown();
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleAuthentic != null) {
            if (roleAuthentic.getMoneyFreeCount() < getAuthenticVo(AuthenticConst.money).getFreecount()
                    && (now.getTimeInMillis() - roleAuthentic.getMoneyTime()) / 1000 < getAuthenticVo(AuthenticConst.money).getFreetime()) {
                //每日重置时，若还在倒计时，特殊处理
                //标记金币鉴宝需要延时重置
                moneyDelayFlag = true;
            } else {
                roleAuthentic.setMoneyFreeCount(0);
            }
            roleAuthentic.setMoneyEnsureCount(0);
            roleAuthentic.setGoldEnsureCount(0);
            roleAuthentic.setMoneyCount(0);
            roleAuthentic.setGoldCount(0);
            context().update(roleAuthentic);
            flushRoleAuth();
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.AUTHENTIC_MONEY))) {
            checkRedPoint(redPointMap, moneyFlag, RedPointConst.AUTHENTIC_MONEY);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.AUTHENTIC_GOLD))) {
            checkRedPoint(redPointMap, goldFlag, RedPointConst.AUTHENTIC_GOLD);
        }
    }

    /**
     * 计算金币鉴宝/元宝鉴宝红点
     *
     * @param redPointMap   红点Map
     * @param redPointFalg  金币鉴宝/元宝鉴宝红点标志位
     * @param redPointConst
     */
    private void checkRedPoint(Map<Integer, String> redPointMap, boolean redPointFalg, int redPointConst) {
        if (redPointFalg) {
            redPointMap.put(redPointConst, redPointConst + "");
        } else {
            redPointMap.put(redPointConst, null);
        }
    }

    /**
     * 标志每日更新时，免费次数还在倒计时的情况...即延时重置免费次数
     */
    private void countDown() {
        if ((System.currentTimeMillis() - roleAuthentic.getMoneyTime()) / 1000 >= getAuthenticVo(AuthenticConst.money).getFreetime()) {
            roleAuthentic.setMoneyFreeCount(0);
            roleAuthentic.setMoneyTime(0);
            context().update(roleAuthentic);
            moneyDelayFlag = false;
            flushRoleAuth();
        }
    }


    /**
     * 根据玩家等级和鉴宝类型获得相应的产品数据
     *
     * @param type
     * @return 产品数据
     */
    private AuthenticVo getAuthenticVo(int type) {
        RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
        return AuthenticManager.getAuthenticVo(roleModule.getLevel(), type);
    }


    /**
     * 获得需要发全服公告的item
     *
     * @param itemMap 鉴宝所获得的物品集合
     * @return 返回需要发全服公告的物品集合
     */
    private Map<Integer, String> getAnnouncementItem(Map<Integer, Integer> itemMap) {
        Map<Integer, String> map;
        Map<Integer, String> tmpMap = new HashMap<>();
        String itemTextStr = DataManager.getCommConfig("authentic_itemnotice");
        map = StringUtil.toMap(itemTextStr, Integer.class, String.class, '+', '|');
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            if (map.containsKey(entry.getKey())) {
                tmpMap.put(entry.getKey(), map.get(entry.getKey()));
            }
        }
        return tmpMap;
    }

    /**
     * 发送全服公告
     */
    private void sendAnnouncement(List<Map<Integer, Integer>> mapList) {
        if (SpecialAccountManager.isSpecialAccount(id())){
            return;
        }
        if (mapList.isEmpty()) return;
        Map<Integer, String> itemText = new HashMap<>();//itemId,textStr
        for (Map<Integer, Integer> map : mapList) {
            itemText.putAll(getAnnouncementItem(map));
        }
        RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
        ArrayList<String> list = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : itemText.entrySet()) {
            list.add(String.format(DataManager.getGametext(entry.getValue()),
                    roleModule.getRoleRow().getName(),
                    DataManager.getGametext(ToolManager.getItemVo(entry.getKey()).getName())));
        }
        if (list.isEmpty()) return;
        for (String key : list) {
            send(new ClientAnnouncement(key));
        }
    }

    /**
     * 玩家是否还有免费的金币鉴宝次数
     *
     * @return
     */
    private boolean isHavMoneyFreeCount(AuthenticVo vo) {
        if (roleAuthentic.getMoneyCount() >= vo.getLimitcount()) return false;
        if (roleAuthentic.getMoneyFreeCount() >= vo.getFreecount()
                || (System.currentTimeMillis() - roleAuthentic.getMoneyTime()) / 1000 < vo.getFreetime()) {
            return false;
        }
        return true;
    }

    /**
     * 玩家是否还有免费的元宝鉴宝次数
     *
     * @return
     */
    private boolean isHavGoldFreeCount(AuthenticVo vo) {
        if (roleAuthentic.getGoldCount() >= vo.getLimitcount()) return false;
        if ((System.currentTimeMillis() - roleAuthentic.getGoldTime()) / 1000 < vo.getFreetime()) {
            return false;
        }
        return true;
    }


    /**
     * 每一次鉴宝都返回掉落的物品集合
     *
     * @param type
     * @param drop
     * @return
     */
    private Map<Integer, Integer> getDropMap(int type, DropModule drop) {
        Map<Integer, Integer> map;
        if (type == AuthenticConst.money) {
            roleAuthentic.setMoneyCountInc();
            if (roleAuthentic.getMoneyEnsureCount() == 9) {
                map = drop.executeDrop(getAuthenticVo(type).getEnsuredrop(), 1,true);
                roleAuthentic.setMoneyEnsureCount(0);
            } else {
                map = drop.executeDrop(getAuthenticVo(type).getCommondrop(), 1,true);
                roleAuthentic.setMoneyEnsureCountInc();
            }
        } else if (type == AuthenticConst.gold) {
            roleAuthentic.setGoldCountInc();
            if (roleAuthentic.getGoldEnsureCount() == 9) {
                map = drop.executeDrop(getAuthenticVo(type).getEnsuredrop(), 1,true);
                roleAuthentic.setGoldEnsureCount(0);
            } else {
                map = drop.executeDrop(getAuthenticVo(type).getCommondrop(), 1,true);
                roleAuthentic.setGoldEnsureCountInc();
            }
        } else {
            throw new IllegalArgumentException("没有此种类型的鉴宝");
        }
        return map;
    }


    private Map<Integer, Integer> removeAutoUseAndBoxJobTool(Map<Integer, Integer> map) {
        List<Integer> maps = isAutoUse(map);
        if (!maps.isEmpty()) {
            for (int itemId : maps) {
                map.remove(itemId);
            }
        }
        return map;
    }

    private List<Integer> isAutoUse(Map<Integer, Integer> tempMap) {
        List<Integer> itemIds = new ArrayList<>();
        ItemVo itemVo;
        for (int itemId : tempMap.keySet()) {
            itemVo = ToolManager.getItemVo(itemId);
            if (itemVo.isAutoUse()) {
                itemIds.add(itemId);
            }
        }
        return itemIds;
    }

    /**
     * 如果需要打折，说明是金币鉴宝/元宝鉴宝十次，所以不需要增加免费次数，所以直接返回删除鉴宝所需道具是否成功。
     * 否则：即为金币鉴宝/元宝鉴宝一次，则判断鉴宝类型。根据类型判断是否还有免费次数，以及倒计时是否完成。
     * 若为真，则增加免费计数，返回true。(2017/1/16:元宝鉴宝不增加免费次数)
     * 否则：返回删除鉴宝所需道具是否成功。
     *
     * @param flag     是否需要打折的标记
     * @param times    次数
     * @param discount 折扣
     * @param type     金币鉴宝、元宝鉴宝
     * @return 是否删除成功
     */
    private boolean deleteAndSendTool(boolean flag, int times, double discount, int type) {
        AuthenticVo vo = getAuthenticVo(type);
        if (flag) {
            return deleteAndSendTool(flag, times, discount, vo);
        } else {
            if (type == AuthenticConst.money) {
                if (isHavMoneyFreeCount(vo)) {
                    moneyFlag = false;
                    roleAuthentic.setMoneyFreeCountInc().setMoneyTime(System.currentTimeMillis());
                    return true;
                } else {
                    return deleteAndSendTool(flag, times, discount, vo);
                }
            } else {
                if (isHavGoldFreeCount(vo)) {
                    goldFlag = false;
                    roleAuthentic.setGoldTime(System.currentTimeMillis());
                    return true;
                } else {
                    return deleteAndSendTool(flag, times, discount, vo);
                }
            }
        }
    }

    /**
     * 根据配置删除鉴宝所需要的道具
     * 如果flag==true，则数量为打折后的数量，向下取整
     * 走到这里说明玩家的的资源足够
     *
     * @param flag     是否需要打折的标记
     * @param discount 折扣
     * @param vo       产品数据
     * @return
     */
    private boolean deleteAndSendTool(boolean flag, int times, double discount, AuthenticVo vo) {
        ToolModule toolModule = module(MConst.Tool);
        if (toolModule.deleteAndSend(vo.getItemId(), flag ? (int) Math.floor(vo.getCount() * discount * times) : vo.getCount() * times, EventType.AUTHENTIC.getCode())) {
            return true;
        }
        return false;
    }

    /**
     * 获得折扣
     *
     * @return 返回折扣的百分比
     */
    private double getDiscount() {
        double discount = Double.parseDouble(DataManager.getCommConfig("authentic_discount"));
        return discount / 100;
    }

    /**
     * 金币鉴宝
     */
    public boolean moneyAuthentic(int times) {
        if (times > 10) {
            warn(I18n.get("authentic.timeslarge"));
            return false;
        }
        if (!isOpen()) {
            warn(I18n.get("authentic.unopen"));
            return false;
        }
        if (roleAuthentic.getMoneyCount() + times > getAuthenticVo(AuthenticConst.money).getLimitcount()) {
            warn(I18n.get("authentic.timesoverlimit"));
            return false;
        }
        List<Map<Integer, Integer>> maps;
        if (roleAuthentic.getNewbeeMoneyCount() < AuthenticConst.newMoneyCount) {
            if (times != 1) {
                warn(I18n.get("authentic.newbeeonlyone"));
                return false;
            }
            if (!deleteAndSendTool(false, times, 1, AuthenticConst.money)) {
                warn(I18n.get("authentic.constnotenoug"));
                return false;
            }
            //todo:判断是否新手
            maps = dealNewPlayerMoneyAuthentic(roleAuthentic.getNewbeeMoneyCount() + 1);
        } else {
            if (!deleteAndSendTool(times == 10, times, times == 10 ? getDiscount() : 1, AuthenticConst.money)) {
                warn(I18n.get("authentic.constnotenoug"));
                return false;
            }
            maps = authentic(AuthenticConst.money, times);
        }
        eventDispatcher().fire(new AuthenticEvent(AuthenticConst.money, times));
        flushRoleAuth();
        sendAnnouncement(maps);
        flushAward(maps);
        signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_MONEY);
        return true;
    }


    /**
     * 元宝鉴宝
     */
    public boolean goldAuthentic(int times) {
        if (times > 10) {
            warn(I18n.get("authentic.timeslarge"));
            return false;
        }
        if (!isOpen()) {
            warn(I18n.get("authentic.unopen"));
            return false;
        }
        if (roleAuthentic.getGoldCount() + times > getAuthenticVo(AuthenticConst.gold).getLimitcount()) {
            warn(I18n.get("authentic.timesoverlimit"));
            return false;
        }
        List<Map<Integer, Integer>> maps;
        if (roleAuthentic.getNewbeeGoldCount() < AuthenticConst.newGoldCount) {
            if (times != 1) {
                warn(I18n.get("authentic.newbeeonlyone"));
                return false;
            }
            if (!deleteAndSendTool(false, times, 1, AuthenticConst.gold)) {
                warn(I18n.get("authentic.constnotenoug"));
                return false;
            }
            //todo:判断是否新手
            maps = dealNewPlayerGoldAuthentic(roleAuthentic.getNewbeeGoldCount() + 1);
        } else {
            if (!deleteAndSendTool(times == 10, times, times == 10 ? getDiscount() : 1, AuthenticConst.gold)) {
                warn(I18n.get("authentic.constnotenoug"));
                return false;
            }
            maps = authentic(AuthenticConst.gold, times);
        }
        eventDispatcher().fire(new AuthenticEvent(AuthenticConst.gold, times));
        flushRoleAuth();
        sendAnnouncement(maps);
        flushAward(maps);
        signCalRedPoint(MConst.Authentic, RedPointConst.AUTHENTIC_GOLD);
        return true;
    }

    private List<Map<Integer, Integer>> dealNewPlayerMoneyAuthentic(int times) {
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        ToolModule module = module(MConst.Tool);
        DropModule drop = (DropModule) moduleMap().get(MConst.Drop);
        int dropid = AuthenticManager.newPlayerMoneyDrop.get(times);
        Map<Integer, Integer> map = drop.executeDrop(dropid, 1,true);
        Map<Integer, Integer> returnMap = module.addAndSend(map, EventType.AUTHENTIC.getCode());
        mapList.add(returnMap);
        roleAuthentic.setMoneyCountInc().setNewbeeMoneyCountInc();
        context().update(roleAuthentic);
        return mapList;
    }

    private List<Map<Integer, Integer>> dealNewPlayerGoldAuthentic(int times) {
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        ToolModule module = module(MConst.Tool);
        DropModule drop = (DropModule) moduleMap().get(MConst.Drop);
        int dropGroup = AuthenticManager.newPlayerGoldDrop.get(times);
        Map<Integer, Integer> map = drop.executeDrop(dropGroup, 1,true);
        Map<Integer, Integer> returnMap = module.addAndSend(map, EventType.AUTHENTIC.getCode());
        mapList.add(returnMap);
        roleAuthentic.setGoldCountInc().setNewbeeGoldCountInc();
        context().update(roleAuthentic);
        return mapList;
    }

    /**
     * 玩家的资源是否满足鉴宝需求
     *
     * @param type  鉴宝类型：金币or元宝
     * @param times 鉴宝次数
     * @return
     */
    private boolean canAuthentic(int type, int times) {
        ToolModule toolModule = module(MConst.Tool);
        AuthenticVo vo = getAuthenticVo(type);
        boolean flag = times != 10 && (type == AuthenticConst.gold ? isHavGoldFreeCount(vo) : isHavMoneyFreeCount(vo));
        if (toolModule.contains(vo.getItemId(),
                (flag ? vo.getCount() * (times - 1) :
                        (times == 10 ? (int) Math.floor(vo.getCount() * getDiscount() * times) :
                                vo.getCount() * times))))
            return true;
        return false;
    }

    /**
     * 鉴宝
     *
     * @param type 鉴宝类型
     * @return 返回掉落的map
     */
    private List<Map<Integer, Integer>> authentic(int type, int times) {
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        ToolModule module = module(MConst.Tool);
        DropModule drop = (DropModule) moduleMap().get(MConst.Drop);
        for (int i = 0; i < times; i++) {
            Map<Integer, Integer> itemMap = getDropMap(type, drop);
            Map<Integer, Integer> returnMap = module.addAndSend(itemMap, EventType.AUTHENTIC.getCode());
            mapList.add(returnMap);
        }
        context().update(roleAuthentic);
        return mapList;
    }


    /**
     * 更新鉴宝所获得的物品数据到客户端
     */
    private void flushAward(List<Map<Integer, Integer>> mapList) {
        if (!mapList.isEmpty()) {
            ClientAuthentic authentic = new ClientAuthentic();
            authentic.setItemMapList(mapList);
            send(authentic);
        }
    }

    /**
     * 更新玩家鉴宝次数等信息到客户端
     */
    public void flushRoleAuth() {
        ClientRoleAuth cra = new ClientRoleAuth();
        cra.setRoleAuthenticPo(roleAuthentic);
        send(cra);
    }

    private boolean isOpen() {
        ForeShowModule module = module(MConst.ForeShow);
        return module.isOpen(ForeShowConst.AUTHENTIC);
    }
}
