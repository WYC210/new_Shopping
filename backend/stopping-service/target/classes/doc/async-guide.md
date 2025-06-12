# 异步处理和前端数据交互指南

## 1. 异步处理概述

在 ShopSphere 系统中，我们使用了 Spring 的@Async 注解实现异步处理，主要用于以下场景：

1. 消息发送 - 通过 RabbitMQ 发送消息
2. 耗时的后台处理 - 不需要即时返回结果的处理
3. 事件通知 - 系统内部事件处理

## 2. 异步方法的特点

使用@Async 注解的方法具有以下特点：

1. 方法会在单独的线程中执行，不会阻塞调用线程
2. 方法的返回值对调用者而言是无效的（立即返回 null）
3. 方法的执行结果无法直接获取

## 3. 数据查询方法不应使用@Async

重要提示：对于需要立即返回数据给前端的方法，不应使用@Async 注解。

错误示例：

```java
@Async("taskExecutor")
public List<OrderDetailVO> getUserOrders(Long userId, String status) {
    // 查询处理
    return orderList;  // 这个返回值实际上会被忽略，调用者会立即获得null
}
```

正确示例：

```java
public List<OrderDetailVO> getUserOrders(Long userId, String status) {
    // 查询处理
    return orderList;  // 同步返回结果
}
```

## 4. 异步处理的正确使用场景

异步处理适合以下场景：

1. 发送通知消息
2. 记录日志
3. 更新缓存
4. 发送邮件
5. 生成报表
6. 数据同步

## 5. 前端交互模式

当需要异步处理且需要前端获取处理结果时，可以采用以下模式：

1. **轮询模式**：前端定期查询处理结果

   ```javascript
   // 提交处理请求
   const taskId = await api.submitTask(data);

   // 定期查询结果
   const checkStatus = async () => {
     const result = await api.getTaskResult(taskId);
     if (result.status === "COMPLETED") {
       // 处理完成，展示结果
       showResult(result.data);
     } else if (result.status === "FAILED") {
       // 处理失败
       showError(result.message);
     } else {
       // 继续等待
       setTimeout(checkStatus, 1000);
     }
   };

   checkStatus();
   ```

2. **WebSocket 通知**：处理完成后通过 WebSocket 推送结果

   ```javascript
   // 提交处理请求
   const taskId = await api.submitTask(data);

   // 监听WebSocket消息
   socket.on("task_completed", (result) => {
     if (result.taskId === taskId) {
       // 处理完成，展示结果
       showResult(result.data);
     }
   });
   ```

## 6. 分页查询

对于分页查询，我们提供了同步的分页 API，返回格式如下：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 100,      // 总记录数
    "pages": 10,       // 总页数
    "current": 1,      // 当前页码
    "records": [...]   // 当前页的数据列表
  }
}
```

前端调用示例：

```javascript
// 获取用户订单列表，第1页，每页10条
const result = await api.getUserOrders({
  status: "PENDING",
  page: 1,
  pageSize: 10,
});

// 使用返回数据
const { total, pages, current, records } = result.data;
```

## 7. 性能优化建议

1. 避免在控制器方法中直接使用@Async
2. 对于耗时操作，应在服务层方法中使用@Async
3. 使用适当的线程池配置，避免资源耗尽
4. 考虑使用响应式编程模型(如 Spring WebFlux)处理高并发场景

如有任何问题，请联系技术支持团队。
