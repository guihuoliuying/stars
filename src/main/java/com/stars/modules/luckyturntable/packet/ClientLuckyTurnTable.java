package com.stars.modules.luckyturntable.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.luckyturntable.LuckyTurnTablePacketSet;
import com.stars.modules.luckyturntable.prodata.LuckyTurnTableVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.luckyturntable.cache.LuckyList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-07-12.
 */
public class ClientLuckyTurnTable extends PlayerPacket {
    public static final byte turnTable = 0x00;
    public static final byte awardList = 0x01;
    public static final byte winning = 0x02;
    public static final byte iconState = 0x03;
    private Map<LuckyTurnTableVo, Byte> itemMap = new HashMap<>();
    private List<LuckyList> list = new LinkedList<>();
    private int Id;
    private byte subType;
    private int count;//已抽奖次数
    private int reqLotteryCount;//
    private int lotteryCount;//
    private byte icon;

    public ClientLuckyTurnTable() {
    }

    public ClientLuckyTurnTable(byte subType) {
        this.subType = subType;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subType);
        switch (subType) {
            case turnTable:
                buff.writeInt(count);//已抽奖次数
                buff.writeInt(reqLotteryCount);//本次抽奖需要多少个抽奖券
                buff.writeInt(lotteryCount);//有多少抽奖券
                buff.writeByte((byte) itemMap.size());
                for (Map.Entry<LuckyTurnTableVo, Byte> entry : itemMap.entrySet()) {
                    entry.getKey().writeToBuff(buff);//玩家能看到的itemId
                    buff.writeByte(entry.getValue());//该物品是否已抽中(1:已抽中,0:未抽中)
                }
                break;
            case awardList:
                buff.writeByte((byte) list.size());
                for (LuckyList luckyList : list) {
                    luckyList.writeToBuff(buff);
                }
                break;
            case winning:
                buff.writeInt(Id);//抽奖所获得的itemId
            case iconState:
                buff.writeByte(icon);//1:活动进行中|0:活动结束
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return LuckyTurnTablePacketSet.C_LUCKYTURNTABLE;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setReqLotteryCount(int reqLotteryCount) {
        this.reqLotteryCount = reqLotteryCount;
    }

    public void setLotteryCount(int lotteryCount) {
        this.lotteryCount = lotteryCount;
    }

    public void setItemMap(Map<LuckyTurnTableVo, Byte> itemMap) {
        this.itemMap = itemMap;
    }

    public void setList(List<LuckyList> list) {
        this.list = list;
    }

    public void setIcon(byte icon) {
        this.icon = icon;
    }
}
