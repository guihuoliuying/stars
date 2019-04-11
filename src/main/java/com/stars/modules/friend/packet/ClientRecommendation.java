package com.stars.modules.friend.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.friend.FriendPacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.friend.memdata.RecommendationFriend;

import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/13.
 */
public class ClientRecommendation extends PlayerPacket {

    Map<Long, RecommendationFriend> recommendationMap;
    private byte isRecommend;

    public ClientRecommendation() {
    }

    public ClientRecommendation(Map<Long, RecommendationFriend> recommendationMap) {
        this.recommendationMap = recommendationMap;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return FriendPacketSet.C_FRIEND_RECOMMENDATION;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(isRecommend);
        if (recommendationMap == null) {
            buff.writeByte((byte) 0);
            return;
        }
        buff.writeByte((byte) recommendationMap.size());
        for (RecommendationFriend recommendation : recommendationMap.values()) {
            buff.writeString(Long.toString(recommendation.getRoleId())); // 玩家ID
            buff.writeInt(recommendation.getJobId()); // 职业ID
            buff.writeString(recommendation.getName()); // 玩家名字
            buff.writeInt(recommendation.getLevel()); // 玩家等级
            buff.writeInt(recommendation.getFightScore()); // 玩家战力
            buff.writeInt(recommendation.getOfflineTimestamp()); // 玩家离线时间
        }
    }

    public Map<Long, RecommendationFriend> getRecommendationMap() {
        return recommendationMap;
    }

    public void setRecommendationMap(Map<Long, RecommendationFriend> recommendationMap) {
        this.recommendationMap = recommendationMap;
    }

    public byte getIsRecommend() {
        return isRecommend;
    }

    public void setIsRecommend(byte isRecommend) {
        this.isRecommend = isRecommend;
    }
}
