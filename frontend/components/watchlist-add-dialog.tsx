
'use client'

import { useState } from 'react'
import { useTranslation } from '@/lib/i18n-client'
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from '@/components/ui/dialog'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Plus, Search, Loader2 } from 'lucide-react'
import { Market, watchlistApi, StockSearchResult, WatchlistGroup } from '@/lib/api/watchlist'
import { toast } from 'sonner'

interface WatchlistAddDialogProps {
    groups?: WatchlistGroup[]
    onSuccess: () => void
}

export function WatchlistAddDialog({ groups = [], onSuccess }: WatchlistAddDialogProps) {
    const dict = useTranslation()
    const [open, setOpen] = useState(false)
    const [keyword, setKeyword] = useState('')
    const [selectedMarket, setSelectedMarket] = useState<Market | 'ALL'>('ALL')
    const [searching, setSearching] = useState(false)
    const [searchResults, setSearchResults] = useState<StockSearchResult[]>([])
    const [selectedGroup, setSelectedGroup] = useState<string>('default')
    const [adding, setAdding] = useState(false)
    const [newGroupName, setNewGroupName] = useState('')
    const [isCreatingGroup, setIsCreatingGroup] = useState(false)

    const handleSearch = async () => {
        if (!keyword.trim()) return
        setSearching(true)
        try {
            const response = await watchlistApi.searchStocks(
                keyword,
                selectedMarket === 'ALL' ? undefined : selectedMarket
            )
            setSearchResults(response.data || [])
        } catch (error) {
            console.error(error)
        } finally {
            setSearching(false)
        }
    }

    const handleAdd = async (stock: StockSearchResult) => {
        setAdding(true)
        try {
            let groupId: number | undefined

            if (isCreatingGroup && newGroupName) {
                await watchlistApi.createGroup({ name: newGroupName })
            } else if (selectedGroup !== 'default') {
                groupId = parseInt(selectedGroup)
            }

            await watchlistApi.addToWatchlist({
                symbol: stock.symbol,
                market: stock.market,
                name: stock.name,
                groupId
            })

            toast.success((dict as any).investment.watchlist.messages.addSuccess)
            setOpen(false)
            onSuccess()
        } catch (error) {
            console.error(error)
        } finally {
            setAdding(false)
        }
    }

    return (
        <Dialog open={open} onOpenChange={setOpen}>
            <DialogTrigger asChild>
                <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    {(dict as any).investment.watchlist.addStock}
                </Button>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[500px]">
                <DialogHeader>
                    <DialogTitle>{(dict as any).investment.watchlist.dialog.title}</DialogTitle>
                    <DialogDescription>
                        {(dict as any).investment.watchlist.dialog.description}
                    </DialogDescription>
                </DialogHeader>

                <div className="grid gap-4 py-4">
                    <div className="flex gap-2">
                        <Select
                            value={selectedMarket}
                            onValueChange={(v) => setSelectedMarket(v as Market | 'ALL')}
                        >
                            <SelectTrigger className="w-[120px]">
                                <SelectValue placeholder={(dict as any).investment.watchlist.dialog.selectMarket} />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="ALL">{(dict as any).investment.watchlist.allMarkets}</SelectItem>
                                {Object.keys(Market).map((m) => (
                                    <SelectItem key={m} value={m}>
                                        {(dict as any).investment.watchlist.markets[m]}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        <Input
                            placeholder={(dict as any).investment.watchlist.searchPlaceholder}
                            value={keyword}
                            onChange={(e) => setKeyword(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                        />
                        <Button size="icon" onClick={handleSearch} disabled={searching}>
                            {searching ? <Loader2 className="h-4 w-4 animate-spin" /> : <Search className="h-4 w-4" />}
                        </Button>
                    </div>

                    <div className="grid gap-2">
                        <label className="text-sm font-medium">{(dict as any).investment.watchlist.dialog.selectGroup}</label>
                        <Select
                            value={isCreatingGroup ? 'new' : selectedGroup}
                            onValueChange={(v) => {
                                if (v === 'new') {
                                    setIsCreatingGroup(true)
                                } else {
                                    setIsCreatingGroup(false)
                                    setSelectedGroup(v)
                                }
                            }}
                        >
                            <SelectTrigger>
                                <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                                <SelectItem value="default">{(dict as any).investment.watchlist.dialog.defaultGroup}</SelectItem>
                                {groups.map((g) => (
                                    <SelectItem key={g.id} value={g.id.toString()}>
                                        {g.name}
                                    </SelectItem>
                                ))}
                                <SelectItem value="new">+ {(dict as any).investment.watchlist.dialog.createGroup}</SelectItem>
                            </SelectContent>
                        </Select>
                        {isCreatingGroup && (
                            <Input
                                placeholder="Group Name"
                                value={newGroupName}
                                onChange={(e) => setNewGroupName(e.target.value)}
                                className="mt-2"
                            />
                        )}
                    </div>

                    <div className="max-h-[300px] overflow-y-auto space-y-2">
                        {searchResults.length > 0 && (
                            <div className="text-sm text-muted-foreground mb-2">
                                {(dict as any).investment.watchlist.dialog.searchResult}
                            </div>
                        )}
                        {searchResults.map((stock) => (
                            <div
                                key={`${stock.market}-${stock.symbol}`}
                                className="flex items-center justify-between p-3 border rounded-lg hover:bg-accent/50 transition-colors"
                            >
                                <div>
                                    <div className="font-medium">{stock.name}</div>
                                    <div className="text-xs text-muted-foreground">
                                        {stock.symbol} · {(dict as any).investment.watchlist.markets[stock.market]}
                                    </div>
                                </div>
                                <Button
                                    size="sm"
                                    variant="secondary"
                                    onClick={() => handleAdd(stock)}
                                    disabled={adding}
                                >
                                    {adding ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
                                </Button>
                            </div>
                        ))}
                    </div>
                </div>
            </DialogContent>
        </Dialog>
    )
}
