package com.stars.modules.oldplayerback.pojo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huwenjun on 2017/7/14.
 */
public class ActOldPlayerBackDate {
    private Date beginDate;
    private Date endDate;

    private ActOldPlayerBackDate(Date beginDate, Date endDate) {
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    public boolean inActivityTime(Date now) {
        return now.after(beginDate) && now.before(endDate);
    }

    public static ActOldPlayerBackDate parse(String comeback_reward_time) {
        String[] group = comeback_reward_time.split("&");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date begin = sdf.parse(group[0]);
            Date end = sdf.parse(group[1]);
            ActOldPlayerBackDate actOldPlayerBackDate = new ActOldPlayerBackDate(begin, end);
            return actOldPlayerBackDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date getEndDate() {
        return endDate;
    }
}
