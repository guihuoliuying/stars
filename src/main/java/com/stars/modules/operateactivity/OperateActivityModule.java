package com.stars.modules.operateactivity;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.operateactivity.event.OperateActivityEvent;
import com.stars.modules.operateactivity.event.OperateActivityFlowEvent;
import com.stars.modules.operateactivity.packet.ClientAllActivityInfo;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.operateactivity.rolelimit.ActLevelLimit;
import com.stars.modules.operateactivity.rolelimit.ActRoleLimitBase;
import com.stars.modules.operateactivity.rolelimit.ActSystemLimit;
import com.stars.modules.role.RoleModule;

import java.util.*;

/**
 * Created by gaopeidian on 2016/12/5.
 */
public class OperateActivityModule extends AbstractModule {

    /**
     * 主渠道码
     */
    private String mainChannel;

    public OperateActivityModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.OperateActivity, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onInit(boolean isCreation) {
        //标记需要计算红点
        //signCalRedPoint(MConst.OperateActivity, RedPointConst.OPERATE_ACTIVITY);
        LoginModule loginModule = module(MConst.Login);
        mainChannel = String.valueOf(loginModule.getChannnel());
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        sendAllActivityInfo();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
//        if (redPointIds.contains(Integer.valueOf(RedPointConst.OPERATE_ACTIVITY))) {
//            redPointMap.put(RedPointConst.OPERATE_ACTIVITY, "");        
//        }
    }

    public void handleRoleLevelUp() {
        sendAllActivityInfo();
    }

    public void handleForeShowChange() {
        sendAllActivityInfo();
    }

    public void handleOperateActivityEvent(OperateActivityEvent event) {
        byte flag = event.getFlag();

        if (flag == OperateActivityEvent.Flag_Send_All) {
            sendAllActivityInfo();
        } else if (flag == OperateActivityEvent.Flag_Open_Activity) {
            sendAllActivityInfo();
        } else if (flag == OperateActivityEvent.Flag_Close_Activity) {
            sendAllActivityInfo();
        }
    }

    public void handleOperateActivityFlowEvent(OperateActivityFlowEvent event) {
        if (event.getStepType() == OperateActivityConstant.FLOW_STEP_NEW_DAY) {
            sendAllActivityInfo();
        }
    }

    public void sendAllActivityInfo() {
        Map<Integer, Integer> activityIdsMap = new HashMap<Integer, Integer>();
        Map<Integer, Byte> isShowLabelMap = new HashMap<Integer, Byte>();
        Map<Integer, String> opMoudleNameMap = OperateActivityManager.getOpMoudleNameMap();
        Set<Map.Entry<Integer, String>> entrySet = opMoudleNameMap.entrySet();
        for (Map.Entry<Integer, String> entry : entrySet) {
            int opType = entry.getKey();
            String moduleName = entry.getValue();
            OpActivityModule opActivityModule = (OpActivityModule) module(moduleName);
            if (opActivityModule instanceof NotSendActivityModule) {
                /**
                 * 实现此接口的活动不下发
                 */
                continue;
            }
            byte isShowLabel = 0;
            int showId = -1;
//            if (opActivityModule != null && (!moduleName.equals(MConst.CountDown))) {  //倒计时有两条数据，需要特殊处理
//                showId = opActivityModule.getCurShowActivityId();
//                isShowLabel = opActivityModule.getIsShowLabel();
//            } else {
//                if (moduleName.equals(MConst.OnlyClientShow) || moduleName.equals(MConst.CountDown)) {//表示并没有对应的服务端活动模块，只下发活动数据即可
//                    showId = OperateActivityManager.getCurActivityId(opType);
//                    OperateActVo operateActVo = OperateActivityManager.getOperateActVo(showId);
//                    if (operateActVo != null) {
//                        boolean show = isShow(operateActVo.getRoleLimitMap()) && isOpenChannel(operateActVo.getChannel());
//                        if (!show) {
//                            showId = -1;
//                        }
//                    }
//                }
//            }
            if (showId != -1) {
                activityIdsMap.put(opType, showId);
                isShowLabelMap.put(opType, isShowLabel);
            }
        }

        ClientAllActivityInfo cInfo = new ClientAllActivityInfo();
        cInfo.setActivityIdsMap(activityIdsMap);
        cInfo.setIsShowLabelMap(isShowLabelMap);
        send(cInfo);
    }

    public boolean isShow(int activityId) {
        OperateActVo vo = OperateActivityManager.getOperateActVo(activityId);
        OperateActivityModule operateActivityModule = (OperateActivityModule) module(MConst.OperateActivity);
        if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap())) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isShow(Map<Integer, ActRoleLimitBase> roleLimitMap) {
        if (roleLimitMap != null) {
            for (ActRoleLimitBase roleLimitBase : roleLimitMap.values()) {
                if (!isFit(roleLimitBase)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 渠道是否开放
     *
     * @param channel
     * @return
     */
    public boolean isOpenChannel(String channel) {
        if ("0".equals(channel)) {
            return true;
        } else {
            String[] passChannelArray = channel.split(",");
            for (String pass : passChannelArray) {
                if (mainChannel.equals(pass)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isFit(ActRoleLimitBase showLimitBase) {
        if (showLimitBase instanceof ActLevelLimit) {
            ActLevelLimit actLevelLimit = (ActLevelLimit) showLimitBase;
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            return roleModule.getLevel() >= actLevelLimit.getLevel();
        } else if (showLimitBase instanceof ActSystemLimit) {
            ActSystemLimit actSystemLimit = (ActSystemLimit) showLimitBase;
            ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
            return foreShowModule.isOpen(actSystemLimit.getSystemName());
        }

        return false;
    }
}

