### AbstractQueuedSynchronizer

​	 队列同步容器，用来构建锁或者其他同步组件的基础框架。使用一个int成员变量表示同步状态，通过内置的FIFO队列来完成资源获取线程的排列工作。同步器提供getState()、setState(int newState)、compareAndSetState(int expect, int update)保证同步状态的改变是安全的。

#### API

同步容器基于模版模式，模版方法：

```java
//独占式获取同步状态
void acquire(int arg);
//同acquire方法，但是响应中断
void acquireInterruptibly(int arg);
//在acquireInterruptibly上增加了超时限制
boolean tryAcquireNanos(int arg, long nanosTimeout);
//独占式释放同步状态
boolean release(int arg); 
//同上，共享式
void acquireShared(int arg);
void acquireSharedInterruptibly(int arg);
boolean tryAcquireSharedNanos(int arg, long nanosTimeout);
boolean releaseShared(int arg);
```

同步组件使用者继承同步容器并重写制定的方法，同步组件调用模版方法，而模版方法将会调用同步组件重写的方法，重写方法：

```java
//独占式获取同步状态
protected final boolean tryAcquire(int acquires);
//独占式释放同步状态
protected boolean tryRelease(int arg);
//共享式获取同步状态
protected int tryAcquireShared(int arg);
//共享式释放同步状态
protected boolean tryReleaseShared(int arg);
//当前同步器是否在独占模式下被线程占用
protected boolean isHeldExclusively();
```

#### 实现原理分析

##### 同步队列

FIFO双向列表

```java
static final class Node {
  //共享
  static final Node SHARED = new Node();
  //独占
  static final Node EXCLUSIVE = null;
  //线程当前在AQS的状态
  volatile int waitStatus;
  //前驱节点
  volatile Node prev;
  //获取节点
  volatile Node next;
  //当前线程
  volatile Thread thread;
}
```

waitStatus

- CANCELLED，值为1，代表同步队列中等待的线程 等待超时 或者 被中断，需要从同步队列中剔除，节点进入该状态以后不会再发生变化了。
- SIGNAL，值为-1，代表后继节点的线程处于等待状态，而如果当前节点的线程如果释放了同步状态或被取消，将会通知后继结点，使后继节点的线程得以运行。
- CONDITION, 值为-2，节点在等待队列，节点线程等待在Condition上，当其他线程调用Condition的signal方法后，该节点将会从等待队列中转移到同步队列中。
- PROPAGATE, 值为-3，表示共享式同步状态回去将会无条件的被传播下去，
- INITAL， 值为0，初始状态。