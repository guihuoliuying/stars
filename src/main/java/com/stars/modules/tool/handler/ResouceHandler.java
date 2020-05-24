package com.stars.modules.tool.handler;


import com.stars.modules.role.RoleModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangjiahua on 2016/2/24.
 */
public class ResouceHandler implements ToolHandler {
    //资源会关联人物模块
    private RoleModule roleModule;
    //用于关联到客户端的业务,在做资源来源时可以作为标记;
    private int clientSystemConstant;

    public ResouceHandler(RoleModule roleModule) {
        this.roleModule = roleModule;
    }

    public void setClientSystemConstant(int clientSystemConstant) {
        this.clientSystemConstant = clientSystemConstant;
    }

    /**
     * 增加资源,这里的id必须已经经过判断
     */
    @Override
    public Map<Integer, Integer> add(int itemId, int count) {
        roleModule.addResource((byte) itemId, count, clientSystemConstant);
        Map<Integer, Integer> resultMap = new HashMap<>();
        resultMap.put(itemId, count);
        return resultMap;
    }


    @Override
    public boolean deleteByItemId(int itemId, int count) {
        if (itemId == ToolManager.GOLD || itemId == ToolManager.BANDGOLD) {//绑金和代金特殊处理
            return deleteGold(count);
        }
        roleModule.addResource((byte) itemId, -count);
        return false;
    }

    private boolean deleteGold(int count) {
        if (count <= 0) return false;
        int bindGold = roleModule.getResource((byte) ToolManager.BANDGOLD);
        if (bindGold > 0) {//优先扣除绑金
            if (bindGold >= count) {
                roleModule.addResource((byte) ToolManager.BANDGOLD, -count);
                count = 0;
            } else {
                roleModule.addResource((byte) ToolManager.BANDGOLD, -bindGold);
                count -= bindGold;
            }
        }
        if (count > 0) {
            roleModule.addResource((byte) ToolManager.GOLD, -count);
        }
        return false;
    }

    @Override
    public boolean deleteByToolId(long toolId, int count) {
        return false;
    }

    @Override
    public void add(RoleToolRow toolRow) {
    }

    @Override
    public int getNullGrid() {
        return 0;
    }

    @Override
    public long getCountByItemId(int itemId) {
        if (itemId == ToolManager.GOLD || itemId == ToolManager.BANDGOLD) {//绑金和代金特殊处理
            return roleModule.getResource((byte) ToolManager.GOLD) + roleModule.getResource((byte) ToolManager.BANDGOLD);
        }
        return roleModule.getResource((byte) itemId);
    }

    @Override
    public void updateToolRow(RoleToolRow toolRow) {

    }

    @Override
    public int canAdd(int itemId, int count) {
        if (itemId == ToolManager.GOLD) {
//            return limit - have;
        } else if (itemId == ToolManager.MONEY) {
//            return limit - have;
        } else if (itemId == ToolManager.BANDGOLD) {
//            return limit - have;
        } else if (itemId == ToolManager.VIGOR) {
//            return limit - have;
        }
        return count;
    }

    @Override
    public void sort() {
    }

    @Override
    public boolean canAdd(Map<Integer, Integer> toolMap) {
        return true;
    }

}
