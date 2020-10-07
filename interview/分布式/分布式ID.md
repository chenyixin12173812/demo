# 分布式ID

# 1 主流手段



## 1.1数据库自增id，多主模式

如果我们两个数据库组成一个**主从模式**集群，正常情况下可以解决数据库可靠性问题，但是如果主库挂掉后，数据没有及时同步到从库，这个时候会出现ID重复的现象。我们可以使用**双主模式**集群，也就是两个Mysql实例都能单独的生产自增ID，这样能够提高效率，但是如果不经过其他改造的话，这两个Mysql实例很可能会生成同样的ID。需要单独给每个Mysql实例配置不同的起始值和自增步长。

第一台Mysql实例配置：

```sql
set @@auto_increment_offset = 1;     -- 起始值
set @@auto_increment_increment = 2;  -- 步长
```

第二台Mysql实例配置：

```sql
set @@auto_increment_offset = 2;     -- 起始值
set @@auto_increment_increment = 2;  -- 步长
```

经过上面的配置后，这两个Mysql实例生成的id序列如下： mysql1,起始值为1,步长为2,ID生成的序列为：1,3,5,7,9,... mysql2,起始值为2,步长为2,ID生成的序列为：2,4,6,8,10,...

对于这种生成分布式ID的方案，需要单独新增一个生成分布式ID应用，比如DistributIdService，该应用提供一个接口供业务应用获取ID，业务应用需要一个ID时，通过rpc的方式请求DistributIdService，DistributIdService随机去上面的两个Mysql实例中去获取ID。

实行这种方案后，就算其中某一台Mysql实例下线了，也不会影响DistributIdService，DistributIdService仍然可以利用另外一台Mysql来生成ID。

但是这种方案的扩展性不太好，如果两台Mysql实例不够用，需要新增Mysql实例来提高性能时，这时就会比较麻烦。

现在如果要新增一个实例mysql3，要怎么操作呢？ 第一，mysql1、mysql2的步长肯定都要修改为3，而且只能是人工去修改，这是需要时间的。 第二，因为mysql1和mysql2是不停在自增的，对于mysql3的起始值我们可能要定得大一点，以给充分的时间去修改mysql1，mysql2的步长。 第三，在修改步长的时候很可能会出现重复ID，要解决这个问题，可能需要停机才行。

为了解决上面的问题，以及能够进一步提高DistributIdService的性能，如果使用第三种生成分布式ID机制





## 1.2号段模式

我们可以使用号段的方式来获取自增ID，号段可以理解成批量获取，比如DistributIdService从数据库获取ID时，如果能批量获取多个ID并缓存在本地的话，那样将大大提供业务应用获取ID的效率。

比如DistributIdService每次从数据库获取ID时，就获取一个号段，比如(1,1000]，这个范围表示了1000个ID，业务应用在请求DistributIdService提供ID时，DistributIdService只需要在本地从1开始自增并返回即可，而不需要每次都请求数据库，一直到本地自增到1000时，也就是当前号段已经被用完时，才去数据库重新获取下一号段。

所以，我们需要对数据库表进行改动，如下：

```sql
CREATE TABLE id_generator (
  id int(10) NOT NULL,
  current_max_id bigint(20) NOT NULL COMMENT '当前最大id',
  increment_step int(10) NOT NULL COMMENT '号段的长度',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

这个数据库表用来记录自增步长以及当前自增ID的最大值（也就是当前已经被申请的号段的最后一个值），因为自增逻辑被移到DistributIdService中去了，所以数据库不需要这部分逻辑了。

这种方案不再强依赖数据库，就算数据库不可用，那么DistributIdService也能继续支撑一段时间。但是如果DistributIdService重启，会丢失一段ID，导致ID空洞。

为了提高DistributIdService的高可用，需要做一个集群，业务在请求DistributIdService集群获取ID时，会随机的选择某一个DistributIdService节点进行获取，对每一个DistributIdService节点来说，数据库连接的是同一个数据库，那么可能会产生多个DistributIdService节点同时请求数据库获取号段，那么这个时候需要利用乐观锁来进行控制，比如在数据库表中增加一个version字段，在获取号段时使用如下SQL：

```sql
update id_generator set current_max_id=#{newMaxId}, version=version+1 where version = #{version}
```

因为newMaxId是DistributIdService中根据oldMaxId+步长算出来的，只要上面的update更新成功了就表示号段获取成功了。

为了提供数据库层的高可用，需要对数据库使用多主模式进行部署，对于每个数据库来说要保证生成的号段不重复，这就需要利用最开始的思路，再在刚刚的数据库表中增加起始值和步长，比如如果现在是两台Mysql，那么 mysql1将生成号段（1,1001]，自增的时候序列为1，3，4，5，7.... mysql1将生成号段（2,1002]，自增的时候序列为2，4，6，8，10...

更详细的可以参考滴滴开源的TinyId：[github.com/didi/tinyid…](https://github.com/didi/tinyid/wiki/tinyid原理介绍)

在TinyId中还增加了一步来提高效率，在上面的实现中，ID自增的逻辑是在DistributIdService中实现的，而实际上可以把自增的逻辑转移到业务应用本地，这样对于业务应用来说只需要获取号段，每次自增时不再需要请求调用DistributIdService了。



## 1.3 雪花算法

我们可以换个角度来对分布式ID进行思考，只要能让负责生成分布式ID的每台机器在每毫秒内生成不一样的ID就行了。

snowflake是twitter开源的分布式ID生成算法，是一种算法，所以它和上面的三种生成分布式ID机制不太一样，它不依赖数据库。

核心思想是：分布式ID固定是一个long型的数字，一个long型占8个字节，也就是64个bit，原始snowflake算法中对于bit的分配如下图：

![雪花算法](https://img2018.cnblogs.com/blog/1843652/201911/1843652-20191109075949247-616133654.png)

- 第一个bit位是标识部分，在java中由于long的最高位是符号位，正数是0，负数是1，一般生成的ID为正数，所以固定为0。
- 时间戳部分占41bit，这个是毫秒级的时间，一般实现上不会存储当前的时间戳，而是时间戳的差值（当前时间-固定的开始时间），这样可以使产生的ID从更小值开始；41位的时间戳可以使用69年，(1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69年
- 工作机器id占10bit，这里比较灵活，比如，可以使用前5位作为数据中心机房标识，后5位作为单机房机器标识，可以部署1024个节点。
- 序列号部分占12bit，支持同一毫秒内同一个节点可以生成4096个ID

根据这个算法的逻辑，只需要将这个算法用Java语言实现出来，封装为一个工具方法，那么各个业务应用可以直接使用该工具方法来获取分布式ID，只需保证每个业务应用有自己的工作机器id即可，而不需要单独去搭建一个获取分布式ID的应用。

snowflake算法实现起来并不难，提供一个github上用java实现的：[github.com/beyondfengy…](https://github.com/beyondfengyu/SnowFlake)

在大厂里，其实并没有直接使用snowflake，而是进行了改造，因为snowflake算法中最难实践的就是工作机器id，原始的snowflake算法需要人工去为每台机器去指定一个机器id，并配置在某个地方从而让snowflake从此处获取机器id。

但是在大厂里，机器是很多的，人力成本太大且容易出错，所以大厂对snowflake进行了改造。



# 2 实战

 

## 1. 美团（Leaf）分布式ID算法(实战)

`eaf`是美团推出的一个分布式ID生成服务，名字取自德国哲学家、数学家莱布尼茨的一句话：“There are no two identical leaves in the world.”（“世界上没有两片相同的树叶”），取个名字都这么有寓意，美团程序员牛掰啊！

`Leaf`的优势：`高可靠`、`低延迟`、`全局唯一`等特点。

目前主流的分布式ID生成方式，大致都是基于`数据库号段模式`和`雪花算法（snowflake）`，而美团（Leaf）刚好同时兼具了这两种方式，可以根据不同业务场景灵活切换。

接下来结合实战，详细的介绍一下`Leaf`的`Leaf-segment号段模式`和`Leaf-snowflake模式`

## 1.1 Leaf-segment号段模式

`Leaf-segment`号段模式是对直接用`数据库自增ID`充当`分布式ID`的一种优化，减少对数据库的频率操作。相当于从数据库批量的获取自增ID，每次从数据库取出一个号段范围，例如 (1,1000] 代表1000个ID，业务服务将号段在本地生成1~1000的自增ID并加载到内存.。

大致的流程入下图所示：

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WDRNZVJBVmR3aWN1STVqT1hkT3I0VGljb0EweVR4T0xLaGljMVZpY0JXUUpGSGFiOERJbkYxTzYxZGcvNjQw?x-oss-process=image/format,png)

 

号段耗尽之后再去数据库获取新的号段，可以大大的减轻数据库的压力。对`max_id`字段做一次`update`操作，`update max_id= max_id + step`，update成功则说明新号段获取成功，新的号段范围是(`max_id ,max_id +step`]。

由于依赖数据库，我们先设计一下表结构：

```sql
CREATE TABLE `leaf_alloc` (  `biz_tag` varchar(128) NOT NULL DEFAULT '' COMMENT '业务key',  `max_id` bigint(20) NOT NULL DEFAULT '1' COMMENT '当前已经分配了的最大id',  `step` int(11) NOT NULL COMMENT '初始步长，也是动态调整的最小步长',  `description` varchar(256) DEFAULT NULL COMMENT '业务key的描述',  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据库维护的更新时间',  PRIMARY KEY (`biz_tag`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

预先插入一条测试的业务数据

```sql
INSERT INTO `leaf_alloc` (`biz_tag`, `max_id`, `step`, `description`, `update_time`) VALUES ('leaf-segment-test', '0', '10', '测试', '2020-02-28 10:41:03');
```

- `biz_tag`：针对不同业务需求，用biz_tag字段来隔离，如果以后需要扩容时，只需对biz_tag分库分表即可
- `max_id`：当前业务号段的最大值，用于计算下一个号段
- `step`：步长，也就是每次获取ID的数量
- `description`：对于业务的描述，没啥好说的

将Leaf项目下载到本地：`https://github.com/Meituan-Dianping/Leaf`

修改一下项目中的`leaf.properties`文件，添加数据库配置

```bash
leaf.name=com.sankuai.leaf.opensource.testleaf.segment.enable=trueleaf.jdbc.url=jdbc:mysql://127.0.0.1:3306/xin-master?useUnicode=true&characterEncoding=utf8leaf.jdbc.username=junkangleaf.jdbc.password=junkang leaf.snowflake.enable=false
```

> **注意**：`leaf.snowflake.enable` 与 `leaf.segment.enable` 是无法同时开启的，否则项目将无法启动。

配置相当的简单，直接启动`LeafServerApplication`后就OK了，接下来测试一下，`leaf`是基于`Http请求`的发号服务， `LeafController` 中只有两个方法，一个号段接口，一个snowflake接口，`key`就是数据库中预先插入的业务`biz_tag`。

```java
@RestControllerpublic class LeafController {    private Logger logger = LoggerFactory.getLogger(LeafController.class);     @Autowired    private SegmentService segmentService;    @Autowired    private SnowflakeService snowflakeService;     /**     * 号段模式     * @param key     * @return     */    @RequestMapping(value = "/api/segment/get/{key}")    public String getSegmentId(@PathVariable("key") String key) {        return get(key, segmentService.getId(key));    }     /**     * 雪花算法模式     * @param key     * @return     */    @RequestMapping(value = "/api/snowflake/get/{key}")    public String getSnowflakeId(@PathVariable("key") String key) {        return get(key, snowflakeService.getId(key));    }     private String get(@PathVariable("key") String key, Result id) {        Result result;        if (key == null || key.isEmpty()) {            throw new NoKeyException();        }        result = id;        if (result.getStatus().equals(Status.EXCEPTION)) {            throw new LeafServerException(result.toString());        }        return String.valueOf(result.getId());    }}
```

访问：`http://127.0.0.1:8080/api/segment/get/leaf-segment-test`，结果正常返回，感觉没毛病，但当查了一下数据库表中数据时发现了一个问题。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WGh0REdNUWRkV0xmcFAwcmtoYUdIUGJnSHFrR3Q2YU5yOVYxaWJpYWljMDI3MGljU2hSTlA5UjNWZGcvNjQw?x-oss-process=image/format,png)

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WEJCTU1jeVNIVzVwTGc3STFFU0h6cXRBbnVWcE1BSzkwS1FVTW5sTVRrZWNKRG14Z0dKOVBJdy82NDA?x-oss-process=image/format,png)

通常在用号段模式的时候，取号段的时机是在前一个号段消耗完的时候进行的，可刚刚才取了一个ID，数据库中却已经更新了`max_id`，也就是说`leaf`已经多获取了一个号段，这是什么鬼操作？

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WGczU2liZ0M3eUl0V01YSVgwdmxpYWtxTnVYc2licEZGZWVIMDlZMlM3Ym90bEtWOWNMM09xR2ljclEvNjQw?x-oss-process=image/format,png)

### **`Leaf-`**`segment`**为啥要这么设计呢？**

`Leaf` 希望能在DB中取号段的过程中做到无阻塞！

当号段耗尽时再去DB中取下一个号段，如果此时网络发生抖动，或者DB发生慢查询，业务系统拿不到号段，就会导致整个系统的响应时间变慢，对流量巨大的业务，这是不可容忍的。

所以`Leaf`在当前号段消费到某个点时，就异步的把下一个号段加载到内存中。而不需要等到号段用尽的时候才去更新号段。这样做很大程度上的降低了系统的风险。

### **那么****`某个点`到底是什么时候呢？**

这里做了一个实验，号段设置长度为`step=10`，`max_id=1`

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WEx5UlJUNGljc2ljemYzeW9yTFR5bEI5WmhVemJEN0NLSVFaOVJwZnJRc1FOaEkzdU1jdUVPSjhnLzY0MA?x-oss-process=image/format,png)

当我拿第一个ID时，看到号段增加了，1/10 

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WGljOEFuWjJjdEs2QVRjdzZKM0ppY3FySFc1akF0U2Jmd2ljRU0xV2JLaWJOaWMzdE5LQlppYnBzOWZ4US82NDA?x-oss-process=image/format,png)

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WGd6VG9IWUR3ZXpXVmRzRnJ0aHBwMjVnUUJ5Q3RBY2pWdmZRalhpYWFEWExyR2FpYjNlREU3V1dRLzY0MA?x-oss-process=image/format,png)

当我拿第三个Id时，看到号段又增加了，3/10

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WHhJaWFGbHRwVWJiandmajF4Q21QSzF5TzR0WUI5emljc0pUYk5FeEloaWNmWVdIRDFXVlpVZ1ZpYkEvNjQw?x-oss-process=image/format,png)

 

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WEtQemRwT2w1RU4weUx1R21acFpqRUIwaGxVUHFFQXhZak9rMFpBTVVCcUY4V1dadUl2bWZpY2cvNjQw?x-oss-process=image/format,png)

 

`Leaf-segment`采用`双buffer`的方式，它的服务内部有两个号段缓存区`segment`。当前号段已消耗10%时，还没能拿到下一个号段，则会另启一个更新线程去更新下一个号段。

简而言之就是`Leaf`保证了总是会多缓存两个号段，即便哪一时刻数据库挂了，也会保证发号服务可以正常工作一段时间。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WEluT1Y3eXJ5Q2NPd1Q5b1djQmVtUk5WeDE4WDREdkJhcDF2ZWQ3WWRYUVNYSktHMzVERVlSUS82NDA?x-oss-process=image/format,png)

通常推荐号段（`segment`）长度设置为服务高峰期发号QPS的600倍（10分钟），这样即使DB宕机，Leaf仍能持续发号10-20分钟不受影响。

**优点：**

- Leaf服务可以很方便的线性扩展，性能完全能够支撑大多数业务场景。
- 容灾性高：Leaf服务内部有号段缓存，即使DB宕机，短时间内Leaf仍能正常对外提供服务。

**缺点：**

- ID号码不够随机，能够泄露发号数量的信息，不太安全。
- DB宕机会造成整个系统不可用（用到数据库的都有可能）。

## 1.2 Leaf-snowflake

`Leaf-snowflake`基本上就是沿用了snowflake的设计，ID组成结构：`正数位`（占1比特）+ `时间戳`（占41比特）+ `机器ID`（占5比特）+ `机房ID`（占5比特）+ `自增值`（占12比特），总共**64比特**组成的一个Long类型。

**`Leaf-snowflake`不同于原始snowflake算法地方，主要是在workId的生成上，`Leaf-snowflake`依靠`Zookeeper`生成`workId`，也就是上边的`机器ID`（占5比特）+ `机房ID`（占5比特）。`Leaf`中workId是基于ZooKeeper的`顺序Id`来生成的，每个应用在使用Leaf-snowflake时，启动时都会都在Zookeeper中生成一个顺序Id，相当于一台机器对应一个顺序节点，也就是一个workId**。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WDdnZng0VUlFSWo2ckszazNsWXNpYlJjZHQwMGFpYjl4TmpVM2RsWmhPcDhsRnFuN0x5WVJGOHV3LzY0MA?x-oss-process=image/format,png)

 

`Leaf-snowflake`启动服务的过程大致如下：

- 启动Leaf-snowflake服务，连接Zookeeper，在leaf_forever父节点下检查自己是否已经注册过（是否有该顺序子节点）。
- 如果有注册过直接取回自己的workerID（zk顺序节点生成的int类型ID号），启动服务。
- 如果没有注册过，就在该父节点下面创建一个持久顺序节点，创建成功后取回顺序号当做自己的workerID号，启动服务。

但`Leaf-snowflake`对Zookeeper是一种弱依赖关系，除了每次会去ZK拿数据以外，也会在本机文件系统上缓存一个`workerID`文件。一旦ZooKeeper出现问题，恰好机器出现故障需重启时，依然能够保证服务正常启动。

启动`Leaf-snowflake`模式也比较简单，起动本地ZooKeeper，修改一下项目中的`leaf.properties`文件，关闭`leaf.segment模式`，启用`leaf.snowflake`模式即可。

```bash
leaf.segment.enable=false#leaf.jdbc.url=jdbc:mysql://127.0.0.1:3306/xin-master?useUnicode=true&characterEncoding=utf8#leaf.jdbc.username=junkang#leaf.jdbc.password=junkang leaf.snowflake.enable=trueleaf.snowflake.zk.address=127.0.0.1leaf.snowflake.port=2181
    /**     * 雪花算法模式     * @param key     * @return     */    @RequestMapping(value = "/api/snowflake/get/{key}")    public String getSnowflakeId(@PathVariable("key") String key) {        return get(key, snowflakeService.getId(key));    }
```

测试一下，访问：`http://127.0.0.1:8080/api/snowflake/get/leaf-segment-test`

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WG9BYjhrSlJZOU1JOWFiQlFmRloyd0tpY1d5ZGliOG53TEdSZTBla1NReWtsVmFGWjNNTHdKV0pBLzY0MA?x-oss-process=image/format,png)

**优点：**

- ID号码是趋势递增的8byte的64位数字，满足上述数据库存储的主键要求。

**缺点：**

- 依赖ZooKeeper，存在服务不可用风险（实在不知道有啥缺点了）

## 1.3 Leaf监控

请求地址：`http://127.0.0.1:8080/cache`

针对服务自身的监控，Leaf提供了Web层的内存数据映射界面，可以实时看到所有号段的下发状态。比如每个号段双buffer的使用情况，当前ID下发到了哪个位置等信息都可以在Web界面上查看。

![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy8wT3phTDV1VzJhT3BQQkdDNDdkYUIyT0lPSHlzUEo2WHFOVTJLd2JVMExMQzRKRmlhcUlSb0c0amF3YnhpYnk4U1g3ejdmYnVJSUJUNmNaeU9ycDh5YUp3LzY0MA?x-oss-process=image/format,png)

## 2 百度（uid-generator）

github地址：[uid-generator](https://github.com/baidu/uid-generator)

uid-generator使用的就是snowflake，只是在生产机器id，也叫做workId时有所不同。

uid-generator中的workId是由uid-generator自动生成的，并且考虑到了应用部署在docker上的情况，在uid-generator中用户可以自己去定义workId的生成策略，默认提供的策略是：**应用启动时由数据库分配。说的简单一点就是：应用在启动时会往数据库表(uid-generator需要新增一个WORKER_NODE表)中去插入一条数据，数据插入成功后返回的该数据对应的自增唯一id就是该机器的workId，而数据由host，port组成。**

**对于uid-generator中的workId，占用了22个bit位，时间占用了28个bit位，序列化占用了13个bit位，需要注意的是，和原始的snowflake不太一样，时间的单位是秒，而不是毫秒，workId也不一样，同一个应用每重启一次就会消费一个workId**。