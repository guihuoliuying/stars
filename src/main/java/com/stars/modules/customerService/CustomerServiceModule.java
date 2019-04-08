package com.stars.modules.customerService;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.customerService.packet.ClientCustomerService;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.services.ServiceHelper;
import com.stars.startup.MainStartup;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class CustomerServiceModule extends AbstractModule {

	public CustomerServiceModule(long id, Player self, EventDispatcher eventDispatcher,
                                 Map<String, Module> moduleMap) {
		super("CustomerService", id, self, eventDispatcher, moduleMap);
	}
	
	private long chargeMoney;//开发30天内充值数
	
	@Override
	public void onDataReq() throws Throwable {
		getChargeMoney();
	}
	
	@Override
	public void onInit(boolean isCreation) throws Throwable {
		check();
	}
	
	private void check(){
		byte state = getByte("customer_service_vip_charge");
		if(chargeMoney>=CustomerServiceManager.CHARGE_MONEY_TARGET&&state!=CustomerServiceManager.SEND_STATE){
			setByte("customer_service_vip_charge", CustomerServiceManager.SEND_STATE);
			//发送邮件
			Integer templateId = CustomerServiceManager.channelMailMap.get(MainStartup.serverChannel);
			if(templateId==null){
				com.stars.util.LogUtil.error("CustomerService send mail fail, serverChannel:"+MainStartup.serverChannel);
			}
			int coolTime = DateUtil.getDateAfterN(CustomerServiceManager.COOL_DAYS);
			ServiceHelper.emailService().sendToSingleWithCoolTime(id(), templateId, 0L, "系统", null, coolTime);
		}
	}
	
	public void getChargeMoney() throws SQLException{
		Date openServerDate = DataManager.getOpenServerDate();
		int openServerTime = (int)(openServerDate.getTime()/1000);
		Date dateAfterN = DateUtil.getDateAfterN(openServerDate, CustomerServiceManager.COUNT_DAYS);
		int endTime = (int)(dateAfterN.getTime()/1000);
		StringBuffer sql = new StringBuffer();
		sql.append("select sum(money) from payment where roleid = ").append(id())
			.append(" and UNIX_TIMESTAMP(time) > ").append(openServerTime)
			.append(" and UNIX_TIMESTAMP(time) < ").append(endTime);
		chargeMoney = DBUtil.queryCount(DBUtil.DB_USER, sql.toString());
	}
	
	public void handleChargeEvent(int money){
		try {
			Date openServerDate = DataManager.getOpenServerDate();
			Date dateAfterN = DateUtil.getDateAfterN(openServerDate, CustomerServiceManager.COUNT_DAYS);
			int endTime = (int)(dateAfterN.getTime()/1000);
			int currentTime = DateUtil.getCurrentTimeInt();
			if(currentTime>endTime) return;
			this.chargeMoney += money;
			check();
		} catch (Exception e) {
			com.stars.util.LogUtil.error("CustomerService handleChargeEvent", e);
		}
	}
	
	public void savePlayerInfo(String cellphone, String qq){
		try {
			byte state = getByte("CustomerService_PlayerInfo");
			if(state==1){//已经记录过
				ClientCustomerService packet = new ClientCustomerService();
				packet.setResult((byte)2);
				send(packet);
				return;
			}
			RoleModule roleModule = module(MConst.Role);
			String roleName = roleModule.getRoleRow().getName();
			ServerLogModule log = module(MConst.ServerLog);
			log.logVipInfo(roleName, cellphone, qq);
			//发奖励
			DropModule drop = module(MConst.Drop);
			Map<Integer, Integer> rewardMap = drop.executeDrop(CustomerServiceManager.AWARD_DROP_ID, 1, true);
			ServiceHelper.emailService().sendToSingle(id(), CustomerServiceManager.AWARD_MAIL_ID, 0L, "系统", rewardMap);
			setByte("CustomerService_PlayerInfo", (byte)1);
			ClientCustomerService packet = new ClientCustomerService();
			packet.setResult((byte)1);
			send(packet);
		} catch (Exception e) {
			ClientCustomerService packet = new ClientCustomerService();
			packet.setResult((byte)0);
			send(packet);
			LogUtil.error("CustomerService PlayerInfo", e);
		}
	}
	
	public byte getSavePlayInfoState(){
		byte state = getByte("CustomerService_PlayerInfo");
		return state;
	}

}
