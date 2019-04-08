package com.stars.services.family;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public interface FamilyPost {

    public final FamilyPost ERROR = new ErrorPost();
    public final FamilyPost MASTER = new MasterPost(); // 族长
    public final FamilyPost ASSISTANT = new AssistantPost(); // 副族长
    public final FamilyPost ELDER = new ElderPost(); // 长老
    public final FamilyPost MEMBER = new MemberPost(); // 成员
    public final FamilyPost MASSES = new MassesPost(); // 群众

    public final byte ERROR_ID = 0x7F;
    public final byte MASTER_ID = 0x01; // 族长
    public final byte ASSISTANT_ID = 0x02; // 副组长
    public final byte ELDER_ID = 0x03; // 长老
    public final byte MEMBER_ID = 0x04; // 成员
    public final byte MASSES_ID = 0x05; // 群众

    public final Map<Byte, FamilyPost> postMap = ImmutableMap.<Byte, FamilyPost>builder()
            .put(ERROR_ID, ERROR)
            .put(MASTER_ID, MASTER)
            .put(ASSISTANT_ID, ASSISTANT)
            .put(ELDER_ID, ELDER)
            .put(MEMBER_ID, MEMBER)
            .put(MASSES_ID, MASSES)
            .build();

    byte getId();

    String getName();

    boolean canCreate(); // 能否创建帮派

    boolean canDissolve(); // 能否解散帮派

    boolean canApply(); // 能否申请

    boolean canVerify(); // 能否审核申请

    boolean canSetAutoVerification(); // 能否设置自动审核

    boolean canSetApplicationQualification(); // 能否设置申请条件

    boolean canSetApplicationAllowance(); // 能否设置允许/禁止申请

    boolean canAppoint(); // 能否任命家族职位

    boolean canAbdicate(); // 能否退位（禅让）

    boolean canKickOut(); // 能否强制踢出

    boolean canLeave(); // 能否主动退出

    boolean canEditNotice(); // 能否编辑公告

    boolean canStartActivities(); // 能否开始活动

    boolean canUpgrade(); // 能否升级

    boolean canInvite(); // 能否邀请

    boolean canPoach(); // 能否挖人

    boolean canRename();//能否家族改名
}

class ErrorPost implements FamilyPost {

    @Override
    public byte getId() {
        return ERROR_ID;
    }

    @Override
    public String getName() {
        return "错误职位";
    }

    @Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDissolve() {
        return false;
    }

    @Override
    public boolean canApply() {
        return false;
    }

    @Override
    public boolean canVerify() {
        return false;
    }

    @Override
    public boolean canSetAutoVerification() {
        return false;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return false;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return false;
    }

    @Override
    public boolean canAppoint() {
        return false;
    }

    @Override
    public boolean canAbdicate() {
        return false;
    }

    @Override
    public boolean canKickOut() {
        return false;
    }

    @Override
    public boolean canEditNotice() {
        return false;
    }

    @Override
    public boolean canLeave() {
        return false;
    }

    @Override
    public boolean canStartActivities() {
        return false;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public boolean canInvite() {
        return false;
    }

    @Override
    public boolean canPoach() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}

class MasterPost implements FamilyPost {

    @Override
    public byte getId() {
        return MASTER_ID;
    }

    @Override
    public String getName() {
        return "族长";
    }

    @Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDissolve() {
        return true;
    }

    @Override
    public boolean canApply() {
        return false;
    }

    @Override
    public boolean canVerify() {
        return true;
    }

    @Override
    public boolean canSetAutoVerification() {
        return true;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return true;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return true;
    }

    @Override
    public boolean canAppoint() {
        return true;
    }

    @Override
    public boolean canAbdicate() {
        return true;
    }

    @Override
    public boolean canKickOut() {
        return true;
    }

    @Override
    public boolean canEditNotice() {
        return true;
    }

    @Override
    public boolean canLeave() {
        return false;
    }

    @Override
    public boolean canStartActivities() {
        return true;
    }

    @Override
    public boolean canUpgrade() {
        return true;
    }

    @Override
    public boolean canInvite() {
        return true;
    }

    @Override
    public boolean canPoach() {
        return true;
    }

    @Override
    public boolean canRename() {
        return true;
    }
}

class AssistantPost implements FamilyPost {

    @Override
    public byte getId() {
        return ASSISTANT_ID;
    }

    @Override
    public String getName() {
        return "副族长";
    }

    @Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDissolve() {
        return false;
    }

    @Override
    public boolean canApply() {
        return false;
    }

    @Override
    public boolean canVerify() {
        return true;
    }

    @Override
    public boolean canSetAutoVerification() {
        return false;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return true;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return false;
    }

    @Override
    public boolean canAppoint() {
        return false;
    }

    @Override
    public boolean canAbdicate() {
        return false;
    }

    @Override
    public boolean canKickOut() {
        return true;
    }

    @Override
    public boolean canEditNotice() {
        return true;
    }

    @Override
    public boolean canLeave() {
        return true;
    }

    @Override
    public boolean canStartActivities() {
        return true;
    }

    @Override
    public boolean canUpgrade() {
        return true;
    }

    @Override
    public boolean canInvite() {
        return true;
    }

    @Override
    public boolean canPoach() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}

class ElderPost implements FamilyPost {

    @Override
    public byte getId() {
        return ELDER_ID;
    }

    @Override
    public String getName() {
        return "长老";
    }

    @Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDissolve() {
        return false;
    }

    @Override
    public boolean canApply() {
        return false;
    }

    @Override
    public boolean canVerify() {
        return true;
    }

    @Override
    public boolean canSetAutoVerification() {
        return false;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return false;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return false;
    }

    @Override
    public boolean canAppoint() {
        return false;
    }

    @Override
    public boolean canAbdicate() {
        return false;
    }

    @Override
    public boolean canKickOut() {
        return true;
    }

    @Override
    public boolean canEditNotice() {
        return false;
    }

    @Override
    public boolean canLeave() {
        return true;
    }

    @Override
    public boolean canStartActivities() {
        return false;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public boolean canInvite() {
        return true;
    }

    @Override
    public boolean canPoach() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}

class MemberPost implements FamilyPost {

    @Override
    public byte getId() {
        return MEMBER_ID;
    }

    @Override
    public String getName() {
        return "成员";
    }

    @Override
    public boolean canCreate() {
        return false;
    }

    @Override
    public boolean canDissolve() {
        return false;
    }

    @Override
    public boolean canApply() {
        return false;
    }

    @Override
    public boolean canVerify() {
        return false;
    }

    @Override
    public boolean canSetAutoVerification() {
        return false;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return false;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return false;
    }

    @Override
    public boolean canAppoint() {
        return false;
    }

    @Override
    public boolean canAbdicate() {
        return false;
    }

    @Override
    public boolean canKickOut() {
        return false;
    }

    @Override
    public boolean canEditNotice() {
        return false;
    }

    @Override
    public boolean canLeave() {
        return true;
    }

    @Override
    public boolean canStartActivities() {
        return false;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public boolean canInvite() {
        return true;
    }

    @Override
    public boolean canPoach() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}

class MassesPost implements FamilyPost {

    @Override
    public byte getId() {
        return MASSES_ID;
    }

    @Override
    public String getName() {
        return "群众";
    }

    @Override
    public boolean canCreate() {
        return true;
    }

    @Override
    public boolean canDissolve() {
        return false;
    }

    @Override
    public boolean canApply() {
        return true;
    }

    @Override
    public boolean canVerify() {
        return false;
    }

    @Override
    public boolean canSetAutoVerification() {
        return false;
    }

    @Override
    public boolean canSetApplicationQualification() {
        return false;
    }

    @Override
    public boolean canSetApplicationAllowance() {
        return false;
    }

    @Override
    public boolean canAppoint() {
        return false;
    }

    @Override
    public boolean canAbdicate() {
        return false;
    }

    @Override
    public boolean canKickOut() {
        return false;
    }

    @Override
    public boolean canEditNotice() {
        return false;
    }

    @Override
    public boolean canLeave() {
        return false;
    }

    @Override
    public boolean canStartActivities() {
        return false;
    }

    @Override
    public boolean canUpgrade() {
        return false;
    }

    @Override
    public boolean canInvite() {
        return false;
    }

    @Override
    public boolean canPoach() {
        return false;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}