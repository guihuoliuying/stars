package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * 自测用的
 * Created by chenkeyu on 2017-03-20 19:47
 */
public class NewServerRankGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        /*if (Integer.parseInt(args[0]) == 213001) {
            ServiceHelper.opActFamilyFightScore().closeFamilyFightScore(213001);
        } else if (Integer.parseInt(args[0]) == 211001) {
            ServiceHelper.opActFightScore().closeFightScore(211001);
        }*/
    }
}
