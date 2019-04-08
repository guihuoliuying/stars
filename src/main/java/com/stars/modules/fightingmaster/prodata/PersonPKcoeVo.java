package com.stars.modules.fightingmaster.prodata;

import com.stars.multiserver.fightingmaster.Matchable;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

/**
 * Created by zhouyaohui on 2016/11/17.
 */
public class PersonPKcoeVo implements Matchable, Cloneable {

    private String powersection;    // 战力分段
    private String disscoresection; // 表现积分分段
    private String rankimage;   // 榜图片
    private String rankiname;   // 榜名字
    private int rankid; // 排行榜id
    private String matchpairodds;   // 匹配机器人几率
    private String matchpower;  // 机器人实际战力
    private String disscorecoe;     // 表现积分参数
    private String matchscorecoe;   // 机器人积分参数
    private String fiveaward;   // 五战宝箱掉落
    private String rankupaward; // 榜晋升宝箱
    private String everyaward;  // 单次奖励
    private int scoresub;    // 积分差极限
    private int levelsub;   // 等级极限差

    private int minPower;
    private int maxPower;
    private int minScore;
    private int maxScore;

    private int minMatchPower;
    private int maxMatchPower;

    private int fiveawardGroupId = 0;   // 五战宝箱掉落groupId
    
    // 比较用
    private int cminPower;
    private int cmaxPower;
    private int cminScore;
    private int cmaxScore;
    
    private String highmatchpower;//最高战力匹配范围
    private int minHighMatchPower = 0;
    private int maxHighMatchPower = 0;

    public int getMinMatchPower() {
        return minMatchPower;
    }

    public int getMaxMatchPower() {
        return maxMatchPower;
    }

    public int getMinScore() {
        return minScore;
    }

    public String getPowersection() {
        return powersection;
    }

    public void setPowersection(String powersection) {
        String[] powers = powersection.split("[+]");
        minPower = Integer.valueOf(powers[0]);
        maxPower = Integer.valueOf(powers[1]);
        cminPower = minPower;
        cmaxPower = maxPower;
        this.powersection = powersection;
    }

    public String getDisscoresection() {
        return disscoresection;
    }

    public void setDisscoresection(String disscoresection) {
        String[] scores = disscoresection.split("[+]");
        minScore = Integer.valueOf(scores[0]);
        maxScore = Integer.valueOf(scores[1]);
        cminScore = minScore;
        cmaxScore = maxScore;
        this.disscoresection = disscoresection;
    }

    public String getRankimage() {
        return rankimage;
    }

    public void setRankimage(String rankimage) {
        this.rankimage = rankimage;
    }

    public String getRankiname() {
        return rankiname;
    }

    public void setRankiname(String rankiname) {
        this.rankiname = rankiname;
    }

    public int getRankid() {
        return rankid;
    }

    public void setRankid(int rankid) {
        this.rankid = rankid;
    }

    public String getMatchpairodds() {
        return matchpairodds;
    }

    public void setMatchpairodds(String matchpairodds) {
        this.matchpairodds = matchpairodds;
    }

    public String getMatchpower() {
        return matchpower;
    }

    public void setMatchpower(String matchpower) {
        this.matchpower = matchpower;
        String[] powers = matchpower.split("[+]");
        minMatchPower = Integer.valueOf(powers[0]);
        maxMatchPower = Integer.valueOf(powers[1]);
    }

    public String getDisscorecoe() {
        return disscorecoe;
    }

    public void setDisscorecoe(String disscorecoe) {
        this.disscorecoe = disscorecoe;
    }

    public String getMatchscorecoe() {
        return matchscorecoe;
    }

    public void setMatchscorecoe(String matchscorecoe) {
        this.matchscorecoe = matchscorecoe;
    }

    public String getFiveaward() {
        return fiveaward;
    }

    public void setFiveaward(String fiveaward) {
        this.fiveaward = fiveaward;

        if (fiveaward == null || StringUtil.isEmpty(fiveaward) || "0".equals(fiveaward)) {
            return;
        }
        
        this.fiveawardGroupId = Integer.parseInt(fiveaward);
    }

    public String getRankupaward() {
        return rankupaward;
    }

    public void setRankupaward(String rankupaward) {
        this.rankupaward = rankupaward;
    }

    public String getEveryaward() {
        return everyaward;
    }

    public void setEveryaward(String everyaward) {
        this.everyaward = everyaward;
    }

    public int getScoresub() {
        return scoresub;
    }

    public void setScoresub(int scoresub) {
        this.scoresub = scoresub;
    }

    public int getLevelsub() {
        return levelsub;
    }

    public void setLevelsub(int levelsub) {
        this.levelsub = levelsub;
    }

    public int getCminPower() {
        return cminPower;
    }

    public void setCminPower(int cminPower) {
        this.cminPower = cminPower;
    }

    public int getCmaxPower() {
        return cmaxPower;
    }

    public void setCmaxPower(int cmaxPower) {
        this.cmaxPower = cmaxPower;
    }

    public int getCminScore() {
        return cminScore;
    }

    public void setCminScore(int cminScore) {
        this.cminScore = cminScore;
    }

    public int getCmaxScore() {
        return cmaxScore;
    }

    public void setCmaxScore(int cmaxScore) {
        this.cmaxScore = cmaxScore;
    }

    
    public int getFiveawardGroupId() {
		return fiveawardGroupId;
	}
    
    @Override
    public int compare(Matchable other) {
        PersonPKcoeVo otherVo = (PersonPKcoeVo) other;
        if (cminPower > otherVo.getCmaxPower()) {
            return 1000;
        }
        if (cmaxPower < otherVo.getCminPower()) {
            return -1000;
        }
        if (cminScore > otherVo.getCmaxScore()) {
            return 1;
        }
        if (cmaxScore < otherVo.getCminScore()) {
            return -1;
        }
        return 0;
    }

    public PersonPKcoeVo copy() {
        try {
            return (PersonPKcoeVo) this.clone();
        } catch (CloneNotSupportedException e) {
            LogUtil.error("clone failed.", e);
        }
        return null;
    }

	public String getHighmatchpower() {
		return highmatchpower;
	}

	public void setHighmatchpower(String highmatchpower) {
		if (highmatchpower == null || highmatchpower.equals("")) {
			return;
		}
		String ss[] = highmatchpower.split("[+]");
		setMinHighMatchPower(Integer.parseInt(ss[0]));
		setMaxHighMatchPower(Integer.parseInt(ss[1]));
	}

	public int getMinHighMatchPower() {
		return minHighMatchPower;
	}

	public void setMinHighMatchPower(int minHighMatchPower) {
		this.minHighMatchPower = minHighMatchPower;
	}

	public int getMaxHighMatchPower() {
		return maxHighMatchPower;
	}

	public void setMaxHighMatchPower(int maxHighMatchPower) {
		this.maxHighMatchPower = maxHighMatchPower;
	}
    
}
