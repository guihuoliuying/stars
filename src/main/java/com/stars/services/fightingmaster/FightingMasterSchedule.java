package com.stars.services.fightingmaster;

import com.stars.services.ServiceHelper;

/**
 * Created by zhouyaohui on 2016/12/29.
 */
public class FightingMasterSchedule implements Runnable {
    @Override
    public void run() {
        ServiceHelper.fightingMasterService().save();
        ServiceHelper.fightingMasterService().reSendAward();
    }
}
