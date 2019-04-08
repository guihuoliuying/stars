package com.stars.modules.fashion;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.fashion.event.FashionChangeEvent;
import com.stars.modules.fashion.packet.ClientFashion;
import com.stars.modules.fashion.prodata.FashionAttrVo;
import com.stars.modules.fashion.prodata.FashionVo;
import com.stars.modules.fashion.summary.FashionSummaryComponentImpl;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.newequipment.packet.ClientNewEquipment;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.impl.FashionToolFunc;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;


/**
 * Created by gaopeidian on 2016/10/08.
 */
public class FashionModule extends AbstractModule {
    private Map<Integer, RoleFashion> roleFashionMap = new HashMap<Integer, RoleFashion>();

    public FashionModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Fashion, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        initUserData();
    }

    @Override
    public void onInit(boolean isCreation) {
        updateFashionAttrAndFightScore();

        //标记需要计算时装红点
        signCalRedPoint(MConst.Fashion, RedPointConst.ACTIVE_FASHION);
    }

    @Override
    public void onDataReq() throws Exception {
        initUserData();
    }

    @Override
    public void onSyncData() throws Throwable {
        Iterator iter = roleFashionMap.values().iterator();
        while (iter.hasNext()) {
            RoleFashion roleFashion = (RoleFashion) iter.next();
            if (roleFashion.getExpiredTime() == 0 && FashionManager.isTimeLimitedFashion(roleFashion.getFashionId())) { //永久时装变限时时装
                FashionVo fashionVo = FashionManager.getFashionVo(roleFashion.getFashionId());
                if (fashionVo == null)
                    continue;
                ItemVo item = ToolManager.getItemVo(fashionVo.getItemId());
                if (fashionVo == null)
                    continue;
                FashionToolFunc fashionToolFunc = (FashionToolFunc) item.getToolFunc();
                if (fashionToolFunc == null)
                    continue;
                roleFashion.setExpiredTime(now() + fashionToolFunc.getAddUseHour() * DateUtil.HOUR);
                roleFashionMap.put(roleFashion.getFashionId(), roleFashion);
                com.stars.util.LogUtil.info("时装永久改成限时，初始化限时时间|roleid:{}|fashionid{}", id(), roleFashion.getFashionId());
            }
        }

        RoleFashion roleFashion = getRoleDressingFashion();
        if (roleFashion != null && roleFashion.isExpired()) { //当前时装过期，则脱下
            undressFashion(roleFashion.getFashionId());
            com.stars.util.LogUtil.info("玩家上线时装过期|roleid:{}|fashionid:{}", id(), roleFashion.getFashionId());
        }
        sendCurrentFahsion2Client();
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            RoleFashion roleFashion = getRoleDressingFashion();
            if (roleFashion == null) {
                roleFashion = newRoleFashion(0);
            }
            componentMap.put(MConst.Fashion, new FashionSummaryComponentImpl(roleFashion));
        }
    }

    @Override
    public void onTimingExecute() {
        //时装的过期处理
        boolean isChange = false;
        Iterator iter = roleFashionMap.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Integer, RoleFashion> entry = (Entry<Integer, RoleFashion>) iter.next();
            RoleFashion roleFashion = entry.getValue();
            if (roleFashion != null && roleFashion.isExpired() && !roleFashion.isExpiredBefore()) { //如果当前限时时装过期,缓存没过期过
                com.stars.util.LogUtil.info("玩家时装过期|roleid:{}|fashionid:{}", id(), roleFashion.getFashionId());
                if (roleFashion.getFashionId() == getDressFashionId()) { //当前时装过期，则脱下
                    undressFashion(roleFashion.getFashionId());
                }
                roleFashion.setExpiredBefore(true); //在线检测到时装过期
                isChange = true;
            }
        }
        if (isChange) {
            //更新时装带来的属性和战力加成，并下发到客户端
            updateFashionAttrAndFightScoreWithSend();

            //标记需要计算时装红点
            signCalRedPoint(MConst.Fashion, RedPointConst.ACTIVE_FASHION);
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.ACTIVE_FASHION))) {
            StringBuilder builder = new StringBuilder("");
            for (RoleFashion roleFashion : roleFashionMap.values()) {
                if (roleFashion.getEverDress() == (byte) 0 && roleFashion.getIsDress() == (byte) 0 && !roleFashion.isExpired()) {//若此时装从未被穿过，则加入红点
                    builder.append(roleFashion.getFashionId()).append("+");
                }
            }
            redPointMap.put(RedPointConst.ACTIVE_FASHION, builder.toString().isEmpty() ? null : builder.toString());
        }
    }

    private void initUserData() throws SQLException {
        String sql = "select * from `rolefashion` where `roleid`=" + id();
        roleFashionMap = DBUtil.queryMap(DBUtil.DB_USER, "fashionId", RoleFashion.class, sql);
        if (roleFashionMap == null) {
            roleFashionMap = new HashMap<Integer, RoleFashion>();
        }
    }

    RoleFashion newRoleFashion(int fashionId) {
        return new RoleFashion(id(), fashionId, (byte) 0, (byte) 0, 0L);
    }

    RoleFashion getRoleFashion(int fashionId) {
        if (roleFashionMap.containsKey(fashionId)) {
            return roleFashionMap.get(fashionId);
        }

        return null;
    }

    //return -1:无穿着时装，other：当前穿着的时装id
    public int getDressFashionId() {
        Set<Entry<Integer, RoleFashion>> entrySet = roleFashionMap.entrySet();
        for (Entry<Integer, RoleFashion> entry : entrySet) {
            RoleFashion roleFashion = entry.getValue();
            byte isDress = roleFashion.getIsDress();
            if (isDress == 1) {
                return roleFashion.getFashionId();
            }
        }

        return -1;
    }

    /**
     * 获得玩家当前穿戴的时装
     *
     * @return
     */
    public RoleFashion getRoleDressingFashion() {
        int fashionId = getDressFashionId();
        if (fashionId == -1) {
            return null;
        }
        if (roleFashionMap.containsKey(fashionId)) {
            return roleFashionMap.get(fashionId);
        }
        return null;
    }

    public boolean getIsActive(int fashionId) {
        RoleFashion roleFashion = getRoleFashion(fashionId);
        if (roleFashion == null) {
            return false;
        } else {
            return roleFashion.isActive();
        }
    }


    public void sendAllFashionInfo() {
        Map<Integer, FashionVo> fashionVoMap = FashionManager.getFashionVoMap();

        ClientFashion clientFashion = new ClientFashion(ClientFashion.RESP_SYNC_ALL);
        clientFashion.setRoleFashionMap(roleFashionMap);
        clientFashion.setFashionVoMap(fashionVoMap);
        send(clientFashion);
    }

    /**
     * 激活时装，限时时装则增加使用小时数
     *
     * @param fashionId
     * @param addUseHours
     */
    public void activeFashion(int fashionId, int addUseHours, int itemId) {
        FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
        if (fashionVo == null) {
            warn(I18n.get("fashion.getFashionProductDataFail"));
            return;
        }

        if (FashionManager.isTimeLimitedFashion(fashionId)) { //是限时时装
            dealActiveTimeLimitFashion(fashionId, addUseHours);
            return;
        }

        //下面是非限时时装激活逻辑
        if (getIsActive(fashionId)) { //非限时，已获得则分解
            Map<Integer, Integer> map = null;
            ToolModule toolModule = module(MConst.Tool);
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            if (itemVo == null) {
                com.stars.util.LogUtil.info("分解时装|不存在itemId={}的道具产品数据", itemId);
                return;
            }
            // 先分解再增加物品
            try {
                map = toolModule.addAndSend(itemVo.getResolveMap(), EventType.RESOLVETOOL.getCode());
                com.stars.util.LogUtil.info("分解时装道具成功|roleid：{}|fashionid:{}|itemId:{}", id(), fashionId, itemVo.getItemId());
                ClientFashion clientFashion = new ClientFashion(ClientFashion.RESP_ACTIVE);
                clientFashion.setActiveType((byte) 2);
                clientFashion.setResolveGetMap(map);
                clientFashion.setRoleFashion(getRoleFashion(fashionId));
                send(clientFashion);

            } catch (Throwable cause) {
                com.stars.util.LogUtil.error("分解时装异常|roleid：{}|fashionid:{}|itemId:{}", id(), fashionId, itemVo.getItemId());
            }
            //warn(I18n.get("fashion.theFashionIsActived"));
            return;
        }

        RoleFashion roleFashion = newRoleFashion(fashionId);
        context().insert(roleFashion);
        roleFashionMap.put(roleFashion.getFashionId(), roleFashion);

        //通知客户端激活成功
        ClientFashion clientFashion = new ClientFashion(ClientFashion.RESP_ACTIVE);
        clientFashion.setRoleFashion(roleFashion);
        clientFashion.setActiveType((byte) 1);
        clientFashion.setShowExpressId(fashionVo.getShowExpressId());
        send(clientFashion);

        //更新时装带来的属性和战力加成，并下发到客户端
        updateFashionAttrAndFightScoreWithSend();

        //标记需要计算时装红点
        signCalRedPoint(MConst.Fashion, RedPointConst.ACTIVE_FASHION);
    }

    /**
     * 激活限时时装
     *
     * @param fashionId
     */
    public void dealActiveTimeLimitFashion(int fashionId, int addHours) {
        long now = System.currentTimeMillis();
        RoleFashion roleFashion = getRoleFashion(fashionId);
        byte isNowActive = (byte) 0;
        if (roleFashion == null) {
            roleFashion = newRoleFashion(fashionId);
            context().insert(roleFashion);
            isNowActive = (byte) 1;
        }
        //确定过期日期
        long remainExpiredTime = roleFashion.getExpiredTime() > now ? roleFashion.getExpiredTime() : now;
        long finalExpiredTime = remainExpiredTime + (addHours * DateUtil.HOUR);
        roleFashion.setExpiredTime(finalExpiredTime);
        roleFashionMap.put(roleFashion.getFashionId(), roleFashion);
        context().update(roleFashion);
        com.stars.util.LogUtil.info("玩家激活了限时时装|roleid:{}|fashionid:{}|expiredTime:{}", id(), roleFashion.getFashionId(), roleFashion.getExpiredTime());
        //通知客户端激活成功
        ClientFashion clientFashion = new ClientFashion(ClientFashion.RESP_ACTIVE);
        int remainSecond = (int) ((finalExpiredTime - now) / DateUtil.SECOND);
        int addSecond = (int) (addHours * DateUtil.HOUR / DateUtil.SECOND);
        FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
        clientFashion.setActiveType(isNowActive);
        clientFashion.setShowExpressId(fashionVo.getShowExpressId());
        clientFashion.setRoleFashion(roleFashion);
        clientFashion.setRemainSecond(remainSecond);
        clientFashion.setAddSecond(addSecond);
        send(clientFashion);
        sendCurrentFahsion2Client();

        //更新时装带来的属性和战力加成，并下发到客户端
        updateFashionAttrAndFightScoreWithSend();

        //标记需要计算时装红点
        signCalRedPoint(MConst.Fashion, RedPointConst.ACTIVE_FASHION);
    }

    public void dressFashion(int fashionId) {
        FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
        if (fashionVo == null) {
            warn(I18n.get("fashion.getFashionProductDataFail"));
            return;
        }

        RoleFashion roleFashion = getRoleFashion(fashionId);
        if (roleFashion == null) {
            warn(I18n.get("fashion.theFashionIsNotActived"));
            return;
        }

        if (roleFashion.isExpired()) {
            warn(I18n.get("fashion.theFashionIsExpired"));
            return;
        }

        if (roleFashion.getIsDress() == (byte) 1) {
            warn(I18n.get("fashion.theFashionIsDressed"));
            return;
        }

        //将其他已经穿戴的时装脱下
        Set<Entry<Integer, RoleFashion>> entrySet = roleFashionMap.entrySet();
        for (Entry<Integer, RoleFashion> entry : entrySet) {
            RoleFashion tempRoleFashion = entry.getValue();
            int tempFashionId = tempRoleFashion.getFashionId();
            byte isDress = tempRoleFashion.getIsDress();
            if (isDress == (byte) 1 && tempFashionId != fashionId) {
                tempRoleFashion.setIsDress((byte) 0);
                context().update(tempRoleFashion);
            }
        }

        //将该时装穿上
        roleFashion.setIsDress((byte) 1);
        //将该时装标记为已穿过
        roleFashion.setEverDress((byte) 1);
        context().update(roleFashion);

        warn(I18n.get("fashion.fashion_tips_takeon"));

        //发穿戴成功的包到客户端(同步该时装的信息到客户端)
        sendCurrentFahsion2Client();


        //同步装备孔位信息
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC);
        if (StringUtil.isEmpty(roleFashion)) {
            roleFashion = new RoleFashion(id(), 0, (byte) 0, (byte) 0, 0L);
        }
        client.setRoleCurrentDressingFashion(roleFashion);
        send(client);

        // fire event
        int curFashionId = getDressFashionId();
        eventDispatcher().fire(new FashionChangeEvent(curFashionId));

        //标记需要计算时装红点
        signCalRedPoint(MConst.Fashion, RedPointConst.ACTIVE_FASHION);

        //更新时装summary数据
        updateFashionSummaryComp();
    }

    public void undressFashion(int fashionId) {
        FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
        if (fashionVo == null) {
            warn(I18n.get("fashion.getFashionProductDataFail"));
            return;
        }

        RoleFashion roleFashion = getRoleFashion(fashionId);
        if (roleFashion == null) {
            warn(I18n.get("fashion.theFashionIsNotActived"));
            return;
        }

        if (roleFashion.getIsDress() == (byte) 0) {
            warn(I18n.get("fashion.theFashionIsNotDressed"));
            return;
        }

        //将该时装脱下
        roleFashion.setIsDress((byte) 0);
        context().update(roleFashion);

        warn(I18n.get("fashion.fashion_tips_takeoff"));

        //发穿戴成功的包到客户端(同步该时装的信息到客户端)
        sendCurrentFahsion2Client();


        //同步装备孔位信息
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC);
        roleFashion = new RoleFashion(id(), 0, (byte) 0, (byte) 0, 0L);
        client.setRoleCurrentDressingFashion(roleFashion);
        send(client);

        int curFashionId = getDressFashionId();
        // fire event
        eventDispatcher().fire(new FashionChangeEvent(curFashionId));

        //更新时装summary数据
        updateFashionSummaryComp();
    }

    public void undressFashion() {
        RoleFashion roleFashion = getRoleDressingFashion();
        if (roleFashion != null) {
            undressFashion(roleFashion.getFashionId());
        }
    }

    /**
     * 发送当前的时装到客户端
     */
    private void sendCurrentFahsion2Client() {
        int curFashionId = getDressFashionId();
        long expirdTimestamp = 0L;
        if (curFashionId != -1) {
            RoleFashion currenFahsion = getRoleDressingFashion();
            expirdTimestamp = currenFahsion.getExpiredTime() > now() ? currenFahsion.getExpiredTime() : 0L;

        }
        ClientFashion clientFashion = new ClientFashion(ClientFashion.RESP_SYNC_CUR_FASHION);
        clientFashion.setCurFashionId(curFashionId);
        clientFashion.setExpiredTimestamp(expirdTimestamp);
        send(clientFashion);
    }

    public void updateFashionAttrAndFightScore() {
        //计算
        Attribute attr = new Attribute();
        int fightScore = 0;

        Set<Entry<Integer, RoleFashion>> entrySet = roleFashionMap.entrySet();
        for (Entry<Integer, RoleFashion> entry : entrySet) {
            RoleFashion roleFashion = entry.getValue();
            if (!roleFashion.isActive())
                continue;
            int fashionId = roleFashion.getFashionId();
            FashionAttrVo fashionAttrVo = FashionManager.getFasionAttrVo(fashionId);
            attr.addAttribute(fashionAttrVo.getAttribute());
        }

        //添加属性加成的战力
        fightScore += FormularUtils.calFightScore(attr);

        //更新
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr("fashion", attr);
        roleModule.updatePartFightScore("fashion", fightScore);
    }

    public void updateFashionAttrAndFightScoreWithSend() {
        //更新
        updateFashionAttrAndFightScore();
        //发送到客户端
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    private void updateFashionSummaryComp() {
//    	ServiceHelper.summaryService().updateSummaryComponent(id(), new FashionSummaryComponentImpl(getDressFashionId()));
        context().markUpdatedSummaryComponent(MConst.Fashion);
    }

    public Map<Integer, RoleFashion> getRoleFashionMap() {
        return roleFashionMap;
    }

    public void setRoleFashionMap(Map<Integer, RoleFashion> roleFashionMap) {
        this.roleFashionMap = roleFashionMap;
    }

    public void onChangeJob(int newJobId) {
        Map<Integer, RoleFashion> newRoleFashionMap = new HashMap<>();
        for (Map.Entry<Integer, RoleFashion> entry : roleFashionMap.entrySet()) {
            RoleFashion roleFashion = entry.getValue();
            RoleFashion newRoleFashion = null;
            try {
                newRoleFashion = (RoleFashion) roleFashion.clone();
            } catch (CloneNotSupportedException e) {
                LogUtil.error(e.getMessage(), e);
            }
            int fashionId = roleFashion.getFashionId();
            FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
            if (fashionVo == null) {
                context().delete(roleFashion);
                continue;
            }
            FashionVo newFashion = FashionManager.jobFashionMap.get(newJobId).get(fashionVo.getType());
            newRoleFashion.setFashionId(newFashion.getFashionId());
            if (!newRoleFashionMap.containsKey(newRoleFashion.getFashionId())) {
                context().delete(roleFashion);
                context().insert(newRoleFashion);
                newRoleFashionMap.put(newFashion.getFashionId(), newRoleFashion);
            } else {
                context().delete(roleFashion);
            }
        }
        setRoleFashionMap(newRoleFashionMap);
        //更新时装summary数据
        updateFashionSummaryComp();
    }
}

