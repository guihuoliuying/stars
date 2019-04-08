package com.stars.multiserver.daily5v5.data;

import com.stars.util.StringUtil;

public class Daily5v5MoraleVo {
	
	private String moraleSection;
    private int buffId;
    private int debuffId;

    private int minMorale;
    private int maxMorale;

    public String getMoraleSection() {
        return moraleSection;
    }

    public void setMoraleSection(String moraleSection) throws Exception {
        this.moraleSection = moraleSection;
        int[] moraleArray = StringUtil.toArray(moraleSection, int[].class, '+');
        this.minMorale = moraleArray[0];
        this.maxMorale = moraleArray[1];
    }

    public int getBuffId() {
        return buffId;
    }

    public void setBuffId(int buffId) {
        this.buffId = buffId;
    }

    public int getMinMorale() {
        return minMorale;
    }

    public int getMaxMorale() {
        return maxMorale;
    }

	public int getDebuffId() {
		return debuffId;
	}

	public void setDebuffId(int debuffId) {
		this.debuffId = debuffId;
	}

}
