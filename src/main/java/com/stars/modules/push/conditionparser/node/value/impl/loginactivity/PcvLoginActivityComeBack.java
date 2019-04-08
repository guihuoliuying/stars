package com.stars.modules.push.conditionparser.node.value.impl.loginactivity;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.oldplayerback.OldPlayerBackModule;
import com.stars.modules.push.conditionparser.node.value.PushCondValue;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class PcvLoginActivityComeBack extends PushCondValue {
    @Override
    public Object eval(Map<String, Module> moduleMap) {
        OldPlayerBackModule oldPlayerBackModule = (OldPlayerBackModule) moduleMap.get(MConst.OldPlayerBack);
        int state = oldPlayerBackModule.getState();
        if (state == 1) {
            return 1L;
        } else {
            return 0L;
        }
    }

    @Override
    public String toString() {
        return "loginactivity_comback";
    }
}
