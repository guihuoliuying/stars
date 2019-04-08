package com.stars.modules.drop;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModuleFactory;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.db.DBUtil;
import com.stars.modules.drop.listener.RequestSendSingleEmailListener;
import com.stars.modules.drop.prodata.DropRewardVo;
import com.stars.modules.drop.prodata.DropVo;
import com.stars.modules.email.event.RequestSendSingleEmailEvent;
import com.stars.util.LogUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liuyuheng on 2016/6/30.
 */
public class DropModuleFactory extends AbstractModuleFactory<DropModule> {
    public DropModuleFactory() {
        super(new DropPacketSet());
    }

    @Override
    public DropModule newModule(long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> map) {
        return new DropModule(id, self, eventDispatcher, map);
    }

    @Override
    public void loadProductData() throws Exception {
        loadDropVo();
    }

    private void loadDropVo() throws Exception {
        String sql = "select * from `drop`; ";
        Map<Integer, DropVo> map = DBUtil.queryMap(DBUtil.DB_PRODUCT, "dropid", DropVo.class, sql);
        if (map != null) {
            DropManager.dropVoMap = map;
        }

//        // 嵌套层数检查
//        AtomicInteger dropLoop;// 掉落嵌套层数
//        for (DropVo dropVo : map.values()) {
//            Set<Integer> itemIdSet = new HashSet<>();
//            for (DropRewardVo rewardVo : dropVo.getRewardList()) {
//                dropLoop = new AtomicInteger(1);// 当前本身就是1层
//                if (rewardVo.getType() == 0) {
//                    itemIdSet.add(rewardVo.getRewardId());
//                    continue;
//                }
//                try {
//                    checkLoop(rewardVo, dropLoop, itemIdSet);
//                } catch (Exception e) {
//                    LogUtil.error("掉落组嵌套超过最大次数[{}],dropId={}", DropManager.MAXLOOP, dropVo.getDropId());
//                    throw e;
//                }
//            }
//            dropVo.setReawardItemIdSet(itemIdSet);
//
//            //根据groupId分组
//            dropGroup = dropGroupMap.get(dropVo.getGroupId());
//            if(dropGroup == null){
//                dropGroup = new ArrayList<>();
//                dropGroupMap.put(dropVo.getGroupId(),dropGroup);
//            }
//            dropGroup.add(dropVo);
//        }

        Map<Integer,List<DropVo>> dropGroupMap = new HashMap<>();
        List<DropVo> dropGroup;
        for (DropVo dropVo : map.values()) {
            //根据groupId分组
            dropGroup = dropGroupMap.get(dropVo.getGroupId());
            if(dropGroup == null){
                dropGroup = new ArrayList<>();
                dropGroupMap.put(dropVo.getGroupId(),dropGroup);
            }
            dropGroup.add(dropVo);
        }
        DropManager.dropGroupMap = dropGroupMap;
    }

    /**
     * 递归检查掉落嵌套层数
     * 例如：101——>102——>103,如果103不再调用掉落组,即是3层嵌套
     *
     * @param rewardVo
     * @param dropLoop
     */
    private void checkLoop(DropRewardVo rewardVo, AtomicInteger dropLoop, Set<Integer> itemIdSet) {
        // 嵌套超过最大次数,直接返回,避免死循环
        if (dropLoop.get() > DropManager.MAXLOOP) {
            throw new IllegalArgumentException();
        }
        if (rewardVo.getType() == 0) {
            itemIdSet.add(rewardVo.getRewardId());
            return;
        }
        DropVo nextDropVo = DropManager.getDropVo(rewardVo.getRewardId());
        if (nextDropVo != null) {
            for (DropRewardVo nextRewardVo : nextDropVo.getRewardList()) {
                if (nextRewardVo.getType() == 1) {
                    dropLoop.incrementAndGet();
                    break;
                }
            }
            for (DropRewardVo nextRewardVo : nextDropVo.getRewardList()) {
                checkLoop(nextRewardVo, dropLoop, itemIdSet);
            }
        } else {
            LogUtil.error("找不到id={}的掉落组配置,请检查表", rewardVo.getRewardId(), new IllegalArgumentException());
            return;
        }
    }

    @Override
    public void registerListener(EventDispatcher eventDispatcher, Module module) {
        eventDispatcher.reg(RequestSendSingleEmailEvent.class, new RequestSendSingleEmailListener(module));
    }
}
