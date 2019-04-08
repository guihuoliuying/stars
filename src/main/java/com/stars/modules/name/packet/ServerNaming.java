package com.stars.modules.name.packet;

import com.stars.modules.name.NamePacketSet;
import com.stars.network.server.buffer.NewByteBuffer;
import com.stars.network.server.packet.Packet;

public class ServerNaming extends Packet {
	private String name;
	
	public ServerNaming(){
		
	}
	public  void writeToBuffer(com.stars.network.server.buffer.NewByteBuffer buff){}
	@Override
	public void execPacket() {
		// TODO Auto-generated method stub
//		if (name.length() > 6) {
//			send(new ClientText("randomename_toolong"));
//			return;
//		}
//		if (name.equals("") || name.length() < 2) {
//			send(new ClientText("randomename_tooshort"));
//			return;
//		}
//		SqlDataReqPacket req = new SqlDataReqPacket(CommonDbInfo.getDbId("common", true), "select unique_id from game_account where name = '" + name + "'");
//        DbClientUtil.getCommClient().request(req, new DbExecCallback(new QueryNameCallback()));
	}
	@Override
    public void readFromBuffer(NewByteBuffer buff) {
		name = buff.readString();
    }
	@Override
	public short getType() {
		// TODO Auto-generated method stub
		return NamePacketSet.Server_Naming;
	}

	
//	class QueryNameCallback implements DbProxyCallback{
//		public QueryNameCallback(){
//		}
//		public void onCalled(Object responseMsg){
//	      if (responseMsg instanceof SqlDataRespPacket) {
//	          SqlDataRespPacket resp = (SqlDataRespPacket) responseMsg;
//	          List<RowData> rowData = resp.getSqlData().getMultiRowResult();
//	          if (rowData != null && rowData.size() > 0) {//说明已经有这个名字了
//	        	  send(new ClientText("randomename_namerepeat"));
//	        	  return;
//	          }else {
//	        	  Object o = session.removeAttribute("roleId");
//	        	  long roleId;
//	        	  if (o == null) {
//	        		 roleId = DataManager2.getIdGenerator().newId("roleId");
//				 }else {
//					roleId = (long)o;
//				 }
//	        	 String uniqueId = (String)(session.getAttribute("uniqueId"));
//                SqlDataReqPacket req = new SqlDataReqPacket(CommonDbInfo.getDbId("common", true), 
//                		"update game_account set name = '" + name+"' where unique_id='"+uniqueId+"'");
//                DbClientUtil.getCommClient().request(req,null); 
//                final DistLock lock = new DistLock(uniqueId);
//             	lock.lock(new ExecCallback(new DistLockCallbck(uniqueId,lock,roleId)));
//	          }     
//	      }
//		}
//		public void onFailed(Object failedMsg){
//			LogUtil.info(failedMsg.toString());
//		}
//		
//	}
//	class DistLockCallbck implements Callback {
//        private String uniqueId;
//        private DistLock lock;
//        private long roleId;
//
//        public DistLockCallbck(String uniqueId, DistLock lock,long roleId) {
//            this.uniqueId = uniqueId;
//            this.lock = lock;
//            this.roleId  = roleId;
//        }
//
//        @Override
//        public void onCalled(CallbackContext ctx) {
////            LogUtil.info("DistLockCallbck");
//        	String token = (String)session.removeAttribute("token");
//            LogUtil.info("登陆，分布式锁返回，account={}, token={}", uniqueId, token);
//            Response<String> response = (Response<String>) ctx.attr("response");
//            if (ctx.isTimeout() || ctx.isFailure()) { // redis异常
//                LogUtil.info("登陆，分布式锁异常/超时，account={}, token={}", uniqueId, token);
//                send(new ClientText("login_err_distlock"));
//                PacketManager.closeFrontend(session);
//                lock.unlock(new EmptyCallback());
//
//            } else if (!"OK".equals(response.get())) {
//                LogUtil.info("登陆，分布式锁失败，account={}, token={}", uniqueId, token);
//                send(new ClientText("login_err_other_login"));
//                PacketManager.closeFrontend(session);
//                lock.unlock(new EmptyCallback());
//
//            } else {
//                LogUtil.info("登陆，分布式锁成功，account={}, token={}", uniqueId, token);
//                Player player = PlayerSystem.getOrAdd(roleId, new Player(roleId));
//                player.setUserName(name);
//                player.tell(new StartLoginMsg(uniqueId, roleId, lock, session, true), Actor.noSender);
//            }
//        } 
//        public void onFailed(Object failedMsg){
//			LogUtil.info(failedMsg.toString());
//		}
//    } 
}
