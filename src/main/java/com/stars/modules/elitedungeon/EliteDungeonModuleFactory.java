package com.stars.modules.elitedungeon;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.handler.EliteDungeonTeamHandler;
import com.stars.modules.data.DataManager;
import com.stars.modules.elitedungeon.event.*;
import com.stars.modules.elitedungeon.gm.EliteDungeonGmHandler;
import com.stars.modules.elitedungeon.listener.EliteDungeonListener;
import com.stars.modules.elitedungeon.prodata.EliteDungeonRobotVo;
import com.stars.modules.elitedungeon.prodata.EliteDungeonVo;
import com.stars.modules.elitedungeon.summary.EliteDungeonSummaryComponentImpl;
import com.stars.modules.elitedungeon.userdata.ElitePlayerImagePo;
import com.stars.modules.gm.GmManager;
import com.stars.modules.scene.event.PassStageEvent;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.util._HashMap;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2017/3/8.
 */
public class EliteDungeonModuleFactory extends AbstractModuleFactory<EliteDungeonModule> {
    public EliteDungeonModuleFactory() {
        super(new EliteDungeonPacketSet());
    }

    @Override
    public void init() throws Exception {
    	BaseTeamManager.registerTeamHandler(BaseTeamManager.TEAM_TYPE_ELITEDUNGEON, EliteDungeonTeamHandler.class);
    	
    	Summary.regComponentClass("elitedungeon", EliteDungeonSummaryComponentImpl.class);
    	
    	loadPlayerImage();
    	
    	GmManager.reg("elitedungeon", new EliteDungeonGmHandler());
    }
    
    /**
     * 加载玩家镜像数据
     * @throws SQLException 
     */
    public void loadPlayerImage() throws SQLException{
    	String sql = "select * from eliteplayerimage order by stageid desc";
    	List<ElitePlayerImagePo> list = DBUtil.queryList(DBUtil.DB_USER, ElitePlayerImagePo.class, sql);
    	Map<Integer, List<ElitePlayerImagePo>> stagePlayerMap = new HashMap<Integer, List<ElitePlayerImagePo>>();
    	Map<String, ElitePlayerImagePo> playerImageMap = new HashMap<>();
    	List<ElitePlayerImagePo> playerList = null;
    	List<ElitePlayerImagePo> addList = new ArrayList<>();
    	for(ElitePlayerImagePo po : list){
    		playerList = stagePlayerMap.get(po.getStageid());
    		if(playerList==null){
    			playerList = new ArrayList<>();
    			stagePlayerMap.put(po.getStageid(), playerList);
    		}
    		playerList.add(po);
    		addList.add(po);
    		playerImageMap.put(po.getRoleid()+"_"+po.getStageid(), po);
    	}
    	EliteDungeonManager.playerImageList = addList;
    	EliteDungeonManager.stagePlayerMap = stagePlayerMap;
    }

    @Override
    public EliteDungeonModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new EliteDungeonModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, EliteDungeonVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "eliteid", EliteDungeonVo.class,
                "select * from elitedungeon");
        int maxId = -1;
        int minId = -1;
        for(Integer eliteid : map.keySet()){
        	if(maxId==-1){
        		maxId = eliteid;
        	}else if(maxId<eliteid){
        		maxId = eliteid;
        	}
        	if(minId==-1){
        		minId = eliteid;
        	}else if(minId>eliteid){
        		minId = eliteid;
        	}
        }
        EliteDungeonManager.setEliteDungeonVoMap(map);
        EliteDungeonManager.Max_Robot_StageId = maxId;
        EliteDungeonManager.Min_Robot_StageId = minId;
        
        initConfig();
        
        loadRobotInfo();
        
        loadRobotRandName();
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    	EliteDungeonListener listener = new EliteDungeonListener((EliteDungeonModule) module);
    	eventDispatcher.reg(EliteDungeonEnterFightEvent.class, listener);
    	eventDispatcher.reg(BackToCityFromEliteDungeonEvent.class, listener);
    	eventDispatcher.reg(EliteDungeonFinishEvent.class, listener);
    	eventDispatcher.reg(EliteDungeonDropEvent.class, listener);
    	eventDispatcher.reg(PassStageEvent.class, listener);
    	eventDispatcher.reg(EliteDungeonAddImageDataEvent.class, listener);
    }
    
    private void initConfig(){
        String str = DataManager.getCommConfig("elitedungeon_membernum");
        String ss[] = str.split("\\+");
        byte minTeamCount = 0;
        byte maxTeamCount = 0;
         if (ss.length >= 2) {
        	 minTeamCount = Byte.parseByte(ss[0]);
             maxTeamCount = Byte.parseByte(ss[1]);
		 }
         
         int delayTime = Integer.parseInt(DataManager.getCommConfig("playerteam_rewardui_delaytime"));
                  
         String rewardTimesStr = DataManager.getCommConfig("elitedungeon_rewardtimes");
        Map<Integer, Integer> rewardTimesMap = new LinkedHashMap<>();
         try {
			rewardTimesMap = StringUtil.toLinkedHashMap(rewardTimesStr, Integer.class, Integer.class, '+', ',');
		} catch (Exception e) {
			com.stars.util.LogUtil.info("EliteDungeonModuleFactory.initConfig init rewardTimesMap exception" , e);
		}
         
         String helpTimesStr = DataManager.getCommConfig("elitedungeon_helptimes");
        Map<Integer, Integer> helpTimesMap = new LinkedHashMap<>();
         try {
			helpTimesMap = StringUtil.toLinkedHashMap(helpTimesStr, Integer.class, Integer.class, '+', ',');
		} catch (Exception e) {
			LogUtil.info("EliteDungeonModuleFactory.initConfig init helpTimesMap exception" , e);
		}
         
        List<int[]> timerRandomList = new ArrayList<>();
        String timerRandomStr = DataManager.getCommConfig("elitedungeon_match_timerandom");
        String[] timerRanges = timerRandomStr.split(",");
        for(String timerRange : timerRanges){
        	String[] arr = timerRange.split("[+]");
        	int[] range = new int[]{Integer.parseInt(arr[0]), Integer.parseInt(arr[1])};
        	timerRandomList.add(range);
        }
        EliteDungeonManager.timerRandomList = timerRandomList;
        EliteDungeonManager.MATCH_TIME = DataManager.getCommConfig("elitedungeon_searchtime", 30);
        

        EliteDungeonManager.minTeamCount = minTeamCount;
        EliteDungeonManager.maxTeamCount = maxTeamCount;
        EliteDungeonManager.delayTime = delayTime;
        EliteDungeonManager.rewardTimesMap = rewardTimesMap;
        EliteDungeonManager.helpTimesMap = helpTimesMap;
    }
    
    /**
     * 加载精英副本机器人数据
     */
    public void loadRobotInfo() throws SQLException{
    	String sql = "select * from eliterobot order by robotlevel desc,robotid desc";
    	List<EliteDungeonRobotVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, EliteDungeonRobotVo.class, sql);
    	EliteDungeonManager.robotList = list;
    }
    
    public void loadRobotRandName() throws SQLException{
    	String sql = "select * from robotrandname";
    	List<com.stars.util._HashMap> ls = DBUtil.queryList(DBUtil.DB_PRODUCT, com.stars.util._HashMap.class, sql);
    	List<String> firstName = new ArrayList<String>();
    	List<String> secondName = new ArrayList<String>();
    	List<String> thirdName = new ArrayList<String>();
    	for (_HashMap map:ls) {
			firstName.add(map.getString("firstname"));
    		secondName.add(map.getString("secondname"));
    		thirdName.add(map.getString("thirdname"));
		}
    	EliteDungeonManager.firstName = firstName;
    	EliteDungeonManager.secondName = secondName;
    	EliteDungeonManager.thirdName = thirdName;
    }
}
