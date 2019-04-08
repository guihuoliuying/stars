package com.stars.core.hotupdate;

/**
 * Created by wuyuxing on 2017/1/5.
 */
public class CommManagerProxy extends HotUpdateProxy {
    public static HotUpdateInterface manager;

    public static CommManagerInterface getManager(){
        if(manager==null){
            setManager(new CommManager());
        }
        return (CommManagerInterface)manager;
    }

    public static void setManager(HotUpdateInterface manager){
        CommManagerProxy.manager = manager;
    }

    @Override
    public void setHotUpdataInstance(HotUpdateInterface instance) {
        setManager(instance);
    }

    @Override
    public String getClassName() {
        return "CommManager";
    }
}
