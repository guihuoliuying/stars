package com.stars.modules.masternotice;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gm.GmManager;
import com.stars.modules.masternotice.event.MasterNoticeOnClockEvent;
import com.stars.modules.masternotice.gm.MasterNoticeGmHandler;
import com.stars.modules.masternotice.listener.MasterNoticeListener;
import com.stars.modules.masternotice.listener.MasterNoticeOnClockListener;
import com.stars.modules.masternotice.prodata.MasterNoticeVo;
import com.stars.modules.scene.event.PassMasterNoticeStageEvent;
import com.stars.modules.shop.event.BuyGoodsEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/11/19.
 */
public class MasterNoticeModuleFactory extends AbstractModuleFactory<MasterNoticeModule> {

	public MasterNoticeModuleFactory() {
		super(new MasterNoticePacketSet());
	}
	
	@Override
    public MasterNoticeModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new MasterNoticeModule(id, self, eventDispatcher, moduleMap);
    }
	
	@Override
    public void init() throws Exception {
		GmManager.reg("master", new MasterNoticeGmHandler());
    }
	
	@Override
    public void loadProductData() throws Exception {
       loadMasterNotice();
       initConfig();
       initFlow();
    }
	
	@Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
		MasterNoticeListener listener = new MasterNoticeListener(module);
    	eventDispatcher.reg(PassMasterNoticeStageEvent.class, listener);
    	eventDispatcher.reg(BuyGoodsEvent.class, listener);
    	eventDispatcher.reg(MasterNoticeOnClockEvent.class, new MasterNoticeOnClockListener(module));
    }  
	
	private void loadMasterNotice() throws SQLException {
        String sql = "select * from `masternotice`; ";
        Map<Integer, MasterNoticeVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "noticeid", MasterNoticeVo.class, sql);
        MasterNoticeManager.setMasterNoticeVoMap(map);
    }
	
    private void initConfig() {
		Map<Integer, Integer> refreshCost = new HashMap<>();
    	String refreshCostStr = DataManager.getCommConfig("masternotice_rmbflash");
    	if (refreshCostStr != null && (!refreshCostStr.equals("")) && (!refreshCostStr.equals("0"))) {
    		String[] sts = refreshCostStr.split("\\|");
       		String[] ts;
       		for(String tmp : sts){
       			ts = tmp.split("\\+");
       			if (ts.length >= 2) {
       				refreshCost.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
       			}
       		}
   		}

		List<Integer> firstNoticeIdList = new ArrayList<>();
    	String firstNoticesStr = DataManager.getCommConfig("masternotice_firstnotice");
    	if (firstNoticesStr != null && (!firstNoticesStr.equals("")) && (!firstNoticesStr.equals("0"))) {
    		String[] sts = firstNoticesStr.split("\\+");
       		for(String tmp : sts){
       			firstNoticeIdList.add(Integer.parseInt(tmp));
       		}
   		}

		MasterNoticeManager.refreshCoolDownTime = Integer.valueOf(DataManager.getCommConfig("masternotice_cooldown"));
		MasterNoticeManager.freeRefreshCount = Integer.valueOf(DataManager.getCommConfig("masternotice_freeflashcount"));
		MasterNoticeManager.costRefreshCount = Integer.valueOf(DataManager.getCommConfig("masternotice_payflashcount"));
		MasterNoticeManager.refreshCost = refreshCost;
		MasterNoticeManager.firstNoticeIdList = firstNoticeIdList;
    }

    private void initFlow() throws Exception{
    	Map<Integer, String> flowMap = new HashMap<>();
    	flowMap.put(MasterNoticeConstant.MASTER_NOTICE_FLOW_STEP, "0 0 * * * ?");//皇榜悬赏的整点触发
        MasterNoticeFlow flow = new MasterNoticeFlow();
        flow.init(SchedulerHelper.getScheduler(), flowMap);
    }
    
}

