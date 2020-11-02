# redis三大集群算法



**一、sentinel哨兵模式介绍**
Sentinel(哨兵)是用于监控redis集群中Master状态的工具，是Redis 的高可用性解决方案，sentinel哨兵模式已经被集成在redis2.4之后的版本中。sentinel是redis高可用的解决方案，sentinel系统可以监视一个或者多个redis master服务，以及这些master服务的所有从服务；当某个master服务下线时，自动将该master下的某个从服务升级为master服务替代已下线的master服务继续处理请求。

sentinel可以让redis实现主从复制，当一个集群中的master失效之后，sentinel可以选举出一个新的master用于自动接替master的工作，集群中的其他redis服务器自动指向新的master同步数据。一般建议sentinel采取奇数台，防止某一台sentinel无法连接到master导致误切换。其结构如下:

![img](https://img2018.cnblogs.com/blog/907596/201903/907596-20190323122922777-731412975.png)

**Redis-Sentinel是Redis官方推荐的高可用性(HA)解决方案**，当用Redis做Master-slave的高可用方案时，假如master宕机了，Redis本身(包括它的很多客户端)都没有实现自动进行主备切换，而Redis-sentinel本身也是一个独立运行的进程，它能监控多个master-slave集群，发现master宕机后能进行自动切换。Sentinel由一个或多个Sentinel 实例 组成的Sentinel 系统可以监视任意多个主服务器，以及这些主服务器属下的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器属下的某个从服务器升级为新的主服务器。



优点：

 redis集群配置了Sentinel 模式，就不会出现数据迁移的情况。 





# 二 集群模式

**一般1000以内**





# 四 hash曹与一致性hash区别

 其实两者区分不大，使用hash槽的好处是可以做到数据分配更均匀，如果有N个节点，每个节点是准确的承担1/N的容量。而一致性hash做不到这点，因为它使用的是hash函数返回的值是随机的。而hash槽类似于我们准确配置每个节点的位置。 无论是hash槽还是一致性hash，**本质上都是通过增加一层来解决依赖性问题。未使用时，key的分配依赖于节点个数，当节点个数变化时，key映射的节点也就改变了。增加了一个稳定层（hash槽），hash槽的个数是固定的，这样key分配到的hash槽也就是固定的。从而实现key与节点个数的解耦。hash槽与节点映射，当增加一个节点时，我们可以自己控制迁移哪些槽到新节点**。 

