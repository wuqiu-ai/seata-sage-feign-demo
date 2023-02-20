## Saga简介
Saga 是一种补偿协议，在 Saga 模式下，分布式事务内有多个参与者，每一个参与者都是一个冲正补偿服务，需要用户根据业务场景实现其正向操作和逆向回滚操作。

分布式事务执行过程中，依次执行各参与者的正向操作，如果所有正向操作均执行成功，那么分布式事务提交。如果任何一个正向操作执行失败，那么分布式事务会退回去执行前面各参与者的逆向回滚操作，回滚已提交的参与者，使分布式事务回到初始状态。
状态图如下：

![状态图](https://raw.githubusercontent.com/ppj19891020/pictures/master/seata/2.jpg)

Saga 模式下分布式事务通常是由事件驱动的，各个参与者之间是异步执行的，Saga 模式是一种长事务解决方案。

事务参与者可能是其它公司的服务或者是遗留系统的服务，无法进行改造和提供 TCC 要求的接口，可以使用 Saga 模式。

Saga模式的优势是：
- 一阶段提交本地数据库事务，无锁，高性能；
- 参与者可以采用事务驱动异步执行，高吞吐；
- 补偿服务即正向服务的“反向”，易于理解，易于实现；

Saga模式缺点：
> Saga 模式由于一阶段已经提交本地数据库事务，且没有进行“预留”动作，所以不能保证隔离性。

## 基于状态机引擎的 Saga 实现

基本原理：
1. 基于json格式定义服务调用状态图；
2. 状态图的一个节点可以是一个服务，节点可以配置补偿节点；
3. 状态图json由状态机执行引擎驱动执行，当出现异常状态时状态机引擎执行反向补偿任务将事物回滚；
4. 异常状态发生时是否进行补偿由用户自定义决定；
5. 可以实现服务编排的需求，支持单项选择、并发、异步、子状态机调用、参数转换、参数映射、服务执行状态判断、异常捕获等功能；

![状态图](http://seata.io/img/saga/demo_statelang.png?raw=true)

## springCloud seata saga接入指南
### 1. 引入jar包
```xml
<!--seata组件包-->
<dependency>
  <groupId>io.seata</groupId>
  <artifactId>seata-all</artifactId>
  <version>${seata.version}</version>
</dependency>

<!--spring cloud 相关定制-->
<dependency>
  <groupId>com.alibaba.cloud</groupId>
  <artifactId>spring-cloud-alibaba-seata</artifactId>
  <version>x.y.z</version>
</dependency>
```
注意：[seata兼容版本说明](https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

### 2. seata 注册中心配置
registry.conf配置文件，euraka中的application是指seata的服务端的服务器，这边要注意seata server有事物分组的概念，用于不同业务方的集群分区。
```
registry {
  # 注册中心支持file 、nacos 、eureka、redis、zk,推荐eureka做负载均衡
  type = "eureka"

  eureka {
    serviceUrl = "http://192.168.202.137:8761/eureka"
    # seata server注册中心的服务名
    application = "seata-server-default-group"
    weight = "1"
  }
}

config {
  # 配置中心支持file、nacos 、apollo、zk,推荐apollo
  type = "file"

  file {
    name = "file.conf"
  }
}
```

### 3. seata 配置中心配置
file.conf配置文件，这里需要注意service中的vgroup_mapping配置，其中vgroup_mapping.my_test_tx_group的my_test_tx_group是表示逻辑服务分组，值表示seata server的实际服务分组，一定要存在seata serve的分组名
```
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
  shutdown {
    # when destroy server, wait seconds
    wait = 3
  }
  serialization = "seata"
  compressor = "none"
}

service {
  #vgroup->rgroup
  vgroup_mapping.my_test_tx_group = "seata-server-default-group"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
  #unit ms,s,m,h,d represents milliseconds, seconds, minutes, hours, days, default permanent
  max.commit.retry.timeout = "-1"
  max.rollback.retry.timeout = "-1"
  disableGlobalTransaction = false
}

client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
  report.retry.count = 5
  tm.commit.retry.count = 1
  tm.rollback.retry.count = 1
}

transaction {
  undo.data.validation = true
  undo.log.serialization = "jackson"
  undo.log.save.days = 7
  #schedule delete expired undo_log in milliseconds
  undo.log.delete.period = 86400000
  undo.log.table = "undo_log"
}

support {
  ## spring
  spring {
    # auto proxy the DataSource bean
    datasource.autoproxy = false
  }
}
```

### 4. TM配置服务分组名
application.yml配置文件
```yaml
spring:
  cloud:
    alibaba:
      seata:
        ## 该服务分组名一定要和file.conf配置文件中的service.vgroup_mapping一致，不然找不到对应的seata server集群名
        tx-service-group: my_test_tx_group
```

### 5. TM配置状态机
这个例子就配置下单服务的saga流程，具体的参数请参考：http://seata.io/zh-cn/docs/user/saga.html

![状态机](https://raw.githubusercontent.com/ppj19891020/pictures/master/seata/3.png)

json状态图格式如下：
```json
{
  "Name": "purchaseProcess",
  "Comment": "用户下单流程-saga流程",
  "StartState": "CreateOrderNo",
  "Version": "1.0.0",
  "States": {
    "CreateOrderNo": {
      "Comment": "生成订单号服务",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.OrderApi",
      "ServiceMethod": "createOrderNo",
      "CompensateState": "CompensationCanalOrder1",
      "Catch": [
        {
          "Exceptions": [
            "java.lang.Throwable"
          ],
          "Next": "CompensationTrigger"
        }],
      "Output": {
        "orderNo":"$.#root"
      },
      "Next": "CreateOrder",
      "Status": {
        "$Exception{java.lang.Throwable}": "UN",
        "#root != null": "SU",
        "#root == null": "FA"
      }
    },
    "CreateOrder": {
      "Comment": "创建订单服务",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.OrderApi",
      "ServiceMethod": "createOrder",
      "CompensateState": "CompensationCanalOrder2",
      "Next": "ReduceStorage",
      "Input": [{
          "orderNo": "$.[orderNo]",
          "userId": "$.[order].userId",
          "productId": "$.[order].productId",
          "count": "$.[order].count",
          "price": "$.[order].price"
        }],
      "Catch": [{
          "Exceptions": [
            "java.lang.Throwable"
          ],
          "Next": "CompensationTrigger"
        }],
      "Status": {
        "$Exception{java.lang.Throwable}": "UN",
        "#root != null": "SU",
        "#root == null": "FA"
      }
    },
    "ReduceStorage": {
      "Comment": "扣减库存服务",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.StorageApi",
      "ServiceMethod": "reduce",
      "CompensateState": "CompensatingReduceStorage",
      "Next":"Succeed",
      "Input": [{
        "orderNo": "$.[orderNo]",
        "productId": "$.[order].productId",
        "count": "$.[order].count"
      }],
      "Catch": [{
        "Exceptions": [
          "java.lang.Throwable"
        ],
        "Next": "CompensationTrigger"
      }]
    },
    "CompensationCanalOrder1": {
      "Comment": "取消订单补偿服务1--用于订单号生成失败",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.OrderApi",
      "ServiceMethod": "canalOrder",
      "Input": [
        "$.[orderNo]",
        1
      ]
    },
    "CompensationCanalOrder2": {
      "Comment": "取消订单补偿服务2--用于订单生成失败",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.OrderApi",
      "ServiceMethod": "canalOrder",
      "Input": [
        "$.[orderNo]",
        2
      ]
    },
    "CompensatingReduceStorage": {
      "Comment": "库存补偿服务",
      "Comment": "扣减库存服务",
      "Type": "ServiceTask",
      "ServiceName": "com.fly.seata.api.StorageApi",
      "ServiceMethod": "compensateReduce",
      "Input": [{
        "orderNo": "$.[orderNo]",
        "productId": "$.[order].productId",
        "count": "$.[order].count"
      }]
    },
    "CompensationTrigger": {
      "Type": "CompensationTrigger"
    },
    "Succeed": {
      "Type":"Succeed"
    },
    "Fail": {
      "Type":"Fail",
      "ErrorCode": "STORAGE_FAILED",
      "Message": "purchase failed"
    }
  }
}
```

### 6. sagaAutoConfig配置
```java
@Configuration
public class SagaConfig {

  @ConfigurationProperties("spring.datasource.saga")
  @Bean
  public DataSource dataSource(){
    return new DruidDataSource();
  }

  @Bean
  public DbStateMachineConfig dbStateMachineConfig(){
    DbStateMachineConfig dbStateMachineConfig = new DbStateMachineConfig();
    dbStateMachineConfig.setDataSource(dataSource());
    Resource[] resources = {new ClassPathResource("statelang/purchase.json")};
    dbStateMachineConfig.setResources(resources);
    dbStateMachineConfig.setEnableAsync(true);
    dbStateMachineConfig.setThreadPoolExecutor(threadPoolExecutor());
    dbStateMachineConfig.setApplicationId("sage-tm");
    dbStateMachineConfig.setTxServiceGroup("my_test_tx_group");
    return dbStateMachineConfig;
  }

  /**
   * saga状态图执行引擎
   * @return
   */
  @Bean
  public StateMachineEngine processCtrlStateMachineEngine(){
    ProcessCtrlStateMachineEngine stateMachineEngine = new ProcessCtrlStateMachineEngine();
    stateMachineEngine.setStateMachineConfig(dbStateMachineConfig());
    return stateMachineEngine;
  }

  @Bean
  public StateMachineEngineHolder stateMachineEngineHolder(){
    StateMachineEngineHolder stateMachineEngineHolder = new StateMachineEngineHolder();
    stateMachineEngineHolder.setStateMachineEngine(processCtrlStateMachineEngine());
    return stateMachineEngineHolder;
  }

  @Bean
  public ThreadPoolExecutor threadPoolExecutor(){
    ThreadPoolExecutorFactoryBean threadPoolExecutorFactoryBean = new ThreadPoolExecutorFactoryBean();
    threadPoolExecutorFactoryBean.setCorePoolSize(1);
    threadPoolExecutorFactoryBean.setMaxPoolSize(20);
    threadPoolExecutorFactoryBean.setThreadNamePrefix("saga_");
    return (ThreadPoolExecutor)threadPoolExecutorFactoryBean.getObject();
  }
}
```
### 7. 状态机执行
```java
@RequestMapping("/tm")
@RestController
public class TmController {

  /**
   * 模拟购买商品流程
   * @return
   */
  @GlobalTransactional
  @GetMapping("/purchase")
  public String purchase(){
    Map<String, Object> startParams = new HashMap<>();
    OrderDTO orderDTO = new OrderDTO();
    orderDTO.setUserId(1l);
    orderDTO.setCount(1);
    orderDTO.setPrice(new BigDecimal(19));
    orderDTO.setProductId(1l);
    startParams.put("order",orderDTO);
    StateMachineInstance stateMachineInstance = stateMachineEngine.start("purchaseProcess",null,startParams);
    return "执行状态:"+stateMachineInstance.getStatus().getStatusString();
  }

}
```