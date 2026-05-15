import { useEffect, useRef, useState } from "react";
import {
  Check,
  Copy,
  Download,
  Loader2,
  Plus,
  Trash2,
  Upload,
} from "lucide-react";
import { Dialog } from "@/components/ui/Dialog";
import { Button } from "@/components/ui/Button";
import { Input } from "@/components/ui/Input";
import { useCategories, useFeeds } from "@/api/queries";
import {
  useCreateCategory,
  useDeleteCategory,
  useDeleteFeed,
  useExportOpml,
  useImportOpml,
  useUpdateFeed,
} from "./mutations";
import AddFeedDialog from "./AddFeedDialog";

interface ManageFeedsDialogProps {
  open: boolean;
  onClose: () => void;
}

export default function ManageFeedsDialog({
  open,
  onClose,
}: ManageFeedsDialogProps) {
  const feedsQ = useFeeds();
  const categoriesQ = useCategories();
  const createCategory = useCreateCategory();
  const deleteCategory = useDeleteCategory();
  const deleteFeed = useDeleteFeed();
  const updateFeed = useUpdateFeed();
  const importOpml = useImportOpml();
  const exportOpml = useExportOpml();
  const fileRef = useRef<HTMLInputElement>(null);
  const [newCategory, setNewCategory] = useState("");
  const [addFeedOpen, setAddFeedOpen] = useState(false);
  const [status, setStatus] = useState<string | null>(null);
  const [copiedFeedId, setCopiedFeedId] = useState<number | null>(null);
  const [movingFeedIds, setMovingFeedIds] = useState<ReadonlySet<number>>(
    () => new Set(),
  );

  useEffect(() => {
    if (copiedFeedId === null) return;
    const timer = setTimeout(() => setCopiedFeedId(null), 1500);
    return () => clearTimeout(timer);
  }, [copiedFeedId]);

  async function handleCopyFeedUrl(feedId: number, url: string) {
    try {
      await navigator.clipboard.writeText(url);
      setCopiedFeedId(feedId);
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Copy failed.");
    }
  }

  async function handleMoveFeed(feedId: number, categoryId: number) {
    setStatus(null);
    setMovingFeedIds((prev) => {
      const next = new Set(prev);
      next.add(feedId);
      return next;
    });
    try {
      await updateFeed.mutateAsync({ feedId, category_id: categoryId });
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Move failed.");
    } finally {
      setMovingFeedIds((prev) => {
        if (!prev.has(feedId)) return prev;
        const next = new Set(prev);
        next.delete(feedId);
        return next;
      });
    }
  }

  async function handleExport() {
    setStatus(null);
    try {
      const xml = await exportOpml.mutateAsync();
      const blob = new Blob([xml], { type: "application/xml" });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `miniflux-export-${new Date()
        .toISOString()
        .slice(0, 10)}.opml`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 100);
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Export failed.");
    }
  }

  async function handleImport(file: File) {
    setStatus(null);
    try {
      const xml = await file.text();
      await importOpml.mutateAsync(xml);
      setStatus("OPML imported.");
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Import failed.");
    }
  }

  async function handleCreateCategory() {
    const name = newCategory.trim();
    if (!name) return;
    try {
      await createCategory.mutateAsync(name);
      setNewCategory("");
    } catch (err) {
      setStatus(err instanceof Error ? err.message : "Create failed.");
    }
  }

  const feedsByCategory = new Map<number, typeof feedsQ.data>();
  for (const feed of feedsQ.data ?? []) {
    const list = feedsByCategory.get(feed.category.id) ?? [];
    list.push(feed);
    feedsByCategory.set(feed.category.id, list);
  }

  return (
    <>
      <Dialog
        open={open}
        onClose={onClose}
        title="Manage feeds"
        description="Add and remove feeds and categories, or back them up via OPML."
        className="max-w-3xl"
        footer={
          <div className="flex items-center justify-end gap-2">
            <Button variant="ghost" onClick={onClose}>
              Done
            </Button>
          </div>
        }
      >
        <div className="space-y-5">
          <div className="flex flex-wrap items-center gap-2">
            <Button onClick={() => setAddFeedOpen(true)}>
              <Plus className="h-4 w-4" /> Add feed
            </Button>
            <Button
              variant="outline"
              onClick={() => fileRef.current?.click()}
              disabled={importOpml.isPending}
            >
              {importOpml.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Upload className="h-4 w-4" />
              )}
              Import OPML
            </Button>
            <Button
              variant="outline"
              onClick={handleExport}
              disabled={exportOpml.isPending}
            >
              {exportOpml.isPending ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <Download className="h-4 w-4" />
              )}
              Export OPML
            </Button>
            <input
              ref={fileRef}
              type="file"
              accept=".opml,.xml,application/xml,text/xml"
              className="hidden"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) handleImport(file);
                e.target.value = "";
              }}
            />
          </div>

          <div className="flex items-center gap-2">
            <Input
              placeholder="New category name"
              value={newCategory}
              onChange={(e) => setNewCategory(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") handleCreateCategory();
              }}
            />
            <Button
              variant="outline"
              onClick={handleCreateCategory}
              disabled={!newCategory.trim() || createCategory.isPending}
            >
              Add
            </Button>
          </div>

          {status && (
            <p className="rounded-md border bg-muted/50 p-2 text-sm text-muted-foreground">
              {status}
            </p>
          )}

          <div className="space-y-4">
            {(categoriesQ.data ?? []).map((category) => {
              const feeds = feedsByCategory.get(category.id) ?? [];
              return (
                <section key={category.id} className="rounded-md border">
                  <header className="flex items-center justify-between border-b bg-muted/30 px-3 py-2">
                    <h3 className="font-display text-sm font-semibold">
                      {category.title}
                      <span className="ml-2 text-xs font-normal text-muted-foreground">
                        {feeds.length} feed{feeds.length === 1 ? "" : "s"}
                      </span>
                    </h3>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => {
                        if (feeds.length > 0) {
                          setStatus(
                            `Move or delete the ${feeds.length} feed${feeds.length === 1 ? "" : "s"} in "${category.title}" before deleting it.`,
                          );
                          return;
                        }
                        if (confirm(`Delete category "${category.title}"?`)) {
                          deleteCategory.mutate(category.id);
                        }
                      }}
                      disabled={deleteCategory.isPending}
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </Button>
                  </header>
                  {feeds.length > 0 ? (
                    <ul className="divide-y">
                      {feeds.map((feed) => (
                        <li
                          key={feed.id}
                          className="flex items-center justify-between gap-3 px-3 py-2"
                        >
                          <div className="min-w-0 flex-1">
                            <p className="truncate text-sm font-medium">
                              {feed.title}
                            </p>
                            <p className="truncate text-xs text-muted-foreground">
                              {feed.feed_url}
                            </p>
                          </div>
                          <select
                            aria-label={`Move feed ${feed.title} to a different category`}
                            value={feed.category.id}
                            onChange={(e) => {
                              const next = Number(e.target.value);
                              if (next !== feed.category.id) {
                                handleMoveFeed(feed.id, next);
                              }
                            }}
                            disabled={movingFeedIds.has(feed.id)}
                            className="h-8 rounded-md border border-input bg-background px-2 text-xs"
                          >
                            {(categoriesQ.data ?? []).map((option) => (
                              <option key={option.id} value={option.id}>
                                {option.title}
                              </option>
                            ))}
                          </select>
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={
                              copiedFeedId === feed.id
                                ? `Copied feed URL for ${feed.title}`
                                : `Copy feed URL for ${feed.title}`
                            }
                            title={
                              copiedFeedId === feed.id
                                ? "Copied"
                                : "Copy feed URL"
                            }
                            onClick={() =>
                              handleCopyFeedUrl(feed.id, feed.feed_url)
                            }
                          >
                            {copiedFeedId === feed.id ? (
                              <Check className="h-3.5 w-3.5" />
                            ) : (
                              <Copy className="h-3.5 w-3.5" />
                            )}
                          </Button>
                          <Button
                            variant="ghost"
                            size="icon"
                            aria-label={`Delete feed ${feed.title}`}
                            onClick={() => {
                              if (confirm(`Delete feed "${feed.title}"?`)) {
                                deleteFeed.mutate(feed.id);
                              }
                            }}
                            disabled={deleteFeed.isPending}
                          >
                            <Trash2 className="h-3.5 w-3.5" />
                          </Button>
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <p className="px-3 py-3 text-xs text-muted-foreground">
                      No feeds yet.
                    </p>
                  )}
                </section>
              );
            })}
          </div>
        </div>
      </Dialog>
      <AddFeedDialog open={addFeedOpen} onClose={() => setAddFeedOpen(false)} />
    </>
  );
}
