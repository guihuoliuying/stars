package com.stars.multiserver.rpctest;

import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataModuleFactory;
import com.stars.modules.email.EmailModuleFactory;
import com.stars.modules.role.RoleModuleFactory;
import com.stars.modules.tool.ToolModuleFactory;
import com.stars.multiserver.chat.ChatRpcServiceManager;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.server.Business;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.ExecuteManager;

/**
 * Created by zhaowenshuo on 2016/10/25.
 */
public class RpcTestStartup implements Business {

    @Override
    public void init() throws Exception {
        try {
            DBUtil.init();// 初始化数据库连接池(proxool)
            ExecuteManager.init();
            PacketManager.loadCorePacket();
            new DataModuleFactory().loadProductData();
            new RoleModuleFactory().loadProductData();
            new ToolModuleFactory().loadProductData();
            new EmailModuleFactory().loadProductData();
            ServiceSystem.init();
            SchedulerManager.init();
            ServiceHelper.init(new RpcTestServiceManager());
            //聊天
            ServiceHelper.init(new ChatRpcServiceManager());
        } catch (Throwable cause) {
            throw new RuntimeException(cause);
        }
    }

    @Override
    public void clear() {

    }

    @Override
    public void dispatch(Packet packet) {
//        RpcManager.handlePacket(packet);
    }
}
