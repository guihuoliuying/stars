package com.stars.services.summary;

/**
 * Created by zhaowenshuo on 2016/8/11.
 */
public interface SummaryComponent extends Cloneable {

    String getName();

    int getLatestVersion();

    void fromString(int version, String str);

    String makeString();

    SummaryComponent clone();

    boolean isDummy();

}
