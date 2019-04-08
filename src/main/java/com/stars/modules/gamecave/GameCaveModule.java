package com.stars.modules.gamecave;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.drop.DropModule;
import com.stars.modules.gamecave.card.CardManager;
import com.stars.modules.gamecave.event.FinishAllTinyGameEvent;
import com.stars.modules.gamecave.packet.*;
import com.stars.modules.gamecave.prodata.GameCaveVo;
import com.stars.modules.gamecave.tinygame.TinyGameBase;
import com.stars.modules.gamecave.tinygame.TinyGameEnum;
import com.stars.modules.gamecave.userdata.RoleGameCave;
import com.stars.modules.gamecave.userdata.TinyGameData;
import com.stars.modules.gamecave.userdata.TinyGameRound;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.serverLog.ThemeType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

/**
 * Created by gaopeidian on 2017/1/13.
 */
public class GameCaveModule extends AbstractModule {
    private RoleGameCave roleGameCave = null;
    //<index , gameData>
    private List<TinyGameData> tinyGameDatas = null;
    //<gameId , <gameIndex , gameRound>>
    private Map<Integer, List<TinyGameRound>> tinyGameRounds = null;
    
    private TinyGameBase curTinyGame = null;

    public GameCaveModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.GameCave, id, self, eventDispatcher, moduleMap);
    }
 
    @Override
    public void onCreation(String name_, String account_) throws Throwable {
    	initUserData();
    }

    @Override
    public void onDataReq() throws Exception {
    	initUserData();
    	
    	updateRankScore();
    }

    @Override
    public void onSyncData() {

    }
    
    @Override
    public void onInit(boolean isCreation) {
    	updateRankScore();
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
    	sendRewardToEmailBeforeReset();
    	resetUserData();
        curTinyGame = null;
        
        //通知客户端游园重置
        ClientGameCaveReset clientGameCaveReset = new ClientGameCaveReset();
        send(clientGameCaveReset);
        //发送重置后游园相关数据
        sendGameCaveData();
        TinyGameData curData = getCurTinyGameData();
        if (curData != null) {
			sendTinyGameData(curData.getGameId());
		}
    }
    
    @Override
    public void onOffline() throws Throwable {
        if (curTinyGame != null) {
			curTinyGame.exit();
			curTinyGame = null;
		}
    }
    
    @Override
    public void onTimingExecute(){
    	if (curTinyGame != null) {
			curTinyGame.onTimeUpdate();
		}
    }

    private void initUserData() throws SQLException{
    	//roleGameCave
    	String sql1 = "select * from `rolegamecave` where `roleid`=" + id();
        roleGameCave = DBUtil.queryBean(DBUtil.DB_USER , RoleGameCave.class , sql1);
        if (roleGameCave == null) {
        	roleGameCave = new RoleGameCave(id(), 0, "", "", (byte)0);
        	context().insert(roleGameCave);
		}
        
        //tinyGameData
        String sql2 = "select * from `tinygamedata` where `roleid`=" + id();
        tinyGameDatas = DBUtil.queryList(DBUtil.DB_USER, TinyGameData.class, sql2);
        if (tinyGameDatas == null || tinyGameDatas.size() <= 0) {
        	tinyGameDatas = new ArrayList<TinyGameData>();
        	int firstGameId = GameCaveManager.firstGameId;
        	TinyGameData initGameData = new TinyGameData(id(), 1, firstGameId, (byte)0, "", "", "", 0, 0, (byte)0, (byte)0, -1);
        	context().insert(initGameData);
        	tinyGameDatas.add(initGameData);
		}
        
        //tinyGameRounds
        String sql3 = "select * from `tinygameround` where `roleid`=" + id();
        tinyGameRounds = new HashMap<Integer, List<TinyGameRound>>();
        List<TinyGameRound> gameRoundList = DBUtil.queryList(DBUtil.DB_USER, TinyGameRound.class, sql3);
        for (TinyGameRound round : gameRoundList) {
        	int gameId = round.getGameId();
        	List<TinyGameRound>unitGameRounds = tinyGameRounds.get(gameId);
			if (unitGameRounds == null) {
				unitGameRounds = new ArrayList<TinyGameRound>();
				tinyGameRounds.put(gameId, unitGameRounds);
			}
			unitGameRounds.add(round);
		}
    }
    
    private void resetUserData(){
    	//roleGameCave
    	roleGameCave.setScore(0);
    	roleGameCave.setChoseCardIds(new ArrayList<Integer>());
    	roleGameCave.setRewardMap(new HashMap<Integer, Integer>());
    	roleGameCave.setIsGetReward((byte)0);
        context().update(roleGameCave);
		     
        //tinyGameData
        for (TinyGameData data : tinyGameDatas) {
			context().delete(data);
		}
        tinyGameDatas.clear();
        int firstGameId = GameCaveManager.firstGameId;
    	TinyGameData initGameData = new TinyGameData(id(), 1, firstGameId, (byte)0, "", "", "", 0, 0, (byte)0, (byte)0, -1);
    	context().insert(initGameData);
    	tinyGameDatas.add(initGameData);
        
        //tinyGameRounds
    	for (List<TinyGameRound> rounds : tinyGameRounds.values()) {
			for (TinyGameRound round : rounds) {
				context().delete(round);
			}
		}
    	tinyGameRounds.clear();
    }
    
    public Map<String, Module> getModuleMap(){
    	return moduleMap();
    }
    
    /**
     * @return 返回当前游戏数据(还未领取卡牌和奖励的，也是当前的)
     */
    public TinyGameData getCurTinyGameData(){
    	for (TinyGameData data : tinyGameDatas) {
			if (data.getIsFinish() != (byte)1 || data.getIsGetReward() != (byte)1) {
				return data;
			}
		}
    	
    	return null;
    }
    
    public TinyGameData getTinyGameDataById(int gameId){
    	for (TinyGameData data : tinyGameDatas) {
			if (data.getGameId() == gameId) {
				return data;
			}
		}
    	
    	return null;
    }
    
    public List<Integer> getPlayedGameIds(){
    	List<Integer> ret = new ArrayList<Integer>();
    	for (TinyGameData data : tinyGameDatas) {
			ret.add(data.getGameId());
		}
    	return ret;
    }
    
    public int getPlayedGameCount(){
    	return getPlayedGameIds().size();
    }
    
    public List<Integer> getFinishGameIds(){
    	List<Integer> ret = new ArrayList<Integer>();
    	for (TinyGameData data : tinyGameDatas) {
    		if (data.getIsFinish() == (byte)1 && data.getIsGetReward() == (byte)1) {
    			ret.add(data.getGameId());
			}
		}
    	return ret;
    }
    
    public int getFinishGameCount(){
    	return getFinishGameIds().size();
    }
    
    public Map<Integer, List<TinyGameRound>> getTinyGameRounds(){
    	return tinyGameRounds;
    }
    
    public List<Integer> getMyCardIds(){
    	List<Integer> myCardIds = new ArrayList<Integer>();
    	for (TinyGameData data : tinyGameDatas) {
			if (data.getIsFinish() == (byte)1 && data.getIsGetReward() == (byte)1) {
				myCardIds.addAll(data.getChoseCardIds());
			}
		}
    	
    	return myCardIds;
    }
    
    public void updateGameCaveScore(){
    	int score = 0;
    	for (TinyGameData data : tinyGameDatas) {
			if (data.getIsFinish() == (byte)1) {
				score += data.getScore();
			}
		}
    	
    	roleGameCave.setScore(score);
    	context().update(roleGameCave);
    }
    
    public void addDailyCount(short dailyId , int count){
    	eventDispatcher().fire(new DailyFuntionEvent(dailyId, count));
    }
    
    public void updateRankScore(){
    	eventDispatcher().fire(new FinishAllTinyGameEvent(id(), roleGameCave.getScore()));
    }
    
    public int getTotalPlayCount(){
//    	short dailyId = DailyManager.DAILYID_GAME_CAVE;
//        DailyVo dailyVo = DailyManager.getDailyVo(dailyId);
//        if (dailyVo != null) {
//			return dailyVo.getCount();
//		}
//        
//        return 0;
    	
    	return GameCaveManager.gameTimes;
    }
    
    public int getLeftPlayCount(){
    	int totalCount = getTotalPlayCount();
    	int finishCount = getFinishGameCount();
    	int leftCount = totalCount - finishCount;
    	return leftCount > 0 ? leftCount : 0;
    }
    
    public void sendRewardToEmailBeforeReset(){
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData != null && curGameData.getIsFinish() == (byte)1) {//通过邮件发奖
    		int gameId = curGameData.getGameId();
    		int star = curGameData.getStar();
    		GameCaveVo vo = GameCaveManager.getGameCaveVo(gameId);
        	if (vo == null) {
    			return;
    		}
    		//发奖
        	Map<Integer, Integer> starReward = vo.getRewardMap();
        	if (starReward.containsKey(star)) {
    			int dropId = starReward.get(star);
    			DropModule dropModule = (DropModule)module(MConst.Drop);
    			Map<Integer, Integer> rewardMap = dropModule.executeDrop(dropId, 1,true);
    			ServiceHelper.emailService().sendToSingle(id(), GameCaveManager.gameCaveMailId,
						0L, "游园", rewardMap, vo.getName());
    		}
		}
    }
    
    public void enterGameCaveScene(){
    	SceneModule sm = module(MConst.Scene);
        int safeId = GameCaveManager.gameCaveSafeId;
        sm.enterScene(SceneManager.SCENETYPE_GAMECAVE, safeId, safeId);
    }
       
    public int getFinishRoundCount(int gameId){
    	List<TinyGameRound> rounds = getGameRounds(gameId);
    	return rounds == null ? 0 : rounds.size();
    }
    
    public List<TinyGameRound> getGameRounds(int gameId){
    	return tinyGameRounds.get(gameId);
    }
    
    public TinyGameBase getTinyGameBase(byte type){
    	TinyGameBase tinyGameBase = null;
    	TinyGameEnum tinyGameEnum = TinyGameEnum.getByType(type);
        try {
        	tinyGameBase = tinyGameEnum.getTinygameClass().newInstance();
		} catch (InstantiationException e) {
			com.stars.util.LogUtil.error("获取小游戏实例失败", e);
		} catch (IllegalAccessException e) {
			com.stars.util.LogUtil.error("获取小游戏实例失败", e);
		}
        
        return tinyGameBase;
    }
    
    public void sendGameCaveVo(byte type){
    	switch (type) {
		case 0:{//0代表游园表
			ClientGameCaveVo clientGameCaveVo = new ClientGameCaveVo(type);
			clientGameCaveVo.setGameCaveVoMap(GameCaveManager.getGameCaveVoMap());
			send(clientGameCaveVo);
			break;
		}
		case TinyGameBase.AnswerType:{
			ClientGameCaveVo clientGameCaveVo = new ClientGameCaveVo(TinyGameBase.AnswerType);
			clientGameCaveVo.setQuestionVoMap(GameCaveManager.getQuestionVoMap());
			send(clientGameCaveVo);
			break;
		}
		case TinyGameBase.ArcherType:{
			ClientGameCaveVo clientGameCaveVo = new ClientGameCaveVo(TinyGameBase.ArcherType);
			clientGameCaveVo.setShootVoMap(GameCaveManager.getShootVoMap());
			send(clientGameCaveVo);
			break;
		}
		default:
			break;
		}
    }
    
    public void sendGameCaveData(){
    	int curGameId = -1;
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData != null) {
			curGameId = curGameData.getGameId();
		}
    	
    	int leftCount = getLeftPlayCount();
    	
    	ClientGameCaveData clientGameCaveData = new ClientGameCaveData();
    	clientGameCaveData.setCurGameId(curGameId);
    	clientGameCaveData.setLeftCount(leftCount);
    	clientGameCaveData.setIsGetReward(roleGameCave.getIsGetReward());
    	clientGameCaveData.setScore(roleGameCave.getScore());
    	clientGameCaveData.setCurCardIds(getMyCardIds());
    	clientGameCaveData.setFinishGameIds(getFinishGameIds());   	
    	send(clientGameCaveData);
    }
    
    public void sendTinyGameData(int gameId){
    	TinyGameData data = getTinyGameDataById(gameId);
    	if (data == null) {
			warn("Game is not exist");
			return;
		}
    	
    	GameCaveVo gameCaveVo = GameCaveManager.getGameCaveVo(gameId);
    	if (gameCaveVo == null) {
    		warn("Get no game product data");
			return;
		}
    	
    	List<TinyGameRound> rounds = getGameRounds(gameId);
    	int finishRound = rounds == null ? 0 : rounds.size();
    	int totalRound = gameCaveVo.getGameTimes();
    	byte gameType = gameCaveVo.getType();
    	TinyGameBase tinyGameBase =  getTinyGameBase(gameType);
    	if (tinyGameBase == null) {
			return;
		}
    	tinyGameBase.setModule(this);
    	tinyGameBase.setGameId(gameId);
    	String dataStr = tinyGameBase.getDataStr();
    	
    	ClientTinyGameData clientTinyGameData = new ClientTinyGameData();
    	clientTinyGameData.setGameId(gameId);
    	clientTinyGameData.setFinishRound(finishRound);
    	clientTinyGameData.setTotalRound(totalRound);
    	clientTinyGameData.setIsGetReward(data.getIsGetReward());
    	clientTinyGameData.setStar(data.getStar());
    	clientTinyGameData.setDataStr(dataStr);   	
    	send(clientTinyGameData);
    }
    
    public void sendChoseGameList(){
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData != null) {
    		warn("You have game already");
			return;
		}
    	
    	Set<Integer> playedGameIds = new HashSet<Integer>(getPlayedGameIds());   	
    	List<Integer> allGameIds = GameCaveManager.getAllGameIds();
    	List<Integer> randomIds = GameCaveManager.getRandomInts(allGameIds, 3, playedGameIds);
    	if (randomIds.size() <= 0) {
    		warn("No game left");
			return;
		}
    	
    	ClientChooseGameList clientChooseGameList = new ClientChooseGameList();
    	clientChooseGameList.setGameIds(randomIds);    	
    	send(clientChooseGameList);
    }
    
    public void chooseGame(int gameId){
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData != null) {
    		warn("You have game already , can not choose");
			return;
		}
    	
    	Set<Integer> playedGameIds = new HashSet<Integer>(getPlayedGameIds());   	
    	if (playedGameIds.contains(gameId)) {
    		warn("You have played the game");
			return;
		}
    	
    	int leftPlayCount = getLeftPlayCount();
    	if (leftPlayCount <= 0) {
			warn("剩余游戏次数为0");
			return;
		}
    	
    	GameCaveVo gameCaveVo = GameCaveManager.getGameCaveVo(gameId);
    	if (gameCaveVo == null) {
			warn("Get no game product data");
			return;
		}
    	
    	int roundIndex = getPlayedGameCount() + 1;
    	TinyGameData initGameData = new TinyGameData(id(), roundIndex, gameId, (byte)0, "", "", "", 0, 0, (byte)0, (byte)0, -1);
    	context().insert(initGameData);
    	tinyGameDatas.add(initGameData);
    	
    	sendTinyGameData(gameId);
    	
    	ClientChooseGame clientChooseGame = new ClientChooseGame();
    	clientChooseGame.setGameId(gameId);
    	clientChooseGame.setResult(GameCaveConstant.RESULT_SUCCESS);    	
    	send(clientChooseGame);
    }
   
    public void requestPlayGame(int gameId){
    	//判断是否为当前gameId
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData == null) {
			warn("当前无正在进行的游戏");
			return;
		}
    	
    	if (curGameData.getIsFinish() == (byte)1) {
    		warn("该游戏已结束");
			return;
		}
    	
    	if (curGameData.getGameId() != gameId) {
			warn("不是当前游戏");
			return;
		}
    	
    	GameCaveVo vo = GameCaveManager.getGameCaveVo(gameId);
    	if (vo == null) {
			warn("获取游戏产品数据失败");
			return;
		}
    	
    	//随机一个游戏给客户端玩
        TinyGameBase tinyGameBase = getTinyGameBase(vo.getType());
        if (tinyGameBase == null) {
			com.stars.util.LogUtil.info("GameCaveModule.requestPlayTinyGame Get tinyGameBase,gameId=" + gameId);
			return;
		}
		
        int roundIndex = getFinishRoundCount(gameId) + 1;
        int lastRoundId = curGameData.getLastRoundId();
        int randomRoundId = tinyGameBase.getRandomRoundId(lastRoundId, roundIndex);
    	
        curGameData.setLastRoundId(randomRoundId);
        context().update(curGameData);
        
    	//返回给客户端
        ClientRequestPlayGame clientRequestPlayGame = new ClientRequestPlayGame();
        clientRequestPlayGame.setGameId(curGameData.getGameId());
        clientRequestPlayGame.setRoundId(randomRoundId);
        clientRequestPlayGame.setResult(GameCaveConstant.RESULT_SUCCESS);        
        send(clientRequestPlayGame);
        
        if (lastRoundId == -1) {//若lastRoundId为-1，则是今天第一次玩这个游戏，则认为是开始玩
        	//打印开始日志;
            ServerLogModule serverLogModule = (ServerLogModule)(module(MConst.ServerLog));
            serverLogModule.Log_core_activity(ThemeType.ACTIVITY_8.getOperateId(), ThemeType.ACTIVITY_START.getOperateName(),
            		gameId, serverLogModule.makeJuci(), gameId, "", "");  
		}
    }

    public void startGame(int gameId , int roundId){
    	//判断是否为当前gameId
    	TinyGameData curGameData = getCurTinyGameData();
    	if (curGameData == null) {
			warn("当前无正在进行的游戏");
			return;
		}
    	
    	if (curGameData.getIsFinish() == (byte)1) {
    		warn("该游戏已结束");
			return;
		}
    	
    	if (curGameData.getGameId() != gameId || curGameData.getLastRoundId() != roundId) {
			warn("不是当前游戏");
			return;
		}
    	
    	GameCaveVo vo = GameCaveManager.getGameCaveVo(gameId);
    	if (vo == null) {
			warn("获取游戏产品数据失败");
			return;
		}
    	
    	if (curTinyGame != null && curTinyGame.getGameId() == gameId && curTinyGame.getCurRoundId() == roundId) {
			warn("游戏正在进行中");
			return;
		}
    	
    	TinyGameBase tinyGameBase = getTinyGameBase(vo.getType());
        if (tinyGameBase == null) {
			com.stars.util.LogUtil.info("GameCaveModule.startGame Get tinyGameBase,gameId=" + gameId);
			return; 
 		}
    	
        tinyGameBase.setModule(this);
        tinyGameBase.setGameId(curGameData.getGameId());
        tinyGameBase.setCurRoundId(curGameData.getLastRoundId());
         
    	curTinyGame = tinyGameBase;
    	curTinyGame.start();
    	
    	//发消息给客户端
    	ClientStartGame clientStartGame = new ClientStartGame();
    	clientStartGame.setResult(GameCaveConstant.RESULT_SUCCESS);  	
    	send(clientStartGame);
    }
    
    public void exitGame(int gameId , int roundId){
    	if (curTinyGame != null && curTinyGame.getGameId() == gameId && curTinyGame.getCurRoundId() == roundId) {
			curTinyGame = null;
			
			ClientExitGame clientExitGame = new ClientExitGame();
			clientExitGame.setResult(GameCaveConstant.RESULT_SUCCESS);
			send(clientExitGame);
		}else{
			warn("不是当前游戏");
		}
    }
    
    public void finishGame(int gameId , int roundId , String dataStr){
    	if (curTinyGame != null && curTinyGame.getGameId() == gameId && curTinyGame.getCurRoundId() == roundId) {
			curTinyGame.finish(dataStr);
			curTinyGame = null;
		}else{
			warn("不是当前游戏");
		}
    }
    
    public void requestGetCard(int gameId){
    	TinyGameData tinyGameData =  getTinyGameDataById(gameId);
    	if (tinyGameData == null || tinyGameData.getIsFinish() != (byte)1) {
			warn("该游戏未完成，不能领牌");
			return;
		}
    	
    	if (tinyGameData.getIsGetReward() == (byte)1) {
			warn("已领取");
			return;
		}
    	
    	GameCaveVo vo = GameCaveManager.getGameCaveVo(gameId);
    	if (vo == null) {
			return;
		}
    	
    	List<Integer> randomCardIds = tinyGameData.getRandomCardIds();  
    	int star = tinyGameData.getStar();
    	int canChooseCardCount = 0;
    	Map<Integer, List<Integer>> cardRewardNum = vo.getCardRewardNumMap();
		List<Integer> cardNum = cardRewardNum.get(star);
		if (cardNum != null && cardNum.size() >= 2) {
			int randomCount = cardNum.get(0);
			canChooseCardCount = cardNum.get(1);			
			if (randomCardIds.size() <= 0) {
				randomCardIds = CardManager.randomCards(randomCount);
				tinyGameData.setRandomCardIds(randomCardIds);
				context().update(tinyGameData);
			}
		}
    	
		//可获得积分
		int score = 0;
    	TinyGameBase tinyGameBase = getTinyGameBase(vo.getType());
        if (tinyGameBase != null) {
        	tinyGameBase.setModule(this);
            tinyGameBase.setGameId(tinyGameData.getGameId());
            tinyGameBase.setCurRoundId(tinyGameData.getLastRoundId());
            score = tinyGameBase.calculateScore();
 		}  	
		
    	//发送消息给客户端
    	ClientRequestGetCard clientRequestGetCard = new ClientRequestGetCard();
    	clientRequestGetCard.setRandomCardIds(randomCardIds);
    	clientRequestGetCard.setCanChooseCount(canChooseCardCount);
    	clientRequestGetCard.setScore(score);
    	send(clientRequestGetCard);
    }
    
    public void getTinyGameCardAndReward(int gameId , List<Integer> chooseCardIds){
    	TinyGameData tinyGameData =  getTinyGameDataById(gameId);
    	if (tinyGameData == null || tinyGameData.getIsFinish() != (byte)1) {
			warn("该游戏未完成，不能领牌");
			return;
		}
    	
    	if (tinyGameData.getIsGetReward() == (byte)1) {
			warn("已领取");
			return;
		}
    	
    	GameCaveVo vo = GameCaveManager.getGameCaveVo(gameId);
    	if (vo == null) {
			return;
		}
    	
    	List<Integer> randomCardIds = tinyGameData.getRandomCardIds();  	
    	for (Integer chooseCardId : chooseCardIds) {
			if (!randomCardIds.contains(chooseCardId)) {
				warn("非法选牌");
				return;
			}
		}
    	
    	int star = tinyGameData.getStar();
    	
    	int canChooseNum = 0;
		Map<Integer, List<Integer>> cardRewardNum = vo.getCardRewardNumMap();
		if (cardRewardNum.containsKey(star)) {
			List<Integer> cardNum = cardRewardNum.get(star);
			canChooseNum = cardNum.get(1);
		}
    	
		if (chooseCardIds.size() > canChooseNum) {
			warn("超过可选牌数");
			return;
		}
		
    	//通过判断可以领牌、领奖、发积分  	
    	//添加牌
    	tinyGameData.setChoseCardIds(chooseCardIds);
    	
    	//发奖
    	Map<Integer, Integer> starReward = vo.getRewardMap();
    	if (starReward.containsKey(star)) {
			int dropId = starReward.get(star);
			DropModule dropModule = (DropModule)module(MConst.Drop);
			ToolModule toolModule = (ToolModule)module(MConst.Tool);
			Map<Integer, Integer> rewardMap = dropModule.executeDrop(dropId, 1,true);
			toolModule.addAndSend(rewardMap, MConst.CCGameCave , EventType.GAMECAVE.getCode());
			tinyGameData.setRewardMap(rewardMap);
		}
    	
    	//添加积分
    	TinyGameBase tinyGameBase = getTinyGameBase(vo.getType());
        if (tinyGameBase != null) {
        	tinyGameBase.setModule(this);
            tinyGameBase.setGameId(tinyGameData.getGameId());
            tinyGameBase.setCurRoundId(tinyGameData.getLastRoundId());
            int score = tinyGameBase.calculateScore();
            tinyGameData.setScore(score);
 		}  	
		//更新游园积分
		updateGameCaveScore();
		//检查是否要更新积分榜
		updateRankScore();
    	
    	tinyGameData.setIsGetReward((byte)1);	
    	context().update(tinyGameData);
    	
    	//发送消息给客户端
    	ClientGetTinyGameRewardAndCard clientGetTinyGameRewardAndCard = new ClientGetTinyGameRewardAndCard();
    	clientGetTinyGameRewardAndCard.setReslut(GameCaveConstant.RESULT_SUCCESS);
    	send(clientGetTinyGameRewardAndCard);
    	
    	sendTinyGameData(gameId);
    	sendGameCaveData();
    	
    	//打印结束日志;
        ServerLogModule serverLogModule = (ServerLogModule)(module(MConst.ServerLog));
        serverLogModule.Log_core_activity(ThemeType.ACTIVITY_8.getOperateId(), ThemeType.ACTIVITY_WIN.getOperateName(), 
        		gameId, serverLogModule.makeJuci(), gameId, "", ""); 
    }
    
    public void getFinalReward(List<Integer> cardIds){
    	if(roleGameCave.getIsGetReward() == (byte)1){
    		warn("奖励已领取");
    		return;
    	}
    	
    	if (getFinishGameCount() < getTotalPlayCount()) {
    		warn("还有游戏未完成，不能领奖");
			return;
		}
    	
    	List<Integer> myCardIds = getMyCardIds();
    	for (Integer cardId : cardIds) {
			if (!myCardIds.remove(cardId)) {
				warn("您没有这样的牌");
				return;
			}
		}
    	
    	int cardGroupType = CardManager.getCardGroupType(cardIds);
    	Map<Integer, Integer> finalRewardMap = GameCaveManager.finalRewardMap;
    	if (!finalRewardMap.containsKey(cardGroupType)) {
	         LogUtil.info("GameCaveModule.getFinalReward card error,cards=" + cardIds + ",roleId=" + id());
	         return;
		}
    	
    	int dropGroup = finalRewardMap.get(cardGroupType);
		DropModule dropModule = (DropModule)module(MConst.Drop);
		ToolModule toolModule = (ToolModule)module(MConst.Tool);
		Map<Integer, Integer> rewardMap = dropModule.executeDrop(dropGroup, 1,true);
		toolModule.addAndSend(rewardMap, MConst.CCGameCave , EventType.GAMECAVE.getCode());
		roleGameCave.setRewardMap(rewardMap);
		roleGameCave.setChoseCardIds(cardIds);
		roleGameCave.setIsGetReward((byte)1);
		context().update(roleGameCave);
		
		//发送消息给客户端
		ClientGetFinalReward clientGetFinalReward = new ClientGetFinalReward();
		clientGetFinalReward.setReslut(GameCaveConstant.GAME_RESULT_SUCCESS);
		send(clientGetFinalReward);
		
		sendGameCaveData();
    }
    
    public void sendAllCards(){
    	List<Integer> myCardIds = getMyCardIds();
    	
    	ClientGetAllCard clientGetAllCard = new ClientGetAllCard();
    	clientGetAllCard.setCardIds(myCardIds);
    	send(clientGetAllCard);
    }
    
    public void sendGameRecordData(){
    	List<TinyGameData> finishGameDatas = new ArrayList<TinyGameData>();
    	for (TinyGameData data : tinyGameDatas) {
    		if (data.getIsFinish() == (byte)1 && data.getIsGetReward() == (byte)1) {
    			finishGameDatas.add(data);
			}
		}
    	
    	ClientGameRecordData clientGameRecordData = new ClientGameRecordData();
    	clientGameRecordData.setFinishGameDatas(finishGameDatas);
    	clientGameRecordData.setRoleGameCave(roleGameCave);
    	send(clientGameRecordData);
    }

	public int getRoleGameCaveScore() {
		return roleGameCave==null?0:roleGameCave.getScore();
	}
}

