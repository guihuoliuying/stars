package com.stars.modules.gamecave;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.gamecave.gm.GameCaveGmHandler;
import com.stars.modules.gamecave.prodata.GameCaveQuestionVo;
import com.stars.modules.gamecave.prodata.GameCaveShootOldVo;
import com.stars.modules.gamecave.prodata.GameCaveVo;
import com.stars.modules.gm.GmManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 洞府(小游戏场景)的模块工厂;
 * Created by gaopeidian on 2017/1/13.
 */
public class GameCaveModuleFactory extends AbstractModuleFactory<GameCaveModule> {

    public GameCaveModuleFactory() {
        super(new GameCavePacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
    	//加载通用配置
    	initCommondConfig();
        //加载洞府的产品数据;
        initProductGameCave();
        //加载答题产品数据;
        initProductAnswer();
        //加载射箭产品数据;
        initProductAcher();
    }

    private void initCommondConfig() {
    	GameCaveManager.firstGameId = Integer.valueOf(DataManager.getCommConfig("gamecave_firstgameid")); 	
    	GameCaveManager.gameCaveMailId = Integer.valueOf(DataManager.getCommConfig("gamecave_rewardemail"));
    	GameCaveManager.defaultCityId = Integer.valueOf(DataManager.getCommConfig("gamecave_originsafeinfo"));
    	GameCaveManager.gameTimes = Integer.valueOf(DataManager.getCommConfig("gamecave_times"));
    		
    	String finalRewardStr = DataManager.getCommConfig("gamecave_finalreward");
    	List<Integer> tempFinalRewardShowItemIds = new ArrayList<Integer>();
    	Map<Integer, Integer> tempFinalRewardMap = new HashMap<Integer, Integer>();
        if (finalRewardStr != null && !finalRewardStr.equals("") && !finalRewardStr.equals("0")) {
        	String[] sts = finalRewardStr.split("\\|");
        	if (sts.length >= 2) {
				String s1 = sts[0];
				String[] s1Array = s1.split("\\+");
				for (String tmp : s1Array) {
					tempFinalRewardShowItemIds.add(Integer.parseInt(tmp));
				}
				
				String s2 = sts[1];
				String[] s2Array = s2.split("\\,");
	       		for(String tmp : s2Array){
	       			String[] ts = tmp.split("\\+");
	       			if (ts.length >= 2) {
	       				tempFinalRewardMap.put(Integer.parseInt(ts[0]), Integer.parseInt(ts[1]));
	       			}
	       		}
			}       		
   		}
        
        GameCaveManager.finalRewardShowItemIds = tempFinalRewardShowItemIds;
        GameCaveManager.finalRewardMap = tempFinalRewardMap;
    }
    
    private void initProductGameCave() throws SQLException {
        String sql = "select * from `gamecave`; ";
        Map<Integer, GameCaveVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "gameid", GameCaveVo.class, sql);
		checkGameCaveVoMap(map);		
        GameCaveManager.setGameCaveVoMap(map);
    }

    private void initProductAnswer() throws SQLException {
    	String sql = "SELECT * FROM gamecavequestion;";
        Map<Integer, GameCaveQuestionVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "questionid", GameCaveQuestionVo.class, sql);
        GameCaveManager.setGameCaveQuestionVoMap(map);
    }
    
    private void initProductAcher() throws SQLException {
    	String sql = "SELECT * FROM gamecaveshootold;";
        Map<Integer, GameCaveShootOldVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "oldshootid", GameCaveShootOldVo.class, sql);
        GameCaveManager.setGameCaveShootOldMap(map);
    }
    
    @Override
    public void init() {
    	GmManager.reg("game", new GameCaveGmHandler());
    }

    @Override
    public GameCaveModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new GameCaveModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {

    }

    private void checkGameCaveVoMap(Map<Integer, GameCaveVo> map){
    	if (map != null) {
			for (GameCaveVo vo : map.values()) {
				int gameId = vo.getGameId();
				int npcId = vo.getNpcId();
				for (GameCaveVo tempVo : map.values()) {
					if (npcId == tempVo.getNpcId() && gameId != tempVo.getGameId()) {
						throw new IllegalArgumentException("gamecave配置有误，npcId重复使用,npcId=" + npcId);
					}
				}
			}
		}
    }

}

