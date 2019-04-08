package com.stars.modules.tool.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.ToolPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class ServerTool extends PlayerPacket {

    private byte opType;
    private byte bagType;//背包类型,1:道具 2:装备
    private int grid;//格子号

    private int generalId;// 武将Id
    private int itemId;// 道具Id
    private long toolId;//道具的唯一id
    private int count;

    private String param;// 使用道具参数
    @Override
    public void execPacket(Player player) {
        ToolModule module = (ToolModule) module(MConst.Tool);
        if (this.opType == (byte) 1) {
            module.changeNewFlag();
        } else if (this.opType == (byte) 2) {
            module.sort();
        } else if (this.opType == (byte) 3) {// 使用所有的道具请求
            module.useAllToolByItemId(itemId,count);
        } else if (this.opType == (byte) 4) {
            module.useTool(toolId, count, param);
        } else  if (this.opType == 5) {
			module.sellTool(toolId, count);
		} else if (this.opType == 6) {
			module.buyItem(itemId, count);
		} else if (this.opType == 7) {// 合成道具
            module.executeCompose(itemId, count);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        this.opType = buff.readByte();
        if (opType == (byte) 1) {
//            this.toolId = Long.parseLong(buff.readString(), 16);
        } else if (opType == (byte) 3) {
//            this.generalId = buff.readInt();
            this.itemId = buff.readInt();
            this.count = buff.readInt();
        } else if (opType == (byte) 4) {
            this.toolId = Long.parseLong(buff.readString(), 16);
            this.count = buff.readInt();
            this.param = buff.readString();
        } else if(opType == (byte)5){
        	this.toolId = Long.parseLong(buff.readString(), 16);
        	this.count= buff.readInt();
        } else if (opType == (byte)6) {
			this.itemId = buff.readInt();
			this.count = buff.readInt();
		} else if (this.opType == 7) {// 合成道具
            this.itemId = buff.readInt();
            this.count = buff.readInt();
        }
    }

    @Override
    public short getType() {
        return ToolPacketSet.S_TOOL;
    }
}
