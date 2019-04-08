package com.stars.modules.newofflinepvp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.newofflinepvp.NewOfflinePvpManager;
import com.stars.modules.newofflinepvp.NewOfflinePvpPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.newofflinepvp.cache.BattleReport;
import com.stars.services.newofflinepvp.userdata.NewOfflinePvpRankPo;

import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2017-03-09 19:36
 */
public class ClientNewOfflinePvp extends PlayerPacket {
    public static final byte view = 0x00;//打开页面
    public static final byte rankAward = 0x01;//排行榜
    public static final byte battleReport = 0x02;//战报
    public static final byte historyRankAward = 0x03;//历史排行奖励
    public static final byte sectionRankAward = 0x05;//晋升奖励
    public static final byte buyCount = 0x04;//购买次数
    public static final byte fightCount = 0x06;//战斗次数

    private byte subtype;
    private int myRank;
    private int myMaxRank;
    private int remainFightCount;
    private int remainBuyCount;
    private List<NewOfflinePvpRankPo> otherPlayerDatas;//下发匹配的数据
    private List<NewOfflinePvpRankPo> rankAwardList;//排行榜
    private List<BattleReport> battleReports;//战报
    private Map<Integer, Integer> historyRankAwardItemMaps;//历史排名奖励
    private Map<Integer, Integer> sectionRankAwardItemMaps;//晋升奖励
    private int onRank;//自己有没有在排行榜上面
    private byte onWhichRank;
    private String roleIdStr;
    private String roleNameStr;
    private int fightScore;
    private int fightTimes;

    public ClientNewOfflinePvp() {
    }

    public ClientNewOfflinePvp(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case view:
                buff.writeInt(myRank);
                buff.writeInt(myMaxRank);
                buff.writeInt(remainFightCount);
                buff.writeInt(remainBuyCount);
                buff.writeByte((byte) (otherPlayerDatas != null ? otherPlayerDatas.size() : 0));
                if (otherPlayerDatas != null) {
                    for (NewOfflinePvpRankPo rankPo : otherPlayerDatas) {
                        buff.writeString(String.valueOf(rankPo.getRoleId()));
                        buff.writeInt(rankPo.getJobId());
                        buff.writeString(rankPo.getRoleName());
                        buff.writeInt(rankPo.getLevel());
                        buff.writeInt(rankPo.getFightScore());
                        buff.writeInt(rankPo.getRank());
                        buff.writeByte(rankPo.getRoleOrRobot());
                    }
                }
                break;
            case rankAward:
                boolean isOnRank = onRank == NewOfflinePvpManager.onRank;
                int size = rankAwardList == null ? 0 : rankAwardList.size();
                if (!isOnRank) {
                    size = size + 1;
                }
                buff.writeByte((byte) size);
                for (NewOfflinePvpRankPo rankPo : rankAwardList) {
                    rankPo.writeToBuff(buff);
                }
                if (!isOnRank) {
                    buff.writeInt(5001);
                    buff.writeString(roleIdStr);
                    buff.writeString(roleNameStr);
                    buff.writeInt(fightScore);
                }
                break;
            case battleReport:
                buff.writeByte((byte) (battleReports == null ? 0 : battleReports.size()));
                for (BattleReport report : battleReports) {
                    report.writeToBuff(buff);
                }
                break;
            case buyCount:
                buff.writeInt(remainFightCount);
                buff.writeInt(remainBuyCount);
                break;
            case sectionRankAward:
                buff.writeInt(myMaxRank);
                buff.writeByte(onWhichRank);
                buff.writeByte((byte) sectionRankAwardItemMaps.size());
                for (Map.Entry<Integer, Integer> entry : sectionRankAwardItemMaps.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case historyRankAward:
                buff.writeInt(myMaxRank);
                buff.writeByte((byte) historyRankAwardItemMaps.size());
                for (Map.Entry<Integer, Integer> entry : historyRankAwardItemMaps.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case fightCount:
                buff.writeInt(fightTimes);
                break;
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewOfflinePvpPacketSet.C_OFFLINEPVP;
    }

    public void setOnWhichRank(byte onWhichRank) {
        this.onWhichRank = onWhichRank;
    }

    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }

    public void setMyMaxRank(int myMaxRank) {
        this.myMaxRank = myMaxRank;
    }

    public void setRemainCount(int remainCount) {
        this.remainFightCount = remainCount;
    }

    public void setOtherPlayerDatas(List<NewOfflinePvpRankPo> otherPlayerDatas) {
        this.otherPlayerDatas = otherPlayerDatas;
    }

    public void setRankAwardList(List<NewOfflinePvpRankPo> rankAwardList) {
        this.rankAwardList = rankAwardList;
    }

    public void setBattleReports(List<BattleReport> battleReports) {
        this.battleReports = battleReports;
    }

    public void setHistoryRankAwardItemMaps(Map<Integer, Integer> historyRankAwardItemMaps) {
        this.historyRankAwardItemMaps = historyRankAwardItemMaps;
    }

    public void setSectionRankAwardItemMaps(Map<Integer, Integer> sectionRankAwardItemMaps) {
        this.sectionRankAwardItemMaps = sectionRankAwardItemMaps;
    }

    public void setRemainBuyCount(int remainBuyCount) {
        this.remainBuyCount = remainBuyCount;
    }

    public void setOnRank(int onRank) {
        this.onRank = onRank;
    }

    public void setRoleIdStr(String roleIdStr) {
        this.roleIdStr = roleIdStr;
    }

    public void setRoleNameStr(String roleNameStr) {
        this.roleNameStr = roleNameStr;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public void setFightTimes(int fightTimes) {
        this.fightTimes = fightTimes;
    }
}
