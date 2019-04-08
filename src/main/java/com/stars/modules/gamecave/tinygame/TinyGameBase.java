package com.stars.modules.gamecave.tinygame;

import com.stars.modules.daily.DailyManager;
import com.stars.modules.gamecave.GameCaveConstant;
import com.stars.modules.gamecave.GameCaveManager;
import com.stars.modules.gamecave.GameCaveModule;
import com.stars.modules.gamecave.packet.ClientFinishGame;
import com.stars.modules.gamecave.prodata.GameCaveVo;
import com.stars.modules.gamecave.userdata.TinyGameData;
import com.stars.modules.gamecave.userdata.TinyGameRound;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 小游戏基类;
 * Created by gaopeidian on 2017/1/14.
 */
public class TinyGameBase implements ITinyGame {
    public static final byte AnswerType = 1;
    public static final byte ArcherType = 9;

    private int gameId;
    private byte type;  
    private int curRoundId;
    //游园模块
    protected GameCaveModule module;
    
    
    //计时用
    //缓存的花费的时间,真正花费的时间需要用那个endTimestamp - startTimestamp计算出来;
    private long tempSpendTime = 0;
    //开始的时间戳,依赖stoppedSpendTime的推移计算;
    private long startTimestamp = 0;
    //结束的时间戳;
    private long endTimestamp = 0;
    
    
    public TinyGameBase() {
        TinyGameEnum tinyGameEnum = TinyGameEnum.getByClass(this.getClass());
        type = tinyGameEnum.getTinygameType();
    }

    public int getGameId(){
    	return gameId;
    }
    
    public void setGameId(int gameId){
    	this.gameId = gameId;
    }
    
    public byte getType() {
        return type;
    }

    public int getCurRoundId(){
    	return curRoundId;
    }
    
    public void setCurRoundId(int curRoundId){
    	this.curRoundId = curRoundId;
    }
    
    public void setModule(GameCaveModule module) {
    	this.module = module;
    }
    
    @Override
    public void start() {
    	startTimestamp = System.currentTimeMillis() - tempSpendTime;
        tempSpendTime = 0;
    }
    
    @Override
    public void exit() {

    }

    @Override
    public void finish(String dataStr) {
    	
    }

    @Override
    public void onTimeUpdate() {
    	
    }
    
    public void finish(int successCount , String dataStr){
    	int curRoundId = getCurRoundId();
    	
    	int playTime = calculatePlayTime();
    	
    	int gameTime = getGameTime(curRoundId);
    	int needSuccessCount = getNeedSuccessCount(curRoundId);
    	//判断结果
    	byte result = GameCaveConstant.GAME_RESULT_FAIL;
    	if (successCount >= needSuccessCount) {
			//判断时间是否是合法的
    		int allowPlayTime = gameTime + GameCaveConstant.gameAddTime;
    		if (playTime <= allowPlayTime) {
    			result = GameCaveConstant.GAME_RESULT_SUCCESS;
			}
		}
    	
    	//计算成功数
    	if (result == GameCaveConstant.GAME_RESULT_FAIL) {
			successCount = successCount > needSuccessCount - 1 ? needSuccessCount - 1 : successCount;
		}else{
			successCount = successCount > needSuccessCount ? needSuccessCount : successCount;
		}
    	
    	//计算剩余时间
    	int leftTime = gameTime - playTime > 0 ? gameTime - playTime : 0;
    	
    	//添加roundData
    	List<TinyGameRound> tinyGameRounds = module.getGameRounds(getGameId());
    	if (tinyGameRounds == null) {
			tinyGameRounds = new ArrayList<TinyGameRound>();
			module.getTinyGameRounds().put(getGameId(), tinyGameRounds);
		}
    	int roundIndex = tinyGameRounds.size() + 1;
    	byte isWin = result == GameCaveConstant.GAME_RESULT_SUCCESS ? (byte)1 : (byte)0;
    	TinyGameRound round = new TinyGameRound(module.id(), getGameId(), roundIndex, leftTime,     			
    			isWin, successCount, "", curRoundId);
    	module.context().insert(round);
    	
    	tinyGameRounds.add(round);
    	
    	//判断tinyGame的进度状态等
    	GameCaveVo gameCaveVo = GameCaveManager.getGameCaveVo(getGameId());
    	if (gameCaveVo == null) {
    		com.stars.util.LogUtil.info("TinyGameArcher.finish get no gameCaveVo,gameId=" + getGameId());
			return;
		}
    	
    	TinyGameData tinyGameData = module.getTinyGameDataById(getGameId());
    	if (tinyGameData == null) {
    		LogUtil.info("TinyGameArcher.finish get no tinyGameData,gameId=" + getGameId());
			return;
		}
    	
    	//更新星数
    	int totalSuccessCount = getTotalSuccessCount(getGameId());
    	int star = gameCaveVo.getStarBySuccessCount(totalSuccessCount);
    	tinyGameData.setStar(star);
    	
    	//更新状态
    	int totalRoundCount = gameCaveVo.getGameTimes();
    	int finishRoundCount = tinyGameRounds.size();
    	if (finishRoundCount >= totalRoundCount) {
    		tinyGameData.setIsFinish((byte)1);
			//添加游园次数
			module.addDailyCount(DailyManager.DAILYID_GAME_CAVE, 1);
		}
    	
    	module.context().update(tinyGameData);
    	
    	//发送消息到客户端更新
    	module.sendTinyGameData(getGameId());
    	module.sendGameCaveData();
    	
    	ClientFinishGame clientFinishGame = new ClientFinishGame();
    	clientFinishGame.setResult(result);    	
    	module.send(clientFinishGame);
    }
    
    public int getRandomRoundId(int lastRoundId , int roundIndex){
    	return -1;
    }
    
    public String getDataStr(){
    	int count = 0;
    	List<TinyGameRound> tinyGameRounds = module.getGameRounds(getGameId());
    	if (tinyGameRounds != null) {
    		for(TinyGameRound round : tinyGameRounds) {
        		count += round.getSuccessCount();
    		}
		}
    	
    	StringBuffer buffer = new StringBuffer("");
    	buffer.append(count);
    	return buffer.toString();
    }
    
	public int getGameTime(int roundId){
		return 0;
	}
	
	public int calculateScore(){
		List<TinyGameRound> tinyGameRounds = module.getGameRounds(getGameId());
		
		int score = 0;
		for (TinyGameRound tempRound : tinyGameRounds) {
			int roundId = tempRound.getRoundId();
			int s1 = tempRound.getIsWin() == (byte)1 ? getWinScore(roundId) : getLoseScore(roundId);
			int s2 = tempRound.getLeftTime() * getCoef(roundId);
			score = score + s1 + s2;
		}
		
		return score;
	}
	
	public int getNeedSuccessCount(int roundId){
		return 0;
	}
	
	public int getTotalSuccessCount(int gameId){
		int totalCount = 0;
		List<TinyGameRound> rounds = module.getGameRounds(gameId);
		if (rounds != null) {
			for (TinyGameRound round : rounds) {
				totalCount += round.getSuccessCount();
			}
		}
		return totalCount;
	}
    
    public int getWinScore(int roundId){
    	return 0;
    }
    
    public int getLoseScore(int roundId){
    	return 0;
    }
    
    public int getCoef(int roundId){
    	return 0;
    }
    
    protected int calculatePlayTime(){
        long playTimeMS = (System.currentTimeMillis() - startTimestamp) / 1000;
        int playTimeS = (int) playTimeMS;
        return playTimeS;
    }
}
