package com.stars.modules.optionalbox;

import com.stars.core.event.EventDispatcher;
import com.stars.core.module.AbstractModule;
import com.stars.core.module.Module;
import com.stars.core.player.Player;
import com.stars.modules.MConst;
import com.stars.modules.optionalbox.packet.ClientOptionalBoxPacket;
import com.stars.modules.optionalbox.prodata.OptionalBox;
import com.stars.modules.serverLog.EventType;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.ToolModule;
import com.stars.modules.tool.func.ToolFunc;
import com.stars.modules.tool.func.impl.OptionalBoxFunc;
import com.stars.modules.tool.packet.ClientAward;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huwenjun on 2017/11/15.
 */
public class OptionalBoxModule extends AbstractModule {
    public OptionalBoxModule(String name, long id, Player self, EventDispatcher eventDispatcher, Map<String, Module> moduleMap) {
        super(name, id, self, eventDispatcher, moduleMap);
    }

    @Override
    public void onDataReq() throws Throwable {

    }

    @Override
    public void onInit(boolean isCreation) throws Throwable {

    }

    /**
     * 请求自选礼包内的物品列表
     *
     * @param itemId
     * @param count
     */
    public void reqToolList(int itemId, int count) {
        if (!isValidOptionBoxByItemId(itemId, count)) {
            warn("无效自选礼包(不拥有或非自选礼包）");
            return;
        }
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        OptionalBoxFunc optionalBoxFunc = (OptionalBoxFunc) itemVo.getToolFunc();
        int group = optionalBoxFunc.getGroup();
        List<OptionalBox> optionBoxList = getOptionBoxList(group);
        ClientOptionalBoxPacket clientOptionalBoxPacket = new ClientOptionalBoxPacket(ClientOptionalBoxPacket.SEND_TOOLLIST);
        clientOptionalBoxPacket.setOptionalBoxes(optionBoxList);
        send(clientOptionalBoxPacket);
    }

    /**
     * 请求获得自选礼包内指定的物品
     *
     * @param itemId
     * @param count
     * @param optionId
     */
    public void reqChooseItem(int itemId, int count, int optionId) {
        if (!isValidOptionBoxByItemId(itemId, count)) {
            warn("无效自选礼包(不拥有或非自选礼包）");
            return;
        }
        ToolModule toolModule = module(MConst.Tool);
        OptionalBox optionalBox = OptionalBoxManager.optionalBoxMap.get(optionId);
        if (optionalBox == null) {
            warn("未选择有效物品");
            return;
        }
        boolean success = toolModule.deleteAndSend(itemId, count, EventType.OPTIONAL_BOX.getCode());
        if (success) {
            LogUtil.info("optionalbox|roleid:{} use itemId:{} choose:{}", id(), itemId, optionId);
            Map<Integer, Integer> reward = new HashMap<>();
            for (int index = 0; index < count; index++) {
                Map<Integer, Integer> itemMap = optionalBox.getItemMap();
                MapUtil.add(reward, itemMap);
            }
            toolModule.addAndSend(reward, EventType.OPTIONAL_BOX.getCode());
            ClientAward clientAward = new ClientAward(reward);
            clientAward.setType((byte) 1);
            send(clientAward);
            ClientOptionalBoxPacket clientOptionalBoxPacket = new ClientOptionalBoxPacket(ClientOptionalBoxPacket.SEND_CHOOSETOOL);
            send(clientOptionalBoxPacket);
        } else {
            warn("扣除物品失败");
        }


    }

    /**
     * 获取可选宝箱列表
     *
     * @param group
     * @return
     */
    public List<OptionalBox> getOptionBoxList(int group) {
        List<OptionalBox> optionalBoxes = new ArrayList<>();
        List<OptionalBox> optionalBoxList = OptionalBoxManager.optionBoxListMap.get(group);
        for (OptionalBox optionalBox : optionalBoxList) {
            if (optionalBox.isValid(moduleMap())) {
                optionalBoxes.add(optionalBox);
            }
        }
        return optionalBoxes;
    }

    /**
     * 是否是有效自选道具礼包
     *
     * @param itemId
     * @return
     */
    public boolean isValidOptionBoxByItemId(int itemId, int count) {
        if (count <= 0) {
            return false;
        }
        ToolModule toolModule = module(MConst.Tool);
        boolean contains = toolModule.contains(itemId, count);
        if (!contains) {
            return false;
        }
        ItemVo itemVo = ToolManager.getItemVo(itemId);
        ToolFunc toolFunc = itemVo.getToolFunc();
        if (toolFunc instanceof OptionalBoxFunc) {
            return true;
        }
        return false;
    }
}
