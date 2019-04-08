package com.stars.modules.ride.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.ride.RideModule;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/19.
 */
public class RideGmHandler implements GmHandler {

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        long rideId = Long.parseLong(args[1]);
        RideModule rideModule = (RideModule) moduleMap.get(MConst.Ride);
        switch (args[0]) {
            case "getride":
                rideModule.getRide(Integer.parseInt(args[1]));
                break;
            case "upgradeone":
                rideModule.upgradeOneTimes(Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                break;
            case "upgradeten":
                rideModule.oneKeyUpgrade();
                break;
            case "geton":
                rideModule.getOn(Integer.parseInt(args[1]));
                break;
            case "getdown":
                rideModule.getDown();
                break;
        }
    }

}
