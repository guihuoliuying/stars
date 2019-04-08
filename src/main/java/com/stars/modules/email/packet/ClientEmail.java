package com.stars.modules.email.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.email.EmailPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class ClientEmail extends PlayerPacket {

    public static final byte C_GET_LIST = 1; // 获取邮件列表
    public static final byte C_DELETE = 3; // 删除邮件
    public static final byte C_FETCH_AFFIXS = 4; // 提取附件
    public static final byte C_ALL_DELETE = 5; // 全部删除
    public static final byte C_ALL_FETCH = 6; // 全部提取
    public static final byte C_NEW = 7; // 新邮件

    private byte sendType; // 子类型

    private Map<Integer, RoleEmailPo> emailPoList = new HashMap<>();
    private int emailId;
    private Map<Integer, Integer> affixsMap;
    private List<Integer> emailIdList;
    private List<Map<Integer, Integer>> affixsMapList;
    private int fetchFailureCount;

    public ClientEmail() {
    }

    public ClientEmail(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return EmailPacketSet.C_EMAIL;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(sendType); // 子类型
        switch (sendType) {
            case C_GET_LIST: // 1 - 获取邮件列表
                writeEmailList(buff);
                break;
            case C_DELETE:
                writeDeleteEmailId(buff);
                break;
            case C_FETCH_AFFIXS:
                writeFetchEmailId(buff);
                break;
            case C_ALL_DELETE:
                writeDeleteAllEmailId(buff);
                break;
            case C_ALL_FETCH:
                writeFetchAll(buff);
                break;
            case C_NEW:
                writeEmailList(buff);
                break;
        }
    }

    private void writeEmailList(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(emailPoList.size()); // 邮件列表大小
        for (RoleEmailPo emailPo : emailPoList.values()) {
            emailPo.writeToBuffer(buff);
        }
    }

    private void writeDeleteEmailId(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(emailId);
    }

    private void writeFetchEmailId(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(emailId);
    }

    private void writeDeleteAllEmailId(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeInt(emailIdList.size());
        for (Integer emailId : emailIdList) {
            buff.writeInt(emailId);
        }
    }

    private void writeFetchAll(NewByteBuffer buff) {
        buff.writeInt(emailIdList.size());
        for (Integer emailId : emailIdList) {
            buff.writeInt(emailId);
        }
        buff.writeInt(fetchFailureCount);
    }

    public void setEmailPoList(Map<Integer, RoleEmailPo> emailPoList) {
        this.emailPoList = emailPoList;
    }

    public void setEmailId(int emailId) {
        this.emailId = emailId;
    }

    public void setAffixsMap(Map<Integer, Integer> affixsMap) {
        this.affixsMap = affixsMap;
    }

    public void setEmailIdList(List<Integer> emailIdList) {
        this.emailIdList = emailIdList;
    }

    public void setFetchFailureCount(int fetchFailureCount) {
        this.fetchFailureCount = fetchFailureCount;
    }
}
