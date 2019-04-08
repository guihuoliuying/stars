package com.stars.modules.tool.func.impl;

import com.stars.core.module.Module;
import com.stars.modules.MConst;
import com.stars.modules.deityweapon.DeityWeaponModule;
import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.ToolFuncResult;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.Map;

/**
 * 激活神兵道具;
 * Created by panzhenfeng on 2016/12/6.
 */
public class ActiveDeityWeaponFunc  extends ToolFunc {
    private byte deityweaponType;
    public ActiveDeityWeaponFunc(ItemVo itemVo) {
        super(itemVo);
    }

    @Override
    public void parseData(String function) {
        // TODO Auto-generated method stub
        //格式11|buffId+buffLevel|文本索引; (注意:这里的类型为保留字段, 暂时没用到)
        if(StringUtil.isNotEmpty(function) && !function.equals("0")){
            String[] args = function.split("\\|");
            deityweaponType = Byte.parseByte(args[1]);
        }
    }

    @Override
    public ToolFuncResult check(Map<String, Module> moduleMap, int count,
                                Object... args) {
        ToolFuncResult tr = super.useCondition(moduleMap, args);
        if (tr == null) {
            tr = new ToolFuncResult(true, null);
        }
        return tr;
    }

    @Override
    public Map<Integer,Integer> use(Map<String, Module> moduleMap, int count, Object... args) {
        DeityWeaponModule deityWeaponModule = (DeityWeaponModule)moduleMap.get(MConst.Deity);
        boolean isActiveSuccess = deityWeaponModule.requestActive(deityweaponType);
        if(isActiveSuccess == false){
//            deityWeaponModule.requestDeCompose(deityweaponType, itemVo(), count);

            //                if(oprState == DeityWeaponConstant.OPR_STATE_FORCE){
//                    requestDeCompose(deityWeaponType);
//                }else{
//                    syncToClientOprResult(deityWeaponType, DeityWeaponConstant.ACTIVE, oprState, (byte)1);
//                }
        }else{
            //判断是否有穿戴神兵了,如果没有的话，就穿戴神兵;
            if(null == deityWeaponModule.getCurRoleDeityWeapon()){
                deityWeaponModule.rqeuestDress(deityweaponType, true);
            }
        }
        LogUtil.info("神兵|使用道具|count:{}", count);
        return null;
    }

    @Override
    public int canAutoUse(Map<String, Module> moduleMap, int count) {
        DeityWeaponModule module = (DeityWeaponModule) moduleMap.get(MConst.Deity);
        RoleDeityWeapon roleDeityWeapon = module.getRoleDeityWeaponByType(deityweaponType);
        return roleDeityWeapon == null || !roleDeityWeapon.isForever() ? 1 : 0;
    }
}

