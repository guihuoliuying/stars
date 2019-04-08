package com.stars.modules.scene;

import com.stars.core.module.Module;
import com.stars.modules.role.RoleModule;

import java.util.Map;
/**
 * 
 *场景统一接口 
 */
public interface SceneInterface {
	
	/**
	 * 场景对象唯一ID
	 * @return
	 */
	public long geSceneObjectId();
	
	/**
	 * 获取场景刷新信息，目前用在刷新周围玩家场景时，可以封装信息传递
	 * @return
	 */
	public Object getSceneMsg();
	
	 /**
     * 能否进入
     *
     * @return
     */
    public  boolean canEnter(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 不能进入新的场景的时候的处理
     */
    public  void cannotEnterNewSceneDo(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 进入场景
     * 调用之前必须经过判断
     */
    public void enter(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 进入场景之后的处理（就算重复进入也会处理），便于扩展实现需求
     * @param moduleMap
     * @param obj
     */
    public void extendEnter(Map<String, Module> moduleMap, Object obj);
    
    /**
     * 退出场景之后的处理（就算重复进入也会处理），便于扩展实现需求
     * @param moduleMap
     * @param obj
     */
    public void extendExit(Map<String, Module> moduleMap, Scene newScene,Object obj);
    
    /**
     * 登陆之后，旧场景的处理
     * @param moduleMap
     * @param obj
     */
    public void login(Map<String, Module> moduleMap, Object obj);

    /**
     * 退出场景
     */
    public  void exit(Map<String, Module> moduleMap);
    
    /**
     * 进入场景时，刷新玩家位置
     * @param roleModule
     * @param sceneId
     * @param moduleMap
     */
    public void enterAndUpdatePosition(RoleModule roleModule,int sceneId,Map<String, Module> moduleMap);

    /**
     * 场景是否结束
     *
     * @return
     */
    public  boolean isEnd();

}
