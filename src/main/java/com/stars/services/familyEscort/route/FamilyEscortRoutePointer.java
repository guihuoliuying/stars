package com.stars.services.familyEscort.route;

/**
 * @author dengzhou
 *镖车行走路线的路点
 */
public class FamilyEscortRoutePointer {
	
	private int id;
	private int pointerX;
	private int pointerY;
	private int pointerZ;
	public FamilyEscortRoutePointer(){
		
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPointerX() {
		return pointerX;
	}
	public void setPointerX(int pointerX) {
		this.pointerX = pointerX;
	}
	public int getPointerY() {
		return pointerY;
	}
	public void setPointerY(int pointerY) {
		this.pointerY = pointerY;
	}
	public int getPointerZ() {
		return pointerZ;
	}
	public void setPointerZ(int pointerZ) {
		this.pointerZ = pointerZ;
	}
}
