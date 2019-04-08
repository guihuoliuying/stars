package com.stars.modules.fashion.userdata;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.fashion.FashionManager;
import com.stars.modules.fashion.prodata.FashionAttrVo;
import com.stars.modules.fashion.prodata.FashionVo;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by gaoepidian on 2016/10/08.
 */
public class RoleFashion extends DbRow implements Cloneable{
    private long roleId;
    private int fashionId;
    private byte isDress;
    private byte everDress;
    private long expiredTime; //如果是过期时装，这里需要个过期时间戳

    private boolean isExpiredBefore = false; //是否本次缓存在线时过期了
    
    public RoleFashion() {

    }

    public RoleFashion(long roleId, int fashionId, byte isDress, byte everDress, long expiredTime) {
        this.roleId = roleId;
        this.fashionId = fashionId;
        this.isDress = isDress;
        this.everDress = everDress;
        this.expiredTime = expiredTime;
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "rolefashion", " roleid=" + this.getRoleId() + " and fashionId=" + this.getFashionId());
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("rolefashion", " roleid=" + this.getRoleId() + " and fashionId=" + this.getFashionId());
    }

    //是否是永久的;
    public boolean isForever(){
        FashionAttrVo fashionAttrVo = FashionManager.getFasionAttrVo(fashionId);
        if(fashionAttrVo != null){
            return fashionAttrVo.getTimeType() == (byte)2;
        }
        return expiredTime == 0;
    }

    //是否过期了;
    public boolean isExpired(){
        if(isForever()){
            return false;
        }else{
            return System.currentTimeMillis() > expiredTime;
        }
    }
    //是否激活状态,永久时装或未过期
    public boolean isActive(){
        return (!isExpired())||isForever();
    }

    public void writeDressedFashionToBuff(NewByteBuffer buffer) { //需要和装备位保持一致
        if (fashionId >0) {
            FashionVo fashionVo = FashionManager.getFashionVo(fashionId);
            FashionAttrVo fashionAttrVo = FashionManager.getFasionAttrVo(fashionId);
            Attribute baseAttr = fashionAttrVo.getAttribute();
            int fighting = FormularUtils.calFightScore(baseAttr);
            buffer.writeByte((byte) 8);             //时装是固定部位8
            buffer.writeLong(roleId);
            buffer.writeInt(fashionVo.getItemId());
            if(fashionVo.getItemId() == 0) return;
            buffer.writeInt(0);     //强化等级
            buffer.writeInt(0);         //星级
            buffer.writeInt(0);   //强化等级加成百分比
            buffer.writeInt(0);   //强化固定加成
            buffer.writeInt(0);       //星级加成百分比
            buffer.writeString("0");
            buffer.writeInt(fighting);          //装备战力
            buffer.writeInt(0);        //装备等级
            buffer.writeString("");       //洗练消耗货币
            buffer.writeString("");     //转移属性消耗货币
            baseAttr.writeToBuffer(buffer);     //基础属性
            buffer.writeByte((byte) 0); //额外属性
            buffer.writeByte((byte)0); //非符文
        }else{
            buffer.writeByte((byte) 8);             //时装是固定部位8
            buffer.writeLong(roleId);
            buffer.writeInt(0);           //itemid

        }
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public int getFashionId(){
    	return fashionId;
    }
    
    public void setFashionId(int value){
    	this.fashionId = value;
    }
    
    public byte getIsDress(){
    	return isDress;
    }
    
    public void setIsDress(byte value){
    	this.isDress = value;
    }
    
    public byte getEverDress(){
    	return everDress;
    }
    
    public void setEverDress(byte value){
    	this.everDress = value;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
        if(isExpired()){
            this.isExpiredBefore = true;
        }else{
            this.isExpiredBefore = false;
        }
    }

    public boolean isExpiredBefore() {
        return isExpiredBefore;
    }

    public void setExpiredBefore(boolean isExpiredBefore) {
        this.isExpiredBefore = isExpiredBefore;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


}
