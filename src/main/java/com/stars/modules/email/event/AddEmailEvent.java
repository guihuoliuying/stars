package com.stars.modules.email.event;

import com.stars.core.event.Event;

import java.util.Set;

/**
 * Created by chenkeyu on 2016/11/29.
 */
public class AddEmailEvent extends Event {
    private int emailId;
    private Set<Integer> emailList;

    public AddEmailEvent(int emailId) {
        this.emailId = emailId;
    }

    public AddEmailEvent(Set<Integer> emailList) {
        this.emailList = emailList;
    }

    public Set<Integer> getEmailList() {
        return emailList;
    }

    public int getEmailId() {
        return emailId;
    }
}
