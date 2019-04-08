package com.stars.services.mail;

import com.google.common.cache.*;
import com.stars.core.persist.DbRowDao;
import com.stars.core.exception.AffixsCoolTimeException;
import com.stars.core.gmpacket.email.condition.RoleMatcherFactory;
import com.stars.core.gmpacket.email.util.EmailUtils;
import com.stars.core.gmpacket.email.vo.AllEmailGmPo;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.core.db.DbRow;
import com.stars.modules.data.DataManager;
import com.stars.modules.email.EmailManager;
import com.stars.modules.email.event.EmailLogEvent;
import com.stars.modules.email.event.EmailRedPointEvent;
import com.stars.modules.email.event.SpecialEmailEvent;
import com.stars.modules.email.packet.ClientEmail;
import com.stars.modules.email.pojodata.EmailConditionArgs;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.ServiceUtil;
import com.stars.services.mail.prodata.EmailTemplateVo;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.services.mail.userdata.RoleEmailInfoPo;
import com.stars.services.mail.userdata.RoleEmailPo;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.stars.modules.email.EmailManager.EMAIL_LIMIT;
import static com.stars.modules.email.EmailManager.templateMap;

/**
 * Created by zhaowenshuo on 2016/8/1.
 */
public class EmailServiceActor extends ServiceActor implements EmailService {

    private static boolean isLoadData = false;

    private static ConcurrentMap<Integer, AllEmailPo> allEmailMap = new ConcurrentHashMap<>();
    private static ConcurrentMap<Integer, AllEmailGmPo> allEmailGmMap = new ConcurrentHashMap<>();
    private static AtomicInteger allEmailIdGenerator = new AtomicInteger(0);

    private DbRowDao dao;
    private String serviceName;
    private Map<Long, RoleEmailData> onlineDataMap; // 在线数据列表
    private LoadingCache<Long, RoleEmailData> offlineDataMap; // 离线数据列表
    private Map<Long, RoleEmailData> pendingSavingDataMap; // 保存失败数据列表

    public EmailServiceActor(String id) {
        this.serviceName = "mail service-" + id;
    }

    public EmailServiceActor(int id) {
        this(Integer.toString(id));
    }

    @Override
    public void init() throws Throwable {
        // 加载产品数据
        synchronized (EmailServiceActor.class) {
            if (!isLoadData) {
                ConcurrentMap<Integer, AllEmailPo> tmpAllEmailMap = DBUtil.queryConcurrentMap(DBUtil.DB_USER, "allemailid", AllEmailPo.class,
                        "select * from `allemail`");
                ConcurrentMap<Integer, AllEmailGmPo> tmpAllEmailGmMap = DBUtil.queryConcurrentMap(DBUtil.DB_USER, "allemailgmid", AllEmailGmPo.class,
                        "select * from `allemailgm`");
                int maxAllEmailId = 0;
                for (Integer allEmailId : tmpAllEmailMap.keySet()) {
                    maxAllEmailId = allEmailId > maxAllEmailId ? allEmailId : maxAllEmailId;
                }
                for (Integer allEmailGmId : tmpAllEmailGmMap.keySet()) {
                    maxAllEmailId = allEmailGmId > maxAllEmailId ? allEmailGmId : maxAllEmailId;
                }
                allEmailMap = tmpAllEmailMap;
                allEmailGmMap = tmpAllEmailGmMap;
                allEmailIdGenerator = new AtomicInteger(maxAllEmailId);
                isLoadData = true;
            }
        }
        dao = new DbRowDao(serviceName);
        ServiceSystem.getOrAdd(serviceName, this);
        onlineDataMap = new HashMap<>();
        offlineDataMap = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(1800, TimeUnit.SECONDS)
                .removalListener(new EmailCacheRemovalListener())
                .build(new RoleEmailDataCacheLoader());
        pendingSavingDataMap = new HashMap<>();
    }

    @Override
    public void printState() {
        int onlineDataMapSize = onlineDataMap == null ? 0 : onlineDataMap.size();

        int offlineDataMapSize = (int) (offlineDataMap == null ? 0 : offlineDataMap.size());

        int pendingSavingDataMapSize = pendingSavingDataMap == null ? 0 : pendingSavingDataMap.size();

        LogUtil.info("容器大小输出:{},onlineDataMap:{},offlineDataMap:{}", this.getClass().getSimpleName(), onlineDataMapSize,
                offlineDataMapSize, pendingSavingDataMapSize);

    }

    @Override
    public void save() {
        dao.flush();
    }

    @Override
    public void online(long roleId, EmailConditionArgs emailConditionArgs) {
        try {
        /* 1. 加载玩家邮件数据
         * 2. 将玩家数据放到在线列表中
         */
            if (onlineDataMap.containsKey(roleId)) {
                return;
            }
            // 从待保存列表中取
            RoleEmailData roleEmailData = pendingSavingDataMap.get(roleId);
            if (roleEmailData != null) {
                onlineDataMap.put(roleId, roleEmailData);
                pendingSavingDataMap.remove(roleId);
                receiveAllEmailNew(roleEmailData, emailConditionArgs);
                sendRedPoint(roleId, roleEmailData);
                return;
            }
            // 从离线缓存中取
            roleEmailData = offlineDataMap.getIfPresent(roleId);
            if (roleEmailData != null) {
                onlineDataMap.put(roleId, roleEmailData);
                offlineDataMap.invalidate(roleId);
                receiveAllEmailNew(roleEmailData, emailConditionArgs);
                sendRedPoint(roleId, roleEmailData);
                return;
            }
            // 从数据库中取

            RoleEmailInfoPo infoPo = DBUtil.queryBean(DBUtil.DB_USER, RoleEmailInfoPo.class,
                    "select * from `roleemailinfo` where `roleid`=" + roleId);
            Map<Integer, RoleEmailPo> emailPoMap = DBUtil.queryMap(DBUtil.DB_USER, "emailid", RoleEmailPo.class,
                    "select * from `roleemail` where `receiverid`=" + roleId + " order by `emailid` asc");
            if (infoPo == null && (emailPoMap == null || emailPoMap.size() == 0)) { // 新建
                infoPo = new RoleEmailInfoPo(roleId, 0, allEmailIdGenerator.get()); // 新建玩家不会收到以前发送的全服邮件
                emailPoMap = new HashMap<>();
                dao.insert(infoPo);
            }
            RoleEmailData data = new RoleEmailData(infoPo, emailPoMap);
            receiveAllEmailNew(data, emailConditionArgs); // 加载未接收到的全服邮件
            onlineDataMap.put(roleId, data);
            sendRedPoint(roleId, data);

        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }


    private void sendRedPoint(long roleId, RoleEmailData data) {
//        Set<Integer> emailList = new HashSet<>();
//        for (Map.Entry<Integer, RoleEmailPo> entry : data.emailMap().entrySet()) {
//            if (!entry.getValue().isRead()) {
//                emailList.add(entry.getValue().getEmailId());
//            }
//        }
//        ServiceHelper.roleService().notice(roleId, new AddEmailEvent(emailList));
        ServiceHelper.roleService().notice(roleId, new EmailRedPointEvent(data.getUntreatedEmailCount()));
    }

    @Override
    public void offline(long roleId) {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            offlineDataMap.put(roleId, data); // 放到离线列表中
            onlineDataMap.remove(roleId); // 从在线列表中移除
        } else {
            LogUtil.error("没有相关邮件");
        }
    }

    @Override
    public void sendMailListToRole(long roleId) {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            ClientEmail packet = new ClientEmail(ClientEmail.C_GET_LIST);
            packet.setEmailPoList(data.emailMap());
            PlayerUtil.send(roleId, packet);
//            LogUtil.info("sendMailListToRole, roleId={}, emailIdSet={}", roleId, data.emailMap().keySet());
        } else {
            LogUtil.error("玩家不在线, roleId=" + roleId);
        }
    }

    @Override
    public void read(long roleId, int mailId) {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            RoleEmailPo roleEmailPo = data.emailMap().get(mailId);
            if (roleEmailPo != null) {
                roleEmailPo.setIsRead((byte) 1);
                dao.update(roleEmailPo);
                //红点逻辑
                data.updateUntreatedEmail(mailId);
//                ServiceHelper.roleService().notice(roleId, new EmailRedPointEvent(data.getUntreatedEmailCount()));
                sendRedPoint(roleId, data);
//                ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent(roleEmailPo.getEmailId())); // todo:

                EmailLogEvent mailLog = new EmailLogEvent(EmailLogEvent.OPRECEIVER, roleEmailPo);
                ServiceHelper.roleService().notice(roleId, mailLog);
            } else {
                LogUtil.error("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
            }
        } else {
            LogUtil.error("玩家不在线, roleId=" + roleId);
        }
    }

    @Override
    public void delete(long roleId, int mailId) {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            RoleEmailPo roleEmailPo = data.emailMap().get(mailId);
            if (roleEmailPo == null) {
                LogUtil.error("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
                ServiceUtil.sendText(roleId, I18n.get("email.delete.noSuchEmail")); // 不存在该邮件
                return;
            }
            if (roleEmailPo.hasAffixs() && !roleEmailPo.isGetAffixs()) {
                LogUtil.error("邮件存在附件没领取, roleId=" + roleId + ", emailId=" + mailId);
                ServiceUtil.sendText(roleId, I18n.get("email.delete.notFetchedAffixs")); // 邮件存在附件没领取
                return;
            }
            int currentTime = DateUtil.getCurrentTimeInt();
            if (roleEmailPo.getCoolTime()!=0&&currentTime<=roleEmailPo.getCoolTime()){//冻结时间内 不删除
            	LogUtil.error("邮件冻结时间内, roleId=" + roleId + ", emailId=" + mailId);
            	int dateAfterN = DateUtil.getDateAfterN(0);
            	int days = (roleEmailPo.getCoolTime()-dateAfterN)/(24*3600);
                ServiceUtil.sendText(roleId, I18n.get("email.delete.stillcooling", days)); // 邮件存在附件没领取
            	return;
            }
            EmailLogEvent mailLog = new EmailLogEvent(EmailLogEvent.OPDELETE, roleEmailPo);
            ServiceHelper.roleService().notice(roleId, mailLog);
            data.emailMap().remove(mailId);
            data.deleteEmailFromList(mailId);
            data.removeUntreatedEmail(mailId);
            dao.delete(roleEmailPo);
            LogUtil.info("删除邮件, 邮件信息: " + roleEmailPo);
            ClientEmail packet = new ClientEmail(ClientEmail.C_DELETE);
            packet.setEmailId(mailId);
            PlayerUtil.send(roleId, packet);

//            ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent(roleEmailPo.getEmailId()));
//            ServiceHelper.roleService().notice(roleId, new EmailRedPointEvent(data.getUntreatedEmailCount()));
            sendRedPoint(roleId, data);
        } else {
            LogUtil.error("玩家不在线, roleId=" + roleId);
        }
    }

    @Override
    public void deleteAll(long roleId) {
//        printActorNumber("deleteAll()");
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data == null) {
            LogUtil.error("玩家不在线, roleId=" + roleId);
            return;
        }
        if (data.emailMap() == null || data.emailMap().size() == 0) {
            ServiceUtil.sendText(roleId, "email_delete_none");
            return;
        }

        int currentTime = DateUtil.getCurrentTimeInt();
        List<Integer> emailIdList = new ArrayList<>();
        Iterator<RoleEmailPo> itor = data.emailMap().values().iterator();
        while (itor.hasNext()) {
            RoleEmailPo roleEmailPo = itor.next();
            if (roleEmailPo.hasAffixs() && !roleEmailPo.isGetAffixs()) {
                LogUtil.error("邮件存在附件没领取, roleId=" + roleId + ", emailId=" + roleEmailPo.getEmailId());
                continue;
            }
            if (roleEmailPo.getCoolTime()!=0&&currentTime<=roleEmailPo.getCoolTime()){//冻结时间内 不删除
            	LogUtil.error("邮件冻结时间内, roleId=" + roleId + ", emailId=" + roleEmailPo.getEmailId());
            	continue;
            }
            itor.remove();
            data.deleteEmailFromList(roleEmailPo.getEmailId());
            data.removeUntreatedEmail(roleEmailPo.getEmailId());
            dao.delete(roleEmailPo);
            EmailLogEvent mailLog = new EmailLogEvent(EmailLogEvent.OPDELETE, roleEmailPo);
            ServiceHelper.roleService().notice(roleId, mailLog);
            emailIdList.add(roleEmailPo.getEmailId());
            LogUtil.info("删除邮件, 邮件信息: " + roleEmailPo);
        }
        ClientEmail packet = new ClientEmail(ClientEmail.C_ALL_DELETE);
        packet.setEmailIdList(emailIdList);
        PlayerUtil.send(roleId, packet);

//        ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent());
        sendRedPoint(roleId, data);
    }

    @Override
    public void sendToSingle(RoleEmailPo emailPo) {
        // todo: 1. check mail
        // todo: 2. check mail list.size()
        EmailUtils.checkRoleEmailPo(emailPo);
        boolean isOnline = false;
        RoleEmailData data = onlineDataMap.get(emailPo.getReceiverId());
        if (data != null) {
            isOnline = true;
        } else {
            data = pendingSavingDataMap.get(emailPo.getReceiverId());
            if (data == null) {
                try {
                    data = offlineDataMap.getUnchecked(emailPo.getReceiverId());
                } catch (Exception e) {
                    LogUtil.error("roleId=" + emailPo.getReceiverId(), e);
                }
            }
        }

        if (data != null) {
            clearUp(data, 1, isOnline);
            emailPo.setEmailId(data.info().nextRoleEmailId());
            /**
             * 发送全服邮件的同时修改全服邮件的接收id
             */
            if (emailPo.getRefEmailId() > data.info().getAllEmailId()) {
                data.info().setAllEmailId(emailPo.getRefEmailId());
            }
            data.emailMap().put(emailPo.getEmailId(), emailPo);
            data.addEmailToList(emailPo);
            data.addUntreatedEmail(emailPo.getEmailId());
            dao.update(data.info());
            dao.insert(emailPo);
            LogUtil.info("sendToSingle(insert,{}), roleId={}, emailId={}", serviceName, emailPo.getReceiverId(), emailPo.getEmailId());
            // notify role
            if (isOnline) {
//                ServiceHelper.roleService().gameboard(data.info().getRoleId(), new NewMailNotification());
                ClientEmail packet = new ClientEmail(ClientEmail.C_NEW);
                Map<Integer, RoleEmailPo> emailPoMap = new HashMap<>();
                emailPoMap.put(emailPo.getEmailId(), emailPo);
                packet.setEmailPoList(emailPoMap);
                PlayerUtil.send(emailPo.getReceiverId(), packet);
//                LogUtil.info("sendToSingle(send,{}), roleId={}, emailId={}", serviceName, emailPo.getReceiverId(), emailPo.getEmailId());
                //红点逻辑
//                ServiceHelper.roleService().notice(data.info().getRoleId(), new EmailRedPointEvent(data.getUntreatedEmailCount()));
//                ServiceHelper.roleService().notice(data.info().getRoleId(), new AddEmailEvent(emailPo.getEmailId()));
                sendRedPoint(emailPo.getReceiverId(), data);
            }

            //打印邮件日志
            EmailLogEvent logEvent = new EmailLogEvent(EmailLogEvent.OPSEND, emailPo);
            ServiceHelper.roleService().notice(data.info().getRoleId(), logEvent);

        } else {
            LogUtil.error("玩家邮件数据不存在, roleId=" + emailPo.getReceiverId() + ", " + emailPo.toString());
        }
    }

    @Override
    public void sendToSingle(long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, String... params) {
        LogUtil.info("sendToSingle({}), roleId={}, templateId={}, senderId={}, senderName={}", serviceName, roleId, templateId, senderId, senderName);
        EmailTemplateVo templateVo = templateMap.get(templateId);
        if (templateVo == null) {
            throw new IllegalArgumentException("邮件模板不存在, templateId=" + templateId);
        }
        RoleEmailPo roleEmailPo = EmailUtils.newRoleEmailPo(templateVo);
        if (senderId != null) {
            roleEmailPo.setSenderId(senderId);
        }
        if (senderName != null) {
            roleEmailPo.setSenderName(senderName);
        }
        if (affixMap != null) {
            roleEmailPo.setAffixMap(new HashMap<>(affixMap));
        }
        roleEmailPo.setReceiverId(roleId);
        roleEmailPo.setParamsArray(params);
        sendToSingle(roleEmailPo);
    }
    
    @Override
    public void sendToSingleWithCoolTime(long roleId, int templateId, Long senderId, String senderName, Map<Integer, Integer> affixMap, int coolTime, String... params) {
        LogUtil.info("sendToSingle({}), roleId={}, templateId={}, senderId={}, senderName={}", serviceName, roleId, templateId, senderId, senderName);
        EmailTemplateVo templateVo = templateMap.get(templateId);
        if (templateVo == null) {
            throw new IllegalArgumentException("邮件模板不存在, templateId=" + templateId);
        }
        RoleEmailPo roleEmailPo = EmailUtils.newRoleEmailPo(templateVo);
        if (senderId != null) {
            roleEmailPo.setSenderId(senderId);
        }
        if (senderName != null) {
            roleEmailPo.setSenderName(senderName);
        }
        if (affixMap != null) {
            roleEmailPo.setAffixMap(new HashMap<>(affixMap));
        }
        roleEmailPo.setReceiverId(roleId);
        roleEmailPo.setParamsArray(params);
        roleEmailPo.setCoolTime(coolTime);
        sendToSingle(roleEmailPo);
    }

    @Override
    public void sendTo(List<Long> roleIdList, byte senderType, Long senderId, String senderName, String title, String text, Map<Integer, Integer> affixMap) {
        EmailUtils.checkTitleAndText(title, text);
        for (Long roleId : roleIdList) {
            RoleEmailPo emailPo = new RoleEmailPo();
            emailPo.setReceiverId(roleId);
            emailPo.setTextMode(RoleEmailPo.TEXT_MODE_SERVER);
            emailPo.setSenderType(senderType);
            emailPo.setSenderId(senderId);
            emailPo.setSenderName(senderName);
            emailPo.setTitle(title);
            emailPo.setText(text);
            emailPo.setAffixMap(affixMap);
            sendToSingle(emailPo);
        }
    }

    //    @Override
    public void sendToOnline(AllEmailPo emailPo) {
        EmailUtils.checkAllEmailPo(emailPo);
        for (RoleEmailData data : onlineDataMap.values()) {
            RoleEmailPo roleEmailPo = EmailUtils.newRoleEmailPo(emailPo);
            roleEmailPo.setEmailId(data.info().nextRoleEmailId());
            roleEmailPo.setReceiverId(data.info().getRoleId());
            sendToSingle(roleEmailPo);
        }
    }

    // todo: 添加参数(是否要做多一次记录)
    @Override
    public void sendToOnline(int templateId, Long senderId, String senderName, String... params) {
        EmailTemplateVo templateVo = templateMap.get(templateId);
        if (templateVo == null) {
            throw new IllegalArgumentException("邮件模板不存在, templateId=" + templateId);
        }
        AllEmailPo allEmailPo = EmailUtils.newAllEmailPo(templateVo);
        if (senderId != null) {
            allEmailPo.setSenderId(senderId);
        }
        if (senderName != null) {
            allEmailPo.setSenderName(senderName);
        }
        if (params != null) {
            allEmailPo.setParamsArray(params);
        }
        sendToOnline(allEmailPo);
    }

    //    @Override
    public void sendToAll(AllEmailPo emailPo) {
        EmailUtils.checkAllEmailPo(emailPo);
        int allMailId = allEmailIdGenerator.incrementAndGet();
        emailPo.setAllEmailId(allMailId);
        allEmailMap.put(allMailId, emailPo);
        dao.insert(emailPo);
        dao.flush();
        innerSendToAll(emailPo);
    }

    @Override
    public void sendToAll(int templateId, Long senderId, String senderName, String... params) {
        EmailTemplateVo templateVo = templateMap.get(templateId);
        if (templateVo == null) {
            throw new IllegalArgumentException("邮件模板不存在, templateId=" + templateId);
        }
        AllEmailPo allEmailPo = EmailUtils.newAllEmailPo(templateVo);
        if (senderId != null) {
            allEmailPo.setSenderId(senderId);
        }
        if (senderName != null) {
            allEmailPo.setSenderName(senderName);
        }
        if (params != null) {
            allEmailPo.setParamsArray(params);
        }
//        allEmailPo.setAllEmailId(allEmailIdGenerator.incrementAndGet());
        sendToAll(allEmailPo);
    }

    @Override
    public Map<Integer, Integer> getAffixs(long roleId, int mailId) throws Exception {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            RoleEmailPo roleEmailPo = data.emailMap().get(mailId);
            if (roleEmailPo != null) {
                /**
                 * 获取附件时计算冷却时间，否则不能领取
                 */
                AffixsCoolTimeException affixsCoolTimeException = checkAffixCoolTimeState(roleEmailPo);
                if (affixsCoolTimeException != null) {
                    throw affixsCoolTimeException;
                }
                Map<Integer, Integer> affixs = roleEmailPo.getAffixMap();
                if (affixs == null) {
                    affixs = new HashMap<>();
                }
//                ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent(roleEmailPo.getEmailId()));
                EmailLogEvent maillog = new EmailLogEvent(EmailLogEvent.OPGETTOOL, roleEmailPo);
                ServiceHelper.roleService().notice(roleId, maillog);
                return affixs;

            } else {
                LogUtil.error("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
                throw new IllegalStateException("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
            }
        } else {
            LogUtil.error("玩家不在线, roleId=" + roleId);
            throw new IllegalStateException("玩家不在线, roleId=" + roleId);
        }
    }

    @Override
    public Map<Integer, Map<Integer, Integer>> getAllAffixs(long roleId) throws Exception {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data == null) {
            LogUtil.error("玩家不在线, roleId=" + roleId);
            throw new IllegalStateException("玩家不在线, roleId=" + roleId);
        }
        Map<Integer, Map<Integer, Integer>> map = new HashMap<>();
        List<AffixsCoolTimeException> affixsCoolTimeExceptions = new ArrayList<>();
        for (RoleEmailPo roleEmailPo : data.emailMap().values()) {
            if (roleEmailPo.hasAffixs() && !roleEmailPo.isGetAffixs()) {
                /**
                 * 获取附件时计算冷却时间，否则不能领取
                 */
                AffixsCoolTimeException affixsCoolTimeException = checkAffixCoolTimeState(roleEmailPo);
                if (affixsCoolTimeException != null) {
                    affixsCoolTimeExceptions.add(affixsCoolTimeException);
                    continue;
                }
                Map<Integer, Integer> affixs = roleEmailPo.getAffixMap();
                map.put(roleEmailPo.getEmailId(), new HashMap<Integer, Integer>(roleEmailPo.getAffixMap()));
//                ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent(roleEmailPo.getEmailId()));
                EmailLogEvent mailLog = new EmailLogEvent(EmailLogEvent.OPGETTOOL, roleEmailPo);
                ServiceHelper.roleService().notice(roleId, mailLog);
            }
        }
        if (affixsCoolTimeExceptions.size() > 0) {
            AffixsCoolTimeException affixsCoolTimeException = affixsCoolTimeExceptions.get(0);
            affixsCoolTimeException.setAllToolMap(map);
            throw affixsCoolTimeException;
        }
        return map;
    }

    private AffixsCoolTimeException checkAffixCoolTimeState(RoleEmailPo roleEmailPo) {
        int allEmailId = roleEmailPo.getRefEmailId();
        if (allEmailId != 0) {
            AllEmailPo allEmailPo = allEmailMap.get(allEmailId);
            if (allEmailPo == null) {
                AllEmailGmPo allEmailGmPo = allEmailGmMap.get(allEmailId);
                allEmailPo = EmailUtils.newAllEmail(allEmailGmPo, true);
            }
            int coolTime = allEmailPo.getCoolTime();

            if (coolTime != 0) {
                Calendar coolEndTime = Calendar.getInstance();
                coolEndTime.setTimeInMillis(allEmailPo.getSendTime() * 1000L);
                coolEndTime.add(Calendar.MINUTE, coolTime);
                Calendar now = Calendar.getInstance();
                if (now.before(coolEndTime)) {
                    int seconds = DateUtil.getSecondsBetweenTwoDates(now.getTime(), coolEndTime.getTime());
                    return new AffixsCoolTimeException("当前附件处于冷却期", seconds);
                }
            }
        }
        return null;
    }

    @Override
    public void fetchAffixs(long roleId, int mailId) throws Exception {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data != null) {
            RoleEmailPo roleEmailPo = data.emailMap().get(mailId);
            if (roleEmailPo != null) {
                if (!roleEmailPo.isGetAffixs()) {
                    roleEmailPo.setIsRead((byte) 1);
                    roleEmailPo.setIsGetAffixs((byte) 1);
                    dao.update(roleEmailPo);
                    data.updateUntreatedEmail(roleEmailPo.getEmailId());
                    sendRedPoint(roleId, data);
                } else {
                    LogUtil.error("邮件附件已领取, roleId=" + roleId + ", emailId=" + mailId);
                    throw new IllegalStateException("邮件附件已领取, roleId=" + roleId + ", emailId=" + mailId);
                }
            } else {
                LogUtil.error("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
                throw new IllegalStateException("邮件不存在, roleId=" + roleId + ", emailId=" + mailId);
            }
        } else {
            LogUtil.error("玩家不在线, roleId=" + roleId);
            throw new IllegalStateException("玩家不在线, roleId=" + roleId);
        }
    }

    @Override
    public void innerSendToAll(AllEmailPo allEmailPo) {
        // 在线玩家全部发送，并更新玩家的全局邮件索引
        for (Map.Entry<Long, RoleEmailData> entry : onlineDataMap.entrySet()) {
            RoleEmailData data = entry.getValue();
            RoleEmailPo emailPo = EmailUtils.newRoleEmailPo(allEmailPo);
            emailPo.setReceiverId(data.info().getRoleId());
            sendToSingle(emailPo);
            data.info().setAllEmailId(allEmailPo.getAllEmailId());
            //红点逻辑
//            ServiceHelper.roleService().notice(data.info().getRoleId(), new AddEmailEvent(allEmailPo.getAllEmailId()));
            dao.update(data.info());
        }
        // 缓存中的玩家全部发送
        for (Map.Entry<Long, RoleEmailData> entry : offlineDataMap.asMap().entrySet()) {
            RoleEmailData data = entry.getValue();
            RoleEmailPo emailPo = EmailUtils.newRoleEmailPo(allEmailPo);
            emailPo.setReceiverId(data.info().getRoleId());
            sendToSingle(emailPo);
            data.info().setAllEmailId(allEmailPo.getAllEmailId());
//            ServiceHelper.roleService().notice(data.info().getRoleId(), new AddEmailEvent(allEmailPo.getAllEmailId()));
            dao.update(data.info());
        }
    }

    /**
     * 发送给白名单账户中的合格的角色
     *
     * @param checkedRoleResult
     */
    @Override
    public void gmSendToWhite(AllEmailGmPo allEmailGmPo, Map<Integer, Set<Long>> checkedRoleResult) {
        AllEmailPo allEmailPo = EmailUtils.newAllEmail(allEmailGmPo, true);
        /**
         * 放入白名单全服邮件中
         */
        allEmailGmMap.put(allEmailGmPo.getAllEmailGmId(), allEmailGmPo);
        gmSendToAll(allEmailPo, checkedRoleResult, true);

    }

    /**
     * 发送给服务器或缓存中的合格的角色
     *
     * @param allEmailPo
     * @param checkedRoleMap
     */
    @Override
    public void gmSendToAll(AllEmailPo allEmailPo, Map<Integer, Set<Long>> checkedRoleMap, boolean isWhite) {
        if (!isWhite) {
            allEmailMap.put(allEmailPo.getAllEmailId(), allEmailPo);
        }
        LogUtil.info("gmSendToAll:allEmailMap:", StringUtil.makeString(allEmailMap.keySet(),'+'));
        LogUtil.info("gmSendToAll:allEmailGMMap:", StringUtil.makeString(allEmailGmMap.keySet(),'+'));
        for (Long roleId : checkedRoleMap.get(RoleMatcherFactory.PASS)) {
            RoleEmailData roleEmailData = null;
            if (!isWhite) {
                roleEmailData = onlineDataMap.get(roleId);
                if (roleEmailData == null) {
                    roleEmailData = offlineDataMap.getIfPresent(roleId);
                }
                if (roleEmailData == null) {
                    continue;
                }
            }

            RoleEmailPo emailPo = EmailUtils.newRoleEmailPo(allEmailPo);
            emailPo.setReceiverId(roleId);
            /**
             * 条件通过的角色发送全服邮件
             */

            ServiceHelper.emailService().sendToSingle(emailPo);
        }

    }

    @Override
    public void gmSend(RoleEmailPo emailPo) {
        sendToSingle(emailPo);
        if(emailPo.getEmailType()==EmailManager.EMAIL_TYPE_1){
        	Player player = PlayerSystem.get(emailPo.getReceiverId());
            if (player != null) {            	
            	SpecialEmailEvent event = new SpecialEmailEvent();
            	event.setEmailType(emailPo.getEmailType());
            	event.setEmailId(emailPo.getEmailId());
            	ServiceHelper.roleService().notice(emailPo.getReceiverId(), event);
            }else{
            	StringBuffer sql = new StringBuffer();
            	sql.append("insert into rolerecords values(").append(emailPo.getReceiverId())
            	.append(", 'email_award_weixin',").append(1).append(");");
            	try {
					DBUtil.execUserSql(sql.toString());
				} catch (SQLException e) {
					LogUtil.error("special email handle fail, roleId:"+emailPo.getReceiverId()+
							", emailType:"+emailPo.getEmailType(), e);
					delete(emailPo.getReceiverId(), emailPo.getEmailId());
				}
            }
        }
    }

    @Override
    public List<Map<String, Object>> gmView(long roleId) {
        RoleEmailData data = getData(roleId);
        if (data == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoleEmailPo emailPo : data.emailMap().values()) {
            Map<String, Object> email = new HashMap<>();
            email.put("id", emailPo.getEmailId());
            String titleText = DataManager.getGametext(emailPo.getTitle());
            email.put("title", titleText == null ? emailPo.getTitle() : titleText);
            String contentText = DataManager.getGametext(emailPo.getText());
            contentText = contentText == null ? emailPo.getText() : contentText;
            if (emailPo.getParams() != null && emailPo.getParams().length() > 0) {
                contentText = contentText + " " + emailPo.getParams();
            }
            email.put("content", contentText);
            email.put("toolList", emailPo.getAffixs());
            email.put("sendTime", emailPo.getSendTime());
            email.put("receiveTime", 0);
            email.put("expireTime", 0);
            list.add(email);
        }
        return list;
    }

    @Override
    public List<Integer> gmDele(long roleId, List<Integer> emailIdList) {
        RoleEmailData data = getData(roleId);
        List<Integer> failureEmailIdList = new ArrayList<>();
        if (data != null) {
            for (int emailId : emailIdList) {
                RoleEmailPo roleEmailPo = data.emailMap().get(emailId);
                if (roleEmailPo == null) {
                    failureEmailIdList.add(emailId);
                    continue;
                }
//                ServiceHelper.roleService().notice(roleId, new RemoveEmailEvent(roleEmailPo.getEmailId()));
                data.emailMap().remove(emailId);
                data.deleteEmailFromList(emailId);
                data.removeUntreatedEmail(emailId);
                dao.delete(roleEmailPo);
                LogUtil.info("删除邮件, 邮件信息: " + roleEmailPo);
                if (onlineDataMap.containsKey(roleId)) {
                    ClientEmail packet = new ClientEmail(ClientEmail.C_DELETE);
                    packet.setEmailId(emailId);
                    ServiceHelper.roleService().send(roleId, packet);

                    sendRedPoint(roleId, data);
                }
            }
        } else {
            failureEmailIdList.addAll(emailIdList);
        }
        return failureEmailIdList;
    }

    //    @Override
//    public void innerSendToSingle(RoleEmailPo emailPo) {
//        ServiceHelper.emailService().sendToSingle(emailPo); // 这里要走代理（因为异步）
//    }

    private RoleEmailData getData(long roleId) {
        RoleEmailData data = onlineDataMap.get(roleId);
        if (data == null) {
            data = pendingSavingDataMap.get(roleId);
        }
        if (data == null) {
            try {
                data = offlineDataMap.getUnchecked(roleId);
            } catch (Exception e) {
                LogUtil.error("roleId=" + roleId, e);
            }
        }
        return data;
    }

    private void receiveAllEmail(RoleEmailData data) {
        // 找到最大的allEmailId
        int allEmailId = EmailUtils.findMaxRoleAllEmailId(data);
        if (allEmailId > data.info().getAllEmailId()) {
            data.info().setAllEmailId(allEmailId);
            dao.update(data.info());
        } else {
            allEmailId = data.info().getAllEmailId();
        }
        //
        int newCount = 0;
        for (AllEmailPo allEmailPo : allEmailMap.values()) {
            if (allEmailPo.getAllEmailId() > allEmailId) {
                RoleEmailPo roleEmailPo = EmailUtils.newRoleEmailPo(allEmailPo);
                roleEmailPo.setEmailId(data.info().nextRoleEmailId());
                roleEmailPo.setReceiverId(data.info().getRoleId());
                dao.insert(roleEmailPo);
                data.emailMap().put(roleEmailPo.getEmailId(), roleEmailPo);
                data.addEmailToList(roleEmailPo);
                data.addUntreatedEmail(roleEmailPo.getEmailId());
                newCount++;
            }
        }
        //
        allEmailId = EmailUtils.findMaxRoleAllEmailId(data);
        if (allEmailId > data.info().getAllEmailId()) {
            data.info().setAllEmailId(allEmailId);
            dao.update(data.info());
        }
        clearUp(data, newCount, false);
    }

    /**
     * 接收全服邮件(新)
     */
    private void receiveAllEmailNew(RoleEmailData data, EmailConditionArgs emailConditionArgs) {
        LogUtil.info("roleid:{} receiving allmail", data.info().getRoleId());
        int maxAllEmailId = EmailUtils.findMaxRoleAllEmailId(data);
        if (maxAllEmailId > data.info().getAllEmailId()) {
            data.info().setAllEmailId(maxAllEmailId);
        } else {
            maxAllEmailId = data.info().getAllEmailId();
        }
        //
        int newCount = 0;
        Set<Integer> newEmails = new HashSet<>();
        for (AllEmailPo allEmailPo : allEmailMap.values()) {
            if (allEmailPo.getAllEmailId() > maxAllEmailId) {
                LogUtil.info("roleid:{} to receive allmailid={}", data.info().getRoleId(),allEmailPo.getAllEmailId());
                Map<Integer, Set<Long>> checkRoleStateMap = EmailUtils.checkRoleState(allEmailPo, emailConditionArgs);
                if (checkRoleStateMap.get(RoleMatcherFactory.PASS).size() > 0) {
                    /**
                     * 判断此时是否在过期时间之前，已经过期则此邮件无法领取
                     */
                    int expireTime = allEmailPo.getExpireTime();
                    Calendar now = Calendar.getInstance();
                    Calendar expireDate = Calendar.getInstance();
                    expireDate.setTimeInMillis(allEmailPo.getSendTime() * 1000L);
                    expireDate.add(Calendar.DAY_OF_YEAR, expireTime);

                    if (expireTime == 0 || now.before(expireDate)) {
                        LogUtil.info("roleid:{} to receive allmailid={} successful", data.info().getRoleId(),allEmailPo.getAllEmailId());
                        RoleEmailPo roleEmailPo = EmailUtils.newRoleEmailPo(allEmailPo);
                        roleEmailPo.setEmailId(data.info().nextRoleEmailId());
                        roleEmailPo.setReceiverId(data.info().getRoleId());
                        dao.insert(roleEmailPo);
                        data.emailMap().put(roleEmailPo.getEmailId(), roleEmailPo);
                        data.addEmailToList(roleEmailPo);
                        data.addUntreatedEmail(roleEmailPo.getEmailId());
                        newEmails.add(roleEmailPo.getEmailId());
                        newCount++;

                    }


                }
                if (allEmailPo.getAllEmailId() > data.info().getAllEmailId()) {
                    data.info().setAllEmailId(allEmailPo.getAllEmailId());
                }
            }
        }
//        if (newEmails.size() > 0) {
//            ServiceHelper.roleService().notice(data.info().getRoleId(), new AddEmailEvent(newEmails));
//        }
        dao.update(data.info());

        clearUp(data, newCount, false);
    }


    public static AtomicInteger getAllEmailIdGenerator() {
        return allEmailIdGenerator;
    }


    private void printActorNumber(String funcName) {
        LogUtil.info(serviceName + "    " + funcName);
    }

    private void clearUp(RoleEmailData data, int requiredRoomForEmail, boolean needSend) {
        int restRoom = EMAIL_LIMIT - data.emailMap().size();
        if (restRoom >= requiredRoomForEmail) {
            return;
        }
        // todo:
        int evictCount = requiredRoomForEmail - restRoom;
        List<RoleEmailPo> emailList = data.emailList();
        List<Integer> deletedIdList = new ArrayList<>();
        for (int i = 0; i < emailList.size() && evictCount > 0; ) {
            RoleEmailPo roleEmailPo = emailList.get(i);
            if (!roleEmailPo.hasAffixs()) {
                data.emailMap().remove(roleEmailPo.getEmailId());
                emailList.remove(i);
                dao.delete(roleEmailPo);
                deletedIdList.add(roleEmailPo.getEmailId());
                evictCount--;
            } else {
                i++;
            }
        }
        for (int i = 0; i < emailList.size() && evictCount > 0; ) {
            RoleEmailPo roleEmailPo = emailList.get(i);
            data.emailMap().remove(roleEmailPo.getEmailId());
            emailList.remove(i);
            dao.delete(roleEmailPo);
            deletedIdList.add(roleEmailPo.getEmailId());
            evictCount--;
        }
        if (needSend) {
            ClientEmail packet = new ClientEmail(ClientEmail.C_ALL_DELETE);
            packet.setEmailIdList(deletedIdList);
            PlayerUtil.send(data.info().getRoleId(), packet);
//            ServiceHelper.roleService().send(data.info().getRoleId(), packet);
        }
    }

    public static ConcurrentMap<Integer, AllEmailPo> getAllEmailMap() {
        return allEmailMap;
    }

    public static ConcurrentMap<Integer, AllEmailGmPo> getAllEmailGmMap() {
        return allEmailGmMap;
    }

    class RoleEmailDataCacheLoader extends CacheLoader<Long, RoleEmailData> {
        @Override
        public RoleEmailData load(Long roleId) throws Exception {
            DbRowDao dbRowDao = new DbRowDao("RoleEmailInfoPo");
            // 从待保存列表找
            RoleEmailData data = pendingSavingDataMap.get(roleId);
            if (data != null) {
                pendingSavingDataMap.remove(roleId);
                return data;
            }
            // 从数据库找
            RoleEmailInfoPo infoPo = DBUtil.queryBean(DBUtil.DB_USER, RoleEmailInfoPo.class,
                    "select * from `roleemailinfo` where `roleid`=" + roleId);
            Map<Integer, RoleEmailPo> emailPoMap = DBUtil.queryMap(DBUtil.DB_USER, "emailid", RoleEmailPo.class,
                    "select * from `roleemail` where `receiverid`=" + roleId + " order by `emailid` asc");
//            if (infoPo == null && (emailPoMap == null || emailPoMap.size() == 0)) { // 新建
//                infoPo = new RoleEmailInfoPo(roleId, 0, 0);
//                emailPoMap = new HashMap<>();
//                dao.insert(infoPo);
//            }
            if (infoPo == null) {
//                throw new NoSuchElementException();
                infoPo = new RoleEmailInfoPo(roleId, 0, 0);
                dbRowDao.insert(infoPo);
                dbRowDao.flush();
            }
            data = new RoleEmailData(infoPo, emailPoMap);
            return data;
        }
    }

    class EmailCacheRemovalListener implements RemovalListener<Long, RoleEmailData> {
        @Override
        public void onRemoval(RemovalNotification<Long, RoleEmailData> notification) {
            if (notification.wasEvicted()) {
                Set<DbRow> set = new HashSet<DbRow>();
                set.addAll(notification.getValue().emailMap().values());
                set.add(notification.getValue().info());
                if (!dao.isSavingSucceeded(set)) {
                    pendingSavingDataMap.put(notification.getKey(), notification.getValue());
                    LogUtil.error("邮件缓存移除异常，roleId=" + notification.getKey());
                }
            }
        }
    }
}
