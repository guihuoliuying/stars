package com.stars.modules.familyTask;

import com.stars.modules.familyTask.prodata.FamilyMissionGroup;
import com.stars.modules.familyTask.prodata.FamilyMissionInfo;

import java.util.List;
import java.util.Map;

/**
 * Created by wuyuxing on 2017/3/28.
 */
public class FamilyTaskManager {

    public static Map<Integer, FamilyMissionInfo> MissionInfoMap;
    public static int MISSION_TOTAL_ODDS;

    public static Map<Integer, FamilyMissionGroup> MissionGroupMap;
    public static Map<Integer,List<FamilyMissionGroup>> GroupListMap;
    public static Map<Integer,Integer> GroupOdds;
    
    public static byte familymission_count = 0;
    
    
    /**常量*/
    
    /*小任务状态*/
    public static final byte HAVE_NOT_COMMIT = 0;//未提交
    
    public static final byte SEEK_HELP = 1;//求助中
    
    public static final byte ALREADY_COMMIT = 2;//已提交
}
