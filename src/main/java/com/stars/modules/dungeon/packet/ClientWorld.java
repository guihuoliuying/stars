package com.stars.modules.dungeon.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.modules.dungeon.DungeonPacketSet;
import com.stars.modules.dungeon.prodata.WorldinfoVo;
import com.stars.modules.dungeon.userdata.RoleChapter;
import com.stars.network.server.buffer.NewByteBuffer;

import java.util.Map;

/**
 * Created by liuyuheng on 2016/6/21.
 */
public class ClientWorld extends PlayerPacket {
    private byte sendType;// 下发类型

    public static final byte SEND_ALL_CHAPTER = 1;// 下发所有章节数据
    public static final byte UPDATE_CHAPTER = 2;// 更新章节数据

    private Map<Integer, WorldinfoVo> chapterVoMap;// 章节产品数据
    private Map<Integer, RoleChapter> roleChapterMap;// 章节玩家数据

    public ClientWorld() {
    }

    public ClientWorld(byte sendType) {
        this.sendType = sendType;
    }

    @Override
    public void execPacket(Player player) {

    }

    @Override
    public short getType() {
        return DungeonPacketSet.C_WORLD;
    }

    @Override
    public void writeToBuffer(NewByteBuffer buff) {
        buff.writeByte(sendType);
        switch (sendType) {
            case SEND_ALL_CHAPTER:// 下发所有章节数据
                byte size = (byte) (chapterVoMap == null ? 0 : chapterVoMap.size());
                buff.writeByte(size);
                if (size != 0) {
                    for (WorldinfoVo chapterVo : chapterVoMap.values()) {
                        chapterVo.writeToBuff(buff);
                        buff.writeByte(roleChapterMap.get(chapterVo.getWorldId()) == null ? 0 :
                                roleChapterMap.get(chapterVo.getWorldId()).getIsReward());
                    }
                }
                break;
            case UPDATE_CHAPTER:
                size = (byte) (roleChapterMap == null ? 0 : roleChapterMap.size());
                buff.writeByte(size);
                if (size != 0) {
                    for (RoleChapter roleChapter : roleChapterMap.values()) {
                        roleChapter.writeToBuff(buff);
                    }
                }
                break;
            default:
                break;
        }
    }

    public void setChapterVoMap(Map<Integer, WorldinfoVo> chapterVoMap) {
        this.chapterVoMap = chapterVoMap;
    }

    public void setRoleChapterMap(Map<Integer, RoleChapter> roleChapterMap) {
        this.roleChapterMap = roleChapterMap;
    }
}
