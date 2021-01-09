# 1MySQL 锁与死锁

#### 案例一

需求：将投资的钱拆成几份随机分配给借款人。

起初业务程序思路是这样的：

投资人投资后，将金额随机分为几份，然后随机从借款人表里面选几个，然后通过一条条select for update 去更新借款人表里面的余额等。

例如两个用户同时投资，A用户金额随机分为2份，分给借款人1，2

B用户金额随机分为2份，分给借款人2，1

由于加锁的顺序不一样，死锁当然很快就出现了。

对于这个问题的改进很简单，直接把所有分配到的借款人直接一次锁住就行了。

```
Select * from xxx where id in (xx,xx,xx) for update
```

在in里面的列表值mysql是会自动从小到大排序，加锁也是一条条从小到大加的锁

例如（以下会话id为主键）：

Session1:

```mysql
mysql> select * from t3 where id in (8,9) for update;
+----+--------+------+---------------------+
| id | course | name | ctime               |
+----+--------+------+---------------------+
|  8 | WA     | f    | 2016-03-02 11:36:30 |
|  9 | JX     | f    | 2016-03-01 11:36:30 |
+----+--------+------+---------------------+
rows in set (0.04 sec)
```

Session2:

```
select * from t3 where id in (10,8,5) for update;
```

锁等待中……

其实这个时候id=10这条记录没有被锁住的，但id=5的记录已经被锁住了，锁的等待在id=8的这里 不信请看

Session3:

```
mysql> select * from t3 where id=5 for update;
```

锁等待中

Session4:

```java
mysql> select * from t3 where id=10 for update;
+----+--------+------+---------------------+
| id | course | name | ctime               |
+----+--------+------+---------------------+
| 10 | JB     | g    | 2016-03-10 11:45:05 |
+----+--------+------+---------------------+
row in set (0.00 sec)
```

在其它session中id=5是加不了锁的，但是id=10是可以加上锁的。

#### 案例二

在开发中，经常会做这类的判断需求：根据字段值查询（有索引），如果不存在，则插入；否则更新。

以id为主键为例，目前还没有id=22的行

Session1:

```
select * from t3 where id=22 for update;Empty set (0.00 sec)
```

session2:

```
select * from t3 where id=23  for update;Empty set (0.00 sec)
```

Session1:

```
insert into t3 values(22,'ac','a',now());
```

锁等待中……

Session2:

```
insert into t3 values(23,'bc','b',now());
ERROR 1213 (40001): Deadlock found when trying to get lock; try restarting transaction
```

当对存在的行进行锁的时候(主键)，mysql就只有行锁。

当对未存在的行进行锁的时候(即使条件为主键)，mysql是会锁住一段范围（有gap锁）

锁住的范围为：

(无穷小或小于表中锁住id的最大值，无穷大或大于表中锁住id的最小值)

如：如果表中目前有已有的id为（11 ， 12）

那么就锁住（12，无穷大）

如果表中目前已有的id为（11 ， 30）

那么就锁住（11，30）

对于这种死锁的解决办法是：

```
insert into t3(xx,xx) on duplicate key update `xx`='XX';
```

用mysql特有的语法来解决此问题。因为insert语句对于主键来说，插入的行不管有没有存在，都会只有行锁

#### 案例三

```java
mysql> select * from t3 where id=9 for update;
+----+--------+------+---------------------+
| id | course | name | ctime               |
+----+--------+------+---------------------+
|  9 | JX     | f    | 2016-03-01 11:36:30 |
+----+--------+------+---------------------+
 
row in set (0.00 sec)
```

Session2:

```
mysql> select * from t3 where id<20 for update;
```

锁等待中

Session1:

```
mysql> insert into t3 values(7,'ae','a',now());
ERROR 1213 (40001): Deadlock found when trying to get lock; try restarting transaction
```

这个跟案例一其它是差不多的情况，只是session1不按常理出牌了，

Session2在等待Session1的id=9的锁，session2又持了1到8的锁（注意9到19的范围并没有被session2锁住），最后，session1在插入新行时又得等待session2,故死锁发生了。

这种一般是在业务需求中基本不会出现，因为你锁住了id=9，却又想插入id=7的行，这就有点跳了，当然肯定也有解决的方法，那就是重理业务需求，避免这样的写法。

#### 案例四

![img](https://mmbiz.qpic.cn/sz_mmbiz_jpg/HV4yTI6PjbJYFcVbbmBpMJ9bgTc0yY32gg1J6uMicksXmtmWIAickVQaTb0EY89kkrE3icFWibpCbMQaliaaKWgGTHw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

一般的情况，两个session分别通过一个sql持有一把锁，然后互相访问对方加锁的数据产生死锁。

#### 案例五

![img](https://mmbiz.qpic.cn/sz_mmbiz_jpg/HV4yTI6PjbJYFcVbbmBpMJ9bgTc0yY323ia3yZmMYV4oR6ibm7JbWiaWWfIwHt3nSQwRuw7eOTvGDhLl6HKGp7zEw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

两个单条的sql语句涉及到的加锁数据相同，但是加锁顺序不同，导致了死锁。

#### 案例六(页锁与行锁)

死锁场景如下：

表结构：

```
CREATE TABLE dltask (    id bigint unsigned NOT NULL AUTO_INCREMENT COMMENT ‘auto id’,    a varchar(30) NOT NULL COMMENT ‘uniq.a’,    b varchar(30) NOT NULL COMMENT ‘uniq.b’,    c varchar(30) NOT NULL COMMENT ‘uniq.c’,    x varchar(30) NOT NULL COMMENT ‘data’,       PRIMARY KEY (id),    UNIQUE KEY uniq_a_b_c (a, b, c)) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT=’deadlock test’;
```

a，b，c三列，组合成一个唯一索引，主键索引为id列。

**事务隔离级别：**

RR (Repeatable Read)

**每个事务只有一条SQL：**

```
delete from dltask where a=? and b=? and c=?;
```

SQL的执行计划：

![img](https://mmbiz.qpic.cn/sz_mmbiz_jpg/HV4yTI6PjbJYFcVbbmBpMJ9bgTc0yY329BVwE1bRITxjQlmPA7uSUfp0jkPczCRarkSmYI0ZYkKvvib0A01wibQg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

死锁日志：

![img](https://mmbiz.qpic.cn/sz_mmbiz_jpg/HV4yTI6PjbJYFcVbbmBpMJ9bgTc0yY32HBlkaLX7WCkTQs8Dto4QqmqXzq8VF3wZZcoS6GeykvGp2gvHRUZNqw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

众所周知，InnoDB上删除一条记录，并不是真正意义上的物理删除，而是将记录标识为删除状态。(注：这些标识为删除状态的记录，后续会由后台的Purge操作进行回收，物理删除。但是，删除状态的记录会在索引中存放一段时间。) 在RR隔离级别下，唯一索引上满足查询条件，但是却是删除记录，如何加锁？InnoDB在此处的处理策略与前两种策略均不相同，或者说是前两种策略的组合：对于满足条件的删除记录，InnoDB会在记录上加next key lock X(对记录本身加X锁，同时锁住记录前的GAP，防止新的满足条件的记录插入。) Unique查询，三种情况，对应三种加锁策略，总结如下：

此处，我们看到了next key锁，是否很眼熟？对了，前面死锁中事务1，事务2处于等待状态的锁，均为next key锁。明白了这三个加锁策略，其实构造一定的并发场景，死锁的原因已经呼之欲出。但是，还有一个前提策略需要介绍，那就是InnoDB内部采用的死锁预防策略。

- 找到满足条件的记录，并且记录有效，则对记录加X锁，No Gap锁(lock_mode X locks rec but not gap)；
- 找到满足条件的记录，但是记录无效(标识为删除的记录)**，则对记录加next key锁(同时锁住记录本身**，以及记录之前的Gap：lock_mode X);
- 未找到满足条件的记录，**则对第一个不满足条件的记录加Gap锁，保证没有满足条件的记录插入(**locks gap before rec)；

### 死锁预防策略

InnoDB引擎内部(或者说是所有的数据库内部)，有多种锁类型：事务锁(行锁、表锁)，Mutex(保护内部的共享变量操作)、RWLock(又称之为Latch，保护内部的页面读取与修改)。

InnoDB每个页面为16K，读取一个页面时，需要对页面加S锁，更新一个页面时，需要对页面加上X锁。任何情况下，操作一个页面，都会对页面加锁，页面锁加上之后，页面内存储的索引记录才不会被并发修改。

因此，为了修改一条记录，InnoDB内部如何处理：

- 根据给定的查询条件，找到对应的记录所在页面；
- 对页面加上X锁(RWLock)，然后在页面内寻找满足条件的记录；
- 在持有页面锁的情况下，对满足条件的记录加事务锁(行锁：根据记录是否满足查询条件，记录是否已经被删除，分别对应于上面提到的3种加锁策略之一)；

相对于事务锁，页面锁是一个短期持有的锁，而事务锁(行锁、表锁)是长期持有的锁。因此，为了防止页面锁与事务锁之间产生死锁。InnoDB做了死锁预防的策略：持有事务锁(行锁、表锁)，可以等待获取页面锁；但反之，持有页面锁，不能等待持有事务锁。

根据死锁预防策略，在持有页面锁，加行锁的时候，如果行锁需要等待。则释放页面锁，然后等待行锁。此时，行锁获取没有任何锁保护，因此加上行锁之后，记录可能已经被并发修改。因此，此时要重新加回页面锁，重新判断记录的状态，重新在页面锁的保护下，对记录加锁。如果此时记录未被并发修改，那么第二次加锁能够很快完成，因为已经持有了相同模式的锁。但是，如果记录已经被并发修改，那么，就有可能导致本文前面提到的死锁问题。

以上的InnoDB死锁预防处理逻辑，对应的函数，是row0sel.c::row_search_for_mysql()。感兴趣的朋友，可以跟踪调试下这个函数的处理流程，很复杂，但是集中了InnoDB的精髓。

### 剖析死锁的成因

做了这么多铺垫，有了Delete操作的3种加锁逻辑、InnoDB的死锁预防策略等准备知识之后，再回过头来分析本文最初提到的死锁问题，就会手到拈来，事半而功倍。

首先，假设dltask中只有一条记录：(1, ‘a’, ‘b’, ‘c’, ‘data’)。三个并发事务，同时执行以下的这条SQL：

```
delete from dltask where a=’a’ and b=’b’ and c=’c’;
```

并且产生了以下的并发执行逻辑，就会产生死锁：

![img](https://mmbiz.qpic.cn/sz_mmbiz_jpg/HV4yTI6PjbJYFcVbbmBpMJ9bgTc0yY32TGrTBgiaaxPyQUiafLdVDVB1ZurKj6smP6wzCDKicYIr6rgBsR41Y3uCQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面分析的这个并发流程，完整展现了死锁日志中的死锁产生的原因。其实，根据事务1步骤6，与事务0步骤3/4之间的顺序不同，死锁日志中还有可能产生另外一种情况，那就是事务1等待的锁模式为记录上的X锁 + No Gap锁(lock_mode X locks rec but not gap waiting)。这第二种情况，也是”润洁”同学给出的死锁用例中，使用MySQL 5.6.15版本测试出来的死锁产生的原因。

此类死锁，产生的几个前提：

- **Delete操作，针对的是唯一索引上的等值查询的删除；(范围下的删除，也会产生死锁，但是死锁的场景**，跟本文分析的场景，有所不同)
- **至少有3个(或以上)的并发删除操作**；
- **并发删除操作，有可能删除到同一条记录，并且保证删除的记录一定存在**；
- **事务的隔离级别设置为Repeatable Read，同时未设置innodb_locks_unsafe_for_binlog**参数(此参数默认为FALSE)；(Read Committed隔离级别，由于不会加Gap锁，不会有next key，因此也不会产生死锁)
- 使用的是InnoDB存储引擎；(废话！MyISAM引擎根本就没有行锁)

# 2 死锁检查处理

innodb默认使用了next-gap算法，这种算法结合了index-row锁和gap锁。正因为这样的锁算法，innodb在可重复读这样的默认隔离级别上，可以避免幻象的产生。

innodb_locks_unsafe_for_binlog最主要的作用就是控制innodb是否对gap加锁。

注意该参数如果是**enable的，则是unsafe的，此时gap不会加锁；反之，如果disable掉该参数，则gap会加锁。当然对于一些和数据完整性相关的定义，如外键和唯一索引（含主键）需要对gap进行加锁，那么innodb_locks_unsafe_for_binlog的设置并不会影响gap是否加锁**。

在5.1.15的时候，innodb引入了一个概念叫做“semi-consistent”，这样会在innodb_locks_unsafe_for_binlog的状态为ennable时在一定程度上提高update并发性。



 正常情况下，死锁发生时，权重最小的连接将被kill并回滚。 

解除正在死锁的状态有两种方法：

第一种杀死会话：

1.查询是否锁表

show OPEN TABLES where In_use > 0;

2.查询进程（如果您有SUPER权限，您可以看到所有线程。否则，您只能看到您自己的线程）

show processlist

或者查询出所有需要Killd的Id;

select concat('KILL ',id,';') from information_schema.processlist where user='root';

3.杀死进程id（就是上面命令的id列）

kill id

 

第二种杀死事务：

1.查看下在锁的事务 

SELECT * FROM INFORMATION_SCHEMA.INNODB_TRX;

2.杀死进程id（就是上面命令的trx_mysql_thread_id列）

kill 线程ID

例子：

查出死锁进程：SHOW PROCESSLIST
杀掉进程     KILL 420821;

其它关于查看死锁的命令：

1：查看当前的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_TRX;

2：查看当前锁定的事务

SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCKS;

3：查看当前等锁的事务
SELECT * FROM INFORMATION_SCHEMA.INNODB_LOCK_WAITS;

 

下面是理论知识：

> **会话和事务线程的关系**
>
> 1、会话可以创建多个事务
> 比如：使用客端连接数据库，这样你就可以执行很多个事务了
>
> 2、一个事务只能由一个会话产生
> 在数据库里的事务，如果在执行的SQL都是由会话发起的，哪怕是自动执行的JOB也是由系统会话发起的
>
> 3、一个事务可能会产生一个或多个线程
> 比如RMAN备份，是可以创建多个线程可加快备份速度
>
> 4、一个线程在同一时间内只能执行一个事务
> 而一个线程，在没结束当前事务是无法释放资源来执行第二个事务

> **connection和session的定义和区别：**
>
> 1.连接(connection)是一个物理的概念，它指的是一个通过网络建立的客户端和专有服务器(Dedicated Server）或调度器(Shared Server)的一个网络连接。
> 2.会话(session)是一个逻辑的概念，它是存在于实例中。
>
> 注：创建一个连接(connection)实际上是在某个实例（instance，或者说是进程）中创建一个或多个线程。
>
> **两者关系：**
>
> - 1、一个连接可以拥有多个会话也可以没有会话（实际上，一条连接上的各个会话可以使用不同的用户身份），同一个连接上的不同会话之间不会相互影响。
>
> - 2、 两个会话之间的影响，体现在锁和锁存，即对相同资源的操作（对象定义或数据块）或请求（CPU/内存），它们的处理一般是按队列来处理的，前面的没有处理 好，后面的就要等待。如果以打电话来比喻：connect就好比你接通对方，这时，connect就建立了，有没有通话，不管。双方进行通话，则 session建立了，如果换人，则新的session建立，原session结束，类似的，可以在同一个connect上进行多个会话。最后，挂 机，connect结束。



# 3MySQL中的实例、数据库关系简介

![img](https://images2015.cnblogs.com/blog/801240/201606/801240-20160615163123463-1325681547.jpg)

1、MySQL是单进程多线程（而Oracle等是多进程），也就是说MySQL实例在系 统上表现就是一个服务进程，即进程（通过多种方法可以创建多实例，再安装一个端口号不同的mysql，或者通过workbench来新建一个端口号不同的 服务器实例等），该架构类似于SQL Server和Windows版本的Oracle；
2、MySQL实例是线程和内存组成，实例才是真正用于操作数据库文件的（MySQL数据库是由一些列物理文件组成，类似于frm、MYD、MYI、ibd结尾的文件）；
3、一般情况下一个实例操作一个或多个数据库（Oracle一个实例对应一个数据库）；集群情况下多个实例操作一个或多个数据库。

注：在实例启动的时候MySQL会读取配置文件，类似于Oracle的spfile文件，不同的是Oracle如果找不到参数文件会启动失
    败，MySQL如果找不到配置文件则会按照默认参数设置启动实例。
—————————————————————————————————————————————————————————————————————————————
**mysql中database、instance、session：**

   mysql中建立一个会话，不是和具体的数据库相连接，而是跟某个instance建立会话（每个会话可以使用不同的用户身份）。

   而一个实例可以操作多个数据库，故一个会话（在操作系统概念里，会话即是线程）可以操作一个实例上的多个数据库。

​    简单描述如下：instance >> database 

—————————————————————————————————————————————————————————————————————————————

**connection和session的定义和区别：**

1.连接(connection)是一个物理的概念，它指的是一个通过网络建立的客户端和专有服务器(Dedicated Server）或调度器(Shared Server)的一个网络连接。
2.会话(session)是一个逻辑的概念，它是存在于实例中。

注：创建一个连接(connection)实际上是在某个实例（instance，或者说是进程）中创建一个或多个线程。

**两者关系：**

- 1、一个连接可以拥有多个会话也可以没有会话（实际上，一条连接上的各个会话可以使用不同的用户身份），同一个连接上的不同会话之间不会相互影响。

- 2、 两个会话之间的影响，体现在锁和锁存，即对相同资源的操作（对象定义或数据块）或请求（CPU/内存），它们的处理一般是按队列来处理的，前面的没有处理 好，后面的就要等待。如果以打电话来比喻：connect就好比你接通对方，这时，connect就建立了，有没有通话，不管。双方进行通话，则 session建立了，如果换人，则新的session建立，原session结束，类似的，可以在同一个connect上进行多个会话。最后，挂 机，connect结束。

—————————————————————————————————————————————————————————
1、会话可以创建多个事务
比如：使用客端连接数据库，这样你就可以执行很多个事务了

2、一个事务只能由一个会话产生
在数据库里的事务，如果在执行的SQL都是由会话发起的，哪怕是自动执行的JOB也是由系统会话发起的

3、一个事务可能会产生一个或多个线程
比如RMAN备份，是可以创建多个线程可加快备份速度

4、一个线程在同一时间内只能执行一个事务
而一个线程，在没结束当前事务是无法释放资源来执行第二个事务
—————————————————————————————————————————————————————————**
事务、会话与线程的关系和区别：
**事务：简单理解局势一个业务需求的最小处理单位。
      如：从A银行卡转账500元到B银行卡，事务就包括两部分，1、从A卡减掉500元 2、从B卡加上500元
      这两个部分只要一个部分出错，就要整体“回滚”，那这就是一个事务

会话：可以包含N个事务
      如：你登陆网银之后，可以重复转账步骤2次，第二次转账失败，并不影响你第一次转账成功。

注：一个会话中可以由多个事务。线程是操作系统概念。

# 4 间隙锁/Next-Key Lock

间隙锁在innoDB中的唯一作用就是在一定的“间隙”内防止其他事务的插入操作，以此防止幻读的发生：

- 防止间隙内有新数据被插入。
- 防止已存在的数据，更新成间隙内的数据。



 记录锁和在此索引记录之前的gap上的锁的结合，这个锁的作用是为了防止幻读，当我们具体where条件指定某一个值时，它也会锁住这个值的前后范围。
比如有一个表child，id列上有90,100,102，
当我们执行select * from chlid where id=100 for update 时，mysql会锁住90到102这个区间，一开始有点疑惑就是其实mysql只需要去锁定id=100这个值就可以防止幻读了，为什么还要去锁定相邻的区间范围呢？
这是为了预防另一种情况的发生。
比如当我们执行 **select * from chlid where id>100 for update时，这时next-key锁就派上用场了。**
**索引扫描到了100和102这两个值，但是仅仅锁住这两个值是不够的，因为当我们在另一个会话插入id=101的时候，就有可能产生幻读了。**
**所以mysql必须锁住[100,102)和[102,无穷大）这个范围，才能保证不会出现幻读**。 



innoDB默认的隔离级别是可重复读(Repeatable Read)，并且会以Next-Key Lock的方式对数据行进行加锁。**Next-Key Lock是行锁和间隙锁的组合，当InnoDB扫描索引记录的时候，会首先对索引记录加上行锁（Record Lock），再对索引记录两边的间隙加上间隙锁（Gap Lock）**。加上间隙锁之后，其他事务就不能在这个间隙修改或者插入记录。

当查询的索引含有唯一属性（唯一索引，主键索引）时，**Innodb存储引擎会对next-key lock进行优化，将其降为record lock,即仅锁住索引本身**，而不是范围。

**幻读有2种情况，一个事物之前读的时候，读到一条记录，再读发现记录没有了，被其它事物删了，另外一种是之前读的时候记录不存在，再读发现又有这条记录，其它事物增加了一条记录，上述操作没有特别说明都是在一个事物里面，属于不同的statement。**
这种情况都是要加范围锁来实现，具体到mysql 就是通过加next key 锁，所谓的next key 锁就是行锁➕gap 锁。**行锁就是锁定你访问的行，gap 锁就是锁定你访问的行到数据库里面相对于这一行的下一条记录的区间**。举个例子数据库里面有2  5条记录，你按照升序扫描，扫到2记录的时候，加的gap 锁的范围就是（2，5）



当你执行 select * from t where d=5 for update的时候，就不止是给数据库中已有的6个记录加上了行锁，还同时加了7个间隙锁。这样就确保了无法再插入新的记录。

 

也就是说这时候，在一行行扫描的过程中，不仅将给行加上了行锁，还给行两边的空隙，也加上了间隙锁

- MVCC(Multi-Version Concurrency Control多版本并发控制)
  如果是简单的SELECT * FROM table1 WHERE
  这种语句为什么读不到隔壁事务的提交数据的原因是，InnoDb使用了MVCC机制，为了提高并发，提供了这个非锁定读，即不需要等待访问行上的锁释放，读取行的一个快照即可。
  但是，它也不会阻止隔壁事务去插入新的数据，因为它并未有加锁操作，但当前事务读不到而已（其实想读也可以读到，请看后部分）。
- Next-Key Lock
  如果是带排他锁操作（除了**INSERT/UPDATE/DELETE这种，还包括SELECT FOR UPDATE**等），它们**默认都在操作的记录上加了Next-Key**
  **Lock**。只有使用了这里的操作后才会在相应的记录周围和记录本上加锁，即Record Lock+ Gap Lock，所以会导致冲突的事务阻塞或超时失败。
  PS.想说，隔离级别越高并发度越差，性能越差，虽然默认的是RR，但是如果业务不需要严格的没有「幻读」现象，是可以降低为RC的或修改innodb_locks_unsafe_for_binlog为1。
  注意有的时候会进行优化，并退化为只加Record Lock，不加Gap Lock，如相关字段为主键的时候。



可重复读在解决lost updates的场景上存在分歧，有的关系型数据库如sql server的snapshot isolation，postgre的repeatable read 实现了**自动检测lost updates**，但mysql innodb的repeated read并没有。

所以在mysql的repeated read场景下，业务仍需要指明排他锁，特别是针对一些余额类的业务更改。

**write skew 以及 phantoms（幻读）**

我们用一个实际的例子来说明什么是**write skew**，假设有一个航空公司，用booking表来记录一次成功的预定，那么在两个用户并发的做预定操作时，会发生如下的场景：

![[公式]](https://www.zhihu.com/equation?tex=%5Cbegin%7Barray%7D%7Bc%7Cc%7D+%5Ctext%7Btime%7D+%26+%5Ctext%7Bsession1%7D+%26+%5Ctext%7Bsession2%7D+%26+%5Ctext%7Brest+-+bookedCount%7D+%5C%5C+%5Chline+1+%26+%5Ctext%7Bbegin%7D+%26+%5Ctext%7B%7D+%26+1%5C%5C+2+%26+%5Ctext%7Bselect+count%28%2A%29+from+booking+where+flight_id+%3D+1234%7D+%26+%26+1+%5C%5C+3+%26+%5Ctext%7Bif+%28rest+-+count+%3E+0%29%7D+%26+%5Ctext%7Bbegin%7D+%26+1%5C%5C+4+%26+%5Ctext%7B%7D+%26+%5Ctext%7Bselect+count%28%2A%29+from+booking+where+flight_id+%3D+1234%7D+%261+%5C%5C+5+%26+%5Ctext%7Binsert+booking+record%7D+%26+%5Ctext%7Bif+%28rest+-+count+%3E+0%29%7D+%261%5C%5C+6+%26+%26+%5Ctext%7Binsert+booking+record%7D+%261%5C%5C+7+%26+%5Ctext%7Bcommit%7D+%26+%5Ctext%7Bcommit%7D+%26-1%5C%5C+8+%26+%5Ctext%7B%7D+%26+%5Ctext%7B%7D+%26-1%5C%5C+%5Cend%7Barray%7D%5C%5C)

这个场景既不是脏写，也不是lost update，因为没有更新在中途丢失，但是在结束业务的时候发生了机票超卖。

再使用一个实际例子来说明什么是**phantoms**

![[公式]](https://www.zhihu.com/equation?tex=%5Cbegin%7Barray%7D%7Bc%7Cc%7D+%5Ctext%7Btime%7D+%26+%5Ctext%7Bsession1%7D+%26+%5Ctext%7Bsession2%7D+%5C%5C+%5Chline+1+%26+%5Ctext%7Bbegin%7D+%26+%5Ctext%7Bbegin%7D+%5C%5C+2+%26+%5Ctext%7BSELECT+%2A+FROM+users+WHERE+age+BETWEEN+10+AND+30%3B%7D+%26+%5C%5C+3+%26+%26+%5Ctext%7BINSERT+INTO+users%28id%2Cname%2Cage%29+VALUES+%28+3%2C+%27Bob%27%2C+27+%29%3B+COMMIT%3B+%7D+%5C%5C+2+%26+%5Ctext%7BSELECT+%2A+FROM+users+WHERE+age+BETWEEN+10+AND+30%3B%7D+%26+%5C%5C+%5Cend%7Barray%7D%5C%5C)

repeated read在这种场景下无法保证session读取到的值在事务中间不变，这就是phantoms。

phantom 和 write skew的解决方法也类似于lost update，用户可以使用显示的锁行，或**materializing conflict**（物化冲突）的思路，创建**一个专门用来发现冲突的表记录，比如在航空公司的例子中，用户可以创建一个 bookingrest 表，专门用来记录某个航班的余量，把单行插入的业务变更为插入一条booking记录并更新bookingrest的两步操作**，在repeated read隔离级别中，递交booking_rest时，冲突事务会因为冲突而被自动回滚。

如果业务影响太大，则可以使用另一种办法，也就是提升到serialize 隔离级别来避免phantom和write skew









MySQL（innodb）的 RR 隔离级别实际上是 snapshot isolation，可以避免通常意义的幻读。

snapshot isolation 的问题是无法处理如下的 read-write conflict：

![img](https://pic1.zhimg.com/50/v2-210b5ce486fd5a85a567d25428f2c24f_hd.jpg?source=1940ef5c)![img](https://pic1.zhimg.com/80/v2-210b5ce486fd5a85a567d25428f2c24f_720w.jpg?source=1940ef5c)来源： 本来数据库中的 x + y <= 100 的（假如事务的修改都要保持 x+y<=100），于是 t1 先读到 x = 30 ,然后write,将y改为60 ；而t2 在此时读到y=10,于是将x改为50 ,于是 最后 x + y > 100 ，之前 x + y <= 100 破坏了，这种关系被破坏的原因，主要的操作都是先读再更新的(read-write)，update的操作也是 read-update ,那么MySQL 如何解决的呢？ 

由于 UPDATE 本身也是一种 read-write，如果执行 UPDATE 也会有 write skew 问题, 那对实际应用来说就太糟糕了。

MySQL（innodb）为了解决这个问题，强行把 read 分成了 *snapshot read*（快照读）和 *locking read* （当前读）。在 **UPDATE 或者 SELECT ... FOR UPDATE** 的时候，innodb 引擎实际执行的是当前读，在扫描过程中加上行锁和区间锁（*gap locks*，*next-key locks*），相当于变相提升到了 serializable 隔离级别，从而消除了 write skew 。

从实用角度看，这个解法还是很赞的。既解决了 UPDATE write-skew 问题，又保证了绝大多数场景 SELECT 的性能，特殊情况还可以用 SELECT ... FOR UPDATE，完美。

但是，，，，MySQL（innodb）当前读的机制本身和 snapshot 是矛盾的。加锁保护的一定是数据最新版本。例如，如果在快照读之后再执行一次当前读，则读到的数据内容不一定能保证一致，因此会有这样的现象：

```sql
mysql> SELECT * FROM char_encode WHERE glyph = 'a';
+-------+-----------+
| glyph | codepoint |
+-------+-----------+
| a     |        97 |
+-------+-----------+
1 row in set (0.03 sec)

mysql> UPDATE char_encode SET codepoint = codepoint + 1 WHERE glyph //触发当前读
    -> = 'a';
Query OK, 1 row affected (0.07 sec)
Rows matched: 1  Changed: 1  Warnings: 0

mysql> SELECT * FROM char_encode WHERE glyph = 'a';
+-------+-----------+
| glyph | codepoint |
+-------+-----------+
| a     |       101 |
+-------+-----------+
```

**这个是不可重复读**

MySQL（innodb）的选择是允许在快照读之后执行当前读，并且更新 snapshot 镜像的版本。严格来说，这个结果违反了 repeatable read 隔离级别，，但是 who cares 呢，毕竟官方都说了：“*This is* ***not a bug\*** *but an intended and documented behavior.*”



# 5 具体锁哪一行?

 https://blog.csdn.net/qq_33300570/article/details/108623075 

gap锁只能锁一段，遇见具体值，就变成一段一段的。

原则 1： 为便于理解，加锁的基本单位认为是 next-key lock。**next-key lock 是前开后闭区间**。

原则 2： 查找过程中访问到的对象才会加锁。

优化 1： 索引上的等值查询，给唯一索引加锁的时候，next-key lock 退化为行锁。

优化 2： 索引上的等值查询，**向右遍历**时且**最后一个值不满足等值条件的时候，next-key lock 退化为间隙锁**。

一个 bug：唯一索引上的范围查询会访问到不满足条件的第一个值为止。



**1 对于等值查询**

--------针对唯一索引

----------------如果命中数据行，则只有行锁。

----------------如果不命中数据行，则锁住对应的间隙。

--------针对非唯一索引

----------------如果命中数据行，则锁住数据行前后的两个间隙。

----------------如果不命中数据行，则锁住对应的间隙。

2 **对于范围查询**

--------针对唯一索引

----------------针对左边的区间

------------------------如果命中数据行，则锁左边的范围跟数据行一致。

------------------------如果不命中数据行，则锁左边的范围包含数据行所在的间隙。

----------------针对右边的区间

------------------------如果命中数据行

--------------------------------如果是开区间，则锁右边的范围包含对应数据行。（bug？）

--------------------------------如果是闭区间，则锁右边的范围包含到对应数据行的下一条记录。（bug？）

------------------------如果不命中数据行，不管是开区间还是闭区间，锁右边的范围都包含到相关间隙的下一数据行。（bug？）

--------针对非唯一索引

----------------针对左边的区间

------------------------如果命中数据行

--------------------------------如果是闭区间，则锁左边范围还需要包含上一个间隙。

--------------------------------如果是开区间，则锁左边范围与左边区间一致。

------------------------如果不命中数据行，不管是开区间还是闭区间，则锁左边范围需要包含数据行所在的间隙。

----------------针对右边的区间

------------------------如果命中数据行

--------------------------------如果是开区间，则锁右边的范围包含对应数据行。（bug？）

--------------------------------如果是闭区间，则锁右边的范围包含到对应数据行的下一条记录。（bug？）

------------------------如果不命中数据行，不管是开区间还是闭区间，锁右边的范围都包含到相关间隙的下一数据行。（bug？）

**3 无索引**

 **InnoDB的行锁是针对索引加的锁，不是针对记录加的锁 ,并且该索引不能失效，否则会从行锁升级为表锁 。**（） 



综上所述，大部分均能理解，但是对于范围查询的右边区域，不知道是否mysql bug或者本人理解不足，发现总是锁多了部分内容，比如：

-------如果不命中数据，不管开闭区间，不仅有间隙锁，连同下一个数据行也有行锁。

-------如果命中数据，如果是开区间，则该数据行也有行锁。

-------如果命中数据，如果是闭区间，则连同该数据行下一个间隙也有间隙锁，同时对应的数据行下一行也有行锁。



# 6 MySQL/InnoDB的加锁分析

> MVVC ——> 读操作（快照读，当前读）——> 涉及GAP锁（间隙锁）

 

### **1.1  MVCC：Snapshot Read vs Current Read**

 

在一个支持MVCC并发控制的系统中，哪些读操作是快照读？哪些操作又是当前读呢？以MySQL InnoDB为例：



**在MVCC并发控制中，读操作可以分成两类：快照读 (snapshot read)与当前读 (current read)。**快照读，读取的是记录的可见版本 (有可能是历史版本)，不用加锁。当前读，读取的是记录的最新版本，并且，当前读返回的记录，都会加上锁，保证其他事务不会再并发修改这条记录。

- **快照读：**简单的select操作，属于快照读，**不加锁。(当然，也有例外，下面会分析)**

  - select * from table where ?;

   

- **当前读：**特殊的读操作，插入/更新/删除操作，属于当前读，需要加锁。

  - select * from table where ? lock in share mode;
  - select * from table where ? for update;
  - insert into table values (…);
  - update table set ? where ?;
  - delete from table where ?;

  所有以上的语句，都属于当前读，读取记录的最新版本。并且，读取之后，还需要保证其他并发事务不能修改当前记录，对读取记录加锁。其中，除了第一条语句，**对读取记录加S锁 (共享锁)外，其他的操作，都加的是X锁 (排它锁)。**

   

为什么将 插入/更新/删除 操作，都归为当前读？可以看看下面这个 更新 操作，在数据库中的执行流程：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430150826970.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

 

从图中，可以看到，一个Update操作的具体流程。当 Update SQL被发给MySQL后，MySQL Server会根据where条件，读取第一条满足条件的记录，然后InnoDB引擎会将第一条记录返回，并加锁 (current read)。待MySQL Server收到这条加锁的记录之后，会再发起一个Update请求，更新这条记录。一条记录操作完成，再读取下一条记录，直至没有满足条件的记录为止。因此，Update操作内部，就包含了一个当前读。同理，Delete操作也一样。**Insert操作会稍微有些不同，简单来说，就是Insert操作可能会触发Unique Key的冲突检查，也会进行一个当前读**。

 

**注**：根据上图的交互，针对一条当前读的SQL语句，InnoDB与MySQL Server的交互，是一条一条进行的，因此，加锁也是一条一条进行的。先对一条满足条件的记录加锁，返回给MySQL Server，做一些DML操作；然后在读取下一条加锁，直至读取完毕。

### **1.2 Cluster Index：聚簇索引**

InnoDB存储引擎的数据组织方式，是聚簇索引表：完整的记录，存储在主键索引中，通过主键索引，就可以获取记录所有的列。关于聚簇索引表的组织方式，可以参考MySQL的官方文档：[Clustered and Secondary Indexes](http://dev.mysql.com/doc/refman/5.0/en/innodb-index-types.html) 。本文假设读者对这个，已经有了一定的认识，就不再做具体的介绍。接下来的部分，主键索引/聚簇索引 两个名称，会有一些混用，望读者知晓。

### **1.3  2PL：Two-Phase Locking**

传统RDBMS加锁的一个原则，来简单看看2PL在MySQL中的实现。

![img](https://img-blog.csdnimg.cn/20200430151408644.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

 

从上图可以看出，2PL就是将加锁/解锁分为两个完全不相交的阶段。加锁阶段：只加锁，不放锁。解锁阶段：只放锁，不加锁。

 

# 二、一条简单SQL的加锁实现分析

在介绍完一些背景知识之后，本文接下来将选择几个有代表性的例子，来详细分析MySQL的加锁处理。当然，还是从最简单的例子说起。经常有朋友发给我一个SQL，然后问我，这个SQL加什么锁？就如同下面两条简单的SQL，他们加什么锁？

在不同的隔离级别下，所使用的锁的算法如下：

**RC：仅有record 锁**

**RR：有record和next_key锁**

 

- **SQL1：**select * from t1 where id = 10;
- **SQL2：**delete from t1 where id = 10;

 

针对这个问题，该怎么回答？我能想象到的一个答案是：

 

- **SQL1：**不加锁。因为MySQL是使用多版本并发控制的，读不加锁。
- **SQL2：**对id = 10的记录加写锁 (走主键索引)。

 

这个答案对吗？说不上来。即可能是正确的，也有可能是错误的，已知条件不足，这个问题没有答案。如果让我来回答这个问题，我必须还要知道以下的一些前提，前提不同，我能给出的答案也就不同。要回答这个问题，还缺少哪些前提条件？

 

- **前提一：**id列是不是主键？

- **前提二：**当前系统的隔离级别是什么？

- **前提三：**id列如果不是主键，那么id列上有索引吗？

- **前提四：**id列上如果有二级索引，那么这个索引是唯一索引吗？

- **前提五：**两个SQL的执行计划是什么？索引扫描？全表扫描？

没有这些前提，直接就给定一条SQL，然后问这个SQL会加什么锁，都是很业余的表现。而当这些问题有了明确的答案之后，给定的SQL会加什么锁，也就一目了然。下面，我将这些问题的答案进行组合，然后按照从易到难的顺序，逐个分析每种组合下，对应的SQL会加哪些锁？

**注：**下面的这些组合，我做了一个前提假设，也就是有索引时，执行计划一定会选择使用索引进行过滤 (索引扫描)。但实际情况会复杂很多，真正的执行计划，还是需要根据MySQL输出的为准。

- **组合一：**id列是主键，RC隔离级别
- **组合二：**id列是二级唯一索引，RC隔离级别
- **组合三：**id列是二级非唯一索引，RC隔离级别
- **组合四：**id列上没有索引，RC隔离级别
- **组合五：**id列是主键，RR隔离级别
- **组合六：**id列是二级唯一索引，RR隔离级别
- **组合七：**id列是二级非唯一索引，RR隔离级别
- **组合八：**id列上没有索引，RR隔离级别
- **组合九：**Serializable隔离级别

排列组合还没有列举完全，但是看起来，已经很多了。真的有必要这么复杂吗？事实上，要分析加锁，就是需要这么复杂。但是从另一个角度来说，只要你选定了一种组合，SQL需要加哪些锁，其实也就确定了。接下来，就让我们来逐个分析这9种组合下的SQL加锁策略。

注：在前面八种组合下，也就是RC，RR隔离级别下，SQL1：select操作均不加锁，采用的是快照读，因此在下面的讨论中就忽略了，主要讨论SQL2：delete操作的加锁。

### 2.1 组合一：id主键+RC

这个组合，是最简单，最容易分析的组合。id是主键，Read Committed隔离级别，给定SQL：delete from t1 where id = 10; 只需要将主键上，id = 10的记录加上X锁即可。如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430152323341.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

**结论：**id是主键时，此SQL只需要在id=10这条记录上加X锁即可。

### **2.2 组合二：id唯一索引+RC**

这个组合，id不是主键，而是一个Unique的二级索引键值。那么在RC隔离级别下，delete from t1 where id = 10; 需要加什么锁呢？见下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/2020043015250641.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

此组合中，id是unique索引，而主键是name列。此时，加锁的情况由于组合一有所不同。由于id是unique索引，因此delete语句会选择走id列的索引进行where条件的过滤，在找到id=10的记录后，首先会将unique索引上的id=10索引记录加上X锁，同时，会根据读取到的name列，回主键索引(聚簇索引)，然后将聚簇索引上的name = ‘d’ 对应的主键索引项加X锁。为什么聚簇索引上的记录也要加锁？试想一下，如果并发的一个SQL，是通过主键索引来更新：update t1 set id = 100 where name = ‘d’; 此时，如果delete语句没有将主键索引上的记录加锁，那么并发的update就会感知不到delete语句的存在，违背了同一记录上的更新/删除需要串行执行的约束。

**结论**：若id列是unique列，其上有unique索引。**那么SQL需要加两个X锁**，一个对应于id unique索引上的id = 10的记录，另一把锁对应于聚簇索引上的[name='d',id=10]的记录。

### 2.3  组合三：id非唯一索引+RC

相对于组合一、二，组合三又发生了变化，隔离级别仍旧是RC不变，但是id列上的约束又降低了，id列不再唯一，只有一个普通的索引。假设delete from t1 where id = 10; 语句，仍旧选择id列上的索引进行过滤where条件，那么此时会持有哪些锁？同样见下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430152616109.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

根据此图，可以看到，首先，id列索引上，满足id = 10查询条件的记录，均已加锁。同时，这些记录对应的主键索引上的记录也都加上了锁。与组合二唯一的区别在于，组合二最多只有一个满足等值查询的记录，而组合三会将所有满足查询条件的记录都加锁。

**结论**：若id列上有非唯一索引，那么对应的所有满足SQL查询条件的记录，都会被加锁。同时，这些记录在主键索引上的记录，也会被加锁。

### 2.4 组合四：id无索引+RC

相对于前面三个组合，这是一个比较特殊的情况。id列上没有索引，where id = 10;这个过滤条件，没法通过索引进行过滤，那么只能走全表扫描做过滤。对应于这个组合，SQL会加什么锁？或者是换句话说，全表扫描时，会加什么锁？这个答案也有很多：有人说会在表上加X锁；有人说会将聚簇索引上，选择出来的id = 10;的记录加上X锁。那么实际情况呢？请看下图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430152716185.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

由于id列上没有索引，因此只能走聚簇索引，进行全部扫描。从图中可以看到，满足删除条件的记录有两条，但是，**聚簇索引上所有的记录，都被加上了X锁。无论记录是否满足条件，全部被加上X锁**。既不是加表锁，也不是在满足条件的记录上加行锁。

有人可能会问？为什么不是只在满足条件的记录上加锁呢？这是由于MySQL的实现决定的。如果一个条件无法通过索引快速过滤，那么存储引擎层面就会将所有记录加锁后返回，然后由MySQL Server层进行过滤。因此也就把所有的记录，都锁上了。

注：在实际的实现中，MySQL有一些改进，在MySQL Server过滤条件，发现不满足后，会调用unlock_row方法，把不满足条件的记录放锁 (违背了2PL的约束)。这样做，保证了最后只会持有满足条件记录上的锁，但是每条记录的加锁操作还是不能省略的。

**结论：**若id列上没有索引，SQL会走聚簇索引的全扫描进行过滤，由于过滤是由MySQL Server层面进行的。因此每条记录，无论是否满足条件，都会被加上X锁。但是，为了效率考量，MySQL做了优化，对于不满足条件的记录，会在判断后放锁，最终持有的，是满足条件的记录上的锁，但是不满足条件的记录上的加锁/放锁动作不会省略。同时，优化也违背了2PL的约束。

### 2.5 组合五：id主键+RR

上面的四个组合，都是在Read Committed隔离级别下的加锁行为，接下来的四个组合，是在Repeatable Read隔离级别下的加锁行为。

组合五，id列是主键列，Repeatable Read隔离级别，针对delete from t1 where id = 10; 这条SQL，命中行加一行，不命中加gap。

### 2.6  组合六：id唯一索引+RR

与组合五类似，组合六的加锁，与组合二：[[id唯一索引，Read Committed](https://blog.csdn.net/destinm/article/details/105861606)]一致。两个X锁，id唯一索引满足条件的记录上一个，对应的聚簇索引上的记录一个。二级索引加锁，命中行加一行，不命中加gap

### 2.7  组合七：id非唯一索引+RR

还记得前面提到的MySQL的四种隔离级别的区别吗？RC隔离级别允许幻读，而RR隔离级别，不允许存在幻读。但是在组合五、组合六中，加锁行为又是与RC下的加锁行为完全一致。那么RR隔离级别下，如何防止幻读呢？执行delete from t1 where id = 10; 假设选择id列上的索引进行条件过滤，最后的加锁行为，是怎么样的呢？同样看下面这幅图：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430153141168.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

此图，相对于组合三：[[id列上非唯一锁，Read Committed](https://blog.csdn.net/destinm/article/details/105861606)]看似相同，其实却有很大的区别。最大的区别在于，这幅图中多了一个GAP锁，而且GAP锁看起来也不是加在记录上的，倒像是加载两条记录之间的位置，GAP锁有何用？

其实这个多出来的GAP锁，就是RR隔离级别，相对于RC隔离级别，不会出现幻读的关键。确实，**GAP锁锁住的位置，也不是记录本身，而是两条记录之间的GAP**。所谓幻读，就是同一个事务，连续做两次当前读 (例如：select * from t1 where id = 10 for update;)，那么这两次当前读返回的是完全相同的记录 (记录数量一致，记录本身也一致)，第二次的当前读，不会比第一次返回更多的记录 (幻象)。

如何保证两次当前读返回一致的记录，那就需要在第一次当前读与第二次当前读之间，其他的事务不会插入新的满足条件的记录并提交。为了实现这个功能，GAP锁应运而生。

如图中所示，有哪些位置可以插入新的满足条件的项 (id = 10)，考虑到B+树索引的有序性，满足条件的项一定是连续存放的。记录[6,c]之前，不会插入id=10的记录；[6,c]与[10,b]间可以插入[10, aa]；[10,b]与[10,d]间，可以插入新的[10,bb],[10,c]等；[10,d]与[11,f]间可以插入满足条件的[10,e],[10,z]等；而[11,f]之后也不会插入满足条件的记录。因此，为了保证[6,c]与[10,b]间，[10,b]与[10,d]间，[10,d]与[11,f]不会插入新的满足条件的记录，**MySQL选择了用GAP锁，将这三个GAP给锁起来**。

Insert操作，如insert [10,aa]，首先会定位到[6,c]与[10,b]间，然后在插入前，会检查这个GAP是否已经被锁上，如果被锁上，则Insert不能插入记录。因此，通过第一遍的当前读，不仅将满足条件的记录锁上 (X锁)，与组合三类似。同时还是增加3把GAP锁，将可能插入满足条件记录的3个GAP给锁上，保证后续的Insert不能插入新的id=10的记录，也就杜绝了同一事务的第二次当前读，出现幻象的情况。

有心的朋友看到这儿，可以会问：既然防止幻读，需要靠GAP锁的保护，为什么组合五、组合六，也是RR隔离级别，却不需要加GAP锁呢？

首先，这是一个好问题。其次，回答这个问题，也很简单。**GAP锁的目的，是为了防止同一事务的两次当前读，出现幻读的情况**。而组合五，id是主键；组合六，id是unique键，都能够保证唯一性。一个等值查询，最多只能返回一条记录，而且新的相同取值的记录，一定不会在新插入进来，因此也就避免了GAP锁的使用。其实，针对此问题，还有一个更深入的问题：如果组合五、组合六下，针对SQL：select * from t1 where id = 10 for update; 第一次查询，没有找到满足查询条件的记录，那么GAP锁是否还能够省略？此问题留给大家思考。

**结论：**Repeatable Read隔离级别下，id列上有一个非唯一索引，对应SQL：delete from t1 where id = 10; 首先，通过id索引定位到第一条满足查询条件的记录，加记录上的X锁，加GAP上的GAP锁，然后加主键聚簇索引上的记录X锁，然后返回；然后读取下一条，重复进行。直至进行到第一条不满足条件的记录[11,f]，此时，不需要加记录X锁，但是仍旧需要加GAP锁，最后返回结束。

### 2.8  组合八：id无索引+RR

组合八，Repeatable Read隔离级别下的最后一种情况，id列上没有索引。此时SQL：delete from t1 where id = 10; 没有其他的路径可以选择，只能进行全表扫描。最终的加锁情况，如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430153427914.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

如图，**这是一个很恐怖的现象。首先，聚簇索引上的所有记录，都被加上了X锁。其次，聚簇索引每条记录间的间隙(GAP)，也同时被加上了GAP锁**。这个示例表，只有6条记录，一共需要6个记录锁，7个GAP锁。试想，如果表上有1000万条记录呢？

 

在这种情况下，这个表上，除了不加锁的快照度，其他任何加锁的并发SQL，均不能执行，不能更新，不能删除，不能插入，全表被锁死。

 

当然，跟组合四：[[id无索引, Read Committed](https://blog.csdn.net/destinm/article/details/105861606)]类似，这个情况下，MySQL也做了一些优化，就是所谓的semi-consistent read。semi-consistent read开启的情况下，对于不满足查询条件的记录，MySQL会提前放锁。针对上面的这个用例，就是除了记录[d,10]，[g,10]之外，所有的记录锁都会被释放，同时不加GAP锁。semi-consistent read如何触发：要么是read committed隔离级别；要么是Repeatable Read隔离级别，同时设置了[innodb_locks_unsafe_for_binlog](http://dev.mysql.com/doc/refman/5.5/en/innodb-parameters.html) 参数。更详细的关于semi-consistent read的介绍，可参考之前的一篇博客：[MySQL+InnoDB semi-consitent read原理及实现分析](http://hedengcheng.com/?p=220) 。

 

**结论：**在Repeatable Read隔离级别下，如果进行全表扫描的当前读，那么会锁上表中的所有记录，同时会锁上聚簇索引内的所有GAP，杜绝所有的并发 更新/删除/插入 操作。当然，也可以通过触发semi-consistent read，来缓解加锁开销与并发影响，但是semi-consistent read本身也会带来其他问题，不建议使用。

### 2.9  组合九：Serializable

针对前面提到的简单的SQL，最后一个情况：Serializable隔离级别。对于SQL2：delete from t1 where id = 10; 来说，Serializable隔离级别与Repeatable Read隔离级别完全一致，因此不做介绍。

 

Serializable隔离级别，影响的是SQL1：select * from t1 where id = 10; 这条SQL，在RC，RR隔离级别下，都是快照读，不加锁。但是在Serializable隔离级别，SQL1会加读锁，也就是说快照读不复存在，MVCC并发控制降级为Lock-Based CC。

 

**结论：**在MySQL/InnoDB中，所谓的读不加锁，并不适用于所有的情况，而是隔离级别相关的。Serializable隔离级别，读不加锁就不再成立，所有的读操作，都是当前读。

# 三、一条复杂的SQL

写到这里，其实MySQL的加锁实现也已经介绍的八八九九。只要将本文上面的分析思路，大部分的SQL，都能分析出其会加哪些锁。而这里，再来看一个稍微复杂点的SQL，用于说明MySQL加锁的另外一个逻辑。SQL用例如下：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430153713109.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

如图中的SQL，会加什么锁？假定在Repeatable Read隔离级别下 (Read Committed隔离级别下的加锁情况，留给读者分析。)，同时，假设SQL走的是idx_t1_pu索引。

在详细分析这条SQL的加锁情况前，还需要有一个知识储备，那就是一个SQL中的where条件如何拆分？具体的介绍，建议阅读我之前的一篇文章：[SQL中的where条件，在数据库中提取与应用浅析](http://hedengcheng.com/?p=577) 。在这里，我直接给出分析后的结果：

 

- **Index key：**pubtime > 1 and puptime < 20。此条件，用于确定SQL在idx_t1_pu索引上的查询范围。
- **Index Filter：**userid = ‘hdc’ 。此条件，可以在idx_t1_pu索引上进行过滤，但不属于Index Key。

- **Table Filter：**comment is not NULL。此条件，在idx_t1_pu索引上无法过滤，只能在聚簇索引上过滤。
-  

在分析出SQL where条件的构成之后，再来看看这条SQL的加锁情况 (RR隔离级别)，如下图所示：

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200430153857442.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2Rlc3Rpbm0=,size_16,color_FFFFFF,t_70)

从图中可以看出，在Repeatable Read隔离级别下，由Index Key所确定的范围，被加上了GAP锁；Index Filter锁给定的条件 (userid = ‘hdc’)何时过滤，视MySQL的版本而定，在MySQL 5.6版本之前，不支持[Index Condition Pushdown](http://dev.mysql.com/doc/refman/5.6/en/index-condition-pushdown-optimization.html)(ICP)，因此Index Filter在MySQL Server层过滤，在5.6后支持了Index Condition Pushdown，则在index上过滤。若不支持ICP，不满足Index Filter的记录，也需要加上记录X锁，若支持ICP，则不满足Index Filter的记录，无需加记录X锁 (图中，用红色箭头标出的X锁，是否要加，视是否支持ICP而定)；而Table Filter对应的过滤条件，则在聚簇索引中读取后，在MySQL Server层面过滤，因此聚簇索引上也需要X锁。最后，选取出了一条满足条件的记录[8,hdc,d,5,good]，但是加锁的数量，要远远大于满足条件的记录数量。

**结论：**在Repeatable Read隔离级别下，针对一个复杂的SQL，首先需要提取其where条件。Index Key确定的范围，需要加上GAP锁；Index Filter过滤条件，视MySQL版本是否支持ICP，若支持ICP，则不满足Index Filter的记录，不加X锁，否则需要X锁；Table Filter过滤条件，无论是否满足，都需要加X锁。





**间隙锁定的表现是：间隙允许update不允许insert**

| 类目                                            | 常见句式                                                     | 结论                                                         |
| ----------------------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 主键\|唯一索引                                  | update t1 set col2='OK' where id=1001; update t1 set col2='OK' where col1=9800; | 1.若对应的id值存在，锁定primary key& unique key对应的record； 若DML最终涉及了primary key& unique key对应的record，还是会被堵塞； DML走的是全表扫描，会被堵塞； 2.若对应的id值不存在，对primary key&unique key对应的record允许update不允许insert； |
| update t1 set col2='OK' where id<1001;          | 锁住where范围内的primary key&unique key对应的record          |                                                              |
| 普通索引                                        | update t1 set col2='OK' where col2='first1234';              | 1.若对应的col2 record存在，对（col2_last_value,col2_value)及（col2_value,col2_next_value)产生间隙锁定；并对col2_value产生record锁定； 2.若对应的col2 record不存在，对(col2_last_value,col2_next_value)产生间隙锁定； |
| update t1 set col2='OK' where col2>'first1234'; | 对(负无穷大，col2_value)产生间隙锁定，并对范围内的行的主键进行record锁定； |                                                              |
| 无索引                                          | update t1 set col2='ERROR' where col3<'2017-09-30 10:00:00'; | 锁全表；                                                     |
| INSERT                                          | insert into t1 values(650,650,'six650','2017-09-30 15:10:00'); insert into t1 values(),(),(),(),(); | 锁住primary key& unique key对应的record                      |
| insert into t1 select * from t1_1;              | 1.事务期间，锁定操作主键，唯一索引对应的record、间隙及以外的部分内容； 2.事务期间，锁定操作主键，唯一索引对应的间隙（间隙锁定） |                                                              |

# 7 实验

```mysql
DROP TABLE  IF EXISTS tb_emp;
CREATE TABLE tb_emp (
emp_id INT(11) NOT NULL AUTO_INCREMENT,
emp_name varchar(255) NOT NULL,
gender CHAR(1),
d_id varchar(11),
primary key (emp_id),
key (d_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

show global variables like '%isolation%';


查看设置结果：SELECT @@tx_isolation;
set global transaction_isolation ='read-committed';

SET AUTOCOMMIT=0 禁止自动提交
SET AUTOCOMMIT=1 开启自动提交
START TRANSACTION;
	
		commit;


insert into tb_emp values(7,'zz','m','7');

select * from tb_emp where emp_id =4 for update;
```

## 1等值查询  锁唯一主键存在

表内容

```mysql
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
+--------+----------+--------+------+
section1
mysql> select * from tb_emp where emp_id =3 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      3 | xin      | s      | 3    |
+--------+----------+--------+------+
1 row in set (0.00 sec)

section2
mysql> select * from tb_emp where emp_id =3 for update;
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
mysql>

```

结论 只锁了那条记录



## 2 等值查询  锁唯一主键不存在

```mysql
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
+--------+----------+--------+------+

session1
mysql> select * from tb_emp where emp_id =4 for update;
Empty set (0.00 sec)

session2
mysql> insert into tb_emp values(5,'zz','m','5');
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
mysql>

session2
mysql> insert into tb_emp values(8,'zz','m','8');
Query OK, 1 row affected (0.00 sec)
//插入3可以获得锁
mysql> insert into tb_emp values(3,'zz','m','3');
ERROR 1062 (23000): Duplicate entry '3' for key 'PRIMARY'
//插入7可以获得锁
mysql> insert into tb_emp values(7,'zz','m','7');
ERROR 1062 (23000): Duplicate entry '7' for key 'PRIMARY'
mysql>

```

**结论：1  5不可以插入，但是3，7，8 可以，锁住了 （3，7）gap锁， 开区间。**

```mysql
mysql> select * from tb_emp where emp_id =6 for update;
Empty set (0.00 sec)

mysql> select * from tb_emp where emp_id =7 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      7 | zz       | m      | 7    |
+--------+----------+--------+------+
1 row in set (0.00 sec)

mysql> select * from tb_emp where emp_id =6 for update;
Empty set (0.00 sec)

mysql> select * from tb_emp where emp_id =4 for update;
Empty set (0.00 sec)

mysql> select * from tb_emp where emp_id >1 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      8 | zz       | m      | 8    |
+--------+----------+--------+------+
4 rows in set (0.00 sec)
```

​            **2 for update 使用gap锁时 不会阻塞update。**

## 3 大于查询  锁唯一主键左开区间

```mysql
表内容
mysql> select * from tb_emp;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
+--------+----------+--------+------+

session1
mysql> select * from tb_emp where emp_id >3 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      7 | zz       | m      | 7    |
|      8 | zz       | m      | 8    |
+--------+----------+--------+------+
2 rows in set (0.00 sec)

session2
mysql> insert into tb_emp values(3,'zz','m','3');
ERROR 1062 (23000): Duplicate entry '3' for key 'PRIMARY'
START TRANSACTION;
mysql> insert into tb_emp values(5,'zz','m','5');

没有锁住3，锁住了5。

```

**结论： 锁住（3，无穷大】；开区间的查询，**

```mysql
mysql> select * from tb_emp;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
|     14 | zz       | m      | 11   |
+--------+----------+--------+------+
10 rows in set (0.00 sec)




seesion1
mysql> select * from tb_emp where emp_id >7 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
|     14 | zz       | m      | 11   |
+--------+----------+--------+------+
6 rows in set (0.00 sec)

seesion2
mysql> select * from tb_emp where emp_id >7 for update;;
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
ERROR:
No query specified

seesion1
mysql> select * from tb_emp where emp_id >1 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
|     14 | zz       | m      | 11   |
+--------+----------+--------+------+
9 rows in set (0.00 sec)

seesion2

mysql> select * from tb_emp where emp_id >1 for update;
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
mysql>


```

**结论： 锁住（7，无穷大】; **(1,无穷大)被锁住 











## 4 大于等于查询  锁唯一主键闭开区间

```java
表内容
mysql> select * from tb_emp;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      8 | zz       | m      | 8    |
+--------+----------+--------+------+
5 rows in set (0.00 sec)

session1
mysql> select * from tb_emp where emp_id >=3 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      3 | xin      | s      | 3    |
|      6 | zz       | m      | 6    |
|      7 | zz       | m      | 7    |
|      8 | zz       | m      | 8    |
+--------+----------+--------+------+
4 rows in set (39.53 sec)

session2
mysql> select * from tb_emp where emp_id =3 for update;
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction
mysql>
mysql> commit;
Query OK, 0 rows affected (0.00 sec)

mysql> START TRANSACTION;
Query OK, 0 rows affected (0.00 sec)

mysql> insert into tb_emp values(2,'zz','m','2');
ERROR 1062 (23000): Duplicate entry '2' for key 'PRIMARY'
mysql>

没有锁住3，没有锁住2。
```

结论： 锁住【3，无穷大）；



# 5 emp_id >=2 and emp_id<7

```mysql
mysql> select * from tb_emp ;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
|     14 | zz       | m      | 11   |
+--------+----------+--------+------+
10 rows in set (0.00 sec)

mysql> select * from tb_emp where emp_id >=2 and emp_id<7 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
+--------+----------+--------+------+
2 rows in set (0.00 sec)

session1
mysql> select * from tb_emp where emp_id >=2 and emp_id<7 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
+--------+----------+--------+------+
2 rows in set (0.00 sec)

session2
mysql> insert into tb_emp values(7,'zz','m','7');
ERROR 1062 (23000): Duplicate entry '7' for key 'PRIMARY'
mysql>


```

[2,3)[3,7)







## 9 非唯一索引 

```mysql
mysql> select * from tb_emp ;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|      1 | chen     | m      | 1    |
|      2 | yi       | m      | 2    |
|      3 | xin      | s      | 3    |
|      7 | zz       | m      | 7    |
|      9 | zz       | m      | 9    |
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
|     14 | zz       | m      | 11   |
+--------+----------+--------+------+

session1

mysql> select * from tb_emp where d_id =10 for update;
+--------+----------+--------+------+
| emp_id | emp_name | gender | d_id |
+--------+----------+--------+------+
|     10 | zz       | m      | 10   |
|     11 | zz       | m      | 10   |
|     12 | zz       | m      | 10   |
|     13 | zz       | m      | 10   |
+--------+----------+--------+------+
4 rows in set (0.00 sec)

session2
mysql> insert into tb_emp values(12,'zz','m','10');
ERROR 1205 (HY000): Lock wait timeout exceeded; try restarting transaction


```

d_id =10 的记录都锁上了，

d_id =9 也锁上了；

d_id =11 也锁上了；

【9，10，10，10，10，11】



# 7 定位配置

```mysql
[mysqld]
log-error =/var/log/mysqld3306.log
innodb_lock_wait_timeout=60     #锁请求超时时间(秒)
innodb_rollback_on_timeout = 1  #事务中某个语句锁请求超时将回滚真个事务
innodb_print_all_deadlocks = 1  #死锁都保存到错误日志
```















