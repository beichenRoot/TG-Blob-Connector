package com.tigergraph.spark_connector.support

import java.util

import com.tigergraph.spark_connector.vo.MappingVO

/**
  * Used to parse different database configuration files
  */
trait Support {
  def getTableInfo(table: String): MappingVO

  def getLoadingJobInfo(table: String): util.HashMap[String, String]
}
