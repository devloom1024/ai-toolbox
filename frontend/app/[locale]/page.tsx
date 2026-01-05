import { AppSidebar } from "@/components/app-sidebar"
import { LanguageToggle } from "@/components/language-toggle"
import {
  Breadcrumb,
  BreadcrumbItem,
  BreadcrumbLink,
  BreadcrumbList,
  BreadcrumbPage,
  BreadcrumbSeparator,
} from "@/components/ui/breadcrumb"
import { Separator } from "@/components/ui/separator"
import {
  SidebarInset,
  SidebarProvider,
  SidebarTrigger,
} from "@/components/ui/sidebar"
import { ThemeToggle } from "@/components/theme-toggle"
import { getDictionary } from "@/lib/dictionaries"
import { i18n, type Locale } from "@/lib/i18n-config"

export const dynamicParams = false

export function generateStaticParams() {
  return i18n.locales.map((locale) => ({ locale }))
}

export default async function Page({
  params,
}: {
  params: Promise<{ locale: Locale }>
}) {
  const { locale } = await params
  const dictionary = await getDictionary(locale)

  return (
    <SidebarProvider>
      <AppSidebar />
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4 transition-[width,height] ease-linear group-has-data-[collapsible=icon]/sidebar-wrapper:h-12">
          <div className="flex flex-1 items-center gap-2">
            <SidebarTrigger className="-ml-1" />
            <Separator
              orientation="vertical"
              className="mr-2 data-[orientation=vertical]:h-4"
            />
            <Breadcrumb>
              <BreadcrumbList>
                <BreadcrumbItem className="hidden md:block">
                  <BreadcrumbLink href="#">
                    {dictionary.header.breadcrumb.root}
                  </BreadcrumbLink>
                </BreadcrumbItem>
                <BreadcrumbSeparator className="hidden md:block" />
                <BreadcrumbItem>
                  <BreadcrumbPage>
                    {dictionary.header.breadcrumb.current}
                  </BreadcrumbPage>
                </BreadcrumbItem>
              </BreadcrumbList>
            </Breadcrumb>
          </div>
          <div className="ml-auto flex items-center gap-2">
            <LanguageToggle
              label={dictionary.header.actions.language.menu}
              srLabel={dictionary.header.actions.language.sr}
            />
            <ThemeToggle
              label={dictionary.header.actions.theme.menu}
              srLabel={dictionary.header.actions.theme.sr}
              options={dictionary.header.actions.theme.options}
            />
          </div>
        </header>
        <div className="flex flex-1 flex-col gap-6 p-6">
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
        </div>
      </SidebarInset>
    </SidebarProvider>
  )
}
