package com.tigergraph.spark_connector.support

import com.tigergraph.spark_connector.vo.MappingVO
import org.apache.spark.internal.Logging
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import java.io.FileInputStream
import java.util

class BlobSupport(val supportName: String, val path: String) extends Support with Cloneable with Logging with Serializable {

  var config: util.HashMap[String, Object] = _

  def init(path: String) = {
    val yaml = new Yaml(new Constructor(classOf[util.HashMap[String, Object]]))
    config = yaml.load(new FileInputStream(path)).asInstanceOf[util.HashMap[String, Object]]
  }

  def this(path: String) = {
    this("blobSupport", path)
    init(path)
  }

  def getTableInfo(table: String): MappingVO = {
    val mappingVO = new MappingVO();

    // 先获取总的header状态
    var header: String = config.getOrDefault("header", "true").asInstanceOf[String]
    var delimiter: String = config.getOrDefault("delimiter", ",").asInstanceOf[String]

    mappingVO.setHeader(header);
    mappingVO.setDelimiter(delimiter);

    val blob2TigerKV = config.get("mappingRules")
      .asInstanceOf[util.HashMap[String, Object]].get(table).asInstanceOf[util.HashMap[String, Object]]

    if (blob2TigerKV.containsKey("sourceConfig")) {
      // 包含需要设置的
      val sourceMap = blob2TigerKV.get("sourceConfig").asInstanceOf[util.HashMap[String, Object]]
      if (sourceMap.containsKey("header")) {
        // 如果这里有header的配置 则以这里为准
        header = sourceMap.get("header").asInstanceOf[String]
        mappingVO.setHeader(header);
      }
      if (sourceMap.containsKey("delimiter")) {
        // 如果这里有header的配置 则以这里为准
        delimiter = sourceMap.get("delimiter").asInstanceOf[String]
        mappingVO.setDelimiter(sourceMap.get("delimiter").asInstanceOf[String]);
      }

      if (sourceMap.containsKey("old_column")) {
        val oldColumn = sourceMap.get("old_column").asInstanceOf[String]
        mappingVO.setOldColumn(oldColumn)
      }

    }

    if (blob2TigerKV.containsKey("jobConfig")) {
      val jobConfig = blob2TigerKV.get("jobConfig").asInstanceOf[util.HashMap[String, Object]]
      if (jobConfig.containsKey("column")) {
        val column = jobConfig.get("column").asInstanceOf[String]
        mappingVO.setSelectedColumn(column)
      }
    }

    val jobName = blob2TigerKV.get("dbtable").toString

    val tigerMap = blob2TigerKV.get("jobConfig").asInstanceOf[util.HashMap[String, Object]]
    val blobColumnStr = tigerMap.get("column").toString

    mappingVO.setTableName(table);
    mappingVO.setSelectedColumn(blobColumnStr);
    mappingVO.setLoadingJobName(jobName);
    return mappingVO;
  }

  def getLoadingJobInfo(table: String): util.HashMap[String, String] = {
    val blob2TigerKV = config.get("mappingRules")
      .asInstanceOf[util.HashMap[String, Object]].get(table).asInstanceOf[util.HashMap[String, Object]]

    val tigerMap = blob2TigerKV.get("jobConfig").asInstanceOf[util.HashMap[String, Object]]
    val filename = tigerMap.get("filename").asInstanceOf[String]

    val map: util.HashMap[String, String] = new util.HashMap[String, String]()

    map.put("filename", filename)

    return map
  }

}
