# 1 总概括

![è¿éåå¾çæè¿°](https://img-blog.csdn.net/20180127210359151)

![è¿éåå¾çæè¿°](https://img-blog.csdn.net/20180127210410630)

## 1、面试题汇总

### （1）java中有几种类型的流？

字符流和字节流。字节流继承inputStream和OutputStream,字符流继承自InputSteamReader和OutputStreamWriter。

 

### （2）谈谈Java IO里面的常见类，字节流，字符流、接口、实现类、方法阻塞

答：输入流就是从外部文件输入到内存，输出流主要是从内存输出到文件。 

IO里面常见的类，第一印象就只知道IO流中有很多类，IO流主要分为字符流和字节流。字符流中有抽象类InputStream和OutputStream，它们的子类FileInputStream，FileOutputStream,BufferedOutputStream等。字符流BufferedReader和Writer等。都实现了Closeable, Flushable, Appendable这些接口。程序中的输入输出都是以流的形式保存的，流中保存的实际上全都是字节文件。 

java中的阻塞式方法是指在程序调用改方法时，必须等待输入数据可用或者检测到输入结束或者抛出异常，否则程序会一直停留在该语句上，不会执行下面的语句。比如read()和readLine()方法。

 

### （3）字符流和字节流有什么区别？

要把一片二进制数据数据逐一输出到某个设备中，或者从某个设备中逐一读取一片二进制数据，不管输入输出设备是什么，我们要用统一的方式来完成这些操作，用一种抽象的方式进行描述，这个抽象描述方式起名为IO流，对应的抽象类为OutputStream和InputStream ，不同的实现类就代表不同的输入和输出设备，它们都是针对字节进行操作的。

（字符流处理的单元为 2 个字节的 Unicode 字符，分别操作字符、字符数组或字符串；而字节流处理单元为 1 个字节，操作字节和字节数组。所以字符流是由Java虚拟机将字节转化为2个字节的Unicode字符为单位的字符而成的，如果是音频文件、图片、歌曲，就用字节流好点（避免数据丢失）；如果是关系到中文（文本）的，用字符流好点）



 

在应用中，经常要完全是字符的一段文本输出去或读进来，用字节流可以吗？ 

计算机中的一切最终都是二进制的字节形式存在。对于“中国”这些字符，首先要得到其对应的字节，然后将字节写入到输出流。读取时，首先读到的是字节，可是我们要把它显示为字符，我们需要将字节转换成字符。由于这样的需求很广泛，人家专门提供了字符流的包装类。

 

底层设备永远只接受字节数据，有时候要写字符串到底层设备，需要将字符串转成字节再进行写入。字符流是字节流的包装，字符流则是直接接受字符串，它内部将串转成字节，再写入底层设备，这为我们向IO设别写入或读取字符串提供了一点点方便。

 

### （4）讲讲NIO

答：看了一些文章，传统的IO流是阻塞式的，会一直监听一个ServerSocket，在调用read等方法时，他会一直等到数据到来或者缓冲区已满时才返回。调用accept也是一直阻塞到有客户端连接才会返回。每个客户端连接过来后，服务端都会启动一个线程去处理该客户端的请求。并且多线程处理多个连接。每个线程拥有自己的栈空间并且占用一些 CPU 时间。每个线程遇到外部未准备好的时候，都会阻塞掉。阻塞的结果就是会带来大量的进程上下文切换。 

对于NIO，它是非阻塞式，核心类： 

1.Buffer为所有的原始类型提供 (Buffer)缓存支持。 

2.Charset字符集编码解码解决方案 

3.Channel一个新的原始 I/O抽象，用于读写Buffer类型，通道可以认为是一种连接，可以是到特定设备，程序或者是网络的连接。

### （5）递归读取文件夹的文件

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

```
 1 package test;
 2 
 3 import java.io.File;
 4 
 5 /**
 6  * 
 7  * 递归读取文件夹的文件
 8  */
 9 public class ListFileDemo {
10     public static void listFile(String path) {
11         if (path == null) {
12             return;// 因为下面的new File如果path为空，回报异常
13         }
14         File[] files = new File(path).listFiles();
15         if (files == null) {
16             return;
17         }
18         for(File file : files) {
19             if (file.isFile()) {
20                 System.out.println(file.getName());
21             } else if (file.isDirectory()) {
22                 System.out.println("Directory:"+file.getName());
23                 listFile(file.getPath());
24             } else {
25                 System.out.println("Error");
26             }
27         }
28     }
29 
30     public static void main(String[] args) {
31         ListFileDemo.listFile("D:\\data");
32 
33     }
34 }
```

[![复制代码](https://common.cnblogs.com/images/copycode.gif)](javascript:void(0);)

![img](https://img2018.cnblogs.com/blog/1468106/201903/1468106-20190301093832521-1954292087.png)

 

![img](https://img2018.cnblogs.com/blog/1468106/201903/1468106-20190301093914075-416656378.png)

## 2、Java IO总结

### （1）明确源和目的。

数据source：就是需要读取，可以使用两个体系：InputStream、Reader； 
数据destination：就是需要写入，可以使用两个体系：OutputStream、Writer；

### （2）操作的数据是否是纯文本数据？

```
    如果是：
           数据source：Reader
           数据destination：Writer 
   如果不是：
           数据source：InputStream
           数据destination：OutputStream
```

### （3）Java IO体系中有太多的对象，到底用哪个呢？

明确操作的数据设备。 
数据source对应的设备：硬盘(File)，内存(数组)，键盘(System.in) 
数据destination对应的设备：硬盘(File)，内存(数组)，控制台(System.out)。

记住，只要一读取键盘录入，就用这句话。

```
BufferedReader bufr = new BufferedReader(new InputStreamReader(System.in)); 
BufferedWriter bufw = new BufferedWriter(new OutputStreamWriter(System.out));
```

# 3.buffer

没有缓存区，那么每read一次，就会发送一次IO操作；有缓存区，第一次read时，会一下读取x个字节放入缓存区，然后后续的read都会从缓存中读取，当read到缓存区末尾时，会再次读取x个字节放入缓存区。会减少IO操作，效率更高，缺点就是，内存占用的多



Buffer使用内存映射的方式来处理输入输出，Buffer将文件或者文件的一部分映射到内存中，这样就可以像访问内存一样访问文件了，通过这种方式访问文件要快很多，所以传统的输入输出是面向流的处理，那么新的输入输出则是面向"块"的处理。

Buffer可以理解成一个容器，发送到channel或者从channel中读取数据都需要先经过buffer进行处理，此处的buffer类似于一个缓冲器，既可以多次访问，每次访问获取一点数据，也可以一次映射某"块"数据加以处理。

从内部结构上来看，buffer就是一个数组，可以保存相同类型的一组数据，它有三个比较重要的概念：容量(capacity)、界限(limit)、位置(position)。
    容量：缓冲区的容量表示buffer可以存储的最多数据量，缓冲区的容量不可能为负值，并且创建后不可修改；
    界限：可以被读取或者可被写入的最大位置；
    位置：用于标志下一个可以被读取或者写入的位置索引；

当初始化一个buffer时，capacity为buffer边界最大值，limit为capacity，position为0，当写入一段数据之后，capacity不变，limit不变，position为写入数据最大值；再次写入数据，capacity不变，limit不变，position为两次数据和的最大值。当要读取数据时，设置buffer状态为读之后，capacity不变，limit为两次写入数据最大位置，position为0；读取一部分数据之后，capacity不变，limit不变，position为读取数据最大值位置。
