package com.stars.modules.fashion.summary;

import com.stars.modules.MConst;
import com.stars.modules.fashion.userdata.RoleFashion;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.MapUtil;
import com.stars.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gaopeidian on 2016/12/16.
 */
public class FashionSummaryComponentImpl extends AbstractSummaryComponent implements FashionSummaryComponent {
    private int dressFashionId = -1;
    private RoleFashion roleDressFashion;

    public FashionSummaryComponentImpl() {
    }

    public FashionSummaryComponentImpl(RoleFashion roleDressFashion) {
        this.roleDressFashion = roleDressFashion;
        if (!StringUtil.isEmpty(this.roleDressFashion)){
            this.dressFashionId = roleDressFashion.getFashionId();
        }
    }

    @Override
    public String getName() {
        return MConst.Fashion;
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
        com.stars.util.MapUtil.setInt(map, "dressFashionId", dressFashionId);
        return StringUtil.makeString2(map, '=', ',');
    }

    private void parseVer1(String str) throws Exception {
        Map<String, String> map = StringUtil.toMap(str, String.class, String.class, '=', ',');        
        this.dressFashionId = MapUtil.getInt(map, "dressFashionId", -1);
    }

    
    /**
     * 获得当前穿着时装id
     * @return 当前穿着时装id，若无穿着时装，则返回-1
     */
	@Override
	public int getDressFashionId() {
		return dressFashionId;
	}

    @Override
    public RoleFashion getRoleDressFashion() {
        return roleDressFashion;
    }
}
