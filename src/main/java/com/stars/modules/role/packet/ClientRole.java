package com.stars.modules.role.packet;

import com.stars.core.attr.Attribute;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.camp.usrdata.RoleCampPo;
import com.stars.modules.role.RoleManager;
import com.stars.modules.role.RolePacketSet;
import com.stars.modules.role.prodata.Grade;
import com.stars.modules.role.prodata.Job;
import com.stars.modules.role.prodata.Resource;
import com.stars.modules.role.userdata.Role;
import com.stars.multiserver.MultiServerHelper;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.List;
import java.util.Map;

/**
 * 玩家基本数据
 *
 * @author huachp
 */
public class ClientRole extends PlayerPacket {
    private byte sendType;

    public static final byte SEND_ROLE = 1;// 下发角色全部数据
    public static final byte UPDATE_BASE = 2;// 基本数据，不会改变的数据，如姓名、职业、性别等
    public static final byte UPDATE_EXP = 3;// 更新角色经验，经验可能会经常变化
    public static final byte UPDATE_ATTR = 4;// 更新属性，等级变了，属性一定会变，属性变了，战力一定会变，所以一起下发
    public static final byte UPDATE_RESOURCE = 5;//资源
    public static final byte UPDATE_TITLE = 6;// 更新角色称号
    public static final byte UPDATE_FIGHTSCORE = 7;// 更新角色战力(各部分战力+总战力)
    public static final byte UPDATE_SKILL = 8;//更新角色技能
    public static final byte UPDATE_REVIVE_NUM = 9;// 更新已复活次数
    public static final byte UPDATE_VIP_LEVEL = 10;// 更新vip等级
    public static final byte UPDATE_MARRY_NAME = 11;    // 更新结义对象名字
    public static final byte UPDATE_TOKEN_SKILL_EFFECT = 12; //更新角色号符文技能外显
    public static final byte UPDATE_LEVEL_SPEED_UP_ADDITION = 13; //更新角色号符文技能外显

    public String skill;//使用的技能列表
    public String pSkill;//使用的被动技能列表
    public String normalSkill;//普攻列表
    private int vipLevel;// vip等级
    private String marryName;   // 结义对象的名字
    private List<String> dragonBallIdList; //外显Id （龙珠）
    private int levelSpeedUpAddtion;//等级加速 经验加成
    private Role role;
    private RoleCampPo roleCampPo;
    private int clientSystemConstant = 0;//客户端系统常量,为了资源变化做提示用;
    private byte isShow = 1;// 战力通知是否提示,默认需要弹出提示

    public ClientRole() {

    }

    public ClientRole(byte sendType, Role role) {
        this.sendType = sendType;
        this.role = role;
    }

    @Override
    public void execPacket(Player player) {

    }

    public void setClientSystemConstant(int clientSystemConstant) {
        this.clientSystemConstant = clientSystemConstant;
    }

    @Override
    public short getType() {
        return RolePacketSet.C_ROLE;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_ROLE:// 下发角色全部数据
                writeBase(buff);
                writeExp(buff);
                writeAtrr(buff);
                writeResources(buff);
                buff.writeInt(role.getTitleId());
                buff.writeInt(role.getFightScore());
                writeReviveInfo(buff);
                buff.writeString(MultiServerHelper.getServerName());
                buff.writeInt(MultiServerHelper.getServerId());
                buff.writeInt(vipLevel);
                buff.writeString(role.getCreateTime() + "");
                buff.writeInt(levelSpeedUpAddtion);
                break;
            case UPDATE_BASE: {
                writeBase(buff);
            }
            break;
            case UPDATE_ATTR:
                writeAtrr(buff);
                break;
            case UPDATE_EXP:
                writeExp(buff);
                break;
            case UPDATE_RESOURCE:
                writeResources(buff);
                break;
            case UPDATE_TITLE:
                buff.writeInt(role.getTitleId());
                break;
            case UPDATE_FIGHTSCORE:
                writeFightScore(buff);
                break;
            case UPDATE_SKILL:
                writeSkill(buff);
                break;
            case UPDATE_REVIVE_NUM:
                writeReviveInfo(buff);
                break;
            case UPDATE_VIP_LEVEL:
                buff.writeInt(vipLevel);
                break;
            case UPDATE_MARRY_NAME:
                buff.writeString(marryName);
                break;
            case UPDATE_TOKEN_SKILL_EFFECT:
                writeTokenSkillEffectInfo(buff);
                break;
            case UPDATE_LEVEL_SPEED_UP_ADDITION:
                buff.writeInt(levelSpeedUpAddtion);
                break;
            default:
                break;
        }
    }

    @Override
    public void readFromBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    private void writeBase(com.stars.network.server.buffer.NewByteBuffer buffer) {
        buffer.writeString(String.valueOf(role.getRoleId()));
        buffer.writeInt(role.getSex());
        buffer.writeString(role.getName());
        Job curJob = RoleManager.getJobById(this.role.getJobId());
        buffer.writeInt(role.getJobId());
        buffer.writeString(curJob.getPose());
        buffer.writeInt(curJob.getRoleinfoscale());
        buffer.writeInt(curJob.getFightarea());
        Resource curResource = RoleManager.getResourceById(curJob.getModelres());
        buffer.writeString(curResource.getModel());
        buffer.writeInt(curResource.getHitsize());
        buffer.writeInt(curResource.getMovespeed());
        buffer.writeInt(curResource.getUiposition());
        buffer.writeInt(curResource.getTurnSpeed());
        buffer.writeString(curResource.getHeadIcon());
        buffer.writeInt(curResource.getNpctalkmodel());
        buffer.writeInt(role.getSafeStageId());
        if (roleCampPo != null) {
            buffer.writeInt(roleCampPo.getCampType());//所属阵营,0表示没有阵营
        } else {
            buffer.writeInt(0);
        }
        buffer.writeInt(role.getBabyStage());
        buffer.writeInt(role.getBabyLevel());
        buffer.writeInt(role.getCurFashionCardId());
    }

    private void writeExp(com.stars.network.server.buffer.NewByteBuffer buffer) {
        buffer.writeInt(role.getExp());
    }

    private void writeAtrr(com.stars.network.server.buffer.NewByteBuffer buffer) {
        buffer.writeInt(role.getLevel());
        buffer.writeInt(RoleManager.getRequestExpByJobLevel(role.getJobId(), role.getLevel() + 1));
        buffer.writeInt(RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel()).getVigorMax()); // 体力最大值
        Grade gradeVo = RoleManager.getGradeByJobLevel(role.getJobId(), role.getLevel());
        buffer.writeString(gradeVo.getBuyMoney());// 对应等级购买金币消耗和获得
        buffer.writeInt(gradeVo.getFreeCount());// 对应等级免费购买金币次数
        buffer.writeInt(gradeVo.getPayCount());// 对应等级付费购买金币次数
        Attribute attr = role.getTotalAttr();
        attr.writeToBuffer(buffer);
    }

    private void writeResources(com.stars.network.server.buffer.NewByteBuffer buffer) {
        buffer.writeInt(clientSystemConstant);
        buffer.writeInt(role.getGold() + role.getBandGold());
        buffer.writeInt(role.getGold() + role.getBandGold());
        buffer.writeInt(role.getMoney());
        buffer.writeInt(role.getVigor());
        buffer.writeInt(role.getGloryPoints());
        buffer.writeInt(role.getSkillPoints());
        buffer.writeInt(role.getFeats());
        buffer.writeInt(role.getBabyEnergy());
    }

    private void writeFightScore(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(isShow);
        buff.writeInt(role.getFightScore());
        byte size = (byte) (role.getFightScoreMap() == null ? 0 : role.getFightScoreMap().size());
        buff.writeByte(size);
        if (size > 0) {
            for (Map.Entry<String, Integer> entry : role.getFightScoreMap().entrySet()) {
                buff.writeString(entry.getKey());
                buff.writeInt(entry.getValue());
            }
        }
    }

    private void writeSkill(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeString(this.normalSkill);
        buff.writeString(this.skill);
        buff.writeString(this.pSkill);
    }

    private void writeReviveInfo(com.stars.network.server.buffer.NewByteBuffer buff) {
        byte size = (byte) (role.getReviveMap() == null ? 0 : role.getReviveMap().size());
        buff.writeByte(size);
        if (size == 0)
            return;
        for (Map.Entry<Byte, Byte> entry : role.getReviveMap().entrySet()) {
            buff.writeByte(entry.getKey());// stageType
            buff.writeByte(entry.getValue());// 已复活次数
        }
    }

    private void writeTokenSkillEffectInfo(NewByteBuffer buff) {
        int size = dragonBallIdList.size();
        buff.writeInt(size);
        if (size == 0)
            return;
        for (String dragonBallId : dragonBallIdList) {
            buff.writeString(dragonBallId);
        }
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public void setPSkill(String pskil) {
        this.pSkill = pskil;
    }

    public String getNormalSkill() {
        return normalSkill;
    }

    public void setNormalSkill(String normalSkill) {
        this.normalSkill = normalSkill;
    }

    public void setIsShow(byte isShow) {
        this.isShow = isShow;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public void setMarryName(String marryName) {
        this.marryName = marryName;
    }

    public List<String> getDragonBallIdList() {
        return dragonBallIdList;
    }

    public void setDragonBallIdList(List<String> dragonBallIdList) {
        this.dragonBallIdList = dragonBallIdList;
    }

    public int getLevelSpeedUpAddtion() {
        return levelSpeedUpAddtion;
    }

    public void setLevelSpeedUpAddtion(int levelSpeedUpAddtion) {
        this.levelSpeedUpAddtion = levelSpeedUpAddtion;
    }

    public RoleCampPo getRoleCampPo() {
        return roleCampPo;
    }

    public void setRoleCampPo(RoleCampPo roleCampPo) {
        this.roleCampPo = roleCampPo;
    }
}
