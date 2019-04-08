package com.stars.modules.quwudu;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.quwudu.packet.ClientQuwuduPacket;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/18.
 */
public class QuwuduModule extends AbstractModule implements OpActivityModule {
    private final static String DAILY_ENTER_TIMES = "Quwudu.DailyEnterTime";

    public QuwuduModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        super.onInit(isCreation);
        signCalRedPoint(MConst.Quwudu, RedPointConst.QUWUDU);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        super.onDailyReset(now, isLogin);
        setInt(DAILY_ENTER_TIMES, 0);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.QUWUDU)) {
            int myEnterTime = getInt(DAILY_ENTER_TIMES, 0);
            if (myEnterTime < QuwuduManager.dailytimes && isOpenActivity()) {
                redPointMap.put(RedPointConst.QUWUDU, "");
            } else {
                redPointMap.put(RedPointConst.QUWUDU, null);

            }
        }
    }

    /**
     * 下发场景信息
     */
    public void enterScene() {
        DungeonModule dungeonModule = module(MConst.Dungeon);
        dungeonModule.sendProduceDungeonVo(QuwuduManager.dungeonType);
        SceneModule scene = module(MConst.Scene);
        ProduceDungeonVo dungeonVo = dungeonModule.getEnterProduceDungeonVo(QuwuduManager.dungeonType);
        if (dungeonVo == null) {
            LogUtil.info("活动副本没有产品数据:{}", QuwuduManager.dungeonType);
            return;
        }
        scene.enterScene(SceneManager.SCENETYPE_BUDDY_DUNGEON, dungeonVo.getStageId(), QuwuduManager
                .dungeonType + "-" + dungeonVo.getStageId());
    }

    /**
     * 是否次数能够进入
     *
     * @return
     */
    public boolean canEnterScene() {
        int myEnterTime = getInt(DAILY_ENTER_TIMES, 0);
        if (myEnterTime < QuwuduManager.dailytimes) {
            setInt(DAILY_ENTER_TIMES, myEnterTime + 1);
            signCalRedPoint(MConst.Quwudu, RedPointConst.QUWUDU);
            return true;
        } else {
            warn(DataManager.getGametext("quwudu_timesshort"));
            return false;
        }
    }

    /**
     * 下发限制次数
     */
    public void sendTime() {
        ClientQuwuduPacket clientQuwuduPacket = new ClientQuwuduPacket(getInt(DAILY_ENTER_TIMES, 0), QuwuduManager.dailytimes);
        send(clientQuwuduPacket);
    }

    private boolean isOpenActivity() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_Quwudu);
        if (curActivityId == -1) return false;
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        return vo != null && operateActivityModule.isShow(vo.getRoleLimitMap());
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_Quwudu);
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show) {
                return curActivityId;
            } else {
                return -1;
            }

        }
        return curActivityId;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }
}
