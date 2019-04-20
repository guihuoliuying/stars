package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class ServerFamilyManagement extends PlayerPacket {

    /* 信息 */
    public static final byte SUBTYPE_INFO = 0x00; // 家族信息
//    public static final byte SUBTYPE_OPTIONS = 0x01; // 家族设置更改
//    public static final byte SUBTYPE_MONEY = 0x02; // 家族资金
//    public static final byte SUBTYPE_LOCK = 0x03; // 家族锁定状态
    /* 创建/解散 */
    public static final byte SUBTYPE_CREATE = 0x04; // 创建家族
    public static final byte SUBTYPE_DISSOLVE = 0x05; // 解散家族
    public static final byte SUBTYPE_EDIT_NOTICE = 0x06; // 更新家族公告
    public static final byte SUBTYPE_SEARCH = 0x07; // 搜索家族
    /* 成员列表 */
    public static final byte SUBTYPE_MEMBER_LIST = 0x08; // 成员列表
//    public static final byte SUBTYPE_MEMBER_ADD = 0x09; // 成员新增
//    public static final byte SUBTYPE_MEMBER_DEL = 0x0A; // 成员删除
    /* 申请列表 */
    public static final byte SUBTYPE_APPLICATION_LIST = 0x0C; // 申请列表
    /* 申请/审核 */
    public static final byte SUBTYPE_APPLY = 0x10; // 申请加入
    public static final byte SUBTYPE_APPLICATION_VERIFY = 0x11; // 审核（同意/拒绝/同意全部/拒绝全部）
    public static final byte SUBTYPE_APPLICATION_CANCEL = 0x12; // 取消申请
    public static final byte SUBTYPE_APPLY_ALL = 0x13; // 一键申请
    /* 邀请 */
    public static final byte SUBTYPE_INVITE = 0x14; // 邀请
    public static final byte SUBTYPE_INVITATION_ACCEPT = 0x15; // 同意邀请
    public static final byte SUBTYPE_INVITATION_REFUSE = 0x16; // 拒绝邀请
    /* 挖人 */
    public static final byte SUBTYPE_POACH = 0x18; // 挖人
    public static final byte SUBTYPE_POACHING_ACCEPT = 0x19; // 同意挖人
    public static final byte SUBTYPE_POACHING_REFUSE = 0x1A; // 拒绝挖人
    /* 退出/踢出 */
    public static final byte SUBTYPE_LEAVE = 0x1C; // 退出家族
    public static final byte SUBTYPE_KICK_OUT = 0x1D; // 踢出家族

    /* 家族邮件 */
    public static final byte SUBTYPE_EMAIL = 0x20;

    /* 任命/禅让 */
    public static final byte SUBTYPE_APPOINT = 0x30; // 任命职位
    public static final byte SUBTYPE_ABDICATE = 0x31; // 禅让族长
    /* 升级 */
    public static final byte SUBTYPE_UPGRADE_INFO = 0x40; // 升级信息
    public static final byte SUBTYPE_UPGRADE = 0x41; // 升级
    /* 成员列表(扩展) */
//    public static final byte SUBTYPE_MEMBER_ONLINE = 0x60; // 成员上线
//    public static final byte SUBTYPE_MEMBER_OFFLINE = 0x61; // 成员下线
//    public static final byte SUBTYPE_MEMBER_POST_CHANGED = 0x62; // 成员职位变动
    /* 设置 */
    public static final byte SUBTYPE_SET_APP_ALLOWANCE = 0x70; // 设置是否允许申请
    public static final byte SUBTYPE_SET_APP_QUALIFICATION = 0x71; // 设置申请条件



    private byte subtype;
    private long familyId;
    private String familyName;
    private String familyNamePattern;
    private String familyNotice;
    private boolean isApproved; // 是否同意审核
    private long applicantId;
    private List<Long> applicantIdList;
    private long memberId;
    private long inviteeId;
    private byte postId;
    private byte donateType;
    private boolean isAllowedApplication;
    private int minLevelQualifaction;
    private int minFightScoreQualifaction;
    private boolean isAutoVerification;
    private String text;

    @Override
    public void execPacket(Player player) {
        ForeShowModule open = module(MConst.ForeShow);
        if (!open.isOpen(ForeShowConst.FAMILY)){
            PlayerUtil.send(player.id(),new ClientText("您的等级过低，无法操作"));
            return;
        }
        FamilyModule familyModule = (FamilyModule) module(MConst.Family);
        switch (subtype) {
            case SUBTYPE_INFO:
                familyModule.sendFamilyInfo();
                break;
            case SUBTYPE_CREATE:
                familyModule.createFamily(familyName, familyNotice);
                break;
            case SUBTYPE_DISSOLVE:
                familyModule.dissolve();
                break;
            case SUBTYPE_EDIT_NOTICE:
                familyModule.editNotice(familyNotice);
                break;
            case SUBTYPE_MEMBER_LIST:
                familyModule.sendMemberList();
                break;
            case SUBTYPE_APPLICATION_LIST:
                familyModule.sendApplicationList();
                break;
            case SUBTYPE_APPLY:
                familyModule.apply(familyId);
                break;
            case SUBTYPE_APPLICATION_VERIFY:
                familyModule.verify(applicantIdList, isApproved);
                break;
            case SUBTYPE_APPLICATION_CANCEL:
                familyModule.cancel(familyId);
                break;
            case SUBTYPE_APPLY_ALL:
                familyModule.applyAll();
                break;
            case SUBTYPE_SEARCH:
                familyModule.search(familyNamePattern);
                break;
            case SUBTYPE_INVITE:
                familyModule.invite(inviteeId);
                break;
            case SUBTYPE_INVITATION_ACCEPT:
                familyModule.acceptInvitation(familyId);
                break;
            case SUBTYPE_INVITATION_REFUSE:
                familyModule.refuseInvitation(familyId);
                break;
            case SUBTYPE_POACH:
//                familyModule.poach(inviteeId);
                break;
            case SUBTYPE_POACHING_ACCEPT:
                familyModule.acceptPoaching(familyId, familyModule.id()); // 邀请的家族id
                break;
            case SUBTYPE_POACHING_REFUSE:
                familyModule.refusePoaching(familyId, familyModule.id());
                break;
            case SUBTYPE_LEAVE:
                familyModule.leave();
                break;
            case SUBTYPE_KICK_OUT:
                familyModule.kickOut(memberId);
                break;
            case SUBTYPE_APPOINT:
                familyModule.appoint(memberId, postId);
                break;
            case SUBTYPE_ABDICATE:
                familyModule.abdicate(memberId);
                break;
            case SUBTYPE_UPGRADE_INFO:
                familyModule.sendUpgradeInfo();
                break;
            case SUBTYPE_UPGRADE:
                familyModule.upgrade();
                break;
            case SUBTYPE_SET_APP_ALLOWANCE:
                familyModule.setAppAllowance(isAllowedApplication);
                break;
            case SUBTYPE_SET_APP_QUALIFICATION:
                familyModule.setAppQualification(minLevelQualifaction, minFightScoreQualifaction, isAutoVerification);
                break;
            case SUBTYPE_EMAIL:
                familyModule.sendEmailToMember(text);
                break;
        }
    }

    @Override
    public short getType() {
        return FamilyPacketSet.S_MANAGEMENT;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        int listSize = 0;
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_CREATE:
                familyName = buff.readString();
                familyNotice = buff.readString();
                break;
            case SUBTYPE_EDIT_NOTICE:
                familyNotice = buff.readString();
                break;
            case SUBTYPE_APPLY:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_APPLICATION_VERIFY:
                isApproved = buff.readByte() == 1 ? true : false;
                listSize = buff.readShort();
                applicantIdList = new ArrayList<>();
                for (int i = 0; i < listSize; i++) {
                    applicantIdList.add(Long.parseLong(buff.readString()));
                }
                break;
            case SUBTYPE_APPLICATION_CANCEL:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_SEARCH:
                familyNamePattern = buff.readString();
                break;
            case SUBTYPE_INVITE:
                inviteeId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_INVITATION_ACCEPT:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_INVITATION_REFUSE:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_POACH:
                inviteeId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_POACHING_ACCEPT:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_POACHING_REFUSE:
                familyId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_LEAVE:
                break;
            case SUBTYPE_KICK_OUT:
                memberId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_APPOINT:
                memberId = Long.parseLong(buff.readString());
                postId = buff.readByte();
                break;
            case SUBTYPE_ABDICATE:
                memberId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_SET_APP_ALLOWANCE:
                isAllowedApplication = buff.readByte() == 1 ? true : false;
                break;
            case SUBTYPE_SET_APP_QUALIFICATION:
                minLevelQualifaction = buff.readInt();
                minFightScoreQualifaction = buff.readInt();
                isAutoVerification = buff.readByte() == 1 ? true : false;
                break;
            case SUBTYPE_EMAIL:
                text = buff.readString();
                break;
        }
    }
}
