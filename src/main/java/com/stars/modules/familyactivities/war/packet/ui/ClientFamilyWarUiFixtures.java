package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarManager;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarFamilyInfo;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxQualifyFamilyWarOpponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class ClientFamilyWarUiFixtures extends PlayerPacket {

    public static final byte SUBTYPE_ALL = 0x00;
    public static final byte SUBTYPE_UPDATED = 0x01;
    public static final byte SUBTYPE_QUALIFY_NONE = 0x02;//跨服海选没资格的家族用的
    public static final byte SUBTYPE_TIPS = 0x03;//弹提示

    public static final byte T_LOCAL = 1; // 本服
    public static final byte T_QUALIFY = 2; // 跨服海选
    public static final byte T_REMOTE = 3;//跨服决赛

    public static final byte S_PREPARE = 0; // 状态：没开始
    public static final byte S_SHEET = 1; // 状态：确定名单
    public static final byte S_SHOW_ICON = 2; // 状态：主界面图标显示到精英赛开始
    public static final byte S_ELITE = 3; // 状态：精英赛进行中
    public static final byte S_ELITE_END = 4; //状态：精英赛结束到某一轮结束
    public static final byte S_BETWEEN_ELITE = 5; //状态：两轮比赛之间
    public static final byte S_CYCLE_END = 6;//状态：赛程结束
    public static final byte S_CANAPPLY = 7;//状态: 可报名

    public static final byte Q_NO = 0; // 资格：没有
    public static final byte Q_NORMAL = 1; // 资格：匹配战
    public static final byte Q_ELITE = 2; // 资格：精英战

    private byte subtype;
    private byte warType; //
    private byte warState;
    private byte playerQualification;
    private byte familyQualification;
    private int nextBattleRemainderTime;
    private int date;
    private int indexOfTimeline;
    private List<PktAuxFamilyWarFamilyInfo> familyInfoList = new ArrayList<>();
    private long[] agenda;
    private long _1stFamilyId;
    private long _2ndFamilyId;
    private long _3rdFamilyId;
    private long _4thFamilyId;
    private String text;//

    private List<PktAuxQualifyFamilyWarOpponent> opponentList = new ArrayList<>();
    private Map<Integer, List<PktAuxFamilyWarFamilyInfo>> familyInfoMap = new HashMap<>();
    private String selfFamilyId;
    private String selffamilyName;
    private long selfPoints;
    private int selfRank;
    private int groupId;

    private long selfFamilyFightScore;
    private long familyWarMinFightScore;
    private long deadLine;
    private int startBattleType;
    private int battleType;
    private Map<Integer, long[]> fixtureMap;//对阵表
    private List<String> familyNameList;
    private byte familySize;
    private String tipText;

    public ClientFamilyWarUiFixtures() {
    }

    public ClientFamilyWarUiFixtures(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {
    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_FIXTURES;
    }

    public void addFamilyInfo(long familyId, String familyName, int serverId) {
        familyInfoList.add(new PktAuxFamilyWarFamilyInfo(familyId, familyName, serverId));
    }

    public void addOpponent(PktAuxQualifyFamilyWarOpponent opponent) {
        opponentList.add(opponent);
    }

    public void addFamilyInfo(PktAuxFamilyWarFamilyInfo familyInfo) {
        List<PktAuxFamilyWarFamilyInfo> infoList = familyInfoMap.get(familyInfo.getGroupId());
        if (infoList == null) {
            infoList = new ArrayList<>();
            familyInfoMap.put(familyInfo.getGroupId(), infoList);
        }
        infoList.add(familyInfo);
    }

    public void setSelfFamilyGroupId(int groupId) {
        this.groupId = groupId;
    }

    public void setSelfFamilyId(String selfFamilyId) {
        this.selfFamilyId = selfFamilyId;
    }

    public void setSelffamilyName(String selffamilyName) {
        this.selffamilyName = selffamilyName;
    }

    public void setSelfPoints(long selfPoints) {
        this.selfPoints = selfPoints;
    }

    public void setSelfRank(int selfRank) {
        this.selfRank = selfRank;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWarType(byte warType) {
        this.warType = warType;
    }

    public void setWarState(byte warState) {
        this.warState = warState;
    }

    public byte getWarState() {
        return warState;
    }

    public void setPlayerQualification(byte playerQualification) {
        this.playerQualification = playerQualification;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setIndexOfTimeline(int indexOfTimeline) {
        this.indexOfTimeline = indexOfTimeline;
    }

    public void setAgenda(long[] agenda) {
        this.agenda = agenda;
    }

    public void set1stFamilyId(long _1stFamilyId) {
        this._1stFamilyId = _1stFamilyId;
    }

    public void set2ndFamilyId(long _2ndFamilyId) {
        this._2ndFamilyId = _2ndFamilyId;
    }

    public void set3rdFamilyId(long _3rdFamilyId) {
        this._3rdFamilyId = _3rdFamilyId;
    }

    public void set4thFamilyId(long _4thFamilyId) {
        this._4thFamilyId = _4thFamilyId;
    }

    public void setSubtype(byte subtype) {
        this.subtype = subtype;
    }

    public void setSelfFamilyFightScore(long selfFamilyFightScore) {
        this.selfFamilyFightScore = selfFamilyFightScore;
    }

    public void setFamilyWarMinFightScore(long familyWarMinFightScore) {
        this.familyWarMinFightScore = familyWarMinFightScore;
    }

    public void setDeadLine(long deadLine) {
        this.deadLine = deadLine;
    }

    public void setStartBattleType(int startBattleType) {
        this.startBattleType = startBattleType;
    }

    public void setBattleType(int battleType) {
        this.battleType = battleType;
    }

    public void setFixtureMap(Map<Integer, long[]> fixtureMap) {
        this.fixtureMap = fixtureMap;
    }

    public void setFamilyNameList(List<String> familyNameList) {
        this.familyNameList = familyNameList;
    }

    public void setFamilySize(byte familySize) {
        this.familySize = familySize;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        buff.writeByte(warType); // 0 - 本服，1 - 跨服海选, 2 - 跨服决赛
        switch (subtype) {
            case SUBTYPE_ALL:
                writeAll(buff);
                break;
            case SUBTYPE_UPDATED:
                writeUpdated(buff);
                break;
            case SUBTYPE_QUALIFY_NONE:
                buff.writeLong(familyWarMinFightScore);//参与家族战最小的战力
                buff.writeLong(selfFamilyFightScore);//家族战力
                buff.writeLong(deadLine);//取数据的时间
                buff.writeByte(familySize);
                for (String name : familyNameList) {
                    buff.writeString(name);
                }
                LogUtil.info("familywar|无资格家族 minFightScore:{},myFamilyFightScore:{},deadLine:{},familyList:{}",
                        familyWarMinFightScore, selfFamilyFightScore, deadLine, familyNameList);
                break;
            case SUBTYPE_TIPS:
                buff.writeString(tipText);
        }
    }

    private void writeAll(NewByteBuffer buff) {
        if (warType == ClientFamilyWarUiFixtures.T_LOCAL) {
            writeTimelineOfLocal(buff);
            writeAllFamilyInfo(buff);
            writeWarState(buff);
            writeFixtures(buff);
            writeRank(buff);
        } else if (warType == ClientFamilyWarUiFixtures.T_QUALIFY) {
            buff.writeString(selfFamilyId);
            buff.writeString(selffamilyName);
            buff.writeLong(selfPoints);
            buff.writeInt(selfRank);
            writeAllOpponent(buff);
            writeWarState(buff);
            LogUtil.info("familywar|发送对方数据");
        } else if (warType == ClientFamilyWarUiFixtures.T_REMOTE) {
            buff.writeInt(groupId);//本家族所在的组 ， 没有的话发0
            buff.writeInt(startBattleType);
            buff.writeInt(battleType);//当前的赛程 初始-0 , 32强-32 ， 16强-16 ， 8强-8 ， 4强 - 4 ， 决赛 - 1 ， 结束-  -1
            writeTimeListOfRemote(buff, startBattleType);
            writeAllFamilyOpp(buff);
            writeWarState(buff);
            writeFixture(buff);
            writeRank(buff);
        }
    }

    private void writeFixture(NewByteBuffer buff) {
        buff.writeInt(fixtureMap == null ? 1 : fixtureMap.size());
        for (Map.Entry<Integer, long[]> entry : fixtureMap.entrySet()) {
            buff.writeInt(entry.getKey());//groupId
            int size = entry.getValue() == null ? 0 : entry.getValue().length;
            buff.writeInt(size);
            for (long familyId : entry.getValue()) {
                buff.writeString(Long.toString(familyId));
            }
            LogUtil.info("familywar|决赛 {} 赛程表 groupId:{} ,fixtureMap:{}", battleType, entry.getKey(), entry.getValue());
        }
    }

    private void writeUpdated(NewByteBuffer buff) {
        writeWarState(buff);
        writeFixtures(buff);
        writeRank(buff);
    }


    private void writeWarState(NewByteBuffer buff) {
        buff.writeByte(warState); // 0 - 未开始，1 - 进行中，2 - 已结束
        buff.writeByte(playerQualification); // 0 - 没资格，1 - 匹配战，3 - 精英战
        buff.writeByte(familyQualification); //0 - 没资格， 1 - 有资格
        buff.writeInt(date); // 日期
        buff.writeInt(indexOfTimeline); // 时间线的下标
        buff.writeInt(nextBattleRemainderTime);//下一场的倒计时
        buff.writeString(text);//按钮下面的文本
        LogUtil.info("familywar|warType:{},warState:{},playerQualification:{},familyQualification:{},date:{},indexOfTimeline:{},nextBattleRemainderTime:{},text:{}",
                warType, warState, playerQualification, familyQualification, date, indexOfTimeline, nextBattleRemainderTime, text);
    }

    private void writeTimelineOfLocal(NewByteBuffer buff) {
        List<Integer> timeline = FamilyActWarManager.timelineOfKnockout;
        buff.writeInt(timeline.size());
        for (int timepoint : timeline) {
            buff.writeInt(timepoint);
        }
        LogUtil.info("familywar|timeline:{}", timeline);
    }

    private void writeTimeListOfRemote(NewByteBuffer buff, int startBattleType) {
        Map<Integer, List<Integer>> timeMap = FamilyActWarManager.timeLineOfRemote;
        List<Integer> timeLine = timeMap.get(startBattleType);
        buff.writeInt(timeLine.size());
        for (int timePoint : timeLine) {
            buff.writeInt(timePoint);
        }
        LogUtil.info("familywar|赛程表时间点:{}", timeLine);
    }

    private void writeAllFamilyOpp(NewByteBuffer buff) {
        buff.writeInt(familyInfoMap.size());
        for (Map.Entry<Integer, List<PktAuxFamilyWarFamilyInfo>> entry : familyInfoMap.entrySet()) {
            buff.writeInt(entry.getKey());//当前组的 groupId
            buff.writeInt(entry.getValue().size());//当前组的家族个数
            for (PktAuxFamilyWarFamilyInfo info : entry.getValue()) {
                writeFamilyOpp(info, buff);
            }
        }
    }

    private void writeFamilyOpp(PktAuxFamilyWarFamilyInfo info, NewByteBuffer buff) {
        buff.writeInt(info.getSeq());
        buff.writeString(Long.toString(info.getFamilyId()));
        buff.writeString(info.getFamilyName());
        buff.writeInt(info.getServerId());
        buff.writeString(info.getServerName());
        LogUtil.info("familywar|跨服决赛 , 对手数据  序列:{},familyId:{},familyName:{},serverId:{},serverName:{}",
                info.getSeq(), info.getFamilyId(), info.getFamilyName(), info.getServerId(), info.getServerName());
    }

    private void writeAllOpponent(NewByteBuffer buff) {
        buff.writeInt(opponentList.size());
        LogUtil.info("familywar|跨服海选，对手数量:{}", opponentList.size());
        for (PktAuxQualifyFamilyWarOpponent opponent : opponentList) {
            writeOpponent(opponent, buff);
        }
    }

    private void writeOpponent(PktAuxQualifyFamilyWarOpponent opponent, NewByteBuffer buff) {
        buff.writeInt(opponent.getBattleType());
        buff.writeString(opponent.getServerName());
        buff.writeString(Long.toString(opponent.getFamilyId()));
        buff.writeString(opponent.getFamilyName());
        buff.writeLong(opponent.getTime());
        buff.writeByte(opponent.getWinOrLose());
        LogUtil.info("familywar|跨服海选，对手数据 battleType:{},serverName:{},familyId:{},familyName:{},time:{},winOrlose:{}"
                , opponent.getBattleType(), opponent.getServerName(), opponent.getFamilyId(), opponent.getFamilyName(), opponent.getTime(), opponent.getWinOrLose());
    }

    private void writeAllFamilyInfo(NewByteBuffer buff) {
        buff.writeInt(familyInfoList.size()); // 家族信息个数
        for (PktAuxFamilyWarFamilyInfo familyInfo : familyInfoList) {
            writeFamilyInfo(familyInfo, buff);
        }
    }

    private void writeFamilyInfo(PktAuxFamilyWarFamilyInfo familyInfo, NewByteBuffer buff) {
        buff.writeString(Long.toString(familyInfo.familyId)); // familyId
        buff.writeString(familyInfo.familyName); // familyName
        buff.writeInt(familyInfo.serverId); // serverId
    }

    private void writeFixtures(NewByteBuffer buff) {
        int size = agenda == null ? 0 : agenda.length;
        buff.writeInt(size);
        if (size > 0) {
            for (long familyId : agenda) {
                buff.writeString(Long.toString(familyId));
            }
        }
    }

    private void writeRank(NewByteBuffer buff) {
        buff.writeString(Long.toString(_1stFamilyId)); // 1st familyId
        buff.writeString(Long.toString(_2ndFamilyId)); // 2nd familyId
        buff.writeString(Long.toString(_3rdFamilyId)); // 3rd familyId
        buff.writeString(Long.toString(_4thFamilyId)); // 4th familyId
    }

    public int getNextBattleRemainderTime() {
        return nextBattleRemainderTime;
    }

    public void setNextBattleRemainderTime(int nextBattleRemainderTime) {
        this.nextBattleRemainderTime = nextBattleRemainderTime;
    }

    public byte getFamilyQualification() {
        return familyQualification;
    }

    public void setFamilyQualification(byte familyQualification) {
        this.familyQualification = familyQualification;
    }

}

