package com.stars.services.family.role;

import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncDelegate;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;

import java.util.Set;

/**
 * Created by zhaowenshuo on 2016/8/23.
 */
public interface FamilyRoleService extends Service, ActorService {

    @DispatchAll
    @AsyncInvocation
    void save();

    @AsyncInvocation
    void online(long roleId); // 上线

    @AsyncInvocation
    void offline(long roleId); // 下线

    @AsyncDelegate
    FamilyAuth getFamilyAuth(long roleId);

    long getFamilyId(long roleId); // 获取家族id

    @AsyncInvocation
    void setFamilyId(long roleId, long familyId); // 设置家族id

    boolean compareAndSetFamilyId(long roleId, long oldFamilyId, long newFamilyId);

    int getContribution(long roleId); // 获取帮贡

    boolean addAndSendContribution(long roleId, int delta); // 增加帮贡

    void multiplyAndSendContribution(long roleId, double factor); // 将帮贡乘以一个百分比

    @AsyncInvocation
    void innerNotifyVerification(long applicantId, long verifierId, long familyId, boolean isOk);

    @AsyncInvocation
    void sendRecommendationList(long roleId);

    @AsyncInvocation
    public void searchFamily(long roleId, String pattern);

    void addAppliedFamilyId(long roleId, long familyId);

    Set<Long> getAppliedFamilyIdSet(long roleId);

}
