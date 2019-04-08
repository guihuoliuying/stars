/**
 * $RCSfile: LinkedListNode.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2009/12/24 02:24:24 $
 *
 * New Jive  from Jdon.com.
 *
 * This software is the proprietary information of CoolServlets, Inc.
 * Use is subject to license terms.
 */

package com.stars.services.chat.cache;

import java.io.Serializable;

/**
 * @author dengzhou
 *
 */
public class MyLinkedListNode<T extends Object> implements Serializable {

    public MyLinkedListNode<T> previous;
    public MyLinkedListNode<T> next;
    private T object;
    
    public MyLinkedListNode(T object){
    	this.object = object;
    }

    /**
     * 构造一节点node
     */
    public MyLinkedListNode(T object, MyLinkedListNode<T> next,
            MyLinkedListNode<T> previous)
    {
        this.object = object;
        this.next = next;
        this.previous = previous;
    }

    /**
     * 把node从link中删除，只是引用删除，如果在其他的地方存在对此node的引用，对象将
     * 不会给垃圾收集
     */
    public void remove() {
    	if (previous != null) {
			previous.next = next;
		}
    	if (next != null) {
			next.previous = previous;
		}
    	this.previous = null;
    	this.next = null;
    	this.object = null;
    }
    @Override
	public String toString() {
        return object.toString();
    }

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}
}