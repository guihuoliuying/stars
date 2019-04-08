package com.stars.modules.familyTask.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.familyTask.FamilyTaskModule;
import com.stars.modules.familyTask.FamilyTaskPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class ServerFamilyTask extends PlayerPacket {

    public static final byte REQ_VIEW_SELF_UI = 0x01; // 请求打开个人家族任务信息界面
    
    public static final byte REQ_VIEW_HELP_UI = 0x02;//打开家族任务求助信息界面
    
    public static final byte REQ_COMMIT_TASK = 0x03;//自己提交任务（提交材料，完成任务）
    
    public static final byte REQ_SEEK_HELP = 0x04;//求助
    
    public static final byte REQ_CANCEL_SEEK_HELP = 0x05;//取消求助
    
    public static final byte REQ_HELP_COMMIT = 0x06;//帮助提交

    public static final byte REQ_GET_AWARD = 0x07;//领取奖励
    
    public static final byte REQ_CHAT_TASK_INFO = 0x08;//点击聊天信息请求任务信息
    
    public static final byte REQ_CREATE_TASK = 0x09;//生成通用任务
    
    public byte subtype;
    
    public int taskId;//家族任务id
    
    public byte helpType;//帮助方式     1 元宝完成         2 材料提交
    
    public String beHelpRoleId;//被帮助者 玩家id
    
    public int nomalTaskId;//需生成的通用任务id

    @Override
    public void execPacket(Player player) {
        FamilyTaskModule familyTaskModule = (FamilyTaskModule) module(MConst.FamilyTask);
        switch (subtype) {
            case REQ_VIEW_SELF_UI:
                familyTaskModule.openSelfInfoUI(ClientFamilyTask.RESP_VIEW_SELF_UI);
                break;
            case REQ_VIEW_HELP_UI:
            	familyTaskModule.openSeekHelpUI();
            	break;
            case REQ_COMMIT_TASK:
            	familyTaskModule.commitTask(taskId);
            	break;
            case REQ_SEEK_HELP:
            	familyTaskModule.seekHelp(taskId);
            	break;
            case REQ_CANCEL_SEEK_HELP:
            	familyTaskModule.cancelSeekHelp(taskId);
            	break;
            case REQ_HELP_COMMIT:
            	familyTaskModule.helpCommit(Long.parseLong(beHelpRoleId), taskId, helpType);
            	break;
            case REQ_GET_AWARD:
            	familyTaskModule.getAward();
            	break;
            case REQ_CHAT_TASK_INFO:
            	familyTaskModule.chatGetSeekHelpInfo(Long.parseLong(beHelpRoleId), taskId);
            	break;
            case REQ_CREATE_TASK:
            	familyTaskModule.createFamilyTask(nomalTaskId);
            	break;
        }
    }

    @Override
    public short getType() {
        return FamilyTaskPacketSet.S_FAMILYTASK;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case REQ_VIEW_SELF_UI:
                break;
            case REQ_VIEW_HELP_UI:
                break;
            case REQ_COMMIT_TASK:
            	taskId = buff.readInt();
                break;
            case REQ_SEEK_HELP:
            	taskId = buff.readInt();
                break;
            case REQ_CANCEL_SEEK_HELP:
            	taskId = buff.readInt();
                break;
            case REQ_HELP_COMMIT:
            	beHelpRoleId = buff.readString();
            	taskId = buff.readInt();
            	helpType = buff.readByte();
                break;
            case REQ_GET_AWARD:
            	break;
            case REQ_CHAT_TASK_INFO:
            	beHelpRoleId = buff.readString();
            	taskId = buff.readInt();
            	break;
            case REQ_CREATE_TASK:
            	nomalTaskId = buff.readInt();
            	break;
        }
    }
}
