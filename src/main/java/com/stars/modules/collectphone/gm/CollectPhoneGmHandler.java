package com.stars.modules.collectphone.gm;

import com.stars.core.hotupdate.CommManager;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.collectphone.CollectPhoneModule;
import com.stars.modules.collectphone.CollectPhoneUtil;
import com.stars.modules.gm.GmHandler;
import com.stars.util.JsonUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class CollectPhoneGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        String params = args[0];
        String[] group = params.split("=");
        String action = group[0];
        CollectPhoneModule collectPhonemodule = (CollectPhoneModule) moduleMap.get(MConst.CollectPhone);
        switch (action) {
            case "phone": {
                String phone = group[1];
                collectPhonemodule.reqSubmit(5, "1|" + phone);
            }
            break;
            case "code": {
                String para = group[1];
                String[] split = para.split("&");
                Map<String, String> p = new HashMap();
                p.put("phone", split[0]);
                p.put("code", split[1]);
                String json = JsonUtil.toJson(p);
                collectPhonemodule.reqSubmit(5, "2|" + json);
            }
            break;
            case "question": {
                String para = group[1];
                String[] split = para.split("\\+");
                collectPhonemodule.reqSubmit(Integer.parseInt(split[0]), split[1]);
            }
            break;
            case "addChannel": {
                String para = group[1];
                String[] split = para.split("#");
                List<String> p1 = new ArrayList<>();
                p1.add("addForbidChannel4CollectPhone");
                List<String> ps = Arrays.asList(split);
                p1.addAll(ps);
                CommManager.addForbidChannel4CollectPhone(p1);
            }
            break;
            case "delChannel": {
                String para = group[1];
                String[] split = para.split("#");
                List<String> p1 = new ArrayList<>();
                p1.add("addForbidChannel4CollectPhone");
                List<String> ps = Arrays.asList(split);
                p1.addAll(ps);
                CommManager.delForbidChannel4CollectPhone(p1);
            }
            break;
            case "isForbid": {
                String para = group[1];
                CollectPhoneUtil.isForbidCollectPhoneChannel(Integer.parseInt(para));
            }
            break;
        }
        collectPhonemodule.warn("执行gm成功");
    }
}
