package com.stars.modules.data;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.data.gm.SetOpenServerTimeHandle;
import com.stars.modules.data.prodata.ActivityFlowStepVo;
import com.stars.modules.data.prodata.GradeCoeffVo;
import com.stars.modules.gm.GmManager;
import com.stars.util.DirtyWords;
import com.stars.util.dirtyword.DirtyWordTire;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by liuyuheng on 2016/1/19.
 */
public class DataModuleFactory extends AbstractModuleFactory<DataModule> {

    public DataModuleFactory() {
        super(new DataPacketSet());
    }

    @Override
    public void init() throws Exception {
        /** 设置开服时间gm */
        GmManager.reg("setOpenServerTime", new SetOpenServerTimeHandle());
    }

    @Override
    public void loadProductData() throws Exception {
        loadCommonConfig();
        loadGameText();
        loadGradeCoeff();
        loadActivityFlowConfigMap();
        loadDirtyWord();
//        loadSensitiveWordExt1();
//        loadSensitiveWordRegex();
        DataManager.init();
       // compare();
    }

    @Override
    public DataModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new DataModule(id, self, eventDispatcher, map);
    }

    private void loadCommonConfig() throws Exception {
        String sql = "select * from `commondefine`;";
        Map<String, String> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "parameter", String.class, sql);
        if (map != null) {
            DataManager.commonConfigMap = map;
        }
    }

    private void loadGameText() throws SQLException {
        String sql = "select * from `gametext`;";
        Map<String, String> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "key", String.class, sql);
        if (map != null) {
            DataManager.gametextMap = map;
        }
    }

    private void loadGradeCoeff() throws SQLException{
        String sql = "select * from `gradecoeff`;";
        List<GradeCoeffVo> vos = DBUtil.queryList(DBUtil.DB_PRODUCT,GradeCoeffVo.class,sql);
        Map<String, Integer> gradecoeffMap = new HashMap<>();
        for(GradeCoeffVo vo : vos){
            gradecoeffMap.put(vo.getGrade()+"+"+vo.getTypeid(),vo.getCoeff());
        }
        // 最后赋值
        DataManager.gradecoeffMap = gradecoeffMap;
    }

    private void loadActivityFlowConfigMap() throws SQLException {
        String sql = "select * from `activityflow`";
        List<ActivityFlowStepVo> list = DBUtil.queryList(DBUtil.DB_PRODUCT, ActivityFlowStepVo.class, sql);
        Map<Integer, List<ActivityFlowStepVo>> map = new HashMap<>();
        for (ActivityFlowStepVo vo : list) {
            List<ActivityFlowStepVo> l = map.get(vo.getActivityId());
            if (l == null) {
                map.put(vo.getActivityId(), l = new ArrayList<>());
            }
            l.add(vo);
        }
        DataManager.activityFlowConfigMap = map;
    }

    private void loadDirtyWord() throws SQLException {
        DirtyWordTire tire = new DirtyWordTire();
        DirtyWordTire ext1Tire = new DirtyWordTire();
        Pattern pattern = null;

        /* 敏感词/正则 - 领导人名字 */
        String sqlRegex = "select * from `sensitivewordregex`";
        List<String> listRegex = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sqlRegex);
        StringBuilder sb = new StringBuilder();
        for(String word : listRegex) {
            sb.append(word).append("|");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        pattern = Pattern.compile(sb.toString());
        /* 敏感词/字典树 - 版署要求 */
        String sql = "select * from `sensitiveword`";
        List<String> list = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sql);
        for (String word : list) {
            tire.addDirtyWord(word);
        }
        /* 敏感词/字典树 - 策划 */
        String sqlExt1 = "select * from `sensitivewordext1`";
        List<String> listExt1 = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sqlExt1);
        for (String word : listExt1) {
            ext1Tire.addDirtyWord(word);
        }

        DirtyWords.pattern = pattern;
        DirtyWords.dirtyWordTire = tire;
        DirtyWords.dirtyWordExt1Tire = ext1Tire;
    }

//    private void loadSensitiveWordExt1() throws SQLException {
//        String sql = "select * from `sensitivewordext1`";
//        List<String> list = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sql);
//        for (String word : list) {
//            StringUtil.addSensitiveWordExt1(word);
//        }
//    }

    private void loadSensitiveWordRegex() throws SQLException{
        String sql = "select * from `sensitivewordregex`";
        List<String> list = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sql);
        StringBuilder sb = new StringBuilder();
        for(String word : list) {
            sb.append(word).append("|");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        DirtyWords.pattern = Pattern.compile(sb.toString());
    }

//    private void compare(){
//        String sql = "select * from `sensitiveword`";
//        try {
//            List<String> list = DBUtil.queryList(DBUtil.DB_PRODUCT, String.class, sql);
//            for (String str : list){
//                String string = StringUtil.replaceSensitiveWord(str);
//                for(int i=0;i<string.length();i++){
//                    if(string.charAt(i)!='*'){
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
}
