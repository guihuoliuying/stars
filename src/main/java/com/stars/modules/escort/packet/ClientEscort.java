package com.stars.modules.escort.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.escort.EscortPacketSet;
import com.stars.modules.escort.userdata.vo.CargoRecord;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/2.
 */
public class ClientEscort extends PlayerPacket {

    public static final byte RESP_VIEW_MAIN_ENTRY = 0x00;   // 查看运镖入口界面
    public static final byte RESP_VIEW_CARGO_SELECT = 0x01; // 镖车选择界面
    public static final byte RESP_PLAY_ENEMY_COME_AMJ = 0x02; // 播放强敌来袭特效(被劫镖时在押镖方播放)
    public static final byte RESP_ESCORT_CONTINUES = 0x03;  // 继续运镖(关闭结算界面)
    public static final byte RESP_BACK_CITY_AND_OPEN_TEAM_UI = 0x04;  // 回城并打开劫镖组队界面

    private byte subtype;
    private int remainTimes;     //剩余运镖次数
    private int remainRobTimes;  //剩余劫镖次数
    private int dailyFreshTimes; //今日刷新次数
    private Map<Byte, CargoRecord> cargoRecordMap;   //镖车选择记录
    private int recordResetCoolDown;

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_VIEW_MAIN_ENTRY:
                writeMainEntryToBuff(buff);
                break;
            case RESP_VIEW_CARGO_SELECT:
                writeCargoSelectToBuff(buff);
                break;
            case RESP_PLAY_ENEMY_COME_AMJ:
                break;
            case RESP_ESCORT_CONTINUES:
                break;
            case RESP_BACK_CITY_AND_OPEN_TEAM_UI:
                buff.writeInt(remainRobTimes);
                break;
        }
    }

    private void writeCargoSelectToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(remainTimes);
        buff.writeInt(dailyFreshTimes);
        buff.writeInt(recordResetCoolDown);
        if(StringUtil.isEmpty(cargoRecordMap)){
            buff.writeByte((byte)0);
        }else{
            buff.writeByte((byte)cargoRecordMap.size());
            CargoRecord record;
            for(Map.Entry<Byte,CargoRecord> entry:cargoRecordMap.entrySet()){
                buff.writeByte(entry.getKey());
                record = entry.getValue();
                buff.writeInt(record.getCargoId());
                buff.writeByte(record.getHasUsed());
            }
        }
    }

    private void writeMainEntryToBuff(NewByteBuffer buff) {
        buff.writeInt(remainTimes);
        buff.writeInt(remainRobTimes);
    }

    public ClientEscort() {
    }

    public ClientEscort(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public short getType() {
        return EscortPacketSet.C_ESCORT;
    }

    @Override
    public void execPacket(Player player) {

    }

    public void setRemainTimes(int remainTimes) {
        this.remainTimes = remainTimes;
    }

    public void setRemainRobTimes(int remainRobTimes) {
        this.remainRobTimes = remainRobTimes;
    }

    public void setCargoRecordMap(Map<Byte, CargoRecord> cargoRecordMap) {
        this.cargoRecordMap = cargoRecordMap;
    }

    public void setRecordResetCoolDown(int recordResetCoolDown) {
        this.recordResetCoolDown = recordResetCoolDown;
    }

    public void setDailyFreshTimes(int dailyFreshTimes) {
        this.dailyFreshTimes = dailyFreshTimes;
    }
}
