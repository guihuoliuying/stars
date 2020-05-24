package com.stars.services;


import com.stars.services.friend.FriendServiceActor;
import com.stars.services.id.IdServiceImpl;
import com.stars.services.localservice.LocalServiceActor;
import com.stars.services.mail.EmailServiceActor;
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
    }

    @Override
    public void initRpc() throws Throwable {

    }

    @Override
    public void runScheduledJob() throws Throwable {
        ServiceHelper.summaryService().save();
        ServiceHelper.emailService().save();
        ServiceHelper.friendService().save();
    }
}
