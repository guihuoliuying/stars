package com.stars.modules.push.prodata;

import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import com.stars.core.expr.node.ExprNode;
import com.stars.modules.base.condition.BaseExprConfig;
import com.stars.modules.push.trigger.PushTriggerSet;
import com.stars.util.LogUtil;

import java.text.SimpleDateFormat;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PushVo {

    /* 数据库数据 */
    private int pushId;
    private int order;
    private int group;
    private int groupRank;
    private int activityId;
    private String platform; // channel?
    private String trigger;
    private String condition;
    private String date;
    private String pushTimes;

    /* 内存数据 */
    private ExprNode condChecker;
    private PushTriggerSet triggerSet;
    private int type;
    private int times;
    private boolean hasExpirationDate = false;
    private long beginTimeMillis;
    private long endTimeMillis;

    public void init() throws Exception {
        try {
            this.condChecker = new ExprParser(new ExprLexer(condition), BaseExprConfig.config).parse();
            this.triggerSet = new PushTriggerSet(pushId, trigger);
            // pushtimes -> type + times
            this.type = Integer.parseInt(pushTimes.split("\\+")[0]);
            this.times = Integer.parseInt(pushTimes.split("\\+")[1]);
            // date -> yyyy-MM-dd HH:mm:ss | yyyy-MM-dd HH:mm:ss
            if (date != null && date.trim().length() > 0 && !date.trim().equals("0")) {
                this.hasExpirationDate = true;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                this.beginTimeMillis = sdf.parse(date.split("\\|")[0].trim()).getTime();
                this.endTimeMillis = sdf.parse(date.split("\\|")[1].trim()).getTime();
            }
        } catch (Exception e) {
            LogUtil.error("推送机制|pushId:{}|异常|trigger:{}|condition:{}", pushId, trigger, condition);
//            throw e;
        }
    }

    public int getPushId() {
        return pushId;
    }

    public void setPushId(int pushId) {
        this.pushId = pushId;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getGroupRank() {
        return groupRank;
    }

    public void setGroupRank(int groupRank) {
        this.groupRank = groupRank;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPushTimes() {
        return pushTimes;
    }

    public void setPushTimes(String pushTimes) {
        this.pushTimes = pushTimes;
    }

    public ExprNode getCondChecker() {
        return condChecker;
    }

    public PushTriggerSet getTriggerSet() {
        return triggerSet;
    }

    public int getType() {
        return type;
    }

    public int getTimes() {
        return times;
    }

    public boolean hasExpirationDate() {
        return hasExpirationDate;
    }

    public long getBeginTimeMillis() {
        return beginTimeMillis;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }
}
