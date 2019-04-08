package com.stars.modules.questionnaire;

import com.stars.core.module.AbstractModuleFactory;

/**
 * Created by chenkeyu on 2017-05-16 9:14
 */
public class QuestionnaireModuleFactory extends AbstractModuleFactory<QuestionnaireModule> {
    public QuestionnaireModuleFactory() {
        super(new QuestionnairePacketSet());
    }
}
