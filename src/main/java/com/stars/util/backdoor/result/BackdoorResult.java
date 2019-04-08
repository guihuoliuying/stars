package com.stars.util.backdoor.result;

import com.stars.util.backdoor.view.IView;

import java.util.*;

public class BackdoorResult {

    public static final int TYPE_ERROR = 0;
    public static final int TYPE_QUIT = 1;
    public static final int TYPE_NULL = 2;

    private static final Set<Integer> resultType = new HashSet<>();

    static {
        resultType.add(TYPE_ERROR);
        resultType.add(TYPE_QUIT);
        resultType.add(TYPE_NULL);
    }

    public static void registerType(int type) {
        if (resultType.contains(type)) {
            throw new IllegalArgumentException("类型已存在");
        }
        resultType.add(type);
    }

	private final int type;
    private final IView view;
    private final List<com.stars.util.backdoor.result.BackdoorRow> rows;
    private final List<BackdoorColumn> cols;
    
    public BackdoorResult(int type, IView view) {
    	this.type = type;
        this.view = view;
        this.rows = new ArrayList<>();
        this.cols = new ArrayList<>();
        if (null == this.view) {
        	this.cols.add(new BackdoorColumn(0));
        } else {
            this.view.setResult(this);
	        for (int i = 0; i < this.view.size(); i++) {
	        	this.cols.add(new BackdoorColumn(i));
	        }
        }
        if (!resultType.contains(type)) {
            throw new IllegalArgumentException("类型不存在");
        }
    }
    
    public BackdoorResult(int type, IView view, List<?> rowsOrColums, Class<?> clazz) {
        this(type, view);
        if (clazz.equals(com.stars.util.backdoor.result.BackdoorRow.class)) {
            constructByRows(rowsOrColums);
        } else if (clazz.equals(BackdoorColumn.class)) {
            constructByColumns(rowsOrColums);
        } else {
        	throw new IllegalArgumentException();
        }
    }
    
    private void constructByRows(List<?> rows) {
        if (true == rows.isEmpty()) {
            return;
        }
        for (Object obj : rows) {
            this.rows.add((com.stars.util.backdoor.result.BackdoorRow) obj);
        }
        // Create columns
        com.stars.util.backdoor.result.BackdoorRow tempRow = this.rows.get(0);
        int colSize = tempRow.size();
        for (int i = 0; i < colSize; i++) {
            this.cols.add(new BackdoorColumn(i));
        }
        // Fill columns
        for (com.stars.util.backdoor.result.BackdoorRow row : this.rows) {
            for (int i = 0; i < colSize; i++) {
                BackdoorColumn col = this.cols.get(i);
                col.addCell(row.getCell(i));
            }
        }
    }
    
    private void constructByColumns(List<?> cols) {
        if (true == cols.isEmpty()) {
            return;
        }
        for (Object obj : cols) {
            this.cols.add((BackdoorColumn) obj);
        }
        // Create rows
        // FIXME: wrong index
        int colSize = cols.size();
        int rowSize = this.cols.get(0).size();
        for (int i = 0; i < rowSize; i++) {
            com.stars.util.backdoor.result.BackdoorRow row = new com.stars.util.backdoor.result.BackdoorRow(i);
            for (int j = 0; j < colSize; j++) {
                BackdoorColumn col = this.cols.get(j);
                row.addCell(col.getCell(i));
            }
        }
    }
    
    public void addRow(com.stars.util.backdoor.result.BackdoorRow row) {
    	if (null == row) {
    		throw new NullPointerException();
    	}
    	this.rows.add(row);
    	for (int i = 0; i < this.cols.size(); i++) {
    		BackdoorColumn cols = this.cols.get(i);
    		BackdoorCell cell = row.getCell(i);
    		cols.addCell(cell);
    	}
    }
    
    public int getType() {
    	return this.type;
    }

    public IView getView() {
        return this.view;
    }
    
    public int sizeOfRow() {
    	return this.rows.size();
    }
    
    public int sizeOfColumn() {
    	return this.cols.size();
    }
    
    public com.stars.util.backdoor.result.BackdoorRow getRow(int index) {
        return this.rows.get(index);
    }
    
    public Iterator<BackdoorRow> rowIterator() {
        return this.rows.iterator();
    }
    
    public BackdoorColumn getColumn(int index) {
        return this.cols.get(index);
    }
    
    public Iterator<BackdoorColumn> columnIterator() {
        return this.cols.iterator();
    }

    public String toStringWithTitle() {
        return null;
    }
    
    public String toStringWithoutTitle() {
        return null;
    }
    
    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }
    
}
