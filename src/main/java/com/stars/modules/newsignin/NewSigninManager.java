package com.stars.modules.newsignin;

import com.stars.modules.newsignin.prodata.SigninVo;

import java.util.*;

/**
 * Created by chenkeyu on 2017/2/5 17:48
 */
public class NewSigninManager {
    private static Map<String, SigninVo> singleSignMap = new HashMap<>();//<yyyy-mm-dd,SigninVo>
    private static Map<String, Map<Integer, SigninVo>> accumulateAwardMap = new HashMap<>();//<yyyy-mm,<times,SigninVo>>
    private static Map<String, Map<Integer, SigninVo>> specialAwardMap = new HashMap<>();//<yyyy-mm,<times,SigninVo>>
    private static Map<Integer, Map<Integer, Integer>> reSignCostMap = new HashMap<>();//<times,<itemId,count>>
    private static int ServerOpenDays;
    private static Map<Integer, String> signId2Date = new HashMap<>();

    public static SigninVo getSigninVo(String date) {
        return singleSignMap.get(date);
    }

    public static List<SigninVo> getSingleSignList(String date) {
        List<SigninVo> tempList = new ArrayList<>();
        for (Map.Entry<String, SigninVo> entry : singleSignMap.entrySet()) {
            if (entry.getKey().substring(0, 7).equals(date)) {
                tempList.add(entry.getValue());
            }
        }
        return tempList;
    }

    public static List<SigninVo> getAccAwardList(String date) {
        List<SigninVo> tempList = new ArrayList<>();
        for (Map.Entry<Integer, SigninVo> entry : accumulateAwardMap.get(date).entrySet()) {
            tempList.add(entry.getValue());
        }
        return tempList;
    }

    public static List<SigninVo> getSpecAwardList(String date) {
        List<SigninVo> tempList = new ArrayList<>();
        for (Map.Entry<Integer, SigninVo> entry : specialAwardMap.get(date).entrySet()) {
            tempList.add(entry.getValue());
        }
        return tempList;
    }

    public static Map<Integer, SigninVo> getAccumulateAwardMap(String date) {
        return accumulateAwardMap.get(date);
    }

    public static Map<Integer, SigninVo> getSpecialAwardMap(String date) {
        return specialAwardMap.get(date);
    }

    public static void setSingleSignMap(Map<String, SigninVo> singleSignMap) {
        NewSigninManager.singleSignMap = singleSignMap;
    }

    public static void setAccumulateAwardMap(Map<String, Map<Integer, SigninVo>> accumulateAwardMap) {
        NewSigninManager.accumulateAwardMap = accumulateAwardMap;
    }

    public static void setSpecialAwardMap(Map<String, Map<Integer, SigninVo>> specialAwardMap) {
        NewSigninManager.specialAwardMap = specialAwardMap;
    }

    public static int getServerOpenDays() {
        return ServerOpenDays;
    }

    public static void setServerOpenDays(int serverOpenDays) {
        ServerOpenDays = serverOpenDays;
    }

    public static Map<Integer, Map<Integer, Integer>> getReSignCostMap() {
        return reSignCostMap;
    }

    public static Map<Integer,Integer> getReqItemMap(int times){
        TreeSet<Integer> timeSet = new TreeSet<>();
        for (Integer time : reSignCostMap.keySet()) {
            timeSet.add(time);
        }
        for (Integer time : timeSet) {
            if (times <= time){
                return reSignCostMap.get(time);
            }
        }
        return null;
    }

    public static void setReSignCostMap(String reSignCost) {
        Map<Integer, Map<Integer, Integer>> reSignCostMap = new HashMap<>();
        String[] timesItemCountStr = reSignCost.split("\\|");
        for (String timesItemCount : timesItemCountStr) {
            int times = Integer.parseInt(timesItemCount.split(",")[0]);
            Map<Integer, Integer> itemMap = reSignCostMap.get(times);
            if (itemMap == null) {
                itemMap = new HashMap<>();
                reSignCostMap.put(times, itemMap);
            }
            String[] itemCountStr = timesItemCount.split(",")[1].split("\\+");
            itemMap.put(Integer.parseInt(itemCountStr[0]), Integer.parseInt(itemCountStr[1]));
        }
        NewSigninManager.reSignCostMap = reSignCostMap;
    }

    public static Map<Integer, String> getSignId2Date() {
        return signId2Date;
    }

    public static void setSignId2Date(Map<Integer, String> signId2Date) {
        NewSigninManager.signId2Date = signId2Date;
    }
}
