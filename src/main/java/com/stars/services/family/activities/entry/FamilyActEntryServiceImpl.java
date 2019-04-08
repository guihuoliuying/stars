package com.stars.services.family.activities.entry;

import com.stars.core.player.PlayerUtil;
import com.stars.modules.family.packet.ClientFamilyActEntry;
import com.stars.util.LogUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 枚举值：
 * 亮: 3
 * 闪: 5
 * 灰: 1
 * Created by zhaowenshuo on 2016/10/8.
 */
public class FamilyActEntryServiceImpl implements FamilyActEntryService {

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    private Map<Integer, FamilyActEntry> entryMap = new HashMap<>();

    private String text = "";

    @Override
    public void init() throws Throwable {
    }

    @Override
    public void setOptions(int activityId, int flag, int deadlineOfCountdown, String text) {
        writeLock.lock();
        try {
            FamilyActEntry entry = entryMap.get(activityId);
            if (entry == null) {
                entry = new FamilyActEntry(activityId);
                entryMap.put(activityId, entry);
            }
            entry.setFlag(flag); // 显示状态
            entry.setDeadlineOfCountdown(deadlineOfCountdown); // 倒计时秒数
            entry.setText(text);
        } catch (Exception e) {
            LogUtil.error("", e);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void sendEntryList(long roleId, Map<Integer, Integer> maskMap, List<Integer> notShowList) {
        readLock.lock();
        try {
            ClientFamilyActEntry entry = new ClientFamilyActEntry(entryMap, maskMap, notShowList);
            PlayerUtil.send(roleId, entry);
        } catch (Exception e) {
            LogUtil.error("", e);
        } finally {
            readLock.unlock();
        }
    }

}
