package com.stars.modules.collectphone.handler;

import com.stars.core.module.Module;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;

import java.util.Map;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class QuestionTemplateHandler extends AbstractTemplateHandler {
    public QuestionTemplateHandler(int step, RoleCollectPhone roleCollectPhone, Map<String, Module> moduleMap) {
        super(step, roleCollectPhone, moduleMap);
    }

    @Override
    public void submit(String answer) {
        getRoleCollectPhone().put(getStep(), answer);
    }

    @Override
   public boolean isOver() {
        return true;
    }
}
