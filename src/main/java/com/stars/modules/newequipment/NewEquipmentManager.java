package com.stars.modules.newequipment;

import com.stars.modules.newequipment.prodata.*;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/10.
 */
public class NewEquipmentManager {


    private static Map<Integer, EquipmentVo> EQUIPMENT_VO_MAP;          //装备产品数据
    private static Map<String, EquipStrengthVo> EQUIP_STRENGTH_VO_MAP;  //装备强化产品数据
    private static Map<String, EquipStarVo> EQUIP_STAR_VO_MAP;          //装备升星产品数据

    private static Map<String, ExtEquipAttrVo> EXT_EQUIP_ATTR_MAP;       //key:type_level_quality,value:attrVo
    private static Map<Byte, List<Integer>> EXT_ATTR_QUALITY_COEFF_MAP;  //key:quality value:scopeList(随机值范围)

    private static List<Integer> NO_WINDOW_TIPS_LIST;   //不下发穿戴提示的装备list

    private static byte MAX_EXTRA_ATTR_NUM; //最大额外属性条目数
    /**
     * 用于转职系统
     * 《jobid，《type，《equiplevel，《color，《changemap，EquipmentVo》》》》》
     */
    public static Map<Byte, Map<Byte, Map<Short, Map<Integer, Map<Integer, EquipmentVo>>>>> jobEquipmentMap;

    private static Map<Integer, TokenVo> TOKEN_VO_MAP; //符文数据
    private static Map<Integer,TokenNumIndexVo> TOKEN_MAX_NUM_INDEX_MAP; //符文索引开放孔位数量
    private static Map<String,TokenLevelVo> TOKEN_LEVEL_VO_MAP; //符文等级数据 key:TokenId_level  value:tokenLevelVo
    private static Map<Integer,TokenSkillVo> TOKEN_SKILL_VO_MAP; //符文技能数据
    private static Map<Long,TokenRandomRangeVo> TOKEN_RANDOM_RANGE_VO_MAP; //符文参数范围数据
    private static Map<Long,TokenWashVo> TOKEN_WASH_VO_MAP; //符文洗练数据
    private static Map<Integer,Integer> TOKEN_LOCK_ITEM_MAP; //符文锁道具

    public static final byte TOKEN_EQUIPMENT_INDICATOR = 1;// 符文装备标识
    public static final byte TOKEN_FIRST_TIME_WASH = 1; //初次洗练
    public static final byte TOKEN_NON_FIRST_TIME_WASH = 2; //非初次洗练
    public static final byte TOKEN_WASH_TOKEN_SKILL = 0; //洗符文技能
    public static final byte TOKEN_WASH_TOKEN = 1; //洗符文
    public static Map<Integer, NewEquipmentUpgradeVo> equipmentUpgradeVoMap;//装备升级


    /**************************以下是对应产品数据的get/set方法*************************/
    private static String getEqxEquipAttrKey(byte type, short level, byte quality) {
        return type + "_" + level + "_" + quality;
    }

    public static String getTypeLevelKey(byte type, int level) {
        return type + "_" + level;
    }

    public static void setEquipmentVoMap(Map<Integer, EquipmentVo> map) {
        EQUIPMENT_VO_MAP = map;
    }

    public static void setEquipStarVoMap(Map<String, EquipStarVo> equipStarVoMap) {
        EQUIP_STAR_VO_MAP = equipStarVoMap;
    }

    public static void setEquipStrengthVoMap(Map<String, EquipStrengthVo> equipStrengthVoMap) {
        EQUIP_STRENGTH_VO_MAP = equipStrengthVoMap;
    }

    public static void setExtEquipAttrMap(HashMap<String, ExtEquipAttrVo> extEquipAttrMap) {
        EXT_EQUIP_ATTR_MAP = extEquipAttrMap;
    }

    public static void setExtAttrQualityCoeffMap(HashMap<Byte, List<Integer>> extAttrQualityCoeffMap) {
        EXT_ATTR_QUALITY_COEFF_MAP = extAttrQualityCoeffMap;
    }

    public static ExtEquipAttrVo getExtEquipAttrVo(EquipmentVo equipment) {
        ItemVo itemVo = ToolManager.getItemVo(equipment.getEquipId());
        if (itemVo == null) return null;//防空
        return getExtEquipAttrVo(equipment.getType(), equipment.getEquipLevel(), itemVo.getColor());
    }

    public static EquipStrengthVo getEquipStrengthVo(String key) {
        if (StringUtil.isEmpty(EQUIP_STRENGTH_VO_MAP)) return null;
        return EQUIP_STRENGTH_VO_MAP.get(key);
    }

    public static Map<Integer, TokenVo> getTokenVoMap() {
        return TOKEN_VO_MAP;
    }

    public static void setTokenVoMap(Map<Integer, TokenVo> tokenVoMap) {
        TOKEN_VO_MAP = tokenVoMap;
    }

    public static TokenVo getTokenVoById(int tokenId){
        return TOKEN_VO_MAP.get(tokenId);
    }
    public static Map<Integer, TokenNumIndexVo> getTokenMaxNumIndexMap() {
        return TOKEN_MAX_NUM_INDEX_MAP;
    }

    public static int getTokenMaxNumIndex(int index){
        return TOKEN_MAX_NUM_INDEX_MAP.get(index).getNumSerial();
    }


    public static void setTokenMaxNumIndexMap(Map<Integer, TokenNumIndexVo> tokenMaxNumIndexMap) {
        TOKEN_MAX_NUM_INDEX_MAP = tokenMaxNumIndexMap;
    }

    public static Map<String, TokenLevelVo> getTokenLevelVoMap() {
        return TOKEN_LEVEL_VO_MAP;
    }

    public static void setTokenLevelVoMap(Map<String, TokenLevelVo> tokenLevelVoMap) {
        TOKEN_LEVEL_VO_MAP = tokenLevelVoMap;
    }

    public static Map<Integer, TokenSkillVo> getTokenSkillVoMap() {
        return TOKEN_SKILL_VO_MAP;
    }

    public static TokenSkillVo getTokenSkillVoBySkillId(int skillId){
        return TOKEN_SKILL_VO_MAP.get(skillId);
    }

    public static void setTokenSkillVoMap(Map<Integer, TokenSkillVo> tokenSkillVoMap) {
        TOKEN_SKILL_VO_MAP = tokenSkillVoMap;
    }

    public static Map<Long, TokenRandomRangeVo> getTokenRandomRangeVoMap() {
        return TOKEN_RANDOM_RANGE_VO_MAP;
    }

    public static void setTokenRandomRangeVoMap(Map<Long, TokenRandomRangeVo> tokenRandomRangeVoMap) {
        TOKEN_RANDOM_RANGE_VO_MAP = tokenRandomRangeVoMap;
    }

    public static TokenRandomRangeVo getTokenRandomRangeVoById(long id){
        return TOKEN_RANDOM_RANGE_VO_MAP.get(id);
    }

    public static Map<Long, TokenWashVo> getTokenWashVoMap() {
        return TOKEN_WASH_VO_MAP;
    }

    public static void setTokenWashVoMap(Map<Long, TokenWashVo> tokenWashVoMap) {
        TOKEN_WASH_VO_MAP = tokenWashVoMap;
    }

    public static TokenWashVo getTokenWashVoById(long id){
        return TOKEN_WASH_VO_MAP.get(id);
    }


    public static boolean isTokenEquipment(int equipmentId){
        EquipmentVo equipmentVo = EQUIPMENT_VO_MAP.get(equipmentId);
        if (equipmentVo == null || equipmentVo.getIsTokenEquip() != TOKEN_EQUIPMENT_INDICATOR)
            return false;
        return true;
    }

    public static TokenLevelVo getTokenLevelVo(String tokenId_Level){
        TokenLevelVo tokenLevelVo = TOKEN_LEVEL_VO_MAP.get(tokenId_Level);
        return tokenLevelVo;
    }

    public static TokenLevelVo getTokenNextLevelVo(TokenLevelVo tokenLevelVo){
        int nextLevel = tokenLevelVo.getLevel() + 1;
        String key = tokenLevelVo.getTokenId() + "_" + nextLevel;
        TokenLevelVo tokenNextLevelVo = TOKEN_LEVEL_VO_MAP.get(key);
        return tokenNextLevelVo;
    }

    public static boolean isTokenMaxLevel(TokenLevelVo tokenLevelVo){
        return getTokenNextLevelVo(tokenLevelVo) == null;
    }

    public static Map<Integer, Integer> getTokenLockItemMap() {
        return TOKEN_LOCK_ITEM_MAP;
    }

    public static void setTokenLockItemMap(Map<Integer, Integer> tokenLockItemMap) {
        TOKEN_LOCK_ITEM_MAP = tokenLockItemMap;
    }


    public static EquipStrengthVo getEquipStrengthVo(RoleEquipment roleEquipment) {
        if (roleEquipment == null || roleEquipment.getStrengthLevel() <= 0) return null;
        if (StringUtil.isEmpty(EQUIP_STRENGTH_VO_MAP)) return null;
        return EQUIP_STRENGTH_VO_MAP.get(getTypeLevelKey(roleEquipment.getType(), roleEquipment.getStrengthLevel()));
    }

    /**
     * 不存在下级强化数据,即为最大强化等级
     */
    public static boolean isMaxEquipStrength(RoleEquipment roleEquipment) {
        return getNextEquipStrengthVo(roleEquipment) == null;
    }

    public static EquipStrengthVo getNextEquipStrengthVo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return null;
        if (StringUtil.isEmpty(EQUIP_STRENGTH_VO_MAP)) return null;
        int nextLevel = roleEquipment.getStrengthLevel() + 1;
        return EQUIP_STRENGTH_VO_MAP.get(getTypeLevelKey(roleEquipment.getType(), nextLevel));
    }

    public static EquipStrengthVo getEquipStrengthVo(byte type, int level) {
        if (StringUtil.isEmpty(EQUIP_STRENGTH_VO_MAP)) return null;
        return EQUIP_STRENGTH_VO_MAP.get(getTypeLevelKey(type, level));
    }

    public static EquipStarVo getEquipStarVo(String key) {
        if (StringUtil.isEmpty(EQUIP_STAR_VO_MAP)) return null;
        return EQUIP_STAR_VO_MAP.get(key);
    }

    /**
     * 不存在下级升星数据,即为最大升星等级
     */
    public static boolean isMaxEquipStar(RoleEquipment roleEquipment) {
        return getNextEquipStarVo(roleEquipment) == null;
    }

    public static EquipStarVo getNextEquipStarVo(RoleEquipment roleEquipment) {
        if (roleEquipment == null) return null;
        if (StringUtil.isEmpty(EQUIP_STAR_VO_MAP)) return null;
        int nextLevel = roleEquipment.getStarLevel() + 1;
        return EQUIP_STAR_VO_MAP.get(getTypeLevelKey(roleEquipment.getType(), nextLevel));
    }

    public static EquipStarVo getEquipStarVo(RoleEquipment roleEquipment) {
        if (roleEquipment == null || roleEquipment.getStarLevel() <= 0) return null;
        if (StringUtil.isEmpty(EQUIP_STAR_VO_MAP)) return null;
        return EQUIP_STAR_VO_MAP.get(getTypeLevelKey(roleEquipment.getType(), roleEquipment.getStarLevel()));
    }

    public static EquipStarVo getEquipStarVo(byte type, int level) {
        if (StringUtil.isEmpty(EQUIP_STAR_VO_MAP)) return null;
        return EQUIP_STAR_VO_MAP.get(getTypeLevelKey(type, level));
    }

    public static EquipmentVo getEquipmentVo(int equipmentId) {
        if (StringUtil.isEmpty(EQUIPMENT_VO_MAP)) return null;
        return EQUIPMENT_VO_MAP.get(equipmentId);
    }

    public static ExtEquipAttrVo getExtEquipAttrVo(byte type, short level, byte quality) {
        if (StringUtil.isEmpty(EXT_EQUIP_ATTR_MAP)) return null;
        return EXT_EQUIP_ATTR_MAP.get(getEqxEquipAttrKey(type, level, quality));
    }

    public static List<Integer> getNoWindowTipsList() {
        return NO_WINDOW_TIPS_LIST;
    }

    public static void setNoWindowTipsList(List<Integer> noWindowTipsList) {
        NO_WINDOW_TIPS_LIST = noWindowTipsList;
    }

    /**
     * 根据品质，获得额外属性随机品质系数范围min,max
     * 用于额外属性的生成
     */
    public static List<Integer> getExtAttrValueByQuality(Byte quality) {
        if (StringUtil.isEmpty(EXT_ATTR_QUALITY_COEFF_MAP)) return null;
        return EXT_ATTR_QUALITY_COEFF_MAP.get(quality);
    }

    public static byte getMaxExtraAttrNum() {
        return MAX_EXTRA_ATTR_NUM;
    }

    public static void setMaxExtraAttrNum(byte maxExtraAttrNum) {
        MAX_EXTRA_ATTR_NUM = maxExtraAttrNum;
    }

    public static Map<Byte, List<Integer>> getExtAttrQualityCoeffMap() {
        return EXT_ATTR_QUALITY_COEFF_MAP;
    }

    /**
     * 对产出进行加成
     * @param targetId:产出的场所id , medalEquipmentId:玩家穿戴的勋章id，-1则没有 , rewardMap:原奖励map
     * 将rewardMap传入此方法后，会自动将rewardMap修改为加成后的rewardMap
     */
//    public static void addProduce(int targetId, int medalEquipmentId, Map<Integer, Integer> rewardMap){
//    	if (rewardMap == null || rewardMap.size() <= 0) {
//			return;
//		}
//    	
//    	EquipmentVo equipmentVo = getEquipmentVo(medalEquipmentId);
//    	if (equipmentVo == null) {
//			return;
//		}
//    	
//    	EquipmentProduceAdd equipmentProduceAdd = equipmentVo.getEquipmentProduceAdd();
//    	int itemId = equipmentProduceAdd.itemId;
//    	if (itemId == -1) {
//			return;
//		}
//    	
//    	boolean isAdd = false;
//    	for (Integer tempTargetId : equipmentProduceAdd.targetIds) {
//			if (targetId == tempTargetId) {
//				isAdd = true;
//				break;
//			}
//		}
//    	
//    	if (!isAdd) {
//			return;
//		}
//    	
//    	if (rewardMap.containsKey(itemId)) {
//			int count = rewardMap.get(itemId);
//			int addCount = (int)(equipmentProduceAdd.percent / 100.0f * count);
//			count += addCount;
//			rewardMap.put(itemId, count);
//		}
//    }

    /**
     * 计算勋章产出进行加成
     *
     * @param targetId:产出的场所id , medalEquipmentId:玩家穿戴的勋章id，-1则没有 , rewardMap:原奖励map
     * @return 加成的奖励map
     */
    public static Map<Integer, Integer> calMedalAddReward(int targetId, int medalEquipmentId, Map<Integer, Integer> rewardMap) {
        Map<Integer, Integer> addItemMap = new HashMap<Integer, Integer>();

        if (rewardMap == null || rewardMap.size() <= 0) return addItemMap;

        if (medalEquipmentId == -1) return addItemMap;

        EquipmentVo equipmentVo = getEquipmentVo(medalEquipmentId);
        if (equipmentVo == null) return addItemMap;

        EquipmentProduceAdd equipmentProduceAdd = equipmentVo.getEquipmentProduceAdd();
        int itemId = equipmentProduceAdd.itemId;
        if (itemId == -1) return addItemMap;

        boolean isAdd = false;
        for (Integer tempTargetId : equipmentProduceAdd.targetIds) {
            if (targetId == tempTargetId) {
                isAdd = true;
                break;
            }
        }

        if (!isAdd) return addItemMap;

        if (rewardMap.containsKey(itemId)) {
            int count = rewardMap.get(itemId);
            int addCount = (int) (equipmentProduceAdd.percent / 100.0f * count);
            addItemMap.put(itemId, addCount);
        }

        return addItemMap;
    }

    /**
     * 通过新职业和装备id转换获取新职业装备
     *
     * @param jobId
     * @param equipId
     * @return
     */
    public static EquipmentVo getNewJobEquipmentVo(int jobId, int equipId) {
        Map<Integer, Map<Integer, EquipmentVo>> colorEquipMap = null;
        EquipmentVo equipmentVo = null;
        byte b_jobId = (byte) jobId;
        equipmentVo = NewEquipmentManager.getEquipmentVo(equipId);
        if (equipmentVo == null) {
            return null;
        }
        Map<Byte, Map<Short, Map<Integer, Map<Integer, EquipmentVo>>>> typeEquipMap = NewEquipmentManager.jobEquipmentMap.get(b_jobId);
        Map<Short, Map<Integer, Map<Integer, EquipmentVo>>> equipLvMap = typeEquipMap.get(equipmentVo.getType());
        colorEquipMap = equipLvMap.get(equipmentVo.getEquipLevel());
        EquipmentVo newEquipmentVo = colorEquipMap.get(equipmentVo.getColor()).get(equipmentVo.getChangeMap());
        if (newEquipmentVo == null) {
            LogUtil.error("找不到平行装备：{}：职业，{}:装备id", jobId, equipId);
        }
        return newEquipmentVo;
    }
}
