package com.stars.modules.truename.packet;

import com.google.gson.Gson;
import com.stars.AccountRow;
import com.stars.ServerVersion;
import com.stars.SwitchEntranceGm;
import com.stars.http.net.HttpConn;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.userdata.AccountPassWord;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.truename.TrueNameManager;
import com.stars.modules.truename.TrueNamePacketSet;
import com.stars.modules.truename.userData.TrueNameCheckResponse;
import com.stars.modules.truename.userData.TrueNameSend;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.util.*;

import java.text.MessageFormat;

public class ServerTrueNameLogin extends Packet {
    private String token;
    private boolean isPressTest = false;//是否白名单
    private String trueName;
    private String idNum;

	@Override
	public short getType() {
		return TrueNamePacketSet.SERVER_TRUENAME_LOGIN;
	}

	@Override
	public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readFromBuffer(NewByteBuffer buff) {
		this.token = buff.readString();
    	this.trueName = buff.readString();
    	this.idNum = buff.readString();
	}

	@Override
	public void execPacket() {
		com.stars.util.LogUtil.info("实名认证|请求登录|token:{}", token);
        // 防止频繁处理同一连接的请求
        if (session.getAttribute("serverloginSession") != null) {
            com.stars.network.server.packet.PacketManager.send(session, new ClientText("请勿频繁点击"));
            Integer count = (Integer) session.getAttribute("serverlogin.count");
            if (count == null) {
                session.putAttribute("serverlogin.count", 1);
            }
            if (count <= 3) {
                session.putAttribute("serverlogin.count", count + 1);
            } else {
                closeSessionWhileException("实名认证|点击频繁", "请勿频繁点击", null);
            }
            return;
        }
        AccountPassWord apw = new AccountPassWord();
        long start = 0, end = 0;
        try {
        	start = System.currentTimeMillis();
            LoginInfo loginInfo = JsonUtil.fromJson(token, LoginInfo.class);
            //设置platform平台值
            loginInfo.setPhoneSystem(loginInfo.getPhoneSystem());//phoneSystem里一起赋值

            // 取账号AccountRow
            AccountRow accountRow = null;
            if (isDrirectConnected(loginInfo)) {
                // 直连
                initDirectConnectedLoginInfo(loginInfo); // 初始化直连
                apw.setAccount(loginInfo.getAccount());
                apw.setPassword(com.stars.util.Md5Util.getMD5Str(loginInfo.getPassword()));
                com.stars.util.LogUtil.info("实名认证|直连|account:{}|password:{}", apw.getAccount(), apw.getPassword());
                accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), apw.getPassword());
                if (accountRow == null && !isPressTest) {
                    com.stars.util.LogUtil.info("实名认证|直连|account:{}|异常:{}", loginInfo.getAccount(), "密码错误");
                    com.stars.network.server.packet.PacketManager.send(session, new ClientText("密码错误"));
                    return;
                }

            } else {
                // SDK(经过登陆服验证), account对应uid, password对应sid
                apw.setAccount(loginInfo.getUid() + "#" + loginInfo.getChannel().split("@")[0]); // UID#主渠道号
                apw.setPassword(loginInfo.getSid());
                apw.setUid(loginInfo.getUid());
                com.stars.util.LogUtil.info("实名认证|SDK|account:{}|password:{}", apw.getAccount(), apw.getPassword());
                if (apw.getPassword().length() <= 4) {
                    closeSessionWhileException(
                            "实名认证|SDK|account:" + apw.getAccount() + "|异常:密码长度至少为4",
                            "密码长度至少为4", null);
                    return;
                }
                if (isWhiteList(apw.getAccount())) {
                    ServerLogConst.console.info(MessageFormat.format("实名认证|SDK|account:{0}|白名单", apw.getAccount()));
                    accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), null);
                } else {
                    String resp = LoginUtil.checkKey(apw.getUid(), apw.getPassword());
                    if (resp != null) {
                        closeSessionWhileException(
                                "实名认证|SDK|account:" + apw.getAccount() + "|异常:" + resp, resp, null); // todo: 格式化
                        return;
                    }
                    accountRow = LoginModuleHelper.getOrLoadAccount(apw.getAccount(), null);
                }

                String verStr = loginInfo.getVerision();
                String[] version = verStr.split("[.]");
                if (version.length != 3) {
                    closeSessionWhileException("版本号格式不正确:" + loginInfo.getVerision(), "版本号不正确", null);
                    return;
                }
                int bigVersion = Integer.valueOf(version[0]);
                int smallVersion = Integer.valueOf(version[1]);
                if (bigVersion < ServerVersion.getBigVersion()) {
                    closeSessionWhileException("版本号不一致:" + verStr, "不支持版本", null);
                    return;
                }
            }

            identificationHandle(loginInfo);
		} catch (Exception e) {
			com.stars.util.LogUtil.error("", e);
		}
	}
        
    public void identificationHandle(LoginInfo loginInfo){
//    	trueName = "马彦博"; idNum = "445200198206141030";
    	StringBuffer url = new StringBuffer();
		url.append(TrueNameManager.URL).append(TrueNameManager.SAVE_METHOD);
	    StringBuffer requestStr = new StringBuffer();//请求参数
	    String emptyStr = "";
    	    String account = loginInfo.getUid();
//	    String account = "10001";
	    requestStr.append("appId=").append(TrueNameManager.APPID).append("&type=9&account=").append(account)
	    .append("&realName=").append(trueName).append("&cardType=1&cardNum=").append(idNum).append("&ext=").append(emptyStr)
	    .append("&version=").append(emptyStr).append("&ip=").append(emptyStr).append("&mac=").append(emptyStr)
	    .append("&imei=").append(emptyStr);
	    StringBuffer signStr = new StringBuffer();
	    String sign = Md5Util.md5(signStr.append(requestStr.toString()).append("||").append(TrueNameManager.appKey).toString());
	    requestStr.append("&sign=").append(sign);
	    
	    TrueNameSend sendData = new TrueNameSend(TrueNameManager.APPID, account, trueName, idNum, sign, "1");
	    String json = new Gson().toJson(sendData);
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
				if(TrueNameManager.SAVE_OK.equals(response.getStatus())){//保存成功
					ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_SUCCESS);
					send(clientPacket);
				}else{//返回失败信息
					if("SMZYZ_109".equals(response.getStatus())){
						ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_SUCCESS);
						send(clientPacket);
					}else if(TrueNameManager.responseMap.containsKey(response.getStatus())){	
						send(new ClientText(TrueNameManager.responseMap.get(response.getStatus())));
					}else{
						send(new ClientText("验证失败"));
					}
					com.stars.util.LogUtil.error("trueName saveMyInfo fail: "+response.getStatus());
				}
			} catch (Exception e) {
				ClientTrueName clientPacket = new ClientTrueName(TrueNameManager.SAVE_FAIL);
				send(clientPacket);
				com.stars.util.LogUtil.error("trueName identification fail", e);
			}
		}
    }

    public void closeSessionWhileException(String logStr, String infoStr, Throwable cause) {
        if (cause == null) {
            com.stars.util.LogUtil.error(logStr);
        } else {
            LogUtil.error(logStr, cause);
        }
        com.stars.network.server.packet.PacketManager.send(session, new ClientText(infoStr == null ? "请求异常" : infoStr));
        PacketManager.closeFrontend(session);
    }
    
    private boolean isEntranceClosed() {
        ServerLogConst.console.info("serverState=" + LoginModuleHelper.serverState);
        return LoginModuleHelper.serverState == SwitchEntranceGm.CLOSE;
    }

    private boolean isWhiteList(String accountName) {
        return true;
    }

    private boolean isDrirectConnected(LoginInfo loginInfo) {
        return loginInfo.getUid() == null && loginInfo.getSid() == null;
    }
    
    private void initDirectConnectedLoginInfo(LoginInfo loginInfo) {
        loginInfo.setPhoneNet("银汉wifi");
        loginInfo.setPhoneSystem("Android");
        if (loginInfo.getChannel() == null || loginInfo.getChannel().trim().equals("")) {
            loginInfo.setChannel("1@2@1026");
        }
        loginInfo.setVerision("0.0.0");
        loginInfo.setIp("127.0.0.1");
        loginInfo.setPlatForm("4");
        loginInfo.setImei("iemi1234");
        loginInfo.setMac("sdfsfsddddfdeefsdfa");
        loginInfo.setUid("yh8787");
        loginInfo.setPhoneType("大米手机");
        loginInfo.setNet("不是电信");
        loginInfo.setGameId("1054");
        loginInfo.setUserId("user23");
        loginInfo.setIdfa("version:56844");
    }

}
