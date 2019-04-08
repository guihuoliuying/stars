package com.stars.services.friend;

import com.stars.modules.MConst;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.ServiceHelper;
import com.stars.services.friend.memdata.RecommendationFriend;
import com.stars.services.summary.Summary;
import com.stars.util.LogUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/12.
 */
public class RecommendationFriendListUpdateTask implements Runnable {

    @Override
    public void run() {
        try {

            // 在线
            List<Summary> summaryList = ServiceHelper.summaryService().getAllOnlineSummary();
            Map<Integer, List<RecommendationFriend>> onlineMap = fillMap(summaryList);
            summaryList = ServiceHelper.summaryService().getAllOfflineSummary();
            Map<Integer, List<RecommendationFriend>> offlineMap = fillMap(summaryList);
            // 赋值
            FriendServiceActor.onlineCandidateMap = onlineMap;
            FriendServiceActor.offlineCandidateMap = offlineMap;
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private Map<Integer, List<RecommendationFriend>> fillMap(List<Summary> summaryList) {
        Map<Integer, List<RecommendationFriend>> map = new HashMap<>();
        for (Summary summary : summaryList) {
            RecommendationFriend roleMo = newRecommendationRoleMo(summary);
            if (roleMo == null)
                continue;
            ForeShowSummaryComponent openComp = (ForeShowSummaryComponent) summary.getComponent(MConst.ForeShow);
            if (!openComp.isOpen(ForeShowConst.FRIEND)) {
                continue;
            }
            List<RecommendationFriend> list = map.get(roleMo.getLevel());
            if (list == null) {
                list = new ArrayList<RecommendationFriend>();
                map.put(roleMo.getLevel(), list);
            }
            list.add(roleMo);
        }
        return map;
    }

    private RecommendationFriend newRecommendationRoleMo(Summary summary) {
        RecommendationFriend roleMo = new RecommendationFriend();
        roleMo.setRoleId(summary.getRoleId());
        RoleSummaryComponent roleComponent = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        if (roleComponent == null)
            return null;
        roleMo.setName(roleComponent.getRoleName());
        roleMo.setJobId(roleComponent.getRoleJob());
        roleMo.setLevel(roleComponent.getRoleLevel());
        roleMo.setFightScore(roleComponent.getFightScore());
        roleMo.setOfflineTimestamp(summary.getOfflineTimestamp());
        return roleMo;
    }

}
