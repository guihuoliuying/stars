package com.stars.modules.newsignin.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.newsignin.NewSigninModule;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/2/8 11:24
 */
public class NewSigninGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        NewSigninModule module = (NewSigninModule) moduleMap.get(MConst.SignIn);
//        switch (args[0]){
//            case "sign":
//                module.doSignin(Integer.parseInt(args[1]));
//                break;
//            case "acc":
//                module.accumulateAward(Integer.parseInt(args[1]));
//                break;
//            default:
//                break;
//        }
    }
}
