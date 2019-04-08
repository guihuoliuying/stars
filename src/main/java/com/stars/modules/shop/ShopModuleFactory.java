package com.stars.modules.shop;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.foreshow.event.ForeShowChangeEvent;
import com.stars.modules.shop.handle.TimeShopExtCondHandle;
import com.stars.modules.shop.listener.ShopListener;
import com.stars.modules.shop.prodata.Shop;
import com.stars.util.LogUtil;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhouyaohui on 2016/9/5.
 */
@DependOn({MConst.Data})
public class ShopModuleFactory extends AbstractModuleFactory<ShopModule> {

    public ShopModuleFactory() {
        super(new ShopPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        loadShopProduct();
    }

    /**
     * 加载商店产品数据
     */
    private void loadShopProduct() throws SQLException {
        String sql = "select * from shop";
        ConcurrentHashMap<Integer, Shop> shopMap = (ConcurrentHashMap<Integer, Shop>) DBUtil.queryConcurrentMap(DBUtil.DB_PRODUCT, "goodsid", Shop.class, sql);
        ConcurrentHashMap<Integer, Shop> fixedMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<Integer, Shop> randomMap = new ConcurrentHashMap<>();
        for (Shop item : shopMap.values()) {
            if (item.getWeight() == 0) {
                fixedMap.put(item.getGoodsId(), item);
            } else {
                randomMap.put(item.getGoodsId(), item);
            }
        }
        initExtendConditionHandle(shopMap);

        ShopManager.shopMap = shopMap;
        ShopManager.fixedMap = fixedMap;
        ShopManager.randomMap = randomMap;
        ShopManager.version++;
    }

    private void initExtendConditionHandle(Map<Integer, Shop> shopMap) {
        for (Shop item : shopMap.values()) {
            int type = item.getShopType();
            Class<? extends ExtendConditionHandle> clzz = ShopManager.handleMap.get(type);
            if (clzz == null) continue;
            try {
                ExtendConditionHandle handle = clzz.newInstance();
                handle.setShopVo(item);
                item.setHandle(handle);
            } catch (Exception e) {
                LogUtil.error("extendcondtionhandle 实例化失败", e);
            }
        }
    }

    @Override
    public void init() throws Exception {
        ShopManager.registerHandle(ShopManager.SHOPTYPE_TIMESHOP, TimeShopExtCondHandle.class);
    }

    @Override
    public ShopModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new ShopModule("商店", id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(ForeShowChangeEvent.class, new ShopListener((ShopModule) module));
    }
}
