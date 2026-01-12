'use client'

import { useState, useEffect } from 'react'
import { useParams, useRouter } from 'next/navigation'
import { useTranslation } from '@/lib/i18n-client'
import { useLocale } from '@/hooks/use-locale'
import { Button } from '@/components/ui/button'
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { ArrowLeft, Pencil, Trash2, Wallet, TrendingUp, BarChart3 } from 'lucide-react'
import { accountApi, AccountResponse, AccountUpdateRequest } from '@/lib/api/account'
import { AccountFormDialog } from '@/components/account-form-dialog'
import { PageContainer } from '@/components/page-container'
import { toast } from 'sonner'
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

export default function AccountDetailPage() {
    const params = useParams()
    const router = useRouter()
    const locale = useLocale()
    const dict = useTranslation()

    const accountId = parseInt(params.id as string)

    const [account, setAccount] = useState<AccountResponse | null>(null)
    const [loading, setLoading] = useState(true)
    const [formOpen, setFormOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

    // 加载账号详情
    const loadAccount = async () => {
        try {
            setLoading(true)
            const response = await accountApi.getAccountById(accountId)
            setAccount(response.data)
        } catch (error) {
            console.error('加载账号详情失败:', error)
            toast.error(dict.investment.account.messages.loadError)
            router.push(`/${locale}/investment/account`)
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadAccount()
    }, [accountId])

    // 更新账号
    const handleUpdate = async (data: AccountUpdateRequest) => {
        try {
            await accountApi.updateAccount(accountId, data)
            toast.success(dict.investment.account.messages.updateSuccess)
            loadAccount()
        } catch (error) {
            console.error('更新账号失败:', error)
            toast.error(dict.investment.account.messages.updateError || '更新账号失败')
            throw error
        }
    }

    // 删除账号
    const handleDelete = async () => {
        try {
            await accountApi.deleteAccount(accountId)
            toast.success(dict.investment.account.messages.deleteSuccess)
            router.push(`/${locale}/investment/account`)
        } catch (error) {
            console.error('删除账号失败:', error)
            toast.error(dict.investment.account.messages.deleteError || '删除账号失败')
        }
    }

    if (loading) {
        return (
            <PageContainer>
                <div className="space-y-6">
                    <Skeleton className="h-10 w-48" />
                    <div className="grid gap-4 md:grid-cols-3">
                        <Skeleton className="h-32" />
                        <Skeleton className="h-32" />
                        <Skeleton className="h-32" />
                    </div>
                    <Skeleton className="h-64" />
                </div>
            </PageContainer>
        )
    }

    if (!account) {
        return null
    }

    const accountTypeBadgeVariant: Record<string, 'default' | 'secondary' | 'outline' | 'destructive'> = {
        BROKER: 'default',
        FUND_PLATFORM: 'secondary',
        BANK: 'outline',
        ALIPAY: 'destructive',
        OTHER: 'outline'
    }

    return (
        <PageContainer>
            {/* 返回按钮 */}
            <Button
                variant="ghost"
                className="mb-4"
                onClick={() => router.push(`/${locale}/investment/account`)}
            >
                <ArrowLeft className="mr-2 h-4 w-4" />
                {dict.investment.account.backToList}
            </Button>

            {/* 账号标题和操作 */}
            <div className="flex items-start justify-between mb-6">
                <div className="flex items-center gap-4">
                    {account.accountIcon && (
                        <img
                            src={account.accountIcon}
                            alt=""
                            className="h-16 w-16 rounded-full"
                        />
                    )}
                    <div>
                        <h1 className="text-3xl font-bold tracking-tight">
                            {account.accountName}
                        </h1>
                        <div className="flex items-center gap-2 mt-2">
                            <Badge variant={accountTypeBadgeVariant[account.accountType]}>
                                {dict.investment.account.types[account.accountType]}
                            </Badge>
                            {account.isActive ? (
                                <Badge variant="outline" className="text-green-600 border-green-600">
                                    {dict.investment.account.detail.active}
                                </Badge>
                            ) : (
                                <Badge variant="outline" className="text-gray-600 border-gray-600">
                                    {dict.investment.account.detail.inactive}
                                </Badge>
                            )}
                        </div>
                    </div>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={() => setFormOpen(true)}>
                        <Pencil className="mr-2 h-4 w-4" />
                        {dict.investment.account.edit}
                    </Button>
                    <Button
                        variant="outline"
                        onClick={() => setDeleteDialogOpen(true)}
                        className="text-destructive hover:text-destructive hover:bg-destructive/10"
                    >
                        <Trash2 className="mr-2 h-4 w-4" />
                        {dict.investment.account.delete}
                    </Button>
                </div>
            </div>

            {/* 统计卡片 */}
            <div className="grid gap-4 md:grid-cols-3 mb-6">
                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">{dict.investment.account.fields.totalAssets}</CardTitle>
                        <Wallet className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold font-mono">
                            ¥{account.totalAssets.toLocaleString('zh-CN', {
                                minimumFractionDigits: 2,
                                maximumFractionDigits: 2
                            })}
                        </div>
                        <p className="text-xs text-muted-foreground mt-1">
                            {dict.investment.account.stats.assetsChange} +0.00%
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">{dict.investment.account.fields.positionCount}</CardTitle>
                        <BarChart3 className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold">{account.positionCount}</div>
                        <p className="text-xs text-muted-foreground mt-1">
                            {dict.investment.account.stats.accountsUnit}
                        </p>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                        <CardTitle className="text-sm font-medium">{dict.investment.account.stats.returnRate}</CardTitle>
                        <TrendingUp className="h-4 w-4 text-muted-foreground" />
                    </CardHeader>
                    <CardContent>
                        <div className="text-2xl font-bold text-green-600">+0.00%</div>
                        <p className="text-xs text-muted-foreground mt-1">
                            {dict.investment.account.stats.cumulativeReturn}
                        </p>
                    </CardContent>
                </Card>
            </div>

            {/* 账号详细信息 */}
            <Card>
                <CardHeader>
                    <CardTitle>{dict.investment.account.detail.accountInfo}</CardTitle>
                    <CardDescription>{dict.investment.account.detail.accountInfoDesc}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.fields.accountType}
                            </div>
                            <div className="text-sm">
                                {dict.investment.account.types[account.accountType]}
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.fields.accountName}
                            </div>
                            <div className="text-sm">{account.accountName}</div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.detail.status}
                            </div>
                            <div className="text-sm">
                                {account.isActive ? dict.investment.account.detail.active : dict.investment.account.detail.inactive}
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.detail.visibility}
                            </div>
                            <div className="text-sm">
                                {account.isHidden ? dict.investment.account.detail.hidden : dict.investment.account.detail.visible}
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.detail.createdAt}
                            </div>
                            <div className="text-sm">
                                {new Date(account.createdAt).toLocaleString('zh-CN')}
                            </div>
                        </div>
                        <div>
                            <div className="text-sm font-medium text-muted-foreground mb-1">
                                {dict.investment.account.fields.sortOrder}
                            </div>
                            <div className="text-sm">{account.sortOrder}</div>
                        </div>
                    </div>
                </CardContent>
            </Card>

            {/* 持仓列表 - 占位符 */}
            <Card className="mt-6">
                <CardHeader>
                    <CardTitle>{dict.investment.account.detail.positionList}</CardTitle>
                    <CardDescription>{dict.investment.account.detail.positionListDesc}</CardDescription>
                </CardHeader>
                <CardContent>
                    <div className="text-center py-12 text-muted-foreground">
                        {dict.investment.account.detail.positionListPlaceholder}
                    </div>
                </CardContent>
            </Card>

            {/* 编辑对话框 */}
            <AccountFormDialog
                open={formOpen}
                onOpenChange={setFormOpen}
                account={account}
                onSubmit={handleUpdate}
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
        </PageContainer>
    )
}
