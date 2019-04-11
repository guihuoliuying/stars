package com.stars.services;


import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.ServerManager;
import com.stars.modules.chat.Connect2RMChatServerCallBack;
import com.stars.multiserver.MainRpcHelper;
import com.stars.services.chat.ChatService;
import com.stars.services.chat.ChatServiceActor;
import com.stars.services.family.activities.entry.FamilyActEntryServiceImpl;
import com.stars.services.family.event.FamilyEventServiceActor;
import com.stars.services.family.main.FamilyMainServiceActor;
import com.stars.services.family.role.FamilyRoleServiceActor;
import com.stars.services.family.welfare.redpacket.FamilyRedPacketServiceActor;
import com.stars.services.fightServerManager.Conn2FightManagerServerCallBack;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.fightServerManager.FSManagerServiceActor;
import com.stars.services.fightServerManager.FSRPCNetExceptionTask;
import com.stars.services.friend.FriendServiceActor;
import com.stars.services.friendInvite.InviteServiceActor;
import com.stars.services.id.IdServiceImpl;
import com.stars.services.localservice.LocalService;
import com.stars.services.localservice.LocalServiceActor;
import com.stars.services.mail.EmailServiceActor;
import com.stars.services.multicommon.MultiCommonService;
import com.stars.services.multicommon.MultiCommonServiceActor;
import com.stars.services.pay.Conn2PayServerCallBack;
import com.stars.services.pay.PayService;
import com.stars.services.pay.PayServiceActor;
import com.stars.services.role.RoleService;
import com.stars.services.role.RoleServiceImpl;
import com.stars.services.summary.SummaryServiceActor;

import static com.stars.services.SConst.*;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public class MainServerServiceManager extends ServiceManager {

    @Override
    public void initSelfServices() throws Throwable {
        registerAndInit(IdService, new IdServiceImpl()); // id生成服务
        registerAndInit(RoleService, new RoleServiceImpl()); // 玩家相关服务（发包，发事件，执行packet）
        registerAndInit(LocalService, newService(new LocalServiceActor())); // 本地服务
        registerAndInit(SummaryService, newDispatchService(SummaryServiceActor.class, 8)); // 摘要数据：核心业务，需要更多的资源
        registerAndInit(EmailService, newDispatchService(EmailServiceActor.class, 8)); // 邮件：核心业务，需要更多的资源
        registerAndInit(FriendService, newDispatchService(FriendServiceActor.class, 2)); // 好友
        registerAndInit(ChatService, newService(new ChatServiceActor())); // 聊天
        registerAndInit(FamilyRoleService, newDispatchService(FamilyRoleServiceActor.class, 2)); // 家族 - 个人
        registerAndInit(FamilyMainService, newDispatchService(FamilyMainServiceActor.class, 2)); // 家族
        registerAndInit(FamilyRedPacketService, newDispatchService(FamilyRedPacketServiceActor.class, 2)); // 家族 - 红包（废弃）
        registerAndInit(FamilyEventService, newDispatchService(FamilyEventServiceActor.class, 2)); // 家族 - 日志
        registerAndInit(FamilyActEntryService, new FamilyActEntryServiceImpl()); // 家族 - 活动入口
        registerAndInit(FSManagerService, newService(new FSManagerServiceActor())); // 战斗管理服
        registerAndInit(PayService, newService(new PayServiceActor())); // 支付服
        registerAndInit(InviteService, newService(new InviteServiceActor())); // 好友邀请
        registerAndInit(MultiCommonService, newService(new MultiCommonServiceActor()));
    }

    @Override
    public void initRpc() throws Throwable {
        exportService(RoleService.class, getService(RoleService)); // 暴露服务
        exportService(ChatService.class, getService(ChatService));
        exportService(LocalService.class, getService(LocalService));
//        exportService(EscortService.class, getService(EscortService));
        exportService(FSManagerService.class, getService(FSManagerService));
        exportService(PayService.class, getService(PayService));
//        exportService(FamilyEscortService.class, getService(FamilyEscortService));//废弃
        exportService(MultiCommonService.class, getService(MultiCommonService));
        initRpcHelper(MainRpcHelper.class); // 初始化helper
        connectServer("multi"); // 连接斗神殿
        connectServer("rmchat", new Connect2RMChatServerCallBack());
        int commonId = ServerManager.getServer().getConfig().getServerId();
        int managerServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps()
                .get(BootstrapConfig.FIGHTMANAGER).getProperty("serverId"));
        connectServer(BootstrapConfig.FIGHTMANAGER,
                new Conn2FightManagerServerCallBack(MainRpcHelper.rmfsManagerService(), commonId, managerServerId),
                new FSRPCNetExceptionTask(BootstrapConfig.FIGHTMANAGER, BootstrapConfig.FIGHTMANAGER1,
                        MainRpcHelper.rmfsManagerService()));

        connectServer(BootstrapConfig.PAYSERVER, new Conn2PayServerCallBack());//支付服
        connectServer(BootstrapConfig.PAYSERVER1, new Conn2PayServerCallBack());
        connectServer(BootstrapConfig.SKYRANK); // 连接天梯服
        connectServer(BootstrapConfig.DAILY5V5);
    }

    @Override
    public void runScheduledJob() throws Throwable {
        ServiceHelper.summaryService().save();
        ServiceHelper.emailService().save();
        ServiceHelper.friendService().save();
        ServiceHelper.familyMainService().save();
        ServiceHelper.familyRoleService().save();
        ServiceHelper.familyRedPacketService().save();
        ServiceHelper.familyEventService().save();
        // ServiceHelper.operateActivityService().save();
        ServiceHelper.chatService().save();
        ServiceHelper.inviteService().save();
    }
}
