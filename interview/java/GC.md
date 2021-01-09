# 1.调优参数





# CMS收集器和G1收集器 他们的优缺点对比 最后并发清除 CMS 不需要停顿，G1需要停顿...



# 4 堆外内存泄漏



# 5 cpu突然暴增

 # 6 小内存避免使用CMS



# 7 小内存为什么不建议使用CMS

**heap size<=3G的情况下完全不要考虑CMS GC，在heap size>3G的情况下也优先选择ParallelOldGC，而不是CMS GC，只有在暂停时间无法接受的情况下才考虑CMS GC**（不过当然，**一般来说在heap size>8G后基本上都得选择CMS GC，否则那暂停时间是相当吓人的，除非是完全不在乎响应时间的应用**），这其实也是官方的建议（每年JavaOne的GC Tuning基本都会这么讲）。

 <=3G的情况下完全不要考虑CMS GC
  1、触发比率不好设置 在JDK 1.6的版本中CMS GC的触发比率默认为old使用到92%时，假设3G的heap size，那么意味着旧生代大概就在1.5G–2.5G左右的大小，假设是92%触发，那么意味着这个时候旧生代只剩120M–200M的大小，通常这点大小很有可能是会导致不够装下新生代晋生的对象，因此需要调整触发比率，但由于heap size比较小，这个时候到底设置为多少是挺难设置的，例如我看过heap size只有1.5G，old才800m的情况下，还使用CMS GC的，触发比率还是80%，这种情况下就悲催了，意味着旧生代只要使用到640m就触发CMS GC，只要应用里稍微把一些东西cache了就会造成频繁的CMS GC。 CMS GC是一个大部分时间不暂停应用的GC，就造成了需要给CMS GC留出一定的时间（因为大部分时间不暂停应用，这也意味着整个CMS GC过程的完成时间是会比ParallelOldGC时的一次Full GC长的），以便它在进行回收时内存别分配满了，而heap size本来就小的情况下，留多了嘛容易造成频繁的CMS GC，留少了嘛会造成CMS GC还在进行时内存就不够用了，而在不够用的情况下CMS GC会退化为采用Serial Full GC来完成回收动作，这个时候就慢的离谱了。

  2、抢占CPU CMS GC大部分时间和应用是并发的，所以会抢占应用的CPU，通常在CMS GC较频繁的情况下，可以很明显看到一个CPU会消耗的非常厉害。

  3、YGC速度变慢 由于CMS GC的实现原理，导致对象从新生代晋升到旧生代时，寻找哪里能放下的这个步骤比ParallelOld GC是慢一些的，因此就导致了YGC速度会有一定程度的下降。

  4、碎片问题带来的严重后果 CMS GC最麻烦的问题在于碎片问题，同样是由于实现原理造成的，CMS GC为了确保尽可能少的暂停应用，取消了在回收对象所占的内存空间后Compact的过程，因此就造成了在回收对象后整个old区会形成各种各样的不连续空间，自然也就产生了很多的碎片，碎片会造成什么后果呢，会造成例如明明旧生代还有4G的空余空间，而新生代就算全部是存活的1.5g对象，也还是会出现promotion failed的现象，而在出现这个现象的情况下CMS GC多数会采用Serial Full GC来解决问题。 碎片问题最麻烦的是你完全不知道它什么时候会出现，因此有可能会造成某天高峰期的时候应用突然来了个长暂停，于是就悲催了，对于很多采用了类似心跳来维持长连接或状态的分布式场景而言这都是灾难，这也是Azul的Zing JVM相比而言最大的优势（可实现不暂停的情况下完成Compact，解决碎片问题）。 目前对于这样的现象我们唯一的解决办法都是选择在低峰期主动触发Full GC（执行jmap -histo:live [pid]）来避免碎片问题，但这显然是一个很龌蹉的办法（因为同样会对心跳或维持状态的分布式场景造成影响）。

  5、CMS GC的”不稳定“性 如果关注过我在之前的blog记录的碰到的各种Java问题的文章（可在此查看），就会发现碰到过很多各种CMS GC的诡异问题，尽管里面碰到的大部分BUG目前均已在新版本的JVM修复，但谁也不知道是不是还有问题，毕竟CMS GC的实现是非常复杂的（因为要在尽可能降低应用暂停时间的情况下还保持对象引用的扫描不要出问题），而ParallelOldGC的实现相对是更简单很多的，因此稳定性相对高多了。
  而且另外一个不太好的消息是JVM Team的精力都已转向G1GC和其他的一些方面，CMS GC的投入已经很少了（这也正常，毕竟G1GC确实是方向）。

  在大内存的情况下，CMS GC绝对是不二的选择，而且Java在面对内存越来越大的情况下，必须采用这种大部分时候不暂停应用的方式，否则Java以后就非常悲催了，G1GC在CMS GC的基础上，有了很多的进步，尤其是会做部分的Compact，但仍然碎片问题还是存在的，哎…

  Java现在在大内存的情况下还面临的另外两个大挑战：

1. 分析内存的堆栈太麻烦，例如如果在大内存的情况下出现OOM，那简直就是杯具，想想dump出一个几十G的文件，然后还要分析，这得多长的时间呀，真心希望JDK在这方面能有更好的工具…
2. 对象结构不够紧凑，导致在内存空间有很高要求的场景Java劣势明显，不过这也是新版本JDK会重点优化的地方。
   至于在cpu cache miss等控制力度上不如C之类的语言，那是更没办法的，相比带来的开发效率提升，也只能认了，毕竟现在多数场景都是工程性质和大规模人员的场景，因此开发效率、可维护性会更重要很多。

# 8 各大GC对比

| **收集器名称**      | **优点**                                                     | **缺点**                                                     | 备注                                                         |
| :------------------ | :----------------------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| Serial/Serial Old   | 1、所有收集器中内存消耗最小的2、相比于其他收集器的单个线程开说，更简单高效 | 1、单线程工作收集器，垃圾收集时必须暂停其他所有工作线程，且暂停时间不可控 | Serial收集器对于运行在客户端模式下、微小型服务或单线程服务的虚拟机来说是一个很好的选择。 |
| ParNew              | 1、是Serial的多线程并发版本2、目前除了Serial只有它可以与CMS垃圾收集器配合使用 | 1、单核环境下不会比Serial更好2、默认开启的收集线程数与处理器核心数相同 |                                                              |
| Parallel Scavenge   | 1、Parallel Scavenge收集器的特点是它的关注点与其他收集器不同，CMS等收集器的关注点是尽可能地缩短垃圾收集时用户线程的停顿时间，而Parallel Scavenge收集器的目标则是达到一个可控制的吞吐量（处理器用于运行用户代码的时间与处理器总消耗时间的比值：运行用户代码时间/运行用户代码时间+运行垃圾收集时间）。2、可以精确配置吞吐量，或自动调整吞吐量（自适应调节策略） | 1、可能造成竞争时间片时间增加，抑或垃圾回收不全面等问题，导致程序运行过程中付出的整体 GC 时间较长。 | 主要适合在后台运算而不需要太多交互的分析任务。               |
| Parallel Old        | 1、Parallel Scavenge的老年代版本，可以与Parallel Scavenge组成吞吐量优先的垃圾收集组合 |                                                              | 在注重吞吐量或者处理器资源较为稀缺的场合，都可以优先考虑Parallel Scavenge加Parallel Old收集器这个组合。 |
| CMS                 | 1、并发收集2、低停顿                                         | 1、对处理器资源敏感，降低吞吐量，当处理器数少于四个时，影响会很大。2、无法处理浮动垃圾，有可能引发Full GC3、采用标记清楚算法，会产生大量碎片空间 | 目前很大一部分的Java应用集中在互联网网站或者基于浏览器的B/S系统的服务端上，这类应用通常都会较为关注服务的响应速度，希望系统停顿时间尽可能短，以给用户带来良好的交互体验。CMS收集器就非常符合这类应用的需求。 |
| G1（Garbage First） | 1、开创了收集器面向局部收集的设计思路和基于Region的内存布局形式2、不再局限于分代收集：衡量标准不再是它属于哪个分代，而是哪块内存中存放的垃圾数量最多，回收收益最大，这就是G1收集器的Mixed GC模式。3、可以配置允许的收集停顿时间4、优先处理回收价值大的Region | 1、内存占用和额外执行负载都比CMS要高                         | 1、主要面向服务器端的垃圾收集器2、目前在小内存应用上CMS的表现大概率仍然要会优于G1，而在大内存应用上G1则大多能发挥其优势， |
| Shenandoah          | 1、支持并发整理算法，可以与用户线程并发2、默认不使用分代3、Shenandoah摒弃了在G1中耗费大量内存和计算资源去维护的记忆集，改用名为“连接矩阵”（Connection Matrix）的全局数据结构来记录跨Region的引用关系，降低了处理跨代指针时的记忆集维护消耗，也降低了伪共享问题的发生概率。 | 1、不由oracle开发和维护                                      |                                                              |
| ZGC（JDK11）        | 1、和Shenandoah高度相似且由Oracle研发2、在任意堆内存大小下都可以把垃圾收集的停顿时间限制在十毫秒以内的低延迟。3、使用了读屏障、染色指针和内存多重映射等技术实现可并发的标记整理算法 |                                                              |                                                              |
| Epsilon             | 1、自动内存管理子系统                                        | 1、不做任何回收行为                                          | 如果应用只要运行数分钟甚至数秒，只要Java虚拟机能正确分配内存，在堆耗尽之前就会退出，那显然运行负载极小、没有任何回收行为的Epsilon便是很恰当的选择。 |



# 9 oom问题

## 9.1 什么情况会产生OOM

**1 堆溢出**

一般的排查方式可以通过设置-XX: +HeapDumpOnOutOfMemoryError在发生异常时dump出当前的内存转储快照来分析，分析可以使用Eclipse Memory Analyzer(MAT)来分析，独立文件可以在[官网](https://www.eclipse.org/mat/downloads.php)下载。

另外如果使用的是IDEA的话，可以使用商业版JProfiler或者开源版本的JVM-Profiler，此外IDEA2018版本之后内置了分析工具，包括Flame Graph(火焰图)和Call Tree(调用树)功能。

**2 方法区(运行时常量池)和元空间溢出**

 线程共享的区域，包含Class文件信息、运行时常量池、常量池，运行时常量池和常量池的主要区别是具备动态性，也就是不一定非要是在Class文件中的常量池中的内容才能进入运行时常量池，运行期间也可以可以将新的常量放入池中，比如String的intern()方法 

**3 直接内存溢出**

直接内存并不是虚拟机运行时数据区域的一部分，并且不受堆内存的限制，但是受到机器内存大小的限制。常见的比如在NIO中可以使用native函数直接分配堆外内存就容易导致OOM的问题。

直接内存大小可以通过-XX:MaxDirectMemorySize指定，如果不指定，则默认与Java 堆最大值-Xmx一样。

由直接内存导致的内存溢出，一个明显的特征是在Dump文件中不会看见明显的异常，如果发现OOM之后Dump文件很小，而程序中又直接或间接使用了NIO，那就可以考虑检查一下是不是这方面的原因。

**4 栈内存溢出**

 栈是线程私有，它的生命周期和线程相同。每个方法在执行的同时都会创建一个栈帧用于存储局部变量表、操作数栈、动态链接、方法出口等信息，方法调用的过程就是栈帧入栈和出栈的过程。 

## 9.2 JVM各种内存溢出是否产生dump

 **永久代内存溢出，有dump文件** 

 **方法区溢出，有dump文件** 

 **栈内存溢出，没有dump文件** 

 **直接内存溢出，没有dump文件** 



## 9.3 OOM问题定位

 [https://www.cnblogs.com/intsmaze/p/9550256.html#dump%E4%B8%A2%E5%A4%B1%E6%89%93%E5%8D%B0--intsmaze](https://www.cnblogs.com/intsmaze/p/9550256.html#dump丢失打印--intsmaze) 

 1 一定要配置jvm启动参数HeapDumpOnOutOfMemoryError。
参数-XX：+HeapDumpOnOutOfMemoryError可以让虚拟机在出现内存溢出异常时Dump出当前的内存堆转储快照以便事后进行分析 

 ![img](https://images2018.cnblogs.com/blog/758427/201808/758427-20180828185246502-1691551912.png) 

2 dump丢失打印

有些时候，我们的应用程序宕机，既不会打印log日常信息，dump文件也不会生成，这个时候基本就是linux系统杀掉了我们的应用程序进程。

#### 查看/var/log/messages文件

messages 日志是核心系统日志文件。它包含了系统启动时的引导消息，以及系统运行时的其他状态消息。在messages里会出现以下信息

```
out of memory:kill process 8398(java) score 699 or sacrifice child
killed process 8398,UID 505,(java) total-vm:2572232kB,anno-rss:1431292kB,file-rss:908kB
```

oom killer是linux系统的一个保护进程，当linux系统所剩的内存空间不足以满足系统正常运行时，会触发。oomkiller执行时，会找出系统所有线程的score值最高的那个pid，然后干掉。
这里我们可以看到，JAVA进程的确是被LINUX的oom killer干掉了。

我们的应用程序和日志都只能记录JVM内发生的内存溢出。如果JVM设置的堆大小超出了操作系统允许的内存大小，那么操作系统会直接杀死进程，这种情况JVM就无法记录本次操作。
Linux对于每个进程有一个OOM评分，这个评分在/proc/pid/oom_score文件中。例如/proc/8398/oom_score，如果不希望杀死这个进程，就将oom_adj内容改为-17。

**最正确的姿势:首先调整JVM的heap大小，使得JVM的OOM优先于操作系统的OOM出现，接着设置运行参数，在发生OOM的时候输出heapdump文件。**



# 10 [Java软件生产监控工具Btrace的使用](https://www.cnblogs.com/barrywxx/p/11428538.html)

# 11 CMS与G1比较

## 1 CMS



CMS收集器是一种以获取最短回收停顿时间为目标的收集器，CMS收集器是基于“”标记--清除”(Mark-Sweep)算法实现的，整个过程分为四个步骤：  

​     1. 初始标记 (Stop the World事件 CPU停顿， 很短) 初始标记仅标记一下GC Roots能直接关联到的对象，速度很快；

​      2. 并发标记 (收集垃圾跟用户线程一起执行) 初始标记和重新标记任然需要“stop the world”，并发标记过程就是进行GC Roots Tracing的过程；

​      3. 重新标记 (Stop the World事件 CPU停顿，比初始标记稍微长，远比并发标记短)修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录，这个阶段的停顿时间一般会比初始标记阶段稍长一些，但远比并发标记时间短

​      4. 并发清理 -清除算法；

#### 优点是：并发收集、低停顿

**缺点：**

  1.CMS收集器对CPU资源非常敏感 

   在并发阶段，虽然不会导致用户线程停顿，但是会因为占用了一部分线程使应用程序变慢，总吞吐量会降低，为了解决这种情况，虚拟机提供了一种“增量式并发收集器” 

的CMS收集器变种， 就是在并发标记和并发清除的时候让GC线程和用户线程交替运行，尽量减少GC 线程独占资源的时间，这样整个垃圾收集的过程会变长，但是对用户程序的影响会减少。（效果不明显，不推荐） 

2. CMS处理器无法处理浮动垃圾 

   CMS在并发清理阶段线程还在运行， 伴随着程序的运行自然也会产生新的垃圾，这一部分垃圾产生在标记过程之后，CMS无法再当次过程中处理，所以只有等到下次gc时候在清理掉，这一部分垃圾就称作“浮动垃圾” ， 

3. CMS是基于**“标记--清除”**算法实现的，所以在收集结束的时候会有大**量的空间碎片**产生。空间碎片太多的时候，将会给大对象的分配带来很大的麻烦，往往会出现老年代还有很大的空间剩余，但是无法找到足够大的连续空间来分配当前对象的，只能提前触发 full gc。 为了解决这个问题，CMS提供了一个开关参数，用于在CMS顶不住要进行full gc的时候开启内存碎片的合并整理过程，内存整理的过程是无法并发的，空间碎片没有了，但是停顿的时间变长了

##  2 G1

**1、初始标记(stop the world事件 CPU停顿只处理垃圾)；**

**2、并发标记(与用户线程并发执行)；**

**3、最终标记(stop the world事件 ,CPU停顿处理垃圾)；**

**4、筛选回收(stop the world事件 根据用户期望的GC停顿时间回收)(注意：CMS 在这一步不需要stop the world)（阿里问为何停顿时间可以设置，参考：**[G1 垃圾收集器架构和如何做到可预测的停顿(阿里)](https://www.cnblogs.com/aspirant/p/8663872.html)**）**

特点：

1、并行于并发：G1能充分利用CPU、多核环境下的硬件优势，使用多个CPU（CPU或者CPU核心）来缩短stop-The-World停顿时间。部分其他收集器原本需要停顿[Java](http://lib.csdn.net/base/java)线程执行的GC动作，G1收集器仍然可以通过并发的方式让java程序继续执行。

2、分代收集(逻辑分代，物理不分代)：虽然G1可以不需要其他收集器配合就能独立管理整个GC堆，但是还是保留了分代的概念。它能够采用不同的方式去处理新创建的对象和已经存活了一段时间，熬过多次GC的旧对象以获取更好的收集效果。

3、空间整合：与CMS的“标记--清理”算法不同**，G1从整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的**。

4、可预测的停顿：这是G1相对于CMS的另一个大优势，降低停顿时间是G1和ＣＭＳ共同的关注点，但Ｇ１除了追求低停顿外，还能建立可预测的停顿时间模型，能让使用者明确指定在一个长度为M毫秒的时间片段内

缺点：

 应用的内存非常吃紧，对内存进行部分回收根本不够，始终要进行整个Heap的回收，那么G1要做的工作量就一点也不会比其它垃圾回收器少，而且因为本身算法复杂了一点，可能比其它回收器还要差 。

 上面几个步骤的运作过程和CMS有很多相似之处。初始标记阶段仅仅只是标记一下GC Roots能直接关联到的对象，并且修改TAMS的值，让下一个阶段用户程序并发运行时，能在正确可用的Region中创建新对象，这一阶段需要停顿线程，但是耗时很短，并发标记阶段是从GC Root开始对堆中对象进行可达性分析，找出存活的对象，这阶段时耗时较长，但可与用户程序并发执行。而最终标记阶段则是为了修正在并发标记期间因用户程序继续运作而导致标记产生变动的那一部分标记记录，虚拟机将这段时间对象变化记录在线程Remenbered Set Logs里面，最终标记阶段需要把Remembered Set Logs的数据合并到Remembered Set Logs里面，最终标记阶段需要把Remembered Set Logs的数据合并到Remembered Set中，这一阶段需要停顿线程，但是可并行执行。最后在筛选回收阶段首先对各个Region的回收价值和成本进行排序，根据用户所期望的GC停顿时间来制定回收计划。 

## 3 [G1 垃圾收集器架构和如何做到可预测的停顿](https://www.cnblogs.com/aspirant/p/8663872.html)

 **G1回收的第4步，它是“选择一些内存块”，而不是整代内存来回收，这是G1跟其它GC非常不同的一点，其它GC每次回收都会回收整个Generation的内存(Eden, Old), 而回收内存所需的时间就取决于内存的大小，以及实际垃圾的多少，所以垃圾回收时间是不可控的；而G1每次并不会回收整代内存，到底回收多少内存就看用户配置的暂停时间，配置的时间短就少回收点，配置的时间长就多回收点，伸缩自如** 。



# G1与cms比较

1 G1 没碎片

2 G1垃圾回收设置STW

3 CMS停顿时间短，

4 cms采用三色标记法   G1中采用了比CMS更快的初始快照算法:snapshot-at-the-beginning (SATB) 



# 4 ZGC

 https://tech.meituan.com/2020/08/06/new-zgc-practice-in-meituan.html 



# 5 GC问题排查

 

## CPU

**一般来讲我们首先会排查cpu方面的问题。** cpu异常往往还是比较好定位的。原因包括业务逻辑问题(死循环)、频繁gc以及上下文切换过多。而最常见的往往是业务逻辑(或者框架逻辑)导致的，可以使用jstack来分析对应的堆栈情况。

### 使用jstack分析cpu问题

我们先用`ps`命令找到对应进程的 pid(如果你有好几个目标进程，可以先用`top`看一下哪个占用比较高)。接着用`top -H -p pid`来找到cpu使用率比较高的一些线程

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uCrg5SGW57dg5j1wXWGmFOFVo2mZSreeYLO9BKSMQyPRgsibgWpKG8lQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

然后将占用最高的pid转换为16进制`printf '%x\n' pid`得到nid

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06u6ZLg8QMIUtAWAyQSicfBpmYia22CzsYAC6oHicvuVozbyQCPXLfSlaTwA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

接着直接在jstack中找到相应的堆栈信息`jstack pid |grep 'nid' -C5 –color`

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uKdykOWtm9icwPxWaiaHhykzHVD7YhpFnN8oQT9Bru31dwAnOUaGvnZsg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到我们已经找到了nid为0x42的堆栈信息，接着只要仔细分析一番即可。

当然更常见的是我们对整个jstack文件进行分析，通常我们会比较关注WAITING和TIMED_WAITING的部分，BLOCKED就不用说了。我们可以使用命令`cat jstack.log | grep "java.lang.Thread.State" | sort -nr | uniq -c`来对jstack的状态有一个整体的把握，如果WAITING 之类的特别多，那么多半是有问题啦。

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06ubnpbXHHAj0XNhoTbYxauCNAqqSUFU3JCPVexGE4skjqWLPiajlWRPuw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 频繁gc

当然我们还是会使用`jstack`来分析问题，但有时候我们可以先确定下gc是不是太频繁，使用`jstat -gc pid 1000`命令来对gc分代变化情况进行观察，1000表示采样间隔(ms)，S0C/S1C、S0U/S1U、EC/EU、OC/OU、MC/MU分别代表两个Survivor区、Eden区、老年代、元数据区的容量和使用量。YGC/YGT、FGC/FGCT、GCT则代表YoungGc、FullGc的耗时和次数以及总耗时。如果看到gc比较频繁，再针对gc方面做进一步分析。

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06ul99HdrIu1hM1jWbOffZDykq6lYibic78iaDly50QfA9oQ963I4sRKIQHQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 上下文切换

针对频繁上下文问题，我们可以使用`vmstat`命令来进行查看

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06u2h1M7uicicSfdziaXfdQ6GWpa924Vib4sBicfaeeIM7x7aRqxQpjptuFlWg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

**cs(context switch)一列则代表了上下文切换的次数。**如果我们希望对特定的pid进行监控那么可以使用 `pidstat -w pid`命令，cswch和nvcswch表示自愿及非自愿切换。

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uzJjQbU2LG32nzfTzHE7r2VWyOMRlkuEv9Q3h8icAUCd5BmHhV4gf4ug/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

## 磁盘

磁盘问题和cpu一样是属于比较基础的。首先是磁盘空间方面，我们直接使用`df -hl`来查看文件系统状态

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06u2X0VgOw650U1dia4wQgX1t2Yb1yaEg2nzyFfHGTtZGYhibCCKYniaAAsA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

更多时候，磁盘问题还是性能上的问题。我们可以通过iostat`iostat -d -k -x`来进行分析

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06u2ude4kz5yVqju46Wl2RP5aCx8DibFSS7VO3kJniaaF06oQbDcIJs9oNg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

最后一列`%util`可以看到每块磁盘写入的程度，而`rrqpm/s`以及`wrqm/s`分别表示读写速度，一般就能帮助定位到具体哪块磁盘出现问题了。

另外我们还需要知道是哪个进程在进行读写，一般来说开发自己心里有数，或者用`iotop`命令来进行定位文件读写的来源。

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uyeS26NjIMIKciaIz8TdCuTBZcV8P7AzL3VGmqQzU0q12YrtFLeE9BEQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)不过这边拿到的是tid，我们要转换成pid，可以通过readlink命令来找到pid:`readlink -f /proc/*/task/tid/../..`。

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uH25MibbUaqUhIodKTy2DQic1yylEQhoicytictoPwP7Qlxrzd4vJcy2Hpw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)找到pid之后就可以看这个进程具体的读写情况`cat /proc/pid/io`![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uNZyUthBMfobVR7PZDs5awgJya35VlZcGOH2iam8VFFLibAt1H1dqFIRA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)我们还可以通过lsof命令来确定具体的文件读写情况`lsof -p pid`![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uCCM7U9BuLTVQSyBvByoRnlxkaicl3lLjG4YbjYZbQpnicxuUOtMib8WZw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

## 内存

内存问题排查起来相对比CPU麻烦一些，场景也比较多。主要包括**OOM**、**GC问题** 和 **堆外内存**。一般来讲，我们会先用`free`命令先来检查一发内存的各种情况。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uSwdeIQ75IyPBeJuBQMNJwfb671THjRmHC7nWpeEmOicl6ia1QTToiaSyg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 堆内内存

内存问题大多还都是堆内内存问题。表象上主要分为**OOM**和**StackOverflow。**

#### OOM

JMV中的内存不足，OOM大致可以分为以下几种：

```
Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
```

这个意思是没有足够的内存空间给线程分配java栈，基本上还是线程池代码写的有问题，比如说忘记shutdown，所以说应该首先从代码层面来寻找问题，使用`jstack`或者`jmap`。如果一切都正常，JVM方面可以通过指定`Xss`来减少单个thread stack的大小。另外也可以在系统层面，可以通过修改`/etc/security/limits.conf`nofile和nproc来增大os对线程的限制![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uwMZEiazd1JErdQzPRhWRjHH0rHI8T96nE7iaJr2GUurwbJXjdsmPyxeQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

```
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

这个意思是堆的内存占用已经达到-Xmx设置的最大值，应该是最常见的OOM错误了。解决思路仍然是先应该在代码中找，怀疑存在内存泄漏，通过jstack和jmap去定位问题。如果说一切都正常，才需要通过调整`Xmx`的值来扩大内存。

```
Caused by: java.lang.OutOfMemoryError: Meta space
```

这个意思是元数据区的内存占用已经达到`XX:MaxMetaspaceSize`设置的最大值，排查思路和上面的一致，参数方面可以通过`XX:MaxPermSize`来进行调整(这里就不说1.8以前的永久代了)。

#### Stack Overflow

栈内存溢出，这个大家见到也比较多。

```
Exception in thread "main" java.lang.StackOverflowError
```

表示线程栈需要的内存大于Xss值，同样也是先进行排查，参数方面通过`Xss`来调整，但调整的太大可能又会引起OOM。

#### 使用JMAP定位代码内存泄漏

上述关于OOM和StackOverflow的代码排查方面，我们一般使用JMAP`jmap -dump:format=b,file=filename pid`来导出dump文件![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uCjjc2FyiaVpyJicopWia05emRv2dGQfTbeNakmMKDrH6M5anevVhXibmWw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)通过mat(Eclipse Memory Analysis Tools)导入dump文件进行分析，内存泄漏问题一般我们直接选Leak Suspects即可，mat给出了内存泄漏的建议。另外也可以选择Top Consumers来查看最大对象报告。和线程相关的问题可以选择thread overview进行分析。除此之外就是选择Histogram类概览来自己慢慢分析，大家可以搜搜mat的相关教程。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uwC60qaOWKqnsxmJ9TntLUnYCo3GDibX51Jv9zejWspx0NzrfFnseBNA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

日常开发中，代码产生内存泄漏是比较常见的事，并且比较隐蔽，需要开发者更加关注细节。比如说每次请求都new对象，导致大量重复创建对象；进行文件流操作但未正确关闭；手动不当触发gc；ByteBuffer缓存分配不合理等都会造成代码OOM。

另一方面，我们可以在启动参数中指定`-XX:+HeapDumpOnOutOfMemoryError`来保存OOM时的dump文件。

#### gc问题和线程

gc问题除了影响cpu也会影响内存，排查思路也是一致的。一般先使用jstat来查看分代变化情况，比如youngGC或者fullGC次数是不是太多呀；EU、OU等指标增长是不是异常呀等。线程的话太多而且不被及时gc也会引发oom，大部分就是之前说的`unable to create new native thread`。除了jstack细细分析dump文件外，我们一般先会看下总体线程，通过`pstreee -p pid |wc -l`。

![img](https://mmbiz.qpic.cn/mmbiz_png/iaIdQfEric9TwjB8RejlO1YJUicIibuLkttbktjMBwG69KCgI4Cc54K1POQ3wgUveF6ibibwGYEAKQZNWkgbxpIl6SJw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)或者直接通过查看`/proc/pid/task`的数量即为线程数量。![img](https://mmbiz.qpic.cn/mmbiz_png/iaIdQfEric9TwjB8RejlO1YJUicIibuLkttbcVNqw4j7ib6GYia9maYpPIognGlh9Gib6wslNAqO0qEkCrsnX9ZtyX2iaQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 堆外内存

如果碰到堆外内存溢出，那可真是太不幸了。首先堆外内存溢出表现就是物理常驻内存增长快，报错的话视使用方式都不确定，如果由于使用Netty导致的，那错误日志里可能会出现`OutOfDirectMemoryError`错误，如果直接是DirectByteBuffer，那会报`OutOfMemoryError: Direct buffer memory`。

堆外内存溢出往往是和NIO的使用相关，一般我们先通过pmap来查看下进程占用的内存情况`pmap -x pid | sort -rn -k3 | head -30`，这段意思是查看对应pid倒序前30大的内存段。这边可以再一段时间后再跑一次命令看看内存增长情况，或者和正常机器比较可疑的内存段在哪里。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06u09YOfqjw7FxrdluQrbJ1MuKnk1rHJauAcibboiaCd8EdtFUyV7LmF3cg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)我们如果确定有可疑的内存端，需要通过gdb来分析`gdb --batch --pid {pid} -ex "dump memory filename.dump {内存起始地址} {内存起始地址+内存块大小}"`

![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uYbE6HgewBT5iaqUdN8OD5TwjdHbRRdZo3vgMLMEniaKviaWqRLhwR5fnw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

获取dump文件后可用heaxdump进行查看`hexdump -C filename | less`，不过大多数看到的都是二进制乱码。

NMT是Java7U40引入的HotSpot新特性，配合jcmd命令我们就可以看到具体内存组成了。需要在启动参数中加入 `-XX:NativeMemoryTracking=summary` 或者 `-XX:NativeMemoryTracking=detail`，会有略微性能损耗。

一般对于堆外内存缓慢增长直到爆炸的情况来说，可以先设一个基线`jcmd pid VM.native_memory baseline`。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uxn1jRpxYhbSCwqZ5ONFoibYSjkmBLdSXCekeXwpdpayjHAl6kYXcUcQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)然后等放一段时间后再去看看内存增长的情况，通过`jcmd pid VM.native_memory detail.diff(summary.diff)`做一下summary或者detail级别的diff。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06utmkPrA8iclrFsVbrMZNibcicF5PRLhUNQYAvq5hgugNboXW3DkficVqib3w/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uYBO0esSiceKtg123EXFtPHR24b1pVfyORguySf5WYjiaKBaFnSHdQSGA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)可以看到jcmd分析出来的内存十分详细，包括堆内、线程以及gc(所以上述其他内存异常其实都可以用nmt来分析)，这边堆外内存我们重点关注Internal的内存增长，如果增长十分明显的话那就是有问题了。detail级别的话还会有具体内存段的增长情况，如下图。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uWiauib0ia2rsWEaFYiaWH18OvzWPMzqlrqWfhBl5Df1sZk4YtJq3VudeJg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

此外在系统层面，我们还可以使用strace命令来监控内存分配 `strace -f -e "brk,mmap,munmap" -p pid`这边内存分配信息主要包括了pid和内存地址。![img](https://mmbiz.qpic.cn/mmbiz_jpg/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uy63trpNJlFp2Kwqx8rQu5NKSBQMkSzeKQ0icWSxFSxaRtegloNNp2xg/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

不过其实上面那些操作也很难定位到具体的问题点，关键还是要看错误日志栈，找到可疑的对象，搞清楚它的回收机制，然后去分析对应的对象。比如DirectByteBuffer分配内存的话，是需要full GC或者手动system.gc来进行回收的(所以最好不要使用`-XX:+DisableExplicitGC`)。那么其实我们可以跟踪一下DirectByteBuffer对象的内存情况，通过`jmap -histo:live pid`手动触发fullGC来看看堆外内存有没有被回收。如果被回收了，那么大概率是堆外内存本身分配的太小了，通过`-XX:MaxDirectMemorySize`进行调整。如果没有什么变化，那就要使用jmap去分析那些不能被gc的对象，以及和DirectByteBuffer之间的引用关系了。

## GC问题

**堆内内存泄漏总是和GC异常相伴。不过GC问题不只是和内存问题相关，还有可能引起CPU负载、网络问题等系列并发症**，只是相对来说和内存联系紧密些，所以我们在此单独总结一下GC相关问题。

我们在cpu章介绍了使用jstat来获取当前GC分代变化信息。而更多时候，我们是通过GC日志来排查问题的，在启动参数中加上`-verbose:gc -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps`来开启GC日志。常见的Young GC、Full GC日志含义在此就不做赘述了。

针对gc日志，我们就能大致推断出youngGC与fullGC是否过于频繁或者耗时过长，从而对症下药。我们下面将对G1垃圾收集器来做分析，这边也建议大家使用G1`-XX:+UseG1GC`。

### youngGC过频繁

youngGC频繁一般是短周期小对象较多，先考虑是不是Eden区/新生代设置的太小了，看能否通过调整-Xmn、-XX:SurvivorRatio等参数设置来解决问题。如果参数正常，但是young gc频率还是太高，就需要使用Jmap和MAT对dump文件进行进一步排查了。

### youngGC耗时过长

耗时过长问题就要看GC日志里耗时耗在哪一块了。以G1日志为例，可以关注Root Scanning、Object Copy、Ref Proc等阶段。Ref Proc耗时长，就要注意引用相关的对象。Root Scanning耗时长，就要注意线程数、跨代引用。Object Copy则需要关注对象生存周期。而且耗时分析它需要横向比较，就是和其他项目或者正常时间段的耗时比较。比如说图中的Root Scanning和正常时间段比增长较多，那就是起的线程太多了。![img](https://mmbiz.qpic.cn/mmbiz_png/QCu849YTaINAdEbfiaQHfnicbVU7B4Z06uXM81HjWAReqBr2wzfAxoWEHotY051V9ibyMx7kaiap6q9lZ9nJ4kZElA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

### 触发fullGC

G1中更多的还是mixedGC，但mixedGC可以和youngGC思路一样去排查。触发fullGC了一般都会有问题，G1会退化使用Serial收集器来完成垃圾的清理工作，暂停时长达到秒级别，可以说是半跪了。fullGC的原因可能包括以下这些，以及参数调整方面的一些思路：

- 并发阶段失败：在并发标记阶段，MixGC之前老年代就被填满了，那么这时候G1就会放弃标记周期。这种情况，可能就需要增加堆大小，或者调整并发标记线程数`-XX:ConcGCThreads`。
- 晋升失败：在GC的时候没有足够的内存供存活/晋升对象使用，所以触发了Full GC。这时候可以通过`-XX:G1ReservePercent`来增加预留内存百分比，减少`-XX:InitiatingHeapOccupancyPercent`来提前启动标记，`-XX:ConcGCThreads`来增加标记线程数也是可以的。
- 大对象分配失败：大对象找不到合适的region空间进行分配，就会进行fullGC，这种情况下可以增大内存或者增大`-XX:G1HeapRegionSize`。
- 程序主动执行`System.gc()`：不要随便写就对了。

另外，我们可以在启动参数中配置`-XX:HeapDumpPath=/xxx/dump.hprof`来dump fullGC相关的文件，并通过jinfo来进行gc前后的dump

```
jinfo -flag +HeapDumpBeforeFullGC pid jinfo -flag +HeapDumpAfterFullGC pid
```

这样得到2份dump文件，对比后主要关注被gc掉的问题对象来定位问题。





