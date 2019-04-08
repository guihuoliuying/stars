package com.stars;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.demologin.userdata.LoginInfo;
import com.stars.modules.demologin.userdata.LoginRow;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.util.DateUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 使用时要小心!
 * Created by zhaowenshuo on 2016/7/25.
 */
public class AccountRow extends DbRow {

    // 不变量(入库)
    private String name; // 账号名
    private String password; // 用户登录密码，md5加密
    private long firstLoginTimestamp; // 第一次登陆时间戳
    private String palform = "4";//平台4=安卓/5=ios/7=wm

    // 变量(入库)
    private AtomicInteger createdRoleCount; // 该值只会增加,标识创建过的角色的个数,但是这里的值不一定和AccuntRole里的个数一样，因为存在删除角色功能;
    private volatile String channel; // 渠道
    private volatile long lastCreateRoleTimestamp; // 上一次创建角色的时间;

    //变量（VIP相关数据），入库
    private int vipLevel;
    private int vipExp;//vip经验
    /**
     * 充值总额
     */
    private int chargeSum;
    private byte identification;//实名验证  2：已经验证，1：已检测未验证，0：未检测 
    private byte identAward;//领奖励状态    1 已领  ；   0 未领取 
    // 变量(不入库)
    private volatile long lastLoginTime = System.currentTimeMillis(); //最后一次登录时间
    private AtomicLong currentRoleId; // --> AtomicLong
    private AtomicInteger roleIdVersion; // -- 角色id的版本号（用于挤号时设置roleId）
    private List<AccountRole> relativeRoleList; // 关联角色列表 --> CopyOnWriteArrayList
    private volatile LoginInfo loginInfo; // volatile
    private volatile String loginToken; // volatile
    private volatile String token; // volatile
    private volatile long lastLogLoginTime = System.currentTimeMillis();

    private byte firstTestAwardSign;//首测奖励记录(用于二测时发放奖励)
    private byte firstTestAwardGet; //二测奖励领取记录
    private List<Integer> activedJobs = new ArrayList<>();//被激活的职业列表
    private Map<Long, LoginRow> loginRowMap=new ConcurrentHashMap<>();//本账户下各角色的登陆记录
    /* 锁 */
    private Lock loginLock = new ReentrantLock(); // 用于登录
    private Lock fieldLock = new ReentrantLock(); // 用于修改钻石

    public AccountRow() {
        this.createdRoleCount = new AtomicInteger(0);
        this.currentRoleId = new AtomicLong(0);
        this.roleIdVersion = new AtomicInteger(0);
    }

    public AccountRow(String name, String channel) {
        this.name = name;
        this.firstLoginTimestamp = System.currentTimeMillis();
        this.channel = channel.toLowerCase();
        this.createdRoleCount = new AtomicInteger(0);
        this.currentRoleId = new AtomicLong(0);
        this.roleIdVersion = new AtomicInteger(0);
        this.relativeRoleList = new ArrayList<>();
    }

    public AccountRow copy() {
        AccountRow row = new AccountRow();
        row.name = this.name;
        row.firstLoginTimestamp = this.firstLoginTimestamp;
        row.lastCreateRoleTimestamp = this.lastCreateRoleTimestamp;
        row.createdRoleCount = new AtomicInteger(this.createdRoleCount.get());
        row.password = this.password;
        row.channel = this.channel;
        row.firstTestAwardSign = this.firstTestAwardSign;
        row.firstTestAwardGet = this.firstTestAwardGet;
        row.vipLevel = this.vipLevel;
        row.vipExp = this.vipExp;
        row.chargeSum = this.chargeSum;
        row.identification = this.identification;
        row.identAward = this.identAward;
        row.palform = this.palform;
        return row;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addCreateRoleCount() {
        this.createdRoleCount.incrementAndGet();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFirstLoginTimestamp() {
        return firstLoginTimestamp;
    }

    public void setFirstLoginTimestamp(long firstLoginTimestamp) {
        this.firstLoginTimestamp = firstLoginTimestamp;
    }

    public long getCurrentRoleId() {
        return currentRoleId.get();
    }

    public boolean setCurrentRoleId(long roleId, int roleIdVersion) {
        if (this.roleIdVersion.compareAndSet(roleIdVersion, roleIdVersion)) {
            this.currentRoleId.set(roleId);
            return true;
        }
        return false;
    }

    public boolean compareAndSetCurrentRoleId(long expect, long update) {
        return this.currentRoleId.compareAndSet(expect, update);
    }

    public int newRoleIdVersion() {
        return this.roleIdVersion.incrementAndGet();
    }

    public Lock getLoginLock() {
        return loginLock;
    }

    public void setLoginLock(Lock loginLock) {
        this.loginLock = loginLock;
    }

    public List<AccountRole> getRelativeRoleList() {
        return relativeRoleList;
    }

    public void setRelativeRoleList(List<AccountRole> relativeRoleList) {
        this.relativeRoleList = new CopyOnWriteArrayList<>(relativeRoleList);
    }

    public AccountRole getAccountRole(long roleId) {
        if (this.relativeRoleList != null) {
            for (AccountRole accountRole : relativeRoleList) {
                if (Long.parseLong(accountRole.getRoleId()) == roleId) {
                    return accountRole;
                }
            }
        }
        return null;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "account", "`name`='" + name + "'");
    }

    @Override
    public String getDeleteSql() {
        return null;
    }

    public int getChargeSum() {
        return chargeSum;
    }

    public void setChargeSum(int chargeSum) {
        this.chargeSum = chargeSum;

    }

    public byte getIdentification() {
        return identification;
    }

    public void setIdentification(byte identification) {
        this.identification = identification;
    }

    public byte getIdentAward() {
        return identAward;
    }

    public void setIdentAward(byte identAward) {
        this.identAward = identAward;
    }

    public long getLastCreateRoleTimestamp() {
        return lastCreateRoleTimestamp;
    }

    public void setLastCreateRoleTimestamp(long lastCreateRoleTimestamp) {
        this.lastCreateRoleTimestamp = lastCreateRoleTimestamp;
    }

    public int getCreatedRoleCount() {
        return createdRoleCount.get();
    }

    public void setCreatedRoleCount(int createdRoleCount) {
        this.createdRoleCount.set(createdRoleCount);
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
        if (StringUtil.isEmpty(channel)) {
            // todo：老账号渠道为空,先写死为ios
            this.channel = "1@2@1026";
        }
    }

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public void setLoginInfo(LoginInfo loginInfo) {
//		LoginInfo copy = new LoginInfo();
//		copy.accoutRegisterTime = loginInfo.accoutRegisterTime;
//		copy.channel = loginInfo.channel;
//		copy.channelSub = loginInfo.channelSub;
//		copy.imei = loginInfo.imei;
//		copy.ip = loginInfo.ip;
//		copy.mac = loginInfo.mac;
//		copy.net = loginInfo.net;
//		copy.phoneNet = loginInfo.phoneNet;
//		copy.phoneSystem = loginInfo.phoneSystem;
//		copy.phoneType = loginInfo.phoneType;
//		copy.platForm = loginInfo.platForm;
//		copy.regChannel = loginInfo.regChannel;
//		copy.sid = loginInfo.sid;
//		copy.uid = loginInfo.uid;
//		copy.verision = loginInfo.verision;
        this.loginInfo = loginInfo;
    }

    public String getRelativeRoleTime(String roleid) {
        String time = DateUtil.formatDateTime(System.currentTimeMillis());
        for (AccountRole accountrole : relativeRoleList) {
            if (accountrole.getRoleId().equals(roleid)) {
                time = accountrole.getTime().toString();
                time = time.substring(0, time.lastIndexOf("."));
                return time;
            }
        }
        return time;
    }

    public void sendLog(ServerLogModule log) {
        log.accept("account", this.getName());
        if (loginInfo != null) {
            log.accept("login_channel", this.loginInfo.getChannelSub());
            log.accept("uid", this.loginInfo.getUid());
            log.accept("phoneSystem", this.loginInfo.getPhoneSystem());
            log.accept("phoneNet", this.loginInfo.getPhoneNet());
            log.accept("reg_channel", this.loginInfo.getRegChannel());
            log.accept("verision", this.loginInfo.getVerision());
            log.accept("platForm", this.loginInfo.getPlatForm());
            log.accept("accoutRegisterTime", loginInfo.getAccoutRegisterTime());
            log.accept("mainChannel", loginInfo.getChannel().split("@")[0]);
            log.accept("osVersion", loginInfo.getOsVersion());
        }
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(long lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getLoginToken() {
        return loginToken;
    }

    public void setLoginToken(String loginToken) {
        this.loginToken = loginToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public byte getFirstTestAwardSign() {
        return firstTestAwardSign;
    }

    public void setFirstTestAwardSign(byte firstTestAwardSign) {
        this.firstTestAwardSign = firstTestAwardSign;
    }

    public String getPalform() {
        return palform;
    }

    public void setPalform(String palform) {
        this.palform = palform;
    }

    public long getLastLogLoginTime() {
        return lastLogLoginTime;
    }

    public void setLastLogLoginTime(long lastLogLoginTime) {
        this.lastLogLoginTime = lastLogLoginTime;
    }

    public byte getFirstTestAwardGet() {
        return firstTestAwardGet;
    }

    public void setFirstTestAwardGet(byte firstTestAwardGet) {
        this.firstTestAwardGet = firstTestAwardGet;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public int getVipExp() {
        return vipExp;
    }

    public void setVipExp(int vipExp) {
        this.vipExp = vipExp;
    }

    public List<Integer> getActivedJobs() {
        return activedJobs;
    }

    public void setActivedJobs(List<Integer> activedJobs) {
        this.activedJobs = activedJobs;
    }

    public void setLoginRowMap(Map<Long, LoginRow> loginRowMap) {
        this.loginRowMap = loginRowMap;
    }

    public Map<Long, LoginRow> getLoginRowMap() {
        return loginRowMap;
    }
}
