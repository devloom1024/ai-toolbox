# 账号管理功能完整实现

## ✅ 已实现功能清单

### 1. 🔍 **搜索和筛选功能**

#### 搜索
- **实时搜索**：输入账号名称即时过滤
- **搜索框位置**：工具栏左侧，带搜索图标
- **空状态优化**：搜索无结果时显示友好提示

#### 筛选
- **类型筛选**：下拉选择账号类型（券商、基金平台、银行、支付宝、其他）
- **全部类型**：默认选项，显示所有账号
- **筛选器位置**：搜索框右侧

**代码位置**：
```typescript
// 搜索状态
const [searchTerm, setSearchTerm] = useState('')
const [filterType, setFilterType] = useState<AccountType | 'ALL'>('ALL')

// 筛选逻辑
const filteredAndSortedAccounts = useMemo(() => {
  let result = [...accounts]
  if (searchTerm) {
    result = result.filter(account => 
      account.accountName.toLowerCase().includes(searchTerm.toLowerCase())
    )
  }
  if (filterType !== 'ALL') {
    result = result.filter(account => account.accountType === filterType)
  }
  // ... 排序逻辑
}, [accounts, searchTerm, filterType, sortField, sortOrder])
```

---

### 2. 📊 **表格排序功能**

#### 可排序字段
- ✅ 账号名称 (accountName)
- ✅ 总资产 (totalAssets)
- ✅ 持仓数量 (positionCount)
- ✅ 创建时间 (createdAt) - 默认排序

#### 排序交互
- **点击表头**：切换升序/降序
- **排序图标**：
  - 未排序：↕️ (ArrowUpDown)
  - 升序：↑ (ArrowUp)
  - 降序：↓ (ArrowDown)

**代码示例**：
```typescript
const handleSort = (field: SortField) => {
  if (sortField === field) {
    setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
  } else {
    setSortField(field)
    setSortOrder('asc')
  }
}
```

---

### 3. ✅ **批量操作功能**

#### 批量选择
- **全选复选框**：表头位置，一键全选/取消全选
- **单选复选框**：每行左侧
- **选中状态**：实时显示已选数量

#### 批量操作菜单
- **批量删除**：删除所有选中的账号
- **批量导出**：导出选中账号为 CSV
- **确认对话框**：批量删除前二次确认

#### 导出功能
- **导出全部**：工具栏"导出"按钮
- **导出选中**：批量操作菜单中
- **CSV 格式**：包含 BOM，支持中文
- **文件命名**：`accounts_YYYY-MM-DD.csv`

**代码示例**：
```typescript
const handleExport = () => {
  const dataToExport = selectedIds.size > 0 
    ? accounts.filter(a => selectedIds.has(a.id))
    : filteredAndSortedAccounts
  
  const csv = [
    ['账号类型', '账号名称', '持仓数', '总资产', '创建时间'],
    ...dataToExport.map(a => [
      dict.investment.account.types[a.accountType],
      a.accountName,
      a.positionCount.toString(),
      a.totalAssets.toString(),
      a.createdAt
    ])
  ].map(row => row.join(',')).join('\n')
  
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  // ... 下载逻辑
}
```

---

### 4. 🎴 **卡片/表格视图切换**

#### 视图模式
- **表格视图** (table)：默认，适合数据对比
- **卡片视图** (grid)：响应式网格布局

#### 切换控件
- **ToggleGroup**：工具栏右侧
- **图标**：
  - 表格：Table2
  - 卡片：LayoutGrid
- **持久化**：保存到 localStorage

#### 卡片视图特性
- **响应式布局**：
  - 移动端：1 列
  - 平板：2 列 (md)
  - 桌面：3 列 (lg)
- **卡片内容**：
  - 账号图标 + 名称
  - 账号类型 Badge
  - 总资产、持仓数
  - 更多操作菜单 (MoreVertical)
- **交互**：
  - 点击卡片：跳转详情页
  - 悬停效果：阴影加深
  - 复选框：左上角

**代码示例**：
```typescript
// 视图切换
<ToggleGroup type="single" value={viewMode} onValueChange={...}>
  <ToggleGroupItem value="table">
    <Table2 className="h-4 w-4" />
  </ToggleGroupItem>
  <ToggleGroupItem value="grid">
    <LayoutGrid className="h-4 w-4" />
  </ToggleGroupItem>
</ToggleGroup>

// 卡片视图渲染
{viewMode === 'grid' && (
  <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
    {filteredAndSortedAccounts.map(account => (
      <Card onClick={() => handleViewDetail(account.id)}>
        {/* 卡片内容 */}
      </Card>
    ))}
  </div>
)}
```

---

### 5. 📄 **账号详情页**

#### 路由
- **路径**：`/[locale]/investment/account/[id]`
- **跳转方式**：
  - 表格视图：点击"查看详情"按钮 (Eye 图标)
  - 卡片视图：点击整个卡片

#### 页面结构
1. **返回按钮**：返回账号列表
2. **账号标题区**：
   - 账号图标 (大尺寸)
   - 账号名称
   - 类型 Badge
   - 状态 Badge (活跃/未激活)
   - 操作按钮 (编辑、删除)
3. **统计卡片**：
   - 总资产
   - 持仓数量
   - 收益率 (占位符)
4. **账号信息卡片**：
   - 账号类型
   - 账号名称
   - 状态
   - 可见性
   - 创建时间
   - 排序
5. **持仓列表**：占位符，待实现

#### 功能
- ✅ 查看账号详细信息
- ✅ 编辑账号
- ✅ 删除账号（删除后返回列表）
- ⏳ 持仓管理（待实现）

---

## 🎨 UI/UX 优化

### 工具栏布局
```
┌─────────────────────────────────────────────────────────────┐
│ [搜索框...] [类型筛选▼] [表格/卡片切换] [已选N项▼] [导出]  │
└─────────────────────────────────────────────────────────────┘
```

### 空状态优化
- **无数据**：显示"暂无投资账号"，引导添加
- **搜索无结果**：显示"未找到匹配的账号"，提示调整条件

### 交互优化
- **Tooltip**：所有图标按钮都有提示
- **悬停效果**：表格行、卡片都有悬停反馈
- **加载状态**：骨架屏替代"加载中..."
- **确认对话框**：删除操作都有二次确认

---

## 📦 使用的组件

### Shadcn UI 组件
- ✅ Button
- ✅ Card
- ✅ Table
- ✅ Input
- ✅ Select
- ✅ Checkbox
- ✅ Badge
- ✅ Skeleton
- ✅ Tooltip
- ✅ ToggleGroup
- ✅ DropdownMenu
- ✅ AlertDialog
- ✅ Empty

### Lucide 图标
- Search, LayoutGrid, Table2
- ArrowUpDown, ArrowUp, ArrowDown
- Download, MoreVertical, Eye
- Plus, Pencil, Trash2
- Wallet, TrendingUp, BarChart3
- ArrowLeft

---

## 🚀 性能优化

### useMemo 优化
```typescript
const filteredAndSortedAccounts = useMemo(() => {
  // 搜索、筛选、排序逻辑
}, [accounts, searchTerm, filterType, sortField, sortOrder])
```

### localStorage 持久化
- 视图模式偏好
- 未来可扩展：排序偏好、筛选条件等

---

## 📝 待实现功能

1. ⏳ **持仓管理**：在详情页显示和管理持仓
2. ⏳ **高级筛选**：多条件组合筛选
3. ⏳ **分页**：数据量大时的分页支持
4. ⏳ **拖拽排序**：手动调整账号顺序
5. ⏳ **批量编辑**：批量修改账号属性

---

## 🎯 使用指南

### 搜索账号
1. 在搜索框输入账号名称
2. 实时显示匹配结果

### 筛选账号
1. 点击"筛选类型"下拉框
2. 选择账号类型
3. 与搜索条件组合生效

### 排序账号
1. 点击表头的排序按钮
2. 再次点击切换升序/降序

### 批量操作
1. 勾选需要操作的账号
2. 点击"已选 N 项"按钮
3. 选择"导出选中"或"批量删除"

### 切换视图
1. 点击工具栏的视图切换按钮
2. 选择表格或卡片视图
3. 偏好自动保存

### 查看详情
- **表格视图**：点击"查看详情"图标
- **卡片视图**：点击整个卡片

---

## 📸 功能截图说明

### 表格视图
- 左侧复选框用于批量选择
- 表头可点击排序
- 右侧操作按钮：查看、编辑、删除

### 卡片视图
- 响应式网格布局
- 左上角复选框
- 右上角更多操作菜单
- 点击卡片查看详情

### 详情页
- 顶部返回按钮
- 账号标题和操作
- 统计卡片
- 详细信息
- 持仓列表（占位符）

---

## 🔧 技术细节

### 类型定义
```typescript
type SortField = 'accountName' | 'totalAssets' | 'positionCount' | 'createdAt'
type SortOrder = 'asc' | 'desc'
type ViewMode = 'table' | 'grid'
```

### 状态管理
- 使用 React useState 管理本地状态
- 使用 useMemo 优化计算性能
- 使用 localStorage 持久化用户偏好

### 路由
- 使用 Next.js App Router
- 动态路由：`[id]`
- 国际化路由：`[locale]`

---

## ✨ 总结

所有请求的功能已全部实现：
1. ✅ 搜索和筛选功能
2. ✅ 表格排序功能
3. ✅ 批量操作（批量删除、导出）
4. ✅ 卡片视图切换
5. ✅ 账号详情页

页面功能完整、交互流畅、视觉美观，符合现代 Web 应用标准！🎉
