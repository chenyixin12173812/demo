# 1.BeanFactory 和 ApplicationContext 区别联系

 ![img](https://img.jbzj.com/file_images/article/202006/2020616165405111.jpg?2020516165418) 

# 2. @Qualifier注解？





# 3**切面有几种类型的通知？分别是？**

前置通知(Before): 目标方法被调用之前调用通知功能。
后置通知(After): 目标方法完成之后调用通。
返回通知(After-returning): 目标方法成功执行之后调用通知。
异常通知(After-throwing): 目标方法抛出异常后调用通知。
环绕通知(Around): 在被通知的方法调用之前和调用之后执行自定义的行为。 

# 4  **Spring中的bean有几种scope?**

- singleton: 单例，每一个bean只创建一个对象实例。
- prototype，原型，每次对该bean请求调用都会生成各自的实例。
- request，请求，针对每次HTTP请求都会生成一个新的bean。表示在一次 HTTP 请求内有效。
- session，在一个http session中，一个bean定义对应一个bean实例。
- global session:在一个全局http session中，一个bean定义对应一个bean实例

# 5 .你能说几个Spring5的新特性吗？

- spring5整个框架基于java8
- 支持http/2
- Spring Web MVC支持最新API
- Spring WebFlux 响应式编程
- 支持Kotlin函数式编程

 # 6. **bean的生命周期?**
1.Spring 对bean进行实例化。
2.Spring将值和bean的引用注入到 bean对应的属性中。
3.如果bean实现了BeanNameAware接口，Spring将bean的ID传递给setBeanName()方法。
4.如果bean实现了BeanFactoryAware接口， Spring将调用setBeanFactory()方法，将 bean所在的应用引用传入进来。
5.如果bean实现了ApplicationContextAware接口，Spring将调用setApplicationContext()方法，将bean所在的应用引用传入进来。
6.如果bean实现了BeanPostProcessor 接口，Spring将调用他们的post-ProcessBeforeInitalization()方法。
7.如果bean实现了InitializingBean接口，Spring将调用他们的after-PropertiesSet()方法，类似地，如果bean使用init-method声明了初始化方法，该方法也会被调用。
8.如果bean实现了BeanPostProcessor接口，Spring将调用它们的post-ProcessAfterInitialization()方法。
9.此时， bean已经准备就绪，可以被应用程序使用了，他们将一直驻留在应用上下文中,直到该应用被销毁。
10.如果bean实现了DisposableBean接口，Spring将调用它的destory()接口方法，同样，如果bean使用destroy-method声明了销毁方法，该方法也会被调用。 

# 7. **什么是连接点 （Join point)?**
连接点是在应用执行过程中能够插入切面的一个点。这个点可以是调用方法时、抛出异常时、甚至修改一个字段时 

# 8.  **切点（Pointcut)?** 

切点的定义会匹配通知所要织入的一个或多个连接点。我们通常使用明确的类和方法名称，或是利用正则表达式定义所匹配的类和方法名称来指定这些切点。有些AOP框架允许我们创建动态的切点，可以根据运行时的决策(比如方法的参数值)来决定是否应用通知。 

# 9. **切面(Aspect)** 

 切面是通知和切点的结合。通知和切点共同定义了切面的全部内容。 

# 10 . **Spring事务传播行为有哪些** 

 ![img](https://img.jbzj.com/file_images/article/202006/2020616165510955.jpg?2020516165556) 



| ***\*传播行为\****                                    | ***\*含义\****                                               |
| ----------------------------------------------------- | ------------------------------------------------------------ |
| PROPAGATION_REQUIRED（XML文件中为REQUIRED)            | 表示当前方法必须在一个具有事务的上下文中运行，如有客户端有事务在进行，那么被调用端将在该事务中运行，否则的话重新开启一个事务。（如果被调用端发生异常，那么调用端和被调用端事务都将回滚） |
| PROPAGATION_SUPPORTS(XML文件中为SUPPORTS）            | 表示当前方法不必需要具有一个事务上下文，但是如果有一个事务的话，它也可以在这个事务中运行 |
| PROPAGATION_MANDATORY(XML文件中为MANDATORY）          | 表示当前方法必须在一个事务中运行，如果没有事务，将抛出异常   |
| PROPAGATION_NESTED(XML文件中为NESTED)                 | 表示如果当前方法正有一个事务在运行中，则该方法应该运行在一个嵌套事务中，被嵌套的事务可以独立于被封装的事务中进行提交或者回滚。如果封装事务存在，并且外层事务抛出异常回滚，那么内层事务必须回滚，反之，内层事务并不影响外层事务。如果封装事务不存在，则同PROPAGATION_REQUIRED的一样 |
| PROPAGATION_NEVER（XML文件中为NEVER)                  | 表示当方法务不应该在一个事务中运行，如果存在一个事务，则抛出异常 |
| PROPAGATION_REQUIRES_NEW(XML文件中为REQUIRES_NEW）    | 表示当前方法必须运行在它自己的事务中。一个**新的事务将启动，而且如果有一个现有的事务在运行的话，则这个方法将在运行期被挂起**，直到新的事务提交或者回滚才恢复执行。 |
| PROPAGATION_NOT_SUPPORTED（XML文件中为NOT_SUPPORTED） | 表示该方法不应该在一个事务中运行。如果有一个事务正在运行，他将在运行期被挂起，直到这个事务提交或者回滚才恢复执行 |



# 11 Spring 框架中用到了哪些设计模式？

工厂设计模式 : Spring使用工厂模式通过 BeanFactory、ApplicationContext 创建 bean 对象。
代理设计模式 : Spring AOP 功能的实现。
单例设计模式 : Spring 中的 Bean 默认都是单例的。
模板方法模式 : Spring 中 jdbcTemplate、hibernateTemplate 等以 Template 结尾的对数据库操作的类，它们就使用到了模板模式。
包装器设计模式 : 我们的项目需要连接多个数据库，而且不同的客户在每次访问中根据需要会去访问不同的数据库。这种模式让我们可以根据客户的需求能够动态切换不同的数据源。
观察者模式: Spring 事件驱动模型就是观察者模式很经典的一个应用。
适配器模式 :Spring AOP 的增强或通知(Advice)使用到了适配器模式、spring MVC 中也是用到了适配器模式适配Controller

# 12 springMVC的流程是什么？主要组件说明?SpringMVC的优点?

# ----------------springMVC的流程------------------------

- 第一步：发起请求到前端控制器(DispatcherServlet)
- 第二步：前端控制器请求HandlerMapping查找 Handler （可以根据xml配置、注解进行查找）
- 第三步：处理器映射器HandlerMapping向前端控制器返回Handler，HandlerMapping会把请求映射为HandlerExecutionChain对象（包含一个Handler处理器（页面控制器）对象，多个HandlerInterceptor拦截器对象），通过这种策略模式，很容易添加新的映射策略
- 第四步：前端控制器调用处理器适配器去执行Handler
- 第五步：处理器适配器HandlerAdapter将会根据适配的结果去执行Handler
- 第六步：Handler执行完成给适配器返回ModelAndView
- 第七步：处理器适配器向前端控制器返回ModelAndView （ModelAndView是springmvc框架的一个底层对象，包括 Model和view）
- 第八步：前端控制器请求视图解析器去进行视图解析 （根据逻辑视图名解析成真正的视图(jsp)），通过这种策略很容易更换其他视图技术，只需要更改视图解析器即可
- 第九步：视图解析器向前端控制器返回View
- 第十步：前端控制器进行视图渲染 （视图渲染将模型数据(在ModelAndView对象中)填充到request域）
- 第十一步：前端控制器向用户响应结果　





![68747470733a2f2f757365722d676f6c642d63646e2e786974752e696f2f323031382f31312f31302f313636666434353738373339343139323f773d3130313526683d34363626663d7765627026733d3335333532](G:\codeTest\interview\spring\68747470733a2f2f757365722d676f6c642d63646e2e786974752e696f2f323031382f31312f31302f313636666434353738373339343139323f773d3130313526683d34363626663d7765627026733d3335333532.webp)





客户端发送请求-> 前端控制器 DispatcherServlet 接受客户端请求 -> 找到处理器映射 HandlerMapping 解析请求对应的 Handler-> HandlerAdapter 会根据 Handler 来调用真正的处理器处理请求，并处理相应的业务逻辑 -> 处理器返回一个模型视图 ModelAndView -> 视图解析器进行解析 -> 返回一个视图对象->前端控制器 DispatcherServlet 渲染数据（Model）->将得到视图对象返回给用户











# ----------------主要组件说明------------------------

首先，我们得说出每个组件是做什么的，当然要每个单词读出来最好。DispatcherServlet：

spring mvc的入口，整个框架运行就是在这个servlet中完成。

HandlerMapping：处理器映射起。用于映射每个处理方法对应的请求路径。是一个map结构<url,method>。

handler：处理器。实际上就是控制器中的每个处理方法。

HandlerAdapter：处理器适配器。专门用来调用handler，因为在spring mvc中每个处理方法参数以及返回类型都不一样，因此需要用适配器来适配。

ViewResovler：视图解析器。用于指定视图技术，以及视图技术相关的配置。

View:视图。springmvc 中支持多种视图，除了jsp外 还有xml,json,pdf等。

# ---------------------SpringMVC的优点-----------------

1.清晰的角色划分：控制器(controller)、验证器(validator)、命令对象(command obect)、表单对象(form object)、模型对象(model object)、Servlet分发器(DispatcherServlet)、处理器映射(handler mapping)、试图解析器(view resoler)等等。每一个角色都可以由一个专门的对象来实现。

2.强大而直接的配置方式：将框架类和应用程序类都能作为JavaBean配置，支持跨多个context的引用，例如，在web控制器中对业务对象和验证器validator)的引用。

3.可适配、非侵入：可以根据不同的应用场景，选择何事的控制器子类(simple型、command型、from型、wizard型、multi-action型或者自定义)，而不是一个单一控制器(比如Action/ActionForm)继承。

4.可重用的业务代码：可以使用现有的业务对象作为命令或表单对象，而不需要去扩展某个特定框架的基类。

5.可定制的绑定(binding)和验证(validation)：比如将类型不匹配作为应用级的验证错误，这可以保证错误的值。再比如本地化的日期和数字绑定等等。在其他某些框架中，你只能使用字符串表单对象，需要手动解析它并转换到业务对象。

6.可定制的handler mapping和view resolution：Spring提供从最简单的URL映射，到复杂的、专用的定制策略。与某些web MVC框架强制开发人员使用单一特定技术相比，Spring显得更加灵活。

7.灵活的model转换：在Springweb框架中，使用基于Map的键/值对来达到轻易的与各种视图技术集成。

8.可定制的本地化和主题(theme)解析：支持在JSP中可选择地使用Spring标签库、支持JSTL、支持Velocity(不需要额外的中间层)等等。

9.简单而强大的JSP标签库(Spring Tag Library)：支持包括诸如数据绑定和主题(theme)之类的许多功能。他提供在标记方面的最大灵活性。

10.JSP表单标签库：在Spring2.0中引入的表单标签库，使用在JSP编写表单更加容易。

11.Spring Bean的生命周期：可以被限制在当前的HTTp Request或者HTTp Session。准确的说，这并非Spring MVC框架本身特性，而应归属于Spring MVC使用的WebApplicationContext容器。

# 13 AOP使用场景

AOP用来封装横切关注点，具体可以在下面的场景中使用:

Authentication 权限 licsens

Caching 缓存

Context passing 内容传递

Error handling 错误处理

Lazy loading　懒加载

Debugging　　调试

logging, tracing, profiling and monitoring　记录跟踪　优化　校准

Performance optimization　性能优化

Persistence　　持久化

Resource pooling　资源池

Synchronization　同步

Transactions 事务

 # 14、什么是 Spring 框架？Spring 框架有哪些主要模块？

# 15、使用 Spring 框架能带来哪些好处？

# 16、什么是控制反转(IOC)？什么是依赖注入？

# 17、请解释下 Spring 框架中的 IoC？

# 18、BeanFactory 和 ApplicationContext 有什么区别？

# 19、Spring 有几种配置方式？

# 20、如何用基于 XML 配置的方式配置 Spring？

# 21、如何用基于 Java 配置的方式配置 Spring？

# 22、怎样用注解的方式配置 Spring？

# 23、请解释 Spring Bean 的生命周期？

# 24、Spring Bean 的作用域之间有什么区别？

# 25、什么是 Spring inner beans？

# 26、Spring 框架中的单例 Beans 是线程安全的么？

# 27、请举例说明如何在 Spring 中注入一个 Java Collection？

# 28、如何向 Spring Bean 中注入一个 Java.util.Properties？

16、请解释 Spring Bean 的自动装配？
17、请解释自动装配模式的区别？
18、如何开启基于注解的自动装配？
19、请举例解释@Required 注解？
20、请举例解释@Autowired 注解？
21、请举例说明@Qualifier 注解？
22、构造方法注入和设值注入有什么区别？
23、Spring 框架中有哪些不同类型的事件？
24、FileSystemResource 和 ClassPathResource 有何区别？
25、Spring 框架中都用到了哪些设计模式？ 

# 29 ExceptionHandler



 @Controller+@ExceptionHandler、HandlerExceptionResolver接口形式、@ControllerAdvice+@ExceptionHandler优缺点说明： 

