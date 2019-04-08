package com.stars.modules.optionalbox.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.optionalbox.OptionalBoxModule;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBoxGm implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        OptionalBoxModule optionalBoxModule = (OptionalBoxModule) moduleMap.get(MConst.OptionalBox);
        String arg = args[0];
        String[] group = arg.split("=");
        String action = group[0];
        switch (action) {
            case "list": {
                int itemId = Integer.parseInt(group[1]);

                optionalBoxModule.reqToolList(itemId, 1);
            }
            break;
            case "choose": {
                int[] params = StringUtil.toArray(group[1], int[].class, '+');
                optionalBoxModule.reqChooseItem(params[0], 1, params[1]);
            }
            break;

        }
        optionalBoxModule.warn("GM执行成功");
    }
}
