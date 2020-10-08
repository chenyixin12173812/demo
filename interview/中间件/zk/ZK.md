# 1 zookeeper调优

1.将集群中的leader设置不接受客户端连接，让它专注于集群的通信、选举等操作

  设置方式:

  在zoo.cfg中增加

​    leaderServes=no

2.在大型的生产系统中，zookeeper机器会很多，因为选举的过半原则，导致每一次选举都需要大量的网络通信，如果并发高，请求多，那么性能会降低很多，为此zookeeper添加了观察者observer，它不参与选举，但是可以接受客户端的连接。

  因为观察者不参与选举，因此观察者挂了的话，并不会影响整个集群的正常运行。

  配置观察者方式：

- 在观察者机器上的zoo.cfg中添加peerType=observer
- 在集群每台机器的zoo.cfg中对应观察者机器的server.x=192.168.123.102:2888:2888后面添加":observer"

  重启所有机器

  登录观察者机器，执行./zkServer.sh status可以看到mode:observer字样，其他是follower或者leader



https://www.cnblogs.com/EasonJim/p/7488834.html

# 2.ZK 缺点



1. zookeeper 的 snapshot（即数据量）不建议超过 1G，否则容易出现选主失败，导致整个集群不可用（ZAB synchronize phrase）；
2. zookeeper 不保证“跨session” 一致性，比如session-A写入数据，session-B未必能读到；或者说，我改了某个节点，这时候网络抖了，导致 session 超时；重建新 session 可能无法读到之前的数据；
3. error handling：因网络故障，无法连上zk-server，是收不到 SESSION_EXPIRED 事件的；因此，需要在对client的各个状态，维护对应计时器，以便争取step down；
4. watch 是一次性的；
5. 先 reset watch，再 read；
6. session timeout 设置多长？有时候，真正的 session timeout 并不是你设置的值，需要在连上 zk-server 后动态获取；
7. 部署上，没法做到动态加 server 进来；
8. JAVA Client 异常不友好，需要 handle 各种情况，容易才坑；忘了 close，会连接泄露；
9. 无法跨地域部署（Observer 模式并没有那么好），有跨地域需求的伙伴，表示很想哭；
10. watch 的目录不合理，很容易出现羊群效应，必须紧跟文档的脚步~千万不能想当然

PS。 

对zk最大的误会，也许在于认为它是一个具有线性一致性的系统；然而并不是，其一致性分为两部分：

1. 同一session：顺序一致性，保证版本不会退；
2. 跨session：最终一致性，你最终会读到新数据的；

3 [Paxos,*Raft*,*Zab*](https://www.baidu.com/link?url=F5vFf6ddd6t6b5d4omVLWteOUc66alPDMjCV4q6Zz1SLoKyTx9GikrSW9UYTRsEvkjVgAjFk4YiChMiFwkx_Za&wd=&eqid=9ee050ff0010f538000000025e939fbd)

# 3脏读

 最终一致性：读数据时，有可能会脏读。比较推荐watch的方式，实现数据的及时生效。

  如果一个zk集群有10000台节点，当进行写入的时候，如果已经有6K个节点写入成功，zk就认为本次写请求成功。但是这时候如果一个客户端读取的刚好是另外4K个节点的数据，那么读取到的就是旧的过期数据 

zk是 leader -follower机制，所有的写操作是leader广播通知到所有follower，有一半确认即可。那么广播肯定是不可靠的，万一有的follower没有操作本地数据，所有打到这台follower的请求读到的不是脏数据了吗？

1、zk保证的是顺序一致性，短时间是会有脏读的产生。leader会为每一个follower创建一个广播队列，保证消息的顺序性。folower端：在下一个消息到来时，必已经顺序操作之前的消息了。

2、如果一个客户端将Znode z的值更新为a，在之后的操作中，它又将z的值更新为b，则没有客户端能够在看到z的值是b之后再看到值a；因为客户端也会保存一个它见过的最大的 zxid，如果读取的时候，如果客户端发现 本地 zxid 比 server 端的最大 zxid 大，则拒绝，client 会自动重连到其他server。所以client可能会读到脏数据，但不会读到实时数据后，还会再读到脏数据。

3、如果是广播同步数据的过程中，集群崩溃了。集群会进入投票状态，会通过投票机制选出一个commit最高（数据最新）的zk节点，然后其他follower 都会同步数据到最高commit（数据最新）。

 **如果客户端A和客户端B读取相同的值很重要，则客户端B应该在执行读取之前从ZooKeeper API方法调用sync()方法** 

# 4 curator

### 

面试题  https://segmentfault.com/a/1190000014479433 

# 5 ZK选举过程



当leader崩溃或者leader失去大多数的follower，这时候zk进入恢复模式，恢复模式需要重新选举出一个新的leader，让所有的Server都恢复到一个正确的状态。Zk的选举算法使用ZAB协议：

1. 选举线程由当前Server发起选举的线程担任，其主要功能是对投票结果进行统计，并选出推荐的Server；
2. 选举线程首先向所有Server发起一次询问(包括自己)；
3. 选举线程收到回复后，验证是否是自己发起的询问(验证zxid是否一致)，然后获取对方的id(myid)，并存储到当前询问对象列表中，最后获取对方提议的leader相关信息(id,zxid)，并将这些信息存储到当次选举的投票记录表中；
4. 收到所有Server回复以后，就计算出zxid最大的那个Server，并将这个Server相关信息设置成下一次要投票的Server；
5. 线程将当前zxid最大的Server设置为当前Server要推荐的Leader，如果此时获胜的Server获得n/2 + 1的Server票数， 设置当前推荐的leader为获胜的Server，将根据获胜的Server相关信息设置自己的状态，否则，继续这个过程，直到leader被选举出来。

通过流程分析我们可以得出：要使Leader获得多数Server的支持，则Server总数最好是奇数2n+1，且存活的Server的数目不得少于n+1

# 6 master/slave之间通信



Storm：定期扫描 
PtBalancer：节点监听

# 7 节点变多时，PtBalancer速度变慢



类似问题：根据Netflix的Curator作者所说，ZooKeeper真心不适合做Queue，或者说ZK没有实现一个好的Queue，详细内容可以看https://cwiki.apache.org/confluence/display/CURATOR/TN4， 
原因有五：

1. ZK有1MB 的传输限制。 实践中ZNode必须相对较小，而队列包含成千上万的消息，非常的大。 
2. 如果有很多节点，ZK启动时相当的慢。 而使用queue会导致好多ZNode. 你需要显著增大 initLimit 和 syncLimit. 
3. ZNode很大的时候很难清理。Netflix不得不创建了一个专门的程序做这事。 
4. 当很大量的包含成千上万的子节点的ZNode时， ZK的性能变得不好 
5. ZK的数据库完全放在内存中。 大量的Queue意味着会占用很多的内存空间。 

尽管如此， Curator还是创建了各种Queue的实现。 如果Queue的数据量不太多，数据量不太大的情况下，酌情考虑，还是可以使用的。

# 8 客户端对ServerList的轮询机制是什么

随机，客户端在初始化( new ZooKeeper(String connectString, int sessionTimeout, Watcher watcher) )的过程中，将所有Server保存在一个List中，然后随机打散，形成一个环。之后从0号位开始一个一个使用。 
两个注意点：

1. Server地址能够重复配置，这样能够弥补客户端无法设置Server权重的缺陷，但是也会加大风险。（比如: 192.168.1.1:2181,192.168.1.1:2181,192.168.1.2:2181).
2. 如果客户端在进行Server切换过程中耗时过长，那么将会收到SESSION_EXPIRED. 这也是上面第1点中的加大风险之处。

# 9 客户端如何正确处理CONNECTIONLOSS(连接断开) 和 SESSIONEXPIRED(Session 过期)两类连接异常

在ZooKeeper中，服务器和客户端之间维持的是一个长连接，在 SESSION_TIMEOUT 时间内，服务器会确定客户端是否正常连接(客户端会定时向服务器发送heart_beat),服务器重置下次SESSION_TIMEOUT时间。因此，在正常情况下，Session一直有效，并且zk集群所有机器上都保存这个Session信息。在出现问题情况下，客户端与服务器之间连接断了（客户端所连接的那台zk机器挂了，或是其它原因的网络闪断），这个时候客户端会主动在地址列表（初始化的时候传入构造方法的那个参数connectString）中选择新的地址进行连接。

好了，上面基本就是服务器与客户端之间维持长连接的过程了。在这个过程中，用户可能会看到两类异常CONNECTIONLOSS(连接断开) 和SESSIONEXPIRED(Session 过期)。

CONNECTIONLOSS发生在上面红色文字部分，应用在进行操作A时，发生了CONNECTIONLOSS，此时用户不需要关心我的会话是否可用，应用所要做的就是等待客户端帮我们自动连接上新的zk机器，一旦成功连接上新的zk机器后，确认刚刚的操作A是否执行成功了。

# 10 一个客户端修改了某个节点的数据，其它客户端能够马上获取到这个最新数据吗

ZooKeeper不能确保任何客户端能够获取（即Read Request）到一样的数据，除非客户端自己要求：方法是客户端在获取数据之前调用org.apache.zookeeper.AsyncCallback.VoidCallback, java.lang.Object) sync. 
通常情况下（这里所说的通常情况满足：1. 对获取的数据是否是最新版本不敏感，2. 一个客户端修改了数据，其它客户端是否需要立即能够获取最新），可以不关心这点。 
在其它情况下，最清晰的场景是这样：ZK客户端A对 /my_test 的内容从 v1->v2, 但是ZK客户端B对 /my_test 的内容获取，依然得到的是 v1. 请注意，这个是实际存在的现象，当然延时很短。解决的方法是客户端B先调用 sync(), 再调用 getData().

# 11 ZK为什么不提供一个永久性的Watcher注册机制

不支持用持久Watcher的原因很简单，ZK无法保证性能。 
使用watch需要注意的几点

1. Watches通知是一次性的，必须重复注册.
2. 发生CONNECTIONLOSS之后，只要在session_timeout之内再次连接上（即不发生SESSIONEXPIRED），那么这个连接注册的watches依然在。
3. 节点数据的版本变化会触发NodeDataChanged，注意，这里特意说明了是版本变化。存在这样的情况，只要成功执行了setData()方法，无论内容是否和之前一致，都会触发NodeDataChanged。
4. 对某个节点注册了watch，但是节点被删除了，那么注册在这个节点上的watches都会被移除。
5. 同一个zk客户端对某一个节点注册相同的watch，只会收到一次通知。
6. Watcher对象只会保存在客户端，不会传递到服务端。

# 12 我能否收到每次节点变化的通知

如果节点数据的更新频率很高的话，不能。 
原因在于：当一次数据修改，通知客户端，客户端再次注册watch，在这个过程中，可能数据已经发生了许多次数据修改，因此，千万不要做这样的测试：”数据被修改了n次，一定会收到n次通知”来测试server是否正常工作。（我曾经就做过这样的傻事，发现Server一直工作不正常？其实不是）。即使你使用了GitHub上这个客户端也一样。

# 13 能为临时节点创建子节点吗

不能。 

# 14 是否可以拒绝单个IP对ZK的访问,操作

ZK本身不提供这样的功能，它仅仅提供了对单个IP的连接数的限制。你可以通过修改iptables来实现对单个ip的限制，当然，你也可以通过这样的方式来解决。https://issues.apache.org/jira/browse/ZOOKEEPER-1320

# 15 在getChildren(String path, boolean watch)是注册了对节点子节点的变化，那么子节点的子节点变化能通知吗

不能

# 16 创建的临时节点什么时候会被删除，是连接一断就删除吗？延时是多少？

连接断了之后，ZK不会马上移除临时数据，只有当SESSIONEXPIRED之后，才会把这个会话建立的临时数据移除。因此，用户需要谨慎设置Session_TimeOut

# 17 zookeeper是否支持动态进行机器扩容？如果目前不支持，那么要如何扩容呢？

截止3.4.3版本的zookeeper，还不支持这个功能，在3.5.0版本开始，支持动态加机器了，期待下吧: https://issues.apache.org/jira/browse/ZOOKEEPER-107

# 18 ZooKeeper集群中服务器之间是怎样通信的？

Leader服务器会和每一个Follower/Observer服务器都建立TCP连接，同时为每个F/O都创建一个叫做LearnerHandler的实体。LearnerHandler主要负责Leader和F/O之间的网络通讯，包括数据同步，请求转发和Proposal提议的投票等。Leader服务器保存了所有F/O的LearnerHandler。

# 19 zookeeper是否会自动进行日志清理？如何进行日志清理？

zk自己不会进行日志清理，需要运维人员进行日志清理

# 20zookeeper是如何保证事务的顺序一致性的

zookeeper采用了递增的事务Id来标识，所有的proposal都在被提出的时候加上了zxid，zxid实际上是一个64位的数字，**高32位是epoch用来标识leader是否发生改变，如果有新的leader产生出来，epoch会自增**，低32位用来递增计数。当新产生proposal的时候，会依据数据库的两阶段过程，首先会向其他的server发出事务执行请求，如果超过半数的机器都能执行并且能够成功，那么就会开始执行



# 21Onserver 跨数据中心处理

![1602172906098](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1602172906098.png)

# 22 动态修改 ZK节点配置

## 1停机重新配置的问题：

会覆盖以前的提交

## 2 如何动态配置，dynamicConfigFile

![1602173359182](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1602173359182.png)

# 23 哪些日志文件

日志文件

快照文件

2个epoch（集群才有）

![1602174677381](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1602174677381.png)