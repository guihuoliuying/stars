package com.stars.core.module;

import com.stars.core.annotation.DependOn;
import com.stars.core.event.EventDispatcher;
import com.stars.core.player.Player;
import com.stars.modules.push.PushModuleFactory;
import com.stars.util.LogUtil;
import com.stars.util.ServerLogConst;

import java.util.*;

/**
 * Created by zws on 2015/11/30.
 */
public class ModuleManager {

    private static Map<String, ModuleFactory> factoryMap = new LinkedHashMap<>();
    private static Map<String, Set<String>> dependencyMap = new HashMap<>(); // 从属
    private static List<String> dependenceSequence = new ArrayList<>(); // 依赖序列

    public static void register(String name, ModuleFactory factory) throws Exception {
        if (factoryMap.containsKey(name)) {
            throw new IllegalArgumentException("模块[" + name + "]已存在"+" | exist mudule="+factoryMap.get(name).getClass().getName());
        }
        factoryMap.put(name, factory);
        factory.initModuleKey(name);
        LogUtil.info("模块[" + name + "]已注册");
    }

    public static void initPacket() throws Exception {
        for (ModuleFactory factory : factoryMap.values()) {
            factory.initPacket();
        }
    }

    public static void init() throws Exception {
        for (ModuleFactory factory : factoryMap.values()) {
            factory.init();
        }
    }

    public static ModuleFactory get(String name) {
        return factoryMap.get(name);
    }

    public static Iterator<Map.Entry<String, ModuleFactory>> iterator() {
        return factoryMap.entrySet().iterator();
    }

    public static Iterator<String> nameIterator() {
        return factoryMap.keySet().iterator();
    }

    public static Iterator<ModuleFactory> factoryIterator() {
        return factoryMap.values().iterator();
    }

    public static void loadProductData() throws Exception {
        for (ModuleFactory factory : factoryMap.values()) {
            factory.loadProductData();
        }
    }

    public static void loadProductData(String... moduleNames) throws Exception {
        Set<String> nameSet = new HashSet<>();
        nameSet.addAll(Arrays.asList(moduleNames)); // maybe wrong
        for (String name : factoryMap.keySet()) {
            if (nameSet.contains(name)) {
                factoryMap.get(name).loadProductData();
                ServerLogConst.console.info("load table = "+name);
            }
        }
    }

    public static Map<String, Module> newModuleList(long id, Player self, EventDispatcher eventDispatcher) {
        Map<String, Module> moduleMap = new LinkedHashMap<>();
        for (Map.Entry<String, ModuleFactory> entry : factoryMap.entrySet()) {
            String name = entry.getKey();
            ModuleFactory factory = entry.getValue();
            Module module = factory.newModule(id, self, eventDispatcher, moduleMap);
            if (module != null) {
                moduleMap.put(name, module);
                factory.registerListener(eventDispatcher, module);
            }
        }
        return moduleMap;
    }

    public static Set<String> moudleNames() {
        return factoryMap.keySet();
    }

    public static void initDependence() {
        Map<String, Set<String>> dependencyMap = new HashMap<>();
        List<String> dependenceSequence = new ArrayList<>();

        for (Map.Entry<String, ModuleFactory> entry : factoryMap.entrySet()) {
            String moduleName = entry.getKey();
            dependencyMap.put(moduleName, new HashSet<String>());
//            dependenceSequence.add(moduleName);

            DependOn dependOn = entry.getValue().getClass().getAnnotation(DependOn.class);
            if (dependOn != null) {
                for (String baseModuleName : dependOn.value()) {
                    dependencyMap.get(baseModuleName).add(moduleName);
                }
            }
        }

        // 完成依赖顺序的排序(先对依赖集排序)
        for (int i = 0; i < factoryMap.size(); i++) {
            for (Map.Entry<String, ModuleFactory> entry : factoryMap.entrySet()) {
                String name = entry.getKey();
                if (dependenceSequence.contains(name)) { // 如果已在队列中则跳过
                    continue;
                }

                DependOn dependOn = entry.getValue().getClass().getAnnotation(DependOn.class);
                boolean flag = true;
                if (dependOn != null) {
                    for (String baseModuleName : dependOn.value()) {
                        if (!dependenceSequence.contains(baseModuleName)) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    dependenceSequence.add(name);
                    break;
                }
            }
        }
        LogUtil.info("模块依赖|dependencyMap:{}", dependencyMap);
        LogUtil.info("模块依赖|dependenceSequence:{}", dependenceSequence);
        // 检查循环依赖
        if (dependenceSequence.size() != dependencyMap.size()) {
            throw new IllegalStateException("模块依赖异常:循环依赖");
        }
    }

    public static void main(String[] args) {
        DependOn dependOn = PushModuleFactory.class.getAnnotation(DependOn.class);
        System.out.println(dependOn);
    }
}
