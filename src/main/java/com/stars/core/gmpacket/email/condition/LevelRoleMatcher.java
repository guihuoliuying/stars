package com.stars.core.gmpacket.email.condition;

import com.stars.modules.email.pojodata.EmailConditionArgs;

/**
 * Created by huwenjun on 2017/3/27.
 */
public class LevelRoleMatcher extends RoleMatcher {


    public LevelRoleMatcher(Integer type, Long maxValue, Long minValue) {
        super(type, maxValue, minValue);

    }

    @Override
    public boolean match(EmailConditionArgs emailConditionArgs) {
        int roleLevel = emailConditionArgs.getLevel();
        if (roleLevel >= getMinValue() && roleLevel <= getMaxValue()) {
            return true;
        }
        return false;
    }

    @Override
    public Integer getType() {
        return LevelRoleMatcher;
    }
}
