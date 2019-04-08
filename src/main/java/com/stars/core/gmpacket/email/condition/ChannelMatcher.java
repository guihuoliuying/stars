package com.stars.core.gmpacket.email.condition;

import com.stars.modules.email.pojodata.EmailConditionArgs;

import java.util.Set;

/**
 * Created by huwenjun on 2017/5/17.
 */
public class ChannelMatcher extends RoleMatcher {
    public Set<Integer> channelIds;

    public ChannelMatcher(Integer type, Long maxValue, Long minValue) {
        super(type, maxValue, minValue);
    }

    @Override
    public boolean match(EmailConditionArgs emailConditionArgs) {
        if (emailConditionArgs.getChannel() == null) {
            return false;
        }
        return getChannelIds().contains(emailConditionArgs.getChannel());
    }

    @Override
    public Integer getType() {
        return null;
    }

    public Set<Integer> getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(Set<Integer> channelIds) {
        this.channelIds = channelIds;
    }
}
