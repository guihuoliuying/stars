package com.stars.modules.newequipment.listener;

import com.stars.core.event.Event;
import com.stars.core.module.AbstractEventListener;
import com.stars.core.module.Module;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentModule;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/22.
 */
public class GetToolEquipmentListener extends AbstractEventListener<Module> {
    public GetToolEquipmentListener(Module module) {
        super(module);
    }

    @Override
    public void onEvent(Event event) {
        AddToolEvent ate = (AddToolEvent)event;
        Map<Integer,Integer> toolMap = ate.getToolMap();
        if (toolMap == null || toolMap.size() <= 0) {
            return;
        }
        Iterator<Integer> iter = toolMap.keySet().iterator();
        int itemId;
        Map<Byte,EquipmentVo> maxFightEquipMap = new HashMap<>();
        EquipmentVo equipmentVo,maxFightEquip = null;
        RoleEquipment roleEquipment;
        NewEquipmentModule module = (NewEquipmentModule)this.module();
        Map<Integer,Integer> effectPlayMap = new HashMap<>();
        List<Integer> noSendList = NewEquipmentManager.getNoWindowTipsList();
        while (iter.hasNext()){
            itemId = iter.next();
            equipmentVo = NewEquipmentManager.getEquipmentVo(itemId);
            if(equipmentVo == null) continue;//不是装备
            if(equipmentVo.getEffectPlay()!=0 && !module.hasPlayEffect(itemId)){
                effectPlayMap.put(itemId,equipmentVo.getEffectPlay());
            }

            if(noSendList.contains(itemId)) continue;//配置的不下发列表

            if(!module.canPutOn(equipmentVo.getEquipId())) continue;//装备穿戴条件检测

            //对应部位已穿戴装备
            roleEquipment = module.getRoleEquipByType(equipmentVo.getType());
            if(roleEquipment==null) continue;

            //基础战力比较
            if(roleEquipment.getBasicFighting() < equipmentVo.getBasicFighting()){
                maxFightEquip = maxFightEquipMap.get(roleEquipment.getType());
                if(maxFightEquip == null || maxFightEquip.getBasicFighting() < equipmentVo.getBasicFighting()){
                    maxFightEquip = equipmentVo;
                    maxFightEquipMap.put(roleEquipment.getType(),maxFightEquip);
                }
            }
        }

        //获得稀有装备特效展示界面
        if(StringUtil.isNotEmpty(effectPlayMap)){
            module.sendEffectPlayList(effectPlayMap);
        }

//        //穿戴装备tips
//        if(StringUtil.isNotEmpty(maxFightEquipMap)){
//            List<EquipmentVo> list = new ArrayList<>(maxFightEquipMap.values());
//            module.sendPutOnNewEquip(list);
//        }

        module.signCalEquipRedPoint();//标识计算装备红点
        module.flushNeedToMarkToClient();//初始化新增装备的角标并同步至客户端
        module.reqCanUpgradeList();//装备升级材料监听
    }
}
