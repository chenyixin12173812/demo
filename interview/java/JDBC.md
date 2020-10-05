

https://cloud.tencent.com/developer/article/1062395



## 1、Truncate与delete的区别

  TRUNCATE table：删除内容、不删除定义、释放空间
  DELETE table：删除内容、不删除定义、不释放空间
  DROP table：删除内容和定义，释放空间
  1、truncate table：只能删除表中全部数据；delete from table where……，可以删除表中全部数据，也可以删除部分数据。
  2、delete from记录是一条条删的，所删除的每行记录都会进日志，而truncate一次性删掉整个页，因此日志里面只记录页的释放。
  3、truncate的执行速度比delete快。
  4、delete执行后，删除的数据占用的存储空间还在，还可以恢复数据；truncate删除的数据占用的存储空间不在，不可以恢复数据。也因此truncate 删除后，不能回滚，delete可以回滚。

# 2 JDBC中的Statement 和PreparedStatement，CallableStatement的区别？

区别：

- PreparedStatement是预编译的SQL语句，效率高于Statement。
- PreparedStatement支持?操作符，相对于Statement更加灵活。
- PreparedStatement可以防止SQL注入，安全性高于Statement。
- CallableStatement适用于执行存储过程。

# 3 JDBC的DataSource是什么，有什么好处

DataSource即数据源，它是定义在javax.sql中的一个接口，跟DriverManager相比，它的功能要更强大**。我们可以用它来创建数据库连接，当然驱动的实现类会实际去完成这个工作。除了能创建连接外，它还提供了如下的特性：**

- **缓存PreparedStatement以便更快的执行**
- **可以设置连接超时时间**
- **提供日志记录的功能**
- **ResultSet大小的最大阈值设置**
- **通过JNDI的支持，可以为servlet容器提供连接池的功能**

## 4 有哪些不同的ResultSet？

根据创建Statement时输入参数的不同，会对应不同类型的ResultSet。如果你看下Connection的方法，你会发现createStatement和prepareStatement方法重载了，以支持不同的ResultSet和并发类型。

一共有三种ResultSet对象。

- **ResultSet.TYPE_FORWARD_ONLY：这是默认的类型，它的游标只能往下移。**
- **ResultSet.TYPE_SCROLL_INSENSITIVE：游标可以上下移动，一旦它创建后，数据库里的数据再发生修改，对它来说是透明的。**
- **ResultSet.TYPE_SCROLL_SENSITIVE：游标可以上下移动，如果生成后数据库还发生了修改操作，它是能够感知到的。**

# 5 execute，executeQuery，executeUpdate的区别是什么？

- Statement的execute(String query)方法用来**执行任意的SQL查询**，如果查询的结果是一个ResultSet，这个方法就返回true。如果结果不是ResultSet，比如insert或者update查询，它就会返回false。我们可以通过它的getResultSet方法来获取ResultSet，或者通过getUpdateCount()方法来获取更新的记录条数。
- Statement的executeQuery(String query)接口用来执行select查询，并且返回ResultSet。即使查询不到记录返回的ResultSet也不会为null。我们通常使用executeQuery**来执行查询语句，这样的话如果传进来的是insert或者update语句的话，它会抛出错误信息为 “executeQuery method can not be used for update”的java.util.SQLException。
- Statement的executeUpdate(String query)方法**用来执行insert或者update/delete（DML）语句，或者 什么也不返回DDL语句**。返回值是int类型，如果是DML语句的话，它就是更新的条数，如果是DDL的话，就返回0。
- 只有当你不确定是什么语句的时候才应该使用execute()方法，否则应该使用executeQuery或者executeUpdate方法。

# 6 jdbc流程

**通过JDBC操作数据库——步骤：**

　　第1步：注冊驱动 (仅仅做一次)

　　第2步：建立连接(Connection)

　　第3步：创建运行SQL的语句(Statement)

　　第4步：运行语句

　　第5步：处理运行结果(ResultSet)

　　第6步：释放资源

![img](https://www.cnblogs.com/yfceshi/p/7095607.html)

使用JDBC第一步：载入驱动

　　注冊驱动有三种方式：

　　1.   Class.forName(“com.mysql.jdbc.Driver”);

　　     推荐这样的方式，不会对详细的驱动类产生依赖

　　2. DriverManager.registerDriver(com.mysql.jdbc.Driver);

 　　    会对详细的驱动类产生依赖

　　3. System.setProperty(“jdbc.drivers”, “driver1:driver2”);

   　  尽管不会对详细的驱动类产生依赖；但注冊不太方便。所以非常少使用

使用JDBC第二步：建立连接

　　通过Connection建立连接，Connection是一个接口类。其功能是与数据库进行连接（会话）。

　　建立Connection接口类对象：

　　Connection conn =DriverManager.getConnection(url, user, password);

　　当中URL的格式要求为：

　　JDBC:子协议:子名称//主机名:port/数据库名？属性名=属性值&…

　　如："jdbc:mysql://localhost:3306/test“

　　user即为登录数据库的username，如root

　　password即为登录数据库的密码，为空就填””

使用JDBC第三步：创建运行对象

　　运行对象Statement负责运行SQL语句。由Connection对象产生。

　　Statement st = connection.createStatement();

　　Statement接口类还派生出两个接口类PreparedStatement和CallableStatement，这两个接口类对象为我们提供了更加强大的数据訪问功能。

　　PreparedStatement能够对SQL语句进行预编译，这样防止了  SQL注入 提高了安全性。

　　PreparedStatement ps=connection.prepareStatement( "update user set id=? where username=?”); ————sql语句中庸 ？ 作为通配符，变量值通过参数设入：ps.setObject(1, object);

　　而且预编译结果能够存储在PreparedStatement对象中。当多次运行SQL语句时能够提高效率。

　　作为Statement的子类，PreparedStatement继承了Statement的全部函数。

 

　　CallableStatement接口

　　CallableStatement类继承了PreparedStatement类，他主要用于运行SQL存储过程。

　　在JDBC中运行SQL存储过程须要转义。

　　JDBC API提供了一个SQL存储过程的转义语法：

　　{call<procedure-name>[<arg1>,<arg2>, ...]}

　　procedure-name：是所要运行的SQL存储过程的名字

　　[<arg1>,<arg2>, ...]：是相相应的SQL存储过程所须要的參数

使用JDBC第四步：运行SQL语句

　　运行对象Statement 或 PreparedStatement 提供两个经常使用的方法来运行SQL语句。

　　executeQuery(Stringsql),该方法用于运行实现查询功能的sql语句。返回类型为ResultSet（结果集）。

　　如：ResultSet rs =st.executeQuery(sql);

　　executeUpdate(Stringsql),该方法用于运行实现增、删、改功能的sql语句，返回类型为int，即受影响的行数。

　　如：int flag = st.executeUpdate(sql);

使用JDBC第五步：处理运行结果

　　ResultSet对象

　　ResultSet对象负责保存Statement运行后所产生的查询结果。

　　结果集ResultSet是通过游标来操作的。

　　游标就是一个可控制的、能够指向随意一条记录的指针。

　　有了这个指针我们就能轻易地指出我们要对结果集中的哪一条记录进行改动、删除，或者要在哪一条记录之前插入数据。一个结果集对象中仅仅包括一个游标。

 　另外，借助ResultSetMetaData ，可以将数据表的结构信息都查出来。

　　ResultSetMetaData rsmd= resultSet.getMetaData();

使用JDBC 第六步——释放资源

　　见随笔：[java mysql数据库链接与资源关闭](https://www.cnblogs.com/lightandtruth/p/9461743.html)

　　数据库资源不关闭，其占用的内存不会被释放，徒耗资源，影响系统。