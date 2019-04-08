package com.stars.services.marry;

import com.stars.core.persist.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.hotupdate.YinHanHotUpdateManager;
import com.stars.core.player.PlayerUtil;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.baseteam.BaseTeamManager;
import com.stars.modules.baseteam.packet.ClientBaseTeamInvite;
import com.stars.modules.baseteam.userdata.TeamInvitee;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyAwardCheckEvent;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.deityweapon.summary.DeityWeaponSummaryComponent;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.marry.MarryManager;
import com.stars.modules.marry.event.SyncSelfDataToTeamEvent;
import com.stars.modules.marry.packet.ClientMarry;
import com.stars.modules.marry.packet.ClientMarryBattleInfo;
import com.stars.modules.marry.prodata.MarryActivityVo;
import com.stars.modules.scene.fightdata.FighterCreator;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.teamdungeon.TeamDungeonManager;
import com.stars.modules.teamdungeon.prodata.TeamDungeonVo;
import com.stars.modules.tool.ToolManager;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.baseteam.BaseTeam;
import com.stars.services.baseteam.BaseTeamMember;
import com.stars.services.friend.userdata.FriendApplicationPo;
import com.stars.services.friend.userdata.FriendPo;
import com.stars.services.marry.event.*;
import com.stars.services.marry.userdata.*;
import com.stars.services.summary.Summary;
import com.stars.util.*;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhouyaohui on 2016/12/1.
 */
public class MarryServiceActor extends ServiceActor implements MarryService {

    private Map<Long, MarryCache> cache = new HashMap<>();
    private Map<Long, MarryRole> roleMap = new HashMap<>();   // 用户数据

    private LinkedList<MarryRole> claimQueue = new LinkedList<>();   // 宣言队列

    private Map<String, MarryProfress> profressMap = new HashMap<>();   // 表白数据
    private Map<Long, List<MarryProfress>> profressTemp = new HashMap<>();  // 被表白数据临时存放，避免角色登陆表白数据还没来得及入库
    private Map<Long, List<MarryProfress>> profressRemoveMap = new HashMap<>(); // 存放角色不存在缓存的情况下被处理的表白请求

    private Map<String, Marry> marryMap = new HashMap<>();  // 结婚数据
    private Map<String, Marry> activeMarryMap = new HashMap<>();  // 预约等待应答

    private ConcurrentHashMap<String, MarryWedding> currentWeddingMap = new ConcurrentHashMap<>(); // 当前举行豪华婚礼数据
    private LinkedList<MarryWedding> currentWeddingList = new LinkedList<>();

    private Set<Long> online = new HashSet<>();


    private Map<Integer, LinkedList<MarryWedding>> weddingMap = new HashMap<>(); // 婚礼数据
    private Map<String, MarryWedding> weddingQueryMap = new HashMap<>();    // 婚礼map marrykey-value

    private DbRowDao dao = new DbRowDao();

    public MarryServiceActor() {
        super(8192);
    }

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.MarryService, this);
        // loadWeddingData();
        loadClaimData();
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.Marry, new MarryServiceCheck(), 30, 3, TimeUnit.SECONDS);
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},cache.size:{},roleMap.size:{},claimQueue.size:{},profressMap.size:{},profressRemoveMap.size:{},profressTemp.size:{}", this.getClass().getSimpleName(), cache.size(), roleMap.size(), claimQueue.size(), profressMap.size(), profressRemoveMap.size(), profressTemp.size());
        LogUtil.info("容器大小输出:{},marryMap.size:{},activeMarryMap.size:{},currentWeddingMap.size:{},currentWeddingList.size:{},online.size:{},weddingQueryMap.size:{}", this.getClass().getSimpleName(), marryMap.size(), activeMarryMap.size(), currentWeddingMap.size(), currentWeddingList.size(), online.size(), weddingQueryMap.size());
    }

    @Override
    public void save() {
        dao.flush();
    }

    /**
     * 加载宣言列表
     */
    private void loadClaimData() throws SQLException {
        String sql = "select * from marryrole where claimstamp > " + (DateUtil.getSecondTime() - MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC) + " and LENGTH(`claim`) > 0";
        List<MarryRole> list = DBUtil.queryList(DBUtil.DB_USER, MarryRole.class, sql);
        for (MarryRole role : list) {
            roleMap.put(role.getRoleId(), role);
        }
        Collections.sort(list, new ClaimComparator());
        claimQueue.addAll(list);
    }

    /**
     * 加载婚礼数据
     */
    private void loadWeddingData() throws SQLException {
        /**
         String sql = "select * from marrywedding where ymd >= " + DateUtil.getYMD_Str();
         List<MarryWedding> weddingList = DBUtil.queryList(DBUtil.DB_USER, MarryWedding.class, sql);
         Set<Long> loadSet = new HashSet<>();
         for (MarryWedding wedding : weddingList) {
         weddingQueryMap.put(wedding.getMarryKey(), wedding);
         LinkedList<MarryWedding> day = weddingMap.get(wedding.getYmd());
         if (day == null) {
         day = new LinkedList<>();
         weddingMap.put(wedding.getYmd(), day);
         }
         int i = 0;
         for (; i < day.size(); i++) {
         if (day.get(i).getHms() > wedding.getHms()) {
         break;
         }
         }
         day.add(i, wedding);
         loadSet.add(wedding.getOrder());
         loadSet.add(wedding.getOther());
         }

         // 加载豪华婚礼相关的角色数据
         StringBuilder builder = new StringBuilder();
         builder.append("select * from marryrole where roleid in (");
         for (long id : loadSet) {
         builder.append(id).append(",");
         }
         if (loadSet.size() != 0) {
         builder.delete(builder.length() - 1, builder.length());
         builder.append(")");
         List<MarryRole> list = DBUtil.queryList(DBUtil.DB_USER, MarryRole.class, builder.toString());
         for (MarryRole role : list) {
         roleMap.put(role.getRoleId(), role);
         }
         }
         // 加载结婚数据
         builder.delete(0, builder.length());
         builder.append("select * from marry where `uniquekey` in (");
         for (MarryWedding mw : weddingList) {
         builder.append("'").append(mw.getMarryKey()).append("'").append(",");
         }
         if (weddingList.size() != 0) {
         builder.delete(builder.length() - 1, builder.length());
         builder.append(")");
         List<Marry> list = DBUtil.queryList(DBUtil.DB_USER, Marry.class, builder.toString());
         for (Marry marry : list) {
         marryMap.put(marry.getUniqueKey(), marry);
         }
         }
         **/
    }

    /**
     * 结婚定时检查，暂定3秒检查一次
     * 主要是预约响应、豪华婚礼的活动检查与触发
     */
    @Override
    public void check() {
        if (YinHanHotUpdateManager.needHotUpdate("marryDungeon")) {
            for (MarryRole marryRole : roleMap.values()) {
                marryRole.setDungeon((byte) 0);
                dao.update(marryRole);
            }
        }
        long now = DateUtil.getSecondTime();
        {
            // 检查预约超时
            long outTime = DataManager.getCommConfig("marry_appointment_delay", 60);
            List<String> removeList = new ArrayList<>();
            for (Map.Entry<String, Marry> m : activeMarryMap.entrySet()) {
                if (m.getValue().getAppointStamp() + outTime < now) {
                    removeList.add(m.getKey());
                }
            }
            for (String key : removeList) {
                handleOutTimeOrRefuseAppoint(key);
            }
        }
        {
            // 检查婚宴是否结束
            long outTime = DataManager.getCommConfig("marry_party_duration", 600);
            int index = -1;

            for (int i = 0; i < currentWeddingList.size(); ++i) {
                if (currentWeddingList.get(i).getStartStamp() + outTime < now) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                for (; index < currentWeddingList.size(); ) {    // 移除过期的婚宴
                    MarryWedding wedding = currentWeddingList.get(index);
                    noticePartyAll(wedding, createWeddingStatePacket(wedding, MarryWedding.END));
                    currentWeddingMap.remove(wedding.getMarryKey());
                    currentWeddingList.remove(index);
                }
            }
        }
        {
            // 检查喜糖活动
            for (Map.Entry<String, MarryWedding> m : currentWeddingMap.entrySet()) {
                if (now - m.getValue().getLastCandyActivity() >= MarryManager.CANDY_DELAY) {
                    LogUtil.info("喜糖活动开始 hms {}", now);
                    // 喜糖活动
                    m.getValue().clearCandySet();
                    String[] candys = DataManager.getCommConfig("marryactivity_candy_position").split("[,]");
                    for (String candy : candys) {
                        m.getValue().getCandySet().add(candy);
                    }
                    m.getValue().setLastCandyActivity((int) now);
                    ClientMarry candyActivity = new ClientMarry();
                    candyActivity.setResType(ClientMarry.CANDY_ACTIVITY);
                    candyActivity.setCandyType(MarryManager.CANDY_ACTIVITY_BEGIN);
                    candyActivity.setCandyStamp(m.getValue().getLastCandyActivity());
                    noticePartyAll(m.getValue(), candyActivity);
                }
            }
        }
        {
            // 婚宴活动开启按钮检查
            byte iconFlag = 0;
            if (currentWeddingList.size() > 0) {
                iconFlag = 1;
            }
            WeddingActCheckEvent ev = new WeddingActCheckEvent(iconFlag);
            for (long id : online) {
                ServiceHelper.roleService().notice(id, ev);
            }
        }
    }

    /**
     * 计算两个时间之间的差值 (hms1 - hms2 = xxx)
     *
     * @param hms1
     * @param hms2
     * @return
     */
    private int getSubTimeFromHMS(int hms1, int hms2) {
        int time1 = 0;
        int time2 = 0;

        time1 += (hms1 % 100);
        hms1 = hms1 / 100;
        time1 += (hms1 % 100) * 60;
        hms1 = hms1 / 100;
        time1 += (hms1 % 100) * 60 * 60;

        time2 += (hms2 % 100);
        hms2 = hms2 / 100;
        time2 += (hms2 % 100) * 60;
        hms2 = hms2 / 100;
        time2 += (hms2 % 100) * 60 * 60;

        return time1 - time2;
    }

    /**
     * 给所有参加婚礼的玩家发送消息
     *
     * @param packet
     */
    private void noticePartyAll(MarryWedding wedding, Packet packet) {
        for (long id : wedding.getParty()) {
            PlayerUtil.send(id, packet);
        }
    }

    /**
     * 给所有在线玩家发消息
     *
     * @param packet
     */
    private void noticeAll(Packet packet) {
        for (long id : online) {
            PlayerUtil.send(id, packet);
        }
    }

    /**
     * 角色上线
     *
     * @param role
     */
    @Override
    public void playerOnline(MarryRole role) {
        try {
            long roleId = role.getRoleId();
            online.add(role.getRoleId());
            if (cache.containsKey(role.getRoleId())) {
                MarryRole marryRole = roleMap.get(role.getRoleId());
                marryRole.setFight(role.getFight());
                marryRole.setJobId(role.getJobId());
                marryRole.setLevel(role.getLevel());
                dao.update(marryRole);
            } else {
                MarryCache marryCache = new MarryCache();

                // 组装角色个人数据
                MarryRole marryRole = null;
                if (roleMap.containsKey(role.getRoleId())) {    // 有缓存就用缓存数据
                    marryRole = roleMap.get(role.getRoleId());
                } else {
                    marryRole = DBUtil.queryBean(DBUtil.DB_USER, MarryRole.class, "select * from marryrole where roleid = " + role.getRoleId());
                    if (marryRole == null) {
                        marryRole = role;
                        dao.insert(marryRole);
                    }
                    roleMap.put(role.getRoleId(), marryRole);
                }
                marryRole.setFight(role.getFight());
                marryRole.setJobId(role.getJobId());
                marryRole.setLevel(role.getLevel());
                dao.update(marryRole);
                marryCache.setMarryRole(marryRole);

                // 组装表白数据
                List<MarryProfress> targetList = DBUtil.queryList(DBUtil.DB_USER, MarryProfress.class, "select * from marryprofress where target = " + role.getRoleId() + " and state = 1");
                List<MarryProfress> profressList = DBUtil.queryList(DBUtil.DB_USER, MarryProfress.class, "select * from marryprofress where profressor = " + role.getRoleId() + " and state = 1");
                marryCache.getProfressed().clear();
                marryCache.getProfress().clear();
                for (MarryProfress profress : targetList) {
                    if (profressMap.containsKey(profress.getUniquekey())) {
                        // 缓存中有，用缓存的
                        marryCache.getProfressed().put(profress.getProfressor(), profressMap.get(profress.getUniquekey()));
                    } else {
                        profressMap.put(profress.getUniquekey(), profress);
                        marryCache.getProfressed().put(profress.getProfressor(), profress);
                    }
                }
                for (MarryProfress profress : profressList) {
                    if (profressMap.containsKey(profress.getUniquekey())) {
                        marryCache.getProfress().put(profress.getTarget(), profressMap.get(profress.getUniquekey()));
                    } else {
                        profressMap.put(profress.getUniquekey(), profress);
                        marryCache.getProfress().put(profress.getTarget(), profress);
                    }
                }
                if (profressTemp.containsKey(role.getRoleId())) {
                    for (MarryProfress profress : profressTemp.get(role.getRoleId())) { // 被表白临时数据
                        //int popularity = marryRole.getPopularity();
                        //marryRole.setPopularity(popularity + 1);
                        if (!marryCache.getProfressed().containsKey(profress.getProfressor())) {   // 还没来得及入库的
                            marryCache.getProfressed().put(profress.getProfressor(), profress);
                        }
                    }
                    //dao.update(marryRole);
                    profressTemp.remove(role.getRoleId());
                }
                if (profressRemoveMap.containsKey(role.getRoleId())) {  // 待移除的表白数据
                    for (MarryProfress profress : profressRemoveMap.get(role.getRoleId())) {
                        if (marryCache.getProfress().containsKey(profress.getTarget())) {
                            marryCache.getProfress().remove(profress.getTarget());
                            profressMap.remove(profress.getUniquekey());
                        }
                    }
                    profressRemoveMap.remove(role.getRoleId());
                }

                // 组装结婚数据
                if (!StringUtil.isEmpty(marryCache.getMarryRole().getMarryKey())) {
                    if (marryMap.containsKey(marryCache.getMarryRole().getMarryKey())) {
                        marryCache.setMarry(marryMap.get(marryCache.getMarryRole().getMarryKey()));
                    } else {
                        Marry marry = DBUtil.queryBean(DBUtil.DB_USER, Marry.class, "select * from marry where uniquekey = '" + marryCache.getMarryRole().getMarryKey() + "'");
                        marryCache.setMarry(marry);
                        marryMap.put(marry.getUniqueKey(), marry);
                    }
                }

                // 加载向角色表白的角色的用户数据,以及结婚的数据
                StringBuilder builder = new StringBuilder();
                builder.append("select * from marryrole where roleid in (");
                boolean needLoad = false;
                if (marryCache.getMarry() != null) {
                    long other = marryCache.getMarry().getOther(marryCache.getMarryRole().getRoleId());
                    if (roleMap.containsKey(other) == false) {
                        needLoad = true;
                        builder.append(other).append(",");
                    }
                }
                for (MarryProfress profress : marryCache.getProfressed().values()) {
                    if (roleMap.containsKey(profress.getProfressor())) {    // 缓存已经存在
                        continue;
                    }
                    needLoad = true;
                    builder.append(profress.getProfressor()).append(",");
                }
                if (needLoad) {
                    builder.delete(builder.length() - 1, builder.length());
                    builder.append(")");
                    List<MarryRole> roleList = DBUtil.queryList(DBUtil.DB_USER, MarryRole.class, builder.toString());
                    for (MarryRole r : roleList) {
                        roleMap.put(r.getRoleId(), r);
                    }
                }

                // 缓存数据
                cache.put(role.getRoleId(), marryCache);
            }

            {
                // 上线检查婚礼宴会状况
                ClientMarry res = new ClientMarry();
                res.setResType(ClientMarry.WEDDING_ICON);
                byte iconFlag = (byte) (currentWeddingList.size() > 0 ? 1 : 0);
                res.setIcon(iconFlag);
                PlayerUtil.send(roleId, res);
            }

            MarryCache marryCache = cache.get(roleId);
            // 检查普通决裂
            if (marryCache.getMarry() != null && marryCache.getMarry().getBreakState() == MarryManager.BREAK_STATE_PROPOSE) {
                if (marryCache.getMarry().getLastBreakStamp() + DataManager.getCommConfig("marry_breakship_lasttime", 24 * 60 * 60) < DateUtil.getSecondTime()) {
                    Marry marry = marryCache.getMarry();
                    marry.setBreakState(MarryManager.BREAK_STATE_OVER);
                    dao.update(marry);
                    cleanCacheAfterBreak(roleId);
                    if (online.contains(marry.getOther(roleId))) {
                        // TODO: 2016/12/26  通知客户端关闭界面
                    }
                }
            }
            // 过期表白清理
            handleOutTimeProfress(roleId);

            // 过期被表白清理
            handleOutTimeProfressed(roleId);

            // 过期宣言清理
            handleOutTimeClaim(roleId);

            if (marryCache.getMarry() != null) {
                // 上线删除已结婚玩家所有的被表白
                handleProfressCache(roleId);
                handleProfressCache(marryCache.getMarry().getOther(roleId));
            }

            // 登陆触发事件通知个人业务
            fireMarryEvent(roleId);     // 聊天红点
            fireProfressEvent(roleId);  // 表白红点
            fireMarryBattleEvent(roleId); //刷新日常任务的状态
        } catch (Exception e) {
            LogUtil.error("marry online error.", e);
        }
    }

    /**
     * 过期表白清理
     *
     * @param roleId
     */
    private void handleOutTimeProfress(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        if (null == marryCache)
            return;
        Map<Long, MarryProfress> tmpProfress = marryCache.getProfress();
        if (tmpProfress.size() <= 0)
            return;
        List<Long> deleteList = new ArrayList<>();
        long now = DateUtil.getSecondTime();
        for (Map.Entry<Long, MarryProfress> m : tmpProfress.entrySet()) {
            String[] key = m.getValue().getUniquekey().split("[+]");
            long profressTime = Long.valueOf(key[2]);
            if ((profressTime + MarryManager.CONFIG_PROFRESS_OUTTIME) < now) {
                deleteList.add(m.getKey());
            }
        }
        if (deleteList.size() <= 0)
            return;
        for (Long id : deleteList) {
            MarryProfress profress = tmpProfress.get(id);
            LogUtil.info("profress{} is outtime,profressor:{},target:{}", profress.getUniquekey(), profress.getProfressor(), profress.getTarget());
            tmpProfress.remove(id);
            profress.setState(MarryManager.PROFRESS_OUTTIME);
            dao.delete(profress);
            MarryCache otherCache = cache.get(profress.getTarget());
            if (null != otherCache) {
                otherCache.getProfressed().remove(profress.getProfressor());
                fireProfressEvent(profress.getTarget());
            }
            profressMap.remove(profress.getUniquekey());
        }
    }

    /**
     * 过期被表白清理
     *
     * @param roleId
     */
    private void handleOutTimeProfressed(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        if (null == marryCache)
            return;
        Map<Long, MarryProfress> tmpProfressed = marryCache.getProfressed();
        if (tmpProfressed.size() <= 0)
            return;
        List<Long> deleteList = new ArrayList<>();
        long now = DateUtil.getSecondTime();
        for (Map.Entry<Long, MarryProfress> m : tmpProfressed.entrySet()) {
            String[] key = m.getValue().getUniquekey().split("[+]");
            long profressTime = Long.valueOf(key[2]);
            if ((profressTime + MarryManager.CONFIG_PROFRESS_OUTTIME) < now) {
                deleteList.add(m.getKey());
            }
        }
        if (deleteList.size() <= 0)
            return;
        for (Long id : deleteList) {
            MarryProfress profress = tmpProfressed.get(id);
            LogUtil.info("profress{} is outtime,profressor:{},target:{}", profress.getUniquekey(), profress.getProfressor(), profress.getTarget());
            tmpProfressed.remove(id);
            profress.setState(MarryManager.PROFRESS_OUTTIME);
            dao.delete(profress);
            MarryCache otherCache = cache.get(profress.getProfressor());
            if (null != otherCache) {
                otherCache.getProfress().remove(profress.getTarget());
            }
            profressMap.remove(profress.getUniquekey());
        }
    }

    /**
     * 宣言过期清理
     *
     * @param roleId
     */
    private void handleOutTimeClaim(long roleId) {
        MarryRole marryRole = roleMap.get(roleId);
        if (null == marryRole)
            return;
        long now = DateUtil.getSecondTime();
        if (!"".equals(marryRole.getClaim())) {
            if (marryRole.getClaimStamp() + MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC < now) {
                marryRole.setClaim("");
                claimQueue.remove(marryRole);
                dao.update(marryRole);
            }
        }
    }

    /**
     * 创建豪华婚礼准备包
     * 改版后只发送宴会结束包
     *
     * @return
     */
    private Packet createWeddingStatePacket(MarryWedding wedding, byte state) {
        ClientMarry res = new ClientMarry();
        MarryRole order = roleMap.get(wedding.getOrder());
        MarryRole other = roleMap.get(wedding.getOther());
        res.setResType(ClientMarry.WEDDING_STATE);
        res.setOtherRole(other);
        res.setOrderRole(order);
        res.setWeddingState(state);
        res.setBeginRemain(0);
        return res;
    }

    /**
     * 角色下线
     *
     * @param roleId
     */
    @Override
    public void playerOffline(String key, long roleId) {
        online.remove(roleId);
        //party.remove(roleId);
        exitWeddingScene(key, roleId);
        // 离开队伍
        //ServiceHelper.baseTeamService().leaveTeam(roleId);
    }

    public void exitWeddingScene(String key, long roleId) {
        if (null == key) return;
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null != wedding) {
            wedding.exit(roleId);
        }
    }

    /**
     * 发布宣言
     *
     * @param claim
     */
    @Override
    public void claim(long roleId, String claim, int reqLevel) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            // 特殊账号不能宣言
            return;
        }
        ClientMarry res = new ClientMarry();
        MarryRole role = roleMap.get(roleId);
        res.setResType(ClientMarry.CLAIM);
//        if (StringUtil.hasSensitiveWordExt1(claim)) {
        if (DirtyWords.checkNotice(claim)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.claim.sensitive")));
            res.setClaim(role.getClaim());
            res.setClaimResult((byte) 0);
            PlayerUtil.send(roleId, res);
            return;
        }

        if (claim.getBytes().length > 120) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.claim.too.long")));
            res.setClaim(role.getClaim());
            res.setClaimResult((byte) 0);
            PlayerUtil.send(roleId, res);
            return;
        }

//        for (int i = 0; i < claim.length(); i++) {
//            char c = claim.charAt(i);
//            if (!StringUtil.isChinese(c) && !Character.isLetterOrDigit(c)) {
//                send(roleId, new ClientText(I18n.get("marry.claim.sensitive")));
//                return ;
//            }
//        }

        if (!StringUtil.isValidString(claim)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.claim.sensitive")));
            return;
        }

        role.setClaim(claim);
        role.setReqLevel(reqLevel);
        role.setClaimStamp(DateUtil.getSecondTime());

        if (claimQueue.contains(role)) {
            claimQueue.remove(role);
        }
        if (!StringUtil.isEmpty(claim)) {
            claimQueue.addFirst(role);
        }
        if (StringUtil.isEmpty(claim)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.claim.cancel")));
            res.setClaimResult((byte) 2);
        } else {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.claim.distribute")));
            res.setClaimResult((byte) 1);
        }
        dao.update(role);
        res.setClaim(role.getClaim());
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "发表宣言:" + claim, true);
    }

    /**
     * 宣言列表
     *
     * @param roleId
     */
    @Override
    public void claimList(long roleId, int index, int endIndex) {
        /**
         * 宣言列表分段处理
         */
        if (index < 0 || endIndex < index) {
            return;
        }

        if (endIndex >= claimQueue.size()) {
            endIndex = claimQueue.size() - 1;
        }

        if (index >= claimQueue.size()) {
            ClientMarry emptyRes = new ClientMarry();
            emptyRes.setResType(ClientMarry.CLAIM_LIST);
            List<MarryRole> emptyList = new ArrayList<>();
            emptyRes.setClaimList(emptyList);
            PlayerUtil.send(roleId, emptyRes);
            return;
        }

        List<MarryRole> list = new ArrayList<>();
        int curor = index;
        int cd = MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC;
        int now = DateUtil.getSecondTime();
        boolean bIsExsitOutTimeClaim = false;
        for (; curor <= endIndex; curor++) {
            if (claimQueue.get(curor).getClaimStamp() + cd < now) {
                bIsExsitOutTimeClaim = true;
                break;
            }
            /**
             * 由于做分段，如果自己的不发，前端认为不好处理，让前端自己屏蔽掉自己的宣言
             if (claimQueue.get(curor).getRoleId() == roleId) {
             continue;
             }
             **/
            list.add(claimQueue.get(curor));
        }
        if (bIsExsitOutTimeClaim) {
            for (; curor < claimQueue.size(); ) {    // 移除过期的宣言
                claimQueue.get(curor).setClaim("");
                dao.update(claimQueue.get(curor));
                claimQueue.remove(curor);
            }
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.CLAIM_LIST);
        res.setClaimList(list);
        PlayerUtil.send(roleId, res);
        //LogUtil.info("claimList para:{},{},result:{}",index,endIndex,list.size());
        fireSpecialAccountEvent(roleId, roleId, "宣言列表", true);
    }

    /**
     * 表白
     *
     * @param roleId
     * @param profressTarget
     */
    @Override
    public void profress(long roleId, long profressTarget, byte way) {
        if (SpecialAccountManager.isSpecialAccount(roleId))
            return;
        if (SpecialAccountManager.isSpecialAccount(profressTarget)) {
            // 不应该出现向特殊账号表白，直接返回算了
            return;
        }
        if (roleId == profressTarget) {
            // 拒绝自己跟自己表白
            return;
        }
        MarryRole target = roleMap.get(profressTarget);
        MarryRole role = roleMap.get(roleId);
        MarryCache marryCache = cache.get(roleId);
        if (marryCache.getProfress().containsKey(profressTarget)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.profressed")));
            return;
        }

        if (way == MarryManager.WAY_CLAIM) {
            if (target == null || claimQueue.contains(target) == false) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.claim.overtime")));
                return;
            }
            if (target.getReqLevel() > role.getLevel()) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.inconformity")));
                return;
            }
        }
        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(profressTarget, MConst.ForeShow);
        if (!fsSummary.isOpen(ForeShowConst.MARRY)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.target.locked")));
            return;
        }
        if (marryCache.getProfress().size() >= MarryManager.MAX_PROFRESS_LIMIT) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.max.profress.limit")));
            return;
        }
        if (null == target) {
            // 当被表白者marryrole数据不在内存时，load数据库，以后不删除
            // 因为这个数据也是很多都会常住内存，主要是宣言是每个人都可以发表引起的
            try {
                target = DBUtil.queryBean(DBUtil.DB_USER, MarryRole.class, "select * from marryrole where roleid = " + profressTarget);
            } catch (Exception e) {
                LogUtil.error("profress load data error");
            }
            if (null != target) {
                roleMap.put(profressTarget, target);
            }
        }
        if (null != target) {
            // 假如load数据的时候异常了，就让表白发出去
            if (null != target.getMarryKey() && (!"".equals(target.getMarryKey()))) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.other.not.single")));
                return;
            }
        }
        MarryCache otherCache = cache.get(profressTarget);
        if (null != otherCache) {
            if (otherCache.getProfressed().size() >= MarryManager.MAX_PROFRESSED) {
                // 直接写死，被表白控制在200(简单点处理，玩家数据在内存才判断，否则不判断了)
                // 这种不做缓存的，每次load数据性能有影响
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.max.profressed.limit")));
                return;
            }
        }
        Map<Integer, Integer> toolMap = StringUtil.toMap(DataManager.getCommConfig("marry_profess_singalcost"), Integer.class, Integer.class, '+', ',');
        MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_PROFRESS, MarryToolEvent.SUB, toolMap, profressTarget);
        ServiceHelper.roleService().notice(roleId, event);
        fireSpecialAccountEvent(roleId, roleId, "回应表白", true);
        fireSpecialAccountEvent(roleId, profressTarget, "表白被回应", false);
    }

    /**
     * 表白操作
     *
     * @param roleId
     * @param profressTarget
     */
    private void profress0(long roleId, long profressTarget) {
        MarryCache marryCache = cache.get(roleId);

        MarryProfress profress = new MarryProfress();
        profress.setTarget(profressTarget);
        profress.setProfressor(roleId);
        profress.setState(MarryManager.PROFRESS_SEND);
        profress.setUniquekey(roleId + "+" + profressTarget + "+" + DateUtil.getSecondTime());
        dao.insert(profress);

        // 缓存处理
        profressMap.put(profress.getUniquekey(), profress);
        marryCache.getProfress().put(profressTarget, profress);
        marryCache = cache.get(profressTarget);
        if (marryCache != null) {
            //int popularity = marryCache.getMarryRole().getPopularity() + 1;
            //marryCache.getMarryRole().setPopularity(popularity);
            marryCache.getProfressed().put(roleId, profress);
            //dao.update(marryCache.getMarryRole());
        } else {
            // 角色没有缓存数据，存放到临时数据防止角色登陆数据还没入库
            List<MarryProfress> list = profressTemp.get(profressTarget);
            if (list == null) {
                list = new ArrayList<>();
                profressTemp.put(profressTarget, list);
            }
            list.add(profress);
        }

        MarryRole target = roleMap.get(profressTarget);
        if (null != target) {
            int popularity = target.getPopularity() + 1;
            target.setPopularity(popularity);
            dao.update(target);
        }

        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.PROFRESS);
        res.setMaker(roleId);

        res.setProfressId(profressTarget);
        res.setProfressType(MarryManager.PROFRESS_SEND);
        PlayerUtil.send(roleId, res);

        if (online.contains(profressTarget)) {
            MarryCache mc = cache.get(profressTarget);
            if (mc != null && mc.getMarry() == null) {
                res.setProfressId(roleId);
                res.setProfressType(MarryManager.PROFRESS_SEND);
                PlayerUtil.send(profressTarget, res);
                fireProfressEvent(profressTarget);  // 表白红点
            }
        }
    }

    /**
     * 表白回应
     *
     * @param roleId
     * @param profressor
     * @param profressType
     */
    @Override
    public void profressResponse(long roleId, long profressor, byte profressType) {
        MarryCache marryCache = cache.get(roleId);
        if (marryCache.getProfressed().containsKey(profressor)) {
            MarryProfress profress = marryCache.getProfressed().get(profressor);
            if (profress.getState() > MarryManager.PROFRESS_SEND) { // 已经回应过
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.responsed")));
                return;
            }
            profress.setState(profressType);
            dao.delete(profress);
            // 清理该表白数据的缓存
            marryCache.getProfressed().remove(profressor);
            MarryCache proCache = cache.get(profressor);
            if (proCache != null) {
                proCache.getProfress().remove(roleId);
            } else {
                List<MarryProfress> list = profressRemoveMap.get(profressor);
                if (list == null) {
                    list = new ArrayList<>();
                    profressRemoveMap.put(profressor, list);
                }
                list.add(profress);
            }
            profressMap.remove(profress.getUniquekey());

            MarryRole role = roleMap.get(roleId);
            MarryRole other = roleMap.get(profressor);
            // 同意表白
            if (profressType == MarryManager.PROFRESS_SUCCESS) {
                if (!StringUtil.isEmpty(role.getMarryKey())) {
                    PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.not.singel")));
                    return;
                }
                if (!StringUtil.isEmpty(other.getMarryKey())) {
                    PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.other.not.single")));
                    /**
                     * 当对方已不是单身状态时需要通知到前端
                     *    这样前端才能做标记
                     */
                    ClientMarry res1 = new ClientMarry();
                    res1.setResType(ClientMarry.PROFRESS);
                    res1.setMaker(roleId);
                    res1.setProfressType(MarryManager.OTHER_NOT_SINGLE);
                    res1.setProfressId(profressor);
                    res1.setProfressName(other.getName());
                    res1.setProfressJob(other.getJobId());
                    res1.setProfressLevel(other.getLevel());
                    PlayerUtil.send(roleId, res1);
                    return;
                }

                Marry marry = new Marry();
                marry.setState(Marry.WAIT_WEDDING_ORDER);
                marry.setUniqueKey(profressor + "+" + roleId + "+" + DateUtil.getSecondTime());
                dao.insert(marry);
                marryMap.put(marry.getUniqueKey(), marry);

                role.setMarryKey(marry.getUniqueKey());
                role.setClaim("");
                role.setClaimStamp(0);
                dao.update(role);
                other.setMarryKey(marry.getUniqueKey());
                other.setClaim("");
                other.setClaimStamp(0);
                dao.update(other);

                marryCache.setMarry(marry);
                marryCache = cache.get(profressor);
                if (marryCache != null) {
                    marryCache.setMarry(marry);
                }

                // 清空表白墙
                claimQueue.remove(role);
                claimQueue.remove(other);

                // 触发结婚事件
                fireMarryEvent(role.getRoleId());
                fireMarryEvent(other.getRoleId());

                handleProfressCache(profressor);
                handleProfressCache(roleId);
                {
                    /**
                     * 表白同意后，如果对方不是自己好友，发送好友申请
                     */
                    FriendPo po = ServiceHelper.friendService().getFriendPo(roleId, profressor);
                    if (null == po) {
                        FriendApplicationPo applicationPo = new FriendApplicationPo();
                        applicationPo.setApplicantId(roleId);
                        applicationPo.setObjectId(profressor);
                        applicationPo.setApplicantName(role.getName());
                        applicationPo.setApplicantJobId(role.getJobId());
                        applicationPo.setApplicantLevel(role.getLevel());
                        applicationPo.setAppliedTimestamp(DateUtil.getSecondTime());
                        ServiceHelper.friendService().applyFriend(roleId, profressor, applicationPo);
                    }
                }
            }

            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.PROFRESS);
            res.setMaker(roleId);
            res.setProfressType(profressType);
            res.setProfressId(profressor);
            res.setProfressName(other.getName());
            res.setProfressJob(other.getJobId());
            res.setProfressLevel(other.getLevel());
            PlayerUtil.send(roleId, res);
            if (online.contains(profressor)) {
                res.setProfressId(roleId);
                res.setProfressName(role.getName());
                res.setProfressJob(role.getJobId());
                res.setProfressLevel(role.getLevel());
                PlayerUtil.send(profressor, res);
            }
        } else {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profress.outtime")));
            return;
        }
        fireProfressEvent(roleId);
        fireSpecialAccountEvent(roleId, roleId, "回应表白", true);
        fireSpecialAccountEvent(roleId, profressor, "表白被回应", false);
    }

    /**
     * 清掉玩家的所有被表白信息
     *
     * @param roleId
     */

    public void handleProfressCache(long roleId) {
        MarryCache roleIdCache = cache.get(roleId);
        if (null == roleIdCache)
            return;
        Map<Long, MarryProfress> profressedMap = roleIdCache.getProfressed();
        if (null == profressedMap)
            return;
        for (Map.Entry<Long, MarryProfress> m : profressedMap.entrySet()) {
            MarryCache tmpCache = cache.get(m.getValue().getProfressor());
            if (null != tmpCache) {
                tmpCache.getProfress().remove(roleId);
            } else {
                // 防止玩家load数据的时候，数据还没有保存
                List<MarryProfress> list = profressRemoveMap.get(m.getValue().getProfressor());
                if (list == null) {
                    list = new ArrayList<>();
                    profressRemoveMap.put(m.getValue().getProfressor(), list);
                }
                list.add(m.getValue());
            }
            m.getValue().setState(MarryManager.PROFRESS_FAILED);
            dao.delete(m.getValue());
            profressMap.remove(m.getKey());
        }
        profressedMap.clear();
    }

    /**
     * 表白列表
     *
     * @param roleId
     */
    @Override
    public void profressList(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        MarryRole role = marryCache.getMarryRole();
        List<MarryRole> list = new ArrayList<>();
        // 最多200条,太多就不显示了
        int count = MarryManager.MAX_PROFRESSED;
        for (Long id : marryCache.getProfressed().keySet()) {
            list.add(roleMap.get(id));
            count--;
            if (count <= 0) {
                break;
            }
        }
        if (DateUtil.getSecondTime() > role.getClaimStamp() + MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC) {
            role.setClaim("");
            dao.update(role);
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.PROFRESS_LIST);
        res.setSelf(role);
        res.setProfressList(list);
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "正在请求表白列表", true);
    }

    private void fireSpecialAccountEvent(long selfId, long roleId, String content, boolean self) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(selfId, new SpecialAccountEvent(roleId, content, self));
        }
    }

    /**
     * 预约婚礼
     *
     * @param roleId
     * @param gender
     * @param reqType
     */
    @Override
    public void appointment(long roleId, byte gender, byte reqType) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();

        if (null == marry) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }

        long other = marry.getOther(roleId);
        if (!online.contains(other)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.other.offline")));
            return;
        }

        if (reqType == MarryManager.GENERAL_WEDDING) {
            // 普通预约只能一次
            if (marry == null || marry.getState() != Marry.WAIT_WEDDING_ORDER) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.appointment.failed")));
                return;
            }
        } else {
            // 豪华婚礼只能预约一次（如果开始是普通预约可以补办一次豪华预约）
            if (marry == null || (marry.getState() == Marry.MARRIED && marry.getLastSuccessAppointTyte() == marry.LUXURIOUS_WEDDING)) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.appointment.failed")));
                return;
            }
            // 同时举办婚宴做限制
            if (currentWeddingMap.size() >= MarryManager.MAX_WEDDING) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.max.wedding.limit")));
                return;
            }
        }

        int deadline = DataManager.getCommConfig("marry_appointment_delay", 60);
        if (DateUtil.getSecondTime() - marry.getAppointStamp() < deadline) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.appointment.cd")));
            return;
        }

        {
            // 这个时候检查是否之前有预约却没有被处理(超时)
            if (activeMarryMap.containsKey(marry.getUniqueKey())) {
                handleOutTimeOrRefuseAppoint(marry.getUniqueKey());
            }
        }

        {
            // 好友亲密度判断
            if (reqType == MarryManager.GENERAL_WEDDING) {
                int friendShipCost = DataManager.getCommConfig("marry_profess_friendshipnum", 100);
                FriendPo po = ServiceHelper.friendService().getFriendPo(roleId, other);
                if (null == po) {
                    PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profess.btninfo.notfriend")));
                    return;
                }
                if (po.getIntimacy() < friendShipCost) {
                    PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profess.btninfo.notfriendship")));
                    return;
                }
            }
            if (reqType == MarryManager.LUXURIOUS_WEDDING) {
                // 豪华预约检查是否是好友
                List<Long> friendList = ServiceHelper.friendService().getFriendList(roleId);
                if (!friendList.contains(other)) {
                    PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profess.btninfo.notfriend")));
                    return;
                }
            }
        }

        {
            // 判断被预约玩家是否在城镇区
            MarryAppointSceneCheckEvent event = new MarryAppointSceneCheckEvent(roleId, other, gender, reqType);
            ServiceHelper.roleService().notice(other, event);
        }
    }

    public void appointSceneCheckBack(long roleId, byte gender, byte reqType, boolean result) {
        if (!result) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.profess.btninfo.notincity")));
            return;
        }
        /**
         // 根据预约类型扣取物品
         String[] cost = DataManager.getCommConfig("marry_appointment_cost").split("[|]");
         Map<Integer, Integer> toolMap;
         if (reqType == MarryManager.GENERAL_WEDDING) {
         toolMap = StringUtil.toMap(cost[0], Integer.class, Integer.class, '+', ',');
         } else {
         toolMap = StringUtil.toMap(cost[1], Integer.class, Integer.class, '+', ',');
         }
         **/
        Byte_Byte ex = new Byte_Byte();
        ex.byte1 = gender;
        ex.byte2 = reqType;
        //byte costType = (reqType == MarryManager.GENERAL_WEDDING) ? MarryManager.TOOL_OPERATOR_APPOINT_GENERAL : MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS;
        //MarryToolEvent event = new MarryToolEvent(costType, MarryToolEvent.SUB, toolMap, ex);
        //ServiceHelper.roleService().notice(roleId, event);
        appointment0(roleId, ex);
        fireSpecialAccountEvent(roleId, roleId, "预约婚礼", true);
    }

    /**
     * 处理拒绝预约或者预约超时
     *
     * @param key
     */

    private void handleOutTimeOrRefuseAppoint(String key) {
        Marry marry = activeMarryMap.get(key);
        activeMarryMap.remove(key);
        if (null == marry) return;
        // 预约被拒绝补偿
        /**
         {
         long roleId = marry.getAppointRole();
         if (roleId == 0) return;
         long other = marry.getOther(roleId);
         byte type = marry.getAppointByte();
         String[] cost = DataManager.getCommConfig("marry_appointment_cost").split("[|]");
         Map<Integer, Integer> toolMap;
         byte returnType = 0;
         if (type == MarryManager.GENERAL_WEDDING) {
         toolMap = StringUtil.toMap(cost[0], Integer.class, Integer.class, '+', ',');
         returnType = MarryManager.TOOL_APPOINT_RETURN_COST;
         } else {
         toolMap = StringUtil.toMap(cost[1], Integer.class, Integer.class, '+', ',');
         returnType = MarryManager.TOOL_APPOINT_LUXURIOUS_RETURN_COST;
         }
         if (online.contains(roleId)) {
         // 玩家在线，直接获得
         MarryToolEvent event = new MarryToolEvent(returnType, MarryToolEvent.ADD, toolMap, null);
         ServiceHelper.roleService().notice(roleId, event);
         } else {
         // 玩家不在线，以邮件下发
         if (type == MarryManager.GENERAL_WEDDING) {
         ServiceHelper.emailService().sendToSingle(roleId, 22007, 0L,
         "系统", toolMap, roleMap.get(other).getName());
         } else {
         ServiceHelper.emailService().sendToSingle(roleId, 22008, 0L,
         "系统", toolMap, roleMap.get(other).getName());
         }
         }
         }
         **/
        marry.setAppointStamp(0);
        if (marry.getState() != Marry.MARRIED) {
            marry.setMan(0);
            marry.setWoman(0);
        }
        marry.setAppointRole(0);
        marry.setAppointByte((byte) 0);
        dao.update(marry);
    }

    public class Byte_Byte {
        public byte byte1;
        public byte byte2;
    }

    private void appointment0(long roleId, Byte_Byte ex) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        if (null == marry) {
            // 这种极限情况做容错处理(这种不在一个线程处理很琐碎)
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            LogUtil.error("roleId:{} appoint{} fail,cost not return", roleId, ex.byte1);
            return;
        }
        long other = marry.getOther(roleId);
        // 设置预约信息
        marry.setAppointStamp(DateUtil.getSecondTime());
        marry.setAppointRole(roleId);
        marry.setAppointByte(ex.byte2);
        // 设置性别
        if (ex.byte1 == MarryManager.MAN) {
            marry.setMan(roleId);
            marry.setWoman(other);
        } else {
            marry.setMan(other);
            marry.setWoman(roleId);
        }
        dao.update(marry);
        activeMarryMap.put(marry.getUniqueKey(), marry);

        MarryCache otherCache = cache.get(other);
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.APPOINTMENT);
        res.setAppointmentId(roleId);
        res.setAppointmentType(ex.byte2);
        res.setAppointmentName(otherCache.getMarryRole().getName());
        res.setGender(ex.byte1);
        res.setMarryKey("");       // 公用消息，空串处理
        PlayerUtil.send(roleId, res);
        if (ex.byte1 == MarryManager.MAN) {
            res.setGender(MarryManager.WOMAN);
        } else {
            res.setGender(MarryManager.MAN);
        }
        res.setAppointmentName(marryCache.getMarryRole().getName());
        PlayerUtil.send(other, res);

        appointmentLog(roleId, marry);
        // 触发结婚事件
        fireMarryEvent(roleId);
        fireMarryEvent(other);
    }

    /**
     * 结婚日志
     *
     * @param roleId
     * @param marry
     */

    private void appointmentLog(long roleId, Marry marry) {
        String[] cost = DataManager.getCommConfig("marry_appointment_cost").split("[|]");
        Map<Integer, Integer> toolMap;
        byte weddingType = 0;
        if (marry.getAppointByte() == MarryManager.GENERAL_WEDDING) {
            toolMap = StringUtil.toMap(cost[0], Integer.class, Integer.class, '+', ',');
            weddingType = 1;
        } else {
            toolMap = StringUtil.toMap(cost[1], Integer.class, Integer.class, '+', ',');
            weddingType = 2;
        }
        StringBuilder logStr = new StringBuilder();
        logStr.append("wedding_type:")
                .append(weddingType)
                .append("#consume@num:");
        if (null != toolMap && toolMap.size() > 0) {
            String subStr = null;
            for (Map.Entry<Integer, Integer> m : toolMap.entrySet()) {
                if (null == subStr) {
                    subStr = String.format("%d@%d", m.getKey(), m.getValue());
                } else {
                    subStr = subStr + "&" + String.format("%d@%d", m.getKey(), m.getValue());
                }
            }
            if (null != subStr) {
                logStr.append(subStr);
            }
        }
        MarryLogEvent event = new MarryLogEvent("wedding", logStr.toString(), "" + marry.getOther(roleId));
        ServiceHelper.roleService().notice(roleId, event);
    }

    /**
     * 回应预约
     * 改版后普通和豪华都需要预约,在有效期内玩家：
     * 2、同意预约（玩家必须在线，这个时候扣取预约消耗，成功后奖励以邮件下发）
     * a.普通预约 不做特殊逻辑处理（简单通知前端）
     * b.豪华预约 把在线玩家拉到宴会场景
     * 3、拒绝预约
     *
     * @param roleId
     * @param type
     */
    @Override
    public void appointmentResPonse(long roleId, byte type) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        if (null == marry) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }
        long other = marry.getOther(roleId);

        MarryWedding marryWedding = currentWeddingMap.get(marry.getUniqueKey());
        if (marry == null || (null != marryWedding && marryWedding.getState() == marryWedding.RUN)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.appointment.already.deal")));
            return;
        }
        int deadline = DataManager.getCommConfig("marry_appointment_delay", 60);
        if (marry.getAppointStamp() + deadline < DateUtil.getSecondTime()) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.appointment.overtime")));
            return;
        }
        // 预约与应答不一致判断
        if (type == MarryManager.AGREE_WEDDING || type == MarryManager.REFUSE_WEDDING) {
            if (marry.getAppointByte() != MarryManager.GENERAL_WEDDING)
                return;
            if (marry.getState() == marry.MARRIED)
                return;
        }

        if (type == MarryManager.LUXURIOUS_AGREE_WEDDING || type == MarryManager.LUXURIOUS_REFUSE_WEDDING) {
            if (marry.getAppointByte() != MarryManager.LUXURIOUS_WEDDING)
                return;
            if (marry.getState() == marry.MARRIED && marry.getLastSuccessAppointTyte() == marry.LUXURIOUS_WEDDING)
                return;
        }

        if (roleId == marry.getAppointRole()) {
            // 自己预约自己应答
            return;
        }

        if (type == MarryManager.AGREE_WEDDING || type == MarryManager.LUXURIOUS_AGREE_WEDDING) {
            // 提出预约者必须在线
            if (!online.contains(other)) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.other.offline")));
                return;
            }
            // 根据预约类型扣取物品
            String[] cost = DataManager.getCommConfig("marry_appointment_cost").split("[|]");
            Map<Integer, Integer> toolMap;
            if (marry.getAppointByte() == MarryManager.GENERAL_WEDDING) {
                toolMap = StringUtil.toMap(cost[0], Integer.class, Integer.class, '+', ',');
            } else {
                toolMap = StringUtil.toMap(cost[1], Integer.class, Integer.class, '+', ',');
            }
            byte costType = (marry.getAppointByte() == MarryManager.GENERAL_WEDDING) ? MarryManager.TOOL_OPERATOR_APPOINT_GENERAL : MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS;
            MarryToolEvent event = new MarryToolEvent(costType, MarryToolEvent.SUB, toolMap, null);
            ServiceHelper.roleService().notice(other, event);
            return;
        }

        if (type == MarryManager.REFUSE_WEDDING || type == MarryManager.LUXURIOUS_REFUSE_WEDDING) {
            handleOutTimeOrRefuseAppoint(marry.getUniqueKey());
            // 通知双方状态
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.APPOINTMENT);
            res.setAppointmentId(roleId);
            res.setAppointmentType(type);
            res.setAppointmentName(roleMap.get(marry.getOther(roleId)).getName());
            res.setMarryKey(marry.getUniqueKey());
            PlayerUtil.send(roleId, res);
            res.setAppointmentName(marryCache.getMarryRole().getName());
            PlayerUtil.send(other, res);
            fireSpecialAccountEvent(roleId, roleId, "回应婚礼预约", true);
        }
    }

    /**
     * @param order     预约者
     * @param responser 应答者
     * @param type
     * @param result
     */

    public void appointmentResPonse0(long order, long responser, byte type, int result) {
        MarryCache mc = cache.get(responser);
        Marry marry = mc.getMarry();
        if (null == marry) {
            return;
        }
        {
            if (0 == result) {
                PlayerUtil.send(responser, new ClientText("对方货币不足,无法举办婚礼"));
                // 当拒绝处理,清掉一些状态
                handleOutTimeOrRefuseAppoint(marry.getUniqueKey());
                return;
            }
            if (marry.getState() != Marry.MARRIED) {
                marry.setState(Marry.MARRIED);
                marry.setMarryStamp(DateUtil.getSecondTime());
                if (type == MarryManager.AGREE_WEDDING) {
                    marry.setLastSuccessAppointTyte(Marry.NOMAL_WEDDING);
                } else if (type == MarryManager.LUXURIOUS_AGREE_WEDDING) {
                    marry.setLastSuccessAppointTyte(Marry.LUXURIOUS_WEDDING);
                }
                marry.setShipValue(DataManager.getCommConfig("marry_comradeship_num", 100));
                dao.update(marry);
            } else {
                // 补办豪华婚宴
                marry.setLastSuccessAppointTyte(Marry.LUXURIOUS_WEDDING);
                dao.update(marry);
            }
            fireMarryEvent(responser);
            fireMarryEvent(order);
            fireMarryBattleEvent(responser);
            fireMarryBattleEvent(order);

            activeMarryMap.remove(marry.getUniqueKey());

            if (type == MarryManager.AGREE_WEDDING) {
                nomalWeddingAward(order, marry);
            }
            if (type == MarryManager.LUXURIOUS_AGREE_WEDDING) {
                luxuriousWeddingAward(order, marry);
            }

            if (type == MarryManager.LUXURIOUS_AGREE_WEDDING) {
                // 豪华预约同意，创建婚宴实体
                MarryWedding wedding = new MarryWedding();
                wedding.setMarryKey(marry.getUniqueKey());
                wedding.setState(MarryWedding.RUN);
                wedding.setStartStamp(DateUtil.getSecondTime());
                wedding.setOrder(order);
                wedding.setOther(responser);
                currentWeddingMap.put(wedding.getMarryKey(), wedding);
                currentWeddingList.addFirst(wedding);
                annoncementNotice(order, marry, wedding);
            }

            {
                // 通知双方状态
                ClientMarry res = new ClientMarry();
                res.setResType(ClientMarry.APPOINTMENT);
                res.setAppointmentId(responser);
                res.setAppointmentType(type);
                res.setAppointmentName(roleMap.get(order).getName());
                res.setMarryKey(marry.getUniqueKey());
                PlayerUtil.send(responser, res);
                res.setAppointmentName(roleMap.get(responser).getName());
                PlayerUtil.send(order, res);
                fireSpecialAccountEvent(order, responser, "回应婚礼预约", true);
            }
        }
    }

    /**
     * 普通婚礼奖励
     *
     * @param roleId
     * @param marry
     */
    private void nomalWeddingAward(long roleId, Marry marry) {
        String[] awards = DataManager.getCommConfig("marry_appointment_sucreward").split("[|]");
        Map<Integer, Integer> tool = ToolManager.parseString(awards[0]);
        MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_APPOINT_AWARD, MarryToolEvent.ADD, tool, null);
        long other = marry.getOther(roleId);
        if (online.contains(roleId)) {
            ServiceHelper.roleService().notice(roleId, event);
        } else {
            ServiceHelper.emailService().sendToSingle(roleId, 22009, 0L,
                    "系统", tool, roleMap.get(other).getName());
        }

        // long other = marry.getOther(roleId);
        ServiceHelper.roleService().notice(other, event);
    }

    /**
     * 豪华婚礼奖励
     *
     * @param roleId
     * @param marry
     */
    private void luxuriousWeddingAward(long roleId, Marry marry) {
        String[] awards = DataManager.getCommConfig("marry_appointment_sucreward").split("[|]");
        Map<Integer, Integer> tool = ToolManager.parseString(awards[1]);
        MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS_AWARD, MarryToolEvent.ADD, tool, null);
        long other = marry.getOther(roleId);
        if (online.contains(roleId)) {
            ServiceHelper.roleService().notice(roleId, event);
        } else {
            ServiceHelper.emailService().sendToSingle(roleId, 22010, 0L,
                    "系统", tool, roleMap.get(other).getName());
        }
        //long other = marry.getOther(roleId);
        ServiceHelper.roleService().notice(other, event);
    }

    /**
     * 豪华婚礼滚动条通知
     */
    private void annoncementNotice(long roleId, Marry marry, MarryWedding wedding) {
        long other = marry.getOther(roleId);
        ServiceHelper.chatService().announce("marry_party_joinattention", roleMap.get(roleId).getName(), roleMap.get(other).getName());
    }

    /**
     * 预约豪华婚礼
     * 改版后不再需要预约时间段（只需两方同意）
     *
     * @param roleId
     * @param gender
     */
    @Override
    public void appointmentLuxurious(long roleId, byte gender) {
        /**
         if (dayOffset < 0 || index < 0) {
         return;
         }
         Marry marry = cache.get(roleId).getMarry();
         if (marry == null) {
         return;
         }
         if (marry.getState() != Marry.WAIT_WEDDING_ORDER &&
         marry.getState() != Marry.MARRIED) {
         send(roleId, new ClientText(I18n.get("marry.luxurious.appoint")));
         return;
         }
         Calendar calendar = Calendar.getInstance();
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.add(Calendar.DATE, dayOffset);
         int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
         dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
         int ymd = Integer.valueOf(new SimpleDateFormat("yyyyMMdd").format(calendar.getTime()));

         if (ymd < DateUtil.getYMDInt()) {
         send(roleId, new ClientText(I18n.get("marry.luxurious.appoint.deadline")));
         return;
         }

         String s = DataManager.getCommConfig("marry_appointment_time");
         Map<Integer, String> indexMap = StringUtil.toMap(s, Integer.class, String.class, ',', '|');
         LinkedList<MarryWedding> list = weddingMap.get(ymd);
         if (indexMap.containsKey(dayOfWeek)) {
         String[] indexs = indexMap.get(dayOfWeek).split("[+]");
         if (index < indexs.length) {
         if (list == null) {
         list = new LinkedList<>();
         weddingMap.put(ymd, list);
         }
         int hms = Integer.valueOf(indexs[index].replace(":", "")) * 100;

         for (MarryWedding mw : list) {
         if (mw.getHms() == hms) {
         send(roleId, new ClientText(I18n.get("marry.luxurious.later")));
         return;
         }
         }

         if (ymd == DateUtil.getYMDInt() && getSubTimeFromHMS(hms, DateUtil.getHMSInt()) <= DataManager.getCommConfig("marry_appoint_overtime", 60 * 30)) {
         send(roleId, new ClientText(I18n.get("marry.luxurious.appoint.deadline")));
         return;
         }

         MarryWedding marryWedding = new MarryWedding();
         marryWedding.setDayOfWeek((byte) (dayOfWeek));
         marryWedding.setHms(hms);
         marryWedding.setOrder(roleId);
         marryWedding.setState(MarryWedding.WAIT);
         marryWedding.setOther(marry.getOther(roleId));
         marryWedding.setMarryKey(marry.getUniqueKey());
         marryWedding.setYmd(ymd);

         String[] cost = DataManager.getCommConfig("marry_appointment_cost").split("[|]");
         Map<Integer, Integer> toolMap = StringUtil.toMap(cost[1], Integer.class, Integer.class, '+', '|');
         MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS, MarryToolEvent.SUB, toolMap, marryWedding);
         ServiceHelper.roleService().notice(roleId, event);
         }
         }
         fireSpecialAccountEvent(roleId, roleId, "预约豪华婚礼", true);
         **/
    }

    private void appointmentLuxurious0(long roleId, MarryWedding marryWedding) {
        /**
         Marry marry = cache.get(roleId).getMarry();
         dao.insert(marryWedding);
         marry.setState(Marry.WAIT_WEDDING);
         dao.update(marry);
         weddingQueryMap.put(marryWedding.getMarryKey(), marryWedding);

         List<MarryWedding> list = weddingMap.get(marryWedding.getYmd());
         int insert = 0;
         for (MarryWedding wedding : list) {
         if (wedding.getHms() > marryWedding.getHms()) {
         break;
         }
         insert++;
         }
         list.add(insert, marryWedding);
         send(roleId, new ClientText(I18n.get("marry.luxurious.success")));
         ClientMarry res = new ClientMarry();
         res.setResType(ClientMarry.APPOINTMENT);
         res.setAppointmentType(MarryManager.LUXURIOUS_WEDDING);
         res.setAppointmentId(roleId);
         res.setAppointmentName(roleMap.get(roleId).getName());
         res.setAppointStamp(getBeginStampFromWedding(marryWedding));
         send(roleId, res);
         if (online.contains(marry.getOther(roleId))) {
         send(marry.getOther(roleId), res);
         }

         // 发奖励
         String[] awards = DataManager.getCommConfig("marry_appointment_sucreward").split("[|]");
         Map<Integer, Integer> tool = ToolManager.parseString(awards[1]);
         MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS_AWARD, MarryToolEvent.ADD, tool, null);
         ServiceHelper.roleService().notice(roleId, event);
         Map<Integer, Integer> tool1 = ToolManager.parseString(awards[1]);
         ServiceHelper.emailService().sendToSingle(marry.getOther(roleId), 22005, 0L,
         I18n.get("marry.appoint.luxurious.email"), tool1, roleMap.get(roleId).getName());


         // 发邮件，顺序：预约方好友->预约方家族成员->对方好友->对方家族成员
         String date = "";
         String hms = "";
         try {
         date = DateUtil.formatDate(new SimpleDateFormat("yyyyMMdd").parse(String.valueOf(marryWedding.getYmd())), "yyyy-MM-dd");
         StringBuilder builder = new StringBuilder();
         if (marryWedding.getHms() < 100000) {
         builder.append(0);
         }
         builder.append(marryWedding.getHms());
         hms = DateUtil.formatDate(new SimpleDateFormat("HHmmss").parse(builder.toString()), "HH:mm");
         } catch (ParseException e) {
         LogUtil.error("", e);
         }
         Set<Long> roleIdSet = new HashSet<>();
         List<Long> sendList = ServiceHelper.friendService().getFriendList(marryWedding.getOrder());
         int dayOfWeek = marryWedding.getDayOfWeek();
         roleIdSet.add(roleId);
         roleIdSet.add(marry.getOther(roleId));
         for (long id : sendList) {
         if (roleIdSet.contains(id)) {
         continue;
         }
         ServiceHelper.emailService().sendToSingle(id, DataManager.getCommConfig("marry_appointment_emailid_friend", 22001), 0L,
         I18n.get("marry.luxurious.wedding"), null, roleMap.get(marryWedding.getOrder()).getName(), roleMap.get(marryWedding.getOther()).getName(),
         date, String.valueOf(dayOfWeek), hms);
         }
         roleIdSet.addAll(sendList);
         long orderFamily = ServiceHelper.familyRoleService().getFamilyId(marryWedding.getOrder());
         sendList = ServiceHelper.familyMainService().getMemberIdList(orderFamily, marryWedding.getOrder());
         for (long id : sendList) {
         if (roleIdSet.contains(id)) {
         continue;
         }
         ServiceHelper.emailService().sendToSingle(id, DataManager.getCommConfig("marry_appointment_emailid_family", 22002), 0L,
         I18n.get("marry.luxurious.wedding"), null, roleMap.get(marryWedding.getOrder()).getName(), roleMap.get(marryWedding.getOther()).getName(),
         date, String.valueOf(dayOfWeek), hms);
         }
         roleIdSet.addAll(sendList);
         sendList = ServiceHelper.friendService().getFriendList(marryWedding.getOther());
         for (long id : sendList) {
         if (roleIdSet.contains(id)) {
         continue;
         }
         ServiceHelper.emailService().sendToSingle(id, DataManager.getCommConfig("marry_appointment_emailid_friend", 22001), 0L,
         I18n.get("marry.luxurious.wedding"), null, roleMap.get(marryWedding.getOther()).getName(), roleMap.get(marryWedding.getOrder()).getName(),
         date, String.valueOf(dayOfWeek), hms);
         }
         roleIdSet.addAll(sendList);
         long otherfamily = ServiceHelper.familyRoleService().getFamilyId(marryWedding.getOther());
         sendList = ServiceHelper.familyMainService().getMemberIdList(otherfamily, marryWedding.getOther());
         for (long id : sendList) {
         if (roleIdSet.contains(id)) {
         continue;
         }
         ServiceHelper.emailService().sendToSingle(id, DataManager.getCommConfig("marry_appointment_emailid_family", 22002), 0L,
         I18n.get("marry.luxurious.wedding"), null, roleMap.get(marryWedding.getOther()).getName(), roleMap.get(marryWedding.getOrder()).getName(),
         date, String.valueOf(dayOfWeek), hms);
         }

         // 触发结婚事件
         fireMarryEvent(roleId);
         fireMarryEvent(marry.getOther(roleId));
         **/
    }

    /**
     * 获取角色状态
     *
     * @param roleId
     */
    @Override
    public void getState(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        byte iswedding = 0;
        MarryWedding wedding = currentWeddingMap.get(marry == null ? "" : marry.getUniqueKey());
        if (wedding != null && wedding.getState() == MarryWedding.RUN) {
            iswedding = 1;
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.STATE);
        res.setIsWedding(iswedding);
        res.setState(marry == null ? -1 : marry.getState());
        res.setTargetList(marryCache.getProfress().keySet());
        res.setOtherName(marry == null ? "" : roleMap.get(marry.getOther(roleId)).getName());
        res.setMarryKey(marry == null ? "" : marry.getUniqueKey());
        byte gender = 0;
        if (null != marry) {
            if (marry.getMan() == roleId) gender = 1;
            if (marry.getWoman() == roleId) gender = 2;
        }
        byte isSpecailAccount = (byte) (SpecialAccountManager.isSpecialAccount(roleId) ? 1 : 0);
        res.setIsSpecailAccount(isSpecailAccount);
        res.setGender(gender);
        PlayerUtil.send(roleId, res);
    }

    /**
     * 决裂
     *
     * @param roleId
     * @param breakType
     */
    @Override
    public void breakMarry(long roleId, byte breakType) {
        Marry marry = cache.get(roleId).getMarry();
        if (marry == null || marry.getBreakState() == MarryManager.BREAK_STATE_OVER) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }

        if (currentWeddingMap.containsKey(marry.getUniqueKey())) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.break.wedding")));
            return;
        }

        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.BREAK_MARRY);
        res.setBreakName(roleMap.get(marry.getOther(roleId)).getName());
        res.setBreakId(roleId);
        res.setMarryState(marry.getState());

        if (breakType == MarryManager.BREAK_BYE) {
            if (marry.getState() <= Marry.WAIT_WEDDING_ORDER) {
                // 接受表白，但是还没真正结婚
                marry.setBreakState(MarryManager.BREAK_STATE_OVER);
                marry.setBreaker(roleId);
                marry.addBreakCount();
                marry.setLastBreakStamp(DateUtil.getSecondTime());
                dao.update(marry);

                // 清理缓存
                cleanCacheAfterBreak(roleId);

                res.setBreakType(MarryManager.BREAK_BYE);
                PlayerUtil.send(roleId, res);
                if (online.contains(marry.getOther(roleId))) {
                    res.setBreakName(roleMap.get(roleId).getName());
                    PlayerUtil.send(marry.getOther(roleId), res);
                }
                breakLog(roleId, breakType, marry);
            }
        }

        if (breakType == MarryManager.BREAK_GENERAL) {
            int cd = getBreakCD(marry);
            if (marry.getLastBreakStamp() + cd > DateUtil.getSecondTime()) {
                PlayerUtil.send(roleId, new ClientText(I18n.get("marry.break.cd")));
                return;
            }
            String[] costs = DataManager.getCommConfig("marry_breakship_normalcost").split("[|]");
            String toolString = "";
            for (String costStr : costs) {
                String[] cost = costStr.split("[,]");
                toolString = cost[1];
                if (marry.getBreakCount() + 1 > Integer.valueOf(cost[0])) {
                    continue;
                }
                break;
            }
            Map<Integer, Integer> toolMap = StringUtil.toMap(toolString, Integer.class, Integer.class, '+', ',');
            MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_BREAK_GENERAL, MarryToolEvent.SUB, toolMap, null);
            ServiceHelper.roleService().notice(roleId, event);
        }

        if (breakType == MarryManager.BREAK_FORCE) {
            String cost = DataManager.getCommConfig("marry_breakship_forcecost");
            Map<Integer, Integer> toolMap = StringUtil.toMap(cost, Integer.class, Integer.class, '+', ',');
            MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_BREAK_FORCE, MarryToolEvent.SUB, toolMap, null);
            ServiceHelper.roleService().notice(roleId, event);
        }

        if (breakType == MarryManager.BREAK_AGREE) {
            marry.setBreakState(MarryManager.BREAK_STATE_OVER);
            dao.update(marry);

            cleanCacheAfterBreak(roleId);

            res.setBreakType(MarryManager.BREAK_AGREE);
            PlayerUtil.send(roleId, res);
            if (online.contains(marry.getOther(roleId))) {
                PlayerUtil.send(marry.getOther(roleId), res);
            }
        }

        if (breakType == MarryManager.BREAK_REFUSE) {
            if (marry.getBreakState() != MarryManager.BREAK_STATE_PROPOSE) {
                return;
            }
            marry.setBreakState(MarryManager.BREAK_STATE_REFUSE);
            dao.update(marry);
            res.setBreakType(MarryManager.BREAK_REFUSE);
            PlayerUtil.send(roleId, res);
            if (online.contains(marry.getOther(roleId))) {
                PlayerUtil.send(marry.getOther(roleId), res);
                PlayerUtil.send(marry.getOther(roleId), new ClientText("marry_breakship_fail",
                        roleMap.get(roleId).getName(), String.valueOf(getBreakCD(marry) / 3600)));
            }
        }
        fireSpecialAccountEvent(roleId, roleId, "婚宴决裂", true);
    }

    /**
     * 获取决裂cd
     *
     * @param marry
     * @return
     */
    private int getBreakCD(Marry marry) {
        int cdTime = 0;
        String[] cds = DataManager.getCommConfig("marry_breakship_cdtime").split("[,]");
        for (String cdStr : cds) {
            String[] cd = cdStr.split("[+]");
            cdTime = Integer.valueOf(cd[1]);
            if (marry.getBreakCount() > Integer.valueOf(cd[0])) {
                continue;
            }
            break;
        }
        return cdTime;
    }

    /**
     * 预约信息
     *
     * @param roleId
     */
    @Override
    public void appointmentInfo(long roleId) {
        /**
         Marry marry = cache.get(roleId).getMarry();
         if (marry == null) {
         send(roleId, new ClientText(I18n.get("marry.info.single")));
         return;
         }
         if (weddingQueryMap.containsKey(marry.getUniqueKey())) {
         send(roleId, new ClientText(I18n.get("marry.wedding.already.appoint")));
         return;
         }
         Calendar calender = Calendar.getInstance();
         calender.setTimeInMillis(System.currentTimeMillis());
         calender.setFirstDayOfWeek(Calendar.MONDAY);
         int dayOfWeek = calender.get(Calendar.DAY_OF_WEEK) - 1;
         dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
         int ymd = DateUtil.getYMDInt();

         Map<Integer, String> appointed = new HashMap<>();
         StringBuilder builder = new StringBuilder();
         for (Map.Entry<Integer, LinkedList<MarryWedding>> entry : weddingMap.entrySet()) {
         builder.delete(0, builder.length());
         for (MarryWedding mw : entry.getValue()) {
         builder.append(mw.getHms()).append("=")
         .append(roleMap.get(mw.getOrder()).getName())
         .append("&")
         .append(roleMap.get(mw.getOther()).getName())
         .append("+");
         }
         if (luxuriousWedding != null && luxuriousWedding.getYmd() == entry.getKey().intValue()) {
         builder.append(luxuriousWedding.getHms()).append("=")
         .append(roleMap.get(luxuriousWedding.getOrder()).getName())
         .append("&")
         .append(roleMap.get(luxuriousWedding.getOther()).getName())
         .append("+");
         }
         if (builder.length() != 0) {
         builder.delete(builder.length() - 1, builder.length());
         appointed.put(entry.getKey() - ymd, builder.toString());
         }
         }

         ClientMarry res = new ClientMarry();
         res.setResType(ClientMarry.APPOINTMENT_INFO);
         res.setTimeStamp(DateUtil.getSecondTime());
         res.setDayOfWeek((byte) dayOfWeek);
         res.setAppointmentInfo(appointed);
         send(roleId, res);
         fireSpecialAccountEvent(roleId, roleId, "获取预约信息", true);
         **/
    }

    /**
     * 道具操作的一些回调
     *
     * @param roleId
     * @param type
     * @param arg
     */
    @Override
    public void toolSubCallback(long roleId, byte type, Object arg) {

        if (type == MarryManager.TOOL_OPERATOR_BREAK_GENERAL) {
            handleGeneralBreak(roleId);
        }

        if (type == MarryManager.TOOL_OPERATOR_BREAK_FORCE) {
            handleForceBreak(roleId);
        }

        if (type == MarryManager.TOOL_OPERATOR_FIREWORKS) {
            fireworks0(roleId, (String) arg);
        }

        if (type == MarryManager.TOOL_OPERATOR_REDBAG_SEND) {
            sendRedbag0(roleId, (String) arg);
        }

        if (type == MarryManager.TOOL_OPERATOR_PROFRESS) {
            long profressTarget = (Long) arg;
            profress0(roleId, profressTarget);
        }

        if (type == MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS || type == MarryManager.TOOL_OPERATOR_APPOINT_GENERAL) {
            MarryCache mc = cache.get(roleId);
            Marry marry = mc.getMarry();
            if (null == marry) {
                // 这种极限情况做容错处理(这种不在一个线程处理很琐碎)
                LogUtil.error("roleId:{} response{} fail,cost not return", roleId, type);
                return;
            }
            byte responseType = MarryManager.AGREE_WEDDING;
            if (type == MarryManager.TOOL_OPERATOR_APPOINT_LUXURIOUS)
                responseType = MarryManager.LUXURIOUS_AGREE_WEDDING;
            long other = marry.getOther(roleId);
            appointmentResPonse0(roleId, other, responseType, (Integer) arg);
        }
    }

    /**
     * 处理普通决裂
     *
     * @param roleId
     */
    private void handleGeneralBreak(long roleId) {
        MarryCache mc = cache.get(roleId);
        Marry marry = mc.getMarry();
        if (null == marry) {
            LogUtil.error("roleId:{} break:{} fail,not return cost", roleId, MarryManager.TOOL_OPERATOR_BREAK_GENERAL);
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }
        breakLog(roleId, MarryManager.BREAK_GENERAL, marry);
        marry.setBreaker(roleId);
        marry.addBreakCount();
        marry.setBreakState(MarryManager.BREAK_STATE_PROPOSE);
        marry.setLastBreakStamp(DateUtil.getSecondTime());
        dao.update(marry);

        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.BREAK_MARRY);
        res.setBreakName(roleMap.get(marry.getOther(roleId)).getName());
        res.setBreakId(roleId);
        res.setBreakType(MarryManager.BREAK_GENERAL);
        res.setMarryState(marry.getState());
        PlayerUtil.send(roleId, res);
        if (online.contains(marry.getOther(roleId))) {
            res.setBreakName(roleMap.get(roleId).getName());
            PlayerUtil.send(marry.getOther(roleId), res);
        }
    }

    /**
     * 处理强制决裂
     *
     * @param roleId
     */
    private void handleForceBreak(long roleId) {
        MarryCache mc = cache.get(roleId);
        Marry marry = mc.getMarry();
        if (null == marry) {
            LogUtil.error("roleId:{} break:{} fail,not return cost", roleId, MarryManager.TOOL_OPERATOR_BREAK_FORCE);
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }
        breakLog(roleId, MarryManager.BREAK_FORCE, marry);
        marry.setBreakState(MarryManager.BREAK_STATE_OVER);
        marry.setBreaker(roleId);
        marry.addBreakCount();
        marry.setLastBreakStamp(DateUtil.getSecondTime());
        dao.update(marry);

        cleanCacheAfterBreak(roleId);

        // 发补偿邮件给对方
        int email = 22004;
        String toolStr = DataManager.getCommConfig("marry_breakship_punishitem");
        //toolStr.trim();
        Map<Integer, Integer> toolMap;
        // 支持不配置附件
        if (null != toolStr && !("".equals(toolStr)) && !("0".equals(toolStr))) {
            toolMap = StringUtil.toMap(toolStr, Integer.class, Integer.class, '+', ',');
        } else {
            toolMap = new HashMap<>();
        }
        MarryRole role = roleMap.get(marry.getBreaker());
        ServiceHelper.emailService().sendToSingle(marry.getOther(roleId), email, 0L, "系统", toolMap, role.getName());

        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.BREAK_MARRY);
        res.setBreakName(roleMap.get(marry.getOther(roleId)).getName());
        res.setBreakId(roleId);
        res.setBreakType(MarryManager.BREAK_FORCE);
        res.setMarryState(marry.getState());
        PlayerUtil.send(roleId, res);
        if (online.contains(marry.getOther(roleId))) {
            PlayerUtil.send(marry.getOther(roleId), res);
        }
    }

    /**
     * 离婚日志
     *
     * @param roleId
     * @param breakType
     * @param marry
     */

    private void breakLog(long roleId, byte breakType, Marry marry) {
        Map<Integer, Integer> toolMap;
        byte tmpType = 0;
        if (breakType == MarryManager.BREAK_GENERAL) {
            String[] costs = DataManager.getCommConfig("marry_breakship_normalcost").split("[|]");
            String toolString = "";
            for (String costStr : costs) {
                String[] cost = costStr.split("[,]");
                toolString = cost[1];
                if (marry.getBreakCount() + 1 > Integer.valueOf(cost[0])) {
                    continue;
                }
                break;
            }
            toolMap = StringUtil.toMap(toolString, Integer.class, Integer.class, '+', ',');
            tmpType = 3;
        } else if (breakType == MarryManager.BREAK_FORCE) {
            String cost = DataManager.getCommConfig("marry_breakship_forcecost");
            toolMap = StringUtil.toMap(cost, Integer.class, Integer.class, '+', ',');
            tmpType = 4;
        } else {
            toolMap = new HashMap<>();
            tmpType = 5;
        }
        StringBuilder logStr = new StringBuilder();
        logStr.append("break_type:")
                .append(tmpType);
        if (null != toolMap && toolMap.size() > 0) {
            logStr.append("#consume@num:");
            String subStr = null;
            for (Map.Entry<Integer, Integer> m : toolMap.entrySet()) {
                if (null == subStr) {
                    subStr = String.format("%d@%d", m.getKey(), m.getValue());
                } else {
                    subStr = subStr + "&" + String.format("%d@%d", m.getKey(), m.getValue());
                }
            }
            if (null != subStr) {
                logStr.append(subStr);
            }
        }
        MarryLogEvent event = new MarryLogEvent("break", logStr.toString(), "" + marry.getOther(roleId));
        ServiceHelper.roleService().notice(roleId, event);
    }

    /**
     * 决裂后的一些处理
     *
     * @param roleId
     */
    private void cleanCacheAfterBreak(long roleId) {
        MarryCache mc = cache.get(roleId);
        Marry marry = mc.getMarry();
        mc.setMarry(null);
        roleMap.get(roleId).setMarryKey("");
        roleMap.get(marry.getOther(roleId)).setMarryKey("");
        dao.update(roleMap.get(roleId));
        dao.update(roleMap.get(marry.getOther(roleId)));
        if (cache.containsKey(marry.getOther(roleId))) {
            cache.get(marry.getOther(roleId)).setMarry(null);
        }
        marryMap.remove(marry.getUniqueKey());

        // 触发结婚事件
        fireMarryEvent(roleId);
        fireMarryEvent(marry.getOther(roleId));
        fireMarryBattleEvent(roleId);
        fireMarryBattleEvent(marry.getOther(roleId));
    }

    /**
     * 结婚的一些信息(表白成功未预约)
     *
     * @param roleId
     */
    @Override
    public void marryInfo(long roleId) {
        MarryCache mc = cache.get(roleId);
        Marry marry = mc.getMarry();
        if (null == marry) return;
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.MARRY_INFO);
        long other = marry.getOther(roleId);
        FriendPo po = ServiceHelper.friendService().getFriendPo(roleId, other);
        res.setFriendShip(po == null ? -1 : po.getIntimacy());
        res.setMarryRole(roleMap.get(marry.getOther(roleId)));
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "获取结婚的一些信息", true);
    }

    /**
     * 创建情谊副本
     *
     * @param creator
     * @param target
     */
    @Override
    public void createTeam(long roleId, BaseTeamMember creator, int target) {
        Marry marry = cache.get(roleId).getMarry();
        if (marry == null ||
                marry.getBreakState() == MarryManager.BREAK_STATE_OVER ||
                marry.getState() != Marry.MARRIED) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.team.inconformity")));
            return;
        }
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team != null) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.team.in")));
            return;
        }
        ServiceHelper.baseTeamService().createTeam(creator, BaseTeamManager.TEAM_TYPE_MARRY, (byte) 1, (byte) 2, target);
    }

    @Override
    public void SyncMarryOther(long roleId) {
        LogUtil.info("结婚组队|给 {} 玩家同步对象数据", roleId);
        Marry marry = cache.get(roleId).getMarry();
        long other = marry.getOther(roleId);
        MarryRole marryRole = roleMap.get(other);
        Summary summary = ServiceHelper.summaryService().getSummary(other);
        FighterEntity fighterEntity = FighterCreator.createBySummary((byte) 1, summary).get(Long.toString(other));
        if (!summary.isOnline()) {
            ClientMarryBattleInfo battleInfo = new ClientMarryBattleInfo();
            battleInfo.setRoleId(other);
            battleInfo.setDeityWeapon(fighterEntity.getCurDeityWeapon());
            battleInfo.setFightScore(fighterEntity.getFightScore());
            battleInfo.setJobId((byte) marryRole.getJobId());
            battleInfo.setLevel((short) fighterEntity.getLevel());
            battleInfo.setName(fighterEntity.getName());
            battleInfo.setType((byte) 0);
            battleInfo.setRoleState(ClientMarryBattleInfo.OFFLINE);
            PlayerUtil.send(roleId, battleInfo);
        } else {
            ServiceHelper.roleService().notice(other, new SyncSelfDataToTeamEvent(roleId, other));
        }
    }

    @Override
    public void addDungeon(List<Long> roleIds) {
        LogUtil.info("addDungeon:{}", roleIds);
        for (long roleId : roleIds) {
            MarryRole marryRole = roleMap.get(roleId);
            marryRole.addDungeon();
            dao.update(marryRole);
            LogUtil.info("{}", marryRole);
            ServiceHelper.roleService().notice(roleId, new DailyFuntionEvent(DailyManager.DAILYID_MARRY_DUNGEON, 1));
            ServiceHelper.roleService().notice(roleId, new DailyAwardCheckEvent());
        }
    }

    @Override
    public void SyncMarryScore(long roleId, int score) {
//        Marry marry = cache.get(roleId).getMarry();
//        long other = marry.getOther(roleId);
//        BaseTeam team = ServiceHelper.baseTeamService().getTeam(other);
//        if (team == null) {
//            LogUtil.info("结婚组队| {} 玩家无需给对象同步自己的积分，因为对象不在队伍中", roleId, other, score);
//            return;
//        }
//        ServiceHelper.roleService().notice(other, new SyncMarryScoreToOtherEvent(roleId, score));
//        LogUtil.info("结婚组队| {} 玩家给对象 {} 同步自己的积分 {}", roleId, other, score);
    }

    /**
     * 获取结婚实体
     *
     * @param roleId
     * @return
     */
    @Override
    public Marry getMarrySync(long roleId) {
        if (null == cache.get(roleId).getMarry())
            return null;
        return cache.get(roleId).getMarry().copy();
    }

    @Override
    public int getRemainTeamDungeon(long roleId) {
        MarryRole role = roleMap.get(roleId);
        return role == null ? 0 : MarryManager.TEAM_DUNGEON_COUNT - role.getDungeon();
    }

    /**
     * 获取豪华婚礼实体
     *
     * @param key
     * @return
     */
    @Override
    public MarryWedding getWeddingSync(String key) {
        if (currentWeddingMap.containsKey(key)) {
            return currentWeddingMap.get(key).copy();
        } else {
            return null;
        }
    }

    public ConcurrentMap<String, MarryWedding> getCurrentWeddingMapSync() {
        ConcurrentMap<String, MarryWedding> map = new ConcurrentHashMap<>();
        for (Map.Entry<String, MarryWedding> entry : currentWeddingMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().copy());
        }
        return map;
    }

    /**
     * 发送邀请列表
     *
     * @param roleId
     */
    @Override
    public void sendCanInviteList(long roleId) {
        ClientBaseTeamInvite res = new ClientBaseTeamInvite(ClientBaseTeamInvite.CAN_INVITE_LIST);
        res.setInviteeType(TeamDungeonManager.INVOTEE_TYPE_MARRY);
        Marry marry = cache.get(roleId).getMarry();
        long otherId = marry.getOther(roleId);
        if (online.contains(otherId) == false) {
            PlayerUtil.send(roleId, new ClientText("对方不在线"));
            return;
        }
        MarryRole role = roleMap.get(marry.getOther(roleId));
        TeamInvitee invitee = new TeamInvitee();
        invitee.setId(role.getRoleId());
        invitee.setName(role.getName());
        invitee.setJob((byte) role.getJobId());
        invitee.setLevel((short) role.getLevel());
        invitee.setFightScore(role.getFight());
        res.addInvitee(invitee);
        PlayerUtil.send(roleId, res);
    }

    /**
     * 豪华婚礼信息
     *
     * @param roleId
     */
    @Override
    public void weddingInfo(long roleId) {
        Marry marry = cache.get(roleId).getMarry();
        if (marry == null) return;
        MarryWedding wedding = weddingQueryMap.get(marry.getUniqueKey());
        if (wedding == null) return;
        int stamp = getBeginStampFromWedding(wedding) - DateUtil.getSecondTime();
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.WEDDING_INFO);
        res.setWeddingName(roleMap.get(marry.getOther(roleId)).getName());
        res.setBeginStamp(stamp);
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "获取豪华婚礼信息", true);
    }

    /**
     * 获取婚礼的开始时间戳
     *
     * @param wedding
     * @return
     */
    private int getBeginStampFromWedding(MarryWedding wedding) {
        int stamp = 0;
        /**
         StringBuilder builder = new StringBuilder();
         builder.append(wedding.getYmd()).append(" ");
         if (wedding.getHms() < 100000) {
         builder.append(0).append(wedding.getHms());
         } else {
         builder.append(wedding.getHms());
         }
         try {
         stamp = (int) (new SimpleDateFormat("yyyyMMdd HHmmss").parse(builder.toString()).getTime() / 1000);
         } catch (ParseException e) {
         LogUtil.error("", e);
         }
         **/
        return stamp;
    }

    /**
     * 进入婚礼场景
     *
     * @param key
     * @param roleId
     */
    @Override
    public void enterWeddingScene(String key, long roleId, boolean login) {
//        if (login == false) {
//            ServiceHelper.roleService().notice(roleId, new EnterWeddingSceneEvent());   // 进入场景
//        }
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null != wedding) {
            wedding.enter(roleId);
        }
    }

    /**
     * 打开喜糖
     *
     * @param position
     * @param candyStamp
     * @param key
     */
    @Override
    public void openCandy(long roleId, String position, int candyStamp, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null == wedding) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        if (wedding.getCandySet().contains(position) && candyStamp == wedding.getLastCandyActivity()) {
            // 获得奖励
            MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_CANDY);
            Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getReward(), Integer.class, Integer.class, '+', ',');
            MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_CANDY_REWARD, MarryToolEvent.ADD, toolMap, null);
            ServiceHelper.roleService().notice(roleId, event);
            wedding.getCandySet().remove(position);

            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.CANDY_ACTIVITY);
            res.setCandyType(MarryManager.CANDY_ACTIVITY_CLICK);
            res.setCandyPos(position);
            res.setCandyTool(toolMap);
            res.setCandyHolder(roleId);
            noticePartyAll(wedding, res);
        } else {
            PlayerUtil.send(roleId, new ClientText("marry_activity_candy_empty"));
        }
        fireSpecialAccountEvent(roleId, roleId, "打开喜糖", true);
    }

    /**
     * 放烟花
     *
     * @param roleId
     * @param key
     */
    @Override
    public void fireworks(long roleId, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (wedding == null) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        if (!wedding.getParty().contains(roleId)) return;
        int now = DateUtil.getSecondTime();
        if (now - wedding.getLastFireworksStamp() < MarryManager.FIREWORKS_DELAY) {
            PlayerUtil.send(roleId, new ClientText("marry_activity_tips_cding"));
            return;
        }
        // 检查放烟花的次数
        int times;
        if (wedding.getFireworksMap().containsKey(roleId)) {
            times = wedding.getFireworksMap().get(roleId);
        } else {
            times = 0;
            wedding.getFireworksMap().put(roleId, 0);
        }
        if (wedding.isMyWedding(roleId)) {
            times -= Integer.valueOf(DataManager.getCommConfig("marry_activity_freetime").split("[+]")[1]);
        }
        MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_FIREWORKS);
        if (times >= vo.getTimes()) {
            PlayerUtil.send(roleId, new ClientText("marry_activity_paper_maxtime"));
            return;
        }
        if (times < 0) {    // 免费
            fireworks0(roleId, key);
        } else {
            Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getCost(), Integer.class, Integer.class, '+', ',');
            MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_FIREWORKS, MarryToolEvent.SUB, toolMap, key);
            ServiceHelper.roleService().notice(roleId, event);
        }
        fireSpecialAccountEvent(roleId, roleId, "放烟花", true);
    }

    /**
     * 放烟花
     *
     * @param roleId
     */
    private void fireworks0(long roleId, String key) {
        int now = DateUtil.getSecondTime();
        MarryCache mc = cache.get(roleId);
        MarryWedding wedding = currentWeddingMap.get(key);

        if (null == wedding) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        wedding.setLastFireworksStamp(now);
        MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_FIREWORKS);
        Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getReward(), Integer.class, Integer.class, '+', ',');
        MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_FIREWORKS_AWARD, MarryToolEvent.ADD, toolMap, null);
        ServiceHelper.roleService().notice(roleId, event);
        {
            int times = wedding.getFireworksMap().get(roleId) + 1;
            wedding.getFireworksMap().put(roleId, times);
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.FIREWORKS_ACTIVITY);
            res.setFireworksCustomer(roleId);
            res.setFireworksTool(toolMap);
            res.setCustonerName(mc.getMarryRole().getName());
            noticePartyAll(wedding, res);
        }
    }

    /**
     * 发红包
     *
     * @param roleId
     * @param key
     */
    @Override
    public void sendRedbag(long roleId, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (wedding == null) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        int now = DateUtil.getSecondTime();
        if (now - wedding.getLastRedbagStamp() < MarryManager.REDBAG_DELAY) {
            PlayerUtil.send(roleId, new ClientText("marry_activity_tips_cding"));
            return;
        }
        Integer times = wedding.getRedbagMap().get(roleId);
        if (times == null) {
            times = 0;
            wedding.getRedbagMap().put(roleId, times);
        }
        if (wedding.isMyWedding(roleId)) {
            times -= Integer.valueOf(DataManager.getCommConfig("marry_activity_freetime").split("[+]")[0]);
        }
        MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_REDBAG);
        if (times >= vo.getTimes()) {
            PlayerUtil.send(roleId, new ClientText("marry_activity_paper_maxtime"));
            return;
        }

        if (times >= 0) {
            Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getCost(), Integer.class, Integer.class, '+', ',');
            MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_REDBAG_SEND, MarryToolEvent.SUB, toolMap, key);
            ServiceHelper.roleService().notice(roleId, event);
        } else {
            // 免费
            sendRedbag0(roleId, key);
        }
        fireSpecialAccountEvent(roleId, roleId, "发红包", true);
    }

    /**
     * 发红包
     *
     * @param roleId
     */
    private void sendRedbag0(long roleId, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null == wedding) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        int now = DateUtil.getSecondTime();
        wedding.setLastRedbagStamp(now);
        //MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_REDBAG);
        Map<Integer, Integer> toolMap = DataManager.getTool("marryactivity_redbag_reward");
        //Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getReward(), Integer.class, Integer.class, '+', ',');
        MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_REDBAG_SENDA_AWARD, MarryToolEvent.ADD, toolMap, null);
        ServiceHelper.roleService().notice(roleId, event);

        int times = wedding.getRedbagMap().get(roleId) + 1;
        wedding.getRedbagMap().put(roleId, times);
        wedding.setRedbagSender(roleId);
        wedding.setRedbagRemain(DataManager.getCommConfig("marryactivity_redbag_num", 10));
        wedding.getRedbagTimesMap().clear();

        MarryRole role = cache.get(roleId).getMarryRole();
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.REDBAG_ACTIVITY);
        res.setRedbagType(MarryManager.REDBAG_ACTIVITY_SEND);
        res.setRedbagSenderName(role.getName());
        res.setRedbagSender(roleId);
        res.setRedbagTool(toolMap);
        noticePartyAll(wedding, res);
    }

    /**
     * 抢红包
     *
     * @param roleId
     * @param senderId
     * @param key
     */
    @Override
    public void getRedbag(long roleId, long senderId, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null == wedding || !wedding.getParty().contains(roleId)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.wedding.inactivity")));
            return;
        }
        if (senderId != wedding.getRedbagSender()) {
            return;
        }
        Long lastStamp = wedding.getRedbagGetMap().get(roleId);
        if (lastStamp == null) {
            lastStamp = 0L;
        }
        int totalRedbagNum = DataManager.getCommConfig("marryactivity_redbag_num", 10);
        int tmpTimes = 0;
        if (wedding.getRedbagTimesMap().containsKey(roleId)) {
            tmpTimes = wedding.getRedbagTimesMap().get(roleId);
        }
        if (tmpTimes >= totalRedbagNum) {
            PlayerUtil.send(roleId, new ClientText("红包已被抢光"));
        } else {
            int randId = DataManager.getCommConfig("marry_redpaper_randomreward", 1);
            Map<Integer, Integer> randToolMap = DropUtil.executeDrop(randId, 1);
            long now = System.currentTimeMillis();
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.REDBAG_ACTIVITY);
            res.setRedbagType(MarryManager.REDBAG_ACTIVITY_GET);
            if (now - lastStamp > DataManager.getCommConfig("marryactivity_redbag_delay", 0)) {
                // 在有效时间内红包不超过总数，可以获得红包奖励，否则就是随机掉落奖励
                if (wedding.getRedbagRemain() > 0) {
                    int remain = wedding.getRedbagRemain() - 1;
                    wedding.setRedbagRemain(remain);
                    MarryActivityVo vo = MarryManager.getMarryActivityVo(MarryManager.ACTIVITY_REDBAG);
                    //Map<Integer, Integer> toolMap = DataManager.getTool("marryactivity_redbag_reward");
                    Map<Integer, Integer> toolMap = StringUtil.toMap(vo.getReward(), Integer.class, Integer.class, '+', ',');
                    MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_REDBAG_GET, MarryToolEvent.ADD, toolMap, null);
                    ServiceHelper.roleService().notice(roleId, event);
                    res.setRedbagTool(toolMap);
                } else {
                    // res.setRedbagTool(new HashMap<Integer, Integer>());
                    MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_REDBAG_GET, MarryToolEvent.ADD, randToolMap, null);
                    ServiceHelper.roleService().notice(roleId, event);
                    res.setRedbagTool(randToolMap);
                }
                PlayerUtil.send(roleId, res);
            } else {
                // PlayerUtil.send(roleId, new ClientText("运气不佳，红包是空的"));
                MarryToolEvent event = new MarryToolEvent(MarryManager.TOOL_OPERATOR_REDBAG_GET, MarryToolEvent.ADD, randToolMap, null);
                ServiceHelper.roleService().notice(roleId, event);
                res.setRedbagTool(randToolMap);
                PlayerUtil.send(roleId, res);
            }
            wedding.getRedbagGetMap().put(roleId, now);
            tmpTimes++;
            wedding.getRedbagTimesMap().put(roleId, tmpTimes);
        }

        fireSpecialAccountEvent(roleId, roleId, "抢" + senderId + "的红包", true);
        fireSpecialAccountEvent(roleId, senderId, "抢" + senderId + "的红包", false);
    }

    /**
     * 是否在婚礼活动中
     *
     * @return
     */
    private boolean inWedding() {
        /**
         if (luxuriousWedding != null && luxuriousWedding.getState() == MarryWedding.RUN) {
         return true;
         }
         **/
        return false;
    }

    /**
     * 情谊信息
     *
     * @param roleId
     */
    @Override
    public void shipInfo(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        if (marry == null) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.info.single")));
            return;
        }
        MarryRole other = roleMap.get(marry.getOther(roleId));

        DeityWeaponSummaryComponent deity = (DeityWeaponSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(other.getRoleId(), MConst.Deity);
        int fashionId = 0;
        MarryWedding wedding = currentWeddingMap.get(marry.getUniqueKey());
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.SHIP_INFO);
        res.setMarry(marry);
        res.setOther(other);
        res.setFashionId(fashionId);
        res.setWeaponId(deity.getCurRoleDeityWeapoonId());
        if (wedding != null) {
            res.setWeddingBeginStamp((int) wedding.getStartStamp());
        }
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "情谊信息", true);
    }

    /**
     * 发送副本数据
     *
     * @param roleId
     */
    @Override
    public void sendShipDungeon(long roleId, int level, Map<Integer, Integer> awardMap) {
        Map<Integer, TeamDungeonVo> map = TeamDungeonManager.getTeamDungeonVoMap();
        Collection<TeamDungeonVo> coll = map.values();
        for (TeamDungeonVo teamDungeon : coll) {
            if (level < teamDungeon.getLevellimit()) {
                continue;
            }
            if (roleMap.containsKey(roleId) && roleMap.get(roleId).getDungeon() < MarryManager.TEAM_DUNGEON_COUNT) {
                ServiceHelper.teamDungeonService().addMemberId(roleId, teamDungeon.getTeamdungeonid());
            }
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.SHOW_MARRY_BATTLE_DETAIL);
        res.setAwardMap(awardMap);
        int roleDungeon = roleMap.containsKey(roleId) ? roleMap.get(roleId).getDungeon() : 0;
        res.setRemainMarryBattleTime(MarryManager.TEAM_DUNGEON_COUNT - roleDungeon);
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "发送副本数据", true);
    }

    /**
     * 进入战斗
     *
     * @param roleId
     */
    @Override
    public void fight(long roleId) {
        BaseTeam team = ServiceHelper.baseTeamService().getTeam(roleId);
        if (team == null) {
            PlayerUtil.send(roleId, new ClientText("没有队伍"));
            return;
        }
        if (team.getCaptainId() != roleId) {
            PlayerUtil.send(roleId, new ClientText("只有队长才可以开始"));
            return;
        }
        for (long id : team.getMembers().keySet()) {
            if (getRemainTeamDungeon(id) <= 0) {
                PlayerUtil.send(roleId, new ClientText("marry_btn_teammate_timenotenough"));
                return;
            }
        }
        ServiceHelper.teamDungeonService().enterMarryFight(roleId);
        fireSpecialAccountEvent(roleId, roleId, "marry进入战斗", true);
    }

    /**
     * 登陆决裂检查
     *
     * @param roleId
     */
    @Override
    public void loginBreakCheck(long roleId) {
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        if (marryCache.getMarry() != null && marryCache.getMarry().getBreakState() == MarryManager.BREAK_STATE_PROPOSE &&
                roleId != marry.getBreaker()) {
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.BREAK_MARRY);
            res.setBreakName(roleMap.get(marry.getOther(roleId)).getName());
            res.setBreakId(marry.getBreaker());
            res.setBreakType(MarryManager.BREAK_GENERAL);
            res.setMarryState(marry.getState());
            PlayerUtil.send(roleId, res);
        }
        fireSpecialAccountEvent(roleId, roleId, "登陆决裂检查", true);
    }

    /**
     * 婚礼活动信息
     *
     * @param roleId
     * @param key
     */
    @Override
    public void weddingActivityInfo(long roleId, String key) {
        MarryWedding wedding = currentWeddingMap.get(key);
        if (null != wedding) {
            ClientMarry res = new ClientMarry();
            res.setResType(ClientMarry.WEDDING_ACTIVITY_INFO);
            res.setRoleId1("" + wedding.getOrder());
            res.setRoleId2("" + wedding.getOther());
            res.setCurrentStamp(DateUtil.getSecondTime());
            res.setLastCandyStamp(wedding.getLastCandyActivity());
            res.setLastRedbagStamp(wedding.getLastRedbagStamp());
            res.setLastFireworksStamp(wedding.getLastFireworksStamp());
            Integer fireworks = wedding.getFireworksMap().get(roleId);
            Integer redbag = wedding.getRedbagMap().get(roleId);
            int remain = DataManager.getCommConfig("marry_party_duration", 600);
            remain -= (DateUtil.getSecondTime() - wedding.getStartStamp());
            res.setFireworksTimes(fireworks == null ? 0 : fireworks);
            res.setRedbagTimes(redbag == null ? 0 : redbag);
            res.setRemainTime(remain);
            PlayerUtil.send(roleId, res);
        }
        fireSpecialAccountEvent(roleId, roleId, "婚礼活动信息", true);
    }

    /**
     * 搜索
     *
     * @param roleId
     * @param searchName
     */
    @Override
    public void search(long roleId, String searchName) {
        String trimName = searchName.trim();
        if ("".equals(trimName)) {
            return;
        }
        int curor = 0;
        boolean bIsOutTime = false;
        int count = 20;  // 最多搜索20条
        List<MarryRole> list = new ArrayList<>();
        for (; curor < claimQueue.size(); curor++) {
            MarryRole claim = claimQueue.get(curor);
            if (claim.getClaimStamp() + MarryManager.MARRY_LOVEINFO_HOLDTIME_PUBLIC < DateUtil.getSecondTime()) {
                bIsOutTime = true;
                break;
            }
            if (claim.getName().contains(trimName) && claim.getRoleId() != roleId) {
                list.add(claim);
                count--;
            }
            if (count <= 0) break;
        }
        if (bIsOutTime) {
            for (; curor < claimQueue.size(); ) {    // 移除过期宣言
                claimQueue.get(curor).setClaim("");
                dao.update(claimQueue.get(curor));
                claimQueue.remove(curor);
            }
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.SEARCH_CLAIM_LIST);
        res.setClaimList(list);
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "marry搜索", true);
    }

    /**
     * 角色日重置
     *
     * @param id
     */
    @Override
    public void onDailyReset(long id) {
        MarryRole role = roleMap.get(id);
        if (role == null) return;
        role.setDungeon((byte) 0);
        dao.update(role);
        LogUtil.info("结婚组队关卡次数，每日重置:{}", role);
    }

    /**
     * 触发表白事件
     *
     * @param roleId
     */
    private void fireProfressEvent(long roleId) {
        if (!online.contains(roleId)) {
            return;
        }
        MarryCache marryCache = cache.get(roleId);
        if (marryCache == null) {
            return;
        }
        Map<Long, MarryProfress> profressedList = marryCache.getProfressed();
        if (profressedList == null) {
            return;
        }
        Set<Long> ids = new HashSet<>();
        for (long id : profressedList.keySet()) {
            ids.add(id);
        }
        MarryProfressEvent event = new MarryProfressEvent(ids);
        ServiceHelper.roleService().notice(roleId, event);
    }

    /**
     * 触发结婚事件
     *
     * @param roleId
     */
    private void fireMarryEvent(long roleId) {
        if (!online.contains(roleId)) {
            return;
        }
        MarryCache marryCache = cache.get(roleId);
        if (marryCache != null) {
            Marry marry = marryCache.getMarry();
            MarryEvent event = new MarryEvent();
            if (marry != null) {
                event.setMarry(marry.copy());
            }
            ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 触发情义副本变化事件
     *
     * @param roleId
     */
    private void fireMarryBattleEvent(long roleId) {
        if (!online.contains(roleId)) {
            return;
        }
        MarryCache marryCache = cache.get(roleId);
        if (marryCache != null) {
            Marry marry = marryCache.getMarry();
            MarryBattleEvent event = new MarryBattleEvent();
            ServiceHelper.roleService().notice(roleId, event);
        }
    }

    /**
     * 获取正在举办的婚宴列表
     *
     * @param roleId
     * @param startIndex
     */

    public void weddingList(long roleId, int startIndex, int endIndex) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            PlayerUtil.send(roleId, new ClientText(I18n.get("marry.notcan.function")));
            return;
        }
        //if (startIndex < 0 || startIndex > endIndex) return;
        boolean bEmpty = false;
        if (currentWeddingList.size() == 0) {
            bEmpty = true;
        }
        /*
        if (!bEmpty && (startIndex + 1) > currentWeddingList.size()) {
            bEmpty = true;
        }
        */
        if (bEmpty) {
            List<WeddingResponse> response1 = new ArrayList<>();
            ClientMarry res1 = new ClientMarry();
            res1.setResType(ClientMarry.WEDDING_LIST);
            res1.setTotal(currentWeddingList.size());
            res1.setWeddingList(response1);
            PlayerUtil.send(roleId, res1);
            return;
        }
        //int start = 0;
        //int end = endIndex;
        //if ((end + 1) > currentWeddingList.size()) end = currentWeddingList.size() - 1;
        int now = DateUtil.getSecondTime();
        int outTime = DataManager.getCommConfig("marry_party_duration", 600);
        List<WeddingResponse> response = new ArrayList<>();
        List<Long> friendList = ServiceHelper.friendService().getFriendList(roleId);
        long familyId = ServiceHelper.familyRoleService().getFamilyId(roleId);
        List<Long> familyList = ServiceHelper.familyMainService().getMemberIdList(familyId, roleId);
        for (int i = 0; i < currentWeddingList.size(); ++i) {
            if (currentWeddingList.get(i).getStartStamp() + outTime < now)
                break;
            long id1 = 0;
            long id2 = 0;
            String[] key = currentWeddingList.get(i).getMarryKey().split("[+]");
            id1 = Long.valueOf(key[0]);
            id2 = Long.valueOf(key[1]);
            WeddingResponse m = new WeddingResponse();
            m.marryKey = currentWeddingList.get(i).getMarryKey();
            m.remainTime = (int) (currentWeddingList.get(i).getStartStamp() + outTime - now);
            m.roleName1 = roleMap.get(id1).getName();
            m.jobId1 = roleMap.get(id1).getJobId();
            m.level1 = roleMap.get(id1).getLevel();
            m.roleName2 = roleMap.get(id2).getName();
            m.jobId2 = roleMap.get(id2).getJobId();
            m.level2 = roleMap.get(id2).getLevel();
            m.icon = 0;
            if (friendList.contains(id1) || friendList.contains(id2)) {
                m.icon = 1;
            } else {
                if (familyList.contains(id1) || familyList.contains(id2)) {
                    m.icon = 2;
                }
            }
            response.add(m);
        }
        ClientMarry res = new ClientMarry();
        res.setResType(ClientMarry.WEDDING_LIST);
        res.setTotal(response.size());
        res.setWeddingList(response);
        PlayerUtil.send(roleId, res);
    }

    /**
     * 预约检查
     *
     * @param roleId
     */
    public void appointmentCheck(long roleId) {
        ClientMarry res = new ClientMarry();
        MarryCache marryCache = cache.get(roleId);
        Marry marry = marryCache.getMarry();
        if (null == marry) return;
        long other = marry.getOther(roleId);
        res.setResType(ClientMarry.APPOINT_CHECK);
        res.setCheckResult((byte) (online.contains(other) ? 0 : 1));
        PlayerUtil.send(roleId, res);
    }

    @Override
    public void updateRoleName(Long id, String newName) {
        MarryRole marryRole = roleMap.get(id);
        marryRole.setName(newName);
        dao.update(marryRole);
    }
}
