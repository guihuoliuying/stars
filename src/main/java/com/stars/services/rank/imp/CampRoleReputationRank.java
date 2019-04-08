package com.stars.services.rank.imp;

import com.stars.core.dao.DbRowDao;
import com.stars.db.DBUtil;
import com.stars.modules.camp.CampManager;
import com.stars.modules.camp.prodata.CampCityVo;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.CampRoleReputationRankPo;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class CampRoleReputationRank extends AbstractRank {
    /**
     * <cityId,<roleid,rankpo>>
     */
    protected Map<Integer, Map<Long, AbstractRankPo>> cpCacheRankMap;// 角色排名
    /**
     * <cityId,boolean>
     */
    protected volatile Map<Integer, Boolean> cpIsVolumeFullMap;// 容器是否已满
    /**
     * <cityId,treeset>
     */
    private Map<Integer, TreeSet<AbstractRankPo>> treeSetMap;//各cp排行榜

    public CampRoleReputationRank() {
        treeSetMap = new ConcurrentHashMap<>();
        cpIsVolumeFullMap = new ConcurrentHashMap<>();
        cpCacheRankMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void loadData() throws SQLException {
        for (CampCityVo campCityVo : CampManager.campCityVoList) {
            treeSetMap.put(campCityVo.getId(), new TreeSet<AbstractRankPo>());
            cpIsVolumeFullMap.put(campCityVo.getId(), false);
            cpCacheRankMap.put(campCityVo.getId(), new ConcurrentHashMap<Long, AbstractRankPo>());
        }
        String sql = "select * from rolereputationrank;";
        List<CampRoleReputationRankPo> reputationRankPoList = DBUtil.queryList(DBUtil.DB_USER, CampRoleReputationRankPo.class, sql);
        for (CampRoleReputationRankPo reputationRankPo : reputationRankPoList) {
            addTreeSet(null, reputationRankPo);
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
        CampRoleReputationRankPo roleReputationRankPo = (CampRoleReputationRankPo) newRankPo;
        int cityId = roleReputationRankPo.getCityId();
        return treeSetMap.get(cityId);
    }

    private Boolean selectIsVolumeFull(AbstractRankPo newRankPo) {
        CampRoleReputationRankPo roleReputationRankPo = (CampRoleReputationRankPo) newRankPo;
        int cityId = roleReputationRankPo.getCityId();
        return cpIsVolumeFullMap.get(cityId);
    }

    private Map<Long, AbstractRankPo> selectCacheRankMap(AbstractRankPo newRankPo) {
        CampRoleReputationRankPo roleReputationRankPo = (CampRoleReputationRankPo) newRankPo;
        int cityId = roleReputationRankPo.getCityId();
        return cpCacheRankMap.get(cityId);
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
    protected List<AbstractRankPo> getFrontRankPo(int cityId, int frontCount) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(cityId);
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
        CampRoleReputationRankPo bestCPVoterRankPo = (CampRoleReputationRankPo) newCache;
        int cityId = bestCPVoterRankPo.getCityId();
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(newCache);
        Boolean isVolumeFull = selectIsVolumeFull(newCache);
        if (isVolumeFull) {// 排序容器已满
            AbstractRankPo last = treeSet.last();
            if (old != null && treeSet.contains(old)) {// 在容器内
                if (newCache.compareTo(last) != 1) {// 大于等于末端节点,加入排序容器
                    addRankTreeSet(old, newCache);
                } else {// 比末端节点小,移除容器内旧的
                    treeSet.remove(old);
                    cpIsVolumeFullMap.put(cityId, false);
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
                cpIsVolumeFullMap.put(cityId, true);
            }
        }
        // 加入map
        updateCacheRankMap(newCache);
    }

    /**
     * 遍历容器,获得容器内准确排名
     */
    protected void sort() {
        for (Map.Entry entry : treeSetMap.entrySet()) {
            int rank = 1;
            TreeSet<AbstractRankPo> treeSet = treeSetMap.get(entry.getKey());
            Map<Long, AbstractRankPo> cacheRankMap = cpCacheRankMap.get(entry.getKey());
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
     * @param roleId
     */
    protected void removeCacheRank(long roleId, int cityId) {
        cacheRankMap = cpCacheRankMap.get(cityId);
        cacheRankMap.remove(roleId);
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

    public TreeSet<AbstractRankPo> getTreeSet(int cityId) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(cityId);
        return treeSet;
    }

    protected AbstractRankPo getRankPo(long uniqueId) {
        return null;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public AbstractRankPo getRankPo(Integer cityId, long uniqueId) {
        Map<Long, AbstractRankPo> cacheRankMap = cpCacheRankMap.get(cityId);
        if (cacheRankMap == null) {
            return null;
        }
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
        Integer cityId = (Integer) args[0];
        Iterator<AbstractRankPo> iterator = treeSetMap.get(cityId).iterator();
        for (int i = 0; i < frontCount && iterator.hasNext(); i++) {
            AbstractRankPo rankPo = iterator.next();
            list.add(rankPo);
        }
        return list;
    }

    /**
     * 重置数据
     */
    public void reset() {
        LogUtil.info("声望排行榜重置");
        for (CampCityVo campCityVo : CampManager.campCityVoList) {
            treeSetMap.put(campCityVo.getId(), new TreeSet<AbstractRankPo>());
            cpIsVolumeFullMap.put(campCityVo.getId(), false);
            cpCacheRankMap.put(campCityVo.getId(), new ConcurrentHashMap<Long, AbstractRankPo>());
        }
        String sql = "delete from rolereputationrank;";
        try {
            DBUtil.execSql(DBUtil.DB_USER, sql);
        } catch (SQLException e) {
            LogUtil.error(e.getMessage(), e);
        }
    }
}
