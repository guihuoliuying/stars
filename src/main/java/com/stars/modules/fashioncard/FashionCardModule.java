package com.stars.modules.fashioncard;

import com.stars.core.attr.Attribute;
import com.stars.core.attr.FormularUtils;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.fashioncard.effect.FashionCardEffect;
import com.stars.modules.fashioncard.event.FashionCardEvent;
import com.stars.modules.fashioncard.handler.FashionCardHandler;
import com.stars.modules.fashioncard.handler.imp.AddItemEffectHandler;
import com.stars.modules.fashioncard.handler.imp.AddPasskillEffectHandler;
import com.stars.modules.fashioncard.handler.imp.DelSkillCdEffectHandler;
import com.stars.modules.fashioncard.handler.imp.OlAnnounceEffectHandler;
import com.stars.modules.fashioncard.packet.ClientFashionCard;
import com.stars.modules.fashioncard.prodata.FashionCard;
import com.stars.modules.fashioncard.userdata.RoleFashionCard;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.packet.ClientRole;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by chenkeyu on 2017-10-13.
 */
public class FashionCardModule extends AbstractModule {

    private RoleFashionCard roleFashionCard;
    private Map<Integer, FashionCardHandler> handlerMap;

    private Map<Integer, Integer> skill_cd_Map = new HashMap<>();


    public FashionCardModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleFashionCard = DBUtil.queryBean(DBUtil.DB_USER, RoleFashionCard.class, "select * from rolefashioncard where roleid=" + id());
        if (roleFashionCard == null) {
            roleFashionCard = new RoleFashionCard();
            roleFashionCard.setRoleId(id());
            roleFashionCard.setFashionState("");
            roleFashionCard.setPutOned("");
            context().insert(roleFashionCard);
        }
        initHandler();
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
        if (roleFashionCard == null) {
            roleFashionCard = new RoleFashionCard();
            roleFashionCard.setRoleId(id());
            roleFashionCard.setFashionState("");
            roleFashionCard.setPutOned("");
            context().insert(roleFashionCard);
        }
        initHandler();
    }

    private void initHandler() {
        handlerMap = new HashMap<>();
        handlerMap.put(FashionCardManager.ADD_PASSKILL, new AddPasskillEffectHandler());
        handlerMap.put(FashionCardManager.ADD_ITEM, new AddItemEffectHandler());
        handlerMap.put(FashionCardManager.DEL_SKILL_CD, new DelSkillCdEffectHandler());
        handlerMap.put(FashionCardManager.OL_ANNOUNCE, new OlAnnounceEffectHandler());
    }

    private Map<FashionCardHandler, FashionCardEffect> getHandlerByCardType(Map<Integer, FashionCardEffect> effectMap) {
        Map<FashionCardHandler, FashionCardEffect> tmpMap = new HashMap<>();
        for (Map.Entry<Integer, FashionCardEffect> entry : effectMap.entrySet()) {
            tmpMap.put(handlerMap.get(entry.getKey()), entry.getValue());
        }
        return tmpMap;
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        Attribute roleAttribute = new Attribute();
        for (int fashionCardId : roleFashionCard.getFashionStateMap().keySet()) {
            FashionCard fashionCard = FashionCardManager.getFashionCardById(fashionCardId);
            roleAttribute.addAttribute(fashionCard.getAttribute());
        }
        roleFashionCard.setRoleAttribute(roleAttribute);
        roleFashionCard.setPower(FormularUtils.calFightScore(roleFashionCard.getRoleAttribute()));
        updateAttrAndFighScore(false);
        signCalRedPoint(MConst.FashionCard, RedPointConst.FASHION_CARD);
    }

    private void updateAttrAndFighScore(boolean send) {
        RoleModule role = module(MConst.Role);
        role.updatePartAttr(MConst.FashionCard, roleFashionCard.getRoleAttribute());
        role.updatePartFightScore(MConst.FashionCard, roleFashionCard.getPower());
        if (send) {
            role.sendRoleAttr();
            role.sendUpdateFightScore();
        }
    }

    @Override
    public void onSyncData() throws Throwable {
        for (int fashionCardId : roleFashionCard.getFashionStateMap().keySet()) {
            FashionCard fashionCard = FashionCardManager.getFashionCardById(fashionCardId);
            Map<FashionCardHandler, FashionCardEffect> tmpHandlerMap = getHandlerByCardType(fashionCard.getEffectMap());
            for (Map.Entry<FashionCardHandler, FashionCardEffect> entry : tmpHandlerMap.entrySet()) {
                entry.getKey().doAfterTransfer(moduleMap(), entry.getValue());
            }
        }
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
        if (redPointIds.contains(RedPointConst.FASHION_CARD)) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, Integer> entry : roleFashionCard.getFashionStateMap().entrySet()) {
                if (entry.getValue() == FashionCardManager.TAKE_OFF && !roleFashionCard.getPutOnedSet().contains(entry.getKey())) {
                    sb.append(entry.getKey()).append("+");
                }

            }
            redPointMap.put(RedPointConst.FASHION_CARD, sb.toString().isEmpty() ? null : sb.toString());
        }
    }

    public void view() {
        ClientFashionCard packet = new ClientFashionCard(ClientFashionCard.RES_VIEW);
        packet.setRoleData(new HashMap<>(roleFashionCard.getFashionStateMap()));
        send(packet);
    }

    public void putOnFashionCard(int fashionCardId) {
        if (roleFashionCard.getFashionStateMap().containsKey(fashionCardId)
                && roleFashionCard.getFashionStateMap().get(fashionCardId) == FashionCardManager.TAKE_OFF) {
            takeOffFashionCard(roleFashionCard.getFashionStateMap().keySet());
            roleFashionCard.changeFashionState(fashionCardId, FashionCardManager.PUT_ON);
            roleFashionCard.addPutOn(fashionCardId);
            eventDispatcher().fire(new FashionCardEvent(fashionCardId));
            context().update(roleFashionCard);
            ClientFashionCard clientFashionCard = new ClientFashionCard(ClientFashionCard.RES_SYNC_STATE);
            clientFashionCard.setFashionCardId(fashionCardId);
            clientFashionCard.setIsPutOn(1);
            send(clientFashionCard);
            RoleModule role = module(MConst.Role);
            role.getRoleRow().setCurFashionCardId(fashionCardId);
            context().update(role.getRoleRow());
            ServiceHelper.arroundPlayerService().updateCurFashionCardId(role.getJoinSceneStr(), id(), fashionCardId);
            ClientRole clientRole = new ClientRole(ClientRole.UPDATE_BASE, role.getRoleRow());
            send(clientRole);
            signCalRedPoint(MConst.FashionCard, RedPointConst.FASHION_CARD);
        } else {
            warn("时装化身尚未激活或者已穿戴");
            com.stars.util.LogUtil.info("时装化身尚未激活或者已穿戴|fashionCardId:{}", fashionCardId);
        }
    }

    public void takeOffFashionCard() {
        takeOffFashionCard(roleFashionCard.getFashionStateMap().keySet());
    }

    private void takeOffFashionCard(Set<Integer> tmpSet) {
        Set<Integer> tmp = new HashSet<>(tmpSet);
        for (int fashionCardId : tmp) {
            takeOffFashionCard(fashionCardId, false);
        }
        eventDispatcher().fire(new FashionCardEvent(0));
        context().update(roleFashionCard);
        RoleModule role = module(MConst.Role);
        role.getRoleRow().setCurFashionCardId(0);
        context().update(role.getRoleRow());
        ServiceHelper.arroundPlayerService().updateCurFashionCardId(role.getJoinSceneStr(), id(), 0);
        ClientRole clientRole = new ClientRole(ClientRole.UPDATE_BASE, role.getRoleRow());
        send(clientRole);
    }

    public void takeOffFashionCard(int fashionCardId, boolean needSave) {
        if (roleFashionCard.getFashionStateMap().containsKey(fashionCardId)) {
            if (roleFashionCard.getFashionStateMap().get(fashionCardId) == FashionCardManager.PUT_ON) {
                roleFashionCard.changeFashionState(fashionCardId, FashionCardManager.TAKE_OFF);
                if (needSave) {
                    eventDispatcher().fire(new FashionCardEvent(0));
                    context().update(roleFashionCard);
                    RoleModule role = module(MConst.Role);
                    role.getRoleRow().setCurFashionCardId(0);
                    context().update(role.getRoleRow());
                    ServiceHelper.arroundPlayerService().updateCurFashionCardId(role.getJoinSceneStr(), id(), 0);
                    ClientRole clientRole = new ClientRole(ClientRole.UPDATE_BASE, role.getRoleRow());
                    send(clientRole);
                }
                ClientFashionCard clientFashionCard = new ClientFashionCard(ClientFashionCard.RES_SYNC_STATE);
                clientFashionCard.setFashionCardId(fashionCardId);
                clientFashionCard.setIsPutOn(0);
                send(clientFashionCard);
            }
        } else {
            warn("时装化身尚未激活");
            com.stars.util.LogUtil.info("时装化身尚未激活|fashionCardId:{}", fashionCardId);
        }
    }

    public void activeFashionCard(int fashionCardId, int addUseHours, int itemId) {
        if (!FashionCardManager.fashionCardMap.containsKey(fashionCardId)) {
            com.stars.util.LogUtil.info("不存在的时装化身|fashionCardId:{},itemId:{}", fashionCardId, itemId);
            return;
        }
        if (roleFashionCard.getFashionStateMap().containsKey(fashionCardId)) {
            ToolModule tool = module(MConst.Tool);
            ItemVo itemVo = ToolManager.getItemVo(itemId);
            tool.addAndSend(itemVo.getResolveMap(), EventType.ADDTOOL.getCode());
            ClientAward clientAward = new ClientAward();
            clientAward.setType((byte) 1);
            clientAward.setAwrd(itemVo.getResolveMap());
            send(clientAward);
            LogUtil.info("已拥有该时装化身|fashionCardId:{},itemId:{}", fashionCardId, itemId);
            return;
        }
        roleFashionCard.changeFashionState(fashionCardId, FashionCardManager.TAKE_OFF);
        FashionCard fashionCard = FashionCardManager.getFashionCardById(fashionCardId);
        Map<FashionCardHandler, FashionCardEffect> tmpHandlerMap = getHandlerByCardType(fashionCard.getEffectMap());
        for (Map.Entry<FashionCardHandler, FashionCardEffect> entry : tmpHandlerMap.entrySet()) {
            entry.getKey().doAfterGetFashionCard(moduleMap(), entry.getValue());
        }
//        eventDispatcher().fire(new FashionCardEvent(fashionCardId));
        roleFashionCard.getRoleAttribute().addAttribute(fashionCard.getAttribute());
        roleFashionCard.setPower(FormularUtils.calFightScore(roleFashionCard.getRoleAttribute()));
        updateAttrAndFighScore(true);
        context().update(roleFashionCard);
        ClientFashionCard clientFashionCard = new ClientFashionCard(ClientFashionCard.RES_ACTIVE);
        clientFashionCard.setFashionCardId(fashionCardId);
        send(clientFashionCard);
        signCalRedPoint(MConst.FashionCard, RedPointConst.FASHION_CARD);
    }

    public void delSkillCd(Map<Integer, Integer> skillMap) {
        skill_cd_Map.putAll(skillMap);
    }

    public Map<Integer, Integer> getSkill_cd_Map() {
        return skill_cd_Map;
    }

    public long getLastAnnounceTimestamp() {
        return roleFashionCard.getLastAnnounceTimestamp();
    }

    public void setLasetAnnounceTimestamp(long timestamp) {
        roleFashionCard.setLastAnnounceTimestamp(timestamp);
        context().update(roleFashionCard);
    }

    public Set<Integer> getRoleFashionCard() {
        return roleFashionCard.getFashionStateMap().keySet();
    }

    public StringBuilder getFashionState() {
        StringBuilder sb = new StringBuilder();
        sb.append("change_fashion@type:");
        for (int id : FashionCardManager.fashionCardMap.keySet()) {
            if (roleFashionCard.getFashionStateMap().containsKey(id)) {
                sb.append(id).append("@1");
            } else {
                sb.append(id).append("@0");
            }
            sb.append("&");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb;
    }
}
