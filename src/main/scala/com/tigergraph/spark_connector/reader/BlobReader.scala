package com.tigergraph.spark_connector.reader

import com.tigergraph.spark_connector.utils.Constants._

import org.apache.commons.lang.StringUtils
import org.apache.spark.internal.Logging
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}

import java.util
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer


class BlobReader(val readerName: String, val path: String) extends Reader with Cloneable with Logging with Serializable {

  private val blobConf = new ConcurrentHashMap[String, String]()
  private val files = new ArrayBuffer[String]()

  def this(path: String) = {
    // default parameters
    this("BlobReader", path)
    init(path)
    initBlobConf()
  }

  private def initBlobConf(): Unit = {
    blobConf.put(STORAGE_ACCOUNT_NAME, config.get(STORAGE_ACCOUNT_NAME).asInstanceOf[String])
    blobConf.put(SAS_TOKEN, config.get(SAS_TOKEN).asInstanceOf[String])
    blobConf.put(CONTAINER_NAME, config.get(CONTAINER_NAME).asInstanceOf[String])
    blobConf.put(PARENT_DIRECTORY, config.getOrDefault(PARENT_DIRECTORY, "").asInstanceOf[String])

    // 分隔符问题
    blobConf.put(HAS_HEADER, config.getOrDefault(HAS_HEADER, "true").asInstanceOf[String])
    blobConf.put(DELIMITER, config.getOrDefault(DELIMITER, ",").asInstanceOf[String])

    val tableList: util.List[String] = config.get(BLOB_FILES).asInstanceOf[util.List[String]]
    if (tableList != null) {
      for (elem <- tableList) {
        files += elem
      }
    }
  }

  override def reader(spark: SparkSession): DataFrameReader = {
    checkBlobConf

    val storageAccountName = config.get(STORAGE_ACCOUNT_NAME)
    val containerName = config.get(CONTAINER_NAME)
    val storageAccountAccessKey = config.get(SAS_TOKEN)

    spark.sparkContext.hadoopConfiguration.set("fs.AbstractFileSystem.wasb.impl", "org.apache.hadoop.fs.azure.Wasb")
    spark.sparkContext.hadoopConfiguration.set("fs.AbstractFileSystem.wasbs.impl", "org.apache.hadoop.fs.azure.Wasbs")


    spark.sparkContext.hadoopConfiguration.set("fs.wasb.impl", "org.apache.hadoop.fs.azure.NativeAzureFileSystem")
    spark.sparkContext.hadoopConfiguration.set("fs.wasbs.impl", "org.apache.hadoop.fs.azure.NativeAzureFileSystem")

    // 这里是设置的权限关键
    spark.sparkContext.hadoopConfiguration.set(s"fs.azure.sas.${containerName}.${storageAccountName}.blob.core.windows.net",
      s"${storageAccountAccessKey}")

    val reader: DataFrameReader = spark.read
      .options(blobConf).format("csv")
    reader
  }

  override def getTables(): ArrayBuffer[String] = {
    files
  }

  def getCusOptions(table: String): util.HashMap[String, String] = {
    val conf: util.HashMap[String, String] = new util.HashMap[String, String]()

    val blob2TigerKV = config.get("mappingRules")
      .asInstanceOf[util.HashMap[String, Object]].get(table).asInstanceOf[util.HashMap[String, Object]]

    if(blob2TigerKV == null){
      throw new IllegalArgumentException(s"Please check ${table} exist in [mappingRules].")
    }

    if (blob2TigerKV.containsKey("sourceConfig")) {
      // 包含需要设置的
      val sourceMap = blob2TigerKV.get("sourceConfig").asInstanceOf[util.HashMap[String, Object]]
      if (sourceMap.containsKey("header")) {
        conf.put(HAS_HEADER, sourceMap.get("header").asInstanceOf[String])
      }
      if (sourceMap.containsKey("delimiter")) {
        conf.put(DELIMITER, sourceMap.get("delimiter").asInstanceOf[String])
      }

    }
    return conf
  }

  override def readTable(df: DataFrameReader, table: String): DataFrame = {

    val containerName = blobConf.get(CONTAINER_NAME)
    val storageAccountName = blobConf.get(STORAGE_ACCOUNT_NAME)
    val parentDirectory = blobConf.get(PARENT_DIRECTORY)

    val options: util.HashMap[String, String] = getCusOptions(table)

    val dataframe = df.option("inferSchema", "true")
      .options(options)
      .option("nullValue", "")
      .load(s"wasbs://${containerName}@${storageAccountName}.blob.core.windows.net/${parentDirectory}/${table}")
    // 将配置还原  否则会被一下自定义的配置所取代
    df.options(blobConf)

    return dataframe
  }

  /** Set a configuration variable. */
  def set(key: String, value: String): BlobReader = {
    if (key == null) {
      throw new NullPointerException("empty key,Please check.")
    }
    if (value == null) {
      throw new NullPointerException(s"empty value for ${key},Please check.")
    }

    blobConf.put(key, value)
    this
  }

  def checkBlobConf(): Unit = {
    if (StringUtils.isEmpty(blobConf.get(STORAGE_ACCOUNT_NAME))) {
      throw new NullPointerException(s"Empty blob storage account name [${STORAGE_ACCOUNT_NAME}] in config file.Please check.")
    }
    if (StringUtils.isEmpty(blobConf.get(CONTAINER_NAME))) {
      throw new NullPointerException(s"Empty blob container name [${CONTAINER_NAME}] in config file.Please check.")
    }
  }

}
