package com.stars.modules.sevendaygoal;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.buddy.event.BuddyUpgradeEvent;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.gem.event.GemFightScoreChangeEvent;
import com.stars.modules.guest.event.GuestAttributeChangeEvent;
import com.stars.modules.newequipment.event.EquipStarChangeEvent;
import com.stars.modules.newequipment.event.EquipStrengthChangeEvent;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.ride.event.RideLevelUpEvent;
import com.stars.modules.role.event.FightScoreChangeEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.sevendaygoal.event.RewardCountChangeEvent;
import com.stars.modules.sevendaygoal.listener.SevenDayGoalListener;
import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;
import com.stars.modules.skill.event.SkillBatchLvUpEvent;
import com.stars.modules.skill.event.SkillLevelUpEvent;
import com.stars.modules.skill.event.SkillPositionChangeEvent;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class SevenDayGoalModuleFactory extends AbstractModuleFactory<SevenDayGoalModule> {
	public SevenDayGoalModuleFactory() {
		super(new SevenDayGoalPacketSet());
	}
	
	@Override
    public SevenDayGoalModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new SevenDayGoalModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		//GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		SevenDayGoalListener listener = new SevenDayGoalListener(module);
		
		eventDispatcher.reg(OperateActivityEvent.class, listener);
		
    	eventDispatcher.reg(RideLevelUpEvent.class, listener);
    	eventDispatcher.reg(EquipStrengthChangeEvent.class, listener);
    	eventDispatcher.reg(EquipStarChangeEvent.class, listener);
    	eventDispatcher.reg(BuddyUpgradeEvent.class, listener);
    	eventDispatcher.reg(GemFightScoreChangeEvent.class, listener);
    	eventDispatcher.reg(SkillLevelUpEvent.class, listener);
    	eventDispatcher.reg(SkillBatchLvUpEvent.class, listener);
    	eventDispatcher.reg(SkillPositionChangeEvent.class, listener);
    	eventDispatcher.reg(FightScoreChangeEvent.class, listener);	
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	
    	eventDispatcher.reg(RewardCountChangeEvent.class, listener);
    	eventDispatcher.reg(OperateActivityFlowEvent.class, listener);
    	
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	eventDispatcher.reg(ForeShowChangeEvent.class, listener);
		eventDispatcher.reg(GuestAttributeChangeEvent.class, listener);
    }
	
	@Override
    public void loadProductData() throws Exception {
		loadSevenDayGoalVo();
    }
	
	private void loadSevenDayGoalVo() throws SQLException {
        String sql = "select * from `sevendaygoal`; ";
        Map<Integer, SevenDayGoalVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "goalid", SevenDayGoalVo.class, sql);
        SevenDayGoalManager.setSevenDayGoalVoMap(map);
    }
}

