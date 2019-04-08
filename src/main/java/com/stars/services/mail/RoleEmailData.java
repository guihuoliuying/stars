package com.stars.services.mail;

import com.stars.services.mail.userdata.RoleEmailInfoPo;
import com.stars.services.mail.userdata.RoleEmailPo;

import java.util.*;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class RoleEmailData {

    private RoleEmailInfoPo info;
    private Map<Integer, RoleEmailPo> emailMap;
    private List<RoleEmailPo> emailList;

    private Set<Integer> untreatedEmailIdSet;

    public RoleEmailData(RoleEmailInfoPo info, Map<Integer, RoleEmailPo> emailMap) {
        this.info = info;
        this.emailMap = emailMap;
        this.emailList = new LinkedList<>(emailMap.values());
        this.untreatedEmailIdSet = new HashSet<>();
        Collections.sort(this.emailList);

        for (RoleEmailPo po : emailMap.values()) {
            if (!po.isRead()) {
                addUntreatedEmail(po.getEmailId());
            }
        }
    }

    public RoleEmailInfoPo info() {
        return info;
    }

    public void info(RoleEmailInfoPo info) {
        this.info = info;
    }

    public Map<Integer, RoleEmailPo> emailMap() {
        return emailMap;
    }

    public List<RoleEmailPo> emailList() {
        return emailList;
    }

    public void deleteEmailFromList(int emailId) {
        Iterator<RoleEmailPo> itor = emailList.iterator();
        while (itor.hasNext()) {
            RoleEmailPo emailPo = itor.next();
            if (emailPo.getEmailId() == emailId) {
                itor.remove();
                return;
            }
        }
    }

    public void addEmailToList(RoleEmailPo roleEmailPo) {
        int emailId = roleEmailPo.getEmailId();
        int listSize = emailList.size();
        for (int i = 0; i < listSize; i++) {
            if (emailList.get(i).getSendTime() > roleEmailPo.getSendTime()) {
                emailList.add(i, roleEmailPo);
                return;
            }
        }
        emailList.add(roleEmailPo);
    }

    // 未处理邮件（红点）
    public void addUntreatedEmail(int emailId) {
        untreatedEmailIdSet.add(emailId);
    }

    public void removeUntreatedEmail(int emailId) {
        untreatedEmailIdSet.remove(emailId);
    }

    public void updateUntreatedEmail(int emailId) {
        RoleEmailPo po = emailMap.get(emailId);
        if (po != null) {
            if (po.isRead()) {
                untreatedEmailIdSet.remove(emailId);
            }
        }
    }

    public boolean hasUntreatedEmail() {
        return !untreatedEmailIdSet.isEmpty();
    }

    public int getUntreatedEmailCount() {
        return untreatedEmailIdSet.size();
    }

    @Override
    public String toString() {
        return "RoleEmailData{" +
                "info=" + info +
                ", emailMap=" + emailMap +
                '}';
    }
}
