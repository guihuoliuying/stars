package com.stars.modules.pk.packet;

import com.stars.network.server.buffer.NewByteBuffer;

import java.util.ArrayList;

/**
 * Created by daiyaorong on 2016/12/1.
 */
public class ServerOrder {

    public final static byte ORDER_TYPE_ADD_CAMP_BUFF = 1;          // 指令：给指定阵营加buff
    public final static byte ORDER_TYPE_REMOVE_CAMP_BUFF = 2;       // 指令：移除指定阵营的buff
    public final static byte ORDER_TYPE_MOVE_CHARAC = 3;            // 指令：让指定角色移动至指定位置(参数:uniqueId, position)
    public final static byte ORDER_TYPE_STOP_CHARAC = 4;            // 指令：让指定角色停止移动(参数:uniqueId)
    public final static byte ORDER_TYPE_CHANGE_FIGHTSTATE = 5;      // 指令：更改战斗状态(参数:fightState,duration)
    public final static byte ORDER_TYPE_RESET_CHARACS = 6;          // 指令：重置角色状态,血量回满,停下技能(参数:uniqueIDs)
    public final static byte ORDER_TYPE_ADD_BUFF = 7;                // 指令：给指定角色添加BUFF
    public final static byte ORDER_TYPE_SETAI = 8;                  // 指令：设置玩家AI    public final static byte NONE = 0;					//无类型或无阵营

    public final static byte NONE = 0;					//无类型或无阵营

    public final static byte CHARACTER_TYPE_PLAYER = 1;        //目标类型：玩家
    public final static byte CHARACTER_TYPE_MONSTER = 2;    //目标类型：怪物

    public final static byte FIGHT_STATE_NONE = 0;          // 战斗状态：无
    public final static byte FIGHT_STATE_READY = 1;         // 战斗状态：准备中
    public final static byte FIGHT_STATE_FIGHTING = 2;      // 战斗状态：战斗中
    public final static byte FIGHT_STATE_PAUSE = 3;         // 战斗状态：暂停中
    public final static byte FIGHT_STATE_END = 4;           // 战斗状态：结束
    public final static byte OPEN_AI = 1;                   //开启AI
    public final static byte CLOSE_AI = 0;                  //关闭AI

    private byte orderType;                 // 指令类型
    private byte cmapId;                    // 阵营id
    private byte characterType;             // 角色类型
    private Integer buffId;                 // buff配置Id
    private Integer level;                  // 等级
    private Integer instanceId;             // 实例Id
    private String uniqueId;                // 角色唯一ID
    private ArrayList<String> uniqueIDs;    // 角色唯一ID列表
    private String position;                // 位置字符串
    private byte fightState;                // 战斗状态
    private Integer duration;               // 战斗状态持续的时间
    private byte aiState;                   // AI状态

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(orderType);
        switch (orderType) {
            case ORDER_TYPE_ADD_CAMP_BUFF://某阵营增加buff
                buff.writeByte(this.cmapId);
                buff.writeByte(this.characterType);
                if (uniqueIDs == null || uniqueIDs.size() == 0) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) uniqueIDs.size());
                    for (String uniqueId : uniqueIDs) {
                        buff.writeString(uniqueId);
                    }
                }
                buff.writeInt(this.buffId);
                buff.writeInt(this.level);
                buff.writeInt(this.instanceId);
                break;
            case ORDER_TYPE_REMOVE_CAMP_BUFF://某阵营移除buff
                buff.writeByte(this.cmapId);
                if (uniqueIDs == null || uniqueIDs.size() == 0) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) uniqueIDs.size());
                    for (String uniqueId : uniqueIDs) {
                        buff.writeString(uniqueId);
                    }
                }
                buff.writeInt(this.instanceId);
                break;
            case ORDER_TYPE_MOVE_CHARAC:
                buff.writeString(uniqueId);
                buff.writeString(position);
                break;
            case ORDER_TYPE_STOP_CHARAC:
                buff.writeString(uniqueId);
                break;
            case ORDER_TYPE_CHANGE_FIGHTSTATE:
                buff.writeByte(fightState);
                buff.writeInt(duration);
                break;
            case ORDER_TYPE_RESET_CHARACS:
                if (uniqueIDs == null || uniqueIDs.size() == 0) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) uniqueIDs.size());
                    for (String uniqueId : uniqueIDs) {
                        buff.writeString(uniqueId);
                    }
                }
                break;
            case ORDER_TYPE_ADD_BUFF:
                if (uniqueIDs == null || uniqueIDs.size() == 0) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) uniqueIDs.size());
                    for (String uniqueId : uniqueIDs) {
                        buff.writeString(uniqueId);
                    }
                }
                buff.writeInt(this.buffId);
                buff.writeInt(this.level);
                buff.writeInt(this.instanceId);
                break;
            case ORDER_TYPE_SETAI:
                if (uniqueIDs == null || uniqueIDs.size() == 0) {
                    buff.writeByte((byte) 0);
                } else {
                    buff.writeByte((byte) uniqueIDs.size());
                    for (String uniqueId : uniqueIDs) {
                        buff.writeString(uniqueId);
                    }
                }
                buff.writeByte(aiState);
                break;
            default:
        }
    }

    public byte getOrderType() {
        return orderType;
    }

    public void setOrderType(byte orderType) {
        this.orderType = orderType;
    }

    public byte getCmapId() {
        return cmapId;
    }

    public void setCmapId(byte cmapId) {
        this.cmapId = cmapId;
    }

    public Integer getBuffId() {
        return buffId;
    }

    public void setBuffId(Integer buffId) {
        this.buffId = buffId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return this.uniqueId;
    }

    public void setPosition(String posStr) {
        this.position = posStr;
    }

    public String getPosition() {
        return this.position;
    }

    public void setUniqueIDs(ArrayList<String> uniqueIds) {
        this.uniqueIDs = uniqueIds;
    }

    public ArrayList<String> getUniqueIDs() {
        return this.uniqueIDs;
    }

    public void setFightState(byte fightState) {
        this.fightState = fightState;
    }

    public byte getFightState() {
        return this.fightState;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getDuration() {
        return this.duration;
    }

    public byte getCharacterType() {
        return characterType;
    }

    public void setCharacterType(byte characterType) {
        this.characterType = characterType;
    }

    public byte getAiState() {
        return aiState;
    }

    public void setAiState(byte aiState) {
        this.aiState = aiState;
    }
}
