package com.stars.modules.elitedungeon.userdata;

import com.stars.core.attr.Attribute;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ElitePlayerImagePo extends DbRow implements Comparable<ElitePlayerImagePo>{
	
	private long roleid;
	
	private String name;
	
	private int level;
	
	private int job;
	
	private String skillstr;
	
	private String attributestr;
	
	private int buddyId;
	
	private int buddyLevel;
	
	private int buddyStageLevel;
	
	private Map<Integer, Integer> skillMap;
	
	private Map<Integer, Integer> robotSkillDamage = new HashMap<>();// 技能附加伤害(装备附魔增加),<id, damage>
	
	private Attribute attribute;
	
	private int fightScore;
	
	private int stageid;
	
	private int createTime;

	public long getRoleid() {
		return roleid;
	}

	public void setRoleid(long roleid) {
		this.roleid = roleid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public String getSkillstr() {
		Set<Entry<Integer, Integer>> set = skillMap.entrySet();
        StringBuffer buffer = new StringBuffer();
        int index = 0;
        for (Entry<Integer, Integer> entry : set) {
            if (index > 0) {
                buffer.append("|");
            }
            buffer.append(entry.getKey());
            buffer.append("=");
            buffer.append(entry.getValue());
            buffer.append("=");
            buffer.append(robotSkillDamage.get(entry.getKey()));
            index++;
        }
        skillstr = buffer.toString();
		return skillstr;
	}

	public void setSkillstr(String skillstr) {
		this.skillstr = skillstr;
		this.skillMap = new HashMap<Integer, Integer>();
		String[] sts1 = skillstr.split("[|]");
        for (String string : sts1) {
            String[] sts2 = string.split("[=]");
            this.skillMap.put(Integer.parseInt(sts2[0]), Integer.parseInt(sts2[1]));
            this.robotSkillDamage.put(Integer.parseInt(sts2[0]), Integer.parseInt(sts2[2]));
        }
	}
	
	public String getAttributestr() {
		attributestr = attribute.getAttributeStr();
		return attributestr;
	}

	public void setAttributestr(String attributestr) {
		this.attributestr = attributestr;
		if(StringUtil.isNotEmpty(attributestr)){			
			this.attribute = new Attribute(attributestr);
		}else{
			this.attribute = new Attribute();
		}
	}

	public int getBuddyId() {
		return buddyId;
	}

	public void setBuddyId(int buddyId) {
		this.buddyId = buddyId;
	}

	public int getBuddyLevel() {
		return buddyLevel;
	}

	public void setBuddyLevel(int buddyLevel) {
		this.buddyLevel = buddyLevel;
	}

	public int getBuddyStageLevel() {
		return buddyStageLevel;
	}

	public void setBuddyStageLevel(int buddyStageLevel) {
		this.buddyStageLevel = buddyStageLevel;
	}

	public Map<Integer, Integer> getSkillMap() {
		return skillMap;
	}

	public void setSkillMap(Map<Integer, Integer> skillMap) {
		this.skillMap = skillMap;
	}

	public Map<Integer, Integer> getRobotSkillDamage() {
		return robotSkillDamage;
	}

	public void setRobotSkillDamage(Map<Integer, Integer> robotSkillDamage) {
		this.robotSkillDamage = robotSkillDamage;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public int getFightScore() {
		return fightScore;
	}

	public void setFightScore(int fightScore) {
		this.fightScore = fightScore;
	}

	public int getStageid() {
		return stageid;
	}

	public void setStageid(int stageid) {
		this.stageid = stageid;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	@Override
	public String getChangeSql() {
		StringBuffer condition = new StringBuffer();
		condition.append(" `roleid`=").append(this.roleid).append(" and `stageid`=").append(this.stageid);
		return SqlUtil.getSql(this, DBUtil.DB_USER, "eliteplayerimage", condition.toString());
	}

	@Override
	public String getDeleteSql() {
		StringBuffer condition = new StringBuffer();
		condition.append(" `createTime`=").append(this.createTime).append(" and `roleid`=").append(this.roleid)
		.append(" and `stageid`=").append(this.stageid);
		return "delete from eliteplayerimage where "+condition.toString();
	}

	@Override
	public int compareTo(ElitePlayerImagePo o) {
		if(this.level>o.level){
			return 1;
		}else if(this.level==o.level){
			if(this.createTime>o.createTime){
				return 1;
			}else if(this.createTime==o.createTime){
				return 0;
			}
		}
		return -1;
	}

}
