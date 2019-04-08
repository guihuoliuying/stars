package com.stars.services.friend.summary;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.services.summary.SummaryConst;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wuyuxing on 2016/11/23.
 */
public class FriendFlowerSummaryComponentImpl extends AbstractSummaryComponent implements FriendFlowerSummaryComponent {

    private int sendFlowerCount;    //送花数量
    private int receiveFlowerCount; //收花数量

    public FriendFlowerSummaryComponentImpl() {
    }

    public FriendFlowerSummaryComponentImpl(int sendFlowerCount, int receiveFlowerCount) {
        this.sendFlowerCount = sendFlowerCount;
        this.receiveFlowerCount = receiveFlowerCount;
    }

    @Override
    public String getName() {
        return SummaryConst.C_FRIEND_FLOWER;
    }

    @Override
    public int getLatestVersion() {
        return 1;
    }

    @Override
    public void fromString(int version, String str) {
        try {
            switch (version) {
                case 1:
                    parseVer1(str);
                    break;
            }
        } catch (Exception e) {
            LogUtil.error("", e);
        }
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.sendFlowerCount = MapUtil.getInt(map, "sendFlowerCount", 0);
        this.receiveFlowerCount = MapUtil.getInt(map, "receiveFlowerCount", 0);
    }

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        MapUtil.setInt(map, "sendFlowerCount", sendFlowerCount);
        MapUtil.setInt(map, "receiveFlowerCount", receiveFlowerCount);
        return StringUtil.makeString2(map, '=', ',');
    }

    public int getSendFlowerCount() {
        return sendFlowerCount;
    }

    public int getReceiveFlowerCount() {
        return receiveFlowerCount;
    }

}
