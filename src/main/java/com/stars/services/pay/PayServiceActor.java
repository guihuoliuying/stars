package com.stars.services.pay;

import com.google.gson.Gson;
import com.stars.AccountRow;
import com.stars.core.player.Player;
import com.stars.core.player.PlayerSystem;
import com.stars.core.db.DBUtil;
import com.stars.core.db.SqlUtil;
import com.stars.modules.demologin.LoginModuleHelper;
import com.stars.modules.demologin.message.PullUpMsg;
import com.stars.modules.demologin.userdata.AccountRole;
import com.stars.modules.role.summary.RoleSummaryComponent;
import com.stars.modules.vip.VipManager;
import com.stars.modules.vip.event.VipChargeEvent;
import com.stars.modules.vip.event.VipLevelupEvent;
import com.stars.modules.vip.prodata.ChargeVo;
import com.stars.modules.vip.prodata.VipinfoVo;
import com.stars.multiserver.MainRpcHelper;
import com.stars.multiserver.payServer.RMPayServiceActor;
import com.stars.services.SConst;
import com.stars.services.ServiceHelper;
import com.stars.services.ServiceSystem;
import com.stars.startup.MainStartup;
import com.stars.util.*;
import com.stars.core.actor.Actor;
import com.stars.core.actor.invocation.ServiceActor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PayServiceActor extends ServiceActor implements PayService {

    public HashSet<String> hasDoOrders;

    public HashMap<String, String> notDoOrders;


    public PayServiceActor() {
        hasDoOrders = new HashSet<String>();
        notDoOrders = new HashMap<String, String>();

    }

    @Override
    public void init() throws Throwable {
        ServiceSystem.getOrAdd(SConst.PayService, this);
        //从数据库里面捞取处理过的订单号(2天内的)
        List<_HashMap> list = DBUtil.queryList(DBUtil.DB_USER, _HashMap.class,
                "select orderno from payment where time > date_sub(now(),interval 2 day)");
        if (list.size() > 0) {
            for (_HashMap _HashMap : list) {
                hasDoOrders.add(_HashMap.getString("orderno"));
            }
        }
    }

    @Override
    public void printState() {

    }

    @Override
    public void recaivePayOrder(int serverId, int payServer, PayOrderInfo pOrderInfo, boolean isUseTool) {
//		cpTradeNo	String	Y	 通行证及支付服务生成的订单号
//		gameId 	int	Y	游戏编号
//		userId	String	Y	用户编号
//		roleId	String	N	角色编号
//		serverId	int	N	游戏区号(客户端传过来)
//		channelId	String	Y	渠道编号
//		itemId	String	N	购买的物品编号
//		itemAmount	int	Y	发货数量，优先使用money字段发货
//		privateField	String	N	应用自定义字段varchar(128) 保留字段，除非有特别说明，否则一律为空字符串。
//		money	int	Y	发货金额（分，值为-1时使用itemAmount字段发货
//		currencyType	String	Y	货币类型：CNY(人民币);USD(美元)；
//		fee	float	Y	实际支付金额(单位，元); 和currencyType结合；人民币以元为单位，USD以美元为单位；
//		status	String	Y	交易状态，0表示成功
//		giftId	String	N	赠品编号
//		sign	String	Y	验证签名，privateKey接入时分配 sign=md5(cpTradeNo|gameId|userId|roleId|serverId|channelId|itemId|itemAmount|privateField|money|status|privateKey)
//		                       字段如果为null则用空的字符串代替

        Gson gson = new Gson();
        String orderStr = gson.toJson(pOrderInfo);

        LogUtil.info("recaive order : " + orderStr);

        String orderNo = pOrderInfo.getCpTradeNo();

        if (hasDoOrders.contains(orderNo)) {
            //已经处理过了
            MainRpcHelper.rmPayServerService().doPayOrderCallBack(payServer, orderNo, RMPayServiceActor.PAY_STATUS_SUCCED);
            return;
        }

        //md5校验
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(pOrderInfo.getCpTradeNo()).append("|");
        sBuilder.append(pOrderInfo.getGameId()).append("|");
        sBuilder.append(pOrderInfo.getUserId()).append("|");
        sBuilder.append(pOrderInfo.getRoleId()).append("|");
        sBuilder.append(pOrderInfo.getServerId()).append("|");
        sBuilder.append(pOrderInfo.getChannelId()).append("|");
        sBuilder.append(pOrderInfo.getItemId()).append("|");
        sBuilder.append(pOrderInfo.getItemAmount()).append("|");
        sBuilder.append(pOrderInfo.getPrivateField()).append("|");
        sBuilder.append(pOrderInfo.getMoney()).append("|");
        sBuilder.append(pOrderInfo.getStatus()).append("|");
        sBuilder.append("e45d4508633746cf4fd5c5f6b");

        //合法性做校验
        if (pOrderInfo.getChannelId() != -1) {//客户端GM不做校验
            String md5 = Md5Util.md5(sBuilder.toString());
            if (!md5.equals(pOrderInfo.getSign())) {
                LogUtil.info("md5 check not pass order:" + orderStr);
                MainRpcHelper.rmPayServerService().doPayOrderCallBack(payServer, pOrderInfo.getCpTradeNo(), RMPayServiceActor.PYA_STATUS_FAILED);
                return;
            }
        }
        long roleId = Long.parseLong(pOrderInfo.getRoleId());
        PayExtent payEx = JsonUtil.fromJson(pOrderInfo.getPrivateField(), PayExtent.class);
        int chargeId = payEx.getId();
        int money = pOrderInfo.getMoney();
        ChargeVo chargeVo = null;
        if (pOrderInfo.getChannelId() == 45) {
            chargeVo = VipManager.getChargeVo(MainStartup.serverChannel, pOrderInfo.getItemId());
        } else {
            chargeVo = VipManager.getChargeVo(MainStartup.serverChannel, chargeId);
        }

        if (money != -1 && money / 100 != chargeVo.getReqRmb()) {
            //这种情况下，应该就是类似于充值卡这种情况了
            List<ChargeVo> cList = VipManager.chargetVoList.get(MainStartup.serverChannel);
            int tChargeId = -1;
            for (ChargeVo cVo : cList) {
                if (money / 100 >= cVo.getReqRmb()) {
                    tChargeId = cVo.getChargeId();
                    break;
                }
            }
            chargeId = tChargeId;
            LogUtil.info(roleId + " Use payCard,payCardMoney=" + money + ",chargeId=" + chargeId);
        }

        if (money == -1) {
            money = chargeVo.getReqRmb();
        } else {
            money = money / 100;
        }

        byte payPoint = payEx.getPoint();


        String accountId = pOrderInfo.getUserId() + "#" + pOrderInfo.getChannelId();
        if (pOrderInfo.getChannelId() == -1) {
            accountId = pOrderInfo.getUserId();
        }

        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("insert into payment values('").append(orderNo).append("'");
        stringBuffer.append(",'").append(money).append("'");
        stringBuffer.append(",'").append(accountId).append("'");
        stringBuffer.append(",'").append(roleId).append("'");
        stringBuffer.append(",now()");
        stringBuffer.append(",0");
        stringBuffer.append(",'").append(chargeId).append("'");
        stringBuffer.append(",'").append(pOrderInfo.getActionType()).append("'");
        stringBuffer.append(")");
        boolean isFirst = false;
        if(!isUseTool) {
            //先入库
            try {
                DBUtil.execSql(DBUtil.DB_USER, stringBuffer.toString());
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                return;
            }
        }
        if(!isUseTool) {
        //加入已处理名单
        hasDoOrders.add(orderNo);
            //通知支付服已经接收订单
            try {
                MainRpcHelper.rmPayServerService().doPayOrderCallBack(payServer, orderNo, RMPayServiceActor.PAY_STATUS_SUCCED);
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
            }
        }

        try {
            AccountRow accountRow = LoginModuleHelper.getOrLoadAccount(accountId, null);

            if (accountRow == null) {
                LogUtil.info("not find accountRow,order failed order = " + orderStr);
                return;
            }

            Player player = PlayerSystem.get(roleId);
            if (player == null) {
                //如果玩家不在线，从数据库里拉起来
                player = LoginModuleHelper.loadPlayerFromDB(roleId, accountRow);
                player.tell(new PullUpMsg(accountRow.getName(), roleId, accountRow.newRoleIdVersion(), false, pOrderInfo.getChannelId()), Actor.noSender);
            }
            if (player == null) {
                LogUtil.info("not find role,order failed order=" + orderStr);
                return;
            }

            if (accountRow.getVipExp() <= 0) {
                isFirst = true;
            }

            int lastVipLevel = accountRow.getVipLevel(); //更新前的VIP等级

            updateAccountData(accountRow, roleId, money, chargeId, orderNo);

            AccountRole accountRole = accountRow.getAccountRole(roleId);
            if (accountRole == null) {
                LogUtil.info("role not match account,order failed order = " + orderStr);
                return;
            }


            notDoOrders.put(orderNo, orderStr);
            //通知角色发货
            ServiceHelper.roleService().notice(roleId, new VipChargeEvent(orderNo, money, chargeId, isFirst, payPoint, pOrderInfo.getActionType(), lastVipLevel));

        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
        }

    }

    public void updateAccountData(AccountRow account, long roleId, int money, int chargeId, String orderNo) {
        int addExp = money * VipManager.VIP_EXP_COEF;
//		String channel = MainStartup.serverChannel;
        //下面主要是更新账号的vip经验及vip等级
        try {
            if (account.getLoginLock().tryLock(500, TimeUnit.MILLISECONDS)) {
                try {
                    int oldVIPLv = account.getVipLevel();
                    int newExp = addExp + account.getVipExp();
                    account.setVipExp(newExp);
                    //更新累计充值
                    account.setChargeSum(account.getChargeSum() + money);
                    //接下来计算VIP等级
                    int counter = oldVIPLv + 1;
                    VipinfoVo vipinfoVo = VipManager.getVipinfoVo(counter);
                    while (vipinfoVo != null && vipinfoVo.getReqExp() <= newExp) {
                        account.setVipLevel(counter);
                        counter++;
                        vipinfoVo = VipManager.getVipinfoVo(counter);
                    }
                    if (oldVIPLv != account.getVipLevel()) {
                        //刷新充值角色的vip等级
                        Player player = PlayerSystem.get(roleId);
                        if (player != null) {
                            LogUtil.info("vipInfo notice role  = " + roleId + "preLv = " + oldVIPLv + ",curLv=" + account.getVipLevel());
                            ServiceHelper.roleService().notice(roleId, new VipLevelupEvent(oldVIPLv, account.getVipLevel()));
                        }

                        //刷新在线角色的vip等级
                        long curRoleId = account.getCurrentRoleId();
                        if (curRoleId != 0 && curRoleId != roleId) {
                            player = PlayerSystem.get(curRoleId);
                            if (player != null) {
                                LogUtil.info("vipInfo notice role  = " + curRoleId + "preLv = " + oldVIPLv + ",curLv=" + account.getVipLevel());
                                ServiceHelper.roleService().notice(curRoleId, new VipLevelupEvent(oldVIPLv, account.getVipLevel()));
                            }
                        }

                        for (int level = oldVIPLv + 1, curLevel = account.getVipLevel(); level <= curLevel; level++) {
                            // 本服滚屏公告
                            vipinfoVo = VipManager.getVipinfoVo(level);
                            if (vipinfoVo.getLevelUpNoticeType() == VipManager.VIP_LEVELUP_NOTICE_TYPE_LOACL && StringUtil.isNotEmpty(vipinfoVo.getLevelUpNotice())) {
                                RoleSummaryComponent rsc = (RoleSummaryComponent) ServiceHelper.summaryService().getSummaryComponent(roleId, "role");
                                if (rsc != null) {
                                    ServiceHelper.chatService().announce(vipinfoVo.getLevelUpNotice(), rsc.getRoleName(),
                                            String.valueOf(level));
                                }
                            }
                        }
                    }
                    //入库
                    DBUtil.execSql(DBUtil.DB_USER, SqlUtil.getUpdateSql(DBUtil.DB_USER, account, "account", "name='" + account.getName() + "'"));
                    DBUtil.execSql(DBUtil.DB_USER, "update payment set status=1 where orderno='" + orderNo + "'");
                    LogUtil.info("order complete order=" + orderNo);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                } finally {
                    account.getLoginLock().unlock();
                }
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
    }

    @Override
    public void consignmentCallBack(String orderNo, int money, int chargeId) {
        LogUtil.info("consignmentCallBack order=" + orderNo);
        String orderStr = notDoOrders.remove(orderNo);
    }
}
