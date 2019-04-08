package com.stars.modules.chargeback;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.chargeback.prodata.BackRule;
import com.stars.modules.chargeback.userdata.AccountChargeBack;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/3/20.
 */
public class ChargeBackModuleFactory extends AbstractModuleFactory<ChargeBackModule> {

    public ChargeBackModuleFactory() {
        super(new ChargeBackPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<String, BackRule> backRuleMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "rulename", BackRule.class, "select * from backrule;");
        if (backRuleMap != null) {
            Map<Integer, Integer> moneyReward = new LinkedHashMap<>();
            /**
             * 初始化充值人民币返还礼包字典
             */
            BackRule packageRule = backRuleMap.get("package");
            if (packageRule != null) {
                String param = packageRule.getParam();
                String[] packageGroup = param.split("\\|");
                for (String packageReward : packageGroup) {
                    String[] reward = packageReward.split("\\+");
                    moneyReward.put(Integer.parseInt(reward[0]), Integer.parseInt(reward[1]));
                }
            }

            ChargeBackManager.backRuleMap = backRuleMap;
            ChargeBackManager.moneyReward = moneyReward;
        }

    }

    @Override
    public ChargeBackModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {

        return new ChargeBackModule(id, self, eventDispatcher, map);

    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
    }

    @Override
    public void init() throws Exception {
        super.init();
        String sql = "select * from accountchargeback ";
        ChargeBackManager.accountChargeBackMap = DBUtil.queryConcurrentMap(DBUtil.DB_COMMON, "account", AccountChargeBack.class, sql);
    }
}
