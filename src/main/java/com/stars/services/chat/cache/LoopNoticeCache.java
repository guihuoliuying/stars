package com.stars.services.chat.cache;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by liuyuheng on 2017/2/14.
 */
public class LoopNoticeCache {
    private int noticeId;// 公告ID
    private String title;// 标题
    private String content;// 公告内容
    private long startTime;// 开始时间,时间戳,单位秒
    private long endTime;// 结束时间,时间戳,单位秒
    private int cycleInterval;// 循环间隔（秒）
    private int priority;// 优先级,数值越大,优先级越高

    /* 内存数据 */
    private List<Long> executeList;// 执行时间(ms)

    public LoopNoticeCache(String title, String content, long startTime, long endTime, int cycleInterval, int priority) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.cycleInterval = cycleInterval;
        this.priority = priority;
        this.executeList = new LinkedList<>();
        long executeTime = startTime * 1000L;
        long interval = cycleInterval * 1000L;
        for (; executeTime <= endTime * 1000L; executeTime = executeTime + interval) {
            executeList.add(executeTime);
        }
    }

    public void removeExecuteTime(List<Long> removes) {
        executeList.removeAll(removes);
    }

    public int getNoticeId() {
        return noticeId;
    }

    public void setNoticeId(int noticeId) {
        this.noticeId = noticeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getCycleInterval() {
        return cycleInterval;
    }

    public void setCycleInterval(int cycleInterval) {
        this.cycleInterval = cycleInterval;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Long> getExecuteList() {
        return executeList;
    }
}
