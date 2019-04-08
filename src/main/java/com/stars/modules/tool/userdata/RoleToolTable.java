package com.stars.modules.tool.userdata;

import com.stars.modules.tool.ToolModule;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class RoleToolTable{

    private ToolModule toolModule;
    private Map<Long, RoleToolRow> toolMap;//道具列表,key:物品id(全局唯一)  value:道具对象
    private int tableId;
    private HashSet<RoleToolRow> flushToClient;
    private byte bagType;

    private HashSet<RoleToolRow> needToMark;//装备待标识列表(新增装备需要计算角标:可穿戴、可洗练)

    /**
     * 只是给checkTable调用的检查方法,功能中不允许被调用
     */
    public RoleToolTable() {
    	flushToClient = new HashSet<RoleToolRow>();
    }

    public RoleToolTable(ToolModule toolModule, int tableId, byte bagType) {
        this.toolModule = toolModule;
        toolMap = new HashMap<>();
        this.bagType = bagType;
        this.setTableId(tableId);
        flushToClient = new HashSet<RoleToolRow>();
    }


//    @Override
//    public String getChangeSql(CacheRow row) {
//        RoleToolRow toolRow = (RoleToolRow) row;
//        return SqlUtil.getSqlForObj(getKey(), row, getTableName(),
//                " roleid = " + getKey() + " and toolid = " + toolRow.getToolId());
//    }
//
//    @Override
//    public Iterator<? extends CacheRow> rowIterator() {
//        return toolMap.values().iterator();
//    }

//    @Override
//    public void load() throws Exception {
//        toolMap = new HashMap<>();
//        gridMap = new HashMap<>();
//        toolMap = DbUtil.queryMap(getKey(), "toolid", RoleToolRow.class,
//                "select * from `roletool" + getTableIndex() + "`  where `roleid` = " + getKey(), HashMap.class);
//    }

//    @Override
//    public boolean getAutoCommit() {
//        return false;
//    }

//    @Override
//    public void loadFromCache(List<CacheRow> multiRows) {
//        //载入前先清空,防止出错
//        toolMap =  new HashMap<>();
//        gridMap =  new HashMap<>();
//        for (CacheRow cacheRow : multiRows) {
//            RoleToolRow row = (RoleToolRow) cacheRow;
//            putRow(row);
//        }
//    }

    /**
     * 添加道具到map中
     *
     * @param row
     */
    public void putRow(RoleToolRow row) {
        toolMap.put(row.getToolId(), row);
    }


    /**
     * 删除道具入口
     * 只管数据上的删除,不管排序
     */
    public void delete(long toolId, int count) {
        RoleToolRow toolRow = toolMap.get(toolId);
        if (toolRow == null) {
            return;
        }
        //分情况讨论,如果删除数量为道具数量,整个道具删除
        //传入的count不会出现比道具数量多的情况
        if (count >= toolRow.getCount()) {
//            String delSql = SqlUtil.getDeleteSql(getTableName(),
//                    " roleid = " + getKey() + " and toolid = " + toolId);
//            addDelSql(Long.toString(toolId), delSql);
        	toolRow.setCount(0);
//            toolRow.setDeleteStatus();
//            SqlPool.addDeleteSql(toolRow.getDeleteSql());
            toolModule.context().delete(toolRow);
            toolMap.remove(toolId);
        } else {
            //如果删除数量不足道具数量,则只是修改
            toolRow.setCount(toolRow.getCount() - count);
//            toolRow.setUpdateStatus();
            toolModule.context().update(toolRow);
        }
        getFlushToClient().add(toolRow);
    }

    /**
     * 下发包内所有的道具
     */
    public void sendAll(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort((short) toolMap.size());
        for (RoleToolRow toolRow : toolMap.values()) {
            toolRow.writeToBuffer(buff);
        }
    }

    /**
     * 根据id下发一部分的数据
     * 这部分数据必须存在,不然会出错
     */
    public void send(NewByteBuffer buff, Set<Long> toolids) {
        buff.writeInt(toolids.size());
        RoleToolRow toolRow;
        for (long toolId : toolids) {
            toolRow = toolMap.get(toolId);
            toolRow.writeToBuffer(buff);
        }
    }



    public Map<Long, RoleToolRow> getToolMap() {
        return toolMap;
    }
    
	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}

	public HashSet<RoleToolRow> getFlushToClient() {
		if (flushToClient == null) {
			flushToClient = new HashSet<RoleToolRow>();
		}
		return flushToClient;
	}

	public void setFlushToClient(HashSet<RoleToolRow> flushToClient) {
		this.flushToClient = flushToClient;
	}

	public void flushAllToClient(){
		if (toolMap != null && toolMap.size() > 0) {
			flushToClient.addAll(toolMap.values());
		}
	}

    public HashSet<RoleToolRow> getNeedToMark() {
        if(needToMark == null){
            needToMark = new HashSet<>();
        }
        return needToMark;
    }
}
