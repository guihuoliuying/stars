package com.stars.services.chat;


import com.stars.modules.chat.prodata.ChatBanVo;

import java.util.List;

public class ChatManager {

    /**
     * 聊天频道定义
     */
    public static byte CHANNEL_WORLD = 0;//世界频道
    public static byte CHANNEL_PERSONAL = 1;//私人频道
    public static byte CHANNEL_FAMILY = 2;//家族频道
    public static byte CHANNEL_TEAM = 3;//队伍频道
    public static byte CHANNEL_SYSTEM = 4; // 系统频道
    public static byte CHANNEL_RM = 5;//跨服频道
    public static byte CHANNEL_TEAM_INVITATION = 6;//队伍频道
    public static byte CHANNEL_RM_CAMP = 7;//跨服阵营频道

    /**
     * 每个角色私聊信息最大的缓存数量
     */
    public static int PERSONAL_OFFLINE_MSG_SIZE = 20;

    private static List<ChatBanVo> CHAT_BAN_VO_LIST;

    public static void setChatBanVoList(List<ChatBanVo> chatBanVoList) {
        CHAT_BAN_VO_LIST = chatBanVoList;
    }

    public static List<ChatBanVo> getChatBanVoList() {
        return CHAT_BAN_VO_LIST;
    }
}
