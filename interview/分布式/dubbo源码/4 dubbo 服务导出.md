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

### 

## 2.1：暴露本地服务

```java
Export dubbo service com.alibaba.dubbo.demo.DemoService to local registry, dubbo version: 2.0.0, current host: 127.0.0.1
    
    
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
                    
                    
private void exportLocal(URL url) {
    // 如果 URL 的协议头等于 injvm，说明已经导出到本地了，无需再次导出
    if (!Constants.LOCAL_PROTOCOL.equalsIgnoreCase(url.getProtocol())) {
        URL local = URL.valueOf(url.toFullString())
            .setProtocol(Constants.LOCAL_PROTOCOL)    // 设置协议头为 injvm
            .setHost(LOCALHOST)
            .setPort(0);
        ServiceClassHolder.getInstance().pushServiceClass(getServiceClass(ref));
        // 创建 Invoker，并导出服务，这里的 protocol 会在运行时调用 InjvmProtocol 的 export 方法
        Exporter<?> exporter = protocol.export(
            proxyFactory.getInvoker(ref, (Class) interfaceClass, local));
        exporters.add(exporter);
    }
}
                
                    
```

exportLocal 方法比较简单，首先根据 URL 协议头决定是否导出服务。若需导出，则创建一个新的 URL 并将协议头、主机名以及端口设置成新的值。然后创建 Invoker，并调用 InjvmProtocol 的 export 方法导出服务。下面我们来看一下 InjvmProtocol 的 export 方法都做了哪些事情。

```java
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
    // 创建 InjvmExporter
    return new InjvmExporter<T>(invoker, invoker.getUrl().getServiceKey(), exporterMap);
}
```


如上，InjvmProtocol 的 export 方法仅创建了一个 InjvmExporter，无其他逻辑。到此导出服务到本地就分析完了，接下来，我们继续分析导出服务到远程的过程。    



## 2.2 暴露远程服务

本节先来分析服务导出逻辑，服务注册逻辑将在下一节进行分析。下面开始分析，我们把目光移动到 RegistryProtocol 的 export 方法

```java
public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
    // 导出服务
    final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);

    // 获取注册中心 URL，以 zookeeper 注册中心为例，得到的示例 URL 如下：
    // zookeeper://127.0.0.1:2181/com.alibaba.dubbo.registry.RegistryService?application=demo-provider&dubbo=2.0.2&export=dubbo%3A%2F%2F172.17.48.52%3A20880%2Fcom.alibaba.dubbo.demo.DemoService%3Fanyhost%3Dtrue%26application%3Ddemo-provider
    URL registryUrl = getRegistryUrl(originInvoker);

    // 根据 URL 加载 Registry 实现类，比如 ZookeeperRegistry
    final Registry registry = getRegistry(originInvoker);
    
    // 获取已注册的服务提供者 URL，比如：
    // dubbo://172.17.48.52:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService&methods=sayHello
    final URL registeredProviderUrl = getRegisteredProviderUrl(originInvoker);

    // 获取 register 参数
    boolean register = registeredProviderUrl.getParameter("register", true);

    // 向服务提供者与消费者注册表中注册服务提供者
    ProviderConsumerRegTable.registerProvider(originInvoker, registryUrl, registeredProviderUrl);

    // 根据 register 的值决定是否注册服务
    if (register) {
        // 向注册中心注册服务
        register(registryUrl, registeredProviderUrl);
        ProviderConsumerRegTable.getProviderWrapper(originInvoker).setReg(true);
    }

    // 获取订阅 URL，比如：
    // provider://172.17.48.52:20880/com.alibaba.dubbo.demo.DemoService?category=configurators&check=false&anyhost=true&application=demo-provider&dubbo=2.0.2&generic=false&interface=com.alibaba.dubbo.demo.DemoService&methods=sayHello
    final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registeredProviderUrl);
    // 创建监听器
    final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl, originInvoker);
    overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
    // 向注册中心进行订阅 override 数据
    registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
    // 创建并返回 DestroyableExporter
    return new DestroyableExporter<T>(exporter, originInvoker, overrideSubscribeUrl, registeredProviderUrl);
}
```

上面代码看起来比较复杂，主要做如下一些操作：

1. 调用 doLocalExport 导出服务
2. 向注册中心注册服务
3. 向注册中心进行订阅 override 数据
4. 创建并返回 DestroyableExporter



主要先看 doLocalExport 方法的逻辑 

```java
private <T> ExporterChangeableWrapper<T> doLocalExport(final Invoker<T> originInvoker) {
    String key = getCacheKey(originInvoker);
    // 访问缓存
    ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
    if (exporter == null) {
        synchronized (bounds) {
            exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
            if (exporter == null) {
                // 创建 Invoker 为委托类对象
                final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker, getProviderUrl(originInvoker));
                // 调用 protocol 的 export 方法导出服务
                exporter = new ExporterChangeableWrapper<T>((Exporter<T>) protocol.export(invokerDelegete), originInvoker);
                
                // 写缓存
                bounds.put(key, exporter);
            }
        }
    }
    return exporter;
}
```

 假设运行时协议为 dubbo，此处的 protocol 变量会在运行时加载 DubboProtocol，并调用 DubboProtocol 的 export 方法。所以，接下来我们目光转移到 DubboProtocol 的 export 方法上，相关分析如下： 

```java
public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
    URL url = invoker.getUrl();

    // 获取服务标识，理解成服务坐标也行。由服务组名，服务名，服务版本号以及端口组成。比如：
    // demoGroup/com.alibaba.dubbo.demo.DemoService:1.0.1:20880
    String key = serviceKey(url);
    // 创建 DubboExporter
    DubboExporter<T> exporter = new DubboExporter<T>(invoker, key, exporterMap);
    // 将 <key, exporter> 键值对放入缓存中
    exporterMap.put(key, exporter);

    // 本地存根相关代码
    Boolean isStubSupportEvent = url.getParameter(Constants.STUB_EVENT_KEY, Constants.DEFAULT_STUB_EVENT);
    Boolean isCallbackservice = url.getParameter(Constants.IS_CALLBACK_SERVICE, false);
    if (isStubSupportEvent && !isCallbackservice) {
        String stubServiceMethods = url.getParameter(Constants.STUB_EVENT_METHODS_KEY);
        if (stubServiceMethods == null || stubServiceMethods.length() == 0) {
            // 省略日志打印代码
        } else {
            stubServiceMethodsMap.put(url.getServiceKey(), stubServiceMethods);
        }
    }

    // 启动服务器
    openServer(url);
    // 优化序列化
    optimizeSerialization(url);
    return exporter;
}
```

 重点关注 DubboExporter 的创建以及 openServer 方法 

```java
private void openServer(URL url) {
    // 获取 host:port，并将其作为服务器实例的 key，用于标识当前的服务器实例
    String key = url.getAddress();
    boolean isServer = url.getParameter(Constants.IS_SERVER_KEY, true);
    if (isServer) {
        // 访问缓存
        ExchangeServer server = serverMap.get(key);
        if (server == null) {
            // 创建服务器实例
            serverMap.put(key, createServer(url));
        } else {
            // 服务器已创建，则根据 url 中的配置重置服务器
            server.reset(url);
        }
    }
}
```



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
                .......省略
          //如果配置不是local则暴露为远程服务.(配置为local，则表示只暴露本地服务)
          -->proxyFactory.getInvoker//原理和本地暴露一样都是为了获取一个Invoker对象
    
    
    
    
```

## 2.3 Invoker 创建过程

## 2.4  服务器实例的创建过程ExchangeServer

```java
private ExchangeServer createServer(URL url) {
    url = url.addParameterIfAbsent(Constants.CHANNEL_READONLYEVENT_SENT_KEY,
    // 添加心跳检测配置到 url 中
    url = url.addParameterIfAbsent(Constants.HEARTBEAT_KEY, String.valueOf(Constants.DEFAULT_HEARTBEAT));
	// 获取 server 参数，默认为 netty
    String str = url.getParameter(Constants.SERVER_KEY, Constants.DEFAULT_REMOTING_SERVER);

	// 通过 SPI 检测是否存在 server 参数所代表的 Transporter 拓展，不存在则抛出异常
    if (str != null && str.length() > 0 && !ExtensionLoader.getExtensionLoader(Transporter.class).hasExtension(str))
        throw new RpcException("Unsupported server type: " + str + ", url: " + url);

    // 添加编码解码器参数
    url = url.addParameter(Constants.CODEC_KEY, DubboCodec.NAME);
    ExchangeServer server;
    try {
        // 创建 ExchangeServer
        server = Exchangers.bind(url, requestHandler);
    } catch (RemotingException e) {
        throw new RpcException("Fail to start server...");
    }
                                   
	// 获取 client 参数，可指定 netty，mina
    str = url.getParameter(Constants.CLIENT_KEY);
    if (str != null && str.length() > 0) {
        // 获取所有的 Transporter 实现类名称集合，比如 supportedTypes = [netty, mina]
        Set<String> supportedTypes = ExtensionLoader.getExtensionLoader(Transporter.class).getSupportedExtensions();
        // 检测当前 Dubbo 所支持的 Transporter 实现类名称列表中，
        // 是否包含 client 所表示的 Transporter，若不包含，则抛出异常
        if (!supportedTypes.contains(str)) {
            throw new RpcException("Unsupported client type...");
        }
    }
    return server;
}
```

如上，createServer 包含三个核心的逻辑。第一是检测是否存在 server 参数所代表的 Transporter 拓展，不存在则抛出异常。第二是创建服务器实例。第三是检测是否支持 client 参数所表示的 Transporter 拓展，不存在也是抛出异常。两次检测操作所对应的代码比较直白了，无需多说。但创建服务器的操作目前还不是很清晰，我们继续往下看。

```java
public static ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    if (handler == null) {
        throw new IllegalArgumentException("handler == null");
    }
    url = url.addParameterIfAbsent(Constants.CODEC_KEY, "exchange");
    // 获取 Exchanger，默认为 HeaderExchanger。
    // 紧接着调用 HeaderExchanger 的 bind 方法创建 ExchangeServer 实例
    return getExchanger(url).bind(url, handler);
}
```

上面代码比较简单，就不多说了。下面看一下 HeaderExchanger 的 bind 方法。

```java
public ExchangeServer bind(URL url, ExchangeHandler handler) throws RemotingException {
	// 创建 HeaderExchangeServer 实例，该方法包含了多个逻辑，分别如下：
	//   1. new HeaderExchangeHandler(handler)
	//	 2. new DecodeHandler(new HeaderExchangeHandler(handler))
	//   3. Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler)))
    return new HeaderExchangeServer(Transporters.bind(url, new DecodeHandler(new HeaderExchangeHandler(handler))));
}
```



## 2.5：启动netty

HeaderExchanger 的 bind 方法包含的逻辑比较多，但目前我们仅需关心 Transporters 的 bind 方法逻辑即可。该方法的代码如下：

```java
public static Server bind(URL url, ChannelHandler... handlers) throws RemotingException {
    if (url == null) {
        throw new IllegalArgumentException("url == null");
    }
    if (handlers == null || handlers.length == 0) {
        throw new IllegalArgumentException("handlers == null");
    }
    ChannelHandler handler;
    if (handlers.length == 1) {
        handler = handlers[0];
    } else {
    	// 如果 handlers 元素数量大于1，则创建 ChannelHandler 分发器
        handler = new ChannelHandlerDispatcher(handlers);
    }
    // 获取自适应 Transporter 实例，并调用实例方法
    return getTransporter().bind(url, handler);
}
```

如上，getTransporter() 方法获取的 Transporter 是在运行时动态创建的，类名为 TransporterAdaptive，也就是自适应拓展类。TransporterAdaptive 会在运行时根据传入的 URL 参数决定加载什么类型的 Transporter，默认为 NettyTransporter。下面我们继续跟下去，这次分析的是 NettyTransporter 的 bind 方法。

```java
public Server bind(URL url, ChannelHandler listener) throws RemotingException {
	// 创建 NettyServer
	return new NettyServer(url, listener);
}
```

这里仅有一句创建 NettyServer 的代码，无需多说，我们继续向下看。

```java
public class NettyServer extends AbstractServer implements Server {
    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        // 调用父类构造方法
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, SERVER_THREAD_POOL_NAME)));
    }
}


public abstract class AbstractServer extends AbstractEndpoint implements Server {
    public AbstractServer(URL url, ChannelHandler handler) throws RemotingException {
        // 调用父类构造方法，这里就不用跟进去了，没什么复杂逻辑
        super(url, handler);
        localAddress = getUrl().toInetSocketAddress();

        // 获取 ip 和端口
        String bindIp = getUrl().getParameter(Constants.BIND_IP_KEY, getUrl().getHost());
        int bindPort = getUrl().getParameter(Constants.BIND_PORT_KEY, getUrl().getPort());
        if (url.getParameter(Constants.ANYHOST_KEY, false) || NetUtils.isInvalidLocalHost(bindIp)) {
            // 设置 ip 为 0.0.0.0
            bindIp = NetUtils.ANYHOST;
        }
        bindAddress = new InetSocketAddress(bindIp, bindPort);
        // 获取最大可接受连接数
        this.accepts = url.getParameter(Constants.ACCEPTS_KEY, Constants.DEFAULT_ACCEPTS);
        this.idleTimeout = url.getParameter(Constants.IDLE_TIMEOUT_KEY, Constants.DEFAULT_IDLE_TIMEOUT);
        try {
            // 调用模板方法 doOpen 启动服务器
            doOpen();
        } catch (Throwable t) {
            throw new RemotingException("Failed to bind ");
        }

        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        executor = (ExecutorService) dataStore.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY, Integer.toString(url.getPort()));
    }
    
    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;
}
```

上面代码多为赋值代码，不需要多讲。我们重点关注 doOpen 抽象方法，该方法需要子类实现。下面回到 NettyServer 中。

```java
protected void doOpen() throws Throwable {
    NettyHelper.setNettyLoggerFactory();
    // 创建 boss 和 worker 线程池
    ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerBoss", true));
    ExecutorService worker = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerWorker", true));
    ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, getUrl().getPositiveParameter(Constants.IO_THREADS_KEY, Constants.DEFAULT_IO_THREADS));
    
    // 创建 ServerBootstrap
    bootstrap = new ServerBootstrap(channelFactory);

    final NettyHandler nettyHandler = new NettyHandler(getUrl(), this);
    channels = nettyHandler.getChannels();
    bootstrap.setOption("child.tcpNoDelay", true);
    // 设置 PipelineFactory
    bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
        @Override
        public ChannelPipeline getPipeline() {
            NettyCodecAdapter adapter = new NettyCodecAdapter(getCodec(), getUrl(), NettyServer.this);
            ChannelPipeline pipeline = Channels.pipeline();
            pipeline.addLast("decoder", adapter.getDecoder());
            pipeline.addLast("encoder", adapter.getEncoder());
            pipeline.addLast("handler", nettyHandler);
            return pipeline;
        }
    });
    // 绑定到指定的 ip 和端口上
    channel = bootstrap.bind(getBindAddress());
}
```

以上就是 NettyServer 创建的过程，dubbo 默认使用的 NettyServer 是基于 netty 3.x 版本实现的，比较老了。因此 Dubbo 另外提供了 netty 4.x 版本的 NettyServer，大家可在使用 Dubbo 的过程中按需进行配置。

到此，关于服务导出的过程就分析完了。整个过程比较复杂，大家在分析的过程中耐心一些。并且多写 Demo 进行调试，以便能够更好的理解代码逻辑。

## 2.6 服务注册(zk)

 RegistryProtocol 的 export 方法上 

```java
public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
    
    // ${导出服务}
    
    // 省略其他代码
    
    boolean register = registeredProviderUrl.getParameter("register", true);
    if (register) {
        // 注册服务
        register(registryUrl, registeredProviderUrl);
        ProviderConsumerRegTable.getProviderWrapper(originInvoker).setReg(true);
    }
    
    final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registeredProviderUrl);
    final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl, originInvoker);
    overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
    // 订阅 override 数据
    registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);

    // 省略部分代码
}
```

RegistryProtocol 的 export 方法包含了服务导出，注册，以及数据订阅等逻辑。其中服务导出逻辑上一节已经分析过了，本节将分析服务注册逻辑，相关代码如下：

```java
public void register(URL registryUrl, URL registedProviderUrl) {
    // 获取 Registry
    Registry registry = registryFactory.getRegistry(registryUrl);
    // 注册服务
    registry.register(registedProviderUrl);
}
```

register 方法包含两步操作，第一步是获取注册中心实例，第二步是向注册中心注册服务。接下来分两节内容对这两步操作进行分析。

#### 2.6.1 创建注册中心

本节内容以 Zookeeper 注册中心为例进行分析。下面先来看一下 getRegistry 方法的源码，这个方法由 AbstractRegistryFactory 实现。如下：

```java
public Registry getRegistry(URL url) {
    url = url.setPath(RegistryService.class.getName())
            .addParameter(Constants.INTERFACE_KEY, RegistryService.class.getName())
            .removeParameters(Constants.EXPORT_KEY, Constants.REFER_KEY);
    String key = url.toServiceString();
    LOCK.lock();
    try {
    	// 访问缓存
        Registry registry = REGISTRIES.get(key);
        if (registry != null) {
            return registry;
        }
        
        // 缓存未命中，创建 Registry 实例
        registry = createRegistry(url);
        if (registry == null) {
            throw new IllegalStateException("Can not create registry...");
        }
        
        // 写入缓存
        REGISTRIES.put(key, registry);
        return registry;
    } finally {
        LOCK.unlock();
    }
}

protected abstract Registry createRegistry(URL url);
```

如上，getRegistry 方法先访问缓存，缓存未命中则调用 createRegistry 创建 Registry，然后写入缓存。这里的 createRegistry 是一个模板方法，由具体的子类实现。因此，下面我们到 ZookeeperRegistryFactory 中探究一番。

```java
public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    // zookeeperTransporter 由 SPI 在运行时注入，类型为 ZookeeperTransporter$Adaptive
    private ZookeeperTransporter zookeeperTransporter;

    public void setZookeeperTransporter(ZookeeperTransporter zookeeperTransporter) {
        this.zookeeperTransporter = zookeeperTransporter;
    }

    @Override
    public Registry createRegistry(URL url) {
        // 创建 ZookeeperRegistry
        return new ZookeeperRegistry(url, zookeeperTransporter);
    }
}
```

ZookeeperRegistryFactory 的 createRegistry 方法仅包含一句代码，无需解释，继续跟下去。

```java
public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
    super(url);
    if (url.isAnyHost()) {
        throw new IllegalStateException("registry address == null");
    }
    
    // 获取组名，默认为 dubbo
    String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT);
    if (!group.startsWith(Constants.PATH_SEPARATOR)) {
        // group = "/" + group
        group = Constants.PATH_SEPARATOR + group;
    }
    this.root = group;
    // 创建 Zookeeper 客户端，默认为 CuratorZookeeperTransporter
    zkClient = zookeeperTransporter.connect(url);
    // 添加状态监听器
    zkClient.addStateListener(new StateListener() {
        @Override
        public void stateChanged(int state) {
            if (state == RECONNECTED) {
                try {
                    recover();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    });
}
```

在上面的代码代码中，我们重点关注 ZookeeperTransporter 的 connect 方法调用，这个方法用于创建 Zookeeper 客户端。创建好 Zookeeper 客户端，意味着注册中心的创建过程就结束了。接下来，再来分析一下 Zookeeper 客户端的创建过程。

前面说过，这里的 zookeeperTransporter 类型为自适应拓展类，因此 connect 方法会在被调用时决定加载什么类型的 ZookeeperTransporter 拓展，默认为 CuratorZookeeperTransporter。下面我们到 CuratorZookeeperTransporter 中看一看。

```java
public ZookeeperClient connect(URL url) {
    // 创建 CuratorZookeeperClient
    return new CuratorZookeeperClient(url);
}
```

继续向下看。

```java
public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorWatcher> {

    private final CuratorFramework client;
    
    public CuratorZookeeperClient(URL url) {
        super(url);
        try {
            // 创建 CuratorFramework 构造器
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                    .connectString(url.getBackupAddress())
                    .retryPolicy(new RetryNTimes(1, 1000))
                    .connectionTimeoutMs(5000);
            String authority = url.getAuthority();
            if (authority != null && authority.length() > 0) {
                builder = builder.authorization("digest", authority.getBytes());
            }
            // 构建 CuratorFramework 实例
            client = builder.build();
            // 添加监听器
            client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState state) {
                    if (state == ConnectionState.LOST) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.DISCONNECTED);
                    } else if (state == ConnectionState.CONNECTED) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.CONNECTED);
                    } else if (state == ConnectionState.RECONNECTED) {
                        CuratorZookeeperClient.this.stateChanged(StateListener.RECONNECTED);
                    }
                }
            });
            
            // 启动客户端
            client.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
```

CuratorZookeeperClient 构造方法主要用于创建和启动 CuratorFramework 实例。以上基本上都是 Curator 框架的代码，大家如果对 Curator 框架不是很了解，可以参考 Curator 官方文档。

本节分析了 ZookeeperRegistry 实例的创建过程，整个过程并不是很复杂。大家在看完分析后，可以自行调试，以加深理解。现在注册中心实例创建好了，接下来要做的事情是向注册中心注册服务，我们继续往下看。

#### 2.6.2 节点创建

以 Zookeeper 为例，所谓的服务注册，本质上是将服务配置数据写入到 Zookeeper 的某个路径的节点下。为了让大家有一个直观的了解，下面我们将 Dubbo 的 demo 跑起来，然后通过 Zookeeper 可视化客户端 [ZooInspector](https://github.com/apache/zookeeper/tree/b79af153d0f98a4f3f3516910ed47234d7b3d74e/src/contrib/zooinspector) 查看节点数据。如下：

![img](http://dubbo.apache.org/docs/zh-cn/source_code_guide/sources/images/service-registry.png)

从上图中可以看到 com.alibaba.dubbo.demo.DemoService 这个服务对应的配置信息（存储在 URL 中）最终被注册到了 /dubbo/com.alibaba.dubbo.demo.DemoService/providers/ 节点下。搞懂了服务注册的本质，那么接下来我们就可以去阅读服务注册的代码了。服务注册的接口为 register(URL)，这个方法定义在 FailbackRegistry 抽象类中。代码如下：

```java
public void register(URL url) {
    super.register(url);
    failedRegistered.remove(url);
    failedUnregistered.remove(url);
    try {
        // 模板方法，由子类实现
        doRegister(url);
    } catch (Exception e) {
        Throwable t = e;

        // 获取 check 参数，若 check = true 将会直接抛出异常
        boolean check = getUrl().getParameter(Constants.CHECK_KEY, true)
                && url.getParameter(Constants.CHECK_KEY, true)
                && !Constants.CONSUMER_PROTOCOL.equals(url.getProtocol());
        boolean skipFailback = t instanceof SkipFailbackWrapperException;
        if (check || skipFailback) {
            if (skipFailback) {
                t = t.getCause();
            }
            throw new IllegalStateException("Failed to register");
        } else {
            logger.error("Failed to register");
        }

        // 记录注册失败的链接
        failedRegistered.add(url);
    }
}

protected abstract void doRegister(URL url);
```

如上，我们重点关注 doRegister 方法调用即可，其他的代码先忽略。doRegister 方法是一个模板方法，因此我们到 FailbackRegistry 子类 ZookeeperRegistry 中进行分析。如下：

```java
protected void doRegister(URL url) {
    try {
        // 通过 Zookeeper 客户端创建节点，节点路径由 toUrlPath 方法生成，路径格式如下:
        //   /${group}/${serviceInterface}/providers/${url}
        // 比如
        //   /dubbo/org.apache.dubbo.DemoService/providers/dubbo%3A%2F%2F127.0.0.1......
        zkClient.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
    } catch (Throwable e) {
        throw new RpcException("Failed to register...");
    }
}
```

如上，ZookeeperRegistry 在 doRegister 中调用了 Zookeeper 客户端创建服务节点。节点路径由 toUrlPath 方法生成，该方法逻辑不难理解，就不分析了。接下来分析 create 方法，如下：

```java
public void create(String path, boolean ephemeral) {
    if (!ephemeral) {
        // 如果要创建的节点类型非临时节点，那么这里要检测节点是否存在
        if (checkExists(path)) {
            return;
        }
    }
    int i = path.lastIndexOf('/');
    if (i > 0) {
        // 递归创建上一级路径
        create(path.substring(0, i), false);
    }
    
    // 根据 ephemeral 的值创建临时或持久节点
    if (ephemeral) {
        createEphemeral(path);
    } else {
        createPersistent(path);
    }
}
```

上面方法先是通过递归创建当前节点的上一级路径，然后再根据 ephemeral 的值决定创建临时还是持久节点。createEphemeral 和 createPersistent 这两个方法都比较简单，这里简单分析其中的一个。如下：

```java
public void createEphemeral(String path) {
    try {
        // 通过 Curator 框架创建节点
        client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
    } catch (NodeExistsException e) {
    } catch (Exception e) {
        throw new IllegalStateException(e.getMessage(), e);
    }
}
```

好了，到此关于服务注册的过程就分析完了。整个过程可简单总结为：先创建注册中心实例，之后再通过注册中心实例注册服务。本节先到这，接下来分析数据订阅过程。



打开连接zk
	INFO zookeeper.ClientCnxn: Opening socket connection to server /192.168.48.117:2181
到zk注册

```java
Register: dubbo://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1
```



​	Register: dubbo://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1

## 2.7 监听zk

registry.subscribe





```java
Subscribe: provider://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, dubbo version: 2.0.0, current host: 127.0.0.1
	Notify urls for subscribe url provider://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465, urls: [empty://192.168.100.38:20880/com.alibaba.dubbo.demo.DemoService?anyhost=true&application=demo-provider&category=configurators&check=false&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.demo.DemoService&loadbalance=roundrobin&methods=sayHello&owner=william&pid=8484&side=provider&timestamp=1473908495465], dubbo version: 2.0.0, current host: 127.0.0.1
```

## 2.8

	                    -->notify(url, listener, urls)
	                      -->FailbackRegistry.notify
	                        -->doNotify(url, listener, urls);
	                          -->AbstractRegistry.notify
	                            -->saveProperties(url);//把服务端的注册url信息更新到C:\Users\bobo\.dubbo\dubbo-registry-192.168.48.117.cache
	                              -->registryCacheExecutor.execute(new SaveProperties(version));//采用线程池来处理
	                            -->listener.notify(categoryList)
	                              -->RegistryProtocol.notify
	                                -->RegistryProtocol.this.getProviderUrl(originInvoker)//通过invoker的url 获取 providerUrl的地址 
# 整体时序

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

