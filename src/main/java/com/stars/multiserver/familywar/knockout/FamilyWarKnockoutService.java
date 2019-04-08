package com.stars.multiserver.familywar.knockout;

import com.stars.modules.scene.fightdata.FighterEntity;
import com.stars.services.Service;
import com.stars.services.family.FamilyAuth;
import com.stars.core.actor.invocation.ActorService;
import com.stars.core.actor.invocation.annotation.AsyncInvocation;

import java.util.Map;

/**
 * 淘汰赛（本服家族战和跨服决赛的流程基本一样）
 * 1. 参战名单管理（报名/审核）
 * 2. 赛程管理
 * 3. 胜负判断和发奖管理（淘汰赛层面）
 *
 * 家族淘汰赛中的一场对战
 *
 * 1. 精英赛场的管理
 *   1.1 fightServerId
 *   1.2 fightId
 *   1.3 士气
 *   1.4 双方家族参战成员
 *   1.5 个人积分管理
 * 2. 分组匹配战场
 *   2.1 匹配逻辑，等待队列处理
 *   2.2 各个分组战场的fightServerId和fightId
 *   2.3 每个分组战场的对战人员管理
 *   2.4 个人积分管理
 *
 * Created by zhaowenshuo on 2016/11/7.
 */
public interface FamilyWarKnockoutService extends Service, ActorService {

    /* 参战名单管理（报名/审核） */
    void signUp(FamilyAuth auth, boolean isCancelled);

    void sendSignUpList(FamilyAuth auth);

    void verifySignUp(FamilyAuth auth, long roleId, boolean isAgreed);

    /* 进入战斗（根据） */
    void enterBattle();

    /*  */
    void finishBattle();

    /* rpc */
    @AsyncInvocation
    void enterEliteFight(int controlServerId, int mainServerId, long battleId, long familyId, long roleId);

    @AsyncInvocation
    void enterNormalFightMatchQueue(int controlServerId, int mainServerId, long battleId, long familyId, long roleId, FighterEntity fighterEntity);

    @AsyncInvocation
    void handleDead(int controlServerId, int fightServerId, long battleId, long fightId, Map<String, String> deadMap);


}
