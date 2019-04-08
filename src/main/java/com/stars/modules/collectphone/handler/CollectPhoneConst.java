package com.stars.modules.collectphone.handler;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class CollectPhoneConst {
    /**
     0	成功
     1	参数错误
     2	加密信息错误
     3	不支持的协议号
     4	其他错误
     1001	今日发送验证码次数达到上限，请明日再来
     2001	验证码错误，请重新输入

     */
    public static final int status_success = 0;
    public static final int status_account_error = 1;
    public static final int status_encrypt_error = 2;
    public static final int status_times_out = 1001;
    public static final int status_code_error = 2001;
}
