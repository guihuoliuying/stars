package com.stars.modules.changejob.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.changejob.ChangeJobModule;
import com.stars.modules.changejob.ChangeJobPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;

/**
 * Created by huwenjun on 2017/5/25.
 */
public class ServerChangeJobPacket extends PlayerPacket {
    private short subType;
    private int jobId;
    private static final int REQ_ACTIVITE_JOB = 1;
    private static final int REQ_CHANGE_JOB = 2;
    private static final int REQ_ACTIVED_JOBS = 3;
    private static final int REQ_ALL_JOBS = 4;
    private static final int REQ_SELECT_ROLE = 5;
    private static final int REQ_CONDITION_CHECK = 6;//条件检测

    @Override
    public void execPacket(Player player) {
        ChangeJobModule module = module(MConst.ChangeJob);
        switch (subType) {
            case REQ_ACTIVITE_JOB: {//激活
                module.activeJob(jobId);
            }
            break;
            case REQ_CHANGE_JOB: {//转职
                module.changeJob(jobId);
            }
            break;
            case REQ_ACTIVED_JOBS: {//请求角色被激活的数据
                module.sendActivedJobs();
            }
            break;
            case REQ_ALL_JOBS: {//请求产品转职数据
                module.sendAllJobs();
            }
            break;
            case REQ_SELECT_ROLE: {//请求前往选人界面数据
                module.gotoSelectRoleUI();
            }
            break;
            case REQ_CONDITION_CHECK: {//条件检测
                module.reqConditionCheck(jobId);
            }
            break;
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subType = buff.readShort();
        switch (subType) {
            case REQ_ACTIVITE_JOB: {
                jobId = buff.readInt();
            }
            break;
            case REQ_CHANGE_JOB: {
                jobId = buff.readInt();
            }
            break;
            case REQ_CONDITION_CHECK: {
                jobId = buff.readInt();
            }
            break;
        }
    }

    @Override
    public short getType() {
        return ChangeJobPacketSet.S_CHANGE_JOB;
    }
}
