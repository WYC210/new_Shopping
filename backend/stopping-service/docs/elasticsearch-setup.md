# Elasticsearch 配置和故障排除指南

## 连接错误问题解决方案

如果您在启动应用时遇到以下错误：

```
Caused by: java.net.ConnectException: Connection refused: getsockopt
```

这表明无法连接到 Elasticsearch 服务器。以下是解决方法：

## 解决方案

### 方案一：禁用 Elasticsearch 功能（推荐）

如果您暂时不需要使用搜索功能，可以禁用 Elasticsearch：

1. 在 `application.yml` 中设置：

```yaml
spring:
  elasticsearch:
    enabled: false # 禁用 Elasticsearch
```

2. **重要：** 系统默认已配置为禁用状态，无需额外操作，会自动回退到使用数据库进行基础搜索

### 方案二：安装和启动 Elasticsearch

1. 下载并安装 Elasticsearch 7.17.x 版本（与系统兼容）
2. 启动 Elasticsearch 服务：
   ```
   bin/elasticsearch
   ```
3. 确认 Elasticsearch 运行在 http://localhost:9200

4. 修改 `application.yml`：
   ```yaml
   spring:
     elasticsearch:
       enabled: true # 启用 Elasticsearch
       auto-sync: true # 启动时自动同步数据到 ES
   ```

### 方案三：配置远程 Elasticsearch

如果您有远程 Elasticsearch 服务，可以修改配置：

```yaml
spring:
  elasticsearch:
    enabled: true
    uris: your-elasticsearch-host:9200
    username: your-username # 如有需要
    password: your-password # 如有需要
```

## 数据同步

在禁用 ES 然后重新启用后，需要同步数据：

1. 通过 API 重建索引：

   ```
   POST /api/products/rebuild-index
   ```

2. 或在配置中启用自动同步：
   ```yaml
   spring:
     elasticsearch:
       enabled: true
       auto-sync: true
   ```

## 功能降级

当 Elasticsearch 不可用时，系统会自动降级：

1. 基础搜索回退到数据库查询
2. 高级搜索特性可能不可用或功能受限
3. 系统会记录警告日志，但会正常运行
4. 已添加熔断器机制，自动处理 ES 连接异常

## 故障排除

如果启用 Elasticsearch 后仍然遇到问题：

1. 检查 ES 服务是否运行：`curl http://localhost:9200`
2. 检查防火墙配置是否阻止了连接
3. 检查 ES 版本是否兼容 (推荐 7.17.x)
4. 查看应用日志中的具体错误信息

## 新增：使用 Resilience4j 熔断器

我们增加了 Resilience4j 熔断器配置来处理 ES 服务不稳定时的情况：

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
```

此配置可在 ES 连接异常时自动跳闸，避免过多失败请求拖慢系统。
