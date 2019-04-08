package com.stars.services.friend.memdata;

import com.stars.services.friend.userdata.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public class FriendData {

    private FriendRolePo rolePo;
    private FriendVigorPo friendVigor;      // 赠送/接收 体力相关记录
    private Map<Long, FriendPo> friendMap; // 好友列表
    private Map<Long, ContactsPo> contactsMap; // 联系人列表
    private Map<Long, BlackerPo> blackerMap; // 黑名单列表
    private Map<Long, FriendApplicationPo> applicationMap; // 收到的申请列表
    private List<SendFlowerRecordPo> sendFlowerList;        //送花记录
    private List<ReceiveFlowerRecordPo> receiveFlowerList;  //收花记录

    /* 内存数据 */
    private Map<Long, Long> appliedObjectMap = new HashMap<>(); // 已申请对象列表
    private boolean isOnline;

    public FriendRolePo getRolePo() {
        return rolePo;
    }

    public void setRolePo(FriendRolePo rolePo) {
        this.rolePo = rolePo;
    }

    public Map<Long, FriendPo> getFriendMap() {
        return friendMap;
    }

    public void setFriendMap(Map<Long, FriendPo> friendMap) {
        this.friendMap = friendMap;
    }

    public Map<Long, ContactsPo> getContactsMap() {
        return contactsMap;
    }

    public void setContactsMap(Map<Long, ContactsPo> contactsMap) {
        this.contactsMap = contactsMap;
    }

    public Map<Long, BlackerPo> getBlackerMap() {
        return blackerMap;
    }

    public void setBlackerMap(Map<Long, BlackerPo> blackerMap) {
        this.blackerMap = blackerMap;
    }

    public Map<Long, FriendApplicationPo> getApplicationMap() {
        return applicationMap;
    }

    public void setApplicationMap(Map<Long, FriendApplicationPo> applicationMap) {
        this.applicationMap = applicationMap;
    }

    public Map<Long, Long> getAppliedObjectMap() {
        return appliedObjectMap;
    }

    public void setAppliedObjectMap(Map<Long, Long> appliedObjectMap) {
        this.appliedObjectMap = appliedObjectMap;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public FriendVigorPo getFriendVigor() {
        return friendVigor;
    }

    public void setFriendVigor(FriendVigorPo friendVigor) {
        this.friendVigor = friendVigor;
    }

    public List<SendFlowerRecordPo> getSendFlowerList() {
        return sendFlowerList;
    }

    public void setSendFlowerList(List<SendFlowerRecordPo> sendFlowerList) {
        this.sendFlowerList = sendFlowerList;
    }

    public List<ReceiveFlowerRecordPo> getReceiveFlowerList() {
        return receiveFlowerList;
    }

    public void setReceiveFlowerList(List<ReceiveFlowerRecordPo> receiveFlowerList) {
        this.receiveFlowerList = receiveFlowerList;
    }
}
