package com.stars.modules.foreshow;


import com.stars.core.db.DBUtil;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.dungeon.DungeonManager;
import com.stars.modules.dungeon.DungeonModule;
import com.stars.modules.dungeon.prodata.DungeoninfoVo;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.foreshow.packet.ClientForeShow;
import com.stars.modules.foreshow.prodata.ForeShowVo;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponentImp;
import com.stars.modules.foreshow.userdata.ForeShowStatePo;
import com.stars.modules.foreshow.userdata.NextForeShowPo;
import com.stars.modules.role.RoleModule;
import com.stars.services.summary.SummaryComponent;

import java.util.*;

/**
 * Created by chenkeyu on 2016/10/28.
 */
public class ForeShowModule extends AbstractModule {

    private Map<String, ForeShowStatePo> foreShowStatePoMap = new HashMap<>();
    private Map<String, ForeShowStatePo> unOpenMap = new HashMap<>();
    private Map<String, ForeShowStatePo> openShowMap = new HashMap<>();
    private Map<String, ForeShowStatePo> openUnShowMap = new HashMap<>();
    private Map<String, ForeShowStatePo> map = new HashMap<>();
    private Set<Integer> itemList = new HashSet<>();
    private Map<String, String> foreShowChangeMap = new HashMap<>();
    private Set<String> bossOpenList = new HashSet<>();//好多全局变量
    private Set<String> bossOpenList0 = new HashSet<>();

    public ForeShowModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("系统预告", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        List<ForeShowStatePo> foreShowStatePoList = DBUtil.queryList(DBUtil.DB_USER, ForeShowStatePo.class, "select * from `foreshowstate` where `roleid`=" + id());
        for (ForeShowStatePo foreShowStatePo : foreShowStatePoList) {
            foreShowStatePoMap.put(foreShowStatePo.getRoleid() + "+" + foreShowStatePo.getOpenname(), foreShowStatePo);
        }

        List<ForeShowStatePo> OpenShowPoList = DBUtil.queryList(DBUtil.DB_USER, ForeShowStatePo.class, "select * from `foreshowstate` where `roleid`=" + id() + " and `openstate`=" + 1);
        for (ForeShowStatePo foreShowStatePo : OpenShowPoList) {
            openShowMap.put(foreShowStatePo.getRoleid() + "+" + foreShowStatePo.getOpenname(), foreShowStatePo);
        }
        List<ForeShowStatePo> OpenUnShowPoList = DBUtil.queryList(DBUtil.DB_USER, ForeShowStatePo.class, "select * from `foreshowstate` where `roleid`=" + id() + " and `openstate`=" + 2);
        for (ForeShowStatePo foreShowStatePo : OpenUnShowPoList) {
            openUnShowMap.put(foreShowStatePo.getRoleid() + "+" + foreShowStatePo.getOpenname(), foreShowStatePo);
        }
        List<ForeShowStatePo> unOpenPoList = DBUtil.queryList(DBUtil.DB_USER, ForeShowStatePo.class, "select * from `foreshowstate` where `roleid`=" + id() + " and `openstate`=" + 3);
        for (ForeShowStatePo foreShowStatePo : unOpenPoList) {
            unOpenMap.put(foreShowStatePo.getRoleid() + "+" + foreShowStatePo.getOpenname(), foreShowStatePo);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        foreShowStatePoMap = new HashMap<>();
        unOpenMap = new HashMap<>();
        openShowMap = new HashMap<>();
        openUnShowMap = new HashMap<>();
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        loginCheckLoseEfficacyData();
        loginForeShow();
    }

    @Override
    public void onSyncData() throws Throwable {
//        loginForeShow();
    }

    @Override
    public void onOffline() throws Throwable {
        bossOpenList.clear();
        bossOpenList0.clear();
    }

    /**
     * 判断用户等级是否足够
     *
     * @param params
     * @return
     */
    private boolean roleLevel(int params) {
        RoleModule roleModule = (RoleModule) moduleMap().get(MConst.Role);
        int level = roleModule.getRoleRow().getLevel();
        if (params <= level)
            return true;
        return false;
    }

    /**
     * 判断任务是否完成
     *
     * @param params
     * @return
     */
    private boolean roleTask(int params) {

        return false;
    }

    private boolean roleAchievement(int params) {

        return false;
    }

    /**
     * 判断是否通关
     *
     * @param params
     * @return
     */
    private boolean roleDungeon(int params) {
        DungeonModule dungeonModule = (DungeonModule) moduleMap().get(MConst.Dungeon);
        if (dungeonModule.isPassDungeon(params))
            return true;
        return false;
    }

    private boolean roleUseTool(int params) {
        if (itemList.contains(params)) {
            return true;
        }
        return false;
    }

    private boolean roleMarry(int params) {

        return false;
    }

    /**
     * @param condition 1:玩家等级;2:完成的任务;3:通关副本
     * @param params    配置的参数
     * @return
     */
    public boolean openOrNot(int condition, int params) {
        switch (condition) {
            case 1:
                return roleLevel(params);
            case 2:
                return roleTask(params);
            case 3:
                return roleDungeon(params);
            case 4:
                return roleAchievement(params);
            case 5:
                return roleUseTool(params);
            case 6:
                return roleMarry(params);
            case 7:
                return roleOldPlayer(params);
            case 8:
                return serverOpenDays(params);
            default:
                break;
        }
        return false;
    }

    private boolean serverOpenDays(int params) {
        return DataManager.getServerDays() >= params;
    }

    /**
     * 判断是否为老友归来
     *
     * @param params
     * @return
     */
    private boolean roleOldPlayer(int params) {
        return false;
    }

    public void addItemList(int itemId) {
        itemList.clear();
        itemList.add(itemId);
    }

    /**
     * 登陆时检查失效数据
     * 策划经常会删除产品库的数据，所以用户库做一下同步
     */
    private void loginCheckLoseEfficacyData() {
        if (foreShowStatePoMap.isEmpty()) return;
        Set<ForeShowStatePo> pos = new HashSet<>();
        for (ForeShowStatePo showStatePo : foreShowStatePoMap.values()) {
            if (!ForeShowManager.foreShowVoMap.containsKey(showStatePo.getOpenname())) {
                pos.add(showStatePo);
                context().delete(showStatePo);
            }
        }
        for (ForeShowStatePo po : pos) {
            if (foreShowStatePoMap.containsKey(po)) {
                foreShowStatePoMap.remove(po);
            }
            if (openShowMap.containsKey(po)) {
                openShowMap.remove(po);
            }
            if (openUnShowMap.containsKey(po)) {
                openUnShowMap.remove(po);
            }
            if (unOpenMap.containsKey(po)) {
                unOpenMap.remove(po);
            }
        }
    }

    /**
     * 登陆时检查系统开放情况
     *
     * @throws Exception
     */
    private void loginForeShow() throws Exception {
        Map<String, ForeShowVo> foreShowVoMap = ForeShowManager.foreShowVoMap;
        int maxSerial = 0;
        if (ForeShowManager.loginCheck) {
            for (Map.Entry<String, ForeShowVo> foreShowVoEntry : foreShowVoMap.entrySet()) {
                ForeShowStatePo foreShowStatePo = new ForeShowStatePo();
                String openLimitStr = foreShowVoEntry.getValue().getOpenlimit();
                foreShowStatePo.setRoleid(id());
                foreShowStatePo.setOpenname(foreShowVoEntry.getValue().getName());
                String mapKey = id() + "+" + foreShowVoEntry.getValue().getName();
                if ("0".equals(openLimitStr) == false) {
                    boolean isOpen = isOpen(foreShowVoEntry.getValue());
                    if (isOpen) {
                        putOpenShowMap(mapKey, foreShowStatePo);
                        if (foreShowVoEntry.getValue().getForeshowserial() > maxSerial) {
                            maxSerial = foreShowVoEntry.getValue().getForeshowserial();//记录最大序号
                        }
                    } else {
                        putUnOpenMap(mapKey, foreShowStatePo);
                    }
                } else {
                    bornOpenShowMap(mapKey, foreShowStatePo);
                }
            }
            List<String> bossOpenMap = containShowSystem();
            for (String openname : bossOpenMap) {
                openBossSystem(openname);
            }
        }
        putForeShowStatePoMap();
        //登陆时下发系统开启信息
        ClientForeShow clientForeShow = new ClientForeShow(ClientForeShow.foreOpenMap);
        clientForeShow.setOpenShowMap(openShowMap);
        clientForeShow.setOpenUnShowMap(openUnShowMap);
        Set<String> bossOpenList0 = new HashSet<>(bossOpenList);
        clientForeShow.setBossOpenShow(bossOpenList0);
        clientForeShow.setNextForeShowPo(getNextForeShowVo());
        send(clientForeShow);
        bossOpenList.clear();
        context().markUpdatedSummaryComponent(MConst.ForeShow);
    }

    private NextForeShowPo getNextForeShowVo() {
        //获得最大预告序列号
        int nextSerial = getMaxOpenSerial() + 1;
        ForeShowVo foreShowVo = ForeShowManager.getForeShowSerialMap().get(nextSerial);
        if (foreShowVo == null) return null;

        //检测是否已开启
        String mapKey = id() + "+" + foreShowVo.getName();
        ForeShowStatePo foreShowStatePo = openShowMap.get(mapKey);
        if (foreShowStatePo != null) return null;
        foreShowStatePo = openUnShowMap.get(mapKey);
        if (foreShowStatePo != null) return null;

        if (!isOpenForeShow(foreShowVo)) return null;

        NextForeShowPo nextForeShowPo = new NextForeShowPo(foreShowVo.getName());
        nextForeShowPo.setShowEffect(isPlayForeShowEffect(foreShowVo));
        return nextForeShowPo;
    }

    private boolean isPlayForeShowEffect(ForeShowVo foreShowVo) {
        if (foreShowVo == null || foreShowVo.isNoEffect()) return false;

        return openOrNot(1, foreShowVo.getEffectLevel())          //等级判断
                && openOrNot(3, foreShowVo.getEffectDungeonId()); //关卡判断
    }

    private boolean isOpenForeShow(ForeShowVo foreShowVo) {
        if (foreShowVo == null) return false;
        if (foreShowVo.isNoForecastCondition()) return true;
        RoleModule roleModule = module(MConst.Role);
        if (roleModule == null) return false;

        return openOrNot(1, foreShowVo.getConditionLevel())          //等级判断
                && openOrNot(3, foreShowVo.getConditionDungeonId()); //关卡判断
    }

    private int getMaxOpenSerial() {
        int maxSerial = 0;
        ForeShowVo showVo;
        for (ForeShowStatePo po : openShowMap.values()) {
            showVo = ForeShowManager.getForeShowVoMap(po.getOpenname());
            if (showVo == null) continue;
            if (showVo.getForeshowserial() > maxSerial) maxSerial = showVo.getForeshowserial();
        }
        for (ForeShowStatePo po : openUnShowMap.values()) {
            showVo = ForeShowManager.getForeShowVoMap(po.getOpenname());
            if (showVo == null) continue;
            if (showVo.getForeshowserial() > maxSerial) maxSerial = showVo.getForeshowserial();
        }
        return maxSerial;
    }

    /**
     * 玩家出生就开放的系统，直接记录到已开放已表现中
     *
     * @param mapKey
     * @param foreShowStatePo
     */
    private void bornOpenShowMap(String mapKey, ForeShowStatePo foreShowStatePo) {
        foreShowStatePo.setOpenstate(ForeShowConst.openShow);
        if (!openShowMap.containsKey(mapKey)) {
            //更新数据库
            context().insert(foreShowStatePo);
            openShowMap.put(mapKey, foreShowStatePo);
        } else {
            context().update(foreShowStatePo);
        }
    }

    /**
     * 如果不在已开放已表现且不在开放未表现记录中,则直接将记录更新到已开放已表现中
     *
     * @param mapKey
     * @param foreShowStatePo
     */
    private void putOpenShowMap(String mapKey, ForeShowStatePo foreShowStatePo) {
        if (!openShowMap.containsKey(mapKey) && !openUnShowMap.containsKey(mapKey)) {
            foreShowStatePo.setOpenstate(ForeShowConst.openShow);
            if (unOpenMap.containsKey(mapKey)) {
                //更新数据库
                context().update(foreShowStatePo);
            }
            if (!unOpenMap.containsKey(mapKey)) {
                //插入数据
                context().insert(foreShowStatePo);
            }
            unOpenMap.remove(mapKey);
            openShowMap.put(mapKey, foreShowStatePo);
        }
    }

    public boolean isOpen(String openname) {
        if (isOpen(openShowMap, openname)) {
            return true;
        }
        if (isOpen(openUnShowMap, openname)) {
            return true;
        }
        return false;
    }

    private boolean isOpen(Map<String, ForeShowStatePo> map, String openname) {
        for (Map.Entry<String, ForeShowStatePo> entry : map.entrySet()) {
            if (entry.getValue().getOpenname().equals(openname)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果不满足条件，则将其记录到未开放记录中
     *
     * @param mapKey
     * @param foreShowStatePo
     */
    private void putUnOpenMap(String mapKey, ForeShowStatePo foreShowStatePo) {
        foreShowStatePo.setOpenstate(ForeShowConst.unOpen);
        if (!unOpenMap.containsKey(mapKey)) {
            if (openShowMap.containsKey(mapKey) || openUnShowMap.containsKey(mapKey)) {
                context().update(foreShowStatePo);
                openShowMap.remove(mapKey);
                openUnShowMap.remove(mapKey);
                unOpenMap.put(mapKey, foreShowStatePo);
            } else {
                context().insert(foreShowStatePo);
                unOpenMap.put(mapKey, foreShowStatePo);
            }
        }
    }

    //好乱的逻辑，加了一个特权给充钱的大爷，提前开放某个系统(坐骑系统)
    private void openBossSystem(String open) {
        if (ForeShowManager.containSysName(open) && !bossOpenList0.contains(open)) {
            List<Integer> idList = ForeShowManager.getIdList(open);
            switch (open) {
            }
        }
    }

    /**
     * 当用户初次创建时，将数据放入foreShowStatePoMap中
     */
    private void putForeShowStatePoMap() {
        // if (foreShowStatePoMap.isEmpty()) {
        foreShowStatePoMap.putAll(openShowMap);
        foreShowStatePoMap.putAll(unOpenMap);
        //  }
    }


    private boolean isOpen(ForeShowVo foreShowVo) {
        if (foreShowVo.getName().equals(ForeShowConst.DAILY_SIGNIN)) {
            if (System.currentTimeMillis() < 1496246400000L) {
                return false;
            }
        }
        String openLimitStr = foreShowVo.getOpenlimit();
        boolean isOpen = foreShowVo.getIsall() == 0 ? false : true;
        String[] limits = openLimitStr.split("[,]");
        for (String limit : limits) {
            String[] condition = limit.split("[+]");
            int type = Integer.valueOf(condition[0]);
            int value = Integer.valueOf(condition[1]);
            if (foreShowVo.getIsall() == 0) {
                // 条件满足关系
                if (openOrNot(type, value)) {
                    isOpen = true;
                    break;
                }
            } else {
                // 条件满足关系 且
                if (!openOrNot(type, value)) {
                    isOpen = false;
                    break;
                }
            }

        }
        return isOpen;
    }

    /**
     * 玩家等级，完成任务，通关副本变化所引起的事件
     */
    public void updateForeShow() {
        map.clear();
        List<String> openList = new ArrayList<>();
        for (Map.Entry<String, ForeShowStatePo> foreShowStatePoEntry : foreShowStatePoMap.entrySet()) {
            ForeShowStatePo foreShowStatePo = foreShowStatePoEntry.getValue();
            String mapKey = id() + "+" + foreShowStatePo.getOpenname();
            ForeShowVo foreShowVo = ForeShowManager.getForeShowVoMap(foreShowStatePo.getOpenname());
            if (foreShowVo == null) {
                continue;
            }
            boolean isOpen = isOpen(foreShowVo);
            if (unOpenMap.containsKey(mapKey)) {
                if (isOpen) {
                    int windowTips = foreShowVo.getWindowtips();
                    unOpen2Open(windowTips, foreShowStatePo, mapKey);
                    Map<String, String> tempChangeMap = new HashMap<>(foreShowChangeMap);
                    eventDispatcher().fire(new ForeShowChangeEvent(tempChangeMap));
                    foreShowChangeMap.clear();
                    openList.add(foreShowStatePo.getOpenname());
                }
            }
        }
        List<String> bossOpenMap = containShowSystem();
        for (String openname : bossOpenMap) {
            openBossSystem(openname);
        }
        toClient(ClientForeShow.openUnShow);
        context().markUpdatedSummaryComponent(MConst.ForeShow);
    }

    private List<String> containShowSystem() {
        List<String> opennames = new ArrayList<>();
        for (ForeShowStatePo statePo : unOpenMap.values()) {
            if (ForeShowManager.containSysName(statePo.getOpenname()) && !opennames.contains(statePo.getOpenname())) {
                opennames.add(statePo.getOpenname());
            }
        }
        return opennames;
    }

    private int getForeShowSerial(ForeShowStatePo foreShowStatePo) {
        return ForeShowManager.getForeShowVoMap(foreShowStatePo.getOpenname()).getForeshowserial();
    }


    private String getNextOpenName(int nextForeShowSerial) {
        String name = "";
        for (Map.Entry<String, ForeShowVo> entry : ForeShowManager.foreShowVoMap.entrySet()) {
            if (entry.getValue().getForeshowserial() == nextForeShowSerial) {
                name = entry.getValue().getName();
            }
        }
        return name;
    }


    private void toClient(byte packet) {
        Set<String> bossOpenList0 = new HashSet<>(bossOpenList);
        //通知客户端有系统开放
        ClientForeShow clientForeShow = new ClientForeShow(packet);
        clientForeShow.setMap(map);
        clientForeShow.setNextForeShowPo(getNextForeShowVo());
        clientForeShow.setBossOpenShow(bossOpenList0);
        send(clientForeShow);
        bossOpenList.clear();
    }

    /**
     * 根据windowTips的值决定该系统是转为已开放已表现还是已开放未表现
     *
     * @param windowTips      1:需要告知客户端，转为已开放未表现，0：直接转为已开放已表现
     * @param foreShowStatePo
     * @param mapKey
     */
    private void unOpen2Open(int windowTips, ForeShowStatePo foreShowStatePo, String mapKey) {
        if (windowTips != 1) {
            foreShowStatePo.setOpenstate(ForeShowConst.openShow);
            context().update(foreShowStatePo);
            unOpenMap.remove(mapKey);
            openShowMap.put(mapKey, foreShowStatePo);
        } else {
            foreShowStatePo.setOpenstate(ForeShowConst.openUnShow);
            context().update(foreShowStatePo);
            unOpenMap.remove(mapKey);
            openUnShowMap.put(mapKey, foreShowStatePo);
        }
        map.put(mapKey, foreShowStatePo);
        foreShowChangeMap.put(foreShowStatePo.getOpenname(), getOpenInduct(foreShowStatePo.getOpenname()));
    }


    /**
     * @param name
     * @return
     */
    private String getOpenInduct(String name) {
        return ForeShowManager.getForeShowVoMap(name).getOpeninduct();
    }


    public static String getforeShowText(String name) {
        ForeShowVo vo = ForeShowManager.getForeShowVoMap(name);
        Object[] params = getParams(vo);
        String format = DataManager.getGametext(vo.getForeshowtext());
        return String.format(format, params);
    }

    public static String getShortText(String name) {
        ForeShowVo vo = ForeShowManager.getForeShowVoMap(name);
        Object[] params = getParams(vo);
        String format = DataManager.getGametext(vo.getConditiondesc());
        return String.format(format, params);
    }

    private static Object[] getParams(ForeShowVo vo) {
        String openLimitStr = vo.getOpenlimit();
        String[] limits = openLimitStr.split("[,]");
        Object[] params = new String[limits.length];
        for (int i = 0; i < limits.length; i++) {
            String[] limit = limits[i].split("[+]");
            int type = Integer.valueOf(limit[0]);
            String param = null;
            switch (type) {
                case 1:
                    param = limit[1];
                    break;
                case 2:
                    int taskId = Integer.valueOf(limit[1]);
                    break;
                case 3:
                    StringBuilder builder = new StringBuilder();
                    int dungeonId = Integer.valueOf(limit[1]);
                    DungeoninfoVo dungeonVo = DungeonManager.getDungeonVo(dungeonId);
                    builder.append(dungeonVo.getWorldId()).append("-").append(dungeonVo.getStep());
                    param = builder.toString();
                    break;
                default:
                    param = "";
            }
            params[i] = param;
        }
        return params;
    }

    /**
     * 客户端请求某个系统的条件文本
     *
     * @param name
     */
    public void foreShowText(String name) {
        String text = getforeShowText(name);
        String shortText = getShortText(name);
        ClientForeShow res = new ClientForeShow(ClientForeShow.openText);
        res.setText(text);
        res.setName(name);
        res.setShortText(shortText);
        send(res);
    }


    /**
     * 根据任务id获取任务名字,并下发至客户端
     *
     * @param taskid
     */
    public void getTaskText(int taskid) {
        ClientForeShow clientForeShow = new ClientForeShow(ClientForeShow.openText);
        clientForeShow.setFlag((byte) 2);
        send(clientForeShow);
    }

    /**
     * 根据关卡id获取关卡名字和章节标题,并下发至客户端
     *
     * @param dungeonid
     */
    public void getDungeonText(int dungeonid) {
        ClientForeShow clientForeShow = new ClientForeShow(ClientForeShow.openText);
        clientForeShow.setFlag((byte) 3);
        clientForeShow.setDungeonName(DungeonManager.getDungeonVo(dungeonid).getName());
        clientForeShow.setWorldTitle(DungeonManager.getChapterVo(DungeonManager.getDungeonVo(dungeonid).getWorldId()).getTitle());
        send(clientForeShow);
    }


    /**
     * 客户端通知某个系统开启完毕
     * 开放未表现转至开放已表现
     *
     * @param name
     */
    public void openUnShow2OpenShow(String name) {
        ForeShowStatePo foreShowStatePo = new ForeShowStatePo();
        if (openUnShowMap.containsKey(id() + "+" + name)) {
            foreShowStatePo.setRoleid(id());
            foreShowStatePo.setOpenname(name);
            foreShowStatePo.setOpenstate(ForeShowConst.openShow);
            context().update(foreShowStatePo);
            openUnShowMap.remove(id() + "+" + name);
            openShowMap.put(id() + "+" + name, foreShowStatePo);
        }
    }

    /**
     * GM指令，解锁所有的系统
     */
    public void openAll() {
        map.clear();
        for (Map.Entry<String, ForeShowStatePo> entry : foreShowStatePoMap.entrySet()) {
            ForeShowStatePo foreShowStatePo = entry.getValue();
            if (openUnShowMap.containsKey(entry.getKey())) {
                foreShowStatePo.setOpenstate(ForeShowConst.openShow);
                context().update(foreShowStatePo);
                openUnShowMap.remove(entry.getKey());
                openShowMap.put(entry.getKey(), foreShowStatePo);
                map.put(entry.getKey(), foreShowStatePo);
            }
            if (unOpenMap.containsKey(entry.getKey())) {
                foreShowStatePo.setOpenstate(ForeShowConst.openShow);
                context().update(foreShowStatePo);
                unOpenMap.remove(entry.getKey());
                openShowMap.put(entry.getKey(), foreShowStatePo);
                map.put(entry.getKey(), foreShowStatePo);
            }
            foreShowChangeMap.put(foreShowStatePo.getOpenname(), getOpenInduct(foreShowStatePo.getOpenname()));
        }
        Map<String, String> tempChangeMap = new HashMap<>(foreShowChangeMap);
        eventDispatcher().fire(new ForeShowChangeEvent(tempChangeMap));
        foreShowChangeMap.clear();
        ClientForeShow clientForeShow = new ClientForeShow(ClientForeShow.openUnShow);
        clientForeShow.setOpenShowMap(openShowMap);
        clientForeShow.setOpenUnShowMap(openUnShowMap);
        clientForeShow.setBossOpenShow(bossOpenList);
        clientForeShow.setMap(map);
        clientForeShow.setNextForeShowPo(getNextForeShowVo());
        send(clientForeShow);
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            Map<String, ForeShowStatePo> map = new HashMap<>();
            map.putAll(openUnShowMap);
            map.putAll(openShowMap);
            List<String> opennames = new ArrayList<>();
            for (ForeShowStatePo po : map.values()) {
                opennames.add(po.getOpenname());
            }
            componentMap.put(MConst.ForeShow, new ForeShowSummaryComponentImp(opennames));
        }
    }
}
