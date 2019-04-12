package com.stars.cronjob;

import com.stars.AccountRow;
import com.stars.core.actor.AbstractActor;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.network.server.session.GameSession;
import com.stars.services.ServiceHelper;
import com.stars.startup.MainStartup;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.management.ManagementFactory;
import java.util.concurrent.ConcurrentHashMap;

public class TenMinuteJob implements Job{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		ServerLogConst.console.info("执行job开始");
		statisticsLog();
		ServerLogConst.console.info("执行job结束");
		printServiceState();
	}
    
	/*统计在线人数10分钟1次*/
	public static void statisticsLog(){
		ConcurrentHashMap<String, String[]> channelRole = new ConcurrentHashMap<>();
		for (AbstractActor actor : PlayerSystem.system().getActors().values()) {
			if (actor instanceof Player) {
				GameSession gameSession = ((Player) actor).session();
				if (gameSession != null && gameSession.isActive()) {
					if(gameSession.getAccount()==null){continue;}
					String transferAccount = LoginModuleHelper.getTransferAccount(gameSession.getAccount());
					AccountRow account = MainStartup.accountMap.get(transferAccount);
					if(account==null){
						continue;
					}
					if(account.getChannel().split("@").length>1){						
						String sub_channel = account.getChannel().split("@")[1];
						String palform = account.getPalform();
						if (channelRole.get(sub_channel+"_"+palform) != null) {
							int num = Integer.parseInt(channelRole.get(sub_channel+"_"+palform)[0]) + 1;
							channelRole.put(sub_channel+"_"+palform, new String[]{String.valueOf(num), palform});
						} else {
							channelRole.put(sub_channel+"_"+palform, new String[]{"1", palform});
						}
					}
				}
			}
		}
		String name = ManagementFactory.getRuntimeMXBean().getName();
		String pid = name.split("@")[0];
	}

    public static void printServiceState() {
        try {
		/* 打印状态 */
            ServiceHelper.summaryService().printState();
            ServiceHelper.chatService().printState();
        } catch (Throwable cause) {
            LogUtil.error("printServiceState异常", cause);
        }
    }
}
