package com.stars.modules.drop;

import com.stars.modules.data.DataManager;
import com.stars.modules.drop.prodata.DropRewardVo;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/12/13.
 */
public class DropUtil {

    /**
     * 掉落外部入口
     *
     * @param dropId 掉落组Id
     * @param times  执行次数
     * @return 合并后的物品map itemId-number
     */
    public static Map<Integer, Integer> executeDrop(int dropId, int times) {
        Map<Integer, Integer> rewardMap = new HashMap<>();
        if (dropId == 0) {
            return rewardMap;
        }
        for (int i = 0; i < times; i++) {
            List<Map<Integer, Integer>> mapList = dropById(dropId, 0L, 0, 0, 0, "", false);
            // 合并掉落结果
            for (Map<Integer, Integer> map : mapList) {
                com.stars.util.MapUtil.add(rewardMap, map);
            }
        }
        return rewardMap;
    }

    public static Map<Integer, Integer> executeDrop(Set<Integer> dropIds) {
        Map<Integer, Integer> rewardMap = new HashMap<>();
        if (dropIds == null || dropIds.isEmpty()) {
            return rewardMap;
        }
        for (int dropId : dropIds) {
            List<Map<Integer, Integer>> mapList = dropById(dropId, 0L, 0, 0, 0, "", false);
            for (Map<Integer, Integer> map : mapList) {
                com.stars.util.MapUtil.add(rewardMap, map);
            }
        }
        return rewardMap;
    }

    public static List<Map<Integer, Integer>> executeDropNotCombine(int dropId, int times) {
        List<Map<Integer, Integer>> list = new LinkedList<>();
        if (dropId == 0) {
            return list;
        }
        for (int i = 0; i < times; i++) {
            list.addAll(dropById(dropId, 0L, 0, 0, 0, "", false));
        }
        return list;
    }

    /**
     * 根据掉落组、等级、职业获取showItem
     */
    public static Map<Integer, Integer> getShowItemByDropGroup(int dropGroup, int roleLevel, int roleJob, int fighting) {
        List<DropVo> list = DropManager.getDropGroup(dropGroup);
        if (StringUtil.isEmpty(list)) return null;

        Map<Integer, Integer> map = new HashMap<>();
        for (DropVo vo : list) {
            if (vo.isMatchCondition(roleJob, roleLevel, fighting)) {
                com.stars.util.MapUtil.add(map, vo.getShowItemMap());
            }
        }
        return map;
    }

    /**
     * 掉落外部入口
     *
     * @param dropGroup 掉落组Id
     * @param times     执行次数
     * @return 合并后的物品map itemId-number
     */
    public static Map<Integer, Integer> executeDrop(int dropGroup, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport, int times) {
        Map<Integer, Integer> rewardMap = new HashMap<>();
        if (dropGroup == 0) {
            return rewardMap;
        }
        for (int i = 0; i < times; i++) {
            List<Map<Integer, Integer>> mapList = drop(dropGroup, roleId, roleLevel, roleJob, fighting, roleName, showReport);
            // 合并掉落结果
            for (Map<Integer, Integer> map : mapList) {
                com.stars.util.MapUtil.add(rewardMap, map);
            }
        }
        return rewardMap;
    }

    public static List<Map<Integer, Integer>> executeDropNotCombine(int dropGroup, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport, int times) {
        List<Map<Integer, Integer>> list = new LinkedList<>();
        if (dropGroup == 0) {
            return list;
        }
        for (int i = 0; i < times; i++) {
            list.addAll(drop(dropGroup, roleId, roleLevel, roleJob, fighting, roleName, showReport));
        }
        return list;
    }

    /**
     * 执行掉落组掉落入口
     */
    private static List<Map<Integer, Integer>> drop(int dropGroup, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport) {
        List<Map<Integer, Integer>> mapList = new LinkedList<>();
        List<Map<Integer, Integer>> result;
        List<DropVo> list = DropManager.getDropGroup(dropGroup);
        if (StringUtil.isEmpty(list)) return mapList;

        for (DropVo vo : list) {
            if (!vo.isMatchCondition(roleJob, roleLevel, fighting)) continue;//不符合条件跳过
            result = dropById(vo.getDropId(), roleId, roleLevel, roleJob, fighting, roleName, showReport);
            if (StringUtil.isEmpty(result)) continue;
            mapList.addAll(result);
        }
        return mapList;
    }

    /**
     * 掉落逻辑执行入口
     */
    private static List<Map<Integer, Integer>> dropById(int dropId, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport) {
        DropVo dropVo = DropManager.getDropVo(dropId);
        if (dropVo == null) {
            LogUtil.error("找不到对应的掉落数据!!!!!!!!!!!!!dropId:" + dropId);
            return null;
        }
        List<Map<Integer, Integer>> list;
        switch (dropVo.getRandType()) {
            case DropManager.ITEM_REWARD_TYPE:
                list = oddsDrop(dropVo, roleId, roleLevel, roleJob, fighting, roleName, showReport);
                break;
            case DropManager.DROP_REWARD_TYPE:
                list = powerDrop(dropVo, roleId, roleLevel, roleJob, fighting, roleName, showReport);
                break;
            default:
                return null;
        }
        if (showReport && StringUtil.isNotEmpty(dropVo.getReportDesc()) && dropVo.getReportCode() != 0 && dropVo.getReportCount() != 0) {//需要传闻
            ItemVo itemVo = ToolManager.getItemVo(dropVo.getReportCode());
            if (itemVo != null) {
                Map<Integer, Integer> rewardMap = new HashMap<>();
                for (Map<Integer, Integer> map : list) {
                    MapUtil.add(rewardMap, map);
                }
                if (totalContain(rewardMap, dropVo.getReportCode(), dropVo.getReportCount())) {//掉落了传闻道具
                    String desc = String.format(DataManager.getGametext(dropVo.getReportDesc()), roleName, DataManager.getGametext(itemVo.getName()), String.valueOf(dropVo.getReportCount()));
                }
            }
        }
        return list;
    }

    private static boolean totalContain(Map<Integer, Integer> target, int code, int count) {
        if (StringUtil.isEmpty(target)) return false;
        Integer num = target.get(code);
        if (num == null || num < count) return false;
        return true;
    }

    /**
     * 概率随机类型掉落
     *
     * @param dropVo
     * @return
     */
    private static List<Map<Integer, Integer>> oddsDrop(DropVo dropVo, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport) {
        List<Map<Integer, Integer>> mapList = new LinkedList<>();
        List<DropRewardVo> rewardVoList = dropVo.getRewardList();
        Set<Integer> indexSet = new HashSet<>();// 下标集合
        for (int i = 0; i < dropVo.getRepeat(); i++) {
            for (int index = 0; index < rewardVoList.size(); index++) {
                // 需要去重&&已经随机到
                if (indexSet.contains(index)) {
                    continue;
                }
                DropRewardVo rewardVo = rewardVoList.get(index);
                // 满足概率
                if (oddsRandom(rewardVo)) {
                    mapList.addAll(getRewardTool(rewardVo, roleId, roleLevel, roleJob, fighting, roleName, showReport));
                    if (dropVo.getRemoveRepeat() == 1) {// 需要去除抽样
                        indexSet.add(index);
                    }
                }
            }
        }
        return mapList;
    }

    /**
     * 概率随机结果
     *
     * @param dropRewardVo
     * @return
     */
    private static boolean oddsRandom(DropRewardVo dropRewardVo) {
        int randomOdds = new Random().nextInt(1000) + 1;
        return randomOdds <= dropRewardVo.getPower();
    }

    /**
     * 权值随机类型掉落
     *
     * @param dropVo
     * @return
     */
    private static List<Map<Integer, Integer>> powerDrop(DropVo dropVo, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport) {
        List<Map<Integer, Integer>> mapList = new LinkedList<>();
        List<DropRewardVo> rewardVoList = dropVo.getRewardList();
        Set<Integer> indexSet = new HashSet<>();// 下标集合
        List<Map<Integer, Integer>> tmpList;
        for (int i = 0; i < dropVo.getRepeat(); i++) {
            DropRewardVo rewardVo = powerRandom(rewardVoList, indexSet, dropVo.getRemoveRepeat() == 1);
            tmpList = getRewardTool(rewardVo, roleId, roleLevel, roleJob, fighting, roleName, showReport);
            if (tmpList != null) {
                mapList.addAll(tmpList);
            }
        }
        return mapList;
    }

    /**
     * 权值随机结果
     *
     * @param rewardVoList
     * @param indexSet
     * @param removeRepeat 是否去除抽样 true=随机到后不再随机这个结果
     * @return
     */
    private static DropRewardVo powerRandom(List<DropRewardVo> rewardVoList, Set<Integer> indexSet, boolean removeRepeat) {
        int totalPower = 0;
        for (int i = 0; i < rewardVoList.size(); i++) {
            DropRewardVo rewardVo = rewardVoList.get(i);
            if (removeRepeat && indexSet.contains(i)) {
                continue;
            }
            totalPower = totalPower + rewardVo.getPower();
        }
        int flag = 0;
        int random = new Random().nextInt(totalPower) + 1;
        for (int i = 0; i < rewardVoList.size(); i++) {
            DropRewardVo rewardVo = rewardVoList.get(i);
            if (removeRepeat && indexSet.contains(i)) {
                continue;
            }
            flag = flag + rewardVo.getPower();
            if (random <= flag) {
                if (removeRepeat) {// 需要去除抽样
                    indexSet.add(i);
                }
                return rewardVo;
            }
        }
        return null;
    }

    /**
     * 随机成功后调用此方法,返回这个reward中的道具
     *
     * @return
     */
    private static List<Map<Integer, Integer>> getRewardTool(DropRewardVo rewardVo, long roleId, int roleLevel, int roleJob, int fighting, String roleName, boolean showReport) {
        if (rewardVo == null || rewardVo.getRewardId() == 0) {
            return null;
        }
        switch (rewardVo.getType()) {
            case 0:// 直接掉物品
                List<Map<Integer, Integer>> list = new LinkedList<>();
                Map<Integer, Integer> itemMap = new HashMap<>();
                itemMap.put(rewardVo.getRewardId(), rewardVo.getNumber());
                list.add(itemMap);
                return list;
            case 1:// 掉落类型,需要找到一个dropgroup再来递归
                return DropUtil.executeDropNotCombine(rewardVo.getRewardId(), roleId, roleLevel, roleJob, fighting, roleName, showReport, rewardVo.getNumber());

        }
        return null;
    }

}
