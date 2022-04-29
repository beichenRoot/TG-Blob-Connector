package com.tigergraph.spark_connector.vo;

public class MappingVO {
    private String tableName;
    private String loadingJobName;

    private String header;
    private String delimiter;
    private String oldColumn;
    private String selectedColumn;

    public MappingVO() {
    }

    public MappingVO(String header, String delimiter, String oldColumn, String selectedColumn) {
        this.header = header;
        this.delimiter = delimiter;
        this.oldColumn = oldColumn;
        this.selectedColumn = selectedColumn;
    }

    public MappingVO(String tableName, String loadingJobName, String header, String delimiter, String oldColumn, String selectedColumn) {
        this.tableName = tableName;
        this.loadingJobName = loadingJobName;
        this.header = header;
        this.delimiter = delimiter;
        this.oldColumn = oldColumn;
        this.selectedColumn = selectedColumn;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getOldColumn() {
        return oldColumn;
    }

    public void setOldColumn(String oldColumn) {
        this.oldColumn = oldColumn;
    }

    public String getSelectedColumn() {
        return selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getLoadingJobName() {
        return loadingJobName;
    }

    public void setLoadingJobName(String loadingJobName) {
        this.loadingJobName = loadingJobName;
    }
}
