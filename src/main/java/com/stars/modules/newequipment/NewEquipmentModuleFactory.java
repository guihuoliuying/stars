package com.stars.modules.newequipment;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.changejob.event.ChangeJobEvent;
import com.stars.modules.data.DataManager;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.newequipment.listener.*;
import com.stars.modules.newequipment.prodata.*;
import com.stars.modules.newequipment.summary.NewEquipmentSummaryComponentImpl;
import com.stars.modules.role.event.RoleLevelUpEvent;
import com.stars.modules.tool.event.AddToolEvent;
import com.stars.modules.tool.event.UseToolEvent;
import com.stars.services.summary.Summary;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.stars.modules.data.DataManager.commonConfigMap;

/**
 * 装备的模块工厂;
 * Created by wuyuxing on 2016/11/10
 */
public class NewEquipmentModuleFactory extends AbstractModuleFactory<NewEquipmentModule> {

    public NewEquipmentModuleFactory() {
        super(new NewEquipmentPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        //初始化装备数据
        initEquipmentProductData();
        //初始化升星数据
        initEquipmentStarProductData();
        //初始化强化数据
        initEquipmentStrengthProductData();
        //初始化额外属性数据
        initExtAttrVo();
        //初始化符文装备数据
        initTokenEquipmentVo();
        //初始化配置数据
        initConfig();
        //装备升级
        initEquipmentUpgrade();
    }

    private void initEquipmentUpgrade() {
        String sql = "select * from newequipupgrade";
        try {
            Map<Integer, NewEquipmentUpgradeVo> equipmentUpgradeVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "nowequipid", NewEquipmentUpgradeVo.class, sql);
            NewEquipmentManager.equipmentUpgradeVoMap=equipmentUpgradeVoMap;
        } catch (SQLException e) {
            LogUtil.error("装备升级数据加载失败", e);
            throw new RuntimeException(e);
        }
    }

    private void initConfig() throws Exception {
        String equipment_nowindowtips = com.stars.util.MapUtil.getString(commonConfigMap, "equipment_nowindowtips", "100125+200125+300125");
        List<Integer> list = StringUtil.toArrayList(equipment_nowindowtips, Integer.class, '+');

        //解析配置数据:    额外属性根据品质的随机值范围
        HashMap<Byte, List<Integer>> coeff_map = new HashMap<>();
        String equip_extattr_qualitycoStr = MapUtil.getString(commonConfigMap, "equip_extattr_qualitycoeff", "1+10+20,2+21+30,3+31+60,4+61+90,5+91+120");
        String[] array = equip_extattr_qualitycoStr.split(",");
        String[] arr;
        List<Integer> scopeList;
        for (String tmp : array) {
            arr = tmp.split("\\+");     //quality,mi
            scopeList = new ArrayList<>();
            scopeList.add(Integer.parseInt(arr[1]));
            scopeList.add(Integer.parseInt(arr[2]));
            coeff_map.put(Byte.parseByte(arr[0]), scopeList);
        }
        Byte maxNum = DataManager.getCommConfig("equip_extattrnum_max", (byte) 0);

        NewEquipmentManager.setNoWindowTipsList(list);
        NewEquipmentManager.setExtAttrQualityCoeffMap(coeff_map);
        NewEquipmentManager.setMaxExtraAttrNum(maxNum);
    }

    //初始化额外属性数据
    private void initExtAttrVo() throws SQLException {
        String sql = "select * from extequipattr";
        List<ExtEquipAttrVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, ExtEquipAttrVo.class, sql);
        HashMap<String, ExtEquipAttrVo> map = new HashMap<>();
        for (ExtEquipAttrVo vo : list) {
            map.put(vo.getKey(), vo);
        }
        NewEquipmentManager.setExtEquipAttrMap(map);
    }

    //初始化装备数据
    private void initEquipmentProductData() throws SQLException {
        String sql = "select * from newequipment";
        Map<Integer, EquipmentVo> equipmentVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "equipid", EquipmentVo.class, sql);
        NewEquipmentManager.setEquipmentVoMap(equipmentVoMap);
        /**
         * 转职平行装备
         * 《jobid，《type，《equiplevel，《color，《changemap,EquipmentVo》》》》》
         */
        Map<Byte, Map<Byte, Map<Short, Map<Integer, Map<Integer, EquipmentVo>>>>> jobEquipmentMap = new HashMap<>();
        for (Map.Entry<Integer, EquipmentVo> entry : equipmentVoMap.entrySet()) {
            EquipmentVo equipmentVo = entry.getValue();
            Map<Byte, Map<Short, Map<Integer, Map<Integer, EquipmentVo>>>> typeEquipMap = jobEquipmentMap.get(equipmentVo.getJob());
            if (typeEquipMap == null) {
                typeEquipMap = new HashMap<>();
                jobEquipmentMap.put(equipmentVo.getJob(), typeEquipMap);
            }
            Map<Short, Map<Integer, Map<Integer, EquipmentVo>>> equipLevelMap = typeEquipMap.get(equipmentVo.getType());
            if (equipLevelMap == null) {
                equipLevelMap = new HashMap<Short, Map<Integer, Map<Integer, EquipmentVo>>>();
                typeEquipMap.put(equipmentVo.getType(), equipLevelMap);
            }
            Map<Integer, Map<Integer, EquipmentVo>> colorEquipMap = equipLevelMap.get(equipmentVo.getEquipLevel());
            if (colorEquipMap == null) {
                colorEquipMap = new HashMap<>();
                equipLevelMap.put(equipmentVo.getEquipLevel(), colorEquipMap);
            }
            Map<Integer, EquipmentVo> changeMapEquipMap = colorEquipMap.get(equipmentVo.getColor());
            if (changeMapEquipMap == null) {
                changeMapEquipMap = new HashMap<>();
                colorEquipMap.put(equipmentVo.getColor(), changeMapEquipMap);
            }
            changeMapEquipMap.put(equipmentVo.getChangeMap(), equipmentVo);
        }
        NewEquipmentManager.jobEquipmentMap = jobEquipmentMap;
    }

    private void initEquipmentStarProductData() throws SQLException {
        String sql = "select * from newequipstar";
        List<EquipStarVo> equipStarVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, EquipStarVo.class, sql);
        HashMap<String, EquipStarVo> equipmentStarMap = new HashMap<>();
        for (EquipStarVo itemVo : equipStarVoList) {
            equipmentStarMap.put(NewEquipmentManager.getTypeLevelKey(itemVo.getType(), itemVo.getLevel()), itemVo);
        }
        NewEquipmentManager.setEquipStarVoMap(equipmentStarMap);
    }

    private void initEquipmentStrengthProductData() throws SQLException {
        String sql = "select * from newequipstrength";
        List<EquipStrengthVo> equipStrengthVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, EquipStrengthVo.class, sql);
        HashMap<String, EquipStrengthVo> equipmentStrengthMap = new HashMap<>();
        for (EquipStrengthVo itemVo : equipStrengthVoList) {
            equipmentStrengthMap.put(NewEquipmentManager.getTypeLevelKey(itemVo.getType(), itemVo.getLevel()), itemVo);
        }
        NewEquipmentManager.setEquipStrengthVoMap(equipmentStrengthMap);
    }

    //初始化符文产品数据
    private void initTokenEquipmentVo() throws SQLException {
        //加载符文表
        String sql = "select * from token";
        Map<Integer, TokenVo> tokenVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "tokenid", TokenVo.class, sql);
        NewEquipmentManager.setTokenVoMap(tokenVoMap);

        //加载符文索引表
        sql = "select * from tokennumindex";
        List<TokenNumIndexVo> tokenNumIndexVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, TokenNumIndexVo.class, sql);
        HashMap<Integer, TokenNumIndexVo> tokenMaxNumIndexMap = new HashMap<>(); //存在每个索引的最大数
        for (TokenNumIndexVo tokenNumIndexVo : tokenNumIndexVoList) {
            TokenNumIndexVo tempTokenIndexVo = tokenMaxNumIndexMap.get(tokenNumIndexVo.getNumIndexId());
            if (tempTokenIndexVo != null && tempTokenIndexVo.getNumIndexId() > tokenNumIndexVo.getNumSerial()) //非最大的忽略
                continue;

            tokenMaxNumIndexMap.put(tokenNumIndexVo.getNumIndexId(), tokenNumIndexVo); //最大值则为该索引下开放的符文孔数
        }
        NewEquipmentManager.setTokenMaxNumIndexMap(tokenMaxNumIndexMap);

        //加载符文等级表
        sql = "select * from tokenlevel";
        Map<String, TokenLevelVo> tokenLevelVoMap = new HashMap<>();
        List<TokenLevelVo> tokenLevelVoList = DBUtil.queryList(DBUtil.DB_PRODUCT, TokenLevelVo.class, sql);
        for (TokenLevelVo tokenLevelVo : tokenLevelVoList) {
            String key = tokenLevelVo.getTokenId() + "_" + tokenLevelVo.getLevel();
            tokenLevelVoMap.put(key, tokenLevelVo);
        }
        NewEquipmentManager.setTokenLevelVoMap(tokenLevelVoMap);

        //加载符文技能表
        sql = "select * from tokenskill";
        Map<Integer, TokenSkillVo> tokenSkillVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "tokenskillid", TokenSkillVo.class, sql);
        NewEquipmentManager.setTokenSkillVoMap(tokenSkillVoMap);

        //加载符文参数范围表
        sql = "select * from tokenrandomrange";
        Map<Long, TokenRandomRangeVo> tokenRandomRangeVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "randomrangeid", TokenRandomRangeVo.class, sql);
        NewEquipmentManager.setTokenRandomRangeVoMap(tokenRandomRangeVoMap);

        //加载符文洗练表
        sql = "select * from tokenwash";
        Map<Long, TokenWashVo> tokenWashVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "tokenwashid", TokenWashVo.class, sql);
        NewEquipmentManager.setTokenWashVoMap(tokenWashVoMap);


        //加载符文锁数据
        Map<Integer, Integer> lockItemMap = new HashMap<>();
        String tokenWashLockStr = DataManager.getCommConfig("tokenwash_lockitem");
        if (StringUtil.isEmpty(tokenWashLockStr)) {
            throw new IllegalArgumentException("初始符文洗练锁配置错误,请检查[commondefine]表[tokenwash_lockitem]字段");
        }
        lockItemMap = StringUtil.toMap(tokenWashLockStr, Integer.class, Integer.class, '+', ',');
        NewEquipmentManager.setTokenLockItemMap(lockItemMap);

    }

    @Override
    public void init() {
        Summary.regComponentClass(SummaryConst.C_NEW_EQUIPMENT, NewEquipmentSummaryComponentImpl.class);
    }

    @Override
    public NewEquipmentModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        return new NewEquipmentModule(id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(AddToolEvent.class, new GetToolEquipmentListener(module));
        eventDispatcher.reg(RoleLevelUpEvent.class, new NewEquipLevelUpListener(module));
        eventDispatcher.reg(UseToolEvent.class, new UseToolListener((NewEquipmentModule) module));
        eventDispatcher.reg(ForeShowChangeEvent.class, new ForeShowChangelListener((NewEquipmentModule) module));
        eventDispatcher.reg(ChangeJobEvent.class, new NewEquipChangeJobListenner((NewEquipmentModule) module));
    }

}
