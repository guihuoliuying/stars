package com.stars.core.gmpacket.giftpackage;

import com.stars.modules.tool.ToolManager;
import com.stars.server.main.gmpacket.GmPacketHandler;
import com.stars.server.main.gmpacket.GmPacketResponse;
import com.stars.util.ServerLogConst;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liuyuheng on 2017/2/10.
 */
public class CreateGiftPackageGm extends GmPacketHandler {
    @Override
    public String handle(HashMap args) {
        GmPacketResponse response = null;
        try {
            int giftId = Integer.parseInt((String) args.get("id"));// 礼包id
            int type = Integer.parseInt((String) args.get("type"));// 操作类型,1:增加;2:修改
            long deadline = Long.parseLong((String) args.get("deadline"));// 礼拜使用截止时间,运营传过来单位是秒
            long totalAmount = Long.parseLong((String) args.get("totalAmount"));// 礼包兑换码数量
            // 礼包物品信息
            List<Map<String, String>> itemDict = (List<Map<String, String>>) args.get("itemData");
            if (giftId == 0 || type == 0 || deadline == 0 || totalAmount == 0 || itemDict.isEmpty()) {
                throw new IllegalArgumentException("礼包信息错误");
            }
            for (Map<String, String> itemMap : itemDict) {
                int itemId = Integer.parseInt(itemMap.get("itemCode"));
                if (!ToolManager.isTool(itemId)) {
                    throw new IllegalArgumentException("礼包物品不存在");
                }
            }
            ServerLogConst.console.info("创建礼包成功");
            response = new GmPacketResponse(GmPacketResponse.SUC, 0, resultToJson(""));
        } catch (Exception e) {
            ServerLogConst.console.info("创建礼包异常," + e.getMessage());
            response = new GmPacketResponse(GmPacketResponse.TIMEOUT, 0, resultToJson(""));
        } finally {
            return response.toString();
        }
    }
}
