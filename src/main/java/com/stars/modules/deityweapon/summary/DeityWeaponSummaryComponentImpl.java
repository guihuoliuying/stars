package com.stars.modules.deityweapon.summary;

import com.stars.modules.MConst;
import com.stars.modules.deityweapon.DeityWeaponConstant;
import com.stars.modules.deityweapon.userdata.RoleDeityWeapon;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.services.ServiceHelper;
import com.stars.services.summary.AbstractSummaryComponent;
import com.stars.util.LogUtil;
import com.stars.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by panzhenfeng on 2016/12/12.
 */
public class DeityWeaponSummaryComponentImpl extends AbstractSummaryComponent implements DeityWeaponSummaryComponent {

    private List<RoleDeityWeapon> roleDeityWeaponList = new ArrayList<>();

    public DeityWeaponSummaryComponentImpl(){

    }

    public DeityWeaponSummaryComponentImpl(List<RoleDeityWeapon> roleDeityWeaponList) {
        this.roleDeityWeaponList = roleDeityWeaponList;
    }

    @Override
    public List<RoleDeityWeapon> getRoleDeityWeaponList() {
        return this.roleDeityWeaponList;
    }

    @Override
    public RoleDeityWeapon getCurRoleDeityWeapon(){
        if(roleDeityWeaponList != null){
            for(int i = 0, len = roleDeityWeaponList.size(); i<len; i++){
                if(roleDeityWeaponList.get(i).getState() == DeityWeaponConstant.DRESS){
                    return roleDeityWeaponList.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public int getCurRoleDeityWeapoonId(){
        RoleDeityWeapon roleDeityWeapon = getCurRoleDeityWeapon();
        if(roleDeityWeapon != null){
            RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(
                    roleDeityWeapon.getRoleId(), "role");
            return roleDeityWeapon.getDeityweaponId(rsc.getRoleJob());
        }
        return 0;
    }

    @Override
    public String getName() {
        return MConst.Deity;
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
        StringBuilder sb = new StringBuilder();
        if(roleDeityWeaponList.size() > 0){
            for(int i = 0, len = roleDeityWeaponList.size(); i<len; i++){
                sb.append(this.roleDeityWeaponList.get(i).makeString());
                if(i+1 < len){
                    sb.append("+");
                }
            }
        }else{
            sb.append("0");
        }
        return sb.toString();
    }

    private void parseVer1(String str) {
        roleDeityWeaponList.clear();
        if(StringUtil.isNotEmpty(str) && !str.equals("0")){
            String[] arr = str.split("[+]");
            RoleDeityWeapon roleDeityWeapon = null;
            for(int i = 0, len = arr.length; i<len; i++){
                roleDeityWeapon = new RoleDeityWeapon();
                roleDeityWeapon.parseString(arr[i]);
                roleDeityWeaponList.add(roleDeityWeapon);
            }
        }
    }


}
