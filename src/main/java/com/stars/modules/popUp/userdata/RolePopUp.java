package com.stars.modules.popUp.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.popUp.PopUpConstant;
import com.stars.modules.popUp.PopUpManager;
import com.stars.modules.popUp.prodata.PopUpInfo;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by wuyuxing on 2017/3/27.
 */
public class RolePopUp extends DbRow {

    private long roleId;
    private List<Integer> forbiddenList = new ArrayList<>();
    private int weekResetTime;//周重置时间记录（当前周 周三零点）

    public RolePopUp() {
    }

    public RolePopUp(long roleId) {
        this.roleId = roleId;
    }

    public String getForbidden() {
        return StringUtil.makeString(forbiddenList,',');
    }

    public void setForbidden(String forbidden) throws Exception {
        if(StringUtil.isEmpty(forbidden)) return;
        forbiddenList = StringUtil.toArrayList(forbidden,Integer.class,',');
    }

    public boolean isForbidden(int popId){
        if(StringUtil.isEmpty(forbiddenList)) return false;
        return forbiddenList.contains(popId);
    }

    public void addToForbidden(int popId){
        if(forbiddenList == null){
            forbiddenList = new ArrayList<>();
        }
        if(forbiddenList.contains(popId)) return;
        forbiddenList.add(popId);
    }

    public void dailyReset(){
    	List<Integer> tempList = new ArrayList<>(forbiddenList);
        int size = tempList.size();
        PopUpInfo popUpInfo = null;
        for(int i=size-1;i>=0;i--){
        	popUpInfo = PopUpManager.getPopUpInfoById(tempList.get(i));
        	if(popUpInfo.getFrequency()==PopUpConstant.FREQUENCY_TYPE_DAILY_ONE_TIMES){
        		tempList.remove(i);
        	}
        }
        forbiddenList = tempList;
    }
    
    public void weeklyReset(){
    	List<Integer> tempList = new ArrayList<>(forbiddenList);
        int size = tempList.size();
        PopUpInfo popUpInfo = null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(day==Calendar.WEDNESDAY){        	
        	if(hour<PopUpConstant.WEEKLY_RESET_HOUR_NUM) return;
        }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        calendar.set(Calendar.HOUR_OF_DAY, PopUpConstant.WEEKLY_RESET_HOUR_NUM);
        int wednesdayTime = (int)(calendar.getTimeInMillis()/1000);//周三 6点 时间值
        if(day<Calendar.WEDNESDAY){//周日，周一，周二   取上周三时间
        	wednesdayTime = wednesdayTime - PopUpConstant.WEEK_TIMES;
        }
        int currentTime = DateUtil.getCurrentTimeInt();
        if(currentTime<wednesdayTime) return;
        int wednesdayZeroTime = (int)(DateUtil.getZeroTime(calendar)/1000);
        if(day<Calendar.WEDNESDAY){//周日，周一，周二   取上周三时间
        	wednesdayZeroTime = wednesdayZeroTime - PopUpConstant.WEEK_TIMES;
        }
        if(weekResetTime!=wednesdayZeroTime){        	
        	for(int i=size-1;i>=0;i--){  
        		popUpInfo = PopUpManager.getPopUpInfoById(tempList.get(i));
        		if(popUpInfo == null || popUpInfo.getFrequency()==PopUpConstant.FREQUENCY_TYPE_WEEKLY_ONE_TIMES){
                    // 移除不存在，或者过期的数据
        			tempList.remove(i);
        		}
        	}
        }
    	weekResetTime = wednesdayZeroTime;
    	forbiddenList = tempList;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolepopup", "`roleid`=" + roleId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolepopup", "`roleid`=" + roleId);
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

	public int getWeekResetTime() {
		return weekResetTime;
	}

	public void setWeekResetTime(int weekResetTime) {
		this.weekResetTime = weekResetTime;
	}
}
