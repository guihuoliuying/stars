package com.stars.services.newredbag;

import com.stars.core.persist.DbRowDao;
import com.stars.core.player.PlayerUtil;
import com.stars.ExcutorKey;
import com.stars.core.schedule.SchedulerManager;
import com.stars.core.db.DBUtil;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.family.event.FamilyLogEvent;
import com.stars.modules.familyactivities.bonfire.BonfireActivityFlow;
import com.stars.modules.newredbag.NewRedbagManager;
import com.stars.modules.newredbag.packet.ClientNewRedbag;
import com.stars.modules.newredbag.prodata.FamilyRedbagVo;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.network.server.packet.Packet;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.services.family.event.FamilyEvent;
import com.stars.services.newredbag.userdata.RoleFamilyRedbag;
import com.stars.services.newredbag.userdata.RoleFamilyRedbagGet;
import com.stars.services.newredbag.userdata.RoleFamilyRedbagSend;
import com.stars.util.*;
import com.stars.core.actor.invocation.ServiceActor;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhouyaohui on 2017/2/13.
 */
public class NewRedbagServiceActor extends ServiceActor implements NewRedbagService {

    private DbRowDao dao = new DbRowDao();
    //key= familyid,roleid,redbagId
    private Map<Long, Map<Long, Map<Integer, RoleFamilyRedbag>>> redbagMap = new HashMap<>();
    //key= familyid,(redbagKey-红包的唯一key)
    private Map<Long, Map<String, RoleFamilyRedbagSend>> sendMap = new HashMap<>();
    //key= familyid,
    private Map<Long, List<RoleFamilyRedbagSend>> sendList = new HashMap<>();
    //key= familyid,
    private Map<Long, List<RoleFamilyRedbag>> autoSend = new HashMap<>();
    //value= familyid,
    private List<Long> autoSendList = new ArrayList<>();
    private boolean bonfireActivity = false;
    //	key= familyid,value=roleId
    private Map<Long, Set<Long>> online = new HashMap<>();

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.NewRedbagService, this);
        SchedulerManager.scheduleAtFixedRateIndependent(ExcutorKey.NEWREDBAG, new NewRedbagSchedule(), 5, 5, TimeUnit.SECONDS);

        // 起服删除那些退出家族的记录
        StringBuilder builder = new StringBuilder();
        builder.append("delete from rolefamilyredbag where `count` = 0");
        DBUtil.execSql(DBUtil.DB_USER, builder.toString());

        // 加载还没有发送的红包
        builder.delete(0, builder.length());
        builder.append("select * from rolefamilyredbag");
        List<RoleFamilyRedbag> list = DBUtil.queryList(DBUtil.DB_USER, RoleFamilyRedbag.class, builder.toString());
        for (RoleFamilyRedbag redbag : list) {
            Map<Integer, RoleFamilyRedbag> map = getRoleMap(redbag.getFamilyId(), redbag.getRoleId());
            map.put(redbag.getRedbagId(), redbag);
        }
    }

    @Override
    public void printState() {
    	int redBagCount = 0;
		for (Map<Long, Map<Integer, RoleFamilyRedbag>> tmpMap : redbagMap.values()) {
			for (Map<Integer, RoleFamilyRedbag> tmpMap1 : tmpMap.values()) {
				redBagCount += tmpMap1.size();
			}
		}
		int sendCount = 0;
		for(Map<String, RoleFamilyRedbagSend> tmpMap :sendMap.values()){
			sendCount+=tmpMap.size();
		}
    	
		LogUtil.info(
				"容器大小输出:{},redBagCount:{},sendCount:{},redbagMap:{},sendMap:{},sendList:{},autoSend:{},autoSendList:{},online:{}",
				this.getClass().getSimpleName(), redBagCount, sendCount, redbagMap.size(), sendMap.size(), sendList.size(),
				autoSend.size(), autoSendList.size(), online.size());
    	
    }

    /**
     * 上线
     * @param familyId
     * @param roleId
     */
    @Override
    public void online(long familyId, long roleId, boolean needSend) {
        Set<Long> roleSet = online.get(familyId);
        if (roleSet == null) {
            roleSet = new HashSet<>();
            online.put(familyId, roleSet);
        }
        roleSet.add(roleId);

        if (needSend) {
            List<RoleFamilyRedbagSend> familySendList = getFamilySendList(familyId);
            List<RoleFamilyRedbagSend> list = new ArrayList<>();
            int now = DateUtil.getSecondTime();
            int count = 50;
            for (RoleFamilyRedbagSend send : familySendList) {
                if (now - send.getStamp() > NewRedbagManager.VALID_TIME) {
                    break;
                }
                if (send.getRecord().containsKey(roleId)) {
                    continue;
                }
                if (send.getCurIndex() >= send.getCount()) {
                    continue;
                }
                list.add(send);
                count--;
                if (count <= 0) {
                    break;
                }
            }
            ClientNewRedbag res = new ClientNewRedbag();
            res.setResType(ClientNewRedbag.SEND);
            res.setRecordList(list);
            sendPacket(roleId, res);
        }
    }

    /**
     * 下线或者退出家族
     * @param familyId
     * @param roleId
     */
    @Override
    public void offlineOrExitFamily(long familyId, long roleId) {
        Set<Long> roleSet = online.get(familyId);
        if (roleSet != null) {
            roleSet.remove(roleId);
        }
    }

    /**
     * 定时线程
     */
    @Override
	public void schedule() {
		try {
			dao.flush();
		} catch (Throwable e) {
			LogUtil.error(e.getMessage());
		}

		// 策划要求篝火活动开启时自动发送，结束时停止
		boolean isOpen = BonfireActivityFlow.getState() == BonfireActivityFlow.OPEN;
		if (bonfireActivity != isOpen && isOpen) {
			initAutoSendMap();
			bonfireActivity = isOpen;
		}
		if (bonfireActivity != isOpen && !isOpen) {
			autoSend.clear();
			autoSendList.clear();
			bonfireActivity = isOpen;
		}

		// 每次发一轮，每个家族发一个红包
		Iterator<Long> iter = autoSendList.iterator();
		while (iter.hasNext()) {
			try {
				long familyId = iter.next().longValue();
				List<RoleFamilyRedbag> list = autoSend.get(familyId);
				Iterator<RoleFamilyRedbag> it = list.iterator();
				while (it.hasNext()) {
					try {
						RoleFamilyRedbag redbag = it.next();
						if (redbag.getCount() <= 0) {
							it.remove();
						} else {
							FamilyRedbagVo redbagVo = NewRedbagManager.getFamilyRedbagVo(redbag.getRedbagId());
							SendInfo info = new SendInfo();
							info.setFamilyId(redbag.getFamilyId());
							info.setRoleId(redbag.getRoleId());
							info.setRedbagId(redbag.getRedbagId());
							info.setSelf(false);
							info.setCount(redbagVo.getMinCount());
							info.setPadding(0);
							info.setValue(redbagVo.getItemValue());
							info.setItemId(redbagVo.getItemId());
							info.setRoleName(redbag.getName());
							info.setJobId(redbag.getJobId());
							send(info);
							break;
						}
					} catch (Throwable e) {
						LogUtil.error(e.getMessage());
					}
				}
				if (list.size() == 0) {
					iter.remove();
				}
			} catch (Throwable e) {
				LogUtil.error(e.getMessage());
			}
		}
	}

    /**
     * 初始化自动发送
     */
    private void initAutoSendMap() {
        autoSend.clear();
        autoSendList.clear();
        for (long familyId : redbagMap.keySet()) {
            Map<Long, Map<Integer, RoleFamilyRedbag>> familyMap = getFamilyMap(familyId);
            List<RoleFamilyRedbag> list = autoSend.get(familyId);
            autoSendList.add(familyId);
            if (list == null) {
                list = new ArrayList<>();
                autoSend.put(familyId, list);
            }
            for (long roleId : familyMap.keySet()) {
                Map<Integer, RoleFamilyRedbag> roleMap = getRoleMap(familyId, roleId);
                list.addAll(roleMap.values());
            }
        }
    }

    /**
     * 新增红包
     *
     * @param familyId
     * @param id
     * @param redbagId
     */
    @Override
    public void add(long familyId, long id, int redbagId, int count, String roleName, int jobId) {
        Map<Integer, RoleFamilyRedbag> roleMap = getRoleMap(familyId, id);
        RoleFamilyRedbag row = roleMap.get(redbagId);
        if (row == null) {
            row = new RoleFamilyRedbag();
            row.setFamilyId(familyId);
            row.setRoleId(id);
            row.setRedbagId(redbagId);
            row.setCount(count);
            row.setName(roleName);
            row.setJobId(jobId);
            dao.insert(row);
            roleMap.put(redbagId, row);
        } else {
            row.setCount(row.getCount() + count);
            dao.update(row);
        }
    }

    /**
     * 家族map
     *
     * @param familyId
     * @return
     */
    private Map<Long, Map<Integer, RoleFamilyRedbag>> getFamilyMap(long familyId) {
        Map<Long, Map<Integer, RoleFamilyRedbag>> familyMap = redbagMap.get(familyId);
        if (familyMap == null) {
            familyMap = new HashMap<>();
            redbagMap.put(familyId, familyMap);
        }
        return familyMap;
    }

    /**
     * 角色map
     *
     * @param familyId
     * @param roleId
     * @return
     */
    private Map<Integer, RoleFamilyRedbag> getRoleMap(long familyId, long roleId) {
        Map<Long, Map<Integer, RoleFamilyRedbag>> familyMap = getFamilyMap(familyId);
        Map<Integer, RoleFamilyRedbag> roleMap = familyMap.get(roleId);
        if (roleMap == null) {
            roleMap = new HashMap<>();
            familyMap.put(roleId, roleMap);
        }
        return roleMap;
    }

    /**
     * 红包row
     *
     * @param familyId
     * @param roleId
     * @param redbagId
     * @return
     */
    private RoleFamilyRedbag getRoleFamilyRedbag(long familyId, long roleId, int redbagId) {
        Map<Integer, RoleFamilyRedbag> roleMap = getRoleMap(familyId, roleId);
        return roleMap.get(redbagId);
    }

    /**
     * 家族发送红包map
     *
     * @param familyId
     * @return
     */
    private Map<String, RoleFamilyRedbagSend> getFamilySendMap(long familyId) {
        Map<String, RoleFamilyRedbagSend> familySendMap = sendMap.get(familyId);
        if (familySendMap == null) {
            familySendMap = new HashMap<>();
            sendMap.put(familyId, familySendMap);
        }
        return familySendMap;
    }

    /**
     * 抢红包
     *
     * @param familyId
     * @param uniqueKey
     */
    @Override
    public void get(long roleId, String name, int jobId, long familyId, String uniqueKey) {
        Map<String, RoleFamilyRedbagSend> map = getFamilySendMap(familyId);
        if (!map.containsKey(uniqueKey)) {
            warn(roleId, "family_tips_nogetred");
            return;
        }
        RoleFamilyRedbagSend send = map.get(uniqueKey);
        if (send.getRecord().containsKey(roleId)) {
            warn(roleId, "newredbag.already.get");
            return;
        }
        int value = send.get();
        if (value == 0) {
            warn(roleId, "family_tips_nogetred");
            return;
        }
        int now = DateUtil.getSecondTime();
        if (now - send.getStamp() > NewRedbagManager.VALID_TIME) {
            warn(roleId, "familyred_tips_redover");
            return;
        }
        send.setCurIndex(send.getCurIndex() + 1);

        // 给玩家加道具
        AddToolByEvent event = new AddToolByEvent(send.getItemId(), value, EventType.FAMILY_NEW_REDBAG);
        ServiceHelper.roleService().notice(roleId, event);

        // 抢红包记录
        RoleFamilyRedbagGet get = new RoleFamilyRedbagGet();
        get.setRoleId(roleId);
        get.setName(name);
        get.setValue(value);
        get.setJobId(jobId);
        send.record(get);

        Map<Integer, Integer> toolMap = new HashMap<>();
        toolMap.put(send.getItemId(), value);
        ClientAward award = new ClientAward(toolMap);
        sendPacket(roleId, award);

        ServerLogConst.console.info(MessageFormat.format("redbag get|{0}|roleid:{1}|senderid:{2}|uniquekey:{3}|redbagid:{4}|value:{5}",
                DateUtil.getYMDHMS_Str(), roleId, send.getSenderId(), send.getUniqueKey(), send.getRedbagId(), value));
        FamilyLogEvent logEvent = new FamilyLogEvent(FamilyLogEvent.RED_SEND);
        logEvent.setType((byte)0);
        int itemId = send.getItemId();
        byte itemType = 0;
        if(itemId==ToolManager.MONEY){
        	itemType = 1;
        }else if(itemId==ToolManager.GOLD||itemId==ToolManager.BANDGOLD){
        	itemType = 2;
        }
        logEvent.setItemType(itemType);
        logEvent.setMoney(value);
        logEvent.setRoleId(send.getSenderId());
        ServiceHelper.roleService().notice(roleId, logEvent);
    }

    /**
     * 发红包
     *
     * @param info
     */
    @Override
    public void send(SendInfo info) {
        RoleFamilyRedbag redbag = getRoleFamilyRedbag(info.getFamilyId(), info.getRoleId(), info.getRedbagId());
        if (!info.isSelf()) {
            if (redbag == null || redbag.getCount() <= 0) {
                warn(info.getRoleId(), "newredbag.send.failed");
                if (info.getPadding() > 0) {
                    // 发红包失败，把扣除的元宝返回给玩家
                    AddToolByEvent event = new AddToolByEvent(1, info.getPadding(), EventType.FAMILY_NEW_REDBAG);
                    ServiceHelper.roleService().notice(info.getRoleId(), event);
                }
                return;
            }
        }

        int now = DateUtil.getSecondTime();
        String rand = randomRedbag(info.getValue(), info.getCount());
        RoleFamilyRedbagSend roleSend = new RoleFamilyRedbagSend();
        roleSend.setFamilyId(info.getFamilyId());
        roleSend.setSenderId(info.getRoleId());
        roleSend.setRoleName(info.getRoleName());
        roleSend.setStamp(now);
        roleSend.setRedbagId(info.getRedbagId());
        roleSend.setItemId(info.getItemId());
        roleSend.setCount(info.getCount());
        roleSend.setValue(info.getValue());
        roleSend.setRandStr(rand);
        roleSend.setJobId(info.getJobId());
        StringBuilder unique = new StringBuilder();
        unique.append(info.getFamilyId())
                .append(roleSend.getStamp())
                .append("+").append(roleSend.hashCode()); // uniquekey=familyId+时间戳+hashcode
        roleSend.setUniqueKey(unique.toString());

        Map<String, RoleFamilyRedbagSend> familySendMap = getFamilySendMap(info.getFamilyId());
        List<RoleFamilyRedbagSend> familySendList = getFamilySendList(info.getFamilyId());
        if (familySendMap.containsKey(roleSend)) {  //发送失败
            warn(info.getRoleId(), "newredbag.send.failed");
            if (info.getPadding() > 0) {
                AddToolByEvent event = new AddToolByEvent(1, info.getPadding(), EventType.FAMILY_NEW_REDBAG);
                ServiceHelper.roleService().notice(info.getRoleId(), event);
            }
            return;
        }

        if (!info.isSelf()) {
            redbag.setCount(redbag.getCount() - 1);
            dao.update(redbag);
        }
        familySendMap.put(roleSend.getUniqueKey(), roleSend);
        familySendList.add(0, roleSend);

        List<RoleFamilyRedbagSend> record = new ArrayList<>();
        record.add(roleSend);
        ClientNewRedbag res = new ClientNewRedbag();
        res.setResType(ClientNewRedbag.SEND);
        res.setRecordList(record);
        bordcast(info.getFamilyId(), res);

        ClientText text = new ClientText("family_desc_redroll", info.getRoleName());
        bordcast(info.getFamilyId(), text);

        // 家族事迹
        ServiceHelper.familyEventService().logEvent(
                info.getFamilyId(), FamilyEvent.W_REDPACKET, info.getRoleName());

        ServerLogConst.console.info(MessageFormat.format("redbag send|{0}|roleid:{1}|familyid:{2}|redbagid:{3}|count:{4}|value:{5}|uniqueKey:{6}",
                DateUtil.getYMDHMS_Str(), info.getRoleId(), info.getFamilyId(), info.getRedbagId(), info.getCount(), info.getValue(), roleSend.getUniqueKey()));
        //红包日志
        FamilyLogEvent logEvent = new FamilyLogEvent(FamilyLogEvent.RED_SEND);
        logEvent.setType((byte)1);
        int itemId = info.getItemId();
        byte itemType = 1;
        if(itemId==ToolManager.MONEY){
        	itemType = 1;
        }else if(itemId==ToolManager.GOLD||itemId==ToolManager.BANDGOLD){
        	itemType = 2;
        }
        logEvent.setItemType(itemType);
        logEvent.setMoney(info.getValue());
        ServiceHelper.roleService().notice(info.getRoleId(), logEvent);
    }

    /**
     * 发送队列
     * @param familyId
     * @return
     */
    private List<RoleFamilyRedbagSend> getFamilySendList(long familyId) {
        List<RoleFamilyRedbagSend> list = sendList.get(familyId);
        if (list == null) {
            list = new ArrayList<>();
            sendList.put(familyId, list);
        }
        return list;
    }

    /**
     * 随机分配红包
     * @param value
     * @param count
     * @return
     */
    private String randomRedbag(int value, int count) {
        int[] array = new int[count];
        double total = 0;
        for (int i = 0; i < count; i++) {
            array[i] = RandomUtil.rand(200, 1000);
            total += array[i];
        }
        int[] values = new int[count];
        int sum = 0;
        for (int i = 0; i < count; i++) {
            int v = (int) Math.floor(value * (array[i] / total));
            values[i] = v;
            sum += v;
        }
        int sub = value - sum;
        if (sub > 0) {
            values[values.length - 1] += sub;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(values[i]).append("+");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    /**
     * 红包主界面
     * @param familyId
     * @param id
     * @param remainCount
     */
    @Override
    public void viewMain(long familyId, long id, int remainCount) {
        ClientNewRedbag res = new ClientNewRedbag();
        Map<Integer, RoleFamilyRedbag> myMap = getRoleMap(familyId, id);
        List<RoleFamilyRedbag> otherList = new ArrayList<>();
        Map<Long, Map<Integer, RoleFamilyRedbag>> familyMap = getFamilyMap(familyId);
        int size = 100;
        for (Map<Integer, RoleFamilyRedbag> map : familyMap.values()) {
            for (RoleFamilyRedbag redbag : map.values()) {
                if (redbag.getRoleId() == id) {
                    break;
                }
                otherList.add(redbag);
                size--;
                if (size <= 0) {
                    break;
                }
            }
            if (size <= 0) {
                break;
            }
        }
        res.setResType(ClientNewRedbag.VIEW_MAIN);
        res.setMyRedbag(myMap);
        res.setOtherRedbag(otherList);
        res.setRemainSelfCount(remainCount);
        sendPacket(id, res);
    }

    /**
     * 红包记录
     * @param familyId
     * @param index
     */
    @Override
    public void record(long roleId, long familyId, int index) {
        List<RoleFamilyRedbagSend> familySendList = getFamilySendList(familyId);
        Map<String, RoleFamilyRedbagSend> familySendMap = getFamilySendMap(familyId);
        List<RoleFamilyRedbagSend> list = new ArrayList<>();
        int count = 50;
        int now = DateUtil.getSecondTime();

        // 清除家族过期红包记录
        Iterator<RoleFamilyRedbagSend> iter = familySendList.iterator();
        while (iter.hasNext()) {
            RoleFamilyRedbagSend send = iter.next();
            if (now - send.getStamp() > NewRedbagManager.RECORD_CLEAR_TIME) {
                iter.remove();
                familySendMap.remove(send.getUniqueKey());
            }
        }

        for (int i = index; i < familySendList.size(); i++) {
            RoleFamilyRedbagSend send = familySendList.get(i);
            if (send == null) {
                break;
            }
            list.add(send);
            count--;
            if (count <= 0) {
                break;
            }
        }
        ClientNewRedbag res = new ClientNewRedbag();
        res.setResType(ClientNewRedbag.RECORD);
        res.setRecordList(list);
        sendPacket(roleId, res);
    }

    /**
     * 红包记录详细信息
     * @param id
     * @param familyId
     * @param redbagKey
     */
    @Override
    public void recordDetail(long id, long familyId, String redbagKey) {
        Map<String, RoleFamilyRedbagSend> sendMap = getFamilySendMap(familyId);
        if (!sendMap.containsKey(redbagKey)) {
            warn(id, "newredbag.over.deadline");
            return;
        }
        RoleFamilyRedbagSend send = sendMap.get(redbagKey);
        List<RoleFamilyRedbagGet> getList = new ArrayList<>();
        getList.addAll(send.getRecord().values());
        ClientNewRedbag res = new ClientNewRedbag();
        res.setResType(ClientNewRedbag.RECORD_DETAIL);
        res.setDetailList(getList);
        res.setRedbagKey(send.getUniqueKey());
        sendPacket(id, res);
    }

    /**
     * 加入或者退出家族
     *
     * @param familyId
     * @param prevFamilyId
     */
    @Override
    public void updateFamilyAuth(long roleId, long familyId, long prevFamilyId) {
        if (prevFamilyId != 0) {
            offlineOrExitFamily(prevFamilyId, roleId);
        }
        if (familyId != 0) {
            online(familyId, roleId, true);
        }
    }

    private void warn(long roleId, String message, String... params) {
        String s = I18n.get(message, (Object[]) params);
        if ("国际化请求失败".equals(s)) {
            ClientText text = new ClientText(message, params);
            sendPacket(roleId, text);
        } else {
            ClientText text = new ClientText(s);
            sendPacket(roleId, text);
        }
    }

    private void sendPacket(long roleId, Packet packet) {
        Objects.requireNonNull(packet);
        PlayerUtil.send(roleId, packet);
    }

    private void bordcast(long familyId, Packet packet) {
        Set<Long> roleSet = online.get(familyId);
        if (roleSet == null) {
            return;
        }
        for (long roleId : roleSet) {
            sendPacket(roleId, packet);
        }
    }
}
