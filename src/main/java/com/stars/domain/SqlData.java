package com.stars.domain;

import com.stars.util._HashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * sql查询数据
 * 
 * @author huachp
 */
public class SqlData {
	
	/** 最大结果集数量 */
	private final int maxResult = 1000;
	/** 结果数据 */
	private List<com.stars.domain.RowData> multiRowData = new ArrayList<>();
	
	
	public SqlData(boolean execSqlSuccess) {
		com.stars.domain.RowData rowData = new com.stars.domain.RowData();
		rowData.putValue("result", execSqlSuccess);
		multiRowData.add(rowData);
	}
	
	
	public SqlData(List<com.stars.util._HashMap> data) {
		for (int i = 0; i < getRowCount(data); i++) {
			com.stars.util._HashMap resultData = data.get(i);
			multiRowData.add(new com.stars.domain.RowData(resultData));
		}
	}
	
	
	private int getRowCount(List<_HashMap> data) {
		int rowCount = data.size();
		return rowCount < maxResult ? rowCount : maxResult;
	}
	
	
	public Object getSingleValResult() {
		if (multiRowData.size() > 0) {
			com.stars.domain.RowData rowData = multiRowData.get(0);
			return rowData.get("result");
		}
		return null;
	}
	
	 
	public com.stars.domain.RowData getSingleRowResult() {
		if (multiRowData.size() > 0) {
			return multiRowData.get(0);
		}
		return null;
	}
	

	public List<RowData> getMultiRowResult() {
		return multiRowData;
	}
	
}
