# API网关

**一定要看：：：：大佬大佬**

 https://blog.csdn.net/weixin_38754564/article/list/2 

**一定要看：：：：大佬大佬**

 https://blog.csdn.net/weixin_38754564/article/list/2 

**一定要看：：：：大佬大佬**

 https://blog.csdn.net/weixin_38754564/article/list/2 





 http://blog.sina.com.cn/s/blog_493a84550102z8z0.html 



# 一 API网关的作用

1 灰度发布

2 路由

3 鉴权

4 统一服务降级

5 统一 服务降级

6 认证

7防攻击

外层网关：提供主备，集群，以及转发功能LVS,DNS,Nginx

内存网关：zuul，spring cloud gateway



# 二 主流实现

私有云开源解决方案如下：

- Kong kong是基于Nginx+Lua进行二次开发的方案， [https://konghq.com/](https://link.zhihu.com/?target=https%3A//konghq.com/)
- Netflix Zuul，zuul是spring cloud的一个推荐组件，[https://github.com/Netflix/zuul](https://link.zhihu.com/?target=https%3A//github.com/Netflix/zuul)
- orange,这个开源程序是国人开发的， [http://orange.sumory.com/](https://link.zhihu.com/?target=http%3A//orange.sumory.com/)

公有云解决方案：

- Amazon API Gateway，[https://aws.amazon.com/cn/api-gateway/](https://link.zhihu.com/?target=https%3A//aws.amazon.com/cn/api-gateway/)
- 阿里云API网关，[https://www.aliyun.com/product/apigateway/](https://link.zhihu.com/?target=https%3A//www.aliyun.com/product/apigateway/)
- 腾讯云API网关， [https://cloud.tencent.com/product/apigateway](https://link.zhihu.com/?target=https%3A//cloud.tencent.com/product/apigateway)

自开发解决方案：

- 基于Nginx+Lua+ OpenResty的方案，可以看到Kong,orange都是基于这个方案
- 基于Netty、非阻塞IO模型。通过网上搜索可以看到国内的宜人贷等一些公司是基于这种方案，是一种成熟的方案。
- 基于Node.js的方案。这种方案是应用了Node.js天生的非阻塞的特性。
- 基于java Servlet的方案。zuul基于的就是这种方案，这种方案的效率不高，这也是zuul总是被诟病的原因。



Linkerd

Linkerd是CNCF的项目，是Scala开发的service mesh应用。他提供反向代理能力用于扩展service mesh能力，例如服务发现。我们评估Linkerd性能并且给出如下结果。Linkerd跟Zuul的性能很接近 



# 三 网关zuul与springcloudgateway 对比



| **网关zuul与springcloudgateway 对比** |                                                              |                                                              |
| ------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
|                                       | springcloudgateway                                           | zuul                                                         |
| 基本介绍                              | Spring Cloud Gateway是Spring官方基于Spring 5.0，Spring Boot 2.0和Project Reactor等技术开发的网关，Spring Cloud Gateway旨在为微服务架构提供一种简单而有效的统一的API路由管理方式。Spring Cloud Gateway作为Spring Cloud生态系中的网关，目标是替代Netflix ZUUL，其不仅提供统一的路由方式，并且基于Filter链的方式提供了网关基本的功能，例如：安全，监控/埋点，和限流等。 | Zuul1 是基于 Servlet 框架构建，如图所示，采用的是阻塞和多线程方式，即一个线程处理一次连接请求，这种方式在内部延迟严重、设备故障较多情况下会引起存活的连接增多和线程增加的情况发生。 |
| 性能                                  | WebFlux 模块的名称是 spring-webflux，名称中的 Flux 来源于 Reactor 中的类 Flux。Spring webflux 有一个全新的非堵塞的函数式 Reactive Web 框架，可以用来构建异步的、非堵塞的、事件驱动的服务，在伸缩性方面表现非常好。使用非阻塞API。 Websockets得到支持，并且由于它与Spring紧密集成，所以将会是一个更好的 开发 体验。 | 本文的Zuul，指的是Zuul 1.x，是一个基于阻塞io的API Gateway。Zuul已经发布了Zuul 2.x，基于Netty，也是非阻塞的，支持长连接，但Spring Cloud暂时还没有整合计划。 |
| 源码维护组织                          | `spring-cloud-Gateway`是spring旗下`spring-cloud`的一个子项目。还有一种说法是因为`zuul2`连续跳票和`zuul1`的性能表现不是很理想，所以催生了spring孵化`Gateway`项目。 | `zuul`则是`netflix`公司的项目，只是spring将`zuul`集成在spring-cloud中使用而已。关键目前spring不打算集成zuul2.x。 |
| 版本                                  | 基于springboot2.0：https://blog.csdn.net/ityouknow/article/details/79421642 | springboot1.x                                                |

# 四 如何选择

**1、性能与可用性**

如果一旦采用了API网关，那么API网关就会作为企业应用核心，因此性能和可用性是必须要求的。

从性能上来说，需要让网关增加的时间消耗越短越好，个人觉得需要10ms以下。系统需要采用非阻塞的IO，如epoll，NIO等。网关和各种依赖的交互也需要是非阻塞的，这样才能保证整体系统的高可用性，如：Node.js的响应式编程和基于java体现的RxJava和Future。

网关必须支持集群部署，任务一台服务器的crash都应该不影响整体系统的可用性。

多套网关应该支持同一管理平台和同一监控中心。如：一个企业的OpenAPI网关和内部应用的多个系统群的不同的微服务网关可以在同一监控中心进行监控。

**2、可扩展性、可维护性**

一款产品总有不能满足生产需求的地方，因此需求思考产品在如何进行二次开发和维护，是否方便公司团队接手维护产品。

**3、需求匹配度**

需要评估各API网关在需求上是否能满足，如：如果是OpenAPI平台需要使用API网关，那么需要看API网关在合作伙伴应用接入、合作伙伴门户集成、访问次数限额等OpenAPI核心需求上去思考产品是否能满足要求。如果是微服务网关，那么要从微服务的运维、监控、管理等方面去思考产品是否足够强大。

**4、是否开源？公司是否有自开发的能力？**

现有的开源产品如kong，zuul，orange都有基础的API网关的核心功能，这些开源产品大多离很好的使用有一定的距离，如：没有提供管理功能的UI界面、监控功能弱小，不支持OpenAPI平台，没有公司运营与运维的功能等。当然开源产品能获取源代码，如果公司有比较强的研发能力，能hold住这些开源产品，经过二次开发kong、zuul应该还是适应一些公司，不过需求注意以下一些点：

- kong是基于ngnix+lua的，从公司的角度比较难于找到能去维护这种架构产品的人。需求评估当前公司是否有这个能力去维护这个产品。
- zuul因为架构的原因在高并发的情况下性能不高，同时需要去基于研究整合开源的适配zuul的监控和管理系统。
- orange由于没有被大量使用，同时是国内个人在开源，在可持续性和社区资源上不够丰富，出了问题后可能不容易找到人问。

另外kong提供企业版本的API网关，当然也是基于ngnix+lua的，企业版本可以购买他们的技术支持、培训等服务、以及拥有界面的管理、监控等功能。

**5、公有云还是私有云**

现在的亚马逊、阿里、腾讯云都在提供基础公有云的API网关，当然这些网关的基础功能肯定是没有问题，但是二次开发，扩展功能、监控功能可能就不能满足部分用户的定制需求了。另外很多企业因为自身信息安全的原因，不能使用外网公有网的API网关服务，这样就只有选择私有云的方案了。

在需求上如果基于公有云的API网关只能做到由内部人员为外网人员申请应用，无法做到定制的合作伙伴门户，这也不适合于部分企业的需求。

如果作为微服务网关，大多数情况下是希望网关服务器和服务提供方服务器是要在内网的，在这里情况下也只有私有云的API网关才能满足需求。

综合上面的分析，基础公有云的API网关只有满足一部分简单客户的需求，对于很多企业来说私有云的API网关才是正确的选择。 

# 五 Service Mesh





### 云原生 Service Mesh 框架 - Istio

Istio由Google，IBM和Lyft联合开发，与 Kubernetes 一脉相承且深度融合：

- Kubernetes 提供了部署、升级和有限的运行流量管理能力
- Istio 补齐了 Kubernetes 在微服务治理能力上的短板（如限流、熔断、降级、分流等）
- Istio 以 Sidecar 的形式运行在 Pod 中，自动注入，自动接管流量，部署过程对业务透明

Istio提供了完整的Service Mesh解决方案：

**数据面**

- 数据面支持多种协议（如HTTP 1.X/2.X，GRPC等），控制服务所有进出流量，同时负责控制面制定的策略执行，并上报遥感数据
- Istio默认的Sidecar是Envoy，它是基于C++开发的L4/L7高性能代理（对标NGINX）
- 具有强大的流量管理能力、治理能力与扩展能力

**控制面**

- Pilot：提供服务发现与抽象能力，负责配置转换与分发（如动态路由等）
- Mixer：访问控制、接收遥感数据等
- Citadel：提供安全证书与秘钥的下发和管理能力。
- Galley：提供配置校验能力

![Istio框架](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy91SDFoZGo1ZGxjTVNubVU5dGlhMkQ3S1pWYmJjdmZGaWFiaWNiWVBKdE1UbFpNck5TbmRDSVdiaWExQkxpY2ZRUVdxWHhhb3JZWEhSOUZ1djlhZElDYnBtc3BBLzY0MA?x-oss-process=image/format,png)

### 功能视角 - 服务治理能力 – 基于Istio+Envoy

从功能视角来看，相比于严选**第一代Service Mesh架构**，在流量管理能力方面（如流量染色、路由控制、流量复制等）有明显的增强，在治理控制方面的能力也更为丰富，提供了如熔断降级、资源隔离、故障注入等能力，在访问控制方面也提供了更多选择。

![Istio框架](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy91SDFoZGo1ZGxjTVNubVU5dGlhMkQ3S1pWYmJjdmZGaWFiVzZ2bDU4bGFxOGliTzlpY2QxbHFZZTNxcFlpY0pwOVJuSjRZOWJDZlo2NGtORURsUjhPWnFIaWJKdy82NDA?x-oss-process=image/format,png)

在Service Mesh架构实践和落地过程中，大家最关心的问题是性能问题，Service Mesh架构虽然解决了很多基础架构的痛点，但相比于原来的一次远程调用，会额外增加1~2跳，直觉告诉我们这会带来额外的延时。

根据我们的压测数据，主机配置为8C16G（严选应用服务器的规格，与cNginx共享），在40并发、1600RPS的情况下，与直连相比，cNginx的延时增加0.4ms（相比直连），Envoy（社区版本，优化前）Client Sidecar模式延时增加0.6ms（相比直连）。

cNginx和Envoy Client模式对性能的影响都比较小，在可接受范围之内。另外，传统的带服务治理能力的中间件（如Spring Cloud/Dubbo等）同样会带来性能开销和资源开销，因此，实际的性能影响其实更小（从前面蚂蚁和酷家乐分享的性能数据来看，Sidecar模式与SDK模式相比，蚂蚁应用场景的平均延时增加约0.2ms，而酷家乐应用场景的延时甚至还有降低）。

### 性能视角 – cNginx vs Envoy（优化后）

由于Service Mesh架构的Sidecar和应用不在一个进程中，因此针对Service Mesh数据面的优化路径会更丰富，优化的可持续性也更强，同时由于优化效果的干扰因素更小，优化数据会更有说服力。

我们的轻舟微服务团队对容器网络和Envoy做了初步的优化：

- 采用 SRIOV 容器网络
- Envoy：将1.13版本中 connection loadbalancer 特性移植到 1.10.x 版本

根据我们的压测数据

- 在并发较低（<64）、1000RPS的情况下，**Envoy优化后的版本在容器网络下开启Client Sidecar**表现要优于虚拟机网络的直连，相较于容器网络直连开销增加0.2~0.6ms
- 在并发较高（>=64）、1000RPS的情况下，**Envoy优化后的版本在容器网络下开启Client Sidecar**表现要远远优于虚拟机网络cNginx的性能，与虚拟机网络的直连性能几乎相当；但相较于容器网络直连1~5ms左右的延时

![Envoy性能](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X3BuZy91SDFoZGo1ZGxjTVNubVU5dGlhMkQ3S1pWYmJjdmZGaWFiT1BDQjVYMDhuUURBNU5SNDgzbmhoeHc0elBSbWRua1owcU85Qk9kS2ljMjhtUEVlYmo3eWlicEEvNjQw?x-oss-process=image/format,png)



# 六 Service Mesh 后我还需要 API 网关吗

 https://blog.csdn.net/weixin_38754564/article/details/104285725 

混淆的原因如下：

- 技术使用上存在重叠（代理）
- 功能上存在重叠（流量控制，路由，指标收集，安全/策略增强等）
- “服务网格”可替代 API 管理的理念
- 服务网格能力的误解
- 一些服务网格有自己的网关

## 重叠在哪里

业务的第一个步骤是识别 API 网关和服务网格功能看上去重叠的区域。两者都处理应用程序流量，所以重叠应该不足为奇。下面的清单列举了一些重叠的功能：

- 遥测数据收集
- 分布式追踪
- 服务发现
- 负载均衡
- TLS 终止/开始
- JWT 校验
- 请求路由
- 流量切分
- 金丝雀发布
- 流量镜像
- 速率控制

好吧，它们确实有重叠。那么你需要一个？还是两个？还是都不需要？

## 分叉点在哪里

服务网格运行在比 API 网关更低的级别，并在架构中所有单个服务上运行。服务网格为服务客户提供关于架构拓扑的“更多细节”（包括客户端负载均衡、服务发现、请求路由），应该实现的弹性机制（超时、重试、熔断），应该收集的遥测（度量、跟踪）和参与的安全流（mTLS、RBAC）。所有这些实现细节通常由某个 sidecar（请考虑 Envoy）提供给应用程序，但它们不必这样做

 **服务网格的目标是通过在 L7 上透明地操作来解决任何服务/应用程序中列举的问题**。换句话说，服**务网格希望接入到服务中**（而不是到服务中编写代码）。 

 另一方面，API 网关则扮演着不同的角色：“抽象细节”和解耦实现。API 网关提供了跨应用程序架构中所有服务的内聚抽象——作为一个整体，为特定的 API 解决了一些边缘/边界问题。 

 无论服务网格是否存在，API 网关都存在于应用程序/服务之上，并为其他部分提供抽象。它们做的事情包括聚合 API、抽象 API 和用不同的实现方式暴露它们，并基于用户在边缘添加更复杂的零信任安全策略。应用程序架构边界上的问题与边界内的问题不同。 

 ![img](https://imgconvert.csdnimg.cn/aHR0cHM6Ly9tbWJpei5xcGljLmNuL21tYml6X2pwZy91SDFoZGo1ZGxjTTB3MlR6aWF6TEZpYmJScDN4ZUxITlVKRHd4U3JxN0RjeEl1NVFjeGZnaHlMVjZ1ZWtjYUpjWjVJQUlPa3V4aWJuMzZTVUpFSHRhbHlNdy82NDA?x-oss-process=image/format,png) 

## 边界问题与服务到服务的挑战不同

在微服务/云原生架构的边界上，**API 网关提供了服务网格无法在同等程度上解决的三个主要能力**：

- 边界解耦
- 严格控制数据的进出
- 桥接安全信任域

### 边界解耦

**API 网关的核心功能是为边界外的客户端提供稳定的 API 接口。**从 Chris Richardson 的微服务模式一书中，我们可以将“API 网关模式”改写为：

> 显式地简化一组 API / 微服务的调用
>
> 为一组特定的用户、客户端或消费者模拟“应用程序”的内聚 API。
>
> 这里的关键是 API 网关，当它实现时，它将作为应用程序架构的单一入口点，成为客户端的 API

来自 API 网关身份危机 一文中 API 网关的实现案例：

- Solo.io Gloo
- Spring Cloud Gateway
- Netflix Zuul
- IBM-Strongloop Loopback/Microgateway

从功能上看，API 网关需要支持什么？企业在现实的用例中会看到哪些需要 API 网关（服务网格不太适合）的情况：

- 请求/响应转换
- 应用协议转换如 REST/SOAP/XSLT
- 错误/速率定制响应
- 直接响应
- 对 API/代理管道的精确控制
- API 聚合/分组

#### 请求/响应传输

作为在 API 网关上暴露 API 的一部分，您可能希望隐藏后端 API 实现的细节。这可能是改变请求内容、删除/添加标头、将标头放入正文的一些组合，反之亦然。当后端服务对 API 进行更改时，或者当客户端不能像提供方那样快速更新时，这提供了一个很好的从客户端解耦的点。

#### 应用协议转换

许多企业在技术上进行了投入，如基于 HTTP、SOAP 的 XML，或基于 HTTP 的 JSON。他们可能希望使用更严格的、特定于客户端的 API 来公开这些 API，并继续保持互操作性。此外，服务提供者可能希望利用新的 RPC 机制（如 gRPC）或流协议（如 rSocket）。

#### 错误/速率定制响应

转换来自上游服务的请求是 API 网关的一项重要功能，定制来自网关本身的响应也是如此。采用 API 网关的虚拟 API 进行请求/响应/错误处理的客户端也希望网关自定义其响应以适应该模型。

#### 直接响应

当客户端（受信任的或恶意的）请求不可用的资源，或由于某种原因被阻止上行时，最好能够终止代理并使用预先屏蔽的响应返回。

#### 对 API/代理管道的精确控制

没有一种方法可以满足所有代理的期望。API 网关应该能够改变应用其功能的顺序（速率限制、authz/n、路由、转换等），并在出现问题时提供一种调试方法。

#### API 聚合

在多个服务上公开一个抽象常常伴随着将多个 API 混合成一个 API 的期望。类似于 GraphQL 的东西可以满足这个需求。

正如您所看到的，在客户端和提供服务者之间提供一个强大的解耦点涉及的不仅仅是允许 HTTP 通信进入集群这么简单。

## 严格控制什么可以进入/离开服务

API 网关的另一个重要功能是“控制”哪些数据/请求允许进入应用架构，哪些数据/响应允许流出。这意味着，网关需要对进入或发出的请求有深入的理解。例如，一个常见的场景是 Web 应用程序防火墙防止 SQL 注入攻击。另一种是“数据丢失预防”技术，用于在请求 PCI-DSS/HIPPA/GDPR 时阻止 SSN 或 PII 被返回。边界是帮助实现这些策略的天然位置。

同样，定义和实施这些功能并不像允许 HTTP 通信流进入集群那么简单。

## 定制安全/桥接信任域

API 网关提供的最后一个主要功能是边缘安全性。这涉及到向存在于应用程序架构之外的用户和服务提供身份和范围策略，从而限制对特定服务和业务功能的访问。这与前面的部分相关。

一个常见的例子是能够绑定到 OAuth/SSO 流，包括 Open ID Connect。这些“标准”的挑战在于，它们可能没有得到充分实施，也可能没有得到正确实施。API 网关需要一种方法来灵活地适应这些环境以及提供定制。

在许多企业中，已经存在身份/信任/认证机制，API 网关的很大一部分是为了向后兼容而进行本地集成。虽然出现了 SPIFEE 这样的新标准，但企业需要一段时间才能落地，与此同时，API 网关（甚至是针对在其下一代架构上运行的应用程序的网关）是一个艰难的要求。同样，你可以检视并说这也和上面提到的变换/解耦点有关。

## 怎样落地其中一个/另一个/两者/两者都不？

在之前的一篇博客中，我概述了一些采用这种技术的挑战（API 网关和服务网格），并给出了关于如何最好地应用这种技术的提示。

重申一下：从边缘开始。这是架构中熟悉的一部分。也要考虑选择最合适的。自从我们引入了云基础设施和云原生应用架构以来，假设（编者注：文章开始所说的假设）已经发生了变化。例如，如果您打算采用 Kubernetes，我强烈建议您考虑使用从头开始构建的应用程序网络技术（例如，检查 Envoy 代理和已经被提升和转移的应用程序网络技术）。例如，在 Solo.io，我们已经为此建立了一个名为 Gloo 的开源项目。

你需要一个服务网格吗？如果您正在部署到云平台，有多种类型的语言/框架来实现您的工作负载，并构建一个微服务架构，那么您可能需要一个。选择也很多。我做过各种比较和对比的演讲，最近的是 OSCON 演讲。请随意参考并找到最合适你的。

