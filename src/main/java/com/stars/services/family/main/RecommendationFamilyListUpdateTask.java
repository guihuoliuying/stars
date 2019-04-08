package com.stars.services.family.main;

import com.stars.db.DBUtil;
import com.stars.modules.family.FamilyManager;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.memdata.RecommendationFamily;
import com.stars.services.family.main.prodata.FamilyLevelVo;
import com.stars.services.family.main.userdata.FamilyPo;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;

import static com.stars.modules.family.FamilyManager.levelVoMap;

/**
 * Created by zhaowenshuo on 2016/8/25.
 */
public class RecommendationFamilyListUpdateTask implements Runnable {

    @Override
    public void run() {
        try {
            List<RecommendationFamily> recommList = new LinkedList<>();
            Map<Long, RecommendationFamily> recommMap = new HashMap<>();
            // 从数据库捞
            try {
                List<FamilyPo> tmpFamilyList = DBUtil.queryList(DBUtil.DB_USER, FamilyPo.class,
                        "select * from `family` where `allowapplication`=1" + makeAvailableConditionString() + " order by `level`, `totalfightscore` desc limit 500");
                for (FamilyPo familyPo : tmpFamilyList) {
                    RecommendationFamily recomm = FamilyMainServiceActor.newRecommendationFamily(familyPo);
                    recommMap.put(recomm.getFamilyId(), recomm);
                }
            } catch (Exception e) {
                LogUtil.error("", e);
            }
            // 从在线捞
            List<RecommendationFamily> tmpRecommList = ServiceHelper.familyMainService().getOnlineRecommendationList();
            for (RecommendationFamily recomm : tmpRecommList) {
                if (recomm.getAllowApplication() == 1) {
                    recommMap.put(recomm.getFamilyId(), recomm);
                }
            }
            recommList.addAll(recommMap.values());
            // 移除已解散的家族
            Iterator<RecommendationFamily> iterator = recommList.iterator();
            while (iterator.hasNext()) {
                RecommendationFamily recomm = iterator.next();
                if (recomm.getMasterName() == null || recomm.getMasterName().equals("") || recomm.getMemberCount() <= 0) {
                    iterator.remove();
                }
            }
            // 排序
            Collections.sort(recommList);
            if (recommList.size() > 0) {
                FamilyMainServiceActor.recommList = new ArrayList<>(
                        recommList.subList(0, Math.min(recommList.size(), FamilyManager.familyRecomListLimit)));
            } else {
                FamilyMainServiceActor.recommList = new ArrayList<>();
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private static String makeAvailableConditionString() {
        if (levelVoMap.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(" and (");
        int count = 0, size = levelVoMap.size();
        for (FamilyLevelVo vo : levelVoMap.values()) {
            sb.append("`level`=").append(vo.getLevel())
                    .append(" and `membercount`<").append(vo.getMemberLimit());
            if (++count < size) {
                sb.append(" or ");
            }
        }
        sb.append(')');
        return sb.toString();
    }

    public static void main(String[] args) throws SQLException {
        LogUtil.init();
        DBUtil.init();
        levelVoMap = DBUtil.queryMap(
                DBUtil.DB_PRODUCT, "level", FamilyLevelVo.class, "select * from `familylevel`");
    }
}
