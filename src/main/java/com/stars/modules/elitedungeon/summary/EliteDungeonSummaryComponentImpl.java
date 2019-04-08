package com.stars.modules.elitedungeon.summary;

import com.stars.modules.MConst;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/19.
 */
public class EliteDungeonSummaryComponentImpl extends AbstractSummaryComponent implements EliteDungeonSummaryComponent {
	private int playCount;
    private int rewardTimes;
    private int helpTimes;

    public EliteDungeonSummaryComponentImpl() {
    }

    public EliteDungeonSummaryComponentImpl(int playCount , int rewardTimes , int helpTimes) {
        this.playCount = playCount;
        this.rewardTimes = rewardTimes;
        this.helpTimes = helpTimes;
    }

    @Override
    public String getName() {
        return MConst.EliteDungeon;
    }
    
    @Override
    public int getLatestVersion() {
        return 1;
    }
    
    @Override
    public int getPlayCount(){
    	return playCount;
    }
    
    @Override
    public int getRewardTimes(){
    	return rewardTimes;
    }
    
    @Override
    public int getHelpTimes(){
    	return helpTimes;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        com.stars.util.MapUtil.setInt(map, "playCount", playCount);
        com.stars.util.MapUtil.setInt(map, "rewardTimes", rewardTimes);
        com.stars.util.MapUtil.setInt(map, "helpTimes", helpTimes);
        return StringUtil.makeString2(map, '=', ',');
    }
    
    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.playCount = com.stars.util.MapUtil.getInt(map, "playCount", 0);
        this.rewardTimes = com.stars.util.MapUtil.getInt(map, "rewardTimes", 0);
        this.helpTimes = MapUtil.getInt(map, "fightScore", 0);
    }
}
