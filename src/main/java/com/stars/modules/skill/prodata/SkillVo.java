package com.stars.modules.skill.prodata;

import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daiyaorong on 2016/6/21.
 */
public class SkillVo {

    private int skillid;
    private String name;
    private String icon;
    private String describ;
    private String skilltype;
    private int skilltarget;
    private String skilldistance;
    private String buffinfo;
    private String effectinfo;
    private String bulleteffectinfo;
    private int effecttype;
    private String movement;
    private String hittime;
    private String collision;
    private String hiteffect;
    private String specialeffect;
    private String sound;
    private int resttime;
    private String damageNumberType;
    private String areawarning;
    private byte isPassskill; 
    private byte ichange;
    private String action;
    private String direction;
    private String joystickarea;
    private String damageadd;
    private String hitlevel;

    /* 内存数据 */
    private Set<Integer> buffIdSet = new HashSet<>();// buffIdSet
    private byte type;

    public SkillVo() {
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt(this.skillid);
        buff.writeString(this.skilltype);
        buff.writeInt(this.skilltarget);
        buff.writeString(this.skilldistance);
        buff.writeString(this.buffinfo);
        buff.writeString(this.effectinfo);
        buff.writeString(this.bulleteffectinfo);
        buff.writeInt(this.effecttype);
        buff.writeString(this.movement);
        buff.writeString(this.hittime);
        buff.writeString(this.collision);
        buff.writeString(this.hiteffect);
        buff.writeString(this.specialeffect);
        buff.writeString(this.sound);
        buff.writeInt(this.resttime);
        buff.writeString(damageNumberType);
        buff.writeString(this.areawarning);
        buff.writeString(this.icon);
        buff.writeString(this.action);
        buff.writeString(this.direction);
        buff.writeString(this.joystickarea);
        buff.writeString(this.damageadd);
        buff.writeString(this.hitlevel);
        buff.writeString(this.name);
    }

    public Set<Integer> getBuffIdSet() {
        return buffIdSet;
    }

    public int getSkillid() {
        return skillid;
    }

    public void setSkillid(int skillid) {
        this.skillid = skillid;
    }

    public String getSkilltype() {
        return skilltype;
    }

    public void setSkilltype(String skilltype) {
        this.skilltype = skilltype;
        String ts[] = skilltype.split("\\|");
        setType(Byte.parseByte(ts[0]));
    }

    public int getSkilltarget() {
        return skilltarget;
    }

    public void setSkilltarget(int skilltarget) {
        this.skilltarget = skilltarget;
    }

    public String getSkilldistance() {
        return skilldistance;
    }

    public void setSkilldistance(String skilldistance) {
        this.skilldistance = skilldistance;
    }

    public String getBuffinfo() {
        return buffinfo;
    }

    public void setBuffinfo(String buffinfo) {
        this.buffinfo = buffinfo;
        if (StringUtil.isEmpty(buffinfo) || "0".equals(buffinfo)) {
            return;
        }
        buffinfo = buffinfo.replace("|", ",");
        String[] temp = buffinfo.split(",");
        for (String buffStr : temp) {
            if (!buffStr.contains("+")) {
                continue;
            }
            String[] buff = buffStr.split("\\+");
            this.buffIdSet.add(Integer.parseInt(buff[0]));
        }
    }

    public String getEffectinfo() {
        return effectinfo;
    }

    public void setEffectinfo(String effectinfo) {
        this.effectinfo = effectinfo;
    }

    public String getBulleteffectinfo() {
        return bulleteffectinfo;
    }

    public void setBulleteffectinfo(String bulleteffectinfo) {
        this.bulleteffectinfo = bulleteffectinfo;
    }

    public int getEffecttype() {
        return effecttype;
    }

    public void setEffecttype(int effecttype) {
        this.effecttype = effecttype;
    }

    public String getMovement() {
        return movement;
    }

    public void setMovement(String movement) {
        this.movement = movement;
    }

    public String getHittime() {
        return hittime;
    }

    public void setHittime(String hittime) {
        this.hittime = hittime;
    }

    public String getCollision() {
        return collision;
    }

    public void setCollision(String collision) {
        this.collision = collision;
    }

    public String getHiteffect() {
        return hiteffect;
    }

    public void setHiteffect(String hiteffect) {
        this.hiteffect = hiteffect;
    }

    public String getSpecialeffect() {
        return specialeffect;
    }

    public void setSpecialeffect(String specaileffect) {
        this.specialeffect = specaileffect;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getDamageNumberType() {
        return damageNumberType;
    }

    public void setDamageNumberType(String damageNumberType) {
        this.damageNumberType = damageNumberType;
    }

    public String getAreawarning() {
        return areawarning;
    }

    public void setAreawarning(String areawarning) {
        this.areawarning = areawarning;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getDescrib() {
		return describ;
	}

	public void setDescrib(String describ) {
		this.describ = describ;
	}

	public byte getIchange() {
		return ichange;
	}

	public void setIchange(byte ichange) {
		this.ichange = ichange;
	}

	public byte getIsPassskill() {
		return isPassskill;
	}

	public void setIsPassskill(byte isPassskill) {
		this.isPassskill = isPassskill;
	}

    public int getResttime() {
        return resttime;
    }

    public void setResttime(int resttime) {
        this.resttime = resttime;
    }

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getJoystickarea() {
        return joystickarea;
    }

    public void setJoystickarea(String joystickarea) {
        this.joystickarea = joystickarea;
    }

    public String getDamageadd() {
        return damageadd;
    }

    public void setDamageadd(String damageadd) {
        this.damageadd = damageadd;
    }

    public String getHitlevel() {
        return hitlevel;
    }

    public void setHitlevel(String hitlevel) {
        this.hitlevel = hitlevel;
    }
}
