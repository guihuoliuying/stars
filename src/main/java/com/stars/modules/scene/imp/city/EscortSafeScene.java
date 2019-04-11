package com.stars.modules.scene.imp.city;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.scene.ArroundScene;
import com.stars.network.server.buffer.NewByteBuffer;
import io.netty.buffer.UnpooledByteBufAllocator;

import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/8.
 */
public class EscortSafeScene extends ArroundScene {

    private String position;

    @Override
    public String getArroundId(Map<String, Module> moduleMap) {
        RoleModule roleModule = (RoleModule) moduleMap.get(MConst.Role);
        return "";
    }

    @Override
    public String getPosition() {
        return position;
    }

    @Override
    public boolean canEnter(Map<String, Module> moduleMap, Object obj) {
        if("".equals(obj)){
            return false;
        }
        return true;
    }

    @Override
    public void enter(Map<String, Module> moduleMap, Object obj) {
        byte[] data = (byte[]) obj;
        com.stars.network.server.buffer.NewByteBuffer buffer = new NewByteBuffer(UnpooledByteBufAllocator.DEFAULT.buffer());
        buffer.getBuff().writeBytes(data);
    }

    @Override
    public void exit(Map<String, Module> moduleMap) {

    }

    @Override
    public boolean isEnd() {
        return false;
    }
}
