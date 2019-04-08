package com.stars.modules.opactkickback;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.drop.DropModule;
import com.stars.modules.opactkickback.userdata.ConsumGateDefineCatalog;
import com.stars.modules.opactkickback.userdata.ConsumeGateDefine;
import com.stars.modules.opactkickback.userdata.RoleConsumeInfo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.opentime.ActOpenTime5;
import com.stars.modules.operateactivity.opentime.ActOpenTimeBase;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.services.ServiceHelper;
import com.stars.services.actloopreset.event.ActLoopResetEvent;
import com.stars.util.DateUtil;
import com.stars.util.LogUtil;

import java.util.Date;
import java.util.Map;

public class OpActKcikBackModule extends AbstractModule implements OpActivityModule {
    private static int MAIL_ID = 26011;
    private RoleConsumeInfo roleConsumeInfo;
    private int curActivityId;
    private long validity;

    public OpActKcikBackModule(String name, long id, Player self, EventDispatcher eventDispatcher,
                               Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public int getCurShowActivityId() {
        if (isOpenActivity()) {
            return curActivityId;
        }
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = String.format("select * from roleconsumeinfo where roleid = %s;", id());
        roleConsumeInfo = DBUtil.queryBean(DBUtil.DB_USER, RoleConsumeInfo.class, sql);
        isOpenActivity(); //只为了初始化当前活动时间
        if (roleConsumeInfo == null) {
            roleConsumeInfo = new RoleConsumeInfo(id());
            roleConsumeInfo.setValidity(validity);
            context().insert(roleConsumeInfo);
        } else {
            if (roleConsumeInfo.getValidity() < validity) { //过期活动
                roleConsumeInfo.reset(validity);
                context().update(roleConsumeInfo);
            }
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        isOpenActivity(); //只为了初始化当前活动时间
        roleConsumeInfo = new RoleConsumeInfo(id());
        roleConsumeInfo.setValidity(validity);
        context().insert(roleConsumeInfo);// 添加插入语句
    }

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    public boolean isEffectiveTime() {
        OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (actVo == null) {
            return false;
        }
        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
        if (!(openTimeBase instanceof ActOpenTime5)) {
            return false;
        }
        ActOpenTime5 openTime5 = (ActOpenTime5) openTimeBase;
        // 有效时间
        validity = openTime5.getEndDate().getTime();
        return DateUtil.isBetween(new Date(), openTime5.getStartDate(), openTime5.getEndDate());
    }

    /**
     * 是否开启活动
     *
     * @return
     */
    public boolean isOpenActivity() {
        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_KickBack);
        if (curActivityId == -1) {
            return false;
        }
        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
        if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap()) && isEffectiveTime()) {
            return true;
        }
        return false;
    }

    public void handleConsumeEvent(int constum) {
        if (!isOpenActivity()) {
            return;
        }
        roleConsumeInfo.incConsume(constum);
        context().update(roleConsumeInfo);
        checkAndSendAward();// 检测并发奖
    }

    private void checkAndSendAward() {
        ConsumeGateDefine[] defines = ConsumGateDefineCatalog.instance.getGateDefines();
        if (defines.length <= 0) {
            return;
        }
        int consume = roleConsumeInfo.getConsume();
        for (ConsumeGateDefine define : defines) {
            if (consume < define.getConsume()) {
                continue;
            }
            if (roleConsumeInfo.hasSendAward(define.getId())) {
                continue;
            }
            sendAward(define);
        }

    }

    private void sendAward(ConsumeGateDefine define) {
        if (define == null) {
            return;
        }
        DropModule dropModule = module(MConst.Drop);
        int dropId = define.getDropId();
        Map<Integer, Integer> map = dropModule.executeDrop(dropId, 1, true);
        ServiceHelper.emailService().sendToSingle(id(), MAIL_ID, 0l, "系统", map, String.valueOf(define.getConsume()));
        roleConsumeInfo.recordSendAward(define.getId());
        context().update(roleConsumeInfo);
    }

    public void view() {
        ServiceHelper.opActKickBack().view(id(), roleConsumeInfo);
    }

    public void onEvent(Event event) {
        if (event instanceof ActLoopResetEvent) {
            try {
                onDataReq();
            } catch (Throwable throwable) {
                LogUtil.error("actLoopReset:3002 fail:" + id(), throwable);

            }
        }
    }
}
