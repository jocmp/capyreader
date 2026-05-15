import type { MinifluxCredentials } from "@/auth/token-store";
import type {
  Category,
  EntriesQuery,
  EntriesResponse,
  Entry,
  EntryStatus,
  Feed,
  FeedCounters,
  User,
} from "@/api/types";

export class MinifluxError extends Error {
  readonly status: number;
  readonly body: unknown;
  constructor(status: number, message: string, body: unknown) {
    super(message);
    this.name = "MinifluxError";
    this.status = status;
    this.body = body;
  }
}

function buildQuery(params: Record<string, unknown> | undefined): string {
  if (!params) return "";
  const sp = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null) continue;
    if (Array.isArray(value)) {
      for (const item of value) sp.append(key, String(item));
    } else {
      sp.set(key, String(value));
    }
  }
  const str = sp.toString();
  return str ? `?${str}` : "";
}

async function request<T>(
  credentials: MinifluxCredentials,
  path: string,
  init: RequestInit = {},
  params?: Record<string, unknown>,
): Promise<T> {
  const url = `${credentials.baseUrl}${path}${buildQuery(params)}`;
  const headers = new Headers(init.headers);
  headers.set("X-Auth-Token", credentials.token);
  headers.set("Accept", "application/json");
  if (init.body !== undefined && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(url, { ...init, headers });

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  let body: unknown = null;
  if (text.length > 0) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }

  if (!response.ok) {
    const message =
      (body && typeof body === "object" && "error_message" in body
        ? String((body as { error_message: unknown }).error_message)
        : response.statusText) || `Miniflux request failed (${response.status})`;
    throw new MinifluxError(response.status, message, body);
  }

  return body as T;
}

export const minifluxApi = {
  me: (creds: MinifluxCredentials) => request<User>(creds, "/v1/me"),

  categories: (creds: MinifluxCredentials, counts = true) =>
    request<Category[]>(creds, "/v1/categories", undefined, {
      counts: counts ? "true" : undefined,
    }),

  feeds: (creds: MinifluxCredentials) => request<Feed[]>(creds, "/v1/feeds"),

  feedCounters: (creds: MinifluxCredentials) =>
    request<FeedCounters>(creds, "/v1/feeds/counters"),

  entries: (creds: MinifluxCredentials, query?: EntriesQuery) =>
    request<EntriesResponse>(creds, "/v1/entries", undefined, query as Record<string, unknown> | undefined),

  feedEntries: (creds: MinifluxCredentials, feedId: number, query?: EntriesQuery) =>
    request<EntriesResponse>(
      creds,
      `/v1/feeds/${feedId}/entries`,
      undefined,
      query as Record<string, unknown> | undefined,
    ),

  categoryEntries: (
    creds: MinifluxCredentials,
    categoryId: number,
    query?: EntriesQuery,
  ) =>
    request<EntriesResponse>(
      creds,
      `/v1/categories/${categoryId}/entries`,
      undefined,
      query as Record<string, unknown> | undefined,
    ),

  entry: (creds: MinifluxCredentials, entryId: number) =>
    request<Entry>(creds, `/v1/entries/${entryId}`),

  updateEntries: (
    creds: MinifluxCredentials,
    entryIds: number[],
    status: EntryStatus,
  ) =>
    request<void>(creds, "/v1/entries", {
      method: "PUT",
      body: JSON.stringify({ entry_ids: entryIds, status }),
    }),

  toggleBookmark: (creds: MinifluxCredentials, entryId: number) =>
    request<void>(creds, `/v1/entries/${entryId}/bookmark`, { method: "PUT" }),

  fetchContent: (creds: MinifluxCredentials, entryId: number) =>
    request<{ content: string }>(creds, `/v1/entries/${entryId}/fetch-content`),

  refreshFeed: (creds: MinifluxCredentials, feedId: number) =>
    request<void>(creds, `/v1/feeds/${feedId}/refresh`, { method: "PUT" }),

  refreshAllFeeds: (creds: MinifluxCredentials) =>
    request<void>(creds, "/v1/feeds/refresh", { method: "PUT" }),

  discoverFeeds: (creds: MinifluxCredentials, url: string) =>
    request<Array<{ title: string; url: string; type: string }>>(
      creds,
      "/v1/discover",
      {
        method: "POST",
        body: JSON.stringify({ url }),
      },
    ),

  createFeed: (
    creds: MinifluxCredentials,
    params: { feed_url: string; category_id: number },
  ) =>
    request<{ feed_id: number }>(creds, "/v1/feeds", {
      method: "POST",
      body: JSON.stringify(params),
    }),

  updateFeed: (
    creds: MinifluxCredentials,
    feedId: number,
    params: { category_id?: number; title?: string },
  ) =>
    request<Feed>(creds, `/v1/feeds/${feedId}`, {
      method: "PUT",
      body: JSON.stringify(params),
    }),

  deleteFeed: (creds: MinifluxCredentials, feedId: number) =>
    request<void>(creds, `/v1/feeds/${feedId}`, { method: "DELETE" }),

  createCategory: (creds: MinifluxCredentials, title: string) =>
    request<Category>(creds, "/v1/categories", {
      method: "POST",
      body: JSON.stringify({ title }),
    }),

  updateCategory: (creds: MinifluxCredentials, categoryId: number, title: string) =>
    request<Category>(creds, `/v1/categories/${categoryId}`, {
      method: "PUT",
      body: JSON.stringify({ title }),
    }),

  deleteCategory: (creds: MinifluxCredentials, categoryId: number) =>
    request<void>(creds, `/v1/categories/${categoryId}`, { method: "DELETE" }),

  importOpml: async (creds: MinifluxCredentials, xml: string) => {
    const response = await fetch(`${creds.baseUrl}/v1/import`, {
      method: "POST",
      headers: {
        "X-Auth-Token": creds.token,
        "Content-Type": "application/xml",
      },
      body: xml,
    });
    if (!response.ok) {
      const text = await response.text();
      throw new MinifluxError(response.status, text || "OPML import failed", text);
    }
  },

  exportOpml: async (creds: MinifluxCredentials) => {
    const response = await fetch(`${creds.baseUrl}/v1/export`, {
      headers: { "X-Auth-Token": creds.token },
    });
    if (!response.ok) {
      const text = await response.text();
      throw new MinifluxError(
        response.status,
        text || "OPML export failed",
        text,
      );
    }
    return response.text();
  },
};
