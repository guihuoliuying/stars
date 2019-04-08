package com.stars.modules.tool;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.deityweapon.DeityWeaponManager;
import com.stars.modules.deityweapon.prodata.DeityWeaponVo;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.packet.ClientNewEquipment;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.handler.*;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.packet.ClientTool;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.modules.tool.userdata.RoleTokenEquipmentHolePo;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.modules.tool.userdata.RoleToolTable;
import com.stars.network.server.packet.Packet;
import com.stars.services.ServiceHelper;
import com.stars.util.I18n;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by Garwah on 2016/2/23.
 */
public class ToolModule extends AbstractModule {
    // 道具模块,几乎会被所有模块所依赖
    // 主要负责道具(含装备)的增删和保存(背包)

    private RoleToolTable itemBag;// 材料背包
    private RoleToolTable equipBag;// 装备背包
    private Map<Byte, ToolHandler> handlers = new HashMap<>();// 增删物品使用的handler
    private int tableId;
    private Map<Integer,Integer> dailyUsedLimitMap = new HashMap<>();
    private Map<Integer,Integer> weeklyUsedLimitMap = new HashMap<>();
    private Map<Integer,Integer> foreverUsedLimitMap = new HashMap<>();

    //常量
    private final static String DAILY_USED_TIME_LIMIT = "tool.use.daily.limit";
    private final static String WEEKLY_USED_TIME_LIMIT = "tool.use.weekly.limit";
    private final static String FOREVER_USED_TIME_LIMIT = "tool.use.forever.limit";

    // 制作人姓名,会被制作系统派发的事件修改
    private String makerName;

    public ToolModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Tool, id, self, eventDispatcher, moduleMap);
        this.tableId = (int) (id % 10);
    }

    @Override
    public void onInit(boolean isCreation) {
        itemBag.flushAllToClient();
        equipBag.flushAllToClient();
        signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
        if (isCreation) {
            autoAddItem();
        }
    }

    /**
     * 创建角色,需要初始化handler
     */
    @Override
    public void onCreation(String name, String account) {
        itemBag = new RoleToolTable(this, getTableId(), ToolManager.ITEM_BAG);
        equipBag = new RoleToolTable(this, getTableId(), ToolManager.EQUIP_BAG);
        initHandler();
        // 登录根据配置自动添加道具
        // autoAddItem();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        dailyUsedLimitMap.clear();
        setString(DAILY_USED_TIME_LIMIT,"");
    }

    @Override
    public void onWeeklyReset(boolean isLogin) throws Throwable {
        weeklyUsedLimitMap.clear();
        setString(WEEKLY_USED_TIME_LIMIT,"");
    }

    public void initEquipBasicAttr(RoleToolRow tool) {
        if (tool == null || !ToolManager.isEquip(tool.getItemId()))
            return;
        tool.setIsEquip((byte) 1);
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(tool.getItemId());
        tool.setEquipLevel(equipmentVo.getEquipLevel());
        tool.setEquipType(equipmentVo.getType());
        tool.setBasicAttr(equipmentVo.getAttributePacked());
        tool.setBasicFighting(equipmentVo.getBasicFighting());
        tool.setJobId(equipmentVo.getJob());
    }

    /**
     * 初始化装备基础信息 计算装备基础战力 & 额外战力
     */
    public void initBasicInfo(RoleToolRow tool) {
        calBasicFighting(tool);// 计算基础战力
        calExtraFighting(tool);// 计算额外属性战力
    }

    /**
     * 计算装备基础战力
     */
    private void calBasicFighting(RoleToolRow tool) {
        int fighting = FormularUtils.calFightScore(tool.getBasicAttr());
        if (StringUtil.isNotEmpty(tool.getExtraAttrMap())) {
            for (ExtraAttrVo vo : tool.getExtraAttrMap().values()) {
                fighting += vo.getFighting();
            }
        }
        tool.setFighting(fighting);
    }

    /**
     * 计算装备额外战力
     */
    public void calExtraFighting(RoleToolRow toolRow) {
        int maxExtraAttrFighting = 0;
        int extraAttrTotalFighting = 0;
        Attribute extraAttr = new Attribute();
        if (toolRow.getExtraAttrMap() != null) {
            for (ExtraAttrVo vo : toolRow.getExtraAttrMap().values()) {
                if (maxExtraAttrFighting == 0 || vo.getFighting() > maxExtraAttrFighting) {
                    maxExtraAttrFighting = vo.getFighting();
                }
                extraAttr.addSingleAttr(vo.getAttrName(), vo.getAttrValue());
                extraAttrTotalFighting += vo.getFighting();
            }
        }
        toolRow.setMaxExtraAttrFighting(maxExtraAttrFighting);
        toolRow.setExtraAttrFighting(extraAttrTotalFighting);
    }

    @Override
    public void onDataReq() throws Exception {
        String sql = new StringBuffer().append("select * from roletool").append(getTableId()).append(" where roleid=")
                .append(id()).toString();
        itemBag = new RoleToolTable(this, getTableId(), ToolManager.ITEM_BAG);
        equipBag = new RoleToolTable(this, getTableId(), ToolManager.EQUIP_BAG);
        initHandler();
        List<RoleToolRow> ls = DBUtil.queryList(DBUtil.DB_USER, RoleToolRow.class, sql);
        for (RoleToolRow tool : ls) {
            initEquipBasicAttr(tool);
            initBasicInfo(tool);
            addRoleTool(tool);
        }
        initUsedLimitData();
    }

    /**
     * 根据itemId返回handler 有可能会返回空
     */
    private ToolHandler getHandlerByItemId(int itemId) {
        byte bagType = ToolManager.getBagType(itemId);
        return handlers.get(bagType);
    }

    private ToolHandler getHandlerByToolId(long toolId) {
        RoleToolRow row = getToolById(toolId);
        if (row != null) {
            byte bagType = ToolManager.getBagType(row.getItemId());
            return handlers.get(bagType);
        }
        return null;
    }

    /**
     * 拿到唯一的道具id
     */
    public long getNextToolId() {
        // return System.currentTimeMillis();
        // return DataManager2.getIdGenerator().newId(Uid.ITEM.getKey());
        return ServiceHelper.idService().newToolId();
    }

    private AddToolResult addToolByItemId(int itemId, int count, short eventType) {
        return addToolByItemId(itemId, count, 0, eventType);
    }

    /**
     * 根据配置id增加道具 有几种情况 1,道具:直接加 2,资源,加到人物模块 3,装备,生成一个空对象,交给装备模块赋值
     * 需要经过判断,最后返回未被添加的数量
     * <p>
     * clientSystemConstant:默认传个0进来即可,用于客户端的显示逻辑需要;
     * eventType:捆绑事件类型,对应EventType的code
     */
    private AddToolResult addToolByItemId(int itemId, int count, int clientSystemConstant, short eventType) {
        ToolHandler handler = getHandlerByItemId(itemId);
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (handler == null || count <= 0) {
            com.stars.util.LogUtil.info("添加道具异常,请检查:" + id() + "|" + itemId + "|" + count);
            return null;
        }
        if (isCoeffTool(itemId)) {
            count = coeffCount(itemId, count);
        }
        int addCount = handler.canAdd(itemId, count);
        if (handler instanceof ResouceHandler) {
            ((ResouceHandler) handler).setClientSystemConstant(clientSystemConstant);
        }
        AddToolResult addToolResult = new AddToolResult(handler.add(itemId, addCount, eventType));
        addToolResult.setAddCount(addCount);
        addToolResult.setResCount(count - addCount);
        if (addCount > 0) {
            if (itemVo != null && itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX) {// 新增宝箱
                signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
            }
        }
        // 需要添加的数量-实际增加的数量 = 未被增加的数量
        return addToolResult;
    }

    /**
     * @param itemId
     * @return
     */
    public boolean isCoeffTool(int itemId) {
        if (ToolManager.TOOL_MAP_COE.containsKey(itemId)) {
            return true;
        }
        return false;
    }

    /**
     * 返回物品乘以系数之后的数量
     *
     * @param itemId
     * @param count
     * @return
     */
    public int coeffCount(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo != null && itemVo.getGradecoefftype() != 0) {// 等级系数
            RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
            double coeff = DataManager.getGradeCoeff(roleModule.getLevel() + "+" + itemVo.getGradecoefftype());
            count = (int) Math.floor(coeff / 100 * (double) count);
        }
        return count;
    }

    public Map<Integer, Integer> coeffCount(Map<Integer, Integer> toolMap) {
        Map<Integer, Integer> newToolMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
            newToolMap.put(entry.getKey(), coeffCount(entry.getKey(), entry.getValue()));
        }
        return newToolMap;
    }

    /**
     * 特殊接口,仅在替换装备时会用，慎用！ 新增装备道具时附带额外属性
     */
    public void addEquipWithExtAttr(int itemId, Map<Byte, ExtraAttrVo> attrMap, Map<Byte, RoleTokenEquipmentHolePo> roleTokenEquipmentHolePoMap,
                                    int tokenSkillId, int tokenSkillLevel) {
        ToolHandler toolHandler = getHandlerByItemId(itemId);
        if (toolHandler == null)
            return;
        EquipHandler handler = (EquipHandler) toolHandler;
        handler.addEquipWithExtAttr(itemId, attrMap, roleTokenEquipmentHolePoMap, tokenSkillId, tokenSkillLevel);
    }

    /**
     * 添加物品已满时就发邮件
     */
    private void sendResToolByMail(Map<Integer, Integer> restMap) {
        if (StringUtil.isEmpty(restMap))
            return;
        Map<Integer, Integer> itemMap = new HashMap<>();
        Map<Integer, Integer> equipMap = new HashMap<>();
        splitTool(restMap, itemMap, equipMap);

        RoleModule roleModule = module(MConst.Role);
        String roleName = roleModule.getRoleRow().getName();
        try {
            if (StringUtil.isNotEmpty(itemMap)) {
                ServiceHelper.emailService().sendToSingle(id(), ToolManager.EMAIL_ID, id(), roleName, itemMap);
                warn("item_bag_full");
            }
            if (StringUtil.isNotEmpty(equipMap)) {
                ServiceHelper.emailService().sendToSingle(id(), ToolManager.EQUIP_FULL_EMAIL_ID, id(), "系统", equipMap);
                warn("item_euipbag_full");
            }
        } catch (Exception e) {
            com.stars.util.LogUtil.error("", e);
        } finally {
            com.stars.util.LogUtil.info("玩家背包已满，剩余道具通过邮件发送: roleId={}, toolMap={}", id(), StringUtil.makeString(restMap, '+', '|'));
        }
    }

    /**
     * 是否能增加某个道具 道具模块每次增加都会成功,空格不够会发邮件 此方法主要是提供给其他模块要在增加资源前检查的逻辑
     * 2016.05版本判断逻辑为:只要可添加数量大于0,允许执行增加操作
     */
    public boolean canAdd(int itemId, int count) {
        ToolHandler handler = getHandlerByItemId(itemId);
        if (handler != null) {
            // 数量为负
            if (count < 0) {
                return contains(itemId, -1 * count);
            }
            int addCount = handler.canAdd(itemId, count);
            return addCount > 0;
        }
        return false;
    }

    /**
     * 能否添加某组道具map 必须为能够全部增加时才会返回true 否则返回false
     */
    public boolean canAdd(Map<Integer, Integer> toolMap) {
        Map<Byte, Map<Integer, Integer>> typeMap = new HashMap<>();
        byte bagType;
        Map<Integer, Integer> checkMap;
        // 物品按bagType分类
        for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
            bagType = ToolManager.getBagType(entry.getKey());
            checkMap = typeMap.get(bagType);
            if (checkMap == null) {
                checkMap = new HashMap<>();
                typeMap.put(bagType, checkMap);
            }
            if (checkMap.containsKey(entry.getKey())) {
                checkMap.put(entry.getKey(), checkMap.get(entry.getKey()) + entry.getValue());
            } else {
                checkMap.put(entry.getKey(), entry.getValue());
            }
        }
        ToolHandler handler;
        // 按bagType分类判断,某一种类型无法全部增加即返回false
        for (Map.Entry<Byte, Map<Integer, Integer>> entry : typeMap.entrySet()) {
            handler = handlers.get(entry.getKey());
            if (handler == null)
                continue;
            if (!handler.canAdd(entry.getValue())) {// 无法全部增加
                return false;
            }
        }
        return true;
    }

    /**
     * 派发增加道具事件
     *
     * @param count:增加的数量(包裹内+邮件)
     */
    public void fireAddItemEvent(int itemId, int count) {
        Map<Integer, Integer> toolMap = new HashMap<>();
        toolMap.put(itemId, count);
        eventDispatcher().fire(new AddToolEvent(toolMap));
    }

    public void fireAddItemEvent(Map<Integer, Integer> toolMap) {
        eventDispatcher().fire(new AddToolEvent(toolMap));
    }

    /**
     * 增加道具并且下发数据 暂时每次增加道具都会下发对应背包的所有数据
     */
    public Map<Integer, Integer> addAndSend(int itemId, int count, short eventType) {
        Map<Integer, Integer> realGetMap = new HashMap<>();
        AddToolResult addToolResult = addToolByItemId(itemId, count, eventType);
        if (addToolResult != null) {
            com.stars.util.MapUtil.add(realGetMap, addToolResult.getRealGetMap());
            if (addToolResult.getResCount() > 0) {
                HashMap<Integer, Integer> restMap = new HashMap<>();
                restMap.put(itemId, addToolResult.getResCount());
                sendResToolByMail(restMap);
                com.stars.util.MapUtil.add(realGetMap, restMap);
            }
        }
        // sendItem(itemId,count);
        ServerLogModule log = module(MConst.ServerLog);
        log.Log_core_item(itemId, count, eventType, (byte) 1);
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
        fireAddItemEvent(itemId, count);
        return realGetMap;

    }

    /**
     * 新增道具并下发,装备和道具都可以一并新增 key:item的配置id. value:数量
     */
    public Map<Integer, Integer> addAndSend(Map<Integer, Integer> toolMap, short eventType) {
        Iterator iter = toolMap.entrySet().iterator();
        int itemId, count;
        HashMap<Integer, Integer> restMap = new HashMap<>();
        Map<Integer, Integer> realGetMap = new HashMap<>();
        AddToolResult addToolResult;
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            itemId = entry.getKey();
            count = entry.getValue();
            addToolResult = addToolByItemId(itemId, count, eventType);
            if (addToolResult != null) {
                com.stars.util.MapUtil.add(realGetMap, addToolResult.getRealGetMap());
                if (addToolResult.getResCount() > 0) {
                    if (restMap.containsKey(itemId)) {
                        restMap.put(itemId, restMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        restMap.put(itemId, addToolResult.getResCount());
                    }

                    if (realGetMap.containsKey(itemId)) {
                        realGetMap.put(itemId, realGetMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        realGetMap.put(itemId, addToolResult.getResCount());
                    }
                }
            }
        }
        sendResToolByMail(restMap);

        if (!com.stars.util.EmptyUtil.isEmpty(toolMap)) {// 有物品变动打日志
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
            log.Log_core_item(toolMap, null, eventType);
        }
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
        fireAddItemEvent(toolMap);
        return realGetMap;
    }

    private void splitTool(Map<Integer, Integer> restMap, Map<Integer, Integer> itemMap,
                           Map<Integer, Integer> equipMap) {
        if (StringUtil.isEmpty(restMap))
            return;
        for (Map.Entry<Integer, Integer> entry : restMap.entrySet()) {
            if (ToolManager.isEquip(entry.getKey())) {
                equipMap.put(entry.getKey(), entry.getValue());
            } else {
                itemMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 新增道具并下发,装备和道具都可以一并新增 key:item的配置id. value:数量 带有客户端系统参数
     */
    public Map<Integer, Integer> addAndSend(Map<Integer, Integer> toolMap, int clientSystemConstant, short eventType) {
        Iterator iter = toolMap.entrySet().iterator();
        int itemId, count;
        AddToolResult addToolResult;
        HashMap<Integer, Integer> restMap = new HashMap<>();
        Map<Integer, Integer> realGetMap = new HashMap<>();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            itemId = entry.getKey();
            count = entry.getValue();
            addToolResult = addToolByItemId(itemId, count, clientSystemConstant, eventType);
            if (addToolResult != null) {
                com.stars.util.MapUtil.add(realGetMap, addToolResult.getRealGetMap());
                if (addToolResult.getResCount() > 0) {
                    if (restMap.containsKey(itemId)) {
                        restMap.put(itemId, restMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        restMap.put(itemId, addToolResult.getResCount());
                    }

                    if (realGetMap.containsKey(itemId)) {
                        realGetMap.put(itemId, realGetMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        realGetMap.put(itemId, addToolResult.getResCount());
                    }
                }
            }
        }
        sendResToolByMail(restMap);
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_item(toolMap, null, eventType);

        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL, clientSystemConstant);
        fireAddItemEvent(toolMap);
        return realGetMap;
    }

    /**
     * 新增道具但不下发 key:item的配置id. value:数量
     */
    public Map<Integer, Integer> addNotSend(Map<Integer, Integer> toolMap, short eventType) {
        Iterator iter = toolMap.entrySet().iterator();
        int itemId, count, restCount;
        HashMap<Integer, Integer> restMap = new HashMap<>();
        Map<Integer, Integer> realGetMap = new HashMap<>();
        AddToolResult addToolResult;
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            itemId = entry.getKey();
            count = entry.getValue();
            addToolResult = addToolByItemId(itemId, count, eventType);
            if (addToolResult != null) {
                com.stars.util.MapUtil.add(realGetMap, addToolResult.getRealGetMap());
                if (addToolResult.getResCount() > 0) {
                    if (restMap.containsKey(itemId)) {
                        restMap.put(itemId, restMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        restMap.put(itemId, addToolResult.getResCount());
                    }

                    if (realGetMap.containsKey(itemId)) {
                        realGetMap.put(itemId, realGetMap.get(itemId) + addToolResult.getResCount());
                    } else {
                        realGetMap.put(itemId, addToolResult.getResCount());
                    }
                }
            }
        }
        sendResToolByMail(restMap);
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_item(toolMap, null, eventType);
        // fireAddItemEvent(toolMap);
        return realGetMap;
    }

    /**
     * 新增道具并下发,装备和道具都可以一并新增 格式:itemid=道具数量,分号隔开/如100001+200|100002+300
     */
    public Map<Integer, Integer> addAndSend(String toolStr, short eventType) {
        return addAndSend(toolStr, 0, eventType);
    }

    /**
     * 新增道具并下发,装备和道具都可以一并新增 格式:itemid=道具数量,分号隔开/如100001+200|100002+300
     */
    public Map<Integer, Integer> addAndSend(String toolStr, int clientSystemConstant, short eventType) {
        if (com.stars.util.EmptyUtil.isEmpty(toolStr)) {
            return null;
        }
        Map<Integer, Integer> toolMap = decodeToolStrToMap(toolStr);
        return addAndSend(toolMap, clientSystemConstant, eventType);
    }

	/*
     * private void sendItem(int itemId,int count){ Map<Integer, Integer> map =
	 * new HashMap<>(); map.put(itemId,count); // sendItem(map); }
	 * 
	 * private void sendItem(Map<Integer, Integer> itemMap){ sendPacket(new
	 * ClientAward(removeAutoUseTool(itemMap))); }
	 * 
	 * private Map<Integer, Integer> removeAutoUseTool (Map<Integer, Integer>
	 * map){ List<Integer> maps = isAutoUse(map); if(!maps.isEmpty()){ for(int
	 * itemId:maps){ map.remove(itemId); } } return map; }
	 * 
	 * private List<Integer> isAutoUse(Map<Integer, Integer> tempMap){
	 * List<Integer> itemIds = new ArrayList<>(); ItemVo itemVo ; for(int itemId
	 * : tempMap.keySet()){ itemVo = ToolManager.getItemVo(itemId);
	 * if(itemVo.isAutoUse()){ itemIds.add(itemId); } } return itemIds; }
	 */

    public Map<Integer, Integer> decodeToolStrToMap(String toolStr) {
        HashMap<Integer, Integer> toolMap = new HashMap<>();
        String[] idAndCounts = toolStr.split(",");
        for (String str : idAndCounts) {
            String[] idAndCount = str.split("\\+");
            if (idAndCount.length != 2) {
                continue;
            }
            toolMap.put(Integer.parseInt(idAndCount[0]), Integer.parseInt(idAndCount[1]));
        }
        return toolMap;
    }

    /**
     * 删除物品前的检查
     */
    private boolean checkBeforeDelete(int itemId, int count) {
        if (count <= 0)
            return false;
        byte bagType = ToolManager.getBagType(itemId);
        if (bagType == ToolManager.EQUIP_BAG) {
            com.stars.util.LogUtil.info("不允许通过配置id删除装备");
            return false;
        }
        if (!contains(itemId, count)) {
            com.stars.util.LogUtil.info("数量不足以扣除:" + itemId + "|" + count + "|" + id());
            return false;
        }
        return true;
    }

    /**
     * 删除物品前的检查
     */
    private boolean checkBeforeDelete(Map<Integer, Integer> toolMap) {
        if (!contains(toolMap)) {
            return false;
        }
        Iterator iter = toolMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            byte bagType = ToolManager.getBagType(entry.getKey());
            if (bagType == ToolManager.EQUIP_BAG) {
                com.stars.util.LogUtil.info("不允许通过配置id删除装备");
                return false;
            }
        }
        return true;
    }

    /**
     * 根据配置id删除道具 不会下发数据 调用这个方法必须经过检查
     */
    private void deleteByItemId(int itemId, int count, short eventType) {
        if (count <= 0)
            return;
        ToolHandler handler = getHandlerByItemId(itemId);
        handler.deleteByItemId(itemId, count, eventType);
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_item(itemId, count, eventType, (byte) 0);
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo != null && itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX) {
            signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
        }
        eventDispatcher().fire(new UseToolEvent(itemId, count));
    }

    public void deleteByToolId(long toolId, int count, short eventType) {
        ToolHandler handler = getHandlerByToolId(toolId);
        RoleToolRow row = getToolById(toolId);
        if (handler != null) {
            RoleToolRow toolRow = getToolById(toolId);
            handler.deleteByToolId(toolId, count);
            ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);

            if (toolRow == null) {
                com.stars.util.LogUtil.info("不存在toolId={}的道具数据");
                return;
            }
            int itemId = toolRow.getItemId();
            log.Log_core_item(itemId, count, eventType, (byte) 0);
            eventDispatcher().fire(new UseToolEvent(itemId, count));
        }
        if (row != null) {
            ItemVo itemVo = ToolManager.getItemVo(row.getItemId());
            if (itemVo != null && itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX) {
                signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
            }
        }

    }

    /**
     * 根据道具类型删除道具 （紧用于道具背包）
     *
     * @param type
     * @param eventType
     */
    public void deleteByType(byte type, short eventType) {
        Map<Long, RoleToolRow> toolMap = itemBag.getToolMap();
        int itemId = 0;
        List<int[]> delectList = new ArrayList<>();
        for (RoleToolRow toolRow : toolMap.values()) {
            itemId = toolRow.getItemId();
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo.getType() != type)
                continue;
            delectList.add(new int[]{itemId, toolRow.getCount()});
        }
        for (int[] deleteInfo : delectList) {
            deleteAndSend(deleteInfo[0], deleteInfo[1], eventType);
        }
    }

    /**
     * 根据配置id删除道具 只允许删除道具和资源,不准删装备 只有删除成功才会return true.并且下发道具
     */
    public boolean deleteAndSend(int itemId, int count, short eventType) {
        if (!checkBeforeDelete(itemId, count)) {
            return false;
        }
        deleteByItemId(itemId, count, eventType);
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ITEM);
        if (count > 0) {
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo != null && itemVo.getFuncType() == ToolManager.FUNC_TYPE_BOX) {// 新增宝箱
                signCalRedPoint(MConst.Tool, RedPointConst.BAG_USE_BOX);
            }
        }
        return true;
    }

    /**
     * 清空背包命令
     *
     * @return
     */
    public boolean deleteAndSend(short eventType) {
        Map<Long, RoleToolRow> itemMap = itemBag.getToolMap();
        Map<Long, String> map = new HashMap<>();
        for (Map.Entry<Long, RoleToolRow> roleToolRowEntry : itemMap.entrySet()) {
            map.put(roleToolRowEntry.getKey(),
                    roleToolRowEntry.getValue().getItemId() + "+" + roleToolRowEntry.getValue().getCount());
        }
        for (Map.Entry<Long, String> longStringEntry : map.entrySet()) {
            String str = longStringEntry.getValue();
            int[] ints;
            try {
                ints = StringUtil.toArray(str, int[].class, '+');

            } catch (Exception e) {
                ints = new int[]{0, 0};
            }
            deleteAndSend(ints[0], ints[1], eventType);
        }
        return true;
    }

    /**
     * 根据map删除道具 检查完再删.
     */
    public boolean deleteAndSend(Map<Integer, Integer> toolMap, short eventType) {
        if (!checkBeforeDelete(toolMap)) {
            return false;
        }
        Iterator iter = toolMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            deleteByItemId(entry.getKey(), entry.getValue(), eventType);
        }
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ITEM);
        return true;
    }

    /**
     * 根据配置id返回数量
     *
     * @param itemId
     */
    public long getCountByItemId(int itemId) {
        ToolHandler handler = getHandlerByItemId(itemId);
        if (handler == null) {
            com.stars.util.LogUtil.info("检查道具数量,请检查:" + id() + "|" + itemId);
            return -1;
        }
        return handler.getCountByItemId(itemId);
    }

    /**
     * 某个id的道具是否足够
     */
    public boolean contains(int itemId, long count) {
        ToolHandler handler = getHandlerByItemId(itemId);
        if (handler == null) {
            com.stars.util.LogUtil.info("检查道具数量,请检查:" + id() + "|" + itemId);
            return false;
        }
        long roleCount = handler.getCountByItemId(itemId);
        // if (roleCount <= 0 || roleCount < count) {
        // return false;
        // }
        if (roleCount < 0 || roleCount < count) {
            return false;
        }
        return true;
    }

    /**
     * 检查一堆东西够不够 如果toolMap是空的会返回true
     */
    public boolean contains(Map<Integer, Integer> toolMap) {
        if (com.stars.util.EmptyUtil.isEmpty(toolMap)) {
            return true;
        }
        Iterator iter = toolMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iter.next();
            if (!contains(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public boolean containsTool(long toolId, int count) {
        if (count <= 0)
            return false;
        RoleToolRow row = getToolById(toolId);
        if (row != null) {
            ToolHandler handler = getHandlerByToolId(toolId);
            if (handler == null) {
                com.stars.util.LogUtil.info("检查道具数量,请检查:" + id() + "|" + toolId);
                return false;
            }
            long roleCount = row.getCount();
            if (roleCount <= 0 || roleCount < count) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 根据toolid返回道具对象 要在两个背包里搜索 有可能会返回空 此方法会在删除道具时被调用
     */
    private RoleToolRow getToolById(long toolId) {
        // 先找道具包
        RoleToolRow toolRow = null;
        toolRow = getItemById(toolId);
        if (toolRow != null) {
            return toolRow;
        }
        toolRow = getEquipById(toolId);
        return toolRow;
    }

    /**
     * 根据唯一id找到道具对象
     *
     * @param toolId
     * @return
     */
    public RoleToolRow getItemById(long toolId) {
        return itemBag.getToolMap().get(toolId);
    }

    public Map<Long, RoleToolRow> getItemMap() {
        return itemBag.getToolMap();
    }

    /**
     * 根据唯一id找到装备对象
     */
    public RoleToolRow getEquipById(long toolId) {
        return equipBag.getToolMap().get(toolId);
    }

    public Map<Long, RoleToolRow> getEquipToolMap() {
        return equipBag.getToolMap();
    }

    public HashSet<RoleToolRow> getRoleEquipFlushToClient(){
        return equipBag.getFlushToClient();
    }

    public int getDailyUsedLimitByItemId(int itemId){
        Integer usedTimes = dailyUsedLimitMap.get(itemId);
        return usedTimes == null ? 0 : usedTimes;
    }
    public int getWeeklyUsedLimitByItemId(int itemId){
        Integer usedTimes = weeklyUsedLimitMap.get(itemId);
        return usedTimes == null ? 0 : usedTimes;
    }

    public int getForeverUsedLimitByItemId(int itemId){
        Integer usedTimes = foreverUsedLimitMap.get(itemId);
        return usedTimes == null ? 0 : usedTimes;
    }

    public Map<Integer, Integer> getDailyUsedLimitMap() {
        return dailyUsedLimitMap;
    }

    public void setDailyUsedLimitMap(Map<Integer, Integer> dailyUsedLimitMap) {
        this.dailyUsedLimitMap = dailyUsedLimitMap;
    }

    public Map<Integer, Integer> getWeeklyUsedLimitMap() {
        return weeklyUsedLimitMap;
    }

    public void setWeeklyUsedLimitMap(Map<Integer, Integer> weeklyUsedLimitMap) {
        this.weeklyUsedLimitMap = weeklyUsedLimitMap;
    }

    public Map<Integer, Integer> getForeverUsedLimitMap() {
        return foreverUsedLimitMap;
    }

    public void setForeverUsedLimitMap(Map<Integer, Integer> foreverUsedLimitMap) {
        this.foreverUsedLimitMap = foreverUsedLimitMap;
    }

    /**
     * 穿装备时道具模块需要处理的内容 1,把道具改成被穿上的状态 2,整理格子
     */
    private void putOn(long toolId, int generalId, int position) {
        // RoleToolRow toolRow = getEquipById(toolId);
        // if (toolRow == null) {
        // return ;
        // }
        // int oldGrid = toolRow.getGrid();
        // toolRow.setEquipStr(generalId + "_" + position);
        // toolRow.setGrid(0);
        // toolRow.setUpdateStatus();
        // EquipHandler handler = (EquipHandler)
        // handlers.get(ToolManager.EQUIP_BAG);
        // handler.fillAfterGrid(oldGrid);
    }

    /**
     * 脱掉装备 1,脱掉,拿一个新的格子填上 2,下发数据 注:如果穿了以后再脱,会有空格子出现,但是如果单独执行脱逻辑,不一定会有格子
     */
    private void putOff(long toolId) {
        // RoleToolRow toolRow = getEquipById(toolId);
        // EquipHandler handler = (EquipHandler)
        // handlers.get(ToolManager.EQUIP_BAG);
        // int nullGrid = handler.getNullGrid();
        // if (nullGrid <= 0 || toolRow == null) {
        // return ;
        // }
        // toolRow.setGrid(nullGrid);
        // equipBag.getGridMap().put(nullGrid, toolId);
        // toolRow.setEquipStr("");
        // toolRow.setUpdateStatus();
    }

    /**
     * 把某个物品的新表识去掉
     */
    // public void changeNewFlag(byte bagType,int grid){
    // RoleToolRow toolRow = getToolByGrid(bagType,grid);
    // if(toolRow == null || toolRow.getNewFlag() == (byte)0){
    // return;
    // }
    // toolRow.setNewFlag((byte)0);
    // toolRow.setUpdateStatus();
    // }

    /**
     * 处理制作系统发来的事件 1,先把制作人的姓名改了 2,制作完了改回来
     */
    // public void disPatchMakeEvent(MakeEvent event){
    // this.makerName = event.getName();
    // Map<Integer,Integer> toolmap = event.getMap();
    // addAndSend(toolmap);
    // this.makerName = "";
    // }

    /**
     * 处理穿上装备的事件 1,先穿上装备,修改格子号 2,再脱下装备,先穿再脱,格子号会空出来,所以肯定会成功
     *
     * @param
     */
    // public void disPatchPutOnEvent(PutOnEquipEvent event){
    // long oldEquipId = event.getOldEquipId() == null? 0:event.getOldEquipId();
    // putOn(event.getNewEquipId(),event.getGeneralId(),event.getPosition());
    // if(oldEquipId > 0){
    // putOff(oldEquipId);
    // }
    // sendAll();
    // }
    @Override
    public Map<String, Module> moduleMap() {
        return super.moduleMap();
    }

    /**
     * 初始化各个handler
     */

    private void initHandler() {
        handlers.put(ToolManager.RESOUCE_BAG, new ResouceHandler((RoleModule) module(MConst.Role)));
        handlers.put(ToolManager.ITEM_BAG, new ItemHandler(this, itemBag));
        handlers.put(ToolManager.EQUIP_BAG,
                new EquipHandler((NewEquipmentModule) module(MConst.NewEquipment), this, equipBag));
        handlers.put(ToolManager.FAMILY_CONTRIBUTION_BAG, new FamilyContributionToolHandler(id(), moduleMap()));
    }

    /**
     * 获取背包中的宝石id列表;
     *
     * @return
     */
    public List<Integer> getBagGemIdList() {
        ItemVo itemVo;
        int tmpid;
        List<Integer> rtnList = new ArrayList<>();
        Map<Long, RoleToolRow> itemMap = itemBag.getToolMap();
        Collection<RoleToolRow> itemRoleToolRowColl = itemMap.values();
        for (RoleToolRow roleToolRow : itemRoleToolRowColl) {
            tmpid = roleToolRow.getItemId();
            itemVo = ToolManager.getItemVo(tmpid);
            if (itemVo != null && itemVo.getType() == ToolManager.TYPE_DIAMOND) {
                if (!rtnList.contains(tmpid)) {
                    rtnList.add(tmpid);
                }
            }
        }
        return rtnList;
    }

    /**
     * 给玩家增加生成好的道具 走这个步骤前必须经过判断背包是否已满
     *
     * @param toolRow
     */
    private void addRoleTool(RoleToolRow toolRow) {
        ToolHandler handler = getHandlerByItemId(toolRow.getItemId());
        if (handler == null) {
            com.stars.util.LogUtil.info("增加道具handler异常,玩家id:" + id() + ",配置id:" + toolRow.getItemId());
            return;
        }
        handler.add(toolRow);
    }

    /**
     * 登录自动添加道具 需要满足角色等级为1,且背包没有任何道具
     */
    private void autoAddItem() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (roleModule.getLevel() != 1) {
            return;
        }
        if (!itemBag.getToolMap().isEmpty()) {
            return;
        }
        Map<Integer, Integer> addMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : ToolManager.birthAddItemMap.entrySet()) {
            addMap.put(entry.getKey(), entry.getValue());
        }
        addNotSend(addMap, EventType.CREATEADDTOOL.getCode());
    }

    @Override
    public void onSyncData() {
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
    }

    /**
     * 下发数据
     */
    public void flushToClient(byte flushType) {
        int defaultClientSystemConstant = 0;
        if (flushType == ToolManager.FLUSH_BAG_TYPE_ALL || flushType == ToolManager.FLUSH_BAG_TYPE_ITEM) {
            ClientTool toolPacket = new ClientTool(itemBag.getFlushToClient(), defaultClientSystemConstant);
            send(toolPacket);
        }

        if (flushType == ToolManager.FLUSH_BAG_TYPE_ALL || flushType == ToolManager.FLUSH_BAG_TYPE_EQUIP) {
            ClientTool equipPacket = new ClientTool(equipBag.getFlushToClient(), defaultClientSystemConstant);
            send(equipPacket);
        }
    }

    /**
     * 下发数据,会交给creator处理,哪个包有改变就发哪个包 带有客户端系统参数
     */
    public void flushToClient(byte flushType, int clientSystemConstant) {
        if (flushType == ToolManager.FLUSH_BAG_TYPE_ALL || flushType == ToolManager.FLUSH_BAG_TYPE_ITEM) {
            ClientTool toolPacket = new ClientTool(itemBag.getFlushToClient(), clientSystemConstant);
            send(toolPacket);
        }

        if (flushType == ToolManager.FLUSH_BAG_TYPE_ALL || flushType == ToolManager.FLUSH_BAG_TYPE_EQUIP) {
            ClientTool equipPacket = new ClientTool(equipBag.getFlushToClient(), clientSystemConstant);
            send(equipPacket);
        }
    }

    /**
     * 根据配置id返回道具名 非道具非装备则返回空
     */
    public String getToolName(int itemId) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo != null) {
            return itemVo.getName();
        }
        return null;
    }

    /**
     * 为了在handler中可以下发数据包,把send方法开放出来
     */
    public void sendPacket(Packet packet) {
        send(packet);
    }

    /**
     * @param itemId
     * @param count
     * @param args   使用所有该类型的道具，用于主界面弹出得宝箱使用
     */
    public void useAllToolByItemId(int itemId, int count, Object... args) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        long roleOwnCount = this.getCountByItemId(itemId); //玩家拥有的数量
        if (roleOwnCount == 0) {
            warn("没有该类型的道具");
            return;
        }
        if (roleOwnCount < count) {
            warn("道具数量不足");
        }
        this.useToolByItemId(itemId, (int) count, args);
        eventDispatcher().fire(new UseToolEvent(itemId, (int) count));
    }

    /**
     * 使用道具 目前没有批量使用需求,如果有,这里增加次数即可
     *
     * @param itemId
     */
    public void useToolByItemId(int itemId, int count, Object... args) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        if (!contains(itemId, count)) {
            com.stars.util.LogUtil.info("背包道具数量不足roleId={},itemId={}", id(), itemId);
            return;
        }

        ToolFunc func = itemVo.getToolFunc();
        if (func != null) {
            // 先做业务上的检查
            ToolFuncResult result = func.check(moduleMap(), count, args);
            if (result != null && !result.isSuccess()) {
                if (result.getMessage() != null) {
                    send(result.getMessage());
                    com.stars.util.LogUtil.info("道具使用失败, roleId={}, itemId={}, count={}, message={}", id(), itemId, count,
                            result.getMessage().getKey());
                }
                return;
            }
            try {
                short eventType = 0;
                if(hasUsedLimit(itemId)){ //次数有限制，只能使用剩余限制次数
                    count = getRemainUseCount(itemId,count);
                }
                deleteByItemId(itemId, count, eventType);
                func.use(moduleMap(), count, args);
                saveUsedLimitData(itemId,count); //记录次数限制
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("道具使用异常, roleId=" + id() + ", itemId=" + itemId + ", count=" + count, cause);
            }
        } else {
            com.stars.util.LogUtil.info("道具不能使用, roleId={}, itemId={}", id(), itemId);
        }
        // 下发背包改变
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);

    }

    /**
     * 使用不在背包中的道具, 不会从背包中删除和检测, 外部去维护道具的状态;
     *
     * @param itemId
     */
    public void useToolNoInBagByItemId(int itemId, int count, Object... args) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        ToolFunc func = itemVo.getToolFunc();
        if (func != null) {
            // 先做业务上的检查
            ToolFuncResult result = func.check(moduleMap(), count, args);
            if (result != null && !result.isSuccess()) {
                if (result.getMessage() != null) {
                    send(result.getMessage());
                    com.stars.util.LogUtil.info("道具使用失败, roleId={}, itemId={}, count={}, message={}", id(), itemId, count,
                            result.getMessage().getKey());
                }
                return;
            }
            try {
                func.use(moduleMap(), count, args);
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("道具使用异常, roleId=" + id() + ", itemId=" + itemId + ", count=" + count, cause);
            }
        } else {
            com.stars.util.LogUtil.info("道具不能使用, roleId={}, itemId={}", id(), itemId);
        }
    }

    /**
     * @param itemId
     * @param count  使用道具不做任何条件检测
     */
    public Map<Integer, Integer> useToolByItemIdNotCheck(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        ToolFunc func = itemVo.getToolFunc();
        if (func != null) {
            return func.use(moduleMap(), count);
        }
        return null;
    }

    /**
     * 使用道具总入口
     *
     * @param toolId
     */
    public void useTool(long toolId) {
        useTool(toolId, 1);
    }

    /**
     * 本模块使用道具入口
     */
    public void useTool(long toolId, int count, Object... args) {
        RoleToolRow toolRow = getToolById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("不存在toolId={}的道具数据");
            return;
        }
        int itemId = toolRow.getItemId();
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        // 检查道具是否有足够数量
        if (!containsTool(toolId, count)) {
            com.stars.util.LogUtil.info("背包道具数量不足roleId={}, itemId={}, toolId={}", id(), toolRow.getItemId(), toolId);
            return;
        }
        // 使用道具
        ToolFunc func = itemVo.getToolFunc();
        if (func != null) {
            // 先做业务上的检查
            ToolFuncResult result = func.check(moduleMap(), count, toolRow.getBornTime(), args);
            if (!result.isSuccess()) {
                if (result.getMessage() != null) {
                    send(result.getMessage());
                    com.stars.util.LogUtil.info("道具使用失败, roleId={}, itemId={}, count={}, message={}", id(), itemId, count,
                            result.getMessage().getKey());
                }
                return;
            }
            // 先删再用
            try {
                if(hasUsedLimit(itemId)){ //次数有限制，只能使用剩余限制次数
                    count = getRemainUseCount(itemId,count);
                }
                deleteByToolId(toolId, count, EventType.USETOOL.getCode());
                com.stars.util.LogUtil.info("使用道具, roleId={}, toolId={}, itemId={}, count={}", id(), toolId, itemId, count);
                func.use(moduleMap(), count, args);
                saveUsedLimitData(itemId,count); //记录次数限制
            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("道具使用异常, roleId=" + id() + ", itemId=" + itemId + ", count=" + count, cause);
            }
        } else {
            com.stars.util.LogUtil.info("道具不能使用, roleId={}, itemId={}", id(), itemId);
        }
        // 下发背包改变
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);

    }

    /**
     * 出售道具给系统
     *
     * @param toolId
     * @param count
     */
    public void sellTool(long toolId, int count) {
        if (count <= 0) {
            com.stars.util.LogUtil.info("出售物品异常，出售数量低于0，count:", count);
            return;
        }
        RoleToolRow toolRow = getToolById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("不存在toolId={}的道具数据");
            return;
        }
        int itemId = toolRow.getItemId();
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        // 检查道具是否有足够数量
        if (!containsTool(toolId, count)) {
            com.stars.util.LogUtil.info("背包道具数量不足roleId={}, itemId={}, toolId={}", id(), toolRow.getItemId(), toolId);
            return;
        }
        deleteByToolId(toolId, count, EventType.SELLTOOL.getCode());
        addAndSend(itemVo.getSellPrice()[0], (itemVo.getSellPrice()[1]) * count, EventType.SELLTOOL.getCode());
        eventDispatcher().fire(new UseToolEvent(itemId, count));
        // 客户端提示
        Map<Integer, Integer> m = new HashMap<Integer, Integer>();
        m.put(itemVo.getSellPrice()[0], (itemVo.getSellPrice()[1]) * count);
        send(new ClientAward(m));
    }

    public String getMakerName() {
        return makerName;
    }

    public void setMakerName(String makerName) {
        this.makerName = makerName;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public void changeNewFlag() {
        // RoleToolRow toolRow = getToolById(toolId);
        // if (toolRow == null) {
        // return;
        // }
        // toolRow.setNewFlag((byte)0);
        Map<Long, RoleToolRow> map = this.itemBag.getToolMap();
        Collection<RoleToolRow> collection = map.values();
        for (RoleToolRow roleToolRow : collection) {
            roleToolRow.setNewFlag((byte) 0);
        }
    }

    public void sort() {
        handlers.get(ToolManager.ITEM_BAG).sort();
        handlers.get(ToolManager.EQUIP_BAG).sort();
        flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
    }

    public void dispathEvent(Event e) {
        eventDispatcher().fire(e);
    }

    public void buyItem(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo.getBuyPrice() == null) {
            warn("此物品不能购买");
            return;
        }
        this.deleteByItemId(itemVo.getBuyPrice()[0], count * (itemVo.getBuyPrice()[1]), EventType.BUYTOOL.getCode());
        this.addAndSend(itemId, count, EventType.BUYTOOL.getCode());
    }

    /**
     * 分解物品(装备&物品)
     */
    public void resolveTool(long toolId, int count) {
        RoleToolRow toolRow = getToolById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("不存在toolId={}的道具数据");
            return;
        }
        int itemId = toolRow.getItemId();
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        // 检查道具是否有足够数量
        if (!containsTool(toolId, count)) {
            com.stars.util.LogUtil.info("背包道具数量不足roleId={}, itemId={}, toolId={}", id(), toolRow.getItemId(), toolId);
            return;
        }
        if (StringUtil.isEmpty(itemVo.getResolveMap())) {
            warn("物品无法分解");
            return;
        }
        forceResolveTool(toolId, itemId, count);
    }

    /**
     * 强制分解; 此接口不做条件检测，使用前必须保证物品存在并且数量充足
     *
     * @param toolId 如果非0的话就会删除物品，否则不删除,但是还会继续走分解流程;
     *               ps:神兵那里有个分解需求会用到分解,是在item的func中的,所以提了这个接口出来;
     * @param itemId
     * @param count
     */
    public void forceResolveTool(long toolId, int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return;
        }
        // 整合分解物品
        Map<Integer, Integer> resolveToolMap = new HashMap<>();
        Map<Integer, Integer> map = new HashMap<>();
        if (itemVo.getResolveMap() != null) {
            for (Map.Entry<Integer, Integer> entry : itemVo.getResolveMap().entrySet()) {
                resolveToolMap.put(entry.getKey(), entry.getValue() * count);
            }
        }

        if (NewEquipmentManager.isTokenEquipment(itemId)) //符文装备不参与分解
            return;

        if (!this.canAdd(resolveToolMap)) {
            warn("背包空间不足,无法分解");
            return;
        }
        // 先分解再增加物品
        try {
            if (toolId != 0) {
                deleteByToolId(toolId, count, EventType.RESOLVETOOL.getCode());
                com.stars.util.LogUtil.info("分解道具, roleId={}, toolId={}, itemId={}, count={}", id(), toolId, itemId, count);
            }
            map = this.addAndSend(resolveToolMap, EventType.RESOLVETOOL.getCode());

        } catch (Throwable cause) {
            com.stars.util.LogUtil.error("道具分解异常, roleId=" + id() + ", itemId=" + itemId + ", count=" + count, cause);
        }

        // 分解结果展示界面
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_RESOLVE_EQUIP_RESULT);
        client.setResolveMap(map);
        send(client);
    }

    /**
     * 一键分解对应品质的装备(一键分解暂时只对装备有效)
     */
    public void resolveEquipByOneKey(Byte resolveQuality) {
        if (StringUtil.isEmpty(equipBag) || resolveQuality == 0) {
            warn("没有可分解的装备");
            return;
        }
        // 待分解装备列表
        List<RoleToolRow> deleteList = new ArrayList<>();
        Map<Integer, Integer> resolveMap = new HashMap<>();
        ItemVo itemVo;
        NewEquipmentModule equip = module(MConst.NewEquipment);
        for (RoleToolRow toolRow : equipBag.getToolMap().values()) {
            if (toolRow == null)
                continue;
            itemVo = ToolManager.getItemVo(toolRow.getItemId());
            if (itemVo == null || StringUtil.isEmpty(itemVo.getResolveMap()) || resolveQuality < itemVo.getColor())
                continue;
            byte mark = equip.calEquipMark(toolRow, MConst.Tool);
            if (mark != ClientNewEquipment.MARK_TYPE_HIGHQUALITY && mark != ClientNewEquipment.MARK_TYPE_WASH
                    && mark != ClientNewEquipment.MARK_TYPE_NONE)
                continue;
            if (NewEquipmentManager.isTokenEquipment(toolRow.getItemId())) //符文装备不参与一键分解
                continue;
            deleteList.add(toolRow);
            com.stars.util.MapUtil.add(resolveMap, itemVo.getResolveMap());
        }
        if (StringUtil.isEmpty(deleteList)) {
            warn("没有可分解的装备");
            return;
        }
        HashMap<Integer, Integer> logDeleteMap = new HashMap<Integer, Integer>();
        int logToolCount = 0;
        for (RoleToolRow toolRow : deleteList) {
            logToolCount = toolRow.getCount();
            equipBag.delete(toolRow.getToolId(), toolRow.getCount());// 删除装备
            Integer tmpNum = logDeleteMap.get(toolRow.getItemId());
            if (null != tmpNum) {
                logDeleteMap.put(toolRow.getItemId(), (logToolCount + tmpNum));
            } else {
                logDeleteMap.put(toolRow.getItemId(), logToolCount);
            }
        }
        ServerLogModule log = (ServerLogModule) module(MConst.ServerLog);
        log.Log_core_item(null, logDeleteMap, EventType.RESOLVETOOL.getCode());
        Map<Integer, Integer> map = addAndSend(resolveMap, EventType.RESOLVETOOL.getCode());// 增加物品并刷新客户端,背包不足直接发邮件

        // 分解结果展示界面
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_RESOLVE_EQUIP_RESULT);
        client.setResolveMap(map);
        send(client);
    }

    /**
     * 更新道具信息
     */
    public void updateToolRow(RoleToolRow toolRow) {
        ToolHandler handler = getHandlerByItemId(toolRow.getItemId());
        if (handler == null) {
            com.stars.util.LogUtil.info("增加道具handler异常,玩家id:" + id() + ",配置id:" + toolRow.getItemId());
            return;
        }
        handler.updateToolRow(toolRow);
    }

    /**
     * 根据itemid获得第一个match的装备的toolid 特殊接口，仅为了实现装备的穿戴提示,所以并没有做成通用，尽量不要用
     */
    public long getRoleEquipToolByItemId(int itemId) {
        if (equipBag != null && StringUtil.isNotEmpty(equipBag.getToolMap())) {
            for (RoleToolRow toolRow : equipBag.getToolMap().values()) {
                if (toolRow.getItemId() == itemId) {
                    return toolRow.getToolId();
                }
            }
        }
        return 0;
    }

    /**
     * 背包内是否存在更好的额外属性
     */
    public boolean hasBetterExtAttrInBag(RoleEquipment roleEquipment) {
        RoleModule roleModule = module(MConst.Role);
        Byte maxNum = Byte.parseByte(DataManager.getCommConfig("equip_extattrnum_max"));
        int size = roleEquipment.getExtraAttrMap() == null ? 0 : roleEquipment.getExtraAttrMap().size();
        boolean hasEmpty = maxNum > size;
        int minFighting = roleEquipment.getMinExtAttrFighting();
        if (equipBag != null && StringUtil.isNotEmpty(equipBag.getToolMap())) {
            for (RoleToolRow toolRow : equipBag.getToolMap().values()) {
                if (toolRow.getEquipType() != roleEquipment.getType())
                    continue;
                if (StringUtil.isEmpty(toolRow.getExtraAttrMap()))
                    continue;// 没有额外属性
                EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
                if (equipmentVo != null && roleModule.getLevel() < equipmentVo.getEquipLevel())
                    continue;
                if (NewEquipmentManager.isTokenEquipment(equipmentVo.getEquipId())
                        && (StringUtil.isNotEmpty(toolRow.getRoleTokenHoleInfoMap()) || toolRow.getTokenSkillId() != 0)) //符文装备而且有符文或符文技能的不能洗练
                    continue;
                if (roleEquipment.getBasicFighting() < toolRow.getBasicFighting())
                    continue;// 可穿戴的不能洗练
                if (hasEmpty)
                    return true;// 有空位

                // 存在更高战力的额外属性
                if (toolRow.getMaxExtraAttrFighting() > minFighting)
                    return true;
            }
        }
        return false;
    }

    /**
     * 背包内是否存在可转移的装备
     */
    public boolean hasCanTransferEquipInBag(RoleEquipment roleEquipment) {
        if (equipBag != null && StringUtil.isNotEmpty(equipBag.getToolMap())) {
            for (RoleToolRow toolRow : equipBag.getToolMap().values()) {
                if (toolRow.getEquipType() != roleEquipment.getType())
                    continue;
                if (toolRow.getBasicFighting() <= roleEquipment.getBasicFighting())
                    continue;// 基础战力低于身上装备
                if (toolRow.getExtraAttrFighting() < roleEquipment.getExtraAttrFighting())
                    return true;
            }
        }
        return false;
    }

    /**
     * 背包内是否存在更好的装备
     */
    public boolean hasBetterEquipInBag(RoleEquipment roleEquipment) {
        RoleModule roleModule = module(MConst.Role);
        if (equipBag != null && StringUtil.isNotEmpty(equipBag.getToolMap())) {
            for (RoleToolRow toolRow : equipBag.getToolMap().values()) {
                EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
                if (equipmentVo != null && roleModule.getLevel() < equipmentVo.getEquipLevel())
                    continue;
                if (toolRow.getEquipType() != roleEquipment.getType())
                    continue;
                if (toolRow.getBasicFighting() > roleEquipment.getBasicFighting())
                    return true;
            }
        }
        return false;
    }

    public HashSet<RoleToolRow> getNeedToMarkSet() {
        if (equipBag == null || equipBag.getNeedToMark() == null)
            return null;
        return equipBag.getNeedToMark();
    }

    public void clearNeedToMark() {
        if (equipBag == null || equipBag.getNeedToMark() == null)
            return;
        equipBag.getNeedToMark().clear();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.BAG_USE_BOX)) {
            checkUseBoxRedPoint(redPointMap);// 背包宝箱红点检测
        }
    }

    /**
     * 背包宝箱红点检测
     */
    private void checkUseBoxRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ItemVo itemVo;
        ToolFunc toolFunc;
        for (RoleToolRow toolRow : itemBag.getToolMap().values()) {
            itemVo = ToolManager.getItemVo(toolRow.getItemId());
            if (itemVo.getType() != ToolManager.TYPE_BOX)
                continue;
            toolFunc = itemVo.getToolFunc();
            if (toolFunc == null)
                continue;
            ToolFuncResult result = toolFunc.check(moduleMap(), 1, toolRow.getBornTime());
            if (result.isSuccess()) {
                builder.append(toolRow.getItemId()).append("+");
            }
        }
        redPointMap.put(RedPointConst.BAG_USE_BOX, builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 能否合成
     *
     * @param itemId
     * @param count
     * @return
     */
    private boolean canCompose(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null || itemVo.getCompoundNeeds().isEmpty()) {
            return false;
        }
        Map<Integer, Integer> cost = new HashMap<>();
        cost.putAll(itemVo.getCompoundNeeds());
        com.stars.util.MapUtil.multiply(cost, count);
        if (!contains(cost)) {
            warn(I18n.get("tool.compound.costNotEngough"));
            return false;
        }
        return true;
    }

    /**
     * 执行合成 先判断,再删除,再增加
     *
     * @param itemId
     * @param count
     * @return
     */
    public void executeCompose(int itemId, int count) {
        if (!canCompose(itemId, count)) {
            return;
        }
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Map<Integer, Integer> cost = new HashMap<>();
        cost.putAll(itemVo.getCompoundNeeds());
        MapUtil.multiply(cost, count);
        deleteAndSend(cost, EventType.COMPOSETOOL.getCode());
        addAndSend(itemId, count, EventType.COMPOSETOOL.getCode());
    }

    public void gmDeleteItem(long toolId, int count) {
        if (count <= 0)
            return;
        RoleToolRow toolRow = getToolById(toolId);
        if (null != toolRow) {
            count = toolRow.getCount() < count ? toolRow.getCount() : count;
            deleteByToolId(toolId, count, EventType.GM_DEL.getCode());
            flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
        }
    }

    public boolean gmDeleteItem(int itemId, int count) {
        if (count <= 0)
            return false;
        if (checkBeforeDelete(itemId, count)) {
            deleteByItemId(itemId, count, EventType.SUBTOOL.getCode());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 修改装备的jobid，OnDataReq方法会在登陆第一次执行，转职后重新进入将不再执行。。。
     *
     * @param newJobId
     */
    public void onChangeJob(Integer newJobId) {
        for (RoleToolRow roleToolRow : equipBag.getToolMap().values()) {
            roleToolRow.setJobId(newJobId);
        }
        Map<Long, RoleToolRow> equipToolMap = getEquipToolMap();
        for (Map.Entry<Long, RoleToolRow> entry : equipToolMap.entrySet()) {
            RoleToolRow roleToolRow = entry.getValue();
            int itemId = roleToolRow.getItemId();
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo.getType() == ToolManager.TYPE_EQUIPMENT) {
                EquipmentVo newJobEquipmentVo = NewEquipmentManager.getNewJobEquipmentVo(newJobId, itemId);
                if (newJobEquipmentVo != null) {
                    roleToolRow.setItemId(newJobEquipmentVo.getEquipId());
                    context().update(roleToolRow);
                }
            }
        }
/**
 * 转换神兵
 */
        for (Map.Entry<Long, RoleToolRow> entry : itemBag.getToolMap().entrySet()) {
            RoleToolRow roleToolRow = entry.getValue();
            DeityWeaponVo deityWeaponVo = DeityWeaponManager.getDeityWeaponVoByItemId(roleToolRow.getItemId());
            if (deityWeaponVo != null) {
                DeityWeaponVo newDeityWeaponVo = DeityWeaponManager.getDeityWeaponVo(newJobId, deityWeaponVo.getType());
                roleToolRow.setItemId(newDeityWeaponVo.getItemId());
                context().update(roleToolRow);
            }
        }
    }


    /**
     * 加载玩家物品使用限制数据
     */
    private void initUsedLimitData(){
        String dailyUsedStr = getString(DAILY_USED_TIME_LIMIT, "");
        String weeklyUsedStr = getString(WEEKLY_USED_TIME_LIMIT, "");
        String foreverUsedStr = getString(FOREVER_USED_TIME_LIMIT, "");
        dailyUsedLimitMap = StringUtil.toMap(dailyUsedStr,Integer.class,Integer.class,'=',',');
        weeklyUsedLimitMap = StringUtil.toMap(weeklyUsedStr,Integer.class,Integer.class,'=',',');
        foreverUsedLimitMap = StringUtil.toMap(foreverUsedStr,Integer.class,Integer.class,'=',',');
    }

    private void saveUsedLimitData(int itemId, int count){
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Map<Byte, String> useCondition = itemVo.getUseCondition();
        if (useCondition == null || useCondition.size() <= 0) {
            return;
        }
        Integer usedTimes = null;
        int newUsedTimes = 0;
        for(Map.Entry<Byte,String> entry : useCondition.entrySet()){
            switch(entry.getKey()){
                case 6:
                    newUsedTimes = count;
                    usedTimes = dailyUsedLimitMap.get(itemId);
                    if(usedTimes != null){
                        newUsedTimes = usedTimes + count;
                    }
                    dailyUsedLimitMap.put(itemId,newUsedTimes);
                    String dailyUsedStr = StringUtil.makeString(dailyUsedLimitMap,'=',',');
                    setString(DAILY_USED_TIME_LIMIT, dailyUsedStr);
                    break;
                case 7:
                    newUsedTimes = count;
                    usedTimes = weeklyUsedLimitMap.get(itemId);
                    if(usedTimes != null){
                        newUsedTimes = usedTimes + count;
                    }
                    weeklyUsedLimitMap.put(itemId,newUsedTimes);
                    String weeklyUsedStr = StringUtil.makeString(weeklyUsedLimitMap,'=',',');
                    setString(WEEKLY_USED_TIME_LIMIT,weeklyUsedStr);
                    break;
                case 8:
                    newUsedTimes = count;
                    usedTimes = foreverUsedLimitMap.get(itemId);
                    if(usedTimes != null){
                        newUsedTimes = usedTimes + count;
                    }
                    foreverUsedLimitMap.put(itemId,newUsedTimes);
                    String foreverUsedStr = StringUtil.makeString(foreverUsedLimitMap,'=',',');
                    setString(FOREVER_USED_TIME_LIMIT,foreverUsedStr);
                    break;
                default:
                    break;
            }
        }

    }

    private int getRemainUseCount(int itemId,int count){
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Map<Byte, String> useCondition = itemVo.getUseCondition();
        if (useCondition == null || useCondition.size() <= 0) { //没有限制可使用次数不变
            return count;
        }

        int remainCount = count; //剩余可使用次数
        int tempCount = count;
        Integer hasUsedRecord;
        for(Map.Entry<Byte,String> entry : useCondition.entrySet()){
            switch(entry.getKey()){
                case 6:
                    int dailyUsedLimit = Integer.parseInt(entry.getValue());
                    hasUsedRecord = dailyUsedLimitMap.get(itemId);
                    if(hasUsedRecord == null){
                        hasUsedRecord = 0;
                    }
                    tempCount = dailyUsedLimit - hasUsedRecord;
                    break;
                case 7:
                    int weeklyUsedLimit = Integer.parseInt(entry.getValue());
                    hasUsedRecord = weeklyUsedLimitMap.get(itemId);
                    if(hasUsedRecord == null){
                        hasUsedRecord = 0;
                    }
                    tempCount = weeklyUsedLimit - hasUsedRecord;
                    break;
                case 8:
                    int foreverUsedLimit = Integer.parseInt(entry.getValue());
                    hasUsedRecord = foreverUsedLimitMap.get(itemId);
                    if(hasUsedRecord == null){
                        hasUsedRecord = 0;
                    }
                    tempCount = foreverUsedLimit - hasUsedRecord;
                    break;
                default:
                    break;
            }
            remainCount = tempCount < remainCount ? tempCount : remainCount; //取最小值
        }
        return remainCount;
    }

    private boolean hasUsedLimit(int itemId){
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Map<Byte, String> useConditionMap = itemVo.getUseCondition();
        if (useConditionMap == null || useConditionMap.size() <= 0) {
            return false;
        }
        if(useConditionMap.containsKey((byte)6) || useConditionMap.containsKey((byte)7) || useConditionMap.containsKey((byte)8)){
            return true;
        }
        return false;
    }

}
