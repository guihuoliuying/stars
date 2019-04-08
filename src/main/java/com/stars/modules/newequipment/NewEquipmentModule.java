package com.stars.modules.newequipment;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.fashion.FashionModule;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.ForeShowModule;
import com.stars.modules.newequipment.event.*;
import com.stars.modules.newequipment.packet.ClientNewEquipment;
import com.stars.modules.newequipment.packet.vo.NextStarInfo;
import com.stars.modules.newequipment.packet.vo.NextStrengthInfo;
import com.stars.modules.newequipment.prodata.*;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponent;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponentImpl;
import com.stars.modules.newequipment.userdata.EffectPlayRecord;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.skill.SkillModule;
import com.stars.modules.skill.prodata.SkillvupVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.modules.tool.userdata.RoleTokenEquipmentHolePo;
import com.stars.modules.tool.userdata.RoleToolRow;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.SummaryComponent;
import com.stars.services.summary.SummaryConst;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.RandomUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * 装备模块;
 * Created by wuyuxing on 2016/11/10.
 */
public class NewEquipmentModule extends AbstractModule {
    // recordmap中的key
    public final static String NEWEQUIPMENT_MAX_HISTORY_FIGHTSCORE = "newequipment.max.history.fightscore"; //历史最高装备战力
    private Map<Byte, RoleEquipment> roleEquipMap = null;
    private EffectPlayRecord effectPlayRecord = null;

    private ExtraAttrVo oldExtAttrVo;   //原来旧的额外属性(临时数据,不入库,用于洗练复原)
    private byte oldType;               //原来旧装备类型(临时数据,不入库,用于洗练复原)
    private byte oldIndex;              //原来旧额外属性index(临时数据,不入库,用于洗练复原)
    private RoleEquipment washingRoleEquipment;  //当前洗练的装备
    private Map<Byte, RoleTokenEquipmentHolePo> newHolePoMap = new HashMap<>(); //符文洗练的结果缓存
    private int newWashSkillId;  //符文洗练技能的缓存
    private int newWashSkillLevel; //符文洗练技能等级的缓存
    private Map<Byte, TokenSkillVo> activeTokenSkillMap = new HashMap<>(); //已激活的符文技能 key:equpType(装备部位) values: 激活的技能
    private Map<Byte, TokenSkillVo> preActiveTokenSkillMap = new HashMap<>(); //检测新技能前的符文技能 key:equpType(装备部位) values: 激活的技能

    public NewEquipmentModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.NewEquipment, id, self, eventDispatcher, moduleMap);
    }

    public Map<Byte, RoleEquipment> getRoleEquipMap() {
        return roleEquipMap;
    }

    public void setRoleEquipMap(Map<Byte, RoleEquipment> roleEquipMap) {
        this.roleEquipMap = roleEquipMap;
    }

    public RoleEquipment getRoleEquipByType(byte type) {
        if (StringUtil.isEmpty(roleEquipMap)) return null;
        return roleEquipMap.get(type);
    }

    public int getRoleJobId() {
        RoleModule roleModule = module(MConst.Role);
        return roleModule.getRoleRow().getJobId();
    }

    public Map<Byte, TokenSkillVo> getActiveTokenSkillMap() {
        return activeTokenSkillMap;
    }

    public List<String> getDragonBallIdList() {
        List<String> dragonBallIdsList = new ArrayList<>();
        Map<Integer, SkillvupVo> tmpActiveSkillMap = new HashMap<>();
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (activeTokenSkillMap.get(roleEquipment.getType()) == null) //该部位未激活技能
                continue;
            SkillvupVo skillLvUpVo = SkillManager.getSkillvupVo(roleEquipment.getTokenSkillId(), roleEquipment.getTokenSKillLevel());
            if (skillLvUpVo == null)
                continue;
            if (skillLvUpVo.getDragonBallid() == 0)
                continue;
            SkillvupVo tmpSkillvupVo = tmpActiveSkillMap.get(skillLvUpVo.getSkillId());
            if (tmpSkillvupVo == null || tmpSkillvupVo.getLevel() < skillLvUpVo.getLevel()) { //只去相同技能中的最高技能
                tmpActiveSkillMap.put(skillLvUpVo.getSkillId(), skillLvUpVo);
            }
        }

        for (SkillvupVo skillLvUpVo : tmpActiveSkillMap.values()) {
            String key = Integer.toString(skillLvUpVo.getSkillId()) + "+" + Integer.toString(skillLvUpVo.getDragonBallid());
            if (!dragonBallIdsList.contains(key)) {
                dragonBallIdsList.add(key); //实际key 是 是skillid+dragonballid 拼起来的字符串
            }
        }

        Collections.sort(dragonBallIdsList);
        return dragonBallIdsList;
    }

    public void setActiveTokenSkillMap(Map<Byte, TokenSkillVo> activeTokenSkillMap) {
        this.activeTokenSkillMap = activeTokenSkillMap;
    }

    /**
     * 初始化装备额外属性
     */
    public void initEquipExtAttr(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return;//防空
        EquipmentVo equipVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        ItemVo itemVo = ToolManager.getItemVo(roleEquipment.getEquipId());
        if (equipVo == null || itemVo == null) return;//防空

        if (equipVo.getExtAttrNum() <= 0) return;//额外属性条目数量为0,不需初始化

        ExtEquipAttrVo extAttrVo = NewEquipmentManager.getExtEquipAttrVo(equipVo);
        if (extAttrVo == null) return;//防空

        Map<Byte, ExtraAttrVo> extraAttrMap = new HashMap<>();
        roleEquipment.setExtraAttrMap(extraAttrMap);  //额外属性map

        initExtAttrMap(extraAttrMap, equipVo, extAttrVo, itemVo);
    }

    private void initExtAttrMap(Map<Byte, ExtraAttrVo> extraAttrMap, EquipmentVo equipVo, ExtEquipAttrVo extAttrVo, ItemVo itemVo) {
        ExtraAttrVo extraAttrVo;        //额外属性基础vo
        ExtAttrWeightVo randomAttrVo;   //随机属性 & 满值
        Byte randomQuality;             //随机品质
        int coeff, value;
        for (byte i = 1, extAttrNum = equipVo.getExtAttrNum(); i <= extAttrNum; i++) {
            randomAttrVo = extAttrVo.getRandomWeightVo();
            randomQuality = i == 1 ? itemVo.getColor() : extAttrVo.getRandomQuality();
            if (randomQuality <= 1) randomQuality = 2;//额外属性最小品质默认为2
            coeff = getExtAttrValueByQuality(randomQuality);//根据品质，获得额外属性随机品质系数
            //属性随机值
            value = randomAttrVo.getValue() * coeff / 1000;//向下取整
            if (value == 0) {//防0处理
                value = 1;
            }

            extraAttrVo = new ExtraAttrVo(i, randomQuality, randomAttrVo.getAttrName(), value);
            extraAttrMap.put(i, extraAttrVo);
        }
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        //新建玩家的时候会进来这里, 使用同步的方式创建对应数据项;log
        RoleModule roleModule = module(MConst.Role);
        Role curRole = roleModule.getRoleRow();
        String roleId = Long.toString(curRole.getRoleId());
        Job curJob = RoleManager.getJobById(curRole.getJobId());

        //初始化各部位装备
        int equipId = 0;
        roleEquipMap = new HashMap<>(NewEquipmentConstant.EQUIPMENT_MAX_COUNT);
        RoleEquipment roleEquipment;
        //为了获取相关的sql语句;
        for (byte i = 1; i <= NewEquipmentConstant.EQUIPMENT_MAX_COUNT; i++) {
            equipId = RoleManager.getBornEquipmentId(curJob.getJobId(), i);//初始化新建角色默认装备
            roleEquipment = new RoleEquipment(id(), i, equipId);
            initEquipExtAttr(roleEquipment);
            updateRoleEquipmentAttr(roleEquipment);
            roleEquipMap.put(i, roleEquipment);

            //添加插入语句
            context().insert(roleEquipment);
        }

        effectPlayRecord = new EffectPlayRecord(id());
        context().insert(effectPlayRecord);//添加插入语句
    }

    @Override
    public void onDataReq() throws Exception {
        // 加载玩家装备数据
        String sql = "select * from `rolenewequip` where `roleid`=" + id();
        roleEquipMap = DBUtil.queryMap(DBUtil.DB_USER, "type", RoleEquipment.class, sql);
        if (roleEquipMap == null) {
            roleEquipMap = new HashMap<>(NewEquipmentConstant.EQUIPMENT_MAX_COUNT);
        }

        //检查并初始化装备
        for (byte i = 1; i <= NewEquipmentConstant.EQUIPMENT_MAX_COUNT; i++) {
            byte equipmentType = i;
            if (!roleEquipMap.containsKey(equipmentType)) {
//                reInitRoleEquipment(equipmentType);
            }
        }

        sql = "select * from `effectplayrecord` where `roleid` = " + id();
        effectPlayRecord = DBUtil.queryBean(DBUtil.DB_USER, EffectPlayRecord.class, sql);
        if (effectPlayRecord == null) {
            effectPlayRecord = new EffectPlayRecord(id());
            context().insert(effectPlayRecord);//添加插入语句
        }

    }

    public void addEffectPlayRecord(int itemId) {
        if (effectPlayRecord == null || effectPlayRecord.getEffectPlayList() == null) return;
        effectPlayRecord.getEffectPlayList().add(itemId);
        context().update(effectPlayRecord);
    }

    public boolean hasPlayEffect(int itemId) {
        return effectPlayRecord.getEffectPlayList().contains(itemId);
    }

    /**
     * 判断是否拥有某个部位的装备;
     */
    public boolean isHasEquipment(byte equipmentType) {
        if (StringUtil.isEmpty(roleEquipMap)) return false;
        RoleEquipment roleEquipment = roleEquipMap.get(equipmentType);
        if (roleEquipment == null || roleEquipment.getEquipId() == 0) return false;
        return true;
    }

    @Override
    public void onInit(boolean isCreation) {
        updateFightScore(NewEquipmentConstant.UPDATE_ALL, false);//更新全部装备属性战力
        checkActiveTokenSkill(true); //玩家激活符文的被动技能检测
        SkillModule skillModule = (SkillModule) moduleMap().get(MConst.Skill);
        Map<Integer, TokenSkillVo> tokenSkillVoMap = NewEquipmentManager.getTokenSkillVoMap();
        for (Integer skillId : tokenSkillVoMap.keySet()) { //符文技能在装备后会加战力
            skillModule.updateTokenPassSkillLv(skillId, null, false);
        }
        signCalEquipRedPoint();
    }

    @Override
    public void onSyncData() {
        syncAllRoleEquip();//下发角色已穿戴装备信息
        flushAllEquipMark();//刷新背包内所有装备的角标
        //登陆检测成就达成
        fireEquipChangeAchieveEvent();
        eventDispatcher().fire(new EquipStarAchieveEvent(getRoleTotalStarLevel(), getRoleStarLevelMap()));
        eventDispatcher().fire(new EquipStrengthAchieveEvent(getRoleTotalStrengthLevel(), getRoleStrengthLevelMap()));
        fireEquipExtAttrAchieveEvent();

    }

    /**
     * 同步全部角色已穿戴装备信息
     */
    public void syncAllRoleEquip() {
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC);
        FashionModule fashionModule = (FashionModule) moduleMap().get(MConst.Fashion);
        RoleFashion roleFashion = fashionModule.getRoleDressingFashion();
        if (StringUtil.isEmpty(roleFashion)) {
            roleFashion = new RoleFashion(id(), 0, (byte) 0, (byte) 0, 0L);
        }
        client.setRoleEquipMap(roleEquipMap);
        client.setRoleCurrentDressingFashion(roleFashion);
        send(client);
    }

    /**
     * 同步单件已穿戴装备信息
     */
    public void syncRoleEquip(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return;
        Map<Byte, RoleEquipment> updateMap = new HashMap<>();
        updateMap.put(roleEquipment.getType(), roleEquipment);
        syncRoleEquipMap(updateMap);
    }

    /**
     * 同步部分角色已穿戴装备信息
     */
    public void syncRoleEquipMap(Map<Byte, RoleEquipment> equipMap) {
        if (equipMap == null) return;
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC);
        client.setRoleEquipMap(equipMap);
        send(client);
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put(SummaryConst.C_NEW_EQUIPMENT, new NewEquipmentSummaryComponentImpl(getSummaryMap(), getDragonBallIdList()));
        }
    }

    private Map<Byte, RoleEquipment> getSummaryMap() {
        Map<Byte, RoleEquipment> summaryMap = new HashMap<>();
        RoleEquipment equipmentData;
        for (Map.Entry<Byte, RoleEquipment> entry : roleEquipMap.entrySet()) {
            equipmentData = new RoleEquipment();
            equipmentData.parseString(entry.getValue().makeString());
            updateRoleEquipmentAttr(equipmentData);
            summaryMap.put(entry.getKey(), equipmentData);
        }
        return summaryMap;
    }

    /**
     * 更新玩家的战力
     */
    private void updateRoleFightScore(boolean isSendToClient) {
        RoleModule roleModule = module(MConst.Role);
        Attribute totalAttr = getEquipmentAttribute();//装备总属性
        // 更新属性
        roleModule.updatePartAttr(RoleManager.ROLEATTR_EQUIPMENT, totalAttr);
        // 更新战力
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_EQUIPMENT, getRoleAllEquipmentFighting());

        updateSummary();//更新玩家装备常用数据

        if (isSendToClient) {
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore();
        }
    }

    /**
     * 更新战力
     * type 对应部位,-1为更新全部装备
     * isSendToClient 是否同步更新至客户端
     */
    private void updateFightScore(byte type, boolean isSendToClient) {
        if (type == -1) {//更新全部装备属性
            updateAllRoleEquipmentAttr();
        } else {  //更新对应部位装备属性
            updateRoleEquipmentAttr(type);
        }

        updateRoleFightScore(isSendToClient);//更新玩家战力
    }

    /**
     * 更新玩家装备常用数据
     */
    private void updateSummary() {
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(id(),
//                    new NewEquipmentSummaryComponentImpl(getSummaryMap()));
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
        context().markUpdatedSummaryComponent(MConst.NewEquipment);
    }

    /**
     * 初始化装备额外属性
     */
    public void initEquipExtAttr(RoleToolRow toolRow) {
        if (toolRow == null) return;//防空
        EquipmentVo equipVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        ItemVo itemVo = ToolManager.getItemVo(toolRow.getItemId());
        if (equipVo == null || itemVo == null) return;//防空

        if (equipVo.getExtAttrNum() <= 0) return;//额外属性条目数量为0,不需初始化

        ExtEquipAttrVo extAttrVo = NewEquipmentManager.getExtEquipAttrVo(equipVo);
        if (extAttrVo == null) return;//防空

        Map<Byte, ExtraAttrVo> extraAttrMap = new HashMap<>();
        toolRow.setExtraAttrMap(extraAttrMap);  //额外属性map

        initExtAttrMap(extraAttrMap, equipVo, extAttrVo, itemVo);
    }

    /**
     * 根据品质，获得额外属性随机品质系数
     */
    private static int getExtAttrValueByQuality(Byte quality) {
        List<Integer> list = NewEquipmentManager.getExtAttrValueByQuality(quality);
        if (StringUtil.isEmpty(list) || list.size() != 2) return 0;
        int min = list.get(0);  //最小值
        int max = list.get(1);  //最大值
        int diff = max - min + 1;   //随机范围

        Random random = new Random();
        int value = random.nextInt(diff);
        return min + value;
    }

    /**
     * 根据增幅获得品质
     */
    private static Byte getQualityByPercent(int value) {
        List<Integer> list;
        for (Map.Entry<Byte, List<Integer>> entry : NewEquipmentManager.getExtAttrQualityCoeffMap().entrySet()) {
            list = entry.getValue();
            if (list.get(0) <= value && list.get(1) >= value) {
                return entry.getKey();
            }
        }
        return 1;//默认普通品质
    }

    /**
     * 获得玩家装备系统总战力
     */
    private int getRoleAllEquipmentFighting() {
        if (roleEquipMap == null) return 0;
        int fighting = 0;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            fighting += roleEquipment.getFighting();
        }
        int maxHistroyFightScore = context().recordMap().getInt(NEWEQUIPMENT_MAX_HISTORY_FIGHTSCORE, 0);
        //记录装备系统历史最高战力
        if (maxHistroyFightScore < fighting) {
            context().recordMap().setInt(NEWEQUIPMENT_MAX_HISTORY_FIGHTSCORE, fighting);
        }
        return fighting;
    }

    /**
     * 获得全部装备总属性
     */
    private Attribute getEquipmentAttribute() {
        if (roleEquipMap == null) return null;
        Attribute attr = new Attribute();
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            attr.addAttribute(roleEquipment.getTotalAttr());
        }
        return attr;
    }

    /**
     * 刷新全部装备属性
     */
    private void updateAllRoleEquipmentAttr() {
        if (roleEquipMap == null) return;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            updateRoleEquipmentAttr(roleEquipment);
        }
    }

    /**
     * 更新对应部位装备属性
     * type 对应部位 1-6
     */
    private void updateRoleEquipmentAttr(byte type) {
        if (roleEquipMap == null) return;
        updateRoleEquipmentAttr(roleEquipMap.get(type));
    }

    /**
     * 更新单件装备属性
     */
    private void updateRoleEquipmentAttr(RoleEquipment roleEquipment) {
        if (roleEquipment == null || roleEquipment.getEquipId() == 0) return; //没有穿上装备
        EquipmentVo equipment = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if (equipment == null) return;
        //基础属性
        roleEquipment.setBaseAttr(equipment.getAttributePacked());
        roleEquipment.setEquipLevel(equipment.getEquipLevel());
        roleEquipment.setWashCost(equipment.getWashCost());
        roleEquipment.setSwitchCost(equipment.getSwitchCost());

        //强化属性
        roleEquipment.setStrengthPercent(0);
        roleEquipment.setStrengthAttrAdd(0);
        EquipStrengthVo equipStrengthVo = NewEquipmentManager.getEquipStrengthVo(roleEquipment);
        if (equipStrengthVo != null &&
                (equipStrengthVo.getAttrPencent() > 0 || equipStrengthVo.getAttrAdd() > 0)) {
            Attribute strengthAttr = new Attribute();
            strengthAttr.addAttribute(equipment.getAttributePacked(), equipStrengthVo.getAttrPencent(), 100);
            strengthAttr.addSingleAttr(equipment.getAttributePacked().getFirstNotZeroAttrIndex(), equipStrengthVo.getAttrAdd());
            roleEquipment.setStrengthAttr(strengthAttr);
            roleEquipment.setStrengthPercent(equipStrengthVo.getAttrPencent());
            roleEquipment.setStrengthAttrAdd(equipStrengthVo.getAttrAdd());
        }

        //额外属性
        if (StringUtil.isNotEmpty(roleEquipment.getExtraAttrMap())) {
            Attribute extraAttr = new Attribute();
            int minExtraAttrFighting = 0;
            for (ExtraAttrVo extraAttrVo : roleEquipment.getExtraAttrMap().values()) {
                extraAttr.addSingleAttr(extraAttrVo.getAttrName(), extraAttrVo.getAttrValue());

                if (minExtraAttrFighting == 0 || minExtraAttrFighting > extraAttrVo.getFighting()) {
                    minExtraAttrFighting = extraAttrVo.getFighting();
                }
            }
            roleEquipment.setExtraAttr(extraAttr);
            roleEquipment.setMinExtAttrFighting(minExtraAttrFighting);
            roleEquipment.setExtraAttrFighting(FormularUtils.calFightScore(extraAttr));
        }

        Attribute totalAttr = new Attribute();
        totalAttr.addAttribute(roleEquipment.getBaseAttr());      //基础属性
        totalAttr.addAttribute(roleEquipment.getStrengthAttr());  //强化属性
        totalAttr.addAttribute(roleEquipment.getExtraAttr());     //额外属性

        //升星属性加成
        roleEquipment.setStarPercent(0);
        EquipStarVo equipStarVo = NewEquipmentManager.getEquipStarVo(roleEquipment);
        if (equipStarVo != null && equipStarVo.getEnhanceAttr() > 0) {
            Attribute tmpAttr = new Attribute();
            tmpAttr.addAttribute(totalAttr, 100 + equipStarVo.getEnhanceAttr(), 100);
            totalAttr = tmpAttr;
            roleEquipment.setStarPercent(equipStarVo.getEnhanceAttr());
            roleEquipment.setStarIcon(equipStarVo.getStarShow());
        }

        //符文加战力
        int tokenEquipFightAdd = roleEquipment.getTokenEquipFight();

        //单件装备全部属性
        roleEquipment.setTotalAttr(totalAttr);
        roleEquipment.setFighting(FormularUtils.calFightScore(totalAttr) + tokenEquipFightAdd);
    }

    /**
     * 重新初始化装备部位信息
     */
    private RoleEquipment reInitRoleEquipment(byte type) {
        RoleModule roleModule = module(MConst.Role);
        Role curRole = roleModule.getRoleRow();
        String roleId = Long.toString(curRole.getRoleId());
        RoleEquipment roleEquipment = new RoleEquipment(id(), type, 0);
        roleEquipMap.put(type, roleEquipment);
        context().insert(roleEquipment);
        return roleEquipment;
    }

    public boolean canPutOn(int equipId) {
        ItemVo itemVo = ToolManager.getItemVo(equipId);
        if (itemVo == null) return false;

        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(equipId);
        if (equipmentVo == null) return false;

        if (getRoleLevel() < equipmentVo.getPutOnLevel())
            return false;

        int jobId = getRoleJobId();
        if (equipmentVo.getJob() != 0 && equipmentVo.getJob() != jobId) {
            return false;
        }

        return true;
    }

    /**
     * 穿上装备
     */
    public void putOn(long toolId) {
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        RoleToolRow toolRow = toolModule.getEquipById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备道具:,info:" + id() + "|" + toolId);
            return;
        }

        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备产品数据:,info:" + id() + "|" + toolId + "|" + toolRow.getItemId());
            return;
        }

        //若是穿戴勋章，判断勋章系统是否开放，勋章系统未开放则提示不可穿戴
        if (equipmentVo.getType() == NewEquipmentConstant.MEDAL_EQUIPMENT_TYPE) {
            ForeShowModule foreShowModule = (ForeShowModule) this.module(MConst.ForeShow);
            if (!foreShowModule.isOpen(ForeShowConst.MEDAL)) {
                warn(I18n.get("medal_weartips_cantwear"));
                return;
            }
        }

        int jobId = getRoleJobId();
        if (equipmentVo.getJob() != 0 && equipmentVo.getJob() != jobId) {
            warn(I18n.get("newequip.jobDifference"));
            return;
        }

        if (!canPutOn(toolRow.getItemId())) {
            warn(I18n.get("newequip.levelNotEnough"));
            return;
        }

        byte type = equipmentVo.getType();//装备部位
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (roleEquipment == null) {//没有部位数据,理论上不存在,因为创角时已经初始化
            roleEquipment = reInitRoleEquipment(type);//重新初始化装备部位信息
        }

        //记录旧装备数据
        int oldEquipId = roleEquipment.getEquipId();
        Map<Byte, ExtraAttrVo> oldAttrMap = null;
        Map<Byte, RoleTokenEquipmentHolePo> oldRoleTokenHoleInfo = new HashMap<>();
        if (StringUtil.isNotEmpty(roleEquipment.getRoleTokenHoleInfoMap())) {
            oldRoleTokenHoleInfo = new HashMap<>(roleEquipment.getRoleTokenHoleInfoMap());
        }
        int oldTokenSkill = roleEquipment.getTokenSkillId();
        int oldTokenSkillLevel = roleEquipment.getTokenSKillLevel();
        if (StringUtil.isNotEmpty(roleEquipment.getExtraAttrMap())) {
            oldAttrMap = new HashMap<>(roleEquipment.getExtraAttrMap());
        }

        //替换新装备数据
        roleEquipment.setEquipId(toolRow.getItemId());      //装备id
        Map<Byte, ExtraAttrVo> newAttrMap = null;           //额外属性
        if (StringUtil.isNotEmpty(toolRow.getExtraAttrMap())) {
            newAttrMap = new HashMap<>(toolRow.getExtraAttrMap());
        }
        roleEquipment.setExtraAttrMap(newAttrMap);
        //符文属性
        roleEquipment.setRoleTokenHoleInfoMap(toolRow.getRoleTokenHoleInfoMap());
        roleEquipment.setTokenSkillId(toolRow.getTokenSkillId());
        roleEquipment.setTokenSKillLevel(toolRow.getTokenSKillLevel());

        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        checkActiveTokenSkill(false); //玩家激活符文的被动技能检测
        context().update(roleEquipment);

        ItemVo itemVo = ToolManager.getItemVo(oldEquipId);
        toolModule.deleteByToolId(toolRow.getToolId(), toolRow.getCount(), EventType.PUTONEQUIP.getCode());//删除背包的装备
        if (itemVo != null) {//身上原来有装备,脱下到装备背包，因为先删除了装备，所以肯定能新增成功
            toolModule.addEquipWithExtAttr(oldEquipId, oldAttrMap, oldRoleTokenHoleInfo, oldTokenSkill, oldTokenSkillLevel);
        }

        // 刷新装备背包
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_EQUIP);

        signCalEquipRedPoint();//标识计算装备红点
        flushMarkByType(type);//刷新背包装备角标
        fireEquipChangeAchieveEvent();
        fireEquipExtAttrAchieveEvent();
    }

    private int getRoleLevel() {
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        return roleModule.getLevel();
    }

    /**
     * 单次强化装备请求
     */
    public void requestStrengthEquip(byte type) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }

        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (NewEquipmentManager.isMaxEquipStrength(roleEquipment)) {
            warn(I18n.get("newequip.maxStrengthLevel"));
            return;
        }

        //下级强化数据
        EquipStrengthVo nextStrengthVo = NewEquipmentManager.getNextEquipStrengthVo(roleEquipment);
        if (nextStrengthVo.getLevelLimit() > getRoleLevel()) {//玩家等级
            warn(I18n.get("newequip.levelNotEnough"));
            return;
        }

        //强化材料判断
        ToolModule toolModule = this.module(MConst.Tool);
        if (!toolModule.contains(nextStrengthVo.getMaterialMap())) {
            //快速获得跳转
            return;
        }

        toolModule.deleteAndSend(nextStrengthVo.getMaterialMap(), EventType.STRENGTHEQUIP.getCode());//扣除强化物品

        roleEquipment.addStrengthLevel();//提升装备强化等级
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        context().update(roleEquipment);
        syncNextStrengthInfo(roleEquipment);//同步下级强化信息
        playStrengthSuccessAmj(type);//播放强化成功特效

        signCalEquipRedPoint();//标识计算装备红点
        fireStrengthChangeEvent();
    }

    /**
     * 是否可进行强化操作
     */
    private boolean canOperateStrength(RoleEquipment roleEquipment) {
        if (!isHasEquipment(roleEquipment.getType())) return false;
        //最大强化等级判断
        if (NewEquipmentManager.isMaxEquipStrength(roleEquipment)) return false;

        //角色等级限制
        EquipStrengthVo nextStrengthVo = NewEquipmentManager.getNextEquipStrengthVo(roleEquipment);
        if (nextStrengthVo.getLevelLimit() > getRoleLevel()) return false;

        //强化材料判断
        ToolModule toolModule = this.module(MConst.Tool);
        if (!toolModule.contains(nextStrengthVo.getMaterialMap())) return false;

        return true;
    }

    public Byte canStrengthEquip() {
        byte minEquip = 1;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (canOperateStrength(roleEquipment)) {
                minEquip = minEquip <= roleEquipment.getType() ? minEquip : roleEquipment.getType();
            }
        }
        return minEquip;
    }


    /**
     * 获得下一个强化的装备
     * 规则:强化等级最小 & 未被标识为不可操作
     */
    private RoleEquipment getNextStrengthEquip() {
        RoleEquipment target = null;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (!roleEquipment.isCanOperate()) continue;//已经被标识为不可操作
            if (target == null) { //默认选一个
                target = roleEquipment;
            } else if (target.getStrengthLevel() > roleEquipment.getStrengthLevel()) {//替换强化等级小的装备
                target = roleEquipment;
            }
        }
        return target;
    }

    /**
     * 重置可操作标识符
     * 全部重置为可操作状态
     */
    private void resetOperateSymbol() {
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            roleEquipment.setCanOperate(true);
        }
    }

    /**
     * 执行一次强化操作
     * 1.必须和canOperateStrength配合使用
     * 2.使用canOperateStrength进行条件判断
     * 3.operateStrengthOneTimes不再进行强化条件检测
     */
    private void operateStrengthOneTimes(RoleEquipment roleEquipment) {
        EquipStrengthVo nextStrengthVo = NewEquipmentManager.getNextEquipStrengthVo(roleEquipment);
        ToolModule toolModule = this.module(MConst.Tool);
        toolModule.deleteAndSend(nextStrengthVo.getMaterialMap(), EventType.STRENGTHEQUIP.getCode());//扣除强化物品

        roleEquipment.addStrengthLevel();//提升装备强化等级
    }

    /**
     * 请求一键自动强化装备
     */
    public void requestAutoStrengthEquip() {
        List<Byte> hasOperateList = new ArrayList<>();//强化过的装备,有序,用于播放一键强化动画
        Map<Byte, RoleEquipment> updateMap = new HashMap<>();

        resetOperateSymbol();//重置可操作标识符
        //获得下一个强化的装备
        RoleEquipment roleEquipment = getNextStrengthEquip();
        while (roleEquipment != null) {
            if (canOperateStrength(roleEquipment)) {//强化条件检测
                operateStrengthOneTimes(roleEquipment);//进行一次强化操作

                if (!hasOperateList.contains(roleEquipment.getType())) {//加入已操作队列
                    hasOperateList.add(roleEquipment.getType());
                }

                updateMap.put(roleEquipment.getType(), roleEquipment);//加入更新队列
                context().update(roleEquipment);
            } else {
                roleEquipment.setCanOperate(false);//标识为不可操作
            }

            roleEquipment = getNextStrengthEquip();//获得下一个强化的装备
        }
        if (StringUtil.isNotEmpty(updateMap)) {
            for (byte type : hasOperateList) {//更新对应部位装备属性
                updateRoleEquipmentAttr(type);
            }
            updateRoleFightScore(true);//更新玩家战力
            syncRoleEquipMap(updateMap);//同步装备信息
            syncNextStrengthInfo(hasOperateList);//同步下级强化信息

            playStrengthSuccessAmj(hasOperateList);//播放强化成功特效
        }

        signCalEquipRedPoint();//标识计算装备红点
        fireStrengthChangeEvent();
    }

    //播放强化成功特效
    private void playStrengthSuccessAmj(List<Byte> strengthList) {
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_PLAY_STRENGTH_SUCCESS_AMJ);
        client.setStrenghtList(strengthList);
        send(client);
    }

    //播放强化成功特效
    private void playStrengthSuccessAmj(byte type) {
        List<Byte> list = new ArrayList<>();
        list.add(type);
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_PLAY_STRENGTH_SUCCESS_AMJ);
        client.setStrenghtList(list);
        send(client);
    }

    /**
     * 下发装备操作相关信息
     */
    public void sendEquipOperateInfo() {
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC_OPERATE_INFO);
        client.setSyncType(ClientNewEquipment.SYNC_TYPE_ALL);
        client.setNextStrengthList(getAllStrengthInfo());
        client.setNextStarInfoList(getAllStarInfo());
        send(client);
    }

    /**
     * 获得各部位下一级升星信息
     */
    private List<NextStarInfo> getAllStarInfo() {
        List<NextStarInfo> list = new ArrayList<>();
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            NextStarInfo nextStarInfo = getNextStarInfo(roleEquipment);
            if (nextStarInfo != null) {
                list.add(nextStarInfo);
            }
        }
        return list;
    }

    /**
     * 获得下级升星信息展示对象
     */
    private NextStarInfo getNextStarInfo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return null;
        EquipStarVo equipStarVo = NewEquipmentManager.getNextEquipStarVo(roleEquipment);
        if (equipStarVo == null) {
            return new NextStarInfo(roleEquipment.getType(), NextStarInfo.MAX);
        }

        return getNextStarInfo(equipStarVo);
    }

    /**
     * 获得封装好的升星展示对象,未满级
     */
    private NextStarInfo getNextStarInfo(EquipStarVo equipStarVo) {
        NextStarInfo nextStarInfo = new NextStarInfo(equipStarVo.getType());
        nextStarInfo.setLevel(equipStarVo.getLevel());
        nextStarInfo.setLevelLimit(equipStarVo.getLevelLimit());
        nextStarInfo.setAttrAdd(equipStarVo.getEnhanceAttr());
        nextStarInfo.setDisplaySuccess(equipStarVo.getDisplaySuccess());
        nextStarInfo.setMaterialMap(equipStarVo.getMaterialMap());
        nextStarInfo.setSafeItemMap(equipStarVo.getLuckyItemMap());
        return nextStarInfo;
    }

    /**
     * 获得各部位下一级强化信息
     */
    private List<NextStrengthInfo> getAllStrengthInfo() {
        List<NextStrengthInfo> list = new ArrayList<>();
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            NextStrengthInfo nextStrengthInfo = getNextStrengthInfo(roleEquipment);
            if (nextStrengthInfo != null) {
                list.add(nextStrengthInfo);
            }
        }
        return list;
    }

    /**
     * 同步下级强化信息
     */
    private void syncNextStrengthInfo(List<Byte> typeList) {
        List<NextStrengthInfo> list = new ArrayList<>();
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC_OPERATE_INFO);
        client.setSyncType(ClientNewEquipment.SYNC_TYPE_STRENGTH);
        for (byte type : typeList) {
            NextStrengthInfo nextStrengthInfo = getNextStrengthInfo(roleEquipMap.get(type));
            if (nextStrengthInfo != null) {
                list.add(nextStrengthInfo);
            }
        }
        client.setNextStrengthList(list);
        send(client);
    }

    /**
     * 同步下级强化信息
     */
    private void syncNextStrengthInfo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return;
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC_OPERATE_INFO);
        client.setSyncType(ClientNewEquipment.SYNC_TYPE_STRENGTH);
        List<NextStrengthInfo> list = new ArrayList<>();
        list.add(getNextStrengthInfo(roleEquipment));
        client.setNextStrengthList(list);
        send(client);
    }

    /**
     * 同步下级升星信息
     */
    private void syncNextStarInfo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return;
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_SYNC_OPERATE_INFO);
        client.setSyncType(ClientNewEquipment.SYNC_TYPE_STAR);
        List<NextStarInfo> list = new ArrayList<>();
        list.add(getNextStarInfo(roleEquipment));
        client.setNextStarInfoList(list);
        send(client);
    }

    /**
     * 获得下级强化信息展示对象
     */
    private NextStrengthInfo getNextStrengthInfo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return null;
        EquipStrengthVo equipStrengthVo = NewEquipmentManager.getNextEquipStrengthVo(roleEquipment);
        if (equipStrengthVo == null) {
            return new NextStrengthInfo(roleEquipment.getType(), NextStrengthInfo.MAX);//强化满级
        }
        return getNextStrengthInfo(equipStrengthVo);
    }

    /**
     * 获得封装好的展示对象,未满级
     */
    private NextStrengthInfo getNextStrengthInfo(EquipStrengthVo equipStrengthVo) {
        NextStrengthInfo nextStrengthInfo = new NextStrengthInfo(equipStrengthVo.getType());
        nextStrengthInfo.setLevel(equipStrengthVo.getLevel());
        nextStrengthInfo.setAttrPercent(equipStrengthVo.getAttrPencent());
        nextStrengthInfo.setAttrAdd(equipStrengthVo.getAttrAdd());
        nextStrengthInfo.setLevelLimit(equipStrengthVo.getLevelLimit());
        nextStrengthInfo.setMaterialMap(equipStrengthVo.getMaterialMap());
        return nextStrengthInfo;
    }

    /**
     * 是否可进行升星操作
     */
    public boolean canOperateUpStar(RoleEquipment roleEquipment) {
        if (!isHasEquipment(roleEquipment.getType())) return false;
        if (NewEquipmentManager.isMaxEquipStar(roleEquipment)) return false;

        EquipStarVo nextStarVo = NewEquipmentManager.getNextEquipStarVo(roleEquipment);
        if (nextStarVo.getLevelLimit() > getRoleLevel()) return false;

        ToolModule toolModule = this.module(MConst.Tool);
        if (!toolModule.contains(nextStarVo.getMaterialMap())) return false;

        return true;
    }

    /**
     * 请求升星操作
     */
    public void requestUpStar(byte type, byte useLuckyItem) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }

        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (NewEquipmentManager.isMaxEquipStar(roleEquipment)) {
            warn(I18n.get("newequip.maxStrengthLevel"));
            return;
        }

        EquipStarVo nextStarVo = NewEquipmentManager.getNextEquipStarVo(roleEquipment);
        if (nextStarVo.getLevelLimit() > getRoleLevel()) {
            warn(I18n.get("newequip.levelNotEnough"));
            return;
        }

        ToolModule toolModule = this.module(MConst.Tool);
        //升星材料
        if (!toolModule.contains(nextStarVo.getMaterialMap())) {
            //快速获得跳转
            return;
        }
        boolean useLucky = useLuckyItem == 1;

        //幸运材料
        if (useLucky && !toolModule.contains(nextStarVo.getLuckyItemMap())) {
            //快速获得跳转
            return;
        }

        toolModule.deleteAndSend(nextStarVo.getMaterialMap(), EventType.UPSTAREQUIP.getCode());
        if (useLucky) {//扣除幸运材料
            toolModule.deleteAndSend(nextStarVo.getLuckyItemMap(), EventType.UPSTAREQUIP.getCode());
        }

        boolean success = false;
        if (useLucky) {//使用幸运材料
            success = true;
        } else {
            success = isSuccess(nextStarVo);//升星成功率判断
        }

        byte resultType;//升星结果
        if (success) {    //成功
            resultType = ClientNewEquipment.SUCCESS;
            roleEquipment.addStarLevel();
        } else {          //失败
            resultType = ClientNewEquipment.SAVE;
        }
//        if(success){        //成功
//            resultType = ClientNewEquipment.SUCCESS;
//            roleEquipment.addStarLevel();
//        }else if(useLucky){  //失败 & 保底
//            resultType = ClientNewEquipment.SAVE;
//        }else {             //失败降级
//            resultType = ClientNewEquipment.FAIL;
//            roleEquipment.reduceStarLevel();
//        }

        if (resultType == ClientNewEquipment.SUCCESS || resultType == ClientNewEquipment.FAIL) {
            updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
            syncNextStarInfo(roleEquipment);//同步下级升星信息
        }
        context().update(roleEquipment);

        EquipStarVo resultVo = NewEquipmentManager.getEquipStarVo(roleEquipment);

        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_UP_STAR_RESULT);
        client.setResultType(resultType);
        client.setEquipId(roleEquipment.getEquipId());
        client.setEquipStarVo(resultVo);
        send(client);

        signCalEquipRedPoint();//标识计算装备红点
        fireStarChangeEvent();
    }

    //升星成功率判断
    private boolean isSuccess(EquipStarVo nextStarVo) {
        Random random = new Random();
        int value = random.nextInt(100);
        return value < nextStarVo.getRealSuccess();
    }

    /**
     * 请求转移装备
     */
    public void requestTransferEquip(byte type, long toolId) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        ToolModule toolModule = this.module(MConst.Tool);
        RoleToolRow toolRow = toolModule.getEquipById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备道具:,info:" + id() + "|" + toolId);
            return;
        }
        EquipmentVo curEquipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if (curEquipmentVo == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到当前装备产品数据:,info:" + id() + "|" + roleEquipment.getEquipId());
            return;
        }

        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备产品数据:,info:" + id() + "|" + toolId + "|" + toolRow.getItemId());
            return;
        }
        if (!canPutOn(toolRow.getItemId())) {
            return;
        }
        if (equipmentVo.getType() != roleEquipment.getType()) return;//部位不对应

        int basicFighting = FormularUtils.calFightScore(roleEquipment.getBaseAttr());
        int toolBasicFighting = FormularUtils.calFightScore(equipmentVo.getAttributePacked());
        if (toolBasicFighting <= basicFighting) return;//基础战力不满足条件

        int extraFighting = getExtraAttrMapTotalFighting(roleEquipment.getExtraAttrMap().values());
        int toolExtraFighting = getExtraAttrMapTotalFighting(toolRow.getExtraAttrMap().values());
        if (extraFighting < toolExtraFighting) return;//额外属性战力不满足条件

        if (!toolModule.deleteAndSend(curEquipmentVo.getSwitchMap(), EventType.TRANSFEREQUIP.getCode())) {//扣除转移消耗材料
            warn(I18n.get("newequip.transferToolNotEnough"));
            return;
        }

        toolModule.deleteByToolId(toolId, 1, EventType.TRANSFEREQUIP.getCode());//删除对应装备
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_EQUIP);//刷新装备背包

        List<ExtraAttrVo> list = new ArrayList<>();
        list.addAll(roleEquipment.getExtraAttrMap().values());
        list.addAll(toolRow.getExtraAttrMap().values());
        Collections.sort(list);//整合额外属性并排序

        Map<Byte, ExtraAttrVo> attrMap = new HashMap<>();
        Byte maxNum = NewEquipmentManager.getMaxExtraAttrNum();
        byte curIndex = 1;
        for (ExtraAttrVo vo : list) {
            if (curIndex > maxNum) break;
            vo.setIndex(curIndex);  //重置index
            resetExtraAttrQuality(vo, equipmentVo);//重置quality
            attrMap.put(curIndex, vo);
            curIndex++;
        }
        //如果是符文装备替换前检查符文的返还
        if (NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) {
            if (StringUtil.isNotEmpty(roleEquipment.getRoleTokenHoleInfoMap())) {
                Map<Integer, Integer> backToolMap = new HashMap<>();
                for (RoleTokenEquipmentHolePo holePo : roleEquipment.getRoleTokenHoleInfoMap().values()) {
                    String key = holePo.getTokenId() + "_" + holePo.getTokenLevel();
                    TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(key);
                    if (tokenLevelVo == null) continue;
                    com.stars.util.MapUtil.add(backToolMap, tokenLevelVo.getTransferBackMap());
                }
                if (StringUtil.isNotEmpty(backToolMap)) {
                    toolModule.addAndSend(backToolMap, EventType.TRANSFEREQUIP.getCode());
                    ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_TRANSFER_BACK);
                    client.setTransferBackToolMap(backToolMap);
                    send(client);
                }
            }
        }
        //替换装备
        roleEquipment.setEquipId(toolRow.getItemId());
        roleEquipment.setExtraAttrMap(attrMap);
        roleEquipment.setRoleTokenHoleInfoMap(toolRow.getRoleTokenHoleInfoMap());
        roleEquipment.setTokenSkillId(toolRow.getTokenSkillId());
        roleEquipment.setTokenSKillLevel(toolRow.getTokenSKillLevel());
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        context().update(roleEquipment);

        signCalEquipRedPoint();//标识计算装备红点
        flushMarkByType(type);//刷新背包装备角标
        fireEquipChangeAchieveEvent();
        fireEquipExtAttrAchieveEvent();
        checkActiveTokenSkill(false);
    }

    /**
     * 获得额外属性集合的总战力
     */
    private int getExtraAttrMapTotalFighting(Collection<ExtraAttrVo> attrSet) {
        int fighting = 0;
        if (StringUtil.isEmpty(attrSet)) return fighting;
        for (ExtraAttrVo vo : attrSet) {
            fighting += vo.getFighting();
        }
        return fighting;
    }

    /**
     * 请求洗练装备
     */
    public void requestWashEquip(byte type, long toolId, byte extraAttrIndex) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        ToolModule toolModule = this.module(MConst.Tool);
        RoleToolRow toolRow = toolModule.getEquipById(toolId);
        if (toolRow == null) return;//没有对应装备物品
        if (toolRow.getIsEquip() == 0) return;//不是装备物品
        if (StringUtil.isNotEmpty(toolRow.getRoleTokenHoleInfoMap()) || toolRow.getTokenSkillId() != 0)
            return; //有符文或符文技能不能洗练
        EquipmentVo toolEquipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        //装备类型不一致,无法洗练
        if (toolEquipmentVo == null || toolEquipmentVo.getType() != roleEquipment.getType()) return;

        Byte maxNum = NewEquipmentManager.getMaxExtraAttrNum();
        if (extraAttrIndex > maxNum) return;//index大于最大额外属性条目数量

        int curEmptyIndex = roleEquipment.getExtraAttrMap().size() + 1;
        if (curEmptyIndex != extraAttrIndex && curEmptyIndex <= maxNum) {
            ExtraAttrVo curEmptyAttr = roleEquipment.getExtarAttrByIndex((byte) curEmptyIndex);
            if (curEmptyAttr == null) {
                extraAttrIndex = (byte) curEmptyIndex;//确保新增的额外属性index为最小的空index
            }
        }

        ExtraAttrVo curExtarAttr = roleEquipment.getExtarAttrByIndex(extraAttrIndex);
        if (curExtarAttr == null) {//允许为空,为空时是新增属性

        }

        if (roleEquipment.getExtraAttrMap().size() >= maxNum && curExtarAttr != null && curExtarAttr.getFighting() > toolRow.getMaxExtraAttrFighting()) {
            warn(I18n.get("newequip.noNeedToWash"));
            return;
        }

        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if (equipmentVo == null) return;//防空
        if (!toolModule.deleteAndSend(equipmentVo.getWashMap(), EventType.WASHEQUIP.getCode())) {
            warn(I18n.get("newequip.washToolNotEnough"));
            return;
        }

        toolModule.deleteByToolId(toolId, 1, EventType.WASHEQUIP.getCode()); //删除洗练装备
//        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_EQUIP);//刷新装备背包

        //背包装备中随机出来的额外属性
        ExtraAttrVo randomVo = toolRow.getRandomExtraAttr();
        byte randomIndex = randomVo.getIndex();
        resetExtraAttrQuality(randomVo, equipmentVo);//重置额外属性品质

        byte flag;//洗练结果标识: 0新增属性 1洗练到低级属性 2洗练到高级属性
        randomVo.setIndex(extraAttrIndex);//修改额外属性index
        resetOldWashData();//重置洗练缓存的旧数据

        oldType = roleEquipment.getType();
        Map<Integer, Integer> resolveMap = null;
        if (curExtarAttr == null) {
            flag = 0;
        } else if (randomVo.getFighting() <= curExtarAttr.getFighting()) {
//            oldExtAttrVo = curExtarAttr;//新属性战力低于原属性,记录为缓存,用于复原
//            oldIndex = extraAttrIndex;
            flag = 1;
            resolveMap = forceResolveToolAfterWash(toolRow.getItemId());
        } else {
            flag = 2;
        }

        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_WASH_RESULT);
        client.setResultType(flag);//洗练结果标识: 0新增属性 1洗练到低级属性 2洗练到高级属性
        client.setRandomIndex(randomIndex);

        if (flag != 1) {
            roleEquipment.getExtraAttrMap().put(extraAttrIndex, randomVo);//替换额外属性
//        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
            context().update(roleEquipment);
            client.setOldExtraAttr(curExtarAttr);
            client.setNewExtraAttr(randomVo);
        } else {
            client.setResolveMap(resolveMap);
        }

        send(client);
        fireWarhChangeEvent();
//        signCalEquipRedPoint();//标识计算装备红点
//        flushMarkByType(type);//刷新背包装备角标
//        fireEquipExtAttrChangeEvent();
    }

    public Map<Integer, Integer> forceResolveToolAfterWash(int itemId) {
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        if (itemVo == null) {
            com.stars.util.LogUtil.info("不存在itemId={}的道具产品数据", itemId);
            return null;
        }
        Map<Integer, Integer> map = null;
        ToolModule toolModule = module(MConst.Tool);

        // 先分解再增加物品
        try {
            map = toolModule.addNotSend(itemVo.getResolveMap(), EventType.RESOLVETOOL.getCode());
        } catch (Throwable cause) {
            com.stars.util.LogUtil.error("洗练装备后道具分解异常, roleId=" + id() + ", itemId=" + itemId + ", count=" + 1, cause);
        }
        return map;
    }

    /**
     * 重置洗练缓存的旧数据
     */
    private void resetOldWashData() {
        oldExtAttrVo = null;
        oldIndex = 0;
        oldType = 0;
    }

    /**
     * 重置额外属性品质
     */
    private void resetExtraAttrQuality(ExtraAttrVo attrVo, EquipmentVo equipmentVo) {
        ExtEquipAttrVo extAttrVo = NewEquipmentManager.getExtEquipAttrVo(equipmentVo);
        if (extAttrVo == null) return;//防空
        ExtAttrWeightVo weightVo = extAttrVo.getExtAttrWeightVoByAttrName(attrVo.getAttrName());
        if (weightVo == null) return;
        int percent = attrVo.getAttrValue() * 1000 / weightVo.getValue();//获得增幅千分比
        byte newQuality = getQualityByPercent(percent);
        if (newQuality <= 1) newQuality = 2;
        attrVo.setQuality(newQuality);//修改额外属性品质
    }

    /**
     * 复原装备洗练的额外属性
     */
    public void recoverWashEquip(byte flag) {
        RoleEquipment roleEquipment = roleEquipMap.get(oldType);
        if (roleEquipment == null) return;
        ToolModule toolModule = this.module(MConst.Tool);
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_ALL);//刷新背包
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        resetOldWashData();//重置洗练缓存的旧数据
        context().update(roleEquipment);
        signCalEquipRedPoint();//标识计算装备红点
        flushMarkByType(roleEquipment.getType());//刷新背包装备角标
        fireEquipExtAttrAchieveEvent();
    }

    /**
     * 穿上此装备后的总战力
     */
    private int getVirtualFighting(RoleToolRow toolRow, RoleEquipment roleEquipment) {
        if (roleEquipment == null) return 0; //没有穿上装备
        EquipmentVo equipment = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipment == null) return 0;

        //基础属性
        Attribute baseAttr = new Attribute(equipment.getAttributePacked());
        Attribute strengthAttr = new Attribute();
        Attribute extraAttr = new Attribute();
        EquipStrengthVo equipStrengthVo = NewEquipmentManager.getEquipStrengthVo(roleEquipment);
        if (equipStrengthVo != null &&
                (equipStrengthVo.getAttrPencent() > 0 || equipStrengthVo.getAttrAdd() > 0)) {
            strengthAttr.addAttribute(equipment.getAttributePacked(), equipStrengthVo.getAttrPencent(), 100);
            strengthAttr.addSingleAttr(equipment.getAttributePacked().getFirstNotZeroAttrIndex(), equipStrengthVo.getAttrAdd());
        }

        //额外属性
        if (StringUtil.isNotEmpty(toolRow.getExtraAttrMap())) {
            for (ExtraAttrVo extraAttrVo : toolRow.getExtraAttrMap().values()) {
                extraAttr.addSingleAttr(extraAttrVo.getAttrName(), extraAttrVo.getAttrValue());
            }
        }

        Attribute totalAttr = new Attribute();
        totalAttr.addAttribute(baseAttr);      //基础属性
        totalAttr.addAttribute(strengthAttr);  //强化属性
        totalAttr.addAttribute(extraAttr);     //额外属性

        //升星属性加成
        EquipStarVo equipStarVo = NewEquipmentManager.getEquipStarVo(roleEquipment);
        if (equipStarVo != null && equipStarVo.getEnhanceAttr() > 0) {
            Attribute tmpAttr = new Attribute();
            tmpAttr.addAttribute(totalAttr, 100 + equipStarVo.getEnhanceAttr(), 100);
            totalAttr = tmpAttr;
        }
        //符文加战力
        int tokenEquipFightAdd = roleEquipment.getTokenEquipFight();
        return FormularUtils.calFightScore(totalAttr) + tokenEquipFightAdd;
    }

    /**
     * 预览穿戴后增加的战力(身上装备与此装备的战力差,包含强化&升星)
     */
    private int previewPutOnUpFighting(RoleToolRow toolRow) {
        if (toolRow == null) return 0;
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo == null) return 0;
        RoleEquipment roleEquipment = getRoleEquipByType(equipmentVo.getType());
        if (roleEquipment == null) return 0;

        //穿上此装备的总战力 - 身上装备的总战力
        int virtualFighting = getVirtualFighting(toolRow, roleEquipment);
        return virtualFighting - roleEquipment.getFighting();
    }

    /**
     * 发送穿戴新装备tips
     */
    public void sendPutOnNewEquip(List<EquipmentVo> list) {
        ToolModule toolModule = module(MConst.Tool);
        long toolId;
        List<Long> toolIdList = new ArrayList<>();
        Map<Long, Integer> map = new HashMap<>(6);
        for (EquipmentVo equipmentVo : list) {
            toolId = toolModule.getRoleEquipToolByItemId(equipmentVo.getEquipId());
            if (toolId != 0) {
                toolIdList.add(toolId);
//                map.put(toolId, previewPutOnUpFighting(equipmentVo));
            }
        }

        //下发穿戴tips
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_PUT_ON_NEW_EQUIP_TIPS);
        client.setDiffMap(map);
        send(client);
    }

    /**
     * 发送获得稀有装备特效
     */
    public void sendEffectPlayList(Map<Integer, Integer> effectPlayMap) {
        ToolModule toolModule = module(MConst.Tool);
        long toolId;
        List<Long> toolIdList = new ArrayList<>();
        Map<Long, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : effectPlayMap.entrySet()) {
            toolId = toolModule.getRoleEquipToolByItemId(entry.getKey());
            if (toolId != 0) {
                map.put(toolId, entry.getValue());
                addEffectPlayRecord(entry.getKey());
            }
        }

        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_EFFECT_PLAY_LIST);
        client.setEffectPlayMap(map);
        send(client);
    }

    /**
     * 更新单个装备的属性 并同步信息至客户端
     */
    private void updateAndSyncRoleEquipment(RoleEquipment roleEquipment) {
        updateFightScore(roleEquipment.getType(), true);//更新属性战力
        syncRoleEquip(roleEquipment);//同步穿戴装备信息
    }

    /**
     * 查看别人已穿戴的装备信息
     */
    public void watchOtherEquip(long otherRoleId, byte type) {
        NewEquipmentSummaryComponent component = (NewEquipmentSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(otherRoleId, SummaryConst.C_NEW_EQUIPMENT);
        if (component == null) return;

        RoleEquipment otherEquip = component.getEquipInfoByType(type);
        if (otherEquip == null) {
            warn(I18n.get("newequip.outTimeMessage"));
            return;
        }

        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_WATCH_OTHER_EQUIP);
        client.setOtherEquipment(otherEquip);
        send(client);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.EQUIP_PUT_ON)) {
            checkPutOnRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.EQUIP_STRENGTH)) {
            checkStrengthRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.EQUIP_STAR)) {
            checkStarRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.EQUIP_WASH)) {
            checkWashRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.EQUIP_TRANSFER)) {
            checkTransferRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.TOKEN_LEVELUP)) {
            checkTokenLevelupRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.TOKEN_WASH)) {
            checkTokenWashRedPoint(redPointMap);
        }
        if (redPointIds.contains(RedPointConst.EQUIPMENT_UPGRADE)) {
            checkEquipmentUpgradeRedPoint(redPointMap);
        }

    }

    /**
     * 检测装备升级红点
     *
     * @param redPointMap
     */
    private void checkEquipmentUpgradeRedPoint(Map<Integer, String> redPointMap) {
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            NewEquipmentUpgradeVo newEquipmentUpgradeVo = NewEquipmentManager.equipmentUpgradeVoMap.get(roleEquipment.getEquipId());
            if (newEquipmentUpgradeVo != null) {
                RoleModule roleModule = module(MConst.Role);
                ToolModule toolModule = module(MConst.Tool);
                int level = roleModule.getLevel();
                boolean success = toolModule.contains(newEquipmentUpgradeVo.getReqItemMap());
                if (newEquipmentUpgradeVo.getReqLevel() <= level && success) {
                    redPointMap.put(RedPointConst.EQUIPMENT_UPGRADE, "");
                    return;
                }
            }
        }
        redPointMap.put(RedPointConst.EQUIPMENT_UPGRADE, null);
    }

    public void signCalEquipRedPoint() {
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIP_PUT_ON);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIP_STRENGTH);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIP_STAR);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIP_WASH);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIP_TRANSFER);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.TOKEN_LEVELUP);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.TOKEN_WASH);
        signCalRedPoint(MConst.NewEquipment, RedPointConst.EQUIPMENT_UPGRADE);

    }

    /**
     * 强化红点检测
     */
    private void checkStrengthRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (canOperateStrength(roleEquipment)) {
                builder.append(roleEquipment.getType()).append("+");
            }
        }
        redPointMap.put(RedPointConst.EQUIP_STRENGTH,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 升星红点检测
     */
    private void checkStarRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (canOperateUpStar(roleEquipment)) {
                builder.append(roleEquipment.getType()).append("+");
            }
        }
        redPointMap.put(RedPointConst.EQUIP_STAR,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 洗练红点检测
     */
    private void checkWashRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ToolModule toolModule = this.module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
            if (equipmentVo == null) continue;
            if (!toolModule.contains(equipmentVo.getWashMap())) continue;//洗练材料不足
            if (toolModule.hasBetterExtAttrInBag(roleEquipment)) {//是否有更好的额外属性可替换
                builder.append(roleEquipment.getType()).append("+");
            }
        }
        redPointMap.put(RedPointConst.EQUIP_WASH,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 符文升级红点检测
     */
    private void checkTokenLevelupRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ToolModule toolModule = this.module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) { //遍历装备
            EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
            if (equipmentVo == null) continue;
            if (!NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) continue; //非符文装备
            if (StringUtil.isNotEmpty(roleEquipment.getRoleTokenHoleInfoMap())) {
                for (RoleTokenEquipmentHolePo holePo : roleEquipment.getRoleTokenHoleInfoMap().values()) { //遍历符文孔位
                    String key = holePo.getTokenId() + "_" + holePo.getTokenLevel();
                    TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(key);
                    if (tokenLevelVo == null) continue;
                    TokenLevelVo nextTokenLevelVo = NewEquipmentManager.getTokenNextLevelVo(tokenLevelVo);
                    if (nextTokenLevelVo == null) continue; //已经是最高级
                    if (!toolModule.contains(nextTokenLevelVo.getMaterialMap())) continue; //材料不足
                    builder.append(roleEquipment.getType()).append("+");
                    break;
                }
            }
        }
        redPointMap.put(RedPointConst.TOKEN_LEVELUP,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 符文洗练红点检测
     */
    private void checkTokenWashRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ToolModule toolModule = this.module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
            if (equipmentVo == null) continue;
            if (!NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) continue; //非符文装备
            if (!toolModule.contains(equipmentVo.getTokenWashCostMap())) continue;//转移材料不足
            builder.append(roleEquipment.getType()).append("+");
        }
        redPointMap.put(RedPointConst.TOKEN_WASH,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 转移红点检测
     */
    private void checkTransferRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ToolModule toolModule = this.module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
            if (equipmentVo == null) continue;
            if (!toolModule.contains(equipmentVo.getSwitchMap())) continue;//转移材料不足
            if (toolModule.hasCanTransferEquipInBag(roleEquipment)) {//背包内是否存在可转移的装备
                builder.append(roleEquipment.getType()).append("+");
            }
        }
        redPointMap.put(RedPointConst.EQUIP_TRANSFER,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 穿戴红点检测
     */
    private void checkPutOnRedPoint(Map<Integer, String> redPointMap) {
        StringBuilder builder = new StringBuilder("");
        ToolModule toolModule = this.module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (toolModule.hasBetterEquipInBag(roleEquipment)) {//背包内是否存在更好的装备
                if (roleEquipment.getType() == NewEquipmentConstant.MEDAL_EQUIPMENT_TYPE) {//若是勋章，则需要判断勋章系统是否开启
                    ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
                    if (foreShowModule.isOpen(ForeShowConst.MEDAL)) {
                        builder.append(roleEquipment.getType()).append("+");
                    }
                } else {
                    builder.append(roleEquipment.getType()).append("+");
                }
            }
        }
        redPointMap.put(RedPointConst.EQUIP_PUT_ON,
                builder.toString().isEmpty() ? null : builder.toString());
    }

    /**
     * 计算装备角标 供外部调用
     *
     * @param toolRow
     * @param module
     * @return
     */
    public byte calEquipMark(RoleToolRow toolRow, String module) {
        if (module.equals(MConst.Tool)) {
            return calEquipMark(toolRow);
        }
        return -1;
    }

    /**
     * 计算装备角标
     */
    private byte calEquipMark(RoleToolRow toolRow) {
        RoleEquipment roleEquipment = getRoleEquipByType(toolRow.getEquipType());
        if (roleEquipment == null) return ClientNewEquipment.MARK_TYPE_NONE;//无角标

        if (roleEquipment.getType() == NewEquipmentConstant.MEDAL_EQUIPMENT_TYPE) {//判断勋章是否开放
            ForeShowModule foreShowModule = (ForeShowModule) module(MConst.ForeShow);
            if (!foreShowModule.isOpen(ForeShowConst.MEDAL)) {
                return ClientNewEquipment.MARK_TYPE_NONE;//无角标
            }
        }

        RoleModule roleModule = module(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();
        int level = roleModule.getRoleRow().getLevel();
        Byte maxNum = NewEquipmentManager.getMaxExtraAttrNum();
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo != null && getRoleLevel() < equipmentVo.getPutOnLevel()) {
            return ClientNewEquipment.MARK_TYPE_HIGHQUALITY;//高品质
        }

        if (equipmentVo != null && (equipmentVo.getJob() == 0 || equipmentVo.getJob() == jobId)
                && roleEquipment.getBasicFighting() < toolRow.getBasicFighting()) {
            if (roleEquipment.getExtraAttrFighting() >= toolRow.getExtraAttrFighting()) {
                return ClientNewEquipment.MARK_TYPE_TRANSFER;//可转移
            }
            return ClientNewEquipment.MARK_TYPE_PUT_ON;//可穿戴
        }

        if (toolRow.isTokenEquip() &&
                (StringUtil.isNotEmpty(toolRow.getRoleTokenHoleInfoMap()) || toolRow.getTokenSkillId() != 0)) //符文装备而且有符文或符文技能的不能洗练)
            return ClientNewEquipment.MARK_TYPE_TOKEN; //符文装备

        if (roleEquipment.getExtraAttrMap() != null
                && equipmentVo != null && getRoleLevel() >= equipmentVo.getPutOnLevel() &&
                (roleEquipment.getExtraAttrMap().size() < maxNum ||
                        roleEquipment.getMinExtAttrFighting() < toolRow.getMaxExtraAttrFighting())) {
            return ClientNewEquipment.MARK_TYPE_WASH;//可洗练
        }

        return ClientNewEquipment.MARK_TYPE_NONE;//无角标
    }

    /**
     * 初始化新增装备的角标并同步至客户端
     */
    public void flushNeedToMarkToClient() {
        ToolModule toolModule = module(MConst.Tool);
        HashSet<RoleToolRow> needToMarkSet = toolModule.getNeedToMarkSet();
        if (StringUtil.isEmpty(needToMarkSet)) return;

        Map<Long, Byte> markMap = new HashMap<>();
        Map<Long, Integer> fightMap = new HashMap<>();
        byte mark;
        for (RoleToolRow toolRow : needToMarkSet) {
            mark = calEquipMark(toolRow);//计算装备角标
            if (mark != ClientNewEquipment.MARK_TYPE_NONE) {
                markMap.put(toolRow.getToolId(), mark);
            }
            if (mark == ClientNewEquipment.MARK_TYPE_PUT_ON || mark == ClientNewEquipment.MARK_TYPE_TRANSFER) {
                fightMap.put(toolRow.getToolId(), previewPutOnUpFighting(toolRow));
            }
        }
        flushMarkToClient(ClientNewEquipment.FLUSH_TYPE_ADD, markMap, fightMap);
        toolModule.clearNeedToMark();
    }

    /**
     * flushType 为0全部刷新、2新增,时可调用此接口
     * flushType 1按部位刷新，必须添加部位参数,不能调用此接口
     */
    private void flushMarkToClient(byte flushType, Map<Long, Byte> markMap, Map<Long, Integer> fightMap) {
        flushMarkToClient(flushType, markMap, fightMap, (byte) 0);
    }

    /**
     * 同步装备角标至客户端
     *
     * @param flushType 刷新类型： 0全部刷新 1按部位刷新 2新增
     * @param markMap   key:toolId valus:markValue
     * @param type      部位类型
     *                  角标刷新规则: 0 将全部装备置为无状态，将下发列表内的状态注入对应toolId的装备中
     *                  1 将对应部位的装备全部置为无状态，将下发列表内的状态注入对应toolId的装备中
     *                  2 直接将下发列表内的状态注入对应toolId的装备中
     *                  ps：所有装备的默认初始状态均为无角标状态
     */
    private void flushMarkToClient(byte flushType, Map<Long, Byte> markMap, Map<Long, Integer> fightMap, byte type) {
        //按部位刷新角标,必须传入部位参数
        if (flushType == ClientNewEquipment.FLUSH_TYPE_PART && type == 0) return;

        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_FLUSH_EQUIP_BAG_MARK);
        client.setFlushType(flushType);
        client.setMarkMap(markMap);
        client.setFightMap(fightMap);
        client.setType(type);
        if (flushType != ClientNewEquipment.FLUSH_TYPE_PART) {    //不按部位刷新,即检测快速穿戴界面
            client.setCheckQuickPutOn((byte) 1);
        }
        send(client);
    }

    /**
     * 刷新背包内所有对应部位的装备角标
     */
    public void flushMarkByType(byte type) {
        ToolModule toolModule = this.module(MConst.Tool);
        Map<Long, RoleToolRow> equipToolMap = toolModule.getEquipToolMap();
        Map<Long, Byte> markMap = new HashMap<>();
        Map<Long, Integer> fightMap = new HashMap<>();
        byte mark;
        for (RoleToolRow toolRow : equipToolMap.values()) {
            if (toolRow.getEquipType() != type) continue;
            mark = calEquipMark(toolRow);//计算装备角标
            if (mark != ClientNewEquipment.MARK_TYPE_NONE) {
                markMap.put(toolRow.getToolId(), mark);
            }
            if (mark == ClientNewEquipment.MARK_TYPE_PUT_ON || mark == ClientNewEquipment.MARK_TYPE_TRANSFER) {
                fightMap.put(toolRow.getToolId(), previewPutOnUpFighting(toolRow));
            }
        }
        flushMarkToClient(ClientNewEquipment.FLUSH_TYPE_PART, markMap, fightMap, type);
    }

    /**
     * 刷新背包内所有装备的角标
     */
    public void flushAllEquipMark() {
        ToolModule toolModule = this.module(MConst.Tool);
        Map<Long, RoleToolRow> equipToolMap = toolModule.getEquipToolMap();
        Map<Long, Byte> markMap = new HashMap<>();
        Map<Long, Integer> fightMap = new HashMap<>();
        byte mark;
        for (RoleToolRow toolRow : equipToolMap.values()) {
            mark = calEquipMark(toolRow);//计算装备角标
            if (mark != ClientNewEquipment.MARK_TYPE_NONE) {
                markMap.put(toolRow.getToolId(), mark);
            }
            if (mark == ClientNewEquipment.MARK_TYPE_PUT_ON || mark == ClientNewEquipment.MARK_TYPE_TRANSFER) {
                fightMap.put(toolRow.getToolId(), previewPutOnUpFighting(toolRow));
            }
        }
        flushMarkToClient(ClientNewEquipment.FLUSH_TYPE_ALL, markMap, fightMap);
    }

    private void fireWarhChangeEvent() {
        eventDispatcher().fire(new EquipWashChangeEvent());
    }

    private void fireStrengthChangeEvent() {
        eventDispatcher().fire(new EquipStrengthChangeEvent(getRoleTotalStrengthLevel(), getRoleStrengthLevelMap()));
        eventDispatcher().fire(new EquipStrengthAchieveEvent(getRoleTotalStrengthLevel(), getRoleStrengthLevelMap()));
    }

    private void fireStarChangeEvent() {
        eventDispatcher().fire(new EquipStarChangeEvent(getRoleTotalStarLevel(), getRoleStarLevelMap()));
        eventDispatcher().fire(new EquipStarAchieveEvent(getRoleTotalStarLevel(), getRoleStarLevelMap()));
    }

    private void fireEquipChangeAchieveEvent() {
        eventDispatcher().fire(new EquipChangeAchieveEvent(getEquipLevelMap(), getEquipQualityMap()));
    }

    private void fireEquipExtAttrAchieveEvent() {
        eventDispatcher().fire(new EquipExtAttrAchieveEvent(getRoleExtAttrMap()));
    }

    public int getRoleTotalStrengthLevel() {
        if (roleEquipMap == null) return 0;
        int level = 0;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            level += roleEquipment.getStrengthLevel();
        }
        return level;
    }

    public int getRoleTotalStarLevel() {
        if (roleEquipMap == null) return 0;
        int level = 0;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            level += roleEquipment.getStarLevel();
        }
        return level;
    }

    public Map<Byte, Integer> getRoleStarLevelMap() {
        Map<Byte, Integer> map = new HashMap<>(6);
        if (roleEquipMap == null) return map;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            map.put(roleEquipment.getType(), roleEquipment.getStarLevel());
        }
        return map;
    }

    public Map<Byte, Integer> getRoleStrengthLevelMap() {
        Map<Byte, Integer> map = new HashMap<>(6);
        if (roleEquipMap == null) return map;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            map.put(roleEquipment.getType(), roleEquipment.getStrengthLevel());
        }
        return map;
    }

    private Map<Byte, Map<Byte, ExtraAttrVo>> getRoleExtAttrMap() {
        Map<Byte, Map<Byte, ExtraAttrVo>> map = new HashMap<>(6);
        if (roleEquipMap == null) return map;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (roleEquipment.getExtraAttrMap() != null) {
                map.put(roleEquipment.getType(), roleEquipment.getExtraAttrMap());
            }
        }
        return map;
    }

    public Map<Byte, Integer> getEquipLevelMap() {
        Map<Byte, Integer> map = new HashMap<>(6);
        if (roleEquipMap == null) return map;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (roleEquipment == null) continue;
            map.put(roleEquipment.getType(), roleEquipment.getEquipLevel());
        }
        return map;
    }

    public Map<Byte, Byte> getEquipQualityMap() {
        Map<Byte, Byte> map = new HashMap<>(6);
        if (roleEquipMap == null) return map;
        ItemVo itemVo;
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            if (roleEquipment == null) continue;
            itemVo = ToolManager.getItemVo(roleEquipment.getEquipId());
            if (itemVo == null) continue;
            map.put(roleEquipment.getType(), itemVo.getColor());
        }
        return map;
    }

    public void onChangeJob(int newJobId) {
        NewEquipmentModule newEquipmentModule = module(MConst.NewEquipment);
        Map<Byte, RoleEquipment> roleEquipMap = newEquipmentModule.getRoleEquipMap();
        for (Map.Entry<Byte, RoleEquipment> entry : roleEquipMap.entrySet()) {
            RoleEquipment roleEquipment = entry.getValue();
            int equipId = roleEquipment.getEquipId();
            EquipmentVo newEquipment = NewEquipmentManager.getNewJobEquipmentVo(newJobId, equipId);
            if (newEquipment != null) {
                roleEquipment.setEquipId(newEquipment.getEquipId());
                newEquipmentModule.context().update(roleEquipment);
            }
        }
        updateSummary();
    }

    public String makeFsStr() {
        int baseFs = 0; // 基础
        int strengthFs = 0; // 强化
        int extraFs = 0; // 附加（洗练
        int starFs = 0; // 升星
        int tokenFs = 0; //符文

        Attribute baseAttr = new Attribute();
        Attribute strengthAttr = new Attribute();
        Attribute extraAttr = new Attribute();
        Attribute totalAttr = new Attribute();
        for (RoleEquipment po : roleEquipMap.values()) {
            baseAttr.addAttribute(po.getBaseAttr());
            strengthAttr.addAttribute(po.getStrengthAttr());
            extraAttr.addAttribute(po.getExtraAttr());
            totalAttr.addAttribute(po.getTotalAttr());
            tokenFs += po.getTokenEquipFight();
        }

        baseFs = FormularUtils.calFightScore(baseAttr);
        strengthFs = FormularUtils.calFightScore(strengthAttr);
        extraFs = FormularUtils.calFightScore(extraAttr);
        starFs = FormularUtils.calFightScore(totalAttr) - baseFs - strengthFs - extraFs;

        StringBuilder sb = new StringBuilder();
        sb.append("equipment_base:").append(baseFs).append("#") // 基础
                .append("equipment_strength:").append(strengthFs).append("#") // 强化
                .append("equipment_extra:").append(extraFs).append("#") // 附加（洗练
                .append("equipment_star:").append(starFs).append("#") // 升星
                .append("equipment_rune:").append(tokenFs).append("#"); //符文
        return sb.toString();
    }


    /***************************符文装备相关**********************************/
    /**
     * 请求符文装备升级
     *
     * @param type
     * @param holeIdList
     */
    public void reqTokenLevelUp(byte type, List<Byte> holeIdList) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (!NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) {
            warn(I18n.get("newequip.token.notTokenEquipment"));
            return;
        }
        if (StringUtil.isEmpty(roleEquipment.getRoleTokenHoleInfoMap())) {
            warn(I18n.get("newequip.token.hadNotToken"));
            return;
        }

        if (checkContainMaxTokenLevel(roleEquipment, holeIdList)) {
            warn(I18n.get("newequip.token.exitsMaxLevelToken"));
            return;
        }
        if (!checkToolEnoughForTokenLevelUp(roleEquipment, holeIdList)) {
            warn(I18n.get("newequip.token.toolNotEnough"));
            return;
        }

        ToolModule toolModule = this.module(MConst.Tool);
        for (Byte holeId : holeIdList) {
            RoleTokenEquipmentHolePo roleTokenEquipmentHolePo = roleEquipment.getRoleTokenHoleInfoMap().get(holeId);
            int nextLevel = roleTokenEquipmentHolePo.getTokenLevel() + 1;
            String key = roleTokenEquipmentHolePo.getTokenId() + "_" + nextLevel;
            TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(key);
            //扣除材料
            toolModule.deleteAndSend(tokenLevelVo.getMaterialMap(), EventType.TOKEN_LEVEL_UP_COST.getCode());
            //更新等级
            roleTokenEquipmentHolePo.setTokenLevel(nextLevel);
            roleEquipment.getRoleTokenHoleInfoMap().put(holeId, roleTokenEquipmentHolePo);
            context().update(roleEquipment);
            //发送飘字
            ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_LEVEL_UP_TIPS);
            String tokenName = NewEquipmentManager.getTokenVoById(roleTokenEquipmentHolePo.getTokenId()).getTokenName();
            client.setTokenName(tokenName);
            client.setTokenNewLevel(nextLevel);
            send(client);
        }
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        flushMarkByType(type);//刷新背包装备角标
        checkActiveTokenSkill(false);
        signCalEquipRedPoint();//刷新红点

    }

    private boolean checkContainMaxTokenLevel(RoleEquipment roleEquipment, List<Byte> holeIdList) {
        for (Byte holeId : holeIdList) {
            RoleTokenEquipmentHolePo roleTokenEquipmentHolePo = roleEquipment.getRoleTokenHoleInfoMap().get(holeId);
            if (roleTokenEquipmentHolePo == null) {
                com.stars.util.LogUtil.info("玩家该孔位没有符文：roleid:{}|postion:{}", id(), holeId);
                continue;
            }
            String tokenId_Level = roleTokenEquipmentHolePo.getTokenId() + "_" + roleTokenEquipmentHolePo.getTokenLevel();
            TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(tokenId_Level);
            if (tokenLevelVo == null) {
                com.stars.util.LogUtil.info("符文等级产品与玩家数据不匹配：roleid:{}|equipmentType:{}|position:{}", id(), roleEquipment.getType(), holeId);
                return true;
            }
            if (NewEquipmentManager.isTokenMaxLevel(tokenLevelVo)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkToolEnoughForTokenLevelUp(RoleEquipment roleEquipment, List<Byte> holeIdList) {

        Map<Integer, Integer> needToolMap = new HashMap<>();
        for (Byte holeId : holeIdList) {
            RoleTokenEquipmentHolePo roleTokenEquipmentHolePo = roleEquipment.getRoleTokenHoleInfoMap().get(holeId);
            if (roleTokenEquipmentHolePo == null) {
                com.stars.util.LogUtil.info("玩家该孔位没有符文：roleid:{}|postion:{}", id(), holeId);
                continue;
            }
            int nextLevel = roleTokenEquipmentHolePo.getTokenLevel() + 1;
            String tokenId_NextLevel = roleTokenEquipmentHolePo.getTokenId() + "_" + nextLevel;
            TokenLevelVo tokenNextLevelVo = NewEquipmentManager.getTokenLevelVo(tokenId_NextLevel);
            if (tokenNextLevelVo == null) {
                com.stars.util.LogUtil.info("符文等级产品与玩家数据不匹配：roleid:{}|equipmentType:{}|position:{}|nextlevel:{}", id(), roleEquipment.getType(), holeId, nextLevel);
                return false;
            }
            com.stars.util.MapUtil.add(needToolMap, tokenNextLevelVo.getMaterialMap());
        }
        ToolModule toolModule = this.module(MConst.Tool);
        return toolModule.contains(needToolMap);
    }

//    private Map<Integer,Integer> calAddToolMapUtil(Map<Integer,Integer> map,Map<Integer,Integer> addMap){
//        Iterator iter = addMap.entrySet().iterator();
//        while(iter.hasNext()){
//            Map.Entry<Integer,Integer> entry = (Map.Entry<Integer,Integer>)iter.next();
//            if(map.containsKey(entry.getKey())){ //如果已经存在，则累加
//                int newCount = map.get(entry.getKey())+entry.getValue();
//                map.put(entry.getKey(),newCount);
//            }else{ //不存在，则直接加入
//                map.put(entry.getKey(),entry.getValue());
//            }
//        }
//        return map;
//    }

    /**
     * 洗练符文装备
     *
     * @param type             装备部位
     * @param washHoldIdsList  //洗练符文孔位
     * @param washSkillOrToken //洗练符文技能还是符文 0--洗练符文技能 1--洗练符文
     */
    public void reqTokenEquipWash(byte type, List<Byte> washHoldIdsList, byte washSkillOrToken) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (!NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) { //检测符文装备
            warn(I18n.get("newequip.token.notTokenEquipment"));
            return;
        }
//        if (isLockSkill == (byte)1 && washHoldIdsList.size() == 0){ //检测全部锁定
//            warn(I18n.get("newequip.token.allLocked"));
//            return;
//        }
        //获得需要材料的检测
        ToolModule toolModule = this.module(MConst.Tool);
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if (!toolModule.contains(equipmentVo.getTokenWashCostMap())) { //检测洗练材料
            warn(I18n.get("newequip.token.toolNotEnough"));
            return;
        }

//        //锁材料的检测  (改版去除锁的概念)
//        int totalHoleCount = NewEquipmentManager.getTokenMaxNumIndex(equipmentVo.getTokenNumIndex());
//        int needLockCount = totalHoleCount - washHoldIdsList.size() + isLockSkill;
//        Map<Integer,Integer> lockItemMap = new HashMap<>();
//        lockItemMap.putAll(NewEquipmentManager.getTokenLockItemMap());
//        MapUtil.multiply(lockItemMap,needLockCount);
//        if (needLockCount != 0 && !toolModule.contains(lockItemMap)){
//            warn(I18n.get("newequip.token.toolNotEnough"));
//            return;
//        }

        byte washType = NewEquipmentManager.TOKEN_FIRST_TIME_WASH;  //默认初次洗练
        if (roleEquipment.getRoleTokenHoleInfoMap().size() != 0)
            washType = NewEquipmentManager.TOKEN_NON_FIRST_TIME_WASH; //有符文则说明洗练过

        ItemVo itemVo = ToolManager.getItemVo(roleEquipment.getEquipId());
        long washId = getRandomWashId(equipmentVo.getJob(), equipmentVo.getEquipLevel(), itemVo.getColor(), equipmentVo.getType(), washType);
        if (washId == -1) {
            com.stars.util.LogUtil.info("玩家找不到匹配的洗练数据|roleid:{}|equipType:{}|equipId:{}", id(), equipmentVo.getType(), equipmentVo.getEquipId());
            return;
        }

        TokenWashVo tokenWashVo = NewEquipmentManager.getTokenWashVoById(washId);
        TokenRandomRangeVo tokenRandomRangeVo = NewEquipmentManager.getTokenRandomRangeVoById(tokenWashVo.getRandomRangeId());
        if (tokenRandomRangeVo == null)
            return;

        //先消耗道具
        Map<Integer, Integer> costToolMap = new HashMap<>();
        costToolMap.putAll(equipmentVo.getTokenWashCostMap());
        //MapUtil.add(costToolMap,lockItemMap); //消耗材料加锁材料
        toolModule.deleteAndSend(costToolMap, EventType.TOKEN_WASH_COST.getCode());
        //得到新的洗练数据

        resetWashTokenEquipResult();
        washingRoleEquipment = roleEquipment;
        if (washSkillOrToken == NewEquipmentManager.TOKEN_WASH_TOKEN) { //洗练符文
            for (Byte holeId : washHoldIdsList) { //洗符文孔
                TokenWashHoleRandomDataVo tokenWashHoleRandomDataVo = tokenRandomRangeVo.getTokenWashHoleRandomDataVoMap().get(holeId);
                int newLevel = tokenWashHoleRandomDataVo.getRandomLevel();
                int newTokenId = tokenWashHoleRandomDataVo.getTokenId();
                RoleTokenEquipmentHolePo holePo = new RoleTokenEquipmentHolePo(holeId, newTokenId, newLevel);
                newHolePoMap.put(holePo.getHoleId(), holePo);
            }
        } else if (washType == NewEquipmentManager.TOKEN_FIRST_TIME_WASH) { //初次洗练只能洗练技能,洗技能同时也会洗初始化符文
            int totalHoleCount = NewEquipmentManager.getTokenMaxNumIndex(equipmentVo.getTokenNumIndex());
            for (byte holeId = 1; holeId <= totalHoleCount; holeId++) {
                TokenWashHoleRandomDataVo tokenWashHoleRandomDataVo = tokenRandomRangeVo.getTokenWashHoleRandomDataVoMap().get(holeId);
                int newLevel = tokenWashHoleRandomDataVo.getRandomLevel();
                int newTokenId = tokenWashHoleRandomDataVo.getTokenId();
                RoleTokenEquipmentHolePo holePo = new RoleTokenEquipmentHolePo(holeId, newTokenId, newLevel);
                newHolePoMap.put(holePo.getHoleId(), holePo);
            }
        }


        if (washSkillOrToken == NewEquipmentManager.TOKEN_WASH_TOKEN_SKILL) { //洗符文技能
            newWashSkillId = tokenRandomRangeVo.getTokenSkillId();
            newWashSkillLevel = tokenRandomRangeVo.getRandomSkillLevel();
        } else {
            newWashSkillId = washingRoleEquipment.getTokenSkillId();
            newWashSkillLevel = washingRoleEquipment.getTokenSKillLevel();
        }

        signCalEquipRedPoint();//刷新红点
        //发送飘字
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_WASH_RESULT);
        client.setRoleEquipment(roleEquipment);
        client.setNewHolePoMap(newHolePoMap);
        client.setNewWashSkillId(newWashSkillId);
        client.setNewWashSkillLevel(newWashSkillLevel);
        send(client);
    }

    /**
     * 确认替换洗练结果
     */
    public void reqEnsureReplaceWashResult() {
        if (StringUtil.isEmpty(newHolePoMap) && newWashSkillId == 0) {
            ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_WASH_REPLACE);
            client.setResultType(ClientNewEquipment.FAIL);
            send(client);
            return;
        }

        for (RoleTokenEquipmentHolePo newHolePo : newHolePoMap.values()) {
            RoleTokenEquipmentHolePo holePo = washingRoleEquipment.getRoleTokenHoleInfoMap().get(newHolePo.getHoleId());
            if (holePo == null) {
                holePo = new RoleTokenEquipmentHolePo(newHolePo.getHoleId(), newHolePo.getTokenId(), newHolePo.getTokenLevel());          //未有过
            } else {
                holePo.setTokenLevel(newHolePo.getTokenLevel());
                holePo.setTokenId(newHolePo.getTokenId());
            }
            washingRoleEquipment.getRoleTokenHoleInfoMap().put(newHolePo.getHoleId(), holePo);
            com.stars.util.LogUtil.info("确认替换符文|roleid:{}|equipType:{}|holeId:{}|newTokenId:{}|newTokenLevel:{}",
                    id(), newHolePo.getHoleId(), washingRoleEquipment.getType(), newHolePo.getTokenId(), newHolePo.getTokenLevel());
        }
        if (newWashSkillId != 0) {
            washingRoleEquipment.setTokenSKillLevel(newWashSkillLevel);
            washingRoleEquipment.setTokenSkillId(newWashSkillId);
            com.stars.util.LogUtil.info("确认替换符文技能|roleId:{}|equipType:{}|newSkillId:{}|newSkillLevel:{}",
                    id(), washingRoleEquipment.getType(), newWashSkillId, newWashSkillLevel);
        }
        roleEquipMap.put(washingRoleEquipment.getType(), washingRoleEquipment);
        context().update(washingRoleEquipment);
        updateAndSyncRoleEquipment(washingRoleEquipment);//更新单个装备的属性 并同步信息至客户端
        flushMarkByType(washingRoleEquipment.getType());//刷新背包装备角标
        signCalEquipRedPoint();//刷新红点
        checkActiveTokenSkill(false);
        resetWashTokenEquipResult();

        //替换成功发送到客户端
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_WASH_REPLACE);
        client.setResultType(ClientNewEquipment.SUCCESS);
        send(client);

    }

    /**
     * 初始化新获得装备道具（策划需求调整，不需要了，暂时保留）
     *
     * @param newTool
     */
    public void initNewAddEquipmentTool(RoleToolRow newTool) {
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(newTool.getItemId());
        if (!NewEquipmentManager.isTokenEquipment(equipmentVo.getEquipId())) //非符文装备不参与初始化洗练
            return;
        ItemVo itemVo = ToolManager.getItemVo(newTool.getItemId());
        byte washType = NewEquipmentManager.TOKEN_FIRST_TIME_WASH;
        long washId = getRandomWashId(equipmentVo.getJob(), equipmentVo.getEquipLevel(), itemVo.getColor(), equipmentVo.getType(), washType);
        if (washId == -1) {
            com.stars.util.LogUtil.info("玩家找不到匹配的洗练数据|roleid:{}|equipType:{}|equipId:{}", id(), equipmentVo.getType(), equipmentVo.getEquipId());
            return;
        }
        TokenWashVo tokenWashVo = NewEquipmentManager.getTokenWashVoById(washId);
        TokenRandomRangeVo tokenRandomRangeVo = NewEquipmentManager.getTokenRandomRangeVoById(tokenWashVo.getRandomRangeId());
        if (tokenRandomRangeVo == null)
            return;
        //初始化第一个随机符文孔
        TokenWashHoleRandomDataVo tokenWashHoleRandomDataVo = tokenRandomRangeVo.getTokenWashHoleRandomDataVoMap().get((byte) 1);
        int randomTokenId = tokenWashHoleRandomDataVo.getTokenId();
        int randomTokenLevel = tokenWashHoleRandomDataVo.getRandomLevel();
        RoleTokenEquipmentHolePo roleTokenEquipmentHolePo = new RoleTokenEquipmentHolePo((byte) 1, randomTokenId, randomTokenLevel);
        newTool.getRoleTokenHoleInfoMap().put((byte) 1, roleTokenEquipmentHolePo);
        //初始化第一个符文孔
        newTool.setTokenSkillId(tokenRandomRangeVo.getTokenSkillId());
        newTool.setTokenSKillLevel(tokenRandomRangeVo.getRandomSkillLevel());
    }

    /**
     * 根据条件选出匹配到洗练数据，再随机一条
     *
     * @param job
     * @param equipLevel
     * @param equipQuality
     * @param equipType
     * @param washType
     * @return
     */
    public long getRandomWashId(byte job, short equipLevel, byte equipQuality, byte equipType, byte washType) {
        Map<Long, TokenWashVo> tokenWashVoMap = NewEquipmentManager.getTokenWashVoMap();
        List<TokenWashVo> tokenWashVosList = new ArrayList<>();
        int totalRandomOdd = 0;
        Iterator iter = tokenWashVoMap.values().iterator();
        while (iter.hasNext()) {
            TokenWashVo tokenWashVo = (TokenWashVo) iter.next();
            //筛选掉非匹配项
            if (!tokenWashVo.matchWashType(washType))
                continue;
            if (!tokenWashVo.matchJobId(job))
                continue;
            if (!tokenWashVo.matchEquipLevel(equipLevel))
                continue;
            if (!tokenWashVo.matchEquipType(equipType))
                continue;
            if (!tokenWashVo.matchEquipQuality(equipQuality))
                continue;
            totalRandomOdd += tokenWashVo.getWeight(); //算出总的权值
            tokenWashVosList.add(tokenWashVo); //所有匹配项
        }

        //随机出参数，算出随机到的洗练方案
        int randomOdd = RandomUtil.rand(0, totalRandomOdd);
        for (TokenWashVo tokenWashVo : tokenWashVosList) {
            if (tokenWashVo.getWeight() >= randomOdd) //匹配到
                return tokenWashVo.getTokenWashId();
            else
                randomOdd = randomOdd - tokenWashVo.getWeight();
        }
        return -1;
    }

    private void resetWashTokenEquipResult() {
        washingRoleEquipment = null;
        newHolePoMap.clear();
        newWashSkillId = 0;
        newWashSkillLevel = 0;
    }

    /**
     * 请求熔炼符文装备
     *
     * @param toolId
     */
    public void reqMeltTokenEquip(long toolId) {
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        RoleToolRow toolRow = toolModule.getEquipById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("熔炼符文装备未找到道具|roleid:{}|toolId:{}", id(), toolId);
            return;
        }
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo == null) {
            com.stars.util.LogUtil.info("未能找到该熔炼的符文装备|roleid:{}|itemId:{}", id(), toolRow.getItemId());
            return;
        }
        if (!NewEquipmentManager.isTokenEquipment(equipmentVo.getEquipId())) {
            warn(I18n.get("newequip.token.notTokenEquipment"));
            return;
        }

        Map<Integer, Integer> returnToolMap = new HashMap<>();
        Map<Byte, RoleTokenEquipmentHolePo> holePoMap = toolRow.getRoleTokenHoleInfoMap();
        for (RoleTokenEquipmentHolePo holePo : holePoMap.values()) { //加入符文等级溶解返还道具
            String tokenId_level = holePo.getTokenId() + "_" + holePo.getTokenLevel();
            TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(tokenId_level);
            com.stars.util.MapUtil.add(returnToolMap, tokenLevelVo.getResolveMap());
        }
        if (toolRow.getTokenSkillId() != 0) { //符文技能溶解产物
            TokenSkillVo tokenSkillVo = NewEquipmentManager.getTokenSkillVoBySkillId(toolRow.getTokenSkillId());
            com.stars.util.MapUtil.add(returnToolMap, tokenSkillVo.getResolveMap());
        }
        ItemVo itemVo = ToolManager.getItemVo(toolRow.getItemId()); //装备道具分解产物
        com.stars.util.MapUtil.add(returnToolMap, itemVo.getResolveMap());

        toolModule.deleteByToolId(toolRow.getToolId(), toolRow.getCount(), EventType.TOKEN_MELT.getCode());//删除背包的装备
        toolModule.addAndSend(returnToolMap, EventType.TOKEN_MELT.getCode()); //获得溶解所得

        // 分解结果展示界面
        ClientNewEquipment clientResolve = new ClientNewEquipment(ClientNewEquipment.RESP_RESOLVE_EQUIP_RESULT);
        clientResolve.setResolveMap(returnToolMap);
        send(clientResolve);

        //熔炼成功发送到客户端
        ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_MELT_RESULT);
        client.setResultType(ClientNewEquipment.SUCCESS);
        send(client);
    }

    //检查激活的符文装备技能
    public void checkActiveTokenSkill(boolean isLogin) {
        preActiveTokenSkillMap.clear();
        preActiveTokenSkillMap.putAll(activeTokenSkillMap);
        activeTokenSkillMap.clear();
        if (StringUtil.isEmpty(roleEquipMap)) {
            return;
        }
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            int tokenSkillId = roleEquipment.getTokenSkillId();
            if (tokenSkillId == 0)
                continue;
            TokenSkillVo skillVo = NewEquipmentManager.getTokenSkillVoBySkillId(tokenSkillId);
            if (skillVo != null && skillVo.isActive(getRoleTotalTokenLevelByTokenId(skillVo.getActiveTokenId()))) {
                activeTokenSkillMap.put(roleEquipment.getType(), skillVo);
            }
        }
        checkAndSendNewSkillTip(isLogin);
        fireTokenLevelChangeEvent();
        updateTokenSkillEffect(); //同步符文技能特效Id
    }

    //某个符文的总等级
    private int getRoleTotalTokenLevelByTokenId(int tokenId) {
        int totalLevel = 0;
        Iterator iter = roleEquipMap.values().iterator();
        while (iter.hasNext()) { //遍历装备
            RoleEquipment roleEquipment = (RoleEquipment) iter.next();
            if (roleEquipment.getRoleTokenHoleInfoMap() == null)
                continue;
            for (RoleTokenEquipmentHolePo tokenHolePo : roleEquipment.getRoleTokenHoleInfoMap().values()) {
                //遍历装备上的符文孔
                if (tokenHolePo.getTokenId() != tokenId)
                    continue;
                totalLevel += tokenHolePo.getTokenLevel();
            }
        }
        return totalLevel;
    }

    private void fireTokenLevelChangeEvent() {
        eventDispatcher().fire(new TokenLevelChangeEvent());
    }

    public void updateTokenSkillEffect() {
        // 下发包更新客户端角色名字
        ClientRole res = new ClientRole(ClientRole.UPDATE_TOKEN_SKILL_EFFECT, null);
        res.setDragonBallIdList(getDragonBallIdList());
        send(res);
        eventDispatcher().fire(new DragonBallChangeEvent(getDragonBallIdList()));
    }

    public void checkAndSendNewSkillTip(boolean isLogin) {
        //发送新激活激活技能到客户端
        if (isLogin) //登陆不需要下发
            return;
        List<Byte> equipTypeList = new ArrayList<>();
        List<Integer> newSkillIds = new ArrayList<>();
        for (Map.Entry<Byte, TokenSkillVo> entry : activeTokenSkillMap.entrySet()) {
            TokenSkillVo tokenSkillVo = entry.getValue();
            if ((!preActiveTokenSkillMap.containsValue(tokenSkillVo))
                    && (!newSkillIds.contains(tokenSkillVo.getTokenSkillId()))) { //是新激活的技能
                equipTypeList.add(entry.getKey());
                newSkillIds.add(tokenSkillVo.getTokenSkillId());
            }
        }
        if (StringUtil.isNotEmpty(equipTypeList)) {
            ClientNewEquipment clientNewEquipment = new ClientNewEquipment(ClientNewEquipment.RESP_ACTIVE_TOKEN_SKILL);
            clientNewEquipment.setEquipTypeList(equipTypeList);
            send(clientNewEquipment);
        }
    }

    public void reqInheritTokenEquip(long toolId) {
        ToolModule toolModule = module(MConst.Tool);
        RoleToolRow roleToolRow = toolModule.getEquipById(toolId);
        if (roleToolRow == null) {
            com.stars.util.LogUtil.info("符文继承|符文装备未找到道具|roleid:{}|toolId:{}", id(), toolId);
            return;
        }
        Map<Byte, RoleTokenEquipmentHolePo> holePoMap = roleToolRow.getRoleTokenHoleInfoMap();
        if (holePoMap.isEmpty()) {
            com.stars.util.LogUtil.info("符文继承|对应的符文装备无法继承|roleid:{}|itemId:{}", id(), roleToolRow.getItemId());
            return;
        }
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleToolRow.getItemId());
        if (equipmentVo == null) {
            com.stars.util.LogUtil.info("符文继承|未能找到对应的符文装备|roleid:{}|itemId:{}", id(), roleToolRow.getItemId());
            return;
        }
        if (!NewEquipmentManager.isTokenEquipment(equipmentVo.getEquipId())) {
            warn(I18n.get("newequip.token.notTokenEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(equipmentVo.getType());
        roleEquipment.setTokenHoleStr(roleToolRow.getTokenHoleStr());
        roleEquipment.setTokenSkillId(roleToolRow.getTokenSkillId());
        roleEquipment.setTokenSKillLevel(roleToolRow.getTokenSKillLevel());
        roleToolRow.setTokenHoleStr("");
        roleToolRow.setTokenSkillStr("");
        context().update(roleToolRow);
        context().update(roleEquipment);
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        flushMarkByType(roleEquipment.getType());//刷新背包装备角标
        toolModule.getRoleEquipFlushToClient().add(roleToolRow);
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_EQUIP);//刷新装备背包
        signCalEquipRedPoint();//刷新红点
        checkActiveTokenSkill(false);
        warn("符文继承成功");
    }

    public void reqTransferAndInheritTokenEquip(byte type, long toolId) {
        if (!isHasEquipment(type)) {
            warn(I18n.get("newequip.notOwnEquipment"));
            return;
        }
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        ToolModule toolModule = this.module(MConst.Tool);
        RoleToolRow toolRow = toolModule.getEquipById(toolId);
        if (toolRow == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备道具:,info:" + id() + "|" + toolId);
            return;
        }
        if (roleEquipment.getRoleTokenHoleInfoMap().isEmpty()) {
            com.stars.util.LogUtil.info("当前装备没有符文:,info:" + id() + "|" + toolId);
            return;
        }
        EquipmentVo curEquipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        if (curEquipmentVo == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到当前装备产品数据:,info:" + id() + "|" + roleEquipment.getEquipId());
            return;
        }

        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(toolRow.getItemId());
        if (equipmentVo == null) {
            com.stars.util.LogUtil.info("穿戴装备时没有找到装备产品数据:,info:" + id() + "|" + toolId + "|" + toolRow.getItemId());
            return;
        }
        if (!canPutOn(toolRow.getItemId())) {
            return;
        }
        if (equipmentVo.getType() != roleEquipment.getType()) return;//部位不对应

        int basicFighting = FormularUtils.calFightScore(roleEquipment.getBaseAttr());
        int toolBasicFighting = FormularUtils.calFightScore(equipmentVo.getAttributePacked());
        if (toolBasicFighting <= basicFighting) return;//基础战力不满足条件

        int extraFighting = getExtraAttrMapTotalFighting(roleEquipment.getExtraAttrMap().values());
        int toolExtraFighting = getExtraAttrMapTotalFighting(toolRow.getExtraAttrMap().values());
        if (extraFighting < toolExtraFighting) return;//额外属性战力不满足条件

        if (!toolModule.deleteAndSend(curEquipmentVo.getSwitchMap(), EventType.TRANSFEREQUIP.getCode())) {//扣除转移消耗材料
            warn(I18n.get("newequip.transferToolNotEnough"));
            return;
        }

        toolModule.deleteByToolId(toolId, 1, EventType.TRANSFEREQUIP.getCode());//删除对应装备
        toolModule.flushToClient(ToolManager.FLUSH_BAG_TYPE_EQUIP);//刷新装备背包

        List<ExtraAttrVo> list = new ArrayList<>();
        list.addAll(roleEquipment.getExtraAttrMap().values());
        list.addAll(toolRow.getExtraAttrMap().values());
        Collections.sort(list);//整合额外属性并排序

        Map<Byte, ExtraAttrVo> attrMap = new HashMap<>();
        Byte maxNum = NewEquipmentManager.getMaxExtraAttrNum();
        byte curIndex = 1;
        for (ExtraAttrVo vo : list) {
            if (curIndex > maxNum) break;
            vo.setIndex(curIndex);  //重置index
            resetExtraAttrQuality(vo, equipmentVo);//重置quality
            attrMap.put(curIndex, vo);
            curIndex++;
        }
        //如果是符文装备替换前检查符文的返还
//        if (NewEquipmentManager.isTokenEquipment(roleEquipment.getEquipId())) {
//            if (StringUtil.isNotEmpty(roleEquipment.getRoleTokenHoleInfoMap())) {
//                Map<Integer, Integer> backToolMap = new HashMap<>();
//                for (RoleTokenEquipmentHolePo holePo : roleEquipment.getRoleTokenHoleInfoMap().values()) {
//                    String key = holePo.getTokenId() + "_" + holePo.getTokenLevel();
//                    TokenLevelVo tokenLevelVo = NewEquipmentManager.getTokenLevelVo(key);
//                    if (tokenLevelVo == null) continue;
//                    MapUtil.add(backToolMap, tokenLevelVo.getTransferBackMap());
//                }
//                if (StringUtil.isNotEmpty(backToolMap)) {
//                    toolModule.addAndSend(backToolMap, EventType.TRANSFEREQUIP.getCode());
//                    ClientNewEquipment client = new ClientNewEquipment(ClientNewEquipment.RESP_TOKEN_TRANSFER_BACK);
//                    client.setTransferBackToolMap(backToolMap);
//                    send(client);
//                }
//            }
//        }
        //替换装备
        roleEquipment.setEquipId(toolRow.getItemId());
        roleEquipment.setExtraAttrMap(attrMap);
//        roleEquipment.setRoleTokenHoleInfoMap(toolRow.getRoleTokenHoleInfoMap());
//        roleEquipment.setTokenSkillId(toolRow.getTokenSkillId());
//        roleEquipment.setTokenSKillLevel(toolRow.getTokenSKillLevel());
        updateAndSyncRoleEquipment(roleEquipment);//更新单个装备的属性 并同步信息至客户端
        context().update(roleEquipment);

        signCalEquipRedPoint();//标识计算装备红点
        flushMarkByType(type);//刷新背包装备角标
        fireEquipChangeAchieveEvent();
        fireEquipExtAttrAchieveEvent();
        checkActiveTokenSkill(false);
        warn("符文继承成功");
    }

    /**
     * @param roleEquipment
     * @param holeList
     * @return
     */
    private boolean checkInvalidHole(RoleEquipment roleEquipment, List<Byte> holeList) {
        EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
        int maxHoleId = NewEquipmentManager.getTokenMaxNumIndex(equipmentVo.getTokenNumIndex());
        Iterator iter = holeList.iterator();
        while (iter.hasNext()) {
            byte holeId = (byte) iter.next();
            if (holeId > maxHoleId) {//超出已开放孔位
                return false;
            }
        }
        return false;
    }

    /**
     * 请求打开装备升级界面
     *
     * @param includeProductData
     */
    public void reqOpenUpgradeUI(boolean includeProductData) {
        ClientNewEquipment clientNewEquipment = new ClientNewEquipment(ClientNewEquipment.RESP_REQ_OPEN_UPGRADE_UI);
        clientNewEquipment.setIncludeProduct(includeProductData);
        if (includeProductData) {
            List<NewEquipmentUpgradeVo> equipmentUpgradeVos = new ArrayList<>(NewEquipmentManager.equipmentUpgradeVoMap.values());
            Collections.sort(equipmentUpgradeVos);
            clientNewEquipment.setEquipmentUpgradeVos(equipmentUpgradeVos);
        }
        List<RoleEquipment> canUpgradeEquipments = new ArrayList<>();
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            NewEquipmentUpgradeVo newEquipmentUpgradeVo = NewEquipmentManager.equipmentUpgradeVoMap.get(roleEquipment.getEquipId());
            if (newEquipmentUpgradeVo != null) {
                canUpgradeEquipments.add(roleEquipment);
            }
        }
        clientNewEquipment.setCanUpgradeEquipments(canUpgradeEquipments);

        send(clientNewEquipment);
    }

    /**
     * 请求能否升级的装备状态列表(包含材料检测)
     */
    public void reqCanUpgradeList() {
        ClientNewEquipment clientNewEquipment = new ClientNewEquipment(ClientNewEquipment.RESP_REQ_CAN_UPGRADE_LIST);
        List<RoleEquipment> canUpgradeEquipments = new ArrayList<>();
        Map<Integer, Integer> canUpgradeStatus = new HashMap<>();
        Map<Integer, Integer> upgradeEquipFightScore = new HashMap<>();
        ToolModule toolModule = module(MConst.Tool);
        for (RoleEquipment roleEquipment : roleEquipMap.values()) {
            NewEquipmentUpgradeVo newEquipmentUpgradeVo = NewEquipmentManager.equipmentUpgradeVoMap.get(roleEquipment.getEquipId());
            if (newEquipmentUpgradeVo != null) {
                canUpgradeEquipments.add(roleEquipment);
                boolean contain = toolModule.contains(newEquipmentUpgradeVo.getReqItemMap());
                if (contain) {
                    canUpgradeStatus.put(roleEquipment.getEquipId(), 1);
                } else {
                    canUpgradeStatus.put(roleEquipment.getEquipId(), 0);
                }
                upgradeEquipFightScore.put((int) roleEquipment.getType(), getFutureRoleEquipmentFightScore(roleEquipment, newEquipmentUpgradeVo.getLaterEquipId()));

            }
        }
        clientNewEquipment.setUpgradeEquipFightScore(upgradeEquipFightScore);
        clientNewEquipment.setCanUpgradeEquipments(canUpgradeEquipments);
        clientNewEquipment.setCanUpgradeStatus(canUpgradeStatus);
        send(clientNewEquipment);

    }

    /**
     * 请求装备升级
     *
     * @param type 装备位置
     */
    public void reqUpgrade(byte type) {
        ClientNewEquipment clientNewEquipment = new ClientNewEquipment(ClientNewEquipment.RESP_REQ_OPEN_UPGRADE);
        clientNewEquipment.setType(type);
        RoleEquipment roleEquipment = roleEquipMap.get(type);
        if (roleEquipment == null) {
            warn("此装备位未装备任何装备");
            return;
        }
        if (!NewEquipmentManager.equipmentUpgradeVoMap.containsKey(roleEquipment.getEquipId())) {
            warn("对应位置装备无法升级");
            return;
        }
        NewEquipmentUpgradeVo newEquipmentUpgradeVo = NewEquipmentManager.equipmentUpgradeVoMap.get(roleEquipment.getEquipId());
        RoleModule roleModule = module(MConst.Role);
        int level = roleModule.getLevel();
        if (newEquipmentUpgradeVo.getReqLevel() > level) {
            warn("equipupgrade_desc_rolelevel", newEquipmentUpgradeVo.getReqLevel() + "");
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        clientNewEquipment.setOldEquipId(newEquipmentUpgradeVo.getNowEquipId());
        clientNewEquipment.setNewEquipId(newEquipmentUpgradeVo.getLaterEquipId());

        boolean success = toolModule.deleteAndSend(newEquipmentUpgradeVo.getReqItemMap(), EventType.UPGRADE_EQUIP.getCode());
        if (success) {
            roleEquipment.setEquipId(newEquipmentUpgradeVo.getLaterEquipId());
            context().update(roleEquipment);
            updateFightScore(type, true);//更新对应位置的装备属性战力
            syncAllRoleEquip();//下发角色已穿戴装备信息
            clientNewEquipment.setSuccess(true);
            LogUtil.info("role:{} upgrade equipment:{}->{}", id(), newEquipmentUpgradeVo.getNowEquipId(), newEquipmentUpgradeVo.getLaterEquipId());
        } else {
            warn("消耗品不足，装备升级失败");
            clientNewEquipment.setSuccess(false);
            return;

        }
        send(clientNewEquipment);
        signCalEquipRedPoint();
        reqOpenUpgradeUI(false);

    }

    /**
     * 获取未来装备战力,用于装备升级
     */
    private int getFutureRoleEquipmentFightScore(RoleEquipment roleEquipment, int equipId) {
//        roleEquipment = roleEquipment.copy();
        EquipmentVo equipment = NewEquipmentManager.getEquipmentVo(equipId);
        //基础属性
        Attribute baseAttribute = new Attribute(equipment.getAttributePacked());

        //强化属性
        EquipStrengthVo equipStrengthVo = NewEquipmentManager.getEquipStrengthVo(roleEquipment);
        Attribute strengthAttr = new Attribute();

        if (equipStrengthVo != null &&
                (equipStrengthVo.getAttrPencent() > 0 || equipStrengthVo.getAttrAdd() > 0)) {
            strengthAttr.addAttribute(equipment.getAttributePacked(), equipStrengthVo.getAttrPencent(), 100);
            strengthAttr.addSingleAttr(equipment.getAttributePacked().getFirstNotZeroAttrIndex(), equipStrengthVo.getAttrAdd());
        }

        //额外属性
        Attribute extraAttr = new Attribute();

        if (StringUtil.isNotEmpty(roleEquipment.getExtraAttrMap())) {
            int minExtraAttrFighting = 0;
            for (ExtraAttrVo extraAttrVo : roleEquipment.getExtraAttrMap().values()) {
                extraAttr.addSingleAttr(extraAttrVo.getAttrName(), extraAttrVo.getAttrValue());

                if (minExtraAttrFighting == 0 || minExtraAttrFighting > extraAttrVo.getFighting()) {
                    minExtraAttrFighting = extraAttrVo.getFighting();
                }
            }
        }

        Attribute totalAttr = new Attribute();
        totalAttr.addAttribute(baseAttribute);      //基础属性
        totalAttr.addAttribute(strengthAttr);  //强化属性
        totalAttr.addAttribute(extraAttr);     //额外属性

        //升星属性加成
        EquipStarVo equipStarVo = NewEquipmentManager.getEquipStarVo(roleEquipment);
        Attribute tmpAttr = new Attribute();

        if (equipStarVo != null && equipStarVo.getEnhanceAttr() > 0) {
            tmpAttr.addAttribute(totalAttr, 100 + equipStarVo.getEnhanceAttr(), 100);
            totalAttr = tmpAttr;
        }

        //符文加战力
        int tokenEquipFightAdd = roleEquipment.getTokenEquipFight();

        //单件装备全部属性
        return FormularUtils.calFightScore(totalAttr) + tokenEquipFightAdd;
    }

}
