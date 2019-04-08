package com.stars.services.family.main;

import com.stars.core.event.Event;
import com.stars.network.server.packet.Packet;
import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.InvocationFuture;
import com.stars.core.actor.invocation.annotation.AsyncDelegate;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;
import com.stars.core.actor.invocation.annotation.Timeout;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public interface FamilyMainService extends Service, ActorService {

    @DispatchAll
    @AsyncInvocation
    void save();

    @DispatchAll
    List<RecommendationFamily> getOnlineRecommendationList();

    /*
     * 更新数据
     */
    @AsyncInvocation
    void online(long familyId, long roleId, boolean needRespAuth); // 更新成员上线

    @AsyncInvocation
    void offline(long familyId, long roleId); // 更新成员下线

    /**
     * 内部使用
     */
    @AsyncInvocation
    void getFamilyAuth(long familyId, long roleId, InvocationFuture future);

    @AsyncInvocation
    void sendFamilyInfo(FamilyAuth auth);

    @AsyncInvocation
    void sendMemberList(FamilyAuth auth);

    List<Long> getMemberIdList(long familyId, long roleId);

    List<FamilyMemberPo> getMemberList(long familyId, boolean isOnline);

    @AsyncInvocation
    void sendApplicationList(FamilyAuth auth);

    @AsyncInvocation
    void updateMemberLevel(long familyId, long memberId, int currentLevel); // 更新成员等级

    void updateFalimyName(long familyId, String newName);

    @AsyncInvocation
    void updateMemberFightScore(long familyId, long memberId, int currentFightScore); // 更新成员战力
    @AsyncInvocation
    void updateMemberName(long familyId, long memberId, String newName);

    @AsyncInvocation
    void updateMemberContribution(long familyId, long memberId, int contributionDelta); // 更新成员帮贡

    @AsyncInvocation
    void updateMemberRmbDonation(long familyId, long memberId, int donationDelta); // 更新成员元宝捐献值

    /*
     * 创建/解散/
     */
    @AsyncDelegate
    @Timeout(timeout = 10_000)
    // 10秒，一旦超时处理起来很麻烦
    FamilyAuth create(long roleId, FamilyPost post, String familyName, String familyNotice,
                      int roleJobId, String roleName, int roleLevel, int roleFightScore); // 创建家族（分配familyId）

    @AsyncInvocation
    void innerCreate(long familyId, long roleId, FamilyPost post, String familyName, String familyNotice,
                     int roleJobId, String roleName, int roleLevel, int roleFightScore, InvocationFuture future); // 创建家族（目前不考虑不同库）

    @Timeout(timeout = 10_000)
    FamilyAuth dissolve(FamilyAuth auth); // 解散家族

    @AsyncInvocation
    void editNotice(FamilyAuth auth, String notice);

    /*
     * 申请/审核
     */
    @AsyncInvocation
    void apply(long familyId, boolean needSendText, long applicantId, FamilyPost post, int roleJobId, String applicantName, int applicantLevel, int applicantFightScore);

//    @AsyncInvocation
//    int applyAll(List<Long> familyIdList, long applicantId, FamilyPost post, int roleJobId, String applicantName, int applicantLevel, int applicantFightScore);

    @AsyncInvocation
    void verify(FamilyAuth auth, long applicantId, boolean isOk); // 审核（同意或拒绝）

    @AsyncInvocation
    void innerAckVerification(long familyId, long verifierId, long applicantId, boolean isOk, String cause);

    @AsyncInvocation
    void cancel(long familyId, long applicantId);

    /*
     * 退出/踢出
     */
    @AsyncInvocation
    void kickOut(FamilyAuth auth, long memberId); // 强制离开

    @AsyncInvocation
    void leave(FamilyAuth auth); // 自动离开

    /*
     * 任命/禅让
     */
    @AsyncInvocation
    void appoint(FamilyAuth auth, long memberId, byte postId); // 任命

    @AsyncInvocation
    void abdicate(FamilyAuth auth, long memberId); // 禅让

    /*
     * 升级
     */
    @AsyncInvocation
    void sendUpgradeInfo(FamilyAuth auth);

    @AsyncInvocation
    void upgrade(FamilyAuth auth); // 升级家族

    /*
     * 设置
     */
    @AsyncInvocation
    void setApplicationAllowance(FamilyAuth auth, boolean isAllowed); // 设置是否允许申请

    @AsyncInvocation
    void setApplicationQualification(FamilyAuth auth, int minLevel, int minFightScore, boolean isAutoVerified); // 设置申请条件

    /*
     * 修改家族资金
     */
    void addMoneyAndUpdateContribution(FamilyAuth auth, long roleId, int moneyDelta,
                                       int contributionDelta, int contributionVersion, int rmbDonationDelta); // 增加/减少家族资金


    /*
     * 挖人（最复杂的流程）
     *
     */
    // inviter权限审查，人数判断
    @AsyncInvocation
    void poach(FamilyAuth auth, long inviteeId);

    // invitee在线判断，职位判断
    @AsyncInvocation
    void innerNotifyPoaching(long oldFamilyId, long newFamilyId, long inviterId, long inviteeId);

    // 生成申请记录，通知invitee
    @AsyncInvocation
    void innerAckPoaching(long newFamilyId, long inviterId, long inviteeId, int inviteeJobId, String inviteeName, int inviteeLevel, int inviteeFightScore); // 成功的情况才有这个

    // 判断是否存在申请记录，占坑
    @AsyncInvocation
    void acceptPoaching(long newFamilyId, long inviteeId);

    // invitee职位判断，删除invitee的成员记录，家族相关数据更新
    @AsyncInvocation
    void innerNotifyAcceptingPoaching(long oldFamilyId, long newFamilyId, long inviteeId);

    // 新增invitee成员记录
    @AsyncInvocation
    void innerAckAcceptingPoaching(long newFamilyId, long inviteeId);

    // 移除
    @AsyncInvocation
    void refusePoaching(long newFamilyId, long inviteeId);

    /*
     * 邀请
     */
    @AsyncInvocation
    void invite(FamilyAuth auth, long inviteeId);

    @AsyncInvocation
    void acceptInvitation(long familyId, long inviteeId);

    @AsyncInvocation
    void refuseInvitation(long familyId, long inviteeId);

    /*
     * 锁定家族
     */

    @AsyncInvocation
    void lockFamily(long familyId);

    @AsyncInvocation
    void halfLockFamily(long familyId);

    @AsyncInvocation
    void unlockFamily(long familyId);

    @AsyncInvocation
    @DispatchAll
    void lockGlobalUnidirect(long timeout);

    @AsyncInvocation
    @DispatchAll
    void unlockGlobalUnidirect();

    /*
     * 重置逻辑
     */
    @DispatchAll
    @AsyncInvocation
    void resetDaily();

    /*
     * 家族的通用功能
     */
    @AsyncInvocation
    void sendToOnlineMember(long familyId, Packet packet);

    @AsyncInvocation
    void sendEventToOnlineMember(long familyId, Event event);

    @AsyncInvocation
    void sendToOnlineManager(long familyId, Packet packet);

    @AsyncInvocation
    void sendEmailToMember(long familyId, long roleId, String title, String text);

    @AsyncInvocation
    void sendFamilySocreOpActAwardEmail(long familyId, Map<Byte, Integer> dropMap, int rank);

    @AsyncInvocation
    void resetEmailCount(long familyId);

    /*
     * 其他服务依赖
     */
    @AsyncInvocation
    void askMemberCount(long familyId);

    @DispatchAll
    List<Long> getOnlineFamilyIdList();

    FamilyData getFamilyDataClone(long familyId);

    @AsyncInvocation
    void log_family();

    @AsyncInvocation
    void updateMemberJob(long familyId, long roleId, Integer newJobId);

    @AsyncInvocation
    void updateFalimyMasterName(long familyId, String name);
}
