package com.stars.core.gmpacket.email.vo;

import com.google.gson.Gson;
import com.stars.core.gmpacket.email.util.EmailUtils;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/3/24.
 */
public class AllEmailGmPo extends DbRow {
    private long taskId;
    private int serverId;
    private int status;
    private String itemDict;
    private String conditionList;
    private String content;
    private Integer coolTime;
    private Integer expireTime;
    private Date createTime;
    private String channelIds;
    private static final int hashCode = new Long(0).hashCode();

    private int allEmailGmId;
    private int templateId; // 用了模板的才有
    private String title;
    private byte textMode; // 正文模式（0 - 服务端拼装，1 - 客户端拼装）
    private String text;
    private String params;
    private byte senderType;
    private long senderId;
    private String senderName;
    private int sendTime = (int) (System.currentTimeMillis() / 1000);
    private Map affixMap;
    private String[] paramsArray;
    private Set<Integer> channelIdSet;

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getItemDict() {
        return itemDict;
    }

    public void setItemDict(String itemDict) {
        this.itemDict = itemDict;
    }

    public int getAllEmailGmId() {
        return allEmailGmId;
    }

    public void setAllEmailGmId(int allEmailGmId) {
        this.allEmailGmId = allEmailGmId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public byte getTextMode() {
        return textMode;
    }

    public void setTextMode(byte textMode) {
        this.textMode = textMode;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public byte getSenderType() {
        return senderType;
    }

    public void setSenderType(byte senderType) {
        this.senderType = senderType;
    }

    public long getSenderId() {
        return senderId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getAffixs() {
        return itemDict;
    }

    public void setAffixs(String affixs) {
        this.itemDict = affixs;
    }

    public int getSendTime() {
        return sendTime;
    }

    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public Map<Integer, Integer> getAffixMap() {
        if (affixMap == null) {
            List list = new Gson().fromJson(itemDict, List.class);
            affixMap = EmailUtils.toToolMap(list);
        }
        return affixMap;
    }


    public String[] getParamsArray() {
        return paramsArray;
    }

    public void setParamsArray(String[] paramsArray) {
        this.paramsArray = paramsArray;
    }

    public String getConditionList() {
        return conditionList;
    }

    public void setConditionList(String conditionList) {
        this.conditionList = conditionList;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(Integer coolTime) {
        this.coolTime = coolTime;
    }

    public Integer getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Integer expireTime) {
        this.expireTime = expireTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<Map<String, String>> getCondition() {
        if (conditionList != null) {
            return new Gson().fromJson(conditionList, List.class);
        } else {
            return new ArrayList<>();
        }
    }


    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "allemailgm", "allemailgmid=" + allEmailGmId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("allemailgm", "allemailgmid=" + allEmailGmId);
    }

    public String getChannelIds() {
        return channelIds;
    }

    public void setChannelIds(String channelIds) {
        this.channelIds = channelIds;
        try {
            channelIdSet = StringUtil.toHashSet(channelIds, Integer.class, ',');
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Set<Integer> getChannelIdSet() {
        return channelIdSet;
    }

}
