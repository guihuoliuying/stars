package com.stars.multiserver.teamPVPGame.helper;

import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.scene.packet.clientEnterFight.ClientEnterPK;
import com.stars.multiserver.MultiServerHelper;
import com.stars.multiserver.fight.data.LuaFrameData;
import com.stars.multiserver.teamPVPGame.TPGFightScence;
import com.stars.multiserver.teamPVPGame.TPGTeam;
import com.stars.multiserver.teamPVPGame.TPGTeamMember;
import com.stars.multiserver.teamPVPGame.stepIns.AbstractTPGStep;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * @author dengzhou
 *
 *组队pvp分组
 */
public class TPGGroup{
	
	private int id;
	
	private List<TPGTeam>teamList;
		
	private Map<String, FaceNode<TPGTeam>>faceNodeMap;
	
	private FaceNode<TPGTeam>root;
	
	private int floor = 3;
	
	private Map<String, TPGFightScence>fightScenceMap;
	
	private AbstractTPGStep tpgStep;
	
	public TPGGroup(int id,AbstractTPGStep tpgStep){
		this.id = id;
		this.tpgStep = tpgStep;
		teamList = new LinkedList<TPGTeam>();
	}

	public void writeToBuff(NewByteBuffer buff) {
		buff.writeInt(id);// 分组Id
		byte size = (byte) (faceNodeMap == null ? 0 : faceNodeMap.size());
		buff.writeByte(size);
		if (size == 0)
			return;
		for (Map.Entry<String, FaceNode<TPGTeam>> entry : faceNodeMap.entrySet()) {
			buff.writeString(entry.getKey());// 对阵节点Id
			// 节点队伍A
			if (entry.getValue().getValueA() == null) {
				buff.writeInt(-1);
			} else {
				buff.writeInt(entry.getValue().getValueA().getTeamId());
			}
			// 节点队伍B
			if (entry.getValue().getValueB() == null) {
				buff.writeInt(-1);
			} else {
				buff.writeInt(entry.getValue().getValueB().getTeamId());
			}
		}
	}

	public Set<TPGTeam> getAllTeam() {
		Set<TPGTeam> list = new HashSet<>();
		if (StringUtil.isEmpty(faceNodeMap))
			return list;
		for (Map.Entry<String, FaceNode<TPGTeam>> entry : faceNodeMap.entrySet()) {
			list.add(entry.getValue().getValueA());
			list.add(entry.getValue().getValueB());
		}
		return list;
	}
	
	public void putTPGTeam(TPGTeam team){
		teamList.add(team);
	}
	
	private String createFaceNodeId(){
		return tpgStep.tHost.getTpgId()+"_tpgGroup_"+(tpgStep.fightIdCounter++);
	}
	
	/**
	 * 生成对阵图(从无到有)
	 */
	public void makeFaceMap(){
		List<FaceNode<TPGTeam>>ls = new ArrayList<FaceNode<TPGTeam>>();
		FaceNode<TPGTeam>faceNode = new FaceNode<TPGTeam>(createFaceNodeId(),null);
		setRoot(faceNode);
		ls.add(faceNode);
		int index = 0;
		while (index < 2) {
			List<FaceNode<TPGTeam>>tp = new ArrayList<FaceNode<TPGTeam>>();
			FaceNode<TPGTeam> fd;
			for (FaceNode<TPGTeam> faceNode2 : ls) {
				for (int i = 0; i < 2; i++) {
					fd = new FaceNode<TPGTeam>(createFaceNodeId(),faceNode2);
//					faceNode2.setSon(fd);
					tp.add(fd);
					//最后一层，要设置节点的队伍了
					if (index == 1 && teamList.size() > 0) {
						fd.addValue(teamList.remove(0));
						if (teamList.size() > 0) {
							fd.addValue(teamList.remove(0));
						}
					}
				}	
			}
			ls = tp;
			index++;
		}
		faceNodeMap = new HashMap<String, FaceNode<TPGTeam>>();
		for (FaceNode<TPGTeam> faceNode2 : ls) {
			faceNodeMap.put(faceNode2.getId(), faceNode2);
		}
	}

	/**
	 * 从数据库加载对阵图数据后，初始化当前进度的节点
	 */
	public void initFaceMap(){
		int back = addCurrentFloorToMap(root, 0);
		LogUtil.info("初始化对阵图层级数："+back);
	}

	
	private int addCurrentFloorToMap(FaceNode<TPGTeam>node,int cFloor){
		if (cFloor == floor) {
			faceNodeMap.put(node.getId(), node);
			return cFloor;
		}
		int back = cFloor;
		if (node.getLeftSon() != null) {
			back = addCurrentFloorToMap(node.getLeftSon(), cFloor+1);
		}
		if (node.getRightSon() != null) {
			back = addCurrentFloorToMap(node.getLeftSon(), cFloor+1);
		}
		return back;
	}
	
	public void saveFaceMap(){
		List<String> sqls = new LinkedList<>();
		//递归保存所有节点了
		saveFaceNode(root, sqls);
		try {
			DBUtil.execBatch(tpgStep.tHost.getDbAlias(), Boolean.TRUE, sqls);
		} catch (Exception e) {
			//异常这里要处理一下咯
			LogUtil.error(e.getMessage(),e);
		}
	}
	
	private void saveFaceNode(FaceNode<TPGTeam> faceNode,List<String> list){
		StringBuilder sBuilder = new StringBuilder("");
		sBuilder.append("insert into tpggroup(tpgid,step,groupid,nodeid,valuea,valueb,parentid) values('");
		sBuilder.append(tpgStep.tHost.getTpgId()).append("','");
		sBuilder.append(tpgStep.tHost.getStep()).append("','");
		sBuilder.append(id).append("','");
		sBuilder.append(faceNode.getId()).append("','");
		TPGTeam team = faceNode.getValueA();
		sBuilder.append(team == null?-1:team.getTeamId()).append("','");
		team = faceNode.getValueB();
		sBuilder.append(team==null?-1:team.getTeamId()).append("','");
		FaceNode<TPGTeam>father = faceNode.getFather();
		sBuilder.append(father == null?"":father.getId()).append("');");
		list.add(sBuilder.toString());
		if (faceNode.getLeftSon() != null) {
			saveFaceNode(faceNode.getLeftSon(), list);
		}
		if (faceNode.getRightSon() != null) {
			saveFaceNode(faceNode.getRightSon(), list);
		}
	}
	
	public void createFight(){
		fightScenceMap = new HashMap<String, TPGFightScence>();
		Collection<FaceNode<TPGTeam>>col = faceNodeMap.values();
		TPGFightScence tFightScence;
		for (FaceNode<TPGTeam> faceNode : col) {
			if (faceNode.getValueA() == null || faceNode.getValueB() == null) {
				continue;
			}
			// 父节点有值,已经战斗过了
			if (faceNode.getFather().getValueA() != null || faceNode.getFather().getValueB() != null) {
				continue;
			}
			tFightScence = new TPGFightScence(faceNode.getId(), tpgStep,id+"|"+faceNode.getId());
			tFightScence.setStageId(tpgStep.fightScenceId);
			tFightScence.addTeam(faceNode.getValueA());
			tFightScence.addTeam(faceNode.getValueB());
			faceNode.getValueA().setFightSceneId(tFightScence.getId());
			faceNode.getValueB().setFightSceneId(tFightScence.getId());
			tFightScence.createFight(tpgStep.tHost.getFightBaseService(), tpgStep.tHost.getFightServerId(),
					tpgStep.tHost.getServerId(), tpgStep.fightScenceId, (int) tpgStep.lastTimeOfFight);
			fightScenceMap.put(tFightScence.getId(), tFightScence);
		}
	}

	public void memberEnterFight(TPGTeamMember teamMember, String fightSceneId) {
		TPGFightScence fightScence = fightScenceMap.get(fightSceneId);
		if (fightScence == null) {
			return;
		}
		if (fightScence.isStart()) {
			PacketManager.send(teamMember.getRoleId(), new ClientText("战斗已经开始,不能进入"));
			return;
		}
		// 先切连接
		MultiServerHelper.modifyConnectorRoute(teamMember.getRoleId(), tpgStep.tHost.getFightServerId());
		// 发包给客户端
		ClientEnterPK clientEnterPK = new ClientEnterPK();
		clientEnterPK.setFightType(SceneManager.SCENETYPE_TPG);
		clientEnterPK.setStageId(tpgStep.fightScenceId);
		clientEnterPK.setLimitTime((int) (tpgStep.lastTimeOfFight / 1000));// 限制时间
		List<FighterEntity> entityList = new LinkedList<>();
		entityList.add(teamMember.getfEntity());
		clientEnterPK.setFighterEntityList(entityList);
		PacketManager.send(teamMember.getRoleId(), clientEnterPK);
		// 新加入玩家提交给战斗
		fightScence.memberEnterFight(tpgStep.tHost.getFightBaseService(), tpgStep.tHost.getFightServerId(),
				tpgStep.tHost.getServerId(), teamMember);
	}

	public void doLuaFram(String fightSceneId, LuaFrameData luaFrameData) {
		TPGFightScence fightScence = fightScenceMap.get(fightSceneId);
		if (fightScence == null) {
			return;
		}
		fightScence.doLuaFram(luaFrameData);
	}

	public void startFight() {
		for (TPGFightScence tpgFightScence : fightScenceMap.values()) {
			tpgFightScence.startFight(tpgStep.tHost.getFightBaseService(), tpgStep.tHost.getFightServerId(),
					tpgStep.tHost.getServerId());
		}
	}
	
	//强制一场战斗结束
	//理论上战斗服已经通知结束了，这里只是保险起见，检查一下
	public void endField(){
		if (fightScenceMap.size() > 0) {
			Collection<TPGFightScence>col = fightScenceMap.values();
			for (TPGFightScence tpgFightScence : col) {
				tpgFightScence.doLuaFramTimeOut();
			}
		}
	}
	/**
	 * 一轮的结束处理
	 * 晋级队伍设置到父节点
	 */
	public void endRing() {
		Collection<FaceNode<TPGTeam>> col = faceNodeMap.values();
		for (FaceNode<TPGTeam> faceNode : col) {
			if (faceNode.getFather() == null) {
				continue;
			}
			// 父节点有值,不用再判断晋级了
			if (faceNode.getFather().getValueA() != null || faceNode.getFather().getValueB() != null) {
				continue;
			}
			if (faceNode.getValueB() == null || faceNode.getValueA().getWinOnceRing() >= 2) {
				faceNode.getFather().addValue(faceNode.getValueA());
				faceNode.getValueA().ringEnd(Boolean.TRUE);
				faceNode.getValueB().ringEnd(Boolean.FALSE);
			} else if (faceNode.getValueA() == null || faceNode.getValueB().getWinOnceRing() >= 2) {
				faceNode.getFather().addValue(faceNode.getValueB());
				faceNode.getValueB().ringEnd(Boolean.TRUE);
				faceNode.getValueA().ringEnd(Boolean.FALSE);
			} else {// 异常情况,比赛没有打,或者没有打完3场
				// 对比已经打了的场次
				if (faceNode.getValueA().getWinOnceRing() != faceNode.getValueB().getWinOnceRing()) {
					// A获胜
					if (faceNode.getValueA().getWinOnceRing() > faceNode.getValueB().getWinOnceRing()) {
						faceNode.getFather().addValue(faceNode.getValueA());
						faceNode.getValueA().ringEnd(Boolean.TRUE);
						faceNode.getValueB().ringEnd(Boolean.FALSE);
					} else {// B获胜
						faceNode.getFather().addValue(faceNode.getValueB());
						faceNode.getValueB().ringEnd(Boolean.TRUE);
						faceNode.getValueA().ringEnd(Boolean.FALSE);
					}
				} else {// 随机选取
					int random = new Random().nextInt(1);
					if (random == 0) {// A获胜
						faceNode.getFather().addValue(faceNode.getValueA());
						faceNode.getValueA().ringEnd(Boolean.TRUE);
						faceNode.getValueB().ringEnd(Boolean.FALSE);
					} else {// B获胜
						faceNode.getFather().addValue(faceNode.getValueB());
						faceNode.getValueB().ringEnd(Boolean.TRUE);
						faceNode.getValueA().ringEnd(Boolean.FALSE);
					}
				}
			}
		}
	}

	/**
	 * 该小组比赛结束
	 * 直接判断根节点,获得晋级队伍
	 */
	public TPGTeam endGroup(){
		if (root != null) {
			if (root.getValueB() == null || root.getValueA().getWinOnceRing() >= 2) {
				return root.getValueA();
			} else {
				return root.getValueB();
			}
		}
		LogUtil.error("小组赛结果异常，groupid=" + id);
		return null;
	}

	/**
	 * 发奖
	 * 重置所有队伍该阶段累积胜利轮数
	 */
	public void grantReward() {
		List<TPGTeam> list = new LinkedList<>();
		list.addAll(getAllTeam());
		Collections.sort(list, new TotalWinRingComparator());
		ServiceHelper.tpgLocalService().grantReward(list, tpgStep.awardTypeId, Boolean.FALSE);
		// 重置所有队伍的累积胜利轮数
		for (TPGTeam tpgTeam : getAllTeam()) {
			tpgTeam.setTotalWinRing(0);
		}
	}
	
	public void removeFightScene(String sceneId){
		fightScenceMap.remove(sceneId);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setRoot(FaceNode<TPGTeam> root) {
		this.root = root;
	}
	
	public FaceNode<TPGTeam> getRoot(){
		return this.root;
	}

	public boolean isFightSceneIn(String fightSceneId) {
		return fightScenceMap.containsKey(fightSceneId);
	}

	class TotalWinRingComparator implements Comparator<TPGTeam> {

		@Override
		public int compare(TPGTeam o1, TPGTeam o2) {
			// 本阶段胜利轮数
			if (o1.getTotalWinRing() != o2.getTotalWinRing()) {
				return o1.getTotalWinRing() - o2.getTotalWinRing();
				// 队伍总战力
			} else if (o1.getFight() != o2.getFight()) {
				return o1.getFight() - o2.getFight();
				// 队伍Id(更小的(先报名)的获胜)
			} else if (o1.getTeamId() != o2.getTeamId()) {
				return o2.getTeamId() - o1.getTeamId();
			}
			return 0;
		}
	}
}
