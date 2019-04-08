package com.stars.domain;

import com.stars.util._HashMap;

import java.util.HashMap;
import java.util.Map;

/**
 * 行数据
 * 
 * @author huachp
 */
public class RowData {
	
	/** 行数据 */
	private Map<String, Object> columnValues = new HashMap<String, Object>();
	
	
	public RowData() {
		
	}
	
	public RowData(_HashMap data) {
		for (Object entryObj : data.entrySet()) {
			Map.Entry entry = (Map.Entry) entryObj;
			String columnName = (String) entry.getKey();
			Object columnVal = entry.getValue();
			String fieldName = columnNameToFieldName(columnName);
			columnValues.put(fieldName, columnVal);
		}
	}
	
	
	private String columnNameToFieldName(String columnName) {
		int fieldNameStart = columnName.indexOf("(");
		int fieldNameEnd = columnName.indexOf(")");
		String fieldName = columnName;
		if (fieldNameStart > -1 && fieldNameEnd > fieldNameStart) {
			fieldName = "result";
		} 
		if (fieldName.indexOf(".") > -1) {
			String[] nameInfo = fieldName.split("\\.");
			return nameInfo[nameInfo.length - 1];
		}
		
		return fieldName;
	}
	
	
	public void putValue(String columnName, Object val) {
		this.columnValues.put(columnName, val);
	}
	
	
	public boolean isNullVal(String key) {
		return columnValues.get(key) == null;
	}
	
	
	public Object get(String key) {
		return columnValues.get(key);
	}
	
	
	public int getInt(String key) {
		Object value = columnValues.get(key);
		return Integer.parseInt(value.toString());
	}
	
	
	public long getLong(String key) {
		Object value = columnValues.get(key);
		return Long.parseLong(value.toString());
	}
	
	
	public String getString(String key) {
		Object value = columnValues.get(key);
		return value.toString();
	}
	
	
	public byte getByte(String key) {
		Object value = columnValues.get(key);
		return Byte.parseByte(value.toString());
	}
	
	
	public short getShort(String key) {
		Object value = columnValues.get(key);
		return Short.parseShort(value.toString());
	}
	
	
	public float getFloat(String key) {
		Object value = columnValues.get(key);
		return Float.parseFloat(value.toString());
	}
	
	
	public double getDouble(String key) {
		Object value = columnValues.get(key);
		return Double.parseDouble(value.toString());
	}
	
	
	public byte[] getBytes(String key) {
		Object value = columnValues.get(key);
		return (byte[]) value;
	}
	
	
	public Map<String, Object> getColumnMap() {
		return columnValues;
	}
	
	
}
