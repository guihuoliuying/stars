package com.stars.services.family.main;

import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.memdata.FamilyPlaceholder;
import com.stars.services.family.main.userdata.FamilyApplicationPo;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.family.main.userdata.FamilyPo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public class FamilyData implements Cloneable {

    private FamilyPo familyPo;
    private Map<Long, FamilyMemberPo> memberPoMap;
    private Map<Long, FamilyApplicationPo> applicationPoMap;
    private Map<Long, FamilyPlaceholder> placeholderMap; // 占坑者
    private Map<Long, FamilyMemberPo> masterPoMap;//家族领导
    private int onlineCount;
    private int currentAssistantCount;
    private int currentElderCount;

    public FamilyData() {
    }

    public FamilyData(FamilyPo familyPo, Map<Long, FamilyMemberPo> memberPoMap, Map<Long, FamilyApplicationPo> applicationPoMap) {
        this.familyPo = familyPo;
        this.memberPoMap = memberPoMap;
        this.applicationPoMap = applicationPoMap;
        this.placeholderMap = new HashMap<>();
        this.masterPoMap = new HashMap<>();


    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        FamilyData other = new FamilyData();
        other.familyPo = (FamilyPo) familyPo.clone();
        other.memberPoMap = new HashMap<>();
        for (FamilyMemberPo memberPo : memberPoMap.values()) {
            other.memberPoMap.put(memberPo.getRoleId(), (FamilyMemberPo) memberPo.clone());
        }
        other.applicationPoMap = new HashMap<>();
        for (FamilyApplicationPo applicationPo : applicationPoMap.values()) {
            other.applicationPoMap.put(applicationPo.getRoleId(), (FamilyApplicationPo) applicationPo.clone());
        }
        other.placeholderMap = new HashMap<>(placeholderMap);
        for (FamilyPlaceholder placeholder : placeholderMap.values()) {
            other.placeholderMap.put(placeholder.getRoleId(), (FamilyPlaceholder) placeholder.clone());
        }
        other.masterPoMap = new HashMap<>(masterPoMap);
        for (FamilyMemberPo masterPo : masterPoMap.values()) {
            other.masterPoMap.put(masterPo.getRoleId(), (FamilyMemberPo) masterPo.clone());
        }
        other.onlineCount = onlineCount;
        other.currentAssistantCount = currentAssistantCount;
        other.currentElderCount = currentElderCount;
        return other;
    }

    public Map<Long, FamilyMemberPo> getMasterPoMap() {
        Map<Long, FamilyMemberPo> masterMap = new HashMap<>();
        for (Map.Entry<Long, FamilyMemberPo> entry : this.memberPoMap.entrySet()) {
            if (entry.getValue().getPostId() == FamilyPost.MASTER_ID ||
                    entry.getValue().getPostId() == FamilyPost.ASSISTANT_ID ||
                    entry.getValue().getPostId() == FamilyPost.ELDER_ID) {
                masterMap.put(entry.getKey(), entry.getValue());
            }
        }
        return masterMap;
    }

    public FamilyPo getFamilyPo() {
        return familyPo;
    }

    public Map<Long, FamilyMemberPo> getMemberPoMap() {
        return memberPoMap;
    }

    public Map<Long, FamilyApplicationPo> getApplicationPoMap() {
        return applicationPoMap;
    }

    public Map<Long, FamilyPlaceholder> getPlaceholderMap() {
        return placeholderMap;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public void increaseOnlineCount() {
        this.onlineCount++;
    }

    public void decreaseOnlineCount() {
        this.onlineCount--;
    }

    public int getCurrentAssistantCount() {
        return currentAssistantCount;
    }

    public void setCurrentAssistantCount(int currentAssistantCount) {
        this.currentAssistantCount = currentAssistantCount;
    }

    public void increaseCurrentAssistantCount() {
        this.currentAssistantCount++;
    }

    public void decreaseCurrentAssistantCount() {
        this.currentAssistantCount--;
    }

    public int getCurrentElderCount() {
        return currentElderCount;
    }

    public void setCurrentElderCount(int currentElderCount) {
        this.currentElderCount = currentElderCount;
    }

    public void increaseCurrentElderCount() {
        this.currentElderCount++;
    }

    public void decreaseCurrentElderCount() {
        this.currentElderCount--;
    }


    public static void main(String[] args) throws CloneNotSupportedException {
        B b = new B(1, 2);
    }


}

class A {
    public int a;
}

class D implements Cloneable {
    public int d;

    public D(int d) {
        this.d = d;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

class B extends A implements Cloneable {
    public int b;
    public int[] c;
    public D d;

    public B(int a, int b) {
        this.a = a;
        this.b = b;
        this.c = new int[]{1, 2};
        this.d = new D(a + b);
    }

    public Object copy() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "(a=" + a + ", b=" + b + ", c=" + c + ", c'=" + Arrays.toString(c) + ", d=" + d + ")";
    }
}
