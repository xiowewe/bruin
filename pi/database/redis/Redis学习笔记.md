#### Redis为什么快

1. 基于内存，绝大部分请求是纯粹的内存操作
2. 数据结构简单，对数据操作也简单
3. 采用单线程，避免了不必要的上下文切换和竞争条件
4. 使用多路I/O复用模型，非阻塞IO
5. 使用底层模型不同，它们之间底层实现方式以及与客户端之间通信的应用协议不一样

##### 数据结构

Redis支持字符串（String），散列（Hash），列表（List），集合（Set），有序集合（Sorted Set或者是ZSet），支撑这些数据类型的底层数据数据结构有6种：简单动态字符串、双向链表、压缩列表、哈希表、跳表、整数数组。

- 全局哈希表

  Redis为了实现从键到值的快速访问，Redis 使用了一个哈希表来保存所有键值对。其实就是一个数组，数组的每个元素称为一个哈希桶。哈希桶中的 entry 元素中保存了*key和*value指针，分别指向了实际的键和值。

- 哈希冲突和rehash

  Redis 解决哈希冲突的方式，就是链式哈希。就是指同一个哈希桶中的多个元素用一个链表来保存，它们之间依次用指针连接。rehash增加现有的哈希桶数量，让逐渐增多的 entry 元素能在更多的桶之间分散保存，减少单个桶中的元素数量，从而减少单个桶中的冲突。为了避免大量的数据拷贝造成阻塞，Redis 采用了**渐进式 rehash**

1. 简单动态字符串（String）O(N)

2. 双向链表（List）O(N)

3. 压缩列表（List、Hash、Sorted Set）O(N)

   实际上类似于一个数组，数组中的每一个元素都对应保存一个数据。和数组不同的是，压缩列表在表头有三个字段 zlbytes、zltail 和 zllen，分别表示列表长度、列表尾的偏移量和列表中的 entry 个数；压缩列表在表尾还有一个 zlend，表示列表结束。

4. 哈希表（Hash、Set）O(1)

5. 跳表（Sorted Set）O(logN)

6. 整数数组（Set）O(N)

##### 单线程

Redis 是单线程，主要是指 Redis 的网络 IO 和键值对读写是由一个线程来完成的，这也是 Redis 对外提供键值存储服务的主要流程。但 Redis 的其他功能，比如持久化、异步删除、集群数据同步等，其实是由额外的线程执行的。Redis 6.0 中提出了**多线程模型**

##### 多路复用

多路复用的 Redis IO 模型：Redis 网络框架调用 epoll 机制，让内核监听这些套接字。Redis 线程不会阻塞在某一个特定的监听或已连接套接字上，也就是说，不会阻塞在某一个特定的客户端请求处理上。正因为此，Redis 可以同时和多个客户端连接并处理请求，从而提升并发性。（具体查看Redis核心技术实战-03）

##### Redis单线程处理IO请求性能瓶颈

1. 任意一个请求在server中一旦发生耗时，都会影响整个server的性能，也就是说后面的请求都要等前面这个耗时请求处理完成，自己才能被处理到。耗时的操作包括以下几种：
   1. 操作bigkey：写入一个bigkey在分配内存时需要消耗更多的时间，同样，删除bigkey释放内存同样会产生耗时；
   2. 使用复杂度过高的命令：例如SORT/SUNION/ZUNIONSTORE，或者O(N)命令，但是N很大，例如lrange key 0 -1一次查询全量数据；
   3. 大量key集中过期：Redis的过期机制也是在主线程中执行的，大量key集中过期会导致处理一个请求时，耗时都在删除过期key，耗时变长；
   4. 淘汰策略：淘汰策略也是在主线程执行的，当内存超过Redis内存上限后，每次写入都需要淘汰一些key，也会造成耗时变长；
   5. AOF刷盘开启always机制：每次写入都需要把这个操作刷到磁盘，写磁盘的速度远比写内存慢，会拖慢Redis的性能；
   6. 主从全量同步生成RDB：虽然采用fork子进程生成数据快照，但fork这一瞬间也是会阻塞整个线程的，实例越大，阻塞时间越久；
2. 并发量非常大时，单线程读写客户端IO数据存在性能瓶颈，虽然采用IO多路复用机制，但是读写客户端数据依旧是同步IO，只能单线程依次读取客户端的数据，无法利用到CPU多核。

**想法**：

针对问题1，一方面需要业务人员去规避，一方面Redis在4.0推出了lazy-free机制，把bigkey释放内存的耗时操作放在了异步线程中执行，降低对主线程的影响。

针对问题2，Redis在6.0推出了多线程，可以在高并发场景下利用CPU多核多线程读写客户端数据，进一步提升server性能，当然，只是针对客户端的读写是并行的，每个命令的真正操作依旧是单线程的。



#### 持久化

Redis是内存操作，一旦服务器宕机，内存中的数据将全部丢失。Redis 的持久化主要有两大机制，即 AOF 日志和 RDB 快照。

##### AOF日志

AOF（Append Only File）日志记录的是执行语句，在系统执行成功后才会记录，一方面避免记录错误命令，也不会阻塞当前的写操作。AOF机制依旧存在风险：1、命令执行完还没有来得及记日志就宕机了2、虽然避免了对当前命令的阻塞，但可能会给下一个操作带来阻塞风险

- AOF写日志策略（appendfsync 配置）

  避免主线程阻塞和减少数据丢失问题，三种策略无法两全齐美，只能在两者之间权衡

  - Always，同步写回：每个写命令执行完，立马同步地将日志写回磁盘；
  - Everysec，每秒写回：每个写命令执行完，只是先把日志写到 AOF 文件的内存缓冲区，每隔一秒把缓冲区中的内容写入磁盘；
  - No，操作系统控制的写回：每个写命令执行完，只是先把日志写到 AOF 文件的内存缓冲区，由操作系统决定何时将缓冲区内容写回磁盘。

- AOF重写机制（auto-aof-rewrite-min-size 10mb 设置日志文件大小）

  为了避免日志文件过大，append命令日志追加效率较低。AOF重写机制指的是，对过大的AOF文件进行重写，以此来压缩AOF文件的大小。 具体的实现是：检查当前键值数据库中的键值对，记录键值对的最终状态，从而实现对 某个键值对 重复操作后产生的多条操作记录压缩成一条 的效果。进而实现压缩AOF文件的大小。

- AOF重写避免阻塞

  AOF重写为了避免阻塞主线程，主线程 fork 出后台的 bgrewriteaof 子进程。此时，fork 会把主线程的内存拷贝一份给 bgrewriteaof 子进程，这里面就包含了数据库的最新数据。然后，bgrewriteaof 子进程就可以在不影响主线程的情况下，逐一把拷贝的数据写成操作，记入重写日志。

  因为主线程未阻塞，仍然可以处理新来的操作。此时，如果有写操作，第一处日志就是指正在使用的 AOF 日志，Redis 会把这个操作写到它的缓冲区。这样就保证重写日志的齐全

##### RDB快照

RDB 持久化可以在指定的时间间隔内生成数据集的时间点快照（point-in-time snapshot），将某一时刻的数据持久化到磁盘中。为了避免快照暂停些操作，Redis采用Copy-On-Write技术。

- save：在主线程中执行，会导致阻塞；
- bgsave：fock一个子进程，专门用于写入 RDB 文件，避免了主线程的阻塞，这也是 Redis RDB 文件生成的默认配置。

RDB快照间隔时间设置，例如save 900 1（15分钟改一次）、save 300 10（5分钟改10次），时间间隔内写操作数据需要记住（带来额外的空间开销），Redis 4.0 中提出了一个混合使用 AOF 日志和内存快照的方法。简单来说，内存快照以一定的频率执行，在两次快照之间，使用 AOF 日志记录这期间的所有命令操作。

##### AOF和RDB对比

RDB的优点：

- RDB 是一个非常紧凑（compact）的文件，它保存了 Redis 在某个时间点上的数据集，这种文件非常适合用于进行备份和传输。
- RDB 非常适用于灾难恢复（disaster recovery）：它只有一个文件，并且内容都非常紧凑，可以（在加密后）将它传送到别的数据中心。
- RDB 可以最大化 Redis 的性能：父进程在保存 RDB 文件时唯一要做的就是 fork 出一个子进程，然后这个子进程就会处理接下来的所有保存工作，父进程无须执行任何磁盘 I/O 操作。
- RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快。

RDB的缺点：

- 如果你需要尽量避免在服务器故障时丢失数据，那么 RDB 不适合你。 虽然 Redis 允许你设置不同的保存点（save point）来控制保存 RDB 文件的频率， 但是， 因为RDB 文件需要保存整个数据集的状态， 所以它并不是一个轻松的操作。 因此你可能会至少 5 分钟才保存一次 RDB 文件。 在这种情况下， 一旦发生故障停机， 你就可能会丢失好几分钟的数据。
- 每次保存 RDB 的时候，Redis 都要 fork() 出一个子进程，并由子进程来进行实际的持久化工作。 在数据集比较庞大时， fork() 可能会非常耗时，造成服务器在某某毫秒内停止处理客户端； 如果数据集非常巨大，并且 CPU 时间非常紧张的话，那么这种停止时间甚至可能会长达整整一秒。 虽然 AOF 重写也需要进行 fork() ，但无论 AOF 重写的执行间隔有多长，数据的耐久性都不会有任何损失。



AOF的优点：

- 使用 AOF 持久化会让 Redis 变得非常耐久（much more durable）：你可以设置不同的 fsync 策略，比如无 fsync ，每秒钟一次 fsync ，或者每次执行写入命令时 fsync 。 AOF 的默认策略为每秒钟 fsync 一次，在这种配置下，Redis 仍然可以保持良好的性能，并且就算发生故障停机，也最多只会丢失一秒钟的数据（ fsync 会在后台线程执行，所以主线程可以继续努力地处理命令请求）。
- AOF 文件是一个只进行追加操作的日志文件（append only log）， 因此对 AOF 文件的写入不需要进行 seek ， 即使日志因为某些原因而包含了未写入完整的命令（比如写入时磁盘已满，写入中途停机，等等）， redis-check-aof 工具也可以轻易地修复这种问题。
- Redis 可以在 AOF 文件体积变得过大时，自动地在后台对 AOF 进行重写： 重写后的新 AOF 文件包含了恢复当前数据集所需的最小命令集合。 整个重写操作是绝对安全的，因为 Redis 在创建新 AOF 文件的过程中，会继续将命令追加到现有的 AOF 文件里面，即使重写过程中发生停机，现有的 AOF 文件也不会丢失。 而一旦新 AOF 文件创建完毕，Redis 就会从旧 AOF 文件切换到新 AOF 文件，并开始对新 AOF 文件进行追加操作。
- AOF 文件有序地保存了对数据库执行的所有写入操作， 这些写入操作以 Redis 协议的格式保存， 因此 AOF 文件的内容非常容易被人读懂， 对文件进行分析（parse）也很轻松。 导出（export） AOF 文件也非常简单： 举个例子， 如果你不小心执行了 FLUSHALL 命令， 但只要 AOF 文件未被重写， 那么只要停止服务器， 移除 AOF 文件末尾的 FLUSHALL 命令， 并重启 Redis ， 就可以将数据集恢复到 FLUSHALL 执行之前的状态。

AOF的缺点：

对于相同的数据集来说，AOF 文件的体积通常要大于 RDB 文件的体积。

根据所使用的 fsync 策略，AOF 的速度可能会慢于 RDB 。 在一般情况下， 每秒 fsync 的性能依然非常高， 而关闭 fsync 可以让 AOF 的速度和 RDB 一样快， 即使在高负荷之下也是如此。 不过在处理巨大的写入载入时，RDB 可以提供更有保证的最大延迟时间（latency）。

AOF 在过去曾经发生过这样的 bug ： 因为个别命令的原因，导致 AOF 文件在重新载入时，无法将数据集恢复成保存时的原样。 （举个例子，阻塞命令 BRPOPLPUSH 就曾经引起过这样的 bug 。） 测试套件里为这种情况添加了测试： 它们会自动生成随机的、复杂的数据集， 并通过重新载入这些数据来确保一切正常。 虽然这种 bug 在 AOF 文件中并不常见， 但是对比来说， RDB 几乎是不可能出现这种 bug 的。



#### 数据同步 

Redis 提供了主从库模式，以保证数据副本的一致，主从库之间采用的是读写分离的方式。

##### 全量同步

从库第一次进行同步时要进行全量复制：replicaof {masterIP} {masterPort}（Redis 5.0 之前使用 slaveof），大致步骤：

1. 第一阶段：建立连接、协商同步

   1. 从库和主库建立起连接，并告诉主库即将进行同步，主库确认回复后，主从库间就可以开始同步了。	psync {runID} {offset}

      runID，是每个 Redis 实例启动时都会自动生成的一个随机 ID，用来唯一标记这个实例。当从库和主库第一次复制时，因为不知道主库的 runID，所以将 runID 设为“？”。offset，此时设为 -1，表示第一次复制。所以第一次为：psync ？ -1

   2. 主库收到 psync 命令后，会用 FULLRESYNC 响应命令带上两个参数：主库 runID 和主库目前的复制进度 offset，返回给从库。从库收到响应后，会记录下这两个参数。

      FULLRESYNC {runID} {offset}

2. 第二阶段：主库将所有数据同步给从库

   主库执行 bgsave 命令，生成 RDB 文件，接着将文件发给从库。从库接收到 RDB 文件后，会先清空当前数据库，然后加载 RDB 文件。在主库将数据同步给从库的过程中，主库不会被阻塞，仍然可以正常接收请求。但是，这些请求中的写操作并没有记录到刚刚生成的 RDB 文件中。为了保证主从库的数据一致性，主库会在内存中用专门的 **replication buffer**，记录 RDB 文件生成后收到的所有写操作。

3. 第三个阶段：发送写命令给从库

   主库完成 RDB 文件发送后，就会把此时 replication buffer 中的修改操作发给从库，从库再重新执行这些操作。这样一来，主从库就实现同步了。

   

   为了避免所有的从库都是和主库连接，导致频繁fock子线程且占用主库带宽。可以通过**“主 - 从 - 从”**模式将主库生成 RDB 和传输 RDB 的压力，以级联的方式分散到个别从库上。

##### 长链接命令传播

主从库完成了全量复制，它们之间就会一直维护一个网络连接，主库会通过这个连接将后续陆续收到的命令操作再同步给从库，这个过程也称为基于长连接的命令传播，可以避免频繁建立连接的开销。主从之间网路不稳定断开了解，为了保持主从一直则需要进行增量复制。

##### 增量复制

repl_backlog_buffer 是一个环形缓冲区，主库会记录自己写到的位置，从库则会记录自己已经读到的位置。当主从库断连后，主库会把断连期间收到的写操作命令。刚开始的时候，主库和从库的写读位置在一起，这算是它们的起始位置。随着主库不断接收新的写操作，它在缓冲区中的写位置会逐步偏离起始位置，我们通常用偏移量来衡量这个偏移距离的大小，对主库来说，对应的偏移量就是 master_repl_offset。主库接收的新写操作越多，这个值就会越大。



- repl_backlog_buffer：不是“主从库断连后”主库才把写操作写入repl_backlog_buffer，只要有从库存在，这个repl_backlog_buffer就会存在。它是为了从库断开之后，如何找到主从差异数据而设计的环形缓冲区，从而避免全量同步带来的性能开销。如果从库断开时间太久，repl_backlog_buffer环形缓冲区被主库的写命令覆盖了，那么从库连上主库后只能乖乖地进行一次全量同步，所以repl_backlog_buffer配置尽量大一些，可以降低主从断开后全量同步的概率。而在repl_backlog_buffer中找主从差异的数据后，如何发给从库呢？这就用到了replication buffer。

- replication buffer：Redis和客户端通信也好，和从库通信也好，Redis都需要给分配一个 内存buffer进行数据交互，客户端是一个client，从库也是一个client，我们每个client连上Redis后，Redis都会分配一个client buffer，所有数据交互都是通过这个buffer进行的：Redis先把数据写到这个buffer中，然后再把buffer中的数据发到client socket中再通过网络发送出去，这样就完成了数据交互。所以主从在增量同步时，从库作为一个client，也会分配一个buffer，只不过这个buffer专门用来传播用户的写命令到从库，保证主从数据一致，我们通常把它叫做replication buffer。

  

#### 哨兵机制

由三个问题引出哨兵机制：

1. 主库挂了怎么办？
2. 该选择那个从库作为主库？
3. 怎么把新住哭的相关信息通知从库和客户端？

##### 基本流程

哨兵其实就是一个运行在特殊模式下的 Redis 进程，主从库实例运行的同时，它也在运行。哨兵的主要三个任务：监控、选主、通知

1. 监控

   周期性地给所有的主从库发送 PING 命令，检测它们是否仍然在线运行。如果从库没有在规定时间内响应哨兵的 PING 命令，哨兵就会把它标记为“下线状态”；如果主库也没有在规定时间内响应哨兵的 PING 命令，哨兵就会判定主库下线，然后开始自动切换主库的流程。

2. 选主

   哨兵就需要从很多个从库里，按照一定的规则选择一个从库实例，把它作为新的主库。

3. 通知

   哨兵会把新主库的连接信息发给其他从库，让它们执行 replicaof 命令，和新主库建立连接，并进行数据复制。同时，哨兵会把新主库的连接信息通知给客户端，让它们把请求操作发到新主库上。

##### 主观和客观下线

1. 哨兵发现主库或从库对 PING 命令的响应超时了，那么，哨兵就会先把它标记为“主观下线”。
2. 如果检测的是主库，避免误判导致主从切换后选主、通知的额外开销，哨兵不能简单地把它标记为“主观下线”，就引入**哨兵集群**同时监控主从库，当有 N 个哨兵实例时，最好要有 N/2 + 1 个实例判断主库为“主观下线”。

##### 选主过程

选主规则和过程：

1. 筛选当前从库网络正常在线，除了要检查从库的当前在线状态，还要判断它之前的网络连接状态。down-after-milliseconds：设置主从库断连的最大连接超时时间，时间越短主从切换越敏感

   down-after-milliseconds * 10 ：在最大连接超时时间内网络断链时间超过10次，则判定不适合作为新主库

2. 完成筛选后，按照规则开始打分。打分规则：从库优先级、从库复制进度以及从库 ID 号

   1. 从库优先级

      可以通过 slave-priority 配置项，给不同的从库设置不同优先级。优先级相同，开始下一轮打分。

   2. 从库复制速度

      库会用 master_repl_offset 记录当前的最新写操作在 repl_backlog_buffer 中的位置，而从库会用 slave_repl_offset 这个值记录当前的复制进度。主库此时链接不上，但是master_repl_offset对于所有从库来说相同，所以比较各个从库**slave_repl_offset**谁更接近主库谁的复制速度最快。

   3. 从库ID

      每个实例都会有一个 ID，在优先级和复制进度都相同的情况下，ID 号最小的从库得分最高，会被选为新主库。

##### 哨兵集群

多个实例组成了哨兵集群，即使有哨兵实例出现故障挂掉了，其他哨兵还能继续协作完成主从库切换的工作，包括判定主库是不是处于下线状态，选择新主库，以及通知从库和客户端。

- **基于 pub/sub 机制的哨兵集群组成**

  哨兵实力之前如何互相发现？就是机遇Redis 提供的 pub/sub 机制。Redis 会以频道的形式，进行消息的发布和订阅。只有订阅了同一个频道的应用，才能通过发布的消息进行信息交换。在主从集群中，主库上有一个名为的`__sentinel__hello`频道，不同哨兵就是通过它来相互发现，实现互相通信的。

  例如：哨兵 1 把自己的 IP（172.16.19.3）和端口（26579）发布到`__sentinel__hello`频道上，哨兵 2 和 3 订阅了该频道。那么此时，哨兵 2 和 3 就可以从这个频道直接获取哨兵 1 的 IP 地址和端口号。

- **哨兵是如何知道从库的 IP 地址和端口**

  这是由哨兵向主库**发送 INFO 命令**来完成的。哨兵给主库发送 INFO 命令，主库接受到这个命令后，就会把从库列表返回给哨兵。接着，哨兵就可以根据从库列表中的连接信息，和每个从库建立连接，并在这个连接上持续地对从库进行监控。

- **基于 pub/sub 机制的客户端事件通知**

  从本质上说，哨兵就是一个运行在特定模式下的 Redis 实例，只不过它并不服务请求操作，只是完成监控、选主和通知的任务。所以，每个哨兵实例也提供 pub/sub 机制，客户端可以从哨兵订阅消息。哨兵提供的消息订阅频道有很多，不同频道包含了主从库切换过程中的不同关键事件。

- **由哪个哨兵执行主从切换**

  主库故障以后，哨兵集群有多个实例，那怎么确定由哪个哨兵来进行实际的主从切换呢？

  1. 任何一个实例只要自身判断主库“主观下线”后，就会给其他实例发送 is-master-down-by-addr 命令。接着，其他实例会根据自己和主库的连接情况，做出 Y 或 N 的响应，Y 相当于赞成票，N 相当于反对票。获取 N/2 + 1 个实例判断主库为“主观下线”。

  2. 这个哨兵就可以再给其他哨兵发送命令，表明希望由自己来执行主从切换，并让所有其他哨兵进行投票。

  3. 开始Leader选举投票，例如：

     S1判断主库为“客观下线”，它想成为 Leader，就先给自己投一张赞成票，然后分别向 S2 和 S3 发送命令，表示要成为 Leader。S2、S3收到第一个Leader请求会回复Y之后再收到则回复N。如果导最终没有选出Leader，哨兵会停一段时间（一般是故障转移超时时间failover_timeout的2倍），然后再可以进行下一轮投票。

#### 切片集群
