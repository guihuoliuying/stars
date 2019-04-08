package com.stars.modules.friendInvite.prodata;

/**
 * Created by chenxie on 2017/6/9.
 */
public class InviteVo {

    private int id;

    private String platform;

    private String channel;

    private String link;

    private String code;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "InviteVo{" +
                "id=" + id +
                ", platform='" + platform + '\'' +
                ", channel='" + channel + '\'' +
                ", link='" + link + '\'' +
                ", code='" + code + '\'' +
                '}';
    }
}
