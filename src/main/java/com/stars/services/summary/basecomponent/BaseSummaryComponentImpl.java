package com.stars.services.summary.basecomponent;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/9/22.
 */
public class BaseSummaryComponentImpl extends AbstractSummaryComponent implements BaseSummaryComponent {

    private int offlineTimestamp;

    public BaseSummaryComponentImpl() {
    }

    public BaseSummaryComponentImpl(int offlineTimestamp) {
        this.offlineTimestamp = offlineTimestamp;
    }

    @Override
    public String getName() {
        return "base";
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

    @Override
    public String makeString() {
        Map<String, String> map = new HashMap<>();
        MapUtil.setInt(map, "offlineTimestamp", offlineTimestamp);
        return StringUtil.makeString2(map, '=', ',');
    }

    @Override
    public int getOfflineTimestamp() {
        return offlineTimestamp;
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        offlineTimestamp = MapUtil.getInt(map, "offlineTimestamp", 0);
    }
}
