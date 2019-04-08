package com.stars.services.mail.prodata;

import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/4.
 */
public class EmailTemplateVo {

    private int templateId;
    private byte senderType;
    private long senderId;
    private String senderName;
    private String title;
    private String text;
    private String affixs;

    private Map<Integer, Integer> affixMap;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAffixs() {
//        affixs = StringUtil.conveterToStrNotReturnNull(affixMap);
        affixs = StringUtil.makeString(affixMap, '+', '|');
        return affixs;
    }

    public void setAffixs(String affixs) {
        this.affixs = affixs;
        if (affixs != null && !affixs.trim().equals("") && !affixs.trim().equals("0")) {
            this.affixMap = new HashMap<>();
//            this.affixMap.putAll(StringUtil.conveterToMapInt(affixs, StringUtil.AND_STR));
            try {
                this.affixMap.putAll(StringUtil.toMap(affixs, Integer.class, Integer.class, '+', '|'));
            } catch (Exception e) {
                throw new RuntimeException(affixs, e);
            }
        }
    }

    public Map<Integer, Integer> getAffixMap() {
        return affixMap;
    }

    public void setAffixMap(Map<Integer, Integer> affixMap) {
        this.affixMap = affixMap;
    }
}
