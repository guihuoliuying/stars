package com.stars.modules.title.summary;

import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhaowenshuo on 2016/8/16.
 */
public class TitleSummaryComponentImpl extends AbstractSummaryComponent implements TitleSummaryComponent {

    // type -> titleId
    private Map<Byte, Integer> topFightScoreTitleMap;

    public TitleSummaryComponentImpl() {
    }

    public TitleSummaryComponentImpl(Map<Byte, Integer> topFightScoreTitleMap) {
        this.topFightScoreTitleMap = new HashMap<>(topFightScoreTitleMap);
    }

    @Override
    public String getName() {
        return "title";
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
        return StringUtil.makeString2(topFightScoreTitleMap, '=', ',');
    }

    @Override
    public Map<Byte, Integer> getTopFightScoreTitleMap() {
        return topFightScoreTitleMap;
    }

    @Override
    public void setTopFightScoreTitleMap(Map<Byte, Integer> topFightScoreTitleMap) {
        this.topFightScoreTitleMap = topFightScoreTitleMap;
    }

    private void parseVer1(String str) throws Exception {
        topFightScoreTitleMap = StringUtil.toMap(str, Byte.class, Integer.class, '=', ',');
    }
}
