
'use client'

import { useState } from 'react'
import useSWR, { mutate } from 'swr'
import { useTranslation } from '@/lib/i18n-client'
import { PageContainer } from '@/components/page-container'
import { watchlistApi, Market, WatchlistGroup } from '@/lib/api/watchlist'
import { WatchlistAddDialog } from '@/components/watchlist-add-dialog'
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from '@/components/ui/table'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Trash2, TrendingUp, TrendingDown, Loader2 } from 'lucide-react'
import { toast } from 'sonner'
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from '@/components/ui/select'

export default function WatchlistPage() {
    const dict = useTranslation()
    const [selectedGroup, setSelectedGroup] = useState<string>('all')
    const [selectedMarket, setSelectedMarket] = useState<Market | 'ALL'>('ALL')

    // Fetch groups
    const { data: groupsResponse } = useSWR('watchlist-groups', () => watchlistApi.getGroups())
    const groups = groupsResponse?.data

    // Fetch watchlist items
    const { data: watchlistResponse, isLoading } = useSWR(
        ['watchlist', selectedGroup, selectedMarket],
        () => watchlistApi.getWatchlist({
            groupId: selectedGroup === 'all' ? undefined : parseInt(selectedGroup),
            market: selectedMarket === 'ALL' ? undefined : selectedMarket
        })
    )
    const watchlist = watchlistResponse?.data

    const handleRemove = async (id: number) => {
        try {
            await watchlistApi.removeFromWatchlist(id)
            toast.success((dict as any).investment.watchlist.messages.removeSuccess)
            mutate(['watchlist', selectedGroup, selectedMarket])
        } catch (error) {
            console.error(error)
        }
    }

    const refreshList = () => {
        mutate(['watchlist', selectedGroup, selectedMarket])
    }

    return (
        <PageContainer>
            <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div>
                    <h2 className="text-2xl font-bold tracking-tight">{(dict as any).investment.watchlist.title}</h2>
                    <p className="text-muted-foreground">{(dict as any).investment.watchlist.description}</p>
                </div>
                <WatchlistAddDialog groups={groups || []} onSuccess={refreshList} />
            </div>

            <div className="flex flex-col gap-4 md:flex-row md:items-center">
                <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">{(dict as any).investment.watchlist.group}:</span>
                    <Select value={selectedGroup} onValueChange={setSelectedGroup}>
                        <SelectTrigger className="w-[150px]">
                            <SelectValue placeholder={(dict as any).investment.watchlist.allGroups} />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">{(dict as any).investment.watchlist.allGroups}</SelectItem>
                            {groups?.map((g: WatchlistGroup) => (
                                <SelectItem key={g.id} value={g.id.toString()}>
                                    {g.name}
                                </SelectItem>
                            ))}
                        </SelectContent>
                    </Select>
                </div>
                <div className="flex items-center gap-2">
                    <span className="text-sm font-medium">{(dict as any).investment.watchlist.market}:</span>
                    <Select
                        value={selectedMarket}
                        onValueChange={(v) => setSelectedMarket(v as Market | 'ALL')}
                    >
                        <SelectTrigger className="w-[150px]">
                            <SelectValue placeholder={(dict as any).investment.watchlist.allMarkets} />
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
                </div>
            </div>

            <div className="rounded-md border">
                <Table>
                    <TableHeader>
                        <TableRow>
                            <TableHead>{(dict as any).investment.watchlist.columns.symbol}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.name}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.price}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.change}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.recommendation}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.confidence}</TableHead>
                            <TableHead>{(dict as any).investment.watchlist.columns.addedAt}</TableHead>
                            <TableHead className="text-right">{(dict as any).investment.watchlist.columns.actions}</TableHead>
                        </TableRow>
                    </TableHeader>
                    <TableBody>
                        {isLoading ? (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center h-24">
                                    <Loader2 className="h-6 w-6 animate-spin mx-auto" />
                                </TableCell>
                            </TableRow>
                        ) : watchlist?.items && watchlist.items.length > 0 ? (
                            watchlist.items.map((item) => (
                                <TableRow key={item.id}>
                                    <TableCell className="font-medium">
                                        {item.symbol}
                                        <div className="text-xs text-muted-foreground md:hidden">
                                            {(dict as any).investment.watchlist.markets[item.market]}
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <div className="flex flex-col">
                                            <span>{item.name}</span>
                                            <span className="text-xs text-muted-foreground hidden md:inline-block">
                                                {(dict as any).investment.watchlist.markets[item.market]}
                                            </span>
                                        </div>
                                    </TableCell>
                                    <TableCell>{item.currentPrice}</TableCell>
                                    <TableCell>
                                        <div className={`flex items-center ${item.changePercent >= 0 ? 'text-red-500' : 'text-green-500'}`}>
                                            {item.changePercent >= 0 ? <TrendingUp className="mr-1 h-3 w-3" /> : <TrendingDown className="mr-1 h-3 w-3" />}
                                            {item.changePercent > 0 ? '+' : ''}{item.changePercent}%
                                        </div>
                                    </TableCell>
                                    <TableCell>
                                        <Badge variant={
                                            item.recommendation === 'BUILD_POSITION' ? 'default' :
                                                item.recommendation === 'HOLD' ? 'secondary' :
                                                    item.recommendation === 'AVOID' ? 'destructive' : 'outline'
                                        }>
                                            {(dict as any).investment.watchlist.recommendations[item.recommendation]}
                                        </Badge>
                                    </TableCell>
                                    <TableCell>
                                        {item.confidence}%
                                    </TableCell>
                                    <TableCell className="text-muted-foreground text-sm">
                                        {new Date(item.addedAt).toLocaleDateString()}
                                    </TableCell>
                                    <TableCell className="text-right">
                                        <Button
                                            variant="ghost"
                                            size="icon"
                                            onClick={() => handleRemove(item.id)}
                                            className="text-muted-foreground hover:text-destructive"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        ) : (
                            <TableRow>
                                <TableCell colSpan={8} className="text-center h-24 text-muted-foreground">
                                    {(dict as any).investment.watchlist.empty.title}
                                </TableCell>
                            </TableRow>
                        )}
                    </TableBody>
                </Table>
            </div>
        </PageContainer>
    )
}
