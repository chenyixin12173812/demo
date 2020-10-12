# java集合问题

# 1 ？ 

# 2  ？ 

# 3 为什么要转成红黑树呢？

 所以只有当包含足够多的 Nodes 时才会转成 TreeNodes，这个足够多的标准就是由 TREEIFY_THRESHOLD 的值（默认值8）决定的。而当桶中节点数由于移除或者 resize (扩容) 变少后，红黑树会转变为普通的链表，这个阈值是 UNTREEIFY_THRESHOLD（默认值6）  

红黑树虽然查询效率比链表高，但是结点占用的空间大，只有达到一定的数目才有树化的意义，这是**基于时间和空间的平衡考虑**。 

# 4 **为什么树化标准是8个**，退化是6个

```
* 0:    0.60653066
* 1:    0.30326533
* 2:    0.07581633
* 3:    0.01263606
* 4:    0.00157952
* 5:    0.00015795
* 6:    0.00001316
* 7:    0.00000094
* 8:    0.00000006
```

 注释中给我们展示了1-8长度的具体命中概率，当长度为8的时候，概率概率仅为0.00000006，这么小的概率，HashMap的红黑树转换几乎不会发生 

 主要是一个过渡，避免链表和红黑树之间频繁的转换。如果阈值是7的话，删除一个元素红黑树就必须退化为链表，增加一个元素就必须树化，来回不断的转换结构无疑会降低性能，所以阈值才不设置的那么临界 

![1597160986874](C:\Users\chen\AppData\Roaming\Typora\typora-user-images\1597160986874.png)

# 5 HashMap为什么不用B+树来替换红黑树

B+胖，一层太多，用来查找的，退化成链表。

# 6 HashMap为什么为什么不使用AVL树而使用红黑树？

红黑树和AVL树都是**最常用的平衡二叉搜索树**，它们的查找、删除、修改都是O(lgn) time

AVL树和红黑树有几点比较和区别：
（1）AVL树是更加严格的平衡，因此可以提供更快的查找速度，一般读取查找密集型任务，适用AVL树。
（2）红黑树更适合于插入修改密集型任务。
（3）通常，AVL树的旋转比红黑树的旋转更加难以平衡和调试。

**总结**：
（1）AVL以及红黑树是高度平衡的树数据结构。它们非常相似，真正的区别在于在任何添加/删除操作时完成的旋转操作次数。
（2）两种实现都缩放为a O(lg N)，其中N是叶子的数量，但实际上AVL树在查找密集型任务上更快：利用更好的平衡，树遍历平均更短。另一方面，插入和删除方面，AVL树速度较慢：需要更高的旋转次数才能在修改时正确地重新平衡数据结构。
（3）在AVL树中，从根到任何叶子的最短路径和最长路径之间的差异最多为1。在红黑树中，差异可以是2倍。
（4）两个都给O（log n）查找，但平衡AVL树可能需要O（log n）旋转，而红黑树将需要最多两次旋转使其达到平衡（尽管可能需要检查O（log n）节点以确定旋转的位置）。旋转本身是O（1）操作，因为你只是移动指针。

# 7 既然红黑树那么好，为啥hashmap不直接采用红黑树，而是当大于8个的时候才转换红黑树？

 因为红黑树需要进行左旋，右旋操作， 而单链表不需要，
以下都是单链表与红黑树结构对比。
如果元素小于8个，查询成本高，新增成本低
如果元素大于8个，查询成本低，新增成本高 

# 8 hashmap的实现

 https://www.jianshu.com/p/d5bcd1b9b2a2 



```text
     int threshold;             // 所能容纳的key-value对极限 
     final float loadFactor;    // 负载因子
     int modCount;  
     int size;
```

怎么优化的？ **加载因子（默认0.75）：为什么需要使用加载因子，为什么需要扩容呢**？**因为如果填充比很大，说明利用的空间很多，如果一直不进行扩容的话，链表就会越来越长，这样查找的效率很低，因为链表的长度很大（当然最新版本使用了红黑树后会改进很多），扩容之后，将原来链表数组的每一个链表分成奇偶两个子链表分别挂在新链表数组的散列位置，这样就减少了每个链表的长度，增加查找效率** 

 从结构实现来讲，HashMap是数组+链表+红黑树（JDK1.8增加了红黑树部分）实现的，如下如所示。 

 ![img](https://pic1.zhimg.com/80/8db4a3bdfb238da1a1c4431d2b6e075c_720w.png) 





# 8.0 桶结构

```java
static class Node<K,V> implements Map.Entry<K,V> {
        final int hash;    //用来定位数组索引位置
        final K key;
        V value;
        Node<K,V> next;   //链表的下一个node

        Node(int hash, K key, V value, Node<K,V> next) { ... }
        public final K getKey(){ ... }
        public final V getValue() { ... }
        public final String toString() { ... }
        public final int hashCode() { ... }
        public final V setValue(V newValue) { ... }
        public final boolean equals(Object o) { ... }
}
```

## 8.1 hash值

```java
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }
```

**key.hashcode()^(hashcode>>>16)%length,当length是2^n时，h&（length-1）运算等价于h%length，而&操作比%效率更高。而采用高16位和低16位进行异或，也可以让所有的位数都参与越算，使得在length比较小的时候也可以做到尽量的散列**。

因为 hash 方法 (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16) 这里返回了 0 ， 0 & 数组长度-1。都等于0，所以**hash数组的第一个位置会存放 key 为 null 的元素**



## 8.2 取模

不是简单的取模，n为2的倍数，而是（n-1）&hash，效率更高，找到桶的位置。

## 8.3 扩容

### 8.3.1 jdk1.7的实现

 

 我们分析下resize的源码，鉴于JDK1.8融入了红黑树，较复杂，为了便于理解我们仍然使用JDK1.7的代码，好理解一些，本质上区别不大，具体区别后文再说

 ```java
void resize(int newCapacity) {   //传入新的容量
 2     Entry[] oldTable = table;    //引用扩容前的Entry数组
 3     int oldCapacity = oldTable.length;         
 4     if (oldCapacity == MAXIMUM_CAPACITY) {  //扩容前的数组大小如果已经达到最大(2^30)了
 5         threshold = Integer.MAX_VALUE; //修改阈值为int的最大值(2^31-1)，这样以后就不会扩容了
 6         return;
 7     }
 8  
 9     Entry[] newTable = new Entry[newCapacity];  //初始化一个新的Entry数组
10     transfer(newTable);                         //！！将数据转移到新的Entry数组里
11     table = newTable;                           //HashMap的table属性引用新的Entry数组
12     threshold = (int)(newCapacity * loadFactor);//修改阈值
13 }
 ```

这里就是使用一个容量更大的数组来代替已有的容量小的数组，transfer()方法将原有Entry数组的元素拷贝到新的Entry数组里。

```text
 1 void transfer(Entry[] newTable) {
 2     Entry[] src = table;                   //src引用了旧的Entry数组
 3     int newCapacity = newTable.length;
 4     for (int j = 0; j < src.length; j++) { //遍历旧的Entry数组
 5         Entry<K,V> e = src[j];             //取得旧Entry数组的每个元素
 6         if (e != null) {
 7             src[j] = null;//释放旧Entry数组的对象引用（for循环后，旧的Entry数组不再引用任何对象）
 8             do {
 9                 Entry<K,V> next = e.next;
10                 int i = indexFor(e.hash, newCapacity); //！！重新计算每个元素在数组中的位置
11                 e.next = newTable[i]; //标记[1]
12                 newTable[i] = e;      //将元素放在数组上
13                 e = next;             //访问下一个Entry链上的元素
14             } while (e != null);
15         }
16     }
17 }
```

n**ewTable[i]的引用赋给了e.next，也就是使用了单链表的头插入方式，同一位置上新元素总会被放在链表的头部位置；这样先放在一个索引上的元素终会被放到Entry链的尾部(如果发生了hash冲突的话），这一点和Jdk1.8有区别，下文详解。在旧数组中同一条Entry链上的元素，通过重新计算索引位置后，有可能被放到了新数组的不同位置上**。

下面举个例子说明下扩容过程。假设了我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。其中的哈希桶数组table的size=2， 所以key = 3、7、5，put顺序依次为 5、7、3。在mod 2以后都冲突在table[1]这里了。这里假设负载因子 loadFactor=1，即当键值对的实际大小size 大于 table的实际大小时进行扩容。接下来的三个步骤是哈希桶数组 resize成4，然后所有的Node重新rehash的过程。

![img](https://pic1.zhimg.com/80/e5aa99e811d1814e010afa7779b759d4_720w.png)





### 8.3.2 jdk1.8的实现

JDK1.8做了哪些**优化**。经过观测可以发现，我们使用的是2次幂的扩展(指**长度扩为原来2倍**)，所以，**元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置**。看下图可以明白这句话的意思，n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。

![img](https://pic2.zhimg.com/80/a285d9b2da279a18b052fe5eed69afe9_720w.png)

元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：

![img](https://pic2.zhimg.com/80/b2cb057773e3d67976c535d6ef547d51_720w.png)

因此，我们在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，**只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变，是1的话索引变成“原索引+oldCap”**，可以看看下图为16扩充为32的resize示意图：

![img](https://pic3.zhimg.com/80/544caeb82a329fa49cc99842818ed1ba_720w.png)

这个设计确实非常的巧妙，既省去了重新计算hash值的时间，而且同时，**由于新增的1bit是0还是1可以认为是随机的，因此resize的过程，均匀的把之前的冲突的节点分散到新的bucket了。这一块就是JDK1.8新增的优化点。有一点注意区别，JDK1.7中rehash的时候，旧链表迁移新链表的时候，如果在新表的数组索引位置相同，则链表元素会倒置，但是从上图可以看出，JDK1.8不会倒置**。有兴趣的同学可以研究下JDK1.8的resize源码，写的很赞，如下:

```java
 1 final Node<K,V>[] resize() {
 2     Node<K,V>[] oldTab = table;
 3     int oldCap = (oldTab == null) ? 0 : oldTab.length;
 4     int oldThr = threshold;
 5     int newCap, newThr = 0;
 6     if (oldCap > 0) {
 7         // 超过最大值就不再扩充了，就只好随你碰撞去吧
 8         if (oldCap >= MAXIMUM_CAPACITY) {
 9             threshold = Integer.MAX_VALUE;
10             return oldTab;
11         }
12         // 没超过最大值，就扩充为原来的2倍
13         else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
14                  oldCap >= DEFAULT_INITIAL_CAPACITY)
15             newThr = oldThr << 1; // double threshold
16     }
17     else if (oldThr > 0) // initial capacity was placed in threshold
18         newCap = oldThr;
19     else {               // zero initial threshold signifies using defaults
20         newCap = DEFAULT_INITIAL_CAPACITY;
21         newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
22     }
23     // 计算新的resize上限
24     if (newThr == 0) {
25 
26         float ft = (float)newCap * loadFactor;
27         newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
28                   (int)ft : Integer.MAX_VALUE);
29     }
30     threshold = newThr;
31     @SuppressWarnings({"rawtypes"，"unchecked"})
32         Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
33     table = newTab;
34     if (oldTab != null) {
35         // 把每个bucket都移动到新的buckets中
36         for (int j = 0; j < oldCap; ++j) {
37             Node<K,V> e;
38             if ((e = oldTab[j]) != null) {
39                 oldTab[j] = null;
40                 if (e.next == null)
41                     newTab[e.hash & (newCap - 1)] = e;
42                 else if (e instanceof TreeNode)
43                     ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
44                 else { // 链表优化重hash的代码块
45                     Node<K,V> loHead = null, loTail = null;
46                     Node<K,V> hiHead = null, hiTail = null;
47                     Node<K,V> next;
48                     do {
49                         next = e.next;
50                         // 原索引
51                         if ((e.hash & oldCap) == 0) {
52                             if (loTail == null)
53                                 loHead = e;
54                             else
55                                 loTail.next = e;
56                             loTail = e;
57                         }
58                         // 原索引+oldCap
59                         else {
60                             if (hiTail == null)
61                                 hiHead = e;
62                             else
63                                 hiTail.next = e;
64                             hiTail = e;
65                         }
66                     } while ((e = next) != null);
67                     // 原索引放到bucket里
68                     if (loTail != null) {
69                         loTail.next = null;
70                         newTab[j] = loHead;
71                     }
72                     // 原索引+oldCap放到bucket里
73                     if (hiTail != null) {
74                         hiTail.next = null;
75                         newTab[j + oldCap] = hiHead;
76                     }
77                 }
78             }
79         }
80     }
81     return newTab;
82 }
```

```java
/**
    * Initializes or doubles table size.  If null, allocates in
    * accord with initial capacity target held in field threshold.
    * Otherwise, because we are using power-of-two expansion, the
    * elements from each bin must either stay at same index, or move
    * with a power of two offset in the new table.
    *
    * @return the table
    */
   final Node<K,V>[] resize() {
       Node<K,V>[] oldTab = table;
       int oldCap = (oldTab == null) ? 0 : oldTab.length;
       int oldThr = threshold;
       int newCap, newThr = 0;

/*如果旧表的长度不是空*/
       if (oldCap > 0) {
           if (oldCap >= MAXIMUM_CAPACITY) {
               threshold = Integer.MAX_VALUE;
               return oldTab;
           }
/*把新表的长度设置为旧表长度的两倍，newCap=2*oldCap*/
           else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
      /*把新表的门限设置为旧表门限的两倍，newThr=oldThr*2*/
               newThr = oldThr << 1; // double threshold
       }
    /*如果旧表的长度的是0，就是说第一次初始化表*/
       else if (oldThr > 0) // initial capacity was placed in threshold
           newCap = oldThr;
       else {               // zero initial threshold signifies using defaults
           newCap = DEFAULT_INITIAL_CAPACITY;
           newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
       }



       if (newThr == 0) {
           float ft = (float)newCap * loadFactor;//新表长度乘以加载因子
           newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                     (int)ft : Integer.MAX_VALUE);
       }
       threshold = newThr;
       @SuppressWarnings({"rawtypes","unchecked"})
/*下面开始构造新表，初始化表中的数据*/
       Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
       table = newTab;//把新表赋值给table
       if (oldTab != null) {//原表不是空要把原表中数据移动到新表中
           /*遍历原来的旧表*/
           for (int j = 0; j < oldCap; ++j) {
               Node<K,V> e;
               if ((e = oldTab[j]) != null) {
                   oldTab[j] = null;
                   if (e.next == null)//说明这个node没有链表直接放在新表的e.hash & (newCap - 1)位置
                       newTab[e.hash & (newCap - 1)] = e;
                   else if (e instanceof TreeNode)
                       ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
/*如果e后边有链表,到这里表示e后面带着个单链表，需要遍历单链表，将每个结点重*/
                   else { // preserve order保证顺序
                ////新计算在新表的位置，并进行搬运
                       Node<K,V> loHead = null, loTail = null;
                       Node<K,V> hiHead = null, hiTail = null;
                       Node<K,V> next;

                       do {
                           next = e.next;//记录下一个结点
          //新表是旧表的两倍容量，实例上就把单链表拆分为两队，
　　　　　　　　　　　　　//e.hash&oldCap为偶数一队，e.hash&oldCap为奇数一对
                           if ((e.hash & oldCap) == 0) {
                               if (loTail == null)
                                   loHead = e;
                               else
                                   loTail.next = e;
                               loTail = e;
                           }
                           else {
                               if (hiTail == null)
                                   hiHead = e;
                               else
                                   hiTail.next = e;
                               hiTail = e;
                           }
                       } while ((e = next) != null);

                       if (loTail != null) {//lo队不为null，放在新表原位置
                           loTail.next = null;
                           newTab[j] = loHead;
                       }
                       if (hiTail != null) {//hi队不为null，放在新表j+oldCap位置
                           hiTail.next = null;
                           newTab[j + oldCap] = hiHead;
                       }
                   }
               }
           }
       }
       return newTab;
   }
```

# 8.4 put

 ![img](https://pic3.zhimg.com/80/58e67eae921e4b431782c07444af824e_720w.png) 

①.判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；

②.根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，转向⑥，如果table[i]不为空，转向③；

③.判断table[i]的首个元素是否和key一样，如果相同直接覆盖value，否则转向④，这里的相同指的是hashCode以及equals；

④.判断table[i] 是否为treeNode，即table[i] 是否是红黑树，如果是红黑树，则直接在树中插入键值对，否则转向⑤；

⑤.遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现key已经存在直接覆盖value即可；

⑥.插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。

```java
public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }
     /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.
     * @return previous value, or null if none
     */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
        Node<K,V>[] tab;
    Node<K,V> p;
    int n, i;
        if ((tab = table) == null || (n = tab.length) == 0)
            n = (tab = resize()).length;
    /*如果table的在（n-1）&hash的值是空，就新建一个节点插入在该位置*/
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
    /*表示有冲突,开始处理冲突*/
        else {
            Node<K,V> e;
        K k;
    /*检查第一个Node，p是不是要找的值*/
            if (p.hash == hash &&((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                for (int binCount = 0; ; ++binCount) {
        /*指针为空就挂在后面*/
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
               //如果冲突的节点数已经达到8个，看是否需要改变冲突节点的存储结构，
　　　　　　　　　　　　//treeifyBin首先判断当前hashMap的长度，如果不足64，只进行
                        //resize，扩容table，如果达到64，那么将冲突的存储结构为红黑树
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            treeifyBin(tab, hash);
                        break;
                    }
        /*如果有相同的key值就结束遍历*/
                    if (e.hash == hash &&((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    p = e;
                }
            }
    /*就是链表上有相同的key值*/
            if (e != null) { // existing mapping for key，就是key的Value存在
                V oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;//返回存在的Value值
            }
        }
        ++modCount;
     /*如果当前大小大于门限，门限原本是初始容量*0.75*/
        if (++size > threshold)
            resize();//扩容两倍
        afterNodeInsertion(evict);
        return null;
    }
```



# 8.5 get

```java
public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }
      /**
     * Implements Map.get and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @return the node, or null if none
     */
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab;//Entry对象数组
    Node<K,V> first,e; //在tab数组中经过散列的第一个位置
    int n;
    K k;
    /*找到插入的第一个Node，方法是hash值和n-1相与，tab[(n - 1) & hash]*/
    //也就是说在一条链上的hash值相同的
        if ((tab = table) != null && (n = tab.length) > 0 &&(first = tab[(n - 1) & hash]) != null) {
    /*检查第一个Node是不是要找的Node*/
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))//判断条件是hash值要相同，key值要相同
                return first;
      /*检查first后面的node*/
            if ((e = first.next) != null) {
                //查找红黑树
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                /*遍历后面的链表，找到key值和hash值都相同的Node*/
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }
```



# 8.6 remove

## 8.7 红黑树 treeifyBin(tab, hash) 方法

> 1、如果数组长度小于 64 MIN_TREEIFY_CAPACITY 那么不进行树化，而是直接进行扩容
>  2、将单向链表转化成 TreeNode ，然后将 TreeNode 连接成双向链表
>  3、将 TreeNode 树化成，红黑树



```java
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    //如果数组长度小于 64 MIN_TREEIFY_CAPACITY 那么不进行树
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
        //扩容
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            //循环将 Node 转换成 TreeNode
            TreeNode<K,V> p = replacementTreeNode(e, null);
            //将 TreeNode 连接成双向链表
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        //转红黑树
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```







## 8.7.Hashtable的size()方法中明明只有一条语句”return count”，为什么还要做同步？

一条语句，为什么还要加锁？


关于这个问题，在慢慢地工作、学习中，有了理解，主要原因有两点：
（1） 同一时间只能有一条线程执行固定类的同步方法，但是对于类的非同步方法，可以多条线程同时访问 。所以，这样就有问题了，可能线程A在执行Hashtable的put方法添加数据，线程B则可以正常调用size()方法读取Hashtable中当前元素的个数，那读取到的值可能不是最新的，可能线程A添加了完了数据，但是没有对size++，线程B就已经读取size了，那么对于线程B来说读取到的size一定是不准确的。而给size()方法加了同步之后，意味着线程B调用size()方法只有在线程A调用put方法完毕之后才可以调用，这样就保证了线程安全性


（2） CPU执行代码，执行的不是Java代码，这点很关键，一定得记住 。Java代码最终是被翻译成汇编代码执行的，汇编代码才是真正可以和硬件电路交互的代码。 即使你看到Java代码只有一行，甚至你看到Java代码编译之后生成的字节码也只有一行，也不意味着对于底层来说这句语句的操作只有一个 。**一句”return count”假设被翻译成了三句汇编语句执行，完全可能执行完第一句，线程就切换了**。





# 9 HashSet 和 HashMap 区别 

### HashSet和HashMap的区别

| *HashMap*                                   | *HashSet*                                                    |
| ------------------------------------------- | ------------------------------------------------------------ |
| HashMap实现了Map接口                        | HashSet实现了Set接口                                         |
| HashMap储存键值对                           | HashSet仅仅存储对象                                          |
| 使用put()方法将元素放入map中                | 使用add()方法将元素放入set中                                 |
| HashMap中使用键对象来计算hashcode值         | HashSet使用成员对象来计算hashcode值，对于两个对象来说hashcode可能相同，所以equals()方法用来判断对象的相等性，如果两个对象不同的话，那么返回false |
| HashMap比较快，因为是使用唯一的键来获取对象 | HashSet较HashMap来说比较慢                                   |

# 10 hashmap的问题

## 1 hashmap多线程操作同时调用put()方法后可能导致get()死循环,从而使CPU使用率达到100%,从而使服务器宕机. 

多个线程put的时候造成了某个key值Entry key List的死循环，然后再调用put方法操作的时候就会进入链表的死循环内。  

**如何产生的？**

 内部实现机制(在多线程环境且未作同步的情况下，对同一个HashMap做put操作可能导致两个或以上线程同时做rehash动作，就可能导致循环键表出现. 

## (1)正常的ReHash过程(hashmap产生死循环链表的操作)

抄了个图做个演示。

1. 我假设了我们的hash算法就是简单的用key mod 一下表的大小（也就是数组的长度）。
2. 最上面的是old hash 表，其中的Hash表的size=2, 所以key = 3, 7, 5，在mod 2以后都冲突在table1这里了。
3. 接下来的三个步骤是Hash表 resize成4，然后所有的 重新rehash的过程。

 

![img](https://images.cnblogs.com/cnblogs_com/andy-zhou/817145/o_HashMap001.jpg)

 

## (2)并发的Rehash过程

（1）假设我们有两个线程。我用红色和浅蓝色标注了一下。我们再回头看一下我们的 transfer代码中的这个细节：

```java
do {    Entry<K,V> next = e.next; // <--假设线程一执行到这里就被调度挂起了    int i = indexFor(e.hash, newCapacity);    e.next = newTable[i];    newTable[i] = e;    e = next;} while (e != null);
```

 而我们的线程二执行完成了。于是我们有下面的这个样子。

![img](https://images.cnblogs.com/cnblogs_com/andy-zhou/817145/o_HashMap002.jpg)

 

注意：因为Thread1的 e 指向了key(3)，而next指向了key(7)，其在线程二rehash后，指向了线程二重组后的链表。我们可以看到链表的顺序被反转后。

（2）线程一被调度回来执行。

1. 先是执行 newTalbe[i] = e。
2. 然后是e = next，导致了e指向了key(7)。
3. 而下一次循环的next = e.next导致了next指向了key(3)。

![img](https://images.cnblogs.com/cnblogs_com/andy-zhou/817145/o_HashMap003.jpg)

###  (3)再接下来

 线程一接着工作。把key(7)摘下来，放到newTable[i]的第一个，然后把e和next往下移。

![img](https://images.cnblogs.com/cnblogs_com/andy-zhou/817145/o_HashMap004.jpg)

###  （4）环形链接出现

e.next = newTable[i] 导致 key(3).next 指向了 key(7)。注意：此时的key(7).next 已经指向了key(3)， 环形链表就这样出现了。 

![img](https://images.cnblogs.com/cnblogs_com/andy-zhou/817145/o_HashMap005.jpg)

 于是，当我们的线程一调用到，HashTable.get(11)时，悲剧就出现了——Infinite Loop



## 3 多线程put的时候可能导致元素丢失

HashMap另外一个并发可能出现的问题是，可能产生元素丢失的现象。

考虑在多线程下put操作时，执行addEntry(hash, key, value, i)，如果有产生哈希碰撞，
导致两个线程得到同样的bucketIndex去存储，就可能会出现覆盖丢失的情况：

## 2 内存泄漏

## 3 头插法和尾插法

1.JDK8以前是头插法，JDK8后是尾插法

2.为什么要从头插法改成尾插法？
A.因为头插法会造成死链，[参考链接](https://blog.csdn.net/chenyiminnanjing/article/details/82706942)
B.JDK7用头插是考虑到了一个所谓的热点数据的点(新插入的数据可能会更早用到)，但这其实是个伪命题,因为JDK7中rehash的时候，旧链表迁移新链表的时候，如果在新表的数组索引位置相同，则链表元素会倒置(就是因为头插) 所以最后的结果 还是打乱了插入的顺序 所以总的来看支撑JDK7使用头插的这点原因也不足以支撑下去了 所以就干脆换成尾插 一举多得

# 11 jdk1.7与jdk1.8中HashMap区别

\1. **最重要的一点是底层结构不一样，1.7是数组+链表，1.8则是数组+链表+红黑树结构**;

\2. **jdk1.7中当哈希表为空时，会先调用inflateTable()初始化一个数组；而1.8则是直接调用resize()扩容**;

\3. **插入键值对的put方法的区别，1.8中会将节点插入到链表尾部，而1.7中是采用头插；**

\4. **jdk1.7中的hash函数对哈希值的计算直接使用key的hashCode值，而1.8中则是采用key的hashCode异或上key的hashCode进行无符号右移16位的结果，避免了只靠低位数据来计算哈希时导致的冲突，计算结果由高低位结合决定，使元素分布更均匀**；
\5. **扩容时1.8会保持原链表的顺序，而1.7会颠倒链表的顺序；而且1.8是在元素插入后检测是否需要扩容，1.7则是在元素插入前**；
\6. j**dk1.8是扩容时通过hash&cap==0将链表分散，无需改变hash值，而1.7是通过更新hashSeed来修改hash值达到分散的目的**；

\7. **扩容策略：1.7中是只要不小于阈值就直接扩容2倍；而1.8的扩容策略会更优化，当数组容量未达到64时，以2倍进行扩容，超过64之后若桶中元素个数不小于7就将链表转换为红黑树，但如果红黑树中的元素个数小于6就会还原为链表，当红黑树中元素不小于32的时候才会再次扩容**。



# 18 hashtable

# 19 concurrenthashmap原理

 https://www.cnblogs.com/zerotomax/p/8687425.html#go0 

 https://www.cnblogs.com/banjinbaijiu/p/9147434.html 

 

## 19.1 基础

其底层数据与HashMap的数据结构相同 。ConcurrentHashMap使用的分段锁技术。将ConcurrentHashMap容器的数据分段存储，每一段数据分配一个Segment（锁），当线程占用其中一个Segment时，其他线程可正常访问

### 19.1.1内部类

 ![img](https://images2018.cnblogs.com/blog/1394959/201806/1394959-20180606194916727-1631558402.png) 



　　说明：可以看到，ConcurrentHashMap的内部类非常的庞大，第二个图是在JDK1.8下增加的类，下面对其中主要的内部类进行分析和讲解。

 　1. **Node类**

　　Node类主要用于存储具体键值对，其子类有ForwardingNode、ReservationNode、TreeNode和TreeBin四个子类。四个子类具体的代码在之后的具体例子中进行分析讲解。

　　2. **Traverser类**

　　Traverser类主要用于遍历操作，其子类有BaseIterator、KeySpliterator、ValueSpliterator、EntrySpliterator四个类，BaseIterator用于遍历操作。KeySplitertor、ValueSpliterator、EntrySpliterator

则用于键、值、键值对的划分。

　　3. **CollectionView类**

　　CollectionView抽象类主要定义了视图操作，其子类KeySetView、ValueSetView、EntrySetView分别表示键视图、值视图、键值对视图。对视图均可以进行操作。

　　4. **Segment类**

　　Segment类在JDK1.8中与之前的版本的JDK作用存在很大的差别，JDK1.8下，其在普通的ConcurrentHashMap操作中已经没有失效，其在序列化与反序列化的时候会发挥作用。

　　5. **CounterCell**

　　CounterCell类主要用于对baseCount的计数。

### 19.1.2 成员

```java
 implements ConcurrentMap<K,V>, Serializable {
    private static final long serialVersionUID = 7249069246763182397L;
    // 表的最大容量
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    // 默认表的大小
    private static final int DEFAULT_CAPACITY = 16;
    // 最大数组大小
    static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    // 默认并发数
    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;
    // 装载因子
    private static final float LOAD_FACTOR = 0.75f;
    // 转化为红黑树的阈值
    static final int TREEIFY_THRESHOLD = 8;
    // 由红黑树转化为链表的阈值
    static final int UNTREEIFY_THRESHOLD = 6;
    // 转化为红黑树的表的最小容量
    static final int MIN_TREEIFY_CAPACITY = 64;
    // 每次进行转移的最小值
    private static final int MIN_TRANSFER_STRIDE = 16;
    // 生成sizeCtl所使用的bit位数
    private static int RESIZE_STAMP_BITS = 16;
    // 进行扩容所允许的最大线程数
    private static final int MAX_RESIZERS = (1 << (32 - RESIZE_STAMP_BITS)) - 1;
    // 记录sizeCtl中的大小所需要进行的偏移位数
    private static final int RESIZE_STAMP_SHIFT = 32 - RESIZE_STAMP_BITS;    
    // 一系列的标识
    static final int MOVED     = -1; // hash for forwarding nodes
    static final int TREEBIN   = -2; // hash for roots of trees
    static final int RESERVED  = -3; // hash for transient reservations
    static final int HASH_BITS = 0x7fffffff; // usable bits of normal node hash
    // 
    /** Number of CPUS, to place bounds on some sizings */
    // 获取可用的CPU个数
    static final int NCPU = Runtime.getRuntime().availableProcessors();
    // 
    /** For serialization compatibility. */
    // 进行序列化的属性
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("segments", Segment[].class),
        new ObjectStreamField("segmentMask", Integer.TYPE),
        new ObjectStreamField("segmentShift", Integer.TYPE)
    };
    
    // 表
    transient volatile Node<K,V>[] table;
    // 下一个表
    private transient volatile Node<K,V>[] nextTable;

    // 基本计数
    private transient volatile long baseCount;
    //
    // 对表初始化和扩容控制
     /**
     * 用来控制表初始化和扩容的，默认值为0，当在初始化的时候指定了大小，这会将这个大小保存在sizeCtl中，大小为数组的0.75
     * 当为负的时候，说明表正在初始化或扩张，
     *     -1表示初始化
     *     -(1+n) n:表示活动的扩张线程
     */
     
    private transient volatile int sizeCtl;
    

    // 扩容下另一个表的索引
    private transient volatile int transferIndex;

    // 旋转锁
    private transient volatile int cellsBusy;


    // counterCell表
    private transient volatile CounterCell[] counterCells;

    // views
    // 视图
    private transient KeySetView<K,V> keySet;
    private transient ValuesView<K,V> values;
    private transient EntrySetView<K,V> entrySet;
    
    // Unsafe mechanics
    private static final sun.misc.Unsafe U;
    private static final long SIZECTL;
    private static final long TRANSFERINDEX;
    private static final long BASECOUNT;
    private static final long CELLSBUSY;
    private static final long CELLVALUE;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        try {
            U = sun.misc.Unsafe.getUnsafe();
            Class<?> k = ConcurrentHashMap.class;
            SIZECTL = U.objectFieldOffset
                (k.getDeclaredField("sizeCtl"));
            TRANSFERINDEX = U.objectFieldOffset
                (k.getDeclaredField("transferIndex"));
            BASECOUNT = U.objectFieldOffset
                (k.getDeclaredField("baseCount"));
            CELLSBUSY = U.objectFieldOffset
                (k.getDeclaredField("cellsBusy"));
            Class<?> ck = CounterCell.class;
            CELLVALUE = U.objectFieldOffset
                (ck.getDeclaredField("value"));
            Class<?> ak = Node[].class;
            ABASE = U.arrayBaseOffset(ak);
            int scale = U.arrayIndexScale(ak);
            if ((scale & (scale - 1)) != 0)
                throw new Error("data type scale not a power of two");
            ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
 　　
```

说明：ConcurrentHashMap的属性很多，其中不少属性在HashMap中就已经介绍过，而对于**ConcurrentHashMap而言，添加了Unsafe实例，主要用于反射获取对象相应的字段。**

### 19.1.3构造函数

对于构造函数而言，会根据输入的initialCapacity的大小来确定一个最小的且大于等于initialCapacity大小的2的n次幂，如initialCapacity为15，则sizeCtl为16，若initialCapacity为16，则sizeCtl为

16。若initialCapacity大小超过了允许的最大值，则sizeCtl为最大值。值得注意的是，构造函数中的concurrencyLevel参数已经在JDK1.8中的意义发生了很大的变化，其并不代表所允许的并发数，

其只是用来确定sizeCtl大小，在JDK1.8中的并发控制都是针对具体的桶而言，即有多少个桶就可以允许多少个并发数



## 19.2 主要函数





```java



```



# 20 实现阻塞队列

# 21 实现 EmunHashMap





