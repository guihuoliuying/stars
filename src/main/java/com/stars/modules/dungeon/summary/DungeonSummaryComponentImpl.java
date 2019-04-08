package com.stars.modules.dungeon.summary;

import com.stars.modules.MConst;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2017/4/12.
 */
public class DungeonSummaryComponentImpl extends AbstractSummaryComponent implements DungeonSummaryComponent {
    private Map<Integer, Byte> dungeonStatusMap;

    public DungeonSummaryComponentImpl() {
    }

    public DungeonSummaryComponentImpl(Map<Integer, Byte> dungeonStatusMap) {
        this.dungeonStatusMap = new HashMap<>(dungeonStatusMap);
    }

    @Override
    public String getName() {
        return MConst.Dungeon;
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
        com.stars.util.MapUtil.setString(map, "dungeonStatusMap", StringUtil.makeString2(dungeonStatusMap, '=', ','));
        return StringUtil.makeString2(map, '=', ',');
    }

    @Override
    public Map<Integer, Byte> getDungeonStatusMap() {
        return dungeonStatusMap;
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');
        this.dungeonStatusMap = StringUtil.toMap(MapUtil.getString(map, "dungeonStatusMap", ""), Integer.class, Byte.class, '=', ',');
    }
}
