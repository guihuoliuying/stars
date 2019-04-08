package com.stars.modules.camp.pojo;

import com.stars.util.LogUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huwenjun on 2017/6/30.
 */
public class CampCityResetMoment {
    private String cron;
    public static final int EVERY_DAY = 2;
    public static final int EVERY_WEEK = 3;
    public static final String formatTemplate = "HH:mm:ss";

    private CampCityResetMoment() {
    }

    public static CampCityResetMoment parse(String moment) {
        CampCityResetMoment campCityResetMoment = new CampCityResetMoment();
        try {
            String[] group = moment.split("\\|");
            switch (group.length) {
                case EVERY_DAY: {
                    String time = group[1];
                    SimpleDateFormat format = new SimpleDateFormat(formatTemplate);
                    Date date = format.parse(time);
                    String cron = "%s %s %s * * ?";
                    campCityResetMoment.cron = String.format(cron, date.getSeconds(), date.getMinutes(), date.getHours());
                }
                break;
                case EVERY_WEEK: {
                    String week = group[1];
                    String time = group[2];
                    SimpleDateFormat format = new SimpleDateFormat(formatTemplate);
                    Date date = format.parse(time);
                    String cron = "%s %s %s ? * %s";
                    int weekValue = Integer.valueOf(week);
                    if(weekValue==7){
                    	weekValue = 1;
                    }else{
                    	weekValue += 1;
                    }
                    campCityResetMoment.cron = String.format(cron, date.getSeconds(), date.getMinutes(), date.getHours(), weekValue);
                }
                break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            LogUtil.error(e.getMessage(), e);
        }
        return campCityResetMoment;
    }

    public String getCron() {
        return cron;
    }


    public static void main(String[] args) {
        CampCityResetMoment parse = CampCityResetMoment.parse("2|3|00:00:00");
        System.out.println(parse.getCron());
    }
}
