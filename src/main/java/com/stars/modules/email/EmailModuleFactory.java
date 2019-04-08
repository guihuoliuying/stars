package com.stars.modules.email;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.drop.listener.RequestSendSingleEmailListener;
import com.stars.modules.email.event.EmailRedPointEvent;
import com.stars.modules.email.event.RequestSendSingleEmailEvent;
import com.stars.modules.email.event.SpecialEmailEvent;
import com.stars.modules.email.gm.*;
import com.stars.modules.email.listener.EmailRedPointListener;
import com.stars.modules.email.listener.SpecialEmailListener;
import com.stars.modules.gm.GmManager;
import com.stars.modules.tool.ToolManager;
import com.stars.services.mail.prodata.EmailTemplateVo;
import com.stars.util.MapUtil;

import java.util.Map;

import static com.stars.modules.data.DataManager.commonConfigMap;

/**
 * Created by zhaowenshuo on 2016/8/2.
 */
public class EmailModuleFactory extends AbstractModuleFactory<EmailModule> {

    public EmailModuleFactory() {
        super(new EmailPacketSet());
    }

    @Override
    public void loadProductData() throws Exception {
        Map<Integer, EmailTemplateVo> tmpTemplateMap = DBUtil.queryMap(DBUtil.DB_PRODUCT, "templateid", EmailTemplateVo.class,
                "select * from `emailtemplate`");
        for (EmailTemplateVo templateVo : tmpTemplateMap.values()) {
            Map<Integer, Integer> affixMap = templateVo.getAffixMap();
            if (affixMap != null) {
                for (Integer itemId : affixMap.keySet()) {
                    if (!ToolManager.isResource(itemId) && !ToolManager.isTool(itemId) && !ToolManager.isEquip(itemId)) {
                        throw new Exception("加载邮件产品数据异常，不存在道具，templateId=" + templateVo.getTemplateId() + "，itemId=" + itemId);
                    }
                }
            }
        }
        EmailManager.templateMap = tmpTemplateMap;

        EmailManager.EMAIL_LIMIT = MapUtil.getInt(commonConfigMap, "email_maxnum", 30);
    }

    @Override
    public EmailModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new EmailModule(id, self, eventDispatcher, map);
    }

    @Override
    public void init() throws Exception {
//        GmManager.reg("sendemail", new SendEmailGmHandler());
        GmManager.reg("recvemail", new RecvEmailGmHandler());
        GmManager.reg("deleteemail", new DeleteEmailGmHandler());
        GmManager.reg("reademail", new ReadEmailGmHandler());
        GmManager.reg("fetchaffixs", new FetchAffixsGmHandler());
        GmManager.reg("sendemailbytemplate", new SendEmailByTemplateGmHandler());
        GmManager.reg("senditem", new SendItemGmHandler());
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RequestSendSingleEmailEvent.class, new RequestSendSingleEmailListener(module));
//        eventDispatcher.reg(AddEmailEvent.class,new AddEmailListener((EmailModule) module));
//        eventDispatcher.reg(RemoveEmailEvent.class,new RemoveEmailListener((EmailModule) module));
        eventDispatcher.reg(EmailRedPointEvent.class, new EmailRedPointListener((EmailModule) module));
        eventDispatcher.reg(SpecialEmailEvent.class, new SpecialEmailListener((EmailModule) module));
    }
}
