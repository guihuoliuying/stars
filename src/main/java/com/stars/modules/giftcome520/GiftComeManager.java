package com.stars.modules.giftcome520;

import java.util.Date;

/**
 * Created by huwenjun on 2017/4/15.
 */
public class GiftComeManager {

    public static final int ACT_NOT_BEGIN = -1;
    public static final int ACT_TAKED_REWARD = 0;
    public static final int ACT_TAKE_REWARD = 1;
    public static final int ACT_END = 2;
    public static final int ACT_NOT_ACTIVE = 3;
    public static String beginDate;//开始时间
    public static String endDate;//结束时间
    public static int npcLoveGiftRewardDropGroupId;//dropgroupid 奖励物品组id
    public static Date benginDateTime;//活动开始时间
    public static Date endDateTime;//活动结束时间
}
