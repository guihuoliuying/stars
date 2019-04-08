package com.stars.modules.truename;

import com.google.gson.Gson;
import com.stars.AccountRow;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.http.net.HttpConn;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.drop.DropModule;
import com.stars.modules.truename.packet.ClientTrueName;
import com.stars.modules.truename.packet.ServerTrueName;
import com.stars.modules.truename.userData.TrueNameCheckResponse;
import com.stars.modules.truename.userData.TrueNameSend;
import com.stars.services.ServiceHelper;
import com.stars.util.JsonUtil;
import com.stars.util.LogUtil;
import com.stars.util.Md5Util;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TrueNameModule extends AbstractModule {

    public TrueNameModule(long id, Player self, EventDispatcher eventDispatcher,
                          Map<String, Module> moduleMap) {
        super("TrueName", id, self, eventDispatcher, moduleMap);
    }

    private String gmAccount;

    private boolean isGmPass = false;

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        identification();
//		saveMyInfo(new ServerTrueName());
    }

    /**
     * 认证检测
     *
     * @throws SQLException
     */
    public void identification() throws SQLException {
        if (!TrueNameManager.isOpen) return;
        LoginModule lModule = module(MConst.Login);
        AccountRow accountRow = lModule.getAccountRow();
        String mainChannel = accountRow.getChannel().split("@")[0];
        if (!"1".equals(mainChannel) && !"45".equals(mainChannel) && !isGmPass) {
            return;
        }
        if (accountRow.getIdentAward() == 1) return;
        LoginInfo loginInfo = accountRow.getLoginInfo();
        String account = "";
        if (loginInfo == null) {
            account = lModule.getAccount();
        } else {
            account = loginInfo.getUid();
        }
        StringBuffer url = new StringBuffer();
        url.append(TrueNameManager.URL).append(TrueNameManager.CHECK_METHOD);
        StringBuffer requestStr = new StringBuffer();//请求参数
        Map<String, Object> sendMap = new HashMap<String, Object>();
        sendMap.put("appId", TrueNameManager.APPID);
        sendMap.put("account", account);
        if (gmAccount != null) {
            account = gmAccount;
        }
        requestStr.append("appId=").append(TrueNameManager.APPID).append("&account=").append(account);
        StringBuffer signStr = new StringBuffer();
        signStr.append(requestStr.toString()).append("||").append(TrueNameManager.appKey);
        String sign = com.stars.util.Md5Util.md5(signStr.toString());
        requestStr.append("&sign=").append(sign);
        sendMap.put("sign", sign);
        String json = new Gson().toJson(sendMap);
        HttpConn httpConn = new HttpConn(url.toString());
        httpConn.get(json);
        httpConn.close();
        if (httpConn.isError()) {
            httpConn = new HttpConn(url.toString());
            httpConn.get(json);
            httpConn.close();
        }
        if (httpConn.getResponseStr() != null) {
            try {
                com.stars.util.LogUtil.info(httpConn.getResponseStr());
                TrueNameCheckResponse response = JsonUtil.fromJson(httpConn.getResponseStr(), TrueNameCheckResponse.class);
                if (TrueNameManager.CHECK_OK.equals(response.getStatus())) {//已经实名认证
                    accountRow.setIdentification((byte) 2);
                    sendAward(accountRow);
                } else {//通知客户端未实名认证
                    accountRow.setIdentification((byte) 1);
                    ClientTrueName packet = new ClientTrueName(TrueNameManager.NOT_IDENTIFICATION);
                    send(packet);
                }
                context().update(accountRow);
            } catch (Exception e) {
                com.stars.util.LogUtil.error("trueName identification fail", e);
            }
        }
    }

    /**
     * 保存实名认证信息
     */
    public void saveMyInfo(ServerTrueName packet) {
        try {
            if (!TrueNameManager.isOpen) return;
            String name = packet.getName();
            String idNum = packet.getIdNum();
            String cardType = "" + packet.getCardType();
            LoginModule lModule = module(MConst.Login);
            AccountRow accountRow = lModule.getAccountRow();
            String mainChannel = accountRow.getChannel().split("@")[0];
            if (!"1".equals(mainChannel) && !"45".equals(mainChannel) && !isGmPass) {
                return;
            }
            if (accountRow.getIdentAward() == 1) {
                send(new ClientText("此账号已验证过"));
                return;
            }
            LoginInfo loginInfo = accountRow.getLoginInfo();
            StringBuffer url = new StringBuffer();
            url.append(TrueNameManager.URL).append(TrueNameManager.SAVE_METHOD);
            StringBuffer requestStr = new StringBuffer();//请求参数
            String emptyStr = "";
            String account = loginInfo.getUid();
            if (gmAccount != null) {
                account = gmAccount;
            }
            requestStr.append("appId=").append(TrueNameManager.APPID).append("&type=9&account=").append(account)
                    .append("&realName=").append(name).append("&cardType=").append(cardType).append("&cardNum=").append(idNum).append("&ext=").append(emptyStr)
                    .append("&version=").append(emptyStr).append("&ip=").append(emptyStr).append("&mac=").append(emptyStr)
                    .append("&imei=").append(emptyStr);
            StringBuffer signStr = new StringBuffer();
            String md5Str = signStr.append(requestStr.toString()).append("||").append(TrueNameManager.appKey).toString();
            String sign = Md5Util.md5(md5Str);
            requestStr.append("&sign=").append(sign);

            TrueNameSend sendData = new TrueNameSend(TrueNameManager.APPID, account, name, idNum, sign, cardType);
            String json = new Gson().toJson(sendData);
            com.stars.util.LogUtil.info("trueName saveMyInfo INFO : " + json);
            HttpConn httpConn = new HttpConn(url.toString());
            httpConn.setContentType("text/plain; charset=UTF-8");
            httpConn.get(json.getBytes("UTF-8"));
            httpConn.close();
            if (httpConn.isError()) {
                httpConn = new HttpConn(url.toString());
                httpConn.setContentType("text/plain; charset=UTF-8");
                httpConn.get(json);
                httpConn.close();
            }
            if (httpConn.getResponseStr() != null) {
                try {
                    com.stars.util.LogUtil.info(httpConn.getResponseStr());
                    TrueNameCheckResponse response = JsonUtil.fromJson(httpConn.getResponseStr(), TrueNameCheckResponse.class);
                    if (TrueNameManager.SAVE_OK.equals(response.getStatus())) {//保存成功
                        accountRow.setIdentification((byte) 2);
                        sendAward(accountRow);
                        ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_SUCCESS);
                        send(clientPacket);
                        context().update(accountRow);
                    } else {//返回失败信息
//						if("SMZYZ_109".equals(response.getStatus())){
//							ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_SUCCESS);
//							send(clientPacket);
//						}else 
//                        if (TrueNameManager.responseMap.containsKey(response.getStatus())) {
//                            send(new ClientText(TrueNameManager.responseMap.get(response.getStatus())));
//                        } else {
//                            send(new ClientText("验证失败"));
//                        }
                    	send(new ClientText(response.getMsg()));
                        com.stars.util.LogUtil.error("trueName saveMyInfo fail: " + response.getStatus());
                    }
                } catch (Exception e) {
                    ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_FAIL);
                    send(clientPacket);
                    com.stars.util.LogUtil.error("trueName identification fail", e);
                }
            }
        } catch (Exception e) {
            ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_FAIL);
            send(clientPacket);
            LogUtil.error("trueName saveMyInfo fail", e);
        }
    }

    /**
     * 发放奖励
     */
    public void sendAward(AccountRow accountRow) {
        DropModule drop = module(MConst.Drop);
        Map<Integer, Integer> dropMap = drop.executeDrop(TrueNameManager.AWARD, 1, true);
//    	ToolModule toolModule = (ToolModule) module(MConst.Tool);
//    	Map<Integer, Integer> getReward = toolModule.addAndSend(dropMap,EventType.TRUE_NAME.getCode());
//    	//发获奖提示到客户端
//    	ClientAward clientAward = new ClientAward(getReward);
//    	send(clientAward);
        ServiceHelper.emailService().sendToSingle(id(), TrueNameManager.MAIL_ID, 0L, "系统", dropMap);
        accountRow.setIdentAward((byte) 1);
        context().update(accountRow);
        send(new ClientText("certification_suctips"));
    }

    public void gmHandle(String[] args) {
        byte opType = Byte.parseByte(args[0]);
        if (opType == 1) {
            this.gmAccount = args[1];
        } else if (opType == 2) {
            this.isGmPass = true;
        } else if (opType == 3) {
            this.isGmPass = false;
        } else if (opType == 4) {
            this.gmAccount = null;
        } else if (opType == 5) {
            TrueNameManager.isOpen = false;
        } else if (opType == 6) {
            TrueNameManager.isOpen = true;
        }
    }

}
