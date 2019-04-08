package com.stars.modules.newequipment.prodata;

import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/6/8.
 */
public class TokenRandomRangeVo {
    private long randomRangeId;
    private int tokenNumIndexId;
    private String tokenLevelRange;
    private String tokenSkillLevelRange;
    private int tokenSkillId;
    private int minSkillLevel;
    private int maxSkillLevel;
    private Map<Byte,TokenWashHoleRandomDataVo> tokenWashHoleRandomDataVoMap;

    public long getRandomRangeId() {
        return randomRangeId;
    }

    public void setRandomRangeId(long randomRangeId) {
        this.randomRangeId = randomRangeId;
    }

    public int getTokenNumIndexId() {
        return tokenNumIndexId;
    }

    public void setTokenNumIndexId(int tokenNumIndexId) {
        this.tokenNumIndexId = tokenNumIndexId;
    }

    public String getTokenLevelRange() {
        return tokenLevelRange;
    }

    public void setTokenLevelRange(String tokenLevelRange) {
        this.tokenLevelRange = tokenLevelRange;
        tokenWashHoleRandomDataVoMap = new HashMap<>();
        if (StringUtil.isEmpty(tokenLevelRange))
            return;
        String[] arrary = tokenLevelRange.split(",");
        for(String holeRandInfo:arrary){
            TokenWashHoleRandomDataVo randomDataVo = new TokenWashHoleRandomDataVo(holeRandInfo);
            tokenWashHoleRandomDataVoMap.put(randomDataVo.getHoleId(),randomDataVo);
        }
    }

    public Map<Byte, TokenWashHoleRandomDataVo> getTokenWashHoleRandomDataVoMap() {
        return tokenWashHoleRandomDataVoMap;
    }

    public void setTokenWashHoleRandomDataVoMap(Map<Byte, TokenWashHoleRandomDataVo> tokenWashHoleRandomDataVoMap) {
        this.tokenWashHoleRandomDataVoMap = tokenWashHoleRandomDataVoMap;
    }

    public String getTokenSkillLevelRange() {
        return tokenSkillLevelRange;
    }

    public void setTokenSkillLevelRange(String tokenSkillLevelRange) {
        this.tokenSkillLevelRange = tokenSkillLevelRange;
        String[] array = tokenSkillLevelRange.split("\\+");
        minSkillLevel = Integer.parseInt(array[0]);
        maxSkillLevel = Integer.parseInt(array[1]);
    }

    public int getMinSkillLevel() {
        return minSkillLevel;
    }

    public void setMinSkillLevel(int minSkillLevel) {
        this.minSkillLevel = minSkillLevel;
    }

    public int getMaxSkillLevel() {
        return maxSkillLevel;
    }

    public void setMaxSkillLevel(int maxSkillLevel) {
        this.maxSkillLevel = maxSkillLevel;
    }

    public int getTokenSkillId() {
        return tokenSkillId;
    }

    public void setTokenSkillId(int tokenSkillId) {
        this.tokenSkillId = tokenSkillId;
    }

    public int getRandomSkillLevel(){
        return RandomUtil.rand(minSkillLevel,maxSkillLevel);
    }
}
