package com.stars.server.login;

/**
 * Created by liuyuheng on 2016/1/5.
 */
public class LoginConstant {
    public static String returnIp;
    public static int returnPort;

    public static int LOGIN_INTERVAL = 1000 * 5;// 登录请求间隔ms

    public static byte LOGINCHECK_FAIL = 0;
    public static byte LOGINCHECK_SUC = 1;

    /* 协议号 */
    public static final short SERVER_HEARTBEAT = 0x0001; // 心跳（暂定未使用）
    public static final short SERVER_LOGINCHECK = 0x0006; // 登陆验证请求
    public static final short CLIENT_LOGINCHECK = 0x0007; // 登陆验证响应
    public static final short SERVER_REGISTER = 0x6005;// 注册请求（暂定未使用）
    public static final short CLIENT_WARNING = 0x0008;// 提示信息
}
