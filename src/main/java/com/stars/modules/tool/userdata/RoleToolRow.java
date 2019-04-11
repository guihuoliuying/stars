package com.stars.modules.tool.userdata;

import com.stars.core.attr.Attribute;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by zhangjiahua on 2016/2/23.
 */
public class RoleToolRow extends DbRow implements Comparable {

    //道具对象,所有的道具在数据库中存储都使用这个对象
    /*******************数据库中的持久化数据********************/
    private long toolId;//道具id,全局唯一
    private long roleId;//道具所述玩家id
    private int itemId;//道具的配置id,如果是装备则是装备id
    private int count;//数量
    private String maker;//制造者,制造模块用
    private byte newFlag = 0;//0:不显示新标签  1:显示新标签
    private long bornTime;
    //    private String extraAttrStr;    //附加属性字符串
    private Map<Byte, ExtraAttrVo> extraAttrMap; //附加属性,key:index value:attrVo
    private String tokenHoleStr; //符文装备上的符文孔信息
    private String tokenSkillStr; //符文装备上的符文技能信息
    private Map<Byte, RoleTokenEquipmentHolePo> roleTokenHoleInfoMap = new HashMap<>(); //装备符文孔信息 key:holeId(孔位) value：RoleTokenEquipmentHolePo
    private int tokenSkillId; //符文技能id
    private int tokenSKillLevel; //符文技能等级

    private byte isEquip;
    private short equipLevel;
    private byte equipType;
    private Attribute basicAttr;
    private int basicFighting;  //基础属性战力
    private int fighting;
    private int extraAttrFighting;      //额外属性总战力
    private int maxExtraAttrFighting;   //最大的额外属性战力
    private int jobId;

    /*******************内存中对应的数据*****************/
    private RoleToolTable table;

    public RoleToolRow() {
    }

    public RoleToolRow(RoleToolTable table, long roleId, long toolId, int itemId, int count) {
        this.table = table;
        this.roleId = roleId;
        this.toolId = toolId;
        this.itemId = itemId;
        this.count = count;
        this.newFlag = 1;
        this.maker = "";
        this.bornTime = System.currentTimeMillis();
//        this.setInsertStatus();
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "roletool" + table.getTableId(),
                " toolid='" + this.toolId + "'");
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("roletool" + table.getTableId(), " toolid='" + this.toolId + "'");
    }

    public long getToolId() {
        return toolId;
    }

    public void setToolId(long toolId) {
        this.toolId = toolId;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getItemId() {
        return itemId;
    }

    public byte getNewFlag() {
        return newFlag;
    }

    public void setNewFlag(byte newFlag) {
        this.newFlag = newFlag;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }


    /**
     * 下发数据,需要注意的是:
     * 1,新标示在下发后,服务端会制成旧的
     * 2,是否下发属性,下发一次后如果属性没变化是不会再下发的
     */
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(Long.toHexString(toolId));
        buff.writeString(String.valueOf(roleId));
        buff.writeInt(itemId);
        buff.writeInt(count);
        buff.writeString(maker);
        buff.writeByte(newFlag);
        buff.writeLong(bornTime);
        buff.writeByte(isEquip);
        if (isEquip == 1) {
            buff.writeInt(jobId);
            buff.writeShort(equipLevel);
            buff.writeByte(equipType);
            buff.writeInt(fighting);
            basicAttr.writeToBuffer(buff);
            if (StringUtil.isEmpty(extraAttrMap)) {
                buff.writeByte((byte) 0);
            } else {
                buff.writeByte((byte) extraAttrMap.size());
                for (ExtraAttrVo vo : extraAttrMap.values()) {
                    vo.writeToBuffer(buff);
                }
            }

        }
    }


    @Override
    public int compareTo(Object o) {
        RoleToolRow nextTool = (RoleToolRow) o;
        //id不同,按id大小排序
        if (nextTool.getItemId() != this.itemId) {
            return this.itemId - nextTool.getItemId();
        }
        if (nextTool.getCount() != this.getCount()) {
            return nextTool.getCount() - this.count;
        }
        if (nextTool.getToolId() > this.getToolId()) {
            return 1;
        } else {
            return -1;
        }
    }

    public RoleToolTable getTable() {
        return table;
    }

    public void setTable(RoleToolTable table) {
        this.table = table;
    }

    public long getBornTime() {
        return bornTime;
    }

    public void setBornTime(long bornTime) {
        this.bornTime = bornTime;
    }

    public String getExtraAttrStr() {
        if (StringUtil.isEmpty(extraAttrMap)) return "";
        StringBuilder sb = new StringBuilder();
        for (ExtraAttrVo vo : extraAttrMap.values()) {
            sb.append(vo.toString()).append("&");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public void setExtraAttrStr(String extraAttrStr) {
        if (StringUtil.isEmpty(extraAttrStr)) return;
        extraAttrMap = new HashMap<>();
        String[] array = extraAttrStr.split("&");
        ExtraAttrVo vo;
        for (String tmp : array) {
            vo = new ExtraAttrVo(tmp);
            extraAttrMap.put(vo.getIndex(), vo);
        }
    }

    public Map<Byte, ExtraAttrVo> getExtraAttrMap() {
        return extraAttrMap;
    }

    public void setExtraAttrMap(Map<Byte, ExtraAttrVo> extraAttrMap) {
        this.extraAttrMap = extraAttrMap;
    }

    public Attribute getBasicAttr() {
        return basicAttr;
    }

    public void setBasicAttr(Attribute basicAttr) {
        this.basicAttr = basicAttr;
    }

    public byte getIsEquip() {
        return isEquip;
    }

    public void setIsEquip(byte isEquip) {
        this.isEquip = isEquip;
    }

    public short getEquipLevel() {
        return equipLevel;
    }

    public void setEquipLevel(short equipLevel) {
        this.equipLevel = equipLevel;
    }

    public void setEquipType(byte equipType) {
        this.equipType = equipType;
    }

    public ExtraAttrVo getRandomExtraAttr() {
        if (StringUtil.isEmpty(extraAttrMap)) return null;
        Random random = new Random();
        int index = random.nextInt(extraAttrMap.size());
        int curIndex = 0;
        ExtraAttrVo firstVo = null;
        for (ExtraAttrVo vo : extraAttrMap.values()) {
            if (index == curIndex) return vo;
            if (firstVo == null) firstVo = vo;
            curIndex++;
        }
        return firstVo;
    }

    public int getFighting() {
        return fighting;
    }

    public void setFighting(int fighting) {
        this.fighting = fighting;
    }

    public byte getEquipType() {
        return equipType;
    }

    public int getExtraAttrFighting() {
        return extraAttrFighting;
    }

    public void setExtraAttrFighting(int extraAttrFighting) {
        this.extraAttrFighting = extraAttrFighting;
    }

    public int getMaxExtraAttrFighting() {
        return maxExtraAttrFighting;
    }

    public void setMaxExtraAttrFighting(int maxExtraAttrFighting) {
        this.maxExtraAttrFighting = maxExtraAttrFighting;
    }

    public int getBasicFighting() {
        return basicFighting;
    }

    public void setBasicFighting(int basicFighting) {
        this.basicFighting = basicFighting;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getJobId() {
        return jobId;
    }

    public String getTokenHoleStr() {
        if (StringUtil.isEmpty(roleTokenHoleInfoMap))
            return "";
        StringBuffer sBuffer = new StringBuffer();
        for (RoleTokenEquipmentHolePo po : roleTokenHoleInfoMap.values()) {
            sBuffer.append(po.toString()).append("&");
        }
        if (sBuffer.length() > 0) {
            sBuffer.deleteCharAt(sBuffer.length() - 1);
        }
        return sBuffer.toString();
    }

    public int getTokenSkillId() {
        return tokenSkillId;
    }

    public void setTokenSkillId(int tokenSkillId) {
        this.tokenSkillId = tokenSkillId;
    }

    public int getTokenSKillLevel() {
        return tokenSKillLevel;
    }

    public void setTokenSKillLevel(int tokenSKillLevel) {
        this.tokenSKillLevel = tokenSKillLevel;
    }

    public void setTokenHoleStr(String tokenHoleStr) {
        this.tokenHoleStr = tokenHoleStr;
        roleTokenHoleInfoMap = new HashMap<>();
        if (StringUtil.isEmpty(tokenHoleStr)) {
            return;
        }
        String[] array = tokenHoleStr.split("&");
        RoleTokenEquipmentHolePo roleTokenEquipmentHolePo;
        for (String tokenHoleInfo : array) {
            roleTokenEquipmentHolePo = new RoleTokenEquipmentHolePo(tokenHoleInfo);
            roleTokenHoleInfoMap.put(roleTokenEquipmentHolePo.getHoleId(), roleTokenEquipmentHolePo);
        }
    }

    public String getTokenSkillStr() {
        if (tokenSkillId == 0)
            return "";
        StringBuffer sBuff = new StringBuffer();
        sBuff.append(tokenSkillId).append("=").append(tokenSKillLevel);
        return sBuff.toString();
    }

    public void setTokenSkillStr(String tokenSkillStr) {
        this.tokenSkillStr = tokenSkillStr;
        if (StringUtil.isEmpty(tokenSkillStr)) {
            this.tokenSkillId = 0;
            this.tokenSKillLevel = 0;
            return;
        }
        String[] array = tokenSkillStr.split("=");
        this.tokenSkillId = Integer.parseInt(array[0]);
        this.tokenSKillLevel = Integer.parseInt(array[1]);

    }

    public Map<Byte, RoleTokenEquipmentHolePo> getRoleTokenHoleInfoMap() {
        if (roleTokenHoleInfoMap == null)
            return new HashMap<>();
        return roleTokenHoleInfoMap;
    }

    public void setRoleTokenHoleInfoMap(Map<Byte, RoleTokenEquipmentHolePo> roleTokenHoleInfoMap) {
        this.roleTokenHoleInfoMap = roleTokenHoleInfoMap;
    }

    public boolean isTokenEquip() {
        return false;
    }

    public Map<Integer, Integer> getWashTokenCost() {
        return null;
    }
}
