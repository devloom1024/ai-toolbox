# 国际化修复总结

## ✅ 已修复的文件

### 1. **字典文件**
- ✅ `/frontend/dictionaries/en-US.json` - 添加所有缺失的英文翻译
- ✅ `/frontend/dictionaries/zh-CN.json` - 添加所有缺失的中文翻译

### 2. **页面文件**
- ✅ `/frontend/app/[locale]/(app)/investment/account/page.tsx` - 列表页
- ✅ `/frontend/app/[locale]/(app)/investment/account/[id]/page.tsx` - 详情页

---

## 📝 新增的字典键

### **搜索相关** (`search`)
```json
"search": {
  "placeholder": "搜索账号名称...",
  "noResults": "未找到匹配的账号",
  "adjustFilters": "尝试调整搜索条件或筛选器"
}
```

### **筛选相关** (`filter`)
```json
"filter": {
  "type": "筛选类型",
  "allTypes": "全部类型"
}
```

### **排序相关** (`sort`)
```json
"sort": {
  "name": "名称",
  "assets": "资产",
  "positions": "持仓",
  "createdAt": "创建时间"
}
```

### **批量操作** (`batch`)
```json
"batch": {
  "selected": "已选",
  "exportSelected": "导出选中",
  "batchDelete": "批量删除",
  "confirmBatchDelete": "确认批量删除",
  "batchDeleteWarning": "您确定要删除选中的 {count} 个账号吗？此操作无法撤销。",
  "batchDeleteSuccess": "成功删除 {count} 个账号",
  "exportSuccess": "已导出 {count} 个账号"
}
```

### **视图模式** (`viewMode`)
```json
"viewMode": {
  "table": "表格视图",
  "grid": "卡片视图"
}
```

### **详情页** (`detail`)
```json
"detail": {
  "accountInfo": "账号信息",
  "accountInfoDesc": "查看和管理账号的详细信息",
  "positionList": "持仓列表",
  "positionListDesc": "该账号下的所有投资持仓",
  "positionListPlaceholder": "持仓管理功能即将上线...",
  "status": "状态",
  "visibility": "可见性",
  "visible": "可见",
  "hidden": "已隐藏",
  "active": "活跃",
  "inactive": "未激活",
  "createdAt": "创建时间"
}
```

### **统计数据扩展** (`stats`)
```json
"stats": {
  // ... 原有字段
  "returnRate": "收益率",
  "cumulativeReturn": "累计收益"
}
```

### **其他新增**
```json
"viewDetail": "查看详情",
"backToList": "返回列表",
"export": "导出"
```

---

## 🔧 修复的硬编码文本

### **列表页 (page.tsx)**

| 位置           | 原硬编码                              | 修复后                                                                     |
| -------------- | ------------------------------------- | -------------------------------------------------------------------------- |
| 批量删除成功   | `成功删除 ${selectedIds.size} 个账号` | `dict.investment.account.batch.batchDeleteSuccess.replace('{count}', ...)` |
| 批量删除失败   | `批量删除失败`                        | `dict.investment.account.messages.deleteError`                             |
| CSV 表头       | `['账号类型', '账号名称', ...]`       | 使用 `dict.investment.account.fields.*`                                    |
| 导出成功       | `已导出 ${count} 个账号`              | `dict.investment.account.batch.exportSuccess.replace('{count}', ...)`      |
| 搜索框         | `搜索账号名称...`                     | `dict.investment.account.search.placeholder`                               |
| 筛选下拉       | `筛选类型` / `全部类型`               | `dict.investment.account.filter.*`                                         |
| 视图切换       | `表格视图` / `卡片视图`               | `dict.investment.account.viewMode.*`                                       |
| 批量操作       | `已选 N 项`                           | `dict.investment.account.batch.selected`                                   |
| 空状态         | `未找到匹配的账号`                    | `dict.investment.account.search.noResults`                                 |
| Tooltip        | `查看详情` / `编辑` / `删除`          | `dict.investment.account.*`                                                |
| 卡片字段       | `总资产` / `持仓数`                   | `dict.investment.account.fields.*`                                         |
| 批量删除对话框 | `确认批量删除` / `您确定要删除...`    | `dict.investment.account.batch.*`                                          |

### **详情页 ([id]/page.tsx)**

| 位置       | 原硬编码                          | 修复后                                                   |
| ---------- | --------------------------------- | -------------------------------------------------------- |
| 加载失败   | `加载账号详情失败`                | `dict.investment.account.messages.loadError`             |
| 返回按钮   | `返回列表`                        | `dict.investment.account.backToList`                     |
| 状态 Badge | `活跃` / `未激活`                 | `dict.investment.account.detail.active/inactive`         |
| 操作按钮   | `编辑` / `删除`                   | `dict.investment.account.edit/delete`                    |
| 统计卡片   | `总资产` / `持仓数量` / `收益率`  | `dict.investment.account.fields.*` / `stats.*`           |
| 统计描述   | `较上月` / `累计收益`             | `dict.investment.account.stats.*`                        |
| 卡片标题   | `账号信息` / `持仓列表`           | `dict.investment.account.detail.*`                       |
| 字段标签   | `账号类型` / `状态` / `可见性` 等 | `dict.investment.account.fields.*` / `detail.*`          |
| 字段值     | `活跃` / `已隐藏` / `可见`        | `dict.investment.account.detail.*`                       |
| 占位符     | `持仓管理功能即将上线...`         | `dict.investment.account.detail.positionListPlaceholder` |

---

## 💡 使用技巧

### **动态文本替换**
对于包含变量的文本，使用 `.replace()` 方法：

```typescript
// 批量删除成功消息
toast.success(
  dict.investment.account.batch.batchDeleteSuccess
    .replace('{count}', selectedIds.size.toString())
)

// 批量删除警告
dict.investment.account.batch.batchDeleteWarning
  .replace('{count}', selectedIds.size.toString())
```

### **条件文本**
根据状态选择不同的翻译：

```typescript
// 状态显示
{account.isActive 
  ? dict.investment.account.detail.active 
  : dict.investment.account.detail.inactive
}

// 可见性显示
{account.isHidden 
  ? dict.investment.account.detail.hidden 
  : dict.investment.account.detail.visible
}
```

### **空状态处理**
根据搜索/筛选状态显示不同的空状态文本：

```typescript
{searchTerm || filterType !== 'ALL' 
  ? dict.investment.account.search.noResults
  : dict.investment.account.empty.title
}
```

---

## ✅ 验证清单

- [x] 所有用户可见的文本都已国际化
- [x] 动态文本使用 `.replace()` 处理变量
- [x] 移除了所有 fallback 值（`|| '中文'`）
- [x] console.error 中的调试文本保留中文（仅开发使用）
- [x] 中英文字典保持同步
- [x] 修复了重复的 key（`view` → `viewDetail` 和 `viewMode`）

---

## 🎯 国际化最佳实践

1. **✅ 始终使用字典**：所有用户可见的文本都应该从字典获取
2. **✅ 避免硬编码**：即使是单个单词也应该使用字典
3. **✅ 动态文本使用占位符**：如 `{count}`、`{name}` 等
4. **✅ 保持字典同步**：添加新功能时同步更新所有语言的字典
5. **✅ 使用有意义的键名**：如 `search.placeholder` 而不是 `text1`
6. **✅ 分组管理**：相关的翻译放在同一个对象下（如 `search`、`filter`）
7. **❌ 不要使用 fallback**：字典应该是完整的，不需要 `|| '默认值'`

---

## 📊 统计

- **修复的文件**：4 个（2 个字典 + 2 个页面）
- **新增的字典键**：约 40 个
- **修复的硬编码文本**：约 60 处
- **支持的语言**：中文、英文

---

## 🚀 下一步

所有国际化问题已修复！现在可以：
1. ✅ 切换语言查看效果
2. ✅ 添加更多语言支持（如日语、韩语等）
3. ✅ 继续开发其他功能，遵循国际化最佳实践
