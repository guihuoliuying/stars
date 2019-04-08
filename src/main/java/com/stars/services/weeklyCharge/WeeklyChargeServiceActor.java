package com.stars.services.weeklyCharge;

import com.stars.db.DBUtil;
import com.stars.services.ServiceSystem;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;

/**
 * Created by chenxie on 2017/5/9.
 */
public class WeeklyChargeServiceActor extends ServiceActor implements WeeklyChargeService {


    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd("weeklyChargeService", this);
    }

    @Override
    public void closeActivity(int activityId){
        String sql = "delete from roleweeklycharge";
        try {
            DBUtil.execSql(DBUtil.DB_USER,sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printState() {

    }
}


