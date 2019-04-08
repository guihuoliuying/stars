package com.stars.modules.gamecave;

import com.stars.modules.gamecave.prodata.GameCaveQuestionVo;
import com.stars.modules.gamecave.prodata.GameCaveShootOldVo;
import com.stars.modules.gamecave.prodata.GameCaveVo;
import com.stars.modules.gamecave.tinygame.TinyGameBase;

import java.util.*;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public class GameCaveManager {
	//通用配置
	public static int firstGameId = 0;//初始游戏id
	public static int gameCaveMailId = 0;//游园发邮件奖励的邮件id
	public static int defaultCityId = 101;//从游园退出后默认回到的scenseId
	public static int gameTimes = 0;//游戏次数
	public static List<Integer> finalRewardShowItemIds = new ArrayList<Integer>();//最终奖励showItem
	public static Map<Integer, Integer> finalRewardMap = new HashMap<Integer, Integer>();//最终奖励<牌型编号,奖励dropid> 
	
	//约定配置
	public static final int gameCaveSafeId = 301;
	
	//<gameid,vo>
    private static Map<Integer, GameCaveVo> gameCaveVoMap = null;
    private static Map<Integer, GameCaveQuestionVo> questionVoMap = null;
    private static Map<Integer, GameCaveShootOldVo> shootVoMap = null;
    //<difficulty , <gameid,vo>>
    private static Map<Integer , Map<Integer, GameCaveQuestionVo>> gameCaveQuestionVoMap = null;
    //<difficulty , <gameid,vo>>
    private static Map<Integer , Map<Integer, GameCaveShootOldVo>> gameCaveShootOldMap = null;
    
    
    //内存数据
    private static Map<Byte, Integer> maxDifficultyMap = new HashMap<Byte, Integer>();
    
    public static void setGameCaveVoMap(Map<Integer, GameCaveVo> map){
        gameCaveVoMap = map;
    }

    public static void setGameCaveQuestionVoMap(Map<Integer, GameCaveQuestionVo> map){
    	questionVoMap = map;
    	
    	Map<Integer , Map<Integer, GameCaveQuestionVo>> tempMap = new HashMap<Integer , Map<Integer, GameCaveQuestionVo>>();
    	int maxDifficulty = 0;
    	for (GameCaveQuestionVo vo : map.values()) {
			int difficulty = vo.getDifficulty();
			maxDifficulty = difficulty > maxDifficulty ? difficulty : maxDifficulty;
			Map<Integer, GameCaveQuestionVo> dMap = tempMap.get(difficulty);
			if (dMap == null) {
				dMap = new HashMap<Integer, GameCaveQuestionVo>();
				tempMap.put(difficulty, dMap);
			}
			dMap.put(vo.getQuestionId(), vo);
		}
    	
    	gameCaveQuestionVoMap = tempMap;
    	
    	maxDifficultyMap.put(TinyGameBase.AnswerType, maxDifficulty);
    }
    
    public static void setGameCaveShootOldMap(Map<Integer, GameCaveShootOldVo> map){
    	shootVoMap = map;
    	
    	Map<Integer , Map<Integer, GameCaveShootOldVo>> tempMap = new HashMap<Integer , Map<Integer, GameCaveShootOldVo>>();
    	int maxDifficulty = 0;
    	for (GameCaveShootOldVo vo : map.values()) {
			int difficulty = vo.getDifficulty();
			maxDifficulty = difficulty > maxDifficulty ? difficulty : maxDifficulty;
			Map<Integer, GameCaveShootOldVo> dMap = tempMap.get(difficulty);
			if (dMap == null) {
				dMap = new HashMap<Integer, GameCaveShootOldVo>();
				tempMap.put(difficulty, dMap);
			}
			dMap.put(vo.getOldShootId(), vo);
		}
    	
    	gameCaveShootOldMap = tempMap;
    	
    	maxDifficultyMap.put(TinyGameBase.ArcherType, maxDifficulty);  
    }
    
    public static Map<Integer, GameCaveVo> getGameCaveVoMap(){
    	return gameCaveVoMap;
    }
    
    public static Map<Integer, GameCaveQuestionVo> getQuestionVoMap(){
    	return questionVoMap;
    }
    
    public static Map<Integer, GameCaveShootOldVo> getShootVoMap(){
    	return shootVoMap;
    }
    
    public static GameCaveVo getGameCaveVo(int gameId){
    	return gameCaveVoMap.get(gameId);
    }
    
    public static GameCaveQuestionVo getGameCaveQuestionVo(int questionId){
    	return questionVoMap.get(questionId);
    }
    
    public static GameCaveShootOldVo getGameCaveShootOldVo(int shootId){
    	return shootVoMap.get(shootId);
    }
    
    public static Map<Integer , Map<Integer, GameCaveQuestionVo>> getGameCaveQuestionVoMap(){
    	return gameCaveQuestionVoMap;
    }
    
    public static Map<Integer , Map<Integer, GameCaveShootOldVo>> getGameCaveShootOldMap(){
    	return gameCaveShootOldMap;
    }
    
    public static int getMaxDificulty(byte gameType){
    	if (maxDifficultyMap.containsKey(gameType)) {
			return maxDifficultyMap.get(gameType);
		}
    	
    	return 0;
    }
    
    public static List<Integer> getAllGameIds(){
    	List<Integer> ret = new ArrayList<Integer>();
    	for (GameCaveVo vo : gameCaveVoMap.values()) {
			ret.add(vo.getGameId());
		}
    	return ret;
    }
    
    public static List<Integer> getRandomInts(List<Integer> randomLibarary, int randomCount, Set<Integer> excepts){
    	List<Integer> ret = new ArrayList<Integer>();
    	
    	//清除掉excepts中的内容    	
		randomLibarary.removeAll(excepts);
    	Random random = new Random();
    	for (int i = 0; i < randomCount; i++) {
			int size = randomLibarary.size();
			if (randomLibarary.size() <= 0) {
				return ret;
			}
			
			int randomIndex = random.nextInt(size);
			int randomInt = randomLibarary.get(randomIndex);
			ret.add(randomInt);
			
			//清除刚随机出来的int
			Set<Integer> remove = new HashSet<Integer>();
			remove.add(randomInt);
			randomLibarary.removeAll(remove);
		}
    	
    	return ret;
    }
    
    public static void main(String args[]){
    	Set<Integer> excepts = new HashSet<Integer>();
    	excepts.add(4);
    	excepts.add(5);
    	excepts.add(6);
    	
    	List<Integer> all = new ArrayList<Integer>();
    	all.add(1);
    	all.add(2);
    	all.add(3);
    	all.add(4);
    	all.add(5);
    	all.add(6);
    	all.add(7);
    	all.add(8);
    	all.add(9);
    	all.add(6);
    	all.add(5);
    	
    	int randomCount = 4;
    	List<Integer> ret = getRandomInts(all, randomCount, excepts);

    }
}
