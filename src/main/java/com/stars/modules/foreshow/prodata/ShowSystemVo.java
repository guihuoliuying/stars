package com.stars.modules.foreshow.prodata;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2017-03-30 15:26
 */
public class ShowSystemVo {
    private String sysName;
    private String searchId;
    private List<Integer> IdList;
    private String moduleName;

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
        try {
            this.IdList = new ArrayList<>();
            String[] tmpSearchStr = searchId.split("\\|");
            this.moduleName = tmpSearchStr[0];
            String[] tmpSearchIdStr = tmpSearchStr[1].split("\\,");
            for (String ids : tmpSearchIdStr) {
                this.IdList.add(Integer.parseInt(ids));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Integer> getIdList() {
        return IdList;
    }
}
