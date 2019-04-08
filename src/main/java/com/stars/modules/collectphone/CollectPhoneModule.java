package com.stars.modules.collectphone;

import com.stars.AccountRow;
import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.collectphone.handler.AbstractTemplateHandler;
import com.stars.modules.collectphone.handler.TemplateHandlerFactory;
import com.stars.modules.collectphone.packet.ClientCollectPhonePacket;
import com.stars.modules.collectphone.prodata.StepOperateAct;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.util.LogUtil;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/13.
 */
public class CollectPhoneModule extends AbstractModule implements OpActivityModule, AccountRowAware {
    private RoleCollectPhone roleCollectPhone;
    private AccountRow accountRow;
    private long joinStatus;

    public CollectPhoneModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        joinStatus = DBUtil.queryCount(DBUtil.DB_USER, String.format("select sum(joinstatus) from rolecollectphone where roleid in (select roleid FROM accountrole where account='%s');", accountRow.getName()));
        roleCollectPhone = DBUtil.queryBean(DBUtil.DB_USER, RoleCollectPhone.class, String.format("select * from rolecollectphone where roleid=%s and acttype=%s;", id(), getActivityType()));

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (getCurShowActivityId() != -1) {
            if (roleCollectPhone == null) {
                roleCollectPhone = new RoleCollectPhone(id(), getActivityType(), 0);
                context().insert(roleCollectPhone);
            }
            signCalRedPoint(MConst.CollectPhone, RedPointConst.COLLECT_PHONE);
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.COLLECT_PHONE)) {
            if (joinStatus == 0) {
                redPointMap.put(RedPointConst.COLLECT_PHONE, "");
            } else {
                redPointMap.put(RedPointConst.COLLECT_PHONE, null);
            }
        }
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(getActivityType());
        if (curActivityId != -1) {
            OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
            boolean join = joinStatus > 0;
            LoginModule loginModule = module(MConst.Login);
            /**
             * 角色是否被限制
             */
            boolean show = operateActivityModule.isShow(operateActVo.getRoleLimitMap());
            if (show && (!CollectPhoneUtil.isForbidCollectPhoneChannel(loginModule.getChannnel())) && (!join)) {
                return curActivityId;
            }
        }
        return -1;
    }

    private int getActivityType() {
        return OperateActivityConstant.ActType_CollectPhone;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    /**
     * 请求主界面数据
     */
    public void reqMainUiData() {
        ClientCollectPhonePacket clientCollectPhonePacket = new ClientCollectPhonePacket(getActivityType(), ClientCollectPhonePacket.SEND_MAIN_UI_DATA);
        int size = roleCollectPhone.getAnswerMap().size();
        int step;
        if (size != 0 && CollectPhoneManager.stepOperateActMap.get(size).getType() == 2) {
            step = size;
        } else {
            step = size + 1;
        }
        clientCollectPhonePacket.setStep(step);
        int remainSecond = CollectPhoneUtil.getRemainSecond(roleCollectPhone);
        clientCollectPhonePacket.setJoinStatus(roleCollectPhone.getJoinStatus());
        clientCollectPhonePacket.setRemainTimes(remainSecond);
        send(clientCollectPhonePacket);
    }


    /**
     * 如果是问答答案直接提供选项id
     * 如果是电话则 1|号码，2|验证码
     *
     * @param step
     * @param answer
     */
    public void reqSubmit(int step, String answer) {
        AbstractTemplateHandler templateHandler = TemplateHandlerFactory.getTemplateHandler(step, roleCollectPhone, moduleMap());
        templateHandler.submit(answer);
        if (step == CollectPhoneManager.stepOperateActList.size() && templateHandler.isOver() && joinStatus == 0) {
            StepOperateAct stepOperateAct = CollectPhoneManager.stepOperateActMap.get(step);
            int groupId = stepOperateAct.getReward();
            ToolModule toolModule = module(MConst.Tool);
            DropModule dropModule = module(MConst.Drop);
            Map<Integer, Integer> reward = dropModule.executeDrop(groupId, 1, true);
            toolModule.addAndSend(reward, EventType.COLLECT_PHONE.getCode());
            ClientAward clientAward = new ClientAward(reward);
            clientAward.setType((byte) 1);
            send(clientAward);
            ClientCollectPhonePacket clientCollectPhonePacket1 = new ClientCollectPhonePacket(getActivityType(), ClientCollectPhonePacket.SEND_FINISH);
            send(clientCollectPhonePacket1);
            roleCollectPhone.setJoinStatus(1);
            joinStatus = 1;
            signCalRedPoint(MConst.CollectPhone, RedPointConst.COLLECT_PHONE);
            ServerLogModule serverLogModule = module(MConst.ServerLog);
            serverLogModule.dynamic_survey_Log(1, roleCollectPhone.getAnswer());
        }
        context().update(roleCollectPhone);
        if (templateHandler.isOver()) {
            ClientCollectPhonePacket clientCollectPhonePacket = new ClientCollectPhonePacket(getActivityType(), ClientCollectPhonePacket.SEND_SUBMIT_QUESTION);
            clientCollectPhonePacket.setStep(step);
            send(clientCollectPhonePacket);
        }
    }

    public void onEvent(Event event) {
        if (event instanceof OperateActivityEvent) {
            OperateActivityEvent operateActivityEvent = (OperateActivityEvent) event;
            if (operateActivityEvent.getActivityType() == getActivityType()) {
                if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Open_Activity) {
                    com.stars.util.LogUtil.info("activity notice:collect phone open:{}", id());
                    try {
                        onInit(false);
                    } catch (Throwable throwable) {
                        com.stars.util.LogUtil.error("collect phone init error!", throwable);
                    }
                } else if (operateActivityEvent.getFlag() == OperateActivityEvent.Flag_Close_Activity) {
                    com.stars.util.LogUtil.info("activity notice:collect phone close:{},the activity data reset", id());
                }
            }
        }
        if (event instanceof RoleLevelUpEvent) {
            if (getCurShowActivityId() != -1) {
                try {
                    onInit(false);
                } catch (Throwable throwable) {
                    LogUtil.error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }
}
