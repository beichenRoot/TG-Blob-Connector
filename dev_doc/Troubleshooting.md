# Troubleshooting



### 1.关于连接Azure Blob时：`No FileSystem for scheme: wasbs`

解决以上的方法，我采用的方法是将hadoop升级。 因为2.7.7一直报以上的错误。

No FileSystem for scheme: wasbs

```java
spark.sparkContext.hadoopConfiguration.set("fs.wasb.impl", "org.apache.hadoop.fs.azure.NativeAzureFileSystem")
spark.sparkContext.hadoopConfiguration.set("fs.wasbs.impl", "org.apache.hadoop.fs.azure.NativeAzureFileSystem")
```





### 2.关于连接`Class org.apache.hadoop.fs.azure.NativeAzureFileSystem not found`



少了以下两个包

```
azure-storage-8.3.0.jar
hadoop-azure-3.2.2.jar

版本未做测试，但以上两个包可以在hadoop安装目录找到。
find $HADOOP_HOME/ -name "*azure*"
```



### 3.关于连接`ClassNotFoundException: org.eclipse.jetty.util.ajax.JSON$Convertor`

少了以下的包

```
jetty-util-ajax-9.4.20.v20190813.jar

也可以在hadoop安装目录找到
```



### 4.关于连接 `org.apache.hadoop.fs.StreamCapabilities`

主要与以下包有关，版本需要和hadoop版本一致

```xml
<dependency>
    <groupId>org.apache.hadoop</groupId>
    <artifactId>hadoop-common</artifactId>
    <version>3.2.2</version>
</dependency>
```






## hadoop 添加Azure支持

version： hadoop-3.2.2

#### 1.修改core-site.xml

```xml
<property>
  <name>fs.AbstractFileSystem.wasb.impl</name>
  <value>org.apache.hadoop.fs.azure.Wasb</value>
</property>

<property>
  <name>fs.AbstractFileSystem.wasbs.impl</name>
  <value>org.apache.hadoop.fs.azure.Wasbs</value>
</property>


<-->blobstorage为container的名字，solutionblobstorage为account的名字</-->
<-->value为sastoken里面的 sig</-->
<property>
  <name>fs.azure.sas.blobstorage.solutionblobstorage.blob.core.windows.net</name>
  <value>kMG%2BEDzp0Wm%2BNWCcF7KFq63OLZa4lVaag3B6GxvSElM%3D</value>
</property>
```

#### 2.重启hdfs

#### 3.测试

```
# 首先获取两个jar包的位置
find $HADOOP_HOME/ -name "*azure*"

/opt/hadoop-3.2.2/share/hadoop/tools/lib/azure-storage-7.0.0.jar
/opt/hadoop-3.2.2/share/hadoop/tools/lib/hadoop-azure-3.2.2.jar

找到以下两个jar包位置

# 运行
bin/hdfs dfs -libjars /opt/hadoop-3.2.2/jars/hadoop-azure-3.2.2.jar,/opt/hadoop-3.2.2/share/hadoop/tools/lib/azure-storage-7.0.0.jar -ls wasbs://blobstorage@solutionblobstorage.blob.core.windows.net/
```

好像也可以使用打开lib的方式调通。时间原因，未做测试。







spark设置问题

https://www.cnblogs.com/cq-lqj/p/11618541.html





#### 出现包找不到的问题

在idea上可以做如下解决，首先确定是什么包，找到了之后，将jar包拷回idea。之后右键`Add as Libary`





#### 不能既添加maven 又添加Libary，这样会有问题





### 关于错误

`Caused by: java.lang.IllegalAccessError: class org.apache.hadoop.hdfs.web.HftpFileSystem cannot access its superinterface org.apache.hadoop.hdfs.web.TokenAspect$TokenManagementDelegator`

将打包的里面的hadoop-common 去掉即可


`Caused by: java.lang.ClassCastException:
org.apache.hadoop.fs.FsUrlConnection cannot be cast to
java.net.HttpURLConnection`  
以上错误主要发现于spark2.4.6版本中，在2.4.8版本中未发现上述错误。
建议更换spark版本。
