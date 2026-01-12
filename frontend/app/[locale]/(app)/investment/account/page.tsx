'use client'

import { useState, useEffect, useMemo } from 'react'
import { useTranslation } from '@/lib/i18n-client'
import { Button } from '@/components/ui/button'
import {
    Card,
    CardContent,
    CardHeader,
    CardTitle,
} from '@/components/ui/card'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle,
} from '@/components/ui/alert-dialog'
import {
    Tooltip,
    TooltipContent,
    TooltipProvider,
    TooltipTrigger,
} from '@/components/ui/tooltip'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import {
    Empty,
    EmptyHeader,
    EmptyTitle,
    EmptyDescription,
    EmptyContent,
    EmptyMedia,
} from '@/components/ui/empty'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Input } from '@/components/ui/input'
import { Checkbox } from '@/components/ui/checkbox'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import {
    Plus,
    Pencil,
    Trash2,
    Wallet,
    TrendingUp,
    BarChart3,
    Search,
    LayoutGrid,
    Table2,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
    Download,
    MoreVertical,
    Eye
} from 'lucide-react'
import { accountApi, AccountResponse, AccountCreateRequest, AccountUpdateRequest, AccountType } from '@/lib/api/account'
import { AccountFormDialog } from '@/components/account-form-dialog'
import { PageContainer } from '@/components/page-container'
import { toast } from 'sonner'
import { useRouter } from 'next/navigation'
import { useLocale } from '@/hooks/use-locale'

type SortField = 'accountName' | 'totalAssets' | 'positionCount' | 'createdAt'
type SortOrder = 'asc' | 'desc'
type ViewMode = 'table' | 'grid'

export default function AccountManagementPage() {
    const dict = useTranslation()
    const router = useRouter()
    const locale = useLocale()

    const [accounts, setAccounts] = useState<AccountResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [formOpen, setFormOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [batchDeleteDialogOpen, setBatchDeleteDialogOpen] = useState(false)
    const [selectedAccount, setSelectedAccount] = useState<AccountResponse | undefined>()
    const [accountToDelete, setAccountToDelete] = useState<number | null>(null)

    // 搜索和筛选
    const [searchTerm, setSearchTerm] = useState('')
    const [filterType, setFilterType] = useState<AccountType | 'ALL'>('ALL')

    // 排序
    const [sortField, setSortField] = useState<SortField>('createdAt')
    const [sortOrder, setSortOrder] = useState<SortOrder>('desc')

    // 视图模式
    const [viewMode, setViewMode] = useState<ViewMode>('table')

    // 批量选择
    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set())

    // 加载账号列表
    const loadAccounts = async () => {
        try {
            setLoading(true)
            const response = await accountApi.getAccountList(false)
            setAccounts(response.data || [])
        } catch (error) {
            console.error('加载账号列表失败:', error)
            toast.error(dict.investment.account.messages.loadError || '加载账号列表失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadAccounts()
        // 从 localStorage 恢复视图模式
        const savedViewMode = localStorage.getItem('account-view-mode') as ViewMode
        if (savedViewMode) setViewMode(savedViewMode)
    }, [])

    // 保存视图模式到 localStorage
    useEffect(() => {
        localStorage.setItem('account-view-mode', viewMode)
    }, [viewMode])

    // 筛选和排序后的账号列表
    const filteredAndSortedAccounts = useMemo(() => {
        let result = [...accounts]

        // 搜索过滤
        if (searchTerm) {
            result = result.filter(account =>
                account.accountName.toLowerCase().includes(searchTerm.toLowerCase())
            )
        }

        // 类型过滤
        if (filterType !== 'ALL') {
            result = result.filter(account => account.accountType === filterType)
        }

        // 排序
        result.sort((a, b) => {
            let aValue: any = a[sortField]
            let bValue: any = b[sortField]

            if (sortField === 'createdAt') {
                aValue = new Date(aValue).getTime()
                bValue = new Date(bValue).getTime()
            }

            if (aValue < bValue) return sortOrder === 'asc' ? -1 : 1
            if (aValue > bValue) return sortOrder === 'asc' ? 1 : -1
            return 0
        })

        return result
    }, [accounts, searchTerm, filterType, sortField, sortOrder])

    // 创建账号
    const handleCreate = async (data: AccountCreateRequest) => {
        try {
            await accountApi.createAccount(data)
            toast.success(dict.investment.account.messages.createSuccess)
            loadAccounts()
        } catch (error) {
            console.error('创建账号失败:', error)
            toast.error(dict.investment.account.messages.createError || '创建账号失败')
            throw error
        }
    }

    // 更新账号
    const handleUpdate = async (data: AccountUpdateRequest) => {
        if (!selectedAccount) return
        try {
            await accountApi.updateAccount(selectedAccount.id, data)
            toast.success(dict.investment.account.messages.updateSuccess)
            loadAccounts()
        } catch (error) {
            console.error('更新账号失败:', error)
            toast.error(dict.investment.account.messages.updateError || '更新账号失败')
            throw error
        }
    }

    // 删除账号
    const handleDelete = async () => {
        if (!accountToDelete) return
        try {
            await accountApi.deleteAccount(accountToDelete)
            toast.success(dict.investment.account.messages.deleteSuccess)
            setDeleteDialogOpen(false)
            setAccountToDelete(null)
            loadAccounts()
        } catch (error) {
            console.error('删除账号失败:', error)
            toast.error(dict.investment.account.messages.deleteError || '删除账号失败')
        }
    }

    // 批量删除
    const handleBatchDelete = async () => {
        try {
            await Promise.all(
                Array.from(selectedIds).map(id => accountApi.deleteAccount(id))
            )
            toast.success(dict.investment.account.batch.batchDeleteSuccess.replace('{count}', selectedIds.size.toString()))
            setBatchDeleteDialogOpen(false)
            setSelectedIds(new Set())
            loadAccounts()
        } catch (error) {
            console.error('批量删除失败:', error)
            toast.error(dict.investment.account.messages.deleteError)
        }
    }

    // 导出数据
    const handleExport = () => {
        const dataToExport = selectedIds.size > 0
            ? accounts.filter(a => selectedIds.has(a.id))
            : filteredAndSortedAccounts

        const csv = [
            [
                dict.investment.account.fields.accountType,
                dict.investment.account.fields.accountName,
                dict.investment.account.fields.positionCount,
                dict.investment.account.fields.totalAssets,
                dict.investment.account.detail.createdAt
            ],
            ...dataToExport.map(a => [
                dict.investment.account.types[a.accountType],
                a.accountName,
                a.positionCount.toString(),
                a.totalAssets.toString(),
                a.createdAt
            ])
        ].map(row => row.join(',')).join('\n')

        const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = `accounts_${new Date().toISOString().split('T')[0]}.csv`
        link.click()

        toast.success(dict.investment.account.batch.exportSuccess.replace('{count}', dataToExport.length.toString()))
    }

    // 打开编辑对话框
    const handleEdit = (account: AccountResponse) => {
        setSelectedAccount(account)
        setFormOpen(true)
    }

    // 打开删除确认对话框
    const handleDeleteClick = (id: number) => {
        setAccountToDelete(id)
        setDeleteDialogOpen(true)
    }

    // 打开创建对话框
    const handleAddClick = () => {
        setSelectedAccount(undefined)
        setFormOpen(true)
    }

    // 查看详情
    const handleViewDetail = (id: number) => {
        router.push(`/${locale}/investment/account/${id}`)
    }

    // 切换排序
    const handleSort = (field: SortField) => {
        if (sortField === field) {
            setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
        } else {
            setSortField(field)
            setSortOrder('asc')
        }
    }

    // 全选/取消全选
    const handleSelectAll = (checked: boolean) => {
        if (checked) {
            setSelectedIds(new Set(filteredAndSortedAccounts.map(a => a.id)))
        } else {
            setSelectedIds(new Set())
        }
    }

    // 切换单个选择
    const handleSelectOne = (id: number, checked: boolean) => {
        const newSet = new Set(selectedIds)
        if (checked) {
            newSet.add(id)
        } else {
            newSet.delete(id)
        }
        setSelectedIds(newSet)
    }

    // 计算统计数据
    const totalAccounts = accounts.length
    const totalAssets = accounts.reduce((sum, acc) => sum + acc.totalAssets, 0)
    const totalPositions = accounts.reduce((sum, acc) => sum + acc.positionCount, 0)
    const activeAccounts = accounts.filter(a => a.isActive).length

    // Badge 颜色映射
    const accountTypeBadgeVariant: Record<AccountType, 'default' | 'secondary' | 'outline' | 'destructive'> = {
        BROKER: 'default',
        FUND_PLATFORM: 'secondary',
        BANK: 'outline',
        ALIPAY: 'destructive',
        OTHER: 'outline'
    }

    // 排序图标
    const SortIcon = ({ field }: { field: SortField }) => {
        if (sortField !== field) return <ArrowUpDown className="ml-2 h-4 w-4" />
        return sortOrder === 'asc'
            ? <ArrowUp className="ml-2 h-4 w-4" />
            : <ArrowDown className="ml-2 h-4 w-4" />
    }

    return (
        <PageContainer>
            {/* 统计概览卡片 */}
            {!loading && accounts.length > 0 && (
                <div className="grid gap-4 md:grid-cols-3 mb-6">
                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">
                                {dict.investment.account.stats?.totalAccounts || '总账号数'}
                            </CardTitle>
                            <Wallet className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{totalAccounts}</div>
                            <p className="text-xs text-muted-foreground">
                                {dict.investment.account.stats?.activeAccounts || '活跃账号'} {activeAccounts} {dict.investment.account.stats?.accountsUnit || '个'}
                            </p>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">
                                {dict.investment.account.stats?.totalAssets || '总资产'}
                            </CardTitle>
                            <TrendingUp className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">
                                ¥{totalAssets.toLocaleString('zh-CN', {
                                    minimumFractionDigits: 2,
                                    maximumFractionDigits: 2
                                })}
                            </div>
                            <p className="text-xs text-muted-foreground">
                                {dict.investment.account.stats?.assetsChange || '较上月'} +0.00%
                            </p>
                        </CardContent>
                    </Card>

                    <Card>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                            <CardTitle className="text-sm font-medium">
                                {dict.investment.account.stats?.totalPositions || '总持仓数'}
                            </CardTitle>
                            <BarChart3 className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>
                        <CardContent>
                            <div className="text-2xl font-bold">{totalPositions}</div>
                            <p className="text-xs text-muted-foreground">
                                {dict.investment.account.stats?.distributedIn || '分布在'} {totalAccounts} {dict.investment.account.stats?.accountsUnit || '个账号'}
                            </p>
                        </CardContent>
                    </Card>
                </div>
            )}

            {/* 工具栏 - 整合所有操作 */}
            {!loading && accounts.length > 0 && (
                <div className="flex flex-col sm:flex-row gap-4 mb-4">
                    {/* 左侧：搜索、筛选、视图切换 */}
                    <div className="flex flex-1 gap-2 flex-wrap">
                        {/* 搜索框 */}
                        <div className="relative flex-1 min-w-[200px]">
                            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                            <Input
                                placeholder={dict.investment.account.search.placeholder}
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                className="pl-9"
                            />
                        </div>

                        {/* 类型筛选 */}
                        <Select value={filterType} onValueChange={(value) => setFilterType(value as AccountType | 'ALL')}>
                            <SelectTrigger className="w-[160px]">
                                <SelectValue placeholder={dict.investment.account.filter.type} />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">{dict.investment.account.filter.allTypes}</SelectItem>
                                <SelectItem value={AccountType.BROKER}>{dict.investment.account.types.BROKER}</SelectItem>
                                <SelectItem value={AccountType.FUND_PLATFORM}>{dict.investment.account.types.FUND_PLATFORM}</SelectItem>
                                <SelectItem value={AccountType.BANK}>{dict.investment.account.types.BANK}</SelectItem>
                                <SelectItem value={AccountType.ALIPAY}>{dict.investment.account.types.ALIPAY}</SelectItem>
                                <SelectItem value={AccountType.OTHER}>{dict.investment.account.types.OTHER}</SelectItem>
                            </SelectContent>
                        </Select>

                        {/* 视图切换 */}
                        <ToggleGroup type="single" value={viewMode} onValueChange={(value) => value && setViewMode(value as ViewMode)}>
                            <ToggleGroupItem value="table" aria-label={dict.investment.account.viewMode.table}>
                                <Table2 className="h-4 w-4" />
                            </ToggleGroupItem>
                            <ToggleGroupItem value="grid" aria-label={dict.investment.account.viewMode.grid}>
                                <LayoutGrid className="h-4 w-4" />
                            </ToggleGroupItem>
                        </ToggleGroup>
                    </div>

                    {/* 右侧：批量操作、导出、添加 */}
                    <div className="flex gap-2">
                        {/* 批量操作 */}
                        {selectedIds.size > 0 && (
                            <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                    <Button variant="outline">
                                        {dict.investment.account.batch.selected} {selectedIds.size}
                                    </Button>
                                </DropdownMenuTrigger>
                                <DropdownMenuContent align="end">
                                    <DropdownMenuItem onClick={handleExport}>
                                        <Download className="mr-2 h-4 w-4" />
                                        {dict.investment.account.batch.exportSelected}
                                    </DropdownMenuItem>
                                    <DropdownMenuSeparator />
                                    <DropdownMenuItem
                                        onClick={() => setBatchDeleteDialogOpen(true)}
                                        className="text-destructive"
                                    >
                                        <Trash2 className="mr-2 h-4 w-4" />
                                        {dict.investment.account.batch.batchDelete}
                                    </DropdownMenuItem>
                                </DropdownMenuContent>
                            </DropdownMenu>
                        )}

                        {/* 导出 */}
                        <Button variant="outline" onClick={handleExport}>
                            <Download className="mr-2 h-4 w-4" />
                            {dict.investment.account.export}
                        </Button>

                        {/* 添加账号 */}
                        <Button onClick={handleAddClick}>
                            <Plus className="mr-2 h-4 w-4" />
                            {dict.investment.account.addAccount}
                        </Button>
                    </div>
                </div>
            )}

            {/* 内容区域 */}
            {loading ? (
                // 加载骨架屏
                <div className="rounded-md border bg-card shadow-sm">
                    <Table>
                        <TableHeader>
                            <TableRow className="hover:bg-transparent">
                                <TableHead className="w-12"></TableHead>
                                <TableHead>{dict.investment.account.fields.accountType}</TableHead>
                                <TableHead>{dict.investment.account.fields.accountName}</TableHead>
                                <TableHead>{dict.investment.account.fields.positionCount}</TableHead>
                                <TableHead>{dict.investment.account.fields.totalAssets}</TableHead>
                                <TableHead className="text-right">
                                    {dict.investment.account.actions || '操作'}
                                </TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {[...Array(5)].map((_, i) => (
                                <TableRow key={i}>
                                    <TableCell><Skeleton className="h-4 w-4" /></TableCell>
                                    <TableCell><Skeleton className="h-5 w-20" /></TableCell>
                                    <TableCell><Skeleton className="h-5 w-32" /></TableCell>
                                    <TableCell><Skeleton className="h-5 w-12" /></TableCell>
                                    <TableCell><Skeleton className="h-5 w-24" /></TableCell>
                                    <TableCell className="text-right">
                                        <Skeleton className="h-8 w-16 ml-auto" />
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            ) : filteredAndSortedAccounts.length === 0 ? (
                // 空状态
                <Empty className="border bg-card shadow-sm">
                    <EmptyHeader>
                        <EmptyMedia variant="icon">
                            <Wallet />
                        </EmptyMedia>
                        <EmptyTitle>
                            {searchTerm || filterType !== 'ALL'
                                ? dict.investment.account.search.noResults
                                : dict.investment.account.empty.title
                            }
                        </EmptyTitle>
                        <EmptyDescription>
                            {searchTerm || filterType !== 'ALL'
                                ? dict.investment.account.search.adjustFilters
                                : dict.investment.account.empty.description
                            }
                        </EmptyDescription>
                    </EmptyHeader>
                    {!searchTerm && filterType === 'ALL' && (
                        <EmptyContent>
                            <Button onClick={handleAddClick}>
                                <Plus className="mr-2 h-4 w-4" />
                                {dict.investment.account.addAccount}
                            </Button>
                        </EmptyContent>
                    )}
                </Empty>
            ) : viewMode === 'table' ? (
                // 表格视图
                <div className="rounded-md border bg-card shadow-sm">
                    <Table>
                        <TableHeader>
                            <TableRow className="hover:bg-transparent">
                                <TableHead className="w-12">
                                    <Checkbox
                                        checked={selectedIds.size === filteredAndSortedAccounts.length}
                                        onCheckedChange={handleSelectAll}
                                    />
                                </TableHead>
                                <TableHead>{dict.investment.account.fields.accountType}</TableHead>
                                <TableHead>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="-ml-3 h-8"
                                        onClick={() => handleSort('accountName')}
                                    >
                                        {dict.investment.account.fields.accountName}
                                        <SortIcon field="accountName" />
                                    </Button>
                                </TableHead>
                                <TableHead>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="-ml-3 h-8"
                                        onClick={() => handleSort('positionCount')}
                                    >
                                        {dict.investment.account.fields.positionCount}
                                        <SortIcon field="positionCount" />
                                    </Button>
                                </TableHead>
                                <TableHead>
                                    <Button
                                        variant="ghost"
                                        size="sm"
                                        className="-ml-3 h-8"
                                        onClick={() => handleSort('totalAssets')}
                                    >
                                        {dict.investment.account.fields.totalAssets}
                                        <SortIcon field="totalAssets" />
                                    </Button>
                                </TableHead>
                                <TableHead className="text-right">
                                    {dict.investment.account.actions || '操作'}
                                </TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {filteredAndSortedAccounts.map((account) => (
                                <TableRow
                                    key={account.id}
                                    className="hover:bg-muted/50 transition-colors"
                                >
                                    <TableCell>
                                        <Checkbox
                                            checked={selectedIds.has(account.id)}
                                            onCheckedChange={(checked) => handleSelectOne(account.id, checked as boolean)}
                                        />
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={accountTypeBadgeVariant[account.accountType]}>
                                            {dict.investment.account.types[account.accountType]}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex items-center gap-2">
                                            {account.accountIcon && (
                                                <img
                                                    src={account.accountIcon}
                                                    alt=""
                                                    className="h-6 w-6 rounded-full"
                                                />
                                            )}
                                            <span className="font-medium">{account.accountName}</span>
                                        </div>
                                    </TableCell>
                                    <TableCell className="text-muted-foreground">
                                        {account.positionCount}
                                    </TableCell>
                                    <TableCell className="font-mono font-semibold tabular-nums">
                                        ¥{account.totalAssets.toLocaleString('zh-CN', {
                                            minimumFractionDigits: 2,
                                            maximumFractionDigits: 2
                                        })}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <div className="flex justify-end gap-1">
                                            <TooltipProvider>
                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            onClick={() => handleViewDetail(account.id)}
                                                        >
                                                            <Eye className="h-4 w-4" />
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>{dict.investment.account.viewDetail}</TooltipContent>
                                                </Tooltip>

                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            onClick={() => handleEdit(account)}
                                                        >
                                                            <Pencil className="h-4 w-4" />
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>
                                                        {dict.investment.account.edit}
                                                    </TooltipContent>
                                                </Tooltip>

                                                <Tooltip>
                                                    <TooltipTrigger asChild>
                                                        <Button
                                                            variant="ghost"
                                                            size="icon"
                                                            onClick={() => handleDeleteClick(account.id)}
                                                            className="text-destructive hover:text-destructive hover:bg-destructive/10"
                                                        >
                                                            <Trash2 className="h-4 w-4" />
                                                        </Button>
                                                    </TooltipTrigger>
                                                    <TooltipContent>
                                                        {dict.investment.account.delete}
                                                    </TooltipContent>
                                                </Tooltip>
                                            </TooltipProvider>
                                        </div>
                                    </TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </div>
            ) : (
                // 卡片视图
                <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                    {filteredAndSortedAccounts.map((account) => (
                        <Card
                            key={account.id}
                            className="hover:shadow-md transition-shadow cursor-pointer relative"
                            onClick={() => handleViewDetail(account.id)}
                        >
                            <div className="absolute top-4 left-4">
                                <Checkbox
                                    checked={selectedIds.has(account.id)}
                                    onCheckedChange={(checked) => handleSelectOne(account.id, checked as boolean)}
                                    onClick={(e) => e.stopPropagation()}
                                />
                            </div>
                            <CardHeader className="pb-3 pt-12">
                                <div className="flex items-start justify-between">
                                    <div className="flex items-center gap-3 flex-1">
                                        {account.accountIcon && (
                                            <img src={account.accountIcon} className="h-10 w-10 rounded-full" alt="" />
                                        )}
                                        <div className="flex-1 min-w-0">
                                            <CardTitle className="text-base truncate">{account.accountName}</CardTitle>
                                            <Badge variant={accountTypeBadgeVariant[account.accountType]} className="mt-1">
                                                {dict.investment.account.types[account.accountType]}
                                            </Badge>
                                        </div>
                                    </div>
                                    <DropdownMenu>
                                        <DropdownMenuTrigger asChild onClick={(e) => e.stopPropagation()}>
                                            <Button variant="ghost" size="icon">
                                                <MoreVertical className="h-4 w-4" />
                                            </Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent align="end">
                                            <DropdownMenuItem onClick={(e) => {
                                                e.stopPropagation()
                                                handleViewDetail(account.id)
                                            }}>
                                                <Eye className="mr-2 h-4 w-4" />
                                                {dict.investment.account.viewDetail}
                                            </DropdownMenuItem>
                                            <DropdownMenuItem onClick={(e) => {
                                                e.stopPropagation()
                                                handleEdit(account)
                                            }}>
                                                <Pencil className="mr-2 h-4 w-4" />
                                                {dict.investment.account.edit}
                                            </DropdownMenuItem>
                                            <DropdownMenuSeparator />
                                            <DropdownMenuItem
                                                onClick={(e) => {
                                                    e.stopPropagation()
                                                    handleDeleteClick(account.id)
                                                }}
                                                className="text-destructive"
                                            >
                                                <Trash2 className="mr-2 h-4 w-4" />
                                                {dict.investment.account.delete}
                                            </DropdownMenuItem>
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <div className="space-y-2">
                                    <div className="flex justify-between text-sm">
                                        <span className="text-muted-foreground">{dict.investment.account.fields.totalAssets}</span>
                                        <span className="font-mono font-semibold">
                                            ¥{account.totalAssets.toLocaleString('zh-CN', {
                                                minimumFractionDigits: 2,
                                                maximumFractionDigits: 2
                                            })}
                                        </span>
                                    </div>
                                    <div className="flex justify-between text-sm">
                                        <span className="text-muted-foreground">{dict.investment.account.fields.positionCount}</span>
                                        <span>{account.positionCount}</span>
                                    </div>
                                </div>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

            {/* 创建/编辑对话框 */}
            <AccountFormDialog
                open={formOpen}
                onOpenChange={setFormOpen}
                account={selectedAccount}
                onSubmit={selectedAccount ? handleUpdate : handleCreate}
            />

            {/* 删除确认对话框 */}
            <AlertDialog open={deleteDialogOpen} onOpenChange={setDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>{dict.investment.account.confirmDelete}</AlertDialogTitle>
                        <AlertDialogDescription>
                            {dict.investment.account.deleteWarning}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>{dict.investment.account.cancel}</AlertDialogCancel>
                        <AlertDialogAction onClick={handleDelete}>
                            {dict.investment.account.confirm}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>

            {/* 批量删除确认对话框 */}
            <AlertDialog open={batchDeleteDialogOpen} onOpenChange={setBatchDeleteDialogOpen}>
                <AlertDialogContent>
                    <AlertDialogHeader>
                        <AlertDialogTitle>{dict.investment.account.batch.confirmBatchDelete}</AlertDialogTitle>
                        <AlertDialogDescription>
                            {dict.investment.account.batch.batchDeleteWarning.replace('{count}', selectedIds.size.toString())}
                        </AlertDialogDescription>
                    </AlertDialogHeader>
                    <AlertDialogFooter>
                        <AlertDialogCancel>{dict.investment.account.cancel}</AlertDialogCancel>
                        <AlertDialogAction onClick={handleBatchDelete} className="bg-destructive text-destructive-foreground hover:bg-destructive/90">
                            {dict.investment.account.confirm}
                        </AlertDialogAction>
                    </AlertDialogFooter>
                </AlertDialogContent>
            </AlertDialog>
        </PageContainer>
    )
}
