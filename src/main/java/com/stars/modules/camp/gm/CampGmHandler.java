package com.stars.modules.camp.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.camp.CampModule;
import com.stars.modules.camp.activity.CampActivity;
import com.stars.modules.camp.activity.imp.QiChuZhiZhengActivity;
import com.stars.modules.gm.GmHandler;

import java.util.Map;

/**
 * Created by huwenjun on 2017/6/29.
 */
public class CampGmHandler implements GmHandler {
    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        try {
            String actionStr = args[0];
            String[] actionGroup = actionStr.split("=");
            CampModule module = (CampModule) moduleMap.get(MConst.Camp);
            switch (actionGroup[0]) {
                case "join": {
                    int campType = Integer.parseInt(actionGroup[1]);
                    if (campType != 0) {
                        module.joinCamp(campType);
                    } else {
                        module.joinCamp(null);
                    }
                }
                break;
                case "my": {
                    module.reqMyCamp();
                }
                break;
                case "load": {
                    module.reqCurrentCampLoad();
                }
                break;
                case "openedcity": {
                    module.reqOpenedCampCities();
                }
                break;
                case "joincity": {
                    int cityId = Integer.parseInt(actionGroup[1]);
                    module.reqJoinCity(cityId);
                }
                break;
                case "sendcityrank": {
                    int cityId = Integer.parseInt(actionGroup[1]);
                    module.sendTheCityRank(cityId);
                }
                break;
                case "upgradeofficer": {
                    module.reqUpgradeOfficer();
                }
                break;
                case "upgradeofficerui": {
                    module.reqOfficerUpgradeUI();
                }
                break;
                case "takereward": {
                    module.takeDailyReward();
                }
                break;
                case "activitylist": {
                    module.reqActivityList();
                }
                break;
                case "missionlist": {
                    module.reqMissionList();
                }
                break;
                case "grantrare": {
                    module.remoteGm();
                }
                break;
                // test
                case "test":
                    QiChuZhiZhengActivity activity = (QiChuZhiZhengActivity) module.getCampActivityById(CampActivity.ACTIVITY_ID_QI_CHU_ZHI_ZHENG);
                    activity.getPlayerImageData();
                    break;
            }
            module.warn("GM执行成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
