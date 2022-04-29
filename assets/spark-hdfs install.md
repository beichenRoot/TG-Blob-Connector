# Install Spark and Hadoop

## 1. Hadoop installation

First, download Hadoop. It seems that Azure supports Hadoop 3 well, so it is recommended to download Hadoop 3.

If you're familiar with Hadoop2 connecting to Azure, you can also use Hadoop2 to do this.

### Download hadoop

[Hadoop's official website](https://hadoop.apache.org/)

This document was downloaded as version 3.2.2. 

[version3.2.2](https://archive.apache.org/dist/hadoop/common/hadoop-3.2.2/hadoop-3.2.2.tar.gz)

### Unzip and configure

```
mv ~/hadoop-3.2.2.tar.gz /opt && cd /opt

tar -xzvf hadoop-3.2.2.tar.gz

cd hadoop-3.2.2 && mkdir hadoopdata
```

### Modifying configuration files

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

set JAVA_HOME
$ echo $JAVA_HOME
/usr/lib/jvm/java-8-openjdk-amd64

$ cd /opt/hadoop-3.2.2
$ bin/hadoop namenode -format

2021-07-08 15:22:33,933 INFO common.Storage: Storage directory /opt/hadoop-3.2.2/hadoopdata/dfs/name has been successfully formatted.

If you see the above prompt, you are successful
```



```
# You can also not start it.Just use some of the Hadoop 3.2 packages to accomplish the task
# start 
sbin/start-dfs.sh
```



If you encounter a public key`hadoop : Warning: Permanently added i' (ECDSA) to the list of known hosts.`

This problem allows you to check whether the user directory has a public key. If not, it can be generated using the following command

```
ssh-keygen -t rsa
```







## 2. Spark installation

### Download Spark
Note: The test download is the version of Spark -without Hadoop.

May be related to the Hadoop version and must use the 3.x version. The following versions have been tried without success

```
 spark-2.4.8-bin-hadoop2.7
 spark-2.4.6-bin-hadoop2.7
The error message is:
 Caused by: java.lang.ClassCastException: org.apache.hadoop.fs.FsUrlConnection cannot be cast to java.net.HttpURLConnection
```

[Spark official website](http://spark.apache.org/)

[version2.4.8-without-hadoop](https://archive.apache.org/dist/spark/spark-2.4.8/spark-2.4.8-bin-without-hadoop.tgz)

### Unzip and configure

```
mv ~/spark-2.4.8-bin-without-hadoop.tgz /opt && cd /opt

tar -xzvf spark-2.4.8-bin-without-hadoop.tgz
```

### Modifying configuration files

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

If you are prompted that the package associated with log4j cannot be found, you can copy it from another version.

### Start 

```
sbin/start-all.sh
```



## Spark - Hadoop version description

Spark version: 2.4.8/2.4.6 Spark version does not include Hadoop.

(Because Spark 2.4.* corresponds to version 2.* of Hadoop)

hadoop version: 3.2.2



**If an error is repeated and no error message is reported, Executor will continue to exit.**

**Checking ideas:** 

1. Check the Spark log first.If the Spark log only sees exits.

2. Can't see the specific error message. You can go to the work directory (Spark root.  $SPARK_HOME/work).View the program specific error report, stderr.

