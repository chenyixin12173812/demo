# 1 简述三次握手和四次挥手

①首先 Client 端发送连接请求报文，

②Server 段接受连接后回复 ACK 报文，并为这次连接分配资源。

③Client 端接收到 ACK 报文后也向 Server 段发生 ACK 报文，并分配资源，这样 TCP 连接就建立了。

①服务端申请断开连接即FIN，发送Seq+Ack

②客户端接收信息返回，表示我已经接收到

③客户端发送信息表示可以断开连接

④服务端接受信息，返回数据表示已接受信息

## 1.1 之间11种状态转移和状态的含义

## 1.2 为什么要Time_Wait

## 1.3 谁先关闭谁先进入time_wait状态

可靠的终止TCP连接。
保证让迟来的TCP报文有足够的时间被识别并丢弃
让网络上的数据包自动消亡，防止旧连接初始了新的连接
这个期间这个连接的四元组不能被使用，可以设置端口重用（慎用）

## 1.4 要是没有三次握手会怎么样

三次握手的首要原因是为了防止旧的重复连接初始化造成混乱。（防止历史连接初始化了连接）
同步双方初始序列号
避免资源浪费

## 1.5 要是没有四次挥手会怎么样

防止旧连接的数据包
保证连接正确关闭

## 1.6 握手报文里都有哪些关键字段

ISN代表什么？意义何在？

ISN，发送方的字节数据编号的原点，让对方生成一个合法的接收窗口。

ISN是固定不变的吗？

动态随机。















# 2 Http 502 响应码一般是什么情况，怎么解决

# 3  七层网络模型中，常用的协议



# 4 udp 和 tcp 的区别



# 5 Http 请求的方法 HEAD？OPTION？PATCH，什么是 restful？Https 的握手过程

http POST 请求都有哪些内容？ body 体中 Form 表单和 file 有什么区别

form: 会将数据 按照urlencode 进行转码

​                Content-Type: application/x-www-form-urlencoded;charset=utf-8  title=test&sub%5B%5D=1&sub%5B%5D=2&sub%5B%5D=3              

\2. file：数据以 **multipart/form-data 来编码**

\3. **application/json，json的数据格式传递**

**4. text/xml 编码格式**





502：Bad Gateway 网关错误，表示它从上游服务器收到的响应是无效的。需要途径的 Web 服务器或者代理服务器对其进行修复，没有足够的进程处理请求。通常通过修改 php-fpm.conf 调整 pm 进程管理方式，或增加static max_children 

pm = dynamic start_server min_spare_server max_spare_server

​     504: Gateway Timeout 表示网关超时，无法在规定的时间内获得想要的响应。通常是进程处理超时导致 1. nginx 修改 timeout 时间 2. 修改 CURL timeout 和 connect_timeout

netstate 查看网络状态 -anp



TCP 是面向连接的, 建立连接的过程
TCP 建立连接的时候, 客户端和服务器端的状态变化(包括超时处理等都要回答)
TCP 的超时重传机制, 拥塞控制机制
DNS 用 TCP 还是 UD

 MTU/MSS
什么场景之下用到了 ARP 协议, 不在同一子网的情况, 前提条件没有配置中继代理
TCP 握手的时候协商了什么

1. 使用 TCP/UDP 反别发送一个 1M 的包，它们会发生分包吗，在哪里发生的分包（主要考的是 MTU/MSS）





 epoll 



1. 服务器如何将消息发送给接收方的（websocket）



# HTTPS的加密机制

https://blog.csdn.net/chenyixin121738/article/details/115987819?spm=1001.2014.3001.5501





# DNS 过程

https 的建立链接过程 

https 的传输数据是否是对称加密 

tcp 拥塞控制



epoll 和 select



长连接短连接区别，应用场景

HTTP 和 HTTPS 的区别

然后 TIME_WAIT 的状态是什么



HTTP1/ 2/3 区别， 有什么改进 

HTTP session 和 cookie 分别有什么作用 

tcp 如何保证可靠性，close_wait 和 time_wait 区别

 输入一个URL到浏览器发生了什么 



- HTTP 有哪些 method？
- 浏览器会对用户的哪些请求方法做记录？
- 请求的状态码有哪些？



(计算机网络)三次握手说一下
(计算机网络)为什么要随机初始化seq
(计算机网络)除了seq还有什么
(计算机网络)拥塞控制和流量控制的区别
(计算机网络)拥塞控制是怎么实现的，说下快恢复
(计算机网络)说下https
(计算机网络)https第一次交互交换了什么东西
(计算机网络)假如有A.com,B.com，服务器怎么知道返回哪个证书

 （计算机网络）TCP和UDP之间的区别说一下，他们各自适合用在什么场景
（计算机网络）TCP的表头结构了解过哪些字段，squence Number是什么？Acknowledgement Number呢？Advertised-Window呢？TCP Flag有哪些类型呢？SYN呢？FIN呢？具体代表什么含义
（计算机网络）三次握手了解吧，具体的讲一下，说的详细一点，交互的各个字段要说一下
（计算机网络）为什么要三次握手？
（计算机网络）为什么要随机初始化seq？
（计算机网络）SYN Flood攻击有了解吗？
（计算机网络）三次握手的第一个包没收到会发生什么？第二个包没收到会发生什么？第三个包没收到会发生什么？

 

ip地址的分类
特殊ip地址
DNS服务器介绍
http协议状态码：502，404，200
cookie & session

 DNS域名解析
• http拥塞控制
• http版本问题 

 发消息时 网络传输过程中（只说应用层）信息怎么保证不被窃取 

 密钥泄漏怎么办？ 

1. select 和 epoll。（很常规的问题，也算是必考题了）
2. HTTP 和 HTTPS 的区别。



1. MTU/MSS

（计网）如何保障网络传输的数据安全
（计网）AES 的加密性能会高于RSA么
（计网）RSA 的实现原理
（计网）MD5 加密以后能解密么
（计网）网络传输中，你只把数据加密了，就能保证网络的安全么？有没有其他的隐患

  计网）洪泛攻击的规避方法 

 常用的对称加密算法，有什么同？ （字节跳动） 



 