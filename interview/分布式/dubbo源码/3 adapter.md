# 3 adapter

# 1 为什么要设计adaptive？注解在类上和注解在方法上的区别？

 在 Dubbo 中，很多拓展都是通过 SPI 机制进行加载的，比如 Protocol、Cluster、LoadBalance 等。有时，**有些拓展并不想在框架启动阶段被加载，而是希望在拓展方法被调用时，根据运行时参数进行加载。这听起来有些矛盾。拓展未被加载，那么拓展方法就无法被调用（静态方法除外）。拓展方法未被调用，拓展就无法被加载。对于这个矛盾的问题，Dubbo 通过自适应拓展机制很好的解决了**。自适应拓展机制的实现逻辑比较复杂，首先 Dubbo 会为拓展接口生成具有代理功能的代码。然后通过 javassist 或 jdk 编译这段代码，得到 Class 类。最后再通过反射创建代理类 。

### 扩展点(Extension Point 是一个Java的接口。

### 扩展(Extension)  扩展点的实现类。

###  扩展实例(Extension Instance)   扩展点实现类的实例。

###  扩展自适应实例(Extension Adaptive Instance)

如果称它为扩展代理类，可能更好理解些。扩展的自适应实例其实就是一个Extension的代理，它实现了扩展点接口。在调用扩展点的接口方法时，会根据实际的参数来决定要使用哪个扩展。比如一个IRepository的扩展点，有一个save方法。有两个实现MysqlRepository和MongoRepository。IRepository的自适应实例在调用接口方法的时候，会根据save方法中的参数，来决定要调用哪个IRepository的实现。如果方法参数中有repository=mysql，那么就调用MysqlRepository的save方法。如果repository=mongo，就调用MongoRepository的save方法。和面向对象的延迟绑定很类似。为什么Dubbo会引入扩展自适应实例的概念呢？

- Dubbo中的配置有两种，一种是固定的系统级别的配置，在Dubbo启动之后就不会再改了。还有一种是运行时的配置，可能对于每一次的RPC，这些配置都不同。比如在xml文件中配置了超时时间是10秒钟，这个配置在Dubbo启动之后，就不会改变了。但针对某一次的RPC调用，可以设置它的超时时间是30秒钟，以覆盖系统级别的配置。对于Dubbo而言，每一次的RPC调用的参数都是未知的。只有在运行时，根据这些参数才能做出正确的决定。
- 很多时候，我们的类都是一个单例的，比如Spring的bean，在Spring bean都实例化时，如果它依赖某个扩展点，但是在bean实例化时，是不知道究竟该使用哪个具体的扩展实现的。这时候就需要一个代理模式了，它实现了扩展点接口，方法内部可以根据运行时参数，动态的选择合适的扩展实现。而这个代理就是自适应实例。 自适应扩展实例在Dubbo中的使用非常广泛，Dubbo中，每一个扩展都会有一个自适应类，如果我们没有提供，Dubbo会使用字节码工具为我们自动生成一个。所以我们基本感觉不到自适应类的存在。后面会有例子说明自适应类是怎么工作的。
- 识别固定已知类和扩展未知类









1.注解在类上：代表人工实现，实现一个装饰类（设计模式中的装饰模式），它主要作用于固定已知类，
  目前整个系统只有2个，AdaptiveCompiler、AdaptiveExtensionFactory。
  a.为什么AdaptiveCompiler这个类是固定已知的？因为整个框架仅支持Javassist和JdkCompiler。
  b.为什么AdaptiveExtensionFactory这个类是固定已知的？因为整个框架仅支持2个objFactory,一个是spi,另一个是spring
2.注解在方法上：代表自动生成和编译一个动态的Adpative类，它主要是用于SPI，因为spi的类是不固定、未知的扩展类，所以设计了动态$Adaptive类.
例如 Protocol的spi类有 injvm dubbo registry filter listener等等 很多扩展未知类，
它设计了Protocol$Adaptive的类，通过ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(spi类);来提取对象

-----------------------getAdaptiveExtension()

-->getAdaptiveExtension()//为cachedAdaptiveInstance赋值
  -->createAdaptiveExtension()
    -->getAdaptiveExtensionClass()
      -->getExtensionClasses()//为cachedClasses 赋值
        -->loadExtensionClasses()
          -->loadFile
      -->createAdaptiveExtensionClass()/**/自动生成和编译一个动态的adpative类，这个类是一个代理类**
        -->ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.compiler.Compiler.class).getAdaptiveExtension()
        -->compiler.compile(code, classLoader)
    -->injectExtension()//作用：进入IOC的反转控制模式，实现了动态入注

# 2编译一个动态的adpative类

## 2.1 javassist生成动态类





## 2.2 jdk

# 2.3 dubbo选择  javassist 原因

生成动态代理类：都可以根据字节码生成class文件，JAVAASSIST既可以通过动态代理也可以通过字节码生成class文件

执行代理类的方法，javassist更快



