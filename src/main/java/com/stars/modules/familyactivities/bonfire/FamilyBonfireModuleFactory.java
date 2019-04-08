package com.stars.modules.familyactivities.bonfire;

import com.stars.bootstrap.SchedulerHelper;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.event.LoginSuccessEvent;
import com.stars.modules.family.event.FamilyAuthUpdatedEvent;
import com.stars.modules.familyactivities.bonfire.event.BonFireDropEvent;
import com.stars.modules.familyactivities.bonfire.event.BonfireActEvent;
import com.stars.modules.familyactivities.bonfire.listener.BonfireActEventListener;
import com.stars.modules.familyactivities.bonfire.listener.JoinFamilyListener;
import com.stars.modules.familyactivities.bonfire.listener.LoginSuccessEventListener;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyFireVo;
import com.stars.modules.familyactivities.bonfire.prodata.FamilyQuestion;

import java.sql.SQLException;
import java.util.Map;

/**
 * Created by zhouyaohui on 2016/10/8.
 */
public class FamilyBonfireModuleFactory extends AbstractModuleFactory<FamilyBonfireModule> {

    public FamilyBonfireModuleFactory() {
        super(new FamilyBonfirePacketSet());
    }

    @Override
    public FamilyBonfireModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new FamilyBonfireModule(id, self, eventDispatcher, map);
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(BonfireActEvent.class, new BonfireActEventListener(module));
        eventDispatcher.reg(FamilyAuthUpdatedEvent.class, new JoinFamilyListener(module));
        eventDispatcher.reg(LoginSuccessEvent.class, new LoginSuccessEventListener(module));
        eventDispatcher.reg(BonFireDropEvent.class, new BonfireActEventListener(module));
    }

    @Override
    public void loadProductData() throws Exception {
        loadFireVo();   //加载篝火等级产品数据
        loadQuestion(); //加载篝火提名
        loadConfig();   //加载config配置
    }

    private void loadFireVo() throws SQLException {
        String sql = "select * from familyfirelv";
        Map<Integer, FamilyFireVo> fireVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "level", FamilyFireVo.class, sql);
        FamilyBonfrieManager.setFamilyFireMap(fireVoMap);
    }

    private void loadQuestion() throws SQLException {
        String sql = "select * from familyquestion";
        Map<Integer, FamilyQuestion> questionVoMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "questionid", FamilyQuestion.class, sql);
        FamilyBonfrieManager.setFamilyQuestionMap(questionVoMap);
    }

    private void loadConfig() throws Exception {
        Map<Integer, String> configMap = DataManager.getActivityFlowConfig(3);
        BonfireActivityFlow flow = new BonfireActivityFlow();
        flow.init(SchedulerHelper.getScheduler(), configMap);

        String defaultExpStr = DataManager.getCommConfig("familyfiree_defaultexp","5+29999");
        String[] defaultExpArr = defaultExpStr.split("\\+");
        FamilyBonfrieManager.DEFAULT_LEVEL = Integer.parseInt(defaultExpArr[0]);
        FamilyBonfrieManager.DEFAULT_EXP = Integer.parseInt(defaultExpArr[1]);
        FamilyBonfrieManager.RED_EXP = DataManager.getCommConfig("familyfiree_redexp", 100);
        FamilyBonfrieManager.WOOD_REFRESH_CD = DataManager.getCommConfig("familyfiree_woodinterval", 20);
        FamilyBonfrieManager.WOOD_ID = DataManager.getCommConfig("familyfiree_wooditemid",20);
        FamilyBonfrieManager.QUESTIONS_INTERVAL = DataManager.getCommConfig("familyfiree_questionsinterval",20);
        FamilyBonfrieManager.QUESTIONS_COUNT = DataManager.getCommConfig("familyfiree_questionscount",20);
        FamilyBonfrieManager.DAILY_THROW_GOLD_COUNT = DataManager.getCommConfig("familyfiree_usegoldcount",20);

        String woodExpStr = DataManager.getCommConfig("familyfiree_woodexp","5+29999");
        String[] woodExpArr = woodExpStr.split("\\+");
        FamilyBonfrieManager.WOOD_EXP = Integer.parseInt(woodExpArr[0]);
        FamilyBonfrieManager.WOOD_DROP_GROUP = Integer.parseInt(woodExpArr[1]);

        String goldExpStr = DataManager.getCommConfig("familyfiree_goldexp","5+29999");
        String[] goldExpArr = goldExpStr.split("\\+");
        FamilyBonfrieManager.GOLD_EXP = Integer.parseInt(goldExpArr[0]);
        FamilyBonfrieManager.GOLD_DROP_GROUP = Integer.parseInt(goldExpArr[1]);
    }
}
