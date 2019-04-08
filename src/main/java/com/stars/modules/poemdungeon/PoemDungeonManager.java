package com.stars.modules.poemdungeon;

import com.stars.modules.poemdungeon.prodata.PoemRobotVo;
import com.stars.modules.poemdungeon.teammember.RobotTeamMember;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.offlinepvp.cache.OPEnemyCache;
import com.stars.util.RandomUtil;

import java.util.*;

/**
 * Created by gaopeidian on 2017/5/12.
 */
public class PoemDungeonManager {
    //<poemrobotid,PoemRobotVo>
    private static Map<Integer, PoemRobotVo> poemRobotVoMap = new HashMap<Integer, PoemRobotVo>();
    //<dungeonId,robotMembers>
    private static Map<Integer, List<RobotTeamMember>> dungeonRobotMemberMap = new HashMap<Integer, List<RobotTeamMember>>();

    public static void setPoemRobotVoMap(Map<Integer, PoemRobotVo> map) {
        poemRobotVoMap = map;

        Map<Integer, List<RobotTeamMember>> roboteMap = new HashMap<Integer, List<RobotTeamMember>>();

        for (PoemRobotVo vo : poemRobotVoMap.values()) {
            OPEnemyCache opEnemy = new OPEnemyCache("r" + vo.getPoemRobotId());
            //OPEnemyCache opEnemy = new OPEnemyCache(Integer.toString(vo.getPoemRobotId()));
            opEnemy.setJobId(vo.getJobId());
            opEnemy.setEntityMap(FighterCreator.createRobot(FighterEntity.CAMP_ENEMY, vo));

            Map<String, FighterEntity> entityMap = new HashMap<>();
            Set<Map.Entry<String, FighterEntity>> set = opEnemy.getEntityMap().entrySet();
            StringBuilder builder = new StringBuilder("");
            builder.append("isRobot=").append("1").append(";");
            FighterEntity entity;
            for (Map.Entry<String, FighterEntity> entry : set) {
                entity = entry.getValue().copy();
                //修改角色entity的阵营
                entity.setCamp(FighterEntity.CAMP_SELF);
                //添加机器人的标记
                entity.addExtraValue(builder.toString());
                if (entity.getFighterType() == FighterEntity.TYPE_PLAYER) {//若是玩家主角，则设置为robot
                    entity.setIsRobot(true);
                }
                entityMap.put(entry.getKey(), entity);
            }
            RobotTeamMember robotMember = new RobotTeamMember((byte) 1);// 构造假玩家数据
            //robotMember.setRoleId(Long.parseLong(opEnemy.getUniqueId()));
            robotMember.setStrRoleId(opEnemy.getUniqueId());
            robotMember.setJob((byte) opEnemy.getJobId());
            robotMember.setEntityMap(entityMap);

            int dungeonId = vo.getDungeonId();
            List<RobotTeamMember> list = roboteMap.get(dungeonId);
            if (list == null) {
                list = new ArrayList<RobotTeamMember>();
                roboteMap.put(dungeonId, list);
            }
            list.add(robotMember);
        }

        dungeonRobotMemberMap = roboteMap;
    }

    public static void setDungeonRobotMemberMap(Map<Integer, List<RobotTeamMember>> map) {
        dungeonRobotMemberMap = map;
    }

    public static List<RobotTeamMember> getRobotMembers(int dungeonId) {
        List<RobotTeamMember> retList = new ArrayList<RobotTeamMember>();
        List<RobotTeamMember> list = dungeonRobotMemberMap.get(dungeonId);
        final int MAX_ROBOT = 3;
        if (list != null) {
            if (list.size() > MAX_ROBOT) {
                retList = RandomUtil.random(list, MAX_ROBOT);
            } else {
                for (RobotTeamMember robotTeamMember : list) {
                    retList.add(robotTeamMember);
                }
            }
        }

        return retList;
    }
}
