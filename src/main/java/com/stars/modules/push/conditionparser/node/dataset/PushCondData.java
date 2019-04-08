package com.stars.modules.push.conditionparser.node.dataset;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public interface PushCondData {

    long getField(String name);

    boolean isOverlay();

    long getOverlayCount();

    boolean isInvalid();

}
