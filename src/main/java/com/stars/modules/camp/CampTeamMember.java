package com.stars.modules.camp;

import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeamMember;

public class CampTeamMember extends BaseTeamMember {
	
	private RoleCampPo roleCampPo;
	
	public CampTeamMember(byte type, RoleCampPo roleCampPo) {
		super();
		this.roleCampPo = roleCampPo;
	}
	
	@Override
	public void writeToBuffer(NewByteBuffer buff) {
		// TODO Auto-generated method stub
		super.writeToBuffer(buff);
	}
	
	@Override
	public FighterEntity getRoleEntity() {
		// TODO Auto-generated method stub
		return super.getRoleEntity();
	}

	public RoleCampPo getRoleCampPo() {
		return roleCampPo;
	}

	public void setRoleCampPo(RoleCampPo roleCampPo) {
		this.roleCampPo = roleCampPo;
	}

}
