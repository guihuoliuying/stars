package com.stars.modules.gm.gmhandler;

import com.stars.core.AccessControl;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.module.ModuleManager;
import com.stars.modules.gm.GmHandler;
import com.stars.network.PacketSet;
import com.stars.network.server.packet.Packet;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/13.
 */
public class AccessControlGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        boolean flag = false;
        switch (args[0]) {
            case "open":
                flag = true;
                break;
            case "close":
                flag = false;
                break;
            default:
                throw new IllegalArgumentException("格式错误");
        }
        List<String> list = new LinkedList<>();
        for (int i = 1; i < args.length; i++) {
            list.add(args[i]);
        }
        openOrClose(flag, list);
    }

    public static void openOrClose(boolean isOpen, List<String> moduleNameList) throws Exception {
        for (int i = 0; i < moduleNameList.size(); i++) {
            AbstractModuleFactory factory = (AbstractModuleFactory) ModuleManager.get(moduleNameList.get(i));
            PacketSet packetSet = factory.getPacketSet();
            for (Class<? extends Packet> clazz : packetSet.getPacketList()) {
                AccessControl.setBitmap(clazz.newInstance().getType(), isOpen);
            }
            AccessControl.updateBitmap();
        }
    }

}
