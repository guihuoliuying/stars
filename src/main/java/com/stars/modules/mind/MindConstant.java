package com.stars.modules.mind;

/**
 * 心法常量;
 * Created by gaopeidian on 2016/9/22.
 */
public class MindConstant {
	//激活心法的条件类型
    public static final int ACTIVE_TYPE_ROLE_LEVEL = 1;
    public static final int ACTIVE_TYPE_UNLOCK_TRUMP = 2;
    public static final int ACTIVE_TYPE_UNLOCK_TRUMP_NUM = 3;
    
    //心法状态值
    public static final int MIND_STATE_ACTIVE = 0;//心法状态，已激活
    public static final int MIND_STATE_NOT_ACTIVE = 1;//心法状态，未激活且可激活
    public static final int MIND_STATE_CAN_NOT_ACTIVE = 2;//心法状态，未激活且不可激活
}
