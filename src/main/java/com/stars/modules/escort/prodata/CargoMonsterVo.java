package com.stars.modules.escort.prodata;

import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.prodata.MonsterAttributeVo;
import com.stars.modules.scene.prodata.MonsterSpawnVo;
import com.stars.modules.scene.prodata.MonsterVo;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/12/15.
 */
public class CargoMonsterVo {
    private int powerSection;           //匹配战力段    表示战力，用于分段
    private int minScoreSection;
    private String monsterSpawnId;      //运镖时候刷怪的刷怪id，多个用 + 隔开

    /* 内存数据 */
    private Map<Integer, MonsterVo> monsterVoMap = new HashMap<>();// 怪物模型数据
    // 刷怪配置IdList
    private List<Integer> monsterSpawnIdList = new LinkedList<>();

    public int getPowerSection() {
        return powerSection;
    }

    public void setPowerSection(int powerSection) {
        this.powerSection = powerSection;
    }

    public String getMonsterSpawnId() {
        return monsterSpawnId;
    }

    public void setMonsterSpawnId(String monsterSpawnId) throws Exception {
        this.monsterSpawnId = monsterSpawnId;
        if (StringUtil.isEmpty(monsterSpawnId) || "0".equals(monsterSpawnId)) {
            return;
        }
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

    public Map<Integer, MonsterVo> getMonsterVoMap() {
        return monsterVoMap;
    }

    public List<Integer> getMonsterSpawnIdList() {
        return monsterSpawnIdList;
    }

    public boolean isInRange(int power){
        return power >= minScoreSection && power <= powerSection;
    }

    public void setMinScoreSection(int minScoreSection) {
        this.minScoreSection = minScoreSection;
    }

}
