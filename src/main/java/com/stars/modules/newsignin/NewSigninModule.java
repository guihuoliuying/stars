package com.stars.modules.newsignin;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.drop.DropModule;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.newsignin.event.SigninAchieveEvent;
import com.stars.modules.newsignin.packet.ClientRoleSignin;
import com.stars.modules.newsignin.packet.ClientSigninAward;
import com.stars.modules.newsignin.packet.ClientSigninVo;
import com.stars.modules.newsignin.prodata.SigninVo;
import com.stars.modules.newsignin.userdata.RoleSigninPo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.vip.VipModule;
import com.stars.util.I18n;
import com.stars.util.TimeUtil;
import com.stars.util._HashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chenkeyu on 2017/2/5 17:08
 */
public class NewSigninModule extends AbstractModule {

    private RoleSigninPo roleSigninPo;

    public NewSigninModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleSigninPo = new RoleSigninPo(id());
        _HashMap map = DBUtil.querySingleMap(DBUtil.DB_USER, "select signeddates,signcount,resigncount,accawards,specawards from rolesignin where roleid=" + id());
        String signedIdStr;
        String accAwardsStr;
        String specAwardsStr;
        int signCount;
        int reSignCount;
        if (map != null && map.size() > 0) {
            signedIdStr = (String) map.get("rolesignin.signeddates");
            accAwardsStr = (String) map.get("rolesignin.accawards");
            specAwardsStr = (String) map.get("rolesignin.specawards");
            signCount = map.getInt("rolesignin.signcount");
            reSignCount = map.getInt("rolesignin.resigncount");
            roleSigninPo.setSignedDatesStr(signedIdStr);
            roleSigninPo.setAccAwardsStr(accAwardsStr);
            roleSigninPo.setSpecAwardsStr(specAwardsStr);
            roleSigninPo.setSignCount(signCount);
            roleSigninPo.setReSignCount(reSignCount);
        } else {
            roleSigninPo.setSignCount(0);
            roleSigninPo.setReSignCount(0);
            context().insert(roleSigninPo);
        }
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        roleSigninPo = new RoleSigninPo(id());
        roleSigninPo.setSignedDatesStr("");
        roleSigninPo.setSignCount(0);
        roleSigninPo.setReSignCount(0);
        context().insert(roleSigninPo);
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        signCalRedPoint(MConst.SignIn, RedPointConst.SIGNIN);
        signCalRedPoint(MConst.SignIn, RedPointConst.ACCSIGNIN);
    }

    @Override
    public void onMonthlyReset() throws Throwable {
        flushProDataToClient();
        roleSigninPo.resetSign();
        roleSigninPo.resetAccAwards();
        roleSigninPo.resetSpecAwards();
        roleSigninPo.setSignCount(0);
        roleSigninPo.setReSignCount(0);
        context().update(roleSigninPo);
        flushRoleSigninToClient();
    }

    /**
     * 签到
     *
     * @param signDate date
     */
    public void doSignin(String signDate) {
        if (!isOpen()) {
            warn(I18n.get("signin.unopen"));
            return;
        }
        if (roleSigninPo.getSignedDatesSet().contains(signDate)) {
            warn(I18n.get("signin.signined"));
            return;
        }
        if (isAfterToday(signDate)) {
            warn(I18n.get("signin.aftertoday"));
            return;
        }
        if (!isToday(signDate)) {
            reSignin(signDate);
        } else {
            flushAwardToClient(doDrop(signDate, 0, NewSigninConst.singleSign), ClientSigninAward.signAward);
            roleSigninPo.addSign(signDate);
            roleSigninPo.signCountInc();
            specialAward();
            flushRoleSigninToClient();
            context().update(roleSigninPo);
            eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_SIGNID, 1));
            fireSigninAchieveEvent();
        }
        signCalRedPoint(MConst.SignIn, RedPointConst.SIGNIN);
        signCalRedPoint(MConst.SignIn, RedPointConst.ACCSIGNIN);
    }

    private void fireSigninAchieveEvent(){
        SigninAchieveEvent event = new SigninAchieveEvent();
        eventDispatcher().fire(event);
    }

    /**
     * 补签
     *
     * @param signDate date
     */
    private void reSignin(String signDate) {
        if (roleSigninPo.getSignedDatesSet().contains(signDate)) {
            warn(I18n.get("signin.signrepeat"));
            return;
        }
        ToolModule tool = module(MConst.Tool);
        if (!tool.deleteAndSend(NewSigninManager.getReqItemMap(roleSigninPo.getReSignCount()), EventType.SIGNIN.getCode())) {
            warn(I18n.get("signin.itemnotenough"));
            return;
        }
        flushAwardToClient(doDrop(signDate, 0, NewSigninConst.singleSign), ClientSigninAward.signAward);
        roleSigninPo.addSign(signDate);
        roleSigninPo.signCountInc();
        roleSigninPo.reSignCountInc();
        flushRoleSigninToClient();
        specialAward();
        context().update(roleSigninPo);
    }

    /**
     * 累积奖励
     *
     * @param times 累积签到次数
     */
    public void accumulateAward(int times) {
        if (roleSigninPo.getSignedDatesSet().size() < times) {
            warn(I18n.get("signin.acccountnotenough"));
            return;
        }
        if (roleSigninPo.getAccAwardsSet().contains(times)) {
            warn(I18n.get("signin.getawardrepeat"));
            return;
        }
        SigninVo vo = NewSigninManager.getAccumulateAwardMap(TimeUtil.getDateYYYYMM()).get(times);
        if (vo == null) {
            warn(I18n.get("signin.notaccdata"));
            return;
        }
        roleSigninPo.addAccAwards(times);
        flushAwardToClient(doDrop(vo.getYyyymm(), times, NewSigninConst.accumulateAward), ClientSigninAward.accAward);
        flushRoleSigninToClient();
        context().update(roleSigninPo);
        signCalRedPoint(MConst.SignIn, RedPointConst.ACCSIGNIN);
    }

    /**
     * 奖励走掉落
     *
     * @param signDate id
     * @param times    次数
     * @param type     操作类型
     * @return
     */
    private Map<Integer, Integer> doDrop(String signDate, int times, int type) {
        Map<Integer, Integer> itemMap;
        DropModule module = module(MConst.Drop);
        switch (type) {
            case NewSigninConst.singleSign:
                SigninVo singleSignVo = NewSigninManager.getSigninVo(signDate);
                if (singleSignVo.getBenefit0() == 1) {
                    itemMap = doVipBenefit(module.executeDrop(singleSignVo.getReward(), 1, true), singleSignVo);
                } else {
                    itemMap = doBenefit(module.executeDrop(singleSignVo.getReward(), 1, true), singleSignVo);
                }
                break;
            case NewSigninConst.accumulateAward:
                SigninVo accumulateAwardVo = NewSigninManager.getAccumulateAwardMap(signDate).get(times);
                if (accumulateAwardVo.getBenefit0() == 1) {
                    itemMap = doVipBenefit(module.executeDrop(accumulateAwardVo.getReward(), 1, true), accumulateAwardVo);
                } else {
                    itemMap = doBenefit(module.executeDrop(accumulateAwardVo.getReward(), 1, true), accumulateAwardVo);
                }
                break;
            case NewSigninConst.specialAward:
                SigninVo specialAwardVo = NewSigninManager.getSpecialAwardMap(signDate).get(times);
                if (specialAwardVo.getBenefit0() == 1) {
                    itemMap = doVipBenefit(module.executeDrop(specialAwardVo.getReward(), 1, true), specialAwardVo);
                } else {
                    itemMap = doBenefit(module.executeDrop(specialAwardVo.getReward(), 1, true), specialAwardVo);
                }
                break;
            default:
                itemMap = new HashMap<>();
                break;
        }
        return itemMap;
    }

    /**
     * 特殊奖励
     */
    private void specialAward() {
        SigninVo vo = NewSigninManager.getSpecialAwardMap(TimeUtil.getDateYYYYMM()).get(roleSigninPo.getSignCount());
        if (vo != null) {
            roleSigninPo.addSpecAwards(roleSigninPo.getSignCount());
            flushAwardToClient(doDrop(vo.getYyyymm(), roleSigninPo.getSignCount(), NewSigninConst.specialAward), ClientSigninAward.specialAward);
            flushRoleSigninToClient();
        }
    }

    /**
     * 给玩家加道具
     *
     * @param itemMap
     */
    private void addAndSend(Map<Integer, Integer> itemMap) {
        ToolModule module = module(MConst.Tool);
        module.addAndSend(itemMap, EventType.SIGNIN.getCode());
    }

    /**
     * vip翻倍
     *
     * @param itemMap
     * @param vo
     * @return
     */
    private Map<Integer, Integer> doVipBenefit(Map<Integer, Integer> itemMap, SigninVo vo) {
        Map<Integer, Integer> itemMapBenefit = new HashMap<>();
        VipModule vip = module(MConst.Vip);
        int vipLevel = vip.getVipLevel();
        if (vo.getVipLv() <= vipLevel) {
            for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
                itemMapBenefit.put(entry.getKey(), vo.getBenefit() * entry.getValue());
            }
            return itemMapBenefit;
        } else {
            return itemMap;
        }
    }

    private Map<Integer, Integer> doBenefit(Map<Integer, Integer> itemMap, SigninVo vo) {
        Map<Integer, Integer> itemMapBenefit = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : itemMap.entrySet()) {
            itemMapBenefit.put(entry.getKey(), vo.getBenefit0() * entry.getValue());
        }
        return itemMapBenefit;
    }

    /**
     * 发送奖励到客户端
     *
     * @param itemMap
     * @param type
     */
    private void flushAwardToClient(Map<Integer, Integer> itemMap, byte type) {
        addAndSend(itemMap);
        ClientSigninAward csa = new ClientSigninAward(type);
        csa.setItemMap(itemMap);
        send(csa);
    }

    /**
     * 发送产品数据
     */
    public void flushProDataToClient() {
        ClientSigninVo csv = new ClientSigninVo();
        csv.setSingleSignList(NewSigninManager.getSingleSignList(TimeUtil.getDateYYYYMM()));
        csv.setAccumulateAwardlist(NewSigninManager.getAccAwardList(TimeUtil.getDateYYYYMM()));
        csv.setSpecialAwardList(NewSigninManager.getSpecAwardList(TimeUtil.getDateYYYYMM()));
        send(csv);
    }

    /**
     * 发送玩家数据到客户端
     */
    public void flushRoleSigninToClient() {
        ClientRoleSignin crs = new ClientRoleSignin();
        crs.setRoleSigninPo(roleSigninPo);
        crs.setIsOpen(isOpen() ? 1 : 0);
        send(crs);
    }

    /**
     * 判断月签到系统是否已开放
     *
     * @return
     */
    private boolean isOpen() {
        ForeShowModule foreshow = module(MConst.ForeShow);
        if (!foreshow.isOpen(ForeShowConst.DAILY_SIGNIN)) {
            return false;
        }
        if (DataManager.getServerDays() < NewSigninManager.getServerOpenDays()) {
            return false;
        }
        RoleModule role = module(MConst.Role);
        if (role.getLevel() < Integer.parseInt(DataManager.getCommConfig("signin_autointerface_level"))) {
            return false;
        }
        return true;
    }

    public void doChangeEvent() {
        flushRoleSigninToClient();
    }

    /**
     * 判断是否是今天
     *
     * @param signDate
     * @return
     */
    private boolean isToday(String signDate) {
        if (TimeUtil.getDateYYYYMMDD().equals(signDate)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是今天之后
     *
     * @param signDate
     * @return
     */
    private boolean isAfterToday(String signDate) {
        if (signDate.compareTo(TimeUtil.getDateYYYYMMDD()) > 0) {
            return true;
        }
        return false;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.SIGNIN)) {
            checkSigninRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.ACCSIGNIN)) {
            checkAccSigninRedPoint(redPointMap);
        }
    }

    private void checkSigninRedPoint(Map<Integer, String> redPointMap) {
        if (isOpen()) {
            StringBuilder builder = new StringBuilder("");
            if (!roleSigninPo.getSignedDatesSet().contains(TimeUtil.getDateYYYYMMDD())) {
                builder.append(TimeUtil.getDateYYYYMMDD());
            }
            redPointMap.put(RedPointConst.SIGNIN,
                    builder.toString().isEmpty() ? null : builder.toString());
        }
    }

    private void checkAccSigninRedPoint(Map<Integer, String> redPointMap) {
        if (isOpen()) {
            StringBuilder builder = new StringBuilder("");
            for (Integer times : NewSigninManager.getAccumulateAwardMap(TimeUtil.getDateYYYYMM()).keySet()) {
                if (times <= roleSigninPo.getSignCount()) {
                    if (!roleSigninPo.getAccAwardsSet().contains(times)) {
                        builder.append(times).append("+");
                    }
                }
            }
            redPointMap.put(RedPointConst.ACCSIGNIN,
                    builder.toString().isEmpty() ? null : builder.toString());
        }
    }
}
