package com.stars.services.mail;

import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;
import com.stars.modules.email.pojodata.EmailConditionArgs;
import com.stars.services.Service;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/7/16.
 */
public interface EmailService extends Service, ActorService {

    public static final byte SENDER_TYPE_SYSTEM = 0;
    public static final byte SENDER_TYPE_ROLE = 1;

    /*
     * 保存接口
     */

    @AsyncInvocation
    @DispatchAll
    void save();

    /*
     * 个人业务接口
     */

    /**
     * 玩家上线时，须异步通知邮件系统（数据加载）
     *  @param roleId
     * @param emailConditionArgs
     */
    @AsyncInvocation
    void online(long roleId, EmailConditionArgs emailConditionArgs);

    /**
     * 玩家下线时，须异步通知邮件系统
     *
     * @param roleId
     */
    @AsyncInvocation
    void offline(long roleId);

    /**
     * 玩家获取邮件列表（邮件系统直接发送到客户端）
     *
     * @param roleId
     */
    @AsyncInvocation
    void sendMailListToRole(long roleId);

    /**
     * 玩家阅读邮件（邮件系统对该邮件进行标记）
     *
     * @param roleId
     * @param emailId
     */
    @AsyncInvocation
    void read(long roleId, int emailId);

    /**
     * 玩家删除邮件（邮件系统删除该邮件）；如果该该邮件存在附件未领取，则不进行删除并通知客户端
     *
     * @param roleId
     * @param emailId
     */
    @AsyncInvocation
    void delete(long roleId, int emailId);

    /**
     * 玩家删除全部邮件（邮件系统删除全部邮件，有附件未领取的邮件不会删除）
     *
     * @param roleId
     */
    @AsyncInvocation
    void deleteAll(long roleId);

    /**
     * 获取玩家邮件附件
     *
     * @param roleId
     * @param emailId
     * @return 附件的道具哈希表
     * @throws Exception
     */
    Map<Integer, Integer> getAffixs(long roleId, int emailId) throws Exception; // 同步，

    /**
     * 获取玩家全部邮件附件
     *
     * @param roleId
     * @return 全部附件的道具哈希表(emailId, (toolId, count))
     * @throws Exception
     */
    Map<Integer, Map<Integer, Integer>> getAllAffixs(long roleId) throws Exception; // 同步

    /**
     * 将邮件附件标记为已领取
     *
     * @param roleId
     * @param emailId
     * @throws Exception
     */
    void fetchAffixs(long roleId, int emailId) throws Exception; // 同步，获取附件

    /*
     * 发送邮件接口
     */

    /**
     * 把邮件发送给单个玩家（一般用于GM）
     * @param emailPo
     */
    @AsyncInvocation
    void sendToSingle(RoleEmailPo emailPo); // 异步，发送邮件（单封邮件）


    /**
     * 根据邮件模板生成邮件并发送给单个玩家
     *
     * @param roleId     玩家id
     * @param templateId 邮件模板id
     * @param senderId   发送者id，用于记录
     * @param senderName 发送者名字
     * @param params     正文的动态字符串（客户端拼接）
     */
    @AsyncInvocation
    void sendToSingle(long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, String... params); // 异步
    
    /**
     * 根据邮件模板生成邮件并发送给单个玩家
     *
     * @param roleId     玩家id
     * @param templateId 邮件模板id
     * @param senderId   发送者id，用于记录
     * @param senderName 发送者名字
     * @param coolTime   冻结时间（这个时间之后才会删除）
     * @param params     正文的动态字符串（客户端拼接）
     */
    @AsyncInvocation
    void sendToSingleWithCoolTime(long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, int coolTime, String... params); // 异步

    /* 群发邮件 */

    /**
     * @param roleIdList
     * @param senderType
     * @param senderId
     * @param senderName
     * @param title
     * @param text
     * @param affixMap
     */
    @AsyncInvocation
    @DispatchAll
    void sendTo(List<Long> roleIdList, byte senderType, Long senderId, String senderName, String title, String text, Map<Integer, Integer> affixMap);

//    /**
//     * todo: 可能需要入库到allemail中
//     * 把邮件发送给在线玩家
//     * @param emailPo
//     */
//    @AsyncInvocation
//    @DispatchAll
//    void sendToOnline(AllEmailPo emailPo); // 异步，发送邮件（在线）

    /**
     * 根据邮件模板生成邮件并发送给在线玩家
     *
     * @param templateId 邮件模板id
     * @param senderId   发送者id
     * @param senderName 发送者名字
     * @param params     正文的动态字符串（客户端拼接）
     */
    @AsyncInvocation
    @DispatchAll
    void sendToOnline(int templateId, Long senderId, String senderName, String... params); // 异步，

//    /**
//     * 把邮件发送给全部玩家
//     * @param emailPo
//     */
//    @AsyncInvocation
//    void sendToAll(AllEmailPo emailPo); // 异步，发送邮件（全部）

    /**
     * 根据邮件模板生成邮件并发送给全部玩家
     *
     * @param templateId 邮件模板id
     * @param senderId   发送者id
     * @param senderName 发送者名称
     * @param params     正文的动态字符串（客户端拼接）
     */
    @AsyncInvocation
    void sendToAll(int templateId, Long senderId, String senderName, String... params);

    void innerSendToAll(AllEmailPo allEmailPo);

    /*
     * 内部接口
     */
    @AsyncInvocation
    @DispatchAll
    void sendToAll(AllEmailPo emailPo); // 异步，内部使用

}
