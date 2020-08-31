### AsyncTool多线程编排工具

#### CompletableFuture

CompletableFuture实现Future和CompletionStage接口，帮助我们简化异步编程的复杂性，并提供了转换和组合CompletableFuture方法以及回调方式处理结果。

##### 纯消费和对结果处理

- 纯消费

  单纯的消费，不会返回新的值

  - thenAccept

    ```java
    public static void thenAcceptTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Void> future = CompletableFuture
                .supplyAsync(() -> 1)
             	//消费 上一级返回值 1
                .thenAccept(System.out::println)
            	//上一级没有返回值 输出null
                .thenAcceptAsync(System.out::println); 
        //消费函数没有返回值 输出null        
        System.out.println(future.get()); 
    }
    ```

  - thenAcceptBoth

    ```java
    public static void thenAcceptBothTest() throws ExecutionException, InterruptedException {
        CompletableFuture
                .supplyAsync(() -> 1)
            	//消费 上一级返回值 1 和当前的返回值 2
                .thenAcceptBoth(CompletableFuture.supplyAsync(() -> 2), (a, b) -> {
                    System.out.println(a);
                    System.out.println(b);
                }).get();
    }
    ```

  - thenRun

    ```java
    public static void thenRunTest() throws ExecutionException, InterruptedException {
        CompletableFuture
                .supplyAsync(() -> 1)
            	//Runnable 类型的参数
                .thenRun(() -> {
                    System.out.println("then run");
                });
    }
    ```

    

- 对结果处理

  CompletableFuture的计算结果完成，或者抛出异常的时候，我们可以执行特定的Action

  - whenComplete

    获取上一步计算的计算结果和异常信息

    ```java
    public static void whenCompleteTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            Thread.sleep(100);
            return 20/0;
           //执行完成获取 返回 和 异常
        }).whenCompleteAsync((v, e) -> {
            System.out.println(v);
            System.out.println(e);
        });
        System.out.println(future.get());
    }
    ```

  - exceptionally

    对异常情况的处理，当函数异常时应该的返回值

    ```java
    public static void exceptionallyTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            ThreadUtil.sleep(100);
            return 10 / 0;
            //执行完成获取 返回 和 异常
        }).whenCompleteAsync((v, e) -> {
            System.out.println(v);
            System.out.println(e);
            //处理异常返回指定值
        }).exceptionally((e) -> {
            System.out.println(e.getMessage());
            return 30;
        });
        System.out.println(future.get());
    }
    
    ```

  - handle

    类似whenComplete，同时接受BiFunction 参数，有thenApply的功能

    ```java
    public static void handleTest() throws ExecutionException, InterruptedException {
        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> 10 / 0)
                .handle((t, e) -> {
                    System.out.println(e.getMessage());
                    return 10;
                });
        System.out.println(future.get());
    }
    ```

    

##### 转换和组合

- 转换 thenApply

  在一个结果计算完成之后紧接着执行下个Action

  ```java
  public static void thenApplyTest() throws ExecutionException, InterruptedException {
      CompletableFuture<Integer> future = CompletableFuture
              .supplyAsync(() -> 1)
              .thenApply((a) -> {
                  //获取结果1，执行下个Action
                  System.out.println(a);
                  return a * 10;
                  //获取结果10，执行下个Action
              }).thenApply((a) -> {
                  System.out.println(a);
                  return a - 5;
              });
      System.out.println(future.get());
  }
  ```

- 组合

  - thenCompose

    ```java
    public static void thenComposeTest() throws ExecutionException, InterruptedException {
    
        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> 1)
                .thenApply((a) -> {
                    ThreadUtil.sleep(1000);
                    return a + 10;
                })
            	//thenCompose 和 supplyAsync 有依赖顺序
                .thenCompose((s) -> {
                    System.out.println(s);
                    return CompletableFuture.supplyAsync(() -> s * 5);
                });
    
        System.out.println(future.get());
    }
    ```

  - thenCombine

    ```java
    public static void thenCombineTest() throws ExecutionException, InterruptedException {
    
        Random random = new Random();
        CompletableFuture<Integer> future = CompletableFuture
                .supplyAsync(() -> {
                    ThreadUtil.sleep(random.nextInt(1000));
                    System.out.println("supplyAsync");
                    return 2;
                }).thenApply((a) -> {
                    ThreadUtil.sleep(random.nextInt(1000));
                    System.out.println("thenApply");
                    return a * 3;
                })
            	//thenCombine 和 supplyAsync 同步执行，不是强依赖关系
                .thenCombine(CompletableFuture.supplyAsync(() -> {
                    ThreadUtil.sleep(random.nextInt(1000));
                    System.out.println("thenCombineAsync");
                    return 10;
                }), (a, b) -> {
                    System.out.println(a);
                    System.out.println(b);
                    return a + b;
                });
    
        System.out.println(future.get());
    }
    ```

  - allOf / anyOf

    所有方法都执行完 / 任何一个执行完

#### AsyncTool工具

解决任意的多线程并行、串行、阻塞、依赖、回调的并行框架，可以任意组合各线程的执行顺序，带全链路执行结果回调。

##### 基本组件

- worker： 一个最小的任务执行单元。T，V两个泛型，分别是入参和出参类型。

  ```java
  public interface IWorker<T, V> {
      /**
       * 在这里做耗时操作，如rpc请求、IO等
       */
      V action(T object);
  
      /**
       * 超时、异常时，返回的默认值
       */
      V defaultValue();
  }
  ```

- callBack：对每个worker的回调。worker执行完毕后，会回调该接口，带着执行成功、失败、原始入参、和详细的结果。

  ```java
  public interface ICallback<T, V> {
  
      void begin();
  
      /**
       * 耗时操作执行完毕后，就给value注入值
       */
      void result(boolean success, T param, WorkResult<V> workResult);
  }
  ```

- wrapper：组合了worker和callback，是一个 最小的调度单元 。通过编排wrapper之间的关系，达到组合各个worker顺序的目的。

  ```java
  public class WorkerWrapper<T, V> {
      //worker将来要处理的param
      private T param;
      private IWorker<T, V> worker;
      private ICallback<T, V> callback;
  }
  ```

  

##### 需求场景及实现

- 任意编排

  - 场景一

    A执行完毕后，开启另外BC，BC执行完毕后，开始执行D

    AsyncTool 实现

    ```java
     /**
      *         ---> product
      *  user                ---> inventory
      *         ---> coupons
      */
    public static ShoppingCatInfo getShoppingCarAsyncTool(String id) throws Exception{
        //user
        WorkerWrapper<String, String> userWorkerWrapper =  new WorkerWrapper.Builder<String, String>()
                .worker(userAction)
                .callback(userAction)
                .param(id)
                .build();
        WorkResult<String> userResult = userWorkerWrapper.getWorkResult();
    
        //inventory
        WorkerWrapper<WorkResult<String>, String> inventoryWorkerWrapper =  new WorkerWrapper.Builder<WorkResult<String>, String>()
                .worker(inventoryAction)
                .callback(inventoryAction)
                .param(userResult).build();
    
        //coupons
        WorkerWrapper<WorkResult<String>, String> couponsWorkerWrapper =  new WorkerWrapper.Builder<WorkResult<String>, String>()
                .worker(couponsAction)
                .callback(couponsAction)
                .param(userResult)
                .depend(userWorkerWrapper)
                .next(inventoryWorkerWrapper).build();
    
        //product
        WorkerWrapper<WorkResult<String>, String> productWorkerWrapper =  new WorkerWrapper.Builder<WorkResult<String>, String>()
                .worker(productAction)
                .callback(productAction)
                .depend(userWorkerWrapper)
                .next(inventoryWorkerWrapper)
                .param(userResult).build();
    
    
        System.out.println("begin-" + SystemClock.now());
    
        Async.beginWork(3000, userWorkerWrapper);
    
        System.out.println("end-" + SystemClock.now());
        System.out.println(Async.getThreadCount());
        Async.shutDown();
    
       return new ShoppingCatInfo(userWorkerWrapper.getWorkResult().getResult(), 
                    productWorkerWrapper.getWorkResult().getResult(),
                    inventoryWorkerWrapper.getWorkResult().getResult(), 
                    couponsWorkerWrapper.getWorkResult().getResult());
    }
    ```

    CompletableFuture实现

    ```java
    public static String getShoppingCarComplateFuture(String id) throws ExecutionException, InterruptedException {
    
        String userId = userAction.action(id);
    
        CompletableFuture<String> invetoryFuture = CompletableFuture
                .supplyAsync(() ->{
                    System.out.println("CouponsAction start");
                    return couponsAction.action(id);
                })
                .thenCombineAsync(CompletableFuture.supplyAsync(() -> {
                    System.out.println("ProductAction start");
                    return productAction.action(userId);
                }), (couponse, product) -> new ShoppingCatInfo(userId, product, inventoryAction.action(userId) ,couponse).toString() , executor);
    
        String str = invetoryFuture.get();
        return str;
    }
    ```

    

  - 场景二

  ![](C:\Users\MI\Desktop\093023_357a2912_303698.png)

- 执行结果回调

  结果回调实现 **ICallback** 接口，类型于CompletableFuture的thenAccept 回调操作，AsyncTool在执行单元未执行的情况下也能实现回调，而thenAccept 必须是CompletableFuture执行后才会进行回调。

- 执行强依赖、弱依赖

  类似于：

  ​	CompletableFuture # **allOf**(CompletableFuture<?>... cfs)

  ​    CompletableFuture # **anyOf**(CompletableFuture<?>... cfs)

  ```java
  // must 强、弱依赖控制
  private void addDepend(WorkerWrapper<?, ?> workerWrapper, boolean must) {
       addDepend(new DependWrapper(workerWrapper, must));
  }
  ```

- 上游执行结果入参

  可以在编排时，就取上有执行单元结果的包装类，作为下游单元的入参。虽然此时上游单元尚未执行，必然是null，但可以保证上游单元完毕后，下游单元的入参会被赋值。

- 全组任务超时

  各个执行单元超时时间不可控，只能控制全组执行的超时时间

  ```java
  //timeout 超时时间控制
  public static boolean beginWork(long timeout, WorkerWrapper... workerWrapper)
  ```

  

##### 类比CompletableFuture优劣

- 优点
  - 可以做到每个单元的执行回调
  - 复杂场景的编排，代码相对清晰
- 缺点
  - 超时控制只能控制在全组任务上
  - 代码不够简化、雅观

#### 

