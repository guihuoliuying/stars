package com.stars.modules.title;

import com.stars.AccountRow;
import com.stars.core.attr.Attribute;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.AccountRowAware;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.title.event.TitleAchieveEvent;
import com.stars.modules.title.packet.ClientTitle;
import com.stars.modules.title.prodata.TitleVo;
import com.stars.modules.title.summary.TitleSummaryComponentImpl;
import com.stars.modules.title.userdata.RoleTitle;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by liuyuheng on 2016/7/21.
 */
public class TitleModule extends AbstractModule implements AccountRowAware {
    private Map<Integer, RoleTitle> roleTitleMap = new HashMap<>();
    private Map<Byte, Integer> topFightScoreMap = new HashMap<>();// 已获得 每个类别最高战力称号
    public Map<Integer, TitleVo> filterTitleVoMap = new HashMap<>();// 过滤后的显示的称号产品数据 titleId-vo
    private Set<Integer> titleIds;
    private AccountRow accountRow;

    public TitleModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("称号", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Exception {
        String sql = "select * from `roletitle` where roleid=" + id();
        roleTitleMap = DBUtil.queryMap(DBUtil.DB_USER, "titleid", RoleTitle.class, sql);
        Map<Integer, RoleTitle> changeMap = new HashMap<>(); //限时称号加expiretime的处理，以前是获得时间用产品数据算
        for (RoleTitle roleTitle : roleTitleMap.values()) {
            TitleVo titleVo = TitleManager.getTitleVo(roleTitle.getTitleId());
            if (titleVo.getUseTimeType() == TitleManager.TIME_TYPE_SPELLTIME) { //限时称号
                long expireTime = roleTitle.getGainTime() + titleVo.getTime();
                if (roleTitle.getExpireTime() == 0L && expireTime > now()) { //未过期,新加字段，过期字段默认0L
                    roleTitle.setExpireTime(expireTime);
                    changeMap.put(roleTitle.getTitleId(), roleTitle);
                    context().update(roleTitle);
                }
            }
            updateTopTitle(roleTitle.getTitleId());
        }
        if (StringUtil.isNotEmpty(changeMap)) { //有变化的
            roleTitleMap.putAll(changeMap);
        }
    }

    @Override
    public void onInit(boolean isCreation) {
        useTimeCheck();
        updateTitleAttr();
        titleIds = new HashSet<>();
        /**
         * 过滤本角色无法显示的称号
         */
        refreshShowTitle();
    }

    @Override
    public void onReconnect() throws Throwable {
        useTimeCheck();
        updateTitleAttr();
    }

    @Override
    public void onSyncData() {
        fireTitleAchieveEvent(); //登陆触发成就达成检测
    }

    @Override
    public void onTimingExecute() {
        Map<Integer, RoleTitle> changeMap = useTimeCheck();
        // 下发
        if (!changeMap.isEmpty()) {
            sendTitleChange(changeMap);
            for (RoleTitle roleTitle : roleTitleMap.values()) {
                updateTopTitle(roleTitle.getTitleId());
            }
            updateTitleAttrWithSend();
        }
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put("title", new TitleSummaryComponentImpl(new HashMap<Byte, Integer>(topFightScoreMap)));
        }
    }

    /**
     * 下发所有称号数据
     */
    public void sendAllTitleData() {

        ClientTitle clientTitle = new ClientTitle(ClientTitle.SEND_ALL_TITLE);
        clientTitle.setTitleVoMap(filterTitleVoMap);
        clientTitle.setRoleTitleMap(roleTitleMap);
        send(clientTitle);
    }

    /**
     * 刷新展示的标题
     */
    public void refreshShowTitle() {
        filterTitleVoMap.clear();
        RoleModule roleModule = module(MConst.Role);
        for (Map.Entry<Integer, TitleVo> entry : TitleManager.titleVoMap.entrySet()) {
            if (entry.getValue().checkCondition(roleModule.getLevel(), accountRow.getVipLevel()) || roleTitleMap.containsKey(entry.getKey()))
                filterTitleVoMap.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * 下发称号状态改变
     *
     * @param roleTitleMap
     */
    public void sendTitleChange(Map<Integer, RoleTitle> roleTitleMap) {
        ClientTitle clientTitle = new ClientTitle(ClientTitle.UPDATE_TITLE);
        clientTitle.setRoleTitleMap(roleTitleMap);
        send(clientTitle);
    }

    /**
     * 激活称号(对外接口)
     *
     * @param titleId
     * @param count
     */
    public void activeTitle(int titleId, int count) {
        TitleVo titleVo = TitleManager.getTitleVo(titleId);
        if (titleVo == null) {
            return;
        }
        if (!activeTitle(titleVo, count)) {
            return;
        }
        if (updateTopTitle(titleId)) {
            updateTitleAttrWithSend();

//            try {
//                ServiceHelper.summaryService().updateSummaryComponent(id(), new TitleSummaryComponentImpl(topFightScoreMap));
//            } catch (Exception e) {
//                LogUtil.error("", e);
//            }
            context().markUpdatedSummaryComponent(MConst.Title);
        }
        if (titleIds == null) {
            titleIds = new HashSet<>();
        }
        titleIds.add(titleId);
        signCalRedPoint(MConst.Title, RedPointConst.TITLE);
        // 下发
        Map<Integer, RoleTitle> changeMap = new HashMap<>();
        changeMap.put(titleId, roleTitleMap.get(titleId));
        sendTitleChange(changeMap);
        /**
         * 刷新显示的称号
         */
        refreshShowTitle();
        sendAllTitleData();
        fireTitleAchieveEvent();
    }

    public void newTitle(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, titleIds, RedPointConst.TITLE);
        titleIds.clear();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.TITLE))) {
            newTitle(redPointMap);
        }
    }

    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Integer> list, int redPointConst) {
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }

    /**
     * 激活称号
     *
     * @param titleVo
     * @param count
     * @return
     */
    private boolean activeTitle(TitleVo titleVo, int count) {
        RoleTitle roleTitle = roleTitleMap.get(titleVo.getTitleId());
        if (roleTitle != null && titleVo.getUseTimeType() != TitleManager.TIME_TYPE_SPELLTIME) {// 已激活
            int itemId = titleVo.getItemId();
            if (itemId == 0) return false;
            ToolModule tool = module(MConst.Tool);
            tool.addAndSend(itemId, titleVo.getItemCount(), EventType.TITLE_RESOLVE.getCode());
            String tmp = String.format(DataManager.getGametext("title_resolve_desc"), DataManager.getGametext(titleVo.getName()), titleVo.getItemCount(),
                    DataManager.getGametext(ToolManager.getItemName(itemId)));
            warn(tmp);
            return false;
        }
        if (titleVo.getUseTimeType() == TitleManager.TIME_TYPE_SPELLTIME) {
            return activeTimeLimitTitle(titleVo, count);
        }
        byte status = TitleManager.TITLE_STATUS_AVAILABLE;
        if (titleVo.getUseTimeType() == TitleManager.TIME_TYPE_TIMEOVER
                && System.currentTimeMillis() > titleVo.getTime()) {
            status = TitleManager.TITLE_STATUS_OUTDATE;
        }
        roleTitle = new RoleTitle(id(), titleVo.getTitleId(), status);
        roleTitleMap.put(roleTitle.getTitleId(), roleTitle);
        context().insert(roleTitle);
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (roleModule.getTitleId() == 0) {// 当前没有使用称号,则装上
            roleModule.updateTitleId(titleVo.getTitleId());
        }
        return true;
    }

    /**
     * 激活限时称号
     *
     * @param titleVo
     * @param count
     * @return
     */
    private boolean activeTimeLimitTitle(TitleVo titleVo, int count) {
        RoleTitle roleTitle = roleTitleMap.get(titleVo.getTitleId());
        if (roleTitle == null) {// 未拥有
            roleTitle = new RoleTitle(id(), titleVo.getTitleId(), TitleManager.TITLE_STATUS_AVAILABLE);
            context().insert(roleTitle);
        }
        long addUseTime = titleVo.getTime() * count; //可延长时间
        if (roleTitle.getExpireTime() < now()) { //从现在开始计时
            roleTitle.setExpireTime(now() + addUseTime);
        } else { //已经拥有，增加时长
            long expireTime = roleTitle.getExpireTime() + addUseTime;
            roleTitle.setExpireTime(expireTime);
        }
        roleTitle.setStatus(TitleManager.TITLE_STATUS_AVAILABLE);
        context().update(roleTitle);
        roleTitleMap.put(roleTitle.getTitleId(), roleTitle);
        return true;
    }

    /**
     * 更换称号
     *
     * @param titleId 为0时表示卸下称号
     */
    public void changeTitle(int titleId) {
        if (titleId != 0) {
            TitleVo titleVo = TitleManager.getTitleVo(titleId);
            if (titleVo == null) {
                return;
            }
            RoleTitle roleTitle = roleTitleMap.get(titleVo.getTitleId());
            // 未激活 || 不可使用状态
            if (roleTitle == null || roleTitle.getStatus() != TitleManager.TITLE_STATUS_AVAILABLE) {
                return;
            }
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updateTitleId(titleId);
    }

    /**
     * 称号使用时间检查
     */
    public Map<Integer, RoleTitle> useTimeCheck() {
        Map<Integer, RoleTitle> changeMap = new HashMap<>();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        for (RoleTitle roleTitle : roleTitleMap.values()) {
            TitleVo titleVo = TitleManager.getTitleVo(roleTitle.getTitleId());
            if (titleVo.getUseTimeType() == TitleManager.TIME_TYPE_FOREVER) {// 永久使用
                continue;
            }
            long overTime = 0;// 过期时间点
            switch (titleVo.getUseTimeType()) {
                case TitleManager.TIME_TYPE_SPELLTIME:
                    overTime = roleTitle.getExpireTime();
                    break;
                case TitleManager.TIME_TYPE_TIMEOVER:
                    overTime = titleVo.getTime();
                    break;
                default:
                    break;
            }
            if (System.currentTimeMillis() > overTime) {// 已过期
                if (topFightScoreMap.get(titleVo.getType()) == roleTitle.getTitleId()) {
                    /**
                     * 寻找同类型战力第二大的称号
                     */
                    List<RoleTitle> roleTitleTmps = new ArrayList<>();
                    for (RoleTitle roleTitleTmp : roleTitleMap.values()) {
                        TitleVo titleVoTmp = roleTitleTmp.getTitleVo();
                        if (titleVoTmp.getType() == titleVo.getType()) {
                            if (isAvaliable(roleTitleTmp)) {
                                roleTitleTmps.add(roleTitleTmp);
                            }
                        }
                    }
                    if (roleTitleTmps.size() == 0) {
                        topFightScoreMap.put(titleVo.getType(), 0);
                    } else {
                        Collections.sort(roleTitleTmps, new Comparator<RoleTitle>() {
                            @Override
                            public int compare(RoleTitle o1, RoleTitle o2) {
                                return o2.getTitleVo().getFightPower() - o1.getTitleVo().getFightPower();
                            }
                        });
                        topFightScoreMap.put(titleVo.getType(), roleTitleTmps.get(0).getTitleId());
                    }
                }
                context().delete(roleTitle);
                roleTitle.setStatus(TitleManager.TITLE_STATUS_OUTDATE);
                changeMap.put(titleVo.getTitleId(), roleTitle);
                if (roleTitle.getTitleId() == roleModule.getTitleId()) {// 当前使用的称号过期
                    roleModule.updateTitleId(0);
                }
            }
        }
        if (!changeMap.keySet().isEmpty()) {
            for (int titleId : changeMap.keySet()) {
                roleTitleMap.remove(titleId);
            }
        }
        return changeMap;
    }

    /**
     * 判断称号是否是有效的
     * @param roleTitle
     * @return
     */
    public boolean isAvaliable(RoleTitle roleTitle) {
        TitleVo titleVo = roleTitle.getTitleVo();
        if (titleVo.getUseTimeType() == TitleManager.TIME_TYPE_FOREVER) {// 永久使用
            return true;
        }
        long overTime = 0;// 过期时间点
        switch (titleVo.getUseTimeType()) {
            case TitleManager.TIME_TYPE_SPELLTIME:
                overTime = roleTitle.getExpireTime();
                break;
            case TitleManager.TIME_TYPE_TIMEOVER:
                overTime = titleVo.getTime();
                break;
            default:
                break;
        }
        if (System.currentTimeMillis() > overTime) {// 已过期
            return false;
        } else {
            return true;
        }

    }

    /**
     * 更新已获得 每个类别最高战力称号map
     *
     * @param titleId
     */
    public boolean updateTopTitle(int titleId) {
        TitleVo titleVo = TitleManager.getTitleVo(titleId);
        byte type = titleVo.getType();
        TitleVo nowTop = TitleManager.getTitleVo(topFightScoreMap.get(type) == null ? 0 : topFightScoreMap.get(type));
        if (nowTop != null && nowTop.getFightPower() > titleVo.getFightPower()) {
            return false;
        }
        topFightScoreMap.put(type, titleVo.getTitleId());
        return true;
    }

    /**
     * 更新称号增加角色属性、战力
     */
    public void updateTitleAttr() {
        Attribute attribute = new Attribute();
        int totalFightScore = 0;
        TitleVo titleVo;
        for (Map.Entry<Byte, Integer> entry : topFightScoreMap.entrySet()) {
            if (entry.getValue() == 0) {
                continue;
            }
            titleVo = TitleManager.getTitleVo(entry.getValue());
            attribute.addAttribute(titleVo.getAttribute());
            totalFightScore = totalFightScore + titleVo.getFightPower();
        }
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_TITLE, attribute);
        // 更新战力
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_TITLE, totalFightScore);
    }

    public void updateTitleAttrWithSend() {
        updateTitleAttr();
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    /**
     * 称号战力日志
     *
     * @return
     */
    public String log_title_fight() {
        StringBuffer fightingStr = new StringBuffer();
        fightingStr.append("title_kind@fight: ");
        StringBuffer titleStr = new StringBuffer();
        int title = 0;
        Integer fighting = 0;
        TitleVo titleVo = null;
        byte tempType = 0;
        Map<Byte, Integer> tempMap = new HashMap<Byte, Integer>();
        for (RoleTitle roleTitle : roleTitleMap.values()) {
            title = roleTitle.getTitleId();
            if (roleTitle.getStatus() != TitleManager.TITLE_STATUS_AVAILABLE) continue;
            titleVo = TitleManager.getTitleVo(title);
            tempType = titleVo.getType();
            fighting = tempMap.get(tempType);
            if (fighting == null) {
                fighting = 0;
            }
            fighting += titleVo.getFightPower();
            tempMap.put(tempType, fighting);
        }
        for (Byte type : TitleManager.typeList) {
            fighting = tempMap.get(type);
            if (fighting == null) {
                fighting = 0;
            }
            if (titleStr.length() == 0) {
                titleStr.append(type).append("@").append(fighting);
            } else {
                titleStr.append("&").append(type).append("@").append(fighting);
            }
        }
        fightingStr.append(titleStr);
        return fightingStr.toString();
    }

    /**
     * 称号获得状态日志
     *
     * @return
     */
    public String log_title_state() {
        StringBuffer stateStr = new StringBuffer();
        stateStr.append("title_code@state: ");
        StringBuffer titleStr = new StringBuffer();
        Iterator<Integer> iterator = TitleManager.titleVoMap.keySet().iterator();
        int title = 0;
        byte state = 0;
        for (; iterator.hasNext(); ) {
            title = iterator.next();
            state = 0;
            if (roleTitleMap.containsKey(title)) {
                state = 1;
            }
            if (titleStr.length() == 0) {
                titleStr.append(title).append("@").append(state);
            } else {
                titleStr.append("&").append(title).append("@").append(state);
            }
        }
        stateStr.append(titleStr);
        return stateStr.toString();
    }

    public String makeFsStr() {
        StringBuilder sb = new StringBuilder();
        for (Byte type : TitleManager.typeList) {
            int fightPower = 0;
            Integer titileId = topFightScoreMap.get(type);
            if (titileId != null) {
                TitleVo titleVo = TitleManager.getTitleVo(titileId);
                if (titleVo != null) {
                    fightPower = titleVo.getFightPower();
                }
            }

            switch (type) {
                case 1:
                    sb.append("title_strength:");
                    break;
                case 2:
                    sb.append("title_vip:");
                    break;
                case 3:
                    sb.append("title_activity:");
                    break;
                case 4:
                    sb.append("title_marry:");
                    break;
            }
            sb.append(fightPower).append("#");
        }
//        for (Map.Entry<Byte, Integer> entry : topFightScoreMap.entrySet()) {
//            if (entry.getValue() != 0) {
//                TitleVo vo = TitleManager.getTitleVo(entry.getValue());
//                if (vo != null) {
//                    switch (entry.getKey()) {
//                        case 1:
//                            sb.append("title_strength:");
//                            break;
//                        case 2:
//                            sb.append("title_vip:");
//                            break;
//                        case 3:
//                            sb.append("title_activity:");
//                            break;
//                        case 4:
//                            sb.append("title_marry:");
//                            break;
//                    }
//                    sb.append(vo.getFightPower()).append("#");
//                }
//            }
//        }
        return sb.toString();
    }

    private void fireTitleAchieveEvent() {
        if (StringUtil.isEmpty(roleTitleMap))
            return;
        TitleAchieveEvent event = new TitleAchieveEvent(roleTitleMap);
        eventDispatcher().fire(event);
    }


    @Override
    public void setAccountRow(AccountRow accountRow) {
        this.accountRow = accountRow;
    }
}
