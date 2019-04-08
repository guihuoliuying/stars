package com.stars.coreManager;

import com.stars.services.chat.cache.MyLinkedList;
import com.stars.services.chat.cache.MyLinkedListNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class SaveDBManager {

    /* 保存手段的开关 */
    public static volatile boolean enableSaving = true; // 正式保存
    public static volatile boolean enableBackupSaving = false; // 后备保存

    /* 后备保存手段 */
    public static Semaphore backupCurrentSavingSemaphore = new Semaphore(24); // 控制同时并发数

    /* 正常保存手段 */
    public static MyLinkedList<Long> roleList = new MyLinkedList<>();

    public static HashMap<Long, MyLinkedListNode<Long>> roleMap = new HashMap<>();

    public static MyLinkedListNode<Long> currentSave;

    public static AtomicInteger Counter = new AtomicInteger(0);

    public static Set<Long> addList = new HashSet<Long>();

    public static Set<Long> removeList = new HashSet<Long>();

    public static void addRole2Save(long roleId) {
        synchronized (addList) {
            addList.add(roleId);
        }
    }

    public static void removeRole(long roleId) {
        synchronized (removeList) {
            removeList.add(roleId);
        }
    }
}

