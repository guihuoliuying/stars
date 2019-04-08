/**
 * $RCSfile: Cache.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/01/21 02:33:07 $
 *
 * New Jive  from Jdon.com.
 *
 * This software is the proprietary information of CoolServlets, Inc.
 * Use is subject to license terms.
 */

package com.stars.services.chat.cache;


import java.io.Serializable;
import java.util.HashMap;


/**
 * @author dengzhou
 *
 *用于存储世界聊天频道用户
 *
 */
public class ChatCache implements Serializable{

    /**
     * 存储数据实体<roleid:object>
     */
    protected HashMap<Long,ChaterObject> objectMap;

    /**
     * 保证顺序,同时在遍历的时候提高效率
     */
    protected MyLinkedList linkedList;
    
    

    protected int size = 0;

    public int getObjectMapSize(){
        return objectMap.size();
    }

    public ChatCache() {
        objectMap = new HashMap<Long,ChaterObject>();
        linkedList = new MyLinkedList();
    }

	public int getSize() {
        //return cachedObjectsHash.size();
        return size;
    }

	
	public ChaterObject get(long key){
		return objectMap.get(key);
	}

    public void add(ChaterObject object) {
        if (objectMap.containsKey(object.getRoleId())) {
            return;
        }

        size ++;
        objectMap.put(object.getRoleId(),object);
        //把索引节点存到cacheObject中，以便能回取
        object.linkedListNode = linkedList.addLast(object);
    }
    
    public void remove(long roleId){
    	if (!objectMap.containsKey(roleId)) {
            return;
        }
    	ChaterObject cacheObject = objectMap.remove(roleId);
    	size--;
    	this.linkedList.remove(cacheObject.linkedListNode);
        cacheObject.linkedListNode = null;
    }
    
    public MyLinkedListNode getFirstObject(){
    	return linkedList.getFirst();
    }
}

