package com.stars.modules.luckydraw.packet;

import com.stars.modules.luckydraw.LuckyDrawManagerFacade;
import com.stars.modules.luckydraw.LuckyDrawPacketSet;
import com.stars.modules.luckydraw.pojo.LuckyDrawAnnounce;
import com.stars.modules.luckydraw.prodata.LuckyPumpAwardVo;
import com.stars.modules.luckydraw.userdata.RoleLuckyDrawTimePo;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/8/10.
 */
public class ClientLuckyDrawPacket extends Packet {
    private int actType;
    private short subType;
    public final static short SEND_MainUiStaticData = 1;//打开界面的数据(静态)
    public final static short SEND_MainUiDynamicData = 2;//打开界面的数据(动态)
    public final static short SEND_LuckyDrawOnce = 3;//抽奖一次
    public final static short SEND_ACTIVITY_STATUS = 4;//活动状态
    private int leftTicketCount;//剩余抽奖券数量
    private int rewardId;
    private RoleLuckyDrawTimePo roleLuckyDrawTime;
    private OperateActVo operateActVo;
    private boolean isOpen;
    private Map<Integer, Integer> itemMap = new HashMap<>();
    List<LuckyDrawAnnounce> luckyAnnounceTop10;

    public ClientLuckyDrawPacket(short subType, int actType) {
        this.subType = subType;
        this.actType = actType;
    }

    public ClientLuckyDrawPacket() {
    }

    @Override
    public short getType() {
        return LuckyDrawPacketSet.C_LUCKY_DRAW;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(actType);
        buff.writeShort(subType);
        switch (subType) {
            case SEND_MainUiStaticData: {
                buff.writeInt(LuckyDrawManagerFacade.getLuckyPumpAwardList(getActType()).size());
                for (LuckyPumpAwardVo luckyPumpAwardVo : LuckyDrawManagerFacade.getLuckyPumpAwardList(getActType())) {
                    luckyPumpAwardVo.writeBuff(buff);
                }
                buff.writeInt(LuckyDrawManagerFacade.getLuckyDrawConsumeUnit(getActType()));//单笔抽奖所需奖券
                buff.writeString(LuckyDrawManagerFacade.getLuckyPumpMoney(getActType()));//格式：额度|itemid+count
                ActOpenTime5 actOpenTime5 = (ActOpenTime5) operateActVo.getActOpenTimeBase();
                buff.writeLong(actOpenTime5.getStartDate().getTime());//开始时间戳
                buff.writeLong(actOpenTime5.getEndDate().getTime());//结束时间戳
            }
            break;
            case SEND_MainUiDynamicData: {
                buff.writeInt(roleLuckyDrawTime.getFreeTime());
                buff.writeInt(leftTicketCount);
                buff.writeInt(LuckyDrawManagerFacade.getLuckyDrawNumlimit(getActType()) - roleLuckyDrawTime.getDailyTime());//剩余次数
                /**
                 * 幸运榜
                 */
                buff.writeInt(luckyAnnounceTop10.size());
                for (LuckyDrawAnnounce luckyDrawAnnounce : luckyAnnounceTop10) {
                    luckyDrawAnnounce.writeBuff(buff);
                }
            }
            break;

            case SEND_LuckyDrawOnce: {
                buff.writeInt(rewardId);
            }
            break;
            case SEND_ACTIVITY_STATUS: {
                if (isOpen) {
                    buff.writeInt(1);
                } else {
                    buff.writeInt(0);
                }
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }


    public int getLeftTicketCount() {
        return leftTicketCount;
    }

    public void setLeftTicketCount(int leftTicketCount) {
        this.leftTicketCount = leftTicketCount;
    }

    public int getRewardId() {
        return rewardId;
    }

    public void setRewardId(int rewardId) {
        this.rewardId = rewardId;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public List<LuckyDrawAnnounce> getLuckyAnnounceTop10() {
        return luckyAnnounceTop10;
    }

    public void setLuckyAnnounceTop10(List<LuckyDrawAnnounce> luckyAnnounceTop10) {
        this.luckyAnnounceTop10 = luckyAnnounceTop10;
    }

    public OperateActVo getOperateActVo() {
        return operateActVo;
    }

    public void setOperateActVo(OperateActVo operateActVo) {
        this.operateActVo = operateActVo;
    }

    public RoleLuckyDrawTimePo getRoleLuckyDrawTime() {
        return roleLuckyDrawTime;
    }

    public void setRoleLuckyDrawTime(RoleLuckyDrawTimePo roleLuckyDrawTime) {
        this.roleLuckyDrawTime = roleLuckyDrawTime;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getActType() {
        return actType;
    }

    public void setActType(int actType) {
        this.actType = actType;
    }
}
