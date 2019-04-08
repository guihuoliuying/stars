package com.stars.modules.teampvpgame.event;

import com.stars.core.event.Event;
import com.stars.modules.teampvpgame.userdata.SignUpSubmiter;

/**
 * 报名确认事件
 * Created by liuyuheng on 2016/12/16.
 */
public class SignUpEvent extends Event {
    private SignUpSubmiter signUpSubmiter;// 报名提交者

    public SignUpEvent(SignUpSubmiter signUpSubmiter) {
        this.signUpSubmiter = signUpSubmiter;
    }

    public SignUpSubmiter getSignUpSubmiter() {
        return signUpSubmiter;
    }
}
