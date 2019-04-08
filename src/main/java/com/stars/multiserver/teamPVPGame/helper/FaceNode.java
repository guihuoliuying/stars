package com.stars.multiserver.teamPVPGame.helper;

/**
 * @author dengzhou
 *对阵节点
 */
public class FaceNode <T extends Object>{
	
	private String id;
	
	private FaceNode<T> father;
	
	private FaceNode<T> leftSon;
	
	private FaceNode<T> rightSon;
	
	private T valueA;
	
	private T valueB;
	
	public FaceNode(String id,FaceNode<T> father){
		this.id = id;
		this.father = father;
		if (father != null) {
			father.setSon(this);
		}
	}
	
	public FaceNode(String id,FaceNode<T> father,T valueA,T valueB){
		this.id = id;
		this.father = father;
		this.valueA = valueA;
		this.valueB = valueB;
	}
	
	public void addValue(T value){
		if (valueA == null) {
			valueA = value;
			return;
		}
		if (valueB != null) {
			throw new RuntimeException("add value err:valueB is not null");
		}
		valueB = value;
	}
	
	public void setSon(FaceNode<T> son){
		if (leftSon == null) {
			leftSon = son;
			return;
		}
		if (rightSon != null) {
			throw new RuntimeException("set son err:rightSon is not null");
		}
		rightSon = son;
	}

	public FaceNode<T> getFather() {
		return father;
	}

	public void setFather(FaceNode<T> father) {
		this.father = father;
	}

	

	public T getValueA() {
		return valueA;
	}

	public void setValueA(T valueA) {
		this.valueA = valueA;
	}

	public T getValueB() {
		return valueB;
	}

	public void setValueB(T valueB) {
		this.valueB = valueB;
	}

	public FaceNode<T> getLeftSon() {
		return leftSon;
	}

	public void setLeftSon(FaceNode<T> leftSon) {
		this.leftSon = leftSon;
	}

	public FaceNode<T> getRightSon() {
		return rightSon;
	}

	public void setRightSon(FaceNode<T> rightSon) {
		this.rightSon = rightSon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
