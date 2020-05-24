package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.role.RoleModule;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.PacketManager;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.userdata.FriendApplicationPo;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class ServerFriend extends PlayerPacket {

    public static final byte SUBTYPE_FRIEND_LIST = 0x00; // 好友列表

    public static final byte SUBTYPE_APPLICATION_LIST = 0x10; // 申请列表
    public static final byte SUBTYPE_APPLY = 0x11; // 申请
    public static final byte SUBTYPE_AGREE = 0x12; // 同意
    public static final byte SUBTYPE_REJECT = 0x13; // 拒绝
    public static final byte SUBTYPE_DELETE = 0x14; // 删除
    public static final byte SUBTYPE_AGREE_ALL = 0x15; // 同意全部
    public static final byte SUBTYPE_REJECT_ALL = 0x16; // 拒绝全部
    public static final byte SUBTYPE_SEND_VIGOR = 0x17; // 赠送体力
    public static final byte SUBTYPE_RECEIVE_VIGOR = 0x18; // 接收体力
    public static final byte SUBTYPE_ONE_KEY_SEND_VIGOR = 0x19; // 一键送体力
    public static final byte SUBTYPE_ONE_KEY_RECEIVE_VIGOR = 0x20; // 一键收体力
    public static final byte SUBTYPE_SEND_FLOWER = 0x21; // 送花
    public static final byte SUBTYPE_FLOWER_RECORD_UI = 0x22; // 收/送 花记录界面
    public static final byte SUBTYPE_OPEN_SEND_FLOWER_UI = 0x23; // 送花选择界面

    private byte subtype;
    private long applicantId;
    private long objectId;
    private int itemId;
    private int count;

    @Override
    public void execPacket(Player player) {
        //清涛说，收/送 花记录界面和送花选择界面不受系统开放影响，所以提到前面处理
        if (subtype == SUBTYPE_FLOWER_RECORD_UI) {
            ServiceHelper.friendService().viewFriendFlowerUI(getRoleId());
            return;
        }
        if (subtype == SUBTYPE_OPEN_SEND_FLOWER_UI) {
            ServiceHelper.friendService().openSendFlowerUI(getRoleId());
            return;
        }
        if (subtype == SUBTYPE_SEND_FLOWER) {
            if (objectId==getRoleId()){
                PacketManager.send(player.id(), new ClientText("不能送给自己"));
                return;
            }
            FriendModule friendModule = module(MConst.Friend);
            friendModule.sendFlower(objectId, itemId, count);
            return;
        }
        switch (subtype) {
            case SUBTYPE_FRIEND_LIST:
                ServiceHelper.friendService().sendFriendList(getRoleId());
                break;
            case SUBTYPE_APPLICATION_LIST:
                ServiceHelper.friendService().sendReceivedApplicationList(getRoleId());
                break;
            case SUBTYPE_APPLY:
                ServiceHelper.friendService().applyFriend(getRoleId(), objectId, newFriendApplicantPo(objectId));
                break;
            case SUBTYPE_AGREE:
                ServiceHelper.friendService().agreeApplication(getRoleId(), applicantId);
                break;
            case SUBTYPE_REJECT:
                ServiceHelper.friendService().rejectApplication(getRoleId(), applicantId);
                break;
            case SUBTYPE_DELETE:
                ServiceHelper.friendService().deleteFriend(getRoleId(), objectId, true);
                break;
            case SUBTYPE_AGREE_ALL:
                ServiceHelper.friendService().agreeAllApplication(getRoleId());
                break;
            case SUBTYPE_REJECT_ALL:
                ServiceHelper.friendService().rejectAllApplication(getRoleId());
                break;
            case SUBTYPE_SEND_VIGOR:
                ServiceHelper.friendService().sendVigor(getRoleId(), objectId);
                break;
            case SUBTYPE_RECEIVE_VIGOR:
                ServiceHelper.friendService().receiveVigor(getRoleId(), objectId);
                break;
            case SUBTYPE_ONE_KEY_SEND_VIGOR:
                ServiceHelper.friendService().sendAllVigor(getRoleId());
                break;
            case SUBTYPE_ONE_KEY_RECEIVE_VIGOR:
                ServiceHelper.friendService().receiveAllVigor(getRoleId());
                break;
        }
    }

    @Override
    public short getType() {
        return FriendPacketSet.S_FRIEND;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        subtype = buff.readByte();
        switch (subtype) {
            case SUBTYPE_APPLY:
                objectId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_AGREE:
            case SUBTYPE_REJECT:
                applicantId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_DELETE:
                objectId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_SEND_VIGOR:
                objectId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_RECEIVE_VIGOR:
                objectId = Long.parseLong(buff.readString());
                break;
            case SUBTYPE_SEND_FLOWER:
                objectId = Long.parseLong(buff.readString());
                itemId = buff.readInt();
                count = buff.readInt();
                break;
        }
    }

    private FriendApplicationPo newFriendApplicantPo(long objectId) {
        RoleModule module = (RoleModule) module(MConst.Role);
        FriendApplicationPo applicationPo = new FriendApplicationPo();
        applicationPo.setApplicantId(getRoleId());
        applicationPo.setObjectId(objectId);
        applicationPo.setApplicantName(module.getRoleRow().getName());
        applicationPo.setApplicantJobId(module.getRoleRow().getJobId());
        applicationPo.setApplicantLevel(module.getRoleRow().getLevel());
        applicationPo.setAppliedTimestamp(now());
        return applicationPo;
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public byte getSubtype() {
        return subtype;
    }

    public void setSubtype(byte subtype) {
        this.subtype = subtype;
    }

    public long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(long applicantId) {
        this.applicantId = applicantId;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }
}
