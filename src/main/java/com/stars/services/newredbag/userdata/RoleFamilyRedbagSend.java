package com.stars.services.newredbag.userdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouyaohui on 2017/2/16.
 */
public class RoleFamilyRedbagSend {

    private long familyId;  // 家族id
    private String uniqueKey;   // 红包的唯一key
    private long senderId;  // 发送者id
    private String roleName;    // 名字
    private int jobId;      // 职业
    private int stamp;      // 发送的时间戳
    private int redbagId;   // 红包id
    private int itemId;     // 红包中item
    private int count;      // 红包数量
    private int value;      // 红包价值
    private int curIndex;   // 当前被抢的个数
    private String randStr; // 红包价值分配

    private List<Integer> randList;
    private Map<Long, RoleFamilyRedbagGet> record = new HashMap<>();

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public int getRedbagId() {
        return redbagId;
    }

    public void setRedbagId(int redbagId) {
        this.redbagId = redbagId;
    }

    public long getFamilyId() {
        return familyId;
    }

    public void setFamilyId(long familyId) {
        this.familyId = familyId;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public int getStamp() {
        return stamp;
    }

    public void setStamp(int stamp) {
        this.stamp = stamp;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getCurIndex() {
        return curIndex;
    }

    public void setCurIndex(int curIndex) {
        this.curIndex = curIndex;
    }

    public String getRandStr() {
        return randStr;
    }

    public void setRandStr(String randStr) {
        this.randStr = randStr;
        randList = new ArrayList<>();
        String[] rands = randStr.split("[+]");
        count = rands.length;
        for (String rand : rands) {
            randList.add(Integer.valueOf(rand));
        }
    }

    public int get() {
        if (curIndex >= count) {
            return 0;
        }
        return randList.get(curIndex);
    }

    public void record(RoleFamilyRedbagGet get) {
        record.put(get.getRoleId(), get);
    }

    public Map<Long, RoleFamilyRedbagGet> getRecord() {
        return record;
    }
}
