package com.stars.modules.tool;

import com.stars.core.annotation.DependOn;
import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.tool.event.GMDelToolEvent;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.impl.*;
import com.stars.modules.tool.listener.GMDelToolListener;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
@DependOn({MConst.Data})
public class ToolModuleFactory extends AbstractModuleFactory<ToolModule> {

    public ToolModuleFactory() {
        super(new ToolPacketSet());
    }

    @Override
    public ToolModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ToolModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        String sql = "select * from item;";
        Map<Integer, ItemVo> toolMap = (HashMap) DBUtil.queryMap(DBUtil.DB_PRODUCT, "itemid", ItemVo.class, sql);

        // 初始化道具函数
        initToolFunc(toolMap);
        //初始化带系数的道具宝箱
        Map<Integer, ItemVo> toolMapCoe = initCoeff(toolMap);
        // 出生增加背包道具
        String autoTools = DataManager.getCommConfig("birthadditem");
        if (StringUtil.isEmpty(autoTools)) {
            LogUtil.info("初始添加道具的birthadditem字段为空");
            return;
        }
        autoTools = autoTools.replace("+", "=");
//        Map<Integer, Integer> map = StringUtil.parseIntegerMap(autoTools, "\\|", HashMap.class);
        Map<Integer, Integer> map = StringUtil.toMap(autoTools, Integer.class, Integer.class, '=', '|');
        if (map != null) {
            ToolManager.birthAddItemMap = map;
        }

        if (DataManager.getCommConfig("item_maxgrid") != null) {
            ToolManager.ITEM_MAX_GRID = Integer.parseInt(DataManager.getCommConfig("item_maxgrid"));
        }
        if (DataManager.getCommConfig("equip_maxgrid") != null) {
            ToolManager.EQUIP_MAX_GRID = Integer.parseInt(DataManager.getCommConfig("equip_maxgrid"));
        }
        /**
         * uc直通车
         */
        String uc_sql = "select * from uczhitongche;";
        List<com.stars.util._HashMap> ucGiftList = DBUtil.queryList(DBUtil.DB_PRODUCT, com.stars.util._HashMap.class, uc_sql);
        Map<Integer, Map<Integer, Integer>> ucGiftMap = new HashMap<>();
        Map<Integer, Integer> ucGiftEmail = new HashMap<>();
        for (_HashMap innerMap : ucGiftList) {
            int itemId = innerMap.getInt("uczhitongche.id");
            int number = innerMap.getInt("uczhitongche.number");
            String type = innerMap.getString("uczhitongche.type");
            int kaid = innerMap.getInt("uczhitongche.kaid");
            int emailTemplate = innerMap.getInt("uczhitongche.emailtemplate");
            ucGiftEmail.put(kaid, emailTemplate);
            Map<Integer, Integer> kaMap = ucGiftMap.get(kaid);
            if (kaMap == null) {
                kaMap = new HashMap<>();
                ucGiftMap.put(kaid, kaMap);
            }
            kaMap.put(itemId, number);
        }
        ToolManager.UC_GIFT_MAP = ucGiftMap;
        ToolManager.UC_GIFT_EMAIL_MAP = ucGiftEmail;
        ToolManager.TOOL_MAP = toolMap;
        ToolManager.TOOL_MAP_COE = toolMapCoe;
        ToolManager.ITEM_MAX_GRID = DataManager.getCommConfig("item_maxgrid", 50);
        ToolManager.EQUIP_MAX_GRID = DataManager.getCommConfig("item_maxgrid", 50);
    }

    @Override
    public void init() throws Exception {

        // 注册道具函数
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_EXP, RoleExpToolFunc.class);
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_BOX, BoxToolFunc.class);
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_UNLOCKEQUIP, UnlockEquipFunc.class);
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_BUDDYEXPBOX, BuddyExpBoxFunc.class);
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_FASHION, FashionToolFunc.class); // 坐骑道具
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_FRIEND_FLOWER, FriendFlowerFunc.class); // 好友鲜花道具
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_BUDDY_EQUIP, BuddyArmEquipFunc.class);// 伙伴武装道具
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_LEVEL, LevelToolFunc.class);//等级系数
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_BOX_NO_TIPS, BoxToolFunc2.class);   // 宝箱道具-无提示
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_MONTHCARD_DAYS, MonthCardFunc.class);   // 月卡道具
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_BOOK, BookToolFunc.class);   // 典籍碎片
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_FAHION_CARD, FashionCardToolFunc.class);//时装化身卡
        ToolManager.regToolFunc(ToolManager.FUNC_TYPE_OPTIONALTOOL, OptionalBoxFunc.class); //自选道具
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(GMDelToolEvent.class, new GMDelToolListener(module));
    }

    private void initToolFunc(Map<Integer, ItemVo> toolMap) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        for (ItemVo itemVo : toolMap.values()) {
            String fcuString = itemVo.getFunction();
            if (fcuString == null || fcuString.equals("0") || fcuString.equals("")) {
                continue;
            }
            byte type = Byte.parseByte(fcuString.split("[|]")[0]);
            Class<? extends ToolFunc> clazz = ToolManager.getToolFunc(type);
            if (clazz != null) {
                ToolFunc func = clazz.getConstructor(ItemVo.class).newInstance(itemVo);
                func.parseData(fcuString);
                itemVo.setToolFunc(func);
                itemVo.setFuncType(type);
            }
        }
    }

    /**
     * 特殊处理
     * 带系数的item，单独列出来
     */
    private Map<Integer, ItemVo> initCoeff(Map<Integer, ItemVo> toolMap) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Map<Integer, ItemVo> toolMapCoe = new HashMap<>();
        for (Map.Entry<Integer, ItemVo> entry : toolMap.entrySet()) {
            if (entry.getValue().getGradecoefftype() != 0) {
                toolMapCoe.put(entry.getKey(), entry.getValue());
            }
        }
        check(toolMapCoe);
        return toolMapCoe;
    }

    /**
     * 检查带系数的宝箱，如果嵌套配置就抛异常
     */
    private void check(Map<Integer, ItemVo> toolMapCoe) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Set<Integer> itemList = new HashSet<>();
        for (ItemVo itemVo : toolMapCoe.values()) {
            byte type = Byte.parseByte(itemVo.getFunction().split("[|]")[0]);
            Class<? extends ToolFunc> clazz = ToolManager.getToolFunc(type);
            if (BoxToolFunc.class == clazz) {
                BoxToolFunc func = (BoxToolFunc) clazz.getConstructor(ItemVo.class).newInstance(itemVo);
                func.parseData(itemVo.getFunction());
                for (int itemId : func.getTools().keySet()) {
                    itemList.add(itemId);
                }
            }
            if (BoxToolFunc2.class == clazz) {
                BoxToolFunc2 func = (BoxToolFunc2) clazz.getConstructor(ItemVo.class).newInstance(itemVo);
                func.parseData(itemVo.getFunction());
                for (int itemId : func.getTools().keySet()) {
                    itemList.add(itemId);
                }
            }
        }
        Iterator<Integer> iterator = itemList.iterator();
        while (iterator.hasNext()) {
            int itemId = iterator.next();
            if (toolMapCoe.containsKey(itemId)) {
                throw new IllegalArgumentException("策划数据配错了,不能嵌套配置宝箱:" + itemId);
            }
        }
    }

}
