package com.stars.modules.gm.gmhandler;

import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/4/30.
 */
public class SudoGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        /* 警告：只用于特殊情况（运营自定义GM失效的情况） */
    }
}
