# 说明
本文档主要目的是记录Blob连接器的环境搭建。
因为在使用过程中，发现Blob和spark结合起来使用的环境容易出现各种问题。所以希望记录下一份完整的可用的环境搭建方法。

已经记录的一些问题，可以见该文档。 [Troubleshooting](./Troubleshooting.md)

## 安装hadoop(只是使用其中的一些包)
hadoop安装，只用解压就可以。 测试版本3.2.2  
2.7.* 会报有一些包无法找到。

## 安装spark
spark安装可见该文档[spark hdfs 安装](./spark-hdfs安装.md)

主要是版本问题： 下载spark必须下载没有hadoop版本的。 本程序均使用spark2.4.*
做测试，未使用3.*做测试。  
如果下载带hadoop版本的，会出现`Caused by: java.lang.ClassCastException:
org.apache.hadoop.fs.FsUrlConnection cannot be cast to
java.net.HttpURLConnection` 目前未发现解决方案。


```
安装好之后，其余配置与其他一致，只用将spark-env.sh设置以下配置即可
export SPARK_DIST_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
```

hadoop版本为3.2.2，若使用其他版本，可自行尝试。

##  包运行方式

```shell script
# 运行中指定的jars 均是在hadoop安装目录下搜索所得
$ find $HADOOP_HOME -name "*azure*" 
# 找到对应的jar即可

./bin/spark-submit --class com.tigergraph.spark_connector.Blob2Spark2Tg --master spark://$SPARK_IP:7077 --jars /opt/hadoop-3.2.2/share/hadoop/tools/lib/hadoop-azure-3.2.2.jar,/opt/hadoop-3.2.2/share/hadoop/tools/lib/azure-storage-7.0.0.jar,/opt/hadoop-3.2.2/share/hadoop/common/hadoop-common-3.2.2.jar,/opt/hadoop-3.2.2/share/hadoop/hdfs/lib/jetty-util-ajax-9.4.20.v20190813.jar   /opt/spark-2.4.8-bin-without-hadoop/TigerGraphConnector-1.0-SNAPSHOT.jar /opt/spark-2.4.8-bin-without-hadoop/connector-blob.yaml
```
