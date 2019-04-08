package com.stars.util.backdoor.result;

public class BackdoorCell {
    
    private final int rowIndex;
    private final int colIndex;
    private final String value;
    
    public BackdoorCell(int rowIndex, int colIndex, String value) {
        this.rowIndex = rowIndex;
        this.colIndex = colIndex;
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean contain(String substr) {
    	return this.value.contains(substr);
    }
    
    public int getRowIndex() {
        return rowIndex;
    }
    
    public int getColumnIndex() {
        return colIndex;
    }
}
