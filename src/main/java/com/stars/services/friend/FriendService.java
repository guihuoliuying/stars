package com.stars.services.friend;

import com.stars.services.Service;
import com.stars.services.friend.userdata.FriendApplicationPo;
import com.stars.services.friend.userdata.FriendPo;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

import java.util.List;

/**
 * 服务端还没有处理好的内容
 * A. 最近联系人管理
 * B. 时间戳处理（好友的离线时间戳，好友/联系人申请的申请时间戳，最近联系人的上次联系时间）
 * C. 状态同步（操作后的列表同步问题）
 * D. 提示
 * E. 操作面板所需的额外信息
 * Created by zhaowenshuo on 2016/7/16.
 */
public interface FriendService extends Service, ActorService {

    /* 1. 我的好友 friends
     *   申请 apply  申请人 subject(主体)  申请对象 object(客体)
     *   操作：发送好友列表，申请好友，同意申请，拒绝申请，删除好友
     * 2. 最近联系人 contacts
     *   操作：发送最近联系人，更新最近联系人
     * 3. 推荐好友 recommendation
     *   操作：获取推荐好友列表，搜索玩家，更新推荐列表
     * 4. 好友申请 application
     *   操作：发送好友申请列表
     * 5. 黑名单
     *   操作：发送黑名单列表，加入黑名单，移除黑名单
     *
     * 6. 常用操作
     *   操作：判断是不是好友，是不是黑名单
     */

    @AsyncInvocation
    @DispatchAll
    void save();

    /**
     * 玩家上线通知好友服务
     * 1. 加载数据
     * 2. 修改成在线状态
     * 3. 设置玩家属性，主要用于判断好友操作是否合法
     *
     * @param roleId 玩家id
     * @param level  等级
     */
    @AsyncInvocation
    void online(long roleId, String roleName, int jobId, int level, int fightScore);

    @AsyncInvocation
    @DispatchAll
    void notifyOnline(List<Long> friendIdList, long roleId);

    /**
     * 玩家下线通知好友服务（修改成离线状态）
     *
     * @param roleId
     */
    @AsyncInvocation
    void offline(long roleId);

    @AsyncInvocation
    @DispatchAll
    void notifyOffline(List<Long> friendIdList, long roleId);

    /**
     * 更新好友属性，主要用于判断好友操作是否合法（捕获了升级事件后，调用该方法更新好友服务中的玩家属性）
     *
     * @param roleId 玩家id
     * @param level  等级
     */
    @AsyncInvocation
    void updateRoleLevel(long roleId, int level);

    void updateRoleName(long roleId, String newName);

    /**
     * 通知好友我改名字了
     *
     * @param targetRoleId
     * @param changeRoleId
     * @param newName
     */
    @AsyncInvocation
    void notifyFriendChangeName(Long targetRoleId, Long changeRoleId, String newName);

    /**
     * 通知联系人改名
     *
     * @param targetRoleId
     * @param changeRoleId
     * @param newName
     */
    @AsyncInvocation
    void notifyContactChangeName(Long targetRoleId, Long changeRoleId, String newName);

    @AsyncInvocation
    void updateRoleFightScore(long roleId, int fightScore);

    @AsyncInvocation
    void updateRoleJob(long roleId, int jobId);

    /*
         * 好友相关
         */
    @AsyncInvocation
    void sendFriendList(long roleId);

    List<Long> getFriendList(long roleId);

    FriendPo getFriendPo(long roleId, long friendId);

    @AsyncInvocation
    void sendReceivedApplicationList(long roleId);

    /*
     * 好友申请
     *     发起申请的一方称为申请方（Applicant，简写为App）
     *     收到申请的一方称为目标方（Object，简写为Obj）
     *     括号中的S表示服务端，C表示客户端
     *
     * 申请流程
     * App(C) --applyFriend--> App(S)
     * App(S) --innerNotifyApplication--> Obj(S)
     *
     * 同意流程
     * Obj(C) --agreeApplication--> Obj(S)
     * Obj(S) --innerNotifyAgreement--> App(S)
     * App(S) --innerAckVerification--> Obj(S)
     *
     * 拒绝流程
     * Obj(C) --rejectApplication--> Obj(S)
     * Obj(S) --innerNotifyRejection--> App(S)
     *
     * innerNotifyApplication/agreeApplication检查不通过
     * Obj(S) --innerNotifyRejection--> App(S)
     */
    @AsyncInvocation
    void applyFriend(long applicantId, long objectId, FriendApplicationPo applicationPo);

    @AsyncInvocation
    void innerNotifyApplication(long objectId, long applicantId, FriendApplicationPo applicationPo);

    @AsyncInvocation
    void agreeApplication(long objectId, long applicantId);

    @AsyncInvocation
    void agreeAllApplication(long objectId);

    @AsyncInvocation
    void innerNotifyAgreement(long applicantId, long objectId, String objectName);

    @AsyncInvocation
    void innerNotifyAck(long objectId, long applicantId, boolean isSuccess, String cause);

    @AsyncInvocation
    void rejectApplication(long objectId, long applicantId);

    @AsyncInvocation
    void rejectAllApplication(long objectId);

    @AsyncInvocation
    void innerNotifyRejection(long applicantId, long objectId, String cause);

    /*
     * 删除
     *     发起申请的一方称为申请方（Applicant，简写为App）
     *     收到申请的一方称为目标方（Object，简写为Obj）
     *     括号中的S表示服务端，C表示客户端
     *
     * 删除流程
     * App(C) --deleteFriend--> App(S)
     * App(S) --innerNotifyDelete--> Obj(S)
     */
    @AsyncInvocation
    void deleteFriend(long applicantId, long objectId, boolean needTips);

    @AsyncInvocation
    void innerNotifyDelete(long objectId, long applicantId);

    /*
     * 联系人相关
     */
    @AsyncInvocation
    void sendContactsList(long roleId);

    @AsyncInvocation
    void updateContacts(long roleId, long contactsId);

    /*
     * 推荐好友相关
     */
    @AsyncInvocation
    void sendRecommendationList(long roleId, int level);

    @AsyncInvocation
    void searchRole(long roleId, String pattern);

    /*
     * 黑名单相关
     */
    @AsyncInvocation
    void sendBlackList(long roleId);

    @AsyncInvocation
    void addToBlackList(long roleId, long blackerId);

    @AsyncInvocation
    void removeFromBlackList(long roleId, long blackerId);

//    List<Long> getBlackList(long roleId);

    @AsyncInvocation
    void sendVigor(long roleId, long friendId);

    @AsyncInvocation
    void receiveVigor(long roleId, long friendId);

    @AsyncInvocation
    void innerNotifySendVigor(long roleId, long friendId);

    @AsyncInvocation
    void sendAllVigor(long roleId);

    @AsyncInvocation
    void receiveAllVigor(long roleId);

    @AsyncInvocation
    void sendFlower(long roleId, long friendId, int itemId, int count);

    @AsyncInvocation
    void innerNotifyReceiveFlower(long roleId, long friendId, String friendName, int jobId, int level, int addFlowerCount, int addIntimacy);

    @AsyncInvocation
    void viewFriendFlowerUI(long roleId);

    /*
     * 重置逻辑
     */
    @DispatchAll
    @AsyncInvocation
    void dailyReset();

    @AsyncInvocation
    void openSendFlowerUI(long roleId);

    /**
     * 上线登陆更新送鲜花常用数据
     */
    @AsyncInvocation
    public void onUpdateSummary(long roleId);

    /**
     * 检测双方是否好友
     *
     * @param roleId
     * @param friendId
     * @return
     */
    public boolean checkIsFriend(long roleId, long friendId);

}
