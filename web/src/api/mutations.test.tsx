import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { ReactNode } from "react";
import { act, renderHook, waitFor } from "@testing-library/react";
import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";
import type { EntriesQuery, EntriesResponse, Entry } from "@/api/types";

const updateEntries = vi.fn();

vi.mock("@/auth/AuthContext", () => ({
  useAuth: () => ({
    credentials: { baseUrl: "https://example.test", token: "tok" },
    signIn: vi.fn(),
    signOut: vi.fn(),
  }),
}));

vi.mock("@/api/miniflux", () => ({
  minifluxApi: {
    updateEntries: (...args: unknown[]) => updateEntries(...args),
  },
}));

const { useMarkEntriesAsRead, useUpdateEntryStatus } = await import(
  "@/api/mutations"
);
const { queryKeys } = await import("@/api/queries");

function entry(overrides: Partial<Entry> & Pick<Entry, "id">): Entry {
  return {
    user_id: 1,
    feed_id: 1,
    status: "unread",
    hash: "",
    title: `entry-${overrides.id}`,
    url: "",
    comments_url: "",
    published_at: "",
    created_at: "",
    content: "",
    author: "",
    starred: false,
    reading_time: 0,
    feed: {
      id: 1,
      user_id: 1,
      feed_url: "",
      site_url: "",
      title: "",
      checked_at: "",
      category: { id: 1, user_id: 1, title: "" },
    },
    ...overrides,
  };
}

const unreadQuery: EntriesQuery = {
  status: "unread",
  limit: 100,
  order: "published_at",
  direction: "desc",
};

function makeWrapper() {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  });
  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
    );
  }
  return { queryClient, Wrapper };
}

describe("entry status mutations", () => {
  beforeEach(() => {
    updateEntries.mockReset();
    updateEntries.mockResolvedValue(undefined);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("useUpdateEntryStatus optimistically marks the entry read but keeps it in the unread list", async () => {
    const { queryClient, Wrapper } = makeWrapper();
    const seed: EntriesResponse = {
      total: 2,
      entries: [
        entry({ id: 7, status: "unread" }),
        entry({ id: 8, status: "unread" }),
      ],
    };
    queryClient.setQueryData(queryKeys.entries(unreadQuery), seed);

    const { result } = renderHook(() => useUpdateEntryStatus(), {
      wrapper: Wrapper,
    });

    await act(async () => {
      await result.current.mutateAsync({ entryId: 7, status: "read" });
    });

    // The mutated row stays in the cached unread list — only its status flips.
    // This is what keeps the article visible in the Unread page after a click.
    const after = queryClient.getQueryData<EntriesResponse>(
      queryKeys.entries(unreadQuery),
    );
    expect(after?.entries.map((e) => e.id)).toEqual([7, 8]);
    expect(after?.entries.find((e) => e.id === 7)?.status).toBe("read");
  });

  it("useUpdateEntryStatus marks entries stale without refetching when marking read", async () => {
    const { queryClient, Wrapper } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

    const { result } = renderHook(() => useUpdateEntryStatus(), {
      wrapper: Wrapper,
    });

    await act(async () => {
      await result.current.mutateAsync({ entryId: 7, status: "read" });
    });

    await waitFor(() => {
      expect(updateEntries).toHaveBeenCalled();
    });

    type InvalidateOpts = { queryKey: readonly unknown[]; refetchType?: string };
    const invalidateCalls = invalidateSpy.mock.calls.map(
      (call) => call[0] as InvalidateOpts,
    );
    const entriesCall = invalidateCalls.find(
      (opts) => Array.isArray(opts.queryKey) && opts.queryKey[0] === "entries",
    );
    // Entries are marked stale (invalidated) but with refetchType:"none" so the
    // active list keeps its optimistic update during the current reading session.
    expect(entriesCall).toBeDefined();
    expect(entriesCall?.refetchType).toBe("none");
    // Counters and the per-entry cache should still invalidate normally.
    expect(
      invalidateCalls.some(
        (opts) => Array.isArray(opts.queryKey) && opts.queryKey[0] === "counters",
      ),
    ).toBe(true);
    expect(
      invalidateCalls.some(
        (opts) =>
          Array.isArray(opts.queryKey) &&
          opts.queryKey[0] === "entry" &&
          opts.queryKey[1] === 7,
      ),
    ).toBe(true);
  });

  it("useUpdateEntryStatus invalidates the entries list when marking unread so the Unread view picks up the article", async () => {
    const { queryClient, Wrapper } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

    const { result } = renderHook(() => useUpdateEntryStatus(), {
      wrapper: Wrapper,
    });

    await act(async () => {
      await result.current.mutateAsync({ entryId: 7, status: "unread" });
    });

    await waitFor(() => {
      expect(updateEntries).toHaveBeenCalled();
    });

    const invalidatedKeys = invalidateSpy.mock.calls.map(
      (call) => (call[0] as { queryKey: readonly unknown[] }).queryKey,
    );
    expect(
      invalidatedKeys.some((key) => Array.isArray(key) && key[0] === "entries"),
    ).toBe(true);
  });

  it("useMarkEntriesAsRead invalidates the entries list so 'Mark above as read' can refresh it", async () => {
    const { queryClient, Wrapper } = makeWrapper();
    const invalidateSpy = vi.spyOn(queryClient, "invalidateQueries");

    const { result } = renderHook(() => useMarkEntriesAsRead(), {
      wrapper: Wrapper,
    });

    await act(async () => {
      await result.current.mutateAsync({ entryIds: [7, 8] });
    });

    const invalidatedKeys = invalidateSpy.mock.calls.map(
      (call) => (call[0] as { queryKey: readonly unknown[] }).queryKey,
    );
    expect(
      invalidatedKeys.some((key) => Array.isArray(key) && key[0] === "entries"),
    ).toBe(true);
  });
});
