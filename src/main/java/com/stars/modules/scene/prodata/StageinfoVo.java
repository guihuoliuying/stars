package com.stars.modules.scene.prodata;

import com.stars.modules.scene.SceneManager;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2016/7/20.
 */
public class StageinfoVo {
    private int stageId;//
    private byte stageType;//
    private String stageMap;//
    private String monsterSpawnId;//
    private String dynamicBlock;//
    private String victoryCondition;//
    private String failCondition;//
    private String position;// '角色出生位置'
    private int rotation;// 朝向角度
    private String stageMusic;//
    private String bossMusic;
    private String stageCamera;
    private String cameraControl;
    private String pathPoint;
    private int rebornTime;// '复活cd(ms)'
    private String multiPosition;// 多人时使用出生坐标
    private String multiRotation;// 多人时使用朝向
    private String drama;// 剧情配置
    private byte newcomer;//是否是新手
    private String cg;

    /* 内存数据 */
    private Map<Integer, MonsterVo> monsterVoMap = new HashMap<>();// 怪物模型数据
    // 刷怪配置IdList
    private List<Integer> monsterSpawnIdList = new LinkedList<>();
    // 动态阻挡配置Map,解析时生成唯一Id(stageId+自增序列)
    private Map<String, DynamicBlock> dynamicBlockMap = new HashMap<>();
    private Map<Byte, Integer> victoryConMap = new HashMap<>();// 胜利条件 type-param
    private Map<Byte, Integer> failConMap = new HashMap<>();// 失败条件 type-param
    private List<String> multiPosList = new LinkedList<>();// 对手出生坐标(根据下标取)
    private List<Integer> multiRotList = new LinkedList<>();// 对手朝向角度(根据下标取)

    /**
     * 是否包含时间胜利失败条件
     *
     * @return
     */
    public boolean containTimeCondition() {
        return victoryConMap.containsKey(SceneManager.VICTORY_CONDITION_TIME)
                || failConMap.containsKey(SceneManager.FAIL_CONDITION_TIME);
    }

    /**
     * 获得使用monsterVo
     *
     * @return
     */
    public Map<Integer, MonsterVo> getMonsterVoMap() {
        return monsterVoMap;
    }

    public List<Integer> getMonsterSpawnIdList() {
        return monsterSpawnIdList;
    }

    public Map<String, DynamicBlock> getDynamicBlockMap() {
        return dynamicBlockMap;
    }

    public Map<Byte, Integer> getVictoryConMap() {
        return victoryConMap;
    }

    public Map<Byte, Integer> getFailConMap() {
        return failConMap;
    }

    public String getEnemyPos(int index) {
        try {
            return multiPosList.get(index);
        } catch (Exception e) {
            com.stars.util.LogUtil.error("stageinfo表enemyposition字段,第{}个坐标不存在", index + 1, e);
            return null;
        }
    }

    public int getEnemyRot(int index) {
        try {
            return multiRotList.get(index);
        } catch (Exception e) {
            LogUtil.error("stageinfo表enemyrotation字段,第{}个朝向不存在", index + 1, e);
            return 0;
        }
    }

    public int getStageId() {
        return stageId;
    }

    public void setStageId(int stageId) {
        this.stageId = stageId;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public byte getStageType() {
        return stageType;
    }

    public void setStageType(byte stageType) {
        this.stageType = stageType;
    }

    public String getMonsterSpawnId() {
        return monsterSpawnId;
    }

    public void setMonsterSpawnId(String monsterSpawnId) throws Exception {
        this.monsterSpawnId = monsterSpawnId;
        if (StringUtil.isEmpty(monsterSpawnId) || "0".equals(monsterSpawnId)) {
            return;
        }
//        monsterSpawnIdList = StringUtil.parseArrayIntList(monsterSpawnId, "\\+");
        monsterSpawnIdList = StringUtil.toArrayList(monsterSpawnId, Integer.class, '+');
        initMonsterVoMap(monsterSpawnIdList);
    }

    private void initMonsterVoMap(List<Integer> monsterSpawnIdList) {
        for (int monsterSpawnId : monsterSpawnIdList) {
            MonsterSpawnVo monsterSpawnVo = SceneManager.getMonsterSpawnVo(monsterSpawnId);
            if (monsterSpawnVo == null) {
                throw new IllegalArgumentException("找不到刷怪组,请检查表id=" + monsterSpawnId);
            }
            for (MonsterAttributeVo monsterAttrVo : monsterSpawnVo.getMonsterAttrList()) {
                monsterVoMap.put(monsterAttrVo.getMonsterId(), monsterAttrVo.getMonsterVo());
            }
        }
    }

    public String getDynamicBlock() {
        return dynamicBlock;
    }

    public void setDynamicBlock(String dynamicBlock) throws Exception {
        this.dynamicBlock = dynamicBlock;
        if (StringUtil.isEmpty(dynamicBlock) || "0".equals(dynamicBlock)) {
            return;
        }
        String[] block = dynamicBlock.split("\\|");
        for (int index = 0; index < block.length; index++) {
            String uniqueId = "" + stageId + index;
            DynamicBlock dynamicBlock_ = new DynamicBlock(uniqueId, block[index]);
            dynamicBlockMap.put(uniqueId, dynamicBlock_);
        }

    }

    public String getVictoryCondition() {
        return victoryCondition;
    }

    public void setVictoryCondition(String victoryCondition) throws Exception {
        this.victoryCondition = victoryCondition;
        if (StringUtil.isEmpty(victoryCondition) || "0".equals(victoryCondition)) {
            return;
        }
        victoryCondition = victoryCondition.replace("+", "=");
//        victoryConMap = StringUtil.parseByteIntHashMap(victoryCondition, "|");
        victoryConMap = StringUtil.toMap(victoryCondition, Byte.class, Integer.class, '=', '|');
    }

    public String getFailCondition() {
        return failCondition;
    }

    public void setFailCondition(String failCondition) throws Exception {
        this.failCondition = failCondition;
        if (StringUtil.isEmpty(failCondition) || "0".equals(failCondition)) {
            return;
        }
        failCondition = failCondition.replace("+", "=");
//        failConMap = StringUtil.parseByteIntMap(failCondition, "|");
        failConMap = StringUtil.toConcurrentMap(failCondition, Byte.class, Integer.class, '=', '|');
    }

    public String getStageMusic() {
        return stageMusic;
    }

    public void setStageMusic(String stageMusic) {
        this.stageMusic = stageMusic;
    }

    public String getCameraControl() {
        return cameraControl;
    }

    public void setCameraControl(String cameraControl) {
        this.cameraControl = cameraControl;
    }

    public String getStageCamera() {
        return stageCamera;
    }

    public void setStageCamera(String stageCamera) {
        this.stageCamera = stageCamera;
    }

    public String getBossMusic() {
        return bossMusic;
    }

    public void setBossMusic(String bossMusic) {
        this.bossMusic = bossMusic;
    }

    public String getPathPoint() {
        return pathPoint;
    }

    public void setPathPoint(String pathPoint) {
        this.pathPoint = pathPoint;
    }

    public String getStageMap() {
        return stageMap;
    }

    public void setStageMap(String stageMap) {
        this.stageMap = stageMap;
    }

    public int getRebornTime() {
        return rebornTime;
    }

    public void setRebornTime(int rebornTime) {
        this.rebornTime = rebornTime;
    }

    public String getMultiPosition() {
        return multiPosition;
    }

    public void setMultiPosition(String multiPosition) throws Exception {
        this.multiPosition = multiPosition;
        if (StringUtil.isEmpty(multiPosition) || "0".equals(multiPosition))
            return;
        multiPosList = StringUtil.toArrayList(multiPosition, String.class, '|');
    }

    public String getMultiRotation() {
        return multiRotation;
    }

    public void setMultiRotation(String multiRotation) throws Exception {
        this.multiRotation = multiRotation;
        if (StringUtil.isEmpty(multiRotation) || "0".equals(multiPosition))
            return;
        multiRotList = StringUtil.toArrayList(multiRotation, Integer.class, '|');
    }

    public List<String> getMultiPosList() {
        return multiPosList;
    }

    public List<Integer> getMultiRotList() {
        return multiRotList;
    }

    public String getDrama() {
        return drama;
    }

    public void setDrama(String drama) throws Exception {
        this.drama = drama;
    }

    public byte getNewcomer() {
        return newcomer;
    }

    public void setNewcomer(byte newcomer) {
        this.newcomer = newcomer;
    }

    public String getCg() {
        return cg;
    }

    public void setCg(String cg) {
        this.cg = cg;
    }
}
