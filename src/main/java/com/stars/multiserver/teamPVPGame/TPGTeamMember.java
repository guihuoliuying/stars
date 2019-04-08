package com.stars.multiserver.teamPVPGame;

import com.stars.db.DBUtil;
import com.stars.db.DbRow;
import com.stars.db.SqlUtil;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.baseteam.BaseTeamMember;

public class TPGTeamMember extends DbRow{
	private long roleId;
	private int teamId;
	private byte job;
	private int fight;
	private String name;
	private short level;
	private String step;
	private String familyName;// 家族名称

	/* 内存数据 */
	private FighterEntity fEntity;
	private boolean isDead = false;

	/**
	 * 被伤害
	 */
	private int hurted = 0;
	
	/**
	 * 对别人的伤害
	 */
	private int hurt = 0;
	
	
	public TPGTeamMember(){
		
	}
	
	@Override
	public String getChangeSql() {
		return SqlUtil.getSql(this, DBUtil.DB_USER, "tpgmember", "`teamid`=" + teamId + " and `roleid`=" + roleId);
	}
	
	@Override
	public String getDeleteSql() {
		return null;
	}

	public void writeToBuff(NewByteBuffer buff) {
		buff.writeString(String.valueOf(roleId));
		buff.writeString(name);
		buff.writeByte(job);
		buff.writeInt(level);
		buff.writeInt(fight);
		buff.writeString(familyName);
	}

	public void reset(){
		isDead = false;
		hurted = 0;
		hurt = 0;
	}

	public void updateMember(BaseTeamMember teamMember) {
		this.job = teamMember.getJob();
		this.level = teamMember.getLevel();
		this.fight = teamMember.getFightSocre();
		this.familyName = teamMember.getFamilyName();
		FighterEntity newEntity = teamMember.getRoleEntity();
		newEntity.setCamp(fEntity.getCamp());
		newEntity.setFighterType(fEntity.getFighterType());
		this.fEntity = newEntity;
	}
	
	public long getRoleId() {
		return roleId;
	}
	public void setRoleId(long roleId) {
		this.roleId = roleId;
	}
	public byte getJob() {
		return job;
	}
	public void setJob(byte job) {
		this.job = job;
	}
	public int getFight() {
		return fight;
	}
	public void setFight(int fight) {
		this.fight = fight;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public short getLevel() {
		return level;
	}
	public void setLevel(short level) {
		this.level = level;
	}
	public int getTeamId() {
		return teamId;
	}
	public void setTeamId(int teamId) {
		this.teamId = teamId;
	}
	public String getStep() {
		return step;
	}
	public void setStep(String step) {
		this.step = step;
	}

	public FighterEntity getfEntity() {
		return fEntity;
	}

	public void setfEntity(FighterEntity fEntity) {
		this.fEntity = fEntity;
	}

	public boolean isDead() {
		return isDead;
	}

	public void setDead(boolean isDead) {
		this.isDead = isDead;
	}

	public int getHurted() {
		return hurted;
	}

	public void setHurted(int hurted) {
		this.hurted = hurted;
	}

	public int getHurt() {
		return hurt;
	}

	public void setHurt(int hurt) {
		this.hurt = hurt;
	}
	
	public void addHurt(int add){
		this.hurt = hurt + add;
	}
	
	public void addHurted(int add){
		hurted = hurted + add;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
}
