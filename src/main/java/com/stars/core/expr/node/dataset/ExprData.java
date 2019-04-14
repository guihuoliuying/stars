package com.stars.core.expr.node.dataset;

/**
 * Created by zhaowenshuo on 2017/3/25.
 */
public interface ExprData {

    long getField(String name);

    boolean isOverlay();

    long getOverlayCount();

    boolean isInvalid();

}
