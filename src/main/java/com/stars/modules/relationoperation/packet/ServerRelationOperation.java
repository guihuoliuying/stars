package com.stars.modules.relationoperation.packet;

import com.stars.core.player.Player;
import com.stars.core.player.PlayerPacket;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.MConst;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.family.summary.FamilySummaryComponent;
import com.stars.modules.foreshow.ForeShowConst;
import com.stars.modules.foreshow.summary.ForeShowSummaryComponent;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.relationoperation.RelationOperationPacketSet;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.summary.Summary;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class ServerRelationOperation extends PlayerPacket {

    private long otherId;

    @Override
    public void execPacket(Player player) {
        FamilyModule selfFamilyModule = (FamilyModule) module(MConst.Family);
        Summary summary = ServiceHelper.summaryService().getSummary(otherId);
        if (summary == null) {
            selfFamilyModule.warn("常用数据缺失");
            return;
        }
        ForeShowSummaryComponent fsSummary = (ForeShowSummaryComponent) summary.getComponent(MConst.ForeShow);
        ClientRelationOperation packet = new ClientRelationOperation();
        /* 好友信息 */
        RoleSummaryComponent otherRoleComp = (RoleSummaryComponent) summary.getComponent(MConst.Role);
        FriendModule selfFriendModule = (FriendModule) module(MConst.Friend);
        packet.setRoleId(otherId);
        if (otherRoleComp != null) {
            packet.setRoleJobId(otherRoleComp.getRoleJob());
            packet.setRoleName(otherRoleComp.getRoleName());
            packet.setRoleLevel(otherRoleComp.getRoleLevel());
            packet.setFriend(selfFriendModule.isFriend(otherId));
            packet.setBlacker(selfFriendModule.isBlacker(otherId));
            packet.setFriendOpen(fsSummary==null?false:fsSummary.isOpen(ForeShowConst.FRIEND));
        }

        /* 家族常用数据 */
        FamilyAuth selfFamilyAuth = selfFamilyModule.getAuth();
        if (selfFamilyAuth == null) {
            return;
        }
        FamilySummaryComponent otherFamilyComp = (FamilySummaryComponent) summary.getComponent("family");
        if (otherFamilyComp != null) {
            packet.setFamilyId(otherFamilyComp.getFamilyId());
            packet.setFamilyName(otherFamilyComp.getFamilyName());
            packet.setPostId(otherFamilyComp.getPostId());
        }
        PlayerUtil.send(getRoleId(), packet);
    }

    @Override
    public short getType() {
        return RelationOperationPacketSet.S_RELATION_OPERATION;
    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        otherId = Long.parseLong(buff.readString());
    }

}
