/**
 * 应用主页 / Dashboard
 */

"use client"

import { PageContainer } from "@/components/page-container"
import { useTranslation } from "@/lib/i18n-client"

export default function HomePage() {
  const dictionary = useTranslation()

  return (
    <PageContainer breadcrumbs={[]}>
      <div className="rounded-xl border bg-card p-8">
        <h1 className="text-2xl font-semibold text-card-foreground">
          {dictionary.dashboard.hero.title}
        </h1>
        <p className="mt-2 text-sm text-muted-foreground">
          {dictionary.dashboard.hero.description}
        </p>
      </div>
      <div className="grid auto-rows-min gap-6 md:grid-cols-3">
        <div className="bg-muted/50 aspect-video rounded-xl" />
        <div className="bg-muted/50 aspect-video rounded-xl" />
        <div className="bg-muted/50 aspect-video rounded-xl" />
      </div>
      <div className="bg-muted/50 min-h-[100vh] flex-1 rounded-xl p-6 md:min-h-min" />
    </PageContainer>
  )
}
