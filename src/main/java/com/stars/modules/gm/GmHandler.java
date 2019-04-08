package com.stars.modules.gm;

import com.stars.core.module.Module;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/1/14.
 */
public interface GmHandler {

    void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception;

}
