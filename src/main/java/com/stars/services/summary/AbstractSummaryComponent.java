package com.stars.services.summary;

/**
 * Created by zhaowenshuo on 2016/9/29.
 */
public abstract class AbstractSummaryComponent implements SummaryComponent {

    @Override
    public SummaryComponent clone() {
        try {
            return (SummaryComponent) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public boolean isDummy() {
        return false;
    }
}
