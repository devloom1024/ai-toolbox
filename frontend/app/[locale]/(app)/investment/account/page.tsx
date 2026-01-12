'use client'

import { useState, useEffect } from 'react'
import { useTranslation } from '@/lib/i18n-client'
import { Button } from '@/components/ui/button'
import {
    Card,
    CardContent,
    CardDescription,
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
import { Badge } from '@/components/ui/badge'
import { Plus, Pencil, Trash2 } from 'lucide-react'
import { accountApi, AccountResponse, AccountCreateRequest, AccountUpdateRequest } from '@/lib/api/account'
import { AccountFormDialog } from '@/components/account-form-dialog'
import { PageContainer } from '@/components/page-container'
import { toast } from 'sonner'

export default function AccountManagementPage() {
    const dict = useTranslation()
    const [accounts, setAccounts] = useState<AccountResponse[]>([])
    const [loading, setLoading] = useState(true)
    const [formOpen, setFormOpen] = useState(false)
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
    const [selectedAccount, setSelectedAccount] = useState<AccountResponse | undefined>()
    const [accountToDelete, setAccountToDelete] = useState<number | null>(null)

    // 加载账号列表
    const loadAccounts = async () => {
        try {
            setLoading(true)
            const response = await accountApi.getAccountList(false)
            setAccounts(response.data || [])
        } catch (error) {
            console.error('加载账号列表失败:', error)
            toast.error('加载账号列表失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        loadAccounts()
    }, [])

    // 创建账号
    const handleCreate = async (data: AccountCreateRequest) => {
        try {
            await accountApi.createAccount(data)
            toast.success(dict.investment.account.messages.createSuccess)
            loadAccounts()
        } catch (error) {
            console.error('创建账号失败:', error)
            toast.error('创建账号失败')
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
            toast.error('更新账号失败')
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
            toast.error('删除账号失败')
        }
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

    return (
        <PageContainer>
            <Card>
                <CardHeader>
                    <div className="flex items-center justify-between">
                        <div>
                            <CardTitle>{dict.investment.account.title}</CardTitle>
                            <CardDescription>{dict.investment.account.description}</CardDescription>
                        </div>
                        <Button onClick={handleAddClick}>
                            <Plus className="mr-2 h-4 w-4" />
                            {dict.investment.account.addAccount}
                        </Button>
                    </div>
                </CardHeader>
                <CardContent>
                    {loading ? (
                        <div className="text-center py-8 text-muted-foreground">加载中...</div>
                    ) : accounts.length === 0 ? (
                        <div className="text-center py-8 text-muted-foreground">
                            暂无账号，点击上方按钮添加
                        </div>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>{dict.investment.account.fields.accountType}</TableHead>
                                    <TableHead>{dict.investment.account.fields.accountName}</TableHead>
                                    <TableHead>{dict.investment.account.fields.positionCount}</TableHead>
                                    <TableHead>{dict.investment.account.fields.totalAssets}</TableHead>
                                    <TableHead className="text-right">操作</TableHead>
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {accounts.map((account) => (
                                    <TableRow key={account.id}>
                                        <TableCell>
                                            <Badge variant="outline">
                                                {dict.investment.account.types[account.accountType]}
                                            </Badge>
                                        </TableCell>
                                        <TableCell className="font-medium">{account.accountName}</TableCell>
                                        <TableCell>{account.positionCount}</TableCell>
                                        <TableCell>¥{account.totalAssets.toFixed(2)}</TableCell>
                                        <TableCell className="text-right">
                                            <div className="flex justify-end gap-2">
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => handleEdit(account)}
                                                >
                                                    <Pencil className="h-4 w-4" />
                                                </Button>
                                                <Button
                                                    variant="ghost"
                                                    size="icon"
                                                    onClick={() => handleDeleteClick(account.id)}
                                                >
                                                    <Trash2 className="h-4 w-4" />
                                                </Button>
                                            </div>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>

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
        </PageContainer>
    )
}
