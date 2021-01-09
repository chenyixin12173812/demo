# mysql 45讲



1

# 2   日志系统： 一条SQL更新语句是如何执行的?

  我们还是从一个表的一条更新语句说起， 下面是这个表的创建语句， 这个表有一个主键ID和一个整型字段c：
如果要将ID=2这一行的值加1， SQL语句就会这么写：前面我有跟你介绍过SQL语句基本的执行链路， 这里我再把那张图拿过来， 你也可以先简单看看这个图回顾下。 首先， 可以确定的说， 查询语句的那一套流程， 更新语句也是同样会走一遍。    

  **在一个表上有更新的时候， 跟这个表有关的查询缓存会失效， 所以这条语句就会把表T上所有缓存结果都清空。 这也就是我们一般不建议使用查询缓存的原因**  





# 3



# 45   自增id用完怎么办  

## 1   表定义自增值id  

 我们以无符号整型为例，存储范围为0～4294967295，约43亿！我们先说一下，一旦自增id达到最大值，此时数据继续插入是会报一个主键冲突异常 。

 那解决方法也是很简单的，将Int类型改为BigInt类型 。



## 2 row_id

 如果你创建的InnoDB表没有指定主键， 那么InnoDB会给你创建一个不可见的， 长度为6个字节的row_id。 InnoDB维护了一个全局的dict_sys.row_id值， 所有无主键的InnoDB表， 每插入一行
数据， 都将当前的dict_sys.row_id值作为要插入数据的row_id， 然后把dict_sys.row_id的值加1。实际上， 在代码实现时row_id是一个长度为8字节的无符号长整型(bigint unsigned)。 但
是， InnoDB在设计时， 给row_id留的只是6个字节的长度， 这样写到数据表中时只放了最后6个字节， 所以row_id能写到数据表中的值， 就有两个特征：

row_id写入表中的值范围， 是从0到2（48） -1；

当dict_sys.row_id=2 时， 如果再有插入数据的行为要来申请row_id， 拿到以再取最后6个
字节的话就是0。也就是说， 写入表的row_id是从0开始到2 -1。 达到上限后， 下一个值就是0， 然后继续循环。
当然， 2（48）-1这个值本身已经很大了， 但是如果一个MySQL实例跑得足够久的话， 还是可能达到这个上限的。 在InnoDB逻辑里， 申请到row_id=N后， 就将这行数据写入表中； 如果表中已经存



```
create table t(id int unsigned auto_increment primary key) auto_increment=4294967295;
insert into t values(null);
//成功插入一行 4294967295
show create table t;
/* CREATE TABLE `t` (
`id` int(10) unsigned NOT NULL AUTO_INCREMENT,
PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4294967295;
*/
insert into t values(null);
//Duplicate entry '4294967295' for key 'PRIMARY'
```

在row_id=N的行， 新写入的行就会覆盖原有的行  

##   3 Xid 

Xid只需要不在同一个binlog文件中出现重复值即可。 虽然理论上会出现重复值， 但是概率极小， 可以忽略不计  

##   4   InnoDB的max_trx_id

 递增值每次MySQL重启都会被保存起来， 所以我们文章中提到的**脏读的例子就是一个必现的bug**， 好在留给我们的时间还很充裕  

  在显示值里面加上2（48） ， 目的是要保证只读事务显示的trx_id值比较大， 正常情况下就会区别于读写事务的id。 但是， trx_id跟row_id的逻辑类似， 定义长度也是8个字节。 因此， 在理论上还是可
能出现一个读写事务与一个只读事务显示的trx_id相同的情况。 不过这个概率很低， 并且也没有什么实质危害， 可以不管它 . 

  **并且， MySQL重启时max_trx_id也不会清0， 也就是说重启MySQL， 这个bug仍然存在**。
**那么， 这个bug也是只存在于理论上吗？**
假设一个MySQL实例的TPS是每秒50万， 持续这个压力的话， 在**17.8年**后， 就会出现这个情况。 如果TPS更高， 这个年限自然也就更短了。 但是， 从MySQL的真正开始流行到现在， 恐怕
都还没有实例跑到过这个上限。 不过， 这个bug是只要MySQL实例服务时间够长， 就会必然出现的。  

# 5   thread_id

是我们使用中最常见的， 而且也是处理得最好的一个自增id逻辑了  
我们在查各种现场的时候， show processlist里面的第一列， 就是thread_id。
系统保存了一个全局变量thread_id_counter， 每新建一个连接， 就将thread_id_counter赋值给这个新连接的线程变量。thread_id_counter定义的大小是4个字节， 因此达到2 -1后， 它就会重置为0， 然后继续增加。但是， 你不会在show processlist里看到两个相同的thread_id  

![1602876358314](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1602876358314.png)