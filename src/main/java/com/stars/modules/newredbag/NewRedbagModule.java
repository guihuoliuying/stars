package com.stars.modules.newredbag;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.familyactivities.bonfire.BonfireActivityFlow;
import com.stars.modules.newredbag.prodata.FamilyRedbagVo;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.newredbag.SendInfo;
import com.stars.util.StringUtil;

import java.util.Calendar;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/2/14.
 */
public class NewRedbagModule extends AbstractModule {

    private final static String SELF_REDBAG_TIMES = "newredbag.self.redbag.times";


    public NewRedbagModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("新版家族红包", id, self, eventDispatcher, moduleMap);
    }

    /**
     * 上线
     */
    public void onlineForService() {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        if (familyId == 0) {
            return;
        }
        ServiceHelper.newRedbagService().online(familyId, id(), true);
    }

    @Override
    public void onReconnect() throws Throwable {
        onlineForService();
    }

    @Override
    public void onOffline() throws Throwable {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        if (familyId == 0) {
            return;
        }
        ServiceHelper.newRedbagService().offlineOrExitFamily(familyId, id());
    }

    /**
     * 获得红包
     * @param redbagId
     */
    public void add(int redbagId, int count) {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        if (familyId == 0) {
            warn("familyred_tips_cantgetred");
            return;
        }
        RoleModule roleModule = module(MConst.Role);
        ServiceHelper.newRedbagService().add(familyId, id(), redbagId, count,
                roleModule.getRoleRow().getName(),
                roleModule.getRoleRow().getJobId());
    }

    /**
     * 发红包
     * @param redbagId
     * @param padding
     * @param count
     */
    public void sendRedbag(int redbagId, int padding, int count) {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        FamilyRedbagVo redbagVo = NewRedbagManager.getFamilyRedbagVo(redbagId);

        check(padding < 0, "common.parameter.error");
        check(padding > NewRedbagManager.MAX_PADDING, "common.parameter.error");
        check(redbagVo == null, "common.parameter.error");
        check(redbagVo.getMinCount() > count, "common.parameter.error");
        check(redbagVo.getMaxCount() < count, "common.parameter.error");
        check(familyId == 0, "newredbag.not.family.memeber");
        check(BonfireActivityFlow.getState() == BonfireActivityFlow.OPEN &&
                redbagVo.getType() != NewRedbagManager.REDBAG_TYPE_SELF, "familyred_tips_autosendred");

        int times = context().recordMap().getInt(SELF_REDBAG_TIMES, 0);
        if (redbagVo.getType() == NewRedbagManager.REDBAG_TYPE_SELF) {
            // 自定义红包
            check(times >= NewRedbagManager.SELF_COUNT_MAX, "newredbag.self.max");
        }

        int value = redbagVo.getItemValue();
        if (padding > 0) {
            ToolModule toolModule = module(MConst.Tool);
            // 先扣元宝，发送失败在加回来
            check(!toolModule.deleteAndSend(1, padding, EventType.FAMILY_NEW_REDBAG.getCode()), "common.resource.lack");
            if (redbagVo.getItemId() == 3) {
                value += DataManager.getCommConfig("family_goldvaluemoney", 1000) * padding;
            } else {
                value += padding;
            }
        }

        if (redbagVo.getType() == NewRedbagManager.REDBAG_TYPE_SELF) {
            context().recordMap().setInt(SELF_REDBAG_TIMES, times + 1);
        }

        RoleModule roleModule = module(MConst.Role);
        SendInfo info = new SendInfo();
        info.setRoleId(id());
        info.setRedbagId(redbagId);
        info.setFamilyId(familyId);
        info.setCount(count);
        info.setItemId(redbagVo.getItemId());
        info.setSelf(redbagVo.getType() == NewRedbagManager.REDBAG_TYPE_SELF);
        info.setValue(value);
        info.setPadding(padding);
        info.setRoleName(roleModule.getRoleRow().getName());
        info.setJobId(roleModule.getRoleRow().getJobId());

        ServiceHelper.newRedbagService().send(info);
    }

    /**
     * 抢红包
     * @param uniqueKey
     */
    public void get(String uniqueKey) {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        check(familyId <= 0, "newredbag.not.family.memeber");
        check(StringUtil.isEmpty(uniqueKey), "common.parameter.error");

        RoleModule roleModule = module(MConst.Role);
        String roleName = roleModule.getRoleRow().getName();
        int jobId = roleModule.getRoleRow().getJobId();
        ServiceHelper.newRedbagService().get(id(), roleName, jobId, familyId, uniqueKey);
    }

    /**
     * 打开红包主界面
     */
    public void viewMain() {
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        check(familyId == 0, "newredbag.not.family.memeber");
        int count = context().recordMap().getInt(SELF_REDBAG_TIMES, 0);
        int configCount = DataManager.getCommConfig("family_freecount", 10);
        ServiceHelper.newRedbagService().viewMain(familyId, id(), configCount - count);
    }

    /**
     * 红包记录
     * @param index
     */
    public void redbagRecord(int index) {
        check(index < 0, "common.parameter.error");
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        check(familyId == 0, "newredbag.not.family.memeber");
        ServiceHelper.newRedbagService().record(id(), familyId, index);
    }

    /**
     * 红包记录详细信息
     * @param redbagKey
     */
    public void recordDetail(String redbagKey) {
        check(StringUtil.isEmpty(redbagKey), "common.parameter.error");
        long familyId = ServiceHelper.familyRoleService().getFamilyId(id());
        ServiceHelper.newRedbagService().recordDetail(id(), familyId, redbagKey);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        context().recordMap().setInt(SELF_REDBAG_TIMES, 0);
    }

    /**
     * 加入或者退出家族
     * @param familyId
     * @param prevFamilyId
     */
    public void updateFamilyAuth(long familyId, long prevFamilyId) {
        if (familyId == prevFamilyId) {
            return;
        }
        ServiceHelper.newRedbagService().updateFamilyAuth(id(), familyId, prevFamilyId);
    }
}
