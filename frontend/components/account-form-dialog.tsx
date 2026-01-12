'use client'

import { useState } from 'react'
import { useTranslation } from '@/lib/i18n-client'
import { Button } from '@/components/ui/button'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { AccountType, AccountCreateRequest, AccountUpdateRequest, AccountResponse } from '@/lib/api/account'

interface AccountFormDialogProps {
    open: boolean
    onOpenChange: (open: boolean) => void
    account?: AccountResponse
    onSubmit: (data: AccountCreateRequest | AccountUpdateRequest) => Promise<void>
}

export function AccountFormDialog({ open, onOpenChange, account, onSubmit }: AccountFormDialogProps) {
    const dict = useTranslation()
    const [formData, setFormData] = useState<AccountCreateRequest>({
        accountType: account?.accountType || AccountType.BROKER,
        accountName: account?.accountName || '',
        accountIcon: account?.accountIcon || '',
        sortOrder: account?.sortOrder || 0,
    })
    const [errors, setErrors] = useState<Record<string, string>>({})
    const [isSubmitting, setIsSubmitting] = useState(false)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        // 验证
        const newErrors: Record<string, string> = {}
        if (!formData.accountType) {
            newErrors.accountType = dict.investment.account.errors.accountTypeRequired
        }
        if (!formData.accountName.trim()) {
            newErrors.accountName = dict.investment.account.errors.accountNameRequired
        }

        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors)
            return
        }

        setIsSubmitting(true)
        try {
            await onSubmit(formData)
            onOpenChange(false)
            // 重置表单
            setFormData({
                accountType: AccountType.BROKER,
                accountName: '',
                accountIcon: '',
                sortOrder: 0,
            })
            setErrors({})
        } catch (error) {
            console.error('提交失败:', error)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>
                        {account ? dict.investment.account.editAccount : dict.investment.account.addAccount}
                    </DialogTitle>
                    <DialogDescription>
                        {dict.investment.account.description}
                    </DialogDescription>
                </DialogHeader>
                <form onSubmit={handleSubmit}>
                    <div className="grid gap-4 py-4">
                        <div className="grid gap-2">
                            <Label htmlFor="accountType">{dict.investment.account.fields.accountType}</Label>
                            <Select
                                value={formData.accountType}
                                onValueChange={(value) => {
                                    setFormData({ ...formData, accountType: value as AccountType })
                                    setErrors({ ...errors, accountType: '' })
                                }}
                            >
                                <SelectTrigger id="accountType">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value={AccountType.BROKER}>
                                        {dict.investment.account.types.BROKER}
                                    </SelectItem>
                                    <SelectItem value={AccountType.FUND_PLATFORM}>
                                        {dict.investment.account.types.FUND_PLATFORM}
                                    </SelectItem>
                                    <SelectItem value={AccountType.BANK}>
                                        {dict.investment.account.types.BANK}
                                    </SelectItem>
                                    <SelectItem value={AccountType.ALIPAY}>
                                        {dict.investment.account.types.ALIPAY}
                                    </SelectItem>
                                    <SelectItem value={AccountType.OTHER}>
                                        {dict.investment.account.types.OTHER}
                                    </SelectItem>
                                </SelectContent>
                            </Select>
                            {errors.accountType && (
                                <p className="text-sm text-destructive">{errors.accountType}</p>
                            )}
                        </div>

                        <div className="grid gap-2">
                            <Label htmlFor="accountName">{dict.investment.account.fields.accountName}</Label>
                            <Input
                                id="accountName"
                                value={formData.accountName}
                                onChange={(e) => {
                                    setFormData({ ...formData, accountName: e.target.value })
                                    setErrors({ ...errors, accountName: '' })
                                }}
                                placeholder={dict.investment.account.placeholders.accountName}
                            />
                            {errors.accountName && (
                                <p className="text-sm text-destructive">{errors.accountName}</p>
                            )}
                        </div>

                        <div className="grid gap-2">
                            <Label htmlFor="accountIcon">{dict.investment.account.fields.accountIcon}</Label>
                            <Input
                                id="accountIcon"
                                value={formData.accountIcon}
                                onChange={(e) => setFormData({ ...formData, accountIcon: e.target.value })}
                                placeholder={dict.investment.account.placeholders.accountIcon}
                            />
                        </div>
                    </div>
                    <DialogFooter>
                        <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                            {dict.investment.account.cancel}
                        </Button>
                        <Button type="submit" disabled={isSubmitting}>
                            {isSubmitting ? dict.investment.account.saving : dict.investment.account.save}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    )
}
