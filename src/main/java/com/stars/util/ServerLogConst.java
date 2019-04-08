package com.stars.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ServerLogConst {

    public static String LOG_SPLIT = "|";
    //日志打印控制开关
    public static boolean logDebug = false;

    //运营日志
    public static Logger user_info = LogManager.getLogger("user_info");// 机型打开
    public static Logger core_account = LogManager.getLogger("core_account");// 账号注册/登陆登出
    public static Logger core_gamesvr = LogManager.getLogger("core_gamesvr");// 区账号注册/登陆登出
    public static Logger core_role = LogManager.getLogger("core_role");// 角色注册/登陆登出
    public static Logger core_item = LogManager.getLogger("core_item");
    public static Logger core_pay = LogManager.getLogger("core_pay");
    public static Logger core_coin = LogManager.getLogger("core_coin");
    public static Logger core_case = LogManager.getLogger("core_case");
    public static Logger core_stat_1 = LogManager.getLogger("core_stat_1");
    public static Logger core_stat_2 = LogManager.getLogger("core_stat_2");
    public static Logger core_client = LogManager.getLogger("core_client");
    public static Logger core_finance = LogManager.getLogger("core_finance");
    public static Logger core_activity = LogManager.getLogger("core_activity");
    public static Logger core_task = LogManager.getLogger("core_task");
    public static Logger core_action = LogManager.getLogger("core_action");
    public static Logger core_market = LogManager.getLogger("core_market");
    public static Logger dynamic_4 = LogManager.getLogger("dynamic_4");
    public static Logger static_4 = LogManager.getLogger("static_4");
    public static Logger monitor_mail = LogManager.getLogger("monitor_mail");//邮件日志
    public static Logger vipInfo = LogManager.getLogger("vipInfo");//vip信息日志（玩家电话 qq）

    // 聊天日志
    public static Logger dynamic_chat = LogManager.getLogger("dynamic_chat");
    //调查问卷
    public static Logger dynamic_survey = LogManager.getLogger("dynamic_survey");

    //开发日志
    public static Logger console = LogManager.getLogger("console");
    public static Logger exception = LogManager.getLogger("exception");
    public static Logger state = LogManager.getLogger("server_state");    // 服务状态打印日志
    public static Logger sql_err = LogManager.getLogger("sql_err");

    //活动日志常量
    public static final byte ACTIVITY_START = -1;
    public static final byte ACTIVITY_WIN = 1;
    public static final byte ACTIVITY_FAIL = 0;

    public static final byte STATUS_LOGIN = 0;    //角色登入
    public static final byte STATUS_LOGOUT = 1;    //角色登出

}
