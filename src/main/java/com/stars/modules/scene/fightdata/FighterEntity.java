package com.stars.modules.scene.fightdata;

import com.stars.core.attr.Attribute;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.*;

/**
 * 组装FighterEntity尽量使用FighterCreator
 * 约定：
 * 1.实体唯一Id生成：角色:roleId;怪物:"m"+生成唯一Id;伙伴:"b"+roleId+伙伴配置Id;机器人:"r"+机器人配置Id
 * Created by liuyuheng on 2016/8/29.
 */
public class FighterEntity implements Cloneable {
    /**
     * 常量
     */
    // 战斗实体类型
    public static byte TYPE_SELF = 0;// 自身
    public static byte TYPE_PLAYER = 1;// 其他玩家
    public static byte TYPE_MONSTER = 2;// 怪物
    public static byte TYPE_NPC = 3;// npc
    public static byte TYPE_BUDDY = 4;// 伙伴
    public static byte TYPE_ROBOT = 5;  // 机器人

    // 阵营
    public static byte CAMP_NEUTRAL = 0;// 中立
    public static byte CAMP_SELF = 1;// 我方
    public static byte CAMP_ENEMY = 2;// 敌方
    // 预加载经验&战力的等级个数
    public static byte NEXT_LEVEL_DATA_NUM = 5;

    public byte fighterType;// 战斗实体类型
    public String uniqueId;// 唯一Id;角色:roleId;怪物:"m"+生成唯一Id;伙伴:roleId+伙伴配置Id;
    private String name;// 名字
    private short level;// 当前等级
    private int exp;// 当前经验
    private int fightScore;// 当前战力
    private int reqExp;// 升级所需经验
    private Map<Integer, Integer> nextLevelExp = new HashMap<>();// 其后的等级&所需经验,<level, reqExp>
    private Map<Integer, Integer> nextLevelFightScore = new HashMap<>();// 其后的等级&增加的战力,<level, addFightScore>
    private byte camp;// 阵营
    private int modelId;// 模型Id;角色对应resourceId,怪物对应monsterId,伙伴对应monsterId
    private short scale;// 模型缩放
    private String position;// '坐标位置'
    private short rotation;// 朝向
    private String awake;// 激活条件
    private String talk;// 说话:伙伴对应follow字段,
    private short resurgence;// 复活时间
    private short fightArea;// 攻击半径
    private short hitSize;// 受击半径
    private short moveSpeed;// 移动速度
    private Attribute attribute;// 属性
    private Map<Integer, Integer> skills;// skillId-level
    private Map<Integer, Integer> skillDamageMap = new HashMap<>();// 技能伤害(角色) skillId-value
    private Map<Integer, Integer> dropMap = new HashMap<>();// 掉落(合并后)
    private List<Map<Integer, Integer>> dropMapList = new LinkedList<>();// 掉落(未合并)，用于下发客户端表现
    private Map<Integer, String> trumpSkillAttr = new HashMap<>();//法宝被动技能附带属性
    private int curDeityWeapon;// 当前使用神兵

    private String extraValue = "";// 扩展字符串

    private String serverName;  // 服务器名字，表示战斗实体的服务器

    /* 怪物扩展 */
    private int monsterAttrId;// 配置Id monsterAttrId
    private int spawnConfigId;// 配置刷怪组Id
    //    private int spawnDelay;// 延迟刷出时间
//    private byte monsterType;// 怪物类型;0=小怪 1=BOSS
    private String spawnUId;// 所属唯一刷怪组Id(内存使用)

    /* 伙伴扩展 */
    private String masterUId;// 所属主人唯一Id(roleId)
    /* 状态,可以根据业务进行使用;*/
    private byte state = 0;
    /* 是否是机器人,默认为false*/
    private boolean isRobot = false;

    /* 符文装备技能外显*/
    private List<String> dragonBallIdList;

    public FighterEntity() {

    }

    public FighterEntity(byte fighterType, String uniqueId) {
        this.fighterType = fighterType;
        this.uniqueId = uniqueId;
        attribute = new Attribute();
        skills = new HashMap<>();
    }

    public FighterEntity copy() {
        try {
            FighterEntity copy = (FighterEntity) this.clone();
            copy.setAttribute(attribute.clone());
            return copy;
        } catch (CloneNotSupportedException e) {
            LogUtil.error("FighterEntity克隆失败", e);
        }
        return null;
    }

    public void writeToBuff(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(fighterType);
        buff.writeString(uniqueId);
        buff.writeString(name);
        buff.writeShort(level);
        buff.writeInt(exp);
        buff.writeInt(reqExp);
        buff.writeInt(fightScore);
        buff.writeByte(camp);
        buff.writeInt(modelId);
        buff.writeShort(scale);
        buff.writeString(position);
        buff.writeShort(rotation);
        buff.writeString(awake);
        buff.writeString(talk);
        buff.writeShort(resurgence);
        buff.writeShort(fightArea);
        buff.writeShort(hitSize);
        buff.writeShort(moveSpeed);
        buff.writeInt(curDeityWeapon);
        attribute.writeToBuffer(buff);
        byte size = (byte) (skills == null ? 0 : skills.size());
        buff.writeByte(size);
        if (size > 0) {
            for (Map.Entry<Integer, Integer> entry : skills.entrySet()) {
                buff.writeInt(entry.getKey());// skillId
                buff.writeShort(entry.getValue().shortValue());// level
                buff.writeInt(!skillDamageMap.containsKey(entry.getKey()) ? 0 : skillDamageMap.get(entry.getKey()));
                buff.writeString(!trumpSkillAttr.containsKey(entry.getKey()) ? "0" : trumpSkillAttr.get(entry.getKey()));
            }
        }
        // 下发掉落
        size = 0;
        for (Map<Integer, Integer> map : dropMapList) {
            size = (byte) (size + map.size());
        }
        buff.writeByte(size);
        if (size > 0) {
            for (Map<Integer, Integer> map : dropMapList) {
                for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                    buff.writeInt(entry.getKey());
                    buff.writeShort(entry.getValue().shortValue());
                }
            }
        }
        size = (byte) (nextLevelExp == null ? 0 : nextLevelExp.size());
        buff.writeByte(size);
        for (Map.Entry<Integer, Integer> entry : nextLevelExp.entrySet()) {
            buff.writeShort(entry.getKey().shortValue());
            buff.writeInt(entry.getValue());
            buff.writeInt(nextLevelFightScore.containsKey(entry.getKey()) ? nextLevelFightScore.get(entry.getKey()) : 0);
        }
        buff.writeString(extraValue);
        if (dragonBallIdList != null) {
            int idSize = dragonBallIdList.size();
            buff.writeByte((byte) idSize);
            if (idSize <= 0)
                return;
            for (String dragonBallId : dragonBallIdList) {
                buff.writeString(dragonBallId);
            }
        } else {
            buff.writeByte((byte)0);
        }


    }

    public void readFromBuff(NewByteBuffer buff) {
        name = buff.readString();
        level = buff.readShort();
        exp = buff.readInt();
        reqExp = buff.readInt();
        fightScore = buff.readInt();
        camp = buff.readByte();
        modelId = buff.readInt();
        scale = buff.readShort();
        position = buff.readString();
        rotation = buff.readShort();
        awake = buff.readString();
        talk = buff.readString();
        resurgence = buff.readShort();
        fightArea = buff.readShort();
        hitSize = buff.readShort();
        moveSpeed = buff.readShort();
        curDeityWeapon = buff.readInt();
        attribute.readFightAtrFromBuffer(buff);
        byte size = buff.readByte();
        if (size > 0) {
            int skillid;
            short level;
            int damage;
            String skillAttr;
            for (byte index = 0; index < size; index++) {
                skillid = buff.readInt();
                level = buff.readShort();
                damage = buff.readInt();
                skillAttr = buff.readString();
                skills.put(skillid, (int)level);
                skillDamageMap.put(skillid, damage);
                trumpSkillAttr.put(skillid, skillAttr);
            }
        }
        size = buff.readByte();
        if (size > 0) {
            int itemId;
            short number;
            for (byte index = 0; index < size; index++) {
                itemId = buff.readInt();
                number = buff.readShort();
                Map<Integer, Integer> dropMap = new HashMap<>();
                dropMap.put(itemId, (int)number);
                dropMapList.add(dropMap);
            }
        }
        size = buff.readByte();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                short level = buff.readShort();
                int reqExp = buff.readInt();
                int fightScore = buff.readInt();
                nextLevelExp.put((int)level, reqExp);
                nextLevelFightScore.put((int)level, fightScore);
            }
        }
        extraValue = buff.readString();
        size = buff.readByte();
        if (size > 0) {
            if (dragonBallIdList == null) {
                dragonBallIdList = new ArrayList<>();
            }
            for (int i = 0; i < size; i++) {
                String dragonBallId = buff.readString();
                dragonBallIdList.add(dragonBallId);
            }
        }
    }

    /**
     * 改变血量
     *
     * @param value 传入值自带符号
     */
    public void changeHp(int value) {
        int result = attribute.getHp() + value;
        if (result < 0) {
            attribute.setHp(Math.max(0, result));
        } else {
            attribute.setHp(Math.min(attribute.getMaxhp(), result));
        }
    }

    /**
     * 是否死亡(hp==0)
     *
     * @return
     */
    public boolean isDead() {
        return attribute.getHp() <= 0;
    }

    public void addExtraValue(String extra) {
        if (extraValue == null) extraValue = "";
        extraValue = extraValue + extra;
    }

    public Map<Integer, Integer> getSkills() {
        return skills;
    }

    public byte getFighterType() {
        return fighterType;
    }

    public void setFighterType(byte type) {
        fighterType = type;
    }

    public int getModelId() {
        return modelId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevel(int level) {
        this.level = (short)level;
    }

    public void setCamp(byte camp) {
        this.camp = camp;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public void setScale(int scale) {
        this.scale = (short)scale;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setRotation(int rotation) {
        this.rotation = (short)rotation;
    }

    public void setAwake(String awake) {
        this.awake = awake;
    }

    public void setTalk(String talk) {
        this.talk = talk;
    }

    public void setSkills(Map<Integer, Integer> skills) {
        this.skills = skills;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public void setResurgence(int resurgence) {
        this.resurgence = (short)resurgence;
    }

    public void setFightArea(int fightArea) {
        this.fightArea = (short)fightArea;
    }

    public void setDropMapList(List<Map<Integer, Integer>> dropMapList) {
        this.dropMapList = dropMapList;
        for (Map<Integer, Integer> map : dropMapList) {
//            StringUtil.combineIntegerMap(dropMap, map);
            MapUtil.add(dropMap, map);
        }
    }

    public void setSkillDamageMap(Map<Integer, Integer> skillDamageMap) {
        this.skillDamageMap = skillDamageMap;
    }

    public void setExtraValue(String extraValue) {
        this.extraValue = extraValue;
    }

    public String getExtraValue() {
        return extraValue;
    }

    public void setHitSize(int hitSize) {
        this.hitSize = (short)hitSize;
    }

    public void setMoveSpeed(int moveSpeed) {
        this.moveSpeed = (short)moveSpeed;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public long getRoleId() {
        return Long.parseLong(uniqueId);
    }

    public int getMonsterAttrId() {
        return monsterAttrId;
    }

    public void setMonsterAttrId(int monsterAttrId) {
        this.monsterAttrId = monsterAttrId;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public byte getCamp() {
        return camp;
    }

    public int getSpawnConfigId() {
        return spawnConfigId;
    }

    public void setSpawnConfigId(int spawnConfigId) {
        this.spawnConfigId = spawnConfigId;
    }

    public String getSpawnUId() {
        return spawnUId;
    }

    public void setSpawnUId(String spawnUId) {
        this.spawnUId = spawnUId;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setFightScore(int fightScore) {
        this.fightScore = fightScore;
    }

    public void setReqExp(int reqExp) {
        this.reqExp = reqExp;
    }

    public void setNextLevelExp(Map<Integer, Integer> nextLevelExp) {
        this.nextLevelExp = nextLevelExp;
    }

    public void setNextLevelFightScore(Map<Integer, Integer> nextLevelFightScore) {
        this.nextLevelFightScore = nextLevelFightScore;
    }

    public String getMasterUId() {
        return masterUId;
    }

    public void setMasterUId(String masterUId) {
        this.masterUId = masterUId;
    }

    public Map<Integer, Integer> getDropMap() {
        return dropMap;
    }

    public String getPosition() {
        return position;
    }

    public int getFightScore() {
        return fightScore;
    }

    public Map<Integer, String> getTrumpSkillAttr() {
        return trumpSkillAttr;
    }

    public void setTrumpSkillAttr(Map<Integer, String> trumpSkillAttr) {
        this.trumpSkillAttr = trumpSkillAttr;
    }

    public byte getState() {
        return state;
    }

    public void setState(byte state) {
        this.state = state;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public int getCurDeityWeapon() {
        return curDeityWeapon;
    }

    public void setCurDeityWeapon(int curDeityWeapon) {
        this.curDeityWeapon = curDeityWeapon;
    }

    public boolean getIsRobot() {
        return isRobot;
    }

    public void setIsRobot(boolean isRobot) {
        this.isRobot = isRobot;
    }

    public List<String> getDragonBallIdList() {
        return dragonBallIdList;
    }

    public void setDragonBallIdList(List<String> dragonBallIdList) {
        this.dragonBallIdList = dragonBallIdList;
    }

    @Override
    public String toString() {
        return "fighterId:" + this.uniqueId;
    }
}
