package com.stars.modules.familyactivities.invade;

import com.stars.modules.familyactivities.invade.prodata.FamilyInvadeVo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by liuyuheng on 2016/10/17.
 */
public class FamilyInvadeManager {
    public static int normalMonsterSpawnCoef;// 小怪刷怪系数
    public static int eliteMonsterSpawnCoef;// 精英怪刷怪系数
    public static int normalMonsterNpcId;// 小怪的触发npcid
    public static int eliteMonsterNpcId;// 精英怪的触发npcid
    public static int[] monsterNpcPosX;// 触发npc坐标x范围
    public static int monsterNpcPosY;// 触发npc坐标y
    public static int[] monsterNpcPosZ;// 触发npc坐标z
    public static int awardBoxNpcId;// 宝箱npcid
    public static int awardBoxNum;// 宝箱数量
    public static Map<Integer, Integer> boxReward;// 宝箱奖励
    public static long awardBoxShow;// 宝箱持续存在时间(ms)
    public static List<int[]> rankGroup;// 排行组,[最小排名,最大排名,奖励组Id]
    public static Map<Integer, Map<Integer, Integer>> rankGroupAward;// 奖励组,<奖励组Id, <itemId, number>>
    public static int rewardRankMax;// 发奖的排行榜最大名次
    public static int emailTemplateId = 14101;// 发奖邮件模板id
    public static byte minTeamCount;// 队伍最小人数
    public static byte maxTeamCount;// 队伍最大人数

    // <id, vo>
    public static Map<Integer, FamilyInvadeVo> invadeVoMap;

    public static int[] getPosition() {
        int x = new Random().nextInt(monsterNpcPosX[1] - monsterNpcPosX[0]) + monsterNpcPosX[0];
        int z = new Random().nextInt(monsterNpcPosZ[1] - monsterNpcPosZ[0]) + monsterNpcPosZ[0];
        return new int[]{x, monsterNpcPosY, z};
    }

    public static String positionToStr(int[] pos) {
        StringBuilder builder = new StringBuilder("");
        builder.append(pos[0])
                .append("+")
                .append(pos[1])
                .append("+")
                .append(pos[2]);
        return builder.toString();
    }

    public static int getRotation() {
        return new Random().nextInt(359);
    }

    public static FamilyInvadeVo getInvadeVo(int invadeId) {
        return invadeVoMap.get(invadeId);
    }

    public static FamilyInvadeVo getInvadeVo(int level, byte challengeType, byte monsterType) {
        for (FamilyInvadeVo invadeVo : invadeVoMap.values()) {
            if (invadeVo.getLevelLimit()[0] <= level && level <= invadeVo.getLevelLimit()[1] &&
                    invadeVo.getTeamType() == challengeType && invadeVo.getMonsterType() == monsterType) {
                return invadeVo;
            }
        }
        return null;
    }

    public static Map<Integer, Integer> getRankAward(int groupId) {
        return rankGroupAward.get(groupId);
    }
}
