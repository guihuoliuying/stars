package com.stars.modules.gameboard.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gameboard.GameboardModule;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by chenkeyu on 2017/1/7 14:06
 */
public class QueryGameboardGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        GameboardModule module = (GameboardModule) moduleMap.get(MConst.Gameboard);
        module.popGameboard();
    }
}
