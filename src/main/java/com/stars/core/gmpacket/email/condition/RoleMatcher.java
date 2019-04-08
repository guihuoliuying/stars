package com.stars.core.gmpacket.email.condition;

import com.stars.modules.email.pojodata.EmailConditionArgs;

/**
 * Created by huwenjun on 2017/3/27.
 */
public abstract class RoleMatcher {
    public static final Integer LevelRoleMatcher = 1;
    public static final Integer CreateTimeRoleMatcher = 2;
    public static final Integer ChannelRoleMatcher = 9;
    private Long maxValue;
    private Long minValue;
    private Integer type;

    public RoleMatcher(Integer type, Long maxValue, Long minValue) {
        this.type = type;
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    public abstract boolean match(EmailConditionArgs emailConditionArgs);


    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public abstract Integer getType();

    public void setType(Integer type) {
        this.type = type;
    }

}
