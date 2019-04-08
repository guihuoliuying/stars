package com.stars.services.opactsceondkill.pojo;

/**
 * Created by chenkeyu on 2017-09-20.
 */
public class OpActSkStepTime implements Comparable<OpActSkStepTime> {
    private int step;
    private long time;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int compareTo(OpActSkStepTime o) {
        return this.step - o.getStep();
    }
}
