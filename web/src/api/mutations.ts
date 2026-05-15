import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "@/auth/AuthContext";
import { minifluxApi } from "@/api/miniflux";
import { resolveMinifluxProxyUrls } from "@/api/resolveProxyUrls";
import type { EntriesResponse, Entry, EntryStatus } from "@/api/types";

function useCredentials() {
  const { credentials } = useAuth();
  if (!credentials) throw new Error("Mutation requires signed-in credentials");
  return credentials;
}

interface UpdateStatusVars {
  entryId: number;
  status: EntryStatus;
}

interface UpdateStatusSnapshot {
  entriesLists: Array<[readonly unknown[], EntriesResponse | undefined]>;
  entry: Entry | undefined;
}

export function useUpdateEntryStatus() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();

  return useMutation<UpdateStatusVars, Error, UpdateStatusVars, UpdateStatusSnapshot>({
    mutationFn: async ({ entryId, status }) => {
      await minifluxApi.updateEntries(credentials, [entryId], status);
      return { entryId, status };
    },
    onMutate: async ({ entryId, status }) => {
      await queryClient.cancelQueries({ queryKey: ["entries"] });
      await queryClient.cancelQueries({ queryKey: ["entry", entryId] });

      const entriesLists = queryClient.getQueriesData<EntriesResponse>({
        queryKey: ["entries"],
      });
      const entry = queryClient.getQueryData<Entry>(["entry", entryId]);

      queryClient.setQueriesData<EntriesResponse>(
        { queryKey: ["entries"] },
        (old) => {
          if (!old) return old;
          return {
            ...old,
            entries: old.entries.map((e) =>
              e.id === entryId ? { ...e, status } : e,
            ),
          };
        },
      );
      queryClient.setQueryData<Entry>(["entry", entryId], (old) =>
        old ? { ...old, status } : old,
      );

      return { entriesLists, entry };
    },
    onError: (_err, _vars, ctx) => {
      if (!ctx) return;
      for (const [key, data] of ctx.entriesLists) {
        queryClient.setQueryData(key, data);
      }
      if (ctx.entry) {
        queryClient.setQueryData(["entry", ctx.entry.id], ctx.entry);
      }
    },
    onSettled: (_data, _err, vars) => {
      // Intentionally not invalidating ["entries"]: the optimistic update has
      // already flipped this row's status in place, and we want it to stay
      // visible in the Unread list during the reading session. The list only
      // refreshes via "Mark above as read", the refresh button, or window
      // focus.
      queryClient.invalidateQueries({ queryKey: ["entry", vars.entryId] });
      queryClient.invalidateQueries({ queryKey: ["counters"] });
    },
  });
}

interface ToggleBookmarkVars {
  entryId: number;
  currentStarred: boolean;
}

interface ToggleBookmarkSnapshot {
  entriesLists: Array<[readonly unknown[], EntriesResponse | undefined]>;
  entry: Entry | undefined;
}

interface FetchFullContentVars {
  entryId: number;
}

export function useFetchFullContent() {
  const credentials = useCredentials();

  return useMutation<{ content: string }, Error, FetchFullContentVars>({
    mutationFn: async ({ entryId }) => {
      const result = await minifluxApi.fetchContent(credentials, entryId);
      return {
        ...result,
        content: resolveMinifluxProxyUrls(result.content, credentials.baseUrl),
      };
    },
  });
}

interface MarkEntriesReadVars {
  entryIds: number[];
}

interface MarkEntriesReadSnapshot {
  entriesLists: Array<[readonly unknown[], EntriesResponse | undefined]>;
  entries: Entry[];
}

export function useMarkEntriesAsRead() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();

  return useMutation<void, Error, MarkEntriesReadVars, MarkEntriesReadSnapshot>({
    mutationFn: async ({ entryIds }) => {
      if (entryIds.length === 0) return;
      await minifluxApi.updateEntries(credentials, entryIds, "read");
    },
    onMutate: async ({ entryIds }) => {
      if (entryIds.length === 0) {
        return { entriesLists: [], entries: [] };
      }
      const idSet = new Set(entryIds);

      await queryClient.cancelQueries({ queryKey: ["entries"] });
      await Promise.all(
        entryIds.map((id) =>
          queryClient.cancelQueries({ queryKey: ["entry", id] }),
        ),
      );

      const entriesLists = queryClient.getQueriesData<EntriesResponse>({
        queryKey: ["entries"],
      });
      const entries: Entry[] = [];
      for (const id of entryIds) {
        const e = queryClient.getQueryData<Entry>(["entry", id]);
        if (e) entries.push(e);
      }

      queryClient.setQueriesData<EntriesResponse>(
        { queryKey: ["entries"] },
        (old) => {
          if (!old) return old;
          return {
            ...old,
            entries: old.entries.map((e) =>
              idSet.has(e.id) ? { ...e, status: "read" as EntryStatus } : e,
            ),
          };
        },
      );
      for (const id of entryIds) {
        queryClient.setQueryData<Entry>(["entry", id], (old) =>
          old ? { ...old, status: "read" } : old,
        );
      }

      return { entriesLists, entries };
    },
    onError: (_err, _vars, ctx) => {
      if (!ctx) return;
      for (const [key, data] of ctx.entriesLists) {
        queryClient.setQueryData(key, data);
      }
      for (const entry of ctx.entries) {
        queryClient.setQueryData(["entry", entry.id], entry);
      }
    },
    onSettled: (_data, _err, vars) => {
      queryClient.invalidateQueries({ queryKey: ["entries"] });
      for (const id of vars.entryIds) {
        queryClient.invalidateQueries({ queryKey: ["entry", id] });
      }
      queryClient.invalidateQueries({ queryKey: ["counters"] });
    },
  });
}

export function useToggleBookmark() {
  const credentials = useCredentials();
  const queryClient = useQueryClient();

  return useMutation<void, Error, ToggleBookmarkVars, ToggleBookmarkSnapshot>({
    mutationFn: async ({ entryId }) => {
      await minifluxApi.toggleBookmark(credentials, entryId);
    },
    onMutate: async ({ entryId, currentStarred }) => {
      const nextStarred = !currentStarred;

      await queryClient.cancelQueries({ queryKey: ["entries"] });
      await queryClient.cancelQueries({ queryKey: ["entry", entryId] });

      const entriesLists = queryClient.getQueriesData<EntriesResponse>({
        queryKey: ["entries"],
      });
      const entry = queryClient.getQueryData<Entry>(["entry", entryId]);

      queryClient.setQueriesData<EntriesResponse>(
        { queryKey: ["entries"] },
        (old) => {
          if (!old) return old;
          return {
            ...old,
            entries: old.entries.map((e) =>
              e.id === entryId ? { ...e, starred: nextStarred } : e,
            ),
          };
        },
      );
      queryClient.setQueryData<Entry>(["entry", entryId], (old) =>
        old ? { ...old, starred: nextStarred } : old,
      );

      return { entriesLists, entry };
    },
    onError: (_err, _vars, ctx) => {
      if (!ctx) return;
      for (const [key, data] of ctx.entriesLists) {
        queryClient.setQueryData(key, data);
      }
      if (ctx.entry) {
        queryClient.setQueryData(["entry", ctx.entry.id], ctx.entry);
      }
    },
    onSettled: (_data, _err, vars) => {
      queryClient.invalidateQueries({ queryKey: ["entries"] });
      queryClient.invalidateQueries({ queryKey: ["entry", vars.entryId] });
      queryClient.invalidateQueries({ queryKey: ["counters"] });
    },
  });
}
