package com.stars.modules.loottreasure.packet;

import com.stars.bootstrap.ServerManager;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.loottreasure.LootTreasureConstant;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.LootTreasurePacketSet;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.I18n;

import java.util.Map;

/**
 * 响应客户端请求野外夺宝的数据;
 * Created by panzhenfeng on 2016/10/10.
 */
public class ClientLootTreasureInfo  extends PlayerPacket {
    public final static byte TYPE_INFO = 0;
    public final static byte TYPE_ACTIVITY_NOTICE = 1;
    public final static byte TYPE_ENTRY_PVP = 2;
    public final static byte TYPE_END_AWARDS = 3;
    public final static byte TYPE_SWITCH_ROOM_RTN = 4;
    //活动阶段的标识量;
    private LootTreasureConstant.ACTIVITYSEGMENT activitysegment = LootTreasureConstant.ACTIVITYSEGMENT.ACTIVITYS_START;

    private byte type = TYPE_INFO; //0标识信息; 1活动状态通知; 3进入PVP场景(只是一个通知);
    public int referenceLevel = 0;
    private Map<Integer, Integer> rewardMap;
    private byte roomType;
    private long switchRoomEndCdStamp;
    private boolean roomTypeIsReallysSwitched = false;
    private short roomDiffBoxCount = 0;
    private long startStamp;
    private long endStamp;

    public ClientLootTreasureInfo(){

    }
    public ClientLootTreasureInfo(byte type) {
        this.type = type;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LootTreasurePacketSet.C_LOOTTREASURE_INFO;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(type);
        switch (type){
            case TYPE_INFO:
                LootSectionVo lootSectionVo = LootTreasureManager.getLootSectionVoByLevel(referenceLevel);
                buff.writeInt(lootSectionVo.getStageid());
                buff.writeInt(lootSectionVo.getMonsterid());
                buff.writeString(lootSectionVo.getShow());
                buff.writeInt(lootSectionVo.getMinLevelSectionLevel());
                buff.writeInt(lootSectionVo.getLevelsection());
                //写入活动时间戳;
                buff.writeString(String.valueOf(startStamp));
                buff.writeString(String.valueOf(endStamp));
//                buff.writeString(ServerManager.getServerName()+ServerManager.getServer().getConfig().getServerId());
                buff.writeString(I18n.get("common.serverName", ServerManager.getServer().getConfig().getServerId()));
//                buff.writeString("首测服");
                break;
            case TYPE_ACTIVITY_NOTICE:
                buff.writeByte((byte)activitysegment.ordinal());
                buff.writeString(String.valueOf(startStamp));
                buff.writeString(String.valueOf(endStamp));
                break;
            case TYPE_ENTRY_PVP:
                buff.writeByte(roomType);
                buff.writeString(String.valueOf(getSwitchRoomEndCdStamp()));
                break;
            case TYPE_END_AWARDS:
                byte size = (byte)(rewardMap == null ? 0 : rewardMap.size());
                buff.writeByte(size);
                for (Map.Entry<Integer, Integer> kvp : rewardMap.entrySet()){
                    buff.writeInt(kvp.getKey());
                    buff.writeInt(kvp.getValue());
                }
                break;
            case TYPE_SWITCH_ROOM_RTN:
                buff.writeByte(this.roomTypeIsReallysSwitched?(byte)1: (byte)0);
                buff.writeByte(roomType);
                if(roomType >= 0){
                    buff.writeString(String.valueOf(getSwitchRoomEndCdStamp()));
                }else if(roomType == (byte)-1){
                    buff.writeShort(roomDiffBoxCount);
                }
                break;
        }
    }

    public void setActivitySegment(LootTreasureConstant.ACTIVITYSEGMENT activitysegment){
        this.activitysegment = activitysegment;

    }

    public void setRewardMap(Map<Integer, Integer> rewardMap) {
        this.rewardMap = rewardMap;
    }

    public void setRoomTypeIsReallySwitched(boolean value){
        this.roomTypeIsReallysSwitched = value;
    }

    public void setRoomType(byte roomType) {
        this.roomType = roomType;
    }

    public long getSwitchRoomEndCdStamp() {
        return switchRoomEndCdStamp;
    }

    public void setSwitchRoomEndCdStamp(long switchRoomEndCdStamp) {
        this.switchRoomEndCdStamp = switchRoomEndCdStamp;
    }

    public void setStartStamp(long startStamp) {
        this.startStamp = startStamp;
    }

    public void setEndStamp(long endStamp) {
        this.endStamp = endStamp;
    }

    public void setRoomDiffBoxCount(int roomDiffBoxCount) {
        this.roomDiffBoxCount = (short)roomDiffBoxCount;
    }
}