package com.stars.modules.email;

import com.stars.services.mail.prodata.EmailTemplateVo;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/1.
 */
public class EmailManager {
    public static String GAMETEXT_PREFIX = "<gt>";

    public static int EMAIL_LIMIT = 4;
    public static Map<Integer, EmailTemplateVo> templateMap;
    
    public static final int EMAIL_TYPE_1 = 1;//微信绑定奖励邮件
}
