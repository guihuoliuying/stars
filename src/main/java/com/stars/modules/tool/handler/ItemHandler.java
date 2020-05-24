package com.stars.modules.tool.handler;

import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.modules.tool.userdata.RoleToolTable;
import com.stars.util.EmptyUtil;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/24.
 */
public class ItemHandler implements ToolHandler {

    private ToolModule toolModule;
    //增加道具的handler会关联道具背包
    private RoleToolTable toolTable;

    public ItemHandler(ToolModule toolModule, RoleToolTable toolTable) {
        this.toolModule = toolModule;
        this.toolTable = toolTable;
    }

    private void handleMakeName(RoleToolRow toolRow) {
        if (EmptyUtil.isNotEmpty(toolModule.getMakerName())) {
            toolRow.setMaker(toolModule.getMakerName());
        }
    }

    /**
     * 更新道具信息
     */
    @Override
    public void updateToolRow(RoleToolRow toolRow){
        toolModule.context().update(toolRow);
        toolTable.getFlushToClient().add(toolRow);
    }

    /**
     * 添加道具比添加装备要麻烦,具体不走
     * 1,检查是否已有这个道具
     * 2,如果有道具,检查加上数量是否会堆满,未堆满则修改数量,会堆满则新建
     * 3,如果没有这个道具,直接新建
     * 流程长,方法也长,
     */
    @Override
    public Map<Integer,Integer> add(int itemId, int count) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Map<Integer,Integer> resultMap = new HashMap<>();
        //如果是自动使用,只走自动使用的逻辑
        if(itemVo.isAutoUse()){
            ToolFunc toolFunc = itemVo.getToolFunc();
            int autoUse = 0;
            if (toolFunc != null) autoUse = toolFunc.canAutoUse(toolModule.moduleMap() ,count);
            Map<Integer,Integer> tmpMap = toolModule.useToolByItemIdNotCheck(itemId, autoUse);
            if(StringUtil.isNotEmpty(tmpMap)){
                MapUtil.add(resultMap, tmpMap);
            }else{
                resultMap.put(itemId,autoUse);
            }
            count = count - autoUse;
            if(count > 0){
                if(resultMap.containsKey(itemId)){
                    resultMap.put(itemId,resultMap.get(itemId) + count);
                }else{
                    resultMap.put(itemId,count);
                }
            }
            {

            }
        }else{
            resultMap.put(itemId,count);
        }
        int storage = itemVo.getStorage();
        int restCount = count;
        RoleToolRow toolRow;
        while (restCount > 0) {
            toolRow = getNotFullTool(itemId);
            if (toolRow != null) {
                //有未满的道具,先填满
                int oldCount = toolRow.getCount();
                if (oldCount + restCount > storage) {
                    toolRow.setCount(storage);
                    restCount = restCount - (storage - oldCount);
                } else {
                    toolRow.setCount(oldCount + restCount);
                    restCount = 0;
                }
//                toolRow.setUpdateStatus();
                toolModule.context().update(toolRow);
                toolRow.setNewFlag((byte) 1);
                this.toolTable.getFlushToClient().add(toolRow);
            } else {
                //没有同类道具,直接添加新的
                int nullGrid = getNullGrid();
                if (nullGrid <= 0) {
                    //背包满了
                    com.stars.util.LogUtil.info("背包已满,剩余的道具丢弃:" + toolModule.id() + "|" + itemId + "|" + restCount);
                    return resultMap;
                }
                //正常添加
                long toolId = toolModule.getNextToolId();
                int newCount = restCount;
                if (restCount > storage) {
                    newCount = storage;//比上限多,这堆物品只能加到上限
                    restCount = restCount - storage;
                } else {
                    restCount = 0;
                }
                RoleToolRow newTool = new RoleToolRow(toolTable, toolModule.id(),toolId, itemId, newCount);
                handleMakeName(newTool);
                toolTable.putRow(newTool);
                this.toolTable.getFlushToClient().add(newTool);
                toolModule.context().insert(newTool);
            }
        }
        return resultMap;
    }

    /**
     * 删除道具,这个道具是配置id
     * 能跑到这里肯定是数量充足可以删除
     */
    @Override
    public boolean deleteByItemId(int itemId, int count) {
        RoleToolRow toolRow;
        int subCount;//一次循环扣除的数量
        while (count > 0) {
            toolRow = getLessTool(itemId);//因为经过了判断,这里不会为空.加个日志记录一下
            if (toolRow == null) {
                LogUtil.info("未删完道具对象却为空,info:" + toolModule.id() + "|" + itemId + "|" + count);
                return false;
            }
            if (toolRow.getCount() > count) { //这一组拥有的数量比扣除数量多,直接改数量
                subCount = count;
                toolRow.setCount(toolRow.getCount() - subCount);
                toolRow.setNewFlag((byte) 0);
//                toolRow.setUpdateStatus();
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
        if(toolRow == null || toolRow.getCount() < count){
            //操作单个格子时,这个格子内的数量必须比减的数量大
            return false;
        }
        int restCount = toolRow.getCount() - count;
        toolRow.setNewFlag((byte) 0);
//        toolRow.setUpdateStatus();
        toolModule.context().update(toolRow);
        if(restCount > 0){
            //还有数量,更改数量即可
            toolRow.setCount(restCount);
            toolTable.getFlushToClient().add(toolRow);
        }else{
            //扣完了
            toolRow.setCount(0);
            toolTable.delete(toolRow.getToolId(),toolRow.getCount());
        }
        return true;
    }



    /**
     * 根据配置id拿到道具对象,只拿没放满的,并且返回数量最多的
     * 如果全放满了,或者根本没有,则返回空
     * ps.增加东西会从多的开始堆
     */
    public RoleToolRow getNotFullTool(int itemId) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        int maxCount = 0;
        long toolId = 0;
        Collection<RoleToolRow> c = toolTable.getToolMap().values();
        for (RoleToolRow toolRow:c) {
            if (toolRow.getItemId() != itemVo.getItemId() || toolRow.getCount() >= itemVo.getStorage()) {
                continue;//id不对或者堆满的不取
            }
            if (toolRow.getCount() > maxCount) {
                maxCount = toolRow.getCount();
                toolId = toolRow.getToolId();
            }
        }
        return getByToolId(toolId);
    }

    /**
     * 根据itemid返回一个道具对象
     * 返回数量少的(删除东西从少的开始删)
     */
    public RoleToolRow getLessTool(int itemId) {
        int minCount = 0;
        long toolId = 0;
        Collection<RoleToolRow> c = toolTable.getToolMap().values();
        for (RoleToolRow toolRow:c) {
            if (toolRow == null) {
                continue;//空对象不取
            }
            if (toolRow.getItemId() != itemId) {
                continue;//id不对不取
            }
            if (minCount == 0 || toolRow.getCount() <= minCount) {
                minCount = toolRow.getCount();
                toolId = toolRow.getToolId();
            }
        }
        return getByToolId(toolId);
    }


    /**
     * handler增加物品对象的入口,在这里就无需判断了
     */
    @Override
    public void add(RoleToolRow toolRow) {
    	toolRow.setTable(toolTable);
        toolTable.putRow(toolRow);
//        toolTable.getFlushToClient().add(toolRow);
    }

    @Override
    public int getNullGrid() {
//        Map<Integer, Long> gridMap = toolTable.getGridMap();
//        for (int index = 1; index <= ToolManager.ITEM_MAX_GRID; index++) {
//            if (gridMap.get(index) == null) {
//                return index;
//            }
//        }
//        return -1;
    	return Math.max(0, ToolManager.ITEM_MAX_GRID - toolTable.getToolMap().size());
    }

    /**
     * 根据id返回道具对象
     */
    public RoleToolRow getByToolId(long toolId) {
        return toolTable.getToolMap().get(toolId);
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
     * 根据id返回背包中同类道具还能添加的数量
     */
    private int getCanAddNumByItemId(int itemId) {
        int restCount = 0;
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        Collection<RoleToolRow> c = toolTable.getToolMap().values();
        for (RoleToolRow toolRow:c) {
            if (toolRow.getItemId() != itemId) {
                continue;
            }
            if (toolRow.getCount() >= itemVo.getStorage()) {
                continue;
            }
            restCount = restCount + (itemVo.getStorage() - toolRow.getCount());
        }
        return restCount;
    }

    /**
     * 根据背包中剩余的空格子数,返回剩余可增加的数量
     */
    private int getCanAddNumByNullGrid(int itemId) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        int res = getNullGrid();
        return res * itemVo.getStorage();
    }

    /**
     * 判断能加多少个道具,返回实际可以新增的数量
     */
    @Override
    public int canAdd(int itemId, int count) {
        //1,如果是自动使用的道具,直接返回实际数量(当做全都自动使用)
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        int autoUse = 0;
        if(itemVo.isAutoUse()){
            ToolFunc toolFunc = itemVo.getToolFunc();
            if (toolFunc == null) return count;
            autoUse = toolFunc.canAutoUse(toolModule.moduleMap(), count);
            count = count - autoUse;
        }
        //2,根据itemId,返回背包中的同类道具还能增加多少个,
        int restAddCount = getCanAddNumByItemId(itemId);
        if (restAddCount >= count) {//能在已有道具叠满就不管了
            return count + autoUse;
        }
        //3,在已有道具中叠满后再看能不能塞进剩余的空格子
        int rest = count - restAddCount;
        int restNullGridCount = getCanAddNumByNullGrid(itemId);
        if (restNullGridCount >= rest) { //空格子可装的数量比剩余的还多,则全放进去
            return count + autoUse;
        }
        return restNullGridCount + restAddCount + autoUse;
    }
    
    /**
     * 道具排序分步骤
     * 1:尽量把道具往前叠满,为0的跳过
     * 2,把道具列表中数量为0的去掉
     * 3,根据规则排序
     */
    @Override
    public void sort() {
        fillBeforeSort();
        subEmptyItem();
//        sortItem();
    }

    /**
     * 排序前先填充满所有道具
     */
    private void fillBeforeSort() {
        ItemVo itemVo;
        RoleToolRow toolRow, nextToolRow;
        Object[] tools = toolTable.getToolMap().values().toArray();
        int size = tools.length;
        for (int index = 0; index < size; index++) {
            toolRow = (RoleToolRow)tools[index];
            if (toolRow == null) {
                continue;
            }
            itemVo = ToolManager.getItemVo(toolRow.getItemId());
            if (toolRow.getCount() >= itemVo.getStorage() || toolRow.getCount() <= 0) {
                continue;//叠满或被扣过的不管
            }
            for (int nextIndex = index + 1; nextIndex < size; nextIndex++) {
                nextToolRow = (RoleToolRow)tools[nextIndex];
                //开始遍历这个道具之后的所有道具,只拿有数量并且道具id相同的
                if (nextToolRow == null || nextToolRow.getItemId() != toolRow.getItemId()) {
                    continue;
                }
                int nextToolCount = nextToolRow.getCount();
                if (nextToolCount <= 0) {
                    continue;
                }
                int restCount = itemVo.getStorage() - toolRow.getCount();
                if (nextToolCount > restCount) {
                    //下一个道具的数量比叠到满的还要大,把第一个叠满,第二个改数量即可
                    toolRow.setCount(itemVo.getStorage());
                    nextToolRow.setCount(nextToolCount - restCount);
                    toolModule.context().update(toolRow);
                    toolModule.context().update(nextToolRow);
                    toolTable.getFlushToClient().add(toolRow);
                    toolTable.getFlushToClient().add(nextToolRow);
                    break;
                } else {
                    //下一个道具的数量全加上都不够叠满,那就加到第一个道具上,扣掉第二个道具
                    toolRow.setCount(toolRow.getCount() + nextToolCount);
                    toolModule.context().update(toolRow);
                    nextToolRow.setCount(0);
                }
                toolTable.getFlushToClient().add(toolRow);
                toolTable.getFlushToClient().add(nextToolRow);
            }
        }
    }

    /**
     * 把道具中数量为0的道具去掉
     */
    private void subEmptyItem() {
        Collection<RoleToolRow> coll = toolTable.getToolMap().values();
        Map<Long, Integer> del = new HashMap<>();
        for (RoleToolRow toolRow:coll) {
            if (toolRow.getCount() > 0) {
                continue;
            }
            del.put(toolRow.getToolId(), toolRow.getCount());
        }
        for (Map.Entry<Long, Integer> entry : del.entrySet()) {
            toolTable.delete(entry.getKey(), entry.getValue());
        }
    }

//    /**
//     * 最后根据id规则和数量排序
//     */
//    private void sortItem() {
//        List<RoleToolRow> toolList = new ArrayList<>();
//        toolList.addAll(toolTable.getToolMap().values());
//        Collections.sort(toolList);
//        int index = 1;
//        for (RoleToolRow toolRow : toolList) {
//            toolRow.setGrid(index);
//            toolRow.setNewFlag((byte) 0);
//            index = index + 1;
//            toolRow.setChanged();
//        }
//        toolTable.settleGrid();
//    }
    
    

    private void print() {
    	Collection<RoleToolRow> c = toolTable.getToolMap().values();
        for (RoleToolRow toolRow:c) {
            System.err.println("道具信息: id:" + toolRow.getToolId() + ",itemid:" + toolRow.getItemId() + ",count:" + toolRow.getCount());
        }
        System.err.print("打印结束");
    }

    /**
     * 判断能否增加道具
     * 可以增加时返回需要增加的gridcount
     * 无法增加时返回-1
     * hasUsedGridCount需要使用的gridcount
     */
    private int canAdd(int itemId, int count,int hasUsedGridCount) {
        //1,如果是自动使用的道具,直接返回实际数量(当做全都自动使用)
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if(itemVo.isAutoUse()){
            ToolFunc toolFunc = itemVo.getToolFunc();
            if (toolFunc == null) return 0;//无需新增格子
            int autoUse = toolFunc.canAutoUse(toolModule.moduleMap(), count);
            count = count - autoUse;
        }
        //2,根据itemId,返回背包中的同类道具还能增加多少个,
        int restAddCount = getCanAddNumByItemId(itemId);
        if (restAddCount >= count) {//能在已有道具叠满就不管了
            return 0;//无需新增格子
        }
        //3,在已有道具中叠满后再看能不能塞进剩余的空格子
        int rest = count - restAddCount;//剩余需要添加的物品数量
        int resGridCount = getNullGrid() - hasUsedGridCount;//剩余格子数(实际剩余格子数 - 虚拟已使用的格子数)

        int restNullGridCount = resGridCount * itemVo.getStorage();
        if (restNullGridCount >= rest) { //空格子可装的数量比剩余的还多
            int usedGridCount;//需要新增的格子数量
            if(rest % itemVo.getStorage() == 0){
                usedGridCount = rest / itemVo.getStorage();
            }else{
                usedGridCount = rest / itemVo.getStorage() + 1;
            }
            return usedGridCount;
        }
        return -1;
    }

    @Override
    public boolean canAdd(Map<Integer, Integer> toolMap) {
        int hasUseGridCount = 0;//总体需要使用的gridCount
        int needUsedCount;//单一物品需要使用的gridCount
        for(Map.Entry<Integer,Integer> entry:toolMap.entrySet()){
            needUsedCount = canAdd(entry.getKey(),entry.getValue(),hasUseGridCount);
            if(needUsedCount == -1) return false;
            hasUseGridCount += needUsedCount;
        }
        return true;
    }

}
