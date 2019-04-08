package com.stars.services.rank.imp;

import com.stars.core.dao.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.db.DBUtil;
import com.stars.modules.bestcp520.BestCPManager;
import com.stars.modules.bestcp520.packet.ClientBestCPPacket;
import com.stars.modules.bestcp520.prodata.BestCP;
import com.stars.services.rank.AbstractRank;
import com.stars.services.rank.RankConstant;
import com.stars.services.rank.userdata.AbstractRankPo;
import com.stars.services.rank.userdata.BestCPVoterRankPo;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by huwenjun on 2017/5/20.
 */
public class BestCpVoterRank extends AbstractRank {
    /**
     * <cpid,<roleid,rankpo>>
     */
    protected Map<Integer, Map<Long, AbstractRankPo>> cpCacheRankMap;// 角色排名
    /**
     * <cpid,boolean>
     */
    protected volatile Map<Integer, Boolean> cpIsVolumeFullMap;// 容器是否已满
    /**
     * <cpid,treeset>
     */
    private Map<Integer, TreeSet<AbstractRankPo>> treeSetMap;//各cp排行榜

    public BestCpVoterRank() {
        treeSetMap = new ConcurrentHashMap<>();
        cpIsVolumeFullMap = new ConcurrentHashMap<>();
        cpCacheRankMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void loadData() throws SQLException {
        for (BestCP bestCp : BestCPManager.bestCPMap.values()) {
            treeSetMap.put(bestCp.getCpId(), new TreeSet<AbstractRankPo>());
            cpIsVolumeFullMap.put(bestCp.getCpId(), false);
            cpCacheRankMap.put(bestCp.getCpId(), new ConcurrentHashMap<Long, AbstractRankPo>());
        }
        String sql = "select * from rankbestcpvoter;";
        List<BestCPVoterRankPo> bestCPVoterRankPos = DBUtil.queryList(DBUtil.DB_USER, BestCPVoterRankPo.class, sql);
        for (BestCPVoterRankPo bestCPVoterRankPo : bestCPVoterRankPos) {
            addTreeSet(null, bestCPVoterRankPo);
        }
    }

    @Override
    protected void sendRankList(long roleId, List<AbstractRankPo> list) {
        ClientBestCPPacket clientBestCPPacket = new ClientBestCPPacket(ClientBestCPPacket.BEST_CP_VOTER_RANK);
        clientBestCPPacket.setBestCPRankVoterList(list);
        clientBestCPPacket.setRoleId(roleId);
        PlayerUtil.send(roleId, clientBestCPPacket);
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
        BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) newRankPo;
        int cpId = bestCPVoterRankPo.getCpId();
        return treeSetMap.get(cpId);
    }

    private Boolean selectIsVolumeFull(AbstractRankPo newRankPo) {
        BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) newRankPo;
        int cpId = bestCPVoterRankPo.getCpId();
        return cpIsVolumeFullMap.get(cpId);
    }

    private Map<Long, AbstractRankPo> selectCacheRankMap(AbstractRankPo newRankPo) {
        BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) newRankPo;
        int cpId = bestCPVoterRankPo.getCpId();
        return cpCacheRankMap.get(cpId);
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
    protected List<AbstractRankPo> getFrontRankPo(int cpId, int frontCount) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(cpId);
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
        BestCPVoterRankPo bestCPVoterRankPo = (BestCPVoterRankPo) newCache;
        int cpId = bestCPVoterRankPo.getCpId();
        TreeSet<AbstractRankPo> treeSet = selectTreeSet(newCache);
        Boolean isVolumeFull = selectIsVolumeFull(newCache);
        if (isVolumeFull) {// 排序容器已满
            AbstractRankPo last = treeSet.last();
            if (old != null && treeSet.contains(old)) {// 在容器内
                if (newCache.compareTo(last) != 1) {// 大于等于末端节点,加入排序容器
                    addRankTreeSet(old, newCache);
                } else {// 比末端节点小,移除容器内旧的
                    treeSet.remove(old);
                    cpIsVolumeFullMap.put(cpId, false);
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
                cpIsVolumeFullMap.put(cpId, true);
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
    protected void removeCacheRank(long roleId, int cpId) {
        cacheRankMap = cpCacheRankMap.get(cpId);
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

    public TreeSet<AbstractRankPo> getTreeSet(int cpId) {
        TreeSet<AbstractRankPo> treeSet = treeSetMap.get(cpId);
        return treeSet;
    }

    protected AbstractRankPo getRankPo(long uniqueId) {
        return null;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public AbstractRankPo getRankPo(Integer cpId, long uniqueId) {
        Map<Long, AbstractRankPo> cacheRankMap = cpCacheRankMap.get(cpId);
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
        Integer cpId = (Integer) args[0];
        Iterator<AbstractRankPo> iterator = treeSetMap.get(cpId).iterator();
        for (int i = 0; i < frontCount && iterator.hasNext(); i++) {
            AbstractRankPo rankPo = iterator.next();
            list.add(rankPo);
        }
        return list;
    }
}
