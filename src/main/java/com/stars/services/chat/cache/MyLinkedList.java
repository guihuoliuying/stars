/**
 * $RCSfile: LinkedList.java,v $
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
public class MyLinkedList<T extends Object> implements Serializable {

    private MyLinkedListNode<T> head;
    
    private MyLinkedListNode<T> tail;

    public MyLinkedList() {
        head = null;
        tail = null;
    }

    public MyLinkedListNode<T> getFirst() {
        return head;
    }

    public MyLinkedListNode<T> getLast() {
       return tail;
    }

    public MyLinkedListNode<T> addFirst(T object) {
        MyLinkedListNode<T> node = new MyLinkedListNode<T>(object);
        if (this.head == null) {
			this.head = node;
			this.tail = node;
			return node;
		}
        node.previous = null;
        head.previous = node;
        node.next = head;
        this.head = node;
        return node;
    }

    public MyLinkedListNode<T> addLast(T object) {
    	MyLinkedListNode<T> node = new MyLinkedListNode<T>(object);
    	if (this.tail == null) {
    		this.head = node;
			this.tail = node;
			return node;
		}
    	this.tail.next = node;
        node.previous = tail;
        this.tail = node;
        return node;
    }

    public void clear() {
        //Remove all references in the list.
        MyLinkedListNode<T> node = getLast();
        while (node != null) {
            node.remove();
            node = getLast();
        }

        //Re-initialize.
        head = null;
        tail = null;
    }
    
    public void remove(MyLinkedListNode<T> node){
    	if (head == node) {
    		if (node.next != null) {
    			setHead(node.next);
			}else {
				setHead(null);
			}
		}
    	if (tail == node) {
    		if (node.previous != null) {
				setTail(node.previous);
			}else {
				setTail(null);
			}
		}
    	node.remove();
    }

    @Override
	public String toString() {
        return "";
    }

	public void setHead(MyLinkedListNode<T> head) {
		this.head = head;
	}

	public void setTail(MyLinkedListNode<T> tail) {
		this.tail = tail;
	}
}