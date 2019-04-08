package com.stars.modules.demologin.gm;

import com.stars.AccountRow;
import com.stars.core.module.Module;
import com.stars.core.player.PlayerUtil;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.packet.ClientText;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.gm.GmHandler;
import com.stars.startup.MainStartup;
import com.stars.util.Md5Util;

import java.util.List;
import java.util.Map;

/**
 * Created by chenkeyu on 2016/11/4.
 */
public class AddUserGmHandler implements GmHandler {
    private String channel;

    @Override
    public void handle(long roleId, Map<String, Module> moduleMap, String[] args) throws Exception {
        if(System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS")!=0){
            PlayerUtil.send(roleId,new ClientText("远程服暂时没有权限注册账号"));
        }
        if(args==null || args.length>2){
            PlayerUtil.send(roleId,new ClientText("账号密码不完整"));
        }
        if (args.length < 2) {
            channel = "-1@-1@-1";
        } else {
            channel = args[1];
        }
        String[] string = args[0].split("=");
        String account=string[0];
        String pass = string[1];
        int count ;
        if(string.length==2){
            reg(roleId,account,pass);
        }
        else if(string.length==3){
            count = Integer.parseInt(string[2]);
            reg(roleId,account,pass,count);
        }else{
            PlayerUtil.send(roleId,new ClientText("参数错误"));
        }
    }
    private void reg(long roleId, String account , String pass , int count) throws Exception{
        String md5Password = com.stars.util.Md5Util.getMD5Str(pass);
        for(int i=1;i<=count;i++){
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account+i,null);
            if(accountRow!=null){
                PlayerUtil.send(roleId,new ClientText("用户已存在，无需创建"));
            }else {
                accountRow = new AccountRow(account+i, channel);
                accountRow.setPassword(md5Password);
                DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRow, "account"));
                // fixme: 临时措施，把老号捞出来 - 开始
                List<AccountRole> relativeRoleList = DBUtil.queryList(DBUtil.DB_USER, AccountRole.class,
                        "select * from `accountrole` where `account`='" + account + "'");
                // fixme: 临时措施，把老号捞出来 - 结束
                accountRow.setRelativeRoleList(relativeRoleList);
                MainStartup.accountMap.putIfAbsent(account, accountRow);
                PlayerUtil.send(roleId,new ClientText("用户"+account+i+"创建成功"));
            }
        }
    }
    private void reg(long roleId, String account , String pass) throws Exception{
        String md5Password = Md5Util.getMD5Str(pass);
        AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(account,null);
        if(accountRow!=null){
            PlayerUtil.send(roleId,new ClientText("用户已存在，无需创建"));
        }else {
            accountRow = new AccountRow(account, channel);
            accountRow.setPassword(md5Password);
            DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRow, "account"));
            // fixme: 临时措施，把老号捞出来 - 开始
            List<AccountRole> relativeRoleList = DBUtil.queryList(DBUtil.DB_USER, AccountRole.class,
                    "select * from `accountrole` where `account`='" + account + "'");
            // fixme: 临时措施，把老号捞出来 - 结束
            accountRow.setRelativeRoleList(relativeRoleList);
            MainStartup.accountMap.putIfAbsent(account, accountRow);
            PlayerUtil.send(roleId,new ClientText("用户"+account+"创建成功"));
        }
    }
}
