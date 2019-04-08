package com.stars.modules.wordExchange;

import com.stars.core.event.Event;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.operateactivity.OpActivityModule;
import com.stars.modules.operateactivity.OperateActivityConstant;
import com.stars.modules.operateactivity.OperateActivityManager;
import com.stars.modules.operateactivity.OperateActivityModule;
import com.stars.modules.operateactivity.labeldisappear.DisappearByDays;
import com.stars.modules.operateactivity.labeldisappear.DisappearByTime;
import com.stars.modules.operateactivity.labeldisappear.LabelDisappearBase;
import com.stars.modules.operateactivity.labeldisappear.NeverDisappear;
import com.stars.modules.operateactivity.prodata.OperateActVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.wordExchange.packet.ClientWordExchange;
import com.stars.modules.wordExchange.prodata.CollectAwardVo;
import com.stars.modules.wordExchange.userdata.RoleWordExchange;
import com.stars.modules.wordExchange.vo.ExchangeVo;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by wuyuxing on 2016/11/7.
 */
public class WordExchangeModule extends AbstractModule implements OpActivityModule {

    private RoleWordExchange roleWordExchange;

    public WordExchangeModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.WordExchange, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from `rolewordexchange` where `roleid`=" + id();
        roleWordExchange = DBUtil.queryBean(DBUtil.DB_USER, RoleWordExchange.class, sql);
        if (roleWordExchange == null) {
            roleWordExchange = new RoleWordExchange(id());
            context().insert(roleWordExchange);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleWordExchange = new RoleWordExchange(id());
        context().insert(roleWordExchange);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        if (getCurShowActivityId() != -1) {
            signCalRedPoint(MConst.WordExchange, RedPointConst.WORD_EXCHANGE);
        }
        if (roleWordExchange == null || StringUtil.isEmpty(roleWordExchange.getRecordMap())) return;
        CollectAwardVo collectAwardVo;
        List<Integer> removeList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : roleWordExchange.getRecordMap().entrySet()) {
            collectAwardVo = WordExchangeManager.getCollectAwardVo(entry.getKey());
            if (collectAwardVo == null) {//检测已经废弃的id，并移除无用的用户数据
                removeList.add(entry.getKey());
            }
        }

        if (StringUtil.isEmpty(removeList)) return;
        for (int key : removeList) {
            roleWordExchange.getRecordMap().remove(key);
        }
        context().update(roleWordExchange);
    }


    @Override
    public void onSyncData() throws Throwable {

    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.WORD_EXCHANGE)) {
            ToolModule toolModule = module(MConst.Tool);
            for (CollectAwardVo collectAwardVo : WordExchangeManager.COLLECT_AWARD_MAP.values()) {
                Map<Integer, Integer> reqItemMap = collectAwardVo.getReqItemMap();
                if (getCanExchangeCount(collectAwardVo) > 0 && toolModule.contains(reqItemMap)) {
                    redPointMap.put(RedPointConst.WORD_EXCHANGE, "");
                    break;
                }
                redPointMap.put(RedPointConst.WORD_EXCHANGE, null);
            }
        }
    }

    @Override
    public void onDailyReset(Calendar now, boolean isLogin) throws Throwable {
        if (roleWordExchange == null || StringUtil.isEmpty(roleWordExchange.getRecordMap())) return;
        CollectAwardVo collectAwardVo;
        List<Integer> removeList = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : roleWordExchange.getRecordMap().entrySet()) {
            collectAwardVo = WordExchangeManager.getCollectAwardVo(entry.getKey());
            if (collectAwardVo == null || collectAwardVo.getResetType() == CollectAwardVo.RESET_TYPE_DAILY) {
                removeList.add(entry.getKey());
            }
        }

        if (StringUtil.isEmpty(removeList)) return;
        for (int key : removeList) {
            roleWordExchange.getRecordMap().remove(key);
        }
        context().update(roleWordExchange);
    }

    @Override
    public int getCurShowActivityId() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WordExchange);
        if (curActivityId != -1) {
            OperateActVo vo = OperateActivityManager.getOperateActVo(curActivityId);
            OperateActivityModule operateActivityModule = module(MConst.OperateActivity);
            if (vo != null && operateActivityModule.isShow(vo.getRoleLimitMap())) {
                return curActivityId;
            }
        }
        return -1;
    }

    @Override
    public byte getIsShowLabel() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WordExchange);
        if (curActivityId == -1) return (byte) 0;

        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        if (operateActVo == null) return (byte) 0;

        LabelDisappearBase labelDisappearBase = operateActVo.getDisappear();
        if (labelDisappearBase == null) return (byte) 0;

        if (labelDisappearBase instanceof NeverDisappear) {
            return (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByDays) {
            int openServerDays = DataManager.getServerDays();
            int continueDays = openServerDays;
            int canContinueDays = ((DisappearByDays) labelDisappearBase).getDays();
            return continueDays > canContinueDays ? (byte) 0 : (byte) 1;
        } else if (labelDisappearBase instanceof DisappearByTime) {
            Date date = ((DisappearByTime) labelDisappearBase).getDate();
            return date.getTime() < new Date().getTime() ? (byte) 0 : (byte) 1;
        }

        return (byte) 0;
    }

    /**
     * 查看集字活动界面
     */
    public void viewMainUI() {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WordExchange);
        if (curActivityId == -1) {
            warn("集字活动已结束");
            return;
        }

        List<CollectAwardVo> list = WordExchangeManager.ActivityCollectList(curActivityId);
        OperateActVo operateActVo = OperateActivityManager.getOperateActVo(curActivityId);
        ClientWordExchange client = new ClientWordExchange(ClientWordExchange.RESP_VIEW);
        client.setVoList(getRoleVoList(list));
        client.setRuledesc(operateActVo == null ? "" : operateActVo.getRuledesc());
        send(client);
    }

    private List<ExchangeVo> getRoleVoList(List<CollectAwardVo> list) {
        if (StringUtil.isEmpty(list)) return null;
        List<ExchangeVo> voList = new ArrayList<>();
        ExchangeVo exchangeVo;
        for (CollectAwardVo vo : list) {
            if (vo == null) continue;
            exchangeVo = new ExchangeVo(vo);
            exchangeVo.setCanExchangeCount(getCanExchangeCount(vo));
            voList.add(exchangeVo);
        }
        Collections.sort(voList);
        return voList;
    }

    private int getCanExchangeCount(CollectAwardVo vo) {
        if (vo == null) return 0;
        if (roleWordExchange == null) return 0;
        int hasUsed = roleWordExchange.getExchangeTimesById(vo.getId());
        int canExchange = vo.getExchangeCount() - hasUsed;
        return canExchange < 0 ? 0 : canExchange;
    }

    /**
     * 集字兑换
     */
    public void exchange(int id, int count) {
        int curActivityId = OperateActivityManager.getCurActivityId(OperateActivityConstant.ActType_WordExchange);
        if (curActivityId == -1) {
            warn("collectaward_late");
            return;
        }
        CollectAwardVo vo = WordExchangeManager.getCollectAwardVo(id);
        if (vo == null) {
            warn("产品数据有误");
            return;
        }
        if (vo.getOperateActId() != curActivityId) {
            warn("collectaward_late");
            return;
        }

        int canExchangeCount = getCanExchangeCount(vo);
        if (canExchangeCount < count) {
            warn("collectaward_usefail");
            return;
        }

        Map<Integer, Integer> reqItems = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : vo.getReqItemMap().entrySet()) {
            reqItems.put(entry.getKey(), entry.getValue() * count);
        }

        ToolModule toolModule = module(MConst.Tool);
        if (!toolModule.deleteAndSend(reqItems, EventType.ACTIVITY_WORD_EXCHANGE.getCode())) {
            warn("collectaward_lack");
            return;
        }

        roleWordExchange.addRecord(id, count);
        context().update(roleWordExchange);

        DropModule dropModule = module(MConst.Drop);
        Map<Integer, Integer> reward = dropModule.executeDrop(vo.getDropGroup(), count, true);
        if (StringUtil.isNotEmpty(reward)) {
            toolModule.addAndSend(reward, EventType.ACTIVITY_WORD_EXCHANGE.getCode());
            send(new ClientAward(reward));//飘字提示
        }

        viewMainUI();//刷新活动界面
        signCalRedPoint(MConst.WordExchange, RedPointConst.WORD_EXCHANGE);
    }

    public void onEvent(Event event) {
        if(event instanceof AddToolEvent){
            signCalRedPoint(MConst.WordExchange, RedPointConst.WORD_EXCHANGE);
        }
    }
}
