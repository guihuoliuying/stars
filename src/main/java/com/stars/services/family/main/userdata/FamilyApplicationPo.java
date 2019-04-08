package com.stars.services.family.main.userdata;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.MConst;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.summary.Summary;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyApplicationPo extends DbRow implements Cloneable {

    public static final byte TYPE_APPLYING = 0x00; // 申请类型
    public static final byte TYPE_INVITING = 0x01; // 邀请类型（可以不入库）
    public static final byte TYPE_POACHING = 0x02; // 挖人类型（可以不入库）

    public static final byte OPTION_NO_QUALIFICATION = 0b0001; // 选项：免申请资格
    public static final byte OPTION_NO_VERIFICATION = 0b0010; // 选项：免审查

    private long familyId; // 家族id
    private byte type; // 申请类型（申请/邀请/挖人）
    private byte options; // 申请选项（免资格，免审查）
    private long roleId; // 玩家id
    private int jobId; // 职业id
    private String roleName; // 玩家名字
    private int roleLevel; // 玩家等级
    private int roleFightScore; // 玩家战力
    private int appliedTimestamp; // 申请时间戳

    /* Mem Data */
    private long inviterId; // 邀请者id

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "familyapplication", "`familyid`=" + familyId + " and `roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `familyapplication` where `familyid`=" + familyId + " and `roleid`=" + roleId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toString(roleId)); // roleId
        buff.writeString(roleName); // 玩家名字
        buff.writeInt(roleLevel); // 玩家等级
        buff.writeInt(roleFightScore); // 玩家战力
    }

    public void writeToBuffer(NewByteBuffer buff, Summary summary) {
        buff.writeString(Long.toString(roleId)); // roleId
        buff.writeString(roleName); // 玩家名字
        RoleSummaryComponent roleComp = (RoleSummaryComponent) (summary != null ? summary.getComponent(MConst.Role) : null);
        if (roleComp != null) {
            buff.writeInt(roleComp.getRoleLevel()); // 玩家等级
            buff.writeInt(roleComp.getFightScore()); // 玩家战力
        } else {
            buff.writeInt(roleLevel); // 玩家等级
            buff.writeInt(roleFightScore); // 玩家战力
        }
    }

    /* Mem Data Getter/Setter */
    public long getInviterId() {
        return inviterId;
    }

    public void setInviterId(long inviterId) {
        this.inviterId = inviterId;
    }

    /* Db Data Getter/Setter */
    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getOptions() {
        return options;
    }

    public void setOptions(byte options) {
        this.options = options;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRoleLevel() {
        return roleLevel;
    }

    public void setRoleLevel(int roleLevel) {
        this.roleLevel = roleLevel;
    }

    public int getRoleFightScore() {
        return roleFightScore;
    }

    public void setRoleFightScore(int roleFightScore) {
        this.roleFightScore = roleFightScore;
    }

    public int getAppliedTimestamp() {
        return appliedTimestamp;
    }

    public void setAppliedTimestamp(int appliedTimestamp) {
        this.appliedTimestamp = appliedTimestamp;
    }
}
