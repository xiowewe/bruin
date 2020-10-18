#### 实践学习笔记

##### String类型怎么保存数据

> 缓存一亿张图片信息，图片ID（key）、图片存储对象ID（value）均为 8 字节的Long类型，总共占用6.4GB平均每张图片缓存占用 64 字节，为啥缓存内容 16 字节却占用 64 字节？

String 类型还需要额外的内存空间记录数据长度、空间使用等信息，这些信息也叫作元数据。当实际保存的数据较小时，元数据的空间开销就显得比较大了，过于浪费空间。

当你保存 64 位有符号整数时，String 类型会把它保存为一个 8 字节的 Long 类型整数，这种保存方式通常叫作 **int 编码方式**。但是，当你保存的数据中包含字符时，String 类型就会用简单动态字符串（Simple Dynamic String，SDS）结构体来保存。

- 简单动态字符串SDS

  <img src="/Users/see-bruin/IdeaProjects/bruin/pi/database/redis/images/SDS.jpg" style="zoom:50%;" />

  1. buf：字节数组，保存实际数据。为了表示字节数组的结束，Redis 会自动在数组最后加一个“\0”，这就会额外占用 1 个字节的开销。
  2. len：占 4 个字节，表示 buf 的已用长度。
  3. alloc：也占个 4 字节，表示 buf 的实际分配长度，一般大于 len。

- RedisObject结构体

  因为 Redis 的数据类型有很多，而且，不同数据类型都有些相同的元数据要记录（比如最后一次访问的时间、被引用的次数等），所以Redis 会用一个 RedisObject 结构体来统一记录这些元数据，同时指向实际数据。一个 RedisObject 包含了 8 字节的元数据和一个 8 字节指针。

  为了节省内存空间，Redis 还对 Long 类型整数和 SDS 的内存布局做了专门的设计。

  - 当保存的是 Long 类型整数时，RedisObject 中的指针就直接赋值为整数数据了，节省了指针的开销

  - 当保存的是字符串数据，并且字符串小于等于 44 字节时，RedisObject 中的元数据、指针和 SDS 是一块连续的内存区域，这样就可以避免内存碎片。称为 **embstr 编码方式**。

  - 当保存的是字符串数据，并且大于 44 字节时，SDS 的数据量就开始变多了，Redis 就不再把 SDS 和 RedisObject 布局在一起了，而是会给 SDS 分配独立的空间，并用指针指向 SDS 结构。这种布局方式被称为 **raw 编码模式**。

    <img src="/Users/see-bruin/IdeaProjects/bruin/pi/database/redis/images/RedisObject.jpg" style="zoom:67%;" />

> 上面图片缓存直接用 int 编码的 RedisObject 保存。每个 int 编码的 RedisObject 元数据部分占 8 字节，指针部分被直接赋值为 8 字节的整数了。两个ID就占用 32 字节，还有32字节呢？

- 全局哈希表

  Redis 会使用一个全局哈希表保存所有键值对，哈希表的每一项是一个 dictEntry 的结构体，用来指向一个键值对。dictEntry 结构中有三个 8 字节的指针，分别指向 key、value 以 next ，三个指针共 24 字节。

  Redis 使用的内存分配库 **jemalloc** ，jemalloc 在分配内存时，会根据我们申请的字节数 N，找一个比 N 大，但是最接近 N 的 2 的幂次数作为分配的空间，这样可以减少频繁分配的次数。例如：如果你申请 6 字节空间，jemalloc 实际会分配 8 字节空间；如果你申请 24 字节空间，jemalloc 则会分配 32 字节。

> 使用 String 类型保存时，却需要 64 字节的内存空间，有 48 字节都没有用于保存实际的数据。



##### 什么数据结构可以节省内存

Redis 有一种底层数据结构，叫压缩列表（ziplist），这是一种非常节省内存的结构。表头有三个字段 **zlbytes、zltail 和 zllen**，分别表示列表长度、列表尾的偏移量，以及列表中的 entry 个数。压缩列表尾还有一个 **zlend**，表示列表结束。

![](/Users/see-bruin/IdeaProjects/bruin/pi/database/redis/images/压缩列表.jpg)

压缩列表之所以能节省内存，就在于它是用一系列连续的 entry 保存数据，节省指针占用的空间。每个 entry 的元数据包括下面几部分。prev_len，表示前一个 entry 的长度。

- prev_len，表示前一个 entry 的长度。prev_len 有两种取值情况：1 字节或 5 字节。取值 1 字节时，表示上一个 entry 的长度小于 254 字节。虽然 1 字节的值能表示的数值范围是 0 到 255，但是压缩列表中 zlend 的取值默认是 255，因此，就默认用 255 表示整个压缩列表的结束，其他表示长度的地方就不能再用 255 这个值了。所以，当上一个 entry 长度小于 254 字节时，prev_len 取值为 1 字节，否则，就取值为 5 字节。
- len：表示自身长度，4 字节；
- encoding：表示编码方式，1 字节；
- content：保存实际数据。

> 压缩列表虽然节省空间，但是用集合类型保存键值对时，一个键对应了一个集合的数据，但是在我们的场景中，一个图片 ID 只对应一个图片的存储对象 ID，我们该怎么用集合类型呢？
>
> 可查看Redis核心技术与实战 —11

##### Redis集合类型数据统计

四个移动应用业务场景，学习几种集合类型数据统计：

- 在移动应用中，需要统计每天的新增用户数和第二天的留存用户数；
- 在电商网站的商品评论中，需要统计评论列表中的最新评论；
- 在签到打卡中，需要统计一个月内连续打卡的用户数；
- 在网页访问记录中，需要统计独立访客（Unique Visitor，UV）量。

###### 聚合统计

> 在移动应用中，需要统计每天的新增用户数和第二天的留存用户数；

所谓的聚合统计，就是指统计多个集合元素的聚合结果，包括：统计多个集合的共有元素（交集统计）；把两个集合相比，统计其中一个集合独有的元素（差集统计）；统计多个集合的所有元素（并集统计）。

我们可以直接使用 Set 类型，key是“user:login”，value是登陆用户的ID

```java
//user:login 记录累计登录用户，user:login:+时间戳记录每天登陆的用户
sadd user:login 1001 1002 1003
sadd user:login:20201016 1001 1002 1003
  
sadd user:login 1002 1004 1005
sadd user:login:20201017 1002 1004 1005
  
//并集:user:login 和 user:login:20201017 的并集存储在user:login（累计登录用户）
sunionstore user:login user:login user:login:20201017
  
//差集：user:login 和 user:login:20201017 的差集存储在user:login:new（17号新增登录用户）
sdiffstore user:login:new user:login user:login:20201017
  
//交集：user:login:20201016 和 user:login:20201017 的差集存储在user:login:again（留存用户）
sinterstore user:login:again user:login:20201016 user:login:20201017
```

Set 的差集、并集和交集的计算复杂度较高，在数据量较大的情况下，如果直接执行这些计算，会导致 Redis 实例阻塞。所以，我给你分享一个小建议：你可以从主从集群中选择一个从库，让它专门负责聚合计算，或者是把数据读取到客户端，在客户端来完成聚合统计，这样就可以规避阻塞主库实例和其他从库实例的风险了。sunionstore、sdiffstore、sinterstore 由于存在store操作从库无法完成，在从库操作时先sunion、sdiff、sinter 命令做数据集合，在sadd命令进行储存

###### 排序统计

> 在电商网站的商品评论中，需要统计评论列表中的最新评论

最新评论列表包含了所有评论中的最新留言，这就要求集合类型能对元素保序。在 Redis 常用的 4 个集合类型中（List、Hash、Set、Sorted Set），List 和 Sorted Set 就属于有序集合。

List 是按照元素进入 List 的顺序进行排序的，而 Sorted Set 可以根据元素的权重来排序，我们可以自己来决定每个元素的权重值。比如说，我们可以根据元素插入 Sorted Set 的时间确定权重值，先插入的元素权重小，后插入的元素权重大。

- List 实现

  ```java
  //lpush 添加元素
  lpush discuss a b c d e f
  
  //每页3条：a b c
  lrange 0 2
  //第二页： d e f
  lrange 3 5
    
  //增加一条
  lpush discuss g
  //此时现实的第二页： e f g
  lrange 3 5
  ```

  List 是通过元素在 List 中的位置来排序的，当有一个新元素插入时，原先的元素在 List 中的位置都后移了一位，所以，对比新元素插入前后，List 相同位置上的元素就会发生变化

- Sorted Set 实现

  ```java
  //zadd 添加元素并设置权重，按照权重排序
  zadd discuss a 0
  ...
  zadd discuss f 5
  
  //查询排序数据 d e f
  zrangebyscope discuss 3 5
    
  //增加一条
  zadd discuss g 6
    
  //查询排序数据 d e f
  zrangebyscope discuss 3 5
  ```

  按照权重排序就不会因为新增数据而调整原有的数据位置

###### 二值状态统计

###### 基数统计