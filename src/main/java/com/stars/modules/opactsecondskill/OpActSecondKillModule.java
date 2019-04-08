package com.stars.modules.opactsecondskill;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.opactsecondskill.packet.ClientOpActSecondKill;
import com.stars.modules.opactsecondskill.prodata.SecKillVo;
import com.stars.modules.opactsecondskill.userdata.RoleSecondSkillPo;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.push.conditionparser.CondUtils;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class OpActSecondKillModule extends AbstractModule implements OpActivityModule {

    //    private int curActivityId;
//    private long validity;
    private RoleSecondSkillPo roleSecondSkillPo;
    private int itemSelect = 0;
    private boolean isOpen = false;


    public OpActSecondKillModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.OpActSecondKill, id, self, eventDispatcher, moduleMap);
    }

    @Override
    protected <T extends Module> T module(String moduleName) {
        return super.module(moduleName);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from rolesecondkill where roleid = " + id();
        roleSecondSkillPo = DBUtil.queryBean(DBUtil.DB_USER, RoleSecondSkillPo.class, sql);
//        if(roleSecondSkillPo == null || roleSecondSkillPo.getRecordMap().size() <= 0){  //登陆模块数据还没初始化，部分条件判断不了
//            initRoleData();
//        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        super.onCreation(name, account);
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        onOpActReset();
    }

    private void onOpActReset() {
        if (roleSecondSkillPo == null) return;
        if (roleSecondSkillPo.getResetTimeStamp() != 0L && roleSecondSkillPo.getResetTimeStamp() < System.currentTimeMillis()) {
            roleSecondSkillPo.reset();
            itemSelect = 0;
            context().update(roleSecondSkillPo);
        }
        if (roleSecondSkillPo.getResetTimeStamp() == 0L && !roleSecondSkillPo.getRecordMap().isEmpty()) {
            roleSecondSkillPo.reset();
            itemSelect = 0;
            context().update(roleSecondSkillPo);
        }
    }


    @Override
    public void onSyncData() throws Throwable {
        if (roleSecondSkillPo == null || roleSecondSkillPo.getRecordMap().size() <= 0)
            initRoleData();
//        onOpActReset();
        viewPushInfo();
        signCalRedPoint();
    }

    @Override
    public void onTimingExecute() {
        if (!isOpenActivity()) {
            isOpen = false;
            onOpActReset();
        }
        if (isOpen) {
            if (roleSecondSkillPo == null) {
                isOpen = false;
            }
            if (roleSecondSkillPo != null && roleSecondSkillPo.getResetTimeStamp() == 0L) {
                initRoleData();
                roleSecondSkillPo.setResetTimeStamp(OpActSecondKillManager.stopTimeStamp);
            }
            if (isOpen) {
                List<SecKillVo> list = getCurrentPushData();
                if (StringUtil.isEmpty(list)) {
                    LogUtil.info("找不到玩家当前推送档位数据|roleid:{}", id());
                    isOpen = false;
                }
            }
            if (!isOpen) {
                ClientOpActSecondKill client = new ClientOpActSecondKill();
                client.setSubType(ClientOpActSecondKill.RESP_VIEW);
                client.setIsClose((byte) 1);
                send(client);
            }
        } else {
            if (!isOpen) {
                ClientOpActSecondKill client = new ClientOpActSecondKill();
                client.setSubType(ClientOpActSecondKill.RESP_VIEW);
                client.setIsClose((byte) 1);
                send(client);
            }
            if (roleSecondSkillPo != null) {
                roleSecondSkillPo.setResetTimeStamp(0L);
            }
        }
    }

    @Override
    public int getCurShowActivityId() {
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        return 0;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        if (isOpenActivity() && roleSecondSkillPo != null && StringUtil.isNotEmpty(getCurrentPushData())) {
            List<SecKillVo> list = getCurrentPushData();
            if (StringUtil.isNotEmpty(list)) {
                SecKillVo secKillVo = list.get(itemSelect);
                ToolModule toolModule = (ToolModule) module(MConst.Tool);
                int reqGold = secKillVo.getNowCost();
                if (secKillVo.getRechargeValue() <= roleSecondSkillPo.getTotalPay()
                        && reqGold > 0 && toolModule.contains(ToolManager.GOLD, reqGold)) {
                    builder.append("+");
                }
            }
        }
        redPointMap.put(RedPointConst.SECOND_KILL,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    public boolean isOpenActivity() {
        ForeShowModule foreShowModule = module(MConst.ForeShow);
        if (!foreShowModule.isOpen(ForeShowConst.SECOND_KILL))
            return false;
        if (!OpActSecondKillManager.isOpActSecKillOpen) {
            return false;
        }
//        validity = OpActSecondKillManager.stopTimeStamp;
        isOpen = true;
        return true;
//        curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_SecondKill);
//        if (curActivityId == -1) {
//            return false;
//        }
//        OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
//        OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
//        if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap())) {
//            ActOpenTimeBase openTimeBase = vo.getActOpenTimeBase();
//            if (!(openTimeBase instanceof ActOpenTime5)) {
//                return false;
//            }
//            ActOpenTime5 openTime5 = (ActOpenTime5) openTimeBase;
//            // 有效时间
//            validity = openTime5.getEndDate().getTime();
//            isOpen = true;
//            return  true;
//        }
    }

    public void signCalRedPoint() {
        signCalRedPoint(MConst.OpActSecondKill, RedPointConst.SECOND_KILL);
    }

    /**
     * 是否在活动有效时间内
     *
     * @return
     */
    public boolean isEffectiveTime() {
        return false;
//        OperateActVo actVo = OperateActivityManager.getOperateActVo(curActivityId);
//        if (actVo == null) {
//            return false;
//        }
//        ActOpenTimeBase openTimeBase = actVo.getActOpenTimeBase();
//        if (!(openTimeBase instanceof ActOpenTime5)) {
//            return false;
//        }
//        ActOpenTime5 openTime5 = (ActOpenTime5) openTimeBase;
//        // 有效时间
//        validity = openTime5.getEndDate().getTime();
//        return DateUtil.isBetween(new Date(), openTime5.getStartDate(), openTime5.getEndDate());
    }

    public void onEvent(Event event) {
        if (event instanceof VipChargeEvent) {
            if (roleSecondSkillPo != null && roleSecondSkillPo.getRecordMap().size() > 0) {
                int addMoney = ((VipChargeEvent) event).getMoney();
                roleSecondSkillPo.setTotalPay(roleSecondSkillPo.getTotalPay() + addMoney);
                context().update(roleSecondSkillPo);
                viewPushInfo();
            }
        }
//        } else if (event instanceof OpActSecondKillStateEvent) {
//            onOpActReset();
//        }

        signCalRedPoint();
    }

    /**
     * 检查物品是否可以推送
     *
     * @param vo
     * @return
     */
    public boolean satisfyCondition(SecKillVo vo) {
        if (vo == null) {
            return false;
        }
        if (!vo.matchPushTime())
            return false;
        return CondUtils.isTrue(vo.getCondChecker(), moduleMap());
    }

    public void initRoleData() {
        if (!isOpenActivity())
            return;
        Map<Integer, SecKillVo> checkGroupSecKillMap = OpActSecondKillManager.getCheckGroupSecKillMap();
        int groupId = 0; //推送的groupid
        for (SecKillVo secKillVo : checkGroupSecKillMap.values()) {
            if (!secKillVo.matchPushTime())
                continue;
            if (!satisfyCondition(secKillVo))
                continue;
            groupId = secKillVo.getGroup();
            break;
        }
        if (groupId == 0) //没有符合的推送组
            return;
        if (roleSecondSkillPo == null) {
            roleSecondSkillPo = new RoleSecondSkillPo(id());
            context().insert(roleSecondSkillPo);
        }

        int maxPushCharge = 0;
        List<SecKillVo> secKillVoList = OpActSecondKillManager.getSecKillVoListByGroup(groupId);
        for (SecKillVo secKillVo : secKillVoList) {
            if (!secKillVo.matchPushTime())
                continue;
            if (!satisfyCondition(secKillVo))
                continue;
            if (secKillVo.getGroupCondition() == (byte) 1) //策划调整，作为组条件这条不计入可推送
                continue;
            roleSecondSkillPo.getRecordMap().put(secKillVo.getId(), 0);
            if (secKillVo.getRechargeValue() > maxPushCharge)
                roleSecondSkillPo.setMaxPushCharge(secKillVo.getRechargeValue());
        }
        context().update(roleSecondSkillPo);
        viewPushInfo();
    }

    public void viewPushInfo() {
        if (!isOpenActivity())
            return;
        if (roleSecondSkillPo == null) {
            LogUtil.info("玩家没有推送限时秒杀数据|roleid:{}", id());
            return;
        }
        List<SecKillVo> list = getCurrentPushData();
        if (StringUtil.isEmpty(list)) {
            LogUtil.info("找不到玩家当前推送档位数据|roleid:{}", id());
            ClientOpActSecondKill client = new ClientOpActSecondKill();
            client.setSubType(ClientOpActSecondKill.RESP_VIEW);
            client.setIsClose((byte) 1);
            send(client);
            return;
        }
        byte canChange = 0; //是否换商品
        if (list.size() > 1)
            canChange = (byte) 1;
        SecKillVo secKillVo = list.get(itemSelect);
        long endTimeStamp = OpActSecondKillManager.stopTimeStamp;
//        if (OpActSecondKillManager.isDailyReset()) {  //获得日重置的时间 今日23:59：59
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.HOUR_OF_DAY, 23);
//            calendar.set(Calendar.MINUTE, 59);
//            calendar.set(Calendar.SECOND, 59);
//            endTimeStamp = calendar.getTimeInMillis();
//        } else {
//            endTimeStamp = validity;
//        }
        long countDownTime = endTimeStamp - now();
        int needPay = secKillVo.getRechargeValue() - roleSecondSkillPo.getTotalPay();
        ClientOpActSecondKill client = new ClientOpActSecondKill();
        client.setSubType(ClientOpActSecondKill.RESP_VIEW);
        client.setCountDownTime(countDownTime / 1000); //倒计时秒数
        client.setNextNeedPay(needPay);
        client.setCurrentPay(roleSecondSkillPo.getTotalPay());
        client.setSecKillVo(secKillVo);
        client.setCanChange(canChange);
        send(client);
    }

    /**
     * 查看下一个商品
     */
    public void changeNextItem() {
        if (roleSecondSkillPo == null) {
            LogUtil.info("玩家没有推送限时秒杀数据|roleid:{}", id());
            return;
        }
        List<SecKillVo> list = getCurrentPushData();
        if (StringUtil.isEmpty(list)) {
            LogUtil.info("找不到玩家当前推送档位数据|roleid:{}", id());
            return;
        }
        if (itemSelect >= (list.size() - 1)) {
            itemSelect = 0;
        } else {
            itemSelect++;
        }
        signCalRedPoint();
        viewPushInfo();
    }

    public void buyItem(int id) {
        if (!isOpenActivity())
            return;
        if (roleSecondSkillPo == null) {
            LogUtil.info("玩家没有推送限时秒杀数据|roleid:{}", id());
            return;
        }
        List<SecKillVo> list = getCurrentPushData();
        if (StringUtil.isEmpty(list)) {
            LogUtil.info("找不到玩家当前推送档位数据|roleid:{}", id());
            return;
        }
        SecKillVo secKillVo = OpActSecondKillManager.getSecKillVoById(id);
        if (secKillVo == null)
            return;
        if (!list.contains(secKillVo)) {
            LogUtil.info("目前限时秒杀推送没有该数据|roleid:{}|id:{}", id(), id);
            return;
        }
        if (secKillVo.getRechargeValue() > roleSecondSkillPo.getTotalPay()) {
            warn("common_tips_nogold");
            return;
        }
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        int reqGold = secKillVo.getNowCost();
        if (reqGold < 0 || !toolModule.contains(ToolManager.GOLD, reqGold)) {
            warn("common_tips_nogold");
            return;
        }
        roleSecondSkillPo.setLastBuyCharge(secKillVo.getRechargeValue()); //记录最新购买档位
        roleSecondSkillPo.getRecordMap().put(secKillVo.getId(), 1); //购买的记录
        context().update(roleSecondSkillPo);
        //先删除元宝
        toolModule.deleteAndSend(ToolManager.GOLD, reqGold, EventType.SEC_SKILL_BUY.getCode());
        //获得商品
        toolModule.addAndSend(secKillVo.getItemMap(), EventType.SEC_SKILL_BUY.getCode());
        ServerLogModule serverLogModule = module(MConst.ServerLog);
        serverLogModule.log_secendKill(roleSecondSkillPo.getTotalPay(), reqGold, secKillVo.getItemMap());
        send(new ClientText("common_tips_buysuc"));
        itemSelect = 0;
        viewPushInfo(); //查看下一档位商品
        signCalRedPoint();
    }

    //获得玩家当前推送档位的数据
    private List<SecKillVo> getCurrentPushData() {
        if (roleSecondSkillPo == null)
            return null;
        TreeMap<Integer, List<SecKillVo>> secKillVoTreeMap = new TreeMap<>();
        for (Integer id : roleSecondSkillPo.getRecordMap().keySet()) { //遍历玩家推送到的数据
            SecKillVo secKillVo = OpActSecondKillManager.getSecKillVoById(id);
            if (secKillVo == null)
                continue;
            int rechargerValue = secKillVo.getRechargeValue();
            if (roleSecondSkillPo.getLastBuyCharge() >= rechargerValue) //充值档位已经失效
                continue;
            List<SecKillVo> secKillVoList = secKillVoTreeMap.get(rechargerValue);
            if (secKillVoList == null) {
                secKillVoList = new ArrayList<>();
            }
            secKillVoList.add(secKillVo);
            secKillVoTreeMap.put(rechargerValue, secKillVoList);
        }
        for (List<SecKillVo> list : secKillVoTreeMap.values()) {
            Collections.sort(list); //排序，按优先级排序
            return list;  //返回当前未完成
        }
        return null;
    }

}
