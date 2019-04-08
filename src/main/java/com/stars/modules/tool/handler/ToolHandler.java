package com.stars.modules.tool.handler;

import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/24.
 */
public interface ToolHandler {

    /**必须实现接口的增删方法
     * @param eventType */
    public Map<Integer,Integer> add(int itemId, int count, short eventType);
    
    public boolean deleteByToolId(long toolId, int count);

    public void add(RoleToolRow toolRow);

    public int getNullGrid();

    public long getCountByItemId(int itemId);

    /**
     * 判断能否增加道具,返回实际可以增加的数量
     */
    public int canAdd(int itemId, int count);

    /**
     * 判断能否增加道具组,必须
     */
    public boolean canAdd(Map<Integer, Integer> toolMap);
    public void sort();

    /**
     * 更新道具信息
     */
    public void updateToolRow(RoleToolRow toolRow);

	boolean deleteByItemId(int itemId, int count, short eventType);

}
