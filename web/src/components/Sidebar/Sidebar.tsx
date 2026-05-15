import { useMemo, useState, type CSSProperties, type RefObject } from "react";
import {
  ChevronDown,
  ChevronRight,
  Folder,
  Inbox,
  Loader2,
  LogOut,
  RefreshCw,
  Settings,
  Star,
} from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { useCategories, useFeedCounters, useFeeds, useMe } from "@/api/queries";
import { MinifluxError } from "@/api/miniflux";
import { Button } from "@/components/ui/Button";
import { Skeleton } from "@/components/ui/Skeleton";
import { SearchBar } from "@/components/SearchBar";
import FeedIcon from "@/components/FeedIcon";
import ManageFeedsDialog from "@/features/subscriptions/ManageFeedsDialog";
import { cn } from "@/lib/cn";
import type { Category, Feed } from "@/api/types";
import { useSelection, type Scope } from "@/hooks/useSelection";
import { feedTint, useFeedColors } from "@/hooks/useFeedColors";

interface SidebarProps {
  className?: string;
  searchInputRef?: RefObject<HTMLInputElement>;
}

interface CategoryGroup {
  category: Category;
  feeds: Feed[];
  unread: number;
}

export default function Sidebar({ className, searchInputRef }: SidebarProps) {
  const { signOut } = useAuth();
  const queryClient = useQueryClient();
  const me = useMe();
  const categoriesQ = useCategories();
  const feedsQ = useFeeds();
  const countersQ = useFeedCounters();
  const feedColors = useFeedColors();
  const { selection, setScope } = useSelection();
  const [manageOpen, setManageOpen] = useState(false);

  const groups = useMemo<CategoryGroup[]>(() => {
    if (!categoriesQ.data || !feedsQ.data) return [];
    const unreadByFeed = countersQ.data?.unreads ?? {};
    const byCategory = new Map<number, Feed[]>();
    for (const feed of feedsQ.data) {
      const list = byCategory.get(feed.category.id) ?? [];
      list.push(feed);
      byCategory.set(feed.category.id, list);
    }
    return categoriesQ.data
      .map((category) => {
        const feeds = (byCategory.get(category.id) ?? []).slice().sort((a, b) =>
          a.title.localeCompare(b.title),
        );
        const unread = feeds.reduce(
          (sum, feed) => sum + (unreadByFeed[String(feed.id)] ?? 0),
          0,
        );
        return { category, feeds, unread };
      })
      .sort((a, b) => a.category.title.localeCompare(b.category.title));
  }, [categoriesQ.data, feedsQ.data, countersQ.data]);

  const totalUnread = useMemo(
    () =>
      Object.values(countersQ.data?.unreads ?? {}).reduce(
        (sum, v) => sum + v,
        0,
      ),
    [countersQ.data],
  );

  const isLoading = categoriesQ.isLoading || feedsQ.isLoading;
  const loadError = feedsQ.error ?? categoriesQ.error ?? null;
  const isRefetching = feedsQ.isFetching || categoriesQ.isFetching;
  const handleRetry = () => {
    // Refetch every query so sibling panes (entries, article) recover too.
    queryClient.invalidateQueries();
  };

  return (
    <aside
      className={cn(
        "flex h-full flex-col border-r bg-card/30",
        className,
      )}
    >
      <div className="flex flex-col gap-2 border-b px-3 py-3">
        <div className="flex items-center justify-between">
          <div className="flex flex-col">
            <span className="font-display text-base font-semibold tracking-tight">
              Capy Reader
            </span>
            <span className="text-xs text-muted-foreground">
              {me.data?.username ?? "…"}
            </span>
          </div>
          <div className="flex items-center gap-1">
            <Button
              variant="ghost"
              size="icon"
              aria-label="Manage feeds"
              onClick={() => setManageOpen(true)}
            >
              <Settings className="h-4 w-4" />
            </Button>
            <Button
              variant="ghost"
              size="icon"
              aria-label="Sign out"
              onClick={signOut}
            >
              <LogOut className="h-4 w-4" />
            </Button>
          </div>
        </div>
        <SearchBar ref={searchInputRef} />
      </div>
      <ManageFeedsDialog
        open={manageOpen}
        onClose={() => setManageOpen(false)}
      />

      <nav className="flex-1 overflow-y-auto py-2 text-sm">
        <SidebarItem
          icon={<Inbox className="h-4 w-4" />}
          label="All articles"
          count={totalUnread}
          active={selection.scope.kind === "all"}
          onClick={() => setScope({ kind: "all" })}
        />
        <SidebarItem
          icon={<Star className="h-4 w-4 text-amber-500" />}
          label="Starred"
          active={selection.scope.kind === "starred"}
          onClick={() => setScope({ kind: "starred" })}
        />

        <div className="my-2 border-t" />

        {isLoading && (
          <div className="flex items-center gap-2 px-4 py-2 text-xs text-muted-foreground">
            <Loader2 className="h-3 w-3 animate-spin" />
            Loading feeds…
          </div>
        )}

        {!isLoading && groups.length === 0 && (
          <p className="px-4 py-6 text-center text-xs text-muted-foreground">
            No feeds yet. Import an OPML in Miniflux to get started.
          </p>
        )}

        {groups.map((group) => (
          <CategoryBlock
            key={group.category.id}
            group={group}
            currentScope={selection.scope}
            counters={countersQ.data?.unreads ?? {}}
            onSelect={setScope}
            feedColors={feedColors}
          />
        ))}
      </nav>

      {loadError && (
        <FeedLoadError
          error={loadError}
          isRetrying={isRefetching}
          onRetry={handleRetry}
          onSignOut={signOut}
        />
      )}

      {isLoading && groups.length === 0 && (
        <div className="space-y-2 p-3">
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-4 w-1/2" />
          <Skeleton className="h-4 w-2/3" />
        </div>
      )}
    </aside>
  );
}

interface FeedLoadErrorProps {
  error: unknown;
  isRetrying: boolean;
  onRetry: () => void;
  onSignOut: () => void;
}

function describeLoadError(error: unknown): {
  title: string;
  detail: string | null;
  isAuth: boolean;
} {
  if (error instanceof MinifluxError) {
    const isAuth = error.status === 401 || error.status === 403;
    return {
      title: isAuth
        ? "Your Miniflux session has expired."
        : `Miniflux returned ${error.status}.`,
      detail: error.message || null,
      isAuth,
    };
  }
  if (error instanceof TypeError) {
    // fetch() throws TypeError for network/CORS/TLS failures, which all surface
    // to the user as "Failed to fetch" with no body. Give them a pointer.
    return {
      title: "Could not reach your Miniflux server.",
      detail:
        "Check that the server is online and that CORS_ALLOWED_ORIGINS on Miniflux includes this site.",
      isAuth: false,
    };
  }
  if (error instanceof Error) {
    return { title: "Could not load feeds.", detail: error.message, isAuth: false };
  }
  return { title: "Could not load feeds.", detail: null, isAuth: false };
}

function FeedLoadError({
  error,
  isRetrying,
  onRetry,
  onSignOut,
}: FeedLoadErrorProps) {
  const { title, detail, isAuth } = describeLoadError(error);
  return (
    <div
      role="alert"
      className="space-y-2 border-t p-3 text-xs text-destructive"
    >
      <p className="font-medium">{title}</p>
      {detail && <p className="text-destructive/80">{detail}</p>}
      <div className="flex gap-2 pt-1">
        {isAuth ? (
          <Button size="sm" variant="outline" onClick={onSignOut}>
            Sign in again
          </Button>
        ) : (
          <Button
            size="sm"
            variant="outline"
            onClick={onRetry}
            disabled={isRetrying}
          >
            {isRetrying ? (
              <Loader2 className="h-3 w-3 animate-spin" />
            ) : (
              <RefreshCw className="h-3 w-3" />
            )}
            Retry
          </Button>
        )}
      </div>
    </div>
  );
}

interface SidebarItemProps {
  icon: React.ReactNode;
  label: string;
  count?: number;
  active?: boolean;
  onClick: () => void;
  indent?: boolean;
  style?: CSSProperties;
}

function SidebarItem({
  icon,
  label,
  count,
  active,
  onClick,
  indent,
  style,
}: SidebarItemProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      style={style}
      className={cn(
        "flex w-full items-center gap-2 rounded-md px-3 py-1.5 text-left transition-colors",
        indent && "pl-8",
        active
          ? "text-accent-foreground ring-1 ring-inset ring-accent-foreground/40"
          : "text-foreground/80 hover:text-foreground hover:brightness-95 dark:hover:brightness-105",
      )}
    >
      <span className="flex h-5 w-5 items-center justify-center">{icon}</span>
      <span className="flex-1 truncate font-medium">{label}</span>
      {count !== undefined && count > 0 && (
        <span
          className={cn(
            "rounded-full px-1.5 py-0.5 text-[10px] font-semibold",
            active
              ? "bg-primary text-primary-foreground"
              : "bg-muted text-muted-foreground",
          )}
        >
          {count > 999 ? "999+" : count}
        </span>
      )}
    </button>
  );
}

interface CategoryBlockProps {
  group: CategoryGroup;
  currentScope: Scope;
  counters: Record<string, number>;
  onSelect: (scope: Scope) => void;
  feedColors: ReturnType<typeof useFeedColors>;
}

function CategoryBlock({
  group,
  currentScope,
  counters,
  onSelect,
  feedColors,
}: CategoryBlockProps) {
  const [open, setOpen] = useState(true);
  const categoryActive =
    currentScope.kind === "category" && currentScope.id === group.category.id;

  return (
    <div className="px-1">
      <div className="group flex items-center gap-0.5">
        <button
          type="button"
          onClick={() => setOpen((v) => !v)}
          aria-label={open ? "Collapse category" : "Expand category"}
          className="flex h-7 w-5 items-center justify-center rounded text-muted-foreground hover:text-foreground"
        >
          {open ? (
            <ChevronDown className="h-3 w-3" />
          ) : (
            <ChevronRight className="h-3 w-3" />
          )}
        </button>
        <SidebarItem
          icon={<Folder className="h-4 w-4" />}
          label={group.category.title}
          count={group.unread}
          active={categoryActive}
          onClick={() =>
            onSelect({ kind: "category", id: group.category.id })
          }
        />
      </div>
      {open && (
        <div className="mt-0.5 space-y-0.5">
          {group.feeds.map((feed) => {
            const active =
              currentScope.kind === "feed" && currentScope.id === feed.id;
            const color = feedColors.get(feed.id);
            const tint = feedTint(color, active ? "strong" : "subtle");
            return (
              <SidebarItem
                key={feed.id}
                icon={
                  <FeedIcon
                    iconId={feed.icon?.icon_id}
                    title={feed.title}
                    sizeClassName="h-4 w-4"
                  />
                }
                label={feed.title}
                count={counters[String(feed.id)] ?? 0}
                active={active}
                onClick={() => onSelect({ kind: "feed", id: feed.id })}
                indent
                style={tint ? { backgroundColor: tint } : undefined}
              />
            );
          })}
        </div>
      )}
    </div>
  );
}
