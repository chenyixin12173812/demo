# dubbo 服务导出

 始于 Spring 容器发布刷新事件，Dubbo 在接收到事件后，会立即执行服务导出逻辑。

1 前置工作，主要用于检查参数，组装 URL。

2 导出服务，包含导出服务到本地 (JVM)，和导出服务到远程两个过程。

3 向注册中心注册服务，用于服务发现。

官方时序图：

![/dev-guide/images/dubbo-export.jpg](http://dubbo.apache.org/docs/zh-cn/dev/sources/images/dubbo-export.jpg)

# 0 入口

入口方法是 ServiceBean 的 onApplicationEvent。onApplicationEvent 是一个事件响应方法，该方法会在收到 Spring 上下文刷新事件后执行服务导出操作。方法代码如下：

```java
public void onApplicationEvent(ContextRefreshedEvent event) {
    // 是否有延迟导出 && 是否已导出 && 是不是已被取消导出
    if (isDelay() && !isExported() && !isUnexported()) {
        // 导出服务
        export();
    }
}
```

------

# 1.前置工作

## 1.1 检查配置

我们只是想本地启动服务进行一些调试工作，我们并不希望把本地启动的服务暴露出去给别人调用。此时，我们可通过配置 export 禁止服务导出，只导出本地服务 比如：

```xml
<dubbo:provider export="false" />
```

简单的总结，如下：

1. 检测 <dubbo:service> 标签的 interface 属性合法性，不合法则抛出异常
2. 检测 ProviderConfig、ApplicationConfig 等核心配置类对象是否为空，若为空，则尝试从其他配置类对象中获取相应的实例。
3. 检测并处理泛化服务和普通服务类
4. 检测本地存根配置，并进行相应的处理
5. 对 ApplicationConfig、RegistryConfig 等配置类进行检测，为空则尝试创建，若无法创建则抛出异常

 doExport 方法所调用的方法进行分析（doExportUrls 方法除外）。在这些方法中，除了 appendProperties 方法稍微复杂一些，其他方法逻辑不是很复杂。因此，大家可自行分析。

## 1.2 多协议多注册中心导出服务



Dubbo 允许我们使用不同的协议导出服务，也允许我们向多个注册中心注册服务。Dubbo 在 doExportUrls 方法中对多协议，多注册中心进行了支持。相关代码如下：

```java
private void doExportUrls() {
    // 加载注册中心链接
    List<URL> registryURLs = loadRegistries(true);
    // 遍历 protocols，并在每个协议下导出服务
    for (ProtocolConfig protocolConfig : protocols) {
        doExportUrlsFor1Protocol(protocolConfig, registryURLs);
    }
}
```

## 1.3  组装 URL

配置检查完毕后，紧接着要做的事情是根据配置，以及其他一些信息组装 URL。前面说过，URL 是 Dubbo 配置的载体，通过 URL 可让 Dubbo 的各种配置在各个模块之间传递。URL 之于 Dubbo，犹如水之于鱼，非常重要。大家在阅读 Dubbo 服务导出相关源码的过程中，要注意 URL 内容的变化。既然 URL 如此重要，那么下面我们来了解一下 URL 组装的过程。

```
ServiceConfig.doExportUrlsFor1Protocol()
```

# 2 暴露服务

 服务导出分为导出到本地 (JVM)，和导出到远程。在深入分析服务导出的源码前，我们先来从宏观层面上看一下服务导出逻辑。如下 

```java
private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {
    
    // 省略无关代码
    
    if (ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
            .hasExtension(url.getProtocol())) {
        // 加载 ConfiguratorFactory，并生成 Configurator 实例，然后通过实例配置 url
        url = ExtensionLoader.getExtensionLoader(ConfiguratorFactory.class)
                .getExtension(url.getProtocol()).getConfigurator(url).configure(url);
    }

    String scope = url.getParameter(Constants.SCOPE_KEY);
    // 如果 scope = none，则什么都不做
    if (!Constants.SCOPE_NONE.toString().equalsIgnoreCase(scope)) {
        // scope != remote，导出到本地
        if (!Constants.SCOPE_REMOTE.toString().equalsIgnoreCase(scope)) {
            exportLocal(url);
        }

        // scope != local，导出到远程
        if (!Constants.SCOPE_LOCAL.toString().equalsIgnoreCase(scope)) {
            if (registryURLs != null && !registryURLs.isEmpty()) {
                for (URL registryURL : registryURLs) {
                    url = url.addParameterIfAbsent(Constants.DYNAMIC_KEY, registryURL.getParameter(Constants.DYNAMIC_KEY));
                    // 加载监视器链接
                    URL monitorUrl = loadMonitor(registryURL);
                    if (monitorUrl != null) {
                        // 将监视器链接作为参数添加到 url 中
                        url = url.addParameterAndEncoded(Constants.MONITOR_KEY, monitorUrl.toFullString());
                    }

                    String proxy = url.getParameter(Constants.PROXY_KEY);
                    if (StringUtils.isNotEmpty(proxy)) {
                        registryURL = registryURL.addParameter(Constants.PROXY_KEY, proxy);
                    }

                    // 为服务提供类(ref)生成 Invoker
                    Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString()));
                    // DelegateProviderMetaDataInvoker 用于持有 Invoker 和 ServiceConfig
                    DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);

                    // 导出服务，并生成 Exporter
                    Exporter<?> exporter = protocol.export(wrapperInvoker);
                    exporters.add(exporter);
                }
                
            // 不存在注册中心，仅导出服务
            } else {
                Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, url);
                DelegateProviderMetaDataInvoker wrapperInvoker = new DelegateProviderMetaDataInvoker(invoker, this);

                Exporter<?> exporter = protocol.export(wrapperInvoker);
                exporters.add(exporter);
            }
        }
    }
    this.urls.add(url);
}
```



上面代码根据 url 中的 scope 参数决定服务导出方式，分别如下：

- scope = none，不导出服务
- scope != remote，导出到本地
- scope != local，导出到远程

不管是导出到本地，还是远程。进行服务导出之前，均需要先创建 Invoker，这是一个很重要的步骤。因此下面先来分析 Invoker 的创建过程。

### 2.2.1 Invoker 创建过程

## 2.1：暴露本地服务

```java
Export dubbo service com.alibaba.dubbo.demo.DemoService to local registry, dubbo version: 2.0.0, current host: 127.0.0.1
```

## 2.2 暴露远程服务

第三个发布动作：启动netty

```
	Export dubbo service com.alibaba.dubbo.demo.DemoService to url dubbo://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1
	Register dubbo service com.alibaba.dubbo.demo.DemoService url dubbo://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&monitor=dubbo%3A%2F%2F192.168.48.117%3A2181%2Fcom.alibaba.dubbo.registry.RegistryService%3Fapplication%3Ddemo-provider%26backup%3D192.168.48.120%3A2181%2C192.168.48.123%3A2181%26dubbo%3D2.0.0%26owner%3Dwilliam%26pid%3D8484%26protocol%3Dregistry%26refer%3Ddubbo%253D2.0.0%2526interface%253Dcom.alibaba.dubbo.monitor.MonitorService%2526pid%253D8484%2526timestamp%253D1473908495729%26registry%3Dzookeeper%26timestamp%3D1473908495398&owner=william&pid=8484&side=provider&timestamp=1473908495465 to registry registry://192.168.48.117:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-provider&backup=192.168.48.120:2181,192.168.48.123:2181&dubbo=2.0.0&owner=william&pid=8484&registry=zookeeper&timestamp=1473908495398, dubbo version: 2.0.0, current host: 127.0.0.1
```

第三个发布动作：启动netty
	Start NettyServer bind /0.0.0.0:20880, export /192.168.100.38:20880, dubbo version: 2.0.0, current host: 127.0.0.1
第四个发布动作：打开连接zk
	INFO zookeeper.ClientCnxn: Opening socket connection to server /192.168.48.117:2181
第五个发布动作：到zk注册
	Register: dubbo://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1
第六个发布动作；监听zk
	Subscribe: provider://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1
	Notify urls for subscribe url provider://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, urls: [empty://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465], dubbo version: 2.0.0, current host: 127.0.0.1

```java
ServiceBean.onApplicationEvent
-->export()
  -->ServiceConfig.export()
    -->doExport()
      -->doExportUrls()//里面有一个for循环，代表了一个服务可以有多个通信协议，例如 tcp协议 http协议，默认是tcp协议
        -->loadRegistries(true)//从dubbo.properties里面组装registry的url信息
        -->doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) 
          //配置不是remote的情况下做本地暴露 (配置为remote，则表示只暴露远程服务)
          -->exportLocal(URL url)
            -->proxyFactory.getInvoker(ref, (Class) interfaceClass, local)
              -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.ProxyFactory.class).getExtension("javassist");
              -->extension.getInvoker(arg0, arg1, arg2)
                -->StubProxyFactoryWrapper.getInvoker(T proxy, Class<T> type, URL url) 
                  -->proxyFactory.getInvoker(proxy, type, url)
                    -->JavassistProxyFactory.getInvoker(T proxy, Class<T> type, URL url)
                      -->Wrapper.getWrapper(com.alibaba.dubbo.demo.provider.DemoServiceImpl)
                        -->makeWrapper(Class<?> c)
                      -->return new AbstractProxyInvoker<T>(proxy, type, url)
            -->protocol.export
              -->Protocol$Adpative.export
                -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension("injvm");
                -->extension.export(arg0)
                  -->ProtocolFilterWrapper.export
                    -->buildInvokerChain //创建8个filter
                    -->ProtocolListenerWrapper.export
                      -->InjvmProtocol.export
                        -->return new InjvmExporter<T>(invoker, invoker.getUrl().getServiceKey(), exporterMap)
                        -->目的：exporterMap.put(key, this)//key=com.alibaba.dubbo.demo.DemoService, this=InjvmExporter
          //如果配置不是local则暴露为远程服务.(配置为local，则表示只暴露本地服务)
          -->proxyFactory.getInvoker//原理和本地暴露一样都是为了获取一个Invoker对象
          -->protocol.export(invoker)
            -->Protocol$Adpative.export
              -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension("registry");
	            -->extension.export(arg0)
	              -->ProtocolFilterWrapper.export
	                -->ProtocolListenerWrapper.export
	                  -->RegistryProtocol.export
	                    -->doLocalExport(originInvoker)
	                      -->getCacheKey(originInvoker);//读取 dubbo://192.168.100.51:20880/
	                      -->rotocol.export
	                        -->Protocol$Adpative.export
	                          -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.rpc.Protocol.class).getExtension("dubbo");
	                          -->extension.export(arg0)
	                            -->ProtocolFilterWrapper.export
	                              -->buildInvokerChain//创建8个filter
	                              -->ProtocolListenerWrapper.export
---------1.netty服务暴露的开始-------    -->DubboProtocol.export
	                                  -->serviceKey(url)//组装key=com.alibaba.dubbo.demo.DemoService:20880
	                                  -->目的：exporterMap.put(key, this)//key=com.alibaba.dubbo.demo.DemoService:20880, this=DubboExporter
	                                  -->openServer(url)
	                                    -->createServer(url)
--------2.信息交换层 exchanger 开始-------------->Exchangers.bind(url, requestHandler)//exchaanger是一个信息交换层
	                                        -->getExchanger(url)
	                                          -->getExchanger(type)
	                                            -->ExtensionLoader.getExtensionLoader(Exchanger.class).getExtension("header")
	                                        -->HeaderExchanger.bind
	                                          -->Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler)))
	                                            -->new HeaderExchangeHandler(handler)//this.handler = handler
	                                            -->new DecodeHandler
	                                            	-->new AbstractChannelHandlerDelegate//this.handler = handler;
---------3.网络传输层 transporter--------------------->Transporters.bind
	                                              -->getTransporter()
	                                                -->ExtensionLoader.getExtensionLoader(Transporter.class).getAdaptiveExtension()
	                                              -->Transporter$Adpative.bind
	                                                -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.remoting.Transporter.class).getExtension("netty");
	                                                -->extension.bind(arg0, arg1)
	                                                  -->NettyTransporter.bind
	                                                    --new NettyServer(url, listener)
	                                                      -->AbstractPeer //this.url = url;    this.handler = handler;
	                                                      -->AbstractEndpoint//codec  timeout=1000  connectTimeout=3000
	                                                      -->AbstractServer //bindAddress accepts=0 idleTimeout=600000
---------4.打开断开，暴露netty服务-------------------------------->doOpen()
	                                                        -->设置 NioServerSocketChannelFactory boss worker的线程池 线程个数为3
	                                                        -->设置编解码 hander
	                                                        -->bootstrap.bind(getBindAddress())
	                                            -->new HeaderExchangeServer
	                                              -->this.server=NettyServer
	                                              -->heartbeat=60000
	                                              -->heartbeatTimeout=180000
	                                              -->startHeatbeatTimer()//这是一个心跳定时器，采用了线程池，如果断开就心跳重连。

	                    -->getRegistry(originInvoker)//zk 连接
	                      -->registryFactory.getRegistry(registryUrl)
	                        -->ExtensionLoader.getExtensionLoader(RegistryFactory.class).getExtension("zookeeper");
	                        -->extension.getRegistry(arg0)
	                          -->AbstractRegistryFactory.getRegistry//创建一个注册中心，存储在REGISTRIES
	                            -->createRegistry(url)
	                              -->new ZookeeperRegistry(url, zookeeperTransporter)
	                                -->AbstractRegistry
	                                  -->loadProperties()//目的：把C:\Users\bobo\.dubbo\dubbo-registry-192.168.48.117.cache
	                                                                                                                                                                    文件中的内容加载为properties
	                                  -->notify(url.getBackupUrls())//不做任何事             
	                                -->FailbackRegistry   
	                                  -->retryExecutor.scheduleWithFixedDelay(new Runnable()//建立线程池，检测并连接注册中心,如果失败了就重连
	                                -->ZookeeperRegistry
	                                  -->zookeeperTransporter.connect(url)
	                                    -->ZookeeperTransporter$Adpative.connect(url)
	                                      -->ExtensionLoader.getExtensionLoader(ZookeeperTransporter.class).getExtension("zkclient");
	                                      -->extension.connect(arg0)
	                                        -->ZkclientZookeeperTransporter.connect
	                                          -->new ZkclientZookeeperClient(url)
	                                            -->AbstractZookeeperClient
	                                            -->ZkclientZookeeperClient
	                                              -->new ZkClient(url.getBackupAddress());//连接ZK
	                                              -->client.subscribeStateChanges(new IZkStateListener()//订阅的目标：连接断开，重连
	                                    -->zkClient.addStateListener(new StateListener() 
	                                      -->recover //连接失败 重连
	                                      
	                    -->registry.register(registedProviderUrl)//创建节点
	                      -->AbstractRegistry.register
	                      -->FailbackRegistry.register
	                        -->doRegister(url)//向zk服务器端发送注册请求
	                          -->ZookeeperRegistry.doRegister
	                            -->zkClient.create
	                              -->AbstractZookeeperClient.create//dubbo/com.alibaba.dubbo.demo.DemoService/providers/
										                              dubbo%3A%2F%2F192.168.100.52%3A20880%2Fcom.alibaba.dubbo.demo.DemoService%3Fanyhost%3Dtrue%26
										                              application%3Ddemo-provider%26dubbo%3D2.0.0%26generic%3Dfalse%26interface%3D
										                              com.alibaba.dubbo.demo.DemoService%26loadbalance%3Droundrobin%26methods%3DsayHello%26owner%3
										                              Dwilliam%26pid%3D2416%26side%3Dprovider%26timestamp%3D1474276306353
	                                -->createEphemeral(path);//临时节点  dubbo%3A%2F%2F192.168.100.52%3A20880%2F.............
	                                -->createPersistent(path);//持久化节点 dubbo/com.alibaba.dubbo.demo.DemoService/providers
	                                    
	                                    
	                    -->registry.subscribe//订阅ZK
	                      -->AbstractRegistry.subscribe
	                      -->FailbackRegistry.subscribe
	                        -->doSubscribe(url, listener)// 向服务器端发送订阅请求
	                          -->ZookeeperRegistry.doSubscribe
	                            -->new ChildListener()
	                              -->实现了 childChanged
	                                -->实现并执行 ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
	                              //A
	                            -->zkClient.create(path, false);//第一步：先创建持久化节点/dubbo/com.alibaba.dubbo.demo.DemoService/configurators
	                            -->zkClient.addChildListener(path, zkListener)
	                              -->AbstractZookeeperClient.addChildListener
	                                //C
	                                -->createTargetChildListener(path, listener)//第三步：收到订阅后的处理，交给FailbackRegistry.notify处理
	                                  -->ZkclientZookeeperClient.createTargetChildListener
	                                    -->new IZkChildListener() 
	                                      -->实现了 handleChildChange //收到订阅后的处理
	                                      	-->listener.childChanged(parentPath, currentChilds);
	                                      	-->实现并执行ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
	                                      	-->收到订阅后处理 FailbackRegistry.notify
	                                //B      	
	                                -->addTargetChildListener(path, targetListener)////第二步
	                                  -->ZkclientZookeeperClient.addTargetChildListener
	                                    -->client.subscribeChildChanges(path, listener)//第二步：启动加入订阅/dubbo/com.alibaba.dubbo.demo.DemoService/configurators
	                    
	                    -->notify(url, listener, urls)
	                      -->FailbackRegistry.notify
	                        -->doNotify(url, listener, urls);
	                          -->AbstractRegistry.notify
	                            -->saveProperties(url);//把服务端的注册url信息更新到C:\Users\bobo\.dubbo\dubbo-registry-192.168.48.117.cache
	                              -->registryCacheExecutor.execute(new SaveProperties(version));//采用线程池来处理
	                            -->listener.notify(categoryList)
	                              -->RegistryProtocol.notify
	                                -->RegistryProtocol.this.getProviderUrl(originInvoker)//通过invoker的url 获取 providerUrl的地址
	                                                                                                        
```

