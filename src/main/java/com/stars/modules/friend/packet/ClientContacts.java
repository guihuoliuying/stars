package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.MConst;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.userdata.ContactsPo;
import com.stars.services.summary.Summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public class ClientContacts extends PlayerPacket {

    public static final byte SUBTYPE_CONTACTS_LIST = 0x00;
    public static final byte SUBTYPE_CONTACTS_NOTIFY_ONLINE = 0x01;
    public static final byte SUBTYPE_CONTACTS_NOTIFY_OFFLINE = 0x02;
    public static final byte SUBTYPE_CONTACTS_NOTIFY_ADD = 0x03;
    public static final byte SUBTYPE_CONTACTS_NOTIFY_DEL = 0x04;

    private byte subtype;
    private long contactsId;
    private ContactsPo contactsPo;
    private Map<Long, ContactsPo> contactsMap;

    public ClientContacts() {

    }

    public ClientContacts(byte subtype) {
        this.subtype = subtype;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FriendPacketSet.C_CONTACTS;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {
        buff.writeByte(subtype);
        switch (subtype) {
            case SUBTYPE_CONTACTS_LIST:
                writeContactsList(buff);
                break;
            case SUBTYPE_CONTACTS_NOTIFY_ONLINE:case SUBTYPE_CONTACTS_NOTIFY_OFFLINE:
                writeContactsId(buff);
                break;
            case SUBTYPE_CONTACTS_NOTIFY_ADD:
                writeContactsPo(buff);
                break;
            case SUBTYPE_CONTACTS_NOTIFY_DEL:
                writeContactsId(buff);
                break;
        }
    }

    private void writeContactsList(com.stars.network.server.buffer.NewByteBuffer buff) {
        if (contactsMap == null) {
            buff.writeByte((byte) 0);
            return;
        }

        List<Summary> summaryList = ServiceHelper.summaryService().getAllSummary(new ArrayList<Long>(contactsMap.keySet()));
        Map<Long, Summary> summaryMap = new HashMap<>();
        for (Summary summary : summaryList) {
            summaryMap.put(summary.getRoleId(), summary);
        }

        buff.writeByte((byte) contactsMap.size()); // 联系人列表大小
        for (ContactsPo po : contactsMap.values()) {
            Summary summary = summaryMap.get(po.getContactsId()); // fixme: 处理为空的情况
            RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
            buff.writeString(Long.toString(po.getContactsId())); // 联系人roleId
            buff.writeInt(component.getRoleJob()); // 联系人jobId
            buff.writeString(po.getContactsName()); // 联系人名字
            buff.writeInt(component.getRoleLevel()); // 联系人等级
            buff.writeInt(component.getFightScore()); // 联系人战力
            buff.writeInt(summary.getOfflineTimestamp()); // 联系人离线时间
            buff.writeInt(po.getLastContactsTimestamp()); // 联系人最后联系时间
        }
    }

    private void writeContactsPo(com.stars.network.server.buffer.NewByteBuffer buff) {
        Summary summary = ServiceHelper.summaryService().getSummary(contactsPo.getContactsId()); // fixme: 处理为空的情况
        RoleSummaryComponent component = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        buff.writeString(Long.toString(contactsPo.getContactsId())); // 联系人roleId
        buff.writeInt(component.getRoleJob()); // 联系人jobId
        buff.writeString(contactsPo.getContactsName()); // 联系人名字
        buff.writeInt(component.getRoleLevel()); // 联系人等级
        buff.writeInt(component.getFightScore()); // 联系人战力
        buff.writeInt(summary.getOfflineTimestamp()); // 联系人离线时间
        buff.writeInt(contactsPo.getLastContactsTimestamp()); // 联系人最后联系时间
    }

    private void writeContactsId(NewByteBuffer buff) {
        buff.writeString(Long.toString(contactsId));
    }

    public Map<Long, ContactsPo> getContactsMap() {
        return contactsMap;
    }

    public void setContactsMap(Map<Long, ContactsPo> contactsMap) {
        this.contactsMap = contactsMap;
    }

    public long getContactsId() {
        return contactsId;
    }

    public void setContactsId(long contactsId) {
        this.contactsId = contactsId;
    }

    public ContactsPo getContactsPo() {
        return contactsPo;
    }

    public void setContactsPo(ContactsPo contactsPo) {
        this.contactsPo = contactsPo;
    }
}
