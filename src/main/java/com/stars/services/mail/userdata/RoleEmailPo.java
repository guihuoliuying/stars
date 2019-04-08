package com.stars.services.mail.userdata;

import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.db.SqlUtil;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by zhaowenshuo on 2016/7/16.
 */
public class RoleEmailPo extends DbRow implements Comparable<RoleEmailPo> {

    public static final byte TEXT_MODE_SERVER = 0; // 正文模式 - 服务端组装
    public static final byte TEXT_MODE_CLIENT = 1; // 正文模式 - 客户端组装

    private int emailId;
    private int templateId; // 用了模板的才有
    private int refEmailId; // 引用了全局邮件的才有
    private String title;
    private byte textMode; // 正文模式（0 - 服务端拼装，1 - 客户端拼装）
    private String text;
    private String params;
    private byte senderType;
    private long senderId;
    private String senderName;
    private long receiverId;
    private String affixs;
    private int sendTime = (int) (System.currentTimeMillis() / 1000);
    private int coolTime;
    private int emailType;//特殊邮件类型

    private byte isRead;
    private byte isGetAffixs;

    private boolean hasAffixs = false;
    private Map<Integer, Integer> affixMap;
    private String[] paramsArray;

    public RoleEmailPo() {
        this.textMode = TEXT_MODE_SERVER;
    }

    public int getEmailId() {
        return emailId;
    }

    public void setEmailId(int emailId) {
        this.emailId = emailId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getRefEmailId() {
        return refEmailId;
    }

    public void setRefEmailId(int refEmailId) {
        this.refEmailId = refEmailId;
    }

    public byte getTextMode() {
        return textMode;
    }

    public void setTextMode(byte textMode) {
        this.textMode = textMode;
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

    public boolean hasAffixs() {
        return hasAffixs;
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

    public long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public String getAffixs() {
//        this.affixs = StringUtil.conveterToStrNotReturnNull(affixMap);
        this.affixs = StringUtil.makeString(affixMap, '+', '|');
        return affixs;
    }

    public void setAffixs(String affixs) {
        this.affixs = affixs;
        if (affixs != null && !"".equals(affixs.trim())) {
            this.affixMap = new HashMap<>();
            hasAffixs = true;
            try {
                this.affixMap.putAll(StringUtil.toMap(affixs, Integer.class, Integer.class, '+', '|'));
            } catch (Exception e) {
                throw new RuntimeException("解析附件出错，affixs=" + affixs, e);
            }
        }
    }

    public Map<Integer, Integer> getAffixMap() {
        return affixMap;
    }

    public void setAffixMap(Map<Integer, Integer> affixMap) {
        this.affixMap = affixMap;
        if (this.affixMap != null && this.affixMap.size() > 0) {
            hasAffixs = true;
        }
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

	public int getEmailType() {
		return emailType;
	}

	public void setEmailType(int emailType) {
		this.emailType = emailType;
	}

	public byte getIsRead() {
        return isRead;
    }

    public void setIsRead(byte isRead) {
        this.isRead = isRead;
    }

    public boolean isRead() {
        return this.getIsRead() == 1;
    }

    public byte getIsGetAffixs() {
        return isGetAffixs;
    }

    public void setIsGetAffixs(byte isGetAffixs) {
        this.isGetAffixs = isGetAffixs;
    }

    public boolean isGetAffixs() {
        return this.isGetAffixs == 1;
    }

    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeInt((int) emailId); // 邮件id
        buff.writeByte(textMode); // 正文模式
        buff.writeString(title); // 标题
        buff.writeString(text); // 正文
        if (textMode == TEXT_MODE_CLIENT) { // 1 - 客户端组装
            int paramsArrayLen = paramsArray == null ? 0 : paramsArray.length;
            buff.writeByte((byte) paramsArrayLen);
            if (paramsArrayLen > 0) {
                for (String param : paramsArray) {
                    buff.writeString(param);
                }
            }
        }
        buff.writeString(senderName); // 发送人名字
        if (affixMap != null) {
            buff.writeByte((byte) affixMap.size()); // 附件列表大小
            for (Map.Entry<Integer, Integer> entry : affixMap.entrySet()) { // 附件内容
                buff.writeInt(entry.getKey()); // 道具id
                buff.writeInt(entry.getValue()); // 道具数据量
            }
        } else {
            buff.writeByte((byte) 0); // 附件列表大小
        }
        buff.writeInt(sendTime); // 发送时间
        buff.writeByte(isRead); // 是否已读 0 - 未读, 1 - 已读
        buff.writeByte(isGetAffixs); // 是否已取 0 - 未取, 1 - 已取
    }

    @Override
    public String getChangeSql() {
        return SqlUtil.getSql(this, DBUtil.DB_USER, "`roleemail`", "`receiverid`=" + receiverId + " and `emailid`=" + emailId);
    }

    @Override
    public String getDeleteSql() {
        return "delete from `roleemail` where `receiverid`=" + receiverId + " and `emailid`=" + emailId;
    }

    @Override
    public int hashCode() {
        return new Long(receiverId).hashCode();
    }

    @Override
    public String toString() {
        return "RoleEmailPo{" +
                "emailId=" + emailId +
                ", templateId=" + templateId +
                ", refEmailId=" + refEmailId +
                ", textMode=" + textMode +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", params='" + params + '\'' +
                ", senderType=" + senderType +
                ", senderId=" + senderId +
                ", senderName='" + senderName + '\'' +
                ", receiverId=" + receiverId +
                ", affixs='" + affixs + '\'' +
                ", sendTime=" + sendTime +
                ", isRead=" + isRead +
                ", isGetAffixs=" + isGetAffixs +
                ", affixMap=" + affixMap +
                ", paramsArray=" + Arrays.toString(paramsArray) +
                '}';
    }

    @Override
    public int compareTo(RoleEmailPo other) {
        return this.sendTime - other.sendTime;
    }
}
