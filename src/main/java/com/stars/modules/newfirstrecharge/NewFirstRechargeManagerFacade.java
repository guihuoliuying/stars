package com.stars.modules.newfirstrecharge;

import com.stars.modules.newfirstrecharge.prodata.NewFirstRecharge;
import com.stars.modules.newfirstrecharge1.NewFirstRecharge1Manager;
import com.stars.modules.operateactivity.OperateActivityConstant;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/6.
 */
public class NewFirstRechargeManagerFacade {
    public static Map<Integer, NewFirstRecharge> getNewFirstRechargeMap(int activityType) {
         switch (activityType) {
            case OperateActivityConstant.ActType_NewFirstRecharge:
                return NewFirstRechargeManager.newFirstRechargeMap;
            case OperateActivityConstant.ActType_NewFirstRecharge1:
                return NewFirstRecharge1Manager.newFirstRechargeMap;
        }
        return null;
    }
}
