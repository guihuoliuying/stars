package com.stars.modules.luckycard.gm;

import com.stars.core.hotupdate.CommManager;
import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.luckycard.LuckyCardModule;
import com.stars.modules.luckycard.pojo.LuckyCardAnnounce;
import com.stars.services.ServiceHelper;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/28.
 */
public class LuckyCardGm implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        LuckyCardModule luckyCardModule = (LuckyCardModule) moduleMap.get(MConst.LuckyCard);
        String arg = args[0];
        String[] group = arg.split("=");
        String action = group[0];
        switch (action) {
            case "main": {
                luckyCardModule.reqMainData(true);
            }
            break;
            case "time": {
                int time = Integer.parseInt(group[1]);
                luckyCardModule.reqLuckyGo(time);
            }
            break;
            case "announce": {
                LuckyCardAnnounce luckyCardAnnounce = new LuckyCardAnnounce(1, "张三", 1);
                ServiceHelper.luckyCardService().luckyAnnounce(luckyCardAnnounce);
            }
            break;
            case "resolve": {
                int cardId = Integer.parseInt(group[1]);
                luckyCardModule.reqResolve(cardId);
            }
            break;
            case "get": {
                int cardId = Integer.parseInt(group[1]);
                luckyCardModule.reqGet(cardId);
            }
            break;
            case "queryTableCount": {
                String tableStr = group[1];
                List<String> _tables = StringUtil.toArrayList(tableStr, String.class, '#');
                List<String> tables = new ArrayList<>();
                tables.add("queryTableCount");
                tables.addAll(_tables);
                CommManager commManager = new CommManager();
                String s = commManager.comm(tables);
                System.err.println(s);
                luckyCardModule.warn(s);
            }
        }
        luckyCardModule.warn("GM执行成功");
    }
}
