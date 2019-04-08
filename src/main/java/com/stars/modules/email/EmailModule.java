package com.stars.modules.email;

import com.stars.core.event.EventDispatcher;
import com.stars.core.exception.AffixsCoolTimeException;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.LoginModule;
import com.stars.modules.email.event.EmailRedPointEvent;
import com.stars.modules.email.packet.ClientEmail;
import com.stars.modules.email.packet.ServerEmail;
import com.stars.modules.email.pojodata.EmailConditionArgs;
import com.stars.modules.newequipment.NewEquipmentManager;
import com.stars.modules.newequipment.prodata.EquipmentVo;
import com.stars.modules.redpoint.RedPointConst;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.ServiceHelper;
import com.stars.util.LogUtil;
import org.springframework.cglib.beans.BeanCopier;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class EmailModule extends AbstractModule {
    private Set<Integer> emailList = new HashSet<>();
    private EmailRedPointEvent lastEvent = null;

    public EmailModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super("邮件", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onCreation(String name, String account) throws Throwable {
    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {
        RoleModule roleModule = module(MConst.Role);
        LoginModule loginModule = module(MConst.Login);
        Role roleRow = roleModule.getRoleRow();
        int channnel = loginModule.getChannnel();
        EmailConditionArgs emailConditionArgs = new EmailConditionArgs();
        emailConditionArgs.setChannel(channnel);
        BeanCopier.create(Role.class, EmailConditionArgs.class, false).copy(roleRow, emailConditionArgs, null);
        ServiceHelper.emailService().online(id(), emailConditionArgs);
//        if (isCreation) {
//            ServiceHelper.emailService().sendToSingle(id(), 10012, id(), "系统", null);
//        }
    }


    @Override
    public void onSyncData() throws Throwable {
        ServiceHelper.emailService().sendMailListToRole(id());
    }

    @Override
    public void onOffline() throws Throwable {
        ServiceHelper.emailService().offline(id());
    }

    public void handleRequest(ServerEmail request) {
        try {
            byte subtype = request.getSubtype();
            switch (subtype) {
                case ServerEmail.S_GET_LIST:
                    ServiceHelper.emailService().sendMailListToRole(id());
                    break;
                case ServerEmail.S_READ:
                    ServiceHelper.emailService().read(id(), request.getEmailId());
                    break;
                case ServerEmail.S_DELETE:
                    ServiceHelper.emailService().delete(id(), request.getEmailId());
                    break;
                case ServerEmail.S_FETCH_AFFIXS:
                    fetchAffixs(request.getEmailId());
                    break;
                case ServerEmail.S_ALL_DELETE:
                    ServiceHelper.emailService().deleteAll(id());
                    break;
                case ServerEmail.S_ALL_FETCH:
                    fetchAllAffixs();
                    break;
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof AffixsCoolTimeException) {
                AffixsCoolTimeException affixsCoolTimeException = (AffixsCoolTimeException) cause;
                if (affixsCoolTimeException.getAllToolMap() != null) {
                    try {
                        sendAllAffixs(affixsCoolTimeException.getAllToolMap());
                    } catch (Exception exception) {
                        com.stars.util.LogUtil.error("邮件服务异常", e);
                    }
                    warn("存在处于冷却状态的附件");
                } else {

                    warn("当前附件处于冷却状态，" + affixsCoolTimeException.getCoolTime() + "秒后可领取");
                }
            } else {
                LogUtil.error("邮件服务异常", e);
            }
        }
    }

    public void fetchAffixs(int emailId) throws Exception {
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        Map<Integer, Integer> toolMap = ServiceHelper.emailService().getAffixs(id(), emailId);
        /**
         * 职业转换
         */
        toolMap = checkChangeJob(toolMap);
        if (toolModule.canAdd(toolMap)) {
            ServiceHelper.emailService().fetchAffixs(id(), emailId);
            toolModule.addAndSend(toolMap, EventType.MAIl.getCode());
            ClientEmail packet = new ClientEmail(ClientEmail.C_FETCH_AFFIXS);
            packet.setEmailId(emailId);
            send(packet);
        } else {
            warn("email_euipbagfull_one");
        }
    }

    /**
     * 检查是否存在前职业装备邮件
     *
     * @param toolMap
     * @return
     */
    private Map<Integer, Integer> checkChangeJob(Map<Integer, Integer> toolMap) {
        RoleModule roleModule = module(MConst.Role);
        Map<Integer, Integer> newToolMap = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : toolMap.entrySet()) {
            Integer itemid = entry.getKey();
            ItemVo itemVo = ToolManager.getItemVo(itemid);
            if (itemVo.getType() == 7) {
                EquipmentVo equipmentVo = NewEquipmentManager.getEquipmentVo(itemid);
                if (equipmentVo.getJob() != roleModule.getRoleRow().getJobId()) {
                    EquipmentVo newJobEquipmentVo = NewEquipmentManager.getNewJobEquipmentVo(roleModule.getRoleRow().getJobId(), itemid);
                    if (newJobEquipmentVo != null) {
                        newToolMap.put(newJobEquipmentVo.getEquipId(), entry.getValue());
                    }else {
                        newToolMap.put(entry.getKey(), entry.getValue());
                    }
                    warn(DataManager.getGametext("changeschool_tips_equipchange"));
                    continue;
                }
            }
            newToolMap.put(entry.getKey(), entry.getValue());
        }
        return newToolMap;
    }

    public void fetchAllAffixs() throws Exception {
        Map<Integer, Map<Integer, Integer>> allToolMap = ServiceHelper.emailService().getAllAffixs(id());

        sendAllAffixs(allToolMap);

    }

    private void sendAllAffixs(Map<Integer, Map<Integer, Integer>> allToolMap) throws Exception {
        ToolModule toolModule = (ToolModule) module(MConst.Tool);
        if (allToolMap == null || allToolMap.size() == 0) {
            warn("email_get_none");
            return;
        }

        List<Integer> emailIdList = new ArrayList<>(allToolMap.keySet());
        Collections.sort(emailIdList);
        List<Integer> returnEmailIdList = new ArrayList<>();
        int fetchFailureCount = 0;
        for (Integer emailId : emailIdList) {
            Map<Integer, Integer> toolMap = allToolMap.get(emailId);
            /**
             * 检查职业转换
             */
            toolMap = checkChangeJob(toolMap);
            if (toolModule.canAdd(toolMap)) {
                ServiceHelper.emailService().fetchAffixs(id(), emailId);
                toolModule.addAndSend(toolMap, EventType.MAIl.getCode());
                returnEmailIdList.add(emailId);
            } else {
                fetchFailureCount++;
            }
        }
        ClientEmail packet = new ClientEmail(ClientEmail.C_ALL_FETCH);
        packet.setEmailIdList(returnEmailIdList);
        packet.setFetchFailureCount(fetchFailureCount);
        send(packet);
    }

    public void addEmailList(int emailId) {
        this.emailList.add(emailId);
    }

    public void addEmailList(Set<Integer> emailList) {
        this.emailList.addAll(emailList);
    }

    public void removeEmailList(int emailId) {
        this.emailList.remove(emailId);
    }

    public void removeEmailList() {
        this.emailList.clear();
    }

    public void setLastEmailRedPointEvent(EmailRedPointEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

    @Override
    public void calRedPoint(List<Integer> redPointIds, Map<Integer, String> redPointMap) {
//        if (redPointIds.contains(Integer.valueOf(RedPointConst.NEW_EMAIL))) {
//            checkRedPoint(redPointMap, emailList, RedPointConst.NEW_EMAIL);
//        }
        if (lastEvent != null) {
            if (lastEvent.getUntreatedCount() > 0) {
                redPointMap.put(RedPointConst.NEW_EMAIL, Integer.toString(lastEvent.getUntreatedCount()));
            } else {
                redPointMap.put(RedPointConst.NEW_EMAIL, null);
            }
            lastEvent = null;
        }
    }


    private void checkRedPoint(Map<Integer, String> redPointMap, Set<Integer> list, int redPointConst) {
        if (list.contains(0)) {
            list.remove(0);
        }
        StringBuilder builder = new StringBuilder("");
        if (!list.isEmpty()) {
            Iterator<Integer> iterator = list.iterator();
            while (iterator.hasNext()) {
                builder.append(iterator.next()).append("+");
            }
            redPointMap.put(redPointConst, builder.toString().isEmpty() ? null : builder.toString());
        } else {
            redPointMap.put(redPointConst, null);
        }
    }
    
    public void handleSpecialEmail(int emailType, int emailId){
    	if(emailType==EmailManager.EMAIL_TYPE_1){//微信绑定奖励邮件
    		if(getByte("email_award_weixin")==1){
    			ServiceHelper.emailService().delete(id(), emailId);
    		}else{    			
    			setByte("email_award_weixin", (byte)1);
    		}
    	}
    }
    
}
