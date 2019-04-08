package com.stars.modules.name;

import java.util.List;
import java.util.Map;

public class NameManager {

    public static List<String> firstName;

    public static List<String> secondName;

    public static List<String> thirdName;
    public static int maxRenameTime;//重命名次数
    public static int renameCd;//重命名冷却时间
    public static Map<Integer, Integer> costItemMap;//重命名消耗道具
}
