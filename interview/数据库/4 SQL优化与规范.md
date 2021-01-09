# 4 SQL优化与规范

**一、 使用延迟查询优化 limit [offset], [rows]**

经常出现类似以下的 SQL 语句:

```
SELECT * FROM film LIMIT 100000, 10
```

offset 特别大!

这是我司出现很多慢 SQL 的**主要原因**之一，尤其是在跑任务需要分页执行时，经常跑着跑着 offset 就跑到几十万了，导致任务越跑越慢。

LIMIT 能很好地解决分页问题，但如果 offset 过大的话，会造成严重的性能问题，原因主要是因为 MySQL 每次会把一整行都扫描出来，扫描 offset 遍，找到 offset 之后会抛弃 offset 之前的数据，再从 offset 开始读取 10 条数据，显然，这样的读取方式问题。

可以通过**延迟查询**的方式来优化

假设有以下 SQL,有组合索引（sex, rating）

```
SELECT <cols> FROM profiles where sex='M' order by rating limit 100000, 10;
```

则上述写法可以改成如下写法

```
SELECT <cols>   FROM profiles inner join(SELECT id form FROM profiles where x.sex='M' order by rating limit 100000, 10)as x using(id);
```

这里利用了覆盖索引的特性，先从覆盖索引中获取 100010 个 id，再丢充掉前 100000 条 id，保留最后 10 个 id 即可，丢掉 100000 条 id 不是什么大的开销，所以这样可以显著提升性能

**二、 利用 LIMIT 1 取得唯一行**

数据库引擎只要发现满足条件的一行数据则立即停止扫描，，这种情况适用于只需查找一条满足条件的数据的情况

**三、 注意组合索引，要符合最左匹配原则才能生效**

假设存在这样顺序的一个联合索引“col_1, col_2, col_3”。这时，指定条件的顺序就很重要。

```
○ SELECT * FROM SomeTable WHERE col_1 = 10 AND col_2 = 100 AND col_3 = 500;○ SELECT * FROM SomeTable WHERE col_1 = 10 AND col_2 = 100 ;× SELECT * FROM SomeTable WHERE col_2 = 100 AND col_3 = 500 ;
```

前面两条会命中索引，第三条由于没有先匹配 col_1，导致无法命中索引， 另外如果无法保证查询条件里列的顺序与索引一致，可以考虑将联合索引 拆分为多个索引。

**四、使用 LIKE 谓词时，只有前方一致的匹配才能用到索引（最左匹配原则）**

```
× SELECT * FROM SomeTable WHERE col_1 LIKE '%a';× SELECT * FROM SomeTable WHERE col_1 LIKE '%a%';○ SELECT * FROM SomeTable WHERE col_1 LIKE 'a%';
```

上例中，只有第三条会命中索引，前面两条进行后方一致或中间一致的匹配无法命中索引

**五、 简单字符串表达式**

模型字符串可以使用 _ 时， 尽可能避免使用 %, 假设某一列上为 char(5)

不推荐

```
SELECT     first_name,     last_name,    homeroom_nbr  FROM Students WHERE homeroom_nbr LIKE 'A-1%';
```

推荐

```
SELECT first_name, last_namehomeroom_nbr  FROM Students WHERE homeroom_nbr LIKE 'A-1__'; --模式字符串中包含了两个下划线
```

**六、尽量使用自增 id 作为主键**

比如现在有一个用户表，有人说身份证是唯一的，也可以用作主键，理论上确实可以，不过用身份证作主键的话，一是占用空间相对于自增主键大了很多，二是很容易引起频繁的页分裂，造成性能问题（什么是页分裂，请参考[这篇文章](https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247484006&idx=1&sn=3e15abeb5299a3e9b578332dd8565273&scene=21#wechat_redirect)）

主键选择的几个原则：自增，尽量小，不要对主键进行修改

**七、如何优化 count(\*)**

使用以下 sql 会导致慢查询

```
SELECT COUNT(*) FROM SomeTableSELECT COUNT(1) FROM SomeTable
```

原因是会造成全表扫描，有人说 **COUNT(\*)** 不是会利用主键索引去查找吗，怎么还会慢，这就要谈到 MySQL 中的聚簇索引和非聚簇索引了，聚簇索引叶子节点上存有主键值+整行数据，非聚簇索叶子节点上则存有辅助索引的列值 + 主键值，如下

![img](https://mmbiz.qpic.cn/mmbiz_jpg/OyweysCSeLVPRT96ibyqu1hv2bWgRLSSKCTuABBUhW9tSWF2fJNpx73G45AFyiaf10sv4Ks1NPVHzshhFpCt9MDA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

所以就算对 COUNT(*) 使用主键查找，由于每次取出主键索引的叶子节点时，取的是一整行的数据，效率必然不高，但是非聚簇索引叶子节点只存储了「列值 + 主键值」,这也启发我们可以用非聚簇索引来优化，假设表有一列叫 status, 为其加上索引后，可以用以下语句优化:

```
SELECT COUNT(status) FROM SomeTable
```

有人曾经测过（见文末参考链接），假设有 100 万行数据，使用聚簇索引来查找行数的，比使用 COUNT(*) 查找速度快 10 几倍。不过需要注意的是通过这种方式无法计算出  status 值为 null 的那些行

如果主键是连续的，可以利用 MAX(id) 来查找，MAX 也利用到了索引，只需要定位到最大 id 即可，性能极好，如下，秒现结果

```
SELECT MAX(id) FROM SomeTable
```

说句题句话，有人说用 MyISAM 引擎调用 COUNT(*) 非常快，那是因为它提前把行数存在磁盘中了，直接拿，当然很快，不过如果有 WHERE 的限制,用 COUNT(*) 还是很慢!

**八、避免使用 SELECT \* ，尽量利用覆盖索引来优化性能**

**SELECT \*** 会提取出一整行的数据，如果查询条件中用的是组合索引进行查找，还会导致回表（先根据组合索引找到叶子节点，再根据叶子节点上的主键回表查询一整行），降低性能，而如果我们所要的数据就在组合索引里，只需读取组合索引列，这样网络带宽将大大减少,假设有组合索引列 (col_1, col_2)

推荐用

```
SELECT col_1, col_2   FROM SomeTable  WHERE col_1 = xxx AND col_2 = xxx
```

不推荐用

```
SELECT *  FROM SomeTable  WHERE col_1 = xxx AND  col_2 = xxx
```

**九、 如有必要，使用 force index() 强制走某个索引**

业务团队曾经出现类似以下的慢 SQL 查询

```
SELECT *  FROM  SomeTable WHERE `status` = 0   AND `gmt_create` > 1490025600   AND `gmt_create` < 1490630400   AND `id` > 0   AND `post_id` IN ('67778', '67811', '67833', '67834', '67839', '67852', '67861', '67868', '67870', '67878', '67909', '67948', '67951', '67963', '67977', '67983', '67985', '67991', '68032', '68038'/*... omitted 480 items ...*/)order by id asc limit 200;
```

post_id 也加了索引，理论上走 post_id 索引会很快查询出来，但实际通过 EXPLAIN 发现走的却是 id 的索引（这里隐含了一个常见考点，在多个索引的情况下, MySQL 会如何选择索引），而 id > 0 这个查询条件没啥用，直接导致了全表扫描， 所以在有多个索引的情况下一定要慎用，可以使用 force index 来强制走某个索引，以这个例子为例，可以强制走 post_id 索引，效果立杆见影。

这种由于表中有多个索引导致 MySQL 误选索引造成慢查询的情况在业务中也是非常常见，一方面是表索引太多，另一方面也是由于 SQL 语句本身太过复杂导致， 针对本例这种复杂的 SQL 查询，其实用 ElasticSearch 搜索引擎来查找更合适，有机会到时出一篇文章说说。

**十、 使用 EXPLAIN 来查看 SQL 执行计划**

上个点说了，可以使用 EXPLAIN 来分析 SQL 的执行情况，如怎么发现上文中的最左匹配原则不生效呢，执行 「EXPLAIN + SQL 语句」可以发现 key 为 None ,说明确实没有命中索引

![img](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLVPRT96ibyqu1hv2bWgRLSSKbDbWW79uenM9WiboG2k6YCRm2KAaK3aR9V8qO7KwqWQjsiaG4yXUqthw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我司在提供 SQL 查询的同时，也贴心地加了一个 EXPLAIN 功能及 sql 的优化建议，建议各大公司效仿 ^_^,如图示

![img](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLVPRT96ibyqu1hv2bWgRLSSKNld8nRtxYbicFPexs6icA4RqKVnbiaQ49HBFEjzic6ibUXtvvNuSqMU0BxA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**十一、 批量插入，速度更快**

当需要插入数据时，批量插入比逐条插入性能更高

推荐用

```
-- 批量插入INSERT INTO TABLE (id, user_id, title) VALUES (1, 2, 'a'),(2,3,'b');
```

不推荐用

```
INSERT INTO TABLE (id, user_id, title) VALUES (1, 2, 'a');INSERT INTO TABLE (id, user_id, title) VALUES (2,3,'b');
```

批量插入 SQL 执行效率高的主要原因是合并后日志量 MySQL 的 binlog 和 innodb 的事务让日志减少了，降低日志刷盘的数据量和频率，从而提高了效率

**十二、 慢日志 SQL 定位**

前面我们多次说了 SQL 的慢查询，那么该怎么定位这些慢查询 SQL 呢，主要用到了以下几个参数

![img](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLVPRT96ibyqu1hv2bWgRLSSKXr1ia5fws5fl3lpXia5cQULf2icicvhKKo6Z6YibmQo88fhmc2Bo1LhmFDA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这几个参数一定要配好，再根据每条慢查询对症下药，像我司每天都会把这些慢查询提取出来通过邮件给形式发送给各个业务团队，以帮忙定位解决





**一、参数是子查询时，使用 EXISTS 代替 IN**

如果 IN 的参数是（1，2，3）这样的值列表时，没啥问题，但如果参数是子查询时，就需要注意了。比如，现在有如下两个表：

![img](4 SQL优化与规范.assets/640.png)

现在我们要查出同时存在于两个表的员工，即田中和铃木，则以下用 IN 和 EXISTS 返回的结果是一样，但是用 EXISTS 的 SQL 会更快:

```
-- 慢SELECT *   FROM Class_AWHERE id IN (SELECT id                FROM  CLASS_B);-- 快SELECT *  FROM Class_A A  WHERE EXISTS(SELECT *    FROM Class_B  B  WHERE A.id = B.id);
```

为啥使用 EXISTS 的 SQL 运行更快呢，有两个原因

1. 可以`用到索引，如果连接列 (id) 上建立了索引，那么查询 Class_B 时不用查实际的表，只需查索引就可以了。
2. 如果使用 EXISTS，那么只要查到一行数据满足条件就会终止查询， 不用像使用 IN 时一样扫描全表。在这一点上 NOT EXISTS 也一样

另外如果 IN 后面如果跟着的是子查询，由于 SQL 会先执行 IN 后面的子查询，会将子查询的结果保存在一张临时的工作表里（内联视图），然后扫描**整个视图**，显然扫描整个视图这个工作很多时候是非常耗时的，而用 EXISTS 不会生成临时表。

当然了，如果 IN 的参数是子查询时，也可以用连接来代替，如下：

```
-- 使用连接代替 IN SELECT A.id, A.nameFROM Class_A A INNER JOIN Class_B B ON A.id = B.id;
```

用到了 「id」列上的索引，而且由于没有子查询，也不会生成临时表

**二、避免排序**

SQL 是声明式语言，即对用户来说，只关心它能做什么，不用关心它怎么做。这样可能会产生潜在的性能问题：排序，会产生排序的代表性运算有下面这些

- GROUP BY 子句
- ORDER BY 子句
- 聚合函数(SUM、COUNT、AVG、MAX、MIN)
- DISTINCT
- 集合运算符(UNION、INTERSECT、EXCEPT)
- 窗口函数(RANK、ROW_NUMBER 等)

如果在内存中排序还好，但如果内存不够导致需要在硬盘上排序上的话，性能就会急剧下降，所以我们需要减少不必要的排序。怎样做可以减少排序呢。

1、 使用集合运算符的 ALL 可选项

SQL 中有 UNION，INTERSECT，EXCEPT 三个集合运算符，默认情况下，这些运算符会为了避免重复数据而进行排序，对比一下使用 UNION 运算符加和不加 ALL 的情况:

![img](4 SQL优化与规范.assets/640.png)

注意：加 ALL 是优化性能非常有效的手段，不过前提是不在乎结果是否有重复数据。

2、使用 EXISTS 代表 DISTINCT

为了排除重复数据， DISTINCT 也会对结果进行排序，如果需要对两张表的连接结果进行去重，可以考虑用 EXISTS 代替 DISTINCT，这样可以避免排序。

![img](4 SQL优化与规范.assets/640.png)

如何找出有销售记录的商品，使用如下 DISTINCT 可以：

```
SELECT DISTINCT I.item_noFROM Items I INNER JOIN SalesHistory SHON I. item_no = SH. item_no;
```

不过更好的方式是使用 EXISTS:

```
SELECT item_no FROM Items IWHERE EXISTS         (SELECT *           FROM SalesHistory SH          WHERE I.item_no = SH.item_no);
```

既用到了索引，又避免了排序对性能的损耗。

**二、在极值函数中使用索引（MAX/MIN）**

使用 MAX/ MIN 都会对进行排序，如果参数字段上没加索引会导致全表扫描，如果建有索引，则只需要扫描索引即可，对比如下

```
-- 这样写需要扫描全表 SELECT MAX(item)  FROM Items;-- 这样写能用到索引 SELECT MAX(item_no)  FROM Items;
```

注意：极值函数参数推荐为索引列中并不是不需要排序，而是优化了排序前的查找速度（毕竟索引本身就是有序排列的）。

**三、能写在 WHERE 子句里的条件不要写在 HAVING 子句里**

下列 SQL 语句返回的结果是一样的:

```
-- 聚合后使用 HAVING 子句过滤SELECT sale_date, SUM(quantity)  FROM SalesHistory GROUP BY sale_dateHAVING sale_date = '2007-10-01';-- 聚合前使用 WHERE 子句过滤SELECT sale_date, SUM(quantity)  FROM SalesHistory WHERE sale_date = '2007-10-01'  GROUP BY sale_date;
```

使用第二条语句效率更高，原因主要有两点

1. 使用 GROUP BY 子句进行聚合时会进行排序，如果事先通过 WHERE 子句能筛选出一部分行，能减轻排序的负担
2. 在 WHERE 子句中可以使用索引，而 HAVING 子句是针对聚合后生成的视频进行筛选的，但很多时候聚合后生成的视图并没有保留原表的索引结构

**四、在 GROUP BY 子句和 ORDER BY 子句中使用索引**

GROUP BY 子句和 ORDER BY 子句一般都会进行排序，以对行进行排列和替换，不过如果指定带有索引的列作为这两者的参数列，由于用到了索引，可以实现高速查询，由于索引是有序的，排序本身都会被省略掉

**五、使用索引时，条件表达式的左侧应该是原始字段**

假设我们在 col 列上建立了索引，则下面这些 SQL 语句无法用到索引

```
SELECT *  FROM SomeTable WHERE col * 1.1 > 100;SELECT *  FROM SomeTable WHERE SUBSTR(col, 1, 1) = 'a';
```

以上第一个 SQL 在索引列上进行了运算, 第二个 SQL 对索引列使用了函数，均无法用到索引，正确方式是把列单独放在左侧,如下:

```
SELECT *  FROM SomeTable WHERE col_1 > 100 / 1.1;
```

当然如果需要对此列使用函数，则无法避免在左侧运算，可以考虑使用函数索引，不过一般不推荐随意这么做。

**六、尽量避免使用否定形式**

如下的几种否定形式不能用到索引：

- <>
- !=
- NOT IN

所以以下 了SQL 语句会导致全表扫描

```
SELECT *  FROM SomeTable WHERE col_1 <> 100;
```

可以改成以下形式

```
SELECT *  FROM SomeTable WHERE col_1 > 100 or col_1 < 100;
```

**七、进行默认的类型转换**

假设 col 是 char 类型，则推荐使用以下第二，三条 SQL 的写法，不推荐第一条 SQL 的写法

```
× SELECT * FROM SomeTable WHERE col_1 = 10;○ SELECT * FROM SomeTable WHERE col_1 = '10';○ SELECT * FROM SomeTable WHERE col_1 = CAST(10, AS CHAR(2));
```

虽然第一条 SQL 会默认把 10 转成 '10'，但这种默认类型转换不仅会增加额外的性能开销，还会导致索引不可用，所以建议使用的时候进行类型转换。

**八、减少中间表**

在 SQL 中，子查询的结果会产生一张新表，不过如果不加限制大量使用中间表的话，会带来两个问题，一是展示数据需要消耗内存资源，二是原始表中的索引不容易用到，所以尽量减少中间表也可以提升性能。

**九、灵活使用 HAVING 子句**

这一点与上面第八条相呼应，对聚合结果指定筛选条件时，使用 HAVING 是基本的原则，可能一些工程师会倾向于使用下面这样的写法:

```
SELECT *  FROM (SELECT sale_date, MAX(quantity) AS max_qty          FROM SalesHistory          GROUP BY sale_date) TMP         WHERE max_qty >= 10;
```

虽然上面这样的写法能达到目的，但会生成 TMP 这张临时表，所以应该使用下面这样的写法:

```
SELECT sale_date, MAX(quantity)   FROM SalesHistory GROUP BY sale_dateHAVING MAX(quantity) >= 10;
```

HAVING 子句和聚合操作是同时执行的，所以比起生成中间表后再执行 HAVING 子句，效率会更高，代码也更简洁

**10、需要对多个字段使用 IN 谓词时，将它们汇总到一处**

一个表的多个字段可能都使用了 IN 谓词，如下:

```
SELECT id, state, city   FROM Addresses1 A1 WHERE state IN (SELECT state                   FROM Addresses2 A2                  WHERE A1.id = A2.id)     AND city IN (SELECT city                   FROM Addresses2 A2                   WHERE A1.id = A2.id);
```

这段代码用到了两个子查询，也就产生了两个中间表，可以像下面这样写

```
SELECT *  FROM Addresses1 A1 WHERE id || state || city IN (SELECT id || state|| city       FROM Addresses2 A2);
```

这样子查询不用考虑关联性，没有中间表产生，而且只执行一次即可。















# 一.基本规范

1，不要使用外键

表达超过100w行，修改需要加锁，影响性能。

2 左前缀原则

3 is null和 is not null不能使用索引

4 like （%abc%）会导致索引失效

如果不得不使用like进行模糊查询时，不要在关键字前面加%。
反例：

```abap
select id,name,age from persons where name like '%abc%'
```



如果在关键字前面加%，那么查询是肯定要走全表查询的。
正例：

```abap
select id,name,age from persons where name like 'abc%'
```

 若要提高效率，可以考虑全文检索。 

5 or 是困难谓词（可以使用in（mysql优化了in可以使用索引））  否则将导致引擎放弃使用索引而进行全表扫描 

6 查询字符串 不加引号会使索引失效

**索引失效会导致行锁变表锁**。比如 vchar 查询不写单引号的情况

7.在索引上计算（计算，函数，手动转换）会导致索引失效。

8  避免在整个表上使用count(*) ，它可能会将整个表锁住 

9  保持查询一致，这样后续类似的查询就能使用查询缓存了 

10  如果合适，用 GROUP BY 代替 DISTINCT 

11  在 WHERE、GROUP BY 和 ORDER BY 的列上加上索引 

12 避免select * ，最小化你要查询的数据，只获取你需要的数据 

13  保证索引简单，不要在同一列上加多个索引 

14  MySQL 会选择错误的索引，这种情况使用 USE INDEX 

15   当服务器的负载增加时，使用SHOW PROCESSLIST来查看慢的/有问题的查询 

16  索引字段少于5个时，UNION 操作用 LIMIT，而不是 OR 

17  应尽量避免在 where 子句中使用!=或<>操作符，否则将引擎放弃使用索引而进行全表扫描 

18  in 和 not in 也是困难谓词，否则会导致全表扫描 

19 关联代替子查询

例如，我们要将客户基本信息表中没有任何订单的客户删除掉，就可以利用子查询先从销售信息表中将所有发出订单的客户ID取出来，然后将结果传递给主查询，如下所示： 

DELETE FROM customerinfo 
WHERE CustomerID NOT in (SELECT CustomerID FROM salesinfo ) 

　　使用子查询可以一次性的完成很多逻辑上需要多个步骤才能完成的SQL操作，同时也可以避免事务或者表锁死，并且写起来也很容易。但是，有些情况下，子查询可以被更有效率的连接（JOIN）.. 替代。例如，假设我们要将所有没有订单记录的用户取出来，可以用下面这个查询完成： 

SELECT * FROM customerinfo 
WHERE CustomerID NOT in (SELECT CustomerID FROM salesinfo ) 

　　如果使用连接（JOIN）.. 来完成这个查询工作，速度将会快很多。尤其是当salesinfo表中对CustomerID建有索引的话，性能将会更好，查询如下： 

SELECT * FROM customerinfo 
LEFT JOIN salesinfoON customerinfo.CustomerID=salesinfo. 
CustomerID 
WHERE salesinfo.CustomerID IS NULL 

　　连接（JOIN）.. 之所以更有效率一些，是因为 MySQL不需要在内存中创建临时表来完成这个逻辑上的需要两个步骤的查询工作 ，两个表都可以使用索引。

20  尽量避免大事务操作，提高系统并发能力 

21 使用索引字段和 ORDER BY 来代替 MAX 

22  使用 INSERT ON DUPLICATE KEY 或 INSERT IGNORE 来代替 UPDATE，避免 UPDATE 前需要先 SELECT

23 如果使用到了临时表，在存储过程的最后务必将所有的临时表显式删除，先 truncate table ，然后 drop table ，这样以避免系统表的较长时间锁定 

24、避免使用 ORDER BY RAND()。 

![\](http://www.2cto.com/uploadfile/Collfiles/20161021/201610211012372353.png)

上面的查询，会导致每条记录都执行rand()，成本很高！

建议，通过mt_rand()，先确定的随机主键，再从数据表中获取数据。25、LIMIT M,N 在特定场景下会降低查询效率，有节制使用。 
26、使用 UNION 来代替 WHERE 子句中的子查询。 
27、对 UPDATE 来说，使用 SHARE MODE 来防止排他锁。 
28、重启 MySQL 时，记得预热数据库，确保将数据加载到内存，提高查询效率。 
29、使用 DROP TABLE ，然后再 CREATE TABLE ，而不是 DELETE FROM ，以删除表中所有数据 

30 尽量用union all代替union
union和union all的差异主要是前者需要将两个（或者多个）结果集合并后再进行唯一性过滤操作，这就会涉及到排序，增加大量的cpu运算，加大资源消耗及延迟。所以当我们可以确认不可能出现重复结果集或者不在乎重复结果集的时候，尽量使用union all而不是union 

31 避免类型转换和隐式转换

这里所说的“类型转换”是指 where 子句中出现 column 字段的类型和传入的参数类型不一致的时候发生的类型转换：

人为在column_name 上通过转换函数进行转换直接导致 MySQL(实际上其他数据库也会有同样的问题)无法使用索引，如果非要转换，应该在传入的参数上进行转换，由数据库自己进行转换，

如果我们传入的数据类型和字段类型不一致，同时我们又没有做任何类型转换处理，MySQL 可能会自己对我们的数据进行类型转换操作，也可能不进行处理而交由存储引擎去处理，这样一来，就会出现索引无法使用的情况而造成执行计划问题

42 大量数据的插入

多条 insert或者Load data into table（从文件里载入数据到表里）

建议，先关闭约束及索引，完成数据插入，再重新生成索引及约束

43 对于并发性的SQL

少用（不用）多表操作（子查询，联合查询），而是将复杂的SQL拆分多次执行。如果查询很原子（很小），会增加查询缓存的利用率

44 单库和索引不查过500G

45 单库表不超过150个

46 单表字段不超过80个

47 禁止使用 存储过程、触发器、Event实现逻辑

48 禁止使用视图

视图无法完成sql审核和优化，容易产生性能问题。

49 禁止外键

50 表的索引不超过5个

51 表join 应有索引

52 尽量满足2NF，冗余可以不满足3NF

53 SQL中的in不超过500个

54多使用等值，《，》使后面的字段无法使用索引

55 update 、delete 不使用limit

56 update 、delete 不使用order by 。group by

57 多表关联必须使用表的别名





# 二 字段数据类型

1.尽量使用数字型字段，若只含数值信息的字段尽量不要设计为字符型，这会降低查询和连接的性能，并会增加存储开销。这是因为引擎在处理查询和连 接时会逐个比较字符串中每一个字符，而对于数字型而言只需要比较一次就够了。

2 .尽可能的使用 varchar/nvarchar 代替 char/nchar ，因为首先变长字段存储空间小，可以节省存储空间，其次对于查询来

3 时间字段使用datetime 不适用timesstamp

4禁止使用Bob或text

5 varchar 长度小于4000

6禁止字符类型放入时间

7禁止字符类型放入数字

8禁止表字段 单独定义字符集

9 禁止使用哪个ENUM 使用TINYINNT代替



MySQL 支持的数据类型非常多，选择正确的数据类型对于获取高性能至关重要。不管存储哪种类型的数据，下面几个简单的原则都有助于做出更好的选择。

- 更小的通常更好：一般情况下，应该尽量使用可以正确存储数据的最小数据类型。
  简单就好：简单的数据类型通常需要更少的CPU周期。例如，整数比字符操作代价更低，因为字符集和校对规则（排序规则）使字符比较比整型比较复杂。
- 尽量避免NULL：通常情况下最好指定列为NOT NULL