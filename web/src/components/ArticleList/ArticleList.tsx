import { useEffect, useRef, useState } from "react";
import { useVirtualizer } from "@tanstack/react-virtual";
import { CheckCheck, Loader2, MoreHorizontal, Star } from "lucide-react";
import type { Entry } from "@/api/types";
import { cn } from "@/lib/cn";
import { relativeTime } from "@/lib/time";

interface ArticleListProps {
  entries: Entry[] | undefined;
  selectedEntryId: number | null;
  onSelect: (entry: Entry) => void;
  onMarkAboveAsRead?: (entryIdsAbove: number[]) => void;
  isLoading: boolean;
  isError: boolean;
  emptyLabel?: string;
}

export default function ArticleList({
  entries,
  selectedEntryId,
  onSelect,
  onMarkAboveAsRead,
  isLoading,
  isError,
  emptyLabel = "No articles.",
}: ArticleListProps) {
  const [openMenuEntryId, setOpenMenuEntryId] = useState<number | null>(null);
  const [markAboveAnchorId, setMarkAboveAnchorId] = useState<number | null>(
    null,
  );
  const markAboveInitialIndexRef = useRef(-1);
  const markAboveInitialEntriesRef = useRef<Entry[] | undefined>(undefined);
  const parentRef = useRef<HTMLDivElement>(null);

  const rowVirtualizer = useVirtualizer({
    count: entries?.length ?? 0,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 92,
    overscan: 8,
  });

  useEffect(() => {
    if (selectedEntryId === null || !entries) return;
    const idx = entries.findIndex((e) => e.id === selectedEntryId);
    if (idx >= 0) rowVirtualizer.scrollToIndex(idx, { align: "auto" });
  }, [selectedEntryId, entries, rowVirtualizer]);

  // After "mark above as read", the scroll container's scrollTop is preserved,
  // leaving the viewport past the anchor row. Re-anchor whenever the anchor
  // article has moved to an earlier index after the list is refreshed. This
  // covers both the partial-page case (list shrinks) and the full-page case
  // (list stays at 100 entries but older items fill in above).
  //
  // Guard against the first run (entries === initial ref) — that fires
  // synchronously before the mutation has a chance to refetch.
  //
  // When the index is unchanged (optimistic-update pass, or All-filter where
  // React Query structurally shares the refetched array), schedule a cleanup
  // so the anchor is not armed indefinitely. In the Unread filter, the real
  // refetch fires before the timeout and its effect cleanup cancels the timer;
  // in the All filter no further entries change arrives, so the timer clears
  // the anchor automatically.
  useEffect(() => {
    if (markAboveAnchorId === null || !entries) return;
    if (entries === markAboveInitialEntriesRef.current) return;
    const idx = entries.findIndex((e) => e.id === markAboveAnchorId);
    if (idx < 0) {
      setMarkAboveAnchorId(null);
      return;
    }
    if (idx >= markAboveInitialIndexRef.current) {
      const timer = setTimeout(() => setMarkAboveAnchorId(null), 3000);
      return () => clearTimeout(timer);
    }
    rowVirtualizer.scrollToIndex(idx, { align: "start" });
    const timer = setTimeout(() => setMarkAboveAnchorId(null), 1500);
    return () => clearTimeout(timer);
  }, [markAboveAnchorId, entries, rowVirtualizer]);

  if (isError) {
    return (
      <div className="flex h-full items-center justify-center p-8 text-sm text-destructive">
        Failed to load articles.
      </div>
    );
  }

  if (isLoading && (!entries || entries.length === 0)) {
    return (
      <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
        <Loader2 className="mr-2 h-4 w-4 animate-spin" /> Loading articles…
      </div>
    );
  }

  if (!entries || entries.length === 0) {
    return (
      <div className="flex h-full items-center justify-center p-8 text-center text-sm text-muted-foreground">
        {emptyLabel}
      </div>
    );
  }

  return (
    <div ref={parentRef} className="h-full overflow-y-auto">
      <div
        style={{
          height: `${rowVirtualizer.getTotalSize()}px`,
          width: "100%",
          position: "relative",
        }}
      >
        {rowVirtualizer.getVirtualItems().map((virtualRow) => {
          const entry = entries[virtualRow.index];
          return (
            <div
              key={entry.id}
              data-index={virtualRow.index}
              ref={rowVirtualizer.measureElement}
              style={{
                position: "absolute",
                top: 0,
                left: 0,
                width: "100%",
                transform: `translateY(${virtualRow.start}px)`,
              }}
            >
              <ArticleRow
                entry={entry}
                selected={selectedEntryId === entry.id}
                onClick={() => onSelect(entry)}
                menuOpen={openMenuEntryId === entry.id}
                onMenuOpenChange={(open) =>
                  setOpenMenuEntryId(open ? entry.id : null)
                }
                canMarkAbove={
                  onMarkAboveAsRead !== undefined &&
                  entries
                    .slice(0, virtualRow.index)
                    .some((e) => e.status === "unread")
                }
                onMarkAboveAsRead={
                  onMarkAboveAsRead
                    ? () => {
                        const above = entries
                          .slice(0, virtualRow.index)
                          .filter((e) => e.status === "unread")
                          .map((e) => e.id);
                        if (above.length > 0) {
                          markAboveInitialIndexRef.current = virtualRow.index;
                          markAboveInitialEntriesRef.current = entries;
                          setMarkAboveAnchorId(entry.id);
                        }
                        onMarkAboveAsRead(above);
                        setOpenMenuEntryId(null);
                      }
                    : undefined
                }
              />
            </div>
          );
        })}
      </div>
    </div>
  );
}

interface ArticleRowProps {
  entry: Entry;
  selected: boolean;
  onClick: () => void;
  menuOpen: boolean;
  onMenuOpenChange: (open: boolean) => void;
  canMarkAbove: boolean;
  onMarkAboveAsRead?: () => void;
}

function ArticleRow({
  entry,
  selected,
  onClick,
  menuOpen,
  onMenuOpenChange,
  canMarkAbove,
  onMarkAboveAsRead,
}: ArticleRowProps) {
  const unread = entry.status === "unread";
  const menuWrapperRef = useRef<HTMLDivElement>(null);
  const showMenu = onMarkAboveAsRead !== undefined;

  useEffect(() => {
    if (!menuOpen) return;
    function onPointerDown(e: MouseEvent) {
      if (!menuWrapperRef.current?.contains(e.target as Node)) {
        onMenuOpenChange(false);
      }
    }
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") onMenuOpenChange(false);
    }
    window.addEventListener("mousedown", onPointerDown);
    window.addEventListener("keydown", onKey);
    return () => {
      window.removeEventListener("mousedown", onPointerDown);
      window.removeEventListener("keydown", onKey);
    };
  }, [menuOpen, onMenuOpenChange]);

  return (
    <div
      className={cn(
        "group relative border-b transition-colors",
        selected ? "bg-accent/80" : "hover:bg-accent/40",
      )}
    >
      <button
        type="button"
        onClick={onClick}
        className="flex w-full flex-col gap-1 px-5 py-3 pr-10 text-left"
      >
        <div className="flex items-center gap-2 text-[11px] uppercase tracking-wide text-muted-foreground">
          {unread && (
            <span
              aria-hidden="true"
              className="inline-block h-2 w-2 flex-shrink-0 rounded-full bg-primary"
            />
          )}
          <span className="truncate font-medium">{entry.feed.title}</span>
          <span className="ml-auto flex-shrink-0">
            {relativeTime(entry.published_at)}
          </span>
        </div>
        <div
          className={cn(
            "line-clamp-2 text-sm leading-snug",
            unread ? "font-semibold text-foreground" : "text-foreground/70",
          )}
        >
          {entry.title}
        </div>
        <div className="flex items-center gap-2 text-xs text-muted-foreground">
          {entry.starred && (
            <Star className="h-3 w-3 fill-amber-500 text-amber-500" />
          )}
          {entry.reading_time > 0 && <span>{entry.reading_time} min read</span>}
        </div>
      </button>
      {showMenu && (
        <div
          ref={menuWrapperRef}
          className="absolute right-2 top-2"
          onClick={(e) => e.stopPropagation()}
        >
          <button
            type="button"
            aria-label="Article actions"
            aria-haspopup="menu"
            aria-expanded={menuOpen}
            onClick={(e) => {
              e.stopPropagation();
              onMenuOpenChange(!menuOpen);
            }}
            className={cn(
              "inline-flex h-7 w-7 items-center justify-center rounded-md text-muted-foreground transition-opacity hover:bg-accent hover:text-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-ring",
              menuOpen
                ? "opacity-100"
                : "opacity-0 focus-visible:opacity-100 group-hover:opacity-100",
            )}
          >
            <MoreHorizontal className="h-4 w-4" />
          </button>
          {menuOpen && (
            <div
              role="menu"
              className="absolute right-0 top-full z-40 mt-1 w-56 overflow-hidden rounded-md border bg-popover text-popover-foreground shadow-lg"
            >
              <button
                type="button"
                role="menuitem"
                disabled={!canMarkAbove}
                onClick={() => {
                  if (canMarkAbove) onMarkAboveAsRead?.();
                }}
                className={cn(
                  "flex w-full items-center gap-2 px-3 py-2 text-left text-sm transition-colors",
                  canMarkAbove
                    ? "hover:bg-accent"
                    : "cursor-not-allowed text-muted-foreground/60",
                )}
              >
                <CheckCheck className="h-4 w-4" />
                Mark above as read
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
