package com.tigergraph.spark_connector

import java.util
import java.util.concurrent.{Future => _, _}
import java.util.regex.Pattern

import com.tigergraph.spark_connector.reader.{BlobReader, Reader}
import com.tigergraph.spark_connector.support.{BlobSupport, Support}
import com.tigergraph.spark_connector.utils.Constants._
import com.tigergraph.spark_connector.utils.{LogDaemon, StateStorage}
import com.tigergraph.spark_connector.vo.MappingVO
import com.tigergraph.spark_connector.writer.TigerGraphWriter
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.functions.{col, date_format}
import org.apache.spark.sql.types.{DoubleType, StringType}
import org.apache.spark.sql.{DataFrame, DataFrameReader, SparkSession}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent._
import scala.concurrent.duration._

object Blob2Spark2Tg {

  // default path
  var CONFIG_PATH: String = _
  var config: util.HashMap[String, Object] = _
  val stateStorage: StateStorage = new StateStorage()

  val LOG = Logger.getLogger("Blob2Spark2Tg")
  LOG.setLevel(Level.INFO)

  def filteredAndFormatted(oldDF: DataFrame, mappingVO: MappingVO): DataFrame = {
    val dfFiltered = doFiltered(oldDF, mappingVO);
    return doFormatted(dfFiltered)
  }

  def doFiltered(oldDF: DataFrame, mappingVO: MappingVO): DataFrame = {

    var filteredDF = oldDF
    val filteredStr = mappingVO.getSelectedColumn

    // 需要做两步  如果设置了header为false

    // 如果选择了没有表头，且没有设置表头 则不过滤
    // 有header 并且为false  就检查 有没有oldColumn
    if (mappingVO.getOldColumn != null && !"".equals(mappingVO.getOldColumn)) {
      // 有提供oldColumn 更换header名字
      val strings = mappingVO.getOldColumn.split(",")

      filteredDF = filteredDF.toDF(strings: _*)
    }

    var columns = ArrayBuffer[String]()

    filteredStr.split(",").foreach(column => {
      columns += column
    })

    filteredDF = filteredDF.select(columns.map(filteredDF.col(_)): _*)

    return filteredDF
  }

  def doFormatted(oldDF: DataFrame): DataFrame = {
    var dfMiddle = oldDF

    // select date type
    val dateArray = dfMiddle.schema.filter(schema => {
      schema.dataType.typeName == "date" || schema.dataType.typeName == "timestamp"
    }).map(_.name).toArray


    val notDateArray = dfMiddle.schema.filter(schema => {
      !dateArray.contains(schema.dataType.typeName)
    }).map(_.name).toArray

    // find decimal(*,0) format  convert to long
    val pattern = "(decimal)\\((\\d)*,0\\)"

    // select decimal type
    val intTypeArray = dfMiddle.schema.filter(schema => {
      // int bigint 转换成 long
      val name = schema.dataType.typeName
      // schema.dataType.typeName.contains("decimal(38,0)")
      Pattern.matches(pattern, name)
    }).map(_.name).toArray

    val decimalTypeArray = dfMiddle.schema.filter(schema => {
      // int bigint 转换成 long
      schema.dataType.typeName.contains("decimal")
    }).map(_.name).filter(name => {
      !intTypeArray.contains(name)
    }).toArray

    for (colName <- dateArray) {
      // convert date to string
      dfMiddle = dfMiddle.withColumn(colName, date_format(col(colName), "yyyy/MM/dd HH:mm:ss"))
    }

    for (colName <- intTypeArray) {
      // convert decimal to int
      dfMiddle = dfMiddle.withColumn(colName, col(colName).cast("long"))
    }

    for (colName <- decimalTypeArray) {
      // convert decimal to int
      dfMiddle = dfMiddle.withColumn(colName, col(colName).cast(DoubleType))
    }

    for (colName <- notDateArray) {
      // convert all not date to String
      dfMiddle = dfMiddle.withColumn(colName, col(colName).cast(StringType))
    }


    // fill null date
    dfMiddle = dfMiddle.na.fill("1900/01/01 00:00:00", dateArray).na.fill("")

    dfMiddle
  }

  def writeDF2Tiger(spark: SparkSession, table: String, support: Support)(implicit xc: ExecutionContext) = Future {
    LOG.info(s"[ ${table} ] begin load data")

    val blobReader: Reader = new BlobReader(CONFIG_PATH)
    val dfReader: DataFrameReader = blobReader.reader(spark)
    var df: DataFrame = null
    try {
      df = blobReader.readTable(dfReader, table)
    } catch {
      case e: NullPointerException =>
        e.printStackTrace()
        System.exit(1)
      case e: IllegalArgumentException =>
        e.printStackTrace()
        System.exit(1)
    }

    var jobName = ""
    var columnStr = ""
    var mappingVO: MappingVO = null
    var loadingInfo: util.HashMap[String, String] = null
    try {
      // get jobName and columnStr
      mappingVO = support.getTableInfo(table)

      jobName = mappingVO.getLoadingJobName
      columnStr = mappingVO.getSelectedColumn

      loadingInfo = support.getLoadingJobInfo(table)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }

    val startTime = System.currentTimeMillis()


    try {
      // 判断是否有设置表头。
      // 如果 header为false oldColumn为空 则不选
      val dfFormatted = filteredAndFormatted(df, mappingVO)
      val tgWriter: TigerGraphWriter = new TigerGraphWriter(CONFIG_PATH)
      tgWriter.write(dfFormatted, jobName, columnStr, loadingInfo)
    } catch {
      case e: Exception =>
        // check tg conf illegal
        // not token or username and password
        e.printStackTrace()
        System.exit(-1)
    }

    stateStorage.successOne()
    LOG.info(s"[ ${table} ] load success, consume time: ${(System.currentTimeMillis() - startTime) / 1000} s")

  }

  def checkArgs(args: Array[String]) = {
    if (args == null || args.length == 0) {
      throw new IllegalArgumentException("There is no config path, please input config path with *.yaml.")
    }
  }

  def main(args: Array[String]): Unit = {

    checkArgs(args)

    val startTime = System.currentTimeMillis()

    CONFIG_PATH = args(0)

    val spark = SparkSession.builder()
      .appName(this.getClass.getCanonicalName)
      .getOrCreate()

    spark.sparkContext.setLogLevel("warn")

    // Set number of threads via a configuration property
    val pool = Executors.newFixedThreadPool(PROCESSOR_COUNT.intValue())

    // create the implicit ExecutionContext based on our thread pool
    implicit val xc = ExecutionContext.fromExecutorService(pool)

    val blobReader: Reader = new BlobReader(CONFIG_PATH)
    val blobSupport: Support = new BlobSupport(CONFIG_PATH)
    val tables: ArrayBuffer[String] = blobReader.getTables

    val tgWriter = new TigerGraphWriter(CONFIG_PATH)

    config = tgWriter.config

    val timeOut: Integer = config.getOrDefault(TIME_OUT, new Integer(24)).asInstanceOf[Integer]
    val timeUnit = config.getOrDefault(TIME_UNIT, "HOURS").asInstanceOf[String]

    val tasks: ArrayBuffer[Future[Unit]] = new ArrayBuffer[Future[Unit]]()

    for (table <- tables) {
      val task = writeDF2Tiger(spark, table, blobSupport)
      tasks += task
    }

    stateStorage.setTotalNum(tables.length)

    // add logging task
    val logDaemon = new LogDaemon(stateStorage);
    logDaemon.setDaemon(true);
    logDaemon.start();

    try {
      // await task complete
      Await.result(Future.sequence(tasks), Duration(timeOut.longValue(), TimeUnit.valueOf(timeUnit)))
    } catch {
      case e: Exception =>
        LOG.error("Wait timeout, try batch import")
        stateStorage.failOne()
        stateStorage.setExit(true)
        e.printStackTrace()
    }

    pool.shutdownNow()
    spark.close()
    LOG.info("The total time consuming:" + (System.currentTimeMillis() - startTime) / 1000 + "s")

  }

}
