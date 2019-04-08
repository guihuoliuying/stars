package com.stars.modules.trump;

import com.stars.core.attr.Attr;
import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.fashioncard.FashionCardModule;
import com.stars.modules.mind.MindModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.serverLog.ServerLogModule;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.prodata.SkillVo;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.trump.event.TrumpAchieveEvent;
import com.stars.modules.trump.packet.ClientTrump;
import com.stars.modules.trump.packet.ClientTrumpKarmaPacket;
import com.stars.modules.trump.prodata.TrumpKarmaVo;
import com.stars.modules.trump.prodata.TrumpLevelVo;
import com.stars.modules.trump.prodata.TrumpVo;
import com.stars.modules.trump.summary.TrumpSumaryComponentImp;
import com.stars.modules.trump.userdata.RoleTrumpKarma;
import com.stars.modules.trump.userdata.RoleTrumpRow;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.I18n;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * Created by zhouyaohui on 2016/9/18.
 */
public class TrumpModule extends AbstractModule {

    private Map<Integer, RoleTrumpRow> roleTrumpMap = new HashMap<>();
    private Set<Integer> canLevelUpList;
    private Set<Integer> unLockList;
    private List<RoleTrumpKarma> roleTrumpKarmas = null;
    private Map<Integer, RoleTrumpKarma> roleTrumpKarmaMap = new HashMap<>();

    public TrumpModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    public Map<Integer, RoleTrumpRow> getRoleTrumpMap() {
        return roleTrumpMap;
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_TRUMP, getAllTrumpScore());
        roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
        unLockList = new HashSet<>();
        canLevelUp();
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_PUTON);
//        getSkillId();
        initTrumpKarma();
    }

    /**
     * 初始化法宝仙缘
     */
    private void initTrumpKarma() {
        for (TrumpKarmaVo trumpKarmaVo : TrumpManager.trumpKarmaMap.values()) {
            RoleTrumpKarma roleTrumpKarma = roleTrumpKarmaMap.get(trumpKarmaVo.getId());
            if (roleTrumpKarma == null) {
                roleTrumpKarma = new RoleTrumpKarma(id(), trumpKarmaVo.getId());
                roleTrumpKarmaMap.put(trumpKarmaVo.getId(), roleTrumpKarma);
                context().insert(roleTrumpKarma);
            }
        }
        checkTrumpKarma();

    }

    /**
     * 检测法宝仙缘激活状态
     */
    private void checkTrumpKarma() {
        for (TrumpKarmaVo trumpKarmaVo : TrumpManager.trumpKarmaMap.values()) {
            RoleTrumpKarma roleTrumpKarma = roleTrumpKarmaMap.get(trumpKarmaVo.getId());
            if (roleTrumpKarma.getStatus() == 0 && roleTrumpKarma.getTrumpKarma().canActive(roleTrumpMap)) {
                roleTrumpKarma.setStatus(2);
                context().update(roleTrumpKarma);
                roleTrumpKarmas = null;
            }
        }
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_KARMA);

    }

    @Override
    public void onSyncData() throws Throwable {
        fireTrumpAchieveEvent(); //登陆触发成就事件检测
    }

    @Override
    public void onReconnect() throws Throwable {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_TRUMP, getAllTrumpScore());
        roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
        canLevelUp();
    }

    @Override
    public void onDataReq() throws Throwable {
        String sql = "select * from roletrump where roleid = " + id();
        roleTrumpMap = DBUtil.queryMap(DBUtil.DB_USER, "trumpid", RoleTrumpRow.class, sql);
        roleTrumpKarmaMap = DBUtil.queryMap(DBUtil.DB_USER, "karmaid", RoleTrumpKarma.class, String.format("select * from roletrumpkarma where roleid=%s;", id()));
    }


    /**
     * 发送法宝用户数据
     */
    public void sendTrumpUserData() {
        ClientTrump userData = new ClientTrump();
        userData.setOpType(ClientTrump.USER_DATA);
        userData.setRoleTrumpList(roleTrumpMap.values());
        send(userData);
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put("trump", new TrumpSumaryComponentImp(getTrumpSkillDamage(), getPutOnTrump()));
        }
    }

    private Map<Integer, Byte> getPutOnTrump() {
        Map<Integer, Byte> map = new HashMap<>();
        for (RoleTrumpRow row : roleTrumpMap.values()) {
            if (row.getPosition() != 0) {
                map.put(row.getTrumpId(), row.getPosition());
            }
        }
        return map;
    }

    /**
     * 发送法宝当前等级的产品数据，没有解封的产品就发最初级的
     */
    public void sendTrumpLevelVo() {
        ClientTrump levelVo = new ClientTrump();
        levelVo.setOpType(ClientTrump.TRUMP_LEVEL_DATA);
        List<TrumpLevelVo> levelVoList = new ArrayList<>();
        for (TrumpVo trumpVo : TrumpManager.trumpMap.values()) {
            int trumpId = trumpVo.getTrumpId();
            RoleTrumpRow row = getRoleTrumpRowById(trumpId);
            if (row == null) {
                TrumpLevelVo cur = TrumpManager.getMinTrumpLevelVo(trumpId);
                levelVoList.add(cur);
                TrumpLevelVo next = TrumpManager.getTrumpLevelVo(trumpId, (short) (cur.getLevel() + 1));
                if (next != null) {
                    levelVoList.add(next);
                }
            } else {
                levelVoList.add(TrumpManager.getTrumpLevelVo(row.getTrumpId(), row.getLevel()));
                TrumpLevelVo next = TrumpManager.getTrumpLevelVo(row.getTrumpId(), (short) (row.getLevel() + 1));
                if (next != null) {
                    levelVoList.add(next);
                }
            }
        }

        Map<String, SkillvupVo> skillvupVoMap = new HashMap<>();
        Map<Integer, SkillVo> skillVoMap = new HashMap<>();
        for (TrumpLevelVo vo : levelVoList) {
            for (Map.Entry<Integer, Integer> entry : vo.getSkillMap().entrySet()) {
                SkillvupVo skillVo = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
                skillvupVoMap.put(skillVo.getSkillId() + "_" + skillVo.getLevel(), skillVo);
                skillVoMap.put(skillVo.getSkillId(), SkillManager.getSkillVo(skillVo.getSkillId()));
            }
        }

        levelVo.setLevelVoList(levelVoList);
        levelVo.setMaxLevel(TrumpManager.maxLevel);
        levelVo.setSkillvupVoMap(skillvupVoMap);
        levelVo.setSkillVoMap(skillVoMap);
        FashionCardModule cardModule = module(MConst.FashionCard);
        levelVo.setSkillCDMap(cardModule.getSkill_cd_Map());
        send(levelVo);
    }

    /**
     * 发送某一级的法宝数据
     *
     * @param trumpId
     * @param level
     */
    public void sendTrumpLevelVoSingle(int trumpId, short level) {
        ClientTrump single = new ClientTrump();
        single.setOpType(ClientTrump.SINGLE);
        TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(trumpId, level);
        if (levelVo == null) {
            send(single);
            return;
        }
        single.setHas(true);
        single.setLevelVo(levelVo);
        single.setMax(TrumpManager.maxLevel.get(trumpId) == level);
        Map<String, SkillvupVo> skillvupVoMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : levelVo.getSkillMap().entrySet()) {
            SkillvupVo s = SkillManager.getSkillvupVo(entry.getKey(), entry.getValue());
            skillvupVoMap.put(s.getSkillId() + "_" + s.getLevel(), s);
        }
        single.setSkillvupVoMap(skillvupVoMap);
        FashionCardModule cardModule = module(MConst.FashionCard);
        single.setSkillCDMap(cardModule.getSkill_cd_Map());
        send(single);
    }

    /**
     * 根据id获取法宝数据
     *
     * @param trumpId
     * @return
     */
    public RoleTrumpRow getRoleTrumpRowById(int trumpId) {
        return roleTrumpMap.get(trumpId);
    }

    /**
     * 解封
     *
     * @param trumpId
     */
    public void unblock(int trumpId) {
        TrumpVo trumpVo = TrumpManager.getTrumpVo(trumpId);
        if (trumpVo == null) {
            warn(I18n.get("trump.NotExit"));
            return;
        }
        if (getRoleTrumpRowById(trumpId) != null) {
            warn(I18n.get("trump.alreadyUnlock"));
            return;
        }
        RoleTrumpRow row = new RoleTrumpRow();
        row.setRoleId(id());
        row.setTrumpId(trumpId);
//        row.setClick((byte) 0);
        roleTrumpMap.put(trumpId, row);
        context().insert(row);
        updateFightScoreAndSend();
        if (unLockList == null) {
            unLockList = new HashSet<>();
        }
        unLockList.add(row.getTrumpId());
        checkTrumpKarma();
        fireTrumpAchieveEvent();
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_NEW);
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_PUTON);
        signCalRedPoint(MConst.Mind, RedPointConst.MIND_ACTIVE);//心法可激活
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(Integer.valueOf(RedPointConst.TRUMP_NEW))) {
            newTrump(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.TRUMP_UP))) {
            canLevelUP(redPointMap);
        }
        if (redPointIds.contains(Integer.valueOf(RedPointConst.TRUMP_PUTON))) {
            canPutOn(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.TRUMP_KARMA)) {
            StringBuilder sb = new StringBuilder();
            int count = 0;
            for (RoleTrumpKarma roleTrumpKarma : roleTrumpKarmaMap.values()) {
                if (roleTrumpKarma.getStatus() == 2) {
                    if (count == 0) {
                        sb.append(roleTrumpKarma.getKarmaId());
                    } else {
                        sb.append("+").append(roleTrumpKarma.getKarmaId());
                    }
                    count++;
                }
            }
            if (sb.length() == 0) {
                redPointMap.put(RedPointConst.TRUMP_KARMA, null);
            } else {
                redPointMap.put(RedPointConst.TRUMP_KARMA, sb.toString());
            }
        }
    }

    private void canLevelUP(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, canLevelUpList, RedPointConst.TRUMP_UP);
    }

    private void newTrump(Map<Integer, String> redPointMap) {
        checkRedPoint(redPointMap, unLockList, RedPointConst.TRUMP_NEW);
        unLockList.clear();
    }

    private void canPutOn(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        byte position = getFreePosition();
        if (position != -1) { //有空位
            for (RoleTrumpRow row : roleTrumpMap.values()) {
                if (row.getPosition() == 0) { //有法宝未佩带
                    builder.append(row.getTrumpId()).append("+");
                }
            }
        }
        redPointMap.put(RedPointConst.TRUMP_PUTON, builder.toString().isEmpty() ? null : builder.toString());
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
     * 分解
     *
     * @param trumpId
     */
    public void resolve(int trumpId, int count) {
        RoleTrumpRow row = getRoleTrumpRowById(trumpId);
        if (row == null) {
            warn(I18n.get("trump.mustUnlock"));
            return;
        }
        TrumpVo trumpVo = TrumpManager.getTrumpVo(trumpId);
        if (trumpVo == null) {
            warn(I18n.get("trump.NotExit"));
            return;
        }

        Map<Integer, Integer> totalMap = new HashMap<>();
        Map<Integer, Integer> map = new HashMap<>();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        for (int i = 0; i < count; i++) {
            map = toolModule.addNotSend(trumpVo.getResolveToolMap(), EventType.TRUMPRESOLVE.getCode());
            com.stars.util.MapUtil.add(totalMap, map);
        }
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);
        toolModule.fireAddItemEvent(totalMap);

        ClientTrump resolve = new ClientTrump();
        resolve.setOpType(ClientTrump.RESOLVE);
        resolve.setResolveStr(trumpVo.getResolve());
        send(resolve);
    }

    /**
     * 升级：包括升级，突破,觉醒
     *
     * @param trumpId
     */
    public void levelUp(int trumpId) {
        RoleTrumpRow row = getRoleTrumpRowById(trumpId);
        if (row == null) {
            warn(I18n.get("trump.mustUnlock"));
            return;
        }
        TrumpLevelVo next = TrumpManager.getTrumpLevelVo(trumpId, (short) (row.getLevel() + 1));
        if (next == null) {
            warn(I18n.get("trump.maxLevel"));
            return;
        }
        boolean isawake = false;
        if (TrumpManager.getTrumpLevelVo(trumpId, (short) (row.getLevel() + 2)) == null) {
            isawake = true;
        }
        Map<Integer, Integer> materialMap = next.getMaterialMap();
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (!toolModule.contains(materialMap)) {
            warn(I18n.get("trump.toolNotEnough"));
            return;
        }
        toolModule.deleteAndSend(materialMap, EventType.TRUMPUP.getCode());

        if (isawake) {
            row.setAwake((byte) 1);
        }
        row.setLevel(next.getLevel());
        context().update(row);
        fireTrumpAchieveEvent();

        ClientTrump success = new ClientTrump();
        success.setOpType(ClientTrump.UPGRADE_SUCCESS);
        success.setTrumpId(row.getTrumpId());
        success.setLevel(row.getLevel());
        send(success);

        updateFightScoreAndSend();
        checkTrumpKarma();
        if (row.getPosition() != 0) {   // 已经佩戴了，就要更新技能等级
            SkillModule skillModule = module(MConst.Skill);
            RoleModule roleModule = module(MConst.Role);
            skillModule.updateTrumpSkill(next.getSkillMap());
            roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
            updateTrumpSummary();
        }

        sendTrumpLevelVoSingle(trumpId, (short) (row.getLevel() + 1));
        canLevelUp();
    }

    /*public void oldTrump(int trumpId){
        if(unLockList == null){
            unLockList = new HashSet<>();
        }
        RoleTrumpRow row = roleTrumpMap.get(trumpId);
        if(row!=null && row.getClick()==(byte)0){
            row.setClick((byte) 1);
            context().update(row);
            if(unLockList.contains(row.getTrumpId())){
                unLockList.remove(row.getTrumpId());
                signCalRedPoint(MConst.Trump,RedPointConst.TRUMP_NEW);
            }
        }
    }*/

    public void canLevelUp() {
        RoleTrumpRow row;
        TrumpLevelVo next;
        Map<Integer, Integer> materialMap;
        ToolModule toolModule = module(MConst.Tool);
        if (canLevelUpList == null) {
            canLevelUpList = new HashSet<>();
        }
        for (Map.Entry<Integer, RoleTrumpRow> entry : roleTrumpMap.entrySet()) {
            row = entry.getValue();
            if (row != null) {
                next = TrumpManager.getTrumpLevelVo(row.getTrumpId(), (short) (row.getLevel() + 1));
                if (next != null) {
                    materialMap = next.getMaterialMap();
                    if (toolModule.contains(materialMap)) {
                        canLevelUpList.add(row.getTrumpId());
                    } else {
                        if (canLevelUpList.contains(row.getTrumpId())) {
                            canLevelUpList.remove(row.getTrumpId());
                        }
                    }
                } else {
                    if (canLevelUpList.contains(row.getTrumpId())) {
                        canLevelUpList.remove(row.getTrumpId());
                    }
                }
            }
        }
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_UP);
    }

    /**
     * 佩戴
     *
     * @param trumpId
     */
    public void putOn(int trumpId) {
        RoleTrumpRow row = getRoleTrumpRowById(trumpId);
        if (row == null) {
            warn(I18n.get("trump.mustUnlock"));
            return;
        }
        if (row.getPosition() > 0) {
            warn(I18n.get("trump.alreadyPutOn"));
            return;
        }
        byte position = getFreePosition();
        if (position == -1) {
            warn(I18n.get("trump.noPosition"));
            return;
        }
        row.setPosition(position);
        context().update(row);
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_PUTON);

        ClientTrump put = new ClientTrump();
        put.setOpType(ClientTrump.PUT_ON);
        put.setTrumpId(row.getTrumpId());
        put.setPosition(row.getPosition());
        send(put);

        TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(trumpId, row.getLevel());
        SkillModule skillModule = module(MConst.Skill);
        RoleModule roleModule = module(MConst.Role);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
        skillModule.updateTrumpSkill(levelVo.getSkillMap());
        updateTrumpSummary();
    }

    /**
     * 卸下
     */
    public void takeOff(int trumpId) {
        RoleTrumpRow row = getRoleTrumpRowById(trumpId);
        if (row == null) {
            warn(I18n.get("trump.mustUnlock"));
            return;
        }
        row.setPosition((byte) 0);
        context().update(row);
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_PUTON);

        /** 将法宝技能卸下来 */
        TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(trumpId, row.getLevel());
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer skillId : levelVo.getSkillMap().keySet()) {
            map.put(skillId, 0);
        }
        SkillModule skillModule = module(MConst.Skill);
        RoleModule roleModule = module(MConst.Role);
        skillModule.updateTrumpSkill(map);
        roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
        updateTrumpSummary();

        ClientTrump off = new ClientTrump();
        off.setOpType(ClientTrump.PUT_ON);
        off.setTrumpId(row.getTrumpId());
        off.setPosition(row.getPosition());
        send(off);
    }

    /**
     * 空闲位置，从小的开始
     *
     * @return
     */
    private byte getFreePosition() {
        int pos = -1;
        String unlockStr = MapUtil.getString(DataManager.commonConfigMap, "trump_matrix_unlock", null);
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        if (unlockStr == null) return (byte) pos;
        Set<Integer> avilible = new HashSet<>();    // 已经解封的孔位
        for (String position : unlockStr.trim().split("\\|")) {
            String[] posStr = position.split("\\+");
            if (Integer.valueOf(posStr[1]) <= roleModule.getLevel()) {
                avilible.add(Integer.valueOf(posStr[0]));
            }
        }
        for (RoleTrumpRow row : roleTrumpMap.values()) {
            if (avilible.contains(Integer.valueOf(row.getPosition()))) {
                avilible.remove(Integer.valueOf(row.getPosition()));
            } else {
                /** 法宝不在合法位置，强行卸载下来 */
                row.setPosition((byte) 0);
                context().update(row);
            }
        }
        for (Integer p : avilible) {
            if (pos == -1 || pos > p) {
                pos = p;
            }
        }
        return (byte) pos;
    }

    /**
     * 法宝总战力
     *
     * @return
     */
    private int getAllTrumpScore() {
        int score = 0;
        for (RoleTrumpRow row : roleTrumpMap.values()) {
            TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(row.getTrumpId(), row.getLevel());
            score += levelVo.getFightScore();
        }
        /**
         * 激活的法宝仙缘
         */
        for (RoleTrumpKarma roleTrumpKarma : roleTrumpKarmaMap.values()) {
            if (roleTrumpKarma.getStatus() == 1) {
                TrumpKarmaVo trumpKarma = roleTrumpKarma.getTrumpKarma();
                score += FormularUtils.calFightScore(trumpKarma.getAttr());
            }
        }
        return score;
    }

    /**
     * 更新法宝战力并下发
     */
    private void updateFightScoreAndSend() {
        RoleModule roleModule = module(MConst.Role);
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_TRUMP, getAllTrumpScore());
        roleModule.sendUpdateFightScore();
        roleModule.sendRoleAttr();
    }

    /**
     * 法宝对应等级的技能伤害值
     *
     * @return
     */
    public Map<Integer, String> getTrumpSkillDamage() {
        Map<Integer, String> map = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        for (RoleTrumpRow row : roleTrumpMap.values()) {
            builder.delete(0, builder.length());
            TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(row.getTrumpId(), row.getLevel());
            builder.append(levelVo.getTriggerRate()).append("+")
                    .append(Attr.getIndexByteEn(levelVo.getAttr().split("[=]")[0]));
            for (Integer skill : levelVo.getSkillMap().keySet()) {
                map.put(skill, builder.toString());
            }
        }
        return map;
    }

    public Set<Integer> getSkillIdList() {
        Set<Integer> skillList = new HashSet<>();
        for (int skillid : getTrumpSkillDamage().keySet()) {
            skillList.add(skillid);
        }
        return skillList;
    }

    @Override
    public void onOffline() throws Throwable {
        doOfflineLog();
    }

    /***
     * 穿戴的法宝属性
     * @return
     */
    private Attribute getAllPutOnTrumpAttr() {
        Attribute attr = new Attribute();
        for (RoleTrumpRow row : roleTrumpMap.values()) {
            if (row.getPosition() != 0) {
                TrumpLevelVo levelVo = TrumpManager.getTrumpLevelVo(row.getTrumpId(), row.getLevel());
                attr.addAttribute(levelVo.getAttribute());
            }
        }
        /**
         * 激活的法宝仙缘
         */
        for (RoleTrumpKarma roleTrumpKarma : roleTrumpKarmaMap.values()) {
            if (roleTrumpKarma.getStatus() == 1) {
                TrumpKarmaVo trumpKarma = roleTrumpKarma.getTrumpKarma();
                attr.addAttribute(trumpKarma.getAttr());
            }
        }
        return attr;
    }

    private void fireTrumpAchieveEvent() {
        if (roleTrumpMap == null) return;
        TrumpAchieveEvent event = new TrumpAchieveEvent(roleTrumpMap);
        eventDispatcher().fire(event);
    }

    public void doOfflineLog() {
        StringBuilder magic_base_str = new StringBuilder();
        StringBuilder magic_matrix_id_str = new StringBuilder();
        magic_base_str.append("magic_base@lv:");
        magic_matrix_id_str.append("magic_matrix_id@magic:");
        boolean hav = false;
        for (RoleTrumpRow trumpRow : roleTrumpMap.values()) {
            magic_base_str.append(trumpRow.getTrumpId()).append("@").append(trumpRow.getLevel()).append("&");
            magic_matrix_id_str.append(trumpRow.getTrumpId()).append("@").append(trumpRow.getPosition()).append("&");
            hav = true;
        }
        if (hav) {
            magic_base_str.deleteCharAt(magic_base_str.length() - 1);
            magic_matrix_id_str.deleteCharAt(magic_matrix_id_str.length() - 1);
        }
        MindModule mind = module(MConst.Mind);
        ServerLogModule log = module(MConst.ServerLog);
        log.log_trump(magic_base_str.toString(), magic_matrix_id_str.toString(), mind.getRoleMindStr());
    }

    /**
     * 更新法宝常用数据
     */
    private void updateTrumpSummary() {
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(id(),
//                    new TrumpSumaryComponentImp(getTrumpSkillDamage(), getPutOnTrump()));
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
        context().markUpdatedSummaryComponent(MConst.Trump);
    }

    public String makeFsStr() {
        StringBuffer sb = new StringBuffer();
        RoleModule roleModule = module(MConst.Role);
        int trumpLevelFs = 0; //法宝等级战力
        int mindFs = 0; //心法战力
        int putonFs = 0; //法阵战力
        trumpLevelFs = getAllTrumpScore();
        mindFs = roleModule.getRoleRow().getFightScoreMap().get(MConst.Mind);
        putonFs = FormularUtils.calFightScore(getAllPutOnTrumpAttr());
        sb.append("magic_base:").append(trumpLevelFs).append("#magic_skill:").append(mindFs).append("#magic_matrix:").append(putonFs).append("#");
        return sb.toString();
    }

    /**
     * 请求法宝仙缘列表
     *
     * @param includeProduct
     */
    public void reqKarmaList(boolean includeProduct) {
        ClientTrumpKarmaPacket clientTrumpKarmaPacket = new ClientTrumpKarmaPacket(ClientTrumpKarmaPacket.SEND_KARMA_LIST);
        clientTrumpKarmaPacket.setRoleTrumpKarmaList(getTrumpKarmaList());
        clientTrumpKarmaPacket.setIncludeProduct(includeProduct);
        send(clientTrumpKarmaPacket);
    }

    /**
     * 请求激活法宝仙缘
     *
     * @param karmaId
     */
    public void activeKarma(int karmaId) {
        RoleTrumpKarma roleTrumpKarma = roleTrumpKarmaMap.get(karmaId);
        if (roleTrumpKarma.getStatus() == 2) {
            roleTrumpKarma.active();
            context().update(roleTrumpKarma);
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_TRUMP, getAllTrumpScore());
            roleModule.updatePartAttr(RoleManager.ROLEATTR_TRUMP, getAllPutOnTrumpAttr());
            updateFightScoreAndSend();
        }
        roleTrumpKarmas = null;
        signCalRedPoint(MConst.Trump, RedPointConst.TRUMP_KARMA);
        ClientTrumpKarmaPacket clientTrumpKarmaPacket = new ClientTrumpKarmaPacket(ClientTrumpKarmaPacket.SEND_ACTIVE_SUCCESS);
        send(clientTrumpKarmaPacket);
        reqKarmaList(false);
    }

    List<RoleTrumpKarma> getTrumpKarmaList() {
        if (roleTrumpKarmas == null) {
            roleTrumpKarmas = new ArrayList<>(roleTrumpKarmaMap.values());
            Collections.sort(roleTrumpKarmas);
        }
        return roleTrumpKarmas;
    }
}
