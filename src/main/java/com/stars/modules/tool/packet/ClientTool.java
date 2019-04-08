package com.stars.modules.tool.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.tool.ToolPacketSet;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.HashSet;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class ClientTool extends PlayerPacket {

    /**
     * 道具系统下发策略:
     * 1,登录时会下发所有(背包,装备,包括武将已装备的,并下发装备的属性)
     * 2,除非装备的属性发生改变,否则装备属性不会再做下发
     * 3,某个背包内的数据发生变化(增,删),会下发对应背包的数据,这样做的好处是:
     *      a,使下发的代码和前端的代码更简洁,不需要考虑逻辑的增删改,直接下发刷新
     *      b,由于不下发属性,数据量不大
     *      c,每一次的道具操作,只需要下发一个包
     *
     * 4,排序时,下发所有道具的信息(不下发属性)
     * 5,某个已有的道具发生变化,下发对应的数据(有可能下发属性)
     */
	int clientSystemConstant;
    private HashSet<RoleToolRow> tools;//部分道具发生变化时使用

    public ClientTool(){
    	
    }
    
    
    public ClientTool(HashSet<RoleToolRow> tools , int clientSystemConstant) {
    	this.clientSystemConstant = clientSystemConstant;
        this.tools = tools;
    }


    @Override
    public void writeToBuffer(NewByteBuffer buff) {
    	buff.writeInt(clientSystemConstant);
    	short size = (short)tools.size();
    	buff.writeShort(size);
    	if (size <= 0) {
			return;
		}
    	for (RoleToolRow toolRow:tools) {
    		if (toolRow == null) {
			}
			toolRow.writeToBuffer(buff);
		}
    	tools.clear();
    }


    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ToolPacketSet.C_TOOL;
    }
}
