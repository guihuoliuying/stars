package com.stars.modules.operateactivity.opentime;

import com.stars.util.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by gaopeidian on 2016/12/6.
 * 时间控制类：固定时间+开服前几天内开放
 */
public class ActOpenTime2 extends ActOpenTimeBase {
    private String startDateString = "";
    private String endDateString = "";
    private Date startDate = null;
    private Date endDate = null;

    private int serverLimitDay = -1;//开服前几天内不开

    ActOpenTime2(String value) {
        this.openTimeType = ActOpenTimeBase.OpenTimeType2;

        if (value == null || value.equals("") || value.equals("0")) {
            return;
        }
        String sts[] = value.split("\\;");

        //检查
        if (sts.length >= 2) {
            String dateStr = sts[0];
            String dateSts[] = dateStr.split("\\&");
            if (dateSts.length >= 2) {
                this.startDateString = dateSts[0];
                this.endDateString = dateSts[1];
                this.startDate = DateUtil.toDate(startDateString);
                this.endDate = DateUtil.toDate(endDateString);
            } else {

            }

            String serverLimitDayStr = sts[1];
            this.serverLimitDay = Integer.parseInt(serverLimitDayStr);
        } else {

        }
    }

    public String getStartDateString() {
        return this.startDateString;
    }

    public String getEndDateString() {
        return this.endDateString;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public int getServerLimitDay() {
        return this.serverLimitDay;
    }

    public static Calendar getStartCalendar(ActOpenTime2 openTime) {
        Date startDays = openTime.getStartDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startDays.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }
}
