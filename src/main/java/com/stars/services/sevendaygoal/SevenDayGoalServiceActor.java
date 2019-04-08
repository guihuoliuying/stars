package com.stars.services.sevendaygoal;

import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.sevendaygoal.SevenDayGoalManager;
import com.stars.modules.sevendaygoal.event.RewardCountChangeEvent;
import com.stars.modules.sevendaygoal.prodata.SevenDayGoalVo;
import com.stars.modules.sevendaygoal.userdata.ActSevenDayRewardRecord;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/19.
 */
public class SevenDayGoalServiceActor extends ServiceActor implements SevenDayGoalService {
    static volatile boolean isLoadData = false;
    private DbRowDao dao = new DbRowDao();

    /**
     * 所有活动的奖励领取数据
     * <活动id,<奖励id,奖励记录>>
     */
    private Map<Integer, Map<Integer, ActSevenDayRewardRecord>> allRewardRecordsMap = new HashMap<Integer, Map<Integer, ActSevenDayRewardRecord>>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.SevenDayGoalService, this);
        synchronized (SevenDayGoalServiceActor.class) {
            if (!isLoadData) {
                loadUserData();
                isLoadData = true;
            }
        }
    }

    @Override
    public void printState() {
        for (Map.Entry<Integer, Map<Integer, ActSevenDayRewardRecord>> entry : allRewardRecordsMap.entrySet()) {
            LogUtil.info("容器大小输出:{},allRewardRecordsMap.get({}).size:{}," , this.getClass().getSimpleName(),  entry.getKey(), allRewardRecordsMap.size());
        }

    }

    @Override
    public void save() {
        dao.flush();
    }

    private void loadUserData() throws SQLException {
        String sql1 = "select * from `actsevendayrewardrecord` where `roleid`=" + -1;
        List<ActSevenDayRewardRecord> rewardRecords = DBUtil.queryList(DBUtil.DB_USER, ActSevenDayRewardRecord.class, sql1);
        if (rewardRecords != null && rewardRecords.size() > 0) {
            for (ActSevenDayRewardRecord record : rewardRecords) {
                int activityId = record.getOperateActId();
                Map<Integer, ActSevenDayRewardRecord> map = allRewardRecordsMap.get(activityId);
                if (map == null) {
                    map = new HashMap<Integer, ActSevenDayRewardRecord>();
                    allRewardRecordsMap.put(activityId, map);
                }
                map.put(record.getGoalId(), record);
            }
        }
    }

    /**
     * 获取某个奖励的剩余领取次数
     *
     * @return 返回-1，则该奖励无领取次数的限制
     */
    @Override
    public int getLeftRewardCount(int activityId, int goalId) {
        SevenDayGoalVo vo = SevenDayGoalManager.getSevenDayGoalVo(goalId);
        if (vo == null) {
            return 0;
        }

        int numLimit = vo.getNumlimit();
        if (numLimit == 0) {
            return -1;
        }
        ActSevenDayRewardRecord record = getRewardRecord(activityId, goalId);
        int leftCount = numLimit - record.getGotCount();
        if (leftCount < 0) {
            leftCount = 0;
        }

        return leftCount;
    }

    /**
     * @return 0:不可领取 ， 1:可以领取
     */
    @Override
    public byte getReward(int activityId, int goalId, long roleId) {
        byte ret = 0;

        SevenDayGoalVo vo = SevenDayGoalManager.getSevenDayGoalVo(goalId);
        if (vo == null) {
            PlayerUtil.send(roleId, new ClientText("Get_no_reward_product_data"));
            ret = 0;
            return ret;
        }

        int numLimit = vo.getNumlimit();
        if (numLimit == 0) {//0则无次数限制，直接返回可领取
            ret = 1;
            return ret;
        }

        int gotCount = 0;

        synchronized (this) {
            ActSevenDayRewardRecord record = getRewardRecord(activityId, goalId);
            gotCount = record.getGotCount();
            if (gotCount >= numLimit) {
                PlayerUtil.send(roleId, new ClientText("sevendays_get_countlimit"));
                ret = 0;
                return ret;
            }

            record.setGotCount(gotCount + 1);
            dao.update(record);
        }

        int leftCount = numLimit - (gotCount + 1);
        if (leftCount < 0) {
            leftCount = 0;
        }
        ServiceHelper.roleService().noticeAll(new RewardCountChangeEvent(goalId, leftCount));

        ret = 1;
        return ret;
    }

    /**
     * 数据操作相关的方法
     */
    private ActSevenDayRewardRecord getRewardRecord(int activityId, int goalId) {
        Map<Integer, ActSevenDayRewardRecord> records = allRewardRecordsMap.get(activityId);
        if (records == null) {
            records = new HashMap<Integer, ActSevenDayRewardRecord>();
            allRewardRecordsMap.put(activityId, records);
        }

        ActSevenDayRewardRecord record = records.get(goalId);
        if (record == null) {
            record = new ActSevenDayRewardRecord(-1, activityId, goalId, 0);
            records.put(record.getGoalId(), record);

            dao.insert(record);
        }

        return record;
    }
}
