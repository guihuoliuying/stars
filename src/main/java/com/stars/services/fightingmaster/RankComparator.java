package com.stars.services.fightingmaster;

import com.stars.multiserver.fightingmaster.Fighter;

import java.util.Comparator;

/**
 * Created by zhouyaohui on 2016/11/21.
 */
public class RankComparator implements Comparator<Fighter> {
    @Override
    public int compare(Fighter o1, Fighter o2) {
        if (o2.getRoleFightingMaster().getDisScore() != o1.getRoleFightingMaster().getDisScore()) {
            return o2.getRoleFightingMaster().getDisScore() - o1.getRoleFightingMaster().getDisScore();
        }
        return o2.getCharactor().getFightScore() - o1.getCharactor().getFightScore();
    }
}
