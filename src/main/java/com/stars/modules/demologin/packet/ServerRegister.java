package com.stars.modules.demologin.packet;

import com.stars.AccountRow;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.data.DataManager;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.LoginPacketSet;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;
import com.stars.network.server.packet.PacketManager;
import com.stars.startup.MainStartup;
import com.stars.util.Md5Util;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by chenkeyu on 2016/11/18.
 */
public class ServerRegister extends Packet {
    private String limit = "~!@#$%^&*()`-=_+{}[]|\\:;\"',./?><";
    private String account;
    private String password;
    @Override
    public short getType() {
        return LoginPacketSet.S_REGISTER;
    }

    @Override
    public void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff) {

    }

    @Override
    public void readFromBuffer(NewByteBuffer buff) {
        account = buff.readString();
        password = buff.readString();
    }

    @Override
    public void execPacket() {
        register(account,password);
    }
    public void register(String account ,String password) {
        if(account.length()>=18){
            account=account.substring(0,18);
        }
        if(account.isEmpty()){
            com.stars.network.server.packet.PacketManager.send(session,new ClientText(DataManager.getGametext("creatrole_account_empty")));
        }
        if(isHasLimit(account)){
            com.stars.network.server.packet.PacketManager.send(session,new ClientText(DataManager.getGametext("creatrole_account_limit")));
        }
        String md5Password = Md5Util.getMD5Str(password);
        AccountRow accountRow = null;
        try {
            accountRow = LoginModuleHelper.getOrLoadAccount(account,null);
            if(accountRow!=null){
                PacketManager.send(session,new ClientText(DataManager.getGametext("creatrole_account_repeat")));
                MainStartup.accountMap.remove(account);
                return;
            }else {
                // fixme:注册渠道号先写死为ios
                accountRow = new AccountRow(account, "45");
                accountRow.setPassword(md5Password);
                DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getInsertSql(DBUtil.DB_USER, accountRow, "account"));
                // fixme: 临时措施，把老号捞出来 - 开始
                List<AccountRole> relativeRoleList = DBUtil.queryList(DBUtil.DB_USER, AccountRole.class,
                        "select * from `accountrole` where `account`='" + account + "'");
                // fixme: 临时措施，把老号捞出来 - 结束
                accountRow.setRelativeRoleList(relativeRoleList);
                MainStartup.accountMap.putIfAbsent(account, accountRow);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        toClient(account,password);
    }
    private boolean isHasLimit(String account){
        for(int i=0;i<account.length();i++){
            for (int j=0;j<limit.length();j++){
                if (account.charAt(i)==limit.charAt(j)){
                    return true;
                }
            }
        }
        return false;
    }
    private void toClient(String account , String password){
        ClientRegister clientRegister = new ClientRegister();
        clientRegister.setAccount(account);
        clientRegister.setPassword(password);
        send(clientRegister);
    }
}
