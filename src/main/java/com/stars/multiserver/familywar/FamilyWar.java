package com.stars.multiserver.familywar;

import com.stars.modules.familyactivities.war.packet.ClientFamilyWarMainIcon;
import com.stars.modules.familyactivities.war.packet.ui.ClientFamilyWarUiPointsRank;
import com.stars.modules.familyactivities.war.packet.ui.ServerFamilyWarUiPointsRank;
import com.stars.modules.familyactivities.war.packet.ui.auxiliary.PktAuxFamilyWarPointsObj;
import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyInfo;
import com.stars.multiserver.familywar.knockout.KnockoutFamilyMemberInfo;
import com.stars.services.ServiceHelper;
import com.stars.services.family.main.userdata.FamilyMemberPo;
import com.stars.services.fightbase.FightBaseService;
import com.stars.services.role.RoleService;
import com.stars.util.LogUtil;
import com.stars.util.ranklist.IndexList;
import com.stars.util.ranklist.RankObj;

import java.util.*;

/**
 * Created by chenkeyu on 2017-05-22.
 */
public abstract class FamilyWar {
    protected Map<Long, KnockoutFamilyInfo> familyMap; // 家族信息表，(familyId, KnockoutFamilyInfo)
    protected Set<Long> failFamilySet;//未能晋级的家族信息表
    protected Map<Long, KnockoutFamilyMemberInfo> memberMap; // 家族成员信息表，(memberId, KnockoutFamilyMemberInfo)

    protected IndexList elitePointsRankList; // 精英战个人积分榜（前几名）
    protected IndexList normalPointsRankList; // 匹配战个人积分榜（前几名）
    protected Map<String, Long> elitePointsMap; // 个人积分（所有的）
    protected Map<String, Long> normalPointsMap; // 匹配战积分（所有的）
    protected Map<Long, Set<Long>> eliteMinPointsAwardAcquiredRecordSet; // 已领取的积分奖励
    protected Map<Long, Set<Long>> normalMinPointsAwardAcquiredRecordSet; // 已领取的积分奖励

    protected Map<Long, Boolean> hasNoticeMasterMap;//masterId,true or false

    public FamilyWar() {
        this.familyMap = new HashMap<>();
        this.failFamilySet = new HashSet<>();
        this.memberMap = new HashMap<>();
        this.elitePointsRankList = new IndexList(5000, 100, -5000);
        this.normalPointsRankList = new IndexList(5000, 100, -5000);
        this.elitePointsMap = new HashMap<>();
        this.normalPointsMap = new HashMap<>();
        this.eliteMinPointsAwardAcquiredRecordSet = new HashMap<>();
        this.normalMinPointsAwardAcquiredRecordSet = new HashMap<>();
        this.hasNoticeMasterMap = new HashMap<>();
    }

    public abstract void finishBattle(String battleId, long winnerFamilyId, long loserFamilyId);

    public abstract FightBaseService fightService();

    public abstract RoleService roleService();

    public abstract void removeBattle();

    public abstract void removeBattle(String battleId, int battleType);

    public abstract Map<Long, KnockoutFamilyInfo> getFamilyMap();

    public abstract void updateElitePoints(String fighterUid, long delta);

    public abstract void updateNormalPoints(String fighterUid, long delta);

    public abstract void enterSafeScene(int controlServerId, int mainServerId, long roleId);

    public abstract void enterSafeScene(int controlServerId, int mainServerId, long familyId, long roleId);

    public abstract void enter(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity);

    public abstract void enterEliteFight(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity);

    public abstract void enterNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId, FighterEntity fighterEntity);

    public abstract void cancelNormalFightWaitingQueue(int controlServerId, int mainServerId, long familyId, long roleId);

    public abstract void handleFighterQuit(String battleId, long roleId, String fightId);

    public abstract IndexList getElitePointsRankList(long roleId);

    public abstract IndexList getNormalPointsRankList(long roleId);

    public void addFamilyInfo(KnockoutFamilyInfo familyInfo) {
        familyMap.put(familyInfo.getFamilyId(), familyInfo);
        memberMap.putAll(familyInfo.getMemberMap());
        hasNoticeMasterMap.put(familyInfo.getMasterId(), false);
    }

    public void sendMainIcon(int mainServer, long roleId, long familyId, int state, long countdown, int type) {
        ClientFamilyWarMainIcon mainIcon = new ClientFamilyWarMainIcon(state, countdown);
        mainIcon.setQualification(isKnockoutFamily(familyId) ? FamilyWarConst.WITH_QUALIFICATION : FamilyWarConst.WITHOUT_QUALIFICATION);
        if (type == FamilyWarConst.W_TYPE_LOCAL) {
            ServiceHelper.roleService().send(mainServer, roleId, mainIcon);
        } else if (type == FamilyWarConst.W_TYPE_QUALIFYING || type == FamilyWarConst.W_TYPE_REMOTE) {
            FamilyWarRpcHelper.roleService().send(mainServer, roleId, mainIcon);
        }
    }

    /**
     * 是否参赛家族
     *
     * @param familyId
     * @return
     */
    public boolean isKnockoutFamily(long familyId) {
        return familyMap.containsKey(familyId) && !failFamilySet.contains(familyId);
    }

    public void sendMainIconToMaster(int type) {
        LogUtil.info("familywar|给族长发设置名单的icon,失败的family:{},通知与否的集合:{}", failFamilySet, hasNoticeMasterMap);
        for (KnockoutFamilyInfo familyInfo : familyMap.values()) {
            LogUtil.info("familywar|0给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
            if (failFamilySet.contains(familyInfo.getFamilyId()))
                continue;
            LogUtil.info("familywar|1给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
            if (hasNoticeMasterMap.get(familyInfo.getMasterId()))
                continue;
            LogUtil.info("familywar|2给族长发设置名单的icon|familyid:{},族长:{}", familyInfo.getFamilyId(), familyInfo.getMasterId());
            sendMainIcon(familyInfo.getMainServerId(), familyInfo.getMasterId(), familyInfo.getFamilyId(), FamilyWarConst.STATE_NOTICE_MASTER, 0L, type);
        }
    }

    public void sendMainIconToMaster(int type, long familyId) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null)
            return;
        if (failFamilySet.contains(familyId))
            return;
        if (hasNoticeMasterMap.get(familyInfo.getMasterId()))
            return;
        sendMainIcon(familyInfo.getMainServerId(), familyInfo.getMasterId(), familyInfo.getFamilyId(), FamilyWarConst.STATE_NOTICE_MASTER, 0L, type);
    }

    public void updatePoints(Map<String, Long> pointsMap, IndexList rankList, String fighterUid, long delta) {
        Long points = pointsMap.get(fighterUid);
        if (points == null) {
            points = 0L;
        }
        points = points + delta;
        pointsMap.put(fighterUid, points);
        if (rankList.containsRank(fighterUid)) {
            rankList.updateRank(fighterUid, points);
        } else {
            KnockoutFamilyMemberInfo memberInfo = memberMap.get(Long.parseLong(fighterUid));
            if (memberInfo == null) return;
            KnockoutFamilyInfo familyInfo = familyMap.get(memberInfo.getFamilyId());
            if (familyInfo == null) return;
            rankList.addRank(fighterUid, new FamilyWarPointsRankObj(
                    fighterUid, points, memberInfo.getMainServerId(), memberInfo.getName(), familyInfo.getFamilyName()));
        }
    }

    public long getElitePoints(String fighterUid) {
        Long points = elitePointsMap.get(fighterUid);
        if (points == null) {
            return 0;
        } else {
            return points;
        }
    }

    /**
     * 发送积分排行榜信息
     *
     * @param mainServerId
     * @param roleId
     * @param subtype
     */
    public void sendPointsRank(int mainServerId, long roleId, byte subtype, byte warType) {
        IndexList rankList = null;
        switch (subtype) {
            case ServerFamilyWarUiPointsRank.SUBTYPE_ELITE_FIGHT:
                rankList = this.elitePointsRankList;
                break;
            case ServerFamilyWarUiPointsRank.SUBTYPE_NORMAL_FIGHT:
                rankList = this.normalPointsRankList;
                break;
            default:
                return;
        }
        ClientFamilyWarUiPointsRank packet = new ClientFamilyWarUiPointsRank(
                subtype, warType, getPktAuxFamilyWarPointsObjList(rankList, 100));
        RankObj myRankObj = rankList.getRankObjByKey(Long.toString(roleId));
        packet.setMyRank(rankList.getRank(Long.toString(roleId)));
        packet.setMyRankObj(createPointsObj(myRankObj));
        roleService().send(mainServerId, roleId, packet);
    }

    /**
     * 获取排行前n名的信息
     *
     * @param rankList
     * @param n
     * @return
     */
    protected List<PktAuxFamilyWarPointsObj> getPktAuxFamilyWarPointsObjList(IndexList rankList, int n) {
        List<PktAuxFamilyWarPointsObj> list = new ArrayList<>();
        List<RankObj> top100List = rankList.getTop(100);
        for (RankObj rankObj : top100List) {
            PktAuxFamilyWarPointsObj pointsObj = createPointsObj(rankObj);
            if (pointsObj != null) {
                list.add(pointsObj);
            }
        }
        return list;
    }

    /**
     * 构建排行榜信息类，可能返回null
     *
     * @param rankObj
     * @return
     */
    protected PktAuxFamilyWarPointsObj createPointsObj(RankObj rankObj) {
        if (rankObj == null) return null;
        FamilyWarPointsRankObj rankObj0 = (FamilyWarPointsRankObj) rankObj;
        return new PktAuxFamilyWarPointsObj(
                Long.parseLong(rankObj.getKey()), rankObj0.getRoleName(), rankObj0.getFamilyName(), rankObj0.getServerId(), rankObj.getPoints()); //
    }

    public Set<Long> getFailFamilySet() {
        return failFamilySet;
    }

    protected int getMainServerId(long roleId) {
        return memberMap.get(roleId).getMainServerId();
    }

    public void addMember(long familyId, FamilyMemberPo memberPo, FighterEntity entity) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) return;
        KnockoutFamilyMemberInfo memberInfo = new KnockoutFamilyMemberInfo();
        memberInfo.setMainServerId(familyInfo.getMainServerId());
        memberInfo.setFamilyId(memberPo.getFamilyId());
        memberInfo.setMemberId(memberPo.getRoleId());
        memberInfo.setName(memberPo.getRoleName());
        memberInfo.setPostId(memberPo.getPostId());
        memberInfo.setLevel(memberPo.getRoleLevel());
        memberInfo.setFightScore(memberPo.getRoleFightScore());
        memberInfo.setFighterEntity(entity);
        familyInfo.getMemberMap().put(memberPo.getRoleId(), memberInfo);
        memberMap.put(memberPo.getRoleId(), memberInfo);
    }

    public void delMember(long familyId, long roleId) {
        KnockoutFamilyInfo familyInfo = familyMap.get(familyId);
        if (familyInfo == null) return;
        if (familyInfo.getTeamSheet().contains(roleId)) {
            familyInfo.getTeamSheet().remove(roleId);
        }
        familyInfo.getMemberMap().remove(roleId);
        memberMap.remove(roleId);
    }


}
