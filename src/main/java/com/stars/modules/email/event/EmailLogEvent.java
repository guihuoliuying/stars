package com.stars.modules.email.event;

import com.stars.core.event.Event;
import com.stars.services.mail.userdata.RoleEmailPo;

public class EmailLogEvent extends Event {

    public static byte OPSEND = 1;//邮件发送
    public static byte OPRECEIVER = 2;//邮件到达
    public static byte OPGETTOOL = 3;//提取邮件
    public static byte OPDELETE = 4;//邮件删除
    public static byte OPDELETEBYSYS = 5;//系统删除

    private int emailId;//邮件id
    private byte opType;//操作类型
    private String title;//邮件标题
    private String content;//邮件内容
    private String freeTime;//冻结时间
    private String overTime;//过期时间
    private long sender;//发送者
    private String tool;
    private String toolInfo;

    public EmailLogEvent(byte optype, RoleEmailPo emailPo) {
        this.setEmailId(emailPo.getEmailId());
        this.setOpType(optype);
        this.setSender(emailPo.getSenderId());
        this.setTitle(emailPo.getTitle());
        this.setContent(emailPo.getText());
        this.setTool(emailPo.getAffixs());
        int refEmailId = emailPo.getRefEmailId();
        if (refEmailId != 0) {

        } else {
            this.setFreeTime("");
            this.setOverTime("");
        }
    }

    public int getEmailId() {
        return emailId;
    }

    public void setEmailId(int emailId) {
        this.emailId = emailId;
    }

    public byte getOpType() {
        return opType;
    }

    public void setOpType(byte opType) {
        this.opType = opType;
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

    public String getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(String freeTime) {
        this.freeTime = freeTime;
    }

    public String getOverTime() {
        return overTime;
    }

    public void setOverTime(String overTime) {
        this.overTime = overTime;
    }

    public long getSender() {
        return sender;
    }

    public void setSender(long sender) {
        this.sender = sender;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public String getToolInfo() {
        return toolInfo;
    }

    public void setToolInfo(String toolInfo) {
        this.toolInfo = toolInfo;
    }
}
