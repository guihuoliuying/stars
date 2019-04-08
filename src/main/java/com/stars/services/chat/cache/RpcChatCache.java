package com.stars.services.chat.cache;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by chenkeyu on 2016/11/9.
 */
public class RpcChatCache implements Serializable {
    /**
     * 存储数据实体<roleid:object>
     */
    protected HashMap<Long,RpcChatObject> objectMap;

    /**
     * 保证顺序,同时在遍历的时候提高效率
     */
    protected MyLinkedList linkedList;

    protected int size = 0;

    public RpcChatCache(){
        objectMap = new HashMap<>();
        linkedList = new MyLinkedList();
    }

    public int getSize() {
        return size;
    }

    public RpcChatObject get(long key){
        return objectMap.get(key);
    }

    public void add(RpcChatObject object){
        if (objectMap.containsKey(object.getRoleId())){
            return;
        }
        size++;
        objectMap.put(object.getRoleId(),object);
        object.linkedListNode = linkedList.addLast(object);
    }

    public void remove(long roleId){
        if(!objectMap.containsKey(roleId)){
            return;
        }
        RpcChatObject rpcChatObject = objectMap.remove(roleId);
        size--;
        this.linkedList.remove(rpcChatObject.linkedListNode);
        rpcChatObject.linkedListNode = null;
    }
    public MyLinkedListNode getFirstObject(){
        return linkedList.getFirst();
    }
}
