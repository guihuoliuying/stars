package com.stars.multiserver.LootTreasure;

import com.stars.modules.loottreasure.LootTreasureManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.network.server.session.GameSession;
import com.stars.network.server.session.SessionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 战斗房间
 * @author dengzhou
 *
 */
public class LTRoom {
	
	private int id;
	
	private Map<Long, Looter> looters;
	
	private Looter master;
	
	private long recycleTime;
	
	private String fightActor;
	
	private long createActorTimeDelay = 0;
	
	private long lastActivityTime;//上一次活跃时间

	//阵营集合,用于记录哪些阵营是否被使用到了/以及未被使用的;
	private Map<Byte, Boolean> campSetMap = new ConcurrentHashMap<>();
	
	private int fightServer;
	
	public Looter getLevelToppestLooter(){
		Looter levelTopLooter = null;
		for(Map.Entry<Long, Looter> kvp: looters.entrySet()){
			if(levelTopLooter == null || kvp.getValue().getFiEntity().getLevel() > levelTopLooter.getFiEntity().getLevel()){
				levelTopLooter = kvp.getValue();
			}
		}
		return levelTopLooter;
	}

	public LTRoom(int id){
		resetCampSet();
		this.id = id;
		looters = new HashMap<Long, Looter>();
		lastActivityTime = 0;
	}

	//初始化阵营集合;
	private void resetCampSet(){
		for (int i = LootTreasureManager.CAMP_MIN; i<=LootTreasureManager.CAMP_MAX; i++){
			campSetMap.put((byte)i, false);
		}
	}

	//获取一个没有被用到的阵营,没有的话,返回-1;
	protected byte getUnUseCamp(){
//		return 1;
		for (Map.Entry<Byte, Boolean> kvp : campSetMap.entrySet()){
			if(!kvp.getValue()){
				return kvp.getKey();
			}
		}
		return -1;
	}
	
	public void removeLooter(long looterId){
		//返还阵营;
		Looter looter = this.looters.get(looterId);
		if(looter != null){
			byte camp = looter.getFiEntity().getCamp();
			if(campSetMap.containsKey(camp)){
				campSetMap.put(camp, false);
			}
		}
		this.looters.remove(looterId);
		if (master != null && master.getId() == looterId) {
			sortLooters();
		}
		if (size() == 1) {
			setRecycleTime(System.currentTimeMillis() + PVPLootTreasure.IDLE_ROOM_RECYCEL_TIME);
		}
	}
	
	public void addLooter(Looter looter){
		looters.put(looter.getId(), looter);
		sortLooters();
		//设置阵营;
		setLooterCamp(looter);
		if (recycleTime != 0) {
			recycleTime = 0;
		}
		if (size() > 1 && recycleTime != 0) {
			this.recycleTime = 0;
		}
	}

	private void setLooterCamp(Looter looter){
		byte camp = getUnUseCamp();
		if(camp >= 0){
			looter.getFiEntity().setCamp(camp);
			campSetMap.put(camp, true);
		}else{
			try {
				throw new Exception("野外夺宝房间阵营分配出问题! 没有可用阵营了!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public Looter getLooter(long looterId){
		return looters.get(looterId);
	}
	
	public int size(){
		return looters.size();
	}

	public long getRecycleTime() {
		return recycleTime;
	}

	public void setRecycleTime(long recycleTime) {
		this.recycleTime = recycleTime;
	}
	
	private void sortLooters(){
		Collection<Looter> col = looters.values();
		Looter tp = null;
		for (Looter looter : col) {
			if (tp == null) {
				tp = looter;
			}else {
				if (looter.getFiEntity().getLevel() > tp.getFiEntity().getLevel()) {
					tp = looter;
				}
			}
		}
		setMaster(tp);
	}
	
	public void recycle(){
		Collection<Looter> col = looters.values();
		for (Looter looter : col) {
			looter.recycle();
		}
		this.looters.clear();
		if (master != null) {
			this.master.recycle();
			setMaster(null);
		}
		this.recycleTime = 0;
		this.createActorTimeDelay = 0;
		resetCampSet();
	}

	public Looter getMaster() {
		return master;
	}

	private void setMaster(Looter master) {
		this.master = master;
	}
	
	public Collection<FighterEntity> getFighterEntitys(){
		Collection<FighterEntity> back = new ArrayList<FighterEntity>();
		Collection<Looter>col = looters.values();
		for (Looter looter : col) {
			back.add(looter.getFiEntity());
		}
		return back;
	}
	
	public void sendPacketToAll(Packet p,long except){
		Collection<Looter>col = looters.values();
		for (Looter looter : col) {
			if (looter.getId() != except) {
				GameSession gs = SessionManager.getSessionMap().get(looter.getId());
				PacketManager.send(gs, p);
			}
		}
	}

	public String getFightActor() {
		return fightActor;
	}

	public void setFightActor(String fightActor) {
		this.fightActor = fightActor;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Map<Long, Looter> getLooters() {
		return looters;
	}

	public void setLooters(Map<Long, Looter> looters) {
		this.looters = looters;
	}

	public long getCreateActorTimeDelay() {
		return createActorTimeDelay;
	}

	public void setCreateActorTimeDelay(long createActorTimeDelay) {
		this.createActorTimeDelay = createActorTimeDelay;
	}

	public long getLastActivityTime() {
		return lastActivityTime;
	}

	public void setLastActivityTime(long lastActivityTime) {
		this.lastActivityTime = lastActivityTime;
	}

	public int getFightServer() {
		return fightServer;
	}

	public void setFightServer(int fightServer) {
		this.fightServer = fightServer;
	}
	
}
