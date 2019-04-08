package com.stars.modules.push.conditionparser.node.dataset.impl.tool;

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
public class PcdsTool implements PushCondData {

    public static Set<String> fieldSet() {
        return new HashSet<String>(Arrays.asList("id", "quality", "type"));
    }

    private RoleToolRow toolRow;
    private ItemVo itemVo;

    public PcdsTool(RoleToolRow toolRow) {
        this.toolRow = toolRow;
        this.itemVo = ToolManager.getItemVo(toolRow.getItemId());
    }

    @Override
    public long getField(String name) {
        switch (name) {
            case "id": return toolRow.getItemId();
            case "quality": return itemVo.getColor();
            case "type": return itemVo.getType();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean isOverlay() {
        return true;
    }

    @Override
    public long getOverlayCount() {
        return toolRow.getCount();
    }

    @Override
    public boolean isInvalid() {
        return itemVo.getType() == ToolManager.TYPE_DIAMOND
                || itemVo.getType() == ToolManager.TYPE_SEARCHTREASURE
                || itemVo.getType() == 7;
    }
}
