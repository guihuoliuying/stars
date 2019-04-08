package com.stars.modules.familyactivities.war.packet.fight;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightPersonalStat;
import com.stars.multiserver.familywar.knockout.fight.elite.FamilyWarEliteFightStat;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/12/12.
 */
public class ClientFamilyWarBattleFightEliteResult extends PlayerPacket {

    private FamilyWarEliteFightStat stat;
    private Map<Integer, Integer> toolMap; // 奖励列表
    private String text;
    private String winText;
    private String camp1ServerName;
    private String camp2ServerName;

    public ClientFamilyWarBattleFightEliteResult() {
    }

    public ClientFamilyWarBattleFightEliteResult(FamilyWarEliteFightStat stat, Map<Integer, Integer> toolMap) {
        this.stat = stat;
        this.toolMap = toolMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_BATTLE_FIGHT_ELITE_RESULT;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(toString(stat.getWinnerFamilyId())); // 胜利的家族id
        buff.writeString(toString(stat.getLoserFamilyId())); // 失败的家族id
        buff.writeString(toString(stat.getCamp1FamilyId())); // 阵营1的家族id
        buff.writeString(toString(stat.getCamp2FamilyId())); // 阵营2的家族id
        buff.writeString(stat.getCamp1FamilyName()); // 阵营1的家族名字
        buff.writeString(stat.getCamp2FamilyName()); // 阵营2的家族名字
        buff.writeString(camp1ServerName);
        buff.writeString(camp2ServerName);
        buff.writeInt(stat.getCamp1Morale()); // 阵营1的士气
        buff.writeInt(stat.getCamp2Morale()); // 阵营2的士气
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        buff.writeString(decimalFormat.format(stat.getCamp1TowerHp() * 100));//阵营1的塔血量百分比
        buff.writeString(decimalFormat.format(stat.getCamp2TowerHp() * 100));//阵营2的塔血量百分比
        LogUtil.info("elite|camp1Tower:{},camp2Tower:{}", decimalFormat.format(stat.getCamp1TowerHp() * 100), decimalFormat.format(stat.getCamp2TowerHp() * 100));
        buff.writeString(winText);

        Map<Long, FamilyWarEliteFightPersonalStat> personalStatMap = stat.getPersonalStatMap();
        buff.writeInt(personalStatMap.size()); // 个人统计数据
        for (FamilyWarEliteFightPersonalStat personalStat : personalStatMap.values()) {
            buff.writeString(toString(personalStat.getFighterId())); // roleId
            buff.writeString(personalStat.getFighterName()); // 名字
            buff.writeByte(personalStat.getCamp()); // 阵营
            buff.writeInt(personalStat.getKillCount()); // 人头数
            buff.writeInt(personalStat.getDeadCount()); // 阵亡数
            buff.writeInt(personalStat.getAssistCount());//助攻数
            buff.writeInt(personalStat.getMaxComboKillCount()); // 连斩数
            buff.writeString(Long.toString(personalStat.getPoints())); // 本场积分
        }
        if (toolMap == null) {
            buff.writeInt(0);
        } else {
            buff.writeInt(toolMap.size()); // 奖励大小
            for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
                buff.writeInt(entry.getKey()); // toolId
                buff.writeInt(entry.getValue()); // num
            }
        }
        buff.writeString(text);
        LogUtil.info("familywar|精英赛结算文本:{},胜利结算文本:{}", text, winText);
    }

    private String toString(long l) {
        return Long.toString(l);
    }

    public FamilyWarEliteFightStat getStat() {
        return stat;
    }

    public void setStat(FamilyWarEliteFightStat stat) {
        this.stat = stat;
    }

    public Map<Integer, Integer> getToolMap() {
        return toolMap;
    }

    public void setToolMap(Map<Integer, Integer> toolMap) {
        this.toolMap = toolMap;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setWinText(String winText) {
        this.winText = winText;
    }

    public void setCamp1ServerName(String camp1ServerName) {
        this.camp1ServerName = camp1ServerName;
    }

    public void setCamp2ServerName(String camp2ServerName) {
        this.camp2ServerName = camp2ServerName;
    }
}
