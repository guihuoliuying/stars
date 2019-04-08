package com.stars.modules.name;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.MConst;
import com.stars.modules.data.DataManager;
import com.stars.modules.family.FamilyModule;
import com.stars.modules.friend.FriendModule;
import com.stars.modules.name.event.RoleRenameEvent;
import com.stars.modules.name.packet.ClientRenamePacket;
import com.stars.modules.role.RoleModule;
import com.stars.modules.role.userdata.Role;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolModule;
import com.stars.services.ServiceHelper;
import com.stars.services.family.FamilyAuth;
import com.stars.services.family.FamilyPost;
import com.stars.services.family.main.FamilyData;
import com.stars.util.DirtyWords;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class NameModule extends AbstractModule {
    public static final String ROLE_RENAME_TIMES = "role.rename.times";
    public static final String ROLE_RENAME_LAST_RENAME_TIME = "role.rename.lastRenameTime";
    public static final int TYPE_ROLE = 1;
    public static final int TYPE_FAMILY = 2;

    public NameModule(long id, Player self, EventDispatcher eventDispatcher,
                      Map<String, Module> moduleMap) {
        super("取名", id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onSyncData() throws Throwable {
        FamilyModule familyModule = module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        if (auth.getPost().canRename()) {
            long familyId = auth.getFamilyId();
            FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
            String oldFamilyName = familyData.getFamilyPo().getName();
            if (oldFamilyName.contains("#")) {
                ClientRenamePacket clientRenamePacket = new ClientRenamePacket(ClientRenamePacket.SEND_FAMILY_RENAME_NOTIFY);
                send(clientRenamePacket);
            }
        }
    }

    public static String randomName() {
        int maxIndex = NameManager.firstName.size();
        Random r = new Random();
        int index = r.nextInt(maxIndex);
        String firtName = NameManager.firstName.get(index);
        maxIndex = NameManager.secondName.size();
        index = r.nextInt(maxIndex);
        String name = firtName + NameManager.secondName.get(index);
        index = r.nextInt(100);
        if (index >= 20) {
            maxIndex = NameManager.thirdName.size();
            index = r.nextInt(maxIndex);
            name = name + NameManager.thirdName.get(index);
        }
        return name;
    }


    /**
     * 角色改名
     *
     * @param name
     */
    public void roleRename(String name) {
        RoleModule roleModule = module(MConst.Role);
        Role roleRow = roleModule.getRoleRow();
        String newName = name.trim();
        String oldName = roleRow.getName();
        if (checkCondition() && isNameLegal(oldName, newName, TYPE_ROLE)) {
            ToolModule toolModule = module(MConst.Tool);
            boolean success = toolModule.deleteAndSend(NameManager.costItemMap, EventType.RENAME.getCode());
            if (success) {
                setInt(ROLE_RENAME_TIMES, getInt(ROLE_RENAME_TIMES, 0) + 1);
                setLong(ROLE_RENAME_LAST_RENAME_TIME, now());
                eventDispatcher().fire(new RoleRenameEvent(newName));
                sendNotifyEmail(oldName, newName);
                send(new ClientRenamePacket(ClientRenamePacket.SEND_ROLE_RENAME_SUCCESS));
                sendRenameTimes();
                warn("changename_tips_win");
                com.stars.util.LogUtil.info("roleReName:roleid={},newName={}", id(), newName);
            } else {
                warn("changename_tips_reqitem");
            }
        }

    }

    /**
     * 家族改名
     *
     * @param familyName
     */
    public void familyRename(String familyName) {

        FamilyModule familyModule = module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        long familyId = auth.getFamilyId();
        String newFamilyName = familyName.trim();
        FamilyData familyData = ServiceHelper.familyMainService().getFamilyDataClone(familyId);
        String oldFamilyName = familyData.getFamilyPo().getName();
        if (!oldFamilyName.contains("#")) {
            warn("您已无权修改本家族名");
            return;
        }
        if (isNameLegal(oldFamilyName, newFamilyName, TYPE_FAMILY)) {
            if (auth != null && auth.getFamilyId() != 0) {
                FamilyPost post = auth.getPost();
                if (post.canRename()) {
                    ServiceHelper.familyMainService().updateFalimyName(auth.getFamilyId(), familyName);
                    send(new ClientRenamePacket(ClientRenamePacket.SEND_FAMILY_RENAME_SUCCESS));
                    warn("changename_tips_familywin");
                    com.stars.util.LogUtil.info("family rename:oldname={},newname={},roleid={}", oldFamilyName, newFamilyName, id());
                } else {
                    warn("您已无权修改本家族名");
                }
            }
        }

    }

    /**
     * 发送通知邮件
     *
     * @param oldName
     * @param newName
     */
    private void sendNotifyEmail(String oldName, String newName) {
        Set<Long> roleSet = Sets.newHashSet();
        /**
         * 给朋友
         */
        FriendModule friendModule = module(MConst.Friend);
        Set<Long> friendList = friendModule.getFriendList();
        roleSet.addAll(friendList);
        /**
         * 给伴侣
         * tips:伴侣会在好友列表内
         */

        /**
         * 给家族成员
         */
        List<Long> memberIdList = Lists.newArrayList();
        FamilyModule familyModule = module(MConst.Family);
        FamilyAuth auth = familyModule.getAuth();
        if (auth != null && auth.getFamilyId() != 0) {
            memberIdList = ServiceHelper.familyMainService().getMemberIdList(auth.getFamilyId(), id());
        }
        roleSet.addAll(memberIdList);
        roleSet.remove(id());
        ServiceHelper.emailService().sendTo(Lists.newArrayList(roleSet), (byte) 0, 0L, "系统", DataManager.getGametext("changename_desc_maintitle"), String.format(DataManager.getGametext("changename_desc_maindesc"), oldName, newName), null);
    }

    /**
     * 检查角色自身条件
     */
    private boolean checkCondition() {
        int renameTimes = getInt(ROLE_RENAME_TIMES, 0);
        if (NameManager.maxRenameTime != -1 && renameTimes >= NameManager.maxRenameTime) {
            warn("changename_tips_useup");
            return false;
        }
        long lastRenameTime = getLong(ROLE_RENAME_LAST_RENAME_TIME, 0);
        long between = lastRenameTime + NameManager.renameCd * 1000 - now();
        if (between > 0) {
            warn("changename_tips_cd", (between / 1000 + 1) + "秒");
            return false;
        }
        return true;
    }


    /**
     * 判断家族名字是否合法;
     */
    private boolean isNameLegal(String oldName, String newName, Integer type) {
        if (StringUtil.isNotEmpty(newName)) {
            /**
             * 是否和改名前一致
             */
            if (oldName.equals(newName)) {
                warn("changename_tips_repeatname");
                return false;
            }
            //判断是否符合限定的长度;
            String[] tmpStrArr = DataManager.getCommConfig("randomname_length").split("\\+");
            int minBytesCount = Integer.parseInt(tmpStrArr[0]);
            int maxBytesCount = Integer.parseInt(tmpStrArr[1]);
            int curRoleNameBytesCount = newName.length();
            if (curRoleNameBytesCount > maxBytesCount) {
                warn("randomename_toolong");
                return false;
            }
            if (curRoleNameBytesCount < minBytesCount) {
                warn("randomename_tooshort");
                return false;
            }
            if (DirtyWords.checkName(newName)) {
                /**
                 *有敏感词
                 */
                warn("randomename_unablecharacter");
                return false;
            }
            if (!StringUtil.isValidString(newName)) {
                warn("randomename_unablecharacter");
                return false;
            }
            //判断是否已经有同名的了;
            try {
                boolean exist = false;
                switch (type) {
                    case TYPE_FAMILY: {
                        exist = DBUtil.queryCount(DBUtil.DB_USER, "select familyid from `family` where `name`=\"" + newName + "\"") > 0;

                    }
                    break;
                    case TYPE_ROLE: {
                        exist = DBUtil.queryCount(DBUtil.DB_USER, "select roleid from `role` where `name`=\"" + newName + "\"") > 0;
                    }
                    break;
                }
                if (exist) {
                    warn("changename_tips_existedname");
                    return false;
                }

            } catch (SQLException e) {
                LogUtil.error(e.getMessage(), e);
            }
            return true;
        }
        warn("changename_tips_isblank");
        return false;
    }

    /**
     * 下发角色改名剩余次数
     */
    public void sendRenameTimes() {
        ClientRenamePacket clientRenamePacket = new ClientRenamePacket(ClientRenamePacket.SEND_RENAME_SURPLUS_TIMES);
        clientRenamePacket.setSurplusTimes(NameManager.maxRenameTime != -1 ? NameManager.maxRenameTime - getInt(ROLE_RENAME_TIMES, 0) : -1);
        clientRenamePacket.setCostItemMap(NameManager.costItemMap);
        send(clientRenamePacket);
    }

    public void sendRandomName() {
        ClientRenamePacket randomNamePacket = new ClientRenamePacket(ClientRenamePacket.SEND_RANDOM_NAME);
        randomNamePacket.setRandomName(randomName());
        send(randomNamePacket);
    }
}
