package com.stars.domain;

import com.stars.util.log.CoreLogger;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Created by zhaowenshuo on 2015/1/14.
 */
public class TableManager {
	
	/** 表映射(表ID={表名 + 实体类信息}) */
    private static final Map<Integer, TableTuple> mapping = new HashMap<Integer, TableTuple>();
    
    
    /**
     * 注册数据库表信息映射
     * 
     * @param id	表ID
     * @param name	表名字
     * @param clazz	实体类信息
     */
    public static void register(int id, String name, Class<? extends CacheTable> clazz) 
    		throws Exception {
        if (clazz == null) {
            throw new NullPointerException("class is null");
        }

        checkId(id); // 检查Id是否重复
        checkName(name); // 检查名字是否重复
        checkClass(clazz); // 检查类型是否能够正确转型
        checkTable(clazz); // 检查TableName
        
        checkClassMembers(clazz); // 验证类成员是否合法

        mapping.put(id, new TableTuple(name, clazz));
    }

    private static void checkId(int id) {
        if (TableManager.getClass(id) != null) {
            throw new IllegalArgumentException("id重复: " + id);
        }
    }

    private static void checkName(String name) {
        Objects.requireNonNull(name, "表名不能为空"); // 表名不能为空
        for (TableTuple tuple : mapping.values()) {
            if (name.equals(tuple.tableName)) {
                throw new IllegalArgumentException("名字重复" + name);
            }
        }
    }

    private static void checkClass(Class<? extends CacheTable> clazz) {
        if (!CacheTable.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("class参数必须是CacheTable类型");
        }
    }
    
    
    private static void checkTable(Class<? extends CacheTable> clazz) throws Exception {
		CacheTable cacheTable = clazz.getConstructor(long.class).newInstance(0L);
		String tableName = cacheTable.getTableName();
		if (tableName == null || tableName.trim().equals("")) {
			throw new IllegalArgumentException(clazz.getSimpleName() + ".getTableName()返回不合法");
		} 
    }
    
    
    private static void checkClassMembers(Class<? extends CacheTable> clazz) throws Exception {
    	Field[] fields = clazz.getDeclaredFields();
    	for (Field field : fields) {
    		if (!validField(field)) {
    			throw new IllegalArgumentException(clazz.getSimpleName() + "." + field.getName() + "是非法类型");
    		}
		}
    }
    
    
    
    private static boolean validField(Field field) throws Exception {
    	Class<?> fieldType = field.getType();
    	if (Modifier.isTransient(field.getModifiers())) {
			return true;
		} else if (Map.class.isAssignableFrom(fieldType)) {
			return validGeneric(field);
		} else if (Collection.class.isAssignableFrom(fieldType)) {
			return validGeneric(field);
		} else if (fieldType.isArray()) {
			Class<?> arrayCls = fieldType.getComponentType();
			return validBaseType(arrayCls);
		} else {
			return validBaseType(fieldType);
		}
    }

    
	private static boolean validGeneric(Field field) throws Exception {
		Type genericType = field.getGenericType();		// 获取属性的泛型类型
		if (genericType instanceof ParameterizedType) {	
			ParameterizedType pt = (ParameterizedType) genericType;
			Type[] genericArr = pt.getActualTypeArguments();	// 获取所有的泛型参数
			
			for (Type generic : genericArr) {
				Class<?> genericCls = (Class<?>) generic;
				if (com.stars.domain.CacheRow.class.isAssignableFrom(genericCls)) {		// 泛型是否为CacheRow
					checkDefaultConstructor(genericCls);
					continue;
				} else if (String.class == genericCls) {				// 泛型是否为String
					continue;
				} else if (Number.class.isAssignableFrom(genericCls)) {	// 泛型是否为Number
					continue;
				} else if (Character.class.isAssignableFrom(genericCls)) { // 泛型是否为Character
                    // do nothing
                    continue;
                } else {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	
	private static boolean validBaseType(Class<?> fieldType) throws Exception {
		if (fieldType.isPrimitive()) {
			return true;
		} else if (String.class == fieldType) {	
			return true;
		} else if (com.stars.domain.CacheRow.class.isAssignableFrom(fieldType)) {
			checkDefaultConstructor(fieldType);
			return true;
		} else if (Number.class.isAssignableFrom(fieldType)) {	
			return true;
		} else if (Character.class.isAssignableFrom(fieldType)) {
            return true;
        } else {
			return false;
		}
	}
    
	
	private static void checkDefaultConstructor(Class<?> cls) throws Exception {
		try {
			cls.getDeclaredConstructor();
			TableTuple.tempRowClass = cls;
		} catch (NoSuchMethodException e) {
			String msg = cls.getSimpleName() + "没有默认的构造函数"; 
			e.initCause(new Exception(msg));
			throw e;
		} 
	}
	
	
    
    /**
     * 通过表ID获取表名字
     * 	
     * @param id	表ID
     * @return {@link String}
     */
    public static String getName(int id) {
        TableTuple tuple = mapping.get(id);
        if (tuple != null) {
            return tuple.tableName;
        }
        return null;
    }

    
    /**
     * 通过表ID获取实体类信息
     * 
     * @param id	表ID
     * @return {@link Class}
     */
    public static Class<? extends CacheTable> getClass(int id) {
        TableTuple tuple = mapping.get(id);
        if (tuple != null) {
            return tuple.tableClass;
        }
        return null;
    }
    
    
    /**
     * 获取实体类的Class信息
     * 
     * @param id	表ID
     * @return {@link Class[]}
     */
    public static Class[] getTableAndRowClass(int id) {
    	TableTuple tuple = mapping.get(id);
        if (tuple != null) {
        	Class[] types = new Class<?>[2];
        	types[0] = tuple.tableClass;
        	types[1] = tuple.rowClass;
            return types;
        }
        return null;
    }
    
    
    /**
     * 获取实体类的构造器信息
     * 
     * @param id	表ID
     * @return {@link Constructor}
     */
	public static Constructor getConstructor(int id) {
		TableTuple tuple = mapping.get(id);
		if (tuple != null) {
			return tuple.constructor;
		}
		return null;
	}
    
    
    /**
     * 通过tableId实例化一个table对象
     * 
     * @param id		表ID
     * @param tableKey	实体Table的唯一id
     * @return {@link CacheTable}
     */
    public static CacheTable newTable(int id, long tableKey)  {
    	try {
    		TableTuple tuple = mapping.get(id);
    		if (tuple != null) {
    			return tuple.constructor.newInstance(tableKey);
    		}
			return null;
		} catch (Exception e) {
			com.stars.util.log.CoreLogger.error("", e);
			return null;
		}
    }
    
    
    /**
     * 通过tableName实例化一个table对象
     * 
     * @param tableName 表名
     * @param tableKey	实体Table的唯一id
     * @return {@link CacheTable}
     */
    public static CacheTable newTable(String tableName, long tableKey) {
    	try {
    		TableTuple tuple = getByName(tableName);
    		if (tuple != null) {
    			return tuple.constructor.newInstance(tableKey);
    		}
			return null;
		} catch (Exception e) {
			CoreLogger.error("", e);
			return null;
		}
    }
    
    
    
	static TableTuple getByName(String tableName) {
		for (Map.Entry<Integer, TableTuple> entry 
				: mapping.entrySet()) {
			TableTuple temp = entry.getValue();
			if (temp.tableName.equals(tableName)) {
				return temp;
			}
		}
		return null;
	}
    
	
    
    private static class TableTuple {
    	static Class tempRowClass;
    	
        final String tableName;
        final Class<? extends CacheTable> tableClass;
        final Class<? extends CacheRow> rowClass;
        final Constructor<? extends CacheTable> constructor;
        
        TableTuple(String tableName, Class<? extends CacheTable> tabClass) 
        		throws Exception {
            this.tableName = tableName;
            this.tableClass = tabClass;
            this.rowClass = tempRowClass;
        	this.constructor = tabClass.getConstructor(long.class);
        }
        
    }

}
