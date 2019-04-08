package com.stars.modules.collectphone.handler;

import com.stars.core.module.Module;
import com.stars.modules.collectphone.CollectPhoneManager;
import com.stars.modules.collectphone.prodata.StepOperateAct;
import com.stars.modules.collectphone.usrdata.RoleCollectPhone;
import com.stars.util.LogUtil;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by huwenjun on 2017/9/20.
 */
public class TemplateHandlerFactory {
    public static Map<Integer, Class<? extends AbstractTemplateHandler>> templateHandlerMap = new HashMap<>();

    static {
        templateHandlerMap.put(1, QuestionTemplateHandler.class);
        templateHandlerMap.put(2, PhoneTemplateHandler.class);
    }

    public static AbstractTemplateHandler getTemplateHandler(int step, RoleCollectPhone roleCollectPhone, Map<String, Module> moduleMap) {
        StepOperateAct stepOperateAct = CollectPhoneManager.stepOperateActMap.get(step);
        int type = stepOperateAct.getType();
        Class<? extends AbstractTemplateHandler> clazz = templateHandlerMap.get(type);
        try {
            Constructor<? extends AbstractTemplateHandler> constructor = clazz.getConstructor(int.class, RoleCollectPhone.class, Map.class);
            AbstractTemplateHandler abstractTemplateHandler = constructor.newInstance(step, roleCollectPhone, moduleMap);
            return abstractTemplateHandler;
        } catch (Exception e) {
            LogUtil.error("init collect phone template handler error", e);
        }
        return null;
    }
}
