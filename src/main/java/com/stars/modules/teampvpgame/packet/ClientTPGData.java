package com.stars.modules.teampvpgame.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.teampvpgame.TeamPVPGamePacketSet;
import com.stars.multiserver.teamPVPGame.TPGTeam;
import com.stars.multiserver.teamPVPGame.TPGTeamMember;
import com.stars.multiserver.teamPVPGame.TPGUtil;
import com.stars.multiserver.teamPVPGame.helper.TPGGroup;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.session.SessionManager;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by liuyuheng on 2016/12/19.
 */
public class ClientTPGData extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte ALL_DATA = 1;// 打开界面下发
    public static final byte SCORE_MATCH_RESULT = 2;// 积分赛匹配对手
    public static final byte TPG_TEAM_DATA = 3;// 队伍信息

    /* 参数 */
    private byte tpgType;// 类型;1=本服/2=跨服
    private String step;// 活动阶段
    private byte status;// 状态;0=未开始/1=已开始
    private TPGTeam myTeam;// 我的队伍信息
    // 积分赛相关
    private int round = 0;// 比赛已进行场次
    // 小组赛相关
    private Map<Integer, TPGGroup> groupFaceMap;// 小组对阵图
    // 四强赛相关
    private Map<Integer, TPGGroup> quarterFaceMap;// 四强对阵图
    // 队伍数据
    private Set<TPGTeam> tpgTeamSet = new HashSet<>();
    // 积分赛匹配对手
    private int enemyTeamId;
    private List<FighterEntity> scoreEnemys;

    public ClientTPGData() {
    }

    public ClientTPGData(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return TeamPVPGamePacketSet.C_TPG_DATA;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case ALL_DATA:
                buff.writeByte(tpgType);// 类型;1=本服/2=跨服
                buff.writeString(step);// 活动阶段
                buff.writeByte(status);// 状态;0=未开始/1=已开始
                buff.writeByte((byte) (myTeam == null ? 0 : 1));// 是否有队伍(已报名)
                if (myTeam != null) {
                    writeMyTeam(buff);
                }
                buff.writeInt(round);// 积分赛已进行场次
                writeGroupFace(groupFaceMap, buff);
                writeGroupFace(quarterFaceMap, buff);
                writeFaceTeam(buff);
                break;
            case SCORE_MATCH_RESULT:
                buff.writeInt(enemyTeamId);// teamid
                byte size = (byte) (scoreEnemys == null ? 0 : scoreEnemys.size());// 队员数量
                buff.writeByte(size);
                if (size == 0)
                    return;
                for (FighterEntity fighterEntity : scoreEnemys) {
                    buff.writeString(fighterEntity.getName());// 名字
                    buff.writeInt(fighterEntity.getModelId());// 模型Id
                    buff.writeInt(fighterEntity.getFightScore());// 战力
                }
                break;
            case TPG_TEAM_DATA:
                writeMyTeam(buff);
                break;
        }
    }

    // 小组赛对阵图
    private void writeGroupFace(Map<Integer, TPGGroup> map, NewByteBuffer buff) {
        short size = (short) (map == null ? 0 : map.size());
        buff.writeShort(size);
        if (size != 0) {
            for (TPGGroup tpgGroup : map.values()) {
                tpgGroup.writeToBuff(buff);
            }
        }
    }

    // 对阵队伍信息
    private void writeFaceTeam(NewByteBuffer buff) {
        short size = (short) (tpgTeamSet == null ? 0 : tpgTeamSet.size());
        buff.writeShort(size);
        if (tpgTeamSet == null)
            return;
        for (TPGTeam tpgTeam : tpgTeamSet) {
            tpgTeam.writeToBuff(buff);
        }
    }

    // 我的队伍信息
    private void writeMyTeam(NewByteBuffer buff) {
        buff.writeInt(myTeam.getTeamId());// 队伍Id
        buff.writeInt(myTeam.getScore());// 积分赛积分
        buff.writeInt(myTeam.getScoreRanking());// 积分赛排名
        buff.writeByte((byte) myTeam.getMembers().size());// 队员数量
        for (TPGTeamMember member : myTeam.getMembers().values()) {
            member.writeToBuff(buff);
            byte online = 1;// 跨服类型默认状态在线
            if (tpgType == TPGUtil.TPG_LOACAL &&
                    !SessionManager.getSessionMap().containsKey(member.getRoleId())) {
                online = 0;
            }
            buff.writeByte(online);// 是否在线;0=不在线/1=在线
        }
    }

    public void setTpgType(byte tpgType) {
        this.tpgType = tpgType;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public void setMyTeam(TPGTeam myTeam) {
        this.myTeam = myTeam;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void setGroupFaceMap(Map<Integer, TPGGroup> groupFaceMap) {
        this.groupFaceMap = groupFaceMap;
        if (StringUtil.isEmpty(groupFaceMap))
            return;
        for (TPGGroup group : groupFaceMap.values()) {
            tpgTeamSet.addAll(group.getAllTeam());
        }
    }

    public void setQuarterFaceMap(Map<Integer, TPGGroup> quarterFaceMap) {
        this.quarterFaceMap = quarterFaceMap;
        if (StringUtil.isEmpty(quarterFaceMap))
            return;
        for (TPGGroup group : quarterFaceMap.values()) {
            tpgTeamSet.addAll(group.getAllTeam());
        }
    }

    public void setScoreEnemy(int enemyTeamId, List<FighterEntity> scoreEnemyList) {
        this.enemyTeamId = enemyTeamId;
        this.scoreEnemys = scoreEnemyList;
    }
}
