//package com.stars.util.vowriter;
//
//import com.stars.server.main.business.fightSync.FightProdDataPool;
//import com.stars.server.main.business.fightSync.proddata.*;
//import com.stars.modules.bigmap.BigMapManager;
//import com.stars.modules.bigmap.prddata.MonsterVo;
//import com.stars.util.EmptyUtil;
//import com.stars.util.LogUtil;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileWriter;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//
///**
// * Created by zhangjiahua on 2016/3/24.
// */
//public class ResoucePrinter {
//
//    private String pathAndName = "./ResourceList.txt";
//
//    private static ResoucePrinter instance = new ResoucePrinter();
//
//    public static ResoucePrinter getInstance(){
//        return instance;
//    }
//
//    /**
//     * 把所有的产品数据关联的资源写成文件
//     * 各模块数据格子返回字符串,最后根据字符串写入文件中
//     */
//    public void writeResourceList(){
//        File file = createFile();
//        BufferedWriter writer = null;
//        try{
//            writer = new BufferedWriter(new FileWriter(file,true));
//            writer.write(writeMap());
//            writer.write(writeModel());
//            writer.write(writeCommonResource());
//            writer.write(writeSoundStr());
//            writer.flush();
//            writer.close();
//        }catch (Exception e){
//            LogUtil.error("输出资源目录错误",e);
//        }finally {
//            writer = null;
//        }
//    }
//
//    private File createFile(){
//        File file = new File(pathAndName);
//        if(file.exists()){
//            file.delete();
//        }
//        try{
//            file.createNewFile();
//        }catch (Exception e){
//            LogUtil.error("输出资源目录错误",e);
//        }
//        return file;
//    }
//
//    private String writeSoundStr(){
//        //加载所有的音效
//        StringBuilder builder = new StringBuilder();
//        Set<String> putted = new HashSet<>();
//        builder.append("\n音效资源:\n");
//        for(SoundProdData soundVo : FightProdDataPool.getInstance().getSoundMap().values()){
//            addToBuider(soundVo.getSoundName(),builder,putted);
//        }
//        return builder.append(";").toString();
//    }
//
//    /**
//     * 写入在FightCommonDefine中的资源
//     * @return
//     */
//    private String writeCommonResource(){
//        StringBuilder builder = new StringBuilder();
//        Map<String, String> defineMap = FightProdDataPool.getInstance().getDefineMap();
//        String hitBackEff = defineMap.get("hitbackeffect");
//        String missEffect = defineMap.get("misseffect");
//        String defenceEff = defineMap.get("defenseffect");
//        String breakeffect = defineMap.get("Breakeffect");
//        builder.append("\ncommonEff:\n")
//                .append(hitBackEff).append(";")
//                .append(missEffect).append(";")
//                .append(defenceEff).append(";")
//                .append(breakeffect).append(";");
//        return builder.append(";").toString();
//    }
//
//    /**
//     * 写入模型信息
//     */
//    private String writeModel(){
//        StringBuilder modelBuider = new StringBuilder();
//        modelBuider.append("\nmodel:");
//        Set<String> putted = new HashSet<>();
//
//        for(MonsterVo monsterVo : BigMapManager.getAllMonster().values()){
//            if(EmptyUtil.isEmpty(monsterVo.getModel()) || monsterVo.getModel().equals("0")){
//                continue;
//            }
//            addToBuider(monsterVo.getModel(),modelBuider,putted);
//        }
//        for(MonsterVo monsterVo : BigMapManager.getAllMonster().values()){
//            if(EmptyUtil.isEmpty(monsterVo.getModel()) || monsterVo.getModel().equals("0")){
//                continue;
//            }
//            addToBuider(monsterVo.getModel(),modelBuider,putted);
//        }
//
//
//        StringBuilder allBuilder = new StringBuilder();
//        return allBuilder.append("\n资源:").append(modelBuider.toString()).append(";").toString();
//    }
//
//    /**
//     * 写入到字符串中
//     * 写入前先判断是否已经被写入过了,否则不写入
//     */
//    private void addToBuider(String str,StringBuilder builder,Set<String> puttedStr){
//        if(puttedStr.contains(str)){
//            return;
//        }
//        builder.append(str).append("=");
//        puttedStr.add(str);
//    }
//
//    /**
//     * 写入Entity的资源
//     */
//    private String writeMap(){
//        Map<Integer,StageProdData> stageMap = FightProdDataPool.getInstance().getStageMap();
//        Set<String> putted = new HashSet<>();
//        StringBuilder mapBuider = new StringBuilder();
//        mapBuider.append("地图:\n");
//        for(StageProdData stageVo : stageMap.values()){
//            addToBuider(stageVo.getMap(),mapBuider,putted);
//            addToBuider(stageVo.getStagemap(),mapBuider,putted);
//        }
//        return mapBuider.append(";").toString();
//    }
//
//}
