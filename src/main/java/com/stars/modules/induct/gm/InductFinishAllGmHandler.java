package com.stars.modules.induct.gm;

import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.induct.InductManager;
import com.stars.modules.induct.InductModule;
import com.stars.modules.induct.prodata.InductVo;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * 完成所有引导;
 * Created by panzhenfeng on 2016/12/9.
 */
public class InductFinishAllGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            InductModule inductModule = (InductModule) moduleMap.get(MConst.Induct);
            for(Map.Entry<Integer, InductVo> kvp : InductManager.inductVoMap.entrySet()){
                inductModule.forceFinish(kvp.getKey());
            }
            PlayerUtil.send(roleId, new ClientText("执行成功, inductfinishall" ));
        } catch (Exception e) {
            PlayerUtil.send(roleId, new ClientText("执行失败, inductfinishall" ));
            LogUtil.error("", e);
        }
    }
}