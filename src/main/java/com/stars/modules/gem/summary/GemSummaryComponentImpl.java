package com.stars.modules.gem.summary;

import com.stars.modules.MConst;
import com.stars.modules.gem.GemModule;
import com.stars.modules.gem.userdata.RoleEquipmentGem;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;

/**
 * Created by zhaowenshuo on 2016/8/16.
 */
public class GemSummaryComponentImpl extends AbstractSummaryComponent implements GemSummaryComponent {

    private RoleEquipmentGem roleGemData;

    public GemSummaryComponentImpl() {
    }

    public GemSummaryComponentImpl(GemModule module, RoleEquipmentGem roleGemData) {
        this.roleGemData = new RoleEquipmentGem(roleGemData);
    }

    @Override
    public String getName() {
        return MConst.GEM;
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
        return this.roleGemData.makeString();
    }

    @Override
    public RoleEquipmentGem getRoleGemData() {
        return this.roleGemData;
    }

    private void parseVer1(String str) {
        this.roleGemData = new RoleEquipmentGem();
        this.roleGemData.parseString(str);
    }


}
