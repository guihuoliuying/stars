package com.stars.modules.family.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.family.FamilyManager;
import com.stars.modules.family.FamilyPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.FamilyData;
import com.stars.services.family.main.prodata.FamilyLevelVo;
import com.stars.services.family.main.userdata.FamilyApplicationPo;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;

import java.util.*;

import static com.stars.modules.family.FamilyManager.levelVoMap;

/**
 * Created by zhaowenshuo on 2016/8/24.
 */
public class ClientFamilyManagement extends PlayerPacket {

    /* 信息 */
    public static final byte SUBTYPE_INFO = 0x00; // 家族信息
    public static final byte SUBTYPE_OPTIONS = 0x01; // 家族设置更改
    public static final byte SUBTYPE_MONEY = 0x02; // 家族资金
    public static final byte SUBTYPE_LOCK = 0x03; // 家族锁定状态
    /* 创建/解散/编辑公告 */
    public static final byte SUBTYPE_CREATE = 0x04; // 创建家族
    public static final byte SUBTYPE_DISSOLVE = 0x05; // 解散家族
    public static final byte SUBTYPE_EDIT_NOTICE = 0x06; // 编辑公告
    //    public static final byte SUBTYPE_SEARCH = 0x07; // 搜索家族
    /* 成员列表 */
    public static final byte SUBTYPE_MEMBER_LIST = 0x08; // 成员列表
    public static final byte SUBTYPE_MEMBER_ADD = 0x09; // 成员新增
    public static final byte SUBTYPE_MEMBER_DEL = 0x0A; // 成员删除
    /* 申请列表 */
    public static final byte SUBTYPE_APPLICATION_LIST = 0x0C; // 申请列表
    /* 申请/审核 */
    public static final byte SUBTYPE_APPLY = 0x10; // 申请加入
    public static final byte SUBTYPE_APPLICATION_VERIFY = 0x11; // 审核（同意/拒绝/同意全部/拒绝全部）
    public static final byte SUBTYPE_APPLICATION_CANCEL = 0x12; // 取消申请
    //        public static final byte SUBTYPE_APPLY_ALL = 0x13; // 一键申请（暂时延后）
    /* 邀请 */
    public static final byte SUBTYPE_INVITE = 0x14; // 邀请
    public static final byte SUBTYPE_INVITATION_APPROVE = 0x15; // 同意邀请
    public static final byte SUBTYPE_INVITATION_REFUSE = 0x16; // 拒绝邀请
    /* 挖人 */
    public static final byte SUBTYPE_POACH = 0x18; // 挖人
    public static final byte SUBTYPE_POACHING_APPROVE = 0x19; // 同意挖人
    public static final byte SUBTYPE_POACHING_REFUSE = 0x1A; // 拒绝挖人
    /* 退出/踢出 */
    public static final byte SUBTYPE_LEAVE = 0x1C; // 退出家族
    public static final byte SUBTYPE_KICK_OUT = 0x1D; // 踢出家族
    /* 任命/禅让 */
    public static final byte SUBTYPE_APPOINT = 0x30; // 任命职位
    public static final byte SUBTYPE_ABDICATE = 0x31; // 禅让族长
    /* 升级 */
    public static final byte SUBTYPE_UPGRADE_INFO = 0x40; // 升级信息
    public static final byte SUBTYPE_UPGRADE = 0x41; // 升级
    /* 成员列表(扩展) */
    public static final byte SUBTYPE_MEMBER_ONLINE = 0x60; // 成员上线
    public static final byte SUBTYPE_MEMBER_OFFLINE = 0x61; // 成员下线
    public static final byte SUBTYPE_MEMBER_POST_CHANGED = 0x62; // 成员职位变动
    /* 设置 */
    public static final byte SUBTYPE_SET_APP_ALLOWANCE = 0x70; // 设置是否允许申请
    public static final byte SUBTYPE_SET_APP_QUALIFICATION = 0x71; // 设置申请条件

    /* 可发邮件次数 */
    public static final byte SUBTYPE_EMAIL_COUNT = 0x72;


    public static final byte INVITATION_BY_MASTER = 0x00; // 由族长邀请
    public static final byte INVITATION_BY_ASSISTANT_ELDER = 0x01; // 由副组长/长老推荐
    public static final byte INVITATION_BY_MEMBER = 0x02; // 由成员推荐

    private byte subtype; //
    private FamilyData familyData;
    private long memberId;
    private String memberName;
    private byte memberPostId;
    private FamilyMemberPo memberPo;

    /* 家族锁定信息 */
    private long familyId;
    private byte isLock;

    /* 通用回包 */
    private boolean isSuccess;
    private String cause;

    /* 审核申请 */
    private long applicantId;

    /* 邀请/挖人 */
    private String inviterName;
    private long invitationFamilyId;
    private String invitationFamilyName;
    private byte invitationType;

    /* 升级信息 */
    private FamilyLevelVo currentLevelVo;
    private FamilyLevelVo nextLevelVo;

    /* 升级 */
    private boolean isMaxLevel;

    /* 可发邮件次数 */
    private byte count;

    public ClientFamilyManagement() {
    }

    public ClientFamilyManagement(byte subtype) {
        this.subtype = subtype;
    }

    public ClientFamilyManagement(byte subtype, boolean isSuccess, String cause) {
        this.subtype = subtype;
        this.isSuccess = isSuccess;
        this.cause = cause;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FamilyPacketSet.C_MANAGEMENT;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_INFO:
                writeFamilyInfo(buff);
                break;
            case SUBTYPE_OPTIONS:
                writeFamilyOptions(buff);
                break;
            case SUBTYPE_MONEY:
                writeFamilyMoney(buff);
                break;
            case SUBTYPE_LOCK:
                writeLock(buff);
                break;
            case SUBTYPE_CREATE:
                writeCommonResp(buff);
                break;
            case SUBTYPE_MEMBER_LIST:
                writeMemberList(buff);
                break;
            case SUBTYPE_MEMBER_ONLINE:
                writeMemberOnline(buff);
                break;
            case SUBTYPE_MEMBER_OFFLINE:
                writeMemberOffline(buff);
                break;
            case SUBTYPE_MEMBER_ADD:
                writeMemberAdd(buff);
                break;
            case SUBTYPE_MEMBER_DEL:
                writeMemberDel(buff);
                break;
            case SUBTYPE_MEMBER_POST_CHANGED:
                writeMemberPostChanged(buff);
                break;
            case SUBTYPE_APPLICATION_LIST:
                writeApplicationList(buff);
                break;
            case SUBTYPE_APPLY:
                writeApply(buff);
                break;
            case SUBTYPE_APPLICATION_VERIFY:
                writeApplicationVerify(buff);
                break;
            case SUBTYPE_APPLICATION_CANCEL:
                writeApplicationCancel(buff);
                break;
            case SUBTYPE_INVITE:
                writeInvitation(buff);
                break;
            case SUBTYPE_POACH:
                writePoaching(buff);
                break;
            case SUBTYPE_UPGRADE_INFO:
                writeUpgradeInfo(buff);
                break;
            case SUBTYPE_UPGRADE:
                writeUpgrade(buff);
                break;
            case SUBTYPE_EMAIL_COUNT:
                writeEmailCount(buff, count);
                break;
        }
    }

    private void writeCommonResp(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte((byte) (isSuccess ? 1 : 0)); // 1-成功，0-失败
        buff.writeString(cause); // 原因
    }

    private void writeFamilyInfo(com.stars.network.server.buffer.NewByteBuffer buff) {

        FamilyPo familyPo = familyData.getFamilyPo();
        FamilyLevelVo levelVo = levelVoMap.get(familyPo.getLevel());
        buff.writeString(Long.toString(familyPo.getFamilyId())); // 家族id
        buff.writeString(familyPo.getName()); // 家族名
        buff.writeString(familyPo.getMasterName()); // 族长名
        buff.writeInt(familyPo.getLevel()); // 家族等级
        buff.writeInt(familyPo.getMoney()); // 资金
        buff.writeInt(familyData.getOnlineCount()); // 在线人数
        buff.writeInt(familyData.getMemberPoMap().size()); // 成员数量
        buff.writeInt(levelVo != null ? levelVo.getMemberLimit() : 50); // todo: 成员上限
        buff.writeString(familyPo.getNotice()); // 公告
        /* 心法上限 */
        buff.writeInt(levelVo != null ? levelVo.getSkillLimit() : 0); // 操蛋，耦合在这里了
        writeEmailCount(buff, (byte) (FamilyManager.emailCount - familyPo.getEmailCount()));
    }

    private void writeEmailCount(com.stars.network.server.buffer.NewByteBuffer buff, byte count) {
        LogUtil.info("count:{}", count);
        buff.writeByte(count);
    }

    private void writeFamilyMoney(com.stars.network.server.buffer.NewByteBuffer buff) {
        FamilyPo familyPo = familyData.getFamilyPo();
        buff.writeString(Long.toString(familyPo.getFamilyId())); // 家族id
        buff.writeInt(familyPo.getMoney()); // 家族资金
    }

    private void writeFamilyOptions(com.stars.network.server.buffer.NewByteBuffer buff) {
        FamilyPo familyPo = familyData.getFamilyPo();
        buff.writeByte(familyPo.getAllowApplication()); // 是否允许申请（1-允许，0-不允许）
        buff.writeInt(familyPo.getQualificationMinLevel()); // 申请资格：最小等级
        buff.writeInt(familyPo.getQualificationMinFightScore()); // 申请资格：最小战力
        buff.writeByte(familyPo.getAutoVerified()); // 是否允许自动审核（1-允许，0-不允许）
    }

    private void writeLock(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(familyId));
        buff.writeByte(isLock); // 锁定: 1; 非锁定: 0; 半锁：-1
    }

    private void writeMemberList(com.stars.network.server.buffer.NewByteBuffer buff) {
        Map<Long, FamilyMemberPo> memberPoMap = familyData.getMemberPoMap();
        buff.writeShort((short) memberPoMap.size());
        for (FamilyMemberPo memberPo : memberPoMap.values()) {
            memberPo.writeToBuffer(buff);
        }
    }

    private void writeMemberOnline(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(memberId));
        buff.writeString(memberName);
    }

    private void writeMemberOffline(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(memberId));
        buff.writeString(memberName);
    }

    private void writeMemberAdd(com.stars.network.server.buffer.NewByteBuffer buff) {
        memberPo.writeToBuffer(buff);
    }

    private void writeMemberDel(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(memberId));
    }

    private void writeMemberPostChanged(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(memberId));
        buff.writeByte(memberPostId);
    }

    private void writeApplicationList(com.stars.network.server.buffer.NewByteBuffer buff) {
        Map<Long, FamilyApplicationPo> applicationPoMap = new HashMap<>(familyData.getApplicationPoMap());
        Iterator<Map.Entry<Long, FamilyApplicationPo>> iterator = applicationPoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, FamilyApplicationPo> entry = iterator.next();
            if (entry.getValue().getType() != FamilyApplicationPo.TYPE_APPLYING) {
                iterator.remove();
            }
        }

        List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(new ArrayList<Long>(applicationPoMap.keySet()));
        Map<Long, Summary> summaryMap = new HashMap<>();
        for (Summary summary : summaryList) {
            summaryMap.put(summary.getRoleId(), summary);
        }

        buff.writeShort((short) applicationPoMap.size());
        for (FamilyApplicationPo applicationPo : applicationPoMap.values()) {
            applicationPo.writeToBuffer(buff, summaryMap.get(applicationPo.getRoleId()));
        }
    }

    private void writeApply(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(familyData.getFamilyPo().getFamilyId()));
    }

    private void writeApplicationVerify(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(applicantId));
    }

    private void writeApplicationCancel(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(familyData.getFamilyPo().getFamilyId()));
    }

    private void writeInvitation(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(invitationType); // 邀请类型（0-族长邀请，1-副族长/长老邀请，2-成员邀请）
        buff.writeString(Long.toString(invitationFamilyId)); // 邀请家族id
        buff.writeString(invitationFamilyName); // 邀请家族名字
        buff.writeString(inviterName); // 邀请者名字
    }

    private void writePoaching(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(Long.toString(invitationFamilyId)); // 挖人家族id
        buff.writeString(invitationFamilyName); // 挖人家族名字
        buff.writeString(inviterName); // 挖人者名字
    }

    private void writeUpgradeInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(currentLevelVo.getLevel()); // 当前等级
        buff.writeInt(nextLevelVo.getLevel()); // 下一等级
        buff.writeInt(nextLevelVo.getMemberLimit()); // 成员上限
        buff.writeInt(nextLevelVo.getSkillLimit()); // 心法上限
        buff.writeInt(nextLevelVo.getRequiredMoney()); // 所需资金
    }

    private void writeUpgrade(NewByteBuffer buff) {
        buff.writeByte((byte) (isSuccess ? 1 : 0)); // 1-成功, 0-失败
        buff.writeByte((byte) (isMaxLevel ? 1 : 0)); // 1-已达最大等级, 0-未达最大等级
    }

    /* Db Data Getter/Setter */

    public byte getSubtype() {
        return subtype;
    }

    public void setSubtype(byte subtype) {
        this.subtype = subtype;
    }

    public FamilyData getFamilyData() {
        return familyData;
    }

    public void setFamilyData(FamilyData familyData) {
        this.familyData = familyData;
    }

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public byte getMemberPostId() {
        return memberPostId;
    }

    public void setMemberPostId(byte memberPostId) {
        this.memberPostId = memberPostId;
    }

    public FamilyMemberPo getMemberPo() {
        return memberPo;
    }

    public void setMemberPo(FamilyMemberPo memberPo) {
        this.memberPo = memberPo;
    }

    public long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public String getInviterName() {
        return inviterName;
    }

    public void setInviterName(String inviterName) {
        this.inviterName = inviterName;
    }

    public long getInvitationFamilyId() {
        return invitationFamilyId;
    }

    public void setInvitationFamilyId(long invitationFamilyId) {
        this.invitationFamilyId = invitationFamilyId;
    }

    public String getInvitationFamilyName() {
        return invitationFamilyName;
    }

    public void setInvitationFamilyName(String invitationFamilyName) {
        this.invitationFamilyName = invitationFamilyName;
    }

    public byte getInvitationType() {
        return invitationType;
    }

    public void setInvitationType(byte invitationType) {
        this.invitationType = invitationType;
    }

    public FamilyLevelVo getCurrentLevelVo() {
        return currentLevelVo;
    }

    public void setCurrentLevelVo(FamilyLevelVo currentLevelVo) {
        this.currentLevelVo = currentLevelVo;
    }

    public FamilyLevelVo getNextLevelVo() {
        return nextLevelVo;
    }

    public void setNextLevelVo(FamilyLevelVo nextLevelVo) {
        this.nextLevelVo = nextLevelVo;
    }

    public boolean isMaxLevel() {
        return isMaxLevel;
    }

    public void setMaxLevel(boolean maxLevel) {
        isMaxLevel = maxLevel;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public byte isLock() {
        return isLock;
    }

    public void setLock(byte lock) {
        isLock = lock;
    }

    public void setCount(byte count) {
        this.count = count;
    }
}
