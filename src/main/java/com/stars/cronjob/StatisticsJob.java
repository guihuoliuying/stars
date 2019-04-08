package com.stars.cronjob;

import com.stars.db.DBUtil;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.serverLog.logBean.Stat2Bean;
import com.stars.modules.serverLog.logBean.StatBean;
import com.stars.services.ServiceHelper;
import com.stars.util.ServerLogConst;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定时统计任务,每天的23:50开始统计
 * @author huangdimin
 *
 */
public class StatisticsJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		stat_account();
		stat_main_icon();
		stat_2_level();
		stat_2_vip();
		ServiceHelper.familyMainService().log_family();
		ServiceHelper.skyRankLocalService().static_log();
	}
	
	/**
	 * 
	 * 统计账号数
	 */
	public void stat_account(){
		String sql = "select channel,count(*) accountCount,palform from account group by channel";
		Connection conn=null;
		Statement stmt= null;
		ResultSet rs = null;
		try {
			conn = DBUtil.getConnection(DBUtil.DB_USER);
			stmt = (Statement) conn.createStatement();
			rs = (ResultSet) stmt.executeQuery(sql);
			ConcurrentHashMap<String, StatBean> accountCounts=new ConcurrentHashMap<>();
			while(rs.next()){
				StatBean bean = new StatBean();
				bean.setChannel(rs.getString(1));
				bean.setAccountCount(rs.getLong(2));
				bean.setPalform(rs.getString(3));
				//accountCounts.put(bean.getChannel(), bean);
				//System.err.println(getSubChannel(bean.getChannel())+"="+bean.getAccountCount());
				putOrAdd(accountCounts,getSubChannel(bean.getChannel()),bean);
			}
	
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String pid = name.split("@")[0];
			StatBean statBean = null;
			for(String channel:accountCounts.keySet()){
				statBean = accountCounts.get(channel);
				ServerLogModule.Log_core_stat_1(ThemeType.STAGE1_ACCOUNT.getOperateId(), channel, statBean.getAccountCount()+"",pid, statBean.getPalform());
				//System.err.println(ThemeType.STAGE1_ACCOUNT.getOperateId()+"="+accountCounts.get(channel).getAccountCount()+"|"+channel);
			}
		} catch (SQLException e) {
			ServerLogConst.exception.info(e.getMessage());
			ServerLogConst.console.info("统计渠道账号异常");
		}finally{
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(conn);
		}
	}
	
	public ArrayList<String> getChannelList(){
		String sql = "select distinct channel from account";
		try {
			List<String> channelList = (ArrayList<String>)DBUtil.queryList(DBUtil.DB_USER, String.class, sql);
		} catch (SQLException e) {
			ServerLogConst.exception.info(e.getMessage());
			ServerLogConst.console.info("统计渠道异常");
		}
		return null;
	}
	
	public void stat_main_icon(){
		String sql = "select channel,sum(gold) sumGold,sum(money) sumMoney,sum(bandgold) sumbandGold,palform from (select role.roleid,account.channel,account.palform,role.gold,role.money,role.bandgold,accountrole.account from role,accountrole,account where role.roleid=accountrole.roleid and account.name=accountrole.account) as t group by channel,palform";
		Connection conn=null;
		Statement stmt= null;
		ResultSet rs = null;
		ConcurrentHashMap<String, StatBean> accountGold = new ConcurrentHashMap<>();
		try {
			conn = DBUtil.getConnection(DBUtil.DB_USER);
			stmt = (Statement) conn.createStatement();
			rs = (ResultSet) stmt.executeQuery(sql);
			while(rs.next()){
				StatBean bean = new StatBean();
				bean.setChannel(rs.getString(1));
				bean.setSumGold(rs.getLong(2));
				bean.setSumMoney(rs.getLong(3));
				bean.setSumbandGold(rs.getLong(4));
				bean.setPalform(rs.getString(5));
				//accountGold.put(bean.getChannel(), bean);
				putOrAdd(accountGold,getSubChannel(bean.getChannel())+"_"+bean.getPalform(),bean);
			}
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String pid = name.split("@")[0];
			StatBean statBean = null;
			String subChannel = "";
			for(String channel:accountGold.keySet()){
				statBean = accountGold.get(channel);
				if(statBean==null) continue;
				subChannel = channel.split("_")[0];
				ServerLogModule.Log_core_stat_1(ThemeType.STAGE1_GOLD.getOperateId(), subChannel, statBean.getSumGold()+"", pid, statBean.getPalform());
				ServerLogModule.Log_core_finance_hold(subChannel,statBean.getSumGold()+"");
				ServerLogModule.Log_core_stat_1(ThemeType.STAGE1_BANDGOLD.getOperateId(), subChannel, statBean.getSumbandGold()+"", pid, statBean.getPalform());
				ServerLogModule.Log_core_stat_1(ThemeType.STAGE1_MONEY.getOperateId(), subChannel, statBean.getSumMoney()+"", pid, statBean.getPalform());
			}
		} catch (SQLException e) {
			ServerLogConst.exception.info(e.getMessage());
			ServerLogConst.console.info("统计金币异常");
		}finally{
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(conn);
		}
	}
	
	private String getSubChannel(String channel){
		if(channel==null){return "";}
		String[] temp = channel.split("@");
		if(temp.length<=2){
			return "2";
		}else{
			return temp[1];
		}		
	}

	private void putOrAdd(ConcurrentHashMap<String, StatBean> map,String channel,StatBean bean){
		if(map.get(channel)==null){
			map.put(channel, bean);
		}else{
			StatBean sorce = map.get(channel);
			StatBean newBean = new StatBean();
			newBean.setSumbandGold(sorce.getSumbandGold()+bean.getSumbandGold());
			newBean.setSumGold(sorce.getSumGold()+bean.getSumGold());
			newBean.setSumMoney(sorce.getSumMoney()+bean.getSumMoney());
			newBean.setAccountCount(sorce.getAccountCount()+bean.getAccountCount());
			newBean.setChannel(channel);
			newBean.setPalform(sorce.getPalform());
			map.put(channel, newBean);
		}
	}
	
	
	private void putOrAdd(ConcurrentHashMap<String, Stat2Bean> map, String key, Stat2Bean bean){
		if(map.get(key)==null){
			map.put(key, bean);
		}else{
			Stat2Bean sorce = map.get(key);
			Stat2Bean newBean = new Stat2Bean();
			newBean.setLevelcount(sorce.getLevelcount()+bean.getLevelcount());
			newBean.setLevel(sorce.getLevel());
			newBean.setChannel(sorce.getChannel());
			newBean.setPalform(sorce.getPalform());
			newBean.setVip(sorce.getVip());
			newBean.setVipcount(sorce.getVipcount()+bean.getVipcount());
			map.put(key, newBean);
			ServerLogConst.console.info("合并key"+key);
		}
	}
	
	
	//角色等级数分布,account表,role表,accountrole表
	//验证sql
	//select account.channel,role.roleid,role.level from role,accountrole,account where account.name=accountrole.account and accountrole.roleid=role.roleid and account.channel='1000@1000@1105919840' and role.level='9';
	public void stat_2_level(){
		String sql = "select t.palform,t.channel,count(t.level) levelcount,t.level from ((select account.name,account.palform,account.channel,role.roleid,role.level from role,accountrole,account where account.name=accountrole.account and accountrole.roleid=role.roleid) as t) group by t.level,t.channel,t.palform";	
		Connection conn=null;
		Statement stmt= null;
		ResultSet rs = null;
		ConcurrentHashMap<String, Stat2Bean> levelStat = new ConcurrentHashMap<>();
		try {
			conn = DBUtil.getConnection(DBUtil.DB_USER);
			stmt = (Statement) conn.createStatement();
			rs = (ResultSet) stmt.executeQuery(sql);
			while(rs.next()){	
				String channel = rs.getString(2);
				if(!channel.contains("@")){
					ServerLogConst.console.info("去掉非法渠道"+rs.getString(2));
					continue;
				}
				Stat2Bean bean = new Stat2Bean();
				bean.setPalform(rs.getString(1));
				bean.setChannel(channel.split("@")[1]);
				bean.setLevelcount(rs.getInt(3));
				bean.setLevel(rs.getInt(4));
				putOrAdd(levelStat,bean.getPalform()+"_"+bean.getLevel()+"_"+bean.getChannel(),bean);
				ServerLogConst.console.info("****"+bean.getPalform()+"_"+bean.getLevel()+"_"+bean.getChannel()+"|"+bean.getLevelcount());
			}
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String pid = name.split("@")[0];	
			System.err.println("szie="+levelStat.size());
			for(Stat2Bean b:levelStat.values()){
				ServerLogModule.Log_core_stat_2(ThemeType.STAGE2_LEVEL.getOperateName(), b.getChannel(), b.getLevel()+"", b.getLevelcount(), b.getPalform(),pid);
			}
		} catch (SQLException e) {
			ServerLogConst.exception.info(e.getMessage());
			ServerLogConst.console.info("统计等级分布异常");
		}finally{
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(conn);
		}
		
	}
	
	
	public void stat_2_vip(){
		String sql = "select account.channel,account.palform,account.viplevel,count(*) vipcount from account group by account.viplevel,account.channel,account.palform";
		Connection conn=null;
		Statement stmt= null;
		ResultSet rs = null;
		ConcurrentHashMap<String, Stat2Bean> levelStat = new ConcurrentHashMap<>();
		try {
			conn = DBUtil.getConnection(DBUtil.DB_USER);
			stmt = (Statement) conn.createStatement();
			rs = (ResultSet) stmt.executeQuery(sql);
			while(rs.next()){
				Stat2Bean bean = new Stat2Bean();
				if(!rs.getString(1).contains("@")){continue;}
				bean.setChannel(rs.getString(1).split("@")[1]);
				bean.setPalform(rs.getString(2));
				bean.setVip(rs.getInt(3));
				bean.setVipcount(rs.getInt(4));
//				if (bean.getVip() <= 0)
//					continue;
				putOrAdd(levelStat,bean.getPalform()+"_"+bean.getVip()+"_"+bean.getChannel(),bean);
			}
			String name = ManagementFactory.getRuntimeMXBean().getName();
			String pid = name.split("@")[0];
			for(Stat2Bean b:levelStat.values()){
				ServerLogModule.Log_core_stat_2(ThemeType.STAGE2_VIP.getOperateName(), b.getChannel(), b.getVip()+"", b.getVipcount(), b.getPalform(),pid);
			}
		} catch (SQLException e) {
			ServerLogConst.exception.info(e.getMessage());
			ServerLogConst.console.info("统计vip分布异常");
		}finally{
			DBUtil.closeResultSet(rs);
			DBUtil.closeStatement(stmt);
			DBUtil.closeConnection(conn);
		}		
	}
	
}
