package com.stars.modules.opactsecondskill.prodata;

import com.stars.modules.push.conditionparser.PushCondLexer;
import com.stars.modules.push.conditionparser.PushCondParser;
import com.stars.modules.push.conditionparser.node.PushCondNode;
import com.stars.util.StringUtil;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhanghaizhen on 2017/7/27.
 */
public class SecKillVo implements Comparable<SecKillVo>{
    private int id;
    private int group; //推送分组
    private byte groupCondition; //1则代表推送组基本条件，每一组只有一条
    private String condition; //精准推送条件
    private String time; //推送时间
    private String item; //推送的道具
    private int rechargeValue; //充值额度档位
    private int oldCost; //原价
    private int nowCost; //现价
    private int priority; //显示优先级
    private String discountIcon; //角标资源

    private PushCondNode condChecker;
    private long beginTimeMillis;
    private long endTimeMillis;
    private Map<Integer,Integer> itemMap = new HashMap<>();


    //内存内容


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public byte getGroupCondition() {
        return groupCondition;
    }

    public void setGroupCondition(byte groupCondition) {
        this.groupCondition = groupCondition;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
        this.condChecker = new PushCondParser(new PushCondLexer(condition)).parse();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) throws Exception{
        this.time = time;
        if (time != null && time.trim().length() > 0 && !time.trim().equals("0")) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.beginTimeMillis = sdf.parse(time.split("\\|")[0].trim()).getTime();
            this.endTimeMillis = sdf.parse(time.split("\\|")[1].trim()).getTime();
        }
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
        this.itemMap = StringUtil.toMap(item,Integer.class,Integer.class,'+','|');
    }

    public int getRechargeValue() {
        return rechargeValue;
    }

    public void setRechargeValue(int rechargeValue) {
        this.rechargeValue = rechargeValue;
    }

    public int getOldCost() {
        return oldCost;
    }

    public void setOldCost(int oldCost) {
        this.oldCost = oldCost;
    }

    public int getNowCost() {
        return nowCost;
    }

    public void setNowCost(int nowCost) {
        this.nowCost = nowCost;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getDiscountIcon() {
        return discountIcon;
    }

    public void setDiscountIcon(String discountIcon) {
        this.discountIcon = discountIcon;
    }

    public PushCondNode getCondChecker() {
        return condChecker;
    }

    public void setCondChecker(PushCondNode condChecker) {
        this.condChecker = condChecker;
    }
    public boolean matchPushTime(){
        long now = System.currentTimeMillis();
        return now > this.beginTimeMillis && now <this.endTimeMillis;
    }

    public Map<Integer, Integer> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<Integer, Integer> itemMap) {
        this.itemMap = itemMap;
    }

    public long getBeginTimeMillis() {
        return beginTimeMillis;
    }

    public void setBeginTimeMillis(long beginTimeMillis) {
        this.beginTimeMillis = beginTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public void setEndTimeMillis(long endTimeMillis) {
        this.endTimeMillis = endTimeMillis;
    }

    @Override
    public int compareTo(SecKillVo o) {
        return o.getPriority() - this.priority;
    }
}
