package com.stars.services.mail;


import com.stars.services.mail.prodata.EmailTemplateVo;
import com.stars.services.mail.userdata.AllEmailPo;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by huwenjun on 2017/3/28.
 */
public class EmailUtils {

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
