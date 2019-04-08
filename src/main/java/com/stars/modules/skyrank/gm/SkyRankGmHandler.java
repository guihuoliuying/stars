package com.stars.modules.skyrank.gm;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.gm.GmHandler;
import com.stars.modules.role.RoleModule;
import com.stars.modules.skyrank.SkyRankScoreHandle;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.Map;

/**
 * 天梯GM命令
 * @author xieyuejun
 *
 */
public class SkyRankGmHandler implements GmHandler {

    @Override
	public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
    	RoleModule rm = (RoleModule) moduleMap.get(MConst.Role);
    	LogUtil.info("SkyRankGmHandler  {}, {}",args);
		switch (args[0]) {
		case "addscore":
			short type = Byte.parseByte(args[1]);
			 ServiceHelper.skyRankLocalService().handleScoreChange(new SkyRankScoreHandle(roleId,type,(byte) 1,rm.getRoleRow().getName(),rm.getRoleRow().getFightScore()));
			break;
		case "subscore":
			type = Byte.parseByte(args[1]);
			 ServiceHelper.skyRankLocalService().handleScoreChange(new SkyRankScoreHandle(roleId,type,(byte) 0,rm.getRoleRow().getName(),rm.getRoleRow().getFightScore()));
			break;
		case "dreset":
			ServiceHelper.skyRankLocalService().dailyReset();
			break;
		}
		if(args[0].indexOf("h") == 0){
			ServiceHelper.skyRankLocalService().gmHandle(roleId, args);
		}
	}

}
