JAVA面试八股文总结：

1.JVM相关

		①类加载的流程，类加载器，例：new一个String对象，它被存在哪?static String呢?final String呢?
	
		②Father father = new Children();这种怎么个加载顺序，可能涉及静态代码块，构造方法，构造代码块等。
	
		③垃圾回收机制，算法，回收器等。哪些可以调优的参数，young/full gc发生时机，为什么需要两个幸存区？只有一个行不行，可达性分析，GCroots？
	
		④强软弱虚引用。
	
		⑤类加载的方式：new，Classloader，class.forName
	
		⑥OutOfMemory的可能

2.多线程相关

		①线程池的参数，原理，发生异常时的执行流程，拒绝策略。
	
		②锁，主要是reentrantlock和synchronized锁，可重入锁，乐观、悲观锁，偏向锁，轻量级锁，重量级锁，公平锁、非公平锁、synchronize锁升级流程，wait和sleep，AQS
	
		③多线程安全，锁，原子类，AtomicReference，ThreadLocal，线程安全的类等。
	
		④Callable与Runnable，execute与submit
	
		⑤并发包，线程状态流转（new，runnable，blocked，waitting，timed-waitting,terminated）
	
		⑥java线程/IO模型，BIO，NIO，IO多路复用，主从reactor。。
	
		⑦线程间通信，进程线程调度算法
	
		⑧ReentrantReadWriteLock来说，其由ReadLock(写锁)和WriteLock(读锁)，其中写锁是独占锁，读锁是共享锁，保证高并发。读写，写读，写写的过程是互斥的。其这些特性可用于缓存机制
	
		⑨定时线程池如何实现的

3.Spring相关

		①Spring启动过程，Spring IOC初始化过程，AOP，循环依赖问题。Bean的注入方式
	
		②Springboot启动原理，主要是SpringFactoriesLoader加载metainfo下的配置文件再加载XXXAutoConfiguration类，把它们对应的Bean加载到IOC容器。
	
		③ApplicationContext和beanFactory的区别，项目里用哪个，JDK与CGLIB动态代理的区别，为什么cglib快于jdk
	
		④spring事务你是怎么用的？加了@Transcational注解spring都做了哪些工作？怎么知道事务执行成功了？
	
		⑤spring task是怎么实现的？
	
		⑥如何自定义实现一个springboot-starter

4.mysql相关

		①Mysql索引的分类特点、失效情况、以及他给了sql，讲述这句sql用没用到索引，如何用索引执行的
	
		②为什么用B+树，其它几种树（普通二叉树，平衡二叉树，红黑树，B树）？
	
		③mysql中的锁，什么时候加什么锁，怎么样会死锁，要知道意向锁和行锁冲突时很可能死锁
	
		④mysql什么时候索引不生效，mysql优化
	
		⑤mysql主从同步有几种方式，为什么延时以及怎么优化。
	
		⑥分库分表方案，不用分片键时查询很慢？要怎么做
	
		⑦事务传播特性，Spring事务，mysql怎么保证事务的ACID，怎样防止大事务，及处理办法
	
		⑧MVCC，一致性视图
	
		⑨查询A in (), MySQL是针对N个值分别查一次索引,还是有更好的操作？

5.网络相关

		①三次握手、四次挥手，为啥挥手有个2倍最大报文时长，握手成功后协商什么数据
	
		②http与tcp，https流程，socket和tcp的区别联系,tcp可靠性如何保证
	
		③session 和cookie，服务端怎么设置cookie。详细描述一下数据从磁盘到网络经历的过程，用户态和内核态。
	
		④浏览器url发送一个请求到controller 接受过程详细描述
	
		⑤rpc调用过程，序列化协议等，netty nio客户端和服务端的编程步骤

6.项目相关

		①介绍，遇到的困难，技术选型、业务难点、技术栈和难点逻辑的实现思路
	
		②线上问题定位

7.消息队列相关

		①延时队列的实现，如何实现延迟队列的监听
	
		②Kafka有一个broker宕机了怎么办
	
		③Zookeeper的Watch机制是怎么实现的
	
		④kafka日志文件目录与topic，partition的关系，kafka日志压缩和删除

8.Redis

	

9.设计类

		①秒杀  ②抢红包 ③论坛发帖盖楼 ④各种中间件，rpc框架等 ⑤即时通讯系统⑥线程安全的某个类（参照某个已有的说）
	
		②配置中心的实时推送怎么做，项目里的限流、降级、熔断怎么做，假如这个限流希望做成可配置的,需要有一个后台管理系统随意对某个api配置全局流量,怎么做？
	
		③头条的文章的评论量非常大,比如说一篇热门文章就有几百万的评论,设计一个后端服务,实现评论的时序展示与分页

10.数据结构

		①并发包用过哪些，CopyOnWriteArrayList，Hashmap，ConcurrentHashmap，跳表，各种树（二叉搜索树，平衡。红黑，B，B+）红黑树左右旋转
	
		②如果mysql的数据都在内存而不是硬盘该用什么数据结构