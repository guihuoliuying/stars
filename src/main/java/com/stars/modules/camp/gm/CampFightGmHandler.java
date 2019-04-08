package com.stars.modules.camp.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuDaZuoZhanActivity;
import com.stars.modules.gm.GmHandler;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * Created by huwenjun on 2017/7/26.
 */
public class CampFightGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        CampModule module = (CampModule) moduleMap.get(MConst.Camp);
        QiChuDaZuoZhanActivity qiChuDaZuoZhanActivity = (QiChuDaZuoZhanActivity) module.getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_DA_ZUO_ZHAN);
        try {
            String actionStr = args[0];
            String[] actionGroup = actionStr.split("=");
            switch (actionGroup[0]) {
                case "main": {
                    qiChuDaZuoZhanActivity.reqMainUIDate();
                }
                break;
                case "match": {
                    qiChuDaZuoZhanActivity.startMatching();
                }
                break;
                case "num": {
                    int roleNum = Integer.parseInt(actionGroup[1]);
                    MainRpcHelper.campRemoteFightService().updatePVPRoleNum(MultiServerHelper.getCampServerId(), roleNum);
                }
            }
            module.warn("gm执行成功");
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            module.warn("gm执行错误");
        }
    }
}
