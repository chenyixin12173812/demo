

# 0 GC 基础

## 1 基础概念

- **GC：**GC 本身有三种语义，下文需要根据具体场景带入不同的语义：
  - **Garbage Collection**：垃圾收集技术，名词。
  - **Garbage Collector**：垃圾收集器，名词。
  - **Garbage Collecting**：垃圾收集动作，动词。
- **Mutator：**生产垃圾的角色，也就是我们的应用程序，垃圾制造者，通过 Allocator 进行 allocate 和 free。
- **TLAB：**Thread Local Allocation Buffer 的简写，基于 CAS 的独享线程（Mutator Threads）可以优先将对象分配在 Eden 中的一块内存，因为是 Java 线程独享的内存区没有锁竞争，所以分配速度更快，每个 TLAB 都是一个线程独享的。
- **Card Table：**中文翻译为卡表，主要是用来标记卡页的状态，每个卡表项对应一个卡页。当卡页中一个对象引用有写操作时，写屏障将会标记对象所在的卡表状态改为 dirty，卡表的本质是用来解决跨代引用的问题。具体怎么解决的可以参考 StackOverflow 上的这个问题 [how-actually-card-table-and-writer-barrier-works](https://stackoverflow.com/questions/19154607/how-actually-card-table-and-writer-barrier-works)，或者研读一下 cardTableRS.app 中的源码。

## 2 Java 8 的内存 

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkxpYlg0TWxEU0tZNWpFSnpkNzI0czR1SGg5MGJiZ0xUak55anJ3VDhKRG9yQk85bE0zdWw3aWJ3LzY0MA.jpg)

 GC 主要工作在 Heap 区和 MetaSpace 区（上图蓝色部分），在 Direct Memory 中，如果使用的是 DirectByteBuffer，那么在分配内存不够时则是 GC 通过 `Cleaner#clean` 间接管理。 

任何自动内存管理系统都会面临的步骤：为新对象分配空间，然后收集垃圾对象空间，下面我们就展开介绍一下这些基础知识

## 3 收集器

目前在 Hotspot VM 中主要有分代收集和分区收集两大类，具体可以看下面的这个图，不过未来会逐渐向分区收集发展。在美团内部，有部分业务尝试用了 ZGC（感兴趣的同学可以学习下这篇文章《[新一代垃圾回收器ZGC的探索与实践](http://mp.weixin.qq.com/s?__biz=MjM5NjQ5MTI5OA%3D%3D&chksm=bd1251228a65d834db610deb2ce55003e0fc1f90793e84873096db19027936f6add301242545&idx=1&mid=2651752559&scene=21&sn=c720b67e93db1885d72dab8799bba78c#wechat_redirect)》），其余基本都停留在 CMS 和 G1 上。另外在 JDK11 后提供了一个不执行任何垃圾回收动作的回收器 Epsilon（A No-Op Garbage Collector）用作性能分析。另外一个就是 Azul 的 Zing JVM，其 C4（Concurrent Continuously Compacting Collector）收集器也在业内有一定的影响力。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RnZmSmlhaWJpYXJNbXpxc0xENm1pY05pYU43ZllKNWJjQjJsVkRrNW9iQXk2UVNMVUF1RzdCSlg1NkNRLzY0MA.jpg)

## 3.1 分代收集器 

- **ParNew：**一款多线程的收集器，采用复制算法，主要工作在 Young 区，可以通过 `-XX:ParallelGCThreads` 参数来控制收集的线程数，整个过程都是 STW 的，常与 CMS 组合使用。
- **CMS：**以获取最短回收停顿时间为目标，采用“标记-清除”算法，分 4 大步进行垃圾收集，其中初始标记和重新标记会 STW ，多数应用于互联网站或者 B/S 系统的服务器端上，JDK9 被标记弃用，JDK14 被删除，详情可见 [JEP 363](https://openjdk.java.net/jeps/363)。

## 3.2 分区收集器

- **G1：**一种服务器端的垃圾收集器，应用在多处理器和大容量内存环境中，在实现高吞吐量的同时，尽可能地满足垃圾收集暂停时间的要求。
- **ZGC：**JDK11 中推出的一款低延迟垃圾回收器，适用于大内存低延迟服务的内存管理和回收，SPECjbb 2015 基准测试，在 128G 的大堆下，最大停顿时间才 1.68 ms，停顿时间远胜于 G1 和 CMS。
- **Shenandoah：**由 Red Hat 的一个团队负责开发，与 G1 类似，基于 Region 设计的垃圾收集器，但不需要 Remember Set 或者 Card Table 来记录跨 Region 引用，停顿时间和堆的大小没有任何关系。停顿时间与 ZGC 接近，下图为与 CMS 和 G1 等收集器的 benchmark。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RmlhZmRVZWcxTEF6TFpxVUNZNXNJYkxvemY1b3NQWXVYYVNrMjY4cXNqblVjZjdyeUtxOURrd2cvNjQw.jpg)

目前使用最多的是 CMS 和 G1 收集器，二者都有分代的概念，主要内存结构如下：

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RmNDOFBrOXBaeG51cmNicGNkZHNuQWxpYXM4UEJNUXVwSkM5RkVCNGFxOVloaWJFRHIyVDM0a05nLzY0MA.png)

以上仅列出常见收集器，除此之外还有很多，如 Metronome、Stopless、Staccato、Chicken、Clover 等实时回收器，Sapphire、Compressor、Pauseless 等并发复制/整理回收器，Doligez-Leroy-Conthier 等标记整理回收器，由于篇幅原因，不在此一一介绍。

## 4 常用工具

## 4.1 命令行终端

- **标准终端类**：jps、jinfo、jstat、jstack、jmap
- **功能整合类**：jcmd、vjtools、arthas、greys

## 4.2 可视化界面

- **简易**：JConsole、JVisualvm、HA、GCHisto、GCViewer
- **进阶**：MAT、JProfiler

**命令行推荐 arthas ，可视化界面推荐 JProfiler**，此外还有一些在线的平台 [gceasy](https://gceasy.io/)、[heaphero](https://heaphero.io/)、[fastthread](https://fastthread.io/) ，美团内部的 Scalpel（一款自研的 JVM 问题诊断工具，暂时未开源）也比较好用。



# 一 调优目标

- **Trade Off：**与 CAP 注定要缺一角一样，GC 优化要在延迟（Latency）、吞吐量（Throughput）、容量（Capacity）三者之间进行权衡。
- **最终手段：**GC 发生问题不是一定要对 JVM 的 GC 参数进行调优，大部分情况下是通过 GC 的情况找出一些业务问题，切记上来就对 GC 参数进行调整，当然有明确配置错误的场景除外。
- **控制变量：**控制变量法是在蒙特卡洛（Monte Carlo）方法中用于减少方差的一种技术方法，我们调优的时候尽量也要使用，每次调优过程尽可能只调整一个变量。
- **善用搜索：**理论上 99.99% 的 GC 问题基本都被遇到了，我们要学会使用搜索引擎的高级技巧，重点关注 StackOverFlow、Github 上的 Issue、以及各种论坛博客，先看看其他人是怎么解决的，会让解决问题事半功倍。能看到这篇文章，你的搜索能力基本过关了~
- **调优重点：**总体上来讲，我们开发的过程中遇到的问题类型也基本都符合正态分布，太简单或太复杂的基本遇到的概率很低，笔者这里将中间最重要的三个场景添加了“*”标识，希望阅读完本文之后可以观察下自己负责的系统，是否存在上述问题。
- **GC 参数：**如果堆、栈确实无法第一时间保留，一定要保留 GC 日志，这样我们最起码可以看到 GC Cause，有一个大概的排查方向。关于 GC 日志相关参数，最基本的 `-XX:+HeapDumpOnOutOfMemoryError` 等一些参数就不再提了，笔者建议添加以下参数，可以提高我们分析问题的效率。

# 2 gc评价标准 

两个核心指标：

- **延迟（Latency）：**也可以理解为最大停顿时间，即垃圾收集过程中一次 STW 的最长时间，越短越好，一定程度上可以接受频次的增大，GC 技术的主要发展方向。
- **吞吐量（Throughput）：**应用系统的生命周期内，由于 GC 线程会占用 Mutator 当前可用的 CPU 时钟周期，吞吐量即为 Mutator 有效花费的时间占系统总运行时间的百分比，例如系统运行了 100 min，GC 耗时 1 min，则系统吞吐量为 99%，吞吐量优先的收集器可以接受较长的停顿。

目前各大互联网公司的系统基本都更追求**低延时**，避免一次 GC 停顿的时间，衡量指标需要结合一下应用服务的 SLA，主要如下两点来判断：

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RmdOUnY0djU3cmdLbTFYaGV2UFljYm84VlNya2RHVDlwZFE1Q1RjZkZyUUhSUWljMDNsYmNFc0EvNjQw.jpg)

备注：除了这两个指标之外还有 **Footprint（资源量大小测量）、反应速度等指标，互联网这种实时系统追求低延迟，而很多嵌入式系统则追求 Footprint**。



MinorGC 执行时间不到50ms；

Minor GC 执行不频繁，约10秒一次；

Full GC 执行时间不到1s；

Full GC 执行频率不算频繁，不低于10分钟1次。



# 二 GC问题分析

# 1  GC Cause 

通过一些工具，我们可以比较直观地看到 Cause 的分布情况，如下图就是使用 gceasy 绘制的图表：

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkJYVzBiclJkWWJySzRhY2FDTTN0OWlhRHQwZ0VFRnlKRFloV2tMOW81bGg1NjN4MFd5dzlQaWNnLzY0MA.png)

先要读懂 GC Cause，即 JVM 什么样的条件下选择进行 GC 操作，Hotspot 源码：src/share/vm/gc/shared/gcCause.hpp 和 src/share/vm/gc/shared/gcCause.cpp ：

```c++
const char* GCCause::to_string(GCCause::Cause cause) {
  switch (cause) {
    case _java_lang_system_gc:
      return "System.gc()";
 
    case _full_gc_alot:
      return "FullGCAlot";
 
    case _scavenge_alot:
      return "ScavengeAlot";
 
    case _allocation_profiler:
      return "Allocation Profiler";
 
    case _jvmti_force_gc:
      return "JvmtiEnv ForceGarbageCollection";
 
    case _gc_locker:
      return "GCLocker Initiated GC";
 
    case _heap_inspection:
      return "Heap Inspection Initiated GC";
 
    case _heap_dump:
      return "Heap Dump Initiated GC";
 
    case _wb_young_gc:
      return "WhiteBox Initiated Young GC";
 
    case _wb_conc_mark:
      return "WhiteBox Initiated Concurrent Mark";
 
    case _wb_full_gc:
      return "WhiteBox Initiated Full GC";
 
    case _no_gc:
      return "No GC";
 
    case _allocation_failure:
      return "Allocation Failure";
 
    case _tenured_generation_full:
      return "Tenured Generation Full";
 
    case _metadata_GC_threshold:
      return "Metadata GC Threshold";
 
    case _metadata_GC_clear_soft_refs:
      return "Metadata GC Clear Soft References";
 
    case _cms_generation_full:
      return "CMS Generation Full";
 
    case _cms_initial_mark:
      return "CMS Initial Mark";
 
    case _cms_final_remark:
      return "CMS Final Remark";
 
    case _cms_concurrent_mark:
      return "CMS Concurrent Mark";
 
    case _old_generation_expanded_on_last_scavenge:
      return "Old Generation Expanded On Last Scavenge";
 
    case _old_generation_too_full_to_scavenge:
      return "Old Generation Too Full To Scavenge";
 
    case _adaptive_size_policy:
      return "Ergonomics";
 
    case _g1_inc_collection_pause:
      return "G1 Evacuation Pause";
 
    case _g1_humongous_allocation:
      return "G1 Humongous Allocation";
 
    case _dcmd_gc_run:
      return "Diagnostic Command";
 
    case _last_gc_cause:
      return "ILLEGAL VALUE - last gc cause - ILLEGAL VALUE";
 
    default:
      return "unknown GCCause";
  }
  ShouldNotReachHere();
}
```

重点GC Cause：

- **System.gc()：**手动触发GC操作。
- **CMS：**CMS GC 在执行过程中的一些动作，重点关注 CMS Initial Mark 和 CMS Final Remark 两个 STW 阶段。
- **Promotion Failure：**Old 区没有足够的空间分配给 Young 区晋升的对象（即使总可用内存足够大）。
- **Concurrent Mode Failure：**CMS GC 运行期间，Old 区预留的空间不足以分配给新的对象，此时收集器会发生退化，严重影响 GC 性能，下面的一个案例即为这种场景。
- **GCLocker Initiated GC：**如果线程执行在 JNI 临界区时，刚好需要进行 GC，此时 GC Locker 将会阻止 GC 的发生，同时阻止其他线程进入 JNI 临界区，直到最后一个线程退出临界区时触发一次 GC。

 Cause何时触发回收，cms源码： /src/hotspot/share/gc/cms/concurrentMarkSweepGeneration.cpp 中。shouldConcurrentCollect

```c++
bool CMSCollector::shouldConcurrentCollect() {
  LogTarget(Trace, gc) log;
 
  if (_full_gc_requested) {
    log.print("CMSCollector: collect because of explicit  gc request (or GCLocker)");
    return true;
  }
 
  FreelistLocker x(this);
  // ------------------------------------------------------------------
  // Print out lots of information which affects the initiation of
  // a collection.
  if (log.is_enabled() && stats().valid()) {
    log.print("CMSCollector shouldConcurrentCollect: ");
 
    LogStream out(log);
    stats().print_on(&out);
 
    log.print("time_until_cms_gen_full %3.7f", stats().time_until_cms_gen_full());
    log.print("free=" SIZE_FORMAT, _cmsGen->free());
    log.print("contiguous_available=" SIZE_FORMAT, _cmsGen->contiguous_available());
    log.print("promotion_rate=%g", stats().promotion_rate());
    log.print("cms_allocation_rate=%g", stats().cms_allocation_rate());
    log.print("occupancy=%3.7f", _cmsGen->occupancy());
    log.print("initiatingOccupancy=%3.7f", _cmsGen->initiating_occupancy());
    log.print("cms_time_since_begin=%3.7f", stats().cms_time_since_begin());
    log.print("cms_time_since_end=%3.7f", stats().cms_time_since_end());
    log.print("metadata initialized %d", MetaspaceGC::should_concurrent_collect());
  }
  // ------------------------------------------------------------------
 
  // If the estimated time to complete a cms collection (cms_duration())
  // is less than the estimated time remaining until the cms generation
  // is full, start a collection.
  if (!UseCMSInitiatingOccupancyOnly) {
    if (stats().valid()) {
      if (stats().time_until_cms_start() == 0.0) {
        return true;
      }
    } else {
 
      if (_cmsGen->occupancy() >= _bootstrap_occupancy) {
        log.print(" CMSCollector: collect for bootstrapping statistics: occupancy = %f, boot occupancy = %f",
                  _cmsGen->occupancy(), _bootstrap_occupancy);
        return true;
      }
    }
  }
  if (_cmsGen->should_concurrent_collect()) {
    log.print("CMS old gen initiated");
    return true;
  }
 
  CMSHeap* heap = CMSHeap::heap();
  if (heap->incremental_collection_will_fail(true /* consult_young */)) {
    log.print("CMSCollector: collect because incremental collection will fail ");
    return true;
  }
 
  if (MetaspaceGC::should_concurrent_collect()) {
    log.print("CMSCollector: collect for metadata allocation ");
    return true;
  }
 
  // CMSTriggerInterval starts a CMS cycle if enough time has passed.
  if (CMSTriggerInterval >= 0) {
    if (CMSTriggerInterval == 0) {
      // Trigger always
      return true;
    }
 
    // Check the CMS time since begin (we do not check the stats validity
    // as we want to be able to trigger the first CMS cycle as well)
    if (stats().cms_time_since_begin() >= (CMSTriggerInterval / ((double) MILLIUNITS))) {
      if (stats().valid()) {
        log.print("CMSCollector: collect because of trigger interval (time since last begin %3.7f secs)",
                  stats().cms_time_since_begin());
      } else {
        log.print("CMSCollector: collect because of trigger interval (first collection)");
      }
      return true;
    }
  }
 
  return false;
}
```

# 2  是不是 GC 引发的问题?

 如何判断是 GC 导致的故障，还是系统本身引发 GC 问题。这里继续拿在本文开头提到的一个 Case：“GC 耗时增大、线程 Block 增多、慢查询增多、CPU 负载高等四个表象，如何判断哪个是根因？ 

四种判断方法供参考：

- **时序分析：**先发生的事件是根因的概率更大，通过监控手段分析各个指标的异常时间点，还原事件时间线，如先观察到 CPU 负载高（要有足够的时间 Gap），那么整个问题影响链就可能是：CPU 负载高 -> 慢查询增多 -> GC 耗时增大 -> 线程Block增多 -> RT 上涨。
- **概率分析：**使用统计概率学，结合历史问题的经验进行推断，由近到远按类型分析，如过往慢查的问题比较多，那么整个问题影响链就可能是：慢查询增多 -> GC 耗时增大 ->  CPU 负载高  -> 线程 Block 增多 -> RT上涨。
- **实验分析：**通过故障演练等方式对问题现场进行模拟，触发其中部分条件（一个或多个），观察是否会发生问题，如只触发线程 Block 就会发生问题，那么整个问题影响链就可能是：线程Block增多  -> CPU 负载高  -> 慢查询增多  -> GC 耗时增大 ->  RT 上涨。
- **反证分析：**对其中某一表象进行反证分析，即判断表象的发不发生跟结果是否有相关性，例如我们从整个集群的角度观察到某些节点慢查和 CPU 都正常，但也出了问题，那么整个问题影响链就可能是：GC 耗时增大 -> 线程 Block 增多 ->  RT 上涨。

不同的根因，后续的分析方法是完全不同的。如果是 CPU 负载高那可能需要用火焰图看下热点、如果是慢查询增多那可能需要看下 DB 情况、如果是线程 Block 引起那可能需要看下锁竞争的情况，最后如果各个表象证明都没有问题，那可能 GC 确实存在问题，可以继续分析 GC 问题了。

  # 3 gc问题分类

## 3.1 Mutator 类型

Mutator 的类型根据对象存活时间比例图来看主要分为两种，在弱分代假说中也提到类似的说法，如下图所示 “Survival Time” 表示对象存活时间，“Rate” 表示对象分配比例：

- **IO 交互型：**互联网上目前大部分的服务都属于该类型，例如分布式 RPC、MQ、HTTP 网关服务等，对内存要求并不大，大部分对象在 TP9999 的时间内都会死亡， **Young 区越大越好**。
- **MEM 计算型：**主要是分布式数据计算 Hadoop，分布式存储 HBase、Cassandra，自建的分布式缓存等，对内存要求高，对象存活时间长，**Old 区越大越好**。

当然，除了二者之外还有介于两者之间的场景，本篇文章主要讨论第一种情况。对象 Survival Time 分布图，对我们设置 GC 参数有着非常重要的指导意义，如下图就可以简单推算分代的边界。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RjNIdmhmZjlVRXRaMzNzZEprM0pqaWFFc0p2VGtwM3BKbWZiaWNlSkZEYTBoWmp6bjhGaWJiTDZZdy82NDA.jpg)

## 3.2 九种不同类型的 GC 问题

- **Unexpected GC：**意外发生的 GC，实际上不需要发生，我们可以通过一些手段去避免。

  - **Space Shock：**空间震荡问题，参见“场景一：动态扩容引起的空间震荡”。
  - **Explicit GC：**显示执行 GC 问题，参见“场景二：显式 GC 的去与留”。

- **Partial GC：**部分收集操作的 GC，只对某些分代/分区进行回收。

  - Young GC：

    分代收集里面的 Young 区收集动作，也可以叫做 Minor GC。

    - **ParNew：**Young GC 频繁，参见“场景四：过早晋升”。

  - Old GC：

    分代收集里面的 Old 区收集动作，也可以叫做 Major GC，有些也会叫做 Full GC，但其实这种叫法是不规范的，在 CMS 发生 Foreground GC 时才是 Full GC，CMSScavengeBeforeRemark 参数也只是在 Remark 前触发一次Young GC。

    - **CMS：**Old GC 频繁，参见“场景五：CMS Old GC 频繁”。
    - **CMS：**Old GC 不频繁但单次耗时大，参见“场景六：单次 CMS Old GC 耗时长”。

- **Full GC：**全量收集的 GC，对整个堆进行回收，STW 时间会比较长，一旦发生，影响较大，也可以叫做 Major GC，参见“场景七：内存碎片&收集器退化”。

- **MetaSpace：**元空间回收引发问题，参见“场景三：MetaSpace 区 OOM”。

- **Direct Memory：**直接内存（也可以称作为堆外内存）回收引发问题，参见“场景八：堆外内存 OOM”。

- **JNI：**本地 Native 方法引发问题，参见“场景九：JNI 引发的 GC 问题”。

# 三 前期规划

## 1基础：

-server//服务器模式
-Xmx4096m //JVM最大允许分配的堆内存，按需分配
-Xms4096m //JVM初始分配的栈内存，**一般和Xmx配置成一样以避免每次gc后JVM重新分配内存**，防止内存抖动。
-Xmn 512m //年轻代内存大小，一般设为整个堆的1/3到1/4左右 整个JVM内存=年轻代 + 年老代 + 持久代

 -XX:SurvivorRatio Eden/Survivor,8表示Survivor:Eden为1:8 一般不用调

 // 一般使用-Xmn和-XX:PermSize确定 -XX:NewRatio //设置新生代空间和老年代空间的占比  一般1：2

-XX:MaxPermSize=512m //最大持久代大小

-XX:NewRatio, -XX:SurvivorRatio 

-Xss256k // 设置每个线程的堆栈大小 一般256 最大512kb-1M

 -XX:MetaSpaceSize可以设置java堆外内存的峰值 

-XX:MaxMetaSpaceSize可以设置java堆外内存的峰值 

-XX:PretenureSizeThreshold 可以设置直接进入老年代的对象大

-XX:MaxDirectMemorySize参数来控制可申请的堆外内存的最大值。在 Java 8 中，如果未配置该参数，默认和 `-Xmx` 相等



## 2 运维辅助信息 ：

 **-Xloggc:filename:**与上面几个配合使用，把相关日志信息记录到文件以便分析。 

 **-XX：+HeapDumpOnOutOfMemoryError**可以让虚拟机在出现内存溢出异常时Dump出当前的内存堆转储快照以便事后进行分析 

 **-XX:+PrintGCDetails 更详细的GC日志** 

 -XX:+PrintGC：输出形式：[GC 118250K->113543K(130112K), 0.0094143 secs] [Full GC 121376K- >10414K(130112K), 0.0650971 secs]

-XX:+PrintGCDetails：输出形式：[GC [DefNew: 8614K->781K(9088K), 0.0123035 secs] 118250K- >113543K(130112K), 0.0124633 secs] [GC [DefNew: 8614K->8614K(9088K), 0.0000665 secs][Tenured: 112761K->10414K(121024K), 0.0433488 secs] 121376K->10414K(130112K), 0.0436268 secs] 

-XX:+PrintGCTimeStamps -XX:+PrintGC：PrintGCTimeStamps可与上面两个混合使用 输出形式：11.851: [GC 98328K->93620K(130112K), 0.0082960 secs] 

-XX:+PrintGCApplicationConcurrentTime：打印每次垃圾回收前，程序未中断的执行时间。可与上面混合 使用。输出形式：Application time: 0.5291524 seconds 

-XX:+PrintGCApplicationStoppedTime：打印垃圾回收期间程序暂停的时间。可与上面混合使用。输出形 式：Total time for which application threads were stopped: 0.0468229 seconds -XX:PrintHeapAtGC: 打印GC前后的详细堆栈信息。输出形式：  

 34.702: [GC {Heap before gc invocations=7: def new generation total 55296K, used 52568K [0x1ebd0000, 0x227d0000, 0x227d0000) eden space 49152K, 99% used [0x1ebd0000, 0x21bce430, 0x21bd0000) from space 6144K, 55% used [0x221d0000, 0x22527e10, 0x227d0000) to space 6144K, 0% used [0x21bd0000, 0x21bd0000, 0x221d0000) tenured generation total 69632K, used 2696K [0x227d0000, 0x26bd0000, 0x26bd0000) the space 69632K, 3% used [0x227d0000, 0x22a720f8, 0x22a72200, 0x26bd0000) compacting perm gen total 8192K, used 2898K [0x26bd0000, 0x273d0000, 0x2abd0000) the space 8192K, 35% used [0x26bd0000, 0x26ea4ba8, 0x26ea4c00, 0x273d0000) ro space 8192K, 66% used [0x2abd0000, 0x2b12bcc0, 0x2b12be00, 0x2b3d0000) rw space 12288K, 46% used [0x2b3d0000, 0x2b972060, 0x2b972200, 0x2bfd0000) 34.735: [DefNew: 52568K->3433K(55296K), 0.0072126 secs] 55264K->6615K(124928K)Heap after gc invocations=8: def new generation total 55296K, used 3433K [0x1ebd0000, 0x227d0000, 0x227d0000) eden space 49152K, 0% used [0x1ebd0000, 0x1ebd0000, 0x21bd0000) http://pengjiaheng.javaeye.com 1.8 JVM调优总结（七）-典型配置举例1 第 29 / 51 页 from space 6144K, 55% used [0x21bd0000, 0x21f2a5e8, 0x221d0000) to space 6144K, 0% used [0x221d0000, 0x221d0000, 0x227d0000) tenured generation total 69632K, used 3182K [0x227d0000, 0x26bd0000, 0x26bd0000) the space 69632K, 4% used [0x227d0000, 0x22aeb958, 0x22aeba00, 0x26bd0000) compacting perm gen total 8192K, used 2898K [0x26bd0000, 0x273d0000, 0x2abd0000) the space 8192K, 35% used [0x26bd0000, 0x26ea4ba8, 0x26ea4c00, 0x273d0000) ro space 8192K, 66% used [0x2abd0000, 0x2b12bcc0, 0x2b12be00, 0x2b3d0000) rw space 12288K, 46% used [0x2b3d0000, 0x2b972060, 0x2b972200, 0x2bfd0000) } , 0.0757599 secs] 

 ![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rktob0Y0bnhpY01lejFpYzFVSnFrYVoyWVJ0enFPNW9aWGRXSFNZR3BhQ2ZSa2hEbjl3eWVuaWJhZy82NDA.png) 

## 3  垃圾回收器 ：

## 3.1 cms

//-XX:+DisableExplicitGC //忽略手动调用GC, System.gc()的调用就会变成一个空调用，完全不触发GC

-XX:+ExplicitGCInvokesConcurrent` 和 `-XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses` 参数来将 System.gc 的触发类型从 Foreground 改为 Background，同时 Background 也会做 Reference Processing，这样的话就能大幅降低了 STW 开销，同时也不会发生 NIO Direct Memory OOM。

-XX:+UseConcMarkSweepGC //并发标记清除（CMS）收集器
-XX:+CMSParallelRemarkEnabled //降低标记停顿
-XX:+UseCMSCompactAtFullCollection //在FULL GC的时候对年老代的压缩
-XX:LargePageSizeInBytes=128m //内存页的大小
-XX:+UseFastAccessorMethods //原始类型的快速优化
-XX:+UseCMSInitiatingOccupancyOnly //使用手动定义初始化定义开始CMS收集
-XX:CMSInitiatingOccupancyFraction=70 //使用cms作为垃圾回收使用70％后开始CMS收集

 -XX:ParallelGCThreads=20

-XX:+UseParNewGC: 设置年轻代为并行收集。可与CMS收集同时使用。JDK5.0以上，JVM会根据 系统配置自行设置，所以无需再设置此值。 java -Xmx3550m -Xms3550m -Xmn2g -Xss128k 

-XX:CMSFullGCsBeforeCompaction=5

 -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction：由于并发收集器不对内存空间进行压缩、整理，所以运行一 段时间以后会产生“碎片”，使得运行效率降低。此值设置运行多少次GC以后对内存空间进行压缩、 整理。

 -XX:+UseCMSCompactAtFullCollection：打开对年老代的压缩。可能会影响性能，但是可以消除 碎片  

 XX:ParallelGCThreads=n:设置并行收集器收集时使用的CPU数。并行收集线程数。 

-XX:MaxGCPauseMillis=n:设置并行收集最大暂停时间 

-XX:GCTimeRatio=n:设置垃圾回收时间占程序运行时间的百分比。公式为1/(1+n) 

**-XX:+UseCMSInitiatingOccupancyOnly:** **不加这个选项的话，根据成本计算决定是否需要执行****CMS gc****，加上这个选项，当旧生代空间使用率达到****92%****时会无条件执行****Full GC****。**

**-XX:+CMSClassUnloadingEnabled** ：**CMS****收集器默认不会对永久代进行垃圾回收。如果希望对永久代进行垃圾回收，****需要设置此选项。**



其实还有两个选项可以考虑：

CMSInitiatingOccupancyFraction： 当旧生代空间使用到一定比率时, 强制执行旧生代的Full GC。默认为92%。这个参数如果设置为50左右的话，Full GC的次数会非常频繁。可能会对应用的性能造成一定影响。

CMSInitiatingPermOccupancyFraction： 当持久代里的使用比例达到一定比率时，进行持久代内存的回收，默认为92%。如果调低的话，也会明显增多持久代gc的次数。

但这个选项也可能对应用的性能造成影响，所以使用需要谨慎

## 3.2 G1

```
-XX:+UseG1GC            # 指定使用G1收集器
-XX:MaxGCPauseMillis    # 为G1设置暂停时间目标，默认值为200毫秒
-XX:G1HeapRegionSize    # 设置每个Region大小，范围1MB到32MB；目标是在最小Java堆时可以拥有约2048个Region
-XX:ParallelGCThreads=n # STW期间，并行线程数。建议设置与处理器相同个数，最多为8。
如果处理器多于8个，则将n的值设置为处理器的大约5/8。
```



 ## 4 进一步调优

1. GC的时间足够的小
2. GC的次数足够的少
3. 发生Full GC的周期足够的长

前两个目前是相悖的，要想GC时间小必须要一个更小的堆，要保证GC次数足够少，必须保证一个更大的堆，我们只能取其平衡。

1. 针对JVM堆的设置，一般可以通过-Xms -Xmx限定其最小、最大值，为了防止垃圾收集器在最小、最大之间收缩堆而产生额外的时间，我们通常把最大、最小设置为相同的值
2. 年轻代和年老代将根据默认的比例（1：2）分配堆内存，可以通过调整二者之间的比率NewRadio来调整二者之间的大小，也可以针对回收代，比如年轻代，**通过 -XX:newSize -XX:MaxNewSize来设置其绝对大小。同样，为了防止年轻代的堆收缩，我们通常会把-XX:newSize -XX:MaxNewSize设置为同样大小**
3. 年轻代和年老代设置多大才算合理？这个我问题毫无疑问是没有答案的，否则也就不会有调优。我们观察一下二者大小变化有哪些影响

-  更大的年轻代必然导致更小的年老代，大的年轻代会延长普通GC的周期，但会增加每次GC的时间；小的年老代会导致更频繁的Full GC
-  更小的年轻代必然导致更大年老代，小的年轻代会导致普通GC很频繁，但每次的GC时间会更短；大的年老代会减少Full GC的频率
-  如何选择应该依赖应用程序对象生命周期的分布情况：如果应用存在大量的临时对象，应该选择更大的年轻代；如果存在相对较多的持久对象，年老代应该适当增大。

（A）本着Full GC尽量少的原则，让年老代尽量缓存常用对象，JVM的默认比例1：2也是这个道理 （B）通过观察应用一段时间，看其他在峰值时年老代会占多少内存，在不影响Full GC的前提下，根据实际情况加大年轻代，比如可以把比例控制在1：1。但应该给年老代至少预留1/3的增长空间**

**策略 1：**将新对象预留在新生代，由于 Full GC 的成本远高于 Minor GC，因此尽可能将对象分配在新生代是明智的做法，实际项目中根据 GC 日志分析新生代空间大小分配是否合理，适当通过“-Xmn”命令调节新生代大小，最大限度降低新对象直接进入老年代的情况。

**策略 2：**大对象进入老年代，虽然大部分情况下，将对象分配在新生代是合理的。但是对于大对象这种做法却值得商榷，大对象如果首次在新生代分配可能会出现空间不足导致很多年龄不够的小对象被分配的老年代，破坏新生代的对象结构，可能会出现频繁的 full gc。因此，对于大对象，-XX:PretenureSizeThreshold 可以设置直接进入老年代的对象大小可以设置直接进入老年代（当然短命的大对象对于垃圾回收老说简直就是噩梦）。

**策略 3：**合理设置进入老年代对象的年龄，-XX:MaxTenuringThreshold 设置对象进入老年代的年龄大小，减少老年代的内存占用，降低 full gc 发生的频率。

**策略 4：**设置稳定的堆大小，堆大小设置有两个参数：-Xms 初始化堆大小，-Xmx 最大堆大小。

**策略5：**注意：如果满足下面的指标，**则一般不需要进行 GC 优化：**



# 四 典型场景

# 0 CMS

 https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/cms.html 

CMS 垃圾收集器的垃圾回收分4个步骤：

- 初始标记（initial mark） 有 STW
- 并发标记（concurrent mark） 没有 STW
- 重新标记（remark） 有 STW
- 并发清除（concurrent sweep） 没有 STW

初始标记：仅仅标记 GC Roots 能直接关联到的对象。
并发标记：对初始标记标记过的对象，进行 trace（进行追踪，得到所有关联的对象，进行标记）
重新标记：（原文）：为了修正并发标记期间因用户程序继续运作而导致标记产生变动的那一部分对象的标记记录。

CMS 垃圾收集器主要有三个问题：

1. 内存碎片（原因是采用了标记-清除算法）
2. 对 CPU 资源敏感（原因是并发时和用户线程一起抢占 CPU）
3. 浮动垃圾：在并发标记阶段产生了新垃圾不会被及时回收，而是只能等到下一次GC

第二次暂停是在并发跟踪阶段结束时进行的，它查找由于CMS收集器完成对对象的引用后，应用程序线程对对象中的引用进行更新而导致并发跟踪遗漏的对象。该第二暂停称为重新标记暂停。

*以下是我个人对这个问题的解答猜想：*

由于标记阶段是从 GC Roots 开始标记可达对象，那么在并发标记阶段可能产生两种变动：

1. 本来可达的对象，变得不可达了
2. 本来不可达的内存，变得可达了

第一种变动会产生所谓的浮动垃圾，第二种变动怎么回事呢？重点在于`miss`。

如果并发标记阶段用户线程里 new 了一个对象，而它在初始标记和并发标记中是不会能够从 GC Roots 可达的，也就是`were missed`。如果没有重新标记阶段来将这个对象标记为可达，那么它会在清理阶段被回收，这是严重的错误，是必须要在重新标记阶段来处理的，所以这就是重新标记阶段实际上的任务。

相比之下，浮动垃圾是可容忍的问题，而不是错误。那么为什么重新标记阶段不处理第一种变动呢？也许是`由可达变为不可达`这样的变化需要重新从 GC Roots 开始遍历，相当于再完成一次初始标记和并发标记的工作，这样不仅前两个阶段变成多余的，浪费了开销浪费，还会大大增加重新标记阶段的开销，所带来的暂停时间是追求低延迟的CMS所不能容忍的。



# 1  场景一：动态扩容引起的空间震荡

**1 现象**

服务**刚刚启动时 GC 次数较多**，最大空间剩余很多但是依然发生 GC，这种情况我们可以通过观察 GC 日志或者通过监控工具来观察堆的空间变化情况即可。GC Cause 一般为 Allocation Failure，且在 GC 日志中会观察到经历一次 GC ，堆内各个空间的大小会被调整，如下图所示： 

 ![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkE3cWZyaWJZRXhBR2ttSHkzZ3dBUmpTMXRLamliQlZ4ZktVU0l6Vkh0Q3Z3ZGxtVklaV2pVWTlRLzY0MA.png) 

**2原因**

在 JVM 的参数中 `-Xms` 和 `-Xmx` 设置的不一致，在初始化时只会初始 `-Xms` 大小的空间存储信息，每当空间不够用时再向操作系统申请，这样的话必然要进行一次 GC。具体是通过 `ConcurrentMarkSweepGeneration::compute_new_size()` 方法计算新的空间大小 

整个伸缩的模型理解可以看这个图，当 committed 的空间大小超过了低水位/高水位的大小，capacity 也会随之调整：

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RjRRblBSaWJYR2xTem42WlRzT2hIUXh3eXB2d2QyVEh3N01hRGliR0lPQktTZFFTY1k5WFZGdDd3LzY0MA.jpg)



**3 解决**

 尽量**将成对出现的空间大小配置参数设置成固定的**，如 `-Xms` 和 `-Xmx`，`-XX:MaxNewSize` 和 `-XX:NewSize`，`-XX:MetaSpaceSize` 和 `-XX:MaxMetaSpaceSize` 等。 

# 2 场景二  System.gc 

 **1 现象**

  扩容缩容会触发 CMS GC 之外，还有 Old 区达到回收阈值、MetaSpace 空间不足、Young 区晋升失败、大对象担保失败等几种触发条件，如果这些情况都没有发生却触发了 GC ？这种情况有可能是代码中手动调用了 System.gc 方法，此时可以找到 GC 日志中的 GC Cause 确认下。，有人说可以添加 `-XX:+DisableExplicitGC` 参数来避免这种 GC，也有人说不能加这个参数，加了就会影响 Native Memory 的回收。先说结论，笔者这里建议保留 System.gc 。

 **2原因**

 System.gc 在 Hotspot 中的源码，可以发现增加 `-XX:+DisableExplicitGC` 参数后，这个方法变成了一个空方法，如果没有加的话便会调用 `Universe::heap()::collect` 方法，继续跟进到这个方法中，发现 System.gc 会引发一次 STW 的 Full GC，对整个堆做收集 

**保留 System.gc**

此处补充一个知识点，**CMS GC 共分为 Background 和 Foreground 两种模式**，前者就是我们常规理解中的并发收集，可以不影响正常的业务线程运行，但 Foreground Collector 却有很大的差异，他会进行一次压缩式 GC。此压缩式 GC 使用的是跟 Serial Old GC 一样的 Lisp2 算法，其使用 Mark-Compact 来做 Full GC，一般称之为 MSC（Mark-Sweep-Compact），它收集的范围是 Java 堆的 Young 区和 Old 区以及 MetaSpace。由上面的算法章节中我们知道 compact 的代价是巨大的，那么使用 Foreground Collector 时将会带来非常长的 STW。如果在应用程序中 System.gc 被频繁调用，那就非常危险了。

**去掉 System.gc**

如果禁用掉的话就会带来另外一个内存泄漏问题，此时就需要说一下 DirectByteBuffer，它有着零拷贝等特点，被 Netty 等各种 NIO 框架使用，会使用到堆外内存。堆内存由 JVM 自己管理，堆外内存必须要手动释放，DirectByteBuffer 没有 Finalizer，它的 Native Memory 的清理工作是通过 `sun.misc.Cleaner` 自动完成的，是一种基于 PhantomReference 的清理工具，比普通的 Finalizer 轻量些

 为 DirectByteBuffer 分配空间过程中会显式调用 System.gc ，希望通过 Full GC 来强迫已经无用的 DirectByteBuffer 对象释放掉它们关联的 Native Memory 

 HotSpot VM 只会在 Old GC 的时候才会对 Old 中的对象做 Reference Processing，而在 Young GC 时只会对 Young 里的对象做 Reference Processing。Young 中的 DirectByteBuffer 对象会在 Young GC 时被处理，也就是说，做 CMS GC 的话会对 Old 做 Reference Processing，进而能触发 Cleaner 对已死的 DirectByteBuffer 对象做清理工作。但如果很长一段时间里没做过 GC 或者只做了 Young GC 的话则不会在 Old 触发 Cleaner 的工作，那么就可能让本来已经死亡，但已经晋升到 Old 的 DirectByteBuffer 关联的 Native Memory 得不到及时释放。这几个实现特征使得依赖于 System.gc 触发 GC 来保证 DirectByteMemory 的清理工作能及时完成。如果打开了 `-XX:+DisableExplicitGC`，清理工作就可能得不到及时完成，于是就有发生 Direct Memory 的 OOM。 

**3 策略**

通过上面的分析看到，无论是保留还是去掉都会有一定的风险点，不过目前互联网中的 RPC 通信会大量使用 NIO，建议保留。此外 JVM 还提供了 `-XX:+ExplicitGCInvokesConcurrent` 和 `-XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses` 参数来将 System.gc 的触发类型从 Foreground 改为 Background，同时 Background 也会做 Reference Processing，这样的话就能大幅降低了 STW 开销，同时也不会发生 NIO Direct Memory OOM。

**4 小结**

不止 CMS，在 G1 或 ZGC中开启 `ExplicitGCInvokesConcurrent` 模式，都会采用高性能的并发收集方式进行收集，不过还是建议在代码规范方面也要做好约束，规范好 System.gc 的使用。

P.S. HotSpot 对 System.gc 有特别处理，最主要的地方体现在一次 System.gc 是否与普通 GC 一样会触发 GC 的统计/阈值数据的更新，HotSpot 里的许多 GC 算法都带有自适应的功能，会根据先前收集的效率来决定接下来的 GC 中使用的参数，但 System.gc 默认不更新这些统计数据，避免用户强行 GC 对这些自适应功能的干扰（可以参考 -XX:+UseAdaptiveSizePolicyWithSystemGC 参数，默认是 false）。

# 3  **场景三：MetaSpace 区 OOM** 

1 **现象**

JVM 在启动后或者某个时间点开始，**MetaSpace 的已使用大小在持续增长，同时每次 GC 也无法释放，调大 MetaSpace 空间也无法彻底解决**。

2  **原因** 

在上层，MetaSpace 主要由 Klass Metaspace 和 NoKlass Metaspace 两大部分组成。

- **Klass MetaSpace：**就是用来存 Klass 的，就是 Class 文件在 JVM 里的运行时数据结构，这部分默认放在 Compressed Class Pointer Space 中，是一块连续的内存区域，紧接着 Heap。Compressed Class Pointer Space 不是必须有的，如果设置了 `-XX:-UseCompressedClassPointers`，或者 `-Xmx` 设置大于 32 G，就不会有这块内存，这种情况下 Klass 都会存在 NoKlass Metaspace 里。
- **NoKlass MetaSpace：**专门来存 Klass 相关的其他的内容，比如 Method，ConstantPool 等，可以由多块不连续的内存组成。虽然叫做 NoKlass Metaspace，但是也其实可以存 Klass 的内容，上面已经提到了对应场景。

具体的定义都可以在源码  shared/vm/memory/metaspace.hpp 中找到.

MetaSpace 的对象为什么无法释放，我们看下面两点：

- **MetaSpace 内存管理：**类和其元数据的生命周期与其对应的类加载器相同，只要类的类加载器是存活的，在 Metaspace 中的类元数据也是存活的，不能被回收。每个加载器有单独的存储空间，通过 ClassLoaderMetaspace 来进行管理 SpaceManager* 的指针，相互隔离的。
- **MetaSpace 弹性伸缩：**由于 MetaSpace 空间和 Heap 并不在一起，所以这块的空间可以不用设置或者单独设置，一般情况下避免 MetaSpace 耗尽 VM 内存都会设置一个 MaxMetaSpaceSize，在运行过程中，如果实际大小小于这个值，JVM 就会通过 `-XX:MinMetaspaceFreeRatio` 和 `-XX:MaxMetaspaceFreeRatio` 两个参数动态控制整个 MetaSpace 的大小，具体使用可以看 `MetaSpaceGC::compute_new_size()` 方法（下方代码），这个方法会在 CMSCollector 和 G1CollectorHeap 等几个收集器执行 GC 时调用。这个里面会根据 `used_after_gc`，`MinMetaspaceFreeRatio` 和 `MaxMetaspaceFreeRatio` 这三个值计算出来一个新的 `_capacity_until_GC` 值（水位线）。然后根据实际的 `_capacity_until_GC` 值使用 `MetaspaceGC::inc_capacity_until_GC()` 和 `MetaspaceGC::dec_capacity_until_GC()` 进行 expand 或 shrink，这个过程也可以参照场景一中的伸缩模型进行理解。

 一般 `-XX:MetaSpaceSize` 和 `-XX:MaxMetaSpaceSize` 两个值设置为固定的，但是这样也会导致在空间不够的时候无法扩容，然后频繁地触发 GC，最终 OOM。所以关键原因就是 ClassLoader 不停地在内存中 load 了新的 Class ，一般这种问题都发生在动态类加载等情况上。 



3**策略**

了解大概什么原因后，如何定位和解决就很简单了，可以 dump 快照之后通过 JProfiler 或 MAT 观察 Classes 的 Histogram（直方图） 即可，或者直接通过命令即可定位， jcmd 打几次 Histogram 的图，看一下具体是哪个包下的 Class 增加较多就可以定位了。不过有时候也要结合InstBytes、KlassBytes、Bytecodes、MethodAll 等几项指标综合来看下。如下图便是笔者使用 jcmd 排查到一个 Orika 的问题。

```go
jcmd <PID> GC.class_stats|awk '{print$13}'|sed  's/\(.*\)\.\(.*\)/\1/g'|sort |uniq -c|sort -nrk1
```

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RmF5SnpwWmpBMGliellyOU5DSk1ncnN4M2U1dExpY0FTQWpMTGZsbUltTWdUZmNtdGZPaWNBc25BUS82NDA.png)



# 4  **场景四： Young GC 频繁  过早晋升** 

 **1 现象** 

这种场景主要发生在分代的收集器上面，专业的术语称为“Premature Promotion”。90% 的对象朝生夕死，只有在 Young 区经历过几次 GC 的洗礼后才会晋升到 Old 区，每经历一次 GC 对象的 GC Age 就会增长 1，最大通过 `-XX:MaxTenuringThreshold` 来控制。

过早晋升一般不会直接影响 GC，总会伴随着浮动垃圾、大对象担保失败等问题，但这些问题不是立刻发生的，我们可以观察以下几种现象来判断是否发生了过早晋升。

**分配速率接近于晋升速率**，对象晋升年龄较小。

GC 日志中出现“Desired survivor size 107347968 bytes, **new threshold 1(max 6)**”等信息，说明此时经历过一次 GC 就会放到 Old 区。

**Full GC 比较频繁**，且经历过一次 GC 之后 Old 区的**变化比例非常大**。

比如说 Old 区触发的回收阈值是 80%，经历过一次 GC 之后下降到了 10%，这就说明 Old 区的 70% 的对象存活时间其实很短，如下图所示，Old 区大小每次 GC 后从 2.1G 回收到 300M，也就是说回收掉了 1.8G 的垃圾，只有 **300M 的活跃对象**。整个 Heap 目前是 4G，活跃对象只占了不到十分之一。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkhEWkhVaktYOUR0ejdWcnhDNTVCM2FvNFNKaWFuMXBlOHlmMHhwdERJQ0pFek1hWFl6NEV3bWcvNjQw.png)

后果：

- Young GC 频繁，总的吞吐量下降。
- Full GC 频繁，可能会有较大停顿。

**2 原因**

- **Young/Eden 区过小：**过小的直接后果就是 Eden 被装满的时间变短，本应该回收的对象参与了 GC 并晋升，**Young GC 采用的是复制算法**，由基础篇我们知道 copying 耗时远大于 mark，也就是 Young GC 耗时本质上就是 copy 的时间（CMS 扫描 Card Table 或 G1 扫描 Remember Set 出问题的情况另说），没来及回收的对象增大了回收的代价，所以 Young GC  时间增加，同时又无法快速释放空间，Young GC 次数也跟着增加。
- **分配速率过大：**可以观察出问题前后 Mutator 的分配速率，如果有明显波动可以尝试观察网卡流量、存储类中间件慢查询日志等信息，看是否有大量数据被加载到内存中。

同时无法 GC 掉对象还会带来另外一个问题，引发动态年龄计算：JVM 通过 `-XX:MaxTenuringThreshold` 参数来控制晋升年龄，每经过一次 GC，年龄就会加一，达到最大年龄就可以进入 Old 区，最大值为 15（因为 JVM 中使用 4 个比特来表示对象的年龄）。设定固定的 MaxTenuringThreshold 值作为晋升条件：

- MaxTenuringThreshold 如果设置得过大，原本应该晋升的对象一直停留在 Survivor 区，直到 Survivor 区溢出，一旦溢出发生，Eden + Survivor 中对象将不再依据年龄全部提升到 Old 区，这样对象老化的机制就失效了。
- MaxTenuringThreshold 如果设置得过小，过早晋升即对象不能在 Young 区充分被回收，大量短期对象被晋升到 Old 区，Old 区空间迅速增长，引起频繁的 Major GC，分代回收失去了意义，严重影响 GC 性能。

相同应用在不同时间的表现不同，特殊任务的执行或者流量成分的变化，都会导致对象的生命周期分布发生波动，那么固定的阈值设定，因为无法动态适应变化，会造成和上面问题，所以 Hotspot 会使用动态计算的方式来调整晋升的阈值。具体动态计算可以看一下 Hotspot 源码 ，具体在 /src/hotspot/share/gc/shared/ageTable.cpp 的 `compute_tenuring_threshold` 方法中 

 可以看到 Hotspot 遍历所有对象时，从所有年龄为 0 的对象占用的空间开始累加，如果加上年龄等于 n 的所有对象的空间之后，使用 Survivor 区的条件值（TargetSurvivorRatio / 100，TargetSurvivorRatio 默认值为 50）进行判断，若大于这个值则结束循环，将 n 和 MaxTenuringThreshold 比较，若 n 小，则阈值为 n，若 n 大，则只能去设置最大阈值为 MaxTenuringThreshold。**动态年龄触发后导致更多的对象进入了 Old 区，造成资源浪费**。 

**3 策略**

知道问题原因后我们就有解决的方向，如果是 **Young/Eden 区过小**，我们可以在总的 Heap 内存不变的情况下适当增大 Young 区，具体怎么增加？一般情况下 **Old 的大小应当为活跃对象的 2~3 倍左右，考虑到浮动垃圾问题最好在 3 倍左右，剩下的都可以分给 Young 区**。

拿笔者的一次典型过早晋升优化来看，原配置为 Young 1.2G + Old 2.8G，通过观察 CMS GC 的情况找到存活对象大概为 300~400M，于是调整 Old 1.5G 左右，剩下 2.5G 分给 Young 区。仅仅调了一个 Young 区大小参数（`-Xmn`），整个 JVM 一分钟 Young GC 从 26 次降低到了 11 次，单次时间也没有增加，总的 GC 时间从 1100ms 降低到了 500ms，CMS GC 次数也从 40 分钟左右一次降低到了 7 小时 30 分钟一次。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rm54Rkd3N09jSjFCWkV5Ulo0QXg0ZlpySjZwUGVqeEVwV1RwNjRJVnZPV0tWZU83SDBPT2M5QS82NDA.png)![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RnhVQXBBWVN1MHltVUdSb0ZiZ1RnN2lidlREUWNqelhKcnRkcDQzRjRpYXYwN01uVElUNWh5NmdBLzY0MA.png)

- **偶发较大**：通过内存分析工具找到问题代码，从业务逻辑上做一些优化。
- **一直较大**：当前的 Collector 已经不满足 Mutator 的期望了，这种情况要么扩容 Mutator 的 VM，要么调整 GC 收集器类型或加大空间。

**4 小结**

过早晋升问题一般不会特别明显，但日积月累之后可能会爆发一波收集器退化之类的问题，所以我们还是要提前避免掉的，可以看看自己系统里面是否有这些现象，如果比较匹配的话，可以尝试优化一下。一行代码优化的 ROI 还是很高的。

如果在观察 Old 区前后比例变化的过程中，发现可以回收的比例非常小，如从 80% 只回收到了 60%，说明我们大部分对象都是存活的，Old 区的空间可以适当调大些。

**5 NewRatio 值**

关于在调整 Young 与 Old 的比例时，如何选取具体的 NewRatio 值，这里将问题抽象成为一个蓄水池模型，找到以下关键衡量指标，大家可以根据自己场景进行推算。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RmliaWFEYlNuWGFmYnlEQ0xzS2JGdFNkcnE5c0hpYTNEUGR3OVhlczdRQUgwOVlzZkJkMHpuTzhjdy82NDA.jpg) 

# 5  **场景五：CMS Old GC 频繁** 

**1 现象**

Old 区频繁的做 CMS GC，但是每次耗时不是特别长，整体最大 STW 也在可接受范围内，但由于 GC 太频繁导致吞吐下降比较多

**2 原理**

这种情况比较常见，基本都是一次 Young GC 完成后，负责处理 CMS GC 的一个后台线程 concurrentMarkSweepThread 会不断地轮询，使用 `shouldConcurrentCollect()` 方法做一次检测，判断是否达到了回收条件。如果达到条件，使用 `collect_in_background()` 启动一次 Background 模式 GC。轮询的判断是使用 `sleepBeforeNextCycle()` 方法，间隔周期为 `-XX:CMSWaitDuration` 决定，默认为2s。

具体代码在：src/hotspot/share/gc/cms/concurrentMarkSweepThread.cpp。

触发 GC，分为以下几种情况：

- **触发 CMS GC：**通过调用 `_collector->collect_in_background()` 进行触发 Background GC 。
  - CMS 默认采用 JVM 运行时的统计数据判断是否需要触发 CMS GC，如果需要根据 `-XX:CMSInitiatingOccupancyFraction` 的值进行判断，需要设置参数 `-XX:+UseCMSInitiatingOccupancyOnly`。
  - 如果开启了 `-XX:UseCMSInitiatingOccupancyOnly` 参数，判断当前 Old 区使用率是否大于阈值，则触发 CMS GC，该阈值可以通过参数 `-XX:CMSInitiatingOccupancyFraction` 进行设置，如果没有设置，默认为 92%。
  - 如果之前的 Young GC 失败过，或者下次 Young 区执行 Young GC 可能失败，这两种情况下都需要触发 CMS GC。
  - CMS 默认不会对 MetaSpace 或 Perm 进行垃圾收集，如果希望对这些区域进行垃圾收集，需要设置参数 `-XX:+CMSClassUnloadingEnabled`。
- **触发 Full GC：**直接进行 Full GC，这种情况到场景七中展开说明。
  - 如果 `_full_gc_requested` 为真，说明有明确的需求要进行 GC，比如调用 System.gc。
  - 在 Eden 区为对象或 TLAB 分配内存失败，导致一次 Young GC，在 `GenCollectorPolicy` 类的 `satisfy_failed_allocation()` 方法中进行判断。

**3 策略**

最常见的达到回收比例这个场景来说，与过早晋升不同的是这些对象确实存活了一段时间，Survival Time 超过了 TP9999 时间，但是又达不到长期存活，如各种数据库、网络链接，带有失效时间的缓存等。

处理这种常规内存泄漏问题基本是一个思路，主要步骤如下：

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkxtblN1ZEg3U3htdWdHMlNLakFIZmR0THh4M2liWmg2c0dBYXFIbkxlYk9WTkVDVUJVOWpLaWNBLzY0MA.jpg)

- **内存 Dump：**使用 jmap、arthas 等 dump 堆进行快照时记得摘掉流量，同时**分别在 CMS GC 的发生前后分别 dump 一次**。
- **分析 Top Component：**要记得按照对象、类、类加载器、包等多个维度观察 Histogram，同时使用 outgoing 和 incoming 分析关联的对象，另外就是 Soft Reference 和 Weak Reference、Finalizer 等也要看一下。
- **分析 Unreachable：**重点看一下这个，关注下 Shallow 和 Retained 的大小。如下图所示，笔者之前一次 GC 优化，就根据 Unreachable Objects 发现了 Hystrix 的滑动窗口问题。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RjVIQm14aWFubEFuZ2FXMlN3VXdna05wMVBVV0FraWEzSFg5SXhJZFZHN1lEOHpVeUJFekJSU1BnLzY0MA.png)

经过整个流程下来基本就能定位问题了，不过在优化的过程中记得使用**控制变量**的方法来优化，防止一些会加剧问题的改动被掩盖

 # 6 场景六：单次 CMS Old GC 耗时长

**1 现象**

CMS GC 单次 STW 最大超过 1000ms，不会频繁发生，如下图所示最长达到了 8000ms。某些场景下会引起“雪崩效应”，这种场景非常危险。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rk1zMXppYlNncHZiTE9YOFVPS3p5a1lhdktndXJKU2tGQU8xN0Nac0xnNXVXQ0RXS1B3NFNnQWcvNjQw.png)



**2 原因**

CMS 在回收的过程中，STW 的阶段主要：

1 **init Mark 和 Final Remark 这两个阶段，也是导致 CMS Old GC 最多的原因**

2在 STW 前等待 Mutator 的线程到达 SafePoint 也会导致时间过长，但这种情况较少。

3 发生收集器退化或者碎片压缩的场景请看场景七。



此处主要讨论 init Mark 和 Final Remark。

两个阶段为什么会耗时的原因：核心代码都在 /src/hotspot/share/gc/cms/concurrentMarkSweepGeneration.cpp 

内部有个线程 ConcurrentMarkSweepThread 轮询来校验，Old 区的垃圾回收相关细节被完全封装在 `CMSCollector` 中，调用入口就是 ConcurrentMarkSweepThread 调用的 `CMSCollector::collect_in_background` 和 `ConcurrentMarkSweepGeneration` 调用的 `CMSCollector::collect` 方法。此处我们讨论大多数场景的 `collect_in_background`。整个过程中会 **STW 的主要是 initial Mark 和 Final Remark，核心代码在 `VM_CMS_Initial_Mark` / `VM_CMS_Final_Remark` 中，执行时需要将执行权交由 VMThread 来执行**。



**1 CMS Init Mark**执行步骤，实现在 `CMSCollector::checkpointRootsInitialWork()` 和 `CMSParInitialMarkTask::work` 中，整体步骤和代码如下：

CMSCollector::checkpointRootsInitialWork()

 ![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RnY0clVTTlBVU05TcDJROUlFZzhFWk9PTjExbW9HUnd1Y2JrRVhpY1dZSEIwWmt5bnVRRFlFUGcvNjQw-1608367554525.jpg) 



整个过程比较简单，从 GC Root 出发标记 Old 中的对象，处理完成后借助 BitMap 处理下 Young 区对 Old 区的引用，**整个过程基本都比较快，很少会有较大的停顿**。

**2  CMS Final Remark** 执行步骤，实现在 `CMSCollector::checkpointRootsFinalWork()` 中 

 ![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rkk3ODBLckhXc3FKYVd2MTNIZElvRTVaWTJOenIyUTBKdnR3NnBJQWNjOVlnR2czMjJNaWI2NHcvNjQw-1608367695193.jpg) 

Final Remark 是最终的第二次标记，这种情况只有在 Background GC 执行了 InitialMarking 步骤的情形下才会执行，如果是 Foreground GC 执行的 InitialMarking 步骤则不需要再次执行 FinalRemark。Final Remark 的开始阶段与 Init Mark 处理的流程相同，但是后续多了 Card Table 遍历、Reference 实例的清理并将其加入到 Reference 维护的 `pend_list` 中，如果要收集元数据信息，还要清理 SystemDictionary、CodeCache、SymbolTable、StringTable 等组件中不再使用的资源。

**3 策略**

 大部分问题都出在 Final Remark 过程，拿这个场景来举例，主要步骤： 

1  **【方向】**观察详细 GC 日志，找到出问题时 Final Remark 日志，分析下 Reference 处理和元数据处理 real 耗时是否正常，详细信息需要通过 `-XX:+PrintReferenceGC` 参数开启。**基本在日志里面就能定位到大概是哪个方向出了问题，耗时超过 10% 的就需要关注**。 

```java
2019-02-27T19:55:37.920+0800: 516952.915: [GC (CMS Final Remark) 516952.915: [ParNew516952.939: [SoftReference, 0 refs, 0.0003857 secs]516952.939: [WeakReference, 1362 refs, 0.0002415 secs]516952.940: [FinalReference, 146 refs, 0.0001233 secs]516952.940: [PhantomReference, 0 refs, 57 refs, 0.0002369 secs]516952.940: [JNI Weak Reference, 0.0000662 secs]
[class unloading, 0.1770490 secs]516953.329: [scrub symbol table, 0.0442567 secs]516953.373: [scrub string table, 0.0036072 secs][1 CMS-remark: 1638504K(2048000K)] 1667558K(4352000K), 0.5269311 secs] [Times: user=1.20 sys=0.03, real=0.53 secs]
```



**2 【根因】**一般来说最容易出问题的地方就是 Reference 中的 **FinalReference** 和元数据信息处理中的 **scrub symbol table** 两个阶段，想要找到具体问题代码就需要内存分析工具 MAT 或 JProfiler 了，注意要 dump 即将开始 CMS GC 的堆。在用 MAT 等工具前也可以先用命令行看下对象 Histogram，有可能直接就能定位问题。

- 对 FinalReference 的分析主要观察 `java.lang.ref.Finalizer` 对象的 dominator tree，找到泄漏的来源。经常会出现问题的几个点有 Socket 的 `SocksSocketImpl` 、Jersey 的 `ClientRuntime`、MySQL 的 `ConnectionImpl` 等等。
- scrub symbol table 表示清理元数据符号引用耗时，符号引用是 Java 代码被编译成字节码时，方法在 JVM 中的表现形式，生命周期一般与 Class 一致，当 `_should_unload_classes` 被设置为 true 时在 `CMSCollector::refProcessingWork()` 中与 Class Unload、String Table 一起被处理。

**3【策略】**有很多时候单台 STW 的时间会比较长，如果业务影响比较大，及时摘掉流量，具体后续优化策略如下：

1 FinalReference：找到内存来源后通过优化代码的方式 。如果短时间无法定位可以增加 `-XX:+ParallelRefProcEnabled` 对 Reference 进行并行处理。

2 symbol table：观察 MetaSpace 区的历史使用峰值，以及每次 GC 前后的回收情况，一般没有使用动态类加载或者 DSL 处理等，MetaSpace 的使用率上不会有什么变化，这种情况**可以通过 `-XX:-CMSClassUnloadingEnabled` 来避免 MetaSpace 的处理，JDK8 会默认开启 CMSClassUnloadingEnabled，这会使得 CMS 在 CMS-Remark 阶段尝试进行类的卸**载。

**4 小结**

正常情况进行的 Background CMS GC，出现问题基本都集中在 Reference 和 Class 等元数据处理上，

1 在 Reference 类的问题处理方面，不管是 FinalReference，还是 SoftReference、WeakReference 核心的手段就是找准时机 dump 快照，然后用内存分析工具来分析。

2 Class 处理方面目前除了关闭类卸载开关，没有太好的方法。

在 G1 中同样有 Reference 的问题，可以观察日志中的 Ref Proc，处理方法与 CMS 类似



# 7  **场景七：内存碎片&收集器退化** 

**1 现象**

并发的 CMS GC 算法，退化为 Foreground 单线程串行 GC 模式，STW 时间超长，有时会长达十几秒。其中 CMS 收集器退化后单线程串行 GC 算法有两种：

- 带压缩动作的算法，称为 MSC，上面我们介绍过，使用标记-清理-压缩，单线程全暂停的方式，对整个堆进行垃圾收集，也就是真正意义上的 Full GC，暂停时间要长于普通 CMS。
- 不带压缩动作的算法，收集 Old 区，和普通的 CMS 算法比较相似，暂停时间相对 MSC 算法短一些。

**2 原因**

CMS 发生收集器退化：

**（1）晋升失败（Promotion Failed）**

晋升失败就是指在进行 Young GC 时，Survivor 放不下，对象只能放入 Old，但此时 Old 也放不下。直觉上乍一看这种情况可能会经常发生，但其实因为有 concurrentMarkSweepThread 和担保机制的存在，发生的条件是很苛刻的，除非是短时间将 Old 区的剩余空间迅速填满，例如上文中说的动态年龄判断导致的过早晋升（见下文的增量收集担保失败）。另外还有一种情况就是内存碎片导致的 Promotion Failed，Young GC 以为 Old 有足够的空间，结果到分配时，晋级的大对象找不到连续的空间存放。

使用 CMS 作为 GC 收集器时，运行过一段时间的 Old 区如下图所示，清除算法导致内存出现多段的不连续，出现大量的内存碎片。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rkc1bUZSWDZXV1Y2WFFRVWV0bEl5a1BmYnlYR2NpYjMzZXd2UTd4MkxhN3c2RHI5MU5laWN3YVd3LzY0MA.png)

**（2）空间分配效率较低**：上文已经提到过，如果是连续的空间 JVM 可以通过使用 pointer bumping 的方式来分配，而对于这种有大量碎片的空闲链表则需要逐个访问 freelist 中的项来访问，查找可以存放新建对象的地址。

**（3）空间利用效率变低**：Young 区晋升的对象大小大于了连续空间的大小，那么将会触发 Promotion Failed ，即使整个 Old 区的容量是足够的，但由于其不连续，也无法存放新对象，也就是本文所说的问题。

**（4）增量收集担保失败**

分配内存失败后，会判断统计得到的 Young GC 晋升到 Old 的平均大小，以及当前 Young 区已使用的大小也就是最大可能晋升的对象大小，是否大于 Old 区的剩余空间。只要 CMS 的剩余空间比前两者的任意一者大，CMS 就认为晋升还是安全的，反之，则代表不安全，不进行Young GC，直接触发Full GC。

**（5）显式 GC**

这种情况参见场景二。

**（6）并发模式失败（Concurrent Mode Failure）**

**最后一种情况，也是发生概率较高**的一种，在 GC 日志中经常能看到 Concurrent Mode Failure 关键字。这种是由于并发 Background CMS GC 正在执行，同时又有 Young GC 晋升的对象要放入到了 Old 区中，而此时 Old 区空间不足造成的。

为什么 CMS GC 正在执行还会导致收集器退化呢？主要是由于 CMS 无法处理浮动垃圾（Floating Garbage）引起的。CMS 的并发清理阶段，Mutator 还在运行，因此不断有新的垃圾产生，而这些垃圾不在这次清理标记的范畴里，无法在本次 GC 被清除掉，这些就是浮动垃圾，除此之外在 Remark 之前那些断开引用脱离了读写屏障控制的对象也算浮动垃圾。所以 **Old 区回收的阈值不能太高，否则预留的内存空间很可能不够，从而导致 Concurrent Mode Failure 发生**。

**3 策略**

分析到具体原因后，我们就可以针对性解决了，具体思路还是从根因出发，具体解决策略：

- **内存碎片：**通过配置 `-XX:UseCMSCompactAtFullCollection=true` 来控制 Full GC的过程中是否进行空间的整理（默认开启，注意是Full GC，不是普通CMS GC），以及 `-XX: CMSFullGCsBeforeCompaction=n` 来控制多少次 Full GC 后进行一次压缩。
- **增量收集：**降低触发 CMS GC 的阈值，即参数 `-XX:CMSInitiatingOccupancyFraction` 的值，让 CMS GC 尽早执行，以保证有足够的连续空间，也减少 Old 区空间的使用大小，另外需要使用 `-XX:+UseCMSInitiatingOccupancyOnly` 来配合使用，不然 JVM 仅在第一次使用设定值，后续则自动调整。
- **浮动垃圾：**视情况控制每次晋升对象的大小，或者缩短每次 CMS GC 的时间，必要时可调节 NewRatio 的值。另外就是使用 `-XX:+CMSScavengeBeforeRemark` 在过程中提前触发一次 Young GC，防止后续晋升过多对象。

**4 小结**

正常情况下触发并发模式的 CMS GC，停顿非常短，对业务影响很小，但 CMS GC 退化后，影响会非常大，建议发现一次后就彻底根治。只要能定位到内存碎片、浮动垃圾、增量收集相关等具体产生原因，还是比较好解决的，关于内存碎片这块，**如果 `-XX:CMSFullGCsBeforeCompaction` 的值不好选取的话，可以使用 `-XX:PrintFLSStatistics` 来观察内存碎片率情况，然后再设置具体的值**。

最后就是在编码的时候也要避免需要连续地址空间的大对象的产生，如过长的字符串，用于存放附件、序列化或反序列化的 byte 数组等，还有就是过早晋升问题尽量在爆发问题前就避免掉。



# 8 场景八：堆外内存 OOM

**1 现象**

内存使用率不断上升，甚至开始使用 SWAP 内存，同时可能出现 GC 时间飙升，线程被 Block 等现象，**通过 top 命令发现 Java 进程的 RES 甚至超过了** **`-Xmx` 的大小**。出现这些现象时，基本可以确定是出现了堆外内存泄漏。

**2 原因**

JVM 的堆外内存泄漏：

1 通过 `UnSafe#allocateMemory`，`ByteBuffer#allocateDirect` 主动申请了堆外内存而没有释放，常见于 NIO、Netty 等相关组件。

2 代码中有通过 JNI 调用 Native Code 申请的内存没有释放。

**3 策略**

首先，我们需要确定是哪种原因导致的堆外内存泄漏。这里可以使用 NMT（[NativeMemoryTracking](https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr007.html)） 进行分析。在项目中添加 `-XX:NativeMemoryTracking=detail` JVM参数后重启项目（需要注意的是，打开 NMT 会带来 5%~10% 的性能损耗）。使用命令 `jcmd pid VM.native_memory detail` 查看内存分布。重点观察 total 中的 committed，因为 jcmd 命令显示的内存包含堆内内存、Code 区域、通过 `Unsafe.allocateMemory` 和 `DirectByteBuffer` 申请的内存，但是不包含其他 Native Code（C 代码）申请的堆外内存。

**如果 total 中的 committed 和 top 中的 RES 相差不大，则应为主动申请的堆外内存未释放造成的，如果相差较大，则基本可以确定是 JNI 调用造成的。**

**原因一：主动申请未释放**

JVM 使用 `-XX:MaxDirectMemorySize=size` 参数来控制可申请的堆外内存的最大值。在 Java 8 中，如果未配置该参数，默认和 `-Xmx` 相等。

NIO 和 Netty 都会取 `-XX:MaxDirectMemorySize` 配置的值，来限制申请的堆外内存的大小。NIO 和 Netty 中还有一个计数器字段，用来计算当前已申请的堆外内存大小，NIO 中是 `java.nio.Bits#totalCapacity`、Netty 中 `io.netty.util.internal.PlatformDependent#DIRECT_MEMORY_COUNTER`。

当申请堆外内存时，NIO 和 Netty 会比较计数器字段和最大值的大小，如果计数器的值超过了最大值的限制，会抛出 OOM 的异常。

NIO 中是：`OutOfMemoryError: Direct buffer memory`。

Netty 中是：`OutOfDirectMemoryError: failed to allocate capacity byte(s) of direct memory (used: usedMemory , max: DIRECT_MEMORY_LIMIT )`。

我们可以检查代码中是如何使用堆外内存的，NIO 或者是 Netty，通过反射，获取到对应组件中的计数器字段，并在项目中对该字段的数值进行打点，即可准确地监控到这部分堆外内存的使用情况。

此时，可以通过 Debug 的方式确定使用堆外内存的地方是否正确执行了释放内存的代码。另外，需要检查 JVM 的参数是否有 `-XX:+DisableExplicitGC` 选项，如果有就去掉，因为该参数会使 System.gc 失效。（场景二：显式 GC 的去与留）

**原因二：通过 JNI 调用的 Native Code 申请的内存未释放**

这种情况排查起来比较困难，我们可以通过 Google perftools + Btrace 等工具，帮助我们分析出问题的代码在哪里。

gperftools 是 Google 开发的一款非常实用的工具集，它的原理是在 Java 应用程序运行时，当调用 malloc 时换用它的 libtcmalloc.so，这样就能对内存分配情况做一些统计。我们使用 gperftools 来追踪分配内存的命令。如下图所示，通过 gperftools 发现 `Java_java_util_zip_Inflater_init` 比较可疑。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkMwZ2F3aWMyMjExNHNQOHM1QVRUQkdDU2JxRFZSb0JtTTVWb1hkUGppYnFSV3kyWENlSmFaaEdRLzY0MA.png)![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rm8ydjZmekxVclJKZnZaM2lhbzdQVHBmWTNSNWpGZGMweWljcTVpY2t0bVY1Q2pWUDc4NnJKcXNMUS82NDA.png)

最终定位到是，项目中对 `GIPInputStream` 的使用错误，没有正确的 close()。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rm1GSm9OVWE2akxsVkNmbDFzaWJFY2lhS25ON2pSeDVUdDZpYmpremRxRE50aFJoMGljVVhOd3BUb1EvNjQw.png)





**4 小结**

首先可以使用 NMT + jcmd 分析泄漏的堆外内存是哪里申请，确定原因后，使用不同的手段，进行原因定位。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkdHRVdUeWx4THV1MzJ1TFRLQ0NFNXVPU0ZKQ2JSeGliSlBEQ0R0Tm42OHJpYXN5SklHbkg5bnpRLzY0MA.png)

# 9 **场景九：JNI 引发的 GC 问题**（情况比较少）

**4.9.1 现象**

在 GC 日志中，出现 GC Cause 为 GCLocker Initiated GC。

```go
2020-09-23T16:49:09.727+0800: 504426.742: [GC (GCLocker Initiated GC) 504426.742: [ParNew (promotion failed): 209716K->6042K(1887488K), 0.0843330 secs] 1449487K->1347626K(3984640K), 0.0848963 secs] [Times: user=0.19 sys=0.00, real=0.09 secs]2020-09-23T16:49:09.812+0800: 504426.827: [Full GC (GCLocker Initiated GC) 504426.827: [CMS: 1341583K->419699K(2097152K), 1.8482275 secs] 1347626K->419699K(3984640K), [Metaspace: 297780K->297780K(1329152K)], 1.8490564 secs] [Times: user=1.62 sys=0.20, real=1.85 secs]
```

**4.9.2 原因**

JNI（Java Native Interface）意为 Java 本地调用，它允许 Java 代码和其他语言写的 Native 代码进行交互。

JNI 如果需要获取 JVM 中的 String 或者数组，有两种方式：

- 拷贝传递。
- 共享引用（指针），性能更高。

由于 Native 代码直接使用了 JVM 堆区的指针，如果这时发生 GC，就会导致数据错误。因此，在发生此类 JNI 调用时，禁止 GC 的发生，同时阻止其他线程进入 JNI 临界区，直到最后一个线程退出临界区时触发一次 GC。

GC Locker 实验：

```go
public class GCLockerTest {   static final int ITERS = 100;  static final int ARR_SIZE =  10000;  static final int WINDOW = 10000000;   static native void acquire(int[] arr);  static native void release(int[] arr);   static final Object[] window = new Object[WINDOW];   public static void main(String... args) throws Throwable {    System.loadLibrary("GCLockerTest");    int[] arr = new int[ARR_SIZE];     for (int i = 0; i < ITERS; i++) {      acquire(arr);      System.out.println("Acquired");      try {        for (int c = 0; c < WINDOW; c++) {          window[c] = new Object();        }      } catch (Throwable t) {        // omit      } finally {        System.out.println("Releasing");        release(arr);      }    }  }}
```

------

```go
#include <jni.h>#include "GCLockerTest.h" static jbyte* sink; JNIEXPORT void JNICALL Java_GCLockerTest_acquire(JNIEnv* env, jclass klass, jintArray arr) {sink = (*env)->GetPrimitiveArrayCritical(env, arr, 0);} JNIEXPORT void JNICALL Java_GCLockerTest_release(JNIEnv* env, jclass klass, jintArray arr) {(*env)->ReleasePrimitiveArrayCritical(env, arr, sink, 0);}
```

运行该 JNI 程序，可以看到发生的 GC 都是 GCLocker Initiated GC，并且注意在 “Acquired” 和 “Released” 时不可能发生 GC。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RkZlVFVCaFJZWFBraWI2UXlHQndEU0FBTmtUUFEydnB4VmtNaWFsSURIdGtXaWFjWnZiNVN5dHZidy82NDA.png)

- 如果此时是 Young 区不够 Allocation Failure 导致的 GC，由于无法进行 Young GC，会将对象直接分配至 Old 区。
- 如果 Old 区也没有空间了，则会等待锁释放，导致线程阻塞。
- 可能触发额外不必要的 Young GC，JDK 有一个 Bug，有一定的几率，本来只该触发一次 GCLocker Initiated GC 的 Young GC，实际发生了一次 Allocation Failure GC 又紧接着一次 GCLocker Initiated GC。是因为 GCLocker Initiated GC 的属性被设为 full，导致两次 GC 不能收敛。

**4.9.3 策略**

- 添加 `-XX+PrintJNIGCStalls` 参数，可以打印出发生 JNI 调用时的线程，进一步分析，找到引发问题的 JNI 调用。
- JNI 调用需要谨慎，不一定可以提升性能，反而可能造成 GC 问题。
- 升级 JDK 版本到 14，避免 [JDK-8048556](https://bugs.openjdk.java.net/browse/JDK-8048556) 导致的重复 GC。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5RjVjSmljaHVtZGlhaWM1dG05SDM0SWVUNFlCbVdieEVtMTlpYmFRbGZiMWNXa05EZjNCamFncndNY2cvNjQw.png)

JNI 产生的 GC 问题较难排查，需要谨慎使用。



# 五 调优建议：

1  根因鱼骨图

送上一张问题根因鱼骨图，一般情况下我们在处理一个 GC 问题时，只要能定位到问题的“病灶”，有的放矢，其实就相当于解决了 80%，如果在某些场景下不太好定位，大家可以借助这种根因分析图通过**排除法**去定位。

![img](1 jvm调优.assets/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy9oRXgwM2NGZ1VzWG5zQXhiQURpYW0waWNwTTJHb0RpYkl5Rm9Wb1NyWkl5Vm55b0Z5bGU4WnVnU1BpY1o1bG5Tb2ppYWFvb3hJWEE2RGliRGppYVlLOHB5TERRU3cvNjQw.png)



2 调优建议

- **Trade Off：**与 CAP 注定要缺一角一样，GC 优化要在延迟（Latency）、吞吐量（Throughput）、容量（Capacity）三者之间进行权衡。
- **最终手段：**GC 发生问题不是一定要对 JVM 的 GC 参数进行调优，大部分情况下是通过 GC 的情况找出一些业务问题，切记上来就对 GC 参数进行调整，当然有明确配置错误的场景除外。
- **控制变量：**控制变量法是在蒙特卡洛（Monte Carlo）方法中用于减少方差的一种技术方法，我们调优的时候尽量也要使用，每次调优过程尽可能只调整一个变量。
- **善用搜索：**理论上 99.99% 的 GC 问题基本都被遇到了，我们要学会使用搜索引擎的高级技巧，重点关注 StackOverFlow、Github 上的 Issue、以及各种论坛博客，先看看其他人是怎么解决的，会让解决问题事半功倍。能看到这篇文章，你的搜索能力基本过关了~
- **调优重点：**总体上来讲，我们开发的过程中遇到的问题类型也基本都符合正态分布，太简单或太复杂的基本遇到的概率很低，笔者这里将中间最重要的三个场景添加了“*”标识，希望阅读完本文之后可以观察下自己负责的系统，是否存在上述问题。
- **GC 参数：**如果堆、栈确实无法第一时间保留，一定要保留 GC 日志，这样我们最起码可以看到 GC Cause，有一个大概的排查方向。关于 GC 日志相关参数，最基本的 `-XX:+HeapDumpOnOutOfMemoryError` 等一些参数就不再提了，笔者建议添加以下参数，可以提高我们分析问题的效率。

其他建议

- 主动式 GC：**也有另开生面的做法，通过监控手段监控观测 Old 区的使用情况，即将到达阈值时将应用服务摘掉流量，手动触发一次 Major GC，减少 CMS GC 带来的停顿，但随之系统的健壮性也会减少，如非必要不建议引入。
- **禁用偏向锁：**偏向锁在只有一个线程使用到该锁的时候效率很高，但是在竞争激烈情况会升级成轻量级锁，此时就需要先**消除偏向锁，这个过程是 STW** 的。如果每个同步资源都走这个升级过程，开销会非常大，所以在已知并发激烈的前提下，一般会禁用偏向锁 `-XX:-UseBiasedLocking` 来提高性能。
- **虚拟内存：**启动初期有些操作系统（例如 Linux）并没有真正分配物理内存给 JVM ，而是在虚拟内存中分配，使用的时候才会在物理内存中分配内存页，这样也会导致 GC 时间较长。这种情况可以添加 `-XX:+AlwaysPreTouch` 参数，让 VM 在 commit 内存时跑个循环来强制保证申请的内存真的 commit，避免运行时触发缺页异常。在一些大内存的场景下，有时候能将前几次的 GC 时间降一个数量级，但是添加这个参数后，启动的过程可能会变慢。

# 参考

1 https://blog.csdn.net/MeituanTech/article/details/109664525 美团cms



