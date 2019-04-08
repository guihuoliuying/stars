package com.stars.modules.teamdungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.teamdungeon.TeamDungeonPacketSet;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/11/17.
 */
public class ClientTeamDungeonPacket extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte TEAM_DUNGEON_INFO = 0;// 组队副本信息
    public static final byte BACK_TO_CITY = 1;// 回城消息

    /* 参数 */
    private List<TeamDungeonVo> teamDungeonVoList;
    private Map<Integer, Integer> remainCountMap;
    private int damage;
    private Map<Integer, Integer> itemMap;

    public ClientTeamDungeonPacket() {
    }

    public ClientTeamDungeonPacket(byte sendType) {
        this.sendType = sendType;
        switch (sendType) {
            case TEAM_DUNGEON_INFO:
                teamDungeonVoList = new ArrayList<>();
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TeamDungeonPacketSet.Client_TeamDungeon;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case TEAM_DUNGEON_INFO:
                writeTeamDungeonInfo(buff);
                break;
            case BACK_TO_CITY:
                buff.writeInt(damage);
                short size = (short) (itemMap == null ? 0 : itemMap.size());
                buff.writeShort(size);
                if (itemMap != null) {
                    for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
        }
    }

    private void writeTeamDungeonInfo(NewByteBuffer buff) {
        byte size = (byte) teamDungeonVoList.size();
        buff.writeByte(size);
        if (size == 0)
            return;
        for (TeamDungeonVo teamDungeon : teamDungeonVoList) {
            buff.writeInt(teamDungeon.getTeamdungeonid());
            buff.writeString(teamDungeon.getTargethpreward());
            buff.writeString(teamDungeon.getSucreward());
            buff.writeString(teamDungeon.getFailreward());
            buff.writeString(teamDungeon.getDamagereward());
            buff.writeString(teamDungeon.getAddPercent());
            int remainCount = 0;
            if (remainCountMap != null && remainCountMap.containsKey(teamDungeon.getTeamdungeonid())) {
                remainCount = remainCountMap.get(teamDungeon.getTeamdungeonid());
            }
            buff.writeByte((byte) remainCount);//剩余次数
        }

    }

    public void addTeamDungeon(TeamDungeonVo td) {
        this.teamDungeonVoList.add(td);
    }

    public void setRemainCountMap(Map<Integer, Integer> value) {
        this.remainCountMap = value;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }
}
