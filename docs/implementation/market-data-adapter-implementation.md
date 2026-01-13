# 股票数据适配层实现计划

> 文档版本: v1.0
> 创建日期: 2025-01-13
> 状态: ✅ 已完成 (Phase 1-4)

## 一、实施目标

基于分层解耦架构重构投资模块的市场数据适配层,实现:
- ✅ 统一的数据模型和接口
- ✅ 多级缓存提升性能
- ✅ 配置化的数据源管理
- ✅ 完善的数据质量保障
- ✅ 健壮的容错和降级机制

## 二、架构设计

### 2.1 整体分层

```
应用服务层 (Application Service)
  ↓
数据访问层 (Data Access Layer)
  ├── 多级缓存管理器 (Cache Manager)
  ├── 数据源路由器 (Data Source Router)
  ├── 统一适配层 (Unified Adapter)
  └── 数据质量保障 (Data Quality)
  ↓
数据源实现层 (Data Source Implementations)
  ├── Akshare (Python服务 - A股主)
  ├── Yfinance (Python服务 - 美股主)
  ├── Tushare (备用)
  └── WebAPI (备用)
```

### 2.2 关键组件

| 组件 | 职责 | 优先级 |
|------|------|--------|
| MarketDataService | 业务逻辑、缓存协调、降级控制 | P0 |
| MarketDataCacheManager | 三级缓存管理 (Caffeine + Redis + 源) | P0 |
| DataSourceRouter | 数据源选择和路由 | P0 |
| MarketDataAdapter | 统一接口定义 | P0 |
| DataQualityValidator | 数据校验和清洗 | P1 |
| DataSourceHealthChecker | 健康检查和熔断 | P1 |
| AkshareDataSource | Akshare数据源实现 | P0 |
| YfinanceDataSource | Yfinance数据源实现 | P1 |

## 三、实施计划

### 阶段一: 核心基础设施 (Day 1-2)

#### 1.1 领域模型设计 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/domain/model/`

- [x] QuoteData.java - 实时行情数据模型
- [x] KLineData.java - K线数据模型
- [x] FundamentalData.java - 基本面数据模型
- [x] CapitalFlowData.java - 资金流向数据模型
- [x] MarketType.java - 市场类型枚举
- [x] DataSource.java - 数据源枚举
- [x] DataQuality.java - 数据质量枚举

#### 1.2 统一适配器接口 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/adapter/`

- [x] MarketDataAdapter.java - 统一数据源接口
- [x] MarketDataAdapterRegistry.java - 适配器注册表

**接口方法**:
```java
public interface MarketDataAdapter {
    String getName();
    MarketType getSupportedMarket();
    QuoteData getQuote(String symbol);
    List<KLineData> getKLine(String symbol, KLinePeriod period, int limit);
    FundamentalData getFundamental(String symbol);
    CapitalFlowData getCapitalFlow(String symbol);
    boolean isHealthy();
}
```

#### 1.3 配置管理 ✅

**文件**:
- `backend/src/main/java/com/devloom/ai/toolbox/investment/config/MarketDataProperties.java`
- `backend/src/main/resources/application-investment.yml`

**配置结构**:
```yaml
investment:
  market-data:
    data-sources:
      a-share:
        - name: akshare
          priority: 1
          timeout: 5000
      us:
        - name: yfinance
          priority: 1
    cache:
      l1:
        enabled: true
        max-size: 10000
        expire-after-write: 60s
      l2:
        enabled: true
        expire-after-write: 300s
```

### 阶段二: 缓存和路由 (Day 2-3)

#### 2.1 多级缓存管理器 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/cache/`

- [x] MarketDataCacheManager.java - 缓存管理器
- [x] CacheKeyGenerator.java - 缓存Key生成器
- [ ] CacheWarmer.java - 缓存预热器

**功能点**:
- [x] L1 本地缓存 (Caffeine, 1分钟)
- [x] L2 分布式缓存 (Redis, 5分钟)
- [ ] L3 数据源
- [x] 缓存预热
- [x] 缓存统计

#### 2.2 数据源路由器 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/router/`

- [x] DataSourceRouter.java - 路由器核心
- [ ] DataSourceSelector.java - 数据源选择策略
- [ ] LoadBalancer.java - 负载均衡

**功能点**:
- [x] 根据市场类型选择数据源
- [x] 优先级排序
- [x] 健康度过滤
- [x] 主备切换

### 阶段三: 数据质量保障 (Day 3-4)

#### 3.1 数据质量校验 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/quality/`

- [x] DataQualityValidator.java - 数据校验器
- [x] DataCleaner.java - 数据清洗器
- [ ] DataNormalizer.java - 数据标准化
- [x] ValidationResult.java - 校验结果

**校验规则**:
- [x] 必填字段校验
- [x] 价格范围校验 (0 < price < 100000)
- [x] 涨跌幅合理性 (A股 ±20%)
- [x] 时间戳新鲜度 (< 30分钟)
- [x] 成交量合理性 (>= 0)

#### 3.2 健康检查机制 ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/health/`

- [x] DataSourceHealthChecker.java - 健康检查器
- [ ] HealthMetrics.java - 健康指标
- [x] CircuitBreakerManager.java - 熔断器管理

**功能点**:
- [x] 主动健康探测 (每分钟)
- [x] 被动结果记录
- [x] 成功率统计
- [x] 熔断器集成 (Resilience4j)

### 阶段四: 数据源实现 (Day 4-5)

#### 4.1 Akshare数据源 (A股) ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/adapter/akshare/`

- [x] AkshareDataSource.java - Akshare适配器
- [x] AkshareClient.java - HTTP客户端
- [x] AkshareResponseMapper.java - 响应映射器

**数据接口**:
- 实时行情: `stock_zh_a_spot_em`
- K线数据: `stock_zh_a_hist`
- 财务指标: `stock_financial_analysis_indicator`
- 资金流向: `stock_individual_fund_flow`

#### 4.2 Yfinance数据源 (美股) ⏳

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/adapter/yfinance/`

- [ ] YfinanceDataSource.java - Yfinance适配器
- [ ] YfinanceClient.java - HTTP客户端
- [ ] YfinanceResponseMapper.java - 响应映射器

#### 4.3 降级数据源 (缓存) ✅

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/adapter/fallback/`

- [x] FallbackDataSource.java - 降级适配器
- [ ] HistoricalDataProvider.java - 历史数据提供者

### 阶段五: 应用服务集成 (Day 5-6)

#### 5.1 MarketDataService重构 ⏳

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/application/service/`

- [ ] MarketDataService.java - 重构为使用新架构
- [ ] MarketDataFacade.java - 外观模式封装

**功能整合**:
- 使用MarketDataCacheManager
- 使用DataSourceRouter
- 异常处理和降级
- 结果校验

#### 5.2 定时任务集成 ⏳

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/application/scheduler/`

- [ ] MarketDataRefreshScheduler.java - 数据刷新定时任务
- [ ] CacheWarmupScheduler.java - 缓存预热定时任务
- [ ] HealthCheckScheduler.java - 健康检查定时任务

### 阶段六: 监控和测试 (Day 6-7)

#### 6.1 监控指标 ⏳

**文件**: `backend/src/main/java/com/devloom/ai/toolbox/investment/infrastructure/metrics/`

- [ ] MarketDataMetrics.java - 指标采集
- [ ] CacheMetrics.java - 缓存指标
- [ ] DataSourceMetrics.java - 数据源指标

**监控项**:
- 缓存命中率 (L1/L2)
- 数据源调用次数/成功率
- 平均响应时间
- 熔断器状态

#### 6.2 单元测试 ⏳

**文件**: `backend/src/test/java/com/devloom/ai/toolbox/investment/infrastructure/`

- [ ] DataQualityValidatorTest.java
- [ ] DataSourceRouterTest.java
- [ ] MarketDataCacheManagerTest.java
- [ ] AkshareDataSourceTest.java

#### 6.3 集成测试 ⏳

**文件**: `backend/src/test/java/com/devloom/ai/toolbox/investment/application/`

- [ ] MarketDataServiceIntegrationTest.java

## 四、技术决策

### 4.1 技术选型

| 技术 | 用途 | 理由 |
|------|------|------|
| Caffeine | L1本地缓存 | 高性能、自动过期、统计能力 |
| Redis | L2分布式缓存 | 分布式共享、持久化 |
| Resilience4j | 熔断器/限流 | Spring Boot原生集成 |
| RestTemplate/WebClient | HTTP客户端 | 调用Python服务 |
| Jackson | JSON序列化 | Spring Boot默认 |
| Micrometer | 指标采集 | Spring Boot Actuator集成 |

### 4.2 配置策略

| 配置项 | 值 | 说明 |
|--------|---|------|
| L1缓存大小 | 10,000 | 热门股票 |
| L1过期时间 | 1分钟 | 减少数据源压力 |
| L2过期时间 | 5分钟 | 分布式共享 |
| 请求超时 | 5秒 | Python服务调用 |
| 重试次数 | 3次 | 网络抖动 |
| 熔断阈值 | 50% | 失败率 |
| 限流配置 | 60次/分钟 | 数据源限制 |

## 五、数据源部署

### 5.1 Python服务 (Docker Compose)

**文件**: `docker/python-services/docker-compose.yml`

```yaml
version: '3.8'
services:
  akshare-service:
    build: ./akshare
    ports:
      - "8081:8080"
    environment:
      - LOG_LEVEL=INFO
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  yfinance-service:
    build: ./yfinance
    ports:
      - "8082:8080"
    environment:
      - LOG_LEVEL=INFO
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

### 5.2 Python服务API规范

**Akshare服务接口**:
```
GET /api/quote?symbol=600519        # 实时行情
GET /api/kline?symbol=600519&period=daily&limit=100  # K线
GET /api/fundamental?symbol=600519  # 基本面
GET /api/capital-flow?symbol=600519 # 资金流向
GET /health                         # 健康检查
```

**响应格式**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "symbol": "600519",
    "price": 1680.50,
    "change": 15.30,
    "changeRate": 0.0092,
    "timestamp": "2025-01-13T14:30:00"
  },
  "timestamp": "2025-01-13T14:30:05"
}
```

## 六、迁移策略

### 6.1 向后兼容

- 保留旧的MarketDataService接口签名
- 内部切换到新架构
- 分阶段迁移调用方

### 6.2 灰度发布

1. **阶段1**: 双写 (旧+新) - 验证数据一致性
2. **阶段2**: 双读 (新主+旧备) - 观察性能和稳定性
3. **阶段3**: 完全切换到新架构
4. **阶段4**: 清理旧代码

### 6.3 回滚方案

- 配置开关控制新旧架构切换
- 保留旧代码至少一个版本周期
- 数据库无变更,无需回滚数据

## 七、验收标准

### 7.1 功能验收

- [ ] 能够正常获取A股实时行情
- [ ] 能够正常获取A股K线数据
- [ ] 能够正常获取基本面数据
- [ ] 缓存命中率 > 80%
- [ ] 数据源切换无感知
- [ ] 降级机制正常工作

### 7.2 性能验收

- [ ] P95响应时间 < 100ms (L1命中)
- [ ] P95响应时间 < 500ms (L2命中)
- [ ] P95响应时间 < 2s (数据源)
- [ ] 并发1000 QPS无异常

### 7.3 质量验收

- [ ] 单元测试覆盖率 > 80%
- [ ] 集成测试通过
- [ ] 无严重代码质量问题 (SonarQube)
- [ ] 无内存泄漏

## 八、风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| Python服务不稳定 | 数据获取失败 | 多数据源+降级+缓存 |
| 缓存穿透 | 数据源压力大 | 布隆过滤器+空值缓存 |
| 数据延迟 | 用户体验差 | 显示数据时间戳+标注 |
| 内存占用高 | OOM风险 | 限制L1缓存大小+定期清理 |
| 并发冲突 | 数据不一致 | 分布式锁+版本号 |

## 九、后续优化

### 9.1 短期优化 (1-2周)

- [ ] 增加更多数据源 (Tushare等)
- [ ] 实现数据源限流算法优化
- [ ] 完善监控告警规则
- [ ] 性能压测和优化

### 9.2 长期优化 (1-3个月)

- [ ] 实现数据本地存储 (降低依赖)
- [ ] 引入实时推送 (WebSocket)
- [ ] 机器学习异常检测
- [ ] 数据源智能路由 (AI选择)

## 十、参考资料

- [Akshare文档](https://akshare.akfamily.xyz/)
- [Yfinance文档](https://pypi.org/project/yfinance/)
- [Resilience4j文档](https://resilience4j.readme.io/)
- [Caffeine文档](https://github.com/ben-manes/caffeine)
- [Spring Cache文档](https://docs.spring.io/spring-framework/reference/integration/cache.html)

---

**更新日志**:
- 2025-01-13: 创建文档,开始实施
