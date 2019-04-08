package com.stars.modules.opactbenefittoken.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.opactbenefittoken.OpActBenefitTokenModule;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/19.
 */
public class OpActBenefitTokenGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        OpActBenefitTokenModule module = (OpActBenefitTokenModule) moduleMap.get(MConst.OpActBenefitToken);
        try {
            switch (args[0]) {
                case "reset":
                    module.onDailyReset(null, false);
                    break;
            }
        } catch (Throwable cause) {
            LogUtil.error("", cause);
        }
    }
}
