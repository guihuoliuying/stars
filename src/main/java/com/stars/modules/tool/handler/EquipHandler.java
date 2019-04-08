package com.stars.modules.tool.handler;

import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.modules.tool.userdata.RoleTokenEquipmentHolePo;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.modules.tool.userdata.RoleToolTable;
import com.stars.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/8.
 */
public class EquipHandler implements ToolHandler {

    private ToolModule toolModule;
    private RoleToolTable toolTable;//增加道具的handler会关联道具背包
    private NewEquipmentModule equipModule;//装备关联装备模块

    public EquipHandler() {
    }

    public EquipHandler(NewEquipmentModule equipModule,ToolModule toolModule, RoleToolTable toolTable) {
        this.toolModule = toolModule;
        this.toolTable = toolTable;
        this.equipModule = equipModule;
    }

    @Override
    public Map<Integer,Integer> add(int itemId, int count,short eventType) {
//        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if(count <= 0) return null;
        Map<Integer,Integer> resultMap = new HashMap<>();
        resultMap.put(itemId,count);
        int restCount = count;
        while (restCount > 0) {
            int nullGrid = getNullGrid();
            if (nullGrid <= 0) {//背包已满,没有空闲的格子添加装备
                toolModule.warn("item_bagfull");
                return resultMap;
            }
            //正常添加道具
            restCount--;
            long toolId = toolModule.getNextToolId();
            RoleToolRow newTool = new RoleToolRow(toolTable, toolModule.id(), toolId, itemId, 1);
            //equipModule.initNewAddEquipmentTool(newTool); 策划需求修改，不需要给符文装备初始化符文和技能了
            toolModule.initEquipBasicAttr(newTool);//初始化装备基础属性
            //初始化装备额外属性
            equipModule.initEquipExtAttr(newTool);
            toolModule.initBasicInfo(newTool);

            toolTable.putRow(newTool);
            toolTable.getFlushToClient().add(newTool);
            toolModule.context().insert(newTool);

            toolTable.getNeedToMark().add(newTool);//加入待标识列表
        }
        return resultMap;
    }

    public void addEquipWithExtAttr(int itemId,Map<Byte, ExtraAttrVo> attrMap,
                                    Map<Byte, RoleTokenEquipmentHolePo> roleTokenEquipmentHolePoMap,
                                    int tokenSkillId,int tokenSkillLevel){
        int nullGrid = getNullGrid();
        if (nullGrid <= 0) {//背包已满,没有空闲的格子添加装备
            return;
        }

        long toolId = toolModule.getNextToolId();
        RoleToolRow newTool = new RoleToolRow(toolTable, toolModule.id(), toolId, itemId, 1);
        toolModule.initEquipBasicAttr(newTool);//初始化装备基础属性
        newTool.setExtraAttrMap(attrMap);
        newTool.setRoleTokenHoleInfoMap(roleTokenEquipmentHolePoMap);
        newTool.setTokenSkillId(tokenSkillId);
        newTool.setTokenSKillLevel(tokenSkillLevel);
        toolModule.initBasicInfo(newTool);
        toolTable.putRow(newTool);
        toolTable.getFlushToClient().add(newTool);
        toolModule.context().insert(newTool);

        toolTable.getNeedToMark().add(newTool);//加入待标识列表
        return;
    }

    /**
     * 根据itemid返回一个第一个删除的装备对象
     * 当前无规则,返回第一个匹配的装备对象
     */
    private RoleToolRow getFirstDeleteTool(int itemId) {
        long toolId = 0;
        Collection<RoleToolRow> c = toolTable.getToolMap().values();
        for (RoleToolRow toolRow:c) {
            if (toolRow == null) {
                continue;//空对象不取
            }
            if (toolRow.getItemId() != itemId) {
                continue;//id不对不取
            }
            return getByToolId(toolId);
        }
        return getByToolId(toolId);
    }

    /**
     * 根据itemid返回一个道具对象
     * 返回数量少的(删除东西从少的开始删)
     */
    @Override
    public boolean deleteByItemId(int itemId, int count,short eventType) {
        RoleToolRow toolRow;
        int subCount;//一次循环扣除的数量
        while (count > 0) {
            toolRow = getFirstDeleteTool(itemId);//因为经过了判断,这里不会为空.加个日志记录一下
            if (toolRow == null) {
                return false;
            }
            if (toolRow.getCount() > count) { //这一组拥有的数量比扣除数量多,直接改数量
                subCount = count;
                toolRow.setCount(toolRow.getCount() - subCount);
                toolRow.setNewFlag((byte) 0);
                toolModule.context().update(toolRow);
                count = 0;
                this.toolTable.getFlushToClient().add(toolRow);
            } else { //这一组拥有的数量比扣除数量少,把这一组全扣掉,继续下一组
                subCount = toolRow.getCount();
                toolRow.setCount(0);
                toolTable.delete(toolRow.getToolId(), toolRow.getCount());
                count = count - subCount;
            }
        }
        return true;
    }

    @Override
    public boolean deleteByToolId(long toolId, int count) {
        RoleToolRow toolRow = getByToolId(toolId);
        //操作单个格子时,这个格子内的数量必须比减的数量大
        if(toolRow == null || toolRow.getCount() < count){
            return false;
        }
        int restCount = toolRow.getCount() - count;
        toolRow.setNewFlag((byte) 0);
        toolModule.context().update(toolRow);
        if(restCount > 0){//还有数量,更改数量即可
            toolRow.setCount(restCount);
            toolTable.getFlushToClient().add(toolRow);
        }else{//扣完了
            toolRow.setCount(0);
            toolTable.delete(toolRow.getToolId(),toolRow.getCount());
        }
        return true;
    }

    @Override
    public void add(RoleToolRow toolRow) {
        toolRow.setTable(toolTable);
        toolTable.putRow(toolRow);
    }

    @Override
    public int getNullGrid() {
        return Math.max(0, ToolManager.EQUIP_MAX_GRID - toolTable.getToolMap().size());
    }

    @Override
    public long getCountByItemId(int itemId) {
        long count = 0;
        for (RoleToolRow toolRow : toolTable.getToolMap().values()) {
            if (toolRow.getItemId() == itemId) {
                count = count + toolRow.getCount();
            }
        }
        return count;
    }

    /**
     * 判断能加多少个道具,返回实际可以新增的数量
     * 现在的需求是:每个装备占一个格子
     */
    @Override
    public int canAdd(int itemId, int count) {
        int nullGrid = getNullGrid();
        return nullGrid >= count ? count : nullGrid;
    }

    /**
     * 判断是否能添加整个toolMap
     */
    @Override
    public boolean canAdd(Map<Integer, Integer> toolMap) {
        if(StringUtil.isEmpty(toolMap)) return true;
        int totalCount = 0;
        for(Integer count:toolMap.values()){
            totalCount += count;
        }
        return getNullGrid() >= totalCount;
    }

    @Override
    public void sort() {
        
    }

    /**
     * 根据id返回道具对象
     */
    public RoleToolRow getByToolId(long toolId) {
        return toolTable.getToolMap().get(toolId);
    }

    /**
     * 更新道具信息
     */
    @Override
    public void updateToolRow(RoleToolRow toolRow){
        toolModule.context().update(toolRow);
        toolTable.getFlushToClient().add(toolRow);
    }
}
