package com.stars.core.event;

import com.stars.util.LogUtil;

import java.util.*;

/**
 * fixme: 正式上线要去掉打印
 * Created by zws on 2015/11/27.
 */
public class EventDispatcher {

    private Map<Class<? extends Event>, List<EventListener>> listeners = new HashMap<>();
    private List<EventListener> allListeners = new LinkedList<>();
    private List<EventListener> lastListeners = new LinkedList<>();
    private Map<Class<? extends Event>, List<EventListener>> removalListeners = new HashMap<>();
    private Map<Class<? extends Event>, List<EventListener>> additionListeners = new HashMap<>();

    private Queue<Event> queue = new LinkedList<>();
    private EventLoopChecker checker;
    private int eventCount;
    private boolean inEventLoop = false;
    private boolean isEnable = true;

    private boolean inEventLoop() {
        return inEventLoop;
    }

    public void reg(Class<? extends Event> eventClass, EventListener listener) {
        if (!additionListeners.containsKey(eventClass)) {
            additionListeners.put(eventClass, new ArrayList<EventListener>());
        }
        additionListeners.get(eventClass).add(listener);
        if (!inEventLoop()) {
            reg0();
        }
    }

    private void reg0() {
        for (Map.Entry<Class<? extends Event>, List<EventListener>> entry : additionListeners.entrySet()) {
            Class<? extends Event> eventClass = entry.getKey();
            List<EventListener> listeners = entry.getValue();
            for (EventListener l : listeners) {
                add(eventClass, l);
            }
        }
        additionListeners.clear();
    }

    private void add(Class<? extends Event> eventClass, EventListener listener) {
        // 关注全部事件
        if (eventClass == Event.ALL) {
            allListeners.add(listener);
            return;
        }
        // 关注事件循环完结事件
        if (eventClass == Event.LAST) {
            lastListeners.add(listener);
            return;
        }
        // 关注一般事件
        if (!listeners.containsKey(eventClass)) {
            listeners.put(eventClass, new ArrayList<EventListener>());
        }
        listeners.get(eventClass).add(listener);
    }

    public void unreg(Class<? extends Event> eventClass, EventListener listener) {
        if (!removalListeners.containsKey(eventClass)) {
            removalListeners.put(eventClass, new ArrayList<EventListener>());
        }
        removalListeners.get(eventClass).add(listener);
        if (!inEventLoop()) {
            unreg0();
        }
    }

    private void unreg0() {
        for (Map.Entry<Class<? extends Event>, List<EventListener>> entry : removalListeners.entrySet()) {
            Class<? extends Event> eventClass = entry.getKey();
            List<EventListener> listeners = entry.getValue();
            for (EventListener l : listeners) {
                del(eventClass, l);
            }
        }
        removalListeners.clear();
    }

    private void del(Class<? extends Event> eventClass, EventListener listener) {
        // 关注全部事件
        if (eventClass == Event.ALL) {
            allListeners.remove(listener);
            return;
        }
        // 关注事件循环完结事件
        if (eventClass == Event.LAST) {
            lastListeners.remove(listener);
            return;
        }
        // 关注一般事件
        if (!listeners.containsKey(eventClass)) {
            listeners.put(eventClass, new ArrayList<EventListener>());
        }
        listeners.get(eventClass).remove(listener);
    }

    public void fire(Event event) {
        if (isDisable()) {
//            LogUtil.info("discard event = {}", event);
            return;
        }
        queue.offer(event);
        if (queue.size() == 1) {
            checker = new EventLoopChecker();
            eventCount = -1;
            checker.add(eventCount, event);
            Event e;
            int numOfEvent = 0;
            boolean existLoop = false;
            inEventLoop = true;
            while ((e = queue.peek()) != null) {
                // 判断是否存在循环事件
                eventCount++;
                // 通知观察者进行
                List<EventListener> list = listeners.get(e.getClass());
                if (list != null) {
                    for (EventListener l : list) {
                        execEvent(l, e);
                    }
                }
                // 通知监听全部事件的观察者
                for (EventListener l : allListeners) {
                    execEvent(l, e);
                }
                queue.poll();
                // 当存在事件循环，且消息数量大于128，可以推断很可能存在死循环
                numOfEvent++;
                if (existLoop && numOfEvent >= 128) {
                    LogUtil.error("escape from event loop");
                    break;
                }
                // 注销和注册事件监听器
                regAndUnreg();
            }
            if (queue.size() > 0) { // 移除剩余的事件（必定是事件循环被打破）
                queue.clear();
            }
            // 事件循环结束事件
            LastEvent lastEvent = new LastEvent();
            for (EventListener l : lastListeners) {
                execEvent(l, lastEvent);
            }
            regAndUnreg(); // 再检测一遍
            if (queue.size() > 0) {
                queue.clear();
                throw new IllegalStateException("在事件循环结束事件中不能产生新的事件");
            }
        } else {
            checker.add(eventCount, event);
        }
        inEventLoop = false; // fixme: maybe a bug
    }

    public boolean isEnable() {
        return isEnable;
    }

    public boolean isDisable() {
        return !isEnable;
    }

    public void enable() {
        isEnable = true;
    }

    public void disable() {
        isEnable = false;
    }

    private void regAndUnreg() {
        if (removalListeners.size() > 0) {
            unreg0();
        }
        if (additionListeners.size() > 0) {
            reg0();
        }
    }

    private void execEvent(EventListener listener, Event event) {
        try {
            listener.onEvent(event);
        } catch (Throwable cause) {
            LogUtil.error("", cause);
        }
    }

    class EventLoopChecker {
        private List<Event> eventList = new ArrayList<>(128);
        private Set<Class<? extends Event>> eventClassSet = new HashSet<>();
        public void add(int parentIdx, Event event) {
//            event.parentIdx = parentIdx;
//            eventList.add(event);
//            if (eventClassSet.contains(event.getClass())) {
//                boolean isLoop = false;
//                for (int idx = parentIdx; idx >= 0; idx = eventList.get(idx).parentIdx) {
//                    if (eventList.get(idx).getClass() == event.getClass()) { // 存在循环
//                        isLoop = true;
//                        break;
//                    }
//                }
//                if (isLoop) {
//                    Deque<Class<? extends Event>> printList = new LinkedList<>();
//                    for (int idx = eventList.size()-1; idx >= 0; idx = eventList.get(idx).parentIdx) {
//                        printList.addFirst(eventList.get(idx).getClass());
//                    }
//                    LogUtil.error("存在事件循环, 触发堆栈: {}", printList);
//                }
//            }
//            eventClassSet.add(event.getClass());
        }
    }

}
