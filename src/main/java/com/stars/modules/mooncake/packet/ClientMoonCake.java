package com.stars.modules.mooncake.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.mooncake.MoonCakeManager;
import com.stars.modules.mooncake.MoonCakePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.mooncake.RoleMoonCakeCache;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangerjiang on 2017/9/14.
 */
public class ClientMoonCake extends PlayerPacket {

    public static final byte RES_VIEW = 0x00;       //响应打开界面
    public static final byte RES_FINISH = 0x01;     //响应结束游戏
    public static final byte RES_GETRWD = 0x02;     //获取积分奖励
    public static final byte RES_RANK = 0x03;       //响应排行榜
    public static final byte RES_FINISH_GAME = 0x04;//强行结束游戏

    private byte subtype;

    private int iWeekSingleMaxScore;
    private int iDaySingleMaxScore;
    private int iMoonCakeRank;

    private int isNewRecord;//1：新纪录 or 0：没破纪录
    private int lastPoint;//之前的最高纪录
    private int thisPoint;//本局积分
    private Map<Integer, Integer> targetScoreRwdMap = new HashMap<>();

    private List<RoleMoonCakeCache> cakeCacheList = new LinkedList<>();

    public ClientMoonCake() {

    }

    public ClientMoonCake(byte subtype) {
        this.subtype = subtype;
    }

    public void setWeekSingleMaxScore(int iWeekSingleMaxScore) {
        this.iWeekSingleMaxScore = iWeekSingleMaxScore;
    }

    public void setDaySingleMaxScore(int iDaySingleMaxScore) {
        this.iDaySingleMaxScore = iDaySingleMaxScore;
    }

    public void setMoonCakeRank(int iMoonCakeRank) {
        this.iMoonCakeRank = iMoonCakeRank;
    }

    public void setTargetScoreRwdMap(Map<Integer, Integer> targetScoreRwdMap) {
        this.targetScoreRwdMap = targetScoreRwdMap;
    }

    public void setIsNewRecord(int isNewRecord) {
        this.isNewRecord = isNewRecord;
    }

    public void setThisPoint(int thisPoint) {
        this.thisPoint = thisPoint;
    }

    public void setLastPoint(int lastPoint) {
        this.lastPoint = lastPoint;
    }

    private void writeViewToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(iWeekSingleMaxScore);
        buff.writeInt(iDaySingleMaxScore);
        buff.writeInt(iMoonCakeRank);

        writeGetRwdToBuff(buff);
    }

    private void writeFinishToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(isNewRecord);
        buff.writeInt(lastPoint);
        buff.writeInt(thisPoint);
        com.stars.util.LogUtil.info("isNew:{},last:{},this:{}", isNewRecord, lastPoint, thisPoint);
    }

    private void writeGetRwdToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) targetScoreRwdMap.size());
        com.stars.util.LogUtil.info("size:{}|targetScoreRwdMap:{}", targetScoreRwdMap.size(), targetScoreRwdMap);
        for (Map.Entry<Integer, Integer> entry : targetScoreRwdMap.entrySet()) {
            buff.writeInt((entry.getKey()));
            buff.writeInt(entry.getValue());

            int iItemId = MoonCakeManager.getDayScoreRwdMap(entry.getKey()).getItemId();
            int iItemCount = MoonCakeManager.getDayScoreRwdMap(entry.getKey()).getCount();
            buff.writeInt(iItemId);
            buff.writeInt(iItemCount);
        }
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        com.stars.util.LogUtil.info("ClientMoonCake|subType:{}", subtype);
        switch (subtype) {
            case RES_VIEW:
                writeViewToBuff(buff);
                break;
            case RES_FINISH:
                writeFinishToBuff(buff);
                break;
            case RES_GETRWD:
                writeGetRwdToBuff(buff);
                break;
            case RES_RANK:
                buff.writeByte((byte) cakeCacheList.size());
                com.stars.util.LogUtil.info("cakeCacheList|size:{}", cakeCacheList.size());
                for (RoleMoonCakeCache roleMoonCakeCache : cakeCacheList) {
                    roleMoonCakeCache.writeToBuff(buff);
                    LogUtil.info("RoleMoonCakeCache:{}", roleMoonCakeCache);
                }
            default:
                break;
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return MoonCakePacketSet.C_MOONCAKE;
    }

    public void setCakeCacheList(List<RoleMoonCakeCache> cakeCacheList) {
        this.cakeCacheList = cakeCacheList;
    }
}
