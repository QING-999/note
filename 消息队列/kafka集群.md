# 集群部署

>  kafka默认就是集群模式，只有一个节点也是一个集群，依赖于Zookeeper进行协调，主要通过brokerid进行区分不同节点

![image-20210516220143766](E:\笔记\消息队列\.assets\image-20210516220143766.png)

 





# 跨集群备份解决方案

## MirrorMaker

> 一般情况下，我们会使用一套 Kafka 集群来完成业务，但有些场景确实会需要多套 Kafka 集群同时工作，比如为了便于实现灾难恢复，在两个机房分别部署单独的 Kafka 集群。如果其中一个机房出现故障，能很容易地把流量打到另一个正常运转的机房下。再比如，想为地理相近的客户提供低延时的消息服务，而你的主机房又离客户很远，这时就可以在靠近客户的地方部署一套 Kafka 集群，让这套集群服务你的客户，从而提供低延时的服务。

**通常我们把数据在单个集群下不同节点之间的拷贝称为备份，而把数据在集群间的拷贝称为镜像**（Mirroring）。

从本质上说，MirrorMaker 就是一个消费者 + 生产者的程序。消费者负责从源集群（Source Cluster）消费数据，生产者负责向目标集群（Target Cluster）发送消息。

![image-20210513221820321](E:\笔记\消息队列\.assets\image-20210513221820321.png)

MirrorMaker 连接的源集群和目标集群，会实时同步消息，事实上，很多用户会部署多套集群，用于实现不同的目的。

如图部署了三套集群，左边的源集群负责主要的业务处理；右上角的目标集群可以用于执行数据分析；而右下角的目标集群则充当源集群的热备份。

![image-20210513221924247](E:\笔记\消息队列\.assets\image-20210513221924247.png)

### 使用MirrorMaker

**命令行工具 kafka-mirror-maker 脚本**

常见用法是指定生产者配置文件、消费者配置文件、线程数以及要执行数据镜像的主题正则表达式。

```bash
$ bin/kafka-mirror-maker.sh --consumer.config ./config/consumer.properties --producer.config ./config/producer.properties --num.streams 8 --whitelist ".*"

```

- consumer.config 。MirrorMaker 中消费者的配置文件地址，最主要的配置项是**bootstrap.servers**，也就是该 MirrorMaker 从哪个 Kafka 集群读取消息。因为 MirrorMaker 有可能在内部创建多个消费者实例并使用消费者组机制，因此你还需要设置 group.id 参数。另外，额外配置 auto.offset.reset=earliest，否则的话，MirrorMaker 只会拷贝那些在它启动之后到达源集群的消息。
- producer.config 。MirrorMaker 内部生产者组件的配置文件地址。通常不需要配置太多参数。唯一的例外依然是**bootstrap.servers**，必须显式地指定。
- num.streams 参数。告诉 MirrorMaker 要创建多少个 KafkaConsumer 实例。当然，它使用的是多线程的方案，即在后台创建并启动多个线程，每个线程维护专属的消费者实例。在实际使用时，可以根据你的机器性能酌情设置多个线程。
- whitelist 参数。如命令所示，这个参数接收一个正则表达式。所有匹配该正则表达式的主题都会被自动地执行镜像。在这个命令中，我指定了“.*”，这表明我要同步源集群上的所有主题。



**1.启动两套集群**

**2.启动MirrorMaker工具**

- consumer配置文件

  ```bash
  bootstrap.servers=localhost:9092
  group.id=mirrormaker
  auto.offset.reset=earliest
  ```



- producer配置文件

  ```bash
  bootstrap.servers=localhost:9093
  ```

- 启动工具

  ```bash
  bin/kafka-mirror-maker.sh --producer.config config/producer.properties --consumer.config config/consumer.properties --num.streams 4 --whitelist ".*"
  WARNING: The default partition assignment strategy of the mirror maker will change from 'range' to 'roundrobin' in an upcoming release (so that better load balancing can be achieved). If you prefer to make this switch in advance of that release add the following to the corresponding config: 'partition.assignment.strategy=org.apache.kafka.clients.consumer.RoundRobinAssignor'
  ```

==**需要注意**==

**MirrorMaker 在执行消息镜像的过程中，如果发现要同步的主题在目标集群上不存在的话，它就会根据 Broker 端参数 num.partitions 和 default.replication.factor 的默认值，自动将主题创建出来**。==在0.11.0.0 版本之前，Kafka 不会严格依照 offsets.topic.replication.factor 参数的值。即使设置为 3 但是此时存活的broker数为1，那么分区数则为1（会按照二者中小的一个创建）==

**在实际使用场景中，推荐提前把要同步的所有主题按照源集群上的规格在目标集群上等价地创建出来**。否则，极有可能出现镜像中的分区数和原来集群不一致，这会导致一些很严重的问题。比如原本在某个分区的消息同步到了目标集群以后，却位于其他的分区中。如果消息处理逻辑依赖于这样的分区映射，就必然会出现问题。

除了常规的 Kafka 主题之外，MirrorMaker 默认还会同步内部主题，MirrorMaker 在镜像位移主题时，如果发现目标集群尚未创建该主题，它就会根据 Broker 端参数 offsets.topic.num.partitions 和 offsets.topic.replication.factor 的值来制定该主题的规格。默认配置是 50 个分区，每个分区 3 个副本。



## 其它方案

### Uber 的 uReplicator 工具

https://eng.uber.com/ureplicator-apache-kafka-replicator/



### LinkedIn 开发的 Brooklin Mirror Maker 工具



### Confluent 公司研发的 Replicator 工具









































