package com.stars.modules.gamecave.tinygame;

import com.stars.modules.gamecave.GameCaveManager;
import com.stars.modules.gamecave.prodata.GameCaveShootOldVo;

import java.util.*;

/**
 * 答题游戏;
 * Created by gaopeidian on 2017/1/13.
 */
public class TinyGameArcher extends TinyGameBase{
	public TinyGameArcher(){

    }
	
	@Override
    public int getRandomRoundId(int lastRoundId , int roundIndex){
		Map<Integer , Map<Integer, GameCaveShootOldVo>> vosMap = GameCaveManager.getGameCaveShootOldMap();
        int dificulty = roundIndex;
        if (!vosMap.containsKey(dificulty)) {
			dificulty = GameCaveManager.getMaxDificulty(TinyGameBase.ArcherType);
		}
        Map<Integer, GameCaveShootOldVo> difVoMap = vosMap.get(dificulty);
        if (difVoMap == null) {
			return -1;
		}
        
        List<Integer> library = new ArrayList<Integer>();
        for (GameCaveShootOldVo vo : difVoMap.values()) {
			library.add(vo.getId());
		}
        Set<Integer> excepts = new HashSet<Integer>();
        excepts.add(lastRoundId);
        
        List<Integer> randomList = GameCaveManager.getRandomInts(library, 1, excepts);
        if (randomList.size() > 0) {
			return randomList.get(0);
		}
        
        return -1;
    }
	
	@Override
	public int getGameTime(int roundId){
		GameCaveShootOldVo shootVo = GameCaveManager.getGameCaveShootOldVo(roundId);
    	if (shootVo == null) {
			return 0;
		}
		
    	return shootVo.getTime();
	}
	
	@Override
	public int getNeedSuccessCount(int roundId){
		GameCaveShootOldVo shootVo = GameCaveManager.getGameCaveShootOldVo(roundId);
    	if (shootVo == null) {
			return 0;
		}
		
    	return shootVo.getBagPositionList().size();
	}
	
	@Override
	public int getWinScore(int roundId){
		GameCaveShootOldVo shootVo = GameCaveManager.getGameCaveShootOldVo(roundId);
    	if (shootVo == null) {
			return 0;
		}
		
    	return shootVo.getWinScore();
    }
    
	@Override
    public int getLoseScore(int roundId){
		GameCaveShootOldVo shootVo = GameCaveManager.getGameCaveShootOldVo(roundId);
    	if (shootVo == null) {
			return 0;
		}
		
    	return shootVo.getLoseScore();
    }
    
	@Override
    public int getCoef(int roundId){
		GameCaveShootOldVo shootVo = GameCaveManager.getGameCaveShootOldVo(roundId);
    	if (shootVo == null) {
			return 0;
		}
		
    	return shootVo.getCoef();
    }
	
	@Override
    public void finish(String dataStr) {
    	int successCount = Integer.parseInt(dataStr);
    	finish(successCount , "");
    }
}



















