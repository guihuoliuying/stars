package com.stars.modules.familyactivities.war.packet.fight;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFightPersonalStat;
import com.stars.multiserver.familywar.knockout.fight.normal.FamilyWarNormalFightStat;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/22.
 */
public class ClientFamilyWarFightNormalResult extends PlayerPacket {

    private FamilyWarNormalFightStat stat;
    private boolean isWin;
    private int moraleDelta;
    private long points;
    private Map<Integer, Integer> toolMap = new HashMap<>();
    private byte isElite;//0:普通成员，1：精英成员
    private String winText;

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_NORMAL_RESULT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(toString(stat.getWinnerFamilyId()));
        buff.writeString(toString(stat.getLoserFamilyId()));
        buff.writeString(toString(stat.getCamp1FamilyId()));
        buff.writeString(toString(stat.getCamp2FamilyId()));
        buff.writeString(stat.getCamp1FamilyName());
        buff.writeString(stat.getCamp2FamilyName());
        buff.writeString(MultiServerHelper.getServerName(stat.getCamp1ServerId()));
        buff.writeString(MultiServerHelper.getServerName(stat.getCamp2ServerId()));
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        buff.writeString(decimalFormat.format(stat.getCamp1TowerHp() * 100));//阵营1的塔血量百分比
        buff.writeString(decimalFormat.format(stat.getCamp2TowerHp() * 100));//阵营2的塔血量百分比
        LogUtil.info("normal|camp1Tower:{},camp2Tower:{}", decimalFormat.format(stat.getCamp1TowerHp() * 100), decimalFormat.format(stat.getCamp2TowerHp() * 100));
        buff.writeString(winText);
        Map<Long, FamilyWarNormalFightPersonalStat> personalStatMap = stat.getPersonalStatMap();
        buff.writeInt(personalStatMap.size());
        for (FamilyWarNormalFightPersonalStat personalStat : personalStatMap.values()) {
            buff.writeString(toString(personalStat.getFighterId())); // roleId
            buff.writeString(personalStat.getFighterName()); // 名字
            buff.writeByte(personalStat.getCamp()); // 阵营
            buff.writeInt(personalStat.getKillCount()); // 人头数
            buff.writeInt(personalStat.getDeadCount()); // 阵亡数
            buff.writeInt(personalStat.getAssistCount());//助攻数
            buff.writeInt(personalStat.getMaxComboKillCount()); // 连斩数
            buff.writeString(Long.toString(personalStat.getPoints())); // 本场积分
        }
        buff.writeInt(toolMap.size()); // 道具列表大小
        for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
            buff.writeInt(entry.getKey()); // itemId
            buff.writeInt(entry.getValue()); // item count
        }
//        buff.writeByte(isElite);//0:普通成员，1：精英成员
//        buff.writeByte(isWin ? TRUE : FALSE); // 是否胜利，1 - 胜利，0 - 失败
//        buff.writeInt(moraleDelta); // 增加的士气量
//        buff.writeString(Long.toString(points)); // 个人积分
//        LogUtil.info("familywar|normalResult,isElite:{},isWin:{},moraleDelta:{},points:{}", isElite, isWin, moraleDelta, points);
    }

    private String toString(long l) {
        return Long.toString(l);
    }

    public void setIsElite(byte isElite) {
        this.isElite = isElite;
    }

    public void setWin(boolean win) {
        isWin = win;
    }

    public void setMoraleDelta(int moraleDelta) {
        this.moraleDelta = moraleDelta;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public void setToolMap(Map<Integer, Integer> toolMap) {
        this.toolMap = toolMap;
    }

    public void setStat(FamilyWarNormalFightStat stat) {
        this.stat = stat;
    }

    public void setWinText(String winText) {
        this.winText = winText;
    }
}
