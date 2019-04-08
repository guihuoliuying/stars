package com.stars.modules.scene.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.escort.EscortConstant;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.ScenePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/7.
 */
public class ClientStageFinish extends PlayerPacket {
    public final static byte VICT = 2;
    public final static byte LOSE = 0;

    public byte stageType;
    private byte status;// 0失败/2胜利
    private byte star;// 星星数
    private int useTime;// 使用时间
    private Map<Integer, Integer> itemMap = new HashMap<>();

    //离线竞技场扩展下发
    private int myRank;
    private int updateRank;

    //家族探宝扩展下发
    private long ftdamage;
    private long weekdamage;
    private Map<Integer, Integer> killItemMap;//itemId,--count  致命一击奖励

    // 特定副本扩展下发
    private int damage;// 角色对boss造成伤害值;stageType=5召唤BOSS副本使用

    // 斗神殿扩展下发
    private int curDisScore;
    private int changeScore;

    // 运镖拓展
    private byte robTimes;   //被劫镖次数
    private byte losePercent;//损失百分比
    private byte doubleAward;//奖励翻倍

    // 组队pvp扩展
    private boolean isScoreStep = Boolean.FALSE;// 是否积分赛
    // 积分赛一场战斗结束,胜负方积分信息
    private Integer[] winnerScoreInfo;// [teamId, 胜(负)积分, 击杀积分, 连胜积分, 总积分]
    private Integer[] loserScoreInfo;

    //夺宝
    private int boxCount;

    // 日常组队副本
    // 关系加成奖励,1=好友,2=夫妻,3=家族,没有为空串,格式:1,2,3
    private String addRewardType;

    //是否属于劫镖
    private byte isHasCar;

    //精英副本首通奖励
    private Map<Integer, Integer> eliteFirstReward = new HashMap<>();

    //结婚组队
    private int mySocre;
    private int marryScore;
    private Map<Integer, Integer> extraItemMap = new HashMap<>();

    //挑战女神扩展
    private long damageForDareGod;

    /**
     * 秦楚大作战
     *
     * @param stageType
     * @param status
     */
    private String name;//击杀者名称

    public ClientStageFinish(byte stageType, byte status) {
        this.stageType = stageType;
        this.status = status;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return ScenePacketSet.C_STAGEFINISH;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (this.stageType == SceneManager.SCENETYPE_ELITEDUNGEON) {
            writeElietDungeons(buff);
        } else {
            buff.writeByte(this.stageType);
            buff.writeByte(status);
            buff.writeByte(star);
            buff.writeInt(useTime);
            short size = (short) (itemMap == null ? 0 : itemMap.size());
            buff.writeShort(size);
            if (itemMap != null) {
                for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
            }
            writeExtra(buff);
        }
    }

    public void writeExtra(com.stars.network.server.buffer.NewByteBuffer buff) {
        switch (stageType) {
            case SceneManager.SCENETYPE_CALLBOSS:
                buff.writeInt(damage);
                break;
            case SceneManager.SCENETYPE_FIGHTINGMASTER:
                buff.writeInt(curDisScore);
                buff.writeInt(changeScore);
                break;
            case SceneManager.SCENETYPE_ESCORT_FIGHT:
                buff.writeByte(doubleAward);
                if (status == EscortConstant.RESULT_ESCORT_FAIL) {
                    buff.writeByte(losePercent);
                } else if (status == EscortConstant.RESULT_ESCORT_FINISH) {
                    buff.writeByte(robTimes);
                    buff.writeByte(losePercent);
                }
                break;
            case SceneManager.SCENETYPE_TPG:
                buff.writeByte((byte) (isScoreStep ? 1 : 0));// 1=积分赛/0=非积分赛
                if (winnerScoreInfo != null) {
                    writeTPGScoreInfo(buff, winnerScoreInfo);
                }
                if (loserScoreInfo != null) {
                    writeTPGScoreInfo(buff, loserScoreInfo);
                }
                break;
            case SceneManager.SCENETYPE_LOOTTREASURE_PVE:
                buff.writeInt(boxCount);
                break;
            case SceneManager.SCENETYPE_LOOTTREASURE_PVP:
                break;
            case SceneManager.SCENETYPE_TEAMDUNGEON:
                buff.writeString(addRewardType);
                break;
            case SceneManager.SCENETYPE_FAMILY_TREASURE:
                buff.writeLong(ftdamage);
                buff.writeLong(weekdamage);
                if (killItemMap != null) {
                    buff.writeByte((byte) killItemMap.size());
                    for (Map.Entry<Integer, Integer> entry : killItemMap.entrySet()) {
                        buff.writeInt(entry.getKey());
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
            case SceneManager.SCENETYPE_NEWOFFLINEPVP:
                buff.writeInt(myRank);
                buff.writeInt(updateRank);
                break;
            case SceneManager.SCENETYPE_FAMILY_ESCORT_PVP_SCENE:
                buff.writeByte(isHasCar);
                break;
            case SceneManager.SCENETYPE_MARRY_DUNGEON:
                buff.writeInt(mySocre);
                buff.writeInt(marryScore);
                buff.writeByte((byte) extraItemMap.size());//额外奖励
                for (Map.Entry<Integer, Integer> entry : extraItemMap.entrySet()) {
                    buff.writeInt(entry.getKey());//itemId
                    buff.writeInt(entry.getValue());//count
                }
                com.stars.util.LogUtil.info("myScore:{},marryScore:{},extraItemMap:{}", mySocre, marryScore, extraItemMap);
                break;
            case SceneManager.SCENETYPE_CAMP_FIGHT: {
                buff.writeInt(mySocre);
                buff.writeString(name);
                break;
            }
            case SceneManager.SCENETYPE_DARE_GOD:
                buff.writeLong(damageForDareGod);
                break;
            default:
                break;
        }
    }

    private void writeTPGScoreInfo(com.stars.network.server.buffer.NewByteBuffer buff, Integer[] scoreInfo) {
        buff.writeInt(scoreInfo[0]);// teamId
        buff.writeInt(scoreInfo[1]);// 胜(负)积分
        buff.writeInt(scoreInfo[2]);// 击杀积分
        buff.writeInt(scoreInfo[3]);// 连胜积分
        buff.writeInt(scoreInfo[4]);// 总积分
    }

    public void writeElietDungeons(NewByteBuffer buff) {
        buff.writeByte(this.stageType);
        buff.writeByte(status);
        buff.writeByte(star);
        buff.writeInt(useTime);
        com.stars.util.LogUtil.info("结算信息--stageType:{}--status:{}--star:{}--useTime:{}", stageType, status, star, useTime);
        short size1 = (short) (itemMap == null ? 0 : itemMap.size());
        short size2 = (short) (eliteFirstReward == null ? 0 : eliteFirstReward.size());
        short size = (short) (size1 + size2);
        buff.writeShort(size);

        if (eliteFirstReward != null) {
            for (Map.Entry<Integer, Integer> entry : eliteFirstReward.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
                buff.writeByte((byte) 1);
                com.stars.util.LogUtil.info("首通奖励:itemId:{},count:{}", entry.getKey(), entry.getValue());
            }
        }

        if (itemMap != null) {
            for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                buff.writeInt(entry.getKey());
                buff.writeInt(entry.getValue());
                buff.writeByte((byte) 0);
                LogUtil.info("普通奖励:itemId:{},count:{}", entry.getKey(), entry.getValue());
            }
        }
    }

    public void setMyRank(int myRank) {
        this.myRank = myRank;
    }

    public void setUpdateRank(int updateRank) {
        this.updateRank = updateRank;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public void setStar(byte star) {
        this.star = star;
    }

    public void setUseTime(int useTime) {
        this.useTime = useTime;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public void setCurDisScore(int curDisScore) {
        this.curDisScore = curDisScore;
    }

    public void setChangeScore(int changeScore) {
        this.changeScore = changeScore;
    }

    public void setRobTimes(byte robTimes) {
        this.robTimes = robTimes;
    }

    public void setLosePercent(byte losePercent) {
        this.losePercent = losePercent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTPGScoreInfo(Integer[] winner, Integer[] loser) {
        this.winnerScoreInfo = winner;
        this.loserScoreInfo = loser;
        if (winnerScoreInfo != null || loserScoreInfo != null) {
            isScoreStep = Boolean.TRUE;
        }
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public void setDoubleAward(byte doubleAward) {
        this.doubleAward = doubleAward;
    }

    public void setBoxCount(int boxCount) {
        this.boxCount = boxCount;
    }

    public void setAddRewardType(String addRewardType) {
        this.addRewardType = addRewardType;
    }

    public void setFtdamage(long ftdamage) {
        this.ftdamage = ftdamage;
    }

    public void setWeekdamage(long weekdamage) {
        this.weekdamage = weekdamage;
    }

    public void setKillItemMap(Map<Integer, Integer> killItemMap) {
        this.killItemMap = killItemMap;
    }

    public void setEliteFirstReward(Map<Integer, Integer> eliteFirstReward) {
        this.eliteFirstReward = eliteFirstReward;
    }

    public byte getIsHasCar() {
        return isHasCar;
    }

    public void setIsHasCar(byte isHasCar) {
        this.isHasCar = isHasCar;
    }

    public void setMySocre(int mySocre) {
        this.mySocre = mySocre;
    }

    public void setMarryScore(int marryScore) {
        this.marryScore = marryScore;
    }

    public void setExtraItemMap(Map<Integer, Integer> extraItemMap) {
        this.extraItemMap = extraItemMap;
    }

    public void setDamageForDareGod(long damageForDareGod) {
        this.damageForDareGod = damageForDareGod;
    }
}
