package com.stars.multiserver.LootTreasure;

import com.stars.bootstrap.ServerManager;
import com.stars.modules.loottreasure.LootTreasureConstant;
import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.loottreasure.packet.*;
import com.stars.modules.loottreasure.prodata.LootSectionVo;
import com.stars.modules.rank.RankManager;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.Damage;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.ClientStageFinish;
import com.stars.modules.scene.packet.ServerExitFight;
import com.stars.modules.scene.packet.ServerExitFightBack;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterLootTreasurePVE;
import com.stars.modules.scene.packet.fightSync.ServerFightDamage;
import com.stars.modules.scene.prodata.DynamicBlock;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.modules.scene.prodata.StageinfoVo;
import com.stars.multiserver.ServerConnSessionManager;
import com.stars.multiserver.fight.RoleId2ActorIdManager;
import com.stars.multiserver.packet.OfflineNotice;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.server.connector.packet.FrontendClosedN2mPacket;
import com.stars.server.fight.MultiServer;
import com.stars.server.main.message.Disconnected;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.prodata.RankAwardVo;
import com.stars.startup.LootTreasureStartup;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PVELootTreasure extends AbstractLootTreasure {

    private LootSectionVo lootSectionVo = null;
    private PVELootRreasureRunner runner;
    private final int MAX_OTHERPLAYER_NUM_PER_PVESTAGE = 9;
    //伤害排行榜排序的间隔秒数;
    private final int SORT_RANK_INTERVAL_SECONDS = 5;
    //伤害排行榜客户端需求显示的条数;
    private final int RANK_CLIENT_SHOW_COUNT = 5;
    
    public static long WAIT_TIME = 20000L;
    
    static long FIGHT_TIME = 180000L;
    
    //记录当前玩家和其他玩家一起打怪的集合(数量少于MAX_OTHERPLAYER_NUM_PER_PVESTAGE的人);
    private Map<Long, List<Long>> recordPlayerWidthOthersMonsterMap = new ConcurrentHashMap<>();
    private MonsterAttributeVo monsterAttributeVo = null;
    //PVE活动怪物出生的时间戳;
    private long pveMonsterStartTimeStamp = 0;
    //上一次排序的时间;
    private int lastSortRankTime = 0;
    private LootTreasureStartup lootTreasureStartup;

    public PVELootTreasure(LTActor ltActor) {
        super(ltActor);
        lootTreasureStartup = (LootTreasureStartup)((MultiServer)ServerManager.getServer()).getBusiness();
        ltDamageRank = new LTDamageRank();
        lootSectionVo = LootTreasureManager.getLootSectionVo(Integer.parseInt(ltActor.getId()));
        //记录怪物的总血量;
        int monsterAttrId = lootSectionVo.getMonsterid();
        monsterAttributeVo = SceneManager.getMonsterAttrVo(monsterAttrId);
    }
    
    @Override
    public void startRunner(){
    	step = STEP_WAIT;
        nextStepTime = System.currentTimeMillis() + LootTreasureManager.PVE_WAIT_TIME;
        runner = new PVELootRreasureRunner();
        runner.runnable = true;
        runner.start();
        LootTreasureManager.log("夺宝服 夺宝活动PVE[" + ltActor.getId() + "] 开启");
    }

	@Override
	public void onReceived(Object message) {
		if (message instanceof RunEvent) {
			doRun();
			return;
		}
		if(message instanceof ServerFightDamage) {
            ServerFightDamage serverFightDamage = (ServerFightDamage)message;
            dealFightDamage(serverFightDamage);
            return;
        }
		if(message instanceof ServerExitFightBack){
            //发送离开战斗协议到主服中;
            ServerExitFightBack serverExitFightBack = (ServerExitFightBack)message;
            offlineLooters.add(serverExitFightBack.getRoleId());
            Looter looter = this.looters.get(serverExitFightBack.getRoleId());
            if(looter != null){
                GameSession gs = ServerConnSessionManager.get(looter.getServerId());
                PacketManager.send(gs, (ServerExitFightBack) message);
            }
            return;
        }
		if (message instanceof ServerExitFight) {
            ServerExitFight serverExitFight = (ServerExitFight)message;
            Looter looter = looters.get(serverExitFight.getRoleId());
            //发送离开战斗协议到主服中;
            if (looter != null) {
                LootTreasureManager.log("夺宝服PVE 请求离开战斗");
                offlineLooters.add(looter.getId());
                RMLTRPCHelper.lootTreasureService().existFight(looter.getServerId(), looter.getId());
 			}
            return;
		}
		if (message instanceof FrontendClosedN2mPacket || message instanceof Disconnected) {
            //不处理,也由服务器将玩家数据拉入之后的活动阶段和其他玩家共同活动;
            Packet p = (Packet)message;
            offlineLooters.add(p.getRoleId());
			return;
		}
        if (message instanceof OfflineNotice) {
            OfflineNotice offlineNotice = (OfflineNotice) message;
            offlineLooters.add(offlineNotice.getRoleId());
            return;
        }
	}

    @Override
    public void stopSelf() {
        runner.runnable = false;
        runner = null;
        recordPlayerWidthOthersMonsterMap.clear();
        recordPlayerWidthOthersMonsterMap = null;
        lastSortRankTime = 0;
    }
    
    @Override
    public void newLooterCome(int serverId, String serverName, FighterEntity fEntity, int jobId) {
        long id = Long.parseLong(fEntity.getUniqueId());
        Looter looter = looters.get(id);
        if (looter == null) {
            looter = new Looter(serverId, serverName, fEntity, jobId);
            looters.put(id, looter);
        }
        //判断是否存在离线的人;
        if (offlineLooters.contains(looter.getId())){
            offlineLooters.remove(looter.getId());
        }
        if (step == STEP_WAIT) {
            //进入等待地图
            noticeClientToEnterWaitScene(looter);
        } else {
            //刷怪地图;
            noticeClientToEnterMonsterScene(looter,true);
        }
        AttendLootTreasureBack attendLootTreasureBack = new AttendLootTreasureBack((byte) 0);
        sendToServer(looter, attendLootTreasureBack);
    }

    public void initPVPLootTreasure() {
        PVPLootTreasure lootTreasure = new PVPLootTreasure(ltActor);
        ltActor.setLootTreasure(lootTreasure);
        this.ltActor = null;
        Map<Long, Looter> curLooterMap = new ConcurrentHashMap<>();
        Set<Long> offlineLooterSet = new HashSet<>();
        Looter tmpLooter = null;
        for (Map.Entry<Long, Looter> kvp : this.getLooters().entrySet()){
            //先判断是否离线了,并且宝箱数是否>0;
            tmpLooter = kvp.getValue();
            if(offlineLooters.contains(kvp.getKey())){
                if(tmpLooter.getBoxs() <= 0){
                    RoleId2ActorIdManager.remove(kvp.getKey());
                }else {
                    curLooterMap.put(kvp.getKey(), kvp.getValue());
                    offlineLooterSet.add(kvp.getKey());
                }
            }else {
                curLooterMap.put(kvp.getKey(), kvp.getValue());
            }
        }
        lootTreasure.setLooters(curLooterMap);
        lootTreasure.setOfflineLooters(offlineLooterSet);
        this.setLooters(null);
        this.offlineLooters = null;
        lootTreasure.startRunner();
    }

    /**
     * 奖励pve伤害排行榜;
     */
    private void awardPveDamageRank(){
        List<LTDamageRankVo> ltDamageRankVos = ltDamageRank.getLtDamageRankVoList();
        List<RankAwardVo> rankAwardVoList = RankManager.getRankAward(RankConstant.RANKID_LOOTTREASURE_PVE);
        RankAwardVo rankAwardVo ;
        LTDamageRankVo damageRankVo;
        Map<Integer, Integer> awardMap;
        Looter looter;
        //现货去奖励的最高名次,防止后面无谓的轮询;
        int maxRankCount = 0;
        for(int i = 0, len = rankAwardVoList.size(); i<len; i++){
            rankAwardVo = rankAwardVoList.get(i);
            if(maxRankCount < rankAwardVo.getSections()[1]){
                maxRankCount = rankAwardVo.getSections()[1];
            }
        }
        maxRankCount = Math.min(maxRankCount, ltDamageRankVos.size());
        for(int k = 0; k<maxRankCount; k++){
            damageRankVo = ltDamageRankVos.get(k);
            //判断是否在线;
//            if(isOnline(damageRankVo.getRoleId())){
            //离线也需要发送宝箱奖励
            looter = this.looters.get(damageRankVo.getRoleId());
            for (int i = 0, len = rankAwardVoList.size(); i < len; i++) {
                rankAwardVo = rankAwardVoList.get(i);
                if (rankAwardVo.isInSectionRange(k + 1)) {
                    //进行奖励;
                    awardMap = rankAwardVo.getRewardMap();
                    looter.setBoxs((int) (awardMap.values().toArray()[0]));
                    if (isOnline(looter.getId())) {
                        ClientStageFinish finish = new ClientStageFinish(SceneManager.SCENETYPE_LOOTTREASURE_PVE, ClientStageFinish.VICT);
                        finish.setBoxCount(looter.getBoxs());
                        PacketManager.send(looter.getId(), finish);
                    }
                    break;
                }
            }
//            }
        }
    }
    
	public void maintenState(){
		if (runner == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now >= nextStepTime) {
			if (step == STEP_WAIT) {
				//等待时间结束
				step = STEP_FIGHT;
				nextStepTime = now + LootTreasureManager.PVE_FIGHT_TIME;
	            pveMonsterStartTimeStamp = System.currentTimeMillis();
                //刷怪地图;
                noticeClientToEnterMonsterScene();
                //通知客户端PVE阶段开始了;
                ClientLootTreasureInfo clientLootTreasureInfo = new ClientLootTreasureInfo(ClientLootTreasureInfo.TYPE_ACTIVITY_NOTICE);
                clientLootTreasureInfo.setStartStamp(lootTreasureStartup.lFlow.getStartTimeStamp());
                clientLootTreasureInfo.setEndStamp(lootTreasureStartup.lFlow.getEndTimeStamp());
                clientLootTreasureInfo.setActivitySegment(LootTreasureConstant.ACTIVITYSEGMENT.PVE_START);
                for(Map.Entry<Long, Looter> kvp : this.looters.entrySet()){
                    sendToClient(kvp.getValue(), clientLootTreasureInfo);
                }
                int lootCount = this.getLooters()==null?0:this.getLooters().size();
                LogUtil.info(this.toString()+" 夺宝PVE开始 人数: "+lootCount);
			}else if (step == STEP_FIGHT) {
                int lootCount = this.getLooters()==null?0:this.getLooters().size();
                LogUtil.info(this.toString()+" 夺宝PVE刷到PVP 人数: "+lootCount);
				stopSelf();
                awardPveDamageRank();
				initPVPLootTreasure();
			}
		}
	}
	
    public void doRun(){
		maintenState();
        lastSortRankTime++;
        if(lastSortRankTime>=SORT_RANK_INTERVAL_SECONDS){
            lastSortRankTime = 0;
            rankSort(true);
        }
	}

    @Override
    public void revive(long roleId) {

    }

    private void dealFightDamage(ServerFightDamage serverFightDamage){
        List<Damage> damageList = serverFightDamage.getDamageList();
        Damage damage ;
        long roleId ;
        Looter looter ;
        for(int i = 0, len = damageList.size(); i<len; i++){
            damage = damageList.get(i);
            roleId = Long.parseLong(damage.getGiverId());
            looter = looters.get(roleId);
            //因为这里客户端上传的值是负的,所以这里再取负值;
            looter.pveLtDamageRankVo.addAddedDamage(-damage.getValue());
            ltDamageRank.setDamage(looter.pveLtDamageRankVo);
        }
    }

    private void noticeClientToEnterMonsterScene(){
        for(Map.Entry<Long, Looter> kvp : looters.entrySet()){
            noticeClientToEnterMonsterScene(kvp.getValue(),false);
        }
    }

    //通知客户端进入怪物PVE场景;
    private void noticeClientToEnterMonsterScene(Looter looter,boolean flushRank) {
    	if (flushRank) {
    		//进房间就发送伤害排行榜数据到参与的客户端;
            List<LTDamageRankVo> firstRankVoList = ltDamageRank.getFirstList(RANK_CLIENT_SHOW_COUNT);
            ClientLootTreasureRankList clientLootTreasureRankList = new ClientLootTreasureRankList(SceneManager.SCENETYPE_LOOTTREASURE_PVE, firstRankVoList);
            clientLootTreasureRankList.setMySelfRankVo(looter.pveLtDamageRankVo);
            sendToClient(looter, clientLootTreasureRankList);
		}  

        long curRoleId = looter.getId();
        Looter tmpLooter = null;
        ClientEnterLootTreasurePVE clientEnterLootTreasure = null;
        List<FighterEntity> fighterEntityList = new ArrayList<>();
        FighterEntity monsterEntity = FighterCreator.create(FighterEntity.TYPE_MONSTER, "m"+monsterAttributeVo.getMonsterId(), "0", 0, monsterAttributeVo, "0", 0, null);
        fighterEntityList.add(0, monsterEntity);
        //将自己和其他玩家弄进PVE中;
        fighterEntityList.add(looter.getFiEntity());
        List<Long> otherList = new ArrayList<Long>();
        int index = 0;
        FighterEntity fighterEntity = null;
        for (Map.Entry<Long, Looter> kvp : looters.entrySet()) {
            if (!kvp.getKey().equals(curRoleId)) {
                index++;
                otherList.add(kvp.getKey());
                fighterEntity = kvp.getValue().getFiEntity().copy();
                fighterEntity.fighterType = FighterEntity.TYPE_PLAYER;
                fighterEntityList.add(fighterEntity);
                if (index >= MAX_OTHERPLAYER_NUM_PER_PVESTAGE) {
                    break;
                }
            }
        }
        //避免足够的人了还要再次轮询;
        if (index < MAX_OTHERPLAYER_NUM_PER_PVESTAGE) {
            recordPlayerWidthOthersMonsterMap.put(curRoleId, otherList);
        }else{
            recordPlayerWidthOthersMonsterMap.remove(curRoleId);
        }
        //告诉玩家自己要进入场景了;
        clientEnterLootTreasure = produceClientEnterLootTreasure(fighterEntityList);
        sendToClient(looter, clientEnterLootTreasure);
        //通知主服进入夺宝了;
        ClientLootTreasureEnterBack clientLootTreasureEnterBack = new ClientLootTreasureEnterBack(clientEnterLootTreasure.getStageId(), looter.getId());
        sendToServer(looter, clientLootTreasureEnterBack);
        //判断记录的列表里人数够不够,不够的话就新添加当前角色进去, 并告诉玩家中途有玩家需要进入他们的场景了;
        Iterator<Map.Entry<Long, List<Long>>> iterator = recordPlayerWidthOthersMonsterMap.entrySet().iterator();
        Map.Entry<Long, List<Long>> entry = null;
        FighterEntity cloneFighterEntity = null;
        while (iterator.hasNext()) {
            entry = iterator.next();
            if (entry.getKey().equals(curRoleId) || entry.getValue().contains(curRoleId)) {
                continue;
            }
            tmpLooter = looters.get(entry.getKey());
            if (tmpLooter != null) {
                if(entry.getValue().size() + 1 >= MAX_OTHERPLAYER_NUM_PER_PVESTAGE){
                    iterator.remove();
                }else{
                    entry.getValue().add(curRoleId);
                }
                fighterEntityList = new ArrayList<>();
                cloneFighterEntity = looter.getFiEntity().copy();
                cloneFighterEntity.fighterType = FighterEntity.TYPE_PLAYER;
                fighterEntityList.add(cloneFighterEntity);
                ClientLootTreasureAddRemovePlayer clientEnterLootTreasurePVE = new ClientLootTreasureAddRemovePlayer();
                clientEnterLootTreasurePVE.setNewFighter(fighterEntityList);
                sendToClient(tmpLooter, clientEnterLootTreasurePVE);
            }
        }
        LootTreasureManager.log("夺宝服 通知客户端进入怪物PVE场景");
    }

    //通知客户端进入等待场景;
    private void noticeClientToEnterWaitScene(Looter looter) {
        List<FighterEntity> fighterEntityList = new ArrayList<>();
        //将自己和其他玩家弄进PVE中;
        fighterEntityList.add(looter.getFiEntity());
        ClientEnterLootTreasurePVE clientEnterLootTreasure = produceClientEnterLootTreasure(fighterEntityList);
        sendToClient(looter, clientEnterLootTreasure);
        //通知主服进入夺宝了;
        ClientLootTreasureEnterBack clientLootTreasureEnterBack = new ClientLootTreasureEnterBack(clientEnterLootTreasure.getStageId(), looter.getId());
        sendToServer(looter, clientLootTreasureEnterBack);
        LootTreasureManager.log("夺宝服 通知客户端进入等待场景");
    }

    //获取进入场景所需要的数据包;
    private ClientEnterLootTreasurePVE produceClientEnterLootTreasure(List<FighterEntity> fighterList) {
        int stageId = lootSectionVo.getStageid();
        StageinfoVo stageVo = SceneManager.getStageVo(stageId);
        ClientEnterLootTreasurePVE clientEnterLootTreasure = new ClientEnterLootTreasurePVE();
        clientEnterLootTreasure.setIsAgain((byte) 0);
        clientEnterLootTreasure.setStageId(stageId);
        clientEnterLootTreasure.setFightType(stageVo.getStageType());
        /* 预加载怪物 */
        // 遍历怪物掉落模型预加载
        MonsterVo monsterVo = SceneManager.getMonsterVo(monsterAttributeVo.getMonsterId());
        clientEnterLootTreasure.setFighterEntityList(fighterList);
        /* 动态阻挡数据 */
        Map<String, Byte> blockStatus = new HashMap<>();
        for (DynamicBlock dynamicBlock : stageVo.getDynamicBlockMap().values()) {
            blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_CLOSE);
            if (dynamicBlock.getShowSpawnId() == 0) {
                blockStatus.put(dynamicBlock.getUnniqueId(), SceneManager.BLOCK_OPEN);
            }
        }
        clientEnterLootTreasure.addMonsterVoMap(monsterVo);
        clientEnterLootTreasure.setBlockMap(stageVo.getDynamicBlockMap());
        clientEnterLootTreasure.addBlockStatusMap(blockStatus);
        //写入当前怪物的剩余血量;
        clientEnterLootTreasure.monsterPos = monsterAttributeVo.getPosition();
        //怪物每秒的减血量,用于新玩家进入PVE怪物场景时的血量同步,避免血量同步还需要服务端和客户端间进行实时同步;
        int minusHpPerSecond = monsterAttributeVo.getHp()/LootTreasureManager.PVE_FIGHT_TIME*1000;
        clientEnterLootTreasure.minusHpPerSecond = minusHpPerSecond;
        clientEnterLootTreasure.timeStamp = String.valueOf(System.currentTimeMillis());
        clientEnterLootTreasure.monsterRemainHp = monsterAttributeVo.getHp() - ((int)(System.currentTimeMillis() - pveMonsterStartTimeStamp))/1000*minusHpPerSecond;
        LTActor.fillClientEnterLootTreasurePvpSkillBuffData(clientEnterLootTreasure);
        return clientEnterLootTreasure;
    }


    private void rankSort(boolean needSyncToClient){
        //排序号;
        ltDamageRank.sortIndex();
        if(needSyncToClient){
            List<LTDamageRankVo> firstRankVoList = ltDamageRank.getFirstList(RANK_CLIENT_SHOW_COUNT);
            //发送伤害排行榜数据到参与的客户端;
            ClientLootTreasureRankList clientLootTreasureRankList = new ClientLootTreasureRankList(SceneManager.SCENETYPE_LOOTTREASURE_PVE, firstRankVoList);
            for (Map.Entry<Long, Looter> kvp : looters.entrySet()){
                clientLootTreasureRankList.setMySelfRankVo(kvp.getValue().pveLtDamageRankVo);
                sendToClient(kvp.getValue(), clientLootTreasureRankList);
            }
        }
    }

    
	class PVELootRreasureRunner extends Thread{
		
		private boolean runnable = false;
		
		private RunEvent rEvent;
		
		public PVELootRreasureRunner(){
			rEvent = new RunEvent(0);
		}
		@Override
		public void run() {
			while (runnable) {
				ltActor.tell(rEvent, Actor.noSender);
				doSleep(1000l);
			}
		}
		
		private void doSleep(long time){
			try {
				sleep(time);
			} catch (Exception e) {
				LogUtil.error(e.getMessage(), e);
			}
		}
	}
}
