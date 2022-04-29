package com.tigergraph.spark_connector.utils;

public class Constants {

    // Blob 相关
    public static final String STORAGE_ACCOUNT_NAME = "storageAccountName";
    public static final String SAS_TOKEN = "sastoken";
    public static final String CONTAINER_NAME = "containerName";
    public static final String PARENT_DIRECTORY = "parentDirectory";
    public static final String BLOB_FILES = "blobFiles";

    //文件信息 是否有文件头、以及分隔符;
    public static final String HAS_HEADER = "header";
    public static final String DELIMITER = "delimiter";


    // 超时类常量
    public static final String TIME_OUT = "timeout";
    public static final String TIME_UNIT = "timeunit";

    public static final Integer PROCESSOR_COUNT = Runtime.getRuntime().availableProcessors();
}
