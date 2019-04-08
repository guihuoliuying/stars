package com.stars.modules.poem.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.poem.PoemPacketSet;
import com.stars.modules.poem.userdata.PoemData;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;


/**
 * Created by gaopeidian on 2017/1/9.
 */
public class ClientPoem extends PlayerPacket {
	public static final byte Flag_Send_All_Poem_Data = 1;
	public static final byte Flag_Send_Cur_Poem = 2;
	public static final byte Flag_Update_Poem = 3;
	
    private byte flag;
    
    //flag = 1 时用
    private Map<Integer, PoemData> poemDatas;
    
    //flag = 2、3 时用
    private PoemData poemData;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return PoemPacketSet.C_POEM;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
    	buff.writeByte(flag);
    	switch (flag) {
		case Flag_Send_All_Poem_Data:
			writePoems(buff);
			break;
		case Flag_Send_Cur_Poem:
			writePoem(buff);
			break;
		case Flag_Update_Poem:
			writePoem(buff);
			break;
		default:
			break;
		}
    }
    
    public void writePoems(com.stars.network.server.buffer.NewByteBuffer buff){
    	short size = (short) (poemDatas == null ? 0 : poemDatas.size());
        buff.writeShort(size);
        if (size != 0) {
            for (PoemData pData : poemDatas.values()) {      	
            	buff.writeInt(pData.poemId);//诗集id
            	buff.writeInt(pData.finishDungeonCount);//完成的关卡次数
            	buff.writeInt(pData.totalDungeonCount);//总关卡次数
            	buff.writeInt(pData.bossDungeonId);//boss关卡id
            	buff.writeString(pData.worldTitle);//章节title
            	buff.writeString(pData.worldName);//章节name
            	buff.writeString(pData.generalDrop);//通关奖励
            	buff.writeInt(pData.recommend);//推荐战力
            	buff.writeString(pData.showItem);
            	buff.writeString(pData.showdescwin);
            	buff.writeInt(pData.teamType);           	
            }
        }
    }
    
    public void writePoem(NewByteBuffer buff){
    	buff.writeInt(poemData.poemId);//诗集id
    	if(poemData.poemId == -1){
    		return;
    	}
    	buff.writeInt(poemData.finishDungeonCount);//完成的关卡次数
    	buff.writeInt(poemData.totalDungeonCount);//总关卡次数
    	buff.writeInt(poemData.bossDungeonId);//boss关卡id
    	buff.writeString(poemData.worldTitle);//章节title
    	buff.writeString(poemData.worldName);//章节name
    	buff.writeString(poemData.generalDrop);//通关奖励
    	buff.writeInt(poemData.recommend);//推荐战力
    	buff.writeString(poemData.showItem);
    	buff.writeString(poemData.showdescwin);
    	buff.writeInt(poemData.teamType);
    }
    
    public void setFlag(byte value){
    	this.flag = value;
    }
    
    public void setPoemDatas(Map<Integer, PoemData> value){
    	this.poemDatas = value;
    }
    
    public void setPoemData(PoemData value){
    	this.poemData = value;
    }
}