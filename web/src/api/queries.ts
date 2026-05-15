import { useQuery } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { minifluxApi } from "@/api/miniflux";
import { resolveMinifluxProxyUrls } from "@/api/resolveProxyUrls";
import type { EntriesQuery, EntriesResponse, Entry } from "@/api/types";

function useCredentials() {
  const { credentials } = useAuth();
  if (!credentials) {
    throw new Error("Authenticated queries require signed-in credentials");
  }
  return credentials;
}

export const queryKeys = {
  me: () => ["me"] as const,
  categories: () => ["categories"] as const,
  feeds: () => ["feeds"] as const,
  counters: () => ["counters"] as const,
  entries: (query: EntriesQuery | undefined) => ["entries", query ?? {}] as const,
  entry: (id: number) => ["entry", id] as const,
  icon: (iconId: number | "none") => ["icon", iconId] as const,
};

export function useMe() {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.me(),
    queryFn: () => minifluxApi.me(credentials),
  });
}

export function useCategories() {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.categories(),
    queryFn: () => minifluxApi.categories(credentials, true),
  });
}

export function useFeeds() {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.feeds(),
    queryFn: () => minifluxApi.feeds(credentials),
  });
}

export function useFeedCounters() {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.counters(),
    queryFn: () => minifluxApi.feedCounters(credentials),
  });
}

function rewriteEntryContent(entry: Entry, baseUrl: string): Entry {
  if (!entry.content) return entry;
  const resolved = resolveMinifluxProxyUrls(entry.content, baseUrl);
  if (resolved === entry.content) return entry;
  return { ...entry, content: resolved };
}

export function useEntries(query?: EntriesQuery, enabled = true) {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.entries(query),
    queryFn: async (): Promise<EntriesResponse> => {
      const result = await minifluxApi.entries(credentials, query);
      return {
        ...result,
        entries: result.entries.map((e) =>
          rewriteEntryContent(e, credentials.baseUrl),
        ),
      };
    },
    enabled,
  });
}

export function useFeedIcon(iconId: number | null | undefined) {
  const credentials = useCredentials();
  const enabled = typeof iconId === "number" && iconId > 0;
  return useQuery({
    // `"none"` keeps the disabled-lookup cache slot distinct from any real
    // icon id — including a hypothetical `0` — so we never read a hole as a
    // hit.
    queryKey: queryKeys.icon(enabled ? (iconId as number) : "none"),
    queryFn: () => minifluxApi.icon(credentials, iconId as number),
    enabled,
    // Favicons rarely change once Miniflux has cached them — keep the data
    // for the session but bound gcTime so a long-running tab with hundreds
    // of feeds doesn't accumulate megabytes of base64 in JS heap.
    staleTime: 24 * 60 * 60 * 1000,
    gcTime: 60 * 60 * 1000,
  });
}

export function useEntry(id: number, enabled = true) {
  const credentials = useCredentials();
  return useQuery({
    queryKey: queryKeys.entry(id),
    queryFn: async () => {
      const entry = await minifluxApi.entry(credentials, id);
      return rewriteEntryContent(entry, credentials.baseUrl);
    },
    enabled,
  });
}
