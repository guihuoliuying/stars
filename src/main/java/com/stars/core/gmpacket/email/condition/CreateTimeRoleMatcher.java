package com.stars.core.gmpacket.email.condition;

import com.stars.modules.email.pojodata.EmailConditionArgs;

import java.util.Calendar;

/**
 * Created by huwenjun on 2017/3/27.
 */
public class CreateTimeRoleMatcher extends RoleMatcher {

    public CreateTimeRoleMatcher(Integer type, Long maxValue, Long minValue) {
        super(type, maxValue, minValue);

    }

    @Override
    public boolean match(EmailConditionArgs emailConditionArgs) {
        long createTimeStamp = emailConditionArgs.getCreateTime();
        Calendar createTime = Calendar.getInstance();
        createTime.setTimeInMillis(createTimeStamp);
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(getMinValue() * 1000L);
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(getMaxValue() * 1000L);
        if (createTime.equals(beginTime) || createTime.equals(endTime))
            return true;
        if (createTime.after(beginTime) && createTime.before(endTime))
            return true;
        return false;
    }

    @Override
    public Integer getType() {
        return CreateTimeRoleMatcher;
    }

}
