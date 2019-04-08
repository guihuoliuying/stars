package com.stars.modules.deityweapon;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.deityweapon.event.ActiveDeityWeaponAchieveEvent;
import com.stars.modules.deityweapon.event.DeityWeaponChangeEvent;
import com.stars.modules.deityweapon.packet.ClientDeityWeaponOpr;
import com.stars.modules.deityweapon.prodata.DeityWeaponLevelVo;
import com.stars.modules.deityweapon.prodata.DeityWeaponVo;
import com.stars.modules.deityweapon.summary.DeityWeaponSummaryComponentImpl;
import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.summary.SummaryComponent;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.*;
import java.util.Map.Entry;

/**
 * 神兵系统模块;
 * 注意：一个角色只能佩戴一个神兵;
 * Created by panzhenfeng on 2016/12/14.
 */
public class DeityWeaponModule extends AbstractModule {
    private List<RoleDeityWeapon> roleDeityWeaponList = new ArrayList<>();
    private byte tmpAddNewDeityweaponType = (byte)0;

    public DeityWeaponModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("神兵", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Exception {
        //获取当前已经拥有的神兵数据;
        String roleDeityweapon = "select * from `" + DeityWeaponConstant.RoledeityweaponName + "` where `roleid`=" + id();
        roleDeityWeaponList = DBUtil.queryList(DBUtil.DB_USER, RoleDeityWeapon.class, roleDeityweapon);

    }

    @Override
    public void onInit(boolean isCreation) {
        updateAttrAndFightScoreWithSend(false);
        updateForgeRedPionts();
    }

    @Override
    public void onSyncData() {
        syncToClientAllRoleDeityInfo();
        Set<Byte> typeSet = new HashSet<>();
        if(StringUtil.isNotEmpty(roleDeityWeaponList)) {  //登陆检测玩家成就是否达成
            for(RoleDeityWeapon roleDeityWeapon:roleDeityWeaponList) {
                typeSet.add(roleDeityWeapon.getType());
            }
        }
        for(byte type:typeSet){
            ActiveDeityWeaponAchieveEvent event = new ActiveDeityWeaponAchieveEvent(type);
            eventDispatcher().fire(event);
        }
    }

    @Override
    public void onTimingExecute() {
        if (roleDeityWeaponList != null) {
            RoleDeityWeapon roleDeityWeapon = null;
            for (int i = roleDeityWeaponList.size() - 1; i >= 0; i--) {
                roleDeityWeapon = roleDeityWeaponList.get(i);
                if (roleDeityWeapon.isExpired() && (roleDeityWeapon.getState() == DeityWeaponConstant.ACTIVE || roleDeityWeapon.getState() == DeityWeaponConstant.DRESS)) {
                    requestDisActive(roleDeityWeapon.getType());
                }
            }
        }
    }

    @Override
    public void onUpateSummary(Map<String, SummaryComponent> componentMap) {
        if (!SpecialAccountManager.isSpecialAccount(id())) {
            componentMap.put(MConst.Deity, getNewSummaryComp(roleDeityWeaponList));
        }
    }

    //同步到常用数据中;
    public void syncToSummary(){
//        try {
//            ServiceHelper.summaryService().updateSummaryComponent(
//                    id(), getNewSummaryComp(roleDeityWeaponList));
//        } catch (Exception e) {
//            LogUtil.error("DeityWeapon", e);
//        }
        context().markUpdatedSummaryComponent(MConst.Deity);
    }

    private DeityWeaponSummaryComponentImpl getNewSummaryComp(List<RoleDeityWeapon> roleDeityWeaponList){
        DeityWeaponSummaryComponentImpl comp = new DeityWeaponSummaryComponentImpl(roleDeityWeaponList);
        DeityWeaponSummaryComponentImpl newComp = new DeityWeaponSummaryComponentImpl();
        newComp.fromString(1,comp.makeString());
        return newComp;
    }

    public void syncToClientAllRoleDeityInfo(){
        List<RoleDeityWeapon> roleDeityWeaponList = getRoleDeityWeaponList();
        ClientDeityWeaponOpr clientDeityWeaponOpr = new ClientDeityWeaponOpr();
        clientDeityWeaponOpr.setRoleDeityWeaponList(roleDeityWeaponList);
        send(clientDeityWeaponOpr);
    }

    public void updateRedPoints(byte deityWeaponType){
        tmpAddNewDeityweaponType = deityWeaponType;
        signCalRedPoint(MConst.Deity, RedPointConst.DEITYWEAPON_NEW_ACTIVE);
    }
    
    public void updateForgeRedPionts(){
    	signCalRedPoint(MConst.Deity, RedPointConst.DEITYWEAPON_FORGE);
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
    	if(redPointIds.contains((RedPointConst.DEITYWEAPON_FORGE))){
    		Map<Byte, Byte> checkList = checkCanForge();
    		Iterator<Entry<Byte, Byte>> iterator = checkList.entrySet().iterator();
    		Entry<Byte, Byte> entry = null;
    		StringBuffer sb = new StringBuffer();
    		for(;iterator.hasNext();){
    			entry = iterator.next();
    			if(entry.getValue()==1){
    				if(sb.length()==0){        				
    					sb.append(entry.getKey());
    				}else{
    					sb.append("+").append(entry.getKey());
    				}
    			}
    		}
    		if(sb.length()>0){        		
    			redPointMap.put(RedPointConst.DEITYWEAPON_FORGE, sb.toString());
    			redPointMap.put(RedPointConst.DEITYWEAPON_NEW_ACTIVE, sb.toString());
    		}else{
    			redPointMap.put(RedPointConst.DEITYWEAPON_FORGE, null);
    			redPointMap.put(RedPointConst.DEITYWEAPON_NEW_ACTIVE, null);
    		}
    	}
        if(redPointIds.contains((RedPointConst.DEITYWEAPON_NEW_ACTIVE))){
            redPointMap.put(RedPointConst.DEITYWEAPON_NEW_ACTIVE, String.valueOf(tmpAddNewDeityweaponType));
        }
    }

    /**
     * 获得新的神兵;
     *
     * @param deityWeaponType
     */
    public void addNewDeityWeapon(byte deityWeaponType) {
        DeityWeaponVo deityWeaponVo = DeityWeaponManager.getDeityWeaponVo(getJobId(), deityWeaponType);
        DeityWeaponLevelVo initDeityweaponLevelVo = DeityWeaponManager.getInitDeityWeaponLevelVo(deityWeaponType);
        RoleDeityWeapon roleDeityWeapon = new RoleDeityWeapon();
        roleDeityWeapon.setRoleId(id());
        roleDeityWeapon.setType(deityWeaponType);
        roleDeityWeapon.setLevel(initDeityweaponLevelVo.getDeityweaponlvl());
        roleDeityWeapon.setState(DeityWeaponConstant.DISDRESS);
        roleDeityWeapon.setEndTimestamp(deityWeaponVo.isForever() ? 0 : geCurrentTimeStamp(deityWeaponVo.getTimelimit()));
        context().insert(roleDeityWeapon);
        if(roleDeityWeaponList==null){
            roleDeityWeaponList = new ArrayList<>();
        }
        roleDeityWeaponList.add(roleDeityWeapon);
        updateAttrAndFightScoreWithSend();

        ActiveDeityWeaponAchieveEvent event = new ActiveDeityWeaponAchieveEvent(deityWeaponType);
        eventDispatcher().fire(event);
        updateForgeRedPionts();
        updateRedPoints(deityWeaponType);
    }


    public long geCurrentTimeStamp(int dayCount) {
        return System.currentTimeMillis() + getDayMillseconds(dayCount);
    }

    public long getDayMillseconds(int dayCount) {
        return dayCount * 86400000;
    }

    public RoleDeityWeapon getCurRoleDeityWeapon(){
        if(roleDeityWeaponList != null){
            for(int i = 0, len = roleDeityWeaponList.size(); i<len; i++){
                if(roleDeityWeaponList.get(i).getState() == DeityWeaponConstant.DRESS){
                    return roleDeityWeaponList.get(i);
                }
            }
        }
        return null;
    }

    public int getCurRoleDeityWeapoonId(){
        RoleDeityWeapon roleDeityWeapon = getCurRoleDeityWeapon();
        if(roleDeityWeapon != null){
            return roleDeityWeapon.getDeityweaponId(getJobId());
        }
        return 0;
    }

    public byte getCurRoleDeityWeaponType(){
        RoleDeityWeapon roleDeityWeapon = getCurRoleDeityWeapon();
        if(roleDeityWeapon != null){
            return roleDeityWeapon.getType();
        }
        return -1;
    }

    public int getJobId() {
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        return roleModule.getRoleRow().getJobId();
    }

    public int getRoleLevel(){
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        return roleModule.getRoleRow().getLevel();
    }

    public List<RoleDeityWeapon> getRoleDeityWeaponList() {
        return roleDeityWeaponList;
    }

    //从当前拥有的神兵列表中获取一个神兵;
    public RoleDeityWeapon getRoleDeityWeaponByType(byte deityweaponType) {
        RoleDeityWeapon roleDeityWeapon = null;
        if(roleDeityWeaponList != null){
            for (int i = 0, len = roleDeityWeaponList.size(); i < len; i++) {
                roleDeityWeapon = roleDeityWeaponList.get(i);
                if (roleDeityWeapon != null) {
                    if (roleDeityWeapon.getType() == deityweaponType) {
                        return roleDeityWeapon;
                    }
                }
            }
        }
        return null;
    }


    //当前用户数据是否有对应神兵;
    public RoleDeityWeapon isHasDeityWeapon(byte deityWeaponType) {
        RoleDeityWeapon roleDeityWeapon = getRoleDeityWeaponByType(deityWeaponType);
        return roleDeityWeapon;
    }

    //神兵是否过期了;
    public boolean isDeityWeaponExpired(byte deityWeaponType) {
        RoleDeityWeapon roleDeityWeapon = getRoleDeityWeaponByType(deityWeaponType);
        if (roleDeityWeapon != null) {
            if (roleDeityWeapon.isForever() == false) {
                return geCurrentTimeStamp(0) > roleDeityWeapon.getEndTimestamp();
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 请求激活神兵;
     *
     * @param deityWeaponType
     */
    public boolean requestActive(byte deityWeaponType) {
        boolean isValid = false;
        do {
            //判断是否激活过;
            RoleDeityWeapon roleDeityWeapon = null;
            //TODO 要刷新计时;
            if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) == null) {
                //直接激活;
                addNewDeityWeapon(deityWeaponType);
            } else {
                //是否是永久的;
                if (roleDeityWeapon.isForever()) {
                    break;
                } else {
                    //加长延时;
                    DeityWeaponVo deityWeaponVo = DeityWeaponManager.getDeityWeaponVo(getJobId(), deityWeaponType);
                    //判断是否已经过期了;
                    if(roleDeityWeapon.isExpired()){
                        roleDeityWeapon.setEndTimestamp(geCurrentTimeStamp(deityWeaponVo.getTimelimit()));
                    }else{
                        long addedDayMillseconds = getDayMillseconds(deityWeaponVo.getTimelimit());
                        roleDeityWeapon.setEndTimestamp(roleDeityWeapon.getEndTimestamp() + addedDayMillseconds);
                    }
                    roleDeityWeapon.setState(DeityWeaponConstant.DISDRESS);
                    context().update(roleDeityWeapon);
                    updateAttrAndFightScoreWithSend();
                }
            }
            isValid = true;
        } while (false);
        return isValid;
    }

    /**
     * 请求反激活神兵;
     *
     * @param deityWeaponType
     */
    public void requestDisActive(byte deityWeaponType) {
        //判断是否激活过;
        RoleDeityWeapon roleDeityWeapon = null;
        if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) != null && ((roleDeityWeapon.getState() == DeityWeaponConstant.ACTIVE) || (roleDeityWeapon.getState() == DeityWeaponConstant.DRESS) || (roleDeityWeapon.getState() == DeityWeaponConstant.DISDRESS))) {
            if(roleDeityWeapon.getState() == DeityWeaponConstant.DRESS){
                dispatchEventCurDeityChanged((byte)0);
            }
            roleDeityWeapon.setState(DeityWeaponConstant.INACTIVE);
            context().update(roleDeityWeapon);
            syncToClientOprResult(deityWeaponType, DeityWeaponConstant.INACTIVE);
            updateAttrAndFightScoreWithSend();
        }
    }

    /**
     * 请求分解神兵;
     *
     * @param deityWeaponType
     */
    public void requestDeCompose(byte deityWeaponType, ItemVo itemVo, int count) {
        RoleDeityWeapon roleDeityWeapon = null;
        //确保已经有对应的神兵先;
        if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) != null) {
            ToolModule toolModule = (ToolModule)module(MConst.Tool);
            toolModule.forceResolveTool(0, itemVo.getItemId(), count);
        }
    }

    /**
     * 请求穿戴;
     *
     * @param deityWeaponType
     */
    public void rqeuestDress(byte deityWeaponType, boolean needSyncToClient) {
        RoleDeityWeapon roleDeityWeapon = null;
        //确保已经有对应的神兵先;
        if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) != null) {
            //判断是否穿戴中，是否过期了;
            if (roleDeityWeapon.isExpired()) {
                warn(I18n.get("deity.forge.isexpired"));
            }else {
                if(roleDeityWeapon.getState()!=DeityWeaponConstant.DRESS){
                    //判断其他的神兵是否有穿戴中的,有的话需要脱下先;
                    RoleDeityWeapon otherRoleDeityWeapon = null;
                    for (int i = 0, len = roleDeityWeaponList.size(); i<len; i++){
                        otherRoleDeityWeapon = roleDeityWeaponList.get(i);
                        if(otherRoleDeityWeapon != null && otherRoleDeityWeapon.getType() != roleDeityWeapon.getType()){
                            if(otherRoleDeityWeapon.getState() == DeityWeaponConstant.DRESS){
                                requestDisDress(otherRoleDeityWeapon.getType(), false);
                            }
                        }
                    }
                    roleDeityWeapon.setState(DeityWeaponConstant.DRESS);
                    context().update(roleDeityWeapon);
                    if(needSyncToClient){
                        syncToClientOprResult(deityWeaponType, DeityWeaponConstant.DRESS);
                    }
                    dispatchEventCurDeityChanged(roleDeityWeapon.getType());
                }else{
                    warn(I18n.get("deity.dress.isalreadydressed"));
                    return;
                }
            }
        }
    }

    /**
     * 请求卸下;
     *
     * @param deityWeaponType
     */
    public void requestDisDress(byte deityWeaponType, boolean needSyncToClient) {
        RoleDeityWeapon roleDeityWeapon = null;
        //确保已经有对应的神兵先;
        if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) != null) {
            if(roleDeityWeapon.getState() == DeityWeaponConstant.DRESS){
                roleDeityWeapon.setState(DeityWeaponConstant.DISDRESS);
                context().update(roleDeityWeapon);
                if(needSyncToClient){
                    syncToClientOprResult(deityWeaponType, DeityWeaponConstant.DISDRESS);
                }
                dispatchEventCurDeityChanged((byte)0);
            }else {
                warn(I18n.get("deity.dress.isnotdressed"));
            }
        }else{
            warn(I18n.get("deity.forge.nothistypedeity"));
        }
    }

    /**
     * 请求锻造;
     *
     * @param deityWeaponType
     */
    public void requestForge(byte deityWeaponType) {
        RoleDeityWeapon roleDeityWeapon = null;
        boolean isSuccess = false;
        do{
            //确保已经有对应的神兵先;
            if ((roleDeityWeapon = isHasDeityWeapon(deityWeaponType)) != null) {
                if(roleDeityWeapon.isExpired()){
                    warn(I18n.get("deity.forge.isexpired"));
                    break;
                }
            }else{
                warn(I18n.get("deity.forge.nothistypedeity"));
                break;
            }
            //先获取当前的等级;
            //判断是否是最高级了;
            int maxLevel = DeityWeaponManager.getMaxDeityWeaponLevelVo(deityWeaponType).getDeityweaponlvl();
            if(roleDeityWeapon.getLevel() >= maxLevel){
                warn(I18n.get("deity.forge.maxlevelLimit"));
                break;
            }
            DeityWeaponLevelVo deityWeaponLevelVo = DeityWeaponManager.getDeityWeaponLevelVo(deityWeaponType, roleDeityWeapon.getLevel()+1);
            //判断是否等级限制;
            if(deityWeaponLevelVo.getLevellimit() > getRoleLevel()){
                warn(I18n.get("deity.dress.islevellimit", deityWeaponLevelVo.getLevellimit()));
                break;
            }
            ToolModule toolModule = (ToolModule)module(MConst.Tool);
            //判断材料是否足够先;
//            Map<Integer, Integer> materialMap = deityWeaponLevelVo.getCostMap();
            Map<Integer, Integer> materialMap = deityWeaponLevelVo.getCostByJobId(getJobId());
            if(materialMap.size() > 0){
                long bagMaterialCount;
                int costId = 0;
                int costCount = 0;
                boolean isMaterialEnough = true;
                for(Map.Entry<Integer, Integer> kvp : materialMap.entrySet()){
                    costId =kvp.getKey();
                    costCount = kvp.getValue();
                    bagMaterialCount = toolModule.getCountByItemId(costId);
                    if (costCount > bagMaterialCount) {
                        ItemVo itemVo = ToolManager.getItemVo(costId);
                        if(itemVo != null){
                            warn("commontext_moneyshort", DataManager.getGametext(itemVo.getName()));
                        }
                        isMaterialEnough = false;
                        break;
                    }
                }
                if(!isMaterialEnough){
                    break;
                }
            }
            try {
                for(Map.Entry<Integer, Integer> kvp : materialMap.entrySet()){
                    if (!toolModule.deleteAndSend(kvp.getKey(), kvp.getValue(),EventType.DEITYWEAPON.getCode())) {
                        LogUtil.error("神兵锻造时,删除道具失败: toolModule.deleteAndSend "+kvp.getKey()+" , "+kvp.getValue());
                        break;
                    }
                }
                roleDeityWeapon.setLevel(roleDeityWeapon.getLevel()+1);
                context().update(roleDeityWeapon);
                updateAttrAndFightScoreWithSend();
            }catch (Exception e){
                throw e;
            }
            isSuccess = true;
        }while(false);
        if(isSuccess){
            syncToClientOprResult(deityWeaponType, DeityWeaponConstant.FORGE);
            updateForgeRedPionts();
        }
    }

    public void syncToClientOprResult(byte deityWeaponType, byte oprType, byte errorCode,List<RoleDeityWeapon> list) {
        ClientDeityWeaponOpr clientDeityWeaponOpr = new ClientDeityWeaponOpr();
        clientDeityWeaponOpr.setOprType(oprType);
        clientDeityWeaponOpr.setErrCode(errorCode);
        clientDeityWeaponOpr.setRoleDeityWeaponList(list);
        send(clientDeityWeaponOpr);
    }

    public void syncToClientOprResult(byte deityWeaponType, byte oprType) {
//        ClientDeityWeaponOpr clientDeityWeaponOpr = new ClientDeityWeaponOpr();
//        clientDeityWeaponOpr.setRoleDeityWeaponList(list);
//        send(clientDeityWeaponOpr);
        syncToClientOprResult(deityWeaponType, oprType, (byte)0, roleDeityWeaponList);
        syncToSummary();
    }

    public void dispatchEventCurDeityChanged(byte deityweaponType){
        DeityWeaponChangeEvent deityWeaponChangeEvent = new DeityWeaponChangeEvent(deityweaponType);
        eventDispatcher().fire(deityWeaponChangeEvent);
    }

    public void updateAttrAndFightScoreWithSend(){
        updateAttrAndFightScoreWithSend(true);
    }

    public void updateAttrAndFightScoreWithSend(boolean syncToClient){

        //计算
        Attribute attr = new Attribute();
        RoleDeityWeapon roleDeityWeapon = null;
        DeityWeaponLevelVo deityWeaponLevelVo = null;
        int jobId = getJobId();
        if(roleDeityWeaponList != null){
            for (int i = 0, len = roleDeityWeaponList.size(); i<len; i++){
                roleDeityWeapon = roleDeityWeaponList.get(i);
                if(roleDeityWeapon != null && (roleDeityWeapon.getState() == DeityWeaponConstant.ACTIVE || roleDeityWeapon.getState() == DeityWeaponConstant.DRESS || roleDeityWeapon.getState() == DeityWeaponConstant.DISDRESS)){
                    deityWeaponLevelVo = DeityWeaponManager.getDeityWeaponLevelVo(roleDeityWeapon.getType(), roleDeityWeapon.getLevel());
                    if(deityWeaponLevelVo != null){
                        attr.addAttribute(deityWeaponLevelVo.getAttribute());
                    }
                }
            }
        }
        //更新
        RoleModule roleModule = (RoleModule) module(MConst.Role);
        roleModule.updatePartAttr("deityweapon", attr);
        roleModule.updatePartFightScore("deityweapon", FormularUtils.calFightScore(attr));

        if(syncToClient){
            //发送到客户端
            roleModule.sendRoleAttr();
            roleModule.sendUpdateFightScore();
        }
    }
    
    public Map<Byte, Byte> checkCanForge(){
    	Map<Byte, Byte> redMap = new HashMap<>();
    	for(RoleDeityWeapon weapon : roleDeityWeaponList){
    		byte deityWeaponType = weapon.getType();
    		//判断是否是最高级了;
            int maxLevel = DeityWeaponManager.getMaxDeityWeaponLevelVo(deityWeaponType).getDeityweaponlvl();
            if(weapon.getLevel() >= maxLevel){
            	redMap.put(deityWeaponType, (byte)0);
                continue;
            }
            DeityWeaponLevelVo deityWeaponLevelVo = DeityWeaponManager.getDeityWeaponLevelVo(deityWeaponType, weapon.getLevel()+1);
            //判断是否等级限制;
            if(deityWeaponLevelVo.getLevellimit() > getRoleLevel()){
            	redMap.put(deityWeaponType, (byte)0);
                continue;
            }
            ToolModule toolModule = (ToolModule)module(MConst.Tool);
            //判断材料是否足够先;
            Map<Integer, Integer> materialMap = deityWeaponLevelVo.getCostByJobId(getJobId());
            if(materialMap.size() > 0){
                long bagMaterialCount;
                int costId = 0;
                int costCount = 0;
                boolean isMaterialEnough = true;
                for(Map.Entry<Integer, Integer> kvp : materialMap.entrySet()){
                    costId =kvp.getKey();
                    costCount = kvp.getValue();
                    bagMaterialCount = toolModule.getCountByItemId(costId);
                    if (costCount > bagMaterialCount) {
                        ItemVo itemVo = ToolManager.getItemVo(costId);
                        if(itemVo != null){
//                            warn("commontext_moneyshort", DataManager.getGametext(itemVo.getName()));
                        }
                        isMaterialEnough = false;
                        break;
                    }
                }
                if(isMaterialEnough){
                	redMap.put(deityWeaponType, (byte)1);
                }else{
                	redMap.put(deityWeaponType, (byte)0);
                }
            }
    	}
    	return redMap;
    }
}
