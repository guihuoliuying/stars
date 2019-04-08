package com.stars.services.advertInf;

import com.stars.coreManager.ExcutorKey;
import com.stars.coreManager.SchedulerManager;
import com.stars.http.net.HttpConn;
import com.stars.modules.demologin.LoginConstant;
import com.stars.modules.demologin.userdata.AdvertInfResponse;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.server.login.util.Md5Util;
import com.stars.services.ServiceHelper;
import com.stars.util.JsonUtil;
import com.stars.util.LogUtil;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 通知银汉广告服
 * @author huzhipeng
 * 不走actor
 *
 */
public class AdvertInfServiceActor implements AdvertInfService{
	
	private ConcurrentHashMap<Long, LoginInfo> sendMap = new ConcurrentHashMap<>();
	
	@Override
	public void init() throws Throwable {
		// TODO Auto-generated method stub
		SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.AdvertInf, new AdvertInfTask(), 1, 10, TimeUnit.SECONDS);
	}

	@Override
	public void printState() {
		LogUtil.info("容器大小输出:{},sendMap.size:{}",  this.getClass().getSimpleName(),  sendMap.size());
	}

	public class AdvertInfTask implements Runnable{

		@Override
		public void run() {//通知银汉广告服务
			Iterator<Entry<Long, LoginInfo>> iterator = sendMap.entrySet().iterator();
			Entry<Long, LoginInfo> entry = null;
			for(;iterator.hasNext();){
				entry = iterator.next();
				ServiceHelper.advertInfService().noticeAdvertInf(entry.getKey(), entry.getValue());
				iterator.remove();
			}
		}

	}
	
	public void noticeAdvertInf(long roleId, LoginInfo loginInfo){
		long time = System.currentTimeMillis();
		HttpConn httpConn = new HttpConn(LoginConstant.MAIN_HTTP_URL);
    	//http://ad.yhres.cn/role?gameId=xxx&userId=xxx&roleId=xxx&idfa=xxx&mac=&channel&sign=xxx
		StringBuffer requestStr = new StringBuffer();
		String userId = loginInfo.getUserId();
		if("IOS".equals(loginInfo.getPhoneSystem())){
			userId = "45_"+userId;
		}
		requestStr.append("gameId=").append(loginInfo.getGameId()).append("&userId=").append(userId)
			.append("&roleId=").append(roleId).append("&idfa=").append(loginInfo.getIdfa())
			.append("&mac=").append(loginInfo.getMac()).append("&channel=").append(loginInfo.getChannel());
		//sign=MD5(gameId=xxx&userId=xxx&roleId=xxx&idfa=xxx&mac=&channel=xxx||encKey) 
		String sign = Md5Util.getMD5Str(requestStr.toString()+"||"+LoginConstant.ADVERT_KEY);
		requestStr.append("&sign=").append(sign);
		httpConn.get(requestStr.toString());
		httpConn.close();
		if (httpConn.isError()) {
			httpConn = new HttpConn(LoginConstant.MAIN_HTTP_URL);
			httpConn.get(requestStr.toString());
			httpConn.close();
		}
		if (httpConn.getResponseStr() != null) {
			try {	
				LogUtil.info("广告服返回: "+httpConn.getResponseStr());
				AdvertInfResponse advertInfResponse = JsonUtil.fromJson(httpConn.getResponseStr(), AdvertInfResponse.class);
			} catch (Exception e) {
				LogUtil.error("广告服返回异常", e);
			}
		}

	}
	
	/**
	 * 加入发送集合
	 */
	public void addToSendMap(long roleId, LoginInfo loginInfo){
		sendMap.put(roleId, loginInfo);
	}

}
