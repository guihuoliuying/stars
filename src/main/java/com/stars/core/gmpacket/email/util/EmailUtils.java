package com.stars.core.gmpacket.email.util;

import com.stars.core.gmpacket.email.condition.ChannelMatcher;
import com.stars.core.gmpacket.email.condition.RoleMatcher;
import com.stars.core.gmpacket.email.condition.RoleMatcherFactory;
import com.stars.core.gmpacket.email.condition.RoleMatcherManager;
import com.stars.core.gmpacket.email.vo.AllEmailGmPo;
import com.stars.modules.email.pojodata.EmailConditionArgs;
import com.stars.services.mail.RoleEmailData;
import com.stars.services.mail.prodata.EmailTemplateVo;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.services.mail.userdata.RoleEmailPo;
import com.stars.util.LogUtil;

import java.util.*;

/**
 * Created by huwenjun on 2017/3/28.
 */
public class EmailUtils {
    /**
     * 找出提供的角色中满足此全服邮件的条件的角色
     *
     * @param allEmailGmPo
     * @param roleIds
     */
    public static Map<Integer, Set<Long>> checkRoleState(AllEmailPo allEmailGmPo, Collection<Long> roleIds) {
        Map<Integer, Set<Long>> roleResultMap = new HashMap<>();
        List<Map<String, String>> conditions = allEmailGmPo.getCondition();
        List<RoleMatcher> matchers = buildRoleMatchers(conditions);
        if (allEmailGmPo.getChannelIdSet().size() != 0) {
            RoleMatcher channelRoleMatcher = buildChannelRoleMatchers(allEmailGmPo.getChannelIdSet());
            matchers.add(channelRoleMatcher);
        }
        Set<Long> passRoleSet = new HashSet<>();
        Set<Long> noPassRoleSet = new HashSet<>();
        for (Long roleId : roleIds) {
            boolean result = true;
            EmailConditionArgs emailConditionArgs = RoleMatcherManager.get(roleId);
            LogUtil.info("roleid:{} to check condition allmailid={},emailConditionArgs={} ", emailConditionArgs.getRoleId(),allEmailGmPo.getAllEmailId(),emailConditionArgs);
            for (RoleMatcher roleMatcher : matchers) {
                result &= roleMatcher.match(emailConditionArgs);
            }
            /**
             *  有条件不满足，此角色无资格接受全服邮件
             */
            if (result) {
                passRoleSet.add(roleId);
            } else {
                noPassRoleSet.add(roleId);
            }
        }
        roleResultMap.put(RoleMatcherFactory.NO_PASS, noPassRoleSet);
        roleResultMap.put(RoleMatcherFactory.PASS, passRoleSet);
        return roleResultMap;

    }


    /**
     * 找出提供的角色中满足此全服邮件的条件的角色
     *
     * @param allEmailGmPo
     * @param emailConditionArgs
     */
    public static Map<Integer, Set<Long>> checkRoleState(AllEmailPo allEmailGmPo, EmailConditionArgs emailConditionArgs) {
        LogUtil.info("roleid:{} to check condition allmailid={},emailConditionArgs={} ", emailConditionArgs.getRoleId(),allEmailGmPo.getAllEmailId(),emailConditionArgs);
        Map<Integer, Set<Long>> roleResultMap = new HashMap<>();
        List<Map<String, String>> conditions = allEmailGmPo.getCondition();
        List<RoleMatcher> matchers = buildRoleMatchers(conditions);
        if (allEmailGmPo.getChannelIdSet().size() != 0) {
            RoleMatcher channelRoleMatcher = buildChannelRoleMatchers(allEmailGmPo.getChannelIdSet());
            matchers.add(channelRoleMatcher);
        }
        Set<Long> passRoleSet = new HashSet<>();
        Set<Long> noPassRoleSet = new HashSet<>();
        boolean result = true;
        for (RoleMatcher roleMatcher : matchers) {
            result &= roleMatcher.match(emailConditionArgs);
        }
        /**
         *  有条件不满足，此角色无资格接受全服邮件
         */
        if (result) {
            LogUtil.info("roleid:{} to check condition allmailid={} pass", emailConditionArgs.getRoleId(),allEmailGmPo.getAllEmailId());
            passRoleSet.add(emailConditionArgs.getRoleId());
        } else {
            LogUtil.info("roleid:{} to check condition allmailid={} not pass", emailConditionArgs.getRoleId(),allEmailGmPo.getAllEmailId());
            noPassRoleSet.add(emailConditionArgs.getRoleId());
        }
        roleResultMap.put(RoleMatcherFactory.NO_PASS, noPassRoleSet);
        roleResultMap.put(RoleMatcherFactory.PASS, passRoleSet);
        return roleResultMap;

    }


    private static List<RoleMatcher> buildRoleMatchers(List<Map<String, String>> conditions) {
        List<RoleMatcher> matchers = new ArrayList<>();
        for (Map<String, String> condition : conditions) {
            String typeStr = condition.get("type");
            String maxValueStr = condition.get("maxValue");
            String minValueStr = condition.get("minValue");
            int type = Integer.parseInt(typeStr);
            Long maxValue = Long.parseLong(maxValueStr);
            Long minValue = Long.parseLong(minValueStr);
            RoleMatcher roleMatcher = RoleMatcherFactory.getInstance(type, maxValue, minValue);
            matchers.add(roleMatcher);
        }
        return matchers;
    }

    private static RoleMatcher buildChannelRoleMatchers(Set<Integer> channelIds) {
        int type = 9;
        Long maxValue = 0L;
        Long minValue = 0L;
        RoleMatcher roleMatcher = RoleMatcherFactory.getInstance(type, maxValue, minValue);
        ChannelMatcher channelMatcher = (ChannelMatcher) roleMatcher;
        channelMatcher.setChannelIds(channelIds);
        return roleMatcher;
    }

    /**
     * 将gm下的物品格式转换成道具系统的物品格式
     *
     * @param itemDict
     * @return
     */
    public static Map<Integer, Integer> toToolMap(List<Map<String, String>> itemDict) {
        Map<Integer, Integer> toolMap = new HashMap<>();
        for (Map<String, String> item : itemDict) {
            int itemId = Integer.parseInt((String) item.get("code"));
            int itemCount = Integer.parseInt((String) item.get("amount"));
            Integer oldValue = toolMap.get(itemId);
            if (oldValue == null) {
                toolMap.put(itemId, itemCount);
            } else {
                toolMap.put(itemId, oldValue + itemCount);
            }
        }
        return toolMap;
    }

    /**
     * 将AllEmailGmPo转换成AllEmailPo copy决定发送时间上是拷贝还是新建
     *
     * @param allEmailGmPo
     * @param copy
     * @return
     */
    public static AllEmailPo newAllEmail(AllEmailGmPo allEmailGmPo, boolean copy) {
        AllEmailPo allEmailPo = new AllEmailPo();
        allEmailPo.setAllEmailId(allEmailGmPo.getAllEmailGmId());
        allEmailPo.setSenderType(allEmailGmPo.getSenderType());
        allEmailPo.setSenderId(allEmailGmPo.getSenderId());
        allEmailPo.setSenderName(allEmailGmPo.getSenderName());
        allEmailPo.setTextMode(RoleEmailPo.TEXT_MODE_SERVER);
        allEmailPo.setTitle(allEmailGmPo.getTitle());
        allEmailPo.setText(allEmailGmPo.getText());
        allEmailPo.setAffixs(allEmailGmPo.getItemDict());
        allEmailPo.setAffixMap(allEmailGmPo.getAffixMap());
        allEmailPo.setCoolTime(allEmailGmPo.getCoolTime());
        allEmailPo.setExpireTime(allEmailGmPo.getExpireTime());
        allEmailPo.setConditionList(allEmailGmPo.getConditionList());
        allEmailPo.setChannelIds(allEmailGmPo.getChannelIds());
        if (copy) {
            allEmailPo.setSendTime(allEmailGmPo.getSendTime());
        }
        return allEmailPo;
    }

    /**
     * 从用户的邮件信息中获取最大的全服邮件id
     *
     * @param data
     * @return
     */
    public static int findMaxRoleAllEmailId(RoleEmailData data) {
        int maxId = 0;
        for (RoleEmailPo emailPo : data.emailMap().values()) {
            if (emailPo.getRefEmailId() > maxId) {
                maxId = emailPo.getRefEmailId();
            }
        }
        return maxId;
    }

    /**
     * 将全服邮件转换成角色邮件
     *
     * @param allEmailPo
     * @return
     */
    public static RoleEmailPo newRoleEmailPo(AllEmailPo allEmailPo) {
        RoleEmailPo roleEmailPo = new RoleEmailPo();
        roleEmailPo.setTemplateId(allEmailPo.getTemplateId());
        roleEmailPo.setRefEmailId(allEmailPo.getAllEmailId());
        roleEmailPo.setSenderType(allEmailPo.getSenderType());
        roleEmailPo.setSenderId(allEmailPo.getSenderId());
        roleEmailPo.setSenderName(allEmailPo.getSenderName());
        roleEmailPo.setTitle(allEmailPo.getTitle());
        roleEmailPo.setTextMode(allEmailPo.getTextMode());
        roleEmailPo.setText(allEmailPo.getText());
        roleEmailPo.setParamsArray(allEmailPo.getParamsArray());
        roleEmailPo.setAffixMap(allEmailPo.getAffixMap());
        roleEmailPo.setSendTime(allEmailPo.getSendTime());
        checkRoleEmailPo(roleEmailPo);
        return roleEmailPo;
    }

    public static RoleEmailPo newRoleEmailPo(AllEmailGmPo allEmailPo) {
        RoleEmailPo roleEmailPo = new RoleEmailPo();
        roleEmailPo.setTemplateId(allEmailPo.getTemplateId());
        roleEmailPo.setRefEmailId(allEmailPo.getAllEmailGmId());
        roleEmailPo.setSenderType(allEmailPo.getSenderType());
        roleEmailPo.setSenderId(allEmailPo.getSenderId());
        roleEmailPo.setSenderName(allEmailPo.getSenderName());
        roleEmailPo.setTitle(allEmailPo.getTitle());
        roleEmailPo.setTextMode(allEmailPo.getTextMode());
        roleEmailPo.setText(allEmailPo.getText());
        roleEmailPo.setParamsArray(allEmailPo.getParamsArray());
        roleEmailPo.setAffixMap(allEmailPo.getAffixMap());
        roleEmailPo.setSendTime(allEmailPo.getSendTime());
        checkRoleEmailPo(roleEmailPo);
        return roleEmailPo;
    }

    public static RoleEmailPo newRoleEmailPo(EmailTemplateVo templateVo) {
        RoleEmailPo roleEmailPo = new RoleEmailPo();
        roleEmailPo.setTextMode(RoleEmailPo.TEXT_MODE_CLIENT);
        roleEmailPo.setTemplateId(templateVo.getTemplateId());
        roleEmailPo.setSenderType(templateVo.getSenderType());
        roleEmailPo.setSenderId(templateVo.getSenderId());
        roleEmailPo.setSenderName(templateVo.getSenderName());
        roleEmailPo.setTitle(templateVo.getTitle());
        roleEmailPo.setText(templateVo.getText());
        roleEmailPo.setAffixMap(templateVo.getAffixMap());
        checkRoleEmailPo(roleEmailPo);
        return roleEmailPo;
    }

    public static AllEmailPo newAllEmailPo(EmailTemplateVo templateVo) {
        AllEmailPo allEmailPo = new AllEmailPo();
        allEmailPo.setTemplateId(templateVo.getTemplateId());
        allEmailPo.setSenderType(templateVo.getSenderType());
        allEmailPo.setSenderId(templateVo.getSenderId());
        allEmailPo.setSenderName(templateVo.getSenderName());
        allEmailPo.setTextMode(RoleEmailPo.TEXT_MODE_CLIENT);
        allEmailPo.setTitle(templateVo.getTitle());
        allEmailPo.setText(templateVo.getText());
        allEmailPo.setAffixMap(templateVo.getAffixMap());
        checkAllEmailPo(allEmailPo);
        return allEmailPo;
    }

    public static void checkRoleEmailPo(RoleEmailPo emailPo) {
//        Objects.requireNonNull(emailPo.getTitle(), "邮件标题不能为空");
//        Objects.requireNonNull(emailPo.getText(), "邮件正文不能为空");
        checkTitleAndText(emailPo.getTitle(), emailPo.getText());
    }

    public static void checkAllEmailPo(AllEmailPo emailPo) {
//        Objects.requireNonNull(emailPo.getTitle(), "邮件标题不能为空");
//        Objects.requireNonNull(emailPo.getText(), "邮件正文不能为空");
        checkTitleAndText(emailPo.getTitle(), emailPo.getText());
    }

    public static void checkTitleAndText(String title, String text) {
        Objects.requireNonNull(title, "邮件标题不能为空");
        Objects.requireNonNull(text, "邮件正文不能为空");
    }
}
