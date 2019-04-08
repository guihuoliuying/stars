package com.stars.services.escort;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.escort.EscortConstant;
import com.stars.modules.escort.EscortManager;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.packet.Packet;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/7.
 */
public class EscortScene {

    private String fightId;
    private int stageId;// 场景Id
    private int carId; //镖车id
    private byte carType;   //镖车类型
    private long roleId;    //运镖人队长id，用于识别仇人
    private String roleName;    //运镖队长名字
    private long familyId;  //家族id
    private byte index;
    private boolean isLeaderOffline = false;

    private String carCurPos;   //镖车当前位置
    private long protectTimes;// 保护时间
    private byte status;// 状态 0未开始 1进行中 2暂停 3pvp中 4结束
    private boolean isCargoCarDead = false;

    private boolean isInPvp = false;    //是否正在pvp
    private byte robbedTimes = 0;       //被抢次数
    private byte beenRobbedSuccess = 0; //被劫成功的次数
    private int escortTotalFighting = 0;//运镖队伍总战力
    private Map<Byte,EscortCargoPosTarget> cargoPosTargets; //镖车的位置目标

    private Map<Long,Escorter> escorterMap = new HashMap<>(); //运镖者
    private Map<Long,Escorter> robberMap = new HashMap<>();   //劫镖者

    private Map<String,FighterEntity> entityMap = new HashMap<>();
    
    private int fightServer;

    public EscortScene() {
    }

    public EscortScene(String fightId,int stageId,int carId,long roleId,byte carType,String roleName,long familyId) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.familyId = familyId;
        this.fightId = fightId;
        this.stageId = stageId;
        this.carId = carId;
        this.carType = carType;
        this.status = EscortConstant.SCENE_STATUS_NOT_BEGIN;

        //镖车行走目标初始化
        this.cargoPosTargets = new HashMap<>();
        byte index = 0;
        String[] posData = EscortManager.getMovePositionData();
        for(String pos:posData){
            cargoPosTargets.put(index,new EscortCargoPosTarget(index,pos));
            index++;
        }
    }

    private void addFightEntity(Escorter escorter){
        entityMap.put(escorter.getPlayerEntity().getUniqueId(),escorter.getPlayerEntity());
        if(StringUtil.isNotEmpty(escorter.getOtherEntities())){
            for(FighterEntity entity:escorter.getOtherEntities()){
                entityMap.put(entity.getUniqueId(),entity);
            }
        }
    }

    public void addEscorter(Escorter escorter){
        escorterMap.put(escorter.getRoleId(),escorter);
        escortTotalFighting += escorter.getPlayerEntity().getFightScore();

        addFightEntity(escorter);
    }

    public void removeEscorter(Escorter escorter){
        if(escorterMap.containsKey(escorter.getRoleId())) {
            escorterMap.remove(escorter.getRoleId());
            escortTotalFighting -= escorter.getPlayerEntity().getFightScore();
        }
    }

    public void addRobber(Escorter escorter){
        if(robberMap == null) robberMap = new HashMap<>();
        robberMap.put(escorter.getRoleId(),escorter);
        addFightEntity(escorter);
    }

    public void removeRobber(Escorter escorter){
        if(robberMap.containsKey(escorter.getRoleId())) {
            robberMap.remove(escorter.getRoleId());
        }
    }

    public void sendPacketToAllPlayer(Packet packet,long exceptRoleId){
        for(Long roleId:escorterMap.keySet()){
            if (roleId != exceptRoleId) PlayerUtil.send(roleId, packet);
        }
        if(StringUtil.isNotEmpty(robberMap)) {
            for (Long roleId : robberMap.keySet()) {
                if (roleId != exceptRoleId) PlayerUtil.send(roleId, packet);
            }
        }
    }

    public Map<String, FighterEntity> getEntityMap() {
        return entityMap;
    }

    public void setEntityMap(Map<String, FighterEntity> entityMap) {
        this.entityMap = entityMap;
    }

    public String getFightId() {
        return fightId;
    }

    public int getStageId() {
        return stageId;
    }

    public int getCarId() {
        return carId;
    }

    public void setFightId(String fightId) {
        this.fightId = fightId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public void setCarId(byte carId) {
        this.carId = carId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public void setProtectTimes(){
        long now = System.currentTimeMillis();
        now += EscortManager.getCargocarProtectTime() * DateUtil.SECOND;
        this.protectTimes = now;
    }

    public boolean isInProtect(){
        long now = System.currentTimeMillis();
        return protectTimes > now;
    }

    public boolean isInPvp() {
        return status == EscortConstant.SCENE_STATUS_IN_PVP;
    }

    public boolean isPause(){
        return status == EscortConstant.SCENE_STATUS_PAUSE;
    }

    public void setInPvp(boolean isInPvp) {
        this.isInPvp = isInPvp;
    }

    public byte getRobbedTimes() {
        return robbedTimes;
    }

    public void addRobbedTimes(){
        this.robbedTimes++;
    }

    public byte getBeenRobbedSuccess() {
        return beenRobbedSuccess;
    }

    public void addBeenRobbedSuccess(){
        this.beenRobbedSuccess++;
    }

    public int getEscortTotalFighting() {
        return escortTotalFighting;
    }

    public Map<Long, Escorter> getEscorterMap() {
        return escorterMap;
    }

    public Map<Long, Escorter> getRobberMap() {
        if(robberMap == null) robberMap = new HashMap<>();
        return robberMap;
    }

    public long getRoleId() {
        return roleId;
    }

    public String getCargoCurPosTargetPos(){
        int len = cargoPosTargets.size();
        EscortCargoPosTarget posTarget;
        for(byte i = 0;i<len;i++){
            posTarget = cargoPosTargets.get(i);
            if(posTarget != null && !posTarget.isFinish()){
                return posTarget.getPosition();
            }
        }
        return "";
    }

    public String getCargoNextTargetPos(int next){
        int len = cargoPosTargets.size();
        EscortCargoPosTarget posTarget;
        String pos = "";
        int target = 0;
        for(byte i = 0;i<len;i++){
            posTarget = cargoPosTargets.get(i);
            if(posTarget != null && !posTarget.isFinish()){
                pos = posTarget.getPosition();
                if(target >= next){
                    return pos;
                }
                target++;
            }
        }
        return pos;
    }

    public EscortCargoPosTarget getCargoCurPosTarget(){
        int len = cargoPosTargets.size();
        EscortCargoPosTarget posTarget;
        for(byte i = 0;i<len;i++){
            posTarget = cargoPosTargets.get(i);
            if(posTarget != null && !posTarget.isFinish()){
                return posTarget;
            }
        }
        return null;
    }

    public void sendPacketToAllEscorter(Packet packet){
        for(Escorter escorter:escorterMap.values()){
            if(escorter == null) continue;
            PlayerUtil.send(escorter.getRoleId(),packet);
        }
    }

    public void sendPacketToAllRobber(Packet packet){
        for(Escorter escorter:robberMap.values()){
            if(escorter == null) continue;
            PlayerUtil.send(escorter.getRoleId(),packet);
        }
    }

    public boolean isCargoCarDead() {
        return isCargoCarDead;
    }

    public void setCargoCarDead(boolean isCargoCarDead) {
        this.isCargoCarDead = isCargoCarDead;
    }

    public FighterEntity getCargoCarEntity(){
        if(entityMap == null) return null;
        return entityMap.get(EscortConstant.CARGO_CAR_FIGHT_ID);
    }

    public String getCarCurPos() {
        return carCurPos;
    }

    public void setCarCurPos(String carCurPos) {
        this.carCurPos = carCurPos;
    }

    public boolean isLeaderOffline() {
        return isLeaderOffline;
    }

    public void setLeaderOffline(boolean isLeaderOffline) {
        this.isLeaderOffline = isLeaderOffline;
    }

    public byte getCarType() {
        return carType;
    }

    public void setCarType(byte carType) {
        this.carType = carType;
    }

	public int getFightServer() {
		return fightServer;
	}

	public void setFightServer(int fightServer) {
		this.fightServer = fightServer;
	}

    public String getRoleName() {
        return roleName;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setRobberMap(Map<Long, Escorter> robberMap) {
        this.robberMap = robberMap;
    }

    public byte getIndex() {
        return index;
    }

    public void setIndex(byte index) {
        this.index = index;
    }
}
