package com.stars.modules.foreshow.summary;


import com.stars.modules.MConst;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chenkeyu on 2017/1/3 18:56
 */
public class ForeShowSummaryComponentImp extends AbstractSummaryComponent implements ForeShowSummaryComponent {
    private List<String>  forwShowList;

    public ForeShowSummaryComponentImp() {
    }

    public ForeShowSummaryComponentImp(List<String> forwShowList) {
        this.forwShowList = new ArrayList<>(forwShowList);
    }

    @Override
    public String getName() {
        return MConst.ForeShow;
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
        if (StringUtil.isEmpty(str)) {
            this.forwShowList = new ArrayList<>();
            return;
        }
        String[] data = str.split("\\+");
        this.forwShowList = Arrays.asList(data);
    }

    @Override
    public String makeString() {
        if (forwShowList == null) return "";
        return makeString1();
    }

    private String makeString1(){
        StringBuilder sb = new StringBuilder();
        for(String openname : forwShowList){
            sb.append(openname).append("+");
        }
        if(sb.length()>0){
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    @Override
    public boolean isOpen(String openname) {
        if(forwShowList.contains(openname)){
            return true;
        }
        return false;
    }
}
