# 1 内存屏障具体怎么实现的

# 2`volatile` 和 `synchronized` 的区别

# 3 ZGC，CMS，G1 

# 4 Java 内存模型

# 5. 分布式总体限流方案 例 

100w 总流量限流如何在 100 台分布机做总体限流 

# 6 synchronized 

 

http://www.cnblogs.com/paddix/p/5367116.html 

 

# 7. 服务器降级方案 如何指定降级优先级

# 8 查看系统负载

top -H -p





nginx 如何处理连接 

nginx 如何做性能优化 

apache 和 nginx 区别



# 9 NIO 的原理和,连接切换方式

你们公司为什么还用 java8,你对高版本的什么特性觉得好



# 10 分布式事务

# 12 JVM GC 时间长如何发现、后来改成问成接口慢如何排查原因 



# 13 对象如何创建，创建对象还有那些方式

# 14 老年代 GC 和 FullGC 的关系

# 15  如何 JVM 调优，Dump 日志如何分析

# 16 动态加载jar包

# 17 类加载过程



# 18hash map 原理

##  1  HashMap数据结构，put值散列冲突怎么解决？

## 2 链表树化转移数量？为什么是8为什么是6？？

## 3 为什么数组要是二次幂？怎么扩容的？扩容rehash的流程 

## 4 hashmap 如何减少 hash 碰撞（hashcode 和低 16 位做异或运算）

hashmap 链表什么时候转成红黑树

#  





## 19  concurrenthashmap？底层实现是什么？
什么时候变成红黑树？双哈希表？「这些其实讲到了源码层面，initTable，resize，tryPresize，Thread.yield，ForwardingNode



# 20.jdk7 相比 jdk5,6 有哪些新特性？jdk8 相比 jdk7 有哪些新特性？jdk9,10,11,12 有哪些新特 

性？

# 21  outofmemory 如何排查和解决



# 22  java 异常 

 



# 23 synchronized 底层原理，jvm 指令是什么

# 24 AQS 是什么，AQS 的内部实现

# 25 final 的关键字的作用

# 26 线上服务器挂掉怎么处理 

实例下线

# 27 熔断、限流、降级的区别

# 28 知道 CAP 理论吗？说下什么是 CAP? 

# 29 redis 是 CP 还是 AP? 

# 30 CountDownLatch 实现原理

# 31 ThreadLocal用过吗？不remove掉会有什么问题 

# 32 服务怎么保证高可用。如何监控服务是否正常。

如何区分服务挂了和网络原因导致未响应



# 33 .static 块,static 属性，非 static 属性，{},构造函数，autowired，postconstruct 先后顺序







# 34 常见的加密方法？RSA 有什么长度要求嘛？为什么？





# 35 微服务有了 Spring Cloud，为什么还要有 Service Mesh？

# 36 List 不同底层结构的应用场景



GIT 在某些场景下的命令使用方案的优缺点



# 37 动态代理和静态代理的区别



#  38 调试时看到能看到某个变量名的原因



. 

# 39 Java 并发包讲一下，都提供哪些并发能力？ 

 

# 40 用 Java 并发包设计一个生产者消费者队列 

# 41 线程 A BC 顺序执行

 

# 42 java 的异常体系 

 

讲一两个 RuntimeException 和普通异常常见的异常类（这里聊了很久，怎么设计代码里 

的异常，什么时候用 RuntimeException，什么时候使用非授检异常。。。）



# 43 阻塞队列有哪些，相对于普通的队列有哪些特点，

get/put 等待通知是如何实现的



# 44 JVM 调优，什么时候需要调优，调优的结果效果



maven 冲突不能排除怎么解决



# 45 ZK 的选举过程，问得比较细



# 46 枚举类反编译后代码、

# 47 JDK 动态代理 与cglib 

 

# 48 Set 实现类 



# 49 令牌锁/漏桶锁区别 [ ip 1 分钟 限流 100 次, redis ]



# 50 NIO 原理 使用场景

# 51 Tomcat 如何打破双亲委派，具体过程



# 52enum 原理

# 53 孤儿进程和僵尸进程是怎么产生的



 **孤儿进程**是因为父进程异常结束了，然后被1号进程init收养 

 **僵尸进程**是 一个进程使用fork创建子进程，如果子进程退出，而父进程并没有调用wait或waitpid获取子进程的状态信息，那么子进程的进程描述符仍然保存在系统中。这种进程称之为僵死进程 

守护进程是创建守护进程时有意把父进程结束，然后被1号进程init收养

区分：    一个正常运行的子进程，如果此刻子进程退出，父进程没有及时调用wait或waitpid收回子进程的系统资源，该进程就是僵尸进程，如果系统收回了，就是正常退出，如果一个正常运行的子进程，父进程退出了但是子进程还在，该进程此刻是孤儿进程，被init收养，如果父进程是故意被杀掉，子进程做相应处理后就是守护进程
  





# 53 dubbo 调用的容错机制

HashMap 结构 put 扩容；1.7 存在的问题； ConcurrentHashMap 如何控并发 

# 54 Synchronized Lock 本质区别与用法 

# 55sychonized 的偏向锁、锁消除

# 56 垃圾回收器

2.CMS 垃圾回收过程 

3.G1 垃圾回收过程



2.dubbo 的 服务暴露 过程，rpc 

调用 ，restful 调用



 



# 57 本地线程和守护线程的区别，

Thread.setDemon(); 

 任何一个守护线程都是整个JVM中所有非守护线程的保姆：

只要当前JVM实例中尚存在任何一个非守护线程没有结束，守护线程就全部工作；只有当最后一个非守护线程结束时，守护线程随着JVM一同结束工作

 垃圾回收线程就是一个经典的守护线程，当我们的程序中不再有任何运行的Thread,程序就不会再产生垃圾，垃圾回收器也就无事可做，所以当垃圾回收线程是JVM上仅剩的线程时，垃圾回收线程会自动离开 

 如果没有用户线程，都是守护线程，那么JVM结束（随之而来的是所有的一切烟消云散，包括所有的守护线程 

# 58 泛型super和extends的区别。

a.说法2：Java 的泛型，<? super T> 和 <? extends T> 的区别。
 为什么IO是耗时操作



 （24）.Excption与Error区别。


# 59  String为什么是不可变的？（字节跳动） 

# 60  栈里面存了啥 





# 60  ArrayList设置了上限吗？超过了怎么办？ 

# 61 讲一下 SQL 注入的方法

# 62 怎么避免 SQL 注入

预编译

参数校验

# 63 讲一下 ZGC，CMS，G1

# 63 进程和线程的区别

# 64 线程的生命周期

# 65 sleep和wait的区别

# 65 死锁产生的原因

 # 65 JVM中一次完整的GC流程是怎样的，对象如何晋升到老年代？

 

# 66 请描述new一个对象的流程，对象会不会分配到栈中？ 

2. 

# 67 JVM DVM ART的区别 

# 68 手写自旋锁

# 69 .Static class 与non static class 





3.md5 是多少位？（答错了，应该是 128 位）

4.md5 原理知道吗？可逆吗？

5.还知道哪些加密算法？（说了对称加密和非对称加密）

6.哪里用到了非对称加密？

 final 、finally、finalize 区别。 

 ）.PathClassLoader与DexClassLoader的区别是什么 

 sleep 、wait、yield 的区别，wait 的线程如何唤醒它 

1. （Java）程序计数器会发生OutOfMemoryError错误吗？
2. （Java）Java 虚拟机栈会出现哪些错误？