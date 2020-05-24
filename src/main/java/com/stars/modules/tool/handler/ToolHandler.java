package com.stars.modules.tool.handler;

import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/24.
 */
public interface ToolHandler {

    Map<Integer,Integer> add(int itemId, int count);
    
    boolean deleteByToolId(long toolId, int count);

    void add(RoleToolRow toolRow);

    int getNullGrid();

    long getCountByItemId(int itemId);

    /**
     * 判断能否增加道具,返回实际可以增加的数量
     */
    int canAdd(int itemId, int count);

    /**
     * 判断能否增加道具组,必须
     */
    boolean canAdd(Map<Integer, Integer> toolMap);
    void sort();

    /**
     * 更新道具信息
     */
    void updateToolRow(RoleToolRow toolRow);

	boolean deleteByItemId(int itemId, int count);

}
