package com.stars.services.shop;

import com.stars.core.persist.DbRowDao;
import com.stars.core.db.DBUtil;
import com.stars.services.Service;
import com.stars.util.LogUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  业务比较简单，就不用actor了
 * Created by zhouyaohui on 2016/9/7.
 */
public class ShopServiceImp implements Service {

    private DbRowDao dao = new DbRowDao();
    private ConcurrentMap<Integer, ShopServerLimitRow> limitMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws Throwable {
        String sql = "select * from shopserverlimit";
        limitMap = (ConcurrentHashMap<Integer, ShopServerLimitRow>) DBUtil.queryConcurrentMap(DBUtil.DB_USER, "goodsid", ShopServerLimitRow.class, sql);
    }

    public void save() {
        LogUtil.debug("=== 定时保存全服限购数据");
        synchronized (dao) {
            dao.flush();
        }
    }

    /*public Map<Integer, Integer> getLimitMap() {
        *//**返回map副本，防止limitMap暴露出去*//*
        Map<Integer, Integer> map = new HashMap<>();
        for (ShopServerLimitRow row : limitMap.values()) {
            map.put(row.getGoodsId(), row.getTimes());
        }
        return map;
    }*/

    /**
     * 购买接口,不是真的购买，只是获取购买资格
     * @param goodsId
     * @param max
     * @return
     */
    public boolean buy(int goodsId, int count, int max) {
        ShopServerLimitRow row = limitMap.putIfAbsent(goodsId, new ShopServerLimitRow(goodsId));
        if (row == null) {  // 首次添加
            row = limitMap.get(goodsId);
        }
        synchronized (dao) {
            if (row.getTimes() == 0) {
                dao.insert(row);
            }
            if (row.getTimes() + count > max) return false;
            row.setTimes(row.getTimes() + count);
            dao.update(row);
            return true;
        }
    }

    /**
     * 根据商品id返回全服限购数量
     * @param goodsId
     * @return
     */
    public int getLimitByGoodsId(int goodsId) {
        ShopServerLimitRow times = limitMap.get(goodsId);
        return times == null ? 0 : times.getTimes();
    }
}
