package com.stars.modules.poemdungeon.teammember;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeamMember;
/**
 * Created by gaopeidian on 2017/5/22.
 */
public class RobotTeamMember extends BaseTeamMember {
    private String strRoleId;

    public RobotTeamMember(byte type) {
    	super(type);
    }
    
    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeString(getStrRoleId());// roleId
        buff.writeByte(getType());// 0=真实玩家;1=构造玩家数据,默认是0
        buff.writeString(getRoleEntity().getName());// 名字
        buff.writeShort((short) getRoleEntity().getLevel());// 等级
        buff.writeByte(getJob());// 职业
        buff.writeInt(getRoleEntity().getFightScore());// 战力
        buff.writeInt(getCurDeityWeapon());// 当前使用神兵
    }
    
    public String getStrRoleId(){
    	return this.strRoleId;
    }
    
    public void setStrRoleId(String value){
    	this.strRoleId = value;
    }
    
    @Override
    public FighterEntity getRoleEntity() {
        return getEntityMap().get(strRoleId);
    }
}
