package com.stars.services.family.welfare.redpacket;

import com.google.common.cache.*;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.modules.family.event.FamilyChangeRedPacketEvent;
import com.stars.modules.family.packet.ClientFamilyRedPacket;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.event.FamilyEvent;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketMemberPo;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketRecordPo;
import com.stars.services.family.welfare.redpacket.userdata.FamilyRedPacketSeizedRecordPo;
import com.stars.util.I18n;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.stars.modules.family.FamilyManager.*;

/**
 * Created by zhaowenshuo on 2016/9/6.
 */
public class FamilyRedPacketServiceActor extends ServiceActor implements FamilyRedPacketService {

    private DbRowDao dao = new DbRowDao();
    private String serviceName;
    private Map<Long, FamilyRedPacketData> onlineDataMap;
    private LoadingCache<Long, FamilyRedPacketData> offlineDataMap;
    private Map<Long, FamilyRedPacketData> pendingSavingDataMap;

    public FamilyRedPacketServiceActor(String id) {
        this.serviceName = "family red packet service-" + id;
    }

    public FamilyRedPacketServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1800, TimeUnit.SECONDS)
                // todo: removalListener
                .removalListener(new RemovalListener<Long, FamilyRedPacketData>() {
                    @Override
                    public void onRemoval(RemovalNotification<Long, FamilyRedPacketData> notification) {
                        if (notification.wasEvicted()) {
                            Set<DbRow> set = new HashSet<DbRow>();
                            set.addAll(notification.getValue().getMemberRedPacketPoMap().values());
                            set.addAll(notification.getValue().getRecordPoList());
                            if (!dao.isSavingSucceeded(set)) {
                                pendingSavingDataMap.put(notification.getKey(), notification.getValue());
                                LogUtil.error("family - red packet缓存移除异常，roleId=" + notification.getKey());
                            }
                        }
                    }
                })
                .build(new FamilyRedPacketDataCacheLoader());
        pendingSavingDataMap = new HashMap<>();
    }

    @Override
    public void printState() {

    }

    @Override
    public void online(long familyId, long roleId) {
        FamilyRedPacketData data = onlineDataMap.get(familyId);
        if (data == null && pendingSavingDataMap.containsKey(familyId)) {
            data = pendingSavingDataMap.remove(familyId);
            onlineDataMap.put(familyId, data);
        }
        if (data == null && offlineDataMap.getIfPresent(familyId) != null) {
            data = offlineDataMap.getIfPresent(familyId);
            offlineDataMap.invalidate(familyId);
            onlineDataMap.put(familyId, data);
        }
        if (data == null) {
            try {
                Map<Long, FamilyRedPacketMemberPo> memberRedPacketPoMap =
                        DBUtil.queryMap(DBUtil.DB_USER, "roleid", FamilyRedPacketMemberPo.class,
                                "select * from `familyredpacketmember` where `familyid`=" + familyId);
                Map<Long, FamilyRedPacketRecordPo> recordPoMap =
                        DBUtil.queryMap(DBUtil.DB_USER, "redpacketid", FamilyRedPacketRecordPo.class,
                                "select * from `familyredpacketrecord` where `familyid`=" + familyId);
                List<FamilyRedPacketSeizedRecordPo> seizedRecordPoList =
                        DBUtil.queryList(DBUtil.DB_USER, FamilyRedPacketSeizedRecordPo.class,
                                "select * from `familyredpacketseizedrecord` where `familyid`=" + familyId);
                //
                for (FamilyRedPacketSeizedRecordPo seizedRecordPo : seizedRecordPoList) {
                    FamilyRedPacketRecordPo recordPo = recordPoMap.get(seizedRecordPo.getRedPacketId());
                    if (recordPo != null) {
                        recordPo.getSeizedRecordPoList().add(seizedRecordPo);
                        recordPo.getSeizedRecordPoMap().put(seizedRecordPo.getSeizerId(), seizedRecordPo);
                    }
                }
                List<FamilyRedPacketRecordPo> recordPoList = new ArrayList<>(recordPoMap.values());
                Collections.sort(recordPoList);
                data = new FamilyRedPacketData();
                data.setMemberRedPacketPoMap(memberRedPacketPoMap);
                data.setRecordPoList(recordPoList);
                data.setMemberCount(memberRedPacketPoMap.size());
                onlineDataMap.put(familyId, data);
            } catch (Exception e) {
                LogUtil.error("", e);
            }
        }
        int count = getOwnedCount(familyId, roleId);
        if (count != 0) {
            ServiceHelper.roleService().notice(roleId, new FamilyChangeRedPacketEvent(count));
        }
        // 通知有红包可抢
        if (data != null && canSeize(data.getLastRecord(), roleId)) {
            ClientFamilyRedPacket packet = new ClientFamilyRedPacket(ClientFamilyRedPacket.SUBTYPE_NOTIFY);
            packet.setRecordPo(data.getLastRecord().copy());
            PlayerUtil.send(roleId, packet);
        }
    }

    @Override
    public void offline(long familyId) {
        FamilyRedPacketData data = onlineDataMap.remove(familyId);
        if (data != null) {
            offlineDataMap.put(familyId, data);
        }
    }

    @Override
    public void save() {
        dao.flush();
    }

    @Override
    public void sendRedPacketInfo(FamilyAuth auth) {
        if (auth.getPost() == FamilyPost.ERROR || auth.getPost() == FamilyPost.MASSES) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyRedPacketData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            ServiceUtil.sendText(auth.getRoleId(), "common_tips_loading");
            return;
        }
        FamilyRedPacketMemberPo memberRedPacketPo = data.getMemberRedPacketPoMap().get(auth.getRoleId());
        if (memberRedPacketPo == null) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return;
        }
        ClientFamilyRedPacket packet = new ClientFamilyRedPacket(ClientFamilyRedPacket.SUBTYPE_ALL_INFO);
        packet.setData(data);
        packet.setMemberRedPacketPo(memberRedPacketPo);
        PlayerUtil.send(auth.getRoleId(), packet);

    }

    @Override
    public boolean giveRedPacket(FamilyAuth auth) {
        if (auth.getPost() == FamilyPost.ERROR || auth.getPost() == FamilyPost.MASSES) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nopost");
            return false;
        }
        FamilyRedPacketData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            ServiceUtil.sendText(auth.getRoleId(), "common_tips_loading");
            return false;
        }
        FamilyRedPacketMemberPo memberRedPacketPo = data.getMemberRedPacketPoMap().get(auth.getRoleId());
        if (memberRedPacketPo == null) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return false;
        }
        FamilyRedPacketRecordPo recordPo = data.getLastRecord();
        if (recordPo != null
                && now() - recordPo.getTimestamp() < rpTimeout
                && recordPo.getSeizedCount() < recordPo.getCount()) {
            ServiceUtil.sendText(auth.getRoleId(), "family_desc_rednoover");
            return false;
        }
        if (memberRedPacketPo.getOwnedCount() <= 0) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nored");
            return false;
        }


        memberRedPacketPo.setOwnedCount(memberRedPacketPo.getOwnedCount() - 1);
        memberRedPacketPo.setGivenCount(memberRedPacketPo.getGivenCount() + 1);
        dao.update(memberRedPacketPo);
        recordPo = new FamilyRedPacketRecordPo();
        recordPo.setFamilyId(auth.getFamilyId());
        recordPo.setRedPacketId(ServiceHelper.idService().newFamilyRedPacketId());
        recordPo.setGiverId(auth.getRoleId());
        recordPo.setGiverName(auth.getRoleName());
        recordPo.setCount((int) Math.ceil(data.getMemberCount() * 1.0 / rpCountDivisor));
        recordPo.setTimestamp(now());
        dao.insert(recordPo);
        data.getRecordPoList().add(recordPo);
        // 通知客户端
        ClientFamilyRedPacket packet = new ClientFamilyRedPacket(ClientFamilyRedPacket.SUBTYPE_NOTIFY);
        packet.setRecordPo(recordPo.copy()); // 因为要交给其他线程进行发送（所以复制了一下）
        ServiceHelper.familyMainService().sendToOnlineMember(auth.getFamilyId(), packet);
        // 如果红包记录太多则清掉
        clearRecords(data);
        // 增加家族事迹
        ServiceHelper.familyEventService().logEvent(
                auth.getFamilyId(), FamilyEvent.W_REDPACKET, auth.getRoleName());
        ServiceHelper.roleService().notice(auth.getRoleId(), new FamilyChangeRedPacketEvent(memberRedPacketPo.getOwnedCount()));

        return true;
    }

    public int getOwnedCount(long familyId, long roleId) {
        FamilyAuth auth = ServiceHelper.familyRoleService().getFamilyAuth(roleId);
        FamilyRedPacketData data = getData(familyId);
        if (auth.getPost() != FamilyPost.ERROR && auth.getPost() != FamilyPost.MASSES) {
            if (data != null) {
                FamilyRedPacketMemberPo memberRedPacketPo = data.getMemberRedPacketPoMap().get(auth.getRoleId());
                FamilyRedPacketRecordPo recordPo = data.getLastRecord();
                if (memberRedPacketPo != null) {
                    if (recordPo == null || now() - recordPo.getTimestamp() >= rpTimeout || recordPo.getSeizedCount() >= recordPo.getCount()) {
                        if (memberRedPacketPo.getOwnedCount() > 0) {
                            return memberRedPacketPo.getOwnedCount();
                        }
                    }
                }
            }
        }
        return 0;
    }


    @Override
    public boolean seizeRedPacket(FamilyAuth auth, long redPacketId) {
        if (auth.getPost() == FamilyPost.ERROR || auth.getPost() == FamilyPost.MASSES) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nopost");
            return false;
        }
        FamilyRedPacketData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            ServiceUtil.sendText(auth.getRoleId(), "common_tips_loading");
            return false;
        }
        FamilyRedPacketMemberPo memberRedPacketPo = data.getMemberRedPacketPoMap().get(auth.getRoleId());
        if (memberRedPacketPo == null) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return false;
        }
        FamilyRedPacketRecordPo recordPo = data.getLastRecord();
        if (recordPo == null
                || recordPo.getRedPacketId() != redPacketId
                || recordPo.getSeizedCount() >= recordPo.getCount()
                || now() - recordPo.getTimestamp() > rpTimeout) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nogetred");
            return false;
        }
        if (recordPo.getGiverId() == auth.getRoleId()) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.rp.seizeSelf"));
            return false;
        }
        if (recordPo.getSeizedRecordPoMap().containsKey(auth.getRoleId())) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.rp.alreadySeize"));
            return false;
        }
        FamilyRedPacketSeizedRecordPo seizedRecordPo = new FamilyRedPacketSeizedRecordPo();
        seizedRecordPo.setFamilyId(auth.getFamilyId());
        seizedRecordPo.setRedPacketId(redPacketId);
        seizedRecordPo.setSeizerId(auth.getRoleId());
        seizedRecordPo.setSeizerName(auth.getRoleName());
        seizedRecordPo.setTimestamp(now());

        recordPo.getSeizedRecordPoList().add(seizedRecordPo);
        recordPo.getSeizedRecordPoMap().put(seizedRecordPo.getSeizerId(), seizedRecordPo);
        recordPo.setSeizedCount(recordPo.getSeizedRecordPoList().size());
        memberRedPacketPo.setSeizedCount(memberRedPacketPo.getSeizedCount() + 1);
        dao.insert(seizedRecordPo);
        dao.update(recordPo, memberRedPacketPo);
        return true;
    }

    @Override
    public void addRedPacket(FamilyAuth auth, int number) {
        if (auth.getPost() == FamilyPost.ERROR || auth.getPost() == FamilyPost.MASSES) {
            ServiceUtil.sendText(auth.getRoleId(), "family_tips_nopost");
            return;
        }
        FamilyRedPacketData data = getOnlineData(auth.getFamilyId());
        if (data == null) {
            ServiceUtil.sendText(auth.getRoleId(), "common_tips_loading");
            return;
        }
        FamilyRedPacketMemberPo memberPo = data.getMemberRedPacketPoMap().get(auth.getRoleId());
        if (memberPo == null) {
            ServiceUtil.sendText(auth.getRoleId(), I18n.get("family.management.noSuchMember"));
            return;
        }

        memberPo.setOwnedCount(memberPo.getOwnedCount() + number);
        dao.update(memberPo);
        ServiceHelper.roleService().notice(auth.getRoleId(), new FamilyChangeRedPacketEvent(memberPo.getOwnedCount()));
    }

    @Override
    public void addFamily(long familyId) {
        FamilyRedPacketData data = new FamilyRedPacketData();
        data.setMemberCount(0);
        data.setMemberRedPacketPoMap(new HashMap<Long, FamilyRedPacketMemberPo>());
        data.setRecordPoList(new ArrayList<FamilyRedPacketRecordPo>());
        onlineDataMap.put(familyId, data);
    }

    @Override
    public void addMember(long familyId, long memberId) {
        FamilyRedPacketData data = getData(familyId);
        if (data == null) {
            LogUtil.error("找不到数据");
            return;
        }
        FamilyRedPacketMemberPo memberRedPacketPo = new FamilyRedPacketMemberPo();
        memberRedPacketPo.setFamilyId(familyId);
        memberRedPacketPo.setRoleId(memberId);
        data.getMemberRedPacketPoMap().put(memberId, memberRedPacketPo);
        dao.insert(memberRedPacketPo);
        data.setMemberCount(data.getMemberRedPacketPoMap().size());
    }

    @Override
    public void delMember(long familyId, long memberId) {
        FamilyRedPacketData data = getData(familyId);
        if (data == null) {
            LogUtil.error("找不到数据");
            return;
        }
        FamilyRedPacketMemberPo memberRedPacketPo = data.getMemberRedPacketPoMap().remove(memberId);
        if (memberRedPacketPo != null) {
            dao.delete(memberRedPacketPo);
            data.setMemberCount(data.getMemberRedPacketPoMap().size());
        }
    }

    @Override
    public void updateMemberCount(long familyId, int memberCount) {
        FamilyRedPacketData data = getOnlineData(familyId);
        if (data != null) {
            data.setMemberCount(memberCount);
        }
    }

    @Override
    public void updateSeizedRedPacketInfo(FamilyAuth auth, long redPacketId, Map<Integer, Integer> toolMap) {
        if (auth.getPost() == FamilyPost.ERROR || auth.getPost() == FamilyPost.MASSES) {
            return;
        }
        FamilyRedPacketData data = getData(auth.getFamilyId());
        if (data == null) {
            return;
        }
        // 先找到对应的红包记录
        FamilyRedPacketRecordPo recordPo = null;
        for (FamilyRedPacketRecordPo r : data.getRecordPoList()) {
            if (r.getRedPacketId() == redPacketId) {
                recordPo = r;
                break;
            }
        }
        if (recordPo == null) {
            return;
        }
        // 找到对应的抢红包记录
        FamilyRedPacketSeizedRecordPo seizedRecordPo = recordPo.getSeizedRecordPoMap().get(auth.getRoleId());
        if (seizedRecordPo == null) {
            return;
        }
        seizedRecordPo.setSeizedToolMap(toolMap);
        dao.update(seizedRecordPo);
        // 通知客户端
        ClientFamilyRedPacket packet = new ClientFamilyRedPacket(ClientFamilyRedPacket.SUBTYPE_SEIZE);
        packet.setSeizedRecordPo(seizedRecordPo);
        PlayerUtil.send(auth.getRoleId(), packet);
    }

    private FamilyRedPacketData getData(long familyId) {
        if (onlineDataMap.containsKey(familyId)) {
            return onlineDataMap.get(familyId);
        }
        return offlineDataMap.getUnchecked(familyId);
    }

    public FamilyRedPacketData getOnlineData(long familyId) {
        return onlineDataMap.get(familyId);
    }

    private int now() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    private void clearRecords(FamilyRedPacketData data) {
        while (data.getRecordPoList().size() > rpRecordMaxSize) {
            FamilyRedPacketRecordPo recordPo = data.getRecordPoList().remove(0);
            dao.delete(recordPo);
            for (FamilyRedPacketSeizedRecordPo seizedRecordPo : recordPo.getSeizedRecordPoList()) {
                dao.delete(seizedRecordPo);
            }
        }
    }

    private boolean canSeize(FamilyRedPacketRecordPo recordPo, long roleId) {
        if (recordPo == null
                || recordPo.getSeizedCount() >= recordPo.getCount()
                || now() - recordPo.getTimestamp() > rpTimeout
                || recordPo.getGiverId() == roleId
                || recordPo.getSeizedRecordPoMap().containsKey(roleId)) {
            return false;
        }
        return true;
    }

    class FamilyRedPacketDataCacheLoader extends CacheLoader<Long, FamilyRedPacketData> {
        @Override
        public FamilyRedPacketData load(Long familyId) throws Exception {
            FamilyRedPacketData data = pendingSavingDataMap.get(familyId);
            if (data != null) {
                pendingSavingDataMap.remove(familyId);
                return data;
            }
            Map<Long, FamilyRedPacketMemberPo> memberRedPacketPoMap =
                    DBUtil.queryMap(DBUtil.DB_USER, "roleid", FamilyRedPacketMemberPo.class,
                            "select * from `familyredpacketmember` where `familyid`=" + familyId);
            Map<Long, FamilyRedPacketRecordPo> recordPoMap =
                    DBUtil.queryMap(DBUtil.DB_USER, "redpacketid", FamilyRedPacketRecordPo.class,
                            "select * from `familyredpacketrecord` where `familyid`=" + familyId);
            List<FamilyRedPacketSeizedRecordPo> seizedRecordPoList =
                    DBUtil.queryList(DBUtil.DB_USER, FamilyRedPacketSeizedRecordPo.class,
                            "select * from `familyredpacketseizedrecord` where `familyid`=" + familyId);
            //
            for (FamilyRedPacketSeizedRecordPo seizedRecordPo : seizedRecordPoList) {
                FamilyRedPacketRecordPo recordPo = recordPoMap.get(seizedRecordPo.getRedPacketId());
                if (recordPo != null) {
                    recordPo.getSeizedRecordPoList().add(seizedRecordPo);
                    recordPo.getSeizedRecordPoMap().put(seizedRecordPo.getSeizerId(), seizedRecordPo);
                }
            }
            List<FamilyRedPacketRecordPo> recordPoList = new ArrayList<>(recordPoMap.values());
            Collections.sort(recordPoList);
            data = new FamilyRedPacketData();
            data.setMemberRedPacketPoMap(memberRedPacketPoMap);
            data.setRecordPoList(recordPoList);
            data.setMemberCount(memberRedPacketPoMap.size());
            return data;
        }
    }
}
