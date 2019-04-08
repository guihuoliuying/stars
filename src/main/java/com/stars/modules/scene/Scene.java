package com.stars.modules.scene;

import com.stars.core.module.Module;
import com.stars.modules.role.RoleModule;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 场景接口
 * 所有类型场景必须实现这个接口
 * Created by liuyuheng on 2016/7/5.
 */
public abstract class Scene implements SceneInterface{
	
	private long id;//场景对象唯一ID
	
	private int sceneId;//场景地图id
	
	public static AtomicLong nowMaxId = new AtomicLong();
	
	private byte sceneType;//场景类型
	
	public Scene(){
		this.id = nowMaxId.addAndGet(1);
	}
	
	public Object getSceneMsg(){
		return null;
	}
	
	   /**
     * 能否重复进入
     *
     * @return
     */
    public boolean isCanRepeatEnter(Scene newScene,byte newSceneType,int newSceneId, Object extend){
    	if (newSceneType == this.sceneType  && newSceneId == this.sceneId) {
			return false;
		}
		return true;
    }
    
	/**
	 * 不能进入安全区，可能是带有进入条件的安全区 比如被踢出了家族了就不能进入 初始化角色安全区位置，重新进入
	 */
    public  void cannotEnterNewSceneDo(Map<String, Module> moduleMap, Object obj){}
    
	public Scene createNewScene(Scene newScene, byte newSceneType, int newSceneId) {
		if (newScene != null)
			return newScene;
		return newScene = SceneManager.newScene(newSceneType);
	}
	

	/**
	 * 是否能进入新的场景
	 * @param newScene
	 * @param sceneType
	 * @param sceneId
	 * @param extend
	 * @param moduleMap
	 * @param obj
	 * @return
	 */
	public boolean isCanEnterNewScene(Scene newScene,byte sceneType, int sceneId, Object extend,Map<String, Module> moduleMap, Object obj){
		if (!newScene.canEnter(moduleMap, extend)) {
			newScene.cannotEnterNewSceneDo(moduleMap, obj);
			return false;
		}
		return true;
	}
	
    /**
     * 能否进入
     *
     * @return
     */
    public abstract boolean canEnter(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 进入场景
     * 调用之前必须经过判断
     */
    public abstract void enter(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 进入场景之后的处理（就算重复进入也会处理），便于扩展实现需求
     * @param moduleMap
     * @param obj
     */
    public void extendEnter(Map<String, Module> moduleMap, Object obj){};
    
    /**
     * 退出场景之后的处理（就算重复进入也会处理），便于扩展实现需求
     * @param moduleMap
     * @param obj
     */
    public void extendExit(Map<String, Module> moduleMap, Scene newScene,Object obj){};
    
    /**
     * 登陆之后，旧场景的处理
     * @param moduleMap
     * @param obj
     */
    public void login(Map<String, Module> moduleMap, Object obj){};

    /**
     * 退出场景
     */
    public abstract void exit(Map<String, Module> moduleMap);

    /**
     * 场景是否结束
     *
     * @return
     */
    public abstract boolean isEnd();
    
    public void enterAndUpdatePosition(RoleModule roleModule,int sceneId,Map<String, Module> moduleMap){
	}

	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}
	
	public boolean isInFightScene(){
		if (this instanceof FightScene) {
			return true;
		}
		return false;
	}

	public byte getSceneType() {
		return sceneType;
	}

	public void setSceneType(byte sceneType) {
		this.sceneType = sceneType;
	}

	public long geSceneObjectId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
}
