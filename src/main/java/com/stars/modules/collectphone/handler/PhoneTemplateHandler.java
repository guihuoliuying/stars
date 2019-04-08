package com.stars.modules.collectphone.handler;

import com.stars.core.module.Module;
import com.stars.http.net.HttpConn;
import com.stars.modules.MConst;
import com.stars.modules.collectphone.CollectPhoneUtil;
import com.stars.modules.collectphone.packet.ClientCollectPhonePacket;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.role.RoleModule;
import com.stars.modules.vip.VipModule;
import com.stars.multiserver.MultiServerHelper;
import com.stars.util.JsonUtil;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class PhoneTemplateHandler extends AbstractTemplateHandler {
    public static final int SUBMIT_PHONE = 1;//提交电话
    public static final int SUBMIT_VERIFY_CODE = 2;//提交验证码
    public static final String SUBMIT_PHONE_URL = "http://123.206.131.206/phoneCollect/getCode.do";//提交验证码
    public static final String CHECK_CODE_URL = "http://123.206.131.206/phoneCollect/checkCode.do";//检验验证码
    public static final String SIGN = "34fd2d07f809db85783fa6d9dbd938d0";
    private int innerStep;//内部第几步，如：第一步：发送手机号，第二步：发送验证码
    private boolean success = false;

    public PhoneTemplateHandler(int step, RoleCollectPhone roleCollectPhone, Map<String, Module> moduleMap) {
        super(step, roleCollectPhone, moduleMap);
    }

    @Override
    public void submit(String answer) {
        String[] group = answer.split("\\|");
        innerStep = Integer.parseInt(group[0]);
        switch (innerStep) {
            case SUBMIT_PHONE: {
                submitPhone(group[1]);
                getRoleCollectPhone().put(getStep(), group[1]);

            }
            break;
            case SUBMIT_VERIFY_CODE: {
                submitVerifyCode(group[1]);
            }
            break;
        }
    }

    /**
     * 提交验证码
     *
     * @param answer
     */
    private void submitVerifyCode(String answer) {
        HashMap<String, String> hashMap = JsonUtil.jsonToMap(answer);
        String phone = hashMap.get("phone");
        String code = hashMap.get("code");
        LoginModule loginModule = (LoginModule) getModuleMap().get(MConst.Login);
        RoleModule roleModule = (RoleModule) getModuleMap().get(MConst.Role);
        VipModule vipModule = (VipModule) getModuleMap().get(MConst.Vip);
        HttpConn httpConn = new HttpConn(CHECK_CODE_URL);
        StringBuilder sb = new StringBuilder();
        sb.append("sign").append("=").append(SIGN).append("&");
        sb.append("gameId").append("=").append(1026).append("&");
        sb.append("versionId").append("=").append(MultiServerHelper.getAreaId()).append("&");
        sb.append("serverId").append("=").append(MultiServerHelper.getServerId()).append("&");
        sb.append("account").append("=").append(loginModule.getAccount()).append("&");
        sb.append("roleId").append("=").append(loginModule.id()).append("&");
        sb.append("roleName").append("=").append(roleModule.getRoleRow().getName()).append("&");
        sb.append("roleLev").append("=").append(roleModule.getLevel()).append("&");
        sb.append("vipLev").append("=").append(vipModule.getVipLevel()).append("&");
        sb.append("channelId").append("=").append(loginModule.getChannnel()).append("&");
        sb.append("actionType").append("=").append(1).append("&");
        sb.append("phone").append("=").append(phone).append("&");
        sb.append("messageCode").append("=").append(code);
        httpConn.post(sb.toString());
        String responseStr = httpConn.getResponseStr();
        com.stars.util.LogUtil.info("roleid:{} collectPhone response:{}", loginModule.id(), responseStr);
        HashMap<String, String> responseMap = JsonUtil.jsonToMap(responseStr);
        String status = responseMap.get("status");
        String msg = responseMap.get("msg");
        ClientCollectPhonePacket clientCollectPhonePacket = new ClientCollectPhonePacket(OperateActivityConstant.ActType_CollectPhone, ClientCollectPhonePacket.SEND_SUBMIT_VERIFY_CODE);
        clientCollectPhonePacket.setData(responseStr);
        loginModule.send(clientCollectPhonePacket);
        switch (Integer.parseInt(status)) {
            case CollectPhoneConst.status_success: {
                success = true;
                com.stars.util.LogUtil.info("roleid:{} submit verify code:{} successfull", loginModule.id(), phone);
            }
            break;
            case CollectPhoneConst.status_account_error: {
                com.stars.util.LogUtil.error(msg);
            }
            break;
            case CollectPhoneConst.status_encrypt_error: {
                com.stars.util.LogUtil.error(msg);
            }
            break;
            case CollectPhoneConst.status_times_out: {
                loginModule.warn(msg);
            }
            break;
            case CollectPhoneConst.status_code_error: {
                loginModule.warn(msg);
            }
            break;
        }
    }

    /**
     * 提交手机号码
     * 成功：
     * {"status":0,"msg":"成功"}
     * 参数错误：
     * {"status":1,"msg":"account参数错误"}
     * {"status":2,"msg":"加密信息错误"}
     * 验证次数过多：
     * {"status":1001,"msg":"今日发送验证码次数达到上限，请明日再来"}
     *
     * @param phone
     */
    private void submitPhone(String phone) {
        LoginModule loginModule = (LoginModule) getModuleMap().get(MConst.Login);
        HttpConn httpConn = new HttpConn(SUBMIT_PHONE_URL);
        StringBuilder sb = new StringBuilder();
        sb.append("sign").append("=").append(SIGN).append("&");
        sb.append("gameId").append("=").append(1026).append("&");
        sb.append("versionId").append("=").append(MultiServerHelper.getAreaId()).append("&");
        sb.append("serverId").append("=").append(MultiServerHelper.getServerId()).append("&");
        sb.append("account").append("=").append(loginModule.getAccount()).append("&");
        sb.append("roleId").append("=").append(loginModule.id()).append("&");
        sb.append("actionType").append("=").append(1).append("&");
        sb.append("phone").append("=").append(phone);
        httpConn.post(sb.toString());
        String responseStr = httpConn.getResponseStr();
        com.stars.util.LogUtil.info("roleid:{} collectPhone response:{}", loginModule.id(), responseStr);
        if (StringUtil.isEmpty(responseStr)) {//可能不会返还结果
            loginModule.warn("连接超时,请重试");
            return;
        }
        getRoleCollectPhone().setLastMsgTime(System.currentTimeMillis());
        HashMap<String, String> responseMap = JsonUtil.jsonToMap(responseStr);
        String status = responseMap.get("status");
        String msg = responseMap.get("msg");
        responseMap.put("remainSeconds", CollectPhoneUtil.getRemainSecond(getRoleCollectPhone()) + "");
        ClientCollectPhonePacket clientCollectPhonePacket = new ClientCollectPhonePacket(OperateActivityConstant.ActType_CollectPhone, ClientCollectPhonePacket.SEND_SUBMIT_PHONE);
        clientCollectPhonePacket.setData(JsonUtil.toJson(responseMap));
        loginModule.send(clientCollectPhonePacket);
        switch (Integer.parseInt(status)) {
            case CollectPhoneConst.status_success: {
                com.stars.util.LogUtil.info("roleid:{} submit phone:{} successfull", loginModule.id(), phone);
            }
            break;
            case CollectPhoneConst.status_account_error: {
                com.stars.util.LogUtil.error(msg);
            }
            break;
            case CollectPhoneConst.status_encrypt_error: {
                LogUtil.error(msg);
            }
            break;
            case CollectPhoneConst.status_times_out: {
                loginModule.warn(msg);
            }
            break;
        }
    }

    @Override
    public boolean isOver() {
        return success;
    }
}
