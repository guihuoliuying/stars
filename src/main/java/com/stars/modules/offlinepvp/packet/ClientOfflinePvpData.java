package com.stars.modules.offlinepvp.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.offlinepvp.OfflinePvpManager;
import com.stars.modules.offlinepvp.OfflinePvpPacketSet;
import com.stars.modules.offlinepvp.prodata.OPRewardVo;
import com.stars.modules.offlinepvp.userdata.RoleOfflinePvp;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.offlinepvp.cache.OPEnemyCache;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/10/11.
 */
public class ClientOfflinePvpData extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte ALL = 1;// 下发全部信息(手动/自动刷新对手)
    public static final byte REWARD = 2;// 领取进度奖励(增量下发)
    public static final byte BUY_REFRESH = 3;// 购买刷新次数
    public static final byte BUY_CHALLENGE = 4;// 购买挑战次数
    public static final byte RESET_DAILY = 5;// 每日重置(刷新,挑战,购买刷新,购买挑战次数)
    public static final byte CHALLENGE_NUMBER = 6;// 挑战次数更新
    public static final byte CHALLENGE_WIN = 7;// 挑战胜利更新(战胜对手序号,增量下发)

    /* 参数 */
    private RoleOfflinePvp roleOfflinePvp;
    private Map<Byte, OPEnemyCache> enemyMap;// 挑战对手,<index, OPEnemyCache>
    private byte winIndex;// 已战胜对手序号
    private byte rewardedIndex;// 已领取奖励序号

    public ClientOfflinePvpData() {
    }

    public ClientOfflinePvpData(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return OfflinePvpPacketSet.C_OFFLINEPVP_DATA;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case ALL:// 下发全部信息
                buff.writeByte(roleOfflinePvp.getRefreshedNum());// 已刷新对手次数
                buff.writeInt(roleOfflinePvp.getChallegedNum());// 已挑战次数
                buff.writeByte(roleOfflinePvp.getBuyRefreshNum());// 已购买刷新次数
                buff.writeByte(roleOfflinePvp.getBuyChallengeNum());// 已购买挑战次数
                OPRewardVo rewardVo = OfflinePvpManager.getOPRewardVo(roleOfflinePvp.getStandardLevel());
                rewardVo.writeToBuff(buff);
                writeRewardedIndex(buff);
                writeEnemys(buff);
                break;
            case REWARD:
                buff.writeByte(rewardedIndex);
                break;
            case BUY_REFRESH:
                buff.writeByte(roleOfflinePvp.getRefreshedNum());// 已刷新对手次数
                buff.writeByte(roleOfflinePvp.getBuyRefreshNum());// 已购买刷新次数
                break;
            case BUY_CHALLENGE:
                buff.writeInt(roleOfflinePvp.getChallegedNum());// 已挑战次数
                buff.writeByte(roleOfflinePvp.getBuyChallengeNum());// 已购买挑战次数
                break;
            case RESET_DAILY:
                buff.writeByte(roleOfflinePvp.getRefreshedNum());// 已刷新对手次数
                buff.writeInt(roleOfflinePvp.getChallegedNum());// 已挑战次数
                buff.writeByte(roleOfflinePvp.getBuyRefreshNum());// 已购买刷新次数
                buff.writeByte(roleOfflinePvp.getBuyChallengeNum());// 已购买挑战次数
                break;
            case CHALLENGE_NUMBER:
                buff.writeInt(roleOfflinePvp.getChallegedNum());// 已挑战次数
                break;
            case CHALLENGE_WIN:
                buff.writeByte(winIndex);
                break;
            default:
                break;
        }
    }

    /* 下发已领取奖励次数号 */
    private void writeRewardedIndex(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) roleOfflinePvp.getRewardedIndexSet().size();
        buff.writeByte(size);
        if (size > 0) {
            for (byte index : roleOfflinePvp.getRewardedIndexSet()) {
                buff.writeByte(index);// 已经领取奖励的次数号
            }
        }
    }

    /* 下发对手信息 */
    private void writeEnemys(NewByteBuffer buff) {
        byte size = (byte) (enemyMap == null ? 0 : enemyMap.size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Byte, OPEnemyCache> entry : enemyMap.entrySet()) {
            OPEnemyCache enemyCache = entry.getValue();
            buff.writeByte(entry.getKey());// 序号,从1开始
            buff.writeString(enemyCache.getUniqueId());// id
            buff.writeInt(enemyCache.getModelId());// 模型Id
            buff.writeString(entry.getValue().getName());// 名字
            buff.writeInt(enemyCache.getRoleLevel());// 等级
            buff.writeInt(enemyCache.getFightScore());// 战力
            buff.writeInt(enemyCache.getCurDeityWeapon());// 当前使用神兵
            buff.writeByte((byte) (roleOfflinePvp.getWinIndexSet().contains(entry.getKey()) ? 1 : 0));// 状态,1=已战胜
        }
    }

    public void setRoleOfflinePvp(RoleOfflinePvp roleOfflinePvp) {
        this.roleOfflinePvp = roleOfflinePvp;
    }

    public void setEnemyMap(Map<Byte, OPEnemyCache> enemyMap) {
        this.enemyMap = enemyMap;
    }

    public void setWinIndex(byte winIndex) {
        this.winIndex = winIndex;
    }

    public void setRewardedIndex(byte rewardedIndex) {
        this.rewardedIndex = rewardedIndex;
    }
}
