# spark及hadoop安装

## 1.hadoop安装

首先下载hadoop，目前看来Azure对hadoop3的支持较好，所以建议下载hadoop3。

如果你熟悉hadoop2与Azure的连接，也可以使用hadoop2完成上述工作。

### 下载hadoop

[hadoop官网](https://hadoop.apache.org/)

本文档下载的为版本3.2.2。[version3.2.2](https://archive.apache.org/dist/hadoop/common/hadoop-3.2.2/hadoop-3.2.2.tar.gz)

### 解压及配置

```
mv ~/hadoop-3.2.2.tar.gz /opt && cd /opt

tar -xzvf hadoop-3.2.2.tar.gz

cd hadoop-3.2.2 && mkdir hadoopdata
```

### 修改配置文件

```
# core-site.xml

<property>
<name>fs.defaultFS</name>
<value>hdfs://spark1:9090</value>
</property>
<property>
<name>hadoop.tmp.dir</name>
<value>/opt/hadoop-3.2.2/hadoopdata</value>
</property>


# hdfs-site.xml
<property>
<name>dfs.replication</name>
<value>1</value>
</property>
```

```
# hadoop-env.sh
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

设置JAVA_HOME
$ echo $JAVA_HOME
/usr/lib/jvm/java-8-openjdk-amd64

$ cd /opt/hadoop-3.2.2
$ bin/hadoop namenode -format

2021-07-08 15:22:33,933 INFO common.Storage: Storage directory /opt/hadoop-3.2.2/hadoopdata/dfs/name has been successfully formatted.

看到如上提示，即为成功
```



```
# 测试发现，也可以不启动。只是使用hadoop3.2的一些包完成任务
# 启动 
sbin/start-dfs.sh
```



如果遇到公钥`hadoop : Warning: Permanently added i' (ECDSA) to the list of known hosts.`

该问题，可以检查该用户目录是否有公钥。如果没有，可使用以下命令生成

```
ssh-keygen -t rsa
```







## 2.Spark安装

### 下载Spark
注意：测试下载的是spark-without hadoop的版本。
可能与hadoop版本有关，必须使用3.x版本。尝试过使用以下版本，均未成功
```
 spark-2.4.8-bin-hadoop2.7
 spark-2.4.6-bin-hadoop2.7
 报错信息为：
 Caused by: java.lang.ClassCastException: org.apache.hadoop.fs.FsUrlConnection cannot be cast to java.net.HttpURLConnection
 ```

[Spark官网](http://spark.apache.org/)

[version2.4.8-without-hadoop](https://archive.apache.org/dist/spark/spark-2.4.8/spark-2.4.8-bin-without-hadoop.tgz)

### 解压及配置

```
mv ~/spark-2.4.8-bin-without-hadoop.tgz /opt && cd /opt

tar -xzvf spark-2.4.8-bin-without-hadoop.tgz
```

### 修改配置

```
cd /opt/spark-2.4.8-bin-without-hadoop/
cp conf/slaves.template conf/slaves

cp conf/log4j.properties.template conf/log4j.properties

cp conf/spark-env.sh.template conf/spark-env.sh

vim conf/spark-env.sh

JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
HADOOP_HOME=/opt/hadoop-3.2.2
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export SPARK_DIST_CLASSPATH=$($HADOOP_HOME/bin/hadoop classpath)
export SPARK_MASTER_HOST=hadoop1
```

如果提示log4j相关的包找不到，可以从其他版本中拷贝。



### 启动

```
sbin/start-all.sh
```



## spark-hadoop版本说明

spark采用版本：2.4.8 spark版本需不带hadoop。(因为spark-2.4.*
对应的hadoop均是2.*版本的)

hadoop采用版本: 3.2.2



**如果不断报错，且没有报错信息，executor不断退出。**

**排查思路：**

1.首先查看spark日志。如果spark日志只能看到退出。

2.看不到具体报错信息。可以进入 work目录(spark根目录) 。查看程序具体报错，stderr。

