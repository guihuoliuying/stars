package com.stars.modules.newserversign;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.newserversign.listener.NewServerSignListener;
import com.stars.modules.newserversign.prodata.NewServerSignVo;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.role.event.RoleLevelUpEvent;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class NewServerSignModuleFactory extends AbstractModuleFactory<NewServerSignModule> {
	public NewServerSignModuleFactory() {
		super(new NewServerSignPacketSet());
	}
	
	@Override
    public NewServerSignModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new NewServerSignModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		//GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		NewServerSignListener listener = new NewServerSignListener(module);
    	eventDispatcher.reg(OperateActivityEvent.class, listener);
    	eventDispatcher.reg(OperateActivityFlowEvent.class, listener);
    	eventDispatcher.reg(RoleLevelUpEvent.class, listener);
    	eventDispatcher.reg(ForeShowChangeEvent.class, listener);   	
    }
	
	@Override
    public void loadProductData() throws Exception {
		loadNewServerSignVo();
		loadFirstTestRewardList();
    }
	
	private void loadNewServerSignVo() throws SQLException {
        String sql = "select * from `newserversign`; ";
        Map<Integer, NewServerSignVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "newserversignid", NewServerSignVo.class, sql);
        NewServerSignManager.setNewServerSignVoMap(map);
    }

	/**
	 * 加载首测发奖名单
	 */
	private void loadFirstTestRewardList() throws SQLException{
		List<String> list = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, "select * from firsttestawardlist");
		Set<String> set = new HashSet<>();
		for(String accountName:list){
			set.add(accountName);
		}
		NewServerSignManager.FIRST_TEST_REWARD_SET = set;
	}
}

