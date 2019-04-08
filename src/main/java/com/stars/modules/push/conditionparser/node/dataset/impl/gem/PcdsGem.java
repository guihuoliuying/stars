package com.stars.modules.push.conditionparser.node.dataset.impl.gem;

import com.stars.modules.gem.GemManager;
import com.stars.modules.gem.prodata.GemLevelVo;
import com.stars.modules.push.conditionparser.node.dataset.PushCondData;
import com.stars.modules.tool.ToolManager;
import com.stars.modules.tool.productdata.ItemVo;
import com.stars.modules.tool.userdata.RoleToolRow;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by zhaowenshuo on 2017/3/27.
 */
public class PcdsGem implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "lv", "type"));
    }

    private ItemVo unembeddedItemVo; // 未激活的产品数据
    private GemLevelVo unembedded; // 未镶嵌
    private long unembeddedCount; // 未镶嵌数量
    private GemLevelVo embedded; // 已镶嵌

    public PcdsGem(RoleToolRow toolRow) {
        this.unembeddedItemVo = ToolManager.getItemVo(toolRow.getItemId());
        if (!isInvalid()) {
            this.unembedded = GemManager.getGemLevelVo(toolRow.getItemId());
            this.unembeddedCount = toolRow.getCount();
        }
    }

    public PcdsGem(GemLevelVo embedded) {
        this.embedded = embedded;
    }

    @Override
    public long getField(String name) {
        GemLevelVo vo = unembedded != null ? unembedded : embedded;
        switch (name) {
            case "id": return vo.getItemId();
            case "lv": return vo.getLevel();
            case "type": return vo.getType();
        }
        throw new RuntimeException();
    }

    @Override
    public boolean isOverlay() {
        if (unembedded != null)
            return true;
        return false;
    }

    @Override
    public long getOverlayCount() {
        if (unembedded != null)
            return unembeddedCount;
        return 0;
    }

    @Override
    public boolean isInvalid() {
        if (unembeddedItemVo != null)
            return unembeddedItemVo.getType() != ToolManager.TYPE_DIAMOND;
        return false;
    }
}
