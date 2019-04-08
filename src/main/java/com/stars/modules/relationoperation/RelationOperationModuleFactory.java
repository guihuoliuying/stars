package com.stars.modules.relationoperation;

import com.stars.core.module.AbstractModuleFactory;

/**
 * Created by zhaowenshuo on 2016/9/14.
 */
public class RelationOperationModuleFactory extends AbstractModuleFactory {

    public RelationOperationModuleFactory() {
        super(new RelationOperationPacketSet());
    }
}
