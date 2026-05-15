export interface User {
  id: number;
  username: string;
  is_admin: boolean;
  theme: string;
  language: string;
  timezone: string;
  entries_per_page: number;
  default_reading_speed?: number;
}

export interface Category {
  id: number;
  user_id: number;
  title: string;
  hide_globally?: boolean;
  feed_count?: number;
  total_unread?: number;
}

export interface Feed {
  id: number;
  user_id: number;
  feed_url: string;
  site_url: string;
  title: string;
  checked_at: string;
  category: Category;
  icon?: { feed_id: number; icon_id: number } | null;
  disabled?: boolean;
  unread_count?: number;
  read_count?: number;
}

export type EntryStatus = "unread" | "read" | "removed";

export interface Entry {
  id: number;
  user_id: number;
  feed_id: number;
  status: EntryStatus;
  hash: string;
  title: string;
  url: string;
  comments_url: string;
  published_at: string;
  created_at: string;
  content: string;
  author: string;
  starred: boolean;
  reading_time: number;
  feed: Feed;
  enclosures?: Array<{
    id: number;
    url: string;
    mime_type: string;
    size?: number;
  }> | null;
}

export interface IconData {
  id: number;
  data: string;
  mime_type: string;
}

export interface EntriesResponse {
  total: number;
  entries: Entry[];
}

export interface EntriesQuery {
  status?: EntryStatus | EntryStatus[];
  feed_id?: number;
  category_id?: number;
  starred?: boolean;
  search?: string;
  limit?: number;
  offset?: number;
  order?: "id" | "status" | "published_at" | "category_title" | "category_id";
  direction?: "asc" | "desc";
  before_entry_id?: number;
  after_entry_id?: number;
}

export interface FeedCounters {
  reads: Record<string, number>;
  unreads: Record<string, number>;
}
