# 1. Iterator与Enumerator区别

1. enumerator缺少remove方法
2. 迭代顺序不同，iterator从前到后
3. iterator支持fast-failed机制，enumerator不支持
4. enumerator迭代速度更快递
5. enumerator用于Vector、HashTable、java.io.SequenceInputStream

#  2. Iterator与ListIterator

1. 都支持fast-failed机制
2. ListIterator实现了Iterator接口，属性有cursor(下一个要遍历的元素索引),lastRest(上一个遍历元素的索引），方法有，hasNext(),next(),remove(),forEachRemainint()(遍历剩下的元素
3. ListIterator构造函数可以穿一个整型参数，表示从指定位置开始遍历；并且在Iterator（Itr）的基础上增加了方法hasPrevious(),nextIndex（），previousIndex(),previous() ,set(E e),add(E e);对应的功能是：返回是否有遍历过元素,返回下一个要遍历元素的索引，返回上一个遍历元素的索引，返回上一个遍历的元素，修改当前遍历元素的值，在当前遍历的元素后面加入一个元素

# 3. ArrayList与Vector

1. Vector与ArrayList一样，继承与List，也是通过数组实现的，不同的是它支持线程的同步 
2. Vector扩容是100%，ArrayList是50%
3. 迭代器AarryList用的是ListIterator，Vector用的是Enumerator

# 4.集合继承关系



 ![img](https://pics2.baidu.com/feed/91529822720e0cf3ac20a1bad10ce21abf09aa68.jpeg?token=a5abc7a8436f7611b13f0d54425522d5&s=6A84ED0E59CF40CA584D2C680300F073) 





Collection

　　**|___**List　　有序，可重复

​              |___AbstractArrayList

　　　　       |___ArrayList 　底层数据结构是数组，增删慢，查询快；线程不安全，效率高

　　　　       |___ AbstractSequentialList

​                              |___ LinkedList　  底层数据结构是链表，增删快，查询慢；线程不安全，效率高

　　　　       |___Vector　　 底层数据结构是数组，增删慢，查询快；线程安全，效率低

​						     |___Stack

​                    |___CopyOnWriteArrayList CopyOnWrite思想的list，有价值的

　　|___Set　　无序，不可重复

​              |___ SortedSet

​					|___NavigableSet

​                           |___TreeSet

​                           |___ConcurrentSkipListSet

​			  |___ AbstractSet

​                     |___EnumSet

　　　　       |___HashSet

​							|___LinkedHashSet

​                     |___TreeSet

​                     |___CopyOnWriteArraySet

​                     |___ConcurrentSkipListSet

​      |__Qeue

​            |__Deque

​                |__BlockingDeque

​                     |__LinkedBlockingDeque

​                |__LinkedList

​                |__ArrayDeque

​                |__ConcurrentLinkedDeque

​            |__BlockingQueue

​                  |__TransferQueue

​                  |__ArrayBlockingQue 常用

​                  |__DelayedWorkQueue

​                  |__BlockingDeque

​                  |__SynchronousQueue 常用

​                  |__DelayQueue

​                  |__LinkedBlockingQueue 常用

​                  |__PriorityBlockingQueue

​            |__AbstractQueue

​                 |__ArrayBlockingQueue

​                 |__LinkedTransferQueue

​				 |__SynchronousQueue

​				 |__PriorityQueue

​                 |__LinkedBlockingDeque

​                 |__DelayQueue

​                 |__LinkedBlockingQueue

​                 |__ConcurrentLinkedQueue

​                 |__PriorityBlockingQueue

​      |__AbstractCollection  

Map

​       |___SortedMap

​               |___NavigableMap

​                      |___TreeMap

​                      |___ConcurrentSkipListMap

​      |___ConcurrentMap

​              |___ConcurrentHashMap                 

​      |___AbstractMap

​              |___EnumMap

​              |___Hashtable

​                     |___Properties

​              |___IdentityHashMap

​              |___HashMap

​                     |___LinkedHashMap

　　       |___TreeMap

​              |___WeakHashMap

​              |___ConcurrentHashMap

​              |___ConcurrentSkipListMap

# 5. Collection



1. 继承与Iteral接口，接口仅包含iteraator spliterator forEach
2. 直接继承子类 

#  6.  Comparator与Comparable

​         **在《Effective Java》一书中，作者Joshua Bloch推荐大家在编写自定义类的时候尽可能的考虑实现一下Comparable接口，一旦实现了Comparable接口，它就可以跟许多泛型算法以及依赖于改接口的集合实现进行协作。你付出很小的努力就可以获得非常强大的功能**。
  **事实上，Java平台类库中的所有值类都实现了Comparable接口**。如果你正在编写一个值类，它具有非常明显的内在排序关系，比如按字母顺序、按数值顺序或者按年代顺序，那你就应该坚决考虑实现这个接口。
  compareTo方法不但允许进行简单的等同性进行比较，而且语序执行顺序比较，除此之外，它与Object的equals方法具有相似的特征，它还是一个泛型。类实现了Comparable接口，就表明它的实例具有内在的排序关系，为实现Comparable接口的对象数组进行排序就这么简单： Arrays.sort(a);是排序接口；若一个类实现了 Comparable 接口，就意味着 “该类支持排序”。而 Comparator 是比较器；我们若需要控制某个类的次序，可以建立一个 “该类的比较器” 来进行排序。
  前者应该比较固定，和一个具体类相绑定，而后者比较灵活，它可以被用于各个需要比较功能的类使用。可以说前者属于 “静态绑定”，而后者可以 “动态绑定”。
  我们不难发现：**Comparable 相当于 “内部比较器”，而 Comparator 相当于 “外部比较器”**

#  7.  CopyOnWriteArrayList

内部组合了ReentrakLock和另一个数组Object[] array;读不加锁；写入加锁，拷贝一份给array。

注意两件事情：

1. 减少扩容开销。根据实际需要，初始化CopyOnWriteMap的大小，避免写时CopyOnWriteMap扩容的开销。

　　2. 使用批量添加。因为每次添加，容器每次都会进行复制，所以减少添加次数，可以减少容器的复制次数。如使用上面代码里的addBlackList方法。

**CopyOnWrite的缺点：**

CopyOnWrite容器有很多优点，但是同时也存在两个问题，即内存占用问题和数据一致性问题。所以在开发的时候需要注意一下。

　　**内存占用问题**。因为CopyOnWrite的写时复制机制，所以在进行写操作的时候，内存里会同时驻扎两个对象的内存，旧的对象和新写入的对象（注意:在复制的时候只是复制容器里的引用，只是在写的时候会创建新对象添加到新容器里，而旧容器的对象还在使用，所以有两份对象内存）。如果这些对象占用的内存比较大，比如说200M左右，那么再写入100M数据进去，内存就会占用300M，那么这个时候很有可能造成频繁的Yong GC和Full GC。之前我们系统中使用了一个服务由于每晚使用CopyOnWrite机制更新大对象，造成了每晚15秒的Full GC，应用响应时间也随之变长。

　　针对内存占用问题，可以通过压缩容器中的元素的方法来减少大对象的内存消耗，比如，如果元素全是10进制的数字，可以考虑把它压缩成36进制或64进制。或者不使用CopyOnWrite容器，而使用其他的并发容器，如ConcurrentHashMap。

　　**数据一致性问题**。CopyOnWrite容器只能保证数据的最终一致性，不能保证数据的实时一致性。所以如果你希望写入的的数据，马上能读到，请不要使用CopyOnWrite容器。

# 8.AbstractSequentialList

​      实现序列访问的数据储存结构的提供了所需要的最小化的接口实现 。 重写了AbstractList中的iterator和listIterator方法，AbstractList中的这个方法便用于ArrayList这种底层顺序表实现的列表 

# 9. Queue

1. offer，add 区别：

一些队列有大小限制，因此如果想在一个满的队列中加入一个新项，多出的项就会被拒绝。

这时新的 offer 方法就可以起作用了。它不是对调用 add() 方法抛出一个 unchecked 异常，而只是得到由 offer() 返回的 false。

2. poll，remove 区别：

remove() 和 poll() 方法都是从队列中删除第一个元素。remove() 的行为与 Collection 接口的版本相似， 但是新的 poll() 方法在用空集合调用时不是抛出异常，只是返回 null。因此新的方法更适合容易出现异常条件的情况。

3. peek，element区别：

element() 和 peek() 用于在队列的头部查询元素。与 remove() 方法类似，在队列为空时， element() 抛出一个异常，而 peek() 返回 null。

# 10. Dueue

|             | **First Element (Head)**                                    |                                                             | **Last Element (Tail)**                                   |                                                           |
| ----------- | ----------------------------------------------------------- | ----------------------------------------------------------- | --------------------------------------------------------- | --------------------------------------------------------- |
|             | *Throws exception*                                          | *Special value*                                             | *Throws exception*                                        | *Special value*                                           |
| **Insert**  | [`addFirst(e)`](../../java/util/Deque.html#addFirst-E-)     | [`offerFirst(e)`](../../java/util/Deque.html#offerFirst-E-) | [`addLast(e)`](../../java/util/Deque.html#addLast-E-)     | [`offerLast(e)`](../../java/util/Deque.html#offerLast-E-) |
| **Remove**  | [`removeFirst()`](../../java/util/Deque.html#removeFirst--) | [`pollFirst()`](../../java/util/Deque.html#pollFirst--)     | [`removeLast()`](../../java/util/Deque.html#removeLast--) | [`pollLast()`](../../java/util/Deque.html#pollLast--)     |
| **Examine** | [`getFirst()`](../../java/util/Deque.html#getFirst--)       | [`peekFirst()`](../../java/util/Deque.html#peekFirst--)     | [`getLast()`](../../java/util/Deque.html#getLast--)       | [`peekLast()`](../../java/util/Deque.html#peekLast--)     |

对比于queue

| **`Queue` Method**                                  | **Equivalent `Deque` Method**                               |
| --------------------------------------------------- | ----------------------------------------------------------- |
| [`add(e)`](../../java/util/Queue.html#add-E-)       | [`addLast(e)`](../../java/util/Deque.html#addLast-E-)       |
| [`offer(e)`](../../java/util/Queue.html#offer-E-)   | [`offerLast(e)`](../../java/util/Deque.html#offerLast-E-)   |
| [`remove()`](../../java/util/Queue.html#remove--)   | [`removeFirst()`](../../java/util/Deque.html#removeFirst--) |
| [`poll()`](../../java/util/Queue.html#poll--)       | [`pollFirst()`](../../java/util/Deque.html#pollFirst--)     |
| [`element()`](../../java/util/Queue.html#element--) | [`getFirst()`](../../java/util/Deque.html#getFirst--)       |
| [`peek()`](../../java/util/Queue.html#peek--)       | [`peekFirst()`](../../java/util/Deque.html#peek--)          |

对比于stack

| **Stack Method**                                | **Equivalent `Deque` Method**                               |
| ----------------------------------------------- | ----------------------------------------------------------- |
| [`push(e)`](../../java/util/Deque.html#push-E-) | [`addFirst(e)`](../../java/util/Deque.html#addFirst-E-)     |
| [`pop()`](../../java/util/Deque.html#pop--)     | [`removeFirst()`](../../java/util/Deque.html#removeFirst--) |
| [`peek()`](../../java/util/Deque.html#peek--)   | [`peekFirst()`](../../java/util/Deque.html#peekFirst--)     |

```java
/** * Returns an iterator over the elements in this deque in reverse * sequential order.  The elements will be returned in order from * last (tail) to first (head). * * @return an iterator over the elements in this deque in reverse * sequence */
Iterator<E> descendingIterator();
```

# 11. LinkedList

```java
// get
Node<E> node(int index) {
        // assert isElementIndex(index);

        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }
```

# 12 NavigableSet

NavigableSet扩展了 SortedSet，具有了为给定搜索目标报告最接近匹配项的导航方法。方法 lower、floor、ceiling 和 higher 分别返回小于、小于等于、大于等于、大于给定元素的元素，如果不存在这样的元素，则返回 null。

 headSet 、tailSet .返回传入元素 小于、大于的视图

# 13 Map

**Map接口提供三个集合视图  Set keySet()  Collection values() Set entrySet()**

![è¿éåå¾çæè¿°](https://img-blog.csdn.net/20170317181610752?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanVzdGxvdmV5b3Vf/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

# 14.HashMap


见面试





# 15.LinkedHashMap

https://www.cnblogs.com/pypua/p/9973794.html     jdk1,7

https://segmentfault.com/a/1190000012964859         jdk1,8

HashMap和双向链表合二为一即是LinkedHashMap。所谓LinkedHashMap，其落脚点在HashMap，因此更准确地说，它是一个将所有Entry节点链入一个双向链表的HashMap。由于LinkedHashMap是HashMap的子类，所以LinkedHashMap自然会拥有HashMap的所有特性。比如，LinkedHashMap的元素存取过程基本与HashMap基本类似，只是在细节实现上稍有不同。当然，这是由LinkedHashMap本身的特性所决定的，因为它额外维护了一个双向链表用于保持迭代顺序。此外，LinkedHashMap可以很好的支持LRU算法。

HashMap是无序的，也就是说，迭代HashMap所得到的元素顺序并不是它们最初放置到HashMap的顺序。HashMap的这一缺点往往会造成诸多不便，因为在有些场景中，我们确需要用到一个可以保持插入顺序的Map。庆幸的是，JDK为我们解决了这个问题，它为HashMap提供了一个子类 —— LinkedHashMap。虽然LinkedHashMap增加了时间和空间上的开销，但是它通过维护一个**额外的双向链表保证了迭代顺序。特别地，该迭代顺序可以是插入顺序，也可以是访问顺序**。因此，根据链表中元素的顺序可以将LinkedHashMap分为：保持插入顺序的LinkedHashMap 和 保持访问顺序的LinkedHashMap，其中LinkedHashMap的默认实现是按插入顺序排序的。

![è¿éåå¾çæè¿°](https://img-blog.csdn.net/20170512160734275?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvanVzdGxvdmV5b3Vf/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

```java
 private final boolean accessOrder;  //true表示按照访问顺序迭代，false时表示按照插入顺序
```

LinkedHashMap采用的hash算法和HashMap相同，但是它重新定义了Entry。LinkedHashMap中的Entry增加了两个指针 before 和 after，它们分别用于维护双向链接列表。特别需要注意的是，next用于维护HashMap各个桶中Entry的连接顺序，before、after用于维护Entry插入的先后顺序的，源代码如下

```java 
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);      // 调用HashMap对应的构造函数
        accessOrder = false;            // 迭代顺序的默认值
    }
```

1. 链表的建立过程：

   在插入键值对节点时开始的，初始情况下，让 LinkedHashMap 的 head 和 tail 引用同时指向新节点，链表就算建立起来了。随后不断有新节点插入，通过将新节点接在 tail 引用指向节点的后面，即可实现链表的更新。Map 类型的集合类是通过 put(K,V) 方法插入键值对，**LinkedHashMap 本身并没有覆写父类的 put 方法**，而是直接使用了父类的实现。但在 HashMap 中，put 方法插入的是 HashMap 内部类 Node 类型的节点，该类型的节点并不具备与 LinkedHashMap 内部类 Entry 及其子类型节点组成链表的能力。那么，LinkedHashMap 是怎样建立链表的呢？

   ```java
   // HashMap 中实现
   public V put(K key, V value) {
       return putVal(hash(key), key, value, false, true);
   }
   
   // HashMap 中实现
   final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                  boolean evict) {
       Node<K,V>[] tab; Node<K,V> p; int n, i;
       if ((tab = table) == null || (n = tab.length) == 0) {...}
       // 通过节点 hash 定位节点所在的桶位置，并检测桶中是否包含节点引用
       if ((p = tab[i = (n - 1) & hash]) == null) {...}
       else {
           Node<K,V> e; K k;
           if (p.hash == hash &&
               ((k = p.key) == key || (key != null && key.equals(k))))
               e = p;
           else if (p instanceof TreeNode) {...}
           else {
               // 遍历链表，并统计链表长度
               for (int binCount = 0; ; ++binCount) {
                   // 未在单链表中找到要插入的节点，将新节点接在单链表的后面
                   if ((e = p.next) == null) {
                       p.next = newNode(hash, key, value, null);
                       if (binCount >= TREEIFY_THRESHOLD - 1) {...}
                       break;
                   }
                   // 插入的节点已经存在于单链表中
                   if (e.hash == hash &&
                       ((k = e.key) == key || (key != null && key.equals(k))))
                       break;
                   p = e;
               }
           }
           if (e != null) { // existing mapping for key
               V oldValue = e.value;
               if (!onlyIfAbsent || oldValue == null) {...}
               afterNodeAccess(e);    // 回调方法，后续说明
               return oldValue;
           }
       }
       ++modCount;
       if (++size > threshold) {...}
       afterNodeInsertion(evict);    // 回调方法，后续说明
       return null;
   }
   
   // HashMap 中实现
   Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
       return new Node<>(hash, key, value, next);
   }
   
   // LinkedHashMap 中覆写
   Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
       LinkedHashMap.Entry<K,V> p =
           new LinkedHashMap.Entry<K,V>(hash, key, value, e);
       // 将 Entry 接在双向链表的尾部
       linkNodeLast(p);
       return p;
   }
   
   // LinkedHashMap 中实现
   private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
       LinkedHashMap.Entry<K,V> last = tail;
       tail = p;
       // last 为 null，表明链表还未建立
       if (last == null)
           head = p;
       else {
           // 将新节点 p 接在链表尾部
           p.before = last;
           last.after = p;
       }
   }
   ```

2  链表节点的删除过程：

所以，在删除及节点后，回调方法 `afterNodeRemoval` 会被调用。LinkedHashMap 覆写该方法，并在该方法中完成了移除被删除节点的操作

```java
// HashMap 中实现
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ?
        null : e.value;
}

// HashMap 中实现
final Node<K,V> removeNode(int hash, Object key, Object value,
                           boolean matchValue, boolean movable) {
    Node<K,V>[] tab; Node<K,V> p; int n, index;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (p = tab[index = (n - 1) & hash]) != null) {
        Node<K,V> node = null, e; K k; V v;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
        else if ((e = p.next) != null) {
            if (p instanceof TreeNode) {...}
            else {
                // 遍历单链表，寻找要删除的节点，并赋值给 node 变量
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key ||
                         (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
            }
        }
        if (node != null && (!matchValue || (v = node.value) == value ||
                             (value != null && value.equals(v)))) {
            if (node instanceof TreeNode) {...}
            // 将要删除的节点从单链表中移除
            else if (node == p)
                tab[index] = node.next;
            else
                p.next = node.next;
            ++modCount;
            --size;
            afterNodeRemoval(node);    // 调用删除回调方法进行后续操作
            return node;
        }
    }
    return null;
}

// LinkedHashMap 中覆写
void afterNodeRemoval(Node<K,V> e) { // unlink
    LinkedHashMap.Entry<K,V> p =
        (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
    // 将 p 节点的前驱后后继引用置空
    p.before = p.after = null;
    // b 为 null，表明 p 是头节点
    if (b == null)
        head = a;
    else
        b.after = a;
    // a 为 null，表明 p 是尾节点
    if (a == null)
        tail = b;
    else
        a.before = b;
}
```

删除的过程并不复杂，上面这么多代码其实就做了三件事：

1. 根据 hash 定位到桶位置
2. 遍历链表或调用红黑树相关的删除方法
3. 从 LinkedHashMap 维护的双链表中移除要删除的节点

举个例子说明一下，假如我们要删除下图键值为 3 的节点。

[![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15166940421133.jpg)](http://www.coolblog.xyz/)

根据 hash 定位到该节点属于3号桶，然后在对3号桶保存的单链表进行遍历。找到要删除的节点后，先从单链表中移除该节点。如下：

[![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15166934395217.jpg)](http://www.coolblog.xyz/)

然后再双向链表中移除该节点：

[![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15166936737479.jpg)](http://www.coolblog.xyz/)

删除及相关修复过程并不复杂，结合上面的图片，大家应该很容易就能理解，这里就不多说了。

3 访问顺序的维护过程

LinkedHashMap 是按插入顺序维护链表。不过我们可以在初始化 LinkedHashMap，指定 accessOrder 参数为 true，即可让它按访问顺序维护链表。访问顺序的原理上并不复杂，当我们调用**get/getOrDefault/replace**等方法时，只需要将这些方法访问的节点移动到链表的尾部即可。

访问的节点移至链表尾部，淘汰时，淘汰头

```java
// LinkedHashMap 中覆写
public V get(Object key) {
    Node<K,V> e;
    if ((e = getNode(hash(key), key)) == null)
        return null;
    // 如果 accessOrder 为 true，则调用 afterNodeAccess 将被访问节点移动到链表最后
    if (accessOrder)
        afterNodeAccess(e);
    return e.value;
}

// LinkedHashMap 中覆写
void afterNodeAccess(Node<K,V> e) { // move node to last
    LinkedHashMap.Entry<K,V> last;
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p =
            (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        // 如果 b 为 null，表明 p 为头节点
        if (b == null)
            head = a;
        else
            b.after = a;
            
        if (a != null)
            a.before = b;
        /*
         * 这里存疑，父条件分支已经确保节点 e 不会是尾节点，
         * 那么 e.after 必然不会为 null，不知道 else 分支有什么作用
         */
        else
            last = b;
    
        if (last == null)
            head = p;
        else {
            // 将 p 接在链表的最后
            p.before = last;
            last.after = p;
        }
        tail = p;
        ++modCount;
    }
}
```

假设我们访问下图键值为3的节点，访问前结构为：

[![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15166338955699.jpg)](http://www.coolblog.xyz/)

访问后，键值为3的节点将会被移动到双向链表的最后位置，其前驱和后继也会跟着更新。访问后的结构如下：

[![img](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15167010301496.jpg)](http://www.coolblog.xyz/)

4 基于 LinkedHashMap 实现缓存

```java
void afterNodeInsertion(boolean evict) { // possibly remove eldest
    LinkedHashMap.Entry<K,V> first;
    // 根据条件判断是否移除最近最少被访问的节点
    if (evict && (first = head) != null && removeEldestEntry(first)) {
        K key = first.key;
        removeNode(hash(key), key, null, false, true);
    }
}

// 移除最近最少被访问条件之一，通过覆盖此方法可实现不同策略的缓存
protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
    return false;
}
```

测试代码如下：

```java
public class SimpleCacheTest {

    @Test
    public void test() throws Exception {
        SimpleCache<Integer, Integer> cache = new SimpleCache<>(3);

        for (int i = 0; i < 10; i++) {
            cache.save(i, i * i);
        }

        System.out.println("插入10个键值对后，缓存内容：");
        System.out.println(cache + "\n");

        System.out.println("访问键值为7的节点后，缓存内容：");
        cache.getOne(7);
        System.out.println(cache + "\n");

        System.out.println("插入键值为1的键值对后，缓存内容：");
        cache.save(1, 1);
        System.out.println(cache);
    }
}
```

测试结果如下：

[![151670457400271](https://blog-pictures.oss-cn-shanghai.aliyuncs.com/15167045740027.jpg)](http://www.coolblog.xyz/)

在测试代码中，设定缓存大小为3。在向缓存中插入10个键值对后，只有最后3个被保存下来了，其他的都被移除了。然后通过访问键值为7的节点，使得该节点被移到双向链表的最后位置。当我们再次插入一个键值对时，键值为7的节点就不会被移除。

# 16 TreeMap

红黑树：[http://www.tianxiaobo.com/2018/01/11/%E7%BA%A2%E9%BB%91%E6%A0%91%E8%AF%A6%E7%BB%86%E5%88%86%E6%9E%90/](http://www.tianxiaobo.com/2018/01/11/红黑树详细分析/)

TreeMap：

[http://www.tianxiaobo.com/2018/01/11/TreeMap%E6%BA%90%E7%A0%81%E5%88%86%E6%9E%90/](http://www.tianxiaobo.com/2018/01/11/TreeMap源码分析/)

1. 查找

`TreeMap`基于红黑树实现，而红黑树是一种自平衡二叉查找树，所以 TreeMap 的查找操作流程和二叉查找树一致。二叉树的查找流程是这样的，先将目标值和根节点的值进行比较，如果目标值小于根节点的值，则再和根节点的左孩子进行比较。如果目标值大于根节点的值，则继续和根节点的右孩子比较。在查找过程中，如果目标值和二叉树中的某个节点值相等，则返回 true，否则返回 false。TreeMap 查找和此类似，只不过在 TreeMap 中，节点（Entry）存储的是键值对``。在查找过程中，比较的是键的大小，返回的是值，如果没找到，则返回`null`。TreeMap 中的查找方法是`get`，具体实现在`getEntry`方法中，相关源码如下：

```java
public V get(Object key) {
    Entry<K,V> p = getEntry(key);
    return (p==null ? null : p.value);
}

final Entry<K,V> getEntry(Object key) {
    // Offload comparator-based version for sake of performance
    if (comparator != null)
        return getEntryUsingComparator(key);
    if (key == null)
        throw new NullPointerException();
    @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
    Entry<K,V> p = root;
    
    // 查找操作的核心逻辑就在这个 while 循环里
    while (p != null) {
        int cmp = k.compareTo(p.key);
        if (cmp < 0)
            p = p.left;
        else if (cmp > 0)
            p = p.right;
        else
            return p;
    }
    return null;
}
```

# 17 WeakHashMap

WeakHashMap 继承于AbstractMap,使用ReferenceQueue是一个队列，它会保存被GC回收的“弱键“，既然有WeakHashMap，那么有WeakHashSet吗？ java collections包是没有直接提供WeakHashSet的。我们可以通过`Collections.newSetFromMap(Map map)`方法可以将任何 Map包装成一个Set。看一下node的定义：

```java
/**
 * The entries in this hash table extend WeakReference, using its main ref
 * field as the key.
 */
private static class Entry<K,V> extends WeakReference<Object> implements Map.Entry<K,V> {
    V value;
    final int hash;
    Entry<K,V> next;
    
    Entry(Object key, V value,
          ReferenceQueue<Object> queue,
          int hash, Entry<K,V> next) {
        super(key, queue);
        this.value = value;
        this.hash  = hash;
        this.next  = next;
    }
```

 `super(key, queue);` 传入父类的仅仅是key，所以经过仔细阅读jdk源码开始部分分析后，得出结论，在WeakHashMap中，有jvm回收的，仅仅是Entry的key部分，所以一旦jvm强制回收，**当GC某个对象时，如果有此对象上还有弱引用与其关联，会将WeakReference对象与Reference类的pending引用关联起来，然后由Reference Handler线程将该插入ReferenceQueue队列**。那么这些key都会为null，再通过私有的`expungeStaleEntries`方法，把value也制null，并且把`size--`。

```java
private void expungeStaleEntries() {
    for (Object x; (x = queue.poll()) != null; ) {
        synchronized (queue) {
            @SuppressWarnings("unchecked")
                Entry<K,V> e = (Entry<K,V>) x;
            int i = indexFor(e.hash, table.length);

            Entry<K,V> prev = table[i];
            Entry<K,V> p = prev;
            while (p != null) {
                Entry<K,V> next = p.next;
                if (p == e) {
                    if (prev == e)
                        table[i] = next;
                    else
                        prev.next = next;
                    // Must not null out e.next;
                    // stale entries may be in use by a HashIterator
                    e.value = null; // Help GC
                    size--;
                    break;
                }
                prev = p;
                p = next;
            }
        }
    }
}
```

一般用做缓存，比如Tomcat的源码里，实现缓存时会用到WeakHashMap，在缓存系统中，使用WeakHashMap可以避免内存泄漏，但是使用WeakHashMap做缓存时要注意，如果只有它的key只有WeakHashMap本身在用，而在WeakHashMap之外没有对该key的强引用，那么GC时会回收这个key对应的entry。所以WeakHashMap不能用做主缓存，**合适的用法应该是用它做二级的内存缓存，即那么过期缓存数据或者低频缓存数据**

# 18 ConcurrentHashMap

见面试



# 19 BlockigQueque

|             | *Throws exception*                                           | *Special value*                                              | *Blocks*                                                     | *Times out*                                                  |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **Insert**  | [`add(e)`](../../../java/util/concurrent/BlockingQueue.html#add-E-) | [`offer(e)`](../../../java/util/concurrent/BlockingQueue.html#offer-E-) | [`put(e)`](../../../java/util/concurrent/BlockingQueue.html#put-E-) | [`offer(e, time, unit)`](../../../java/util/concurrent/BlockingQueue.html#offer-E-long-java.util.concurrent.TimeUnit-) |
| **Remove**  | [`remove()`](../../../java/util/concurrent/BlockingQueue.html#remove-java.lang.Object-) | [`poll()`](../../../java/util/concurrent/BlockingQueue.html#poll-long-java.util.concurrent.TimeUnit-) | [`take()`](../../../java/util/concurrent/BlockingQueue.html#take--) | [`poll(time, unit)`](../../../java/util/concurrent/BlockingQueue.html#poll-long-java.util.concurrent.TimeUnit-) |
| **Examine** | [`element()`](../../../java/util/Queue.html#element--)       | [`peek()`](../../../java/util/Queue.html#peek--)             | *not applicable*                                             | *not applicable*                                             |

```java
 class Producer implements Runnable {
   private final BlockingQueue queue;
   Producer(BlockingQueue q) { queue = q; }
   public void run() {
     try {
       while (true) { queue.put(produce()); }
     } catch (InterruptedException ex) { ... handle ...}
   }
   Object produce() { ... }
 }

 class Consumer implements Runnable {
   private final BlockingQueue queue;
   Consumer(BlockingQueue q) { queue = q; }
   public void run() {
     try {
       while (true) { consume(queue.take()); }
     } catch (InterruptedException ex) { ... handle ...}
   }
   void consume(Object x) { ... }
 }

 class Setup {
   void main() {
     BlockingQueue q = new SomeQueueImplementation();
     Producer p = new Producer(q);
     Consumer c1 = new Consumer(q);
     Consumer c2 = new Consumer(q);
     new Thread(p).start();
     new Thread(c1).start();
     new Thread(c2).start();
   }
 }
```



```java
int drainTo(Collection<? super E> c)
```

从该队列中删除所有可用元素并将它们添加到给定集合中。此操作可能比重复轮询此队列更有效。尝试将元素添加到集合c时遇到错误，可能导致在引发关联的异常时，元素既不在集合中，也不在两个集合中。尝试将队列自身排出会导致IllegalArgumentException。此外，如果在操作进行时修改了指定的集合，则此操作的行为是未定义的

# 20 ArrayBlockingQueue



线程安全，有界，支持公平和非公平锁，ArrayBlockingQueue 使用场景。

- 先进先出队列（队列头的是最先进队的元素；队列尾的是最后进队的元素）
- 有界队列（即初始化时指定的容量，就是队列最大的容量，不会出现扩容，容量满，则阻塞进队操作；容量空，则阻塞出队操作）
- 队列不支持空元素

```java
/** The queued items */
final Object[] items;

/** items index for next take, poll, peek or remove */
int takeIndex;

/** items index for next put, offer, or add */
int putIndex;

/** Number of elements in the queue */
int count;

/*
 * Concurrency control uses the classic two-condition algorithm
 * found in any textbook.
 */

/** Main lock guarding all access */
final ReentrantLock lock;

/** Condition for waiting takes */
private final Condition notEmpty;

/** Condition for waiting puts */
private final Condition notFull;
```

```java

// put和take方法相应中断
public void put(E e) throws InterruptedException {
    checkNotNull(e);
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == items.length)
            notFull.await();
        enqueue(e);
    } finally {
        lock.unlock();
    }
}
    public E take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (count == 0)
                notEmpty.await();
            return dequeue();
        } finally {
            lock.unlock();
        }
    }


```

# 21 LinkedBlockingQueue

LinkedBlockingQueue不同于ArrayBlockingQueue，它如果不指定容量，默认为`Integer.MAX_VALUE`，也就是无界队列。

```java
/** The capacity bound, or Integer.MAX_VALUE if none */
private final int capacity;

/** Current number of elements */ // 原子的
private final AtomicInteger count = new AtomicInteger();

/**
 * Head of linked list.
 * Invariant: head.item == null
 */
transient Node<E> head;

/**
 * Tail of linked list.
 * Invariant: last.next == null
 */
private transient Node<E> last;

/** Lock held by take, poll, etc */
private final ReentrantLock takeLock = new ReentrantLock();

/** Wait queue for waiting takes */
private final Condition notEmpty = takeLock.newCondition();

/** Lock held by put, offer, etc */
private final ReentrantLock putLock = new ReentrantLock();

/** Wait queue for waiting puts */
private final Condition notFull = putLock.newCondition();




/**
     * Inserts the specified element at the tail of this queue, waiting if
     * necessary for space to become available.
     */
    public void put(E e) throws InterruptedException {
        if (e == null) throw new NullPointerException();
        // Note: convention in all put/take/etc is to preset local var
        // holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly(); //加 putLock 锁
        try {
			//当队列满时，调用notFull.await()方法释放锁，陷入等待状态。
			//有两种情况会激活该线程
			//第一、 某个put线程添加元素后，发现队列有空余，就调用notFull.signal()方法激活阻塞线程
			//第二、 take线程取元素时，发现队列已满。则其取出元素后，也会调用notFull.signal()方法激活阻塞线程
            while (count.get() == capacity) { 
                    notFull.await();
            }
			// 把元素 e 添加到队列中（队尾）
            enqueue(e);
            // 将count自增，并且将自增前的值保存到变量c中（注意getAndIncrement返回之前旧值！）
            // 这里保存自增前的值，有两层作用，1是紧接着的下面这个判断激活notFull-Condtion
            // 第2个作用是判断其大小是否为0，如果为0，则代表有notEmpty.wait()的线程，则激活之
            c = count.getAndIncrement();
			//发现队列未满，调用notFull.signal()激活阻塞的put线程（可能存在）
            if (c + 1 < capacity)
                notFull.signal();
        } finally {
            putLock.unlock();
        }
        
        
        //为什么只有=0时？因为take方法会自己检查，自己发empty 
        if (c == 0)
			//队列空，说明已经有take线程陷入阻塞，故调用signalNotEmpty激活阻塞的take线程
            signalNotEmpty();
    }
 public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
                while (count.get() == 0) {
                    notEmpty.await();
                }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1)
                notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
     	//为什么只有=capacity时？因为put方法会自己检查，自己发NotFull
        if (c == capacity)
            signalNotFull();
        return x;
    }

```

```java
private void enqueue(Node<E> node) {
    // assert putLock.isHeldByCurrentThread();
    // assert last.next == null;
    last = last.next = node;
}

// 有个空的头结点，链表长度大于1
private E dequeue() {
    // assert takeLock.isHeldByCurrentThread();
    // assert head.item == null;
    Node<E> h = head;
    Node<E> first = h.next;
    h.next = h; // help GC
    head = first;
    E x = first.item;
    first.item = null;
    return x;
}
```

# 22 ArrayBlockingQueue与LinkedBlockingQueue单/双锁实现，性能对比

1. ArrayBlockingQueue可以使用双锁？但为什么没有？ArrayBlockingQueue是定长的，当putIndex==length时，putIndex会重置为0，**这样入队和出队的index可能是同一个，在这种情况下还需要考虑锁之间的通讯**，**不考虑的话有并发问题，参考读写锁**。LinkedBlockingQueue操作的链表的头和尾，不存在竞争同一个index

2. 性能对比：

   LinkedBlockingQueue是基于链表的，所以生产者每次放入元素会构造一个新节点对象，在大量并发的情况下可能会对系统GC造成一定影响，而ArrayBlockingQueue不存在这种情况。LinkedBlockingQueue同样是使用通知模式来实现。相对于ArrayBlockingQueue，LinkedBlockingQueue生产者和消费者分别使用两把重入锁来实现同步，所以可以提高系统的并发度。

# 23  SynchronousQueue 同步队列

https://blog.csdn.net/demon7552003/article/details/92080415

https://blog.csdn.net/bluetjs/article/details/53000564

SynchronousQueue是这样一种阻塞队列，其中每个 put 必须等待一个 take，反之亦然。同步队列没有任何内部容量，甚至连一个队列的容量都没有。有了插入线程和移除线程，元素很快就从插入线程移交给移除线程。也就是说这更像是一种信道（管道），资源从一个方向快速传递到另一方 向。需要特别说明的是，尽管元素在SynchronousQueue 内部不会“停留”，但是并不意味之SynchronousQueue 内部没有队列。实际上SynchronousQueue 维护者线程队列，也就是插入线程或者移除线程在不同时存在的时候就会有线程队列。既然有队列，同样就有公平性和非公平性特性，公平性保证正在等待的插入线 程或者移除线程以FIFO的顺序传递资源 


1. 它一种阻塞队列，其中每个 put 必须等待一个 take，反之亦然。同步队列没有任何内部容量，甚至连一个队列的容量都没有。 
2. 它是线程安全的，是阻塞的。 \
3. 不允许使用 null 元素。 
4. 公平排序策略是指调用put的线程之间，或take的线程之间。公平排序策略可以查考ArrayBlockingQueue中的公平策略。 
5. SynchronousQueue的以下方法： 
       \* iterator() 永远返回空，因为里面没东西。 
        \* peek() 永远返回null。 
        \* put() 往queue放进去一个element以后就一直wait直到有其他thread进来把这个element取走。 
       \* offer() 往queue里放一个element后立即返回，如果碰巧这个element被另一个thread取走了，offer方法返回true，认为offer成功；否则返回false。 
        \* offer(2000, TimeUnit.SECONDS) 往queue里放一个element但是等待指定的时间后才返回，返回的逻辑和offer()方法一样。 
        \* take() 取出并且remove掉queue里的element（认为是在queue里的。。。），取不到东西他会一直等。 
        \* poll() 取出并且remove掉queue里的element（认为是在queue里的。。。），只有到碰巧另外一个线程正在往queue里offer数据或者put数据的时候，该方法才会取到东西。否则立即返回null。 
        \* poll(2000, TimeUnit.SECONDS) 等待指定的时间然后取出并且remove掉queue里的element,其实就是再等其他的thread来往里塞。 
        \* isEmpty()永远是true。 
       \* remainingCapacity() 永远是0。 
       \* remove()和removeAll() 永远是false。
6.   不能在同步队列上进行 peek，因为仅在试图要取得元素时，该元素才存在；



由于SynchronousQueue的支持公平策略和非公平策略，所以底层可能两种数据结构：队列（实现公平策略）和栈（实现非公平策略），队列与栈都是通过链表来实现的。具体的数据结构如下

![img](https://blog.csdn.net/bluetjs/article/details/53000564)　　说明：数据结构有两种类型，栈和队列；栈有一个头结点，队列有一个头结点和尾结点；栈用于实现非公平策略，队列用于实现公平策略。

```java
   static final class TransferStack<E> extends Transferer<E> {
        /*
         * This extends Scherer-Scott dual stack algorithm, differing,
         * among other ways, by using "covering" nodes rather than
         * bit-marked pointers: Fulfilling operations push on marker
         * nodes (with FULFILLING bit set in mode) to reserve a spot
         * to match a waiting node.
         */

        /* Modes for SNodes, ORed together in node fields */
        /** Node represents an unfulfilled consumer */
        // 表示消费数据的消费者
        static final int REQUEST    = 0;
        /** Node represents an unfulfilled producer */
        // 表示生产数据的生产者
        static final int DATA       = 1;
        /** Node is fulfilling another unfulfilled DATA or REQUEST */
        // 表示匹配另一个生产者或消费者
        static final int FULFILLING = 2;
        
        /** The head (top) of the stack */
        // 头结点
        volatile SNode head;
    }
```

sdaaaaassssssssssssssssssss

# 22  ConcurrentLinkedQueue

https://www.cnblogs.com/sunshine-2015/p/6067709.html

1. 初始化

![img](https://upload-images.jianshu.io/upload_images/2615789-a3dbf8f54bb3452e.png?imageMogr2/auto-orient/strip|imageView2/2/w/176/format/webp)



2.

```java
/**
返回p的后续节点，如果p.next已链接到self，则返回head节点，只有使用现在已从列表中删除的过时指针遍历时，此值才为true。
*/
final Node<E> succ(Node<E> p) {
    Node<E> next = p.next;
    return (p == next) ? head : next;
}
```

入队操作主要做两件事情，第一是将入队节点设置成当前队列尾节点的下一个节点。第二是更新tail节点，如果tail节点的next节点不为空，则将入队节点设置成tail节点，如果tail节点的next节点为空，则将入队节点设置成tail的next节点，所以tail节点不总是尾节点，理解这一点很重要。

```java
public boolean add(E e) {
    return offer(e);
}
 
public boolean offer(E e) {
    // 如果e为null，则直接抛出NullPointerException异常
    checkNotNull(e);
    // 创建入队节点
    final Node<E> newNode = new Node<E>(e);
 
    // 循环CAS直到入队成功
    // 1、根据tail节点定位出尾节点（last node）；2、将新节点置为尾节点的下一个节点；3、casTail更新尾节点
    for (Node<E> t = tail, p = t;;) {
        // p用来表示队列的尾节点，初始情况下等于tail节点
        // q是p的next节点
        Node<E> q = p.next;
        // 判断p是不是尾节点，tail节点不一定是尾节点，判断是不是尾节点的依据是该节点的next是不是null
        // 如果p是尾节点
        if (q == null) {
            // p is last node
            // 设置p节点的下一个节点为新节点，设置成功则casNext返回true；否则返回false，说明有其他线程更新过尾节点
            if (p.casNext(null, newNode)) {
                // Successful CAS is the linearization point
                // for e to become an element of this queue,
                // and for newNode to become "live".
                // 如果p != t，则将入队节点设置成tail节点，更新失败了也没关系，因为失败了表示有其他线程成功更新了tail节点
                if (p != t) // hop two nodes at a time
                    casTail(t, newNode);  // Failure is OK.
                return true;
            }
            // Lost CAS race to another thread; re-read next
        }
        // 多线程操作时候，由于poll时候会把旧的head变为自引用，然后将head的next设置为新的head
        // 所以这里需要重新找新的head，因为新的head后面的节点才是激活的节点
        else if (p == q)
            // We have fallen off list.  If tail is unchanged, it
            // will also be off-list, in which case we need to
            // jump to head, from which all live nodes are always
            // reachable.  Else the new tail is a better bet.
            p = (t != (t = tail)) ? t : head;
        // 寻找尾节点
        else
            // Check for tail updates after two hops.
            p = (p != t && t != (t = tail)) ? t : q;
    }
}
```



# 23 isEmpty()与size

size，链表实现的需要每次计算，效率非常低、所以尽量要避免用size而改用isEmpty().



# 24 PriorityQueue

https://blog.csdn.net/jaybill/article/details/89762556

https://blog.csdn.net/weixin_37373020/article/details/93577529?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase

1.1 如果**数组长度**小于64，直接扩容1倍
1.2 否则扩容0.5倍

1.3 默认容量为11

```java
public boolean add(E e) {

    return offer(e);

}

public boolean offer(E e) {

    if (e == null)

        throw new NullPointerException();

    modCount++;

    int i = size;

    if (i >= queue.length)

        grow(i + 1);//扩容

    siftUp(i, e);//堆有序化过滤

    size = i + 1;

    return true;

}

private void grow(int minCapacity) {

    int oldCapacity = queue.length;

    // Double size if small; else grow by 50%

    int newCapacity = oldCapacity + ((oldCapacity < 64) ?

                                     (oldCapacity + 2) :

                                     (oldCapacity >> 1));

    // overflow-conscious code

    if (newCapacity - MAX_ARRAY_SIZE > 0)

        newCapacity = hugeCapacity(minCapacity);

    queue = Arrays.copyOf(queue, newCapacity);

}
```
别的不用管，就是两个极其重要的操作：

grow：数组扩张

siftUp：维护堆性质

第二个一会再说，先看扩容规则，能十分清晰地看出大概是：

如果原数组capacity<64，那么 cap=2*cap+2；否则cap=1.5cap。

堆化是最重要的操作，这里面我们可以简单的称siftUp为“上移”操作（算法导论称上浮）。

先观察代码：

```java
private void siftUp(int k, E x) {
        if (comparator != null)
            siftUpUsingComparator(k, x, queue, comparator);//以此函数为例说明过程，下面同
        else
            siftUpComparable(k, x, queue);
    }
    private static <T> void siftUpUsingComparator(
        int k, T x, Object[] es, Comparator<? super T> cmp) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;//父节点下标：（k-1）/2
            Object e = es[parent];//交换上浮
            if (cmp.compare(x, (T) e) >= 0)
                break;//默认小顶堆，不满足交换条件就退出
            es[k] = e;
            k = parent;
        }
        es[k] = x;
    }
```



移除操作


```java
public E poll() {
    final Object[] es;
    final E result;
 
    if ((result = (E) ((es = queue)[0])) != null) {
        modCount++;
        final int n;
        final E x = (E) es[(n = --size)];//弹出下标为0的元素，末尾元素放置到0,并且开始下移
        es[n] = null;//尾元素置为null，size减小1
        if (n > 0) {
            final Comparator<? super E> cmp;
            if ((cmp = comparator) == null)
                siftDownComparable(0, x, es, n);//下移操作
            else
                siftDownUsingComparator(0, x, es, n, cmp);
        }
    }
    return result;
}


    private static <T> void siftDownUsingComparator(
        int k, T x, Object[] es, int n, Comparator<? super T> cmp) {
        // assert n > 0;
        int half = n >>> 1;//为了保证k（作为父节点）不会出现在最后一层（叶子），需要k<size/2
        while (k < half) {
            int child = (k << 1) + 1;//左孩子
            Object c = es[child];
            int right = child + 1;
            if (right < n && cmp.compare((T) c, (T) es[right]) > 0)
                c = es[child = right];//选取左右两个孩子中较小的那个
            if (cmp.compare(x, (T) c) <= 0)
                break;//如果发现已经父节点小于两个孩子，那么退出，否则交换
            es[k] = c;
            k = child;
        }
        es[k] = x;
    }
```






最后看一个课本上的“堆化”操作。

```java
private void heapify() {
    final Object[] es = queue;
    int n = size, i = (n >>> 1) - 1;
    final Comparator<? super E> cmp;
    if ((cmp = comparator) == null)
        for (; i >= 0; i--)//从倒数第二层开始每个节点都下“下移”一次
            siftDownComparable(i, (E) es[i], es, n);
    else
        for (; i >= 0; i--)
            siftDownUsingComparator(i, (E) es[i], es, n, cmp);
}
```
# 25 有界队列与无界队列

###### 1. 有界队列

- ArrayBlockingQueue是最典型的的有界队列，其内部以final的数组保存数据，数组的大小就决定了队列的边界，所以我们在创建ArrayBlockingQueue时，都要指定容量，如

```
public ArrayBlockingQueue(int capacity, boolean fair) 
```

- LinkedBlockingQueue，容易被误解为无边界，但其实其行为和内部代码都是基于有界的逻辑实现的，只不过如果我们没有在创建队列时就指定容量，那么其容量限制就自动被设置为Integer.MAX_VALUE，成为了无界队列。
- SynchronousQueue，这是一个非常奇葩的队列实现，每个删除操作都要等待插入操作，反之每个插入操作也都要等待删除动作。那么这个队列的容量是多少呢？是1吗？其实不是的，其内部容量是0。

###### 2.无界队列

- PriorityBlockingQueue是无边界的优先队列，虽然严格意义上来讲，其大小总归是要受系统资源影响。
- DelayedQueue和LinkedTransferQueue同样是无边界的队列。对于无边界的队列，有一个自然的结果，就是put操作永远也不会发生其他BlockingQueue的那种等待情况。

###### 3 有界队列使用场景

以LinkedBlockingQueue、ArrayBlockingQueue和SynchronousQueue为例，根据需求可以从很多方面考量：

- 考虑应用场景中对队列边界的要求。ArrayBlockingQueue是有明确的容量限制的，而LinkedBlockingQueue则取决于我们是否在创建时指定，SynchronousQueue则干脆不能缓存任何元素。
- 从空间利用角度，数组结构的ArrayBlockingQueue要比LinkedBlockingQueue紧凑，因为其不需要创建所谓节点，但是其初始分配阶段就需要一段连续的空间，所以初始内存需求更大。
- 通用场景中，LinkedBlockingQueue的吞吐量一般优于ArrayBlockingQueue，因为它实现了更加细粒度的锁操作。
- ArrayBlockingQueue实现比较简单，性能更好预测，属于表现稳定的“选手”。
- 如果需要实现的是两个线程之间接力性（handoff）的场景，可能会选择CountDownLatch，但是SynchronousQueue也是完美符合这种场景的，而且线程间协调和数据传输统一起来，代码更加规范。
- 可能令人意外的是，**很多时候SynchronousQueue的性能表现，往往大大超过其他实现，尤其是在队列元素较小的场景。**

