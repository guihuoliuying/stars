package com.stars.services.mail.userdata;

import com.google.gson.Gson;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.util.StringUtil;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class AllEmailPo extends DbRow {

    private static final int hashCode = new Long(0).hashCode();

    private int allEmailId;
    private int templateId; // 用了模板的才有
    private String title;
    private byte textMode; // 正文模式（0 - 服务端拼装，1 - 客户端拼装）
    private String text;
    private String params;
    private byte senderType;
    private long senderId;
    private String senderName;
    private String affixs;
    private int sendTime = (int) (System.currentTimeMillis() / 1000);
    private int coolTime;
    private int expireTime;
    private String conditionList;
    private Map<Integer, Integer> affixMap;
    private String[] paramsArray;
    private String channelIds;
    private Set<Integer> channelIdSet = new HashSet<>();

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "allemail", "allemailid=" + allEmailId);
    }

    @Override
    public String getDeleteSql() {
        return SqlUtil.getDeleteSql("allemail", "allemailid=" + allEmailId);
    }

    @Override
    public int hashCode() {
        return hashCode; // 强制发给全部人的邮件都用同一个Actor处理
    }

    public int getAllEmailId() {
        return allEmailId;
    }

    public void setAllEmailId(int allEmailId) {
        this.allEmailId = allEmailId;
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
        Objects.requireNonNull(params);
        this.params = params;
        this.paramsArray = params.split(",");
    }

    public String[] getParamsArray() {
        return paramsArray;
    }

    public void setParamsArray(String[] paramsArray) {
        if (paramsArray != null) {
            this.paramsArray = paramsArray;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < paramsArray.length; i++) {
                sb.append(paramsArray[i]);
                if (i < paramsArray.length - 1) {
                    sb.append(',');
                }
            }
            this.params = sb.toString();
        }
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
        this.affixs = StringUtil.makeString(affixMap, '+', '|');
        return affixs;
    }

    public void setAffixs(String affixs) {
        this.affixs = affixs;
        if (affixs != null && !"".equals(affixs.trim())) {
            this.affixMap = new HashMap<>();
            try {
                this.affixMap = StringUtil.toMap(affixs, Integer.class, Integer.class, '+', '|');
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Map<Integer, Integer> getAffixMap() {
        return affixMap;
    }

    public void setAffixMap(Map<Integer, Integer> affixMap) {
        this.affixMap = affixMap;
    }

    public int getSendTime() {
        return sendTime;
    }

    public void setSendTime(int sendTime) {
        this.sendTime = sendTime;
    }

    public int getCoolTime() {
        return coolTime;
    }

    public void setCoolTime(int coolTime) {
        this.coolTime = coolTime;
    }

    public int getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(int expireTime) {
        this.expireTime = expireTime;
    }

    public String getConditionList() {
        return conditionList;
    }

    public void setConditionList(String conditionList) {
        this.conditionList = conditionList;
    }

    public List<Map<String, String>> getCondition() {
        if (conditionList != null) {
            return new Gson().fromJson(conditionList, List.class);
        } else {
            return new ArrayList<>();
        }
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
