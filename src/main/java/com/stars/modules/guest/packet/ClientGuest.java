package com.stars.modules.guest.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.guest.GuestPacketSet;
import com.stars.modules.guest.userdata.RoleGuest;
import com.stars.modules.guest.userdata.RoleGuestExchange;
import com.stars.modules.guest.userdata.RoleGuestMission;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/1/3.
 */
public class ClientGuest extends PlayerPacket {

    public final static byte RES_DISPATCH = 1;  // 派遣
    public final static byte RES_MISSION_INFO = 2;  // 任务信息
    public final static byte RES_GUEST_INFO = 3;    // 门客信息
    public final static byte RES_AWARD = 4;     // 领取奖励
    public final static byte RES_ACTIVE = 5;    // 激活
    public final static byte RES_UPSTAR = 6;    // 升星
    public final static byte RES_EXCHANGE_INFO = 7; // 交换信息
    public final static byte RES_ASK = 8;       // 求助
    public final static byte RES_GIVE = 9;      // 给予
    public final static byte RES_OPEN_MISSION = 10; // 客户端要求新加打开任务列表界面
    public final static byte RES_GIVE_AWARD = 11;   // 给予奖励

    private byte resType;

    private byte dispatchResult;
    private Map<Integer, RoleGuestMission> missionMap;
    private Map<Integer, RoleGuest> guestMap;
    private Map<Integer, Integer> guest2mission;
    private Map<Integer, Integer> toolMap;
    private RoleGuest guest;
    private int missionId;
    private List<RoleGuestExchange> infoList;
    private int askCount;
    private long giveId;
    private int flushCount;
    private int remainTime;

    public void setRemainTime(int remainTime) {
        this.remainTime = remainTime;
    }

    public void setFlushCount(int flushCount) {
        this.flushCount = flushCount;
    }

    public void setGiveId(long giveId) {
        this.giveId = giveId;
    }

    public void setAskCount(int askCount) {
        this.askCount = askCount;
    }

    public void setInfoList(List<RoleGuestExchange> infoList) {
        this.infoList = infoList;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public void setGuest(RoleGuest guest) {
        this.guest = guest;
    }

    public void setToolMap(Map<Integer, Integer> toolMap) {
        this.toolMap = toolMap;
    }

    public void setGuest2mission(Map<Integer, Integer> guest2mission) {
        this.guest2mission = guest2mission;
    }

    public void setGuestMap(Map<Integer, RoleGuest> guestMap) {
        this.guestMap = guestMap;
    }

    public void setMissionMap(Map<Integer, RoleGuestMission> missionMap) {
        this.missionMap = missionMap;
    }

    public void setResType(byte resType) {
        this.resType = resType;
    }

    public void setDispatchResult(byte dispatchResult) {
        this.dispatchResult = dispatchResult;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(resType);
        switch (resType) {
            case RES_DISPATCH:
                buff.writeByte(dispatchResult);
                buff.writeInt(DateUtil.getSecondTime());
                break;
            case RES_MISSION_INFO:
                buff.writeInt(flushCount);
                buff.writeInt(remainTime);
                buff.writeInt(DateUtil.getSecondTime());
                buff.writeInt(missionMap.size());
                for (RoleGuestMission mission : missionMap.values()) {
                    buff.writeInt(mission.getMissionId());
                    buff.writeInt(mission.getFreshStamp());
                    buff.writeInt(mission.getStartStamp());
                    buff.writeString(mission.getGuestGroup());
                    buff.writeByte(mission.getState());
                }
                break;
            case RES_GUEST_INFO:
                buff.writeInt(guestMap.size());
                for (RoleGuest guest : guestMap.values()) {
                    Integer missionId = guest2mission.get(guest.getGuestId());
                    buff.writeInt(guest.getGuestId());
                    buff.writeInt(guest.getLevel());
                    buff.writeInt(missionId == null ? 0 : missionId);
                }
                break;
            case RES_AWARD:
                buff.writeInt(missionId);
                buff.writeInt(toolMap.size());
                for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case RES_ACTIVE:
                buff.writeInt(guest.getGuestId());
                buff.writeInt(guest.getLevel());
                break;
            case RES_UPSTAR:
                buff.writeInt(guest.getGuestId());
                buff.writeInt(guest.getLevel());
                break;
            case RES_EXCHANGE_INFO:
                buff.writeInt(DateUtil.getSecondTime());
                buff.writeInt(askCount);
                buff.writeInt(infoList.size());
                for (RoleGuestExchange exchange : infoList) {
                    buff.writeString(String.valueOf(exchange.getRoleId()));
                    buff.writeString(exchange.getName());
                    buff.writeInt(exchange.getGuestId());
                    buff.writeInt(exchange.getLevel());
                    buff.writeInt(exchange.getGiveCount());
                    buff.writeInt(exchange.getItemId());
                    buff.writeString(exchange.getAskClaim());
                    buff.writeInt(exchange.getAskCount());
                    buff.writeInt(exchange.getStamp());
                    if (exchange.getGiveSet().contains(giveId)) {
                        buff.writeByte((byte) 1);
                    } else {
                        buff.writeByte((byte) 0);
                    }
                }
                break;
            case RES_GIVE_AWARD:
                buff.writeInt(toolMap.size());
                for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return GuestPacketSet.C_GUEST;
    }
}
