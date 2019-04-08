package com.stars.multiserver.fightutil.daily5v5;

import com.stars.multiserver.fightutil.FightStat;

/**
 * Created by chenkeyu on 2017-05-04 19:31
 */
public class Daily5v5BattleStat extends FightStat {
    public Daily5v5BattleStat(long camp1Id, long camp2Id, int camp1Morale, int camp2Morale) {
        super(camp1Id, camp2Id);
        this.setCamp1TotalPoints(0);
        this.setCamp2TotalPoints(0);
        this.setCamp1Morale(camp1Morale);
        this.setCamp2Morale(camp2Morale);;
    }
    
}
