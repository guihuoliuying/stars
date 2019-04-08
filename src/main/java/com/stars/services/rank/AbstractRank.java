package com.stars.services.rank;

import com.stars.core.dao.DbRowDao;
import com.stars.services.rank.userdata.AbstractRankPo;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by liuyuheng on 2016/8/23.
 */
public abstract class AbstractRank {
    protected int rankId;// 排行榜id,用于区分排行榜类型
    protected TreeSet<AbstractRankPo> treeSet;// 有限个数有序排行容器
    protected Map<Long, AbstractRankPo> cacheRankMap;// 角色排名

    protected volatile boolean isVolumeFull = false;// 容器是否已满

    /**
     * 发送排行榜
     *
     * @param roleId
     * @param list
     */
    protected abstract void sendRankList(long roleId, List<AbstractRankPo> list);

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
    protected List<AbstractRankPo> getFrontRankPo(int frontCount) {
        List<AbstractRankPo> list = new ArrayList<>();
        Iterator<AbstractRankPo> iterator = treeSet.iterator();
        for (int i = 0; i < frontCount && iterator.hasNext(); i++) {
            AbstractRankPo rankPo = iterator.next();
            list.add(rankPo);
        }
        return list;
    }

    protected void loadData() throws SQLException {
    }

    public AbstractRank() {
        treeSet = new TreeSet<>();
        cacheRankMap = new ConcurrentHashMap<>();
    }

    /**
     * 加入排行容器
     *
     * @param newCache
     */
    protected void addTreeSet(AbstractRankPo old, AbstractRankPo newCache) {
        if (isVolumeFull) {// 排序容器已满
            AbstractRankPo last = treeSet.last();
            if (old != null && treeSet.contains(old)) {// 在容器内
                if (newCache.compareTo(last) != 1) {// 大于等于末端节点,加入排序容器
                    addRankTreeSet(old, newCache);
                } else {// 比末端节点小,移除容器内旧的
                    treeSet.remove(old);
                    isVolumeFull = false;
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
                isVolumeFull = true;
            }
        }
        // 加入map
        updateCacheRankMap(newCache);
    }

    /**
     * 遍历容器,获得容器内准确排名
     */
    protected void sort() {
        int rank = 1;
        Iterator<AbstractRankPo> iterator = treeSet.iterator();
        while (iterator.hasNext()) {
            AbstractRankPo roleRankPo = iterator.next();
            roleRankPo.setRank(rank);
            cacheRankMap.put(roleRankPo.getUniqueId(), roleRankPo);
            rank++;
        }
    }

    /**
     * 移除角色排名数据缓存
     *
     * @param roleId
     */
    protected void removeCacheRank(long roleId) {
        cacheRankMap.remove(roleId);
    }

    /**
     * 移除角色排名数据缓存
     *
     * @param roleId
     */
    protected void removeCacheRank(long roleId, AbstractRankPo rankPo) {
        cacheRankMap.remove(roleId);
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
        return treeSet.contains(rankPo);
    }

    /**
     * 加入排序容器先移除原有的
     *
     * @param oldRankPo
     */
    protected void addRankTreeSet(AbstractRankPo oldRankPo, AbstractRankPo newRankPo) {
        if (oldRankPo != null) {
            boolean isRemove = treeSet.remove(oldRankPo);

//            LogUtil.info("isRemove:"+isRemove);
        }
        treeSet.add(newRankPo);
    }

    /**
     * 更新角色排名缓存
     *
     * @param roleRankPo
     */
    protected void updateCacheRankMap(AbstractRankPo roleRankPo) {
        cacheRankMap.put(roleRankPo.getUniqueId(), roleRankPo);
    }

    protected TreeSet<AbstractRankPo> getTreeSet() {
        return treeSet;
    }

    protected AbstractRankPo getRankPo(long uniqueId) {
        return cacheRankMap.get(uniqueId);
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }


}
