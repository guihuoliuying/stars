package com.stars.modules.gamecave.tinygame;

import com.stars.modules.gamecave.GameCaveManager;
import com.stars.modules.gamecave.prodata.GameCaveQuestionVo;

import java.util.*;

/**
 * 答题游戏;
 * Created by gaopeidian on 2017/1/13.
 */
public class TinyGameAnswer extends TinyGameBase{
    public TinyGameAnswer(){

    }
    
    @Override
    public int getRandomRoundId(int lastRoundId , int roundIndex){
    	Map<Integer , Map<Integer, GameCaveQuestionVo>> vosMap = GameCaveManager.getGameCaveQuestionVoMap();
        int dificulty = roundIndex;
        if (!vosMap.containsKey(dificulty)) {
			dificulty = GameCaveManager.getMaxDificulty(TinyGameBase.AnswerType);
		}
        Map<Integer, GameCaveQuestionVo> difVoMap = vosMap.get(dificulty);
        if (difVoMap == null) {
			return -1;
		}
        
        List<Integer> library = new ArrayList<Integer>();
        for (GameCaveQuestionVo vo : difVoMap.values()) {
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
		GameCaveQuestionVo questionVo = GameCaveManager.getGameCaveQuestionVo(roundId);
    	if (questionVo == null) {
			return 0;
		}
		
    	return questionVo.getTime();
	}
	
	@Override
	public int getNeedSuccessCount(int roundId){
		return 1;
	}
	
	@Override
	public int getWinScore(int roundId){
		GameCaveQuestionVo questionVo = GameCaveManager.getGameCaveQuestionVo(roundId);
    	if (questionVo == null) {
			return 0;
		}
		
    	return questionVo.getWinScore();
    }
    
	@Override
    public int getLoseScore(int roundId){
		GameCaveQuestionVo questionVo = GameCaveManager.getGameCaveQuestionVo(roundId);
    	if (questionVo == null) {
			return 0;
		}
		
    	return questionVo.getLoseScore();
    }
    
	@Override
    public int getCoef(int roundId){
		GameCaveQuestionVo questionVo = GameCaveManager.getGameCaveQuestionVo(roundId);
    	if (questionVo == null) {
			return 0;
		}
		
    	return questionVo.getCoef();
    }
	
	@Override
    public void finish(String dataStr) {
    	int successCount = Integer.parseInt(dataStr);
    	finish(successCount , "");
    }
}
