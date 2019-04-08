package com.stars.modules.newequipment.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.NewEquipmentPacketSet;
import com.stars.modules.newequipment.packet.vo.NextStarInfo;
import com.stars.modules.newequipment.packet.vo.NextStrengthInfo;
import com.stars.modules.newequipment.prodata.EquipStarVo;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.newequipment.prodata.NewEquipmentUpgradeVo;
import com.stars.modules.newequipment.userdata.RoleEquipment;
import com.stars.modules.skill.SkillManager;
import com.stars.modules.tool.userdata.ExtraAttrVo;
import com.stars.modules.tool.userdata.RoleTokenEquipmentHolePo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;
import com.stars.util.vowriter.BuffUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/16.
 */
public class ClientNewEquipment extends PlayerPacket {

    public static final byte RESP_SYNC = 0x00; // 同步已穿戴装备数据
    public static final byte RESP_SYNC_OPERATE_INFO = 0x01; // 同步装备操作所需信息(强化/升星)
    public static final byte RESP_UP_STAR_RESULT = 0x02; // 升星结果展示界面
    public static final byte RESP_WASH_RESULT = 0x03; // 洗练结果展示界面
    public static final byte RESP_PUT_ON_NEW_EQUIP_TIPS = 0x04; // 穿戴新装备提示
    public static final byte RESP_PLAY_STRENGTH_SUCCESS_AMJ = 0x05; // 强化成功播放特效
    public static final byte RESP_WATCH_OTHER_EQUIP = 0x06; // 查看他人装备信息
    public static final byte RESP_FLUSH_EQUIP_BAG_MARK = 0x07;  //刷新背包装备角标
    public static final byte RESP_RESOLVE_EQUIP_RESULT = 0x08;  //分解装备结果展示
    public static final byte RESP_EFFECT_PLAY_LIST = 0x09;  //获得稀有装备特效展示界面
    public static final byte RESP_WITCH_EQUIP = 0x0b; //哪一中类型的装备可以强化
    public static final byte RESP_TOKEN_LEVEL_UP_TIPS = 0x0c; //符文装备升级成功提示
    public static final byte RESP_TOKEN_WASH_RESULT = 0x0d; //符文洗练结果（替换前）
    public static final byte RESP_TOKEN_TRANSFER_BACK = 0x0e;//符文转移（？继承）返还
    public static final byte RESP_TOKEN_WASH_REPLACE = 0x0f; //符文洗练替换结果
    public static final byte RESP_TOKEN_MELT_RESULT = 0x10; //符文熔炼结果
    public static final byte RESP_ACTIVE_TOKEN_SKILL = 0x11; //发送新激活的符文技能
    public static final byte RESP_REQ_OPEN_UPGRADE_UI = 0x12; //发送装备升级界面数据
    public static final byte RESP_REQ_OPEN_UPGRADE = 0x13; //发送装备升级结果
    public static final byte RESP_REQ_CAN_UPGRADE_LIST = 0x14; //发送装备升级名单

    public static final byte SYNC_TYPE_ALL = -1;    // 同步全部操作类型
    public static final byte SYNC_TYPE_STRENGTH = 1;// 同步强化信息表
    public static final byte SYNC_TYPE_STAR = 2;    // 同步升星信息

    public static final byte SUCCESS = 1;   //升星成功
    public static final byte FAIL = 2;      //失败降级
    public static final byte SAVE = 3;      //失败保底

    public static final byte FLUSH_TYPE_ALL = 0;    // 刷新角标类型-全部刷新
    public static final byte FLUSH_TYPE_PART = 1;   // 刷新角标类型-按部位刷新
    public static final byte FLUSH_TYPE_ADD = 2;    // 刷新角标类型-新增

    public static final byte MARK_TYPE_NONE = 0;    // 装备角标状态-无
    public static final byte MARK_TYPE_WASH = 1;    // 装备角标状态-可洗练
    public static final byte MARK_TYPE_TRANSFER = 2;  // 装备角标状态-可转移
    public static final byte MARK_TYPE_TOKEN = 3;   //符文装备状态
    public static final byte MARK_TYPE_HIGHQUALITY = 4;//// TODO: 2017-03-23 :高品质
    public static final byte MARK_TYPE_PUT_ON = 5;  // 装备角标状态-可穿戴

    private byte subtype;
    private byte syncType;
    private byte resultType;
    private byte flushType;
    private byte type;
    private byte randomIndex;
    private byte witchEquip;
    private Map<Byte, RoleEquipment> roleEquipMap;
    private List<NextStrengthInfo> nextStrengthList;
    private List<NextStarInfo> nextStarInfoList;

    private EquipStarVo equipStarVo;
    private int equipId;
    private ExtraAttrVo oldExtraAttr;
    private ExtraAttrVo newExtraAttr;
    private List<Byte> strenghtList;
    private RoleEquipment otherEquipment;
    private Map<Long, Integer> diffMap;
    private Map<Long, Byte> markMap;
    private Map<Long, Integer> fightMap;
    private Map<Integer, Integer> resolveMap;
    private Map<Long, Integer> effectPlayMap;
    private byte checkQuickPutOn;   //是否检测快速装备弹窗
    private RoleFashion roleCurrentDressingFashion; //玩家当前穿戴的时装
    private String tokenName; //符文名称
    private int tokenNewLevel; //符文新的等级
    private Map<Byte, RoleTokenEquipmentHolePo> newHolePoMap = new HashMap<>(); //符文洗练的结果缓存
    private int newWashSkillId;  //符文洗练技能的缓存
    private int newWashSkillLevel; //符文洗练技能等级的缓存
    private Map<Integer, Integer> transferBackToolMap;
    private RoleEquipment roleEquipment;
    private List<Byte> equipTypeList; //装备部位
    /**
     * [装备升级]
     */
    private List<NewEquipmentUpgradeVo> equipmentUpgradeVos;
    private boolean includeProduct;
    private List<RoleEquipment> canUpgradeEquipments;
    private Map<Integer, Integer> canUpgradeStatus;//能否升级状态
    private Map<Integer, Integer> upgradeEquipFightScore;//升级后的装备战力
    private boolean success;
    private int oldEquipId;
    private int newEquipId;

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case RESP_SYNC:
                writeSync(buff);
                break;
            case RESP_SYNC_OPERATE_INFO:
                writeSyncOperateInfo(buff);
                break;
            case RESP_UP_STAR_RESULT:
                writeUpStarResult(buff);
                break;
            case RESP_WASH_RESULT:
                writeWashResult(buff);
                break;
            case RESP_PUT_ON_NEW_EQUIP_TIPS:
                if (StringUtil.isEmpty(diffMap)) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) diffMap.size());
                    for (Map.Entry<Long, Integer> entry : diffMap.entrySet()) {
                        buff.writeString(Long.toHexString(entry.getKey()));
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
            case RESP_PLAY_STRENGTH_SUCCESS_AMJ:
                writeStrengthSuccess(buff);
                break;
            case RESP_WATCH_OTHER_EQUIP:
                otherEquipment.writeToBuff(buff);
                break;
            case RESP_FLUSH_EQUIP_BAG_MARK:
                writeMarkMapToBuff(buff);
                break;
            case RESP_RESOLVE_EQUIP_RESULT:
                BuffUtil.writeIntMapToBuff(buff, resolveMap);
                break;
            case RESP_EFFECT_PLAY_LIST:
                if (StringUtil.isEmpty(effectPlayMap)) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) effectPlayMap.size());
                    for (Map.Entry<Long, Integer> entry : effectPlayMap.entrySet()) {
                        buff.writeString(Long.toHexString(entry.getKey()));
                        buff.writeInt(entry.getValue());
                    }
                }
                break;
            case RESP_WITCH_EQUIP:
                buff.writeByte(witchEquip);
                break;
            case RESP_TOKEN_LEVEL_UP_TIPS:
                buff.writeString(tokenName);
                buff.writeInt(tokenNewLevel);
                break;
            case RESP_TOKEN_WASH_RESULT:
                EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(roleEquipment.getEquipId());
                Map<Byte, RoleTokenEquipmentHolePo> roleTokenHoleInfoMap = roleEquipment.getRoleTokenHoleInfoMap();
                int totalHoldCount = NewEquipmentManager.getTokenMaxNumIndex(equipmentVo.getTokenNumIndex());
                buff.writeInt(totalHoldCount); //当前已开启装备符文孔
                for (byte i = 1; i <= totalHoldCount; i++) {
                    RoleTokenEquipmentHolePo holePo = newHolePoMap.get(i); //拿新的
                    if (holePo == null)
                        holePo = roleTokenHoleInfoMap.get(i); //拿玩家当前
                    if (holePo != null) {
                        holePo.writeToBuffer(buff);
                    } else {
                        buff.writeByte((byte) i);
                        buff.writeInt(0);
                        buff.writeInt(0);
                    }
                }

                buff.writeInt(newWashSkillId);
                buff.writeInt(newWashSkillLevel);
                if (newWashSkillId != 0) {
                    int maxSkillLv = SkillManager.getMaxSkillLevel(newWashSkillId);
                    buff.writeInt(maxSkillLv);
                } else {
                    buff.writeInt(0);
                }
                break;
            case RESP_TOKEN_TRANSFER_BACK:
                buff.writeInt(transferBackToolMap.size());
                for (Map.Entry<Integer, Integer> entry : transferBackToolMap.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeInt(entry.getValue());
                }
                break;
            case RESP_TOKEN_WASH_REPLACE:
                buff.writeByte(resultType);
                break;
            case RESP_TOKEN_MELT_RESULT:
                buff.writeInt(resultType);
                break;
            case RESP_ACTIVE_TOKEN_SKILL:
                buff.writeInt(equipTypeList.size());
                for (byte equipType : equipTypeList) {
                    buff.writeByte(equipType);
                }
                break;
            case RESP_REQ_OPEN_UPGRADE_UI: {
                if (includeProduct) {
                    buff.writeInt(1);
                    buff.writeInt(equipmentUpgradeVos.size());
                    for (NewEquipmentUpgradeVo newEquipmentUpgradeVo : equipmentUpgradeVos) {
                        newEquipmentUpgradeVo.writeBuff(buff);
                    }
                } else {
                    buff.writeInt(0);
                }
                buff.writeInt(canUpgradeEquipments.size());
                for (RoleEquipment roleEquipment : canUpgradeEquipments) {
                    buff.writeInt(roleEquipment.getEquipId());
                }
            }
            break;
            case RESP_REQ_OPEN_UPGRADE: {
                buff.writeInt(success ? 1 : 0);
                buff.writeInt(type);//装备位
                buff.writeInt(oldEquipId);//旧装备id
                buff.writeInt(newEquipId);//新装备id
            }
            break;
            case RESP_REQ_CAN_UPGRADE_LIST: {
                buff.writeInt(canUpgradeEquipments.size());
                for (RoleEquipment roleEquipment : canUpgradeEquipments) {
                    buff.writeInt(roleEquipment.getEquipId());
                    if (canUpgradeStatus.get(roleEquipment.getEquipId()) == 1) {
                        buff.writeInt(1);//1表示材料充足
                    } else {
                        buff.writeInt(0);//0表示材料不足
                    }
                    buff.writeInt(upgradeEquipFightScore.get((int)roleEquipment.getType()));
                }
            }
            break;
            default:
                break;

        }
    }

    private void writeMarkMapToBuff(NewByteBuffer buff) {
        buff.writeByte(checkQuickPutOn);
        buff.writeByte(flushType);
        if (flushType == ClientNewEquipment.FLUSH_TYPE_PART) {
            buff.writeByte(type);
        }
        if (StringUtil.isEmpty(markMap)) {
            buff.writeInt(0);
        } else {
            buff.writeInt(markMap.size());
            for (Map.Entry<Long, Byte> entry : markMap.entrySet()) {
                buff.writeString(Long.toHexString(entry.getKey()));
                buff.writeByte(entry.getValue());
            }
        }
        if (StringUtil.isEmpty(fightMap)) {
            buff.writeInt(0);
        } else {
            buff.writeInt(fightMap.size());
            for (Map.Entry<Long, Integer> entry : fightMap.entrySet()) {
                buff.writeString(Long.toHexString(entry.getKey()));
                buff.writeInt(entry.getValue());
            }
        }
    }

    private void writeStrengthSuccess(NewByteBuffer buff) {
        if (StringUtil.isEmpty(strenghtList)) {
            buff.writeByte((byte) 0);
        } else {
            buff.writeByte((byte) strenghtList.size());
            for (Byte type : strenghtList) {
                buff.writeByte(type);
            }
        }
    }

    private void writeWashResult(NewByteBuffer buff) {
        buff.writeByte(resultType);//洗练结果标识: 0新增属性 1洗练到低级属性 2洗练到高级属性
        buff.writeByte(randomIndex);
        if (resultType == 1) {    //洗练到低级属性，直接分解洗练装备
            BuffUtil.writeIntMapToBuff(buff, resolveMap);
        } else {                 //洗练到高级属性,展示洗练结果
            newExtraAttr.writeToBuffer(buff);
            if (resultType != 0) {//0为新增额外属性,没有旧属性
                oldExtraAttr.writeToBuffer(buff);
            }
        }
    }

    private void writeUpStarResult(NewByteBuffer buff) {
        buff.writeByte(resultType);     //升星结果： 1成功 2失败降级 3失败保底
        buff.writeInt(equipId);
        buff.writeInt(equipStarVo.getLevel());
        buff.writeInt(equipStarVo.getEnhanceAttr());
        buff.writeString(equipStarVo.getStarShow());
    }

    private void writeSync(NewByteBuffer buff) {
        byte hasDressingFasion = (byte) 0;
        if (!StringUtil.isEmpty(roleCurrentDressingFashion)) {
            hasDressingFasion = (byte) 1;
        }
        if (StringUtil.isEmpty(roleEquipMap)) {
            buff.writeByte(hasDressingFasion);
        } else {
            buff.writeByte((byte) (roleEquipMap.size() + hasDressingFasion));
            for (RoleEquipment roleEquipment : roleEquipMap.values()) {
                roleEquipment.writeToBuff(buff);
            }
        }
        if (!StringUtil.isEmpty(roleCurrentDressingFashion)) {
            roleCurrentDressingFashion.writeDressedFashionToBuff(buff);
        }
    }

    private void writeSyncOperateInfo(NewByteBuffer buff) {
        buff.writeByte(syncType);   //同步信息类型： -1全部 1强化 2升星
        if (syncType == SYNC_TYPE_ALL || syncType == SYNC_TYPE_STRENGTH) {
            if (StringUtil.isEmpty(nextStrengthList)) {
                buff.writeByte((byte) 0);
            } else {
                buff.writeByte((byte) nextStrengthList.size());
                for (NextStrengthInfo strengthInfo : nextStrengthList) {
                    strengthInfo.writeToBuff(buff);
                }
            }
        }
        if (syncType == SYNC_TYPE_ALL || syncType == SYNC_TYPE_STAR) {
            if (StringUtil.isEmpty(nextStarInfoList)) {
                buff.writeByte((byte) 0);
            } else {
                buff.writeByte((byte) nextStarInfoList.size());
                for (NextStarInfo starInfo : nextStarInfoList) {
                    starInfo.writeToBuff(buff);
                }
            }
        }
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return NewEquipmentPacketSet.C_NEW_EQUIPMENT;
    }

    public ClientNewEquipment() {
    }

    public ClientNewEquipment(byte subtype) {
        this.subtype = subtype;
    }

    public void setRoleEquipMap(Map<Byte, RoleEquipment> roleEquipMap) {
        this.roleEquipMap = roleEquipMap;
    }

    public void setSyncType(byte syncType) {
        this.syncType = syncType;
    }

    public void setNextStrengthList(List<NextStrengthInfo> nextStrengthList) {
        this.nextStrengthList = nextStrengthList;
    }

    public void setNextStarInfoList(List<NextStarInfo> nextStarInfoList) {
        this.nextStarInfoList = nextStarInfoList;
    }

    public void setResultType(byte resultType) {
        this.resultType = resultType;
    }

    public void setEquipId(int equipId) {
        this.equipId = equipId;
    }

    public void setEquipStarVo(EquipStarVo equipStarVo) {
        this.equipStarVo = equipStarVo;
    }

    public void setOldExtraAttr(ExtraAttrVo oldExtraAttr) {
        this.oldExtraAttr = oldExtraAttr;
    }

    public void setNewExtraAttr(ExtraAttrVo newExtraAttr) {
        this.newExtraAttr = newExtraAttr;
    }

    public List<Byte> getStrenghtList() {
        return strenghtList;
    }

    public void setStrenghtList(List<Byte> strenghtList) {
        this.strenghtList = strenghtList;
    }

    public void setOtherEquipment(RoleEquipment otherEquipment) {
        this.otherEquipment = otherEquipment;
    }

    public Map<Long, Integer> getDiffMap() {
        return diffMap;
    }

    public void setDiffMap(Map<Long, Integer> diffMap) {
        this.diffMap = diffMap;
    }

    public byte getFlushType() {
        return flushType;
    }

    public void setFlushType(byte flushType) {
        this.flushType = flushType;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public Map<Long, Byte> getMarkMap() {
        return markMap;
    }

    public void setMarkMap(Map<Long, Byte> markMap) {
        this.markMap = markMap;
    }

    public Map<Integer, Integer> getResolveMap() {
        return resolveMap;
    }

    public void setResolveMap(Map<Integer, Integer> resolveMap) {
        this.resolveMap = resolveMap;
    }

    public void setEffectPlayMap(Map<Long, Integer> effectPlayMap) {
        this.effectPlayMap = effectPlayMap;
    }

    public byte getRandomIndex() {
        return randomIndex;
    }

    public void setRandomIndex(byte randomIndex) {
        this.randomIndex = randomIndex;
    }

    public byte getWitchEquip() {
        return witchEquip;
    }

    public void setWitchEquip(byte witchEquip) {
        this.witchEquip = witchEquip;
    }

    public void setCheckQuickPutOn(byte checkQuickPutOn) {
        this.checkQuickPutOn = checkQuickPutOn;
    }

    public Map<Long, Integer> getFightMap() {
        return fightMap;
    }

    public void setFightMap(Map<Long, Integer> fightMap) {
        this.fightMap = fightMap;
    }

    public RoleFashion getRoleCurrentDressingFashion() {
        return roleCurrentDressingFashion;
    }

    public void setRoleCurrentDressingFashion(RoleFashion roleCurrentDressingFashion) {
        this.roleCurrentDressingFashion = roleCurrentDressingFashion;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public void setTokenNewLevel(int tokenNewLevel) {
        this.tokenNewLevel = tokenNewLevel;
    }

    public int getNewWashSkillLevel() {
        return newWashSkillLevel;
    }

    public void setNewWashSkillLevel(int newWashSkillLevel) {
        this.newWashSkillLevel = newWashSkillLevel;
    }

    public int getNewWashSkillId() {
        return newWashSkillId;
    }

    public void setNewWashSkillId(int newWashSkillId) {
        this.newWashSkillId = newWashSkillId;
    }

    public Map<Byte, RoleTokenEquipmentHolePo> getNewHolePoMap() {
        return newHolePoMap;
    }

    public void setNewHolePoMap(Map<Byte, RoleTokenEquipmentHolePo> newHolePoMap) {
        this.newHolePoMap = newHolePoMap;
    }

    public Map<Integer, Integer> getTransferBackToolMap() {
        return transferBackToolMap;
    }

    public void setTransferBackToolMap(Map<Integer, Integer> transferBackToolMap) {
        this.transferBackToolMap = transferBackToolMap;
    }

    public RoleEquipment getRoleEquipment() {
        return roleEquipment;
    }

    public void setRoleEquipment(RoleEquipment roleEquipment) {
        this.roleEquipment = roleEquipment;
    }

    public List<Byte> getEquipTypeList() {
        return equipTypeList;
    }

    public void setEquipTypeList(List<Byte> equipTypeList) {
        this.equipTypeList = equipTypeList;
    }

    public List<NewEquipmentUpgradeVo> getEquipmentUpgradeVos() {
        return equipmentUpgradeVos;
    }

    public void setEquipmentUpgradeVos(List<NewEquipmentUpgradeVo> equipmentUpgradeVos) {
        this.equipmentUpgradeVos = equipmentUpgradeVos;
    }

    public boolean isIncludeProduct() {
        return includeProduct;
    }

    public void setIncludeProduct(boolean includeProduct) {
        this.includeProduct = includeProduct;
    }

    public List<RoleEquipment> getCanUpgradeEquipments() {
        return canUpgradeEquipments;
    }

    public void setCanUpgradeEquipments(List<RoleEquipment> canUpgradeEquipments) {
        this.canUpgradeEquipments = canUpgradeEquipments;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getOldEquipId() {
        return oldEquipId;
    }

    public void setOldEquipId(int oldEquipId) {
        this.oldEquipId = oldEquipId;
    }

    public int getNewEquipId() {
        return newEquipId;
    }

    public void setNewEquipId(int newEquipId) {
        this.newEquipId = newEquipId;
    }

    public Map<Integer, Integer> getCanUpgradeStatus() {
        return canUpgradeStatus;
    }

    public void setCanUpgradeStatus(Map<Integer, Integer> canUpgradeStatus) {
        this.canUpgradeStatus = canUpgradeStatus;
    }

    public Map<Integer, Integer> getUpgradeEquipFightScore() {
        return upgradeEquipFightScore;
    }

    public void setUpgradeEquipFightScore(Map<Integer, Integer> upgradeEquipFightScore) {
        this.upgradeEquipFightScore = upgradeEquipFightScore;
    }
}