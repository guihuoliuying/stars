package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;

import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/12/27.
 */
public class ClientFamilyWarUiMinPointsAward extends PlayerPacket {

    public static final byte SUBTYPE_VIEW = 0x00; // 查看积分
    public static final byte SUBTYPE_ACQUIRE = 0x01; // 获得奖励

    public static final byte AWARD_ELITE = 0x00;
    public static final byte AWARD_NORMAL = 0x01;
    public static final byte AWARD_ = 0x00;

    private byte subtype; // 暂定（应该没有用
    private byte awardType; // 0 - 精英，1 - 匹配，2 - 海选
    private long points;
    private Set<Long> recordSet;

    public ClientFamilyWarUiMinPointsAward() {
    }

    public ClientFamilyWarUiMinPointsAward(byte subtype, byte awardType, long points, Set<Long> recordSet) {
        this.subtype = subtype;
        this.awardType = awardType;
        this.points = points;
        this.recordSet = recordSet;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_MIN_POINTS_AWARD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype); // 0 - 查看，1 - 奖励（暂时没有）
        switch (subtype) {
            case SUBTYPE_VIEW:
                buff.writeByte(awardType); // 0 - 精英，1 - 匹配，2 - 海选
                buff.writeString(Long.toString(points)); // 自己积分
                int size = recordSet == null ? 0 : recordSet.size();
                buff.writeInt(size); // 已领取的积分的数量
                if (size > 0) {
                    for (long points : recordSet) {
                        buff.writeString(Long.toString(points)); // 已领取的积分
                    }
                }
                LogUtil.info("familywar查看奖励|awardType:{},points:{},recordSet:{}", awardType, points, recordSet);
                break;
            case SUBTYPE_ACQUIRE:
                buff.writeByte(awardType); // 0 - 精英，1 - 匹配，2 - 海选
                buff.writeString(Long.toString(points)); // 领取奖励的积分
                break;
        }
    }
}
