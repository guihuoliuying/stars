package com.stars.core.persist;

import com.stars.core.persist.SaveDBManager;
import com.stars.core.persist.SaveDbResult;
import com.stars.core.player.PlayerSystem;
import com.stars.modules.demologin.message.AutoSaveMsg;
import com.stars.services.chat.cache.MyLinkedListNode;
import com.stars.util.LogUtil;
import com.stars.core.actor.Actor;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by zhaowenshuo on 2017/2/7.
 */ //自动保存任务
public class TriggerAutoSavingTask implements Runnable {

    private long lastPrintedTime = 0L;
    private List<SaveDbResult> resultList = new LinkedList<>();

    @Override
    public void run() {
        long now = System.currentTimeMillis();
        if (now - lastPrintedTime > 30_000) { // 每30秒打印一次
            LogUtil.info("定时触发自动保存任务进行, sizeOfPlayerSystem=" + PlayerSystem.size() +
                    "|tid:" + Thread.currentThread().getId() +
                    "|counter:" + SaveDBManager.Counter +
                    "|resultList:" + resultList.size());
            lastPrintedTime = now;
        }

        //
        if (resultList.size() > 0) {
            Iterator<SaveDbResult> it = resultList.iterator();
            while (it.hasNext()) {
                try {
                    SaveDbResult result = it.next();
                    if (result.isFinished()) {
                        it.remove();
                    } else if (now - result.getTimestamp() > 30_000) {
                        if (result.cancel()) {
                            SaveDBManager.Counter.decrementAndGet(); // 强制释放
                        }
                        it.remove();
                        LogUtil.error("自动保存|超时|roleId:" + result.getRoleId());
                    }
                } catch (Throwable t) {
                    LogUtil.error("自动保存|超时检查异常", t);
                }
            }
        }

        if (!SaveDBManager.enableSaving) {
            return;
        }

        try {
        	
        	synchronized (SaveDBManager.removeList) {
                if (SaveDBManager.removeList.size() > 0) {
                    for (Long roleId : SaveDBManager.removeList) {
                        if (SaveDBManager.currentSave != null && SaveDBManager.currentSave.getObject().longValue() == roleId) {
                            SaveDBManager.currentSave = SaveDBManager.currentSave.next;
                        }
                        if (SaveDBManager.roleMap.containsKey(roleId)) {
                        	SaveDBManager.roleList.remove(SaveDBManager.roleMap.remove(roleId));
                        }

                    }
                    SaveDBManager.removeList.clear();
                }
            }
        	
            synchronized (SaveDBManager.addList) {
                if (SaveDBManager.addList.size() > 0) {
                    for (Long roleId : SaveDBManager.addList) {
                    	if (SaveDBManager.roleMap.containsKey(roleId)) {
							continue;
						}
                        SaveDBManager.roleMap.put(roleId, SaveDBManager.roleList.addLast(roleId));
                    }
                    SaveDBManager.addList.clear();
                }
            }
            

            if (SaveDBManager.currentSave == null) {
                SaveDBManager.currentSave = SaveDBManager.roleList.getFirst();
            }

            MyLinkedListNode<Long> head = SaveDBManager.currentSave;
            while (SaveDBManager.currentSave != null) {
                if (SaveDBManager.Counter.incrementAndGet() > 24) {
                    SaveDBManager.Counter.decrementAndGet();
                    return;
                }
                try {
                    long roleId = SaveDBManager.currentSave.getObject();
                    Actor actor = PlayerSystem.system().getActor(roleId);
                    SaveDBManager.currentSave = SaveDBManager.currentSave.next;
                    if (SaveDBManager.currentSave == null) {
                        SaveDBManager.currentSave = SaveDBManager.roleList.getFirst();
                    }
                    if (actor != null) {
                        SaveDbResult result = new SaveDbResult(roleId, now, SaveDBManager.Counter);
                        actor.tell(new AutoSaveMsg(result), Actor.noSender);
                        resultList.add(result);
                    } else {
                        SaveDBManager.Counter.decrementAndGet();
                        SaveDBManager.removeRole(roleId);
                    }
                } catch (Exception e) {
                    SaveDBManager.Counter.decrementAndGet();
                    SaveDBManager.currentSave = SaveDBManager.roleList.getFirst();
                    LogUtil.error(e.getMessage(), e);
                }
                if (head == SaveDBManager.currentSave) {
                    return;
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            LogUtil.error("定时触发自动保存任务异常, sizeOfPlayerSystem=" + PlayerSystem.size() + "|tid:" + Thread.currentThread().getId());
        }
    }
}
