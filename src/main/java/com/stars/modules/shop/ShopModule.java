package com.stars.modules.shop;

import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.operateCheck.OperateCheckModule;
import com.stars.modules.operateCheck.OperateConst;
import com.stars.modules.push.PushManager;
import com.stars.modules.push.PushModule;
import com.stars.modules.push.userdata.RolePushPo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.shop.event.BuyGoodsEvent;
import com.stars.modules.shop.packet.ClientShopData;
import com.stars.modules.shop.prodata.Shop;
import com.stars.modules.shop.userdata.RoleShopPersonalLimitRow;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.services.ServiceHelper;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhouyaohui on 2016/9/5.
 */
public class ShopModule extends AbstractModule implements AccountRowAware {
    /**
     * recordMap 中的key
     */
    private final static String RANDOM_YMD = "shop.randomFlushTime"; // 随机商店的刷新时间
    private final static String RANDOM_STR = "shop.randomShopItems"; // 随机商店的商品id
    private final static String FREE_YMD = "shop.freeFlushYMD";      // 免费刷新次数的日期
    private final static String FREE_TIMES = "shop.freeTimes";       // 免费刷新次数
    private final static String DAILY_LIMIT = "shop.dailyLimit";     // 每日限购商品
    private final static String DAILY_LIMIT_YMD = "shop.dailyLimitYMD";  // 每日限购ymd;
    private final static String RANDOM_BUYED = "shop.randombuy";     // 随机商店购买过的商品
    private final static String PAY_TIMES = "shop.payTimes";// 付费刷新次数
    private final static String TIMESHOP = "shop.timeShop"; // 限时特惠

    /**
     * 购买结果
     */
    private final static byte SUCCESS = 0;  // 购买成功
    private final static byte EXPIRED = 1;  // 商品过期
    private final static byte LEVEL_INVAILD = 2;    // 等级不符合
    private final static byte OVER_TIMES = 3;   // 购买次数不足
    private final static byte RESOURCE_UNENOUGH = 4;  // 资源不足
    private final static byte FAILED = 5;   // 购买失败
    private final static byte VIP_LEVEL_INVAILD = 6;   // vip等级不符合

    private Set<Integer> fixedMap = new HashSet<>();   // 固定商品列表
    private Set<Integer> randomMap = new HashSet<>();  // 随即商品列表
    private Set<Integer> timeLimitMap = new HashSet<>(); // 限时特惠商品列表
    private Set<Integer> oldTimeLimitMap = new HashSet<>(); // (前)限时特惠商品列表
    private Map<Integer, RoleShopPersonalLimitRow> personalLimitMap = new HashMap<>();  // 终身限购map
    private Map<Integer, Integer> dailyLimitMap = new HashMap<>();  // 每日限购map
    private Set<Integer> randomBuy = new HashSet<>();     // 随机商店每个商品只能购买一次，需要刷新后才能再次购买
    private AccountRow accountRow;


    public void setRandomMap(Set<Integer> randomMap) {
        this.randomMap = randomMap;
        if (isRandomShopFlush()) {
            randomBuy.clear();
            updateRandomBuy();
        }
        context().recordMap().setString(RANDOM_YMD, DateUtil.getYMDHMSStr());
        context().recordMap().setString(RANDOM_STR, converRandomShopToString());
    }

    public ShopModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        this.timeLimitMap = StringUtil.toHashSet(getString(TIMESHOP, ""), Integer.class, ',');
        this.oldTimeLimitMap = timeLimitMap;
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roleshoppersonallimit where roleid =" + id();
        personalLimitMap = DBUtil.queryMap(DBUtil.DB_USER, "goodsid", RoleShopPersonalLimitRow.class, sql);
        String dailyLimitStr = context().recordMap().getString(DAILY_LIMIT);
        if (dailyLimitStr != null) {
            dailyLimitMap = StringUtil.toMap(dailyLimitStr, Integer.class, Integer.class, '=', '&');
        }

        String goodsIds = context().recordMap().getString(RANDOM_BUYED, "");
        for (String goodsId : goodsIds.split("&")) {
            if (goodsId.isEmpty()) continue;
            randomBuy.add(Integer.valueOf(goodsId));
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        initTimeShop();
    }

    /**
     * 填充商品列表
     *
     * @param list
     * @param set
     * @param map
     * @param check 是否需要检查条件，在剔除无效商品时，不需检查否则剔除不了
     */
    private void fillShopList(List<Shop> list, Set<Integer> set, Map<Integer, Shop> map, boolean check) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(set);
        long curMillls = System.currentTimeMillis();
        RoleModule roleModule = module(MConst.Role);
        int level = roleModule.getLevel();
        for (int goodsId : set) {
            Shop shop = map.get(goodsId);
            if (shop != null) {
                if (!checkShopVo(shop, curMillls, level) && check) continue;
                list.add(shop);
            }
        }
    }

    /**
     * 从列表中移除商品数据
     *
     * @param list
     * @param set
     */
    private void removeFromList(List<Shop> list, Set<Integer> set) {
        Objects.requireNonNull(list);
        Objects.requireNonNull(set);
        Iterator<Shop> iter = list.iterator();
        while (iter.hasNext()) {
            Shop shop = iter.next();
            if (set.contains(shop.getGoodsId())) {
                iter.remove();
            }
        }
    }

    /**
     * 打开商店
     */
    public void openShop(int version) {
        if (version != ShopManager.version) {
            fixedMap = generateFixedShopList(); // 请求全部，重新生成固定商品表
            if (StringUtil.isEmpty(randomMap)) {
                converRandomShopStringToMap();
            } else if (isRandomShopFlush()) {
                Set<Integer> randomMap = generateRandomShopList();
                setRandomMap(randomMap);
            }
            List<Shop> shopList = new ArrayList<>();
            fillShopList(shopList, fixedMap, ShopManager.fixedMap, true);
            fillShopList(shopList, randomMap, ShopManager.randomMap, true);
            fillShopList(shopList, timeLimitMap, ShopManager.shopMap, true);
            oldTimeLimitMap = timeLimitMap;
            ClientShopData product = new ClientShopData();
            product.setOpType(ClientShopData.SHOPLIST);
            product.setShopList(shopList);
            send(product);
        }

        if (version == ShopManager.version) {
            Set<Integer> newFixedMap = generateFixedShopList();
            List<Shop> newList = new ArrayList<>();
            List<Shop> oldList = new ArrayList<>();
            if (isRandomShopFlush()) {
                Set<Integer> newRandomMap = generateRandomShopList();
                fillShopList(newList, newFixedMap, ShopManager.fixedMap, true);
                fillShopList(newList, newRandomMap, ShopManager.randomMap, true);
                fillShopList(newList, timeLimitMap, ShopManager.shopMap, true); //
                fillShopList(oldList, fixedMap, ShopManager.fixedMap, false);
                fillShopList(oldList, randomMap, ShopManager.randomMap, false);
                fillShopList(oldList, oldTimeLimitMap, ShopManager.shopMap, false);

                removeFromList(newList, fixedMap);
                removeFromList(newList, randomMap);
                removeFromList(newList, oldTimeLimitMap);
                removeFromList(oldList, newFixedMap);
                removeFromList(oldList, newRandomMap);
                removeFromList(oldList, timeLimitMap);
                fixedMap = newFixedMap;
                oldTimeLimitMap = timeLimitMap;
                setRandomMap(newRandomMap);
            } else {
                fillShopList(newList, newFixedMap, ShopManager.fixedMap, true);
                fillShopList(oldList, fixedMap, ShopManager.fixedMap, false);
                fillShopList(newList, timeLimitMap, ShopManager.shopMap, true); //
                fillShopList(oldList, oldTimeLimitMap, ShopManager.shopMap, false);

                removeFromList(newList, fixedMap);
                removeFromList(newList, oldTimeLimitMap);
                removeFromList(oldList, newFixedMap);
                removeFromList(oldList, timeLimitMap);
                fixedMap = newFixedMap;
                oldTimeLimitMap = timeLimitMap;
            }
            ClientShopData res = new ClientShopData();
            res.setOpType(ClientShopData.UPDATESHOP);
            res.setAddList(newList);
            res.setSubList(oldList);
            send(res);
        }
        sendUserData();
    }

    /**
     * 购买
     *
     * @param goodsId
     * @param count
     */
    public void buyGoodsById(int goodsId, int count) {
        if (!OperateCheckModule.checkOperate(id(), OperateConst.SHOP_BUY, OperateConst.FIVE_HUNDRED_MS)) return;
        if (count <= 0) {
            warn(I18n.get("store.buyCount.negative"));
            return;
        }
        if (!randomMap.contains(goodsId) && !fixedMap.contains(goodsId)) {
            return;
        }
        Shop goods = ShopManager.shopMap.get(goodsId);
        if (goods == null) {
            warn(I18n.get("store.itemNotExis"));
            return;
        }
        int limitBuyCount = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "shop_buy_maxnum", 100);
        if (count > limitBuyCount) {
            warn(I18n.get("store.overTimes"));
            return;
        }
        if (!goods.checkExtendCondition(moduleMap())) {
            warn(I18n.get("store.condition.fail"));
            return;
        }
        ClientShopData res = new ClientShopData();
        res.setOpType(ClientShopData.BUYGOODS);
        res.setGoodsId(goods.getGoodsId());
        res.setCount(count);
        long curMillis = System.currentTimeMillis();
        int level = ((RoleModule) module(MConst.Role)).getLevel();
        int serverDays = DataManager.getServerDays(curMillis);

        if (!goods.checkLevel(level)) {
            res.setBuyResult(LEVEL_INVAILD);
            send(res);
            return;
        }
        if (!goods.checkVipLevel(accountRow.getVipLevel())) {
            res.setBuyResult(VIP_LEVEL_INVAILD);
            send(res);
            return;
        }
        if (!goods.checkOnlineAndTime(curMillis, serverDays)) {
            res.setBuyResult(EXPIRED);
            send(res);
            return;
        }
        if ((goods.getServiceLimitNum() != 0 && getServiceLimitRemainder(goods) < count) ||
                (goods.getPersonalLimitNum() != 0 && getPersonalLimitRemainder(goods) < count) ||
                (goods.getDailyLimitNum() != 0 && getDailyLimitRemainder(goods) < count)) {
            res.setBuyResult(OVER_TIMES);
            send(res);
            return;
        }
        if (randomBuy.contains(goods.getGoodsId())) {
            res.setBuyResult(OVER_TIMES);
            send(res);
            return;
        }

        String[] priceArray = goods.getPrice().split("\\+");
        ToolModule toolModule = module(MConst.Tool);
        int toolCount = count * goods.getCount();
        int resItemId = Integer.valueOf(priceArray[0]);
        int resCount = Integer.valueOf(priceArray[1]) * count;
        if (toolModule.canAdd(goods.getItemId(), toolCount) &&
                toolModule.contains(resItemId, resCount)) {
            if (goods.isServiceLimit() && !ServiceHelper.shopService().buy(goods.getGoodsId(), count, goods.getServiceLimitNum())) {
                res.setBuyResult(OVER_TIMES);
                send(res);
                return;
            }
            if (toolModule.deleteAndSend(resItemId, resCount, EventType.SHOPBUY.getCode())) {
                toolModule.addAndSend(goods.getItemId(), count * goods.getCount(), EventType.SHOPBUY.getCode());
                Map<Integer, Integer> subMap = new HashMap<>();
                Map<Integer, Integer> addMap = new HashMap<>();
                subMap.put(resItemId, resCount);
                addMap.put(goods.getItemId(), count * goods.getCount());
                ServerLogModule log = module(MConst.ServerLog);
                log.Log_shop_buy(goods.getShopType(), addMap, subMap);
            } else {
                res.setBuyResult(RESOURCE_UNENOUGH);
                send(res);
                return;
            }
        } else {
            if (!toolModule.contains(resItemId, resCount)) {
                res.setBuyResult(RESOURCE_UNENOUGH);
            } else {
                res.setBuyResult(FAILED);
            }
            send(res);
            return;
        }
        if (goods.isPersonalLimit()) {
            addPersionalLimitTimes(goods.getGoodsId(), count);
        }
        if (goods.isDailyLimit()) {
            addDailyTime(goods.getGoodsId(), count);
        }
        res.setBuyResult(SUCCESS);

        //抛出成功购买商品事件
        this.eventDispatcher().fire(new BuyGoodsEvent(goodsId, count));

        int remainder = 0;
        if (goods.getPersonalLimitNum() != 0) remainder = getPersonalLimitRemainder(goods);
        if (goods.getServiceLimitNum() != 0) remainder = getServiceLimitRemainder(goods);
        if (goods.getDailyLimitNum() != 0) remainder = getDailyLimitRemainder(goods);
        res.setRemainder(remainder);
        send(res);

        if (goods.getWeight() != 0) {
            randomBuy.add(goods.getGoodsId());
            updateRandomBuy();
        }

        sendUserData();  // 更新用户数据
    }

    /**
     * 刷新限时商店
     */
    public void flush() {
        int count = getFreeFlushTimes();
        if (count <= 0) {   // 没有免费次数了，需要消耗道具
            // 付费刷新次数不足
            check(getRestPayFlushTimes() <= 0, "store.payfresh.max");
            count = context().recordMap().getInt(PAY_TIMES, 0);
            count += 1; // 配置从1开始，调整count从零开始的误差
            String[] array = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "shop_refresh_refreshcost", "").split("\\|");
            int itemId = 0;
            int itemCount = 0;
            for (String unit : array) {
                String[] arr = unit.split(",");
                if (arr.length == 1) {
                    itemId = Integer.valueOf(arr[0].split("\\+")[0]);
                    itemCount = Integer.valueOf(arr[0].split("\\+")[1]);
                    break;
                }
                if (count >= Integer.valueOf(arr[1].split("\\+")[0]) &&
                        count <= Integer.valueOf(arr[1].split("\\+")[1])) {
                    itemId = Integer.valueOf(arr[0].split("\\+")[0]);
                    itemCount = Integer.valueOf(arr[0].split("\\+")[1]);
                    break;
                }
            }
            ToolModule toolModule = (ToolModule) module(MConst.Tool);
            if (!toolModule.deleteAndSend(itemId, itemCount, EventType.SHOPREFRESH.getCode())) {
                warn(I18n.get("store.toolNotEnough"));
                return;
            }
            context().recordMap().setInt(PAY_TIMES, context().recordMap().getInt(PAY_TIMES, 0) + 1);
        } else {
            context().recordMap().setInt(FREE_TIMES, context().recordMap().getInt(FREE_TIMES, 0) + 1);
        }

        Set<Integer> newMap = generateRandomShopList();
        List<Shop> newList = new ArrayList<>();
        List<Shop> oldList = new ArrayList<>();
        fillShopList(newList, newMap, ShopManager.randomMap, true);
        fillShopList(oldList, randomMap, ShopManager.randomMap, false);
        removeFromList(newList, randomMap);
        removeFromList(oldList, newMap);

        ClientShopData res = new ClientShopData();
        res.setOpType(ClientShopData.UPDATESHOP);
        res.setAddList(newList);
        res.setSubList(oldList);
        res.setIsFlush((byte) 1);
        send(res);


        setRandomMap(newMap);
        randomBuy.clear();
        updateRandomBuy();
        sendUserData();  // 下发用户数据下去
    }

    /**
     * 发送用户数据
     */
    private void sendUserData() {
        Map<Shop, Integer> dailyLimitMap = new HashMap<>();
        Map<Shop, Integer> personalLimitMap = new HashMap<>();
        Map<Shop, Integer> serviceLimitMap = new HashMap<>();
        List<Shop> temp = new ArrayList<>();
        fillShopList(temp, fixedMap, ShopManager.fixedMap, true);
        fillShopList(temp, randomMap, ShopManager.randomMap, true);
        for (Shop item : temp) {
            if (item.getServiceLimitNum() != 0) {
                serviceLimitMap.put(item, getServiceLimitTimes(item.getGoodsId()));
                continue;
            }
            if (item.getPersonalLimitNum() != 0) {
                RoleShopPersonalLimitRow row = this.personalLimitMap.get(item.getGoodsId());
                personalLimitMap.put(item, row == null ? 0 : row.getTimes());
                continue;
            }
            if (item.getDailyLimitNum() != 0) {
                Integer times = this.dailyLimitMap.get(item.getGoodsId());
                dailyLimitMap.put(item, times == null ? 0 : times);
                continue;
            }
        }
        ClientShopData shopUserData = new ClientShopData();
        shopUserData.setOpType(ClientShopData.USERDATA);
        shopUserData.setDailyLimit(dailyLimitMap);
        shopUserData.setPersonalLimit(personalLimitMap);
        shopUserData.setServiceLimit(serviceLimitMap);
        shopUserData.setServerDays(DataManager.getServerDays());
        shopUserData.setRemainderFlushTimes(getFreeFlushTimes());
        shopUserData.setRemainPayFlushTimes(getRestPayFlushTimes());
        shopUserData.setRandomBuy(randomBuy);
        send(shopUserData);
    }

    /**
     * 获取剩余免费刷新次数
     *
     * @return
     */
    private int getFreeFlushTimes() {
        int ymd = context().recordMap().getInt(FREE_YMD, 0);
        if (Integer.valueOf(DateUtil.getYMD_Str()) != ymd) {
            context().recordMap().setInt(FREE_YMD, Integer.valueOf(DateUtil.getYMD_Str()));
            // 重置免费次数时,一起重置已付费刷新次数
            context().recordMap().setInt(FREE_TIMES, 0);
            context().recordMap().setInt(PAY_TIMES, 0);
        }
        int times = context().recordMap().getInt(FREE_TIMES, 0);
        int max = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "shop_refresh_freechance", 0);
        return max - times;
    }

    /**
     * 获得剩余付费刷新次数
     *
     * @return
     */
    private int getRestPayFlushTimes() {
        int times = context().recordMap().getInt(PAY_TIMES, 0);
        VipModule vipModule = module(MConst.Vip);
        VipinfoVo vipinfoVo = vipModule.getCurVipinfoVo();
        int limit = Integer.parseInt(DataManager.getCommConfig("shop_refresh_paychance")) +
                (vipinfoVo == null ? 0 : vipinfoVo.getRefreshCount());
        return Math.max(limit - times, 0);
    }

    /**
     * 初始化角色的随机商品
     */
    private void initRandomShopList() {
        if (StringUtil.isEmpty(this.randomMap)) {
            // 随即商品列表为空,从commondefine中获取初始值
            String defaultGroup = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "shop_fresh_originitem", null);
            Objects.requireNonNull(defaultGroup, "默认随即商品列表不能为空");
            Set<Integer> randomMap = new HashSet<>();
            for (String goodsId : defaultGroup.trim().split("\\+")) {
                Shop item = ShopManager.randomMap.get(Integer.valueOf(goodsId));
                if (item == null) continue;
                randomMap.add(item.getGoodsId());
            }
            if (StringUtil.isEmpty(randomMap)) {
                randomMap = generateRandomShopList();
            }
            setRandomMap(randomMap);
        }
    }

    /**
     * 将随机商店转换成String
     *
     * @return
     */
    private String converRandomShopToString() {
        StringBuilder builder = new StringBuilder();
        for (int goodsId : randomMap) {
            builder.append(goodsId).append("&");
        }
        if (builder.length() != 0) {
            builder.delete(builder.length() - 1, builder.length());
        }
        return builder.toString();
    }

    /**
     * 从数据库里面获取随机商品列表
     */
    private void converRandomShopStringToMap() {
        String shopStr = context().recordMap().getString(RANDOM_STR, "");
//        if (StringUtil.isEmpty(shopStr)) {  // 第一次打开商店，初始化随机商店
//            initRandomShopList();
//            return;
//        }
        if (isRandomShopFlush() || StringUtil.isEmpty(shopStr)) {  // 需要刷新，重新生成
            Set<Integer> randomMap = generateRandomShopList();
            setRandomMap(randomMap);
            return;
        }
        Set<Integer> randomMap = new HashSet<>();
        for (String itemStr : shopStr.split("&")) {
            Shop item = ShopManager.randomMap.get(Integer.valueOf(itemStr));
            if (item == null) continue;
            randomMap.add(item.getGoodsId());
        }
        setRandomMap(randomMap);
    }

    /**
     * 判断需不需要刷新
     *
     * @return
     */
    private boolean isRandomShopFlush() {
        final int dayBase = 1000000;
        String oldYMD = context().recordMap().getString(RANDOM_YMD, null);
        if (StringUtil.isEmpty(oldYMD)) return true;
        String flushStr = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "shop_refreshtime", "");
        if (StringUtil.isEmpty(flushStr)) return true;
        String[] flushArray = flushStr.trim().split("\\+");
        int index = -1;

        long old = Long.valueOf(oldYMD);
        long now = Long.valueOf(DateUtil.getYMDHMSStr());
        if (now / dayBase - old / dayBase > 0) {    // 不是同一天
            if ((now % dayBase) < Integer.valueOf(flushArray[0].replace(":", "")) &&
                    (old % dayBase) > Integer.valueOf(flushArray[flushArray.length - 1].replace(":", ""))) {
                return false;
            } else {
                return true;
            }
        } else {    // 同一天
            for (int i = 0; i < flushArray.length; i++) {
                if ((old % dayBase) >= Integer.valueOf(flushArray[i].replace(":", ""))) {
                    index = i;
                }
            }
            if (index >= flushArray.length - 1) return false;   // 当天最后一次刷新时间刷新过，不需要刷新了
            if ((now % dayBase) > Integer.valueOf(flushArray[index + 1].replace(":", ""))) return true;
            return false;
        }
    }

    /**
     * 生成随即商品列表
     */
    private Set<Integer> generateRandomShopList() {
        String randomGroups = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "shop_fresh_groupweight", "0");
        int power = 0;
        List<Integer> groupList = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        for (String group : randomGroups.trim().split("\\|")) {
            String[] groupArray = group.trim().split("\\+");
            power += Integer.valueOf(groupArray[1]);
            groupList.add(Integer.valueOf(groupArray[0]));
            powerList.add(Integer.valueOf(groupArray[1]));
        }
        int groupId = 0;
        if (power != 0) {
            int randomInt = new Random().nextInt(power) + 1;
            int cursor = 0;
            for (int i = 0; i < groupList.size(); i++) {
                cursor += powerList.get(i);
                if (randomInt - cursor <= 0) {
                    groupId = groupList.get(i);
                    break;
                }
            }
        }
        Map<Integer, Shop> randomMap = new HashMap<>();
        long curMillis = System.currentTimeMillis();
        int level = ((RoleModule) (module(MConst.Role))).getLevel();
        if (groupId != 0) {
            String goodsIds = com.stars.util.MapUtil.getString(DataManager.commonConfigMap, "shop_fresh_itemgroup", null);
            if (!StringUtil.isEmpty(goodsIds)) {
                for (String goodsId : goodsIds.trim().split("\\|")) {
                    String[] array = goodsId.trim().split("\\+");
                    if (Integer.valueOf(array[0]) != groupId) continue;
                    for (int i = 1; i < array.length; i++) {
                        int id = Integer.valueOf(array[i]);
                        Shop item = ShopManager.randomMap.get(id);
                        if (item != null) {
                            if (!checkShopVo(item, curMillis, level)) continue;
                            randomMap.put(item.getGoodsId(), item);
                        }
                    }
                }
            }
        }

        if (randomMap.size() == 0) {
            // 没有找打合适的，或者groupid == 0,随机整个randomMap
            List<Shop> randomList = new ArrayList<>();
            for (Shop item : ShopManager.randomMap.values()) {
                if (!checkShopVo(item, curMillis, level)) continue;
                randomList.add(item);
            }
            int requireCount = com.stars.util.MapUtil.getInt(DataManager.commonConfigMap, "shop_refreshnum", ShopManager.DEFAULTCOUNT);
            if (requireCount > randomList.size()) {
                for (Shop item : randomList) {
                    randomMap.put(item.getGoodsId(), item);
                }
            } else {
                for (int i = 0; i < requireCount; i++) {
                    powerRandom(randomList, randomMap);
                }
            }
        }
        return randomMap.keySet();
    }

    /**
     * 加权平均
     *
     * @param randomList
     * @param randomMap
     */
    private void powerRandom(List<Shop> randomList, Map<Integer, Shop> randomMap) {
        int totalPower = 0;
        for (Shop item : randomList) {
            if (randomMap.get(item.getGoodsId()) != null) continue; // 不放回抽样
            totalPower += item.getWeight();
        }
        int randomInt = new Random().nextInt(totalPower) + 1;
        int cursor = 0; // 游标
        for (Shop item : randomList) {
            if (randomMap.get(item.getGoodsId()) != null) continue;
            cursor += item.getWeight();
            if (randomInt - cursor <= 0) {
                randomMap.put(item.getGoodsId(), item);
                LogUtil.debug("权值:{} ----- 随机数{} ----- 权重{}", totalPower, randomInt, item.getWeight());
                break;
            }
        }
    }

    /**
     * 生成固定商品列表
     */
    private Set<Integer> generateFixedShopList() {
        long curMillls = System.currentTimeMillis();
        RoleModule roleModule = module(MConst.Role);
        int level = roleModule.getLevel();
        Set<Integer> fixedMap = new HashSet<>();
        for (Map.Entry<Integer, Shop> entry : ShopManager.fixedMap.entrySet()) {
            Shop shop = entry.getValue();
            if (!checkShopVo(shop, curMillls, level)) continue;
            fixedMap.add(shop.getGoodsId());
        }
        return fixedMap;
    }

    /**
     * 返回个人终身限购次数
     *
     * @param goodsId
     * @return
     */
    private int getPersonalLimitTimes(int goodsId) {
        RoleShopPersonalLimitRow row = personalLimitMap.get(goodsId);
        return row == null ? 0 : row.getTimes();
    }

    /**
     * 增加个人终身限购的购买数量，不做条件判断
     *
     * @param goodsId
     * @param count
     */
    private void addPersionalLimitTimes(int goodsId, int count) {
        RoleShopPersonalLimitRow row = personalLimitMap.get(goodsId);
        if (row == null) {
            row = new RoleShopPersonalLimitRow();
            row.setGoodsId(goodsId);
            row.setRoleId(id());
            row.setTimes(0);
            personalLimitMap.put(goodsId, row);
            context().insert(row);
        }
        row.setTimes(row.getTimes() + count);
        context().update(row);
    }

    /**
     * 返回每日限购次数
     *
     * @param goodsId
     * @return
     */
    private int getDailyTimes(int goodsId) {
        int oldYMD = context().recordMap().getInt(DAILY_LIMIT_YMD, 0);
        int nowYMD = Integer.valueOf(DateUtil.getYMD_Str());
        if (nowYMD != oldYMD) {
            context().recordMap().setInt(DAILY_LIMIT_YMD, nowYMD);
            context().recordMap().setString(DAILY_LIMIT, "");
            dailyLimitMap.clear();
        }
        Integer times = dailyLimitMap.get(goodsId);
        return times == null ? 0 : times;
    }

    /**
     * 增加每日购买数量，不做条件判断
     *
     * @param goodsId
     * @param count
     */
    private void addDailyTime(int goodsId, int count) {
        int oldYMD = context().recordMap().getInt(DAILY_LIMIT_YMD, 0);
        int nowYMD = Integer.valueOf(DateUtil.getYMD_Str());
        if (nowYMD != oldYMD) {
            context().recordMap().setInt(DAILY_LIMIT_YMD, nowYMD);
            dailyLimitMap.clear();
        }
        Integer items = dailyLimitMap.get(goodsId);
        dailyLimitMap.put(goodsId, items == null ? count : items + count);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : dailyLimitMap.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        builder.delete(builder.length() - 1, builder.length());
        context().recordMap().setString(DAILY_LIMIT, builder.toString());
    }

    /**
     * 返回全服限购数量
     *
     * @param goodsId
     * @return
     */
    private int getServiceLimitTimes(int goodsId) {
        return ServiceHelper.shopService().getLimitByGoodsId(goodsId);
    }

    /**
     * 更新随机商店购买过的商品数据
     */
    private void updateRandomBuy() {
        StringBuilder builder = new StringBuilder();
        for (Integer goodsId : randomBuy) {
            builder.append(goodsId).append("&");
        }
        if (builder.length() != 0) builder.delete(builder.length() - 1, builder.length());
        context().recordMap().setString(RANDOM_BUYED, builder.toString());
    }

    /**
     * 剩余个人终身购买次数
     *
     * @param goods
     * @return
     */
    private int getPersonalLimitRemainder(Shop goods) {
        return goods.getPersonalLimitNum() - getPersonalLimitTimes(goods.getGoodsId());
    }

    /**
     * 剩余全服购买次数
     *
     * @param goods
     * @return
     */
    private int getServiceLimitRemainder(Shop goods) {
        return goods.getServiceLimitNum() - getServiceLimitTimes(goods.getGoodsId());
    }

    /**
     * 剩余每日购买次数
     *
     * @param goods
     * @return
     */
    private int getDailyLimitRemainder(Shop goods) {
        return goods.getDailyLimitNum() - getDailyTimes(goods.getGoodsId());
    }

    /**
     * 检查商品条件
     *
     * @param shopVo
     * @param curMillls
     * @param level
     * @return
     */
    private boolean checkShopVo(Shop shopVo, long curMillls, int level) {
        if (!shopVo.checkOnlineAndTime(curMillls, DataManager.getServerDays(curMillls))) return false;
        if (!shopVo.checkLevel(level)) return false;
        if (!shopVo.checkPersonalLimit(getPersonalLimitTimes(shopVo.getGoodsId()))) return false;
        if (!shopVo.checkServiceLimit(getServiceLimitTimes(shopVo.getGoodsId()))) return false;
        if (!shopVo.checkDailyLimit(getDailyTimes(shopVo.getGoodsId()))) return false;
        if (shopVo.getServiceLimitNum() != 0 && getServiceLimitRemainder(shopVo) <= 0) return false;
        if (shopVo.getPersonalLimitNum() != 0 && getPersonalLimitRemainder(shopVo) <= 0) return false;
        /**
         * 检查商品的角色vip级别限制
         */
        if (!shopVo.checkVipLevel(accountRow.getVipLevel())) return false;
//        if (shopVo.getDailyLimitNum() != 0 && getDailyLimitRemainder(shopVo) <= 0) return false;
        return true;
    }

    public void initTimeShop() {
        oldTimeLimitMap = timeLimitMap;
        timeLimitMap = new HashSet<>();
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        PushModule pushModule = module(MConst.Push);
        if (foreShowModule.isOpen(ForeShowConst.TIMESHOP)) {
//        if (true) {
            Map<Integer, RolePushPo> pushPoMap = pushModule.getPushPoMapByActivityId(2);
            for (RolePushPo po : pushPoMap.values()) {
                if (po.getState() == PushManager.STATE_ACTIVED && pushModule.satisfyCondition(po.getPushId())) {
                    timeLimitMap.add(po.getPushId());
                    pushModule.inactivePush(po.getPushId());
                }
            }
        }
        // 保存
        setString(TIMESHOP, StringUtil.makeString(timeLimitMap, ','));
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }
}
