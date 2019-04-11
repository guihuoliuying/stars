package com.stars.services;

import com.stars.ExcutorKey;
import com.stars.core.actor.invocation.ServiceActor;
import com.stars.core.schedule.SchedulerManager;
import com.stars.multiserver.fightManager.RMFSManagerService;
import com.stars.services.chat.ChatService;
import com.stars.services.family.activities.entry.FamilyActEntryService;
import com.stars.services.family.event.FamilyEventService;
import com.stars.services.family.main.FamilyMainService;
import com.stars.services.family.role.FamilyRoleService;
import com.stars.services.family.welfare.redpacket.FamilyRedPacketService;
import com.stars.services.fightServerManager.FSManagerService;
import com.stars.services.friend.FriendService;
import com.stars.services.friendInvite.InviteService;
import com.stars.services.id.IdService;
import com.stars.services.localservice.LocalService;
import com.stars.services.mail.EmailService;
import com.stars.services.multicommon.MultiCommonService;
import com.stars.services.pay.PayService;
import com.stars.services.role.RoleService;
import com.stars.services.summary.SummaryService;
import com.stars.util.LogUtil;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhaowenshuo on 2016/7/14.
 */
public class ServiceHelper {

    public static ConcurrentMap<Short, ServiceActor> serviceMapByPacketType = new ConcurrentHashMap<>();
    public static ConcurrentMap<String, ServiceActor> serviceMapByName = new ConcurrentHashMap<>();

    private static ServiceManager manager;
    private static Field[] fields = ServiceHelper.class.getDeclaredFields();

    public static void init(ServiceManager manager) throws Throwable {
        ServiceHelper.manager = manager;
        manager.init();
        initService();
        setupScheduleTask();
    }

    public static void initService() throws Exception {
        for (Field f : fields) {
            if (Service.class.isAssignableFrom(f.getType())) {
                f.setAccessible(true);
                f.set(ServiceHelper.class, manager.getService(f.getName()));
            }
        }
    }

    static void initField(String fieldName) throws Exception {
        for (Field f : fields) {
            if (f.getName().equals(fieldName)) {
                f.setAccessible(true);
                f.set(ServiceHelper.class, manager.getService(f.getName()));
            }
        }
    }

    /**
     * 手动调用保存
     *
     * @return
     */
    public static boolean executeSave() {
        try {
            manager.runScheduledJob();
        } catch (Throwable throwable) {
            LogUtil.error("", throwable);
            return false;
        }
        return true;
    }

    private static void setupScheduleTask() {
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.ServiceHelper,
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            manager.runScheduledJob();
                        } catch (Throwable e) {
                            LogUtil.error("", e);
                        }
                    }
                }, 5, 30, TimeUnit.SECONDS);
    }

    /* 服务字段属性 */
    static IdService idService;
    static RoleService roleService;
    static EmailService emailService;
    static FriendService friendService;
    static ChatService chatService;
    static SummaryService summaryService;
    static FamilyRoleService familyRoleService;
    static FamilyMainService familyMainService;
    static FamilyRedPacketService familyRedPacketService;
    static FamilyEventService familyEventService;
    static FamilyActEntryService familyActEntryService;
    static RMFSManagerService rmfManagerService;
    static FSManagerService fsManagerService;
    static PayService payService;
    static LocalService localService;
    static InviteService inviteService;
    static MultiCommonService multiCommonService;

    /* 访问方法 */
    public static IdService idService() {
        return idService;
    }

    public static RoleService roleService() {
        return roleService;
    }

    public static EmailService emailService() {
        return emailService;
    }

    public static FriendService friendService() {
        return friendService;
    }

    public static ChatService chatService() {
        return chatService;
    }

    public static SummaryService summaryService() {
        return summaryService;
    }

    public static FamilyRoleService familyRoleService() {
        return familyRoleService;
    }

    public static FamilyMainService familyMainService() {
        return familyMainService;
    }

    public static FamilyRedPacketService familyRedPacketService() {
        return familyRedPacketService;
    }

    public static FamilyEventService familyEventService() {
        return familyEventService;
    }

    public static FamilyActEntryService familyActEntryService() {
        return familyActEntryService;
    }

    public static RMFSManagerService rmfManagerService() {
        return rmfManagerService;
    }

    public static FSManagerService fsManagerService() {
        return fsManagerService;
    }

    public static ServiceManager getManager() {
        return manager;
    }

    public static PayService payService() {
        return payService;
    }

    public static Service getServiceByName(String serviceName) {
        return manager.getService(serviceName);
    }

    public static LocalService localService() {
        return localService;
    }

    public static InviteService inviteService() {
        return inviteService;
    }

    public static MultiCommonService multiCommonService() {
        return multiCommonService;
    }

}
