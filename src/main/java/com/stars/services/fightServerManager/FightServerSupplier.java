package com.stars.services.fightServerManager;

import com.stars.multiserver.fightManager.FightServer;
import com.stars.services.chat.cache.MyLinkedList;
import com.stars.services.chat.cache.MyLinkedListNode;
import com.stars.util.LogUtil;
import com.stars.core.rpc.RpcClient;
import com.stars.core.rpc.RpcManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FightServerSupplier {
	
	private Map<Integer, MyLinkedListNode<FightServer>>fServerMap;
	
	private MyLinkedList<FightServer>fServerLink;
	
	private MyLinkedListNode<FightServer>pointer;
	
	private byte level;
	
	public FightServerSupplier(byte level){
		this.level = level;
		fServerMap = new ConcurrentHashMap<Integer, MyLinkedListNode<FightServer>>();
		fServerLink = new MyLinkedList<FightServer>();
	}
	
	public List<FightServer> updateFightServer(Map<Integer, FightServer> map){
		List<FightServer>back = new ArrayList<FightServer>();
		//先判断新增
		Collection<FightServer>col = map.values();
		for (FightServer fightServer : col) {
			if (fightServer.getLeve() != level) {
				continue;
			}
			if (!fServerMap.containsKey(fightServer.getServerId())) {
				if (!RpcManager.rcpClientMap.containsKey(fightServer.getServerId())) {
					try {
						RpcClient rpcClient = new RpcClient(fightServer.getServerId(),
								fightServer.getIp(),fightServer.getPort(), new Connect2FightServerCallBack(level)).connect();
						RpcManager.rcpClientMap.put(fightServer.getServerId(), rpcClient);
					} catch (Exception e) {
						LogUtil.error(e.getMessage(), e);
						continue;
					}
					
				}
				MyLinkedListNode<FightServer>node = fServerLink.addLast(fightServer);
				fServerMap.put(fightServer.getServerId(), node);
				back.add(fightServer);
			}else {
				fServerMap.get(fightServer.getServerId()).getObject().setLoad(fightServer.getLoad());
			}
		}
		//再判断删除
		MyLinkedListNode<FightServer>node = fServerLink.getFirst();
		while (node != null) {
			FightServer fightServer = node.getObject();
			MyLinkedListNode<FightServer>next = node.next;
			if (!map.containsKey(fightServer.getServerId())) {
				fServerMap.remove(fightServer.getServerId());
				if (pointer != null && pointer.getObject().getServerId() == fightServer.getServerId()) {
					pointer = next;
				}
				fServerLink.remove(node);
			}
			node = next;
		}
		if (pointer == null) {
			pointer = fServerLink.getFirst();
		}
		return back;
	}
	
	
	public int getFightServer(){
		if (pointer == null) {
			pointer = fServerLink.getFirst();
		}
		if (pointer == null) {
			LogUtil.error("not find fightServer because list is null");
			return -1;
		}
		int counter = 0;
		while (!pointer.getObject().isConnect()) {
			if (counter >= 100) {
				break;
			}
			pointer = pointer.next;
			if (pointer == null) {
				pointer = fServerLink.getFirst();
			}
			counter++;
		}
		if (pointer == null) {
			LogUtil.error("not find fightServer because all not connect");
			return -1;
		}
		int back = pointer.getObject().getServerId();
		pointer = pointer.next;
		return back;
	}
	
	public void setFightServerConnect(int fightServer,boolean status){
		MyLinkedListNode<FightServer> node = fServerMap.get(fightServer);
		if (node != null) {
			node.getObject().setConnect(true);
		}
	}
}
