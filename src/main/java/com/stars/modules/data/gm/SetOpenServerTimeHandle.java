package com.stars.modules.data.gm;

import com.stars.core.SystemRecordMap;
import com.stars.core.module.Module;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by zhouyaohui on 2016/9/12.
 */
public class SetOpenServerTimeHandle implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        /**args: yyyyMMddHHmmss */
        if (args.length != 1) return;
        long ymd = Long.valueOf(args[0]);
        SystemRecordMap.update("openServerTime", ymd);
    }
}
