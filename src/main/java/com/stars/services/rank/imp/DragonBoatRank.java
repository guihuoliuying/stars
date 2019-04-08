package com.stars.services.rank.imp;

import com.stars.core.dao.DbRowDao;
import com.stars.db.DBUtil;
import com.stars.modules.dragonboat.DragonBoatManager;
import com.stars.modules.dragonboat.event.DragonBoatHistorySendEvent;
import com.stars.modules.dragonboat.prodata.DragonBoatVo;
import com.stars.services.ServiceHelper;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.DragonBoatRankPo;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class DragonBoatRank extends AbstractRank {
    protected Map<Long, Map<Long, AbstractRankPo>> boatCacheRankMap;// 龙舟排名
    protected volatile Map<Long, Boolean> cpIsVolumeFullMap;// 容器是否已满
    private Map<Long, TreeSet<AbstractRankPo>> treeSetMap;//各cp排行榜

    public DragonBoatRank() {
        treeSetMap = new ConcurrentHashMap<>();
        cpIsVolumeFullMap = new ConcurrentHashMap<>();
        boatCacheRankMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void loadData() throws SQLException {
        String sql = "select * from rankdragonboat;";
        List<DragonBoatRankPo> dragonBoatRankPoList = DBUtil.queryList(DBUtil.DB_USER, DragonBoatRankPo.class, sql);
        for (DragonBoatRankPo dragonBoatRankPo : dragonBoatRankPoList) {
            addTreeSet(null, dragonBoatRankPo);
        }
    }

    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {

    }

    /**
     * 加入排序容器先移除原有的
     *
     * @param oldRankPo
     */
    @Override
    protected void addRankTreeSet(AbstractRankPo oldRankPo, AbstractRankPo newRankPo) {
        /**
         * 放到指定的排序集中
         */
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(newRankPo);
        if (oldRankPo != null) {
            treeSet.remove(oldRankPo);
        }
        treeSet.add(newRankPo);
    }

    private TreeSet<AbstractRankPo> selectTreeSet(AbstractRankPo newRankPo) {
        DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) newRankPo;
        Long stageTime = dragonBoatRankPo.getStageTime();
        TreeSet<AbstractRankPo> abstractRankPos = treeSetMap.get(stageTime);
        if (abstractRankPos == null) {
            abstractRankPos = new TreeSet<>();
            treeSetMap.put(stageTime, abstractRankPos);
        }
        return abstractRankPos;
    }

    private Boolean selectIsVolumeFull(AbstractRankPo newRankPo) {
        DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) newRankPo;
        Long stageTime = dragonBoatRankPo.getStageTime();
        Boolean isFull = cpIsVolumeFullMap.get(stageTime);
        if (isFull == null) {
            isFull = false;
            cpIsVolumeFullMap.put(stageTime, isFull);
        }
        return isFull;
    }

    private Map<Long, AbstractRankPo> selectCacheRankMap(AbstractRankPo newRankPo) {
        DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) newRankPo;
        Long stageTime = dragonBoatRankPo.getStageTime();
        Map<Long, AbstractRankPo> rankPoMap = boatCacheRankMap.get(stageTime);
        if (rankPoMap == null) {
            rankPoMap = new ConcurrentHashMap<>();
            boatCacheRankMap.put(stageTime, rankPoMap);
        }
        return rankPoMap;
    }


    /**
     * 每日重置
     */
    protected void dailyReset(DbRowDao rankDao) {

    }

    public Map<Long, AbstractRankPo> getCacheRankMap() {
        return cacheRankMap;
    }

    /**
     * 获得排行榜前n名
     * 提供默认实现；如果有需要，子类可重写
     *
     * @param frontCount
     * @return
     */
    protected List<AbstractRankPo> getFrontRankPo(int stageTime, int frontCount) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(stageTime);
        List<AbstractRankPo> list = new ArrayList<>();
        Iterator<AbstractRankPo> iterator = treeSet.iterator();
        for (int i = 0; i < frontCount && iterator.hasNext(); i++) {
            AbstractRankPo rankPo = iterator.next();
            list.add(rankPo);
        }
        return list;
    }


    /**
     * 加入排行容器
     *
     * @param newCache
     */
    protected void addTreeSet(AbstractRankPo old, AbstractRankPo newCache) {
        DragonBoatRankPo dragonBoatRankPo = (DragonBoatRankPo) newCache;
        Long stageTime = dragonBoatRankPo.getStageTime();
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(newCache);
        Boolean isVolumeFull = selectIsVolumeFull(newCache);
        if (isVolumeFull) {// 排序容器已满
            AbstractRankPo last = treeSet.last();
            if (old != null && treeSet.contains(old)) {// 在容器内
                if (newCache.compareTo(last) != 1) {// 大于等于末端节点,加入排序容器
                    addRankTreeSet(old, newCache);
                } else {// 比末端节点小,移除容器内旧的
                    treeSet.remove(old);
                    cpIsVolumeFullMap.put(stageTime, false);
                }
            } else if (newCache.compareTo(last) == -1) {// 不在容器内 && 比末端节点大,加入排序容器
                treeSet.remove(last);
                newCache.setRank(RankConstant.RANK_INIT);
                addRankTreeSet(old, newCache);
            }
        } else {// 容器未满,直接加入
            newCache.setRank(RankConstant.RANK_INIT);
            addRankTreeSet(old, newCache);
            if (treeSet.size() >= RankConstant.RANK_VOLUME_MAX) {
                cpIsVolumeFullMap.put(stageTime, true);
            }
        }
        // 加入map
        updateCacheRankMap(newCache);
    }

    /**
     * 遍历容器,获得容器内准确排名
     */
    protected void sort() {
        for (Map.Entry<Long, TreeSet<AbstractRankPo>> entry : treeSetMap.entrySet()) {
            int rank = 1;
            TreeSet<AbstractRankPo> treeSet = treeSetMap.get(entry.getKey());
            Map<Long, AbstractRankPo> cacheRankMap = boatCacheRankMap.get(entry.getKey());
            Iterator<AbstractRankPo> iterator = treeSet.iterator();
            while (iterator.hasNext()) {
                AbstractRankPo roleRankPo = iterator.next();
                roleRankPo.setRank(rank);
                cacheRankMap.put(roleRankPo.getUniqueId(), roleRankPo);
                rank++;
            }
        }
    }

    /**
     * 移除角色排名数据缓存
     *
     * @param dragonBoatId
     */
    protected void removeCacheRank(long dragonBoatId, String stageTime) {
        cacheRankMap = boatCacheRankMap.get(stageTime);
        cacheRankMap.remove(dragonBoatId);
    }

    /**
     * 移除角色排名数据缓存
     *
     * @param roleId
     */
    protected void removeCacheRank(long roleId, AbstractRankPo rankPo) {
        Map<Long, AbstractRankPo> cacheRankMap = selectCacheRankMap(rankPo);
        cacheRankMap.remove(roleId);
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(rankPo);
        if (rankPo != null && treeSet.contains(rankPo)) {
            treeSet.remove(rankPo);
        }
    }

    /**
     * 容器内存是否存在
     *
     * @param rankPo
     * @return
     */
    protected boolean contains(AbstractRankPo rankPo) {
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(rankPo);
        return treeSet.contains(rankPo);
    }


    /**
     * 更新角色排名缓存
     *
     * @param roleRankPo
     */
    protected void updateCacheRankMap(AbstractRankPo roleRankPo) {
        Map<Long, AbstractRankPo> cacheRankMap = selectCacheRankMap(roleRankPo);
        cacheRankMap.put(roleRankPo.getUniqueId(), roleRankPo);
    }

    protected TreeSet<AbstractRankPo> getTreeSet() {
        return treeSet;
    }

    public TreeSet<AbstractRankPo> getTreeSet(String stageTime) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(stageTime);
        return treeSet;
    }

    protected AbstractRankPo getRankPo(long uniqueId) {
        return null;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public AbstractRankPo getRankPo(Long stageTime, long uniqueId) {
        if (!boatCacheRankMap.containsKey(stageTime)) {
            ConcurrentHashMap<Long, AbstractRankPo> map = new ConcurrentHashMap<Long, AbstractRankPo>();
            boatCacheRankMap.put(stageTime, map);
        }
        Map<Long, AbstractRankPo> cacheRankMap = boatCacheRankMap.get(stageTime);
        return cacheRankMap.get(uniqueId);
    }

    /**
     * 获得排行榜前n名
     * 提供默认实现；如果有需要，子类可重写
     *
     * @param frontCount
     * @return
     */
    public List<AbstractRankPo> getFrontRankPo(int frontCount, Object... args) {
        List<AbstractRankPo> list = new ArrayList<>();
        Long stageTime = (Long) args[0];
        TreeSet treeSet = treeSetMap.get(stageTime);
        if (treeSet == null) {
            /**
             * 不按照流程来给予初始列表，防止客户端显示错误
             */
            for (DragonBoatVo dragonBoat : DragonBoatManager.dragonBoats) {
                DragonBoatRankPo rank = new DragonBoatRankPo(-1L, dragonBoat.getDragonBoatId(), 0, 0, 0);
                list.add(rank);
            }
            return list;
        }
        Iterator<AbstractRankPo> iterator = treeSet.iterator();
        for (int i = 0; i < frontCount && iterator.hasNext(); i++) {
            AbstractRankPo rankPo = iterator.next();
            list.add(rankPo);
        }

        return list;
    }

    /**
     * 获取所有排行榜时间阶段
     *
     * @return
     */
    public List<Long> getAllStageTimeList() {
        Set<Long> stageTimes = boatCacheRankMap.keySet();
        ArrayList<Long> stageTimeList = new ArrayList<>(stageTimes);
        Collections.sort(stageTimeList);
        return stageTimeList;
    }

    public Map<Long, TreeSet<AbstractRankPo>> getTreeSetMap() {
        return treeSetMap;
    }

    public void setTreeSetMap(Map<Long, TreeSet<AbstractRankPo>> treeSetMap) {
        this.treeSetMap = treeSetMap;
    }

    public void sendRankMap(long uniqueId, Map<Long, List<AbstractRankPo>> rankMap) {

        ServiceHelper.roleService().notice(uniqueId, new DragonBoatHistorySendEvent(rankMap));
    }
}
