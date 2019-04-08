package com.stars.modules.levelSpeedUp;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.levelSpeedUp.productData.LevelSpeedUpAdditionVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.Map;

public class LevelSpeedUpModule extends AbstractModule {

	public LevelSpeedUpModule(long id, Player self, EventDispatcher eventDispatcher,
                              Map<String, Module> moduleMap) {
		super("LevelSpeedUp", id, self, eventDispatcher, moduleMap);
	}
	
	@Override
	public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
		
	}
	
	public int getLevelSpeedUpAddition(){
		int addition = 0;
		RoleModule roleModule = module(MConst.Role);
		int level = roleModule.getRoleRow().getLevel();
		if(LevelSpeedUpManager.TOP_LEVEL<LevelSpeedUpManager.TOP_LEVEL_STANDARD){
			com.stars.util.LogUtil.info("LevelSpeedUp , 最高等级不满足, TOP_LEVEL:"+LevelSpeedUpManager.TOP_LEVEL
					+" , TOP_LEVEL_STANDARD:"+LevelSpeedUpManager.TOP_LEVEL_STANDARD);
			return addition;//最高等级不满足
		}
		if(LevelSpeedUpManager.OPEN_DAYS<LevelSpeedUpManager.OPEN_DAYS_STANDARD){
			com.stars.util.LogUtil.info("LevelSpeedUp , 开服天数不满足");
			return addition;//开服天数不满足
		}
		if(level<LevelSpeedUpManager.START_LEVEL){
			com.stars.util.LogUtil.info("LevelSpeedUp , 自身等级不满足, level:"+level+" , start level:"+LevelSpeedUpManager.START_LEVEL);
			return addition;//自身等级不满足
		}
		if(level>=LevelSpeedUpManager.MEAN_LEVEL){
			com.stars.util.LogUtil.info("LevelSpeedUp , 已大于等于平均等级, level:"+level+" , GAD_STANDARD:"+LevelSpeedUpManager.MEAN_LEVEL);
			return addition;//已大于等于平均等级
		}
		int levelGad = LevelSpeedUpManager.TOP_LEVEL-level;
		if(levelGad<LevelSpeedUpManager.GAD_STANDARD){
			LogUtil.info("LevelSpeedUp , 与最高等级差距不满足, levelGad:"+levelGad+" , GAD_STANDARD:"+LevelSpeedUpManager.GAD_STANDARD);
			return addition;//与最高等级差距不满足
		}
		if(levelGad>LevelSpeedUpManager.MAX_LEVEL_GAD){
			levelGad = LevelSpeedUpManager.MAX_LEVEL_GAD;
		}
		LevelSpeedUpAdditionVo additionVo = LevelSpeedUpManager.gadAdditionMap.get(levelGad);
		if(additionVo!=null){			
			addition = additionVo.getAddition();
		}
		return addition;
	}
	
	/**
	 * 更新加成值到客户端
	 */
	public void updateAddtion(){
		int addition = getLevelSpeedUpAddition();
		ClientRole packet = new ClientRole(ClientRole.UPDATE_LEVEL_SPEED_UP_ADDITION, null);
		packet.setLevelSpeedUpAddtion(addition);
		send(packet);
	}

}
