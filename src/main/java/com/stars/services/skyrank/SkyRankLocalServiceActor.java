package com.stars.services.skyrank;

import com.stars.bootstrap.BootstrapConfig;
import com.stars.bootstrap.GameBootstrap;
import com.stars.bootstrap.ServerManager;
import com.stars.core.SystemRecordMap;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerSystem;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.drop.DropUtil;
import com.stars.modules.skyrank.SkyRankManager;
import com.stars.modules.skyrank.SkyRankScoreHandle;
import com.stars.modules.skyrank.event.SkyRankDailyAwardEvent;
import com.stars.modules.skyrank.event.SkyRankLogEvent;
import com.stars.modules.skyrank.event.SkyRankScoreHandleEvent;
import com.stars.modules.skyrank.packet.ClientSkyRankMyData;
import com.stars.modules.skyrank.packet.ClientSkyRankRankData;
import com.stars.modules.skyrank.prodata.*;
import com.stars.modules.skyrank.userdata.SkyRankDataPo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * * 天梯排行榜（本服）
 *
 * @author xieyuejun
 */
public class SkyRankLocalServiceActor extends ServiceActor implements SkyRankLocalService {

    // 本地玩家开始的缓存数量
    public static int MAX_CACHE = 10000;

    public static long SAVE_INTERVAL = 1 * 60 * 1000;

    public Map<Long, SkyRankShowData> skyRankDataMap = new HashMap<>();
    public List<SkyRankShowData> skyRankList = new ArrayList<>();

    //跨服的排名数据，用于获取本服玩家的排名
    private Map<Long, Integer> skyRankKfFrankMap = new HashMap<Long, Integer>();

    // 本地玩家的信息 FIXME 太多数量，内存溢出，要清理过期数据？
    public Map<Long, SkyRankRoleOp> skyRankLocalMap = new HashMap<>();

    private DbRowDao dao = new DbRowDao("roleskyrank", DBUtil.DB_USER);


    public static int runInterval = 1;// 检测间隔

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.SkyRankLocalService, this);

        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.SkyRank, new SchedulerTask(), runInterval, runInterval,
                TimeUnit.SECONDS);

        loadRankData();
    }

    class SchedulerTask implements Runnable {
        @Override
        public void run() {
            if (!GameBootstrap.getServerType().equals(BootstrapConfig.MAIN)) {
                return;
            }
            ServiceHelper.skyRankLocalService().runUpdate();
        }
    }

    public void runUpdate() {
        saveUserData();
        update();
        checkSeasonTimeEvent();
        checkSeasonChange();
        checkScoreLock();
    }

    long lastSaveTime = 0;
//	public static long SAVE_INTERVAL = 1 * 60 * 1000;

    /**
     * 保存用户数据
     */
    public void saveUserData() {
        try {
            long now = System.currentTimeMillis();
            if (now - lastSaveTime < SAVE_INTERVAL) {
                return;
            }
            lastSaveTime = now;
            dao.flush();
            //保存后再清除
            clearOverTimeData();
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * rpc处理积分变化
     */
    public void handleScoreEvent(int serverId, long roleId, short fightEvent, byte isWin) {
        ServiceHelper.roleService().notice(roleId, new SkyRankScoreHandleEvent(fightEvent, isWin));
    }

    /**
     * 获取天梯段位积分
     *
     * @param roleId
     * @param name       角色名
     * @param fightScore 角色战力
     * @return
     */
    public int getSkyScore(long roleId, String name, int fightScore) {
        SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, name, fightScore);
        return skyRankRoleOp.getScore();
    }

    public SkyRankRoleOp getSkyRankData(long roleId) {
        SkyRankRoleOp rp = skyRankLocalMap.get(roleId);
        if (rp != null) {
            rp.active();
        }
        return rp;
    }

    /**
     * 初始化数据
     *
     * @param roleId
     * @param name
     * @param fightScore
     * @return
     */
    public SkyRankRoleOp getAndInitSkyRankData(long roleId, String name, int fightScore) {
        SkyRankRoleOp rp = skyRankLocalMap.get(roleId);
        if (rp != null) {
            rp.active();
            return rp;
        }
        SkyRankDataPo srd = null;
        String sql = "select * from `roleskyrank` where `roleid`=" + roleId;
        try {
            srd = DBUtil.queryBean(DBUtil.DB_USER, SkyRankDataPo.class, sql);
            if (srd != null) {
                srd.saveStatus();
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
        if (srd == null) {
            srd = new SkyRankDataPo();
            srd.setRoleId(roleId);
            srd.setName(name);
            srd.setFightScore(fightScore);
            srd.setInsertStatus();
            dao.insert(srd);
        }
        rp = new SkyRankRoleOp(srd);
        rp.active();
        skyRankLocalMap.put(roleId, rp);
        return rp;
    }

    /**
     * gm控制积分
     */
    public void gmHandle(long roleId, String[] args) {
        int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
        SkyRankRoleOp rp = skyRankLocalMap.get(roleId);
        int oldScore = rp.getScore();
        switch (args[0]) {
            case "haddscore":
                rp.addScore(Integer.parseInt(args[1]));
                break;
            case "hsubscore":
                rp.reduceScore(Integer.parseInt(args[1]));
                break;
            case "hsetbuf":
                rp.getSkyRankData().setHideScore(Integer.parseInt(args[1]));
                break;
            case "hlock":
                lastLockedSeasonId = nowSeasonId;
                lockAllScore();
                break;
            case "haward":
                lastAwardedSeasonId = nowSeasonId;
                seasonAward();
                break;
            case "hreset":
                lastResetSeasonId = nowSeasonId;
                seasonReset();
                break;
        }
        rp.checkGradDown(oldScore, rp.getScore());
        rp.checkGradUp(oldScore, rp.getScore());
        dao.update(rp.getSkyRankData());
        updateLocalSkyRankData(rp.getSkyRankData().getNewShowData());
    }


    /**
     * 积分有变化时处理
     */
    public void handleScoreChange(SkyRankScoreHandle scoreHandle) {
        try {
            SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(scoreHandle.getRoleId(), scoreHandle.getName(), scoreHandle.getFightScore());
            //战力更新 FIXME 战力也需要实时更新？监听战力变化事件？
            skyRankRoleOp.getSkyRankData().setFightScore(scoreHandle.getFightScore());
            //名字更新 FIXME 名字也需要实时更新？yes，改名卡
            skyRankRoleOp.getSkyRankData().setName(scoreHandle.getName());
            int oldScore = skyRankRoleOp.getScore();
            boolean isChange = scoreHandle.handleScore(skyRankRoleOp);
            int newScore = skyRankRoleOp.getScore();
            if (isChange) {
                skyRankRoleOp.getSkyRankData().setUpdateStatus();
                dao.update(skyRankRoleOp.getSkyRankData());
                updateLocalSkyRankData(skyRankRoleOp.getSkyRankData().getNewShowData());
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public int getMyKFRank(long roleId) {
        if (skyRankKfFrankMap == null) return 0;
        Integer mRank = skyRankKfFrankMap.get(roleId);
        if (mRank == null) return 0;
        return mRank;
    }

    @Override
    public void updateRoleName(long roleId, String newName, int fightScore) {
        SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, newName, fightScore);
        skyRankRoleOp.getSkyRankData().setName(newName);
        dao.update(skyRankRoleOp.getSkyRankData());
        updateLocalSkyRankData(skyRankRoleOp.getSkyRankData().getNewShowData());
    }
    
    /**
     * 重置每日奖励
     */
    @Override
    public void RoledailyReset(long roleId, String name, int fightScore){
    	SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, name, fightScore);
    	if(skyRankRoleOp!=null){
    		SkyRankDataPo skyRankData = skyRankRoleOp.getSkyRankData();
    		SkyRankGradVo skyRankGradVo = SkyRankManager.getManager().getSkyRankGradVoByScore(skyRankData.getScore());
    		int skyRankGradId = skyRankGradVo.getSkyRankGradId();
    		skyRankData.setAwardGrad(skyRankGradId);
    		//根据段位设置奖励
    		SkyRankDailyAwardVo awardVo = SkyRankManager.getManager().getRankDailyAwardMap().get(skyRankGradId);
    		if(awardVo!=null){
    			skyRankData.setDailyAward(awardVo.getDropId());
    		}else{    			
    			skyRankData.setDailyAward(0);
    		}
    		skyRankData.setDailyAwardState((byte)0);
    		dao.update(skyRankData);
    		ServiceHelper.roleService().notice(roleId, new SkyRankDailyAwardEvent());
    	}
    }
    
    /**
     * 领取每日奖励id
     */
    @Override
    public int getDailyAward(long roleId, String name, int fightScore){
    	SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, name, fightScore);
    	if(skyRankRoleOp!=null){
    		SkyRankDataPo skyRankData = skyRankRoleOp.getSkyRankData();
    		if(skyRankData.getDailyAwardState()==0){
    			skyRankData.setDailyAwardState((byte)1);
    			int dailyAward = skyRankData.getDailyAward();
    			dao.update(skyRankData);
    			return dailyAward;
    		}
    	}
    	return 0;
    }
    
    /**
     * 获取每日奖励信息
     */
    public Object[] getDailyAwardState(long roleId, String name, int fightScore){
    	Object[] info = new Object[]{0,0};
    	SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, name, fightScore);
    	if(skyRankRoleOp!=null){
    		SkyRankDataPo skyRankData = skyRankRoleOp.getSkyRankData();
    		SkyRankDailyAwardVo awardVo = SkyRankManager.getManager().getRankDailyAwardMap().get(skyRankData.getAwardGrad());
    		int dailyAwardUid = 0;
    		if(awardVo!=null){    			
    			dailyAwardUid = awardVo.getUid();
    		}
    		byte dailyAwardState = skyRankData.getDailyAwardState();
    		info[0] = dailyAwardUid;
    		info[1] = dailyAwardState;
    	}
    	return info;
    }

    /**
     * 请求排行榜信息
     */
    public void reqRankMsg(long roleId, SkyRankShowData myDefalutRank) {
        ClientSkyRankRankData clientSkyRankRankData = new ClientSkyRankRankData();
        clientSkyRankRankData.setSkyRankList(skyRankList);
        SkyRankShowData myRank = skyRankDataMap.get(roleId);
        if (myRank == null) {
            myRank = myDefalutRank;
            SkyRankRoleOp ro = getAndInitSkyRankData(roleId, myDefalutRank.getName(), myDefalutRank.getFightscore());
            if (ro != null) {
                myRank.setScore(ro.getScore());
            }
            if (myRank != null) {
                myRank.setRank(getMyKFRank(roleId));
            }
        }
        clientSkyRankRankData.setMyRank(myRank);
        PacketManager.send(roleId, clientSkyRankRankData);

//		int skyRankServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().get("skyrank")
//				.getProperty("serverId"));
//		MainRpcHelper.skyRankKFService().getMyRankData(skyRankServerId, roleId, myDefalutRank);

    }

    /**
     * 请求角色自身天梯相关信息
     */
    public void reqRoleScoreMsg(long roleId, SkyRankShowData myDefalutRank) {
        ClientSkyRankMyData smd = new ClientSkyRankMyData();
        SkyRankRoleOp roleMsg = getAndInitSkyRankData(roleId, myDefalutRank.getName(), myDefalutRank.getFightscore());
        smd.setRank(getMyKFRank(roleId));
        if (roleMsg != null) {
            smd.setMyScore(roleMsg.getScore());
        }
        PacketManager.send(roleId, smd);
    }

    int lastLockedSeasonId;

    int lastAwardedSeasonId;

    int lastResetSeasonId;

    public void loadRankData() {
        if (!GameBootstrap.getServerType().equals(BootstrapConfig.MAIN)) {
            return;
        }

        lastLockedSeasonId = SystemRecordMap.lastLockedSkyRankSeasonId;

        lastAwardedSeasonId = SystemRecordMap.lastAwardedSkyRankSeasonId;

        lastResetSeasonId = SystemRecordMap.lastResetSkyRankSeasonId;

        // CREATE TABLE `roleskyrank` (
        // `roleid` bigint(3) NOT NULL,
        // `name` varchar(30) DEFAULT NULL,
        // `fightscore` int(3) DEFAULT NULL COMMENT '战力',
        // `score` int(3) DEFAULT NULL COMMENT '积分',
        // `hidescore` int(3) DEFAULT NULL COMMENT '隐藏积分',
        // `upawardrecord` text COMMENT ' 已领取的段位升级奖励',
        // `scorerecordmap` text COMMENT '积分获取信息记录',
        // `maxscore` int(3) DEFAULT NULL COMMENT '历史最高积分',
        // PRIMARY KEY (`roleid`)
        // ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

        // DBUtil.querySingleMap(alias, sql)

        // String sql = "select * from `roleskyrank` where `roleid`=" + id();

        String sql = "select * from roleskyrank" + " order by score desc limit " + MAX_CACHE;
        try {
            Map<Long, SkyRankDataPo> tmpSkyRankLocalMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", SkyRankDataPo.class, sql);
            Map<Long, SkyRankRoleOp> skyRankLocalMap = new HashMap<>();
            if (tmpSkyRankLocalMap != null) {
                for (SkyRankDataPo rdp : tmpSkyRankLocalMap.values()) {
                    rdp.setSaveStatus();
                    skyRankLocalMap.put(rdp.getRoleId(), new SkyRankRoleOp(rdp));
                }
            }
            this.skyRankLocalMap = skyRankLocalMap;
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public void updateLocalSkyRankData(SkyRankShowData srd, int skyRankServerId) {
        // 如果满足王者分数，或者在跨服排行榜上
        if (srd.getScore() >= SkyRankConfig.config.KING_REQ_SCORE || (getMyKFRank(srd.getRoleId()) > 0)) {
            MainRpcHelper.skyRankKFService().updateSkyRankData(skyRankServerId, srd);
        }
    }

    /**
     * 更新到跨服排行榜
     */
    public void updateLocalSkyRankData(SkyRankShowData srd) {
        int skyRankServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().get("skyrank")
                .getProperty("serverId"));
        updateLocalSkyRankData(srd, skyRankServerId);
    }

    /**
     * 定时请求跨服排行榜信息
     */
    long lastReqTime = 0;
    public static long REQ_INTERVAL = 5 * 1000;

    public void update() {
        try {
            long now = System.currentTimeMillis();
            if (now - lastReqTime < REQ_INTERVAL) {
                return;
            }
            lastReqTime = now;
            sendLocalRankDataToKF();
            // 请求跨服排行榜信息
            int skyRankServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().get("skyrank")
                    .getProperty("serverId"));
            MainRpcHelper.skyRankKFService().reqSkyRankData(skyRankServerId,
                    MultiServerHelper.getServerId());
        } catch (Throwable e) {
//			LogUtil.error(e.getMessage(), e);
        }
    }

    public void sendLocalRankDataToKF() {
        try {
            if (skyRankLocalMap == null)
                return;
            int skyRankServerId = Integer.parseInt(ServerManager.getServer().getConfig().getProps().get("skyrank")
                    .getProperty("serverId"));
            for (SkyRankRoleOp rop : skyRankLocalMap.values()) {
                updateLocalSkyRankData(rop.getSkyRankData().getNewShowData(), skyRankServerId);
            }
        } catch (Throwable e) {
//			LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 接受跨服排行榜的发奖信息
     */
    public void receiveRankAward(int serverId, List<SkyRankShowData> rankAwardList) {
        if (rankAwardList == null)
            return;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        int month = cal.get(Calendar.MONTH) + 1;
        for (SkyRankShowData skyRd : rankAwardList) {
            LogUtil.info("receiveSkyRankAward " + skyRd.toString());
            SkyRankSeasonRankAwardVo rav = SkyRankManager.getManager().getSkyRankSeasonRankAwardVo(skyRd.getRank());
            if (rav == null)
                continue;
            Map<Integer, Integer> dropMap = DropUtil.executeDrop(rav.getDropId(), 1);
            ServiceHelper.emailService().sendToSingle(skyRd.getRoleId(), SkyRankConfig.config.RANK_AWARD_MAIL, 0L, "系统", dropMap,
                    month + "", skyRd.getRank() + "");
        }
    }

    /**
     * 新的赛季生成
     */
    public void checkNewSeason() {
        long now = System.currentTimeMillis();
        for (SkyRankSeasonVo seasonVo : SkyRankManager.getManager().getSkyRankSeasonList()) {
            if (now < seasonVo.getFinishedTime()) {
                SkyRankManager.getManager().setNowSeasonId(seasonVo.getSkyRankTimeid());
                break;
            }
        }
    }

    /**
     * 检查积分锁开启
     */
    public void checkScoreLock() {
        if (lastLockedSeasonId != SkyRankManager.getManager().getNowSeasonId()) {
            SkyRankScoreVo.SCORE_SWITCH_ALL = true;
            SkyRankScoreVo.SCORE_SWITCH_OFFLINEPVP = true;
            SkyRankScoreVo.SCORE_SWITCH_KFPVP = true;
            SkyRankScoreVo.SCORE_SWITCH_5V5PVP = true;
        } else {
            SkyRankScoreVo.SCORE_SWITCH_ALL = false;
        }
    }

    /**
     * 检查赛季转换
     */
    public void checkSeasonChange() {
        try {
            int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
            SkyRankSeasonVo nowSeason = SkyRankManager.getManager().getSkyRankSeasonVo(nowSeasonId);
            if (nowSeason == null) {
                checkNewSeason();
            } else {
                long now = System.currentTimeMillis();
                // 如果已经过了结束时间
                if (now > nowSeason.getFinishedTime()) {
                    checkNewSeason();
                }
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    //总积分锁开启
    public void lockAllScore() {
        LogUtil.info("skyrank lockAllScore " + SkyRankManager.getManager().getNowSeasonId());
        SkyRankScoreVo.SCORE_SWITCH_ALL = false;
    }

    long lastNoticeTime = 0;

    /**
     * 赛季发奖通知
     *
     * @param nowSeason
     */
    public void checkNoticeTime(SkyRankSeasonVo nowSeason) {
        try {
            long now = System.currentTimeMillis();
            long nowBeginNoticeTime = nowSeason.getLockedTime() - SkyRankConfig.config.rankreward_screennotice_begin * 1000;
            long nowEndNoticeTime = nowSeason.getLockedTime() - SkyRankConfig.config.rankreward_screennotice_end * 1000;
            if (now >= nowBeginNoticeTime && now <= nowEndNoticeTime) {
                if (now - lastNoticeTime < SkyRankConfig.config.rankreward_screennotice_interval * 1000) {
                    return;
                }
                lastNoticeTime = now;
                String content = SkyRankConfig.config.skyrank_screennotice_rewardsendtime;
                int minute = (int) ((nowSeason.getLockedTime() - now) / 1000 / 60);
                int secend = (int) ((nowSeason.getLockedTime() - now) / 1000 % 60);
                ServiceHelper.chatService().announce(content, minute + "", secend + "");
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 赛季时间事件触发
     */
    public void checkSeasonTimeEvent() {
        try {
            int nowSeasonId = SkyRankManager.getManager().getNowSeasonId();
            SkyRankSeasonVo nowSeason = SkyRankManager.getManager().getSkyRankSeasonVo(nowSeasonId);
            if (nowSeason == null)
                return;
            // 积分锁定
            long now = System.currentTimeMillis();
            // System.err.println(now+"|"+nowSeason.getLockedTime()+"|"+nowSeason.getSendAwardTime()+"|"+nowSeason.getFinishedTime());
            // System.err.println("===================="+nowSeasonId);
            // System.err.println(now);
            // System.err.println(nowSeason.getLockedTime());
            // System.err.println(nowSeason.getSendAwardTime());
            // System.err.println(nowSeason.getFinishedTime());
            // System.err.println("===================="+nowSeasonId);

            //发奖提前通知
            checkNoticeTime(nowSeason);
            if (nowSeasonId != lastLockedSeasonId && now >= nowSeason.getLockedTime()) {
                lastLockedSeasonId = nowSeasonId;
                SystemRecordMap.update("lastLockedSkyRankSeasonId", nowSeasonId);
                checkScoreLock();
            }
            // 赛季段位发奖
            if (nowSeasonId != lastAwardedSeasonId && now >= nowSeason.getSendAwardTime()) {
                lastAwardedSeasonId = nowSeasonId;
                SystemRecordMap.update("lastAwardedSkyRankSeasonId", nowSeasonId);
                seasonAward();
            }
            // 赛季重置
            if (nowSeasonId != lastResetSeasonId && now >= nowSeason.getFinishedTime()) {
                lastResetSeasonId = nowSeasonId;
                SystemRecordMap.update("lastResetSkyRankSeasonId", nowSeasonId);
                seasonReset();
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static final String RECORD_KEY_LASTLOCKEDSEASIONID = "lastLockedSkyRankSeasonId";
    public static final String RECORD_KEY_LASTAWARDEDSEASIONID = "lastAwardedSkyRankSeasonId";
    public static final String RECORD_KEY_LASTRESETSEASIONID = "lastResetSkyRankSeasonId";


    /**
     * 赛季重置
     */
    public void seasonReset() {
        //有个地方记录着目前的赛季id，也记录着已重置的赛季id，如果赛季id和重置的不一致时，则
        LogUtil.info("skyrank seasonReset " + SkyRankManager.getManager().getNowSeasonId());
        //当前应该是赛季id
        //当前正在进行的赛季id
        //上次已重置的赛季id

        dao.flush();
        skyRankLocalMap = new HashMap<>();
        try {
            DBUtil.execSql(DBUtil.DB_USER, "delete from roleskyrank");
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
        skyRankDataMap = new HashMap<>();
        skyRankList = new ArrayList<>();
        skyRankKfFrankMap = new HashMap<Long, Integer>();
    }

    /**
     * 个人赛事段位奖励
     *
     * @param rdp
     * @param month
     */
    public void sendOneSeasonAward(SkyRankShowData rdp, int month) {
        try {
            LogUtil.info("sendOneSeasonAward " + rdp.toString());
            SkyRankGradVo rgv = SkyRankManager.getManager().getSkyRankGradVoByScore(rdp.getScore());
            if (rgv == null) {
                LogUtil.info("seasonAward SkyRankGradVo== null" + rdp.getRoleId() + "|" + rdp.getScore());
                return;
            }
            SkyRankSeasonGradAwardVo seasonGradAward = SkyRankManager.getManager().getSkyRankSeasonGradAwardVo(
                    rgv.getSkyRankGradId());
            if (seasonGradAward == null) {
                LogUtil.info("seasonAward seasonGradAward== null" + rdp.getRoleId() + "|" + rdp.getScore() + "|"
                        + rgv.getSkyRankGradId());
                return;
            }
            Map<Integer, Integer> dropMap = DropUtil.executeDrop(seasonGradAward.getDropId(), 1);
            ServiceHelper.emailService().sendToSingle(rdp.getRoleId(), SkyRankConfig.config.GRAD_AWARD_MAIL, 0L,
                    "系统", dropMap, month + "", rgv.getName() + "");
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }


    /**
     * 赛季段位奖励
     */
    public void seasonAward() {
        // 先保存
        try {
            LogUtil.info("skyrank seasonAward " + SkyRankManager.getManager().getNowSeasonId());
            dao.flush();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            int month = cal.get(Calendar.MONTH) + 1;
            int minScore = SkyRankManager.getManager().getMinGradAwardScore();
            String sql = "select roleid,name,fightscore,score from roleskyrank where score >= " + minScore;
            try {
                //防止数据量过大，用轻量级的对象缓存
                Map<Long, SkyRankShowData> tmpSkyRankLocalMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", SkyRankShowData.class, sql);
                if (tmpSkyRankLocalMap != null) {
                    for (SkyRankShowData rdp : tmpSkyRankLocalMap.values()) {
                        sendOneSeasonAward(rdp, month);
                    }
                }
            } catch (Throwable e) {
                LogUtil.error(e.getMessage(), e);
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 接受跨服排行榜到本服的同步信息
     */
    public void receiveRankData(int serverId, List<SkyRankShowData> skyRankList, Map<Long, Integer> skyRankKfFrankMap) {
        Map<Long, SkyRankShowData> skyRankDataMap = new HashMap<>();
        if (skyRankList != null && skyRankList.size() > 0) {
            for (SkyRankShowData srd : skyRankList) {
                skyRankDataMap.put(srd.getRoleId(), srd);
            }
        }
        this.skyRankList = skyRankList;
        this.skyRankDataMap = skyRankDataMap;
        this.skyRankKfFrankMap = skyRankKfFrankMap;
        if (this.skyRankKfFrankMap != null) {
            for (Entry<Long, Integer> entry : this.skyRankKfFrankMap.entrySet()) {
                updateRoleMaxRank(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 从天梯服获取玩家名次
     */
    public void receiveRankData(int serverId, long roleId, SkyRankShowData myRank, SkyRankShowData myDefalutRank) {
        ClientSkyRankRankData clientSkyRankRankData = new ClientSkyRankRankData();
        clientSkyRankRankData.setSkyRankList(skyRankList);
        if (myRank == null) {
            myRank = myDefalutRank;
            SkyRankRoleOp ro = getSkyRankData(roleId);
            if (ro != null) {
                myRank.setScore(ro.getScore());
            }
        }
        clientSkyRankRankData.setMyRank(myRank);
        PacketManager.send(roleId, clientSkyRankRankData);
    }

    /**
     * 玩家历史最高排名更新
     *
     * @param
     */
    public void updateRoleMaxRank(long roleId, int rank) {
        try {
            SkyRankRoleOp rop = getSkyRankData(roleId);
            if (rop == null) {
                return;
            }
            int nowMaxRank = rop.getSkyRankData().getMaxRank();
            int newRank = rank;
            if (nowMaxRank == 0 || nowMaxRank > newRank) {
                rop.getSkyRankData().setMaxRank(newRank);
                if (newRank <= SkyRankConfig.config.screennotice_lower && newRank >= SkyRankConfig.config.screennotice_upper) {
                    String content = SkyRankConfig.config.skyrank_screennotice_ranknotice;
                    ServiceHelper.chatService().announce(content, rop.getSkyRankData().getName() + "", newRank + "");
                }
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    public static int CLEAR_TIME = 3 * 60 * 60 * 1000;

//	public static int CLEAR_TIME = 1*60*1000;

//	public static int CLEAR_TIME = 1*1000;

    /**
     * 清除过期数据，避免冗余
     */
    public void clearOverTimeData() {
        if (skyRankLocalMap == null) return;
        if (skyRankLocalMap.size() <= MAX_CACHE) return;
        long now = System.currentTimeMillis();
        List<Long> removeList = new ArrayList<Long>();
        for (SkyRankRoleOp rp : skyRankLocalMap.values()) {
            if (rp.getScore() >= SkyRankConfig.config.KING_REQ_SCORE) {
                continue;
            }
            if (now - rp.getLastActivtyTime() > CLEAR_TIME) {
                removeList.add(rp.getSkyRankData().getRoleId());
            }
        }
        for (Long roleid : removeList) {
            skyRankLocalMap.remove(roleid);
        }
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出：{},skyRankLocalMap：{}",
                this.getClass().getSimpleName(), skyRankLocalMap.size());
    }

    /**
     * 每天重置
     */
    public void dailyReset() {
        try {
            int size = skyRankLocalMap.size();
            LogUtil.info("roleskyrank dailyReset begin size=" + size);
            dao.flush();
            // 重置每天现在次数
            DBUtil.execSql(DBUtil.DB_USER, "update roleskyrank set scorerecordmap=''");
            for (SkyRankRoleOp rp : skyRankLocalMap.values()) {
                try {
                    rp.getSkyRankData().setScoreRecordMap("");
                    rp.getSkyRankData().setUpdateStatus();
                    dao.update(rp.getSkyRankData());
                } catch (Throwable e) {
                    LogUtil.error(e.getMessage(), e);
                }
            }
            LogUtil.info("roleskyrank dailyReset end size=" + size);
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    /**
     * 统计天梯段位日志
     */
    public void static_log() {
        try {
            String log_id = "grading@lv:";
            String sql = "select * from roleskyrank order by score desc";
            List<SkyRankDataPo> list = DBUtil.queryList(DBUtil.DB_USER, SkyRankDataPo.class, sql);
            if (StringUtil.isEmpty(list)) return;
//			String accountRoleSql = "select * from accountrole where roleid in (select roleid from roleskyrank)";
//			Map<String, AccountRole> accountRoleMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", AccountRole.class, accountRoleSql);
//			String accountSql = "select * from account WHERE `name` in (select account from accountrole where roleid in (select roleid from roleskyrank))";
//			Map<String, AccountRow> accountMap = DBUtil.queryMap(DBUtil.DB_USER, "name", AccountRow.class, accountSql);
//			String simpleRoleSql = "select roleid,level,jobid,createtime from role where roleid in (select roleid from roleskyrank)";
//			Map<Long, SimpleRolePo> simpleRoleMap = DBUtil.queryMap(DBUtil.DB_USER, "roleid", SimpleRolePo.class, simpleRoleSql);
            SkyRankDataPo po = null;
            SkyRankGradVo skyRankGradVo = null;
            SkyRankGradVo lowSkyRankGradVo = null;
//			AccountRole accountRole = null;
//			AccountRow accountRow = null;
//			SimpleRolePo simpleRolePo = null;
            int[] info = null;
            int size = list.size();
            Map<Integer, int[]> skyRankGradStageMap = SkyRankManager.getManager().getSkyRankGradStageMap();
            String logInfo = "";
            long roleId = 0;
            for (int i = 0; i < size; i++) {
                po = list.get(i);
                roleId = po.getRoleId();
                skyRankGradVo = SkyRankManager.getManager().getSkyRankGradVoByScore(po.getScore());
                info = skyRankGradStageMap.get(skyRankGradVo.getSkyRankGradId());
                if (info[1] == -1) {
                    lowSkyRankGradVo = SkyRankManager.getManager().getSkyRankGradVoByScore(skyRankGradVo.getReqscore() - 1);
                    logInfo = log_id + info[0] + "@" + (po.getScore() - lowSkyRankGradVo.getReqscore());
                } else {
                    logInfo = log_id + info[0] + "@" + info[1];
                }
                if (PlayerSystem.get(roleId) != null) {//在线玩家处理
                    SkyRankLogEvent event = new SkyRankLogEvent();
                    event.setInfo(logInfo);
                    ServiceHelper.roleService().notice(roleId, event);
                }
//				else{
//					accountRole = accountRoleMap.get(String.valueOf(roleId));
//					if(accountRole==null) continue;
//					accountRow = accountMap.get(accountRole.getAccount());
//					if(accountRow==null) continue;
//					simpleRolePo = simpleRoleMap.get(roleId);
//					if(simpleRolePo==null) continue;
//					ServerLogModule.log_skyRank_offline(accountRow, simpleRolePo, logInfo);
//				}
            }
        } catch (Exception e) {
            LogUtil.error("skyRank, static_log", e);
        }
    }

    public Map<Long, Integer> getSkyRankKfFrankMap() {
        return skyRankKfFrankMap;
    }

    public void setSkyRankKfFrankMap(Map<Long, Integer> skyRankKfFrankMap) {
        this.skyRankKfFrankMap = skyRankKfFrankMap;
    }

    @Override
    public void checkRankGradeWhileLogin(long roleId, String roleName, int fightScore) {
        SkyRankRoleOp skyRankRoleOp = getAndInitSkyRankData(roleId, roleName, fightScore);
        skyRankRoleOp.checkAwardWhileLogin(skyRankRoleOp.getScore()); // 发奖相关
        updateLocalSkyRankData(skyRankRoleOp.getSkyRankData().getNewShowData()); // 更新天梯服
        dao.update(skyRankRoleOp.getSkyRankData());
    }

    //FIXME 赛季重置

    // 主动请求到跨服拿排行榜数据，接收跨服同步过来的排行榜数据

    // 接收跨服发奖，直接到角色身上

    // 获取玩家排行榜排名

}
