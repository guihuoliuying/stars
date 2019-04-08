package com.stars.modules.familyactivities.war.packet.ui;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.familyactivities.war.FamilyActWarPacketSet;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarApplicant;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/12/15.
 */
public class ClientFamilyWarUiApply extends PlayerPacket {

    public static final byte SUBTYPE_INFO = 0x00; // 请求报名/参战名单
    public static final byte SUBTYPE_APPLY = 0x01; // 报名
    public static final byte SUBTYPE_APPLY_CANCEL = 0x01; // 取消报名
    public static final byte SUBTYPE_CONFIRM = 0x02; // 确认名单

    private byte subtype;
    private byte selfQualification;
    private byte lock;
    private List<PktAuxFamilyWarApplicant> applicantList = new ArrayList<>(10);

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyActWarPacketSet.C_FAMILY_WAR_UI_APPLY;
    }

    public void setSelfQualification(byte selfQualification) {
        this.selfQualification = selfQualification;
    }

    public void addApplicant(long roleId, String name, byte postId, int level, int fightScore, int elapseFromOffline, boolean isOnline, byte qualification) {
        applicantList.add(new PktAuxFamilyWarApplicant(
                roleId, name, postId, level, fightScore, elapseFromOffline, isOnline, qualification));
    }

    public void addApplicant(PktAuxFamilyWarApplicant applicant) {
        applicantList.add(applicant);
    }

    public void setLock(byte lock) {
        this.lock = lock;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_INFO:
                writeInfo(buff);
                break;
        }
    }

    private void writeInfo(NewByteBuffer buff) {
        buff.writeByte(selfQualification); // 报名状态；0 - 族长，1 - 未报名，2 - 已报名
        buff.writeByte(lock); // 是否锁定；0 - 不锁定，1 - 锁定
        buff.writeInt(applicantList.size()); // 报名列表大小
        for (PktAuxFamilyWarApplicant applicant : applicantList) {
            buff.writeString(Long.toString(applicant.getRoleId())); // roleId
            buff.writeString(applicant.getName()); // 名字
            buff.writeByte(applicant.getPostId()); // 职位id
            buff.writeInt(applicant.getLevel()); // 等级
            buff.writeInt(applicant.getFightScore()); // 战力
            buff.writeInt(applicant.getElapseFromOffline()); // 离线时间（0为在线，单位秒）
            buff.writeByte((byte) (applicant.isOnline() ? 1 : 0));//是否在线，0为离线,1为在线
            buff.writeByte(applicant.getQualification()); // 资格：0 - 不参赛（匹配战），1 - 参赛（精英战）
        }
    }
}


