package com.stars.services.guest;

import com.stars.core.persist.DbRowDao;
import com.stars.core.gmpacket.specialaccount.SpecialAccountManager;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.event.FamilyLogEvent;
import com.stars.modules.guest.GuestManager;
import com.stars.modules.guest.packet.ClientGuest;
import com.stars.modules.guest.userdata.RoleGuestExchange;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.serverLog.event.SpecialAccountEvent;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.util.DateUtil;
import com.stars.util.I18n;
import com.stars.util.LogUtil;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.*;

/**
 * Created by zhouyaohui on 2017/1/9.
 */
public class GuestServiceActor extends ServiceActor implements GuestService {

    private DbRowDao dao = new DbRowDao();
    private Map<Long, Map<Integer, RoleGuestExchange>> exchangeMap = new HashMap<>();   // roleId <-> map(stamp<->roleguestexchange)
    private Map<Long, LinkedList<RoleGuestExchange>> listMap = new HashMap<>(); // familyId <-> list;

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.GuestService, this);
        // 加载交换数据
        String sql = "select * from roleguestexchange where stamp > "
                + (DateUtil.getSecondTime() - GuestManager.HELP_LIMIT_TIME) + " and familyid > 0";
        List<RoleGuestExchange> list = DBUtil.queryList(DBUtil.DB_USER, RoleGuestExchange.class, sql);
        Collections.sort(list, new StampComparator(true));
        for (RoleGuestExchange exchange : list) {
            Map<Integer, RoleGuestExchange> map = exchangeMap.get(exchange.getRoleId());
            if (map == null) {
                map = new HashMap<>();
                exchangeMap.put(exchange.getRoleId(), map);
            }
            map.put(exchange.getStamp(), exchange);

            LinkedList<RoleGuestExchange> exchangeList = listMap.get(exchange.getFamilyId());
            if (exchangeList == null) {
                exchangeList = new LinkedList<>();
                listMap.put(exchange.getFamilyId(), exchangeList);
            }
            exchangeList.addLast(exchange);
        }
    }

    @Override
    public void printState() {
        LogUtil.info("容器大小输出:{},exchangeMap:{},listMap:{}", this.getClass().getSimpleName(), exchangeMap.size(), listMap.size());
    }

    /**
     * 定时调度
     */
    @Override
    public void onSchedule() {
        // 数据保存
        dao.flush();

        // 清除过期的求助信息
        int now = DateUtil.getSecondTime();
        int validTime = GuestManager.HELP_LIMIT_TIME;
        for (LinkedList<RoleGuestExchange> exchangeList : listMap.values()) {
            Iterator<RoleGuestExchange> iter = exchangeList.iterator();
            while (iter.hasNext()) {
                RoleGuestExchange exchange = iter.next();
                if (exchange.getStamp() + validTime < now) {
                    iter.remove();
                    Map<Integer, RoleGuestExchange> map = exchangeMap.get(exchange.getRoleId());
                    if (map != null) {
                        map.remove(exchange.getStamp());
                        if (map.size() == 0) {
                            exchangeMap.remove(exchange.getRoleId());
                        }
                    }
                }
            }
        }
    }

    /**
     * 求助
     */
    @Override
    public void ask(RoleGuestExchange exchange) {
        dao.insert(exchange);
        Map<Integer, RoleGuestExchange> roleExchange = exchangeMap.get(exchange.getRoleId());
        if (roleExchange == null) {
            roleExchange = new HashMap<>();
            exchangeMap.put(exchange.getRoleId(), roleExchange);
        }
        roleExchange.put(exchange.getStamp(), exchange);
        LinkedList<RoleGuestExchange> exchangeList = listMap.get(exchange.getFamilyId());
        if (exchangeList == null) {
            exchangeList = new LinkedList<>();
            listMap.put(exchange.getFamilyId(), exchangeList);
        }
        exchangeList.addFirst(exchange);

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_ASK);
        PlayerUtil.send(exchange.getRoleId(), res);
        fireSpecialAccountEvent(exchange.getRoleId(), exchange.getRoleId(), "求助门客", true);
    }

    private void fireSpecialAccountEvent(long selfId, long roleId, String content, boolean self) {
        if (SpecialAccountManager.isSpecialAccount(roleId)) {
            ServiceHelper.roleService().notice(selfId, new SpecialAccountEvent(roleId, content, self));
        }
    }

    /**
     * 给予
     *
     * @param id
     * @param askId
     * @param askStamp
     * @param itemId
     */
    @Override
    public void give(long id, long askId, int askStamp, int itemId) {
        Map<Integer, RoleGuestExchange> roleExchange = exchangeMap.get(askId);
        if (roleExchange == null) {
            ServiceHelper.roleService().notice(id, new GuestExchangeEvent(false, itemId));
            warn(id, "guest.give.not.found");
            return;
        }
        RoleGuestExchange exchange = roleExchange.get(askStamp);
        if (exchange == null) {
            ServiceHelper.roleService().notice(id, new GuestExchangeEvent(false, itemId));
            warn(id, "guest.give.not.found");
            return;
        }
        if (exchange.getGiveCount() >= exchange.getAskCount()) {
            ServiceHelper.roleService().notice(id, new GuestExchangeEvent(false, itemId));
            warn(id, "guest.give.done");
            return;
        }
        if (exchange.getGiveSet().contains(id)) {
            ServiceHelper.roleService().notice(id, new GuestExchangeEvent(false, itemId));
            warn(id, "guest.give.do.before");
            return;
        }
        if (exchange.getItemId() != itemId) {
            ServiceHelper.roleService().notice(id, new GuestExchangeEvent(false, itemId));
            warn(id, "guest.req.error");
            return;
        }

        exchange.setGiveCount(exchange.getGiveCount() + 1);
        exchange.getGiveSet().add(id);
        dao.update(exchange);
        ServiceHelper.roleService().notice(id, new GuestExchangeEvent(true, itemId));

        Map<Integer, Integer> toolMap = new HashMap<>();
        toolMap.put(itemId, 1);
        RoleSummaryComponent self = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(id, MConst.Role);
        ItemVo itemVo = ToolManager.getItemVo(exchange.getItemId());
        ServiceHelper.emailService().sendToSingle(askId, 10801, 0L, I18n.get("guest.give.email"), toolMap, self.getRoleName());

        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_GIVE);
        PlayerUtil.send(id, res);
        fireSpecialAccountEvent(exchange.getRoleId(), exchange.getRoleId(), "门客给予", true);
        //家族交换 日志
        FamilyLogEvent event = new FamilyLogEvent(FamilyLogEvent.EXCHANGE);
        event.setType((byte) 1);
        event.setItemId(itemId);
        event.setNum(1);
        event.setRoleId(askId);
        ServiceHelper.roleService().notice(id, event);
    }

    /**
     * 交换信息
     *
     * @param roleId
     * @param index
     */
    @Override
    public void info(long roleId, long familyId, int index, int askCount, List<Long> member) {
        if (index < 0) return;
        int now = DateUtil.getSecondTime();
        int count = 100;
        List<RoleGuestExchange> list = new ArrayList<>();
        List<RoleGuestExchange> exchangeList = listMap.get(familyId);
        if (exchangeList != null) {
            for (int i = index; i < exchangeList.size(); i++) {
                if (count == 0) {
                    break;
                }
                RoleGuestExchange exchange = exchangeList.get(i);
                if (exchange.getStamp() + GuestManager.HELP_LIMIT_TIME < now) {
                    // 过期的就不下发了
                    continue;
                }
                list.add(exchange);
                count--;
            }
            Collections.sort(list, new StampComparator(true));
        }
        ClientGuest res = new ClientGuest();
        res.setResType(ClientGuest.RES_EXCHANGE_INFO);
        res.setAskCount(askCount);
        res.setInfoList(list);
        res.setGiveId(roleId);
        PlayerUtil.send(roleId, res);
        fireSpecialAccountEvent(roleId, roleId, "门客交换信息", true);
    }

    /**
     * 移除玩家在家族中的求助
     *
     * @param askId
     * @param familyId
     */
    @Override
    public void removeFromFamily(long askId, Long familyId) {
        Map<Integer, RoleGuestExchange> map = exchangeMap.get(askId);
        if (map == null) {
            return;
        }
        Iterator<Map.Entry<Integer, RoleGuestExchange>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            RoleGuestExchange exchange = iter.next().getValue();
            if (exchange.getFamilyId() == familyId) {
                iter.remove();
                LinkedList<RoleGuestExchange> list = listMap.get(familyId);
                if (list != null) {
                    list.remove(exchange);
                }
                exchange.setFamilyId(0);
                dao.update(exchange);
            }
        }
    }

    /**
     * 给客户端提示
     *
     * @param roleId
     * @param partten
     * @param params
     */
    private void warn(long roleId, String partten, String... params) {
        String message = I18n.get(partten, (Object[]) params);
        PlayerUtil.send(roleId, new ClientText(message));
    }

    @Override
    public void updateRoleName(long roleId, long familyId, String newName) {
        LinkedList<RoleGuestExchange> roleGuestExchanges = listMap.get(familyId);
        if (roleGuestExchanges != null) {
            for (RoleGuestExchange roleGuestExchange : roleGuestExchanges) {
                if (roleGuestExchange.getRoleId() == roleId) {
                    roleGuestExchange.setName(newName);
                    dao.update(roleGuestExchange);
                }
            }
        }
    }
}
