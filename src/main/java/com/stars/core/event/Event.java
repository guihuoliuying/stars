package com.stars.core.event;

/**
 * Created by zws on 2015/11/27.
 */
public abstract class Event {

    public static Class<? extends Event> ALL = AllEvent.class; //
    public static Class<? extends Event> LAST = LastEvent.class; //

    int parentIdx;

    @Override
    public String toString() {
        return "<" + parentIdx + "," + this.getClass().getSimpleName() + ">";
    }
}

class AllEvent extends Event {

}

class LastEvent extends Event {

}
