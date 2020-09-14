结算业务订单确认收货mq小时频繁丢失问题追踪过程

1. 首先排查业务出发点是否send message

   发现某个功能迭代确实漏发消息，但是大部分的丢失消息不是这个原因造成

2. consumer是否正确消费

   其他业务场景下消息正确发送日志打印，确定发送消息发送至broker，但消费端消费log未打印且未消费。排查所有消费结算，确实存在异常consumer（无数据裤权限）且日志未收集（项目蓝绿部署，且当时正迁移tke）。但任然存在大部分丢失消息。

3. consumer消费姿势问题

   1. 项目rabbitmq是默认的配置：手动ACK，但是代码又实用手动ACK处理，rabbitmq server端日志频繁抛“unknown delivery tag”异常。

      分析原因：rabbitmq 为每一个channel维护了一个delivery tag的计数器，这里采用正向自增，新消息投递时自增，当消息响应时自减；
      在连续收发的场景中，由于消息发送的间隔较短，部分消息因 consumer的重复确认被rabbitmq 当做已处理而丢弃。

   2. 在test消费姿势代码时如果异常，会关闭channel并重启consumer再次消费

      ```java
      //通道关闭
      o.s.a.r.c.CachingConnectionFactory       : Channel shutdown: channel error; protocol method: #method<channel.close>(reply-code=406, reply-text=PRECONDITION_FAILED - unknown delivery tag 1, class-id=60, method-id=80)
      
      //重启consumer
      o.s.a.r.l.SimpleMessageListenerContainer : Restarting Consumer@4312b203: tags=[[amq.ctag-CdBhy7XB9O-g6_af-EfaUQ]], channel=Cached Rabbit Channel: PublisherCallbackChannelImpl: AMQChannel(amqp://guest@127.0.0.1:5672/,6), conn: Proxy@213860b8 Shared Rabbit Connection: SimpleConnection@15fdd1f2 [delegate=amqp://guest@127.0.0.1:5672/, localPort= 54072], acknowledgeMode=AUTO local queue size=0
      ```

      每一次异常后都会关闭channel，重启consumer并消费没有ack的消息，test姿势业务时间短可能会出发重复消费，但是真实场景下业务流程较长，消息基本上被自动ack后丢失

   

   消息自动ACK失效问题

   当序列化为JSON时，此配置会失效

   https://www.cnblogs.com/sw008/p/11054293.html
   
   
   
   
   
   
   
   