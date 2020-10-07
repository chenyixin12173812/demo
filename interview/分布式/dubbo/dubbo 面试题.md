dubbo 面试题



 20、什么是 RPC?RPC原理是什么? 

 21、Dubbo 工作原理？ 

 22、解释一下什么是负载均衡？ 

 23、看看 Dubbo 提供的负载均衡策略？ 

 24、zoo[keep]()er宕机与dubbo直连的情况

**1、Dubbo是什么？**

Dubbo是阿里巴巴开源的基于 Java 的高性能 RPC 分布式服务框架，现已成为 Apache 基金会孵化项目。

面试官问你如果这个都不清楚，那下面的就没必要问了。

> 官网：http://dubbo.apache.org

**2、为什么要用Dubbo？**

因为是阿里开源项目，国内很多互联网公司都在用，已经经过很多线上考验。内部使用了 Netty、Zookeeper，保证了高性能高可用性。

使用 Dubbo 可以将核心业务抽取出来，作为独立的服务，逐渐形成稳定的服务中心，可用于提高业务复用灵活扩展，使前端应用能更快速的响应多变的市场需求。

下面这张图可以很清楚的诠释，最重要的一点是，分布式架构可以承受更大规模的并发流量。

![img](https://img-blog.csdn.net/20181002113705457?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

下面是 Dubbo 的服务治理图。

![img](https://img-blog.csdn.net/20181002113717176?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

**3、Dubbo 和 Spring Cloud 有什么区别？**

两个没关联，如果硬要说区别，有以下几点。

1）通信方式不同

Dubbo 使用的是 RPC 通信，而 Spring Cloud 使用的是 HTTP RESTFul 方式。

2）组成部分不同

![img](https://img-blog.csdn.net/20181002113728152?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

**4、dubbo都支持什么协议，推荐用哪种？**

- dubbo://（推荐）
- rmi://
- hessian://
- http://
- webservice://
- thrift://
- memcached://
- redis://
- rest://

**5、Dubbo需要 Web 容器吗？**

不需要，如果硬要用 Web 容器，只会增加复杂性，也浪费资源。

**6、Dubbo内置了哪几种服务容器？**

- Spring Container
- Jetty Container
- Log4j Container

Dubbo 的服务容器只是一个简单的 Main 方法，并加载一个简单的 Spring 容器，用于暴露服务。

**7、Dubbo里面有哪几种节点角色？**

 

![img](https://img-blog.csdn.net/20181002113745869?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**8、画一画服务注册与发现的流程图**

![img](https://img-blog.csdn.net/20181002113850939?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

该图来自 Dubbo 官网，供你参考，如果你说你熟悉 Dubbo, 面试官经常会让你画这个图，记好了。

**9、Dubbo默认使用什么注册中心，还有别的选择吗？**

推荐使用 Zookeeper 作为注册中心，还有 Redis、Multicast、Simple 注册中心，但不推荐。

**10、Dubbo有哪几种配置方式？**

1）Spring 配置方式
2）Java API 配置方式

**11、Dubbo 核心的配置有哪些？**

我曾经面试就遇到过面试官让你写这些配置，我也是蒙逼。。

 

![img](https://img-blog.csdn.net/20181002113904858?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

配置之间的关系见下图。

 

![img](https://img-blog.csdn.net/20181002113916948?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**12、在 Provider 上可以配置的 Consumer 端的属性有哪些？**

1）timeout：方法调用超时
2）retries：失败重试次数，默认重试 2 次
3）loadbalance：负载均衡算法，默认随机
4）actives 消费者端，最大并发调用限制

**13、Dubbo启动时如果依赖的服务不可用会怎样？**

Dubbo 缺省会在启动时检查依赖的服务是否可用，不可用时会抛出异常，阻止 Spring 初始化完成，默认 check="true"，可以通过 check="false" 关闭检查。

**14、Dubbo推荐使用什么序列化框架，你知道的还有哪些？**

推荐使用Hessian序列化，还有Duddo、FastJson、Java自带序列化。

**15、Dubbo默认使用的是什么通信框架，还有别的选择吗？**

Dubbo 默认使用 Netty 框架，也是推荐的选择，另外内容还集成有Mina、Grizzly。

**16、Dubbo有哪几种集群容错方案，默认是哪种？**

 

![img](https://img-blog.csdn.net/20181002113930188?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**17、Dubbo有哪几种负载均衡策略，默认是哪种？**

 

![img](https://img-blog.csdn.net/20181002113941952?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

**18、注册了多个同一样的服务，如果测试指定的某一个服务呢？**

可以配置环境点对点直连，绕过注册中心，将以服务接口为单位，忽略注册中心的提供者列表。

**19、Dubbo支持服务多协议吗？**

Dubbo 允许配置多协议，在不同服务上支持不同协议或者同一服务上同时支持多种协议。

**20、当一个服务接口有多种实现时怎么做？**

当一个接口有多种实现时，可以用 group 属性来分组，服务提供方和消费方都指定同一个 group 即可。

**21、服务上线怎么兼容旧版本？**

可以用版本号（version）过渡，多个不同版本的服务注册到注册中心，版本号不同的服务相互间不引用。这个和服务分组的概念有一点类似。

**22、Dubbo可以对结果进行缓存吗？**

可以，Dubbo 提供了声明式缓存，用于加速热门数据的访问速度，以减少用户加缓存的工作量。

**23、Dubbo服务之间的调用是阻塞的吗？**

默认是同步等待结果阻塞的，支持异步调用。

Dubbo 是基于 NIO 的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个 Future 对象。

异步调用流程图如下。

![img](https://img-blog.csdn.net/20181002113955917?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

**24、Dubbo支持分布式事务吗？**

目前暂时不支持，后续可能采用基于 JTA/XA 规范实现，如以图所示。

![img](https://img-blog.csdn.net/20181002114007181?watermark/2/text/aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L21vYWt1bg==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70)

 

**25、Dubbo telnet 命令能做什么？**

dubbo 通过 telnet 命令来进行服务治理，具体使用看这篇文章《[dubbo服务调试管理实用命令](https://mp.weixin.qq.com/s?__biz=MzI3ODcxMzQzMw==&mid=2247483709&idx=1&sn=afe0688c184f00902529583a85d90089&scene=21#wechat_redirect)》。

> telnet localhost 8090

**26、Dubbo支持服务降级吗？**

Dubbo 2.2.0 以上版本支持。

**27、Dubbo如何优雅停机？**

Dubbo 是通过 JDK 的 ShutdownHook 来完成优雅停机的，所以如果使用 kill -9 PID 等强制关闭指令，是不会执行优雅停机的，只有通过 kill PID 时，才会执行。

**28、服务提供者能实现失效踢出是什么原理？**

服务失效踢出基于 Zookeeper 的临时节点原理。

**29、如何解决服务调用链过长的问题？**

Dubbo 可以使用 Pinpoint 和 Apache Skywalking(Incubator) 实现分布式服务追踪，当然还有其他很多方案。

**30、服务读写推荐的容错策略是怎样的？**

读操作建议使用 Failover 失败自动切换，默认重试两次其他服务器。

写操作建议使用 Failfast 快速失败，发一次调用失败就立即报错。

**31、Dubbo必须依赖的包有哪些？**

Dubbo 必须依赖 JDK，其他为可选。

**32、Dubbo的管理控制台能做什么？**

管理控制台主要包含：路由规则，动态配置，服务降级，访问控制，权重调整，负载均衡，等管理功能。

**33、说说 Dubbo 服务暴露的过程。**

Dubbo 会在 Spring 实例化完 bean 之后，在刷新容器最后一步发布 ContextRefreshEvent 事件的时候，通知实现了 ApplicationListener 的 ServiceBean 类进行回调 onApplicationEvent 事件方法，Dubbo 会在这个方法中调用 ServiceBean 父类 ServiceConfig 的 export 方法，而该方法真正实现了服务的（异步或者非异步）发布。

**34、Dubbo 停止维护了吗？**

2014 年开始停止维护过几年，17 年开始重新维护，并进入了 Apache 项目。

**35、Dubbo 和 Dubbox 有什么区别？**

Dubbox 是继 Dubbo 停止维护后，当当网基于 Dubbo 做的一个扩展项目，如加了服务可 Restful 调用，更新了开源组件等。

**36、你还了解别的分布式框架吗？**

别的还有 Spring cloud、Facebook 的 Thrift、Twitter 的 Finagle 等。

**37、Dubbo 能集成 Spring Boot 吗？**

可以的，项目地址如下。

> https://github.com/apache/incubator-dubbo-spring-boot-project

**38、在使用过程中都遇到了些什么问题？**

Dubbo 的设计目的是为了满足高并发小数据量的 rpc 调用，在大数据量下的性能表现并不好，建议使用 rmi 或 http 协议。

**39、你读过 Dubbo 的源码吗？**

要了解 Dubbo 就必须看其源码，了解其原理，花点时间看下吧，网上也有很多教程，后续有时间我也会在公众号上分享 Dubbo 的源码。

## 什么是RPC

RPC（Remote Procedure Call Protocol）远程过程调用协议，它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。简言之，RPC使得程序能够像访问本地系统资源一样，去访问远端系统资源。比较关键的一些方面包括：通讯协议、序列化、资源（接口）描述、服务框架、性能、语言支持等。



![img](https://pic4.zhimg.com/80/v2-11bb64168ab851e7c68e835d021bacf3_720w.jpg)



简单的说，RPC就是从一台机器(客户端)上通过参数传递的方式调用另一台机器(服务器)上的一个函数或方法(可以统称为服务)并得到返回的结果。

## 三、PRC架构组件

一个基本的RPC架构里面应该至少包含以下4个组件：

### 1、客户端（Client）:

服务调用方（服务消费者）

### 2、客户端存根（Client Stub）:

存放服务端地址信息，将客户端的请求参数数据信息打包成网络消息，再通过网络传输发送给服务端

### 3、服务端存根（Server Stub）:

接收客户端发送过来的请求消息并进行解包，然后再调用本地服务进行处理

### 4、服务端（Server）:

服务的真正提供者



![img](https://pic4.zhimg.com/80/v2-4cf587c7240cc73034f5f70e20e24027_720w.jpg)



**具体调用过程：**

**1、**服务消费者（client客户端）通过调用本地服务的方式调用需要消费的服务；

**2、**客户端存根（client stub）接收到调用请求后负责将方法、入参等信息序列化（组装）成能够进行网络传输的消息体；

**3、**客户端存根（client stub）找到远程的服务地址，并且将消息通过网络发送给服务端；

**4、**服务端存根（server stub）收到消息后进行解码（反序列化操作）；

**5、**服务端存根（server stub）根据解码结果调用本地的服务进行相关处理；

**6、**本地服务执行具体业务逻辑并将处理结果返回给服务端存根（server stub）；

**7、**服务端存根（server stub）将返回结果重新打包成消息（序列化）并通过网络发送至消费方；

**8、**客户端存根（client stub）接收到消息，并进行解码（反序列化）；

**9、**服务消费方得到最终结果；

而RPC框架的实现目标则是将上面的第2-10步完好地封装起来，也就是把调用、编码/解码的过程给封装起来，让用户感觉上像调用本地服务一样的调用远程服务。

## 四、RPC和SOA、SOAP、REST的区别

### 1、REST

可以看着是HTTP协议的一种直接应用，默认基于JSON作为传输格式,使用简单,学习成本低效率高,但是安全性较低。

### 2、SOAP

SOAP是一种数据交换协议规范,是一种轻量的、简单的、基于XML的协议的规范。而SOAP可以看着是一个重量级的协议，基于XML、SOAP在安全方面是通过使用XML-Security和XML-Signature两个规范组成了WS-Security来实现安全控制的,当前已经得到了各个厂商的支持 。

它有什么优点？简单总结为：易用、灵活、跨语言、跨平台。

### 3、SOA

面向服务架构，它可以根据需求通过网络对松散耦合的粗粒度应用组件进行分布式部署、组合和使用。服务层是SOA的基础，可以直接被应用调用，从而有效控制系统中与软件代理交互的人为依赖性。

SOA是一种粗粒度、松耦合服务架构，服务之间通过简单、精确定义接口进行通讯，不涉及底层编程接口和通讯模型。SOA可以看作是B/S模型、XML（标准通用标记语言的子集）/Web Service技术之后的自然延伸。

### 4、REST 和 SOAP、RPC 有何区别呢？

没什么太大区别，他们的本质都是提供可支持分布式的基础服务，最大的区别在于他们各自的的特点所带来的不同应用场景 。

## 五、RPC框架需要解决的问题？

**1、**如何确定客户端和服务端之间的通信协议？

**2、**如何更高效地进行网络通信？

**3、**服务端提供的服务如何暴露给客户端？

**4、**客户端如何发现这些暴露的服务？

**5、**如何更高效地对请求对象和响应结果进行序列化和反序列化操作？

## 六、RPC的实现基础？

**1、**需要有非常高效的网络通信，比如一般选择Netty作为网络通信框架；

**2、**需要有比较高效的序列化框架，比如谷歌的Protobuf序列化框架；

**3、**可靠的寻址方式（主要是提供服务的发现），比如可以使用Zookeeper来注册服务等等；

**4、**如果是带会话（状态）的RPC调用，还需要有会话和状态保持的功能；

## 七、RPC使用了哪些关键技术？

### 1、动态代理

生成Client Stub（客户端存根）和Server Stub（服务端存根）的时候需要用到Java动态代理技术，可以使用JDK提供的原生的动态代理机制，也可以使用开源的：CGLib代理，Javassist字节码生成技术。

### 2、序列化和反序列化

在网络中，所有的数据都将会被转化为字节进行传送，所以为了能够使参数对象在网络中进行传输，需要对这些参数进行序列化和反序列化操作。

序列化：把对象转换为字节序列的过程称为对象的序列化，也就是编码的过程。

反序列化：把字节序列恢复为对象的过程称为对象的反序列化，也就是解码的过程。

目前比较高效的开源序列化框架：如Kryo、FastJson和Protobuf等。

### 3、NIO通信

出于并发性能的考虑，传统的阻塞式 IO 显然不太合适，因此我们需要异步的 IO，即 NIO。Java 提供了 NIO 的解决方案，Java 7 也提供了更优秀的 NIO.2 支持。可以选择Netty或者MINA来解决NIO数据传输的问题。

### 4、服务注册中心

可选：Redis、Zookeeper、Consul 、Etcd。一般使用ZooKeeper提供服务注册与发现功能，解决单点故障以及分布式部署的问题(注册中心)。

## 八、主流RPC框架有哪些

### 1、RMI

利用java.rmi包实现，基于Java远程方法协议(Java Remote Method Protocol) 和java的原生序列化。

### 2、Hessian

是一个轻量级的remoting onhttp工具，使用简单的方法提供了RMI的功能。基于HTTP协议，采用二进制编解码。

### 3、protobuf-rpc-pro

是一个Java类库，提供了基于 Google 的 Protocol Buffers 协议的远程方法调用的框架。基于 Netty 底层的 NIO 技术。支持 TCP 重用/ keep-alive、SSL加密、RPC 调用取消操作、嵌入式日志等功能。

### 4、Thrift

是一种可伸缩的跨语言服务的软件框架。它拥有功能强大的代码生成引擎，无缝地支持C + +，C#，Java，Python和PHP和Ruby。thrift允许你定义一个描述文件，描述数据类型和服务接口。依据该文件，编译器方便地生成RPC客户端和服务器通信代码。

最初由facebook开发用做系统内个语言之间的RPC通信，2007年由facebook贡献到apache基金 ，现在是apache下的opensource之一 。支持多种语言之间的RPC方式的通信：php语言client可以构造一个对象，调用相应的服务方法来调用java语言的服务，跨越语言的C/S RPC调用。底层通讯基于SOCKET。

### 5、Avro

出自Hadoop之父Doug Cutting, 在Thrift已经相当流行的情况下推出Avro的目标不仅是提供一套类似Thrift的通讯中间件,更是要建立一个新的，标准性的云计算的数据交换和存储的Protocol。支持HTTP，TCP两种协议。

### 6、Dubbo

Dubbo是 阿里巴巴公司开源的一个高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring框架无缝集成。

## 九、RPC的实现原理架构图



![img](https://pic4.zhimg.com/80/v2-b36773a8a41f70b490b1901f9e5efbef_720w.jpg)





![img](https://pic4.zhimg.com/80/v2-e61a7ee67906cbe2eb3c42dbadb95f13_720w.jpg)

PS：这张图非常重点，是PRC的基本原理，请大家一定记住！

也就是说两台服务器A，B，一个应用部署在A服务器上，想要调用B服务器上应用提供的函数/方法，由于不在一个内存空间，不能直接调用，需要通过网络来表达调用的语义和传达调用的数据。

比如说，A服务器想调用B服务器上的一个方法：

User getUserByName(String userName)

### 1、建立通信

首先要解决通讯的问题：即A机器想要调用B机器，首先得建立起通信连接。

主要是通过在客户端和服务器之间建立TCP连接，远程过程调用的所有交换的数据都在这个连接里传输。连接可以是按需连接，调用结束后就断掉，也可以是长连接，多个远程过程调用共享同一个连接。

通常这个连接可以是按需连接（需要调用的时候就先建立连接，调用结束后就立马断掉），也可以是长连接（客户端和服务器建立起连接之后保持长期持有，不管此时有无数据包的发送，可以配合心跳检测机制定期检测建立的连接是否存活有效），多个远程过程调用共享同一个连接。

### 2、服务寻址

要解决寻址的问题，也就是说，A服务器上的应用怎么告诉底层的RPC框架，如何连接到B服务器（如主机或IP地址）以及特定的端口，方法的名称名称是什么。

通常情况下我们需要提供B机器（主机名或IP地址）以及特定的端口，然后指定调用的方法或者函数的名称以及入参出参等信息，这样才能完成服务的一个调用。

可靠的寻址方式（主要是提供服务的发现）是RPC的实现基石，比如可以采用Redis或者Zookeeper来注册服务等等。



![img](https://pic2.zhimg.com/80/v2-23a8bf12b961898b7f792a6925759d1d_720w.jpg)



### 2.1、从服务提供者的角度看：

**2.1.1、**当服务提供者启动的时候，需要将自己提供的服务注册到指定的注册中心，以便服务消费者能够通过服务注册中心进行查找；

**2.1.2、**当服务提供者由于各种原因致使提供的服务停止时，需要向注册中心注销停止的服务；

**2.1.3、**服务的提供者需要定期向服务注册中心发送心跳检测，服务注册中心如果一段时间未收到来自服务提供者的心跳后，认为该服务提供者已经停止服务，则将该服务从注册中心上去掉

### 2.2、从调用者的角度看：

**2.2.1、**服务的调用者启动的时候根据自己订阅的服务向服务注册中心查找服务提供者的地址等信息；

**2.2.2、**当服务调用者消费的服务上线或者下线的时候，注册中心会告知该服务的调用者；

**2.2.3、**服务调用者下线的时候，则取消订阅。

### 3、网络传输

### 3.1、序列化

当A机器上的应用发起一个RPC调用时，调用方法和其入参等信息需要通过底层的网络协议如TCP传输到B机器，由于网络协议是基于二进制的，所有我们传输的参数数据都需要先进行序列化（Serialize）或者编组（marshal）成二进制的形式才能在网络中进行传输。然后通过寻址操作和网络传输将序列化或者编组之后的二进制数据发送给B机器。

### 3.2、反序列化

当B机器接收到A机器的应用发来的请求之后，又需要对接收到的参数等信息进行反序列化操作（序列化的逆操作），即将二进制信息恢复为内存中的表达方式，然后再找到对应的方法（寻址的一部分）进行本地调用（一般是通过生成代理Proxy去调用,
通常会有JDK动态代理、CGLIB动态代理、Javassist生成字节码技术等），之后得到调用的返回值。

### 4、服务调用

B机器进行本地调用（通过代理Proxy和反射调用）之后得到了返回值，此时还需要再把返回值发送回A机器，同样也需要经过序列化操作，然后再经过网络传输将二进制数据发送回A机器，而当A机器接收到这些返回值之后，则再次进行反序列化操作，恢复为内存中的表达方式，最后再交给A机器上的应用进行相关处理，一般是业务逻辑处理操作。

通常，经过以上四个步骤之后，一次完整的RPC调用算是完成了，另外可能因为网络抖动等原因需要重试等。