package com.stars.modules.questionnaire;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.core.db.DBUtil;
import com.stars.modules.questionnaire.userdata.RoleQuestionnaire;
import com.stars.util._HashMap;

import java.util.Map;

/**
 * Created by chenkeyu on 2017-05-16 9:14
 */
public class QuestionnaireModule extends AbstractModule {
    private RoleQuestionnaire roleQuestionnaire;

    public QuestionnaireModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {
        roleQuestionnaire = new RoleQuestionnaire(id());
        _HashMap hashMap = DBUtil.querySingleMap(DBUtil.DB_USER, "select roleid,group,stepandquestion,commit from rolequestionnaire where roleid=" + id());
        if (hashMap != null && hashMap.size() > 0) {

        }

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }
}
