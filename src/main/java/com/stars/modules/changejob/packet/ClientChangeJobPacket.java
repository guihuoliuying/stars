package com.stars.modules.changejob.packet;

import com.stars.modules.changejob.ChangeJobManager;
import com.stars.modules.changejob.ChangeJobPacketSet;
import com.stars.modules.changejob.prodata.ChangeJobVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/5/25.
 */
public class ClientChangeJobPacket extends Packet {
    private short subType;

    public static final short SEND_ACTIVED_JOBS = 1;//发送被激活的职业名单
    public static final short GO_TO_ACTIVE_VIEW = 2;//前往激活页面
    public static final short SEND_ALL_JOBS = 3;//发送转职产品数据列表
    public static final short SEND_CHANGE_JOB_SUCCESS = 4;//转职成功回调
    public static final short SEND_CHECK_CONDITION_SUCCESS = 5;//转职条件检测成功
    private List<Integer> activedJobs;
    private Long lastChangeTime;
    private Integer needActiveJobId;
    private Integer jobId;

    public ClientChangeJobPacket(short subType) {
        this.subType = subType;
    }

    public ClientChangeJobPacket() {
    }

    @Override
    public short getType() {
        return ChangeJobPacketSet.C_CHANGE_JOB;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeShort(subType);
        switch (subType) {
            case SEND_ACTIVED_JOBS: {
                writeActivedJobs(buff);
            }
            break;
            case GO_TO_ACTIVE_VIEW: {
                writeGoToActiveView(buff);
            }
            break;
            case SEND_ALL_JOBS: {
                writeAllJobs(buff);
            }
            break;
            case SEND_CHECK_CONDITION_SUCCESS:{
                buff.writeInt(jobId);
            }
        }
    }

    /**
     * 下发所有转职角色数据
     *
     * @param buff
     */
    private void writeAllJobs(com.stars.network.server.buffer.NewByteBuffer buff) {
        int size = ChangeJobManager.changeJobMap.size();
        buff.writeInt(size);
        for (Map.Entry<Integer, ChangeJobVo> entry : ChangeJobManager.changeJobMap.entrySet()) {
            ChangeJobVo changeJobVo = entry.getValue();
            buff.writeInt(changeJobVo.getJobType());
            buff.writeInt(changeJobVo.getViplevel());
            buff.writeString(changeJobVo.getReqitem());
            buff.writeInt(changeJobVo.getLevel());
            buff.writeString(changeJobVo.getReqJob());
            buff.writeInt(changeJobVo.getChangetime());
            buff.writeInt(changeJobVo.getChange());
            buff.writeString(changeJobVo.getImage());
        }
    }

    /**
     * 前往激活界面并选中指定职业
     *
     * @param buff
     */
    private void writeGoToActiveView(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(needActiveJobId);
    }

    /**
     * 下发被激活的职业
     *
     * @param buff
     */
    private void writeActivedJobs(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeLong(lastChangeTime);//上次转职时间,0:未转职过，其他则为时间戳
        buff.writeInt(activedJobs.size());
        for (Integer jobId : activedJobs) {
            buff.writeInt(jobId);
        }
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {

    }

    @Override
    public void execPacket() {

    }

    public List<Integer> getActivedJobs() {
        return activedJobs;
    }

    public void setActivedJobs(List<Integer> activedJobs) {
        this.activedJobs = activedJobs;
    }

    public Long getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(Long lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }

    public Integer getNeedActiveJobId() {
        return needActiveJobId;
    }

    public void setNeedActiveJobId(Integer needActiveJobId) {
        this.needActiveJobId = needActiveJobId;
    }

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }
}
