package com.stars.modules.mind;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.mind.packet.ClientMindInfo;
import com.stars.modules.mind.packet.ClientMindVo;
import com.stars.modules.mind.packet.ClientUpgradeMind;
import com.stars.modules.mind.prodata.MindActiveData;
import com.stars.modules.mind.prodata.MindLevelVo;
import com.stars.modules.mind.prodata.MindVo;
import com.stars.modules.mind.userdata.RoleMind;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.trump.TrumpModule;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;


/**
 * Created by gaopeidian on 2016/9/21.
 */
public class MindModule extends AbstractModule {
    private Map<Integer, RoleMind> roleMindMap = new HashMap<Integer, RoleMind>();

    public MindModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.Mind, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        initUserData();
    }

    @Override
    public void onInit(boolean isCreation) {
        updateMindAttrAndFightScore();
        signCalRedPoint(MConst.Mind, RedPointConst.MIND_LVUP);
        signCalRedPoint(MConst.Mind, RedPointConst.MIND_ACTIVE);
    }

    @Override
    public void onDataReq() throws Exception {
        initUserData();
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.MIND_LVUP))) {
            canLvUp(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.MIND_ACTIVE))) {
            canActive(redPointMap);
        }
    }

    private void canLvUp(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        for (RoleMind roleMind : roleMindMap.values()) {
            int nowLevel = roleMind.getMindLevel();
            int mindId = roleMind.getMindId();
            if (nowLevel > 0) {
                if (nowLevel >= MindManager.getMindMaxLevel(mindId)) //已满级
                    continue;
                int toLevel = nowLevel + 1;
                MindLevelVo toMindLevelVo = MindManager.getMindLevelVo(mindId, toLevel);
                if (toMindLevelVo == null) { //缺乏配置
                    continue;
                }
                Map<Integer, Integer> material = toMindLevelVo.getMaterialMap();
                ToolModule toolModule = (ToolModule) module(MConst.Tool);
                Iterator<Entry<Integer, Integer>> iter = material.entrySet().iterator();
                boolean isToolEnough = true;
                while (iter.hasNext()) {
                    Entry<Integer, Integer> entry = iter.next();
                    int itemId = entry.getKey();
                    int count = entry.getValue();
                    if (!toolModule.contains(itemId, count)) { //道具不足
                        isToolEnough = false;
                    }
                }
                if (isToolEnough)
                    builder.append(mindId).append("+");
            }
        }
        redPointMap.put(RedPointConst.MIND_LVUP, builder.toString().isEmpty() ? null : builder.toString());
    }

    private void canActive(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        Map<Integer, MindVo> mindMap = MindManager.getMindVoMap();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        for (MindVo mindvo : mindMap.values()) {
            int mindId = mindvo.getMindId();
            RoleMind roleMind = getRoleMind(mindId);
            if (roleMind != null && roleMind.getMindLevel() > 0) {
                continue;//已激活
            }
            if (canActive(mindId)) {
                Iterator<Entry<Integer, Integer>> iter = mindvo.getActivecostMap().entrySet().iterator();
                boolean isToolEnough = true;
                while (iter.hasNext()) {
                    Entry<Integer, Integer> entry = iter.next();
                    int itemId = entry.getKey();
                    int count = entry.getValue();
                    if (!toolModule.contains(itemId, count)) { //道具不足
                        isToolEnough = false;
                    }
                }
                if (isToolEnough) {
                    builder.append(mindId).append("+");
                }
            }
        }
        redPointMap.put(RedPointConst.MIND_ACTIVE, builder.toString().isEmpty() ? null : builder.toString());
    }

    private void initUserData() throws SQLException {
        String sql1 = "select * from `rolemind` where `roleid`=" + id();
        roleMindMap = DBUtil.queryMap(DBUtil.DB_USER, "mindId", RoleMind.class, sql1);
        if(roleMindMap == null){
            roleMindMap = new HashMap<>();
        }
        Map<Integer, MindVo> mindVos = MindManager.getMindVoMap();
        if (mindVos != null) {
            Set<Entry<Integer, MindVo>> set = mindVos.entrySet();
            for (Entry<Integer, MindVo> entry : set) {
                MindVo mindVo = entry.getValue();
                if(roleMindMap.containsKey(mindVo.getMindId()))
                    continue;
                RoleMind roleMind = newRoleMind(mindVo.getMindId());
                context().insert(roleMind);
                roleMindMap.put(roleMind.getMindId(), roleMind);
            }
        }
    }

    public String getRoleMindStr() {
        StringBuilder sb = new StringBuilder();
        sb.append("magic_skill@lv:");
        boolean hav = false;
        for (RoleMind roleMind : roleMindMap.values()) {
            sb.append(roleMind.getMindId()).append("@").append(roleMind.getMindLevel()).append("&");
            hav = true;
        }
        if (hav) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public RoleMind getRoleMind(int mindId) {
        if (roleMindMap.containsKey(mindId)) {
            return roleMindMap.get(mindId);
        }

        return null;
    }

    RoleMind newRoleMind(int mindId) {
        return new RoleMind(id(), mindId, 0);
    }

    public void upgradeMind(int mindId) {
        //判断配置表是否存在此心法
        MindVo mindVo = MindManager.getMindVo(mindId);
        if (mindVo == null) {
            com.stars.util.LogUtil.info("MindModule.upgradeMind get mindVo is null,roleid=" + id() + ",mindId=" + mindId);
            warn(I18n.get("mind.cantFindMindProductData"));
            return;
        }

        RoleMind roleMind = getRoleMind(mindId);
        if (roleMind == null) {
            roleMind = newRoleMind(mindId);
            context().insert(roleMind);
            roleMindMap.put(roleMind.getMindId(), roleMind);
        }

        int nowLevel = roleMind.getMindLevel();

        //判断心法是否已激活了
        if (nowLevel <= 0) {//若未激活，则走激活流程
            if (canActive(mindId)) {
                //道具是否足够,足够就直接扣除道具
                Map<Integer, Integer> activeCost = mindVo.getActivecostMap();
                ToolModule toolModule = (ToolModule) module(MConst.Tool);

                Iterator<Entry<Integer, Integer>> iter = activeCost.entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<Integer, Integer> entry = iter.next();
                    int itemId = entry.getKey();
                    int count = entry.getValue();
                    if (!toolModule.contains(itemId, count)) {
                        warn(I18n.get("mind.callboss_itemshort"), ToolManager.getItemVo(itemId).getName());// 道具不足
                        return;
                    }
                }

                if (!toolModule.deleteAndSend(activeCost, EventType.UPMIND.getCode())) {
                    com.stars.util.LogUtil.info("MindModule.upgradeMind item is not enough,roleid=" + id() + ",mindId=" + mindId);
                    warn(I18n.get("mind.itemNotEnoughCantActive"));
                    return;
                }

                //激活心法
                int initLevel = 1;
                roleMind.setMindLevel(initLevel);
                context().update(roleMind);
                signCalRedPoint(MConst.Mind, RedPointConst.MIND_LVUP);
                signCalRedPoint(MConst.Mind, RedPointConst.MIND_ACTIVE);

                //修改心法对玩家的属性和战力加成
                updateMindAttrAndFightScoreWithSend();

                //发包给客户端
                //warn("激活成功");
                sendUpradeMind(mindId);

                return;
            } else {
                String desc = getCantActiveDesc(mindId);
                warn(desc);
                return;
            }
        } else {//若已激活，则走升级流程
            //判断是否满级了
            if (nowLevel >= MindManager.getMindMaxLevel(mindId)) {
                com.stars.util.LogUtil.info("MindModule.upgradeMind mind level is max,roleid=" + id() + ",mindId=" + mindId);
                warn(I18n.get("mind.mindReachMaxLevelCantUpgrade"));
                return;
            }

            //道具是否足够,足够就直接扣除道具
            int toLevel = nowLevel + 1;
            MindLevelVo toMindLevelVo = MindManager.getMindLevelVo(mindId, toLevel);
            if (toMindLevelVo == null) {
                com.stars.util.LogUtil.info("MindModule.upgradeMind toMindLevelVo is null,roleid=" + id() + ",mindId=" + mindId + ",toMindLevelVo=" + toMindLevelVo);
                warn(I18n.get("mind.cantFindProductDataAfterUpgrade"));
                return;
            }

            Map<Integer, Integer> material = toMindLevelVo.getMaterialMap();
            ToolModule toolModule = (ToolModule) module(MConst.Tool);

            Iterator<Entry<Integer, Integer>> iter = material.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<Integer, Integer> entry = iter.next();
                int itemId = entry.getKey();
                int count = entry.getValue();
                if (!toolModule.contains(itemId, count)) {
                    warn(I18n.get("mind.callboss_itemshort"), ToolManager.getItemVo(itemId).getName()); // 道具不足
                    return;
                }
            }

            if (!toolModule.deleteAndSend(material, EventType.UPMIND.getCode())) {
                com.stars.util.LogUtil.info("MindModule.upgradeMind item is not enough,roleid=" + id() + ",mindId=" + mindId);
                warn(I18n.get("mind.itemNotEnoughCantUpgrade"));
                return;
            }

            //升级心法
            roleMind.setMindLevel(toLevel);
            context().update(roleMind);
            signCalRedPoint(MConst.Mind, RedPointConst.MIND_LVUP);

            //修改心法对玩家的属性和战力加成
            updateMindAttrAndFightScoreWithSend();

            warn(I18n.get("mind.mind_btntext_levelupsuc"));

            sendUpradeMind(mindId);

            return;
        }
    }

    public boolean canActive(int mindId) {
        MindVo mindVo = MindManager.getMindVo(mindId);
        if (mindVo == null) {
            LogUtil.info("MindModule.canActive get mindVo is null,roleid=" + id() + ",mindId=" + mindId);
            return false;
        }
        List<MindActiveData> activeDatas = mindVo.getActiveDatas();
        for (MindActiveData data : activeDatas) {
            int typeId = data.typeId;
            int param = data.param;
            switch (typeId) {
                case MindConstant.ACTIVE_TYPE_ROLE_LEVEL: {
                    int level = param;
                    RoleModule roleModule = (RoleModule) module(MConst.Role);
                    if (roleModule.getLevel() < level) {
                        return false;
                    }
                    break;
                }
                case MindConstant.ACTIVE_TYPE_UNLOCK_TRUMP: {
                    int trumpId = param;
                    if (!isTrumpUnlock(trumpId)) {
                        return false;
                    }
                    break;
                }
                case MindConstant.ACTIVE_TYPE_UNLOCK_TRUMP_NUM: {
                    int unlockNum = param;
                    if (getUnlockTrumpNum() < unlockNum) {
                        return false;
                    }
                    break;
                }
                default:
                    break;
            }
        }

        return true;
    }

    public String getCantActiveDesc(int mindId) {
        MindVo mindVo = MindManager.getMindVo(mindId);
        if (mindVo == null) {
            return "";
        }
        List<MindActiveData> activeDatas = mindVo.getActiveDatas();
        for (MindActiveData data : activeDatas) {
            int typeId = data.typeId;
            int param = data.param;
            switch (typeId) {
                case MindConstant.ACTIVE_TYPE_ROLE_LEVEL: {
                    int level = param;
                    RoleModule roleModule = (RoleModule) module(MConst.Role);
                    if (roleModule.getLevel() < level) {
                        return I18n.get("mind.mind_active_errordesc_rolelvl");
                    }
                    break;
                }
                case MindConstant.ACTIVE_TYPE_UNLOCK_TRUMP: {
                    int trumpId = param;
                    if (!isTrumpUnlock(trumpId)) {
                        return I18n.get("mind.mind_active_errordesc_trump");
                    }
                    break;
                }
                case MindConstant.ACTIVE_TYPE_UNLOCK_TRUMP_NUM: {
                    int unlockNum = param;
                    if (getUnlockTrumpNum() < unlockNum) {
                        return I18n.get("mind.mind_active_errordesc_trumpactive");
                    }
                    break;
                }
                default:
                    break;
            }
        }

        return "";
    }

    public byte getMindState(int mindId) {
        RoleMind roleMind = getRoleMind(mindId);
        if (roleMind == null) {
            return MindConstant.MIND_STATE_CAN_NOT_ACTIVE;
        }

        if (roleMind.getMindLevel() > 0) {
            return MindConstant.MIND_STATE_ACTIVE;
        }

        if (roleMind.getMindLevel() <= 0 && canActive(mindId)) {
            return MindConstant.MIND_STATE_NOT_ACTIVE;
        } else {
            return MindConstant.MIND_STATE_CAN_NOT_ACTIVE;
        }
    }

    public boolean isTrumpUnlock(int trumpId) {
        TrumpModule trumpModule = (TrumpModule) module(MConst.Trump);
        Map<Integer, RoleTrumpRow> trumps = trumpModule.getRoleTrumpMap();
        if (trumps != null) {
            return trumps.containsKey(trumpId);
        }

        return false;
    }

    public int getUnlockTrumpNum() {
        int unlockNum = 0;
        TrumpModule trumpModule = module(MConst.Trump);
        Map<Integer, RoleTrumpRow> trumps = trumpModule.getRoleTrumpMap();
        if (trumps != null) {
            unlockNum = trumps.size();
        }

        return unlockNum;
    }

    public void updateMindAttrAndFightScore() {
        //计算
        Attribute attr = new Attribute();
        int fightScore = 0;

        Set<Entry<Integer, RoleMind>> entrySet = roleMindMap.entrySet();
        for (Entry<Integer, RoleMind> entry : entrySet) {
            RoleMind roleMind = entry.getValue();
            int mindId = roleMind.getMindId();
            int mindLevel = roleMind.getMindLevel();
            MindLevelVo mindLevelVo = MindManager.getMindLevelVo(mindId, mindLevel);
            //if (mindLevelVo != null) {
            if (mindLevel > 0 && mindLevelVo != null) {
                attr.addAttribute(mindLevelVo.getVoAttribute());//角色基本属性
                attr.addAttribute(mindLevelVo.getResistAttribute());//抗性(或穿透性)属性
                fightScore += mindLevelVo.getFightscore();
            }
        }

        //添加属性加成的战力
        fightScore += FormularUtils.calFightScore(attr);

        //更新
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr(MConst.Mind, attr);
        roleModule.updatePartFightScore(MConst.Mind, fightScore);
    }

    public void updateMindAttrAndFightScoreWithSend() {
        //更新
        updateMindAttrAndFightScore();
        //发送到客户端
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.sendRoleAttr();
        roleModule.sendUpdateFightScore();
    }

    public void sendAllMindVo() {
        Map<Integer, MindVo> mindVoMap = MindManager.getMindVoMap();
        ClientMindVo clientMindVo = new ClientMindVo();
        clientMindVo.setMindVoMap(mindVoMap);
        send(clientMindVo);
    }

    public void sendAllMindInfo() {
        Map<Integer, Byte> stateMap = new HashMap<Integer, Byte>();
        for (RoleMind roleMind : roleMindMap.values()) {
            int mindId = roleMind.getMindId();
            Byte state = getMindState(mindId);
            stateMap.put(mindId, state);
        }

        ClientMindInfo clientMindInfo = new ClientMindInfo();
        clientMindInfo.setRoleMindMap(roleMindMap);
        clientMindInfo.setStateMap(stateMap);
        send(clientMindInfo);
    }

    public void sendUpradeMind(int mindId) {
        RoleMind roleMind = getRoleMind(mindId);
        if (roleMind == null) {
            roleMind = newRoleMind(mindId);
            context().insert(roleMind);
            roleMindMap.put(roleMind.getMindId(), roleMind);
        }

        byte state = getMindState(mindId);

//		Map<Integer, RoleMind> map = new HashMap<Integer, RoleMind>();
//		map.put(mindId, roleMind);
//		
//		Map<Integer, Byte> stateMap = new HashMap<Integer, Byte>();
//		stateMap.put(mindId, state);
//				
//		ClientMindInfo clientMindInfo = new ClientMindInfo();
//        clientMindInfo.setRoleMindMap(map);
//        clientMindInfo.setStateMap(stateMap);
//        send(clientMindInfo);

        int mindLevel = roleMind.getMindLevel();
        ClientUpgradeMind clientUpgradeMind = new ClientUpgradeMind();
        clientUpgradeMind.setMindId(mindId);
        clientUpgradeMind.setMindLevel(mindLevel);
        clientUpgradeMind.setState(state);
        send(clientUpgradeMind);
    }
}

