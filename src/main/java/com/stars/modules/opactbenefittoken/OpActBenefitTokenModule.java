package com.stars.modules.opactbenefittoken;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.ProduceDungeonVo;
import com.stars.modules.opactbenefittoken.packet.ClientOpActBenefitToken;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.scene.SceneManager;
import com.stars.modules.scene.SceneModule;
import com.stars.util.LogUtil;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2017/6/15.
 */
public class OpActBenefitTokenModule extends AbstractModule implements OpActivityModule {

    private static final String F_TIMES = "benefitToken.times";

    public OpActBenefitTokenModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("符文体验副本", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        signCalRedPoint(MConst.OpActBenefitToken, RedPointConst.OPACT_BENEFIT_TOKEN);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        setInt(F_TIMES, 0);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.OPACT_BENEFIT_TOKEN)) {
            if (getInt(F_TIMES, 0) < OpActBenefitTokenManager.timesLimit && isOpenActivity()) {
                redPointMap.put(RedPointConst.OPACT_BENEFIT_TOKEN, "");
            } else {
                redPointMap.put(RedPointConst.OPACT_BENEFIT_TOKEN, null);
            }
        }
    }

    /**
     * 下发场景信息
     */
    public void enterScene() {
        DungeonModule dungeonModule = module(MConst.Dungeon);
        dungeonModule.sendProduceDungeonVo(OpActBenefitTokenManager.dungeonType);
        SceneModule scene = module(MConst.Scene);
        ProduceDungeonVo dungeonVo = dungeonModule.getEnterProduceDungeonVo(OpActBenefitTokenManager.dungeonType);
        if (dungeonVo == null) {
            LogUtil.info("活动副本没有产品数据:{}", OpActBenefitTokenManager.dungeonType);
            return;
        }
        scene.enterScene(SceneManager.SCENETYPE_OPACT_BENEFIT_TOKEN_DUNGEON, dungeonVo.getStageId(),
                OpActBenefitTokenManager.dungeonType + "-" + dungeonVo.getStageId());
    }

    /**
     * 是否次数能够进入
     *
     * @return
     */
    public boolean canEnterScene() {
        int times = getInt(F_TIMES, 0);
        if (times < OpActBenefitTokenManager.timesLimit) {
            setInt(F_TIMES, times + 1);
            signCalRedPoint(MConst.OpActBenefitToken, RedPointConst.OPACT_BENEFIT_TOKEN);
            return true;
        } else {
            warn(DataManager.getGametext("quwudu_timesshort"));
            return false;
        }
    }


    private boolean isOpenActivity() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_BenefitToken);
        if (curActivityId == -1) return false;
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        return vo != null && operateActivityModule.isShow(vo.getRoleLimitMap());
    }

    /**
     * 下发限制次数
     */
    public void sendTimes() {
        send(new ClientOpActBenefitToken(getInt(F_TIMES, 0), OpActBenefitTokenManager.timesLimit));
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_BenefitToken);
        OperateActivityModule opActModule = module(MConst.OperateActivity);
        return opActModule.isShow(curActivityId) ? curActivityId : -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }
}
