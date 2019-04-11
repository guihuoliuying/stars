package com.stars.modules.serverLog;

import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.giftpackage.GiftLogEvent;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.daily.prodata.DailyBallStageVo;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.email.event.EmailLogEvent;
import com.stars.modules.push.event.PushActivedEvent;
import com.stars.modules.push.event.PushInfo;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.multiserver.MultiServerHelper;
import com.stars.services.ServiceHelper;
import com.stars.services.chat.ChatMessage;
import com.stars.services.family.main.userdata.FamilyLogData;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.services.summary.Summary;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import com.stars.util.StringUtil;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;



public class ServerLogModule extends AbstractModule {
    //基本信息,调用getBaseInfo()初始化
    private String account = null;                                //账号
    private String roleid = null;                                 //角色id
    private String serverId = MultiServerHelper.getServerId() + ""; //区ID
    private String level = "1";                                   //角色等级
    private String vip = "0";                                     //vip等级
    private String login_channel = null;                          //登陆渠道(子渠道)
    private String reg_channel = null;                            //注册渠道(子渠道)
    private String uid = null;                                    //uid
    private String time = null;                                   //当前时间
    private String fightScore = "1";                              //战力
    private String phoneSystem = null;                            //手机系统
    private String phoneNet = null;                               //网络环境
    private String verision = null;                                //客户端版本
    private String job = null;                                    //职业
    private String platForm = null;                                 //手机平台
    private String roleCreateTime = null;                         //角色创建时间
    private String accoutRegisterTime = null;                     //账号注册时间
    private String mainChannel = null;                            //主渠道
    private String osVersion = null;                              //操作系统版本

    public ServerLogModule(long id, Player self,
                           EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.ServerLog, id, self, eventDispatcher, moduleMap);

    }

    /*****************************************************************
     *模块还未初始化字段接收（相当恶心）
     /*****************************************************************
     /**
     * 接收其他模块传来的数据
     * @param key
     * @param value
     */
    public void accept(String key, String value) {
        try {
//            ServerLogConst.console.info("accept:" + key + "-->" + value);
            Method m = this.getClass().getDeclaredMethod("set" + key.substring(0, 1).toUpperCase() + key.substring(1), String.class);
            m.invoke(this, value);
        } catch (Exception e) {
            ServerLogConst.exception.info(e.getMessage());
        }
    }

    /********************************************************************
     * 模块初始化已完成状态下变化字段监听接收
     * ******************************************************************
     */
    public void doVipLevelUpEvent(VipLevelupEvent event) {
        this.vip = event.getNewVipLevel() + "";
    }

    public void doRoleLevelUpEvent(RoleLevelUpEvent event) {
        this.level = event.getNewLevel() + "";

    }

    //1	付费成功，发货后打印
    //打印时间|core_pay|用户登录区ID|getTime()|pay|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职业等级|充值币数量（新增，一定为正）|充值金额（分）|充值入口|订单id|此处为空|pay_finish|此处为空|1|为空"
    public void Log_core_pay(String orderNo, int money, int gold, String comeFrom, int vipLv, String account, long roleId) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_pay")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("pay")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + account)
                .append("|").append(account)
                .append("|").append(roleId)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(vipLv)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append(level)//职业等级
                .append("|").append(gold)//充值币数量（新增，一定为正）
                .append("|").append(money * 100)//充值金额（分）
                .append("|").append(comeFrom)//充值入口
                .append("|").append(orderNo)//订单id
                .append("|").append("")//此处为空
                .append("|").append("pay_finish")//
                .append("|").append("")//此处为空
                .append("|").append("1")//
                .append("|").append("")//为空
        ;
        com.stars.util.LogUtil.info(logStr.toString());
        ServerLogConst.core_pay.info(logStr.toString());
    }

    public void doFightScoreChangeEvent(FightScoreChangeEvent event) {
        this.fightScore = event.getNewFightScore() + "";
    }

    public void doNoticeMainServerAddTool(byte status) {
        if (status == (byte) 1) {
            Log_core_activity(ServerLogConst.ACTIVITY_WIN, ThemeType.ACTIVITY_103.getThemeId(), makeJuci(), ThemeType.ACTIVITY_103.getThemeId(), 0, 0);
        } else {
            Log_core_activity(ServerLogConst.ACTIVITY_FAIL, ThemeType.ACTIVITY_103.getThemeId(), makeJuci(), ThemeType.ACTIVITY_103.getThemeId(), 0, 0);
        }
        ServiceHelper.roleService().notice(id(), new DailyFuntionEvent(DailyManager.DAILYID_FIGHTING_MASTER, 1));
    }

    public void doSpecialAccountEvent(long roleId, String content, boolean isSelf) {
        String account = SpecialAccountManager.getAccountByRoleId(roleId);
        if (account == null || account.equals("")) {
            com.stars.util.LogUtil.error("特殊账号出现异常--roleId:{} 对应的account为null");
            return;
        }
        String roleName = "";
        if (isSelf) {
            RoleModule roleModule = module(MConst.Role);
            roleName = roleModule.getRoleRow().getName();
        } else {
            Summary summary = ServiceHelper.summaryService().getSummary(roleId);
            RoleSummaryComponent roleSummary = (RoleSummaryComponent) summary.getComponent(MConst.Role);
            roleName = roleSummary.getRoleName();
        }
        dynamic_4_Log(ThemeType.SPECIAL_ACCOUNT.getThemeId(), account, roleId, roleName, content);
    }

    public void doEmailLog(EmailLogEvent event) {
        Log_monitor_mail(event);
    }

    public void doGiftLog(GiftLogEvent event) {
        Log_core_market(event);
    }

    /***********************************************************************/


    /**********************************************************************
     * 日志格式化区
     **********************************************************************/
    //打印时间|core_account|用户登录区ID|日志的触发时间|account_act|用户登录区ID|注册渠道ID_UID|UID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|手机运营商|2g/3g/4g/wifi等|手机型号|当前游戏客户端版本|ip地址（不包含端口）|IMEI信息|mac地址|sdk版本|sdk_id|此处为空|此处为空|account_logout|此处为空|0=失败/1=成功|online_time:本次在线时长（单位秒）(整数)
    public void Log_core_account(LoginInfo info, String onlineTime) {
        StringBuffer logStr = new StringBuffer();
        logStr.append("core_account")
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append("account_act")
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(info.getChannel().split("@")[0] + "_" + info.getUid())//"注册渠道ID_UID"
                .append("|").append(info.getUid())
                .append("|").append(info.getRegChannel())
                .append("|").append(info.getRegChannel())
                .append("|").append(info.getPlatForm())
                .append("|").append(info.getAccoutRegisterTime())
                .append("|").append(info.getPhoneNet())
                .append("|").append(info.getNet())
                .append("|").append(info.getPhoneType())
                .append("|").append(info.getVerision())
                .append("|").append(info.getIp())
                .append("|").append(info.getImei())
                .append("|").append(info.getMac())
                .append("|").append(info.getSdkVersion())//sdk版本
                .append("|").append(info.getChannel().split("@")[2])
                .append("|").append("")
                .append("|").append("")
                .append("|").append("account_logout")
                .append("|").append("")
                .append("|").append("1")
                .append("|").append(onlineTime)
                .append("|").append("")
                .append("|").append(info.getOsVersion())
        ;
        ServerLogConst.core_account.info(logStr);
    }


    //打印时间|core_account|用户登录区ID|日志的触发时间|account_act|用户登录区ID|注册渠道ID_UID|UID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|手机运营商|2g/3g/4g/wifi等|手机型号|当前游戏客户端版本|ip地址（不包含端口）|IMEI信息|mac地址|sdk版本|sdk_id|此处为空|此处为空|account_logout|此处为空|0=失败/1=成功|online_time:本次在线时长（单位秒）(整数)
    public static void static_core_account_int(LoginInfo info, String onlineTime) {
        StringBuffer logStr = new StringBuffer();
        logStr.append("core_account")
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append("account_act")
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(info.getChannel().split("@")[0] + "_" + info.getUid())//"注册渠道ID_UID"
                .append("|").append(info.getUid())
                .append("|").append(info.getRegChannel())
                .append("|").append(info.getRegChannel())
                .append("|").append(info.getPlatForm())
                .append("|").append(info.getAccoutRegisterTime())
                .append("|").append(info.getPhoneNet())
                .append("|").append(info.getNet())
                .append("|").append(info.getPhoneType())
                .append("|").append(info.getVerision())
                .append("|").append(info.getIp())
                .append("|").append(info.getImei())
                .append("|").append(info.getMac())
                .append("|").append(info.getSdkVersion())//sdk版本
                .append("|").append(info.getChannel().split("@")[2])
                .append("|").append("")
                .append("|").append("")
                .append("|").append("account_login")
                .append("|").append("")
                .append("|").append("1")
                .append("|").append(onlineTime)
                .append("|").append("")
                .append("|").append(info.getOsVersion())
        ;
        ServerLogConst.core_account.info(logStr);
    }


    //打印时间|core_account|用户登录区ID|getTime()|account_act|用户登录区ID|注册渠道ID_UID|UID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|手机运营商|2g/3g/4g/wifi等|手机型号|当前游戏客户端版本|ip地址（不包含端口）|IMEI信息|mac地址|sdk版本|sdk_id|此处为空|此处为空|account_login|此处为空|0=失败/1=成功|为空
    public void Log_user_info(String phoneType) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_account")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("account_act")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append("手机运营商")
                .append("|").append("2g/3g/4g/wifi等")
                .append("|").append("手机型号")
                .append("|").append(this.verision)
                .append("|").append("ip地址（不包含端口）")
                .append("|").append("IMEI信息")
                .append("|").append("mac地址")
                .append("|").append("sdk版本")
                .append("|").append("sdk_id")
                .append("|").append("此处为空")
                .append("|").append("此处为空")
                .append("|").append("account_login")
                .append("|").append("此处为空")
                .append("|").append("0=失败/1=成功")
                .append("|").append("为空")
        ;
        ServerLogConst.core_account.info(logStr);
    }

    //1	区账号登录：点击进入区按钮
    //2	区账号登出：用户从服务器内存中清除的时间
    //打印时间|core_gamesvr|用户登录区ID|getTime()|gamesvr_reg|用户登录区ID|注册渠道ID_UID|UID|用户注册子渠道|用户登录渠道|4（4=安卓/5=ios/7=wm）|区UID创建时间|用户VIP等级|手机运营商|wifi（2g/3g/4g/wifi等）|手机型号|客户端版本|ip地址，不包含端口|IMEI信息|mac地址|此处为空|此处为空|gamesvr_reg|此处为空|1|为空"
    public void Log_core_gamesvr(LoginInfo loginInfo, int vip, String registTime, long online) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_gamesvr")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("gamesvr_act")
                .append("|").append(this.serverId)
                .append("|").append(loginInfo == null ? "" : loginInfo.getChannel().split("@")[0] + "_" + loginInfo.getUid())
                .append("|").append(loginInfo == null ? "" : loginInfo.getUid())
                .append("|").append(loginInfo == null ? "" : loginInfo.getRegChannel())
                .append("|").append(loginInfo == null ? "" : loginInfo.getRegChannel())
                .append("|").append(loginInfo == null ? "" : loginInfo.getPlatForm())
                .append("|").append(registTime + "")
                .append("|").append(vip)
                .append("|").append(loginInfo == null ? "" : loginInfo.getPhoneNet())
                .append("|").append(loginInfo == null ? "" : loginInfo.getNet())
                .append("|").append(loginInfo == null ? "" : loginInfo.getPhoneType())
                .append("|").append(loginInfo == null ? "" : loginInfo.getVerision())
                .append("|").append(loginInfo == null ? "" : loginInfo.getIp())
                .append("|").append(loginInfo == null ? "" : loginInfo.getImei())
                .append("|").append(loginInfo == null ? "" : loginInfo.getMac())
                .append("|").append("")//此处为空
                .append("|").append("")//此处为空
                .append("|").append("gamesvr_logout")
                .append("|").append("")//此处为空
                .append("|").append("1")
                .append("|").append("online_time:").append(online)//为空
                .append("|").append("")
        ;
        ServerLogConst.core_gamesvr.info(logStr);
    }

//    public static String getPlatForm(String platForm) {
//        if (platForm.equalsIgnoreCase("Android")) {
//            return "4";
//        } else if (platForm.equalsIgnoreCase("IOS")) {
//            return "5";
//        }
//        return "0";
//    }


    //1	区账号登录：点击进入区按钮
    //2	区账号登出：用户从服务器内存中清除的时间
    //打印时间|core_gamesvr|用户登录区ID|getTime()|gamesvr_reg|用户登录区ID|注册渠道ID_UID|UID|用户注册子渠道|用户登录渠道|4（4=安卓/5=ios/7=wm）|区UID创建时间|用户VIP等级|手机运营商|wifi（2g/3g/4g/wifi等）|手机型号|客户端版本|ip地址，不包含端口|IMEI信息|mac地址|此处为空|此处为空|gamesvr_reg|此处为空|1|为空"
    public static void static_core_gamesvr(LoginInfo loginInfo, String registTime, int vip, String operateName, String operateId, String onlineTime) {
        if (null == registTime) {
            registTime = DateUtil.formatDateTime(System.currentTimeMillis());
        }
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_gamesvr")
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append(operateId)
                .append("|").append(MultiServerHelper.getServerId())
                .append("|").append(loginInfo.getChannel().split("@")[0] + "_" + loginInfo.getUid())
                .append("|").append(loginInfo.getUid())
                .append("|").append(loginInfo.getRegChannel())
                .append("|").append(loginInfo.getRegChannel())
                .append("|").append(loginInfo.getPlatForm())
                .append("|").append(registTime + "")
                .append("|").append(vip)
                .append("|").append(loginInfo.getPhoneNet())
                .append("|").append(loginInfo.getNet())
                .append("|").append(loginInfo.getPhoneType())
                .append("|").append(loginInfo.getVerision())
                .append("|").append(loginInfo.getIp())
                .append("|").append(loginInfo.getImei())
                .append("|").append(loginInfo.getMac())
                .append("|").append("")
                .append("|").append("")
                .append("|").append(operateName)
                .append("|").append("")
                .append("|").append("1")
                .append("|").append(onlineTime)
                .append("|").append("")
        ;
        ServerLogConst.core_gamesvr.info(logStr);
    }

    //1	点击角色创建界面创建按钮
    //1	"角色登录：通过角色选择界面或角色创建界面登录到游戏内"
    //2	角色登出：用用户从服务器内存中清除的时间

    //打印时间|core_role|用户登录区ID|getTime()|role_reg|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职业等级|此处为空|此处为空|role_reg|此处为空|1|为空
    public void Log_core_role(String themeType, String themeNmae, String createTime, String info) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_role")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(themeType)
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(createTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append("")//此处为空
                .append("|").append("")//此处为空
                .append("|").append(themeNmae)
                .append("|").append("")//此处为空
                .append("|").append("1")
                .append("|").append(info)//统计字段
                .append("|").append("")//广告短链id
        ;
        ServerLogConst.core_role.info(logStr);
    }

    //1	任何物品变动都计入此日志，触发时打印
    //打印时间|core_item|用户登录区ID|getTime()|item|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职业等级|场景id/地图id|一级变动原因|二级变动原因|物品code1：数量1&物品code2：数量2: 物品code3：数量3…|物品code1：数量1&物品code2：数量2: 物品code3：数量3…|1|此处留空
    public void Log_core_item(Map<Integer, Integer> addlMap, Map<Integer, Integer> deleteMap, short eventType) {
        String[] sencenInfo = getSceneInfo();
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_item")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("item")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职级
                .append("|").append(sencenInfo[0]).append("/").append(sencenInfo[1])
                .append("|").append(eventType)
                .append("|").append("")//"二级变动原因"
                .append("|").append(makeToolStr(addlMap))//"增加物品"
                .append("|").append(makeToolStr(deleteMap))//消耗物品
                .append("|").append("1")
                .append("|").append("")
        ;
        ServerLogConst.core_item.info(logStr);
    }

    public String makeToolStr(Map<Integer, Integer> toolMap) {
        if (toolMap == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        for (int code : toolMap.keySet()) {
            sb.append(code).append(":").append(toolMap.get(code));
            sb.append("&");
        }

        if (sb.length() >= 1) {
            sb.deleteCharAt(sb.lastIndexOf("&"));
        }
        return sb.toString();
    }

    public String[] getSceneInfo() {
        SceneModule sceneM = (SceneModule) module(MConst.Scene);
        int sceneId = 0;
        byte sceneType = SceneManager.SCENETYPE_CITY;
        if (sceneM.getScene() != null) {
            sceneId = sceneM.getScene().getSceneId();
            sceneType = sceneM.getScene().getSceneType();
        }
        String[] tmp = new String[]{sceneId + "", sceneType + ""};
        return tmp;
    }


    //1	任何物品变动都计入此日志，触发时打印
    //打印时间|core_item|用户登录区ID|getTime()|item|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职业等级|场景id/地图id|一级变动原因|二级变动原因|物品code1：数量1&物品code2：数量2: 物品code3：数量3…|物品code1：数量1&物品code2：数量2: 物品code3：数量3…|1|此处留空
    public void Log_core_item(long item, int count, short eventType, byte addorsub) {
        String[] sencenInfo = getSceneInfo();
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_item")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("item")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append(sencenInfo[0]).append("/").append(sencenInfo[1])
                .append("|").append(eventType)
                .append("|").append("");//二级变动原因
        if (addorsub == 1) {
            logStr.append("|").append(item + ":" + count)
                    .append("|").append("");
        } else if (addorsub == 0) {
            logStr.append("|").append("")
                    .append("|").append(item + ":" + count);
        }
        logStr.append("|").append("1")
                .append("|").append("")
        ;
        ServerLogConst.core_item.info(logStr);
    }


    //打印时间|core_coin|用户登录区ID|getTime()|main_coin|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职业等级|场景id/地图id|一级变动原因|二级变动原因|充值币新增消耗数量（新增为正数，消耗为负数）|变动后充值货币个人持有量|1|此处留空
    public void Log_core_coin(short eventType, int addorsub, byte resourceId) {

        String[] sencenInfo = getSceneInfo();
        StringBuilder logStr = new StringBuilder();
        String operateName = "";
        if (resourceId != 1 && resourceId != 2 && resourceId != 3) {
            return;
        }
        switch (resourceId) {
            case (byte) 1:
                operateName = ThemeType.COIN_GOLD.getOperateName();
                // 财务日志
                Log_core_finance(eventType, addorsub);
                break;
            case (byte) 2:
                operateName = ThemeType.COIN_BANDGOLD.getOperateName();
                break;
            case (byte) 3:
                operateName = ThemeType.COIN_MONEY.getOperateName();
                break;
        }
        //持有量
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        String resource = roleModule.getResource(resourceId) + "";

        logStr.append("core_coin")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(operateName)
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append(sencenInfo[0]).append("/").append(sencenInfo[1])
                .append("|").append(eventType)
                .append("|").append("")
                .append("|").append(addorsub)
                .append("|").append(resource)
                .append("|").append("1")
                .append("|").append("")//此处留空
        ;
        ServerLogConst.core_coin.info(logStr);
    }

    //打印时间|core_case|用户登录区ID|getTime()|case|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职级|关卡入口|局次code|关卡code|此处为空|case_start|此处为空|0=失败/1=成功|为空"
    public void Log_core_case(String operateId, String operateName, String enter, String juci, String guankaCode, String info, String goType, int guankaType) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_case")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(operateId)
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职级
                .append("|").append(enter)
                .append("|").append(juci)
                .append("|").append(guankaCode)
                .append("|").append(goType)//统计类型
                .append("|").append(operateName)
                .append("|").append(guankaType)
                .append("|").append("1")
                .append("|").append(info)
                .append("|").append("");
        ;
        ServerLogConst.core_case.info(logStr);
    }

    //打印时间|core_stat_1|用户登录区ID|getTime()|stat_account|用户登录区ID|注册子渠道ID|总账号统计|4=安卓/5=ios/7=wm|进程id
    public static void Log_core_stat_1(String operateId, String channel, String count, String pid, String platForm) {
        StringBuilder logStr = new StringBuilder();
        String subChannel = channel;
        logStr.append("core_stat_1")
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append(operateId)
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(subChannel)
                .append("|").append(count)
                .append("|").append(platForm)
                .append("|").append(pid)
        ;
        ServerLogConst.core_stat_1.info(logStr);
    }

    //打印时间|Log_core_finance_hold
    public static void Log_core_finance_hold(String channel, String count) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("core_finance")
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append("finance_main_coin_hold")
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(MultiServerHelper.getServerId() + "_" + channel)
                .append("|").append(channel)
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("0")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append(count)
        ;
        ServerLogConst.core_finance.info(logStr);
    }

    //打印时间|core_stat_2|用户登录区ID|getTime()|stat_account_vip|用户登录区ID|注册渠道ID|用户VIP等级|总账号统计|4=安卓/5=ios/7=wm
    public static void Log_core_stat_2(String operateId, String subChannel, String levelOrVip, int count, String platform, String pid) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_stat_2")
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append(operateId)
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(subChannel)
                .append("|").append(levelOrVip)
                .append("|").append(count)
                .append("|").append(platform)
                .append("|").append(pid)
        ;
        ServerLogConst.core_stat_2.info(logStr);
    }

    //打印时间|core_finance|用户登录区ID|getTime()|finance_main_coin_add|用户登录区ID|this.account|用户注册子渠道|用户登录子渠道|事件TYPE|事件TYPE（二级类型）|充值币新增数量|为空|为空|为空|当前充值金币持有量
    public void Log_core_finance(short eventType, int addorsub) {
        if (0 == addorsub) return;
        int changeNum = addorsub < 0 ? (0 - addorsub) : addorsub;
        String themeStr = "";
        if (addorsub > 0) {
            themeStr = ThemeType.FINANCE_ADD.getOperateName();
        } else {
            themeStr = ThemeType.FINANCE_SUB.getOperateName();
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        String resource = roleModule.getResource((byte) 1) + "";

        StringBuilder logStr = new StringBuilder();
        logStr.append("core_finance")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(themeStr)
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(eventType)
                .append("|").append("")
                .append("|").append(changeNum)
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append(resource)
        ;
        ServerLogConst.core_finance.info(logStr);
    }

    //打印时间|core_activity|用户登录区ID|getTime()|activity|用户登录区ID|注册渠道ID_UID|UID|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职级|活动入口|局次code|活动code|此处为空|activity_start|关卡code|0=失败/1=成功|为空

    /**
     * @param operateId    操作id
     * @param operateName  操作名
     * @param enter        活动入口
     * @param juci         活动局次
     * @param activityCode 活动code
     * @param guankaCode   关卡code
     * @param time         活动通过时间
     */

    public void Log_core_activity(String operateId, String operateName, int enter, String juci, int activityCode, String guankaCode, String time) {
        if (this.uid == null) {
            com.stars.util.LogUtil.error("Log_core_activity", new Exception());
        }
        StringBuilder logStr = new StringBuilder();
        //入口，局次暂时置为空，有运营日志需求时再处理
        enter = 0;
        juci = "";

        logStr.append("core_activity")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(operateId)
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职级
                .append("|").append(enter)
                .append("|").append(juci)
                .append("|").append(activityCode)
                .append("|").append("")//空
                .append("|").append(operateName)
                .append("|").append(guankaCode)
                .append("|").append("1");
        if (operateName.equals(ThemeType.ACTIVITY_START.getOperateName())) {
            logStr.append("|").append("");//空
        } else {
            if (time == null) {
                time = "0";
            }
            logStr.append("|").append("activity_time:").append(time);//空
        }
        ServerLogConst.core_activity.info(logStr);
    }

    private ThemeType getActivityOperateTheme(byte operateType) {
        if (operateType == ServerLogConst.ACTIVITY_START) {
            return ThemeType.ACTIVITY_START;
        } else if (operateType == ServerLogConst.ACTIVITY_WIN) {
            return ThemeType.ACTIVITY_WIN;
        } else if (operateType == ServerLogConst.ACTIVITY_FAIL) {
            return ThemeType.ACTIVITY_FAIL;
        }
        return null;
    }


    public void Log_core_activity(byte type, int enter, String juci, int themeId, int guankaCode, long time) {
        if (this.uid == null) {
            com.stars.util.LogUtil.error("Log_core_activity", new Exception());
        }
        ThemeType themeType = getActivityOperateTheme(type);
        if (themeType == null) return;
        StringBuilder logStr = new StringBuilder();
        //入口，局次暂时置为空，有运营日志需求时再处理
        enter = 0;
        juci = "";
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (guankaCode == 0) {
            guankaCode = roleModule.getRoleRow().getSafeStageId();
        }
        logStr.append("core_activity")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(themeType.getOperateId())
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职级
                .append("|").append(enter)
                .append("|").append(juci)
                .append("|").append(themeId)
                .append("|").append("")//空
                .append("|").append(themeType.getOperateName())
                .append("|").append(guankaCode)
                .append("|").append("1");
        if (themeType.getOperateName().equals(ThemeType.ACTIVITY_START.getOperateName())) {
            logStr.append("|").append("");//空
        } else {
            logStr.append("|").append("activity_time:").append(time);//空
        }
        ServerLogConst.core_activity.info(logStr);
    }

    public void Log_core_activity(byte type, int themeId, int guankaCode) {//拿不到活动时间
        if (this.uid == null) {
            com.stars.util.LogUtil.error("Log_core_activity", new Exception());
        }
        ThemeType themeType = getActivityOperateTheme(type);
        if (themeType == null) return;
        StringBuilder logStr = new StringBuilder();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (guankaCode == 0) {
            guankaCode = roleModule.getRoleRow().getSafeStageId();
        }
        logStr.append("core_activity")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append(themeType.getOperateId())
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职级
                .append("|").append(themeId)//活动入口
                .append("|").append(makeJuci())//局次
                .append("|").append(themeId)
                .append("|").append("")//空
                .append("|").append(themeType.getOperateName())
                .append("|").append(guankaCode)//关卡code
                .append("|").append("1");
        if (themeType.getOperateName().equals(ThemeType.ACTIVITY_START.getOperateName())) {
            logStr.append("|").append("");//空
        } else {
            logStr.append("|").append("activity_time:").append(0);//空
        }
        ServerLogConst.core_activity.info(logStr);
    }

    //打印时间|core_task|用户登录区ID|getTime()|task|用户登录区ID|this.account|uid|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户角色等级|this.fightScore|职业ID|职级|此处为空|此处为空|任务ID|1=主线/2=支线|task_start|此处为空|0=失败/1=成功|空
    public void Log_core_task(String operateName, String enter, int taskId, byte taskType) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_task")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("task")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel).append("_").append(this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//"此处为空"
                .append("|").append(enter)//"入口"
                .append("|").append("")//"此处为空"
                .append("|").append(taskId)
                .append("|").append(taskType)//"1=主线/2=支线"
                .append("|").append(operateName)
                .append("|").append("")//"此处为空"
                .append("|").append(1)
                .append("|").append("")//"此处为空"
                .append("|").append("")//"此处为空"
        ;
        ServerLogConst.core_task.info(logStr);
    }

    //打印时间|core_action|用户登录区ID|getTime()|upgrade|用户登录区ID|this.account|uid|角色ID|用户注册子渠道|用户登录渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户当前角色等级|this.fightScore|职业ID|职级|map_id|此处为空|主角色ID|此处为空|upgrade_role|升级前等级|1|参考操作码对照表对应统计信息||"
    public void Log_core_action(int level, int sourceLevel, int lastFighting) {
        StringBuilder logStr = new StringBuilder();
        String[] sceneInfo = getSceneInfo();
        logStr.append("core_action")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("upgrade")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")
                .append("|").append(sceneInfo[0])
                .append("|").append("")
                .append("|").append(this.roleid)
                .append("|").append("")
                .append("|").append("upgrade_role")
                .append("|").append(sourceLevel)
                .append("|").append("1")
                .append("|").append("last_fight_power:").append(lastFighting);
        ;
        ServerLogConst.core_action.info(logStr);
    }


    //打印时间|core_action|用户登录区ID|getTime()|package|用户登录区ID|this.account|uid|角色ID|用户注册子渠道|用户登录子渠道|4=安卓/5=ios/7=wm|UID创建时间|角色创建时间|当前游戏客户端版本|用户VIP等级|用户当前角色等级|this.fightScore|职业ID|职级|礼包兑换界面|请求序列号|礼包id|礼包type|package_win|帮派id|1|item@num:兑换物品code1@兑换物品数量&兑换物品code1@兑换物品数量…#|广告短链id
    public void Log_core_market(GiftLogEvent event) {
        StringBuilder logStr = new StringBuilder();

        logStr.append("core_market")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("package")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//"职级"
                .append("|").append("1")//礼包兑换界面
                .append("|").append(event.getSerizesid())
                .append("|").append(event.getGiftID())//"礼包id"
                .append("|").append(event.getGiftType())//"礼包type"
                .append("|").append(event.getGiftType().equals("1") ? "package_win" : "package_fail")
                .append("|").append(event.getGuildId())//"帮派id"
                .append("|").append("1")
                .append("|").append(event.getGiftType().equals("1") ? "item@num:" : "").append(event.getToolInfo())//"item@num:兑换物品code1@兑换物品数量&兑换物品code1@兑换物品数量…#"
                .append("|").append("")//广告短链id
        ;
        ServerLogConst.core_market.info(logStr);
    }

    //打印时间|dynamic_chat|用户登录区ID|发送者账号|发送者角色ID|发送者注册渠道|发送者VIP等级|发送者角色等级|接收者角色ID|聊天范围|聊天内容
    public void log_chat(ChatMessage message) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("dynamic_chat")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("chat")
                .append("|").append(getAccount())
                .append("|").append(message.getSenderId())
                .append("|").append(getReg_channel())
                .append("|").append(getVip())
                .append("|").append(getLevel())
                .append("|").append(message.getReceiver())
                .append("|").append(message.getChannel())
                .append("|").append(message.getContent());

        ServerLogConst.dynamic_chat.info(logStr.toString());
    }

    /**
     * 元宝抽奖活动
     *
     * @param stepId
     * @param reward
     */
    public void log_Raffle(int stepId, Map<Integer, Integer> reward) {
        StringBuilder logStr = new StringBuilder();
        StringBuilder rewardStr = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
            rewardStr.append(entry.getKey() + "@" + entry.getValue());
        }
        logStr.append("dynamic_4")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("451")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("draw")
                .append("|").append("")
                .append("|").append("1")
                .append("|").append("stepid:").append(stepId).append("#award@number:").append(rewardStr)
                .append("|").append("");

        ServerLogConst.dynamic_4.info(logStr.toString());
    }

    public void Log_monitor_mail(EmailLogEvent event) {
        StringBuffer logStr = new StringBuffer();
        String title = DataManager.getGametext(event.getTitle());
        String content = DataManager.getGametext(event.getContent());
        logStr.append("monitor_mail")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("mail")
                .append("|").append(this.roleid)
                .append("|").append(event.getEmailId())
                .append("|").append(event.getOpType())
                .append("|").append(title == null ? event.getTitle() : title)
                .append("|").append(content == null ? event.getContent() : content)
                .append("|").append(event.getFreeTime())//"冻结时间"
                .append("|").append(event.getOverTime())//"过期时间"
                .append("|").append(event.getTool().replace("|", "&").replace("+", ":"))//"物品code1:数量1&物品code2:数量2:物品code3:数量3…"
                .append("|").append(event.getToolInfo() == null ? "" : event.getToolInfo())//"code1(名称+属性)&code2（名称+属性）&code3（名称+属性）....."
        ;
        ServerLogConst.monitor_mail.info(logStr);
    }


    public String getAccount() {
        return account;
    }


    public void setAccount(String account) {
        this.account = account;
    }


    public String getRoleid() {
        return roleid;
    }


    public void setRoleid(String roleid) {
        this.roleid = roleid;
    }


    public String getServerId() {
        return serverId;
    }


    public void setServerId(String serverId) {
        this.serverId = serverId;
    }


    public String getLevel() {
        return level;
    }


    public void setLevel(String level) {
        this.level = level;
    }


    public String getVip() {
        return vip;
    }


    public void setVip(String vip) {
        this.vip = vip;
    }


    public String getLogin_channel() {
        return login_channel;
    }


    public void setLogin_channel(String login_channel) {
        this.login_channel = login_channel;
    }


    public String getReg_channel() {
        return reg_channel;
    }


    public void setReg_channel(String reg_channel) {
        this.reg_channel = reg_channel;
    }


    public String getUid() {
        return uid;
    }


    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getTime() {
        return DateUtil.formatDateTime(System.currentTimeMillis());
    }


    public void setTime(String time) {
        this.time = time;
    }


    public String getFightScore() {
        return fightScore;
    }


    public void setFightScore(String fightScore) {
        this.fightScore = fightScore;
    }

    public String getPhoneSystem() {
        return phoneSystem;
    }

    public void setPhoneSystem(String phoneSystem) {
        this.phoneSystem = phoneSystem;
    }

    public String getPhoneNet() {
        return phoneNet;
    }

    public void setPhoneNet(String phoneNet) {
        this.phoneNet = phoneNet;
    }


    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getPlatForm() {
        return platForm;
    }

    public void setPlatForm(String platForm) {
        this.platForm = platForm;
    }

    public String getRoleCreateTime() {
        return roleCreateTime;
    }

    public void setRoleCreateTime(String roleCreateTime) {
        this.roleCreateTime = roleCreateTime;
    }

    public String getAccoutRegisterTime() {
        return accoutRegisterTime;
    }

    public void setAccoutRegisterTime(String accoutRegisterTime) {
        this.accoutRegisterTime = accoutRegisterTime;
    }

    public String getMainChannel() {
        return mainChannel;
    }

    public void setMainChannel(String mainChannel) {
        this.mainChannel = mainChannel;
    }

    public String getVerision() {
        return verision;
    }

    public void setVerision(String verision) {
        this.verision = verision;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String makeJuci() {
        SceneModule scene = (SceneModule) module(MConst.Scene);
        int sceneId = 0;
        if (scene.getScene() != null) {
            sceneId = scene.getScene().getSceneId();
        }
        return sceneId + DateUtil.getms(System.currentTimeMillis());
    }

	/*public static void AccountLog_user_info(String phoneType){
        StringBuilder logStr = new StringBuilder();
		//
		logStr.append(getTime())
		.append("|").append("core_account")
		.append("|").append(this.serverId)
		.append("|").append(getTime())
		.append("|").append("account_act")
		.append("|").append(this.serverId)
		.append("|").append(this.reg_channel+"_"+this.uid)
		.append("|").append(this.uid)
		.append("|").append(this.reg_channel)
		.append("|").append(this.login_channel)
		.append("|").append(this.)
		.append("|").append(this.accoutRegisterTime)
		.append("|").append("手机运营商")
		.append("|").append("2g/3g/4g/wifi等")
		.append("|").append("手机型号")
		.append("|").append(this.verision)
		.append("|").append("ip地址（不包含端口）")
		.append("|").append("IMEI信息")
		.append("|").append("mac地址")
		.append("|").append("sdk版本")
		.append("|").append("sdk_id")
		.append("|").append("此处为空")
		.append("|").append("此处为空")
		.append("|").append("account_login")
		.append("|").append("此处为空")
		.append("|").append("0=失败/1=成功")
		.append("|").append("为空")
		;
		ServerLogConst.user_info.info(logStr);
	}**/

    /**
     * @param themeId 主题ID,需要向数据中心赵军磊同学申请
     * @param status  状态,0=上线/1=下线
     * @param info    统计字段
     */
    public void static_4_Log(int themeId, int status, String info) {
        try {
            //时间|static_4|区id|日志触发时间|主题id|区id|注册渠道id_账号id|uid|角色id|子渠道|登录渠道|平台|0|角色创建时间|客户端版本
            //vip等级|角色等级|战力|职业id|战阶|状态|类型|统计字段|广告位id
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("static_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append(status)
                    .append("|").append("")
                    .append("|").append(info)
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.static_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("static_4_Log", e.getMessage(), e);
        }
    }

    /**
     * @param themeId   主题ID
     * @param operateId 操作码
     * @param info      统计字段
     */
    public void dynamic_4_Log(int themeId, String operateId, String info, String target) {
        try {
            //时间|dynamic_4|区id|日志触发时间|主题id|区id|注册渠道id_账号id|uid|角色id|子渠道|登录渠道|平台|0|角色创建时间|客户端版本
            //vip等级|角色等级|战力|职业id|战阶|入口|统计批次|统计对象|统计类型|操作码|关联对象|操作结果|统计字段|广告位id
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append(0)
                    .append("|").append(0)
                    .append("|").append(target)
                    .append("|").append(0)
                    .append("|").append(operateId)
                    .append("|").append(0)
                    .append("|").append(1)
                    .append("|").append(info)
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("static_4_Log", e.getMessage(), e);
        }
    }

    /**
     * @param themeId   主题ID
     * @param operateId 操作码
     * @param info      统计字段
     */
    public void dynamic_4_Log_str(int themeId, String operateId, String info, String target) {
        try {
            //时间|dynamic_4|区id|日志触发时间|主题id|区id|注册渠道id_账号id|uid|角色id|子渠道|登录渠道|平台|0|角色创建时间|客户端版本
            //vip等级|角色等级|战力|职业id|战阶|入口|统计批次|统计对象|统计类型|操作码|关联对象|操作结果|统计字段|广告位id
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append(target)
                    .append("|").append("")
                    .append("|").append(operateId)
                    .append("|").append("")
                    .append("|").append(1)
                    .append("|").append(info)
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("static_4_Log", e.getMessage(), e);
        }
    }

    /**
     * @param themeId         主题ID
     * @param operateId       操作码
     * @param entrance        入口,无则填0
     * @param batch           统计批次,无则填0
     * @param statisticalObj  统计对象,无则填0
     * @param statisticalType 统计类型,无则填0
     * @param relateObj       关联对象,无则填0
     * @param result          操作结果,无则填1
     * @param info            统计字段
     */
    public void dynamic_4_Log(int themeId, String operateId, String entrance, String batch, String statisticalObj, String statisticalType, String relateObj, int result, String info) {
        try {
            //时间|dynamic_4|区id|日志触发时间|主题id|区id|注册渠道id_账号id|uid|角色id|子渠道|登录渠道|平台|0|角色创建时间|客户端版本
            //vip等级|角色等级|战力|职业id|战阶|入口|统计批次|统计对象|统计类型|操作码|关联对象|操作结果|统计字段|广告位id
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("static_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append(entrance)
                    .append("|").append(batch)
                    .append("|").append(statisticalObj)
                    .append("|").append(statisticalType)
                    .append("|").append(operateId)
                    .append("|").append(relateObj)
                    .append("|").append(result)
                    .append("|").append(info)
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_4_Log", e.getMessage(), e);
        }
    }

    public void dynamic_4_Log_for_Baby_or_Refine(int themeId, String operate, String statices) {
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append(operate)
                    .append("|").append("")
                    .append("|").append(1)
                    .append("|").append(statices)
                    .append("|").append("");
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_4_Log", e.getMessage(), e);
        }
    }


    public static String itemMapStr(Map<Integer, Integer> itemMap) {
        StringBuilder itemMapStr = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            itemMapStr.append(entry.getKey()).append("@").append(entry.getValue()).append("&");
        }
        if (itemMapStr.length() != 0) {
            itemMapStr.deleteCharAt(itemMapStr.length() - 1);
        }
        return itemMapStr.toString();
    }

    /**
     * 特殊账号用的
     *
     * @param themeId  主题id
     * @param account  账号
     * @param roleId   角色id
     * @param roleName 角色名称
     * @param content  告警内容
     */
    public void dynamic_4_Log(int themeId, String account, long roleId, String roleName, String content) {
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("specialAccount_warn")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(account)
                    .append("|").append(roleId)
                    .append("|").append(roleName)
                    .append("|").append(content);
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_4_Log", e.getMessage(), e);
        }
    }

    /**
     * 家族战用的
     *
     * @param type
     * @param rank
     * @param kill
     * @param integral
     * @param success
     * @param itemMap
     */
    public void dynamic_4_Log(int themeId, int type, int rank, int kill, long integral, int success, int warType, int battleType, Map<Integer,
            Integer> itemMap) {
        StringBuilder itemMapStr = new StringBuilder();
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            itemMapStr.append(entry.getKey()).append("@").append(entry.getValue()).append("&");
        }
        if (itemMapStr.length() != 0) {
            itemMapStr.deleteCharAt(itemMapStr.length() - 1);
        }
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append(warType)
                    .append("|").append("")
                    .append("|").append(battleType)
                    .append("|").append("family_fight")
                    .append("|").append("")
                    .append("|").append(success)
                    .append("|").append("type:").append(type).append("#").append("rank:").append(rank).append("#")
                    .append("kill:").append(kill).append("#").append("intergal:").append(integral).append("#").append("reward:").append(itemMapStr).append("|").append("");
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_4_Log", e.getMessage(), e);
        }
    }

    /**
     * 给周惠礼包用的
     *
     * @param themeId
     * @param giftId
     * @param charge
     * @param itemMap
     */
    public void dynamic_4_Log(int themeId, int giftId, int charge, Map<Integer, Integer> itemMap) {
        String tmpStr = StringUtil.makeString(itemMap, '@', '&');
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("week_buy")
                    .append("|").append("week_buy@money:").append(giftId).append("@").append(charge)
                    .append("#").append("reward@number").
                    append(tmpStr).append("|").append("");
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_4_Log", e.getMessage(), e);
        }
    }

    /**
     * 问卷调查日志
     *
     * @param themeId
     * @param answer
     */
    public void dynamic_survey_Log(int themeId, String answer) {
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_survey")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append("survey")
                    .append("|").append(this.account)
                    .append("|").append(this.roleid)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(themeId)
                    .append("|").append(answer);
            ServerLogConst.dynamic_survey.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("dynamic_survey_Log", e.getMessage(), e);
        }
    }

    /**
     * 角色登入/登出日志
     */
    public void loginLogoutLog(byte status) {
        equipmentLog(status);    //装备
        rideLog(status);        //坐骑
        deityWeaponLog(status);    //神兵
        gemStoneLog(status);    //宝石
        guestLog(status);        //门客
        skillLog(status);        //技能
        onlineRewardLog(status);//在线奖励
        sevenDayGoalLog(status);//七日目标
        newServerSignLog(status);//新服签到
//        fightScoreComposeLog(status); //战力构成
        fightScoreComposeLog2(status); // 详尽的战力构成
        log_title(status);//称号
        log_skyRank(status);//天梯段位
        log_tokenEquipment(status); //符文装备
        log_fashion(status);
    }

    /**
     * 装备运营静态日志
     */
    private void equipmentLog(byte status) {
    }

    /**
     * 坐骑运营静态日志
     */
    private void rideLog(byte status) {

    }

    /**
     * 神兵运营静态日志
     */
    private void deityWeaponLog(byte status) {

    }

    private String getEquipLogNameByType(byte type) {
        switch (type) {
            case 1:
                return "weapon";
            case 2:
                return "clothes";
            case 3:
                return "necklace";
            case 4:
                return "belt";
            case 5:
                return "ring";
            case 6:
                return "shoes";
        }
        return "weapon";
    }

    /**
     * 宝石运营静态日志
     */
    private void gemStoneLog(byte status) {

    }

    /**
     * 门客运营静态日志
     */
    private void guestLog(byte status) {

    }

    /**
     * 技能运营静态日志
     */
    private void skillLog(byte status) {
    }

    /**
     * 在线奖励运营静态日志
     */
    private void onlineRewardLog(byte status) {
    }

    /**
     * 七日目标运营静态日志
     */
    private void sevenDayGoalLog(byte status) {

    }

    /**
     * 新服签到运营静态日志
     */
    private void newServerSignLog(byte status) {

    }

    /**
     * 战力构成运营静态日志
     */
    private void fightScoreComposeLog(byte status) {
        RoleModule roleModule = module(MConst.Role);
        if (roleModule == null || roleModule.getRoleRow() == null || roleModule.getRoleRow().getFightScoreMap() == null)
            return;
        Map<String, Integer> fightScoreMap = roleModule.getRoleRow().getFightScoreMap();
        StringBuffer sb = new StringBuffer();
        sb.append("equipment:").append(getFightScore(fightScoreMap, "equipment")).append("#");       //装备
        sb.append("grade:").append(getFightScore(fightScoreMap, "grade")).append("#");               //角色等级
        sb.append("title:").append(getFightScore(fightScoreMap, "title")).append("#");               //称号
        sb.append("gem:").append(getFightScore(fightScoreMap, "gem")).append("#");                   //宝石
        sb.append("skill:").append(getFightScore(fightScoreMap, "skill")).append("#");               //技能
        sb.append("buddy:").append(getFightScore(fightScoreMap, "buddy")).append("#");               //角色伙伴
        sb.append("ride:").append(getFightScore(fightScoreMap, "ride")).append("#");                 //坐骑
        sb.append("familySkill:").append(getFightScore(fightScoreMap, "familySkill")).append("#");   //家族心法
        sb.append("trump:").append(getFightScore(fightScoreMap, "trump")).append("#");               //法宝
//        sb.append("mind:").append(getFightScore(fightScoreMap, "mind")).append("#");                 //心法(经脉)
        sb.append("fashion:").append(getFightScore(fightScoreMap, "fashion")).append("#");           //时装
        sb.append("deityweapon:").append(getFightScore(fightScoreMap, "deityweapon")).append("#");    //神兵
        sb.append("guest:").append(getFightScore(fightScoreMap, "guest")).append("#");                //门客
        sb.append("marry:").append(getFightScore(fightScoreMap, "marryring")).append("#");            //结婚
        sb.append("book:").append(getFightScore(fightScoreMap, RoleManager.FIGHTSCORE_BOOK)).append("#"); //典籍
        static_4_Log(ThemeType.STATIC_FIGHTING.getThemeId(), status, sb.toString());
    }

    private void fightScoreComposeLog2(byte status) {
        try {
            RoleModule roleModule = module(MConst.Role);
            if (roleModule == null || roleModule.getRoleRow() == null || roleModule.getRoleRow().getFightScoreMap() == null)
                return;
            Map<String, Integer> fightScoreMap = roleModule.getRoleRow().getFightScoreMap();
            StringBuffer sb = new StringBuffer();
            sb.append(makeFsEquipmentStr()); // 装备
            sb.append("grade:").append(getFightScore(fightScoreMap, "grade")).append("#"); // 角色等级
            sb.append(makeFsTitleStr()); // 称号
            sb.append(makeFsGemStr());  // 宝石
            sb.append("skill:").append(getFightScore(fightScoreMap, "skill")).append("#"); // 技能
            sb.append(makeFsBuddyStr()); // 角色伙伴
            sb.append(makeFsRideStr()); // 坐骑
            sb.append("familySkill:").append(getFightScore(fightScoreMap, "familySkill")).append("#"); // 家族心法
            //sb.append("trump:").append(getFightScore(fightScoreMap, "trump")).append("#"); // 法宝
            sb.append(makeFsTrumpStr()); //法宝
            sb.append(makeFsFashionStr(fightScoreMap));//时装
            sb.append("deityweapon:").append(getFightScore(fightScoreMap, "deityweapon")).append("#"); // 神兵
            sb.append(makeFsGuestStr()); // 门客
            sb.append("marry:").append(getFightScore(fightScoreMap, "marryring")).append("#"); // 结婚
            sb.append(makeFsBookStr()); // 典籍
            sb.append(makeFsCampStr()); //阵营
            sb.append(makeFsBabyStr()); //宝宝
            sb.append("soul:").append(getFightScore(fightScoreMap, "daily")).append("#"); //魂珠
            sb.append(makeFsSoulGod()); //元神系统
            //sb.append("kingdom_official:").append(getFightScore(fightScoreMap, RoleManager.FIGHTSCORE_CAMP)).append("#"); // 阵营
            static_4_Log(ThemeType.STATIC_FIGHTING.getThemeId(), status, sb.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("日志-战力构成", e);
        }
    }


    private String makeFsFashionStr(Map<String, Integer> fightScoreMap) {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }

    private String makeFsBabyStr() {
        return null;
    }

    /**
     * 元神系统战力
     *
     * @return
     */
    private String makeFsSoulGod() {
        StringBuilder sb = new StringBuilder("soulgod:");
        RoleModule roleModule = module(MConst.Role);
        Integer totalFightScore = roleModule.getRoleRow().getFightScoreMap().get(RoleManager.FIGHTSCORE_SOUL);
        if (totalFightScore == null) {
            totalFightScore = 0;
        }
        sb.append(totalFightScore).append("#");
        return sb.toString();
    }

    private int getFightScore(Map<String, Integer> map, String key) {
        if (!map.containsKey(key)) return 0;
        return map.get(key);
    }

    private String makeFsEquipmentStr() {
        return null;
    }

    private String makeFsTitleStr() {
        return null;
    }

    private String makeFsGemStr() {
        return null;
    }

    private String makeFsBuddyStr() {
        return null;
    }

    private String makeFsRideStr() {
        return null;
    }

    private String makeFsTrumpStr() {
        return null;
    }

    private String makeFsGuestStr() {
        return null;
    }

    private String makeFsBookStr() {
        return null;
    }

    private String makeFsCampStr() {
        return null;
    }

    public void Log_shop_buy(int shopType, Map<Integer, Integer> addItemList, Map<Integer, Integer> subItemList) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("dynamic_4")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("440")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append(shopType)
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("shop")
                .append("|").append("")
                .append("|").append("1")
                .append("|");
        if (null != subItemList) {
            logStr = logStr.append("consume@number:");
            String subStr = null;
            for (Map.Entry<Integer, Integer> m : subItemList.entrySet()) {
                if (null == subStr) {
                    subStr = String.format("%d@%d", m.getKey(), m.getValue());
                } else {
                    subStr = subStr + "&" + String.format("%d@%d", m.getKey(), m.getValue());
                }
            }
            logStr = logStr.append(subStr);
        }
        if (null != addItemList) {
            logStr = logStr.append("#reward@number:");
            String addStr = null;
            for (Map.Entry<Integer, Integer> m : addItemList.entrySet()) {
                if (null == addStr) {
                    addStr = String.format("%d@%d", m.getKey(), m.getValue());
                } else {
                    addStr = addStr + "&" + String.format("%d@%d", m.getKey(), m.getValue());
                }
            }
            logStr = logStr.append(addStr);
        }
        ServerLogConst.dynamic_4.info(logStr.toString());
    }

    public void log_buddy(String staticStr) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("static_4")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("439")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append("1")
                .append("|").append("")
                .append("|").append(staticStr + "")
                .append("|").append("")
        ;
        ServerLogConst.static_4.info(logStr.toString());
    }

    public void log_trump(String magic_base, String magic_skill, String matrix) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("static_4")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("496")
                .append("|").append(this.serverId)
                .append("|").append(this.mainChannel + "_" + this.uid)
                .append("|").append(this.uid)
                .append("|").append(this.roleid)
                .append("|").append(this.reg_channel)
                .append("|").append(this.login_channel)
                .append("|").append(this.platForm)
                .append("|").append(this.accoutRegisterTime)
                .append("|").append(this.roleCreateTime)
                .append("|").append(this.verision)
                .append("|").append(this.vip)
                .append("|").append(this.level)
                .append("|").append(this.fightScore)
                .append("|").append(this.job)
                .append("|").append("")//职业等级
                .append("|").append("1")
                .append("|").append("")
                .append("|").append(magic_base).append("#").append(magic_skill).append("#").append(matrix)
                .append("|").append("")
        ;
        ServerLogConst.static_4.info(logStr.toString());
    }

    /**
     * 好友申请
     *
     * @param friendId
     */
    public void log_friend_apply(long friendId) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("friend_id:").append(friendId);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_apply", logStr.toString(), friendId + "");
    }

    /**
     * 添加好友
     *
     * @param friendId
     * @param isAccept 1=接受 0=拒绝
     */
    public void log_friend_accept(long friendId, byte isAccept) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("apply_id@result:").append(friendId).append("@").append(isAccept);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_accept", logStr.toString(), friendId + "");
    }

    /**
     * 送体力
     */
    public void log_friend_physical(int num, long friendId) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("physical@number:体力赠送@").append(num);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_physical", logStr.toString(), friendId + "");
    }

    /**
     * 送花
     *
     * @param friendId
     * @param friendShip  好友度
     * @param flowerState 送花类型
     * @param num
     */
    public void log_friend_flower(long friendId, int friendShip, byte flowerState, int num) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("flower_state@number:").append(flowerState).append("@").append(num)
                .append("#friend@friendship:").append(friendId).append("@").append(friendShip);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_flower", logStr.toString(), friendId + "");
    }

    /**
     * 拉黑名单
     *
     * @param friendId
     * @param totalNum 黑名单总数量
     */
    public void log_friend_blacklist(long friendId, int totalNum) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("blacklist_human@number:").append(friendId).append("@").append(totalNum);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_blacklist", logStr.toString(), friendId + "");
    }

    /**
     * 好友切磋
     *
     * @param result 1=接受、2=拒绝
     */
    public void log_friend_fight(long friendId, byte result) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("fight_human@result:").append(friendId).append("@").append(result);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_fight", logStr.toString(), friendId + "");
    }

    /**
     * 邀请进入家族（好友）
     *
     * @param friendId
     * @param result   1=接受、2=拒绝
     */
    public void log_friend_family(long friendId, byte result) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("family_invite@result:").append(friendId).append("@").append(result);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND.getThemeId(), "friend_family", logStr.toString(), friendId + "");
    }

    /**
     * 家族信息日志
     */
    public static void log_family(FamilyLogData familyLogData) {
        FamilyPo familyPo = familyLogData.getFamilyPo();
        StringBuilder logStr = new StringBuilder();
        logStr.append("static_4")
                .append("|").append(MultiServerHelper.getServerId() + "")
                .append("|").append(DateUtil.formatDateTime(System.currentTimeMillis()))
                .append("|").append("412")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append("")
                .append("|").append(familyPo.getFamilyId())
                .append("#president:").append(familyLogData.getMaster())
                .append("#vice_president:").append(familyLogData.getAssistantStr())
                .append("#veteran:").append(familyLogData.getElderStr())
                .append("#elite:").append(familyLogData.getMemberStr())
                .append("#family_lv:").append(familyPo.getLevel())
                .append("#family_money:").append(familyPo.getMoney())
                .append("#family_ranking:").append(familyLogData.getRanking())
                .append("#total_number:").append(familyPo.getMemberCount())
                .append("#total_fight:").append(familyPo.getTotalFightScore())
                .append("#active_number:").append(familyLogData.getActiveNum())
                .append("|").append("")//广告短链id
        ;
        ServerLogConst.static_4.info(logStr.toString());
    }

    /**
     * 家族捐献
     */
    public void log_personal_family_donate(byte type, int num) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("donate_type@num:").append(type).append("@").append(num);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_donate", logStr.toString(), "0");
    }

    /**
     * 家族心法
     */
    public void log_personal_family_spell(String str) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("spell_lv:").append(str);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_spell", logStr.toString(), "0");
    }

    /**
     * 家族 红包发送
     */
    public void log_personal_family_red_send(byte type, byte itemType, int money, long senderId) {
        StringBuilder logStr = new StringBuilder();
        if (type == 1) {
            logStr.append("red_send@money:").append(itemType).append("@").append(money);
        } else {
            logStr.append("red_accept@money:").append(itemType).append("@").append(money);
        }
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_red_send", logStr.toString(), senderId + "");
    }

    /**
     * 家族交换
     */
    public void log_personal_family_exchange(byte type, int code, int num, long target) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("exchange_type@code@number:").append(type).append("@").append(code).append("@").append(num);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_exchange", logStr.toString(), target + "");
    }

    /**
     * 退出家族
     */
    public void log_personal_family_quit(long familyId, long roleId) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("family_id@uid:").append(familyId).append("@").append(roleId);
        if (String.valueOf(roleId).equals(roleid)) {
            dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_quit", logStr.toString(), "0");
        } else {
            dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_quit", logStr.toString(), roleId + "");
        }
    }

    /**
     * 家族篝火
     */
    public void log_personal_family_bonfire(byte type, String str) {
        StringBuilder logStr = new StringBuilder();
        if (type == 1) {
            logStr.append("bonfire_in:参与篝火");
        } else if (type == 2) {
            logStr.append("bonfire_out:退出篝火");
        } else {
            logStr.append("reward@numbe:").append(str);
        }
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_bonfire", logStr.toString(), "0");
    }

    /**
     * 家族探险
     */
    public void log_personal_family_find(String timeStr, String awardStr, String damageStr) {
        StringBuilder logStr = new StringBuilder();
        logStr.append("travel@time:").append(timeStr).append("#")
                .append("award@number:").append(awardStr).append("#")
                .append("map@hurt_number:").append(damageStr);
        dynamic_4_Log_str(ThemeType.DYNAMIC_FAMILY.getThemeId(), "family_find", logStr.toString(), "0");
    }

    /**
     * 称号
     */
    public void log_title(byte status) {

    }


    /**
     * 结婚
     */
    public void marryLog(String operateId, String staticStr, String target) {
        dynamic_4_Log_str(ThemeType.DYNAMIC_MARRY.getThemeId(), operateId, staticStr.toString(), target);
    }

    /**
     * 关卡断线重连次数
     */
    public void logDungeonReconnect(int dungeonId) {
        dynamic_4_Log_str(ThemeType.DYNAMIC_DUNGEON_RECONNECT.getThemeId(), "weak", "", toString(dungeonId));
    }

    /**
     * 精准营销（推送）
     *
     * @param event
     */
    public void logPush(PushActivedEvent event) {
        Map<Integer, PushInfo> pushInfoMap = event.getPushInfoMap();
        Iterator<PushInfo> iterator = pushInfoMap.values().iterator();
        while (iterator.hasNext()) {
            PushInfo pushInfo = iterator.next();
            StringBuffer sb = new StringBuffer();
            sb.append("sale_code@time:").append(pushInfo.getPushId()).append("@").append(1);
            LogBean logBean = new LogBean();
            logBean.setThemeId("466");
            logBean.setOperateId("sale");
            logBean.setInfo(sb.toString());
            dynamic4Log(logBean);
        }
    }

    /**
     * 充值特惠（推送）
     *
     * @param pushInfoMap
     */
    public void logPrecisionPush(Map<Integer, PushInfo> pushInfoMap) {

    }

    /**
     * 充值特惠（完成购买）
     *
     * @param pushId
     */
    public void logPrecisionPushFinish(int pushId) {

    }

    /**
     * 精准营销（完成购买）
     */
    public void logPushFinish(int pushId, int times) {
        StringBuilder sb = new StringBuilder();
        sb.append("finish_sale@time:").append(pushId).append("@").append(times);
        LogBean logBean = new LogBean();
        logBean.setThemeId("466");
        logBean.setOperateId("finish_sale");
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    /**
     * 日常5v5
     *
     * @param opType 开始：1； 结束：2
     * @param style  1v1=1；5v5=2
     * @param result
     */
    public void log_daily5v5(byte opType, byte style, byte result) {
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.DYNAMIC_DAILY_5V5.getThemeId()));
        String operateId = "pvp_start";
        if (opType == 2) {
            operateId = "pvp_finish";
            sb.append("style:").append(style).append("#").append("result:").append(result);
        } else {
            sb.append("style:").append(style);
        }
        logBean.setOperateId(operateId);
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    public void log_dailyAward(Short dailyId, byte awardType, Map<Integer, Integer> awardMap) {
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.DYNAMIC_DAIYLY.getThemeId()));
        logBean.setObject(dailyId.toString());
        if (awardType == DailyManager.SUPER_AWARD) {
            sb.append("super_reward@number:");
        } else if (awardType == DailyManager.MUTIPLE_AWARD) {
            sb.append("special_reward@number:");
        }
        if (StringUtil.isEmpty(sb.toString())) {
            return;
        }
        StringBuffer sb1 = new StringBuffer(); //记录获得的道具
        for (Map.Entry<Integer, Integer> entry : awardMap.entrySet()) {
            sb1.append(entry.getKey()).append("@").append(entry.getValue()).append("&");
        }
        if (StringUtil.isNotEmpty(sb1.toString())) {
            sb1.deleteCharAt(sb1.length() - 1);
        }
        sb.append(sb1);
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    public void log_dailyBall(int level) {
        DailyBallStageVo dailyBallStageVo = DailyManager.getDailyBallStageVoByLevel(level);
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.DYNAMIC_DAIYLY.getThemeId()));
        sb.append("soul_lv@star_id:")
                .append(dailyBallStageVo.getStage())
                .append("@")
                .append(dailyBallStageVo.getStar());
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    public void log_secendKill(int totalPay, int costMoney, Map<Integer, Integer> toolMap) {
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.DYNAMIC_SECSKILL.getThemeId()));
        sb.append("money@gold:").append(totalPay).append("@").append(costMoney).append("#");
        sb.append("reward@number:");
        boolean isTheFirst = true;
        for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
            if (!isTheFirst) { //不是第一个,前面补 &
                sb.append("&");
            }
            sb.append(entry.getKey()).append("@").append(entry.getValue());
            isTheFirst = false; //第一个打完，之后再不是第一个
        }
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    /**
     * 天梯段位   在线玩家处理(23:50~23:59)
     */
    public void log_skyRank(String info) {
        static_4_Log(ThemeType.STATIC_SKYRANK.getThemeId(), 1, info);
    }

    /**
     * 天梯段位   玩家上下线
     */
    public void log_skyRank(byte status) {

    }

    /**
     * 天梯段位   玩家上下线
     */
    public void log_tokenEquipment(byte status) {

    }

    /**
     * 时装  玩家上下线
     */
    public void log_fashion(byte status) {

    }

    /**
     * 动态日志-普通，可通过扩展LogBean类并修改此方法代码进行扩展
     *
     * @param logBean
     */
    public void dynamic4Log(LogBean logBean) {
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")//日志名称
                    .append("|").append(this.serverId)//用户登录区ID
                    .append("|").append(getTime())//日志触发时间
                    .append("|").append(logBean.getThemeId() == null ? "" : logBean.getThemeId())//主题ID
                    .append("|").append(this.serverId)//用户登录区ID
                    .append("|").append(this.mainChannel + "_" + this.uid)//注册渠道ID_UID
                    .append("|").append(this.uid)//账号ID
                    .append("|").append(this.roleid)//角色ID
                    .append("|").append(this.reg_channel)//用户注册子渠道
                    .append("|").append(this.login_channel)//用户登录渠道
                    .append("|").append(this.platForm)//平台
                    .append("|").append(this.accoutRegisterTime)//账号创建时间
                    .append("|").append(this.roleCreateTime)//角色创建时间
                    .append("|").append(this.verision)//当前游戏客户端版本
                    .append("|").append(this.vip)//用户VIP等级
                    .append("|").append(this.level)//用户角色等级
                    .append("|").append(this.fightScore)//用户战力
                    .append("|").append(this.job)//职级ID
                    .append("|").append("")//职级
                    .append("|").append("")//入口
                    .append("|").append(logBean.getObjectBatch() == null ? "" : logBean.getObjectBatch())//统计对象批次
                    .append("|").append(logBean.getObject() == null ? "" : logBean.getObject())//统计对象
                    .append("|").append(logBean.getStatType() == null ? "" : logBean.getStatType())//统计类型
                    .append("|").append(logBean.getOperateId() == null ? "" : logBean.getOperateId())//操作码
                    .append("|").append("")//关联对象
                    .append("|").append(1)//操作结果
                    .append("|").append(logBean.getInfo() == null ? "" : logBean.getInfo())//统计字段
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("static_4_Log", e.getMessage(), e);
        }
    }

    /**
     * 核心日志-活动，可通过扩展LogBean类并修改此方法代码进行扩展
     *
     * @param logBean
     */
    public void coreActivity(LogBean logBean) {
        try {
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("core_activity")//日志名称
                    .append("|").append(this.serverId)//用户登录区ID
                    .append("|").append(getTime())//日志触发时间
                    .append("|").append(logBean.getThemeId() == null ? "" : logBean.getThemeId())//主题ID
                    .append("|").append(this.serverId)//用户登录区ID
                    .append("|").append(this.mainChannel + "_" + this.uid)//注册渠道ID_UID
                    .append("|").append(this.uid)//账号ID
                    .append("|").append(this.roleid)//角色ID
                    .append("|").append(this.reg_channel)//用户注册子渠道
                    .append("|").append(this.login_channel)//用户登录渠道
                    .append("|").append(this.platForm)//平台
                    .append("|").append(this.accoutRegisterTime)//账号创建时间
                    .append("|").append(this.roleCreateTime)//角色创建时间
                    .append("|").append(this.verision)//当前游戏客户端版本
                    .append("|").append(this.vip)//用户VIP等级
                    .append("|").append(this.level)//用户角色等级
                    .append("|").append(this.fightScore)//用户战力
                    .append("|").append(this.job)//职级ID
                    .append("|").append("")//职级
                    .append("|").append("")//入口
                    .append("|").append(logBean.getObjectBatch() == null ? "" : logBean.getObjectBatch())//统计对象批次
                    .append("|").append(logBean.getObject() == null ? "" : logBean.getObject())//统计对象
                    .append("|").append("")//统计类型
                    .append("|").append(logBean.getOperateId() == null ? "" : logBean.getOperateId())//操作码
                    .append("|").append(logBean.getAssociateObject() == null ? "" : logBean.getAssociateObject())//关联对象
                    .append("|").append(1)//操作结果
                    .append("|").append(logBean.getInfo() == null ? "" : logBean.getInfo())//统计字段
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.core_activity.info(sBuff.toString());
        } catch (Exception e) {
            com.stars.util.LogUtil.error("core_activity", e.getMessage(), e);
        }
    }


    /**
     * 精英组队，每次创建队伍或加入队伍时记录
     */
    public void logTeamStyle(int mapCode, int style) {
        StringBuilder sb = new StringBuilder();
        sb.append("map_code@style:").append(mapCode).append("@").append(style);
        LogBean logBean = new LogBean();
        logBean.setThemeId("453");
        logBean.setObjectBatch("1");//1=组队精英关卡
        logBean.setOperateId("team_style");
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    /**
     * 精英组队，开始组队关卡时记录
     */
    public void logTeamBegin(int people, int rewardTimes, int helpTimes) {
        StringBuilder sb = new StringBuilder();
        sb.append("people#").append(people).append("&style@time#").append(2).append("@").append(rewardTimes).append("&").append(1).append("@").append(helpTimes);
        LogBean logBean = new LogBean();
        logBean.setThemeId("453");
        logBean.setObjectBatch("1");//1=组队精英关卡
        logBean.setOperateId("team_begin");
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }

    /**
     * 精英组队，结束组队关卡时记录
     */
    public void logTeamFinish(Map<Integer, Integer> rewardMap, int result) {
        StringBuilder sb = new StringBuilder();
        if (result == 1) {
            String showItem = StringUtil.makeString(rewardMap, '@', '&');
            sb.append("award@number:").append(showItem).append("&result#").append(result);
        } else {
            sb.append("result#").append(result);
        }
        LogBean logBean = new LogBean();
        logBean.setThemeId("453");
        logBean.setObjectBatch("1");//1=组队精英关卡
        logBean.setOperateId("team_finish");
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }


    public void log_change_job(int themeId, String operateId, String info, String target) {
        try {
            //时间|dynamic_4|区id|日志触发时间|主题id|区id|注册渠道id_账号id|uid|角色id|子渠道|登录渠道|平台|0|角色创建时间|客户端版本
            //vip等级|角色等级|战力|职业id|战阶|入口|统计批次|统计对象|统计类型|操作码|关联对象|操作结果|统计字段|广告位id
            StringBuffer sBuff = new StringBuffer();
            sBuff.append("dynamic_4")
                    .append("|").append(this.serverId)
                    .append("|").append(getTime())
                    .append("|").append(themeId)
                    .append("|").append(this.serverId)
                    .append("|").append(this.mainChannel + "_" + this.uid)
                    .append("|").append(this.uid)
                    .append("|").append(this.roleid)
                    .append("|").append(this.reg_channel)
                    .append("|").append(this.login_channel)
                    .append("|").append(this.platForm)
                    .append("|").append(this.accoutRegisterTime)
                    .append("|").append(this.roleCreateTime)
                    .append("|").append(this.verision)
                    .append("|").append(this.vip)
                    .append("|").append(this.level)
                    .append("|").append(this.fightScore)
                    .append("|").append(this.job)
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append("")
                    .append("|").append(target)
                    .append("|").append("")
                    .append("|").append(operateId)
                    .append("|").append("")
                    .append("|").append(1)
                    .append("|").append(info)
                    .append("|").append("")//广告短链id
            ;
            ServerLogConst.dynamic_4.info(sBuff.toString());
        } catch (Exception e) {
            LogUtil.error("static_4_Log", e.getMessage(), e);
        }
    }

    /**
     * 精英副本，活动开始
     */
    public void logBaseTeamBegin(int playCount, int eliteid) {
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.ACTIVITY_32.getThemeId()));
        logBean.setOperateId("activity_start");
        logBean.setObjectBatch(String.valueOf(playCount));
        logBean.setObject(String.valueOf(ThemeType.ACTIVITY_32.getThemeId()));
        logBean.setAssociateObject(String.valueOf(eliteid));
        logBean.setInfo(sb.toString());
        coreActivity(logBean);
    }

    /**
     * 精英副本，活动结束
     */
    public void logBaseTeamFinish(int playCount, int spendTime, int result, int eliteid) {
        StringBuilder sb = new StringBuilder();
        sb.append("activity_time:").append(spendTime);
        LogBean logBean = new LogBean();
        if (result == 1) {
            logBean.setOperateId("activity_win");
        } else {
            logBean.setOperateId("activity_fail");
        }
        logBean.setThemeId(String.valueOf(ThemeType.ACTIVITY_32.getThemeId()));
        logBean.setObjectBatch(String.valueOf(playCount));
        logBean.setObject(String.valueOf(ThemeType.ACTIVITY_32.getThemeId()));
        logBean.setAssociateObject(String.valueOf(eliteid));
        logBean.setInfo(sb.toString());
        coreActivity(logBean);
    }

    /**
     * 家族运镖，活动开始
     */
    public void logFamilyEscortBegin() {
        StringBuilder sb = new StringBuilder();
        LogBean logBean = new LogBean();
        logBean.setThemeId(String.valueOf(ThemeType.ACTIVITY_41.getThemeId()));
        logBean.setOperateId("activity_start");
        logBean.setObject(String.valueOf(ThemeType.ACTIVITY_41.getThemeId()));
        logBean.setInfo(sb.toString());
        coreActivity(logBean);
    }

    /**
     * 家族运镖，活动结束
     */
    public void logFamilyEscortFinish(int spendTime, int result) {
        StringBuilder sb = new StringBuilder();
        sb.append("activity_time:").append(spendTime);
        LogBean logBean = new LogBean();
        if (result == 1) {
            logBean.setOperateId("activity_win");
        } else {
            logBean.setOperateId("activity_fail");
        }
        logBean.setThemeId(String.valueOf(ThemeType.ACTIVITY_41.getThemeId()));
        logBean.setObject(String.valueOf(ThemeType.ACTIVITY_41.getThemeId()));
        logBean.setInfo(sb.toString());
        coreActivity(logBean);
    }

    /**
     * 好友邀请，成功邀请
     */
    public void logInvite(long roleId, int inviteCount, Map<Integer, Integer> toolMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("invite:").append(inviteCount).append("#reward@number:").append(StringUtil.makeString(toolMap, '@', '&'));
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND_INVATE.getThemeId(), "award", sb.toString(), "");
    }

    /**
     * 好友邀请，成功受邀
     */
    public void logBeInvite(boolean isOk, long inviterId, String invitationCode) {
        String info = "";
        if (isOk) {
            info = "type:1#number:" + invitationCode;
        } else {
            info = "type:2";
        }
        dynamic_4_Log_str(ThemeType.DYNAMIC_FRIEND_INVATE.getThemeId(), "invited", info, Long.toString(inviterId));
    }

    /**
     * vip玩家手机 qq信息记录日志
     */
    public void logVipInfo(String roleName, String cellphone, String qq) {
        StringBuffer sBuff = new StringBuffer();
        sBuff.append("vipInfo")
                .append("|").append(this.serverId)
                .append("|").append(getTime())
                .append("|").append("vipInfo")
                .append("|").append(this.uid)//账号ID
                .append("|").append(this.roleid)//角色ID
                .append("|").append(roleName)//角色昵称
                .append("|").append(this.reg_channel)//用户注册子渠道
                .append("|").append(this.level)//用户角色等级
                .append("|").append(this.vip)//用户VIP等级
                .append("|").append(cellphone)//手机号
                .append("|").append(qq)//qq
        ;
        ServerLogConst.vipInfo.info(sBuff.toString());
    }

    /**
     * 老玩家回归奖励日志
     *
     * @param day
     * @param reward
     */
    public void log_old_player_back_reward(int day, Map<Integer, Integer> reward) {
        LogBean logBean = new LogBean();
        logBean.setThemeId("497");
        logBean.setOperateId("comeback");
        StringBuilder sb = new StringBuilder();
        sb.append("reward_id:").append(day).append("#");
        int index = 1;
        sb.append("reward@number:");
        for (Map.Entry<Integer, Integer> entry : reward.entrySet()) {
            if (index == reward.size()) {
                sb.append(entry.getKey() + "@" + entry.getValue());
            } else {
                sb.append(entry.getKey() + "@" + entry.getValue() + "&");
            }
            index++;
        }
        logBean.setInfo(sb.toString());
        dynamic4Log(logBean);
    }
}

class LogBean {

    /**
     * 主题id
     */
    private String themeId;

    /**
     * 统计对象批次
     */
    private String objectBatch;
    /**
     * 统计类型
     */
    private String statType;
    /**
     * 统计对象
     */
    private String object;

    /**
     * 操作码
     */
    private String operateId;

    /**
     * 关联对象
     */
    private String associateObject;

    /**
     * 统计字段
     */
    private String info;

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public String getObjectBatch() {
        return objectBatch;
    }

    public void setObjectBatch(String objectBatch) {
        this.objectBatch = objectBatch;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public String getOperateId() {
        return operateId;
    }

    public void setOperateId(String operateId) {
        this.operateId = operateId;
    }

    public String getAssociateObject() {
        return associateObject;
    }

    public void setAssociateObject(String associateObject) {
        this.associateObject = associateObject;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getStatType() {
        return statType;
    }

    public void setStatType(String statType) {
        this.statType = statType;
    }
}
