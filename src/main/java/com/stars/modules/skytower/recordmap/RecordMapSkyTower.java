package com.stars.modules.skytower.recordmap;

import com.stars.core.module.ModuleContext;
import com.stars.core.recordmap.RecordMap;
import com.stars.modules.data.DataManager;
import com.stars.modules.skytower.SkyTowerManager;
import com.stars.modules.skytower.SkyTowerModule;
import com.stars.modules.skytower.prodata.SkyTowerVo;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.DateUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * 角色的镇妖塔数据;
 * Created by panzhenfeng on 2016/8/10.
 */
public class RecordMapSkyTower{
    private int preDayMaxlayerId;
    private byte preDayMaxLayerIsPass = 0; //昨天的当前层 0未通关,1通关;
    private int curLayerId;
    private byte curLayerIsPass; //当前层 0未通关,1通关;
    //挑战奖励,可累加;
    private String preChallengeAwardLayerIds;
    //每日奖励层ID;
    private int dayRewardLayerId;
    //当天添加的挑战奖励;
    private String curFailChallengeAwardLayerIds;
    //用于记录今天是否进入过;
    private String dateStr = "0";
    //记录本周重置过的次数
    private int weeklyResetLayerCount;
    //记录最高达到过的塔的层数
    private int historyMaxLayerId;
    //记录上次重置塔重置重置次数的时间戳
    private long lastResetLayerTime;
    /**失败奖励的最大个数;*/
    private int failedAwardMaxCount = 0;
    /**每周重置层次的最大次数限制*/
    private int maxWeeklyResetLayerCount = 0;
    /**每周重置次数重置时间点*/
    private long thisWeekResetLayerTime;

    protected ModuleContext context;
    protected RecordMap recordMap;
    protected SkyTowerModule skyTowerModule = null;
    //第一次进来的日期;
    private String firstInDate;

    public RecordMapSkyTower(SkyTowerModule skyTowerModule, ModuleContext context) {
        this.skyTowerModule = skyTowerModule;
        this.context = context;
        this.recordMap = this.context.recordMap();
        initRecordMapData();
        failedAwardMaxCount = Integer.parseInt(DataManager.getCommConfig("skytower_failrewardnum"));
        maxWeeklyResetLayerCount = Integer.parseInt(DataManager.getCommConfig("skytower_reset_chance","1"));
        String resetTimeStr = DataManager.getCommConfig("skytower_reset_weektime","3,00:00:00");
        calThisWeekResetTimeStamp(resetTimeStr);

    }

    private void calThisWeekResetTimeStamp(String resetTimeStr){
        String[] array = resetTimeStr.split(",");
        int weekDay = Integer.parseInt(array[0]); //或者星期几
        String[] array1 = array[1].split(":");
        int hour = Integer.parseInt(array1[0]);
        int minute = Integer.parseInt(array1[1]);
        int second = Integer.parseInt(array1[2]);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, DateUtil.toDay(weekDay));// 获取星期三4点的时间,合区时间修改后更改为凌晨0点发
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE,minute);
        cal.set(Calendar.SECOND,second);
        thisWeekResetLayerTime = cal.getTimeInMillis();
    }

    /**
     * 是否今日第一次进入;
     */
    public boolean isTodayFirstIn(){
        String todayDateStr = DateUtil.getDateStr();
        if(this.dateStr.equals(todayDateStr)){
            return false;
        }
        return true;
    }


    public String getTodayDateStr(){
        Calendar cal = new GregorianCalendar();
        return DateUtil.toSimpleDateStr(cal);
    }

    /**
     * 标识今日进去过了;
     */
    public void setTodayStr(){
        this.dateStr = DateUtil.getDateStr();
        recordMap.setString("skytower.dateStr", dateStr);
    }

    //由外部调用,内部调用单一setString即可;
    private void encodeData(){
        recordMap.setString("skytower.preDayMaxLayerIsPass", Byte.toString(preDayMaxLayerIsPass));
        recordMap.setString("skytower.preDayMaxlayerId", Integer.toString(preDayMaxlayerId));
        recordMap.setString("skytower.curLayerId", Integer.toString(curLayerId));
        recordMap.setString("skytower.curLayerIsPass", Byte.toString(curLayerIsPass));
        recordMap.setString("skytower.preChallengeAwardLayerIds", preChallengeAwardLayerIds);
        recordMap.setString("skytower.dayRewardLayerId", Integer.toString(dayRewardLayerId));
        recordMap.setString("skytower.curFailChallengeAwardLayerIds", curFailChallengeAwardLayerIds);
        recordMap.setString("skytower.dateStr", dateStr);
        recordMap.setString("skytower.weeklyResetLayerCount", Integer.toString(weeklyResetLayerCount));
        recordMap.setString("skytower.historyMaxLayerId",Integer.toString(historyMaxLayerId));
        recordMap.setString("skytower.lastResetLayerTime",Long.toString(lastResetLayerTime));
    }

    private void decodeData(){
        this.preDayMaxLayerIsPass = Byte.parseByte(recordMap.getString("skytower.preDayMaxLayerIsPass", Byte.toString(preDayMaxLayerIsPass)));
        this.preDayMaxlayerId = Integer.parseInt(recordMap.getString("skytower.preDayMaxlayerId", Integer.toString(SkyTowerManager.getInitLayerId())));
        this.curLayerId = Integer.parseInt(recordMap.getString("skytower.curLayerId", Integer.toString(SkyTowerManager.getInitLayerId())));
        this.curLayerIsPass = Byte.parseByte(recordMap.getString("skytower.curLayerIsPass", "0"));
        this.preChallengeAwardLayerIds = recordMap.getString("skytower.preChallengeAwardLayerIds", "0");
        this.dayRewardLayerId = Integer.parseInt(recordMap.getString("skytower.dayRewardLayerId", "0"));
        this.curFailChallengeAwardLayerIds = recordMap.getString("skytower.curFailChallengeAwardLayerIds", "0");
        this.dateStr = recordMap.getString("skytower.dateStr", "0");
        this.weeklyResetLayerCount = Integer.parseInt(recordMap.getString("skytower.weeklyResetLayerCount","0"));
        this.historyMaxLayerId = Integer.parseInt(recordMap.getString("skytower.historyMaxLayerId",Integer.toString(curLayerId)));
        this.lastResetLayerTime = Long.parseLong(recordMap.getString("skytower.lastResetLayerTime","0"));
    }

    private void initRecordMapData(){
        //首次进来的日期;
        firstInDate = recordMap.getString("skytower.firstInDate", "0");
        if(firstInDate.equals("0")){
            firstInDate = getTodayDateStr();
            recordMap.setString("skytower.firstInDate", firstInDate);
        }
        decodeData();
        if(historyMaxLayerId == 0) { //新加最高层次的记录，初始化为当前层次
            historyMaxLayerId = curLayerId;
            setHistoryMaxLayerId(historyMaxLayerId);
        }

        this.skyTowerModule.syncSkyTowerRank(this.historyMaxLayerId);
    }

    public String addFailChallengeAwardLayerId(int layerId){
        if (curFailChallengeAwardLayerIds.equals("0")){
            this.setCurFailChallengeAwardLayerIds(Integer.toString(layerId));
        }else{
            this.setCurFailChallengeAwardLayerIds(this.curFailChallengeAwardLayerIds + "+" + layerId);
        }
        return  this.curFailChallengeAwardLayerIds;
    }

    /**
     * 移除所有的挑战失败奖励数据;
     */
    public void removeAllPreChallengeAwardLayerId() {
        this.preChallengeAwardLayerIds = "0";
        recordMap.setString("skytower.preChallengeAwardLayerIds", preChallengeAwardLayerIds);
    }

    /**
     * 将今日的奖励放置到之前的挑战奖励中累加;
     */
    public void setCurFailChallengeToPre(){
        if (preChallengeAwardLayerIds.equals("0")){
            preChallengeAwardLayerIds = curFailChallengeAwardLayerIds;
        }else {
            preChallengeAwardLayerIds = preChallengeAwardLayerIds + "+" + curFailChallengeAwardLayerIds;
        }
        setCurFailChallengeAwardLayerIds("0");
        recordMap.setString("skytower.preChallengeAwardLayerIds", preChallengeAwardLayerIds);
    }

    /**
     * 判断是否能增加失败奖励;
     * @return
     */
    public boolean isCanAddFailedAward(){
        boolean rtnValue = false;
        do{
            //今天一个挑战层都没有通过才能添加失败奖励;

            //判断之前的当前层是否已经通关了, 这个情况目前只在达到最高层才会出现;
            if(this.preDayMaxLayerIsPass == (byte)1){
                break;
            }
            //判断今天是否有通关层先;
            List<Integer> challengLayerIdList = SkyTowerManager.isHasChallengeLayerBetween(this.preDayMaxlayerId, this.curLayerId, true);
            if (challengLayerIdList.size() > 0){
                //判断这些挑战层是否是今天才通关的;
                int challengeLayerId = 0;
                boolean isBreakout = false;
                for(int i = 0, len = challengLayerIdList.size(); i<len; i++){
                    challengeLayerId = challengLayerIdList.get(i);
                    if(this.curLayerId > challengeLayerId){
                        isBreakout = true;
                        break;
                    }
                    if(challengeLayerId == this.preDayMaxlayerId &&
                            this.preDayMaxlayerId == this.curLayerId && this.preDayMaxLayerIsPass != this.curLayerIsPass){
                        isBreakout = true;
                        break;
                    }
                }
                if(isBreakout){
                    break;
                }
            }

            int tmpCount = 0;
            if (!preChallengeAwardLayerIds.equals("0")){
                tmpCount += preChallengeAwardLayerIds.split("\\+").length;
            }

            if (tmpCount >= failedAwardMaxCount){
                break;
            }
            rtnValue = true;
        }while(false);
        return rtnValue;
    }

    /**
     * 当前奖励是否有效;
     * @return
     */
    public boolean isCurFailChallengeAwardValid(){
        return !curFailChallengeAwardLayerIds.equals("0") && StringUtil.isNotEmpty(curFailChallengeAwardLayerIds);
    }


    /**
     * 获取挑战奖励的最后一项，没有返回空;
     * @return
     */
    public String getPreChallengeLastAwardLayerId(){
        //判断是否有有效值;
        if(this.preChallengeAwardLayerIds.equals("0") || !StringUtil.isNotEmpty(this.preChallengeAwardLayerIds)){
            String[] tmpArr = this.preChallengeAwardLayerIds.split("\\+");
            return  tmpArr[tmpArr.length-1];
        }
        return null;
    }

    public String getSumFailedAward(){
        String tmpStr = "";
        if(!this.preChallengeAwardLayerIds.equals("0") && StringUtil.isNotEmpty(this.preChallengeAwardLayerIds)){
            String[] tmpLayerIdArr = preChallengeAwardLayerIds.split("\\+");
            SkyTowerVo skyTowerVo = null;
            for(int i = 0, len = tmpLayerIdArr.length; i<len; i++){
                skyTowerVo = SkyTowerManager.getSkyTowerById(Integer.parseInt(tmpLayerIdArr[i]));
                tmpStr += skyTowerVo.getChallengeFailReward();
                if(i+1<len){
                    tmpStr += "|";
                }
            }
        }
//        if(!this.curFailChallengeAwardLayerIds.equals("0") && StringUtil.isNotEmpty(this.curFailChallengeAwardLayerIds)){
//            String[] tmpLayerIdArr = curFailChallengeAwardLayerIds.split("\\+");
//            SkyTowerVo skyTowerVo = null;
//            tmpStr += "|";
//            for(int i = 0, len = tmpLayerIdArr.length; i<len; i++){
//                skyTowerVo = SkyTowerManager.getSkyTowerById(Integer.parseInt(tmpLayerIdArr[i]));
//                tmpStr += skyTowerVo.getChallengeFailReward();
//                if(i+1<len){
//                    tmpStr += "|";
//                }
//            }
//        }
        return tmpStr;
    }

    public Map<Integer, Integer> getPreFailChallengeAwardMap(){
        if(this.preChallengeAwardLayerIds.equals("0") || !StringUtil.isNotEmpty(this.preChallengeAwardLayerIds)){
            return null;
        }
        Map<Integer, Integer> rtnMap = new HashMap<>();
        String[] tmpLayerIdArr = preChallengeAwardLayerIds.split("\\+");
        SkyTowerVo skyTowerVo = null;
        for(int i = 0, len = tmpLayerIdArr.length; i<len; i++){
            skyTowerVo = SkyTowerManager.getSkyTowerById(Integer.parseInt(tmpLayerIdArr[i]));
            if(skyTowerVo != null){
                MapUtil.add(rtnMap, skyTowerVo.getChallengeFailRewardsMap());
            }
        }
        return rtnMap;
    }

    public Map<Integer, Integer> getDayAwardMap(){
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(dayRewardLayerId);
        if(skyTowerVo != null){
            return  skyTowerVo.getDailyPassRewardsMap();
        }
        return null;
    }


    public String getDayAwardStr(){
        return getAwardStrByLayerId(dayRewardLayerId);
    }

    public String getAwardStrByLayerId(int layerId){
        SkyTowerVo skyTowerVo = SkyTowerManager.getSkyTowerById(layerId);
        if(skyTowerVo != null){
            return skyTowerVo.getDayReward();
        }
        return null;
    }

    public boolean isCanGetDayAward(){
        boolean isFirstInData = getTodayDateStr().equals(firstInDate);
        boolean isCanGetDayAwards = (!isFirstInData && dayRewardLayerId>0);
        return  isCanGetDayAwards;
    }

    public int getRemainResetLayerCount(){
        int remainCount = maxWeeklyResetLayerCount - weeklyResetLayerCount;
        return remainCount >= 0 ? remainCount : 0;
    }


    public void writeToBuff(NewByteBuffer buffer) {
        buffer.writeInt(curLayerId);
        buffer.writeByte(curLayerIsPass);
        //发送挑战胜利奖励字符串;
        SkyTowerVo nextChallengeSkyTowerVo = SkyTowerManager.getNextChallengeSkyTowerVo(historyMaxLayerId, true);
        buffer.writeString(nextChallengeSkyTowerVo.getChallengeSucReward());
        buffer.writeInt(nextChallengeSkyTowerVo.getLayerSerial());
        boolean isFirstInData = getTodayDateStr().equals(firstInDate);
        boolean isCanGetDayAwards = (!isFirstInData && dayRewardLayerId>0);
        int showDailyAwardLayerId = isCanGetDayAwards?dayRewardLayerId: curLayerId;
        buffer.writeString(getAwardStrByLayerId(showDailyAwardLayerId));
        buffer.writeByte(isCanGetDayAwards?(byte)0:(byte)1);
        buffer.writeInt(SkyTowerManager.getMaxLayerId());
        //发送当前累计的挑战失败奖励;
        buffer.writeString(getSumFailedAward());
        buffer.writeInt(getRemainResetLayerCount()); //剩余重置次数
        SkyTowerVo historyMaxSkyTowerVo = SkyTowerManager.getSkyTowerById(historyMaxLayerId);
        if (historyMaxSkyTowerVo != null){
            buffer.writeInt(historyMaxSkyTowerVo.getLayerSerial());
        }else{
            buffer.writeInt(0);
        }

    }

    public int getCurLayerId() {
        return curLayerId;
    }

    public void setCurLayerId(int curLayerId) {
        this.curLayerId = curLayerId;
        recordMap.setString("skytower.curLayerId", Integer.toString(curLayerId));

    }

    public int getPreDayMaxlayerId(){
        return preDayMaxlayerId;
    }

    public void setPreDayMaxlayerId(int preDayMaxlayerId){
        this.preDayMaxlayerId = preDayMaxlayerId;
        recordMap.setString("skytower.preDayMaxlayerId", Integer.toString(preDayMaxlayerId));
    }

    /**获取在layerId之前，离他最近的一层的失败奖励数据;*/
    public SkyTowerVo getPrestFailAwardSkyTowerVo(int layerId){
        int initLayerId = SkyTowerManager.getInitLayerId();
        SkyTowerVo skyTowerVo;
        Map<Integer, Integer> rtnMap;
        for(int i = layerId-1; i>=initLayerId; i--){
            skyTowerVo = SkyTowerManager.getSkyTowerById(i);
            rtnMap = skyTowerVo.getChallengeFailRewardsMap();
            if(rtnMap != null){
                return skyTowerVo;
            }
        }
        return null;
    }

    public void setNextLayerId(){
        int nextLayerId = curLayerId +1;
        int maxLayerId = SkyTowerManager.getMaxLayerId();
        if(nextLayerId > maxLayerId){
            return;
        }
        setCurLayerId(nextLayerId);
        setCurLayerIsPass((byte) 0);
        if(nextLayerId > historyMaxLayerId) { //记录新的最高记录
            historyMaxLayerId = nextLayerId;
            setHistoryMaxLayerId(historyMaxLayerId);
        }
    }

    public byte getCurLayerIsPass() {
        return curLayerIsPass;
    }

    public void setCurLayerIsPass(byte curLayerIsPass) {
        this.curLayerIsPass = curLayerIsPass;
        recordMap.setString("skytower.curLayerIsPass", Byte.toString(curLayerIsPass));
    }

    public void setDailyAwardLayerId(int layerId) {
        dayRewardLayerId = layerId;
        recordMap.setString("skytower.dayRewardLayerId", String.valueOf(dayRewardLayerId));
        skyTowerModule.updateRedPoints();
    }

    public int getDayRewardLayerId(){
        return dayRewardLayerId;
    }

    public String getCurFailChallengeAwardLayerIds(){
        return curFailChallengeAwardLayerIds;
    }

    public void setCurFailChallengeAwardLayerIds(String value){
        this.curFailChallengeAwardLayerIds = value;
        recordMap.setString("skytower.curFailChallengeAwardLayerIds", curFailChallengeAwardLayerIds);
    }

    public byte getPreDayMaxLayerIsPass() {
        return preDayMaxLayerIsPass;
    }

    public void setPreDayMaxLayerIsPass(byte preDayMaxLayerIsPass) {
        this.preDayMaxLayerIsPass = preDayMaxLayerIsPass;
        recordMap.setString("skytower.preDayMaxLayerIsPass", Byte.toString(preDayMaxLayerIsPass));
    }

    public int getWeeklyResetLayerCount() {
        return weeklyResetLayerCount;
    }

    public void setWeeklyResetLayerCount(int weeklyResetLayerCount) {
        this.weeklyResetLayerCount = weeklyResetLayerCount;
        recordMap.setString("skytower.weeklyResetLayerCount",Integer.toString(weeklyResetLayerCount));
        setLastResetLayerTime(System.currentTimeMillis());
    }

    public void addWeeklyResetLayerCount(){
        this.weeklyResetLayerCount ++;
        recordMap.setString("skytower.weeklyResetLayerCount",Integer.toString(this.weeklyResetLayerCount));
    }

    public int getHistoryMaxLayerId() {
        return historyMaxLayerId;
    }

    public boolean isHistoryPassed(){ //关卡以前通过过了
        return historyMaxLayerId > curLayerId;
    }

    public void setHistoryMaxLayerId(int historyMaxLayerId) {
        this.historyMaxLayerId = historyMaxLayerId;
        recordMap.setString("skytower.historyMaxLayerId",Integer.toString(historyMaxLayerId));
        this.skyTowerModule.syncSkyTowerRank(this.historyMaxLayerId);
    }

    public int getMaxWeeklyResetLayerCount() {
        return maxWeeklyResetLayerCount;
    }

    public void setMaxWeeklyResetLayerCount(int maxWeeklyResetLayerCount) {
        this.maxWeeklyResetLayerCount = maxWeeklyResetLayerCount;
    }

    public long getLastResetLayerTime() {
        return lastResetLayerTime;
    }

    public void setLastResetLayerTime(long lastResetLayerTime) {
        this.lastResetLayerTime = lastResetLayerTime;
        recordMap.setString("skytower.lastResetLayerTime",Long.toString(lastResetLayerTime));
    }

    public long getThisWeekResetLayerTime() {
        return thisWeekResetLayerTime;
    }

    public void setThisWeekResetLayerTime(long thisWeekResetLayerTime) {
        this.thisWeekResetLayerTime = thisWeekResetLayerTime;
    }
}
