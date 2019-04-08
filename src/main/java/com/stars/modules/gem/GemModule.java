package com.stars.modules.gem;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.daily.DailyManager;
import com.stars.modules.daily.event.DailyFuntionEvent;
import com.stars.modules.gem.event.GemEmbedAchievementEvent;
import com.stars.modules.gem.event.GemFightScoreChangeEvent;
import com.stars.modules.gem.packet.ClientGemResponse;
import com.stars.modules.gem.packet.ClientGemTishenOpr;
import com.stars.modules.gem.packet.ClientGemTishenVo;
import com.stars.modules.gem.packet.ClientRoleGemInfo;
import com.stars.modules.gem.prodata.GemHoleVo;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.modules.gem.summary.GemSummaryComponentImpl;
import com.stars.modules.gem.userdata.RoleEquipmentGem;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 装备模块;
 * Created by panzhenfeng on 2016/6/24.
 */
public class GemModule extends AbstractModule {
    // recordmap中的key
    public final static String GEM_MAX_HISTORY_FIGHTSCORE = "gem.max.history.fightscore"; //历史最高宝石战力

    RoleEquipmentGem roleGemData = null;
    private long oneKeyEmbedTimestamp = 0L; // 一键镶嵌的时间戳（需要要1秒间隔）

    public GemModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(MConst.GEM, id, self, eventDispatcher, moduleMap);
    }

    public int getRoleJobId() {
        RoleModule roleModule = module(MConst.Role);
        return roleModule.getRoleRow().getJobId();
    }

    @Override
    public void onCreation(String name_, String account_) throws Throwable {
        //新建玩家的时候会进来这里, 使用同步的方式创建对应数据项;
        RoleModule roleModule = module(MConst.Role);
        Role curRole = roleModule.getRoleRow();
        String roleId = Long.toString(curRole.getRoleId());
        //创建用户宝石数据;
        roleGemData = new RoleEquipmentGem(roleId);
        roleGemData.setModuleContext(context(), this);
        context().insert(roleGemData);
    }

    @Override
    public void onDataReq() throws Exception {
        String gemSelectSql = "select * from `roleequipmentgem` where `roleid`=" + id();
        // 宝石
        this.roleGemData = DBUtil.queryBean(DBUtil.DB_USER, RoleEquipmentGem.class, gemSelectSql);
        this.roleGemData.setModuleContext(context(), this);

        for (byte i = 1; i <= GemConstant.EQUIPMENT_MAX_COUNT; i++) {//宝石下掉了6,7级的数据，需要加载时检测并处理
            roleGemData.checkGemLevel(i);
        }

    }



    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        /**
         * 红点id=14003，当前有任何可以替换的宝石时显示。指任何装备没有空余孔位，但是背包中有可以进行替换的宝石
         */
        if (redPointIds.contains((RedPointConst.GEM_EQUIP_SLOT_CAN_EMBED))) {
            //查看是否有孔位可以镶嵌;
            Map<Byte, List<Integer>> notEmbedHoleIndexMap = new HashMap<>();
            for (byte i = 1; i <= GemConstant.EQUIPMENT_MAX_COUNT; i++) {
                notEmbedHoleIndexMap.put(i, isEquipmentTypeHasNotEmbedGemHole(i, -1));
            }
            boolean canEmbedFull = false;
            boolean canEmbedNotFull = false;
            //查看是否有宝石在背包中;
            ToolModule toolModule = (ToolModule) module(MConst.Tool);
            List<Integer> gemIdList = toolModule.getBagGemIdList();
            StringBuilder notFull = new StringBuilder();
            StringBuilder full = new StringBuilder();
            if (toolModule.getBagGemIdList().size() > 0) {
                if (!notEmbedHoleIndexMap.isEmpty()) {
                    List<Integer> holeIndexList = null;
                    for (byte key : notEmbedHoleIndexMap.keySet()) {
                        holeIndexList = notEmbedHoleIndexMap.get(key);
                        if (holeIndexList.size() > 0) {
                            canEmbedNotFull = true;
                            StringBuilder notFullItem = new StringBuilder();
                            notFullItem.append(key).append("=");
                            for (int i = 0, len = holeIndexList.size(); i < len; i++) {
                                notFullItem.append(i + 1).append("|");
                            }
                            notFull.append(notFullItem).append("+");
                        } else if (holeIndexList.size() == 0) {
                            byte equipType = key;
                            int gemHoleCount = GemManager.getGemHoleCount(equipType);
                            for (int holeIndex = 0; holeIndex < gemHoleCount; holeIndex++) {
                                //获取槽位当前的槽类型;
                                String[] tmpValueArr = roleGemData.getEquipmentGemValue(equipType, holeIndex).split(",");
                                Integer tmpGemLevelId = Integer.parseInt(tmpValueArr[0]);
                                //每次都要判断当前是否有比当前宝石战力更高的宝石战力存在背包中，然后进行替换;

                                GemLevelVo toppestFightScoreGemLevelVo = getGemFightScoreTopeest(gemIdList);
                                if (toppestFightScoreGemLevelVo != null) {
                                    GemLevelVo gemLevelVo = GemManager.getGemLevelVo(tmpGemLevelId);
                                    if (gemLevelVo == null) {
                                        continue;
                                    }
                                    if (toppestFightScoreGemLevelVo.getFightScore() > gemLevelVo.getFightScore()) {
                                        canEmbedFull = true;
                                        StringBuilder fullItem = new StringBuilder();
                                        fullItem.append(key).append("=");
                                        for (int i = 0, len = holeIndexList.size(); i < len; i++) {
                                            fullItem.append(i + 1).append("|");
                                        }
                                        full.append(fullItem).append("+");
                                    }
                                }

                            }
                        }
                    }
                }
            }
            redPointMap.put(RedPointConst.GEM_EQUIP, canEmbedNotFull ? notFull.toString() : null);
            redPointMap.put(RedPointConst.GEM_EQUIP_SLOT_CAN_EMBED, canEmbedFull ? full.toString() : null);
        }
        /**
         * 红点id=14001，当前任何可以合成的宝石时显示
         */
        if (redPointIds.contains(RedPointConst.GEM_COMPOSITION)) {
            ToolModule toolModule = (ToolModule) module(MConst.Tool);
            RoleModule roleModule = (RoleModule) module(MConst.Role);
            List<Integer> gemIdList = toolModule.getBagGemIdList();
            Map<Integer, Long> gemIdCountMap = new HashMap<>();
            boolean canCompose = false;
            for (int i = 0, len = gemIdList.size(); i < len; i++) {
                int gemId = gemIdList.get(i);
                GemLevelVo gemLevelVo = GemManager.getNextGemLevelId(gemId);
                if (gemLevelVo == null) {
                    continue;
                }
                if (!gemLevelVo.getCompoundmaterial().trim().equals("0")) {
                    String[] composeMaterialInfoArr = gemLevelVo.getCompoundmaterial().split("\\+");
                    int materialId = Integer.parseInt(composeMaterialInfoArr[0]);
                    int materialCount = Integer.parseInt(composeMaterialInfoArr[1]);
                    long materialBagCount = toolModule.getCountByItemId(materialId);
                    int levelLimit = gemLevelVo.getLevellimit();
                    if (materialBagCount >= materialCount && levelLimit <= roleModule.getLevel()) {
                        canCompose = true;
                        break;
                    }
                }
            }
            redPointMap.put(RedPointConst.GEM_COMPOSITION, canCompose ? "" : null);
        }
    }


    public void updateRedPoints() {
        signCalRedPoint(MConst.GEM, RedPointConst.GEM_EQUIP_SLOT_CAN_EMBED);
        signCalRedPoint(MConst.GEM, RedPointConst.GEM_EQUIP);
        signCalRedPoint(MConst.GEM, RedPointConst.GEM_COMPOSITION);
    }

    @Override
    public void onInit(boolean isCreation) {
        RoleModule roleModule = module(MConst.Role);
        int jobId = roleModule.getRoleRow().getJobId();
        updateFightScore(false);
        updateRedPoints();
    }

    @Override
    public void onSyncData() {
        //发送到客户端提升相关的vo数据(当前等级，当前等级+1);
        RoleModule roleModule = module(MConst.Role);
        //发送当前装备所拥有的装备提升相关的VO数据;
        int jobId = roleModule.getRoleRow().getJobId();
        int tmpLvl = 0;
        String[] gemIdArr = null;
        //当前等级和下一等级的数据;
        ClientGemTishenVo clientGemTishenVo = new ClientGemTishenVo();
        for (byte i = 1, len = GemConstant.EQUIPMENT_MAX_COUNT; i <= len; i++) {
            if (isHasEquipment(i)) {
                //获取装备位的宝石数据;
                gemIdArr = getCurEquipmentGemList(i);
                if (gemIdArr != null) {
                    int gemLevelId = 0;
                    for (int k = 0, klen = gemIdArr.length; k < klen; k++) {
                        gemLevelId = Integer.parseInt(gemIdArr[k].split(",")[0]);
                        //下发装备宝石相关的数据;
                        if (gemLevelId > 0) {
                            clientGemTishenVo.addGemLevelVo(gemLevelId);
                        }
                    }
                }
            }
        }
        //更新战力;
        updateFightScore(false);
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        List<Integer> gemLevelIdList = toolModule.getBagGemIdList();
        for (int i = 0, len = gemLevelIdList.size(); i < len; i++) {
            clientGemTishenVo.addGemLevelVo(gemLevelIdList.get(i));
        }
        send(clientGemTishenVo);
        syncGemInfo();

        //派发玩家身上的全部装备当前镶嵌了每种类型宝石的总数（新宝石加入成就相关)
        Set<Byte> typeSet = new HashSet<>();
        List<GemLevelVo> getRoleAllGemList = roleGemData.getAllEquipmentGemList();
        for(GemLevelVo levelVo : getRoleAllGemList){  //得到玩家身上宝石种类
            typeSet.add(levelVo.getType());
        }
        for(byte gemType : typeSet) { //抛事件检查玩家成就是否达成
            GemEmbedAchievementEvent gemEmbedEvent = new GemEmbedAchievementEvent(gemType, roleGemData.getAllEquipmentEmbedGemList(gemType));
            eventDispatcher().fire(gemEmbedEvent);
        }
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put(MConst.GEM, new GemSummaryComponentImpl(this, roleGemData));
        }
    }

    /**
     * 更新玩家的战力; TODO 之后改为模块都初始化完以后执行的点去;
     */
    private void updateRoleFightScore(boolean isSendToClient) {
        RoleModule roleModule = module(MConst.Role);
        Attribute gemAttr = getGemAttribute();
        // 更新属性
        roleModule.updatePartAttr(RoleManager.ROLEATTR_GEM, gemAttr);
        roleModule.sendRoleAttr();
        int preGemFightScore = 0;
        Map<String, Integer> roleFightScoreMap = roleModule.getRoleRow().getFightScoreMap();
        if (roleFightScoreMap.containsKey(RoleManager.FIGHTSCORE_GEM)) {
            preGemFightScore = roleFightScoreMap.get(RoleManager.FIGHTSCORE_GEM);
        }
        int newGemFightScore = getGemFightScore();
        roleModule.updatePartFightScore(RoleManager.FIGHTSCORE_GEM, newGemFightScore);
        //记录历史宝石最高战力
        int maxHistoryFightScore = context().recordMap().getInt(GEM_MAX_HISTORY_FIGHTSCORE,0);
        if(maxHistoryFightScore < newGemFightScore){
            context().recordMap().setInt(GEM_MAX_HISTORY_FIGHTSCORE,newGemFightScore);
        }
        if (isSendToClient) {
            roleModule.sendUpdateFightScore();
        }
        if (newGemFightScore != preGemFightScore) {
            eventDispatcher().fire(new GemFightScoreChangeEvent());
        }
    }

    public int getGemFightScore() {
        Attribute gemAttr = getGemAttribute();
        return FormularUtils.calFightScore(gemAttr);
    }

    /**
     * 更新战力;
     */
    private void updateFightScore() {
        updateFightScore(true);
    }

    /**
     * 更新战力;
     */
    private void updateFightScore(boolean isSendToClient) {
        updateRoleFightScore(isSendToClient);

//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(
//                    id(), new GemSummaryComponentImpl(this, roleGemData));
//        } catch (Exception e) {
//            LogUtil.error("", e);
//        }
        context().markUpdatedSummaryComponent(MConst.GEM);
    }

    /**
     * 获取装备位的宝石列表;
     *
     * @param equipmentType_
     * @return
     */
    public String[] getCurEquipmentGemList(byte equipmentType_) {
        if (isHasEquipment(equipmentType_)) {
            String typeStr = roleGemData.getTypeFieldValue(equipmentType_);
            if (typeStr == "0" || typeStr.length() <= 0) {
                return null;
            }
            return typeStr.split("\\+");
        }
        return null;
    }

    public List<GemLevelVo> getEquipmentGemList(byte gemType) {
        if (roleGemData == null) return null;
        if (!isHasEquipment(gemType)) return null;
        return roleGemData.getEquipmentGemList(gemType);
    }

    public List<GemLevelVo> getEquipmentGemList() {
        if (roleGemData == null) return null;
        return roleGemData.getAllEquipmentGemList();
    }

    public List<Integer> isEquipmentTypeHasNotEmbedGemHole(byte equipmentType_, int gemHoleIndex_) {
        List<Integer> rtnHoleIndexList = new ArrayList<>();
        //判断装备位开放没先;
        NewEquipmentModule equipmentModule = (NewEquipmentModule) moduleMap().get(MConst.NewEquipment);
        if (!equipmentModule.isHasEquipment(equipmentType_)) {
            return rtnHoleIndexList;
        }
        String tmpGemIdInfo = null;
        String tmpGemId = null;
        //判断是否开放了当前槽类型;
        int startIndex = 0;
        int endIndex = 0;
        if (gemHoleIndex_ >= 0) {
            startIndex = gemHoleIndex_;
            endIndex = gemHoleIndex_;
        } else {
            endIndex = GemManager.getGemHoleCount(equipmentType_);
        }
        do {
            if (isEquipmentGemHoleHasOpend(equipmentType_, (byte) (startIndex + 1))) {
                //查看当前槽位是否镶嵌了宝石了;
                tmpGemIdInfo = roleGemData.getEquipmentGemValue(equipmentType_, startIndex);
                //查看是否有宝石;
                String[] valueArr = tmpGemIdInfo.split(",");
                int gemLevelId = Integer.parseInt(valueArr[0]);
                if (gemLevelId <= 0) {
                    rtnHoleIndexList.add(startIndex);
                }
            }
            startIndex++;
        } while (startIndex < endIndex);
        return rtnHoleIndexList;
    }

    /**
     * 请求镶嵌宝石;
     *
     * @param equipmentType_
     * @param gemHoleIndex_
     * @param gemLevelId_
     */
    private boolean requestEmbedGem(byte equipmentType_, int gemHoleIndex_, int gemLevelId_, byte tishenOprType_, boolean needWarn, boolean needUpdateFightScore) {
        do {
            //判断当前当前部位是否有装备;
            NewEquipmentModule newEquipmentModule = (NewEquipmentModule) module(MConst.NewEquipment);
            if (!newEquipmentModule.isHasEquipment(equipmentType_)) {
                break;
            }
            //判断当前槽位能否镶嵌这种宝石;
            //获取对应的gemlevel数据，用于获取宝石类型;
            GemLevelVo gemLevelVo = GemManager.getGemLevelVo(gemLevelId_);
            if (gemLevelVo == null) {
                if (needWarn) {
                    warn("gem_compound_listempty");
                }
                break;
            }
            //判断是否开放了当前槽类型;
            if (!isEquipmentGemHoleHasOpend(equipmentType_, (byte) (gemHoleIndex_ + 1))) {
                if (needWarn) {
                    warn(I18n.get("equipment.gem.gemholenotopen"));
                }
                break;
            }
            //判断背包中是否有对应宝石;
            ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
            if (toolModule.getCountByItemId(gemLevelId_) <= 0) {
                if (needWarn) {
                    warn("gem_compound_listempty");
                }
                break;
            }
            //尝试从背包中删除一个宝石然后放到RoleEquipmentGem表中;
            if (false == toolModule.deleteAndSend(gemLevelId_, 1, EventType.EMBEDGEM.getCode())) {
                if (needWarn) {
                    com.stars.util.LogUtil.error("装备-镶嵌宝石,从背包删除1个道具时报错! itemId=" + gemLevelId_);
                }
                break;
            }
            roleGemData.setEquipmentGemValue(equipmentType_, gemHoleIndex_, gemLevelId_);
            if (needUpdateFightScore) {
                updateFightScore();
            }
            ClientGemTishenOpr clientGemTishenOpr = new ClientGemTishenOpr();
            clientGemTishenOpr.addData(equipmentType_, roleGemData.getTypeFieldValue(equipmentType_), tishenOprType_);
            send(clientGemTishenOpr);
            //派发玩家身上的全部装备当前镶嵌了这个类型宝石的总数;
            GemEmbedAchievementEvent gemEmbedEvent = new GemEmbedAchievementEvent(gemLevelVo.getType(), roleGemData.getAllEquipmentEmbedGemList(gemLevelVo.getType()));
            eventDispatcher().fire(gemEmbedEvent);
            return true;
        } while (false);
        return false;
    }

    public boolean isEquipmentGemHoleHasOpend(byte equipmentType_, byte holeId_) {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        GemHoleVo gemHoleVo = GemManager.getGemHoleVo(equipmentType_, holeId_);
        return !(gemHoleVo.getUnlockLvl() > roleModule.getLevel());
    }

    /**
     * 请求脱下宝石;
     *
     * @param equipmentType_
     * @param gemHoleIndex_
     */
    private Integer requestDisEmbedGem(byte equipmentType_, int gemHoleIndex_, boolean needWarn_, byte tishenOprType_, boolean needNotifyClient) {
        String srcGemValue = roleGemData.getEquipmentGemValue(equipmentType_, gemHoleIndex_);
        //查看是否有宝石;
        String[] valueArr = srcGemValue.split(",");
        int gemLevelId = Integer.parseInt(valueArr[0]);
        if (gemLevelId <= 0) {
            if (needWarn_) {
                warn("gem_compound_listempty");
            }
            return 0;
        }
        //将RoleEquipmentGem中对应槽位的宝石清掉;
        roleGemData.setEquipmentGemValue(equipmentType_, gemHoleIndex_, 0);
        if (needNotifyClient) {
            ClientGemTishenOpr clientGemTishenOpr = new ClientGemTishenOpr();
            clientGemTishenOpr.addData(equipmentType_, roleGemData.getTypeFieldValue(equipmentType_), tishenOprType_);
            send(clientGemTishenOpr);
        }
        updateFightScore(needNotifyClient);
//        updateFightScore();
        //放入背包中;
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        toolModule.addAndSend(gemLevelId, 1, EventType.DISBEDGEM.getCode());
        return gemLevelId;
    }

    /**
     * 请求升级宝石;
     *
     * @param equipmentType_
     * @param gemHoleIndex_
     * @param gemLevelId_
     */
    private void requestLevelupGem(byte equipmentType_, int gemHoleIndex_, int gemLevelId_, byte tishenOprType_) {
        //获取当前宝石的下一级宝石数据,用于获取升级所需材料;
        GemLevelVo curGemLevelVo = GemManager.getGemLevelVo(gemLevelId_);
        if (curGemLevelVo == null) {
            com.stars.util.LogUtil.error("装备-升级宝石,找不到对应的GemLevel数据：" + gemLevelId_);
            return;
        }
        if (!GemManager.isGemLevelCanLevelUp(gemLevelId_)) {
            warn(I18n.get("equipment.gem.cannotlevel"));
            return;
        }
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        if (curGemLevelVo.getLevellimit() > roleModule.getLevel()) {
            warn(I18n.get("equipment.gem.rolelevelisnotenough"));
            return;
        }
        GemLevelVo nextGemLevelVo = GemManager.getGemLevelVoByTypeLevel(curGemLevelVo.getType(), curGemLevelVo.getLevel() + 1);
        if (nextGemLevelVo == null) {
            warn(I18n.get("equipment.gem.gemmaxlevel"));
            return;
        }
        String levelMaterial = nextGemLevelVo.getLevelupmaterial();
        if (levelMaterial != "0" && StringUtil.isNotEmpty(levelMaterial)) {
            ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
            String[] infoArr = levelMaterial.split(",");
            String[] itemArr = null;
            int materialId = 0;
            int materialCount = 0;
            long materalBagCount = 0;
            //判断材料是否足够;
            for (int i = 0, len = infoArr.length; i < len; i++) {
                itemArr = infoArr[i].split("\\+");
                materialId = Integer.parseInt(itemArr[0]);
                materialCount = Integer.parseInt(itemArr[1]);
                materalBagCount = toolModule.getCountByItemId(materialId);
                if (materalBagCount < materialCount) {
                    warn("gem_compound_disable");
                    return;
                }
            }
            //到这里说明材料足够,开始消耗材料;
            for (int i = 0, len = infoArr.length; i < len; i++) {
                itemArr = infoArr[i].split("\\+");
                materialId = Integer.parseInt(itemArr[0]);
                materialCount = Integer.parseInt(itemArr[1]);
                materalBagCount = toolModule.getCountByItemId(materialId);
                if (false == toolModule.deleteAndSend(materialId, materialCount, EventType.LEVELUPGEM.getCode())) {
                    com.stars.util.LogUtil.error("装备-升级宝石,材料消耗异常");
                    return;
                }
            }
        }
        //升级宝石,放在这里是因为,如果不需要材料也可以升级的情况;
        //升级的话需要通知客户端下一级的数据,用于显示需要;
        ClientGemTishenVo clientGemTishenVo = new ClientGemTishenVo();
        clientGemTishenVo.addGemLevelVo(nextGemLevelVo.getItemId());
        send(clientGemTishenVo);
        //将RoleEquipmentGem中对应槽位放入下一等级的宝石;
        roleGemData.setEquipmentGemValue(equipmentType_, gemHoleIndex_, nextGemLevelVo.getItemId());
        ClientGemTishenOpr clientGemTishenOpr = new ClientGemTishenOpr();
        clientGemTishenOpr.addData(equipmentType_, roleGemData.getTypeFieldValue(equipmentType_), tishenOprType_);
        send(clientGemTishenOpr);

    }

    private GemLevelVo getGemFightScoreTopeestByBag() {
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        //获取当前背包中的宝石列表;
        List<Integer> gemIdList = toolModule.getBagGemIdList();
        //如果一颗宝石都没有,就不需要执行下面的逻辑了;
        if (gemIdList.size() <= 0) {
            return null;
        }
        return getGemFightScoreTopeest(gemIdList);
    }

    //从背包中获取战力最高的宝石数据;
    private GemLevelVo getGemFightScoreTopeest(List<Integer> gemIdList) {
        //转换为GemLevelVo列表，因为需要排序;
        List<GemLevelVo> gemLevelVoList = new ArrayList<>();
        for (int i = 0, len = gemIdList.size(); i < len; i++) {
            gemLevelVoList.add(GemManager.getGemLevelVo(gemIdList.get(i)));
        }
        if (gemLevelVoList.size() > 0) {
            Collections.sort(gemLevelVoList);
            return gemLevelVoList.get(0);
        }
        return null;
    }

    /**
     * 一键镶嵌宝石;
     *
     * @param equipmentType_
     */
    private int requestOneKeyEmbedGem(byte equipmentType_, byte tishenOprType_, Map<Integer, Long> remainGemIdCountMap) {
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        //判断下可镶嵌的宝石还有没有数量;
        int remainGemIdCount = 0;
        for (Integer tmpGemIdKey : remainGemIdCountMap.keySet()) {
            remainGemIdCount += remainGemIdCountMap.get(tmpGemIdKey);
        }
        if (remainGemIdCount <= 0) {
            return -1;
        }
        //获取当前装备可镶嵌的槽位数;
        int gemHoleCount = GemManager.getGemHoleCount(equipmentType_);
        String[] tmpValueArr = null;
        int tmpGemLevelId = 0;
        int hasEmbedCount = 0;
        GemLevelVo toppestFightScoreGemLevelVo = null;
        GemLevelVo tmpGemLevelVo = null;
        List<Integer> tmpGemIdList = new ArrayList<>();
        int disEmbedGemId;
        for (int i = 0; i < gemHoleCount; i++) {
            //获取槽位当前的槽类型;
            tmpValueArr = roleGemData.getEquipmentGemValue(equipmentType_, i).split(",");
            tmpGemLevelId = Integer.parseInt(tmpValueArr[0]);
            //每次都要判断当前是否有比当前宝石战力更高的宝石战力存在背包中，然后进行替换;
            tmpGemIdList.clear();
            for (Integer tmpGemIdKey : remainGemIdCountMap.keySet()) {
                if (remainGemIdCountMap.get(tmpGemIdKey) > 0) {
                    tmpGemIdList.add(tmpGemIdKey);
                }
            }
            if (tmpGemIdList.size() <= 0) {
                break;
            }
            toppestFightScoreGemLevelVo = getGemFightScoreTopeest(tmpGemIdList);
            if (toppestFightScoreGemLevelVo == null) {
                continue;
            }
            //判断当前槽位能否镶嵌宝石;
            tmpGemLevelVo = GemManager.getGemLevelVo(tmpGemLevelId);
            if (tmpGemLevelVo != null) {
                if (toppestFightScoreGemLevelVo.getFightScore() > tmpGemLevelVo.getFightScore()) {
                    tmpGemLevelVo = toppestFightScoreGemLevelVo;
                } else {
                    continue;
                }
            } else {
                tmpGemLevelVo = toppestFightScoreGemLevelVo;
            }
            //先摘除当前位的宝石数据;
            disEmbedGemId = requestDisEmbedGem(equipmentType_, i, false, tishenOprType_, false);
            if (disEmbedGemId > 0) {
                if (!remainGemIdCountMap.containsKey(disEmbedGemId)) {
                    remainGemIdCountMap.put(disEmbedGemId, 0L);
                }
                remainGemIdCountMap.put(disEmbedGemId, remainGemIdCountMap.get(disEmbedGemId) + 1);
            }
            //镶嵌新宝石;
            if (tmpGemLevelVo != null && remainGemIdCountMap.containsKey(tmpGemLevelVo.getItemId())) {
                if (remainGemIdCountMap.get(tmpGemLevelVo.getItemId()) > 0) {
                    if (requestEmbedGem(equipmentType_, i, tmpGemLevelVo.getItemId(), tishenOprType_, false, false)) {
                        remainGemIdCountMap.put(tmpGemLevelVo.getItemId(), remainGemIdCountMap.get(tmpGemLevelVo.getItemId()) - 1);
                        hasEmbedCount++;
                    }
                }
            }
        }
        return hasEmbedCount;
    }

    /**
     * 一键脱下宝石;
     *
     * @param equipmentType_
     */
    private void requestOneKeyDisEmbedGem(byte equipmentType_, byte tishenOprType_) {
        //获取当前装备可镶嵌的槽位数;
        int gemHoleCount = GemManager.getGemHoleCount(equipmentType_);
        //循环遍历槽位即可;
        for (int i = 0; i < gemHoleCount; i++) {
            requestDisEmbedGem(equipmentType_, i, false, tishenOprType_, true);
        }
    }

    /**
     * 判断自身目前拥有的材料能合成多少个指定宝石;
     *
     * @param waitToComposeGemLevelId
     * @return
     */
    public int getCanComposeGemCount(int waitToComposeGemLevelId) {
        //判断是否配置有所需的宝石数据;
        GemLevelVo gemLevelVo = GemManager.getGemLevelVo(waitToComposeGemLevelId);
        if (gemLevelVo == null) {
            return 0;
        }
        if (!GemManager.isGemLevelCanComposed(waitToComposeGemLevelId)) {
            return 0;
        }
        //获取升级该宝石所需的材料是否背包中也有;
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        String[] composeMaterialInfoArr = gemLevelVo.getCompoundmaterial().split("\\+");
        int materialId = Integer.parseInt(composeMaterialInfoArr[0]);
        int materialCount = Integer.parseInt(composeMaterialInfoArr[1]);
        long materialBagCount = toolModule.getCountByItemId(materialId);
        int rtnCount = 0;
        while (materialCount <= materialBagCount) {
            rtnCount++;
            materialBagCount -= materialCount;
        }
        return rtnCount;
    }

    /**
     * 请求合成宝石;
     *
     * @param gemLevelId_
     */
    private boolean requestComposeGem(int gemLevelId_, boolean needWarn_, byte tishenOprType_, boolean needResponse) {
        //判断是否配置有所需的宝石数据;
        GemLevelVo gemLevelVo = GemManager.getGemLevelVo(gemLevelId_);
        if (gemLevelVo == null) {
            if (needWarn_) {
                warn("gem_compound_listempty");
            }
            return false;
        }
        if (!GemManager.isGemLevelCanComposed(gemLevelId_)) {
            if (needWarn_) {
                warn(I18n.get("equipment.gem.cannotlevel"));
            }
            return false;
        }
        RoleModule roleModule = (RoleModule) this.module(MConst.Role);
        if (gemLevelVo.getLevellimit() > roleModule.getLevel()) {
            if (needWarn_) {
                warn(I18n.get("equipment.gem.rolelevelisnotenough"));
            }
            return false;
        }
        //获取升级该宝石所需的材料是否背包中也有;
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        String[] composeMaterialInfoArr = gemLevelVo.getCompoundmaterial().split("\\+");
        int materialId = Integer.parseInt(composeMaterialInfoArr[0]);
        int materialCount = Integer.parseInt(composeMaterialInfoArr[1]);
        long materialBagCount = toolModule.getCountByItemId(materialId);
        int count = 1; //倍数
        if(tishenOprType_ == GemConstant.TISHEN_OPR_EXTEND_5){
            if(materialBagCount  > materialCount){
                count = (int) Math.floor(materialBagCount/materialCount);
                materialCount = materialCount * count;
            }
        }
        //查看是否数量足够;
        if (materialCount > materialBagCount) {
            if (needWarn_) {
                warn("gem_compound_disable");
            }
            return false;
        }
        //消耗原来的宝石数据，放入新的宝石数据;
        if ((materialBagCount - materialCount) >= 0) {
            //进来就说明符合要求;
            if (toolModule.deleteAndSend(materialId, materialCount, EventType.COMPOSEGEM.getCode())) {
                Map<Integer,Integer> addToolMap = toolModule.addAndSend(gemLevelId_, count, EventType.COMPOSEGEM.getCode());
                if(StringUtil.isNotEmpty(addToolMap)){
                    ClientAward client = new ClientAward(addToolMap);
                    send(client);
                }
            } else {
                LogUtil.error("装备-升级宝石,材料消耗异常");
            }
        }
        //发送给客户端提示
        if (needResponse) {
            ClientGemResponse clientGemResponse = new ClientGemResponse();
            clientGemResponse.type = 0;
            clientGemResponse.param = gemLevelId_ + "+1";
            send(clientGemResponse);
        }
        updateRedPoints();
        //发送操作反馈,用于一些信息的更新;
        ClientGemTishenOpr clientGemTishenOpr = new ClientGemTishenOpr();
        clientGemTishenOpr.addData((byte) 0, "", tishenOprType_);
        send(clientGemTishenOpr);
        // 抛出日常活动事件
        eventDispatcher().fire(new DailyFuntionEvent(DailyManager.DAILYID_GEM_COMPOSE, 1));
        return true;
    }

    /**
     * 请求一键合成宝石;
     */
    private void requestOneKeyCompose(byte tishenOprType_, int requestComposeGemLevelId) {
        //获取背包中所有的宝石数据;
        ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
        List<Integer> gemIdList = null;
        if (requestComposeGemLevelId > 0) {
            gemIdList = new ArrayList<>();
            gemIdList.add(requestComposeGemLevelId);
        } else {
            gemIdList = toolModule.getBagGemIdList();
        }
        //获取下一等级的宝石列表;
        gemIdList = GemManager.getNextGemLevelIds(gemIdList);
        boolean recordHasEffective = false;
        boolean tmpCurHasEffective = false;
        Map<Integer, Integer> effectiveMap = new ConcurrentHashMap<>();
        do {
            tmpCurHasEffective = false;
            for (int i = 0, len = gemIdList.size(); i < len; i++) {
                if (requestComposeGem(gemIdList.get(i), false, tishenOprType_, false)) {
                    tmpCurHasEffective = true;
                    recordHasEffective = true;
                    if (!effectiveMap.containsKey(gemIdList.get(i))) {
                        effectiveMap.put(gemIdList.get(i), 1);
                    } else {
                        effectiveMap.put(gemIdList.get(i), effectiveMap.get(gemIdList.get(i)) + 1);
                    }
                }
            }
        } while (tmpCurHasEffective);
//        if (effectiveMap.size() > 0) {
//            ClientGemResponse clientGemResponse = new ClientGemResponse();
//            for (Map.Entry<Integer, Integer> kvp : effectiveMap.entrySet()) {
//                clientGemResponse.type = 0;
//                clientGemResponse.param = kvp.getKey() + "+" + kvp.getValue();
//                send(clientGemResponse);
//            }
//        }
        if (recordHasEffective == false) {
            warn("gem_compound_listempty");
        }
        updateRedPoints();
    }

    public boolean isHasEquipment(byte equipmentType) {
        NewEquipmentModule newEquipmentModule = (NewEquipmentModule) module(MConst.NewEquipment);
        return newEquipmentModule.isHasEquipment(equipmentType);
    }

    /**
     * 宝石提升;
     *
     * @param equipmentType_ 1:镶嵌,2脱下,3升级(已弃用)，4合成, 5一键镶嵌 6一键脱下 7一键合成
     * @param tishenOprType_
     */
    private void tishenGem(byte equipmentType_, byte tishenOprType_, String extendParam_) {
        //判断是否已经拥有该装备了;
        if (equipmentType_ > 0 && !isHasEquipment(equipmentType_)) {
            warn(I18n.get("equipment.hasnoequipments"));
            return;
        }
        String[] paramArr = null;
        int gemHoleIndex = -1;
        int gemLevelId = -1;
        switch (tishenOprType_) {
            case GemConstant.TISHEN_OPR_ONE:
                paramArr = extendParam_.split("\\+");
                gemHoleIndex = Integer.parseInt(paramArr[0]);
                gemLevelId = Integer.parseInt(paramArr[1]);
                requestDisEmbedGem(equipmentType_, gemHoleIndex, false, tishenOprType_, false);
                requestEmbedGem(equipmentType_, gemHoleIndex, gemLevelId, tishenOprType_, true, true);
                break;
            case GemConstant.TISHEN_OPR_ONE_KEY:
                gemHoleIndex = Integer.parseInt(extendParam_);
                requestDisEmbedGem(equipmentType_, gemHoleIndex, true, tishenOprType_, true);
                break;
            case GemConstant.TISHEN_OPR_EXTEND_1:
                paramArr = extendParam_.split("\\+");
                gemHoleIndex = Integer.parseInt(paramArr[0]);
                gemLevelId = Integer.parseInt(paramArr[1]);
                requestLevelupGem(equipmentType_, gemHoleIndex, gemLevelId, tishenOprType_);
                break;
            case GemConstant.TISHEN_OPR_EXTEND_2:
                gemLevelId = Integer.parseInt(extendParam_);
                requestComposeGem(gemLevelId, true, tishenOprType_, true);
                break;
            case GemConstant.TISHEN_OPR_EXTEND_3:
                if (System.currentTimeMillis() - oneKeyEmbedTimestamp < 2000) {
                    warn(I18n.get("equipment.gem.onekeyembed.cooldown"));
                    break;
                }
                oneKeyEmbedTimestamp = System.currentTimeMillis();
                int resultCount = 0;
                //要在这里获取宝石数量，防止在镶嵌的过程中触发了成就奖励，又奖励了宝石，导致新的宝石也被镶嵌了，造成体验奇怪;
                //获取当前背包中的宝石列表;
                ToolModule toolModule = (ToolModule) this.module(MConst.Tool);
                List<Integer> gemIdList = toolModule.getBagGemIdList();
                Map<Integer, Long> gemIdCountMap = new HashMap<>();
                for (int i = 0, len = gemIdList.size(); i < len; i++) {
                    gemIdCountMap.put(gemIdList.get(i), toolModule.getCountByItemId(gemIdList.get(i)));
                }
                if (equipmentType_ <= 0) {
                    for (byte i = 1; i <= GemConstant.EQUIPMENT_MAX_COUNT; i++) {
                        resultCount += requestOneKeyEmbedGem(i, tishenOprType_, gemIdCountMap);
                    }
                    if (resultCount > 0) {
                        warn("gem_tips_fillall_updategem", String.valueOf(resultCount));
                    } else {
                        warn("gem_tips_fillall_none");
                    }
                } else {
                    resultCount += requestOneKeyEmbedGem(equipmentType_, tishenOprType_, gemIdCountMap);
                    if (resultCount > 0) {
                        warn("gem_tips_fillall_updategem", String.valueOf(resultCount));
                    } else {
                        warn("gem_tips_fillall_none");
                    }
                }
                updateFightScore();
                break;
            case GemConstant.TISHEN_OPR_EXTEND_4:
                requestOneKeyDisEmbedGem(equipmentType_, tishenOprType_);
                break;
            case GemConstant.TISHEN_OPR_EXTEND_5:
                int requestComposeGemLevelId = 0;
                if (StringUtil.isNotEmpty(extendParam_)) {
                    requestComposeGemLevelId = Integer.parseInt(extendParam_);
                }
                requestOneKeyCompose(tishenOprType_, requestComposeGemLevelId);
                break;
        }
        updateRedPoints();
    }

    /**
     * 请求装备提升;
     *
     * @param equipmentType_ 1~6
     * @param tishenOprType_ 参见EquipmentConstant
     * @param extendParam_   用于自定义对某个类型的参数,使用的话自行约定格式即可;
     * @return
     */
    public void requestTishen(byte equipmentType_, byte tishenOprType_, int jobId_, String extendParam_) {
        tishenGem(equipmentType_, tishenOprType_, extendParam_);
    }

    public Attribute getGemAttribute() {
        Attribute rtnAttribute = new Attribute();
        //宝石;
        Attribute attribute;
        for (byte i = 1, len = GemConstant.EQUIPMENT_MAX_COUNT; i <= len; i++) {
            attribute = roleGemData.getAttribute(i);
            if (attribute != null) {
                rtnAttribute.addAttribute(attribute);
            }
        }
        return rtnAttribute;
    }

    public void syncGemInfo() {
        ClientRoleGemInfo clientRoleGemInfo = new ClientRoleGemInfo();
        clientRoleGemInfo.setData(roleGemData);
        send(clientRoleGemInfo);
    }

    public String makeFsStr() {
        int braveFs = 0; // 勇者宝石
        int richFs = 0; // 富贵宝石
        int fightFs = 0; // 斗魂宝石
        Attribute braveAttr = new Attribute();
        Attribute richAttr = new Attribute();
        Attribute fightAttr = new Attribute();
        List<GemLevelVo> list = roleGemData.getAllEquipmentGemList();
        for (GemLevelVo vo : list) {
            switch (vo.getType()) {
                case 1: // 勇者
                    braveAttr.addAttribute(vo.getAttributeattribute());
                    break;
                case 2: // 富贵
                    richAttr.addAttribute(vo.getAttributeattribute());
                    break;
                case 3: // 斗魂
                    fightAttr.addAttribute(vo.getAttributeattribute());
                    break;
            }
        }
        braveFs = FormularUtils.calFightScore(braveAttr);
        richFs = FormularUtils.calFightScore(richAttr);
        fightFs = FormularUtils.calFightScore(fightAttr);

        StringBuilder sb = new StringBuilder();
        sb.append("gem_brave:").append(braveFs).append("#")
                .append("gem_rich:").append(richFs).append("#")
                .append("gem_fight:").append(fightFs).append("#");
        return sb.toString();
    }
}
