package com.stars.modules.collectphone;

import com.stars.core.SystemRecordMap;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;
import com.stars.modules.data.DataManager;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huwenjun on 2017/9/21.
 */
public class CollectPhoneUtil {
    public static Set<Integer> forbidCollectPhoneChannelSet = null;

    /**
     * 是否是禁止收集号码的渠道
     *
     * @param channelId
     * @return
     */
    public static boolean isForbidCollectPhoneChannel(int channelId) {
        if (forbidCollectPhoneChannelSet == null) {
            initForbidCollectPhoneChannels();
        }
        return forbidCollectPhoneChannelSet.contains(channelId);
    }

    /**
     * 添加
     *
     * @param channelIds
     */

    public static void addForbidCollectPhoneChannels(Set<Integer> channelIds) {
        try {
            if (forbidCollectPhoneChannelSet == null) {
                initForbidCollectPhoneChannels();
            }
            forbidCollectPhoneChannelSet.addAll(channelIds);
            updateForbidCollectPhoneChannels2Db();
        } catch (Exception e) {
            com.stars.util.LogUtil.error("更新禁止收集号码渠道", e);
        }
    }

    public static void updateForbidCollectPhoneChannels2Db() {
        String forbidCollectPhoneChannels = StringUtil.makeString(forbidCollectPhoneChannelSet, '#');
        SystemRecordMap.update("forbidCollectPhoneChannels", forbidCollectPhoneChannels);
    }

    /**
     * 删除
     *
     * @param channelIds
     */
    public static void delForbidCollectPhoneChannels(Set<Integer> channelIds) {
        try {
            if (forbidCollectPhoneChannelSet == null) {
                initForbidCollectPhoneChannels();
            }
            forbidCollectPhoneChannelSet.removeAll(channelIds);
            updateForbidCollectPhoneChannels2Db();
        } catch (Exception e) {
            com.stars.util.LogUtil.error("更新禁止收集号码渠道", e);
        }
    }

    public static void initForbidCollectPhoneChannels() {
        try {
            if (StringUtil.isEmpty(SystemRecordMap.forbidCollectPhoneChannels)) {
                forbidCollectPhoneChannelSet = new HashSet<>();
            } else {
                forbidCollectPhoneChannelSet = StringUtil.toHashSet(SystemRecordMap.forbidCollectPhoneChannels, Integer.class, '#');
            }
        } catch (Exception e) {
            LogUtil.error("初始化禁止收集手机号码渠道错误", e);
        }
    }

    /**
     * 获取短信验证码剩余冷却时间
     * @param roleCollectPhone
     * @return
     */
    public static int getRemainSecond(RoleCollectPhone roleCollectPhone) {
        int remainSecond;
        if (roleCollectPhone.getLastMsgTime() == 0) {
            remainSecond = -1;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(roleCollectPhone.getLastMsgTime());
            int codecd = DataManager.getCommConfig("stepoperateact_codecd", 120);
            calendar.add(Calendar.SECOND, codecd);
            Date endDatetime = calendar.getTime();
            remainSecond = DateUtil.getSecondsBetweenTwoDates(new Date(), endDatetime);
        }
        return remainSecond;
    }
}
