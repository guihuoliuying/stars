package com.stars.services.summary;

import com.stars.services.Service;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;
import com.stars.core.actor.invocation.annotation.DispatchAll;
import com.stars.core.actor.invocation.annotation.Timeout;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/10.
 */
public interface SummaryService extends ActorService, Service {

    @AsyncInvocation
    @DispatchAll
    void save();

    @AsyncInvocation
    void online(long roleId); //

    @AsyncInvocation
    void offline(long roleId); //

    @AsyncInvocation
    void updateSummaryComponent(long roleId, SummaryComponent component);

    @AsyncInvocation
    public void updateOfflineSummaryComponent(long roleId, SummaryComponent component);

    @AsyncInvocation
    void updateSummaryComponent(long roleId, Map<String, SummaryComponent> componentMap);

//    /**
//     * 更新单个属性；如果需要更新多个属性时，请不要使用这个方法
//     * @param roleId
//     * @param componentName
//     * @param fieldName
//     * @param fieldValue
//     */
//    @AsyncInvocation
//    void update(long roleId, String componentName, String fieldName, Object fieldValue);
//
//    /**
//     * 更新多个属性；通常情况下使用updateSummaryComponent就可以解决问题
//     * @param roleId
//     * @param componentName
//     * @param map
//     */
//    @AsyncInvocation
//    void update(long roleId, String componentName, Map<String, Object> map);
//
//    /**
//     * 更新人物单个属性；如果需要更新多个属性，请不要使用这个方法
//     * @param roleId
//     * @param fieldName
//     * @param fieldValue
//     */
//    @AsyncInvocation
//    void updateToRoleComp(long roleId, String fieldName, Object fieldValue);
//
//    /**
//     * 更新人物多个属性；通常情况下使用updateSummaryComponent就可以解决问题
//     * @param roleId
//     * @param map
//     */
//    @AsyncInvocation
//    void updateToRoleComp(long roleId, Map<String, Object> map);

    SummaryComponent getOnlineSummaryComponent(long roleId, String componentName);

    SummaryComponent getSummaryComponent(long roleId, String componentName);

    Summary getOnlineSummary(long roleId);

    Summary getSummary(long roleId);

    @DispatchAll
    @Timeout(timeout = 5000)
    List<Summary> getAllOnlineSummary();

    @DispatchAll
    @Timeout(timeout = 5000)
    List<Summary> getAllOfflineSummary();

    @DispatchAll
    @Timeout(timeout = 5000)
    List<Summary> getAllSummary(List<Long> roleIdList);

    @DispatchAll
    @Timeout(timeout = 5000)
    List<Summary> getAllOnlineSummary(List<Long> roleIdList);

//    /**
//     * 获取单个属性；如果需要获取多个属性时，请不要使用这个方法
//     * @param roleId
//     * @param componentName
//     * @param fieldName
//     * @return
//     */
//    Object getVal(long roleId, String componentName, String fieldName);
//
//    /**
//     * 从人物那里获取单个属性；如果需要获取多个属性时，请不要使用这个方法
//     * @param roleId 玩家id
//     * @param fieldName 属性名
//     * @return
//     */
//    Object getValFromRoleComp(long roleId, String fieldName);

}
